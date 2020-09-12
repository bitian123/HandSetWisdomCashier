package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;

import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.XLogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuhc on 2017/2/8.<br>
 * 域39 应答码(Response Code)，2个字节定长的字符。<br>
 * POS中心返回给POS终端的应答码。应答码可以是发卡方、CUPS或POS中心产生的。<br>
 * POS终端上送的冲正通知中表明冲正原因。<br>
 * 交易应答码中仅"00"为交易成功，“10”为部分交易成功，“11”、“A2”、“A4”、“A5”、“A6”为有缺陷的成功，其它为交易不成功。<br>
 * POS终端引发的冲正消息中，存放冲正原因码。
 */

public class BaseField39 implements I8583Field {

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField39.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String,Object> tradeInfo;

    public BaseField39(Map<String, Object> tradeInfo) {
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
        String reverseCode = (String) tradeInfo.get(TradeInformationTag.REVERSE_CODE);

        if (TextUtils.isEmpty(reverseCode)) {
            XLogUtil.e(TAG, "^_^ 获取冲正原因码失败 ^_^ ");
            return null;
        }

        XLogUtil.d(TAG, "^_^ encode result:" + reverseCode + " ^_^");
        return reverseCode;
    }

    /**
     * 从域数据中取出平台返回的返回码，并保存到指定TAG
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
        tradeInfo.put(TradeInformationTag.RESPONSE_CODE, fieldMsg);
        XLogUtil.d(TAG, "^_^ decode result:" + tradeInfo + " ^_^");
        return tradeInfo;
    }

}
