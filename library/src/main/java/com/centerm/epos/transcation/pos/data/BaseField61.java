package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;

import com.centerm.epos.bean.TranscationFactor;
import com.centerm.epos.bean.transcation.OriginalMessage;
import com.centerm.epos.common.ConstDefine;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.transcation.pos.constant.TranscationFactorTable;
import com.centerm.epos.utils.XLogUtil;

import java.util.Locale;
import java.util.Map;

/**
 * Created by yuhc on 2017/2/8.<br>
 * 域61 原始信息域(Original Message)，最大29个字节的数字字符域。<br>
 * 冲正、撤销和退货交易时填原始交易数据.
 */

public class BaseField61 implements I8583Field {

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField61.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String,Object> tradeInfo;

    public BaseField61(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    /**
     * 从业务数据中取出原始交易信息，并根据规范要求输出。
     *
     * @return 原始交易信息
     */
    @Override
    public String encode() {
        if (tradeInfo == null) {
            XLogUtil.e(TAG, "^_^ encode 输入参数 tradeInfo 为空 ^_^");
            return null;
        }
        String tradeName = (String) tradeInfo.get(TradeInformationTag.TRANSACTION_TYPE);
        if (TextUtils.isEmpty(tradeName))
            return null;
        String formatedData = null;
        if (TransCode.SCAN_QUERY.equals(tradeName)){
            formatedData = "" + tradeInfo.get(TradeInformationTag.BATCH_NUMBER)+ tradeInfo.get(TradeInformationTag
                    .TRACE_NUMBER)+ tradeInfo.get(TradeInformationTag.TRANS_DATE);
        }else if(TransCode.UPLOAD_SCRIPT_RESULT.equals(tradeName)){
            formatedData = (String)tradeInfo.get(TransDataKey.iso_f61);
        }
        else if(TransCode.MAG_CASH_LOAD.equals(tradeName)){
            formatedData = "" + tradeInfo.get(TradeInformationTag.BATCH_NUMBER)+ tradeInfo.get(TradeInformationTag.TRACE_NUMBER);
        }
        else if(TransCode.OFFLINE_SETTLEMENT.equals(tradeName)){
            String authMode = (String)tradeInfo.get(TradeInformationTag.ORIGINAL_AUTH_MODE);
            formatedData = "000000"+"000000"+"0000"+authMode;
            if( ConstDefine.OFFLINE_AUTH_CODE_TELEPHONE.equals(authMode) ){
                formatedData += tradeInfo.get(TradeInformationTag.ORIGINAL_AUTH_ORG_CODE);
            }
        }
        else {
            Object object = tradeInfo.get(TradeInformationTag.ORIGINAL_MESSAGE);
            if (object == null) {
                XLogUtil.e(TAG, "^_^ 获取原交易信息失败 ^_^");
                return null;
            }
            OriginalMessage originalMessage = (OriginalMessage) object;
            switch (tradeName) {
                case TransCode.VOID:
                case TransCode.VOID_INSTALLMENT:
                case TransCode.VOID_SCAN:
                case TransCode.EC_VOID_CASH_LOAD:
                case TransCode.ISS_INTEGRAL_VOID:
                case TransCode.UNION_INTEGRAL_VOID:
                case TransCode.RESERVATION_VOID:
                    formatedData = String.format(Locale.CHINA, "%06d%06d%s", originalMessage.getBatchNumber(),
                            originalMessage.getTraceNumber(), originalMessage.getDate());
                    break;
                case TransCode.E_REFUND:/*脱机退货*/
                case TransCode.REFUND:
                case TransCode.REFUND_SCAN:
                case TransCode.CANCEL:
                case TransCode.COMPLETE_VOID:
                case TransCode.AUTH_COMPLETE:
                case TransCode.AUTH_REVERSE:
                case TransCode.AUTH_COMPLETE_REVERSE:
                case TransCode.UNION_INTEGRAL_REFUND:
                    formatedData = String.format(Locale.CHINA, "%06d%06d%s", originalMessage.getBatchNumber(),
                            originalMessage.getTraceNumber(), originalMessage.getDate());
                    break;
            }
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
        
        return null;
    }

}
