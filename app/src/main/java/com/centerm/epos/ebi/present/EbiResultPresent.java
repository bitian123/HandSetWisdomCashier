package com.centerm.epos.ebi.present;

import android.os.Bundle;
import android.text.TextUtils;

import com.centerm.epos.ActivityStack;
import com.centerm.epos.EposApplication;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.StatusCode;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.ebi.keys.JsonKey;
import com.centerm.epos.ebi.msg.GetRequestData;
import com.centerm.epos.ebi.utils.DateUtil;
import com.centerm.epos.present.transaction.IResult;
import com.centerm.epos.transcation.pos.constant.JsonKeyGT;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.DataHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.epos.common.TransDataKey.iso_f39;
import static com.centerm.epos.common.TransDataKey.keyBalanceAmt;
import static com.centerm.epos.ebi.common.TransCode.SALE_SCAN_QUERY;
import static com.centerm.epos.ebi.common.TransCode.SALE_SCAN_REFUND;
import static com.centerm.epos.ebi.common.TransCode.SALE_SCAN_REFUND_QUERY;
import static com.centerm.epos.ebi.common.TransCode.SALE_SCAN_VOID;
import static com.centerm.epos.ebi.common.TransCode.SALE_SCAN_VOID_QUERY;

/**
 * Created by yuhc on 2017/2/24.
 * 结果显示界面业务处理逻辑
 */

public class EbiResultPresent extends BaseTradePresent implements IResult {
    private static final String TAG = EbiResultPresent.class.getSimpleName();

    protected boolean isSuccess;
    private String respCode, respMsg,amount,time;

    public EbiResultPresent(ITradeView mTradeView) {
        super(mTradeView);
    }

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        String f39 = "-1";
        if(tempMap!=null){
            f39 = tempMap.get(iso_f39);
        }
        if(transDatas==null){
            transDatas = new HashMap<>();
        }
        if(JsonKeyGT.successFlag.equals(transDatas.get(JsonKeyGT.successFlag))){
            f39 = "00";
        }
        logger.debug("getF39=>" + f39);
//        isSuccess = "00".equals(f39) || "0".equals(f39) || "11".equals(f39) || "A2".equals(f39)
//                || "A4".equals(f39) || "A5".equals(f39) || "A6".equals(f39)
//                //|| "10".equals(f39) //没送订单号给银联
//        ;
        isSuccess = "00".equals(f39);
        respCode = tempMap.get(TransDataKey.key_resp_code);
        if (StatusCode.EMV_KERNEL_EXCEPTION.getStatusCode().equals(respCode)
                || StatusCode.TRADING_TERMINATES.getStatusCode().equals(respCode)
                || StatusCode.TRADING_REFUSED.getStatusCode().equals(respCode)) {
            logger.warn("Kernel back the failed event,may cause reverse");
            isSuccess = false;
        }

        //签名不正确，删除主密钥
        if("P0".equals(f39)){
            Settings.setValue(EposApplication.getAppContext(), JsonKey.MAK, "");
            logger.error("签名不正确，删除主密钥");
        }

        //扫码类交易 需要判断交易结果，成功才打单
        if(tempMap.get(JsonKey.TRANS_RESULT_FLAG)!=null){
            logger.debug("trans_result => " + tempMap.get(JsonKey.TRANS_RESULT_FLAG));
            if("S".equals(tempMap.get(JsonKey.TRANS_RESULT_FLAG))){
                isSuccess = true;
            }else {
                isSuccess = false;
            }
        }
        respMsg = tempMap.get(TransDataKey.key_resp_msg);

        if(SALE_SCAN_QUERY.equals(getTradeCode())||SALE_SCAN_VOID_QUERY.equals(getTradeCode())
                ||SALE_SCAN_REFUND_QUERY.equals(getTradeCode())){
            //交易详情中的查询完成后不需要打印
            if(getTransData().get(JsonKey.QUERY_FLAG)==null){
                afterInitView();
            }
        }else {
            afterInitView();
        }

    }

    protected void afterInitView() {
        logger.debug("执行afterInitView方法");
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

        if(mTradeInformation.getTransCode().equals(com.centerm.epos.ebi.common.TransCode.SALE_SCAN_REFUND_QUERY)
            ||mTradeInformation.getTransCode().equals(com.centerm.epos.ebi.common.TransCode.SALE_SCAN_REFUND)){
            tempMap.put(TradeInformationTag.SCAN_VOUCHER_NO, tempMap.get(JsonKey.mer_refund_order_no));
        }else {
            tempMap.put(TradeInformationTag.SCAN_VOUCHER_NO, tempMap.get(JsonKey.mer_order_no));
        }
        tempMap.put(TradeInformationTag.TRANSACTION_TYPE, mTradeInformation.getTransCode());
    }

    @Override
    public boolean isEnableShowingTimeout() {
        return true;
    }

    @Override
    public Object onConfirm(Object paramObj) {
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

    @Override
    public List<ItemViewData> getItemViewData() {
        List<ItemViewData> itemViewDataList = new ArrayList<>();
        if (isSuccess) {
            switch (mTradeInformation.getTransCode()) {
                case TransCode.BALANCE:
                    if (!Settings.bIsSettingBlueTheme()) {
                        itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(R.string.label_balance),
                                tempMap.get(keyBalanceAmt), true));
                    }
                    //可用余额
                    itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(R.string.label_card_no),
                            DataHelper.shieldCardNo((String) transDatas.get(TradeInformationTag.BANK_CARD_NUM)),
                            Settings.bIsSettingBlueTheme() ? true : false));

                    if (Settings.bIsSettingBlueTheme()) {
                        itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(R.string
                                .label_trans_time),
                                DataHelper.formatIsoF12F13((String) respDataMap.get(TradeInformationTag.TRANS_TIME),
                                        (String)respDataMap.get(TradeInformationTag.TRANS_DATE)), false));
                    }

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
                case TransCode.VOID_SCAN:
                case TransCode.REFUND_SCAN:

                    break;
                case TransCode.SALE_SCAN://扫码支付
                case SALE_SCAN_QUERY://扫码支付查询
                    itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(R.string.label_trans_type2),
                            GetRequestData.getTransName(mTradeInformation.getTransCode(), (String)transDatas.get(JsonKey.pay_type)), true));
                    amount = (String) transDatas.get(TradeInformationTag.TRANS_MONEY);
                    itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(R.string.label_trans_amt2),
                            (TextUtils.isEmpty(amount) ? "0.00" : amount) + "元", true));
                    itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(com.centerm.epos.ebi.R.string.label_orderid),
                            transDatas.get(JsonKey.mer_order_no) + "", true));
                    if(transDatas.get(JsonKey.pay_no)!=null){
                        itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(com.centerm.epos.ebi.R.string.pay_no_label),
                                transDatas.get(JsonKey.pay_no) + "", true));
                    }
                    time = (String) transDatas.get(JsonKey.sendTime);
                    time = DateUtil.formatTime(time, "yyyyMMddHHmmss", "yyyy-MM-dd HH:mm:ss");
                    itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(R.string.label_trans_time),
                            time,false));
                    break;
                case SALE_SCAN_VOID://订单撤销
                case SALE_SCAN_VOID_QUERY://订单撤销
                    itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(R.string.label_trans_type2),
                            GetRequestData.getTransName(mTradeInformation.getTransCode(), (String)transDatas.get(JsonKey.pay_type)), true));
                    amount = (String) transDatas.get(TradeInformationTag.TRANS_MONEY);
                    itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(R.string.label_trans_amt2),
                            (TextUtils.isEmpty(amount) ? "0.00" : amount) + "元", true));
                    itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(com.centerm.epos.ebi.R.string.label_serial_num_detail_ori_orderid),
                            transDatas.get(TradeInformationTag.SCAN_VOUCHER_NO) + "", true));
                    time = (String) transDatas.get(JsonKey.sendTime);
                    time = DateUtil.formatTime(time, "yyyyMMddHHmmss", "yyyy-MM-dd HH:mm:ss");
                    itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(R.string.label_trans_time),
                            time,false));
                    break;
                case SALE_SCAN_REFUND://订单退货
                case SALE_SCAN_REFUND_QUERY://订单退货查询
                    itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(R.string.label_trans_type2),
                            GetRequestData.getTransName(mTradeInformation.getTransCode(), (String)transDatas.get(JsonKey.pay_type)), true));
                    itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(com.centerm.epos.ebi.R.string.refund_result_label),
                            transDatas.get(JsonKey.refund_result) + "", true));
                    amount = (String) transDatas.get(TradeInformationTag.TRANS_MONEY);
                    itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(R.string.label_trans_amt2),
                            (TextUtils.isEmpty(amount) ? "0.00" : amount) + "元", true));
                    itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(com.centerm.epos.ebi.R.string.label_orderid),
                            transDatas.get(JsonKey.mer_refund_order_no) + "", true));
                    itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(com.centerm.epos.ebi.R.string.label_serial_num_detail_ori_orderid),
                            transDatas.get(JsonKey.mer_order_no) + "", true));
                    time = (String) transDatas.get(JsonKey.sendTime);
                    time = DateUtil.formatTime(time, "yyyyMMddHHmmss", "yyyy-MM-dd HH:mm:ss");
                    itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(R.string.label_trans_time),
                            time,false));
                    break;
                default:
                    itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(R.string.label_trans_type2),
                            mTradeView.getStringFromResource(TransCode.codeMapName(mTradeInformation.getTransCode())),
                            true));
                    String moneyStr = (String) transDatas.get(TradeInformationTag.TRANS_MONEY);
                    itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(R.string.label_trans_amt2),
                            DataHelper.formatIsoF4(TextUtils.isEmpty(moneyStr) ? "0.00" : moneyStr)+"元", true));
                    itemViewDataList.add(new ItemViewData(mTradeView.getStringFromResource(R.string.label_pos_serial),
                            (String) transDatas.get(TradeInformationTag.TRACE_NUMBER), true));

                    if (!TransCode.SALE_SCAN.equals(mTradeInformation.getTransCode()) && !TransCode.VOID_SCAN.equals
                            (mTradeInformation.getTransCode()) && !TransCode.REFUND_SCAN.equals(mTradeInformation.getTransCode())) {
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
