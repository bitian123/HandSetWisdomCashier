package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;

import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;
import com.centerm.epos.bean.TranscationFactor;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.transcation.pos.constant.TranscationFactorTable;
import com.centerm.epos.utils.XLogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuhc on 2017/2/8.<br>
 * 域0：交易类型代码
 */

public class BaseField0 implements I8583Field {

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField0.class.getSimpleName();
    private Map<String,Object> tradeInfo;

    public BaseField0(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    /**
     * 从业务数据中取出交易类型代码，4个Byte的ASC码数据。
     * @return  交易类型代码
     */
    @Override
    public String encode() {
        if (tradeInfo == null){
            XLogUtil.e(TAG, "^_^ encode 输入参数 tradeInfo 为空 ^_^");
            return null;
        }
        String transCode = (String) tradeInfo.get(TradeInformationTag.TRANSACTION_CODE);
        if (TextUtils.isEmpty(transCode)){
            String tradeName = (String) tradeInfo.get(TradeInformationTag.TRANSACTION_TYPE);
            if (!TextUtils.isEmpty(tradeName)){
                transCode = getMessageTypeCodeByName(tradeName);
            }
        }
        if (TextUtils.isEmpty(transCode)) {
            XLogUtil.e(TAG, "^_^ 获取交易类型码失败！^_^");
            return null;
        }
        XLogUtil.d(TAG, "^_^ encode result:"+transCode+" ^_^");
        return transCode;
    }

    /**
     * 从域数据中取出交易类型代码，并保存到指定TAG
     * @param fieldMsg  域数据
     * @return  业务数据对象
     */
    @Override
    public Map<String,Object> decode(String fieldMsg) {
        if (TextUtils.isEmpty(fieldMsg)){
            XLogUtil.e(TAG, "^_^ decode 输入参数 fieldMsg 为空 ^_^");
            return null;
        }

        Map<String,Object> tradeInfo = new HashMap<>(1);
        tradeInfo.put(TradeInformationTag.TRANSACTION_TYPE, fieldMsg);
        XLogUtil.d(TAG, "^_^ decode result:"+tradeInfo+" ^_^");
        return tradeInfo;
    }

    /**
     * 根据业务名称获取交易类型代码，从交易要素表中获取。
     * @param tradeName 交易名称，例如消费
     * @return 交易处理码
     */
    private String getMessageTypeCodeByName(String tradeName){
        TranscationFactor transcationFactor = TranscationFactorTable.getTranscationFactor(tradeName);
        if (transcationFactor == null)
            return null;
        return transcationFactor.getMessageTypeRequest();
    }
}
