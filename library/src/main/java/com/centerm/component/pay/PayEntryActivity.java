package com.centerm.component.pay;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.centerm.component.pay.cont.EnumRespInfo;
import com.centerm.component.pay.cont.Keys;
import com.centerm.component.pay.mvp.presenter.IPayEntryPresenter;
import com.centerm.component.pay.mvp.view.IPayEntryView;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseActivity;
import com.centerm.epos.base.TradeFragmentContainer;
import com.centerm.epos.common.Settings;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.mvp.listener.StatusListener;
import com.centerm.epos.xml.bean.process.TradeProcess;
import com.wang.avi.AVLoadingIndicatorView;

import config.BusinessConfig;


/**
 * 收单组件入口
 * author:wanliang527</br>
 * date:2017/3/8</br>
 */
public class PayEntryActivity extends BaseActivity implements IPayEntryView {
    private IPayEntryPresenter presenter;

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        if (!environmentCheck())
            return;
        presenter = getPayEntryPresenter();
        presenter.initPresenter(this);
    }

    public IPayEntryPresenter getPayEntryPresenter() {
//        ConfigureManager config = getConfigureManager();
//        RedevelopItem redevelop = config.getRedevelopItem(getContext(), com.centerm.epos.xml.keys.Keys.obj()
// .redevelop_pay_entry_presenter);
//        String clzName = redevelop.getClassName();
//        try {
//            Class clz = Class.forName(clzName);
//            Object obj = clz.newInstance();
//            if (obj instanceof IPayEntryPresenter) {
//                return (IPayEntryPresenter) obj;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
        return (IPayEntryPresenter) ConfigureManager.getRedevelopAction(com.centerm.epos.xml.keys.Keys.obj()
                .redevelop_pay_entry_presenter, IPayEntryPresenter.class);
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_pay_entry;
    }

    @Override
    public void onInitView() {

        startAnim("正在开启安全支付通道", 0);
    }

    @Override
    public void afterInitView() {
        super.afterInitView();
        final Bundle extras = getIntent().getExtras();
        if (presenter == null)
            return;
        presenter.onPrepare(extras, new StatusListener<String>() {
            @Override
            public void onFinish(String[] result) {
                String respCode = result[0];
                if (EnumRespInfo.OK.getCode().equals(respCode)) {
                    //预处理成功，开始发起交易流程
                    presenter.onProcess(extras);
                } else if (EnumRespInfo.PAYMENT_OPERLOGIN.getCode().equals(respCode)){
                    //如果没有操作员登录， 要跳转到操作员登录界面
                    final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    activityStack.pop();
                }else {
                    //预处理失败，返回结果给上层应用
                    presenter.onFinish(result);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        logger.warn("返回码：" + resultCode);
        Bundle paymentResp;
        if (requestCode == REQ_JBOSS_PAYMENT) {
            paymentResp = data == null ? null : presenter.mapJBossRespDatas(data.getExtras());
        } else
            paymentResp = data == null ? null : data.getExtras();
        switch (resultCode) {
            case RESULT_OK:
                if (paymentResp != null && "00".equals(paymentResp.getString(Keys.obj().resp_code))) {
                    if (presenter.postProcess())
                        break;
                }
                finish(true, paymentResp == null ? null : paymentResp);
                break;
            default:
            case RESULT_CANCELED:
                if (paymentResp != null && TextUtils.isEmpty(paymentResp.getString(Keys.obj().resp_msg)))
                    paymentResp.putString(Keys.obj().resp_msg, "用户取消");
                finish(false, paymentResp == null ? null : paymentResp);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        stopAnim();
        super.onDestroy();
    }

    @Override
    public void startAnim(String tip, int drawableId) {
        View loadBlock = findViewById(R.id.load_block);
        if (loadBlock != null) {
            loadBlock.setVisibility(View.VISIBLE);
        }
        AVLoadingIndicatorView view = (AVLoadingIndicatorView) findViewById(R.id.loading_view);
        if (view != null) {
            view.show();
        }
    }

    @Override
    public void stopAnim() {
        View loadBlock = findViewById(R.id.load_block);
        if (loadBlock != null) {
            loadBlock.setVisibility(View.INVISIBLE);
        }
        AVLoadingIndicatorView view = (AVLoadingIndicatorView) findViewById(R.id.loading_view);
        if (view != null) {
            view.hide();
        }
    }

    @Override
    public void finish(boolean resultOk, Bundle extras) {
        int resultCode = resultOk ? RESULT_OK : RESULT_CANCELED;
        Intent intent = new Intent();
        if (extras != null) {
            intent.putExtras(extras);
        }
        setResult(resultCode, intent);
//        finish();
        activityStack.pop();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void jumpToTrade(String transCode, TradeProcess process, Bundle bundle) {
        Intent intent = new Intent(getContext(), TradeFragmentContainer.class);
        intent.putExtra(BaseActivity.KEY_TRANSCODE, transCode);
        intent.putExtra(BaseActivity.KEY_PROCESS, process);
        intent.putExtra(BaseActivity.KEY_OUT_BUNDLE, bundle);
        startActivityForResult(intent, REQ_TRANSACTION);
        stopAnim();

    }

    @Override
    public void jumpToThirdPayment(Intent jBossPaymentIntent, Bundle extras) {
        startActivityForResult(jBossPaymentIntent, REQ_JBOSS_PAYMENT);
        stopAnim();
    }

    @Override
    public void jumpToLocalFunction(Intent funcIntent, Bundle extras) {
        startActivityForResult(funcIntent, REQ_LOCAL_FUNCTION);
        stopAnim();
    }

    /**
     * 检查支付环境，是否已经初始化，是否已经签到过。
     */
    public boolean environmentCheck() {
        Keys k = Keys.obj();
        Bundle bundle = new Bundle();
        boolean initFlag = Settings.isAppInit(this);
        if (!initFlag) {
            bundle.putString(k.resp_code, "E99");
            bundle.putString(k.resp_msg, "支付组件未初始化");
            finish(false, bundle);
            return false;
        }

        boolean check_merchant_terminal_is_null = ConfigureManager.getInstance(getContext()).isOptionFuncEnable
                (getContext(), com.centerm.epos.xml.keys.Keys.obj().check_merchant_terminal_is_null);

        if (check_merchant_terminal_is_null) {
            if (TextUtils.isEmpty(BusinessConfig.getInstance().getIsoField(context, 41)) ||
                    TextUtils.isEmpty(BusinessConfig.getInstance().getIsoField(context, 42))) {
                bundle.putString(k.resp_code, "E98");
                bundle.putString(k.resp_msg, "商户号和终端号未设置");
                finish(false, bundle);
                return false;
            }
        }
        return true;
    }
}
