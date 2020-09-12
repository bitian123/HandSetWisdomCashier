package com.centerm.epos.present.transaction;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.define.ICardReaderDev;
import com.centerm.cpay.midsdk.dev.define.IPbocService;
import com.centerm.cpay.midsdk.dev.define.cardreader.CardInfo;
import com.centerm.cpay.midsdk.dev.define.cardreader.CardReaderListener;
import com.centerm.cpay.midsdk.dev.define.cardreader.EnumReadCardType;
import com.centerm.cpay.midsdk.dev.define.pboc.EnumPbocFlow;
import com.centerm.cpay.midsdk.dev.define.pboc.EnumPbocResultType;
import com.centerm.cpay.midsdk.dev.define.pboc.EnumPbocSlot;
import com.centerm.cpay.midsdk.dev.define.pboc.TransParams;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.bean.BinData;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DataHelper;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.utils.XLogUtil;
import com.centerm.epos.view.AlertDialog;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.epos.common.TransDataKey.FLAG_REQUEST_ONLINE;
import static com.centerm.epos.common.TransDataKey.KEY_HOLDER_NAME;
import static com.centerm.epos.common.TransDataKey.iso_f2;
import static com.centerm.epos.common.TransDataKey.keyFlagFallback;

/**
 * Created by yuhc on 2017/2/15.
 * 检卡界面的业务逻辑处理
 */

public class CheckCardPresentSecond extends CheckCardPresent implements ICheckCard {
    private int retryTimes;
    private EnumReadCardType cardType;
    private Handler retryHandler;
    private Runnable retryThread;
    private boolean isFallback = false;//是否降级
    private CommonDao<BinData> dao;
    private ICardReaderDev cardReaderDev;//读卡设备
    private IPbocService pbocService;//PBOC服务
    private int maxRetryTimes;


    public CheckCardPresentSecond(ITradeView mTradeView) {
        super(mTradeView);
    }

    /*
    * 电子现金类交易
    * */
    public boolean bIsEcTransAndSetCardType()
    {
        boolean bIsTure = true;

        if( TransCode.E_BALANCE.equals( getTradeCode() )
            || TransCode.EC_LOAD_RECORDS.equals( getTradeCode() )
            || TransCode.EC_TRANS_RECORDS.equals( getTradeCode() )
            || TransCode.EC_LOAD_CASH.equals( getTradeCode()) ){
            cardType = EnumReadCardType.INSERT_SWING;
        }
        else if( TransCode.E_QUICK.equals( getTradeCode() ) ){
            cardType = EnumReadCardType.SWING;
        }
        else if( TransCode.E_COMMON.equals( getTradeCode() ) || TransCode.EC_LOAD_OUTER.equals( getTradeCode())
                || TransCode.EC_LOAD_INNER.equals( getTradeCode()) ){
            cardType = EnumReadCardType.INSERT;
        }
        else if( TransCode.MAG_ACCOUNT_LOAD.equals( getTradeCode() ) || TransCode.MAG_ACCOUNT_LOAD_VERIFY.equals( getTradeCode() ) ){
            cardType = EnumReadCardType.SWIPE;
        }
        else{
            bIsTure = false;
        }
        transDatas.put(TransDataKey.key_is_load_second_use_card,null);
        return bIsTure;
    }

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        XLogUtil.w("bIsEcTransAndSetCardType","getTradeCode():"+getTradeCode());
        if( !bIsEcTransAndSetCardType() ) {

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
        XLogUtil.w("bIsEcTransAndSetCardType","cardType:"+cardType);
        dao = new CommonDao<>(BinData.class, dbHelper);

        try {
            cardReaderDev = DeviceFactory.getInstance().getCardReaderDev();
            pbocService = DeviceFactory.getInstance().getPbocService();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCancel() {
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
        return (TransCode.AUTH_COMPLETE.equals(transType) || TransCode.CANCEL.equals(transType));
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


    @Override
    public void onExit() {
        onCancelCheckCard();
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
    private void setIccContactPBOCParams(){
        if (mTradeInformation.getPbocParams() == null) {
            mTradeInformation.setPbocParams(new TransParams(EnumPbocSlot.SLOT_IC, EnumPbocFlow
                    .PBOC_FLOW));
        } else {
            mTradeInformation.getPbocParams().setFlow(EnumPbocFlow.PBOC_FLOW);
            mTradeInformation.getPbocParams().setSlot(EnumPbocSlot.SLOT_IC);
        }
        if( TransCode.EC_LOAD_OUTER.equals( getTradeCode() ) ) {
            mTradeInformation.getTransDatas().put(TransDataKey.KEY_TRANSFER_INTO_CARD_SERVICE_ENTRY_MODE, "05");
        }else{
            mTradeInformation.getTransDatas().put(TradeInformationTag.SERVICE_ENTRY_MODE, "05");
        }
        //退货流程需输完金额才开启PBOC流程，在TradingActivity中开启
        if (TransCode.REFUND.equals(mTradeInformation.getTransCode())
                || TransCode.AUTH_COMPLETE.equals(mTradeInformation.getTransCode())
                || TransCode.CANCEL.equals(mTradeInformation.getTransCode())) {
            //消费，先输金额再检卡
            //预授权/预授权撤销/预授权完成，先检卡再输金额
            //预授权按照客户需求，改为跟消费一致的流程
            mTradeInformation.getPbocParams().setRequestAmtAfterCardNo(true);
        }else if( TransCode.E_COMMON.equals(getTradeCode())
                || TransCode.E_BALANCE.equals(getTradeCode()) ) {
                                /*
                                *接触式电子现金余额
                                * */
            mTradeInformation.getPbocParams().setSupportEc(true);
            mTradeInformation.getPbocParams().setForceOnline(false);
        }
        else if( TransCode.EC_LOAD_CASH.equals(getTradeCode())
                 || TransCode.EC_LOAD_INNER.equals(getTradeCode())
                 || TransCode.EC_LOAD_OUTER.equals(getTradeCode())){
            mTradeInformation.getPbocParams().setForceOnline(true);
            mTradeInformation.getPbocParams().setRequestAmtAfterCardNo(true);
        }
        if( TransCode.EC_LOAD_OUTER.equals( getTradeCode() ) ){
            transDatas.put(TransDataKey.key_is_load_second_use_card,"1");
        }
    }
    /*
    * 非接触式IC卡参数设置
    * */
    private void setIccContactlessPBOCParams(){
        if( TransCode.EC_LOAD_OUTER.equals( getTradeCode() ) ) {
            mTradeInformation.getTransDatas().put(TransDataKey.KEY_TRANSFER_INTO_CARD_SERVICE_ENTRY_MODE, "07");
        }else{
            mTradeInformation.getTransDatas().put(TradeInformationTag.SERVICE_ENTRY_MODE, "07");
        }
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
                || TransCode.CANCEL.equals(mTradeInformation.getTransCode())) {
            //消费，在卡号确认前输入金额
            //预授权，在卡号确认后输入金额
            mTradeInformation.getPbocParams().setRequestAmtAfterCardNo(true);
        }else if( TransCode.E_QUICK.equals(getTradeCode())||TransCode.E_COMMON.equals(getTradeCode())
                                    /*||TransCode.E_LOAD.equals(getTradeCode())||TransCode.E_UNLOAD.equals(getTradeCode())*/
                ||TransCode.E_BALANCE.equals(getTradeCode() )
                ||TransCode.EC_LOAD_RECORDS.equals(getTradeCode())
                ||TransCode.EC_TRANS_RECORDS.equals(getTradeCode())) {
            mTradeInformation.getPbocParams().setSupportEc(true);
            mTradeInformation.getPbocParams().setForceOnline(false);
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
                            mTradeView.popLoading(Settings.bIsSettingBlueTheme() ? R.string.tip_ic_Read_processing :
                                    R.string.tip_ic_on_processing);
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
                    if ("05".equals(mode) && !getTradeCode().equals(TransCode.EC_LOAD_OUTER)) {
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

    public void onSwipeCardSuccess(CardInfo cardInfo, boolean isUnionInternationalCard) {
        transDatas.put(TransDataKey.key_is_load_second_use_card,"1");

        String track2 = cardInfo.getTrack2();
        String track3 = cardInfo.getTrack3();
        if (TextUtils.isEmpty(track2)) {
            logger.warn("二磁道数据为空");
            // TODO: 2017/9/29 是否兼容有时无2磁，有3磁、卡号的情况
            mTradeView.popToast(R.string.tip_read_card_failed);
            onRetry(true);
            return;
        }

        if( TransCode.MAG_ACCOUNT_LOAD_VERIFY.equals(mTradeInformation.getTransCode())
                || TransCode.MAG_ACCOUNT_LOAD.equals(mTradeInformation.getTransCode())){

            transDatas.put(TradeInformationTag.TRANSFER_INTO_CARD,cardInfo.getCardNo() );
            transDatas.put(TransDataKey.KEY_TRANSFER_INTO_CARD_SERVICE_ENTRY_MODE,"02");
            transDatas.put(TransDataKey.KEY_TRANSFER_INTO_CARD_TRACK_2_DATA, track2);
            String expDate = cardInfo.getExpDate();
            if (expDate != null && expDate.length() >= 4) {
                expDate = expDate.substring(0, 4);
            }
            transDatas.put(TransDataKey.KEY_TRANSFER_INTO_CARD_DATE_EXPIRED, expDate);
            if (!TextUtils.isEmpty(track3)) {
                transDatas.put(TransDataKey.KEY_TRANSFER_INTO_CARD_TRACK_3_DATA, track3);
            }
            mTradeView.getHostActivity().jumpToNext();
            return ;
        }

        transDatas.put(TradeInformationTag.SERVICE_ENTRY_MODE, "02");
        String track1 = cardInfo.getTrack1();
        transDatas.put(KEY_HOLDER_NAME, DataHelper.extractName(track1));

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

        if (!isFallback && CommonUtils.isIcCard(track2) && (!TransCode.MAG_ACCOUNT_LOAD_VERIFY.equals(getTradeCode())) ) {
            if (BusinessConfig.getInstance().getFlag(mTradeView.getHostActivity(), BusinessConfig.Key.FLAG_PREFER_CLSS)) {
                mTradeView.popToast(R.string.tip_force_clss);
            } else {
                mTradeView.popToast(R.string.tip_force_ic);
            }
            onRetry(false);
        } else {
            if (TransCode.VOID.equals(mTradeInformation.getTransCode()) || TransCode.COMPLETE_VOID.equals
                    (mTradeInformation.getTransCode())) {
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

    @Override
    public boolean onPbocChangeUserInterface() {
        String mode = (String) mTradeInformation.getTransDatas().get(TradeInformationTag.SERVICE_ENTRY_MODE);
        DialogFactory.hideAll();
        /*圈存的卡不走fallback流程*/
        if ( "05".equals(mode) && !getTradeCode().equals(TransCode.EC_LOAD_OUTER) ) {
            mTradeView.popToast("IC卡读卡失败，请刷卡！");
            cardType = EnumReadCardType.SWIPE_INSERT;
            isFallback = true;
            onRetry(false);
        } else {
            //提示并且重试，重试次数以BusinessConfig当中的为准
            mTradeView.popToast(R.string.tip_read_card_failed);
            onRetry(true);
        }
        return true;
    }

    @Override
    public boolean onPbocConfirmCardNo(String cardNo) {
        if( getTradeCode().equals(TransCode.EC_LOAD_OUTER) ){
            transDatas.put(TradeInformationTag.TRANSFER_INTO_CARD, cardNo);
        }else {
            transDatas.put(TradeInformationTag.BANK_CARD_NUM, cardNo);
        }
        if (!mTradeView.getHostActivity().isPbocTerminated()) {
            String transCode = getTradeCode();
            Context context = mTradeView.getContext();
            //消费撤销和预授权完成撤销判断卡号是否符合
            if (TransCode.VOID.equals(transCode) || TransCode.COMPLETE_VOID.equals(transCode)) {
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
                pbocService.importResult(EnumPbocResultType.CARD_INFO_CONFIRM, true);
            else {
                stopPbocProcess();
            }
            gotoNextStep();
        }
        return true;
    }

    private void stopPbocProcess() {
        pbocService.abortProcess();
        transDatas.put(FLAG_REQUEST_ONLINE, "1");
    }
}
