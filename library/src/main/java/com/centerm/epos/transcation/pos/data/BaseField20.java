package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;
import android.util.Log;

import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.DataHelper;
import com.centerm.epos.utils.XLogUtil;

import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Created by liubit on 2018/1/3.<br>
 * 域20 交易码以及订单号信息
 */

public class BaseField20 implements I8583Field {

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField20.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String,Object> tradeInfo;

    public BaseField20(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }
    /**
     * 从业务数据中取出服务点输入方式码，并根据规范要求输出3个Byte的ASC码数据。
     * @return  有效期，例如：
     */
    @Override
    public String encode() {
        if (tradeInfo == null){
            XLogUtil.e(TAG, "^_^ encode 输入参数 tradeInfo 为空 ^_^");
            return null;
        }
        String tradeName = (String) tradeInfo.get(TradeInformationTag.TRANSACTION_TYPE);
        if(TextUtils.isEmpty(tradeName)){
            tradeName = "null";
        }
        String code = "000";
        String order = "00";
        switch (tradeName){
        case TransCode.SIGN_IN:
            code = "003";
            break;
        case TransCode.SIGN_OUT:
            code = "004";
            break;
        case TransCode.SETTLEMENT:
            code = "005";
            break;
        case TransCode.TRANS_IC_DETAIL:
        case TransCode.TRANS_CARD_DETAIL:
        case TransCode.TRANS_FEFUND_DETAIL:
            code = "006";//批上送
            break;
        case TransCode.OBTAIN_TMK:
            code = "008";
            String icCode = (String) tradeInfo.get("IC_NO");//4130303030303034
            icCode = new String(HexUtils.hexStringToByte(icCode));//A0000001
            String hasTime = "0"+tradeInfo.get("HAS_TIME");
            String orderLen = DataHelper.fillLeftZero((icCode+hasTime).length()+"", 2);
            order = orderLen+icCode+hasTime;
            break;
        case TransCode.BALANCE:
            code = "010";
            break;
        case TransCode.SALE:
        case TransCode.SALE_REVERSE:
            code = "000";
            if(tradeInfo.get("out_order_no")!=null){
                order = appendLen((String) tradeInfo.get("out_order_no"));
            }
            break;
        case TransCode.VOID:
        case TransCode.VOID_REVERSE:
            code = "001";
            break;
        case TransCode.REFUND:
            code = "002";
            break;
        case TransCode.AUTH:
            code = "030";
            break;
        case TransCode.AUTH_REVERSE:
            code = "C30";
            break;
        case TransCode.CANCEL:
        case TransCode.CANCEL_REVERSE:
            code = "031";
            break;
        case TransCode.AUTH_COMPLETE:
        case TransCode.AUTH_COMPLETE_REVERSE:
            code = "032";
            break;
        case TransCode.COMPLETE_VOID:
            code = "033";
            break;
        case TransCode.COMPLETE_VOID_REVERSE:
            code = "C33";
            break;
        case TransCode.DOWNLOAD_TERMINAL_PARAMETER:
            code = "N07";
            break;
        case TransCode.POS_STATUS_UPLOAD:
            code = "901";
            break;
        case TransCode.DOWNLOAD_BLACK_CARD_BIN_QPS:
            code = "902";
            break;
        case TransCode.DOWNLOAD_PARAMS:
            code = "007";
            break;
        case TransCode.DOWNLOAD_PARAMS_FINISHED:
            code = "903";
            break;
        case TransCode.IC_OFFLINE_UPLOAD:
            code = "050";
            break;
        case TransCode.IC_OFFLINE_UPLOAD_SETTLE:
            code = "050";
            break;
        case TransCode.E_REFUND:
            code = "051";
            break;
        case TransCode.ESIGN_UPLOAD:
            code = "009";
            break;
        default:
            break;
        }
        return code+order;
    }

    public static String appendLen(String content){
        if(TextUtils.isEmpty(content)||TextUtils.equals("null", content)){
            return "00";
        }
        int len = content.length();
        String dataLen = DataHelper.fillLeftZero(""+len,2);
        return dataLen+content;
    }

    /**
     * 占位，平台不返回此域
     *
     * @param fieldMsg null
     * @return null
     */
    @Override
    public Map<String, Object> decode(String fieldMsg) {
        return null;
    }

    public static String getRerseIso20(String transCode){
        String code = "000";
        String order = "00";
        switch (transCode){
            case TransCode.SALE_REVERSE:
                code = "000";
                break;
            case TransCode.VOID_REVERSE:
                code = "001";
                break;
            case TransCode.REFUND:
                code = "002";
                break;
            case TransCode.AUTH_REVERSE:
                code = "C30";
                break;
            case TransCode.CANCEL_REVERSE:
                code = "031";
                break;
            case TransCode.AUTH_COMPLETE_REVERSE:
                code = "032";
                break;
            case TransCode.COMPLETE_VOID_REVERSE:
                code = "C33";
                break;
            default:
                break;
        }
        return code+order;
    }
}
