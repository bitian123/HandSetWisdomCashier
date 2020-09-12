package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;

import com.centerm.epos.EposApplication;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.XLogUtil;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import config.BusinessConfig;

/**
 * Created by yuhc on 2017/2/8.<br>
 * 域63 自定义域(Reserved Private)，最大163个字节的数据。<br>
 * 用法一：国际信用卡公司代码    用法二：操作员代码
 */

public class BaseField63 implements I8583Field {

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField63.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String, Object> tradeInfo;

    public BaseField63(Map<String, Object> tradeInfo) {
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
        String transType = (String) tradeInfo.get(TradeInformationTag.TRANSACTION_TYPE);
        if (TextUtils.isEmpty(transType)) {
            XLogUtil.e(TAG, "^_^ 获取业务数据失败 ^_^");
            return null;
        }
        String operatorCode;
        if ( TransCode.REFUND.equals(transType)
                || TransCode.REFUND_SCAN.equals(transType)
                || TransCode.IC_OFFLINE_UPLOAD.equals(transType)
                || TransCode.E_REFUND.equals(transType)
                || TransCode.UNION_INTEGRAL_REFUND.equals(transType) ) {
            operatorCode = "CUP";
            /*
            * 默认退货的卡组 为CUP
            * */
            tradeInfo.put(TradeInformationTag.CREDIT_CODE, operatorCode);
            tradeInfo.put(TradeInformationTag.BANKCARD_ORGANIZATION,operatorCode);
        }
        else if( TransCode.OFFLINE_SETTLEMENT.equals(transType)
                 || TransCode.OFFLINE_ADJUST.equals(transType)
                 || TransCode.OFFLINE_ADJUST_TIP.equals(transType)
                 || TransCode.IC_OFFLINE_UPLOAD_SETTLE.equals(transType)
                 || TransCode.OFFLINE_SETTLEMENT_UPLOAD_SETTLE.equals(transType)
                 || TransCode.OFFLINE_ADJUST_UPLOAD_SETTLE.equals(transType)
                 || TransCode.OFFLINE_ADJUST_TIP_UPLOAD_SETTLE.equals(transType)){
            operatorCode = (String)tradeInfo.get(TradeInformationTag.BANKCARD_ORGANIZATION);
        }
        else {
            operatorCode = (String) tradeInfo.get(TradeInformationTag.OPERATOR_CODE);
            if (operatorCode == null) {
                String oper = BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), BusinessConfig.Key
                        .KEY_OPER_ID);
                if (TextUtils.isEmpty(oper)) {
                    XLogUtil.e(TAG, "^_^ 获取操作员代码失败 ^_^");
                    //无操作员登录，说明是支付组件调用
                    operatorCode = "000";
                } else
                    operatorCode = "0" + oper;
            }
        }
        XLogUtil.d(TAG, "^_^ encode result:" + operatorCode + " ^_^");
        return operatorCode;
    }

    /**
     * @param fieldMsg 域数据
     * @return 国际信用卡代码
     */
    @Override
    public Map<String, Object> decode(String fieldMsg) {
        if (TextUtils.isEmpty(fieldMsg)) {
            XLogUtil.e(TAG, "^_^ decode 输入参数 fieldMsg 为空 ^_^");
            return null;
        }
        Map<String, Object> tradeData = new HashMap<>(1);
        String tranType = (String) tradeInfo.get(TradeInformationTag.TRANSACTION_TYPE);
        if (TransCode.SETTLEMENT.equals(tranType)) {
            tradeData.put(TradeInformationTag.OPERATOR_CODE, fieldMsg);
        } else if(TransCode.CONTRACT_INFO_QUERY.equals(tranType)){
            tradeData.put(TradeInformationTag.CONTRACT_INFO, fieldMsg);
        } else{
            tradeData.put(TradeInformationTag.CREDIT_CODE, fieldMsg.substring(0, 3));
            tradeData.put(TradeInformationTag.BANKCARD_ORGANIZATION, fieldMsg.substring(0, 3));
            if (fieldMsg.length() > 3)
                parseAndPutReverseFieldData(tradeData, fieldMsg.substring(3));
        }

        XLogUtil.d(TAG, "^_^ decode result:" + tradeData + " ^_^");
        return tradeData;
    }

    /**
     * 保存发卡方保留域、中国银联保留域、受理机构保留域、POS终端保留域数据，用于打印在备注内。
     * 去除填充域，并组成打印字符串。
     *
     * @param tradeData 数据缓存
     * @param substring 待解析数据
     */
    private void parseAndPutReverseFieldData(Map<String, Object> tradeData, String substring) {
        if (TextUtils.isEmpty(substring))
            return;

        int fieldLen = 20;
        byte[] fieldChars;
        byte[] leftChars;
        try {
            leftChars = substring.getBytes("GBK");
            StringBuilder reverseFieldBuf = new StringBuilder(120);
            for (int i = 0; i < 4 && leftChars.length > 0; i++) {
                if (i == 3) {
                    //POS终端保留域长度为60
                    fieldLen = 60;
                }
                if (leftChars.length < fieldLen) {
                    reverseFieldBuf.append(new String(leftChars, "GBK"));
                    break;
                }
                fieldChars = Arrays.copyOf(leftChars, fieldLen);
                if (!isFillSpaceChar(fieldChars))
                    reverseFieldBuf.append(new String(fieldChars, "GBK"));
                else
                    fieldChars = null;
                leftChars = Arrays.copyOfRange(leftChars, fieldLen, leftChars.length);
                if (i + 1 < 4 && leftChars.length > 0 && fieldChars != null)
                    reverseFieldBuf.append("\n");
            }
            if (reverseFieldBuf.toString().length() > 0)
                tradeData.put(TradeInformationTag.REVERSE_FIELD, reverseFieldBuf.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }
    }

    /**
     * 字符串内容是否都是空格。
     *
     * @param fieldData 待检查字符串
     * @return true 全为空格或字符串为空
     */
    private boolean isFillSpaceChar(byte[] fieldData) {
        if (fieldData == null || fieldData.length == 0)
            return true;
        for (int i = 0; i < fieldData.length; i++) {
            if (fieldData[i] != ' ')
                return false;
        }
        return true;
    }

}
