package com.centerm.epos.base;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.centerm.component.pay.cont.Keys;
import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.define.IPbocService;
import com.centerm.cpay.midsdk.dev.define.pboc.EmvTag;
import com.centerm.cpay.midsdk.dev.define.pboc.EnumPbocFlow;
import com.centerm.cpay.midsdk.dev.define.pboc.EnumPbocResultType;
import com.centerm.cpay.midsdk.dev.define.pboc.EnumPbocSlot;
import com.centerm.cpay.midsdk.dev.define.pboc.EnumTransType;
import com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction;
import com.centerm.cpay.midsdk.dev.define.pboc.TransParams;
import com.centerm.epos.EposApplication;
import com.centerm.epos.R;
import com.centerm.epos.bean.QpsBinData;
import com.centerm.epos.bean.QpsBlackBinData;
import com.centerm.epos.bean.TradeInfo;
import com.centerm.epos.bean.iso.Iso62Qps;
import com.centerm.epos.bean.transcation.TradeInformation;
import com.centerm.epos.common.StatusCode;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.fragment.trade.InputMoneyFragment;
import com.centerm.epos.fragment.trade.InputPwdFragment;
import com.centerm.epos.fragment.trade.ResultFragment;
import com.centerm.epos.fragment.trade.SignatureFragment;
import com.centerm.epos.fragment.trade.TradingFragment;
import com.centerm.epos.model.ITradeModel;
import com.centerm.epos.model.ITradeParameter;
import com.centerm.epos.model.TradeModelImpl;
import com.centerm.epos.redevelop.CustomPbocTranType;
import com.centerm.epos.redevelop.IOtherTransDatasInit;
import com.centerm.epos.redevelop.IPbocTranType;
import com.centerm.epos.redevelop.OtherTransDatasInit;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.transcation.pos.controller.ITradeUIController;
import com.centerm.epos.transcation.pos.controller.ProcessRequestManager;
import com.centerm.epos.transcation.pos.manager.UploadESignatureTradeChecker;
import com.centerm.epos.transcation.pos.manager.UploadOfflineTradeChecker;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DataHelper;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.StopWatch;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.utils.XLogUtil;
import com.centerm.epos.view.AlertDialog;
import com.centerm.epos.xml.bean.menu.Menu;
import com.centerm.epos.xml.bean.process.ComponentNode;
import com.centerm.epos.xml.bean.process.TradeProcess;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import config.BusinessConfig;
import config.Config;

import static com.centerm.cpay.midsdk.dev.define.pboc.EmvTag.EMVTAG_AID;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.ACTION_ERROR;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.ACTION_NEED_CHANGE_READ_CARD_TYPE;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.ACTION_REQUEST_AID_SELECT;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.ACTION_REQUEST_AMOUNT;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.ACTION_REQUEST_CARD_INFO_CONFIRM;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.ACTION_REQUEST_EC_TIPS_CONFIRM;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.ACTION_REQUEST_ONLINE;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.ACTION_REQUEST_PIN;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.ACTION_REQUEST_TIPS_CONFIRM;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.ACTION_REQUEST_USER_AUTH;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.ACTION_RETURN_CARD_LOAD_LOG;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.ACTION_RETURN_CARD_OFFLINE_BALANCE;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.ACTION_RETURN_CARD_TRANS_LOG;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.ACTION_TRADE_APPROVED;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.ACTION_TRADE_REFUSED;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.ACTION_TRADE_TERMINATED;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.KEY_FIRST_EC_BALANCE;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.KEY_FIRST_EC_CODE;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.KEY_LOAD_LOG;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.KEY_SECOND_EC_BALANCE;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.KEY_SECOND_EC_CODE;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.KEY_TIPS;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.KEY_TRANS_LOG;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.getAllActions;
import static com.centerm.epos.common.TransCode.BALANCE;
import static com.centerm.epos.common.TransCode.SALE;
import static com.centerm.epos.common.TransCode.TRANS_CARD_DETAIL;
import static com.centerm.epos.common.TransCode.UNION_INTEGRAL_BALANCE;
import static com.centerm.epos.common.TransDataKey.FLAG_AUTO_SIGN;
import static com.centerm.epos.common.TransDataKey.FLAG_IMPORT_AMOUNT;
import static com.centerm.epos.common.TransDataKey.FLAG_REQUEST_ONLINE;
import static com.centerm.epos.common.TransDataKey.KEY_IC_DATA_PRINT;
import static com.centerm.epos.common.TransDataKey.iso_f39;

/**
 * 交易类界面的父类
 * author:wanliang527</br>
 * date:2016/10/22</br>
 */
public class TradeFragmentContainer extends BaseFragmentActivity {

    public static final String PRESENT_CLASS_NAME = "present class name";
    private TransParams pbocParams;//PBOC参数
    private Iso62Qps qpsParams;//小额免密免签参数
    private String transCode;//交易码
    private TradeProcess tradeProcess;//交易流程
    private Map<String, Object> responseDataMap;//交易数据集合
    private Map<String, String> tempMap;//临时数据集合
    private Map<String, Object> transDatas;//业务数据
    private PbocEventReceiver receiver;//PBOC广播接收
    private IPbocService pbocService;//PBOC服务
    private boolean clssForcePin = false;//闪付凭密标识

    private boolean gotTerminatedEvent = false;
    private View titleView;
    private Bundle mInvokerParams; //组件调用，传进来的参数
    private ITradeModel mTradeModel;

    protected long tradePageTimeout = Config.PAGE_TIMEOUT;
    //    protected GlobalTouchListener tradeTouchListener;
    protected StopWatch tradeStopWatch;
    protected StopWatch.TimeoutHandler tradeTimeoutHandler;

//    public Map<String, String> tradeInfo;//调用者传入的交易信息

  /*  public static void nullQpsParams() {
        qpsParams = null;
    }*/

    //    FragmentManager mFragmentManager;
    public final TradeInformation mTradeInformation = new TradeInformation();


    @Override
    public int onLayoutId() {
        return R.layout.activity_trade_fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommonUtils.disableStatusBar(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, getAllActions());
//        ViewUtils.disableStatusBar(context);
    }

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        mTradeModel = TradeModelImpl.getInstance();
        if (pbocParams == null) {
            pbocParams = new TransParams(EnumPbocSlot.SLOT_IC, EnumPbocFlow.PBOC_FLOW);
            mTradeInformation.setPbocParams(pbocParams);
        }
        //不支持电子现金
        pbocParams.setSupportEc(false);
        Bundle innerInvokerParams = getIntent().getBundleExtra(ITradeParameter.KEY_TRANS_PARAM);
        if (innerInvokerParams != null) {
            mTradeModel.setTradeParam(innerInvokerParams);
        }
        initTradeProcess();
        initTradeInfomation();
        mInvokerParams = getIntent().getBundleExtra(BaseActivity.KEY_OUT_BUNDLE);
        if (mInvokerParams != null) //不为空，说明是以支付组件的形式调用
        {
            TradeModelImpl.getInstance(null).createBundleOfResult();
            TradeModelImpl.getInstance(null).setResultCode(Activity.RESULT_CANCELED);
        }

        pageTimeout = BusinessConfig.getInstance().getNumber(this, BusinessConfig.Key.KEY_TRADE_VIEW_OP_TIMEOUT) * 1000;
    }

    private void initTradeInfomation() {
        if (transDatas == null)
            return;
        if ((TransCode.SALE.equals(transCode) || TransCode.AUTH.equals(transCode)) && qpsParams == null) {
            //初始化QPS参数
            mTradeModel.getQpsParams();
        }
        transDatas.put(TradeInformationTag.TRANSACTION_TYPE, transCode);
        if (mTradeModel.getTradeParam() != null) {
            ArrayList<String> msgTags = mTradeModel.getTradeParam().getStringArrayList(ITradeParameter.KEY_MSG_TAGS);
            if (msgTags != null && msgTags.size() > 0)
                transDatas.put(ITradeParameter.KEY_MSG_TAGS, msgTags);
        }
        IOtherTransDatasInit otherTransDatasInit = (IOtherTransDatasInit) ConfigureManager.getSubPrjClassInstance(new
                OtherTransDatasInit());
        otherTransDatasInit.init(transDatas);
    }

    @Override
    public void onInitView() {
        jumpToNext();
    }

    private final void _initView() {
        View v = findViewById(R.id.imgbtn_back);
        if (v != null)
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
    }

    @Override
    public void onBackPressed() {
        if (CommonUtils.isFastClick()) {
            logger.debug("==>重复的onBackPressed事件，不响应！");
            return;
        }
        //回调操作界面的返回处理, fragment可先行处理
        ITradeView tradeView = (BaseTradeFragment) getShowingFragment();
        if (tradeView != null) {
            if (tradeView.onBackPressed())
                return;
        }
        // TODO: 2017/2/22 返回上一个界面
//        finish();
        activityStack.pop();
    }

    /**
     * 初始化交易节点
     */
    private void initTradeProcess() {
        Intent intent = getIntent();
        String action = intent.getAction();
        transCode = intent.getStringExtra(KEY_TRANSCODE);
        tradeProcess = intent.getParcelableExtra(KEY_PROCESS);
        transCode = translateTransCode(transCode);
        if (tradeProcess == null) {
            logger.warn("TradeProcess is null");
            throw new IllegalStateException("TradeProcess is null! Please check!");
        }
        //菜单配置文件中定义的交易码优先级高于流程定义中的交易码
        if (TextUtils.isEmpty(transCode)) {
            transCode = tradeProcess.getTransCode();
        } else {
            tradeProcess.setTransCode(transCode);
        }
        if (TextUtils.isEmpty(transCode)) {
            throw new IllegalStateException("TransCode is empty! Please check!");
        }

        mTradeInformation.setTransCode(transCode);
        mTradeInformation.setTradeProcess(tradeProcess);

//        dataMap = tradeProcess.getDataMap();
        tempMap = tradeProcess.getTempMap();
        transDatas = tradeProcess.getTransDatas();

//        mTradeInformation.setDataMap(dataMap);
        mTradeInformation.setTempMap(tempMap);
        mTradeInformation.setTransDatas(transDatas);

        for (ComponentNode mComponentNode : tradeProcess.getComponentNodeList()) {
            if (mComponentNode.getComponentName().equals(action)) {
                tradeProcess.setCurNode(mComponentNode, true);
            }
        }
        receiver = new PbocEventReceiver();
    }

    private String translateTransCode(String transCode) {
        if (TextUtils.isEmpty(transCode))
            return transCode;
        if (TransCode.SALE_NEED_PIN.equals(transCode)) {
            clssForcePin = true;
            mTradeInformation.setForcePin(true);
            return TransCode.SALE;
        }
        if (TransCode.SALE_INSERT.equals(transCode)) {
            mTradeInformation.setForceInsert(true);
            return TransCode.SALE;
        }
        if (TransCode.AUTH_NEED_PIN.equals(transCode)) {
            clssForcePin = true;
            mTradeInformation.setForcePin(true);
            return TransCode.AUTH;
        }
        mTradeInformation.setPreferClss(BusinessConfig.getInstance().getFlag(context, BusinessConfig.Key
                .FLAG_PREFER_CLSS));
        return transCode;
    }

    @Override
    protected void onStart() {
        super.onStart();
//        switchCommandUI(new CheckCardFragment());
    }

    @Override
    public void afterInitView() {
        super.afterInitView();
        int titleId = TransCode.codeMapName(transCode);
        if (titleId == R.string.unknown) {
            titleId = R.string.title_result;
        }
        //隐藏标题栏，交易流程相关的Fragment使用自定义的标题栏，且自行控制
        setTopViewType(Menu.TopViewType.NONE);
        setTitle(titleId);

    }

    @Override
    protected void onPause() {
        super.onPause();
        ViewUtils.enableStatusBar(context);
        if (receiver != null) {
            try {
                LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void finish() {
        if (mInvokerParams != null) {
            ITradeModel model = TradeModelImpl.getInstance(null);
            Intent intent = new Intent();
            intent.putExtras(getDataToActivityResult());
            int resultCode = TextUtils.isEmpty(tempMap.get(iso_f39)) ? Activity.RESULT_CANCELED : Activity.RESULT_OK;
            setResult(resultCode, intent);
        }
        TradeModelImpl.release();
        super.finish();
    }

    public Bundle getDataToActivityResult() {
        Bundle bundle = new Bundle();
        String f39 = tempMap.get(iso_f39);
        boolean isSuccess = "00".equals(f39) || "0".equals(f39) || "10".equals(f39) || "11".equals(f39) || "A2"
                .equals(f39)
                || "A4".equals(f39) || "A5".equals(f39) || "A6".equals(f39);
        bundle.putString(Keys.obj().resp_code, isSuccess ? "00" : tempMap.get(TransDataKey
                .key_resp_code));
        bundle.putString(Keys.obj().resp_msg, tempMap.get(TransDataKey.key_resp_msg));
        if (isSuccess) {
            bundle.putString(Keys.obj().order_no, tempMap.get(TradeInformationTag
                    .SCAN_VOUCHER_NO));
            //交易时间
            bundle.putString(Keys.obj().trans_time, tempMap.get(TradeInformationTag.TRANS_DATE));
            //平台流水号，即终端流水号
            bundle.putString(Keys.obj().plat_trans_no, (String) transDatas.get(TradeInformationTag.TRACE_NUMBER));
            //检索参考号
            bundle.putString(Keys.obj().retri_ref_no, tempMap.get(TradeInformationTag.REFERENCE_NUMBER));
        }
        return bundle;
    }

    @Override
    protected void onDestroy() {
        if (pbocService != null)
            pbocService.abortProcess();
        DialogFactory.hideAll();
        CommonUtils.enableStatusBar(this);
        /*BUGID:0003158 扫码撤销结果界面
        *点击返回出现异常奔溃
        * */
        clearPageTimeout();
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && tradeStopWatch != null) {
            tradeStopWatch.reset();
        }
        return super.onTouchEvent(event);
    }

    public void openPageTimeout(int timeOutS, final String tips) {
        if(Config.isEnableShowingTimeout){
            return;
        }
        clearPageTimeout();
        tradeStopWatch = new StopWatch(context, timeOutS == 0 ? this.pageTimeout : timeOutS * 1000);
        tradeTimeoutHandler = new StopWatch.TimeoutHandler() {
            @Override
            public void onTimeout() {
                tradeStopWatch.stop();//停止计时任务
                if (context == null) {
                    logger.error("^_^ activity 已经销毁，还进行超时处理！ ^_^");
                    return;
                }
                AlertDialog dialog = DialogFactory.showSelectDialog(context, "提示", TextUtils.isEmpty(tips) ?
                        "长时间未操作\n是否返回主界面" : tips, new AlertDialog.ButtonClickListener() {
                    @Override
                    public void onClick(AlertDialog.ButtonType button, View v) {
                        switch (button) {
                            case POSITIVE:
                                jumpToNext("999");
                                clearPageTimeout();
                                break;
                            case NEGATIVE:
                                tradeStopWatch.start();//重新开始计时任务
                                break;
                        }
                    }
                }, 30);
                dialog.setAutoPerformPositive(true);
            }
        };
        tradeStopWatch.setTimeoutHandler(tradeTimeoutHandler);
        tradeStopWatch.start();
    }

    public void clearPageTimeout() {
        if (tradeStopWatch != null) {
            tradeStopWatch.stop();
            tradeStopWatch = null;
        }
    }

    /**
     * 跳转到下一个交易流程，条件ID为1
     */
    public void jumpToNext() {
        jumpToNext("1");
    }

    public void jumpToMain() {
        activityStack.pop();
    }

    @Override
    protected void jumpToLogin() {
//        Intent intent = new Intent(context, LoginActivity.class);
//        startActivity(intent);
        BusinessConfig config = BusinessConfig.getInstance();
        config.setValue(this, BusinessConfig.Key.KEY_OPER_ID, null);
        activityStack.pop();
    }

    /**
     * 跳转到到下一个界面
     *
     * @param conditionId 条件ID
     */
    public void jumpToNext(String conditionId) {
        jumpToNext(conditionId, null, null);
    }


    public void jumpToPrevious() {
        if (getShowingFragment() instanceof TradingFragment) {
            ViewUtils.showToast(this, "正在通讯，请稍候...");
            return;
        }
        DialogFactory.hideAll();
        BaseTradeFragment fragment = (BaseTradeFragment) getPreFragment();
        if (fragment != null) {
            if (true/*fragment.mTradePresent.isRepeatable()*/) {
                replace(fragment).commit();
            } else {
                logger.warn("上一个步骤不支持返回！");
                ViewUtils.showToast(context, R.string.tip_goto_prestep_unenable);
            }
        } else {
            logger.warn("上一个步骤不存在！");
            activityStack.pop();
        }
    }

    /**
     * 跳转到下一个界面并携带数据
     *
     * @param conditionId 条件ID
     * @param key 数据的键
     * @param data 数据
     */
    public void jumpToNext(String conditionId, String key, Serializable data) {
        DialogFactory.hideAll();
        //快速点击时间重置
        CommonUtils.resetLastClickTime();
        Fragment fragment = getNextFragment(conditionId);
        if (fragment != null) {
            if (!TextUtils.isEmpty(key)) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(key, data);
                fragment.setArguments(bundle);
            }

            if (fragment instanceof TradingFragment) {
                if(showingFragment != null && ((BaseTradeFragment)showingFragment).mTradePresent.isEnableShowingTimeout())
                    closePageTimeout();
                //联机交互界面需要上一个界面做为背景，因此不能够调用replace方法

                if(showingFragment!=null){
                    logger.error(showingFragment.getClass().toString());
                }
                if(showingFragment instanceof TradingFragment
                        || (showingFragment != null&&showingFragment.getClass().toString().contains("EbiInputMoneyFragment"))
                        || (showingFragment != null&&showingFragment.getClass().toString().contains("EbiScanQRCodeFragment"))
                        ||(showingFragment != null&&showingFragment.getClass().toString().contains("EbiShowTradeInfoFragment"))){
                    replace(fragment).commit();
                }else {
                    add(fragment).commitAllowingStateLoss();
                }
            } else {
                replace(fragment).commit();
            }
        } else {
            logger.warn("条件ID：" + conditionId + "==>无法找到对应组件");
            activityStack.pop();
            TradeModelImpl.getInstance(null).setResultCode(Activity.RESULT_OK);
        }
    }

    private ComponentNode processController(ComponentNode showingNode, boolean isNext) {
        if (showingNode == null)
            return null;
        ComponentNode returnNode = showingNode;
        String controlClassName;
        Class clz;
        ITradeUIController tradeUIController;
        try {
            //循环执行每个节点的显示控制
            do {
                controlClassName = returnNode.getController();
                if (TextUtils.isEmpty(controlClassName))
                    return returnNode;
                clz = Class.forName(controlClassName);
                tradeUIController = (ITradeUIController) clz.newInstance();
                tradeUIController.setTransctionData(transDatas);
                tradeUIController.setTransctionTempData(tempMap);
                if (tradeUIController.isShowUI(transCode)) {
                    return returnNode;
                } else {
                    if (isNext) {
                        returnNode = tradeProcess.getNextComponentNode(returnNode, "1");
                    }
                    else {
                        returnNode = tradeProcess.getLastNode();
                    }
                }
            } while (returnNode != null);
        } catch (Exception e) {
            logger.warn("^_^ " + e.getMessage() + " ^_^");
        }
        return returnNode;
    }

    private ComponentNode filteForQps(ComponentNode showingNode, boolean isNext) {
        ComponentNode returnNode = showingNode;
        if (showingNode == null)
            return null;
        if (InputPwdFragment.class.getName().equals(showingNode.getComponentName())) {
            boolean[] qpsCondition = getQpsCondition();
            if (qpsCondition[0]) {
                if (isNext)
                    returnNode = tradeProcess.getNextComponentNode(returnNode, "1");
                else
                    returnNode = tradeProcess.getLastNode();
            }
        }
        return returnNode;
    }

    private ComponentNode controlFromOutInvoke(ComponentNode showingNode, boolean isNext) {
        ComponentNode returnNode = showingNode;
        if (mInvokerParams == null || showingNode == null)
            return returnNode;
        Bundle controlParams = mInvokerParams.getBundle(Keys.obj().control_bundle);
        //是否需要输入金额, 0不需要输入金额
        if (!controlParams.getBoolean(Keys.obj().input_money_view)) {
            Double amount = mInvokerParams.getDouble(Keys.obj().trans_amt);
            if (amount != 0 || TransCode.BALANCE.equals(mInvokerParams.getString(Keys.obj().trans_code))) {
                if (InputMoneyFragment.class.getName().equals(showingNode.getComponentName())) {
                    transDatas.put(TradeInformationTag.TRANS_MONEY, String.format(Locale.CHINESE, "%.2f", amount));
                    //存入金额
                    if (isNext)
                        returnNode = tradeProcess.getNextComponentNode(returnNode, "1");
                    else
                        returnNode = tradeProcess.getLastNode();
//                    controlParams.putByte(Keys.obj().input_money_view, (byte) 1);   //只控制首次出现的金额输入
                }
            }
        }
        //是否显示结果页面，0表示不显示
        if (!controlParams.getBoolean(Keys.obj().result_view)) {
            if (ResultFragment.class.getName().equals(showingNode.getComponentName())) {
                if (isNext)
                    returnNode = tradeProcess.getNextComponentNode(returnNode, "1");
                else
                    returnNode = tradeProcess.getLastNode();
                // TODO: 2017/4/22控制打印


//                controlParams.putByte(Keys.obj().result_view, (byte) 1);   //只控制首次出现的结果显示
            }
        }
        //是否显示签名界面，0表示不显示
        if (!controlParams.getBoolean(Keys.obj().signature_view) && SignatureFragment.class.getName().equals(showingNode.getComponentName())) {
            if (isNext)
                returnNode = tradeProcess.getNextComponentNode(returnNode, "1");
            else
                returnNode = tradeProcess.getLastNode();


//            controlParams.putByte(Keys.obj().signature_view, (byte) 1);   //只控制首次出现的签名
        }
        return returnNode;
    }

    /**
     * 跳转到下一个界面并携带数据
     *
     * @param key 键
     * @param data 数据
     */
    public void jumpToNext(String key, Serializable data) {
        jumpToNext("1", key, data);
    }

    /**
     * 跳转到结果页
     *
     * @param status 状态
     */
    public void jumpToResultActivity(StatusCode status) {
        tempMap.put(iso_f39, status.getStatusCode());
        tempMap.put(TransDataKey.key_resp_code, status.getStatusCode());
        tempMap.put(TransDataKey.key_resp_msg, getString(status.getMsgId()));
        //结果页的条件码统一定义为99
        jumpToNext("99");
        if (pbocService != null) {
            pbocService.abortProcess();
        }
    }

    public void delayJumpToNext() {
        DialogFactory.showLoadingDialog(context, "处理中，请稍候...");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DialogFactory.hideAll();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                jumpToNext();
            }
        }, 1000);
    }

    public void jumpToSignIn() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                jumpToTradingActivity(TransCode.SIGN_IN);
            }
        }, 200);
    }

    public void jumpToDownloadTmk() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(context, TradeFragmentContainer.class);
                intent.putExtra(KEY_TRANSCODE, TransCode.OBTAIN_TMK);
                intent.putExtra(FLAG_AUTO_SIGN, true);
                startActivity(intent);
            }
        }, 200);
    }

    private void jumpToTradingActivity(String transCode) {
        Intent intent = new Intent(context, TradeFragmentContainer.class);
        intent.putExtra(KEY_TRANSCODE, transCode);
        startActivity(intent);
    }

    private Fragment getPreFragment() {
        ComponentNode node = tradeProcess.getLastNode();
        if (node == null) {
            logger.warn("当前交易：" + transCode + "==>未找到上一个交易节点");
            return null;
        }
        node = filteForQps(node, false);
        node = controlFromOutInvoke(node, false);
        node = processController(node, false);
        if (node == null) {
            logger.warn("当前交易：" + transCode + "==>未找到上一个交易节点");
            return null;
        }
        String clzName = node.getComponentName();
        Fragment fragment = null;
        try {
            Class clz = Class.forName(clzName);
            Object obj = clz.newInstance();
            if (!(obj instanceof Fragment)) {
                throw new IllegalStateException("[" + clzName + "] must be extends Fragment!");
            }
            fragment = (Fragment) obj;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (fragment != null)
            tradeProcess.setCurNode(node, false);
        return fragment;
    }

    private Fragment getNextFragment(String conditionId) {
        ComponentNode node;
        logger.warn("未找到下一个交易节点 conditionId：" + conditionId);
        node = tradeProcess.getNextComponentNode(conditionId);
        if (node == null) {
            logger.warn("当前交易：" + transCode + "==>未找到下一个交易节点");
            node = postProcess();
            if (node == null)
                return null;
        }
        node = filteForQps(node, true);
        node = controlFromOutInvoke(node, true);
        if(!"555".equals(conditionId)){
            node = processController(node, true);
        }
        if (node == null) {
            logger.warn("当前交易：" + transCode + "==>未找到上一个交易节点");
            return null;
        }
        logger.error(node.getComponentName());
        String clzName = node.getComponentName();
        Fragment fragment = null;
        try {
            Class clz = Class.forName(clzName);
            Object obj = clz.newInstance();
            if (!(obj instanceof Fragment)) {
                throw new IllegalStateException("[" + clzName + "] must be extends Fragment!");
            }
            fragment = (Fragment) obj;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        //设置配置文件定义的业务处理类
        if (!TextUtils.isEmpty(node.getPresentName())) {
            logger.warn("^_^ 业务处理类名：" + node.getPresentName() + " ^_^");
            Bundle param = new Bundle();
            param.putString(PRESENT_CLASS_NAME, node.getPresentName());
            fragment.setArguments(param);
        }
        if (fragment != null)
            tradeProcess.setCurNode(node, true);
        return fragment;
    }

    /**
     * 交易后置处理，报文头处理，脚本结果上送等
     */
    private ComponentNode postProcess() {
        try {
            //清空数据，防止重复发交易时导致的冲正问题
            mTradeInformation.getTransDatas().clear();
            mTradeInformation.getDataMap().clear();
            mTradeInformation.getTempMap().clear();
            transDatas.clear();

            transCode = getPostTransCode();
            tradeProcess = getPostTransProcess(transCode);
            if (TextUtils.isEmpty(transCode) || tradeProcess == null)
                return null;
            if (((BaseTradeFragment) showingFragment).mTradePresent.isEnableShowingTimeout()) {
                closePageTimeout();
            }
            mTradeInformation.setTransCode(transCode);
            mTradeInformation.setTradeProcess(tradeProcess);
            transDatas.put(TradeInformationTag.TRANSACTION_TYPE, transCode);
            return tradeProcess.getNextComponentNode("1");
        }catch (Exception e){
            logger.error(e.toString());
            return null;
        }
    }

    /**
     * 获取交易流程
     *
     * @param transCode 交易类型
     * @return 交易流程
     */
    private TradeProcess getPostTransProcess(String transCode) {
        return ConfigureManager.getInstance(EposApplication.getAppContext()).getTradeProcess(this, "online.xml");
    }

    /**
     * 获取交易类型
     *
     * @return 交易类型
     */
    private String getPostTransCode() {
        //获取报文头处理的业务处理要求
        if (ProcessRequestManager.isExistProcessRequest() && !transCode.equals(ProcessRequestManager
                .getRequestTradeCode()))
            return ProcessRequestManager.getRequestTradeCode();
        /*
        * 如果是签到，和脱机交易，不用检查 是否上送离线交易和电子签名
        * 离线交易上送 除签到外的联机交易需要检查是否上送离线交易
        * @author zhouzhihua
        * */
        String code = ((BaseTradeFragment) showingFragment).mTradePresent.getTradeCode();

        logger.warn("^_^ getPostTransCode transCode ：" + transCode + " code:"+code+" ^_^");

        if( TransCode.E_QUICK.equals(code)
            || TransCode.E_COMMON.equals(code)
            || TransCode.SIGN_IN.equals(code) || TransCode.E_BALANCE.equals(code)
            || TransCode.OFFLINE_ADJUST.equals(code)
            || TransCode.OFFLINE_ADJUST_TIP.equals(code)
            || TransCode.OFFLINE_SETTLEMENT.equals(code)){
            return null;
        }
        /*
        * @author zhouzhihua 增加离线交易上送
        * 离线交易交易完立刻上送
        * */
        if( new UploadOfflineTradeChecker().check(null,((BaseTradeFragment) showingFragment).mTradePresent) ){
            return TransCode.OFFLINE_UPLOAD;
        }

        //获取电子签名上送交易代码
        if (new UploadESignatureTradeChecker().check(null, ((BaseTradeFragment) showingFragment).mTradePresent))
            return TransCode.ESIGN_UPLOAD;

        return null;
    }

    public boolean isPbocTerminated() {
        return gotTerminatedEvent;
    }

    /**
     * 获取IC卡卡片相关信息的tag列表
     *
     * @return tag列表
     */
    public List<EmvTag.Tag> getCardInfoTags() {
        List<EmvTag.Tag> tagList = new ArrayList<>();
        tagList.add(EmvTag.Tag._57);//二磁
        tagList.add(EmvTag.Tag._5F24);//卡片失效日期
        tagList.add(EmvTag.Tag._5F34);//卡序列号
        return tagList;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_HOME:
                return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (CommonUtils.isFastClick())
            return true;
        logger.info("keyCode -> "+keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (getShowingFragment() instanceof TradingFragment) {
                    ViewUtils.showToast(this, "正在通讯，请稍候...");
                    return true;
                }
                ITradeView tradeView = (ITradeView) getShowingFragment();
                if (tradeView != null) {
                    if (tradeView.onBacKeyPressed()) {
                        return true;
                    }
                }
                activityStack.pop();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 开启PBOC流程
     */
    protected void beginPbocProcess() {
        try {
            pbocService = DeviceFactory.getInstance().getPbocService();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mTradeInformation.setPbocService(pbocService);
        logger.info("开启PBOC流程==>" + codeMapPbocType() + "==>" + pbocParams.toString());
        pbocService.startProcess(codeMapPbocType(), pbocParams);
    }

    protected EnumTransType codeMapPbocType() {
        if (transCode == null) {
            throw new IllegalArgumentException("TransCode is null");
        }
        switch (transCode) {

            case TransCode.E_QUICK: //快速支付
            case TransCode.E_COMMON: //普通支付
            case TransCode.SALE:
            case TransCode.ISS_INTEGRAL_SALE:
            case TransCode.UNION_INTEGRAL_SALE:
                return EnumTransType.TRANS_TYPE_CONSUME;
            case TransCode.BALANCE:
            case TransCode.UNION_INTEGRAL_BALANCE:
                return EnumTransType.TRANS_TYPE_BALANCE_QUERY;
            case TransCode.VOID:
            case TransCode.COMPLETE_VOID:
            case TransCode.ISS_INTEGRAL_VOID:
            case TransCode.UNION_INTEGRAL_VOID:
                return EnumTransType.TRANS_TYPE_CONSUME_CANCEL;
            case TransCode.AUTH:
            case TransCode.AUTH_COMPLETE:
            case TransCode.CANCEL:
                return EnumTransType.TRANS_TYPE_PRE_AUTH;
            case TransCode.REFUND:
            case TransCode.E_REFUND:
            case TransCode.UNION_INTEGRAL_REFUND:
                return EnumTransType.TRANS_TYPE_RETURN;
            /*
            * 电子现金余额
            * */
            case TransCode.E_BALANCE: return EnumTransType.CARD_BALANCE_INQUIRY;

            case TransCode.EC_LOAD_RECORDS : return EnumTransType.CARD_TRANSFER_LOG_QUERY;

            case TransCode.EC_TRANS_RECORDS : return EnumTransType.CARD_TRANSACTION_LOG_QUERY;

            case TransCode.EC_LOAD_CASH : return EnumTransType.TRANS_TYPE_CASH_FOR_LOAD;

            case TransCode.EC_LOAD_INNER : return EnumTransType.TRANS_TYPE_NAMED_ACCOUNT_CREDIT_FOR_LOAD;

            case TransCode.MAG_ACCOUNT_LOAD: return EnumTransType.TRANS_NON_NAMED_ACCOUNTA_CREDIT_FOR_LOAD_TRANSFER_CARD;
            case TransCode.EC_LOAD_OUTER : {
                return this.isIccSecondUseCard() ? EnumTransType.TRANS_TYPE_NON_NAMED_ACCOUNTA_CREDIT_FOR_LOAD : EnumTransType.TRANS_NON_NAMED_ACCOUNTA_CREDIT_FOR_LOAD_TRANSFER_CARD;
            }

            case TransCode.EC_VOID_CASH_LOAD: return EnumTransType.TRANS_TYPE_CASH_DEPOSIT_CANCEL;

            default:
                IPbocTranType customPbocTranType = (IPbocTranType) ConfigureManager.getProjectClassInstance(CustomPbocTranType.class);
                if (customPbocTranType != null)
                    return customPbocTranType.getTranTypeMap(transCode);

                return EnumTransType.TRANS_TYPE_CONSUME;
        }
    }

    /**
     * 非指定账户圈存第二次用卡，插卡或者挥卡
     *
     * @return 二次刷卡true，否则返回false
     */
    public boolean isIccSecondUseCard() {
        String use = (String) transDatas.get(TransDataKey.key_is_load_second_use_card);

        return ( ( null != use ) && use.equals("1"));
    }
    /**
     * 判断是否IC卡插卡交易，判断的前提是已经检卡
     *
     * @return 插卡交易返回true，否则返回false
     */
    protected boolean isICInsertTrade() {
        String iso22 = (String) transDatas.get(TradeInformationTag.SERVICE_ENTRY_MODE);
        if (iso22 != null && iso22.startsWith("05")) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否是IC卡交易，包含插卡和非接，判断的前提是已经检卡
     *
     * @return IC卡交易返回true，否则返回false
     */
    protected boolean isICTrade() {
        String iso22 = (String) transDatas.get(TradeInformationTag.SERVICE_ENTRY_MODE);
        if (iso22 != null && (iso22.startsWith("05") || iso22.startsWith("07"))) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否是挥卡交易，判断的前提是已经检卡
     *
     * @return 挥卡交易返回true，否则返回false
     */
    protected boolean isICClssTrade() {
        String iso22 = (String) transDatas.get(TradeInformationTag.SERVICE_ENTRY_MODE);
        if (iso22 != null && iso22.startsWith("07")) {
            return true;
        }
        return false;
    }
    /**
     * 电子现金圈存交易需要导入联机数据
     * @return true-is ec load ,other-is false
     */
    private boolean bIsEcLoadTrans()
    {
        return ( TransCode.EC_LOAD_CASH.equals(transCode)
                || TransCode.EC_LOAD_INNER.equals(transCode)
                || TransCode.EC_LOAD_OUTER.equals(transCode)
                || TransCode.EC_VOID_CASH_LOAD.equals(transCode) );
    }

    /**
     * 判断当前交易是否需要导入联机响应数据
     *
     * @return 是返回true，否则返回false
     *
     * transCode 取出的交易类型可能不是当前交易的
     */
    protected boolean isImportOnlineRespTrade() {
        return ( bIsEcLoadTrans()
                || ( isICInsertTrade()
                && ( TransCode.UNION_INTEGRAL_BALANCE.equals(transCode)
                     || TransCode.BALANCE.equals(transCode)
                     || TransCode.SALE.equals(transCode)
                     || TransCode.AUTH.equals(transCode)
                     || TransCode.ISS_INTEGRAL_SALE.equals(transCode)
                     || TransCode.UNION_INTEGRAL_SALE.equals(transCode)) ) );
    }

    /**
     * 判断是否满足小额免密免签业务要求
     *
     * @return 是返回true，否则返回false
     */
    private boolean[] flags;


    public void resetQpsConditionFlags() {
        flags = null;
    }

    public boolean[] getQpsCondition() {
        if (flags != null)
            return flags;
        double printAmount = 0.0;
        flags = new boolean[]{false, false};
        try {
            String iso22 = (String) transDatas.get(TradeInformationTag.SERVICE_ENTRY_MODE);//服务点输入方式码
            String amt = (String) transDatas.get(TradeInformationTag.TRANS_MONEY);//金额
            String cardNo = (String) transDatas.get(TradeInformationTag.BANK_CARD_NUM);//卡号
//            String cardType = transDatas.get(TradeInformationTag.IC_PARAMETER_AID);
            logger.info("终端QPS业务参数==>" + (qpsParams == null ? "null" : qpsParams.toString()));
            logger.info("当前交易QPS相关参数==>TransCode==>" + transCode + "==>ISO f22==>" + iso22 + "==>ISO f4==>" +
                    DataHelper.formatIsoF4(amt));
            qpsParams = mTradeModel.getQpsParams();

            /*
            * 快速支付不需要走免签免密流程
            * */
            if( TransCode.E_QUICK.equals(transCode) ){
                mTradeModel.setTradeSlipNoSign(false);
                mTradeModel.setTradeNoPin(false);
                return flags;
            }

            if (!clssForcePin
                    && (TransCode.SALE.equals(transCode) || TransCode.AUTH.equals(transCode))
                    && qpsParams != null) {
                if (iso22 == null || !iso22.startsWith("07")) {
                    //非非接方式，不支持QPS业务
                    return flags;
                }
                double currentAmt = DataHelper.parseIsoF4(amt);
                double noPinLimit = qpsParams.getNoPinLimit();
                double noSignLimit = qpsParams.getNoSignLimit();
                int stage = qpsParams.getPromotionStage();//推广阶段
                long start = System.currentTimeMillis();
                mTradeModel.setTradeSlipNoSign(false);
                mTradeModel.setTradeNoPin(false);
                if (qpsParams.isNoPinOn() && currentAmt <= noPinLimit) {
                    CommonDao<QpsBinData> qpsBinDao = new CommonDao<>(QpsBinData.class, getDbHelper());
                    CommonDao<QpsBlackBinData> qpsBlackBinDao = new CommonDao<>(QpsBlackBinData.class, getDbHelper());
                    String cardNoPrefix = null;
                    if (!TextUtils.isEmpty(cardNo)) {
                        cardNoPrefix = cardNo.length() > 6 ? cardNo.substring(0, 6) : cardNo;
                    } else {
                        logger.warn("卡号为空，无法判断");
                        return flags;
                    }
                    if (currentAmt <= noPinLimit) {
                        switch (stage) {
                            case 0:
                                //不支持非接快速业务
                                flags[0] = false;
                                break;
                            case 1: {
                                //试点阶段一（借贷记卡从BIN表A中判断）
                                List<QpsBinData> binDatas = qpsBinDao.queryBuilder().where().eq("type", "A").and().eq
                                        ("cardBin", cardNoPrefix).query();
                                logger.debug("卡号：" + cardNo + "==>查询BIN表A的信息为==>" + binDatas + "==>耗时：" + (System
                                        .currentTimeMillis() - start));
                                if (binDatas != null && binDatas.size() > 0) {
                                    for (int i = 0; i < binDatas.size(); i++) {
                                        if (cardNo.length() == binDatas.get(i).getCardLen()) {
                                            flags[0] = true;
                                            break;
                                        }
                                    }
                                }
                                break;
                            }
                            case 2:
                                //试点阶段二（贷记卡全部支持，借记卡从BIN表B中判断）
                                boolean isjieji = (boolean) transDatas.get(TradeInformationTag.BANK_CARD_TYPE);
                                if (!isjieji) {
                                    flags[0] = true;
                                    break;
                                }
                                start = System.currentTimeMillis();
                                if (!flags[0]) {
                                    List<QpsBinData> dataList2 = qpsBinDao.queryBuilder().where().eq("cardBin",
                                            cardNoPrefix).and().eq("type", "B").query();
                                    logger.debug("卡号：" + cardNo + "==>查询BIN表AB的信息为==>" + dataList2 + "==>耗时：" + (System
                                            .currentTimeMillis() - start));
                                    if (dataList2.size() > 0) {
                                        for (int i = 0; i < dataList2.size(); i++) {
                                            if (cardNo.length() == dataList2.get(i).getCardLen()) {
                                                flags[0] = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                                break;
                            case 3: {
                                //全面推广阶段
                                List<QpsBlackBinData> binDatas = qpsBlackBinDao.queryBuilder().where().eq
                                        ("cardBin", cardNoPrefix).query();
                                logger.debug("卡号：" + cardNo + "==>查询黑名单的信息为==>" + binDatas + "==>耗时：" + (System
                                        .currentTimeMillis() - start));
                                /*BUGID:0003193
                                * @author:zhouzhihua
                                * 默认不需要输入pin，只有在黑名单中，才需要输入密码。
                                * */
                                flags[0] = true;//提前初始化参数
                                if (binDatas != null && binDatas.size() > 0) {
                                    for (int i = 0; i < binDatas.size(); i++) {
                                        if (cardNo.length() == binDatas.get(i).getCardLen()) {
                                            flags[0] = false;
                                            break;
                                        }
                                    }
                                }
                                /*BUGID:0003193
                                * 上一个break只会退出for循环，无法跳出case 3，flags[0] 恒等于 true，
                                * 导致所有都免密
                                * */
                                //flags[0] = true;
                                break;
                            }
                        }
                  /*  //金额满足免密限额
                    BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.KEY_NOT_PIN, true);
                    printAmount = noPinLimit;
                    flags[0] = true;*/
                    }
                }
                if (flags[0]) {
                    //金额满足免密限额
                    mTradeModel.setTradeNoPin(true);
                    if (qpsParams.isNoSignOn())
                        printAmount = noPinLimit;
                }
                if (qpsParams.isNoSignOn() && currentAmt <= noSignLimit) {
                    //金额满足免签限额
                    if (printAmount < noSignLimit) {
                        printAmount = noSignLimit;
                    }
                    mTradeModel.setTradeSlipNoSign(true);
                    flags[1] = true;
                }

                mTradeModel.setSlipNoSignAmount(printAmount + "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //其它类型业务无需支持QPS业务
        return flags;
    }


    /**
     * 内核事件广播接收器
     */
    public class PbocEventReceiver extends BroadcastReceiver {

        public void onImportAmount() {
            BaseTradeFragment tradeFragment = (BaseTradeFragment) getShowingFragment();
            XLogUtil.w("onImportAmount: ", getShowingFragment().getClass().getSimpleName());
            XLogUtil.w("onImportAmount: ", " "+tradeFragment.mTradePresent);
            if ( ( tradeFragment != null ) && ( null != tradeFragment.mTradePresent ) ) {
                if (tradeFragment.mTradePresent.onPbocImportAmount())
                return;
            }
            //金额是在卡号之前输入的交易，需要先导入金额，否则内核无法上报卡号确认事件
            String amt = (String) transDatas.get(TradeInformationTag.TRANS_MONEY);
            if (!TextUtils.isEmpty(amt)) {
                if (amt.length() == 12) {
                    amt = DataHelper.formatIsoF4(amt);
                }
                pbocService.importAmount(amt);
            } else {
                transDatas.put(FLAG_IMPORT_AMOUNT, "1");
            }
        }

        public void onImportPin(boolean bIsOfflinePin) {
            BaseTradeFragment tradeFragment = (BaseTradeFragment) getShowingFragment();
            if ( ( tradeFragment != null ) && ( null != tradeFragment.mTradePresent ) ) {
                if (tradeFragment.mTradePresent.onPbocImportPin(false))
                    return;
            }
            pbocService.importPIN(false, null);
        }

        //要放到收到数据以后处理
        public void onRequestOnline() {
            BaseTradeFragment tradeFragment = (BaseTradeFragment) getShowingFragment();
            XLogUtil.w("zhouzhihua","onRequestOnline ："+tradeFragment.getClass().getSimpleName());
            if (tradeFragment != null) {
                if (tradeFragment.mTradePresent.onPbocRequestOnline())
                    return;
            }
            transDatas.put(FLAG_REQUEST_ONLINE, "1");
        }

        public void onConfirmCardNo(String cardNo) {
            BaseTradeFragment tradeFragment = (BaseTradeFragment) getShowingFragment();
            if (tradeFragment != null) {
                if (tradeFragment.mTradePresent.onPbocConfirmCardNo(cardNo))
                    return;
            }
            transDatas.put(TradeInformationTag.BANK_CARD_NUM, cardNo);
        }

        public void onReturnOfflineBalance(String code1, String balance1, String code2, String balance2) {
            BaseTradeFragment tradeFragment = (BaseTradeFragment) getShowingFragment();
            if (tradeFragment != null) {
                if (tradeFragment.mTradePresent.onReturnOfflineBalance(code1, balance1, code2, balance2))
                    return;
            }
        }

        public void onReturnCardTransLog(List<Parcelable> data) {
            BaseTradeFragment tradeFragment = (BaseTradeFragment) getShowingFragment();
            if (tradeFragment != null) {
                if (tradeFragment.mTradePresent.onReturnCardTransLog(data))
                    return;
            }
        }

        public void onReturnCardLoadLog(List<Parcelable> data) {
            BaseTradeFragment tradeFragment = (BaseTradeFragment) getShowingFragment();
            XLogUtil.w("onReturnCardLoadLog"," "+data);
            XLogUtil.w("onReturnCardLoadLog","getSimpleName:"+tradeFragment.getClass().getSimpleName());
            if (tradeFragment != null) {
                if (tradeFragment.mTradePresent.onReturnCardLoadLog(data))
                    return;
            }
        }

        /*请求提示信息确认*/
        public void onRequestTipsConfirm(String tips){
            BaseTradeFragment tradeFragment = (BaseTradeFragment) getShowingFragment();
            if (tradeFragment != null) {
                if (tradeFragment.mTradePresent.onPbocRequestTipsConfirm(tips))
                    return;
            }
            pbocService.importResult(EnumPbocResultType.MSG_CONFIRM, true);
        }
        //REQUEST_AID_SELECT
        public void onRequestUserAidSelect(String[] aidList) {
            BaseTradeFragment tradeFragment = (BaseTradeFragment) getShowingFragment();
            if (tradeFragment != null) {
                if (tradeFragment.mTradePresent.onPbocRequestUserAidSelect(aidList))
                    return;
            }
            pbocService.importAidSelectResult(1);
        }
        /*请求电子现金提示确认*/
        public void onRequestEcTipsConfirm(){
            BaseTradeFragment tradeFragment = (BaseTradeFragment) getShowingFragment();
            if (tradeFragment != null) {
                if (tradeFragment.mTradePresent.onPbocRequestEcTipsConfirm())
                    return;
            }
            pbocService.importResult(EnumPbocResultType.EC_TIP_CONFIRM, true);
            transDatas.put(TransDataKey.KEY_EC_TIPS_CONFIRM,"1");/*此标志表示接触电子现金交易金额小于余额*/
        }

        public boolean onTradeRefused() {
            BaseTradeFragment tradeFragment = (BaseTradeFragment) getShowingFragment();
            if (tradeFragment != null && tradeFragment.mTradePresent != null) {
                return tradeFragment.mTradePresent.onPbocTradeRefused();
            }
            return false;
        }

        public boolean onTradeTerminated() {
            gotTerminatedEvent = true;
            BaseTradeFragment tradeFragment = (BaseTradeFragment) getShowingFragment();
            if (tradeFragment != null && tradeFragment.mTradePresent != null) {
                return tradeFragment.mTradePresent.onPbocTradeTerminated();
            }
            return false;
        }

        public boolean onTradeApproved() {
            BaseTradeFragment tradeFragment = (BaseTradeFragment) getShowingFragment();
            if (tradeFragment != null && tradeFragment.mTradePresent != null) {
                return tradeFragment.mTradePresent.onPbocTradeApproved();
            }
            return false;
        }

        public boolean onFallback() {
            return false;
        }

        public boolean onNeedChangeUserFaces() {
            ViewUtils.showToast(context, R.string.tip_read_card_failed);
            pbocService.stopProcess();
            DialogFactory.hideAll();
            activityStack.pop();
            return true;
        }

        public boolean onError() {
            BaseTradeFragment tradeFragment = (BaseTradeFragment) getShowingFragment();
            if (tradeFragment != null && tradeFragment.mTradePresent != null) {
                return tradeFragment.mTradePresent.onPbocTradeError();
            }
            return false;
        }

        @Override
        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();
            Bundle bundle = intent.getExtras();
            logger.debug(getClass().getSimpleName() + "==>接收到广播==>" + action);
            final IPbocService pbocService;
            try {
                pbocService = DeviceFactory.getInstance().getPbocService();
            } catch (Exception e) {
                e.printStackTrace();
                jumpToResultActivity(StatusCode.EMV_KERNEL_EXCEPTION);
                return;
            }
            switch (action) {
                case ACTION_REQUEST_AMOUNT:
                    //请求导入金额
                    onImportAmount();
                    break;
                case ACTION_REQUEST_TIPS_CONFIRM:
                    //请求提示信息确认
                    onRequestTipsConfirm(bundle.getString(KEY_TIPS));
                    break;
                case ACTION_REQUEST_AID_SELECT:
                    //请求AID应用选择
                    onRequestUserAidSelect(bundle.getStringArray(PbocEventAction.KEY_AIDS));
                    break;
                case ACTION_REQUEST_EC_TIPS_CONFIRM:
                    //请求电子现金提示确认
                    onRequestEcTipsConfirm();
                    break;
                case ACTION_REQUEST_CARD_INFO_CONFIRM:
                    //请求卡号信息确认
                    //异步读取卡信息，避免过程动画停止
                    new AsyncTask<IPbocService, Integer, Boolean>() {

                        @Override
                        protected Boolean doInBackground(IPbocService... params) {
                            return getAndStoreTradeInfo(params[0]);
                        }

                        @Override
                        protected void onPostExecute(Boolean aBoolean) {
                            String cardNo = intent.getExtras().getString(PbocEventAction.KEY_CARD_INFO);
                            onConfirmCardNo(cardNo);
                        }
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, pbocService);
                    break;
                case ACTION_REQUEST_PIN://请求导入PIN
                    XLogUtil.w("zhouzhihua","ACTION_REQUEST_PIN:"+bundle.getBoolean(PbocEventAction.KEY_PIN_TYPE));
                    onImportPin(bundle.getBoolean(PbocEventAction.KEY_PIN_TYPE,false));
                    break;
                case ACTION_REQUEST_USER_AUTH://请求用户认证
                    pbocService.importResult(EnumPbocResultType.USER_AUTH, true);
                    break;
                case ACTION_REQUEST_ONLINE://请求联机
                    XLogUtil.w("zhouzhihua","" + ACTION_REQUEST_ONLINE);
                    if (!getAndStoreTradeInfo(pbocService))
                        break;
                    XLogUtil.w("zhouzhihua","ACTION_REQUEST_ONLINE:" + ACTION_REQUEST_ONLINE);
                    String iso55 = null;
                    String iso55_reserve = null;
                    String print = null;
                    XLogUtil.w("zhouzhihua","transCode:" + transCode);
                    if (BALANCE.equals(transCode)||UNION_INTEGRAL_BALANCE.equals(transCode))
                        iso55 = pbocService.readTlvKernelData(EmvTag.getF55Tags1());//读取55域数据
                    else if (TransCode.PRINT_IC_INFO.contains(transCode)) {
                        iso55 = pbocService.readTlvKernelData(EmvTag.getF55Tags1());//读取55域数据
                        iso55_reserve = pbocService.readTlvKernelData(EmvTag.getF55Tags2());//读取冲正时用到的55域数据
                        print = pbocService.readTlvKernelData(EmvTag.getTagsForPrint());
                    } else {
                        //其它业务，只是为读取卡信息。后续应该修改为调用PBOC的读取卡号流程，就不会执行到这里
                        pbocService.abortProcess();
                    }

                    transDatas.put(TradeInformationTag.IC_DATA, iso55);
                    transDatas.put(TradeInformationTag.IC_DATA_REVERSE, iso55_reserve);
//                    Map<String, String> stringMap = TlvUtils.tlvToMap(print);
                    logger.debug("ic卡打印信息为：" + print);
                    if (!TextUtils.isEmpty(print)) {
                        transDatas.put(KEY_IC_DATA_PRINT, print);
                        tempMap.put(KEY_IC_DATA_PRINT, iso55 + print);
                    }
                    onRequestOnline();
                    break;
                case ACTION_RETURN_CARD_OFFLINE_BALANCE:
                    //返回卡片脱机余额
                    onReturnOfflineBalance(
                            bundle.getString(KEY_FIRST_EC_CODE),
                            bundle.getString(KEY_FIRST_EC_BALANCE),
                            bundle.getString(KEY_SECOND_EC_CODE),
                            bundle.getString(KEY_SECOND_EC_BALANCE));
                    break;
                case ACTION_RETURN_CARD_TRANS_LOG:
                    //返回卡片交易日志
                    onReturnCardTransLog(bundle.getParcelableArrayList(KEY_TRANS_LOG));
                    break;
                case ACTION_RETURN_CARD_LOAD_LOG:
                    //返回卡片圈存日志
                    onReturnCardLoadLog(bundle.getParcelableArrayList(KEY_LOAD_LOG));
                    break;
                case ACTION_TRADE_APPROVED:
                    //交易批准
                    if (!onTradeApproved()) {
                        pbocService.stopProcess();
                    }
                    break;
                case ACTION_TRADE_REFUSED:
                    //交易拒绝
                    if (!onTradeRefused()) {
                        jumpToResultActivity(StatusCode.TRADING_REFUSED);
                        DialogFactory.hideAll();
                    }
                    break;
                case ACTION_TRADE_TERMINATED:
                    //交易终止
                    if (!onTradeTerminated()) {
                        jumpToResultActivity(StatusCode.TRADING_TERMINATES);
                        DialogFactory.hideAll();
                    }
                    break;
                case ACTION_NEED_CHANGE_READ_CARD_TYPE:
                    BaseTradeFragment tradeFragment = (BaseTradeFragment) getShowingFragment();
                    if (tradeFragment != null && tradeFragment.mTradePresent != null) {
                        tradeFragment.mTradePresent.getTransData().put(ACTION_NEED_CHANGE_READ_CARD_TYPE, true);
                        if (tradeFragment.mTradePresent.onPbocChangeUserInterface())
                            break;
                    }
                    //交易降级或者采用其它用户界面
                    int resultCode = intent.getExtras().getInt(PbocEventAction.KEY_TRANS_RESULT);
                    if (resultCode == 3) {
                        if (!onFallback()) {
                            jumpToResultActivity(StatusCode.TRADING_FALLBACK);
                            DialogFactory.hideAll();
                        }
                    } else {
                        if (!onNeedChangeUserFaces()) {
                            jumpToResultActivity(StatusCode.TRADING_CHANGE_OTHER_FACE);
                            DialogFactory.hideAll();
                        }
                    }

                    break;
                case ACTION_ERROR:
                    //内核异常
                    if (!onError()) {
                        jumpToResultActivity(StatusCode.EMV_KERNEL_EXCEPTION);
                        DialogFactory.hideAll();
                    }
                    break;
            }
        }
    }

    private boolean getAndStoreTradeInfo(IPbocService pbocService) {
        String secondUseCard = (String)transDatas.get(TransDataKey.key_is_load_second_use_card);
        logger.info("getAndStoreTradeInfo ：" + secondUseCard);
        /*
        * 非指定账户圈存卡序列号使用转入卡的
        * */
        if( secondUseCard != null && transCode.equals(TransCode.EC_LOAD_OUTER) && secondUseCard.equals("1") ){
            Map<String, String> cardInfo = pbocService.readKernelData(EmvTag.Tag._5F34.getByteValue());
            transDatas.put(TradeInformationTag.CARD_SEQUENCE_NUMBER, cardInfo.get("5F34"));
            return true;
        }

        if (transDatas.get(TradeInformationTag.BANK_CARD_TYPE) != null)
            return true;
        Map<String, String> aidMap = pbocService.readKernelData(EMVTAG_AID);
        //在卡号确认时，读取内核数据，第一次读取失败，所以多读一次
        if (aidMap == null)
            aidMap = pbocService.readKernelData(EMVTAG_AID);
        if (aidMap != null) {
            String aid = (String) aidMap.values().toArray()[0];
            transDatas.put(TradeInformationTag.BANK_CARD_TYPE, "A000000333010101".equals(aid));
        }
        Map<String, String> cardInfo = pbocService.readKernelData(getCardInfoTags());
        if (cardInfo == null)
            return false;
        logger.info("IC卡卡片信息读取成功：" + cardInfo.toString());
        String tag57 = cardInfo.get("57");
        String[] track2Info = tag57.split("D");
        if (track2Info.length > 0)
            transDatas.put(TradeInformationTag.BANK_CARD_NUM, track2Info[0]);
        if (tag57.endsWith("F") || tag57.endsWith("f"))
            tag57 = tag57.substring(0, tag57.length() - 1);
        transDatas.put(TradeInformationTag.TRACK_2_DATA, tag57);
        String expiry = cardInfo.get("5F24");

        if (expiry != null && expiry.length() >= 4) {
            expiry = expiry.substring(0, 4);
        } else if (track2Info.length > 1)
            expiry = track2Info[1].substring(0, 4);

        transDatas.put(TradeInformationTag.DATE_EXPIRED, expiry);
        transDatas.put(TradeInformationTag.CARD_SEQUENCE_NUMBER, cardInfo.get("5F34"));
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        BaseTradeFragment tradeFragment = (BaseTradeFragment) getShowingFragment();
        if (tradeFragment != null) {
            tradeFragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}