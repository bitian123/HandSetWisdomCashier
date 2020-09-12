package com.centerm.epos.transcation.pos.data;

import android.content.Context;
import android.text.TextUtils;

import com.centerm.epos.EposApplication;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.XLogUtil;

import java.util.HashMap;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.epos.common.TransCode.AUTH;
import static com.centerm.epos.common.TransCode.AUTH_COMPLETE;
import static com.centerm.epos.common.TransCode.AUTH_SETTLEMENT;
import static com.centerm.epos.common.TransCode.BALANCE;
import static com.centerm.epos.common.TransCode.CANCEL;
import static com.centerm.epos.common.TransCode.COMPLETE_VOID;
import static com.centerm.epos.common.TransCode.CONTRACT_INFO_QUERY;
import static com.centerm.epos.common.TransCode.DOWNLOAD_AID;
import static com.centerm.epos.common.TransCode.DOWNLOAD_BLACK_CARD_BIN_QPS;
import static com.centerm.epos.common.TransCode.DOWNLOAD_CAPK;
import static com.centerm.epos.common.TransCode.DOWNLOAD_CARD_BIN;
import static com.centerm.epos.common.TransCode.DOWNLOAD_CARD_BIN_QPS;
import static com.centerm.epos.common.TransCode.DOWNLOAD_PARAMS;
import static com.centerm.epos.common.TransCode.DOWNLOAD_PARAMS_FINISHED;
import static com.centerm.epos.common.TransCode.DOWNLOAD_QPS_PARAMS;
import static com.centerm.epos.common.TransCode.DOWNLOAD_TERMINAL_PARAMETER;
import static com.centerm.epos.common.TransCode.EC_LOAD_CASH;
import static com.centerm.epos.common.TransCode.EC_LOAD_INNER;
import static com.centerm.epos.common.TransCode.EC_LOAD_OUTER;
import static com.centerm.epos.common.TransCode.EC_VOID_CASH_LOAD;
import static com.centerm.epos.common.TransCode.ESIGN_UPLOAD;
import static com.centerm.epos.common.TransCode.ESIGN_UPLOAD_PART;
import static com.centerm.epos.common.TransCode.E_REFUND;
import static com.centerm.epos.common.TransCode.IC_OFFLINE_UPLOAD;
import static com.centerm.epos.common.TransCode.IC_OFFLINE_UPLOAD_SETTLE;
import static com.centerm.epos.common.TransCode.ISS_INTEGRAL_SALE;
import static com.centerm.epos.common.TransCode.ISS_INTEGRAL_VOID;
import static com.centerm.epos.common.TransCode.MAG_ACCOUNT_LOAD;
import static com.centerm.epos.common.TransCode.MAG_ACCOUNT_LOAD_VERIFY;
import static com.centerm.epos.common.TransCode.MAG_ACCOUNT_VERIFY;
import static com.centerm.epos.common.TransCode.MAG_CASH_LOAD;
import static com.centerm.epos.common.TransCode.MAG_CASH_LOAD_CONFIRM;
import static com.centerm.epos.common.TransCode.OBTAIN_TMK;
import static com.centerm.epos.common.TransCode.OFFLINE_ADJUST;
import static com.centerm.epos.common.TransCode.OFFLINE_SETTLEMENT;
import static com.centerm.epos.common.TransCode.POS_STATUS_UPLOAD;
import static com.centerm.epos.common.TransCode.REFUND;
import static com.centerm.epos.common.TransCode.REFUND_SCAN;
import static com.centerm.epos.common.TransCode.RESERVATION_SALE;
import static com.centerm.epos.common.TransCode.RESERVATION_VOID;
import static com.centerm.epos.common.TransCode.SALE;
import static com.centerm.epos.common.TransCode.SALE_INSTALLMENT;
import static com.centerm.epos.common.TransCode.SALE_RESULT_QUERY;
import static com.centerm.epos.common.TransCode.SALE_SCAN;
import static com.centerm.epos.common.TransCode.SETTLEMENT;
import static com.centerm.epos.common.TransCode.SETTLEMENT_DONE;
import static com.centerm.epos.common.TransCode.SIGN_IN;
import static com.centerm.epos.common.TransCode.SIGN_OUT;
import static com.centerm.epos.common.TransCode.TRANS_CARD_DETAIL;
import static com.centerm.epos.common.TransCode.UNION_INTEGRAL_BALANCE;
import static com.centerm.epos.common.TransCode.UNION_INTEGRAL_REFUND;
import static com.centerm.epos.common.TransCode.UNION_INTEGRAL_SALE;
import static com.centerm.epos.common.TransCode.UNION_INTEGRAL_VOID;
import static com.centerm.epos.common.TransCode.UPLOAD_SCRIPT_RESULT;
import static com.centerm.epos.common.TransCode.VOID;
import static com.centerm.epos.common.TransCode.VOID_INSTALLMENT;
import static com.centerm.epos.common.TransCode.VOID_SCAN;
import static com.centerm.epos.common.TransDataKey.KEY_PARAMS_TYPE;
import static com.centerm.epos.common.TransDataKey.iso_f60_origin;
import static com.centerm.epos.common.TransDataKey.keyFlagFallback;
import static com.centerm.epos.common.TransDataKey.key_is_amount_ok;
import static com.centerm.epos.common.TransDataKey.key_is_balance_settle;
import static com.centerm.epos.common.TransDataKey.key_is_balance_settle_foreign;
/**
 * Created by yuhc on 2017/2/8.<br>
 * 域60 自定义域(Reserved Private)，最大17个字节的数字字符。<br>
 * 所有的POS终端向POS中心发送的交易消息中，均包含60.1域和60.2域指明本交易的交易类型和清算批次。<br>
 * POS的网络管理类报文中，网络管理信息码与消息类型码的组合标识不同的网络管理类消息。
 */

public class BaseField60 implements I8583Field {

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField60.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String, Object> tradeInfo;

    public BaseField60(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    /**
     * 从业务数据中取出网络管理信息码、批次号，并根据规范要求输出。
     *
     * @return 网络管理信息码、批次号
     */
    @Override
    public String encode() {
        if (tradeInfo == null) {
            XLogUtil.e(TAG, "^_^ encode 输入参数 tradeInfo 为空 ^_^");
            return null;
        }
        String transType = (String) tradeInfo.get(TradeInformationTag.TRANSACTION_TYPE);
        if (TextUtils.isEmpty(transType)) {
            XLogUtil.e(TAG, "^_^ 获取业务数据失败 ^_^");
            return null;
        }

        String formatedData;
        if (TransCode.TRANS_IC_DETAIL.equals(transType)){
            formatedData = (String) tradeInfo.get(TradeInformationTag.CUSTOM_INFO_60);
        }else  if (TransCode.SETTLEMENT_INFO_QUERY.equals(transType)){
            formatedData = "01"+ BusinessConfig.getInstance().getBatchNo(EposApplication.getAppContext())+"710";
        } else {
            tradeInfo.put(TradeInformationTag.BATCH_NUMBER, BusinessConfig.getInstance().getBatchNo(EposApplication
                    .getAppContext()));
            formatedData = getIso60(EposApplication.getAppContext(), transType, "1".equals(tradeInfo.get
                    (keyFlagFallback)));
        }
        XLogUtil.d(TAG, "^_^ encode result:" + formatedData + " ^_^");
        return formatedData;
    }

    /**
     * @param fieldMsg 域数据
     * @return null
     */
    @Override
    public Map<String, Object> decode(String fieldMsg) {
        Map<String, Object> tradeData = new HashMap<>();
        tradeData.put(TradeInformationTag.CUSTOM_INFO_60, fieldMsg);
        if (fieldMsg.length()>=10)
            tradeData.put(TradeInformationTag.BATCH_NUMBER, fieldMsg.substring(2,8));

        return tradeData;
    }


    /**
     * 组合60域的数据
     *
     * @param context Context
     * @return 60域数据
     */
    private String getIso60(Context context, String transCode, boolean isfallback) {
        //1-消息类型码：00管理类交易；01查询；03积分查询；10预授权/冲正；11预授权撤销/冲正；20预授权完成（请求）/冲正；21预授权完成撤销/冲正；22消费/冲正；23
        // 消费撤销/冲正；24预授权完成（通知）；99获取主密钥
        //2-批次号：xxxxxx
        //3-网络信息管理码：000默认值（非网络管理类交易）；001单倍长密钥；003双倍长密钥；004双倍长密钥（包含磁道密钥）
        //4-终端读取能力：0不可预知；2磁条卡；5磁条卡+接触式；6磁条卡+接触式+非接触式
        //5-IC卡条件代码：0默认；1代表IC卡进行刷卡的不规范操作；2代表降级交易
        //绿城项目新增 iso60_9 分账标识 0不分账，值不上送或为空也表示不分账，1分账
        String iso60_1 = null, iso60_2, iso60_3 = null, iso60_4 = null, iso60_5 = null, iso60_6 =
                null, iso60_7 = null,iso60_9 = null;
        iso60_2 = BusinessConfig.getInstance().getBatchNo(context);
        switch (transCode) {
            case OBTAIN_TMK:
                iso60_1 = "00";
                iso60_2 = "000000";
                iso60_3 = "401";
                break;
            case SIGN_IN:
                iso60_1 = "00";
                iso60_2 = BusinessConfig.getInstance().getBatchNo(context);
                iso60_3 = BusinessConfig.getInstance().getValue(context, "tag27");
                if(TextUtils.isEmpty(iso60_3)){
                    iso60_3 = "004";
                }
                break;
            case POS_STATUS_UPLOAD:
                String paramsType = (String) tradeInfo.get(KEY_PARAMS_TYPE);
                iso60_1 = "00";
                iso60_2 = BusinessConfig.getInstance().getBatchNo(context);
                //IC卡公钥下载372，国密公钥下载373，IC卡参数下载382
                if ("1".equals(paramsType)) {
                    iso60_3 = "372";
                } else if ("2".equals(paramsType)) {
                    iso60_3 = "382";
                } else if ("3".equals(paramsType)) {
                    iso60_3 = "373";
                } else if ("8".equals(paramsType)) {
                    iso60_3 = "362";
                } else {
                    XLogUtil.d(TAG, "60.3域数据构造异常");
                }
                break;
            case DOWNLOAD_PARAMS:
                String paramType = (String) tradeInfo.get(TradeInformationTag. PARAMS_TYPE);
                if (TextUtils.isEmpty(paramType))
                    break;
                iso60_1 = "00";
                iso60_2 = BusinessConfig.getInstance().getBatchNo(context);
                switch (paramType) {
                    case DOWNLOAD_CAPK:
                        iso60_3 = "370";
                        break;
                    case DOWNLOAD_AID:
                        iso60_3 = "380";
                        break;
                    case DOWNLOAD_QPS_PARAMS:
                        iso60_3 = "394";
                        break;
                    case DOWNLOAD_TERMINAL_PARAMETER:
                        //iso60_3 = "360";
                        iso60_3 = "364";
                        break;
                }
                break;
            case DOWNLOAD_CARD_BIN:
                iso60_1 = "00";
                iso60_2 = BusinessConfig.getInstance().getBatchNo(context);
                iso60_3 = "960";
                break;
            case DOWNLOAD_CARD_BIN_QPS:
                iso60_1 = "00";
                iso60_2 = BusinessConfig.getInstance().getBatchNo(context);
                iso60_3 = "396";
                break;
            case DOWNLOAD_BLACK_CARD_BIN_QPS:
                iso60_1 = "00";
                iso60_2 = BusinessConfig.getInstance().getBatchNo(context);
                iso60_3 = "398";
                break;
            case DOWNLOAD_PARAMS_FINISHED:
                String type = (String) tradeInfo.get(KEY_PARAMS_TYPE);
                iso60_1 = "00";
                iso60_2 = BusinessConfig.getInstance().getBatchNo(context);
                if ("1".equals(type) || "3".equals(type)) {
                    iso60_3 = "371";
                } else if ("2".equals(type)) {
                    iso60_3 = "381";
                } else if ("4".equals(type)) {
                    iso60_3 = "395";
                } else if ("5".equals(type)) {
                    iso60_3 = "961";
                } else if ("6".equals(type)) {
                    iso60_3 = "397";
                } else if ("7".equals(type)) {
                    iso60_3 = "399";
                } else if ("8".equals(type)) {
                    iso60_3 = "361";
                }else {
                    XLogUtil.w(TAG, "60.3域数据构造异常");
                }
                break;
            case UPLOAD_SCRIPT_RESULT:
                String origin60 = (String) tradeInfo.get(iso_f60_origin);
                iso60_1 = "00";
                iso60_2 = BusinessConfig.getInstance().getBatchNo(context);
                iso60_3 = "951";
                iso60_4 = origin60.substring(11, 12);
                iso60_5 = origin60.substring(12, 13);
                break;
            case SIGN_OUT:
                iso60_1 = "00";
                iso60_3 = "002";
                break;
            case ESIGN_UPLOAD:
                iso60_1 = "07";
                iso60_2 = BusinessConfig.getInstance().getBatchNo(context);
                iso60_3 = (String) tradeInfo.get(TradeInformationTag.NET_MANAGE_CODE);
                break;
            case ESIGN_UPLOAD_PART:
                iso60_1 = "08";
                iso60_2 = BusinessConfig.getInstance().getBatchNo(context);
                iso60_3 = (String) tradeInfo.get(TradeInformationTag.NET_MANAGE_CODE);
                iso60_4 = (String) tradeInfo.get(TradeInformationTag.E_SIGNATURE_UPLOAD_END_FLAG);
                break;
            case BALANCE:
            case UNION_INTEGRAL_BALANCE:
                iso60_1 = UNION_INTEGRAL_BALANCE.equals(transCode) ? "03":"01";
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                if(UNION_INTEGRAL_BALANCE.equals(transCode)){
                    iso60_6 = "0";
                    iso60_7 = "065";
                }
                break;
            case SALE:
                iso60_1 = "22";
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                if( UNION_INTEGRAL_SALE.equals(transCode) || ISS_INTEGRAL_SALE.equals(transCode) ){
                    iso60_6 = "1";
                    iso60_7 = UNION_INTEGRAL_SALE.equals(transCode) ? "065" : "048";
                }
                iso60_9 = "0";
                break;
            case SALE_SCAN:
            case SALE_INSTALLMENT:
            case UNION_INTEGRAL_SALE:
            case ISS_INTEGRAL_SALE:
                iso60_1 = "22";
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                if( UNION_INTEGRAL_SALE.equals(transCode) || ISS_INTEGRAL_SALE.equals(transCode) ){
                    iso60_6 = "1";
                    iso60_7 = UNION_INTEGRAL_SALE.equals(transCode) ? "065" : "048";
                }
                break;
            case RESERVATION_SALE:
                iso60_1 = "54";
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                break;
            case RESERVATION_VOID:
                iso60_1 = "53";
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                break;
            case VOID:
            case VOID_SCAN:
            case VOID_INSTALLMENT:
            case UNION_INTEGRAL_VOID:
            case ISS_INTEGRAL_VOID:
                iso60_1 = "23";
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                if( UNION_INTEGRAL_VOID.equals(transCode) || ISS_INTEGRAL_VOID.equals(transCode) ){
                    iso60_6 = "1";
                    iso60_7 = UNION_INTEGRAL_VOID.equals(transCode) ? "065" : "048";
                }
                break;
            case REFUND:
            case REFUND_SCAN:
            case UNION_INTEGRAL_REFUND:
                //// TODO: 2016/11/6 IC卡脱机交易退货填27，其它交易退货填25
                iso60_1 = "25";
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                if( UNION_INTEGRAL_REFUND.equals(transCode) ){
                    iso60_6 = "1";
                    iso60_7 = "065";
                }
                break;
            case AUTH:
                iso60_1 = "10";
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                break;
            case CANCEL:
                iso60_1 = "11";
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                break;
            case AUTH_COMPLETE:
                iso60_1 = "20";
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                break;
            case AUTH_SETTLEMENT:
                iso60_1 = "24";
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                break;
            case COMPLETE_VOID:
                iso60_1 = "21";
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                break;
            case TRANS_CARD_DETAIL:
            case SETTLEMENT:
                iso60_1 = "00";
                iso60_2 = BusinessConfig.getInstance().getBatchNo(context);
                iso60_3 = "201";
                break;
            case SETTLEMENT_DONE:
                iso60_1 = "00";
                iso60_2 = BusinessConfig.getInstance().getBatchNo(context);
                if ("1".equals(tradeInfo.get(key_is_amount_ok))) {
                    /*
                    *BUGID:0002279: 进行结算，平台返回对账不平，批上送结束为207，应该为202
                    * 增加外卡对账，暂时未使用
                    *@author zhouzhihua 2017.11.07
                    * */
                    iso60_3 = ("1".equals(tradeInfo.get(key_is_balance_settle)) && "1".equals(tradeInfo.get(key_is_balance_settle_foreign))) ? "207" : "202";
                    XLogUtil.d("zhouzhihua","key_is_balance_settle="+tradeInfo.get(key_is_balance_settle));
                    //iso60_3 = "1".equals(tradeInfo.get(key_is_balance_settle)) ? "207" : "202";
                } else {
                    iso60_3 = "206";
                }
                break;
            case EC_LOAD_CASH:
                iso60_1 = "46";
                iso60_2 = BusinessConfig.getInstance().getBatchNo(context);
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                iso60_6 = "1";
                break;
            case EC_LOAD_INNER:
                iso60_1 = "45";
                iso60_2 = BusinessConfig.getInstance().getBatchNo(context);
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                iso60_6 = "1";
                break;
            case EC_LOAD_OUTER:
                iso60_1 = "47";
                iso60_2 = BusinessConfig.getInstance().getBatchNo(context);
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                iso60_6 = "1";
                break;
            case EC_VOID_CASH_LOAD:
                iso60_1 = "51";
                iso60_2 = BusinessConfig.getInstance().getBatchNo(context);
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                iso60_6 = "1";
                break;
            case E_REFUND:
                iso60_1 = "27";
                iso60_2 = BusinessConfig.getInstance().getBatchNo(context);
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                iso60_6 = "1";
                break;
            case IC_OFFLINE_UPLOAD:
            case IC_OFFLINE_UPLOAD_SETTLE:
                iso60_1 = "36";
                iso60_2 = (String)tradeInfo.get(TradeInformationTag.BATCH_NUMBER);
                iso60_3 = "000";
                iso60_4 = "6";//原本为5，但是联迪送6
                iso60_5 = isfallback ? "2" : "0";
                break;

            case MAG_ACCOUNT_LOAD_VERIFY:
            case MAG_ACCOUNT_VERIFY:
            case MAG_CASH_LOAD_CONFIRM:
            case MAG_ACCOUNT_LOAD:
                iso60_1 = (MAG_ACCOUNT_VERIFY.equals(transCode)||MAG_ACCOUNT_LOAD_VERIFY.equals(transCode)) ? "01" :(MAG_CASH_LOAD_CONFIRM.equals(transCode)? "48" : "49");
                iso60_2 = BusinessConfig.getInstance().getBatchNo(context);
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                iso60_6 = "1";
                break;
            case MAG_CASH_LOAD:
                iso60_1 = "48";
                iso60_2 = BusinessConfig.getInstance().getBatchNo(context);
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                iso60_6 = "1";
                break;
            case OFFLINE_SETTLEMENT:
            case OFFLINE_ADJUST:
                iso60_1 = OFFLINE_SETTLEMENT.equals(transCode) ? "30" : "32";
                iso60_2 = (String)tradeInfo.get(TradeInformationTag.BATCH_NUMBER);
                iso60_3 = "000";
                iso60_4 = "6";
                iso60_5 = isfallback ? "2" : "0";
                break;
            case SALE_RESULT_QUERY:
                iso60_1 = "61";
                break;
            case CONTRACT_INFO_QUERY:
                iso60_1 = "01";
                iso60_2 = (String)tradeInfo.get(TradeInformationTag.BATCH_NUMBER);
                break;
        }
        /*
        * 因暂不支持部分扣款，第6子域如果有赋值，全部设置为0,统一处理为不支持部分扣款
        * author:zhouzhihua
        * */
        iso60_6 = ( iso60_6 != null ) ? "0": null;

        StringBuilder builder = new StringBuilder();
        if (iso60_1 != null) {
            builder.append(iso60_1);
        }
        if (iso60_2 != null) {
            builder.append(iso60_2);
        }
        if (iso60_3 != null) {
            builder.append(iso60_3);
        }
        if (iso60_4 != null) {
            builder.append(iso60_4);
        }
        if (iso60_5 != null) {
            builder.append(iso60_5);
        }
        if (iso60_6 != null) {
            builder.append(iso60_6);
        }
        if (iso60_7 != null) {
            builder.append(iso60_7);
        }
        if (iso60_9 != null) {
            builder.append(iso60_9);
        }
        XLogUtil.d(TAG, "IOS F60 ==> " + builder.toString());
        return builder.toString();
    }
}
