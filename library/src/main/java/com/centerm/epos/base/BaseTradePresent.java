package com.centerm.epos.base;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.TextUtils;

import com.centerm.cpay.midsdk.dev.define.pboc.EnumTransType;
import com.centerm.epos.ActivityStack;
import com.centerm.epos.EposApplication;
import com.centerm.epos.activity.MainActivity;
import com.centerm.epos.bean.transcation.TradeInformation;
import com.centerm.epos.common.StatusCode;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.net.htttp.JsonKey;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.XLogUtil;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import org.apache.log4j.Logger;
import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.epos.base.BaseActivity.KEY_TRANSCODE;
import static com.centerm.epos.common.TransDataKey.FLAG_AUTO_SIGN;
import static com.centerm.epos.common.TransDataKey.iso_f39;
import static com.centerm.epos.common.TransDataKey.key_resp_code;
import static com.centerm.epos.common.TransDataKey.key_resp_msg;

/**
 * Created by yuhc on 2017/2/15.
 * 实现基本的操作
 */
public class BaseTradePresent implements ITradePresent {
    private static final String TAG = BaseTradePresent.class.getSimpleName();
    //关联的UI界面操作
    protected ITradeView mTradeView;
    public TradeInformation mTradeInformation;

    protected DbHelper dbHelper;
    protected Logger logger = Logger.getLogger(this.getClass());

    //    protected Map<String,String> dataMap;
    protected Map<String, String> tempMap;
    protected Map<String, Object> transDatas;
    protected Map<String, Object> respDataMap;

    public BaseTradePresent(ITradeView mTradeView) {
        this.mTradeView = mTradeView;
        try {
            mTradeInformation = mTradeView.getHostActivity().mTradeInformation;
            tempMap = mTradeInformation.getTempMap();
            transDatas = mTradeInformation.getTransDatas();
            respDataMap = mTradeInformation.getRespDataMap();
        }catch (Exception e){
            e.printStackTrace();
            tempMap = new HashMap<>();
            transDatas = new HashMap<>();
            respDataMap = new HashMap<>();
        }
    }

    public DbHelper getDbHelper() {
        return dbHelper;
    }

    @Override
    public Object getShowInfo() {
        return null;
    }

    @Override
    public void displayUI() {

    }

    @Override
    public void beginTransaction() {

    }

    @Override
    public void endTransaction() {

    }

    @Override
    public void release() {
        if (isOpenDataBase()) {
            OpenHelperManager.releaseHelper();
            dbHelper = null;
        }
        EventBus.getDefault().unregister(this);
        mTradeView = null;
    }

    @Override
    public void gotoNextStep() {
        mTradeView.getHostActivity().jumpToNext();
    }

    @Override
    public void gotoNextStep(String nextTag) {
        mTradeView.getHostActivity().jumpToNext(nextTag);
    }


    protected void jumpToResult(String code, String msg) {
        tempMap.put(TransDataKey.key_resp_code, code);
        tempMap.put(TransDataKey.key_resp_msg, msg);
        //结果页的条件码统一定义为99
        gotoNextStep("99");
        if (mTradeInformation.getPbocService() != null) {
            mTradeInformation.getPbocService().abortProcess();
        }
    }

    /**
     * 结束当前界面并跳转到登录界面
     */
    public void jumpToLogin() {
        //在此处处理，在用户签退或者退出时，将操作员账号置空
        BusinessConfig config = BusinessConfig.getInstance();
        config.setValue(mTradeView.getHostActivity(), BusinessConfig.Key.KEY_OPER_ID, null);
//        Intent intent = new Intent(mTradeView.getHostActivity(), LoginActivity.class);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                activityStack.removeExcept(LoginActivity.class);
//            }
//        }, 300);

        mTradeView.getHostActivity().setResult(MainActivity.SHOW_LOGIN);
        ActivityStack.getInstance().pop();

//        mTradeView.getHostActivity().startActivity(intent);
    }

    public void jumpToMain() {
        ActivityStack.getInstance().pop();
    }

    protected void jumpToSignIn() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                jumpToTradingActivity(TransCode.SIGN_IN);
            }
        }, 200);
    }

    protected void jumpToDownloadTmk() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(mTradeView.getHostActivity(), TradeFragmentContainer.class);
                intent.putExtra(KEY_TRANSCODE, TransCode.OBTAIN_TMK);
                intent.putExtra(FLAG_AUTO_SIGN, true);
                mTradeView.getHostActivity().startActivity(intent);
            }
        }, 200);
    }

    private void jumpToTradingActivity(String transCode) {
        Intent intent = new Intent(mTradeView.getHostActivity(), TradeFragmentContainer.class);
        intent.putExtra(KEY_TRANSCODE, transCode);
        mTradeView.getHostActivity().startActivity(intent);
    }

    @Override
    public void gotoPreStep() {
        mTradeView.getHostActivity().jumpToPrevious();
    }

    @Override
    public void onTransactionError() {

    }

    @Override
    public void onTransactionQuit() {
        if(ActivityStack.getInstance().getActivityStack().size()>1) {
            ActivityStack.getInstance().pop();
        }
    }

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {

    }

    @Override
    public void OpenDatabase() {
        if (isOpenDataBase())
            dbHelper = OpenHelperManager.getHelper(EposApplication.getAppContext(), DbHelper.class);
    }

    @Override
    public String getTradeName() {
        return mTradeView.getHostActivity().getString(TransCode.codeMapName(mTradeInformation.getTransCode()));
    }

    @Override
    public String getTradeCode() {
        if(mTradeInformation==null){
            return "";
        }
        return mTradeInformation.getTransCode();
    }

    @Override
    public String getTransData(String tag) {
        return (String) transDatas.get(tag);
    }

    @Override
    public Map<String, Object> getTransData() {
        return transDatas;
    }

    public String getRespData(String tag) {
        return (String) respDataMap.get(tag);
    }

    public Map<String, Object> getRespData() {
        return respDataMap;
    }

    @Override
    public String getTempData(String tag) {
        return tempMap.get(tag);
    }

    public Map<String, String> getTempData() {
        return tempMap;
    }

    @Override
    public boolean isPbocTerminated() {
        return mTradeView.getHostActivity().isPbocTerminated();
    }

    /**
     * 判断是否IC卡插卡交易，判断的前提是已经检卡
     *
     * @return 插卡交易返回true，否则返回false
     */
    @Override
    public boolean isICInsertTrade() {
        String iso22 = (String) transDatas.get(TradeInformationTag.SERVICE_ENTRY_MODE);
        XLogUtil.w("isICInsertTrade","isICInsertTrade:"+iso22);
        if (iso22 != null && (iso22.startsWith("05"))) {
            return true;
        }
        return false;
    }

    @Override
    public void onCancel() {
        gotoPreStep();
    }

    @Override
    public Object onConfirm(Object paramObj) {
        return null;
    }

    @Override
    public void onExit() {

    }

    @Override
    public boolean isEnableShowingTimeout() {
        return false;
    }

    @Override
    public boolean onPbocConfirmCardNo(String cardNo) {
        return false;
    }

    @Override
    public boolean onPbocRequestOnline() {
        return false;
    }

    @Override
    public boolean onPbocImportAmount() {
        return false;
    }

    @Override
    public boolean onPbocTradeApproved() {
        return false;
    }

    @Override
    public boolean onPbocTradeTerminated() {
        return false;
    }

    @Override
    public boolean onPbocTradeRefused() {
        return false;
    }

    @Override
    public boolean onPbocTradeError() {
        return false;
    }

    @Override
    public boolean onPbocChangeUserInterface() {
        return false;
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public boolean onReturnOfflineBalance(String code1, String balance1, String code2, String balance2) {
        return false;
    }

    @Override
    public boolean onReturnCardTransLog(List<Parcelable> data) {
        return false;
    }

    @Override
    public boolean onReturnCardLoadLog(List<Parcelable> data) {
        return false;
    }

    @Override
    public boolean onPbocRequestTipsConfirm(String tips) {
        return false;
    }

    @Override
    public boolean onPbocRequestEcTipsConfirm() {
        return false;
    }

    @Override
    public boolean onPbocImportPin(boolean bIsOffLinePin){
        return false;
    }

    @Override
    public boolean onPbocRequestUserAidSelect(String[] aidList){ return false; }

    /**
     * @return 是否开启数据库模块
     */
    public boolean isOpenDataBase() {
        return false;
    }


    protected EnumTransType codeMapPbocType() {
        if (mTradeInformation.getTransCode() == null) {
            throw new IllegalArgumentException("TransCode is null");
        }
        switch (mTradeInformation.getTransCode()) {
            case TransCode.SALE:
                return EnumTransType.TRANS_TYPE_CONSUME;
            case TransCode.BALANCE:
                return EnumTransType.TRANS_TYPE_BALANCE_QUERY;
            case TransCode.VOID:
            case TransCode.COMPLETE_VOID:
                return EnumTransType.TRANS_TYPE_CONSUME_CANCEL;
            case TransCode.AUTH:
            case TransCode.AUTH_COMPLETE:
            case TransCode.CANCEL:
                return EnumTransType.TRANS_TYPE_PRE_AUTH;
            case TransCode.REFUND:
                return EnumTransType.TRANS_TYPE_RETURN;
            default:
                return null;
        }
    }

    /**
     * 开启PBOC流程
     */
    protected void beginPbocProcess() {
        if (mTradeView != null) {
            mTradeView.getHostActivity().beginPbocProcess();
        }
    }


    public void putResponseCode(String respCode, String respMsg) {
        if(TextUtils.isEmpty(respCode)){
            respCode = "-1";
        }
        if(TextUtils.isEmpty(respMsg)){
            respMsg = "未知";
        }
        if(!TextUtils.isEmpty(getTradeCode())&&getTradeCode().contains("SCAN")){
            if(transDatas!=null&&transDatas.get("isOrderQueryAct")==null){
                respMsg = respMsg + "，\n请在【扫码订单查询】中查询交易结果";
            }
        }
        tempMap.put(iso_f39, respCode);
        tempMap.put(key_resp_code, respCode);
        tempMap.put(key_resp_msg, respMsg);
    }

    protected void putResponseCode(StatusCode code) {
        putResponseCode(code.getStatusCode(), mTradeView.getStringFromResource(code.getMsgId()));
    }

//    public void handleMessage(PrinteEvent event){
//        logger.debug("^_^ EVENT what:" + event.getWhat() + " code:"+event.getCode()+" message:"+event.getMsg()+ "
// ^_^");
//    }
}
