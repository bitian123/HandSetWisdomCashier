package com.centerm.epos.present.transaction;

import android.os.Bundle;
import android.text.TextUtils;

import com.centerm.epos.ActivityStack;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.StatusCode;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.DataHelper;
import com.centerm.epos.utils.XLogUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.epos.common.TransDataKey.iso_f39;
import static com.centerm.epos.common.TransDataKey.keyBalanceAmt;
import static com.centerm.epos.common.TransDataKey.keyBalanceAmtCode;
import static com.centerm.epos.common.TransDataKey.keyBalanceAmtSecond;
import static com.centerm.epos.common.TransDataKey.keyBalanceAmtSecondCode;

/**
 * Created by yuhc on 2017/2/24.
 * 结果显示界面业务处理逻辑
 */
/*
* @author:zhouzhihua
* 常用的8种货币
* */
enum CurrencyCode {
    KRWCurrencyCode("410","KRW","韩元"  ),
    USDCurrencyCode("840","USD","美元"  ),
    CNYCurrencyCode("156","CNY","人民币"),
    HKDCurrencyCode("344","HKD","港币"  ),
    TWDCurrencyCode("901","TWD","台币"  ),
    GBPCurrencyCode("826","GBP","英镑"  ),
    JPYCurrencyCode("392","JPY","日元"  ),
    EURCurrencyCode("978","EUR","欧元"  );

    private String currencyCode;
    private String currencyEnName;
    private String currencyCNName;


    CurrencyCode(String currencyCode ,String currencyEnName ,String currencyCNName)
    {
        this.currencyCode = currencyCode;
        this.currencyEnName = currencyEnName;
        this.currencyCNName = currencyCNName;
    }

    public static String getCurrencyEnName(String currencyCode)
    {
        for( CurrencyCode code : CurrencyCode.values() ){
            if( code.currencyCode.equals(currencyCode)){
                return code.currencyEnName;
            }
        }
        return "";
    }


}

public class ResultPresent extends BaseTradePresent implements IResult {
    private static final String TAG = ResultPresent.class.getSimpleName();

    protected boolean isSuccess;
    private String respCode, respMsg;

    public ResultPresent(ITradeView mTradeView) {
        super(mTradeView);
    }

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        String f39 = tempMap.get(iso_f39);
        logger.debug("getF39=>" + f39);
        isSuccess = "00".equals(f39) || "0".equals(f39) || "10".equals(f39) || "11".equals(f39) || "A2".equals(f39)
                || "A4".equals(f39) || "A5".equals(f39) || "A6".equals(f39);
        respCode = tempMap.get(TransDataKey.key_resp_code);
        if (StatusCode.EMV_KERNEL_EXCEPTION.getStatusCode().equals(respCode)
                || StatusCode.TRADING_TERMINATES.getStatusCode().equals(respCode)
                || StatusCode.TRADING_REFUSED.getStatusCode().equals(respCode)) {
            logger.warn("Kernel back the failed event,may cause reverse");
            isSuccess = false;
        }
        respMsg = tempMap.get(TransDataKey.key_resp_msg);
        if(TextUtils.isEmpty(respCode)){
            respCode = "-1";
        }
        if(TextUtils.isEmpty(respMsg)){
            respMsg = "交易失败";
        }

        afterInitView();
    }

    protected void afterInitView() {
        logger.debug("执行afterInitView方法");
/*        if (isSuccess && (TransCode.DEBIT_SETS.contains(mTradeInformation.getTransCode())
                || TransCode.CREDIT_SETS.contains(mTradeInformation.getTransCode()))) {
            QianBaoPrinter qianBaoPrinter = QianBaoPrinter.getMenuPrinter();
            qianBaoPrinter.init(mTradeView.getContext());
            addTradeInfo();
            qianBaoPrinter.printData(tempMap, mTradeInformation.getTransCode(), false);
        }*/
        BusinessConfig.getInstance().setValue(mTradeView.getContext(), BusinessConfig.Key.KEY_NOT_SIGN_OR_PIN_AMOUNT,
                null);
        BusinessConfig.getInstance().setFlag(mTradeView.getContext(), BusinessConfig.Key.KEY_NOT_SIGN, false);
        BusinessConfig.getInstance().setFlag(mTradeView.getContext(), BusinessConfig.Key.KEY_NOT_PIN, false);
    }

    protected void addTradeInfo() {
        tempMap.putAll(mTradeInformation.getDataMap());
        Iterator<Map.Entry<String, Object>> iterator = transDatas.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            if (entry.getValue() instanceof String) {
                if (!tempMap.containsKey(entry.getKey()))
                    tempMap.put(entry.getKey(), (String) entry.getValue());
            }
        }
    }

    @Override
    public boolean isEnableShowingTimeout() {
        return true;
    }

    @Override
    public Object onConfirm(Object paramObj) {
//        String oper = BusinessConfig.getInstance().getValue(mTradeView.getHostActivity(), BusinessConfig.Key
// .KEY_OPER_ID);
        ActivityStack.getInstance().pop();
        if ("06".equals(respCode)) {
            //重新签到
            jumpToSignIn();
        } else if ("18".equals(respCode)) {
            //重新下载主密钥
            jumpToDownloadTmk();
        }
        return null;
    }

    @Override
    public String getResponseCode() {
        return respCode;
    }

    @Override
    public String getResponseMessage() {
        return respMsg;
    }

    @Override
    public boolean isSuccess() {
        return isSuccess;
    }

    @Override
    public String getTradeTitle() {
        String title;
        if (isSuccess) {
            switch (mTradeInformation.getTransCode()) {
                case TransCode.BALANCE:
                case TransCode.UNION_INTEGRAL_BALANCE:
                    if (Settings.bIsSettingBlueTheme()) {
                        title = mTradeView.getStringFromResource(R.string.tip_query_value);
                    } else {
                        title = mTradeView.getStringFromResource(R.string.tip_query_success);
                    }
                    break;
                case TransCode.SIGN_IN:
                    title = mTradeView.getStringFromResource(R.string.tip_sign_in_success);
                    break;
                case TransCode.OBTAIN_TMK:
                    title = mTradeView.getStringFromResource(R.string.tip_load_tmk_success);
                    break;
                case TransCode.DOWNLOAD_CAPK:
                    title = mTradeView.getStringFromResource(R.string.tip_load_capk_success);
                    break;
                case TransCode.DOWNLOAD_AID:
                    title = mTradeView.getStringFromResource(R.string.tip_load_aid_success);
                    break;
                case TransCode.DOWNLOAD_QPS_PARAMS:
                    title = mTradeView.getStringFromResource(R.string.tip_load_clss_params_success);
                    break;
                case TransCode.DOWNLOAD_CARD_BIN:
                    title = mTradeView.getStringFromResource(R.string.tip_load_card_bin_success);
                    break;
                default:
                    title = mTradeView.getStringFromResource(R.string.tip_trade_success);
                    break;
            }
        } else {
            switch (mTradeInformation.getTransCode()) {
                case TransCode.BALANCE:
                    title = mTradeView.getStringFromResource(R.string.tip_query_failed);
                    break;
                case TransCode.SIGN_IN:
                    title = mTradeView.getStringFromResource(R.string.tip_sign_in_failed);
                    break;
                case TransCode.OBTAIN_TMK:
                    title = mTradeView.getStringFromResource(R.string.tip_load_tmk_failed);
                    break;
                case TransCode.DOWNLOAD_CAPK:
                    title = mTradeView.getStringFromResource(R.string.tip_load_capk_failed);
                    break;
                case TransCode.DOWNLOAD_AID:
                    title = mTradeView.getStringFromResource(R.string.tip_load_aid_failed);
                    break;
                case TransCode.DOWNLOAD_QPS_PARAMS:
                    title = mTradeView.getStringFromResource(R.string.tip_load_aid_failed);
                    break;
                case TransCode.DOWNLOAD_CARD_BIN:
                    title = mTradeView.getStringFromResource(R.string.tip_load_card_bin_failed);
                    break;
                default:
                    title = mTradeView.getStringFromResource(R.string.tip_trade_failed2);
                    break;
            }
        }
        return title;
    }

    /*
    * 填充ic卡脱机余额信息
    * 支持双币卡余额信息
    * */
    private void setEcOffBalanceInfo(List<ItemViewData> itemViewDataList)
    {
        String balance = tempMap.get(keyBalanceAmtSecond);
        String secondCode = tempMap.get(keyBalanceAmtSecondCode);
        String firstCode = tempMap.get(keyBalanceAmtCode);
        boolean bIsTrue = ((balance!=null) && (balance.length() > 0));

        firstCode = (firstCode==null) ? "" : firstCode;
        secondCode = (secondCode==null) ? "" : secondCode;

        firstCode = firstCode.replaceFirst("^0*", "");
        secondCode = secondCode.replaceFirst("^0*", "");

        if( firstCode != null && firstCode.length() > 0 ){
            firstCode = "("+CurrencyCode.getCurrencyEnName(firstCode)+")";
        }
        if( secondCode != null && secondCode.length() > 0 ){
            secondCode = "("+CurrencyCode.getCurrencyEnName(secondCode)+")";
        }
        itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(
                R.string.label_balance)+firstCode,tempMap.get(keyBalanceAmt)+"元", bIsTrue));
        if( bIsTrue ){
            itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(
                    R.string.label_balance)+secondCode,balance+"元", false));
        }
    }

    private void setEcOfflineApproveInfo(List<ItemViewData> itemViewDataList)
    {
        itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(R.string.label_trans_type2),
                mTradeView.getStringFromResource(TransCode.codeMapName(mTradeInformation.getTransCode())),
                true));
        String moneyStr = (String) transDatas.get(TradeInformationTag.TRANS_MONEY);
        itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(R.string.label_trans_amt2),
                DataHelper.formatIsoF4(TextUtils.isEmpty(moneyStr) ? "0.00" : moneyStr)+"元", true));

        moneyStr = (String) transDatas.get(TradeInformationTag.EC_TRANS_BALANCE);
        XLogUtil.d("zhouzhihua","setEcOfflineApproveInfo:"+moneyStr);
        if( null != moneyStr ) {
            itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(R.string.label_ec_balance),
                    DataHelper.formatIsoF4(TextUtils.isEmpty(moneyStr) ? "0.00" : moneyStr)+"元", true));
        }

        itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(R.string.label_trans_card2),
                (String) transDatas.get(TradeInformationTag.BANK_CARD_NUM),true));

        itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(R.string.label_trans_time),
                            DataHelper.formatIsoF12F13((String) transDatas.get(TradeInformationTag.TRANS_TIME),
                        (String)transDatas.get(TradeInformationTag.TRANS_DATE)),false));
    }

    @Override
    public List<ItemViewData> getItemViewData() {
        List<ItemViewData> itemViewDataList = new ArrayList<>();
        if (isSuccess) {
            switch (mTradeInformation.getTransCode()) {
                case TransCode.BALANCE:
                case TransCode.UNION_INTEGRAL_BALANCE:

                    if (!Settings.bIsSettingBlueTheme()) {
                        itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(R.string.label_balance),
                                tempMap.get(keyBalanceAmt), true));
                    }
                    //可用余额
                    itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(R.string.label_card_no),
                            DataHelper.shieldCardNo((String) transDatas.get(TradeInformationTag.BANK_CARD_NUM)),
                            Settings.bIsSettingBlueTheme() ? true : false));

                    if ( Settings.bIsSettingBlueTheme() ) {
                        itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(R.string
                                .label_trans_time),
                                DataHelper.formatIsoF12F13((String) respDataMap.get(TradeInformationTag.TRANS_TIME),
                                        (String)
                                                respDataMap.get
                                                        (TradeInformationTag.TRANS_DATE)),
                                false));
                    }
                    break;
                case TransCode.E_BALANCE:/*IC卡脱机余额显示，暂时不处理货币代码*/
                    setEcOffBalanceInfo(itemViewDataList);
                    break;

                case TransCode.E_COMMON:
                case TransCode.E_QUICK:
                    setEcOfflineApproveInfo(itemViewDataList);
                    break;

                case TransCode.SIGN_IN:
                case TransCode.SIGN_OUT:
                    break;
                case TransCode.OBTAIN_TMK:
                    break;
                case TransCode.DOWNLOAD_CAPK:
                    break;
                case TransCode.DOWNLOAD_AID:
                    break;
                case TransCode.DOWNLOAD_TERMINAL_PARAMETER:
                    break;
                case TransCode.DOWNLOAD_QPS_PARAMS:
                    break;
                case TransCode.DOWNLOAD_CARD_BIN:
                case TransCode.DOWNLOAD_CARD_BIN_QPS:
                case TransCode.DOWNLOAD_BLACK_CARD_BIN_QPS:
                    break;
                case TransCode.SALE_SCAN:
                case TransCode.VOID_SCAN:
                case TransCode.REFUND_SCAN:

                    break;
                default:
                    itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(R.string.label_trans_type2),
                            mTradeView.getStringFromResource(TransCode.codeMapName(mTradeInformation.getTransCode())),
                            true));
                    String moneyStr = (String) transDatas.get(TradeInformationTag.TRANS_MONEY);
                    itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(R.string.label_trans_amt2),
                            DataHelper.formatIsoF4(TextUtils.isEmpty(moneyStr) ? "0.00" : moneyStr)+"元", true));
                    //modify by yuhc 20180102
                    //使用终端本地的流水号，忽略平台返回的流水号。
                    //因为个别平台不规范，返回的流水号与终端上送的不一致；而终端流水号作为交易信息存储的主键，必须要保证正确性，否则会出现问题；终端
                    //流水号一般是由终端管理。
                    itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(R.string.label_pos_serial),
                            (String) transDatas.get(TradeInformationTag.TRACE_NUMBER), true));

                    if (!TransCode.SALE_SCAN.equals(mTradeInformation.getTransCode()) && !TransCode.VOID_SCAN.equals
                            (mTradeInformation.getTransCode()) && !TransCode.REFUND_SCAN.equals(mTradeInformation
                            .getTransCode())) {
                        if (!TransCode.AUTH.equals(mTradeInformation.getTransCode())) {
                            itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(R.string
                                    .label_trans_card2),
                                    DataHelper.shieldCardNo((String) transDatas.get(TradeInformationTag.BANK_CARD_NUM)),
                                    true));
                        } else {
                            itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(R.string
                                    .label_trans_card2),
                                    (String) transDatas.get(TradeInformationTag.BANK_CARD_NUM), true));
                        }
                    }
                    if( TransCode.EC_LOAD_CASH.equals(mTradeInformation.getTransCode())
                        || TransCode.EC_LOAD_INNER.equals(mTradeInformation.getTransCode())
                        || TransCode.EC_LOAD_OUTER.equals(mTradeInformation.getTransCode()) ){
                        moneyStr = (String) transDatas.get(TradeInformationTag.EC_TRANS_BALANCE);
                        XLogUtil.d("zhouzhihua","EC_LOAD_CASH:"+moneyStr);
                        if( null != moneyStr ) {
                            itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(R.string.label_ec_balance),
                                    DataHelper.formatIsoF4(TextUtils.isEmpty(moneyStr) ? "0.00" : moneyStr)+"元", true));
                        }
                    }
                    itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(R.string.label_trans_time),
                            DataHelper.formatIsoF12F13((String) respDataMap.get(TradeInformationTag.TRANS_TIME),
                                    (String)
                                            respDataMap.get
                                                    (TradeInformationTag.TRANS_DATE)),
                            false));
                    break;
            }
        }
        return itemViewDataList;
    }
}
