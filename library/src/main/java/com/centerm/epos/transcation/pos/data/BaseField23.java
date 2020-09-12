package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;

import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.XLogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuhc on 2017/2/8.<br>
 * 域23 卡序列号(Card Sequence Number)，3个字节的定长数字字符域。<br>
 * IC卡的序列号，用于区别具有相同PAN的不同卡。只在IC卡交易时使用。
 */

public class BaseField23 implements I8583Field {

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField23.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String,Object> tradeInfo;

    public BaseField23(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    /**
     * 从业务数据中取出卡序列号，并根据规范要求输出3个Byte的ASC码数据。
     * @return  卡序列号，例如：001
     */
    @Override
    public String encode() {
        if (tradeInfo == null){
            XLogUtil.e(TAG, "^_^ encode 输入参数 tradeInfo 为空 ^_^");
            return null;
        }
        String entryMode = (String) tradeInfo.get(TradeInformationTag.CARD_SEQUENCE_NUMBER);
        if (TextUtils.isEmpty(entryMode)){
            XLogUtil.e(TAG, "^_^ 获取卡序列号失败 ^_^ ");
        }
        return entryMode;
    }

    /**
     * 从域数据中取出平台返回的卡序列号（去除右补的一个0），并保存到指定TAG
     *
     * @param fieldMsg 域数据
     * @return 业务数据对象，卡序列号
     */
    @Override
    public Map<String, Object> decode(String fieldMsg) {
        if (TextUtils.isEmpty(fieldMsg)) {
            XLogUtil.e(TAG, "^_^ decode 输入参数 fieldMsg 为空 ^_^");
            return null;
        }

        Map<String, Object> tradeInfo = new HashMap<>(1);
        if (fieldMsg.length()>3){
            //默认取前3个字符。注意：有些项目是前面填充0，此时要取后3位。
            tradeInfo.put(TradeInformationTag.CARD_SEQUENCE_NUMBER, fieldMsg.substring(0,3));
        }else
            tradeInfo.put(TradeInformationTag.CARD_SEQUENCE_NUMBER, fieldMsg);
        XLogUtil.d(TAG, "^_^ decode result:" + tradeInfo + " ^_^");
        return tradeInfo;
    }
}
