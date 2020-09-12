package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;

import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.XLogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuhc on 2017/2/8.<br>
 * 域38 授权标识应答码(Authorization Identification Response Code)，6个字节定长的字母、数字和特殊字符。<br>
 * 在预授权交易中，发卡方将在成功的应答消息中返回一个有效的授权号，以供后续交易使用。<br>
 * 在预授权完成（请求）交易的请求消息中，POS终端将预授权交易中得到的授权号放入本域，传给发卡方。由发卡方去匹配原始预授权交易；授权应答码不足6位时，左靠，右补空格至6位
 */

public class BaseField38 implements I8583Field {

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField38.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String,Object> tradeInfo;

    public BaseField38(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    /**
     * 从业务数据中取出授权标识应答码，并根据规范要求输出6个Byte的ASC码数据。
     *
     * @return 授权标识应答码
     */
    @Override
    public String encode() {
        if (tradeInfo == null) {
            XLogUtil.e(TAG, "^_^ encode 输入参数 tradeInfo 为空 ^_^");
            return null;
        }
        /*
        * 离线结算的授权码 使用元授权码  只有在电话和pos授权时使用
        * */
        boolean bIsOffline = TransCode.OFFLINE_SETTLEMENT.equals( tradeInfo.get(TradeInformationTag.TRANSACTION_TYPE) );

        String authId = bIsOffline ? (String) tradeInfo.get(TransDataKey.key_oriAuthCode) : (String) tradeInfo.get(TradeInformationTag.AUTHORIZATION_IDENTIFICATION);

        if (TextUtils.isEmpty(authId)) {
            //未传入授权标识应答码，此处再从交易记录中进行获取
            String traceNumber = (String) tradeInfo.get(TradeInformationTag.TRACE_NUMBER);
            if (!TextUtils.isEmpty(traceNumber))
                authId = getTransAuthId(traceNumber);
        }

        XLogUtil.d(TAG, "^_^ encode result:" + authId + " ^_^");
        return authId;
    }

    /**
     * 从域数据中取出平台返回的授权标识应答码，并保存到指定TAG
     *
     * @param fieldMsg 域数据
     * @return 业务数据对象，授权标识应答码
     */
    @Override
    public Map<String, Object> decode(String fieldMsg) {
        if (TextUtils.isEmpty(fieldMsg)) {
            XLogUtil.e(TAG, "^_^ decode 输入参数 fieldMsg 为空 ^_^");
            return null;
        }

        Map<String, Object> tradeInfo = new HashMap<>(1);
        tradeInfo.put(TradeInformationTag.AUTHORIZATION_IDENTIFICATION, fieldMsg);
        XLogUtil.d(TAG, "^_^ decode result:" + tradeInfo + " ^_^");
        return tradeInfo;
    }

    /**
     * 根据交易流水号读取交易记录，并返回授权标识应答码
     * @param traceNumber   交易流水号
     * @return  授权标识
     */
    private String getTransAuthId(String traceNumber) {
        // TODO: 2017/2/10

        return null;
    }
}
