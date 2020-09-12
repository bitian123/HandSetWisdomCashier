package com.centerm.component.pay.mvp.presenter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.centerm.component.pay.cont.EnumRespInfo;
import com.centerm.component.pay.cont.JPayComponent;
import com.centerm.component.pay.cont.Keys;
import com.centerm.component.pay.cont.TransCode;
import com.centerm.component.pay.mvp.model.IPayEntryModel;
import com.centerm.component.pay.mvp.model.PayEntryModel;
import com.centerm.component.pay.mvp.view.IPayEntryView;
import com.centerm.epos.activity.msn.BaseQueryTradeActivity;
import com.centerm.epos.activity.msn.LastSlipPrintActivity;
import com.centerm.epos.common.Settings;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.db.CommonManager;
import com.centerm.epos.mvp.listener.StatusListener;
import com.centerm.epos.redevelop.ICommonManager;
import com.centerm.epos.task.AsyncBatchSettleDown;
import com.centerm.epos.transcation.pos.controller.AutoSignInController;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.view.AlertDialog;
import com.centerm.epos.xml.bean.process.TradeProcess;

import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.Locale;

import config.BusinessConfig;


/**
 * author:wanliang527</br>
 * date:2017/3/8</br>
 */
public class PayEntryPresenter implements IPayEntryPresenter {

    private Logger logger = Logger.getLogger(getClass());

    private IPayEntryView view;
    private IPayEntryModel model;
    private String mReverseTransCode;
    private Bundle mBundle;

    public PayEntryPresenter() {}

    @Override
    public void initPresenter(IPayEntryView view) {
        this.view = view;
        model = new PayEntryModel();
    }

    @Override
    public void onPrepare(Bundle bundle, final StatusListener<String> listener) {
        new AsyncTask<Bundle, Integer, String[]>() {

            @Override
            protected String[] doInBackground(Bundle... params) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                adapterParameter(params[0]);
                String[] result = model.onCheckParams(params[0]);
                if (model.isResultOk(result[0])) {
                    result = onCheckStatus(params[0]);
                }
                return result;
            }

            private void adapterParameter(Bundle param) {
                if (param == null)
                    return;
                String amountStr = param.getString(Keys.obj().trans_amt);
                if (TextUtils.isEmpty(amountStr))
                    return;
                double transAmt = Double.valueOf(amountStr);
                param.putDouble(Keys.obj().trans_amt, transAmt);
            }

            @Override
            protected void onPostExecute(String[] strings) {
                listener.onFinish(strings);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, bundle);
    }

    private String[] onCheckStatus(Bundle param) {
        //// TODO: 2017/3/9 这里需要进行交易前置条件判断，例如操作员登录状态、签到状态、下载密钥状态
        //业务方式有两种：
        //(1)第三方应用真正开始交易前，强制要求其进入到收单应用中进行签到或者下载密钥
        //(2)第三方应用在调用交易时，组件自动完成签到和下载密钥等操作，完成后继续第三方的支付请求
        String[] retStr = environmentCheck();
        if (retStr == null) {
            retStr = new String[]{EnumRespInfo.OK.getCode(),EnumRespInfo.OK.getMsg()};
        }
        return retStr;
    }

    /*@Override
    public Bundle newBundle(String respCode, String respMsg) {
        return model.newBundle(respCode, respMsg);
    }*/

    @Override
    public void onProcess(Bundle extras) {
        Keys k = Keys.obj();
        String transCodeFromInvoke = extras.getString(k.trans_code);
        if (TextUtils.isEmpty(transCodeFromInvoke)) {
            logger.warn("【支付组件】交易失败==>交易码参数不能为空");
            onFinish(EnumRespInfo.TRANSCODE_NULL_ERROR);
            return;
        }
        //微信和支付宝调用其它支付渠道(杰博实)
        if (TransCode.obj().WX_PAY_SCAN.equals(transCodeFromInvoke) || TransCode.obj().ALI_PAY_SCAN.equals
                (transCodeFromInvoke)) {
            Intent thirdPayment = getJBossPaymentParames(extras);
            if (thirdPayment == null) {
                logger.warn("【支付组件】交易失败==>创建杰博实汇报支付渠道失败");
                onFinish(EnumRespInfo.PAYMENT_CREATE_ERROR);
            }
            view.jumpToThirdPayment(thirdPayment, extras);
            return;
        }

        //本地功能
        if (transCodeFromInvoke.startsWith("F")){
            Intent localFunIntent = getLocalFunctionIntent(transCodeFromInvoke);
            if (localFunIntent == null){
                logger.warn("【支付组件】交易失败==>跳转本地功能失败");
                onFinish(EnumRespInfo.PAYMENT_CREATE_ERROR);
            }
            view.jumpToLocalFunction(localFunIntent, extras);
            return;
        }

        String codeInvoke = extras.getString(Keys.obj().trans_code);
        if (TransCode.obj().SETTLE.equals(codeInvoke)){
            readyToSettleDown(view.getContext(), extras);
            return;
        }
        if (!TransCode.obj().SIGN_IN.equals(codeInvoke) && AutoSignInController.isNeedAutoSignIn()){
            mReverseTransCode = codeInvoke;
            mBundle = extras;
            extras.putString(Keys.obj().trans_code, TransCode.obj().SIGN_IN);
            beginTransaction(extras);
            return;
        }
        beginTransaction(extras);
    }

    /**
     * 后置处理，自动签到后恢复之前点击的业务
     * @return true正在恢复业务处理  false无待恢复的业务
     */
    @Override
    public boolean postProcess(){
        if (!TextUtils.isEmpty(mReverseTransCode) && mBundle != null){
            mBundle.putString(Keys.obj().trans_code, mReverseTransCode);
            mReverseTransCode = null;
            beginTransaction(mBundle);
            return true;
        }
        return false;
    }

    private void beginTransaction(Bundle extras) {
        String codeInvoke = extras.getString(Keys.obj().trans_code);
        //交易码映射
        String transCode = model.transCodeMapping(codeInvoke);
        if (TextUtils.isEmpty(transCode)) {
            logger.warn("【支付组件】交易失败==>交易码：" + codeInvoke + "==>映射失败");
            onFinish(EnumRespInfo.MAPPING_ERROR);
            return;
        }
        String processFile = model.processFileMapping(codeInvoke);
        ConfigureManager config = ConfigureManager.getInstance(view.getContext());
        //交易流程映射
        TradeProcess process = config.getTradeProcess(view.getContext(), processFile);
        if (process == null) {
            logger.warn("【支付组件】交易失败==>交易码：" + extras.getString(codeInvoke) + "，未找到对应的流程文件");
            onFinish(EnumRespInfo.PROCESS_UNDEFINED);
        } else {
            //开始交易
            translateControlFlag(extras);
            view.jumpToTrade(transCode, process, extras);
        }
    }

    private Intent getLocalFunctionIntent(String transCodeFromInvoke) {
        if (TextUtils.isEmpty(transCodeFromInvoke))
            return null;

        Class cla = null;
        if (TransCode.obj().REPRINT_SLIP.equals(transCodeFromInvoke)){
            cla = BaseQueryTradeActivity.class;
        }else if (TransCode.obj().REPRINT_LAST_SLIP.equals(transCodeFromInvoke)){
            cla = LastSlipPrintActivity.class;
        }

        if (cla == null)
            return null;

        return new Intent(view.getContext(), cla);
    }

    /**
     * 按位控制的控制信息，转化为名称控制的信息数据
     * @param extras
     */
    private void translateControlFlag(Bundle extras) {
        if (null == extras)
            return;
        Keys k = Keys.obj();
        Bundle controlBundle = new Bundle();
        extras.putBundle(k.control_bundle, controlBundle);
        String controlInfo = extras.getString(k.control_info);
        if (TextUtils.isEmpty(controlInfo)) {
            controlBundle.putBoolean(k.input_money_view, true);
            controlBundle.putBoolean(k.signature_view, false);
            controlBundle.putBoolean(k.result_view, true);
//            controlBundle.putByte(k.print_pages, (byte) 2);
        }else {
            controlBundle.putBoolean(k.input_money_view, '1' == controlInfo.charAt(0));
            controlBundle.putBoolean(k.result_view, '0' == controlInfo.charAt(1));
            controlBundle.putBoolean(k.signature_view, '0' == controlInfo.charAt(2));
//            controlBundle.putByte(k.print_pages, (byte) 2);
        }
    }

    @Override
    public void onFinish(String[] result) {
        Bundle extras = model.newBundle(result[0], result[1]);
        view.finish(model.isResultOk(result[0]), extras);
    }

    @Override
    public Bundle mapJBossRespDatas(Bundle jBossBundle) {
        if (jBossBundle == null)
            return null;
        Bundle bundle = new Bundle();
        bundle.putString(Keys.obj().resp_code, jBossBundle.getString(JPayComponent.resp_code));
        bundle.putString(Keys.obj().resp_msg, jBossBundle.getString(JPayComponent.resp_msg));
        bundle.putString(Keys.obj().trans_time, jBossBundle.getString(JPayComponent.TRANS_TIME));
        bundle.putString(Keys.obj().order_no, jBossBundle.getString(JPayComponent.ORDER_NO));
        return bundle;
    }

    public Intent getJBossPaymentParames(Bundle extras) {
        Intent intent = new Intent();
        String packageName = "com.centerm.cpay.payment.onlyqr";
        String activityName = "com.centerm.cpay.paycomponent.EntryActivity";
        ComponentName comp = new ComponentName(packageName, activityName);
        intent.setComponent(comp);
        int payChannel = model.transCodeMappingJBoss(extras.getString(Keys.obj().trans_code));
        if (payChannel < 0)
            return null;
        Double amount = extras.getDouble(Keys.obj().trans_amt);
        if (amount == 0)
            return null;
        intent.putExtras(createBundle(payChannel, String.format(Locale.CHINA, "%.2f", amount)));
        return intent;
    }

    private Bundle createBundle(int payChannel, String amountStr) {
        Bundle data = new Bundle();
        data.putInt(JPayComponent.trans_tp, payChannel);
        data.putString(JPayComponent.trans_code, "T00001");
        data.putString(JPayComponent.trans_amt, amountStr);
        data.putString(JPayComponent.caller_id, view.getContext().getPackageName());
        data.putString(JPayComponent.caller_secret, view.getContext().getPackageName());
//        data.putString(JPayComponent.goods_abs, "商品信息摘要");
//        data.putString(JPayComponent.goods_detail, "鸡腿X1，鸡翅膀X19，鸡屁股X3，鸡胸肉X9");
//        data.putString(JPayComponent.goods_tag, "jlsjdf");
//        data.putString(JPayComponent.attach, "附加数据");
        data.putString(JPayComponent.control_info, getTradeControlInfo());
        data.putInt(JPayComponent.print_pages, 2);  //打印几联
//        if(payChannel == 21 || payChannel == 22)
//        {
//            data.putString(JPayComponent.discountable_amount, alipayDiscountAmt.getText().toString());
//            data.putString(JPayComponent.undiscountable_amount, alipayUnDiscountAmt.getText().toString());
//        }
        return data;
    }

    private String getTradeControlInfo() {
        String tempControlInfo = "";
        StringBuilder sb = new StringBuilder();
        sb.append("0"); //0不在组件内输入金额，1在组件内输入金额
        sb.append("1"); //0需要组件内显示结果业，1不需要
        sb.append("1"); //0需要电子签名，1不需要
        sb.append("00000");
        tempControlInfo = sb.toString();
        return tempControlInfo;
    }

    public void onFinish(EnumRespInfo resp) {
        this.onFinish(new String[]{resp.getCode(), resp.getMsg()});
    }

    /**
     * 检查支付环境，是否已经初始化，是否已经签到过。
     */
    public String[] environmentCheck() {
        boolean initFlag = Settings.isAppInit(view.getContext());
        if (!initFlag) {
            return new String[]{"E99","支付组件未初始化"};
        }
        if(BusinessConfig.getInstance().getFlag(view.getContext(), BusinessConfig.Key.FLAG_TRADE_STORAGE_WARNING)){
            return new String[]{"E98","交易流水存储已满，请先进行批结算"};
        }
        return null;
    }

    /**
     * 批结算
     */
    private void readyToSettleDown(final Context contextActivity, final Bundle extras) {
        String isBatch = Settings.getValue(contextActivity, Settings.KEY.BATCH_SEND_STATUS, "0");
        boolean isSignIn = BusinessConfig.getInstance().getFlag(contextActivity, BusinessConfig.Key
                .FLAG_SIGN_IN);

        if (isSignIn) {
            if ("2".equals(isBatch)) {
                ViewUtils.showToast(contextActivity, contextActivity.getString(com.centerm.epos.R.string
                        .tip_batch_over_please_sign_out));
                onFinish(EnumRespInfo.PAYMENT_ERROR);
            } else {
                new AsyncBatchSettleDown(contextActivity) {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        DialogFactory.showLoadingDialog(contextActivity, contextActivity.getString(com.centerm.epos.R
                                .string
                                .tip_query_flow));
                    }

                    @Override
                    public void onFinish(Object o) {
                        super.onFinish(o);
                        if (o instanceof Boolean && (Boolean) o) {
                            DialogFactory.showSelectDialog(contextActivity, contextActivity.getString(com.centerm
                                    .epos.R.string
                                    .tip_notification), contextActivity.getString(com.centerm.epos.R.string
                                    .tip_comfirm_batch), new AlertDialog.ButtonClickListener() {
                                @Override
                                public void onClick(AlertDialog.ButtonType button, View v) {
                                    switch (button) {
                                        case POSITIVE:
                                            PayEntryPresenter.this.beginTransaction(extras);
                                            break;
                                        case NEGATIVE:
                                            PayEntryPresenter.this.onFinish(EnumRespInfo.PAYMENT_CANCEL);
                                            break;
                                    }
                                }
                            });
                        } else {
                            DialogFactory.hideAll();
                            ViewUtils.showToast(contextActivity, contextActivity.getString(com.centerm.epos.R.string
                                    .tip_no_trans_flow));
                            PayEntryPresenter.this.onFinish(EnumRespInfo.PAYMENT_ERROR);
                        }
                    }
                }.execute();
            }
        } else {
            ViewUtils.showToast(contextActivity, contextActivity.getString(com.centerm.epos.R.string
                    .tip_no_sign_in_status));
            onFinish(EnumRespInfo.PAYMENT_ERROR);
        }
    }

}
