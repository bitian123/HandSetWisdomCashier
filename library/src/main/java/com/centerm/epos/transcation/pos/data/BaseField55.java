package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;
import android.util.Log;

import com.centerm.epos.common.TransCode;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.XLogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuhc on 2017/2/8.<br>
 * 域55 IC卡数据域(Intergrated Circuit Card System Related Data)，最长可达255个字节。<br>
 * 本域将根据不同的交易种类包含不同的子域。处理中心仅在受理方和发卡方之间传递这些适用于IC卡交易的特有数据，而不对它们进行任何修改和处理。<br>
 * 为适应该子域需要不断变化的情况，本域采用TLV（tag-length-value）的表示方式，即每个子域由tag标签(T)，子域取值的长度(L)和子域取值(V)构成
 */

public class BaseField55 implements I8583Field {

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField55.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String,Object> tradeInfo;

    public BaseField55(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    /**
     * 从业务数据中取出IC卡数据，并根据规范要求输出。
     *
     * @return IC卡数据
     */
    @Override
    public String encode() {
        if (tradeInfo == null) {
            XLogUtil.e(TAG, "^_^ encode 输入参数 tradeInfo 为空 ^_^");
            return null;
        }
        String transType = (String) tradeInfo.get(TradeInformationTag.TRANSACTION_TYPE);
        if (TextUtils.isEmpty(transType)) {
            XLogUtil.e(TAG, "^_^ 获取交易类型数据失败 ^_^");
            return null;
        }

        String icData;
        if (TransCode.ESIGN_UPLOAD.equals(transType)){
            icData = (String) tradeInfo.get(TradeInformationTag.E_SLIP_KEY_DATA);
        }else {
            icData = (String) tradeInfo.get(TradeInformationTag.IC_DATA);
        }

        if (TextUtils.isEmpty(icData)) {
            XLogUtil.e(TAG, "^_^ IC卡数据为空 ^_^");
            return null;
        }
        XLogUtil.d(TAG, "^_^ encode result:" + icData + " ^_^");
        return icData;
    }

    /**
     * 从域数据中取出平台返回的IC卡数据，并保存到指定TAG
     *
     * @param fieldMsg 域数据
     * @return 业务数据对象，IC卡数据
     */
    @Override
    public Map<String, Object> decode(String fieldMsg) {
        if (TextUtils.isEmpty(fieldMsg)) {
            XLogUtil.e(TAG, "^_^ decode 输入参数 fieldMsg 为空 ^_^");
            return null;
        }

        Map<String, Object> tradeInfo = new HashMap<>(1);
        tradeInfo.put(TradeInformationTag.IC_DATA, fieldMsg);
        XLogUtil.d(TAG, "^_^ decode result:" + tradeInfo + " ^_^");
        return tradeInfo;
    }

}
