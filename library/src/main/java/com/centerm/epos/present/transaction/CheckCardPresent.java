package com.centerm.epos.present.transaction;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.common.utils.TlvUtils;
import com.centerm.cpay.midsdk.dev.define.ICardReaderDev;
import com.centerm.cpay.midsdk.dev.define.IPbocService;
import com.centerm.cpay.midsdk.dev.define.cardreader.CardInfo;
import com.centerm.cpay.midsdk.dev.define.cardreader.CardReaderListener;
import com.centerm.cpay.midsdk.dev.define.cardreader.EnumReadCardType;
import com.centerm.cpay.midsdk.dev.define.pboc.CardLoadLog;
import com.centerm.cpay.midsdk.dev.define.pboc.CardTransLog;
import com.centerm.cpay.midsdk.dev.define.pboc.EmvTag;
import com.centerm.cpay.midsdk.dev.define.pboc.EnumPbocFlow;
import com.centerm.cpay.midsdk.dev.define.pboc.EnumPbocResultType;
import com.centerm.cpay.midsdk.dev.define.pboc.EnumPbocSlot;
import com.centerm.cpay.midsdk.dev.define.pboc.TransParams;
import com.centerm.epos.EposApplication;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.bean.BinData;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.bean.TradePbocDetail;
import com.centerm.epos.bean.TradePrintData;
import com.centerm.epos.bean.transcation.IccRecordsInfo;
import com.centerm.epos.bean.transcation.TradeInformation;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.StatusCode;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.CommonManager;
import com.centerm.epos.redevelop.ICommonManager;
import com.centerm.epos.task.IcCardCheckTask;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DataHelper;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.NewEmvTag;
import com.centerm.epos.utils.SecurityTool;
import com.centerm.epos.utils.TlvUtil;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.utils.XLogUtil;
import com.centerm.epos.view.AlertDialog;
import com.centerm.smartpos.util.HexUtil;

import java.io.Serializable;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.cpay.midsdk.dev.define.pboc.EmvTag.EMVTAG_AID;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.KEY_LOAD_LOG;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.KEY_TRANS_LOG;
import static com.centerm.epos.common.TransDataKey.FLAG_IMPORT_AMOUNT;
import static com.centerm.epos.common.TransDataKey.FLAG_REQUEST_ONLINE;
import static com.centerm.epos.common.TransDataKey.KEY_HOLDER_NAME;
import static com.centerm.epos.common.TransDataKey.KEY_IC_CONTINUE_ONLINE;
import static com.centerm.epos.common.TransDataKey.KEY_IC_DATA_PRINT;
import static com.centerm.epos.common.TransDataKey.iso_f2;
import static com.centerm.epos.common.TransDataKey.iso_f39;
import static com.centerm.epos.common.TransDataKey.keyBalanceAmt;
import static com.centerm.epos.common.TransDataKey.keyBalanceAmtCode;
import static com.centerm.epos.common.TransDataKey.keyFlagFallback;

/**
 * Created by yuhc on 2017/2/15.
 * 检卡界面的业务逻辑处理
 */

public class CheckCardPresent extends BaseTradePresent implements ICheckCard {
    private int retryTimes;
    private EnumReadCardType cardType;
    private Handler retryHandler;
    private Runnable retryThread;
    private boolean isFallback = false;//是否降级
    private CommonDao<BinData> dao;
    private ICardReaderDev cardReaderDev;//读卡设备
    private IPbocService pbocService;//PBOC服务
    private int maxRetryTimes;

    IcCardCheckTask icCardCheckTask;

    public CheckCardPresent(ITradeView mTradeView) {
        super(mTradeView);
    }

    /*
    * 电子现金类交易
    * */
    public boolean bIsEcTransAndSetCardType() {
        boolean bIsTure = true;

        if (TransCode.E_BALANCE.equals(getTradeCode())
                || TransCode.EC_LOAD_RECORDS.equals(getTradeCode())
                || TransCode.EC_TRANS_RECORDS.equals(getTradeCode())
                || TransCode.EC_LOAD_CASH.equals(getTradeCode())
                || TransCode.EC_VOID_CASH_LOAD.equals(getTradeCode())
                || TransCode.E_REFUND.equals(getTradeCode())) {
            cardType = EnumReadCardType.INSERT_SWING;
        } else if (TransCode.E_QUICK.equals(getTradeCode())) {
            cardType = EnumReadCardType.SWING;
        } else if (TransCode.E_COMMON.equals(getTradeCode()) || TransCode.EC_LOAD_INNER.equals(getTradeCode())) {
            cardType = EnumReadCardType.INSERT;
        } else if ( TransCode.EC_LOAD_OUTER.equals(getTradeCode())
                    || TransCode.UNION_INTEGRAL_BALANCE.equals(getTradeCode())
                    || TransCode.UNION_INTEGRAL_REFUND.equals(getTradeCode())
                    || TransCode.ISS_INTEGRAL_SALE.equals(getTradeCode())
                    || TransCode.UNION_INTEGRAL_SALE.equals(getTradeCode())
                    || TransCode.ISS_INTEGRAL_VOID.equals(getTradeCode())
                    || TransCode.UNION_INTEGRAL_VOID.equals(getTradeCode())) {
            cardType = EnumReadCardType.SWIPE_INSERT;
        }
        else if( TransCode.MAG_ACCOUNT_VERIFY.equals(getTradeCode()) || TransCode.MAG_CASH_LOAD.equals(getTradeCode()) ){
            cardType = EnumReadCardType.SWIPE;
        }
        else if( TransCode.MAG_ACCOUNT_LOAD_VERIFY.equals(getTradeCode()) ){
            /*磁条卡账户充值第一次刷卡*/
            cardType = EnumReadCardType.SWIPE_INSERT;
        }else {
            bIsTure = false;
        }
        transDatas.put(TransDataKey.key_is_load_second_use_card, null);/*二次用卡*/
        transDatas.put(TransDataKey.KEY_EC_TIPS_CONFIRM,null);/*电子现金提示*/
        transDatas.put(KEY_IC_CONTINUE_ONLINE,null);/*ic卡交易是否继续联机*/
        return bIsTure;
    }

    /*
    * 现金圈存和指定账户圈存不支持
    * */
    private boolean bIsSupportFallBack() {
        return (TransCode.EC_LOAD_CASH.equals(getTradeCode())
                || TransCode.EC_LOAD_INNER.equals(getTradeCode())
                || TransCode.EC_LOAD_OUTER.equals(getTradeCode()));
    }

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        XLogUtil.w("bIsEcTransAndSetCardType", "getTradeCode():" + getTradeCode());
        if (!bIsEcTransAndSetCardType()) {

            if (mTradeInformation.isForceInsert()) {
                //强制插卡
                cardType = EnumReadCardType.INSERT;
            } else if (mTradeInformation.isForcePin()) {
                //闪付凭密
                cardType = EnumReadCardType.SWING;
            } else if (mTradeInformation.isPreferClss() && (TransCode.SALE.equals(getTradeCode()) || TransCode.AUTH.equals
                    (getTradeCode()))) {
                //优先挥卡
                cardType = EnumReadCardType.SWIPE_SWING;
            } else {
                cardType = EnumReadCardType.ALL;
            }
        }
        XLogUtil.w("bIsEcTransAndSetCardType", "cardType:" + cardType);
        dao = new CommonDao<>(BinData.class, dbHelper);
//        maxRetryTimes = mTradeView.getHostActivity().getConfigureManager().getDefaultParamsValue(mTradeView
// .getContext(),Keys.obj().)
        try {
            cardReaderDev = DeviceFactory.getInstance().getCardReaderDev();
            pbocService = DeviceFactory.getInstance().getPbocService();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCancel() {
        cancelIcCardCheckTask();
        try {
            ICardReaderDev cardReaderDev = DeviceFactory.getInstance().getCardReaderDev();
            cardReaderDev.stopReadCard();
            IPbocService pbocService = DeviceFactory.getInstance().getPbocService();
            pbocService.abortProcess();
            super.onCancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onCancel2() {
        cancelIcCardCheckTask();
        try {
            ICardReaderDev cardReaderDev = DeviceFactory.getInstance().getCardReaderDev();
            cardReaderDev.stopReadCard();
            IPbocService pbocService = DeviceFactory.getInstance().getPbocService();
            pbocService.abortProcess();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isOpenDataBase() {
        return true;
    }

    @Override
    public EnumReadCardType getCardType() {
        return cardType;
    }

    @Override
    public boolean isSupportScan() {
        // TODO: 2017/4/17 先屏幕扫码交易
        return mTradeInformation.getTransCode().equals(TransCode.SALE) && !mTradeInformation.isForceInsert() &&
                !mTradeInformation.isForcePin();
//        return false;
    }

    /**
     * 是否支持手输卡号，预授权撤销类和预授权完成可以手输卡号，且要判断对应的开关控制是否打开
     *
     * @return true 支持 false 不支持
     */
    @Override
    public boolean isEnableInputCardNumber() {
        //先检查手输卡号设置开关
        if (!BusinessConfig.getInstance().getFlag(mTradeView.getContext(), BusinessConfig.Key.TOGGLE_CARD_NUM_BY_HAND))
            return false;

        //再判断支持的业务
        String transType = mTradeInformation.getTransCode();
        if (TransCode.AUTH_COMPLETE.equals(transType) || TransCode.CANCEL.equals(transType))
            return true;

        return false;
    }

    @Override
    public void onButtonConfirm(int viewId, String data) {
        if (viewId == R.id.btn_confirm) {
            if (TextUtils.isEmpty(data))
                throw new RuntimeException("卡号不能为空！");
            if (data.length() < 12)
                throw new RuntimeException("卡号长度不足！");
            transDatas.put(TradeInformationTag.SERVICE_ENTRY_MODE, "01");
            transDatas.put(TradeInformationTag.BANK_CARD_NUM, data);
            transDatas.put(TradeInformationTag.IS_CARD_NUM_MANUAL, true);
            gotoNextStep();
        }
    }

    @Override
    public void onStartScanCode() {
        onCancelCheckCard();
    }

    @Override
    public void onGetScanCode(String code) {
        if (TextUtils.isEmpty(code))
            beginSearchCard();
        transDatas.put(TradeInformationTag.SCAN_CODE, code);
        transDatas.put(TradeInformationTag.SERVICE_ENTRY_MODE, "03");
        if (TransCode.SALE.equals(mTradeInformation.getTransCode())) {
            transDatas.put(TradeInformationTag.TRANSACTION_CODE, TransCode.SALE_SCAN);
            mTradeInformation.setTransCode(TransCode.SALE_SCAN);
        }
        gotoNextStep("2");
    }

    @Override
    public void beginTransaction() {
        beginSearchCard();
    }

    private void onRetry(final boolean addTimes) {
        if (retryHandler == null) {
            retryHandler = new Handler();
        }
        retryThread = null;
        retryThread = new Runnable() {
            @Override
            public void run() {
                if (addTimes) {
                    retryTimes++;
                }
                if (retryTimes > BusinessConfig.CHECK_CARD_RETRY_TIMES) {
                    /*
                    * @author zhouzhihua
                    * 异常情况下刷卡界面已经destroy 此处在提示导致应用崩溃
                    *2017.11.16
                    * */
                    if (mTradeView != null) {
                        mTradeView.popToast(R.string.tip_card_retry_limited);
                    }
                    onTransactionQuit();
                } else {
                    beginSearchCard();
                }
            }
        };
        retryHandler.postDelayed(retryThread, 1500);
    }

    @Override
    public void onTransactionQuit() {
        super.onTransactionQuit();
        if (retryHandler != null) {
            retryHandler.removeCallbacks(retryThread);
        }
        if (cardReaderDev != null) {
            cardReaderDev.stopReadCard();
            cardReaderDev = null;
        }
        if (pbocService != null) {
            pbocService.abortProcess();
            pbocService = null;
        }
        cancelIcCardCheckTask();
    }

    /**
     * 根据卡号判断是否属于银联国际卡。银联国际卡需要进行风险提示
     *
     * @param cardNo 卡号
     * @return 属于银联国际卡返回true，否则返回false
     */
    private boolean isUnionPayInternationalCard(String cardNo) {
        if (cardNo == null) {
            return false;
        }
        String bin = cardNo.length() > 6 ? cardNo.substring(0, 6) : cardNo;
        try {
            List<BinData> binList = dao.queryBuilder().where().like("cardBin", bin + "%").query();
            if (binList != null && binList.size() > 0) {
                for (int i = 0; i < binList.size(); i++) {
                    BinData item = binList.get(i);
                    if (cardNo.startsWith(item.getCardBin())) {
                        //卡组织：1-银联；2-银联国际卡；3-JCB；4-VISA；5-MASTER
                        if ("2".equals(item.getCardOrg().trim())) {
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    /*
    * 接触式IC卡 取消 检查移卡的异步事件
    * */
    private void cancelIcCardCheckTask(){
        if( null != icCardCheckTask  && !icCardCheckTask.isCancelled() ){
            icCardCheckTask.cancel(true);
        }
        icCardCheckTask = null;
    }

    @Override
    public void onExit() {
        onCancelCheckCard();
        cancelIcCardCheckTask();
    }

    private void onCancelCheckCard() {
        try {
            ICardReaderDev cardReaderDev = DeviceFactory.getInstance().getCardReaderDev();
            cardReaderDev.stopReadCard();
            IPbocService pbocService = DeviceFactory.getInstance().getPbocService();
            pbocService.abortProcess();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    * 接触式IC卡参数设置
    * */
    private void setIccContactPBOCParams() {
        if (mTradeInformation.getPbocParams() == null) {
            mTradeInformation.setPbocParams(new TransParams(EnumPbocSlot.SLOT_IC, EnumPbocFlow
                    .PBOC_FLOW));
        } else {
            mTradeInformation.getPbocParams().setFlow(EnumPbocFlow.PBOC_FLOW);
            mTradeInformation.getPbocParams().setSlot(EnumPbocSlot.SLOT_IC);
        }
        mTradeInformation.getTransDatas().put(TradeInformationTag.SERVICE_ENTRY_MODE, "05");
        //退货流程需输完金额才开启PBOC流程，在TradingActivity中开启
        if ( TransCode.REFUND.equals(mTradeInformation.getTransCode() )
                || TransCode.AUTH_COMPLETE.equals(mTradeInformation.getTransCode())
                || TransCode.CANCEL.equals(mTradeInformation.getTransCode())
                || TransCode.E_REFUND.equals(mTradeInformation.getTransCode())
                || TransCode.UNION_INTEGRAL_REFUND.equals(mTradeInformation.getTransCode())
                || TransCode.MAG_ACCOUNT_LOAD.equals(mTradeInformation.getTransCode())
                || TransCode.MAG_ACCOUNT_LOAD_VERIFY.equals(mTradeInformation.getTransCode())) {
            //消费，先输金额再检卡
            //预授权/预授权撤销/预授权完成，先检卡再输金额
            //预授权按照客户需求，改为跟消费一致的流程
            mTradeInformation.getPbocParams().setRequestAmtAfterCardNo(true);
        } else if (TransCode.E_COMMON.equals(getTradeCode())
                || TransCode.E_BALANCE.equals(getTradeCode())) {
            /*
            *接触式电子现金余额
            * */
            mTradeInformation.getPbocParams().setSupportEc(true);
            mTradeInformation.getPbocParams().setForceOnline(false);
        } else if (TransCode.EC_LOAD_CASH.equals(getTradeCode())
                || TransCode.EC_LOAD_INNER.equals(getTradeCode())) {
            mTradeInformation.getPbocParams().setForceOnline(true);
            mTradeInformation.getPbocParams().setRequestAmtAfterCardNo(true);
        } else if (TransCode.EC_LOAD_OUTER.equals(getTradeCode())) {
            /*
            * 非指定账户圈存转出卡执行到数据认证
            * */
            mTradeInformation.getPbocParams().setForceOnline(true);
            mTradeInformation.getPbocParams().setRequestAmtAfterCardNo(true);
        }
    }

    /*
    * 非接触式IC卡参数设置
    * */
    private void setIccContactlessPBOCParams() {
        mTradeInformation.getTransDatas().put(TradeInformationTag.SERVICE_ENTRY_MODE, "07");
        if (mTradeInformation.getPbocParams() == null) {
            mTradeInformation.setPbocParams(new TransParams(EnumPbocSlot.SLOT_RF, EnumPbocFlow.QPBOC_FLOW));
        } else {
            mTradeInformation.getPbocParams().setFlow(EnumPbocFlow.QPBOC_FLOW);
            mTradeInformation.getPbocParams().setSlot(EnumPbocSlot.SLOT_RF);
        }
        //退货流程需输完金额才开启PBOC流程，在TradingActivity中开启
        if (TransCode.AUTH.equals(mTradeInformation.getTransCode())
                || TransCode.REFUND.equals(mTradeInformation.getTransCode())
                || TransCode.AUTH_COMPLETE.equals(mTradeInformation.getTransCode())
                || TransCode.CANCEL.equals(mTradeInformation.getTransCode())
                || TransCode.E_REFUND.equals(mTradeInformation.getTransCode())) {
            //消费，在卡号确认前输入金额
            //预授权，在卡号确认后输入金额
            mTradeInformation.getPbocParams().setRequestAmtAfterCardNo(true);
        } else if (TransCode.E_QUICK.equals(getTradeCode()) || TransCode.E_COMMON.equals(getTradeCode())
                || TransCode.E_BALANCE.equals(getTradeCode())
                || TransCode.EC_LOAD_RECORDS.equals(getTradeCode())
                || TransCode.EC_TRANS_RECORDS.equals(getTradeCode())) {
            mTradeInformation.getPbocParams().setSupportEc(true);
            mTradeInformation.getPbocParams().setForceOnline(false);
        } else if (TransCode.EC_LOAD_CASH.equals(getTradeCode())
                || TransCode.EC_LOAD_INNER.equals(getTradeCode())
                || TransCode.EC_LOAD_OUTER.equals(getTradeCode())) {
            mTradeInformation.getPbocParams().setForceOnline(true);
            mTradeInformation.getPbocParams().setRequestAmtAfterCardNo(false);
        }
    }

    /**
     * 开始读卡
     */
    private void beginSearchCard() {
        /*BUGID:0003538
        * @author zhouzhihua 2017.11.27
        *增加mTradeView!=null的判断，非接读卡异常时容易导致程序崩溃。
        * */
        if (cardReaderDev != null && null != mTradeView) {
            CardReaderListener listener = new CardReaderListener() {
                @Override
                public void onSuccess(final CardInfo cardInfo) {
                    /*读卡后在进入输密界面，立刻点击返回键导致程序崩溃
                    *还未引出各种情况的异常，直接在密码界面，取消返回按键，即可
                    * */
                    if (mTradeView == null) {
                        return;
                    }
                    switch (cardInfo.getCardType()) {
                        case MAG_CARD:
                            String cardNo = cardInfo.getCardNo();
                            if ((TransCode.SALE.equals(mTradeInformation.getTransCode()) || TransCode.AUTH.equals
                                    (mTradeInformation.getTransCode())) &&
                                    isUnionPayInternationalCard(cardNo)) {
                                mTradeView.popMessageBox("提示", "银联国际卡交易手续费高！\n确定继续", new AlertDialog
                                        .ButtonClickListener() {
                                    @Override
                                    public void onClick(AlertDialog.ButtonType button, View v) {
                                        switch (button) {
                                            case POSITIVE:
                                                onSwipeCardSuccess(cardInfo, true);
                                                break;
                                            case NEGATIVE:
                                                onTransactionQuit();
                                                break;
                                        }
                                    }
                                });
                            } else if (TextUtils.isEmpty(cardNo)) {
                                mTradeView.popToast(R.string.tip_read_card_failed);
                                onRetry(true);
                            } else {
                                onSwipeCardSuccess(cardInfo, false);
                            }
                            break;
                        case IC_CARD:
                            setIccContactPBOCParams();
                            beginPbocProcess();//开启PBOC流程
                            mTradeView.popLoading(Settings.bIsSettingBlueTheme() ? R.string.tip_ic_Read_processing : R.string.tip_ic_on_processing);
                            break;
                        case RF_CARD:
                            setIccContactlessPBOCParams();
                            beginPbocProcess();//开启PBOC流程
                            break;
                    }
                }

                @Override
                public void onFailure() {
                    String mode = (String) mTradeInformation.getTransDatas().get(TradeInformationTag.SERVICE_ENTRY_MODE);
                    if ("05".equals(mode) && (!bIsSupportFallBack())) {
                        mTradeView.popToast("IC卡读卡失败，请刷卡！");
                        cardType = EnumReadCardType.INSERT_SWING;
                        onRetry(false);
                    } else {
                        //提示并且重试，重试次数以BusinessConfig当中的为准
                        mTradeView.popToast(R.string.tip_read_card_failed);
                        onRetry(true);
                    }
                }

                @Override
                public void onTimeout() {
                    //检卡超时，退回到主界面
                    if (mTradeView != null) {
                        mTradeView.popToast(R.string.tip_read_card_timeout);
                        onTransactionQuit();
                    }
                }

                @Override
                public void onCanceled() {
                    //这里暂时无需处理
                }

                @Override
                public void onError(int i, String s) {
                    //提示并且重试，重试次数以BusinessConfig当中的为准
//                    mTradeView.popToast(R.string.tip_read_card_failed);
//                    onRetry(true);
                    onFailure();
                }
            };

            cardReaderDev.beginReadCard(cardType, BusinessConfig.getInstance().getNumber(mTradeView.getContext(),
                    BusinessConfig.Key.KEY_TRADE_VIEW_OP_TIMEOUT) * 1000, listener);

        }
    }
    private boolean bIsCheckSameCardNo(String transCode){
        return (TransCode.EC_VOID_CASH_LOAD.equals(transCode)
               ||TransCode.ISS_INTEGRAL_VOID.equals(transCode)
               ||TransCode.UNION_INTEGRAL_VOID.equals(transCode));
    }

    public void onSwipeCardSuccess(CardInfo cardInfo, boolean isUnionInternationalCard) {
        transDatas.put(TradeInformationTag.SERVICE_ENTRY_MODE, "02");
        String track1 = cardInfo.getTrack1();
        transDatas.put(KEY_HOLDER_NAME, DataHelper.extractName(track1));
        String track2 = cardInfo.getTrack2();
        String track3 = cardInfo.getTrack3();
        if (TextUtils.isEmpty(track2)) {
            logger.warn("二磁道数据为空");
            // TODO: 2017/9/29 是否兼容有时无2磁，有3磁、卡号的情况
            mTradeView.popToast(R.string.tip_read_card_failed);
            onRetry(true);
            return;
        }
        transDatas.put(TradeInformationTag.TRACK_2_DATA, track2);
        transDatas.put(TradeInformationTag.BANK_CARD_NUM, cardInfo.getCardNo());
        String expDate = cardInfo.getExpDate();
        if (expDate != null && expDate.length() >= 4) {
            expDate = expDate.substring(0, 4);
        }
        transDatas.put(TradeInformationTag.DATE_EXPIRED, expDate);
        if (!TextUtils.isEmpty(track3)) {
            transDatas.put(TradeInformationTag.TRACK_3_DATA, track3);
        }
        if (isFallback) {
            //注明降级交易
            transDatas.put(keyFlagFallback, "1");
        }
        /*磁条卡现金充值，或者磁条卡账户充值二次刷卡 不检查IC*/
        if (!isFallback && CommonUtils.isIcCard(track2) && !( TransCode.MAG_CASH_LOAD.equals(getTradeCode()) || TransCode.MAG_ACCOUNT_VERIFY.equals(getTradeCode())) ) {
            if (BusinessConfig.getInstance().getFlag(mTradeView.getHostActivity(), BusinessConfig.Key
                    .FLAG_PREFER_CLSS)) {
                mTradeView.popToast(R.string.tip_force_clss);
            } else {
                mTradeView.popToast(R.string.tip_force_ic);
            }
            onRetry(false);
        } else {
            if (TransCode.VOID.equals(mTradeInformation.getTransCode()) || TransCode.COMPLETE_VOID.equals
                    (mTradeInformation.getTransCode()) || bIsCheckSameCardNo(mTradeInformation.getTransCode())) {
                Map<String, String> dataMap = mTradeInformation.getDataMap();
                if (!dataMap.get(iso_f2).equals(cardInfo.getCardNo())) {
                    ViewUtils.showToast(mTradeView.getContext(), "原交易卡号不匹配");
                    mTradeView.getHostActivity().jumpToNext("98");
                    return;
                }
            }

            if (isUnionInternationalCard && (TransCode.SALE.equals(mTradeInformation.getTransCode()) || TransCode
                    .AUTH.equals(mTradeInformation.getTransCode()))) {
                mTradeView.getHostActivity().jumpToNext("3");//银联国际卡要求输入主管密码
            } else {
                mTradeView.getHostActivity().jumpToNext();
            }
        }
    }

    public void jump(){
        mTradeView.getHostActivity().jumpToNext();
    }

    /*
    * 非指定账户圈存必须检查是否移卡
    * */
    private void checkCardMove() {
        DialogFactory.hideAll();
        icCardCheckTask = new IcCardCheckTask(mTradeView.getContext()){
            @Override
            public void onFinish(String[] status) {
                if(mTradeView != null ) {
                    gotoNextStep();
                }
            }
        };
        icCardCheckTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,new String[3]);
    }

    private boolean bIsContactLessEcLoad(TradeInformation tradeInformation){

        if( tradeInformation == null ){ return false; }
        String transCode = tradeInformation.getTransCode();
        String entryMode = (String)tradeInformation.getTransDatas().get(TradeInformationTag.SERVICE_ENTRY_MODE);

        return ( (transCode.equals(TransCode.EC_LOAD_CASH) || transCode.equals(TransCode.EC_LOAD_INNER)) && entryMode.equals("07") );
    }

    private boolean bIsContactLess(TradeInformation tradeInformation){

        if( tradeInformation == null ){ return false; }
        String entryMode = (String)tradeInformation.getTransDatas().get(TradeInformationTag.SERVICE_ENTRY_MODE);

        return ( (null != entryMode) && entryMode.equals("07") );
    }


    @Override
    public boolean onPbocImportAmount(){
        boolean bIsTrue = false;
        logger.warn("onPbocImportAmount bIsContactLessEcLoad ： " + bIsContactLessEcLoad(mTradeInformation));
        if( bIsContactLessEcLoad(mTradeInformation) ) {
            transDatas.put(FLAG_IMPORT_AMOUNT, "1");
            gotoNextStep();
            bIsTrue =  true;
        }
        return bIsTrue;
    }

    @Override
    public boolean onPbocChangeUserInterface() {
        String mode = (String) mTradeInformation.getTransDatas().get(TradeInformationTag.SERVICE_ENTRY_MODE);
        DialogFactory.hideAll();
        if ("05".equals(mode) && !bIsSupportFallBack() ) {
            mTradeView.popToast("IC卡读卡失败，请刷卡！");
            cardType = EnumReadCardType.SWIPE_INSERT;
            isFallback = true;
            onRetry(false);
//            //关闭交易降级
//            mTradeView.popToast("IC卡读卡失败，请重试！");
//            cardType = EnumReadCardType.ALL;
//            onRetry(true);
        } else {
            //提示并且重试，重试次数以BusinessConfig当中的为准
            mTradeView.popToast(R.string.tip_read_card_failed);
            onRetry(true);
        }
        return true;
    }

    @Override
    public boolean onPbocRequestTipsConfirm(String tips) {
        if( tips == null || ( bIsContactLess(mTradeInformation) && TransCode.E_BALANCE.equals(getTradeCode()) ) ){
            return false;
        }
        XLogUtil.w("onPbocRequestEcTipsConfirm","check card fragment");
        mTradeView.showSelectDialog("提示", tips+",\n确认继续", new AlertDialog.ButtonClickListener() {
            @Override
            public void onClick(AlertDialog.ButtonType button, View v) {
                switch (button) {
                    case POSITIVE:
                        pbocService.importResult(EnumPbocResultType.MSG_CONFIRM, true);
                        mTradeView.popLoading(Settings.bIsSettingBlueTheme() ? R.string.tip_ic_Read_processing : R.string.tip_ic_on_processing);
                        break;
                    case NEGATIVE:
                        mTradeView.popLoading(Settings.bIsSettingBlueTheme() ? R.string.tip_ic_Read_processing : R.string.tip_ic_on_processing);
                        jumpToResult("U001","用户取消，交易终止");
                        break;
                }
            }
        });
        return true;
    }
    @Override
    public boolean onPbocRequestUserAidSelect(String[] aidList)
    {
        if( aidList == null || aidList.length == 1 ){
            return false;
        }
        pbocService.importAidSelectResult(1);
//        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mTradeView.getContext());
//
//
//        builder.setTitle(R.string.tip_user_aid_select).setCancelable(true).setSingleChoiceItems(aidList, -1, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
//                pbocService.importAidSelectResult(which+1);
//                dialog.dismiss();
//            }
//        }).create().show();

        //mTradeView.getHostActivity()

        //mTradeView.getHostActivity().f


        return true;
    }
    /*
    * 卡号确认
    * */
    private boolean confirmCardNo(String cardNo) {
        transDatas.put(TradeInformationTag.BANK_CARD_NUM, cardNo);
        if (!mTradeView.getHostActivity().isPbocTerminated()) {
            String transCode = getTradeCode();
            Context context = mTradeView.getContext();
            //消费撤销和预授权完成撤销判断卡号是否符合
            if (TransCode.VOID.equals(transCode) || TransCode.COMPLETE_VOID.equals(transCode) || bIsCheckSameCardNo(mTradeInformation.getTransCode()) ) {
                Map<String, String> dataMap = mTradeInformation.getDataMap();
                if (!dataMap.get(iso_f2).equals(cardNo)) {
                    ViewUtils.showToast(context, "原交易卡号不匹配");
                    gotoNextStep("98");
                    return true;
                }
            }
            //目前只有插卡消费和预授权/余额查询要执行完整PBOC流程，其它业务只是取卡信息
            if( TransCode.E_COMMON.equals(transCode) ){
                mTradeInformation.setTransCode(TransCode.SALE);
                transCode = getTradeCode();
            }
            if (TransCode.FULL_PBOC_SETS.contains(transCode))
                if( TransCode.EC_LOAD_OUTER.equals(transCode) ) {
                    pbocService.abortProcess();
                    checkCardMove();
                    return true;
                }
                else {
                    pbocService.importResult(EnumPbocResultType.CARD_INFO_CONFIRM, true);
                }
            else {
                stopPbocProcess();
                if( TransCode.MAG_ACCOUNT_LOAD.equals(transCode) || TransCode.MAG_ACCOUNT_LOAD_VERIFY.equals(transCode) ){
                    checkCardMove(); return true;
                }
            }
            gotoNextStep();
        }
        return true;
    }
    /*接触式普通支付卡号确认流程*/
    private boolean bIsContactEcCommonConfirmCardNo(final String cardNo){
        String ecTipsConfirm = (String)transDatas.get(TransDataKey.KEY_EC_TIPS_CONFIRM);
        XLogUtil.w("onPbocConfirmCardNo","isSupportEc:"+mTradeInformation.getPbocParams().isSupportEc()+"  ecTipsConfirm:"+ecTipsConfirm);
        /*接触式脱机交易需要显示卡号*/
        if( TransCode.E_COMMON.equals(getTradeCode())
                && mTradeInformation.getPbocParams().isSupportEc()
                && ( ecTipsConfirm != null && ecTipsConfirm.equals("1") ) ){
            transDatas.put(TransDataKey.KEY_EC_TIPS_CONFIRM,null);
            /*接触式电子现金在导入金额后，
            *会获取电子现金余额，
            *如果余额大于交易金额，
            *进行卡号确认，卡号确认后根据实际情况，
            *走输密或者联机或者脱机或者拒绝
            */
            transDatas.put(TradeInformationTag.BANK_CARD_NUM, cardNo);
            gotoNextStep("3");
            return true;
        }
        return false;
    }
    /*接触式电子现金 现金充值卡号确认流程*/
    private boolean bIsContactEcLoadCashConfirmCardNo(final String cardNo){

        String snm = (String)mTradeInformation.getTransDatas().get(TradeInformationTag.SERVICE_ENTRY_MODE);
        if( (snm != null && snm.equals("05")) && TransCode.EC_LOAD_CASH.equals(getTradeCode()) )
        {
            mTradeView.showSelectDialog("提示", "卡号："+ cardNo, new AlertDialog.ButtonClickListener() {
                @Override
                public void onClick(AlertDialog.ButtonType button, View v) {
                    switch (button) {
                        case POSITIVE:
                            mTradeView.popLoading(Settings.bIsSettingBlueTheme() ? R.string.tip_ic_Read_processing : R.string.tip_ic_on_processing);
                            confirmCardNo(cardNo);
                            break;
                        case NEGATIVE:
                            mTradeView.popLoading(Settings.bIsSettingBlueTheme() ? R.string.tip_ic_Read_processing : R.string.tip_ic_on_processing);
                            jumpToResult("U001","用户取消，交易终止");
                            break;
                    }
                }
            });
            return true;
        }
        return false;
    }

    /*
    *卡号确认
    * @param cardNo 卡号
    * @return true/false
    * */
    @Override
    public boolean onPbocConfirmCardNo(final String cardNo) {
        transDatas.put(TradeInformationTag.BANK_CARD_NUM, cardNo);

        if( bIsContactEcCommonConfirmCardNo(cardNo) ){
            return true;
        }
        else if( bIsContactEcLoadCashConfirmCardNo(cardNo) ){
            return true;
        }
        return confirmCardNo(cardNo);
    }

    private void stopPbocProcess() {
        pbocService.abortProcess();
        transDatas.put(FLAG_REQUEST_ONLINE, "1");
    }
    /*
   * 判断ic卡是否为纯电子现金卡
   * @param no
   * @return true-yes,false-no
   * */
    private boolean bIsPureEcCard(){
        String pureAid ="A000000333010106";
        Map<String, String> aidMap = pbocService.readKernelData(EMVTAG_AID);

        XLogUtil.w("IsPureEcCard",""+aidMap);
        return ((aidMap != null) && pureAid.equals(aidMap.get("4F")));
    }
    /*
    * 纯电子现金卡不能进行非圈存类的联机
    * @return true-yes,false-no
    * */
    private boolean bIsPureEcCardTransTerminate(){
        boolean bIsTerminate = false;
        String transCode = getTradeCode();
        if( this.bIsPureEcCard() ){
            if( !transCode.equals(TransCode.EC_LOAD_CASH)
                && !transCode.equals(TransCode.EC_VOID_CASH_LOAD)
                && !transCode.equals(TransCode.EC_LOAD_INNER)
                && !transCode.equals(TransCode.EC_LOAD_OUTER) ){
                bIsTerminate = true;
            }
        }
        if( !bIsTerminate ) { return bIsTerminate; }

        switch (transCode){
            case TransCode.SALE: jumpToResult(StatusCode.PRUE_PLZ_SELECT_EC_TRANS.getStatusCode(),mTradeView.getHostActivity().getString(StatusCode.PRUE_PLZ_SELECT_EC_TRANS.getMsgId())); break;
            default: jumpToResult(StatusCode.AUTH_AMOUNT_NOT_PRUE_EC.getStatusCode(),mTradeView.getHostActivity().getString(StatusCode.AUTH_AMOUNT_NOT_PRUE_EC.getMsgId())); break;
        }
        return bIsTerminate;
    }

    @Override
    public boolean onPbocRequestOnline() {
        transDatas.put(FLAG_REQUEST_ONLINE, "1");
        if( this.bIsPureEcCardTransTerminate() ){
            return true;
        }
        String transCode = getTradeCode();
        Context context = mTradeView.getContext();
        if (TransCode.VOID.equals(transCode) || TransCode.COMPLETE_VOID.equals(transCode)) {
            String cardNo = (String) transDatas.get(TradeInformationTag.BANK_CARD_NUM);
            Map<String, String> dataMap = mTradeInformation.getDataMap();
            if (!dataMap.get(iso_f2).equals(cardNo)) {
                ViewUtils.showToast(context, "原交易卡号不匹配");
                gotoNextStep("98");
                return true;
            }
        }
        logger.warn("onPbocRequestOnline transCode ： " + transCode);

        if (TransCode.SALE.equals(transCode) || TransCode.AUTH.equals(transCode)) {
            //消费业务，输入金额在检卡之前，判断是否属于小额免密业务，小额免密可以直接进行联机
            boolean[] qpsCondition = mTradeView.getHostActivity().getQpsCondition();
            if (qpsCondition[0]) {
                transDatas.put(TransDataKey.KEY_QPS_FLAG, "true");//小额免密标识，用于组59域报文
                gotoNextStep("2");
            } else {
                gotoNextStep();
            }
        }
        else{
            if(TransCode.E_QUICK.equals(transCode) || TransCode.E_COMMON.equals(transCode)){
            /*
            * 快速支付走联机，和普通消费流程一致
            * 纯电子现金卡是否可以联机，有待确认。
            * "\xA0\x00\x00\x03\x33\x01\x01\x06" 纯卡 aid
            * */
                mTradeInformation.setTransCode(TransCode.SALE);
            }
            gotoNextStep();
        }

        return true;
    }
    /*
    * IC卡脱机余额
    * 多币种的需要正确显示各个币种的余额情况。
    * */
    @Override
    public boolean onReturnOfflineBalance(String code1, String balance1, String code2, String balance2){
        logger.info("读取到电子现金：code1 = "+code1+"; balance1="+balance1+"; code2="+code2 + "; balance2="+balance2);
        tempMap.put(iso_f39, "00");

        tempMap.put(keyBalanceAmtCode, code1);
        tempMap.put(keyBalanceAmt, balance1);

        tempMap.put(TransDataKey.keyBalanceAmtSecondCode,code2);
        tempMap.put(TransDataKey.keyBalanceAmtSecond,balance2);

        gotoNextStep();
        return true;
    }
    public List<EmvTag.Tag> getCardInfoTags() {
        List<EmvTag.Tag> tagList = new ArrayList<>();
        tagList.add(EmvTag.Tag._57);//二磁
        tagList.add(EmvTag.Tag._5F24);//卡片失效日期
        tagList.add(EmvTag.Tag._5F34);//卡序列号
        tagList.add(EmvTag.Tag._9F79);
        tagList.add(EmvTag.Tag._82);
        return tagList;
    }

    /**
     * 将ic卡数据保存到数据库
     */
    private void saveIcOfflinePrintData( Map<String, String> tempMap ) {
        String unKnown = null;
        String aid = null;
        String tc = null;
        String iad = null;
        String atc = null;
        String tvr = null;
        String tsi = null;
        String aip = null,csn = null;
        String limitAmount = null;

        String icData = tempMap.get(TransDataKey.KEY_IC_DATA_PRINT);
        if (null != icData) {
            Map<String, String> stringMap = TlvUtils.tlvToMap(icData);
            //不可预知数
            unKnown = stringMap.get("9F37");
            aid = stringMap.get("4F");
            tc = stringMap.get("9F26");
            iad = stringMap.get("9F10");
            atc = stringMap.get("9F36");
            tvr = stringMap.get("95");
            tsi = stringMap.get("9B");
            aip = stringMap.get("82");
        }
        TradePrintData tradePrintData = new TradePrintData();
        if (null != tempMap.get(TradeInformationTag.TRACE_NUMBER)) {
            tradePrintData.setIso_f11(tempMap.get(TradeInformationTag.TRACE_NUMBER));
        }
        if (null != tc) {
            tradePrintData.setTc(tc);
        }
        if (null != tvr) {
            tradePrintData.setTvr(tvr);
        }
        if (null != aid) {
            tradePrintData.setAid(aid);
        }
        if (null != atc) {
            tradePrintData.setAtc(atc);
        }
        if (null != tsi) {
            tradePrintData.setTsi(tsi);
        }
        if (null != unKnown) {
            tradePrintData.setUmpr_num(unKnown);
        }
        if (null != aip) {
            tradePrintData.setAip(aip);
        }
        if (null != iad) {
            tradePrintData.setIad(iad);
        }
        if (null != limitAmount) {
            tradePrintData.setAmount(limitAmount);
        }

        tradePrintData.setNoNeedSign(false);
        tradePrintData.setNoNeedPin(false);
        tradePrintData.setRePrint(false);
        CommonDao<TradePrintData> printDataCommonDao = new CommonDao<>(TradePrintData.class, dbHelper);
        printDataCommonDao.save(tradePrintData);
    }
    /**
     * 检查交易记录是否已经存满了，如果满了则置位标志
     */
    private void checkTradeStorage() {
        try {
            ICommonManager commonManager = (ICommonManager) ConfigureManager.getInstance(mTradeView.getHostActivity()).getSubPrjClassInstance(new CommonManager());
            long counts = commonManager.getBatchCount();
            long config = BusinessConfig.getInstance().getNumber(mTradeView.getHostActivity(),
                    BusinessConfig.Key.KEY_MAX_TRANSACTIONS);
            logger.info("已存储成功流水数量==>" + counts + "==>终端最大存储数量==>" + config);
            if (counts >= config) {
                logger.warn("交易流水数量超限==>下次联机前将进行批结算");
                BusinessConfig.getInstance().setFlag(mTradeView.getHostActivity(), BusinessConfig
                        .Key.FLAG_TRADE_STORAGE_WARNING, true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private Map<String, String> convertObject2String(Map<String, Object> objMap) {
        if (objMap == null || objMap.size() == 0)
            return null;
        Map<String, String> strMap = new HashMap<>();
        Iterator<Map.Entry<String, Object>> iterator = objMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            if (entry.getValue() instanceof String)
                strMap.put(entry.getKey(), (String) entry.getValue());
        }
        return strMap;
    }

    private boolean saveIccOffLineTrans(boolean bIsApproved){
        Context context = null;
        Map<String, String> cardInfo = pbocService.readKernelData(this.getCardInfoTags());
        context = mTradeView.getContext();

        logger.info("IC卡卡片信息读取成功：" + cardInfo.toString());

        String balance = cardInfo.get("9F79");

        if( null != balance ) {
            balance = String.format(Locale.CHINA, "%d.%02d", Long.parseLong(balance) / 100, Long.parseLong(balance) % 100);
        }
        transDatas.put(TradeInformationTag.EC_TRANS_BALANCE,balance);

        /*
        * 测试使用
        *
        *bIsApproved = !"0.02".equals(transDatas.get(TradeInformationTag.TRANS_MONEY));
        */

        String tag57 = cardInfo.get("57");
        transDatas.put(TradeInformationTag.BANK_CARD_NUM, tag57.split("D")[0]);
        transDatas.put(TradeInformationTag.TRANSACTION_TYPE, getTradeCode());

        transDatas.put(TradeInformationTag.MERCHANT_NAME, BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), BusinessConfig.Key.KEY_MCHNT_NAME));
        transDatas.put(TradeInformationTag.TRANS_YEAR, String.format(Locale.CHINA, "%04d", Calendar.getInstance().get(Calendar.YEAR)));

        transDatas.put(TradeInformationTag.TERMINAL_IDENTIFICATION, BusinessConfig.getInstance().getIsoField(context, 41));
        transDatas.put(TradeInformationTag.MERCHANT_IDENTIFICATION, BusinessConfig.getInstance().getIsoField(context, 42));

        transDatas.put(TradeInformationTag.CARD_SEQUENCE_NUMBER,cardInfo.get("5F34"));

        String posSerial = BusinessConfig.getInstance().getPosSerial(context);
        transDatas.put(TradeInformationTag.TRACE_NUMBER, posSerial);
        transDatas.put(TradeInformationTag.BATCH_NUMBER, BusinessConfig.getInstance().getBatchNo(context));

        String operatorID = BusinessConfig.getInstance().getValue(mTradeView.getContext(), BusinessConfig.Key.KEY_OPER_ID);
        tempMap.put(TradeInformationTag.OPERATOR_CODE, operatorID);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String tradeDate = formatter.format(new Date());

        transDatas.put(TradeInformationTag.TRANS_TIME, tradeDate.substring(8, 14)); //保存脱机交易时间
        transDatas.put(TradeInformationTag.TRANS_DATE, tradeDate.substring(4, 8)); //保存脱机交易时间
        transDatas.put(TradeInformationTag.TRACK_2_DATA, tag57);
        transDatas.put(TradeInformationTag.DATE_SETTLEMENT,null);
        transDatas.put(TradeInformationTag.REFERENCE_NUMBER,null);

        String expiry = cardInfo.get("5F24");
        if (expiry != null && expiry.length() == 6) {
            expiry = expiry.substring(0, 4);
        }
        transDatas.put(TradeInformationTag.DATE_EXPIRED, expiry);
        transDatas.put(TradeInformationTag.CARD_SEQUENCE_NUMBER, cardInfo.get("5F34"));

        String iso55 = pbocService.readTlvKernelData(NewEmvTag.getF55TagsOffLine()); //读55域数据

        Map<String,String> map = TlvUtils.tlvToMap(iso55);
        map.put("9F27",bIsApproved ? "40" : "00");
        iso55 = TlvUtil.mapToTlv(map);
        transDatas.put(TradeInformationTag.IC_DATA, iso55 + ( bIsApproved ? "8A025931" : "8A025A31" ));//电子现金手动补Y1 ,拒绝Z1

        String print = pbocService.readTlvKernelData(EmvTag.getTagsForPrint()); //读取打印数据
        print += "8202"+cardInfo.get("82");

        logger.debug("ic卡打印信息+55域为：" + print + "/" + iso55);

        transDatas.put(TradeInformationTag.RESPONSE_CODE, "00");

        transDatas.put(KEY_IC_DATA_PRINT, print);
        transDatas.put(TradeInformationTag.CURRENCY_CODE,"156");

        transDatas.put(TradeInformationTag.BANKCARD_ORGANIZATION,"CUP");
        transDatas.put(TradeInformationTag.CREDIT_CODE,"CUP");

        /*用以下方式保存脱机交易数据*/

        tempMap.putAll(convertObject2String(transDatas));

        mTradeInformation.setRespDataMap(transDatas);/*离线交易发送包和接受包数据一致*/

        this.saveIcOfflinePrintData(tempMap);

        logger.warn("transDatas:" + transDatas.toString());

        if(TransCode.NEED_INSERT_TABLE_SETS.contains(getTradeCode())){
            CommonDao<TradeInfoRecord> tradeDao = new CommonDao<>(TradeInfoRecord.class, dbHelper);
            TradeInfoRecord curTradeInfo = new TradeInfoRecord(getTradeCode(), transDatas);
            TradePbocDetail pbocDetail = new TradePbocDetail(TlvUtils.tlvToMap(tempMap.get(KEY_IC_DATA_PRINT)));
            curTradeInfo.setTransStatus(bIsApproved ? 0 : 0x0800);
            logger.warn("pbocDetail:" + pbocDetail.convert2Map());
            curTradeInfo.setPbocDetail(pbocDetail);
            curTradeInfo.getPbocDetail().setECInfoEx(tempMap);
            tradeDao.save(curTradeInfo);
        }
        this.checkTradeStorage();
        return true;
    }
    /*
    * 脱机消费拒绝,脱机拒绝的交易在批结算时要上送拒绝的交易
    * CID 9f27 0x00  AAC
    * */
    @Override
    public boolean onPbocTradeRefused(){
        if( getTradeCode().equals(TransCode.E_QUICK)
            || getTradeCode().equals(TransCode.E_COMMON) ) {
            logger.info("脱机消费 onPbocTradeRefused");
            saveIccOffLineTrans(false);
            putResponseCode(StatusCode.TRADING_REFUSED);
            gotoNextStep("99");
            return true;
        }
        return false;
    }
    /*
    * 脱机消费批准
    * CID 9f27 0x40 TC
    * */
    @Override
    public boolean onPbocTradeApproved(){
        if(getTradeCode().equals(TransCode.E_QUICK) || getTradeCode().equals(TransCode.E_COMMON) ){
            logger.info("脱机消费 onPbocTradeApproved");
            saveIccOffLineTrans(true);
            putResponseCode("00","电子现金消费成功");
            gotoNextStep("2");
            return true;
        }
        return false;
    }


    @Override
    public boolean onReturnCardTransLog(List<Parcelable> data) {

        if( (data != null) && ( data.size() > 0 ) )
        {
            List<IccRecordsInfo> iccRecordsInfoList = new ArrayList<>();
            for( int i = 0 ; i < data.size(); i++ ){
                iccRecordsInfoList.add(new IccRecordsInfo((CardTransLog)data.get(i)));
            }
            mTradeView.getHostActivity().jumpToNext( KEY_TRANS_LOG , (Serializable)iccRecordsInfoList );
        }else{
            putResponseCode("N002","没有交易日志");
            mTradeView.getHostActivity().jumpToNext("2");
        }
        return true;
    }
    @Override
   public boolean onReturnCardLoadLog(List<Parcelable> data){
        XLogUtil.w("onReturnCardLoadLog"," "+data);
        if( (data != null) && ( data.size() > 0 ) )
        {
            List<CardLoadLog> iccRecordsInfoList = new ArrayList<>();
            for( int i = 0 ; i < data.size(); i++ ){
                iccRecordsInfoList.add((CardLoadLog)data.get(i));
            }
            mTradeView.getHostActivity().jumpToNext( KEY_LOAD_LOG , (Serializable)iccRecordsInfoList );
        }
        else{
            putResponseCode("N001","没有圈存日志");
            mTradeView.getHostActivity().jumpToNext("2");
        }
        return true;
    }
}
