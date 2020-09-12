package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;

import com.centerm.epos.bean.TranscationFactor;
import com.centerm.epos.model.ITradeParameter;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.transcation.pos.constant.TranscationFactorTable;
import com.centerm.epos.utils.XLogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuhc on 2017/2/9.<br>
 * 域25 服务点条件码(Point Of Service Condition Mode)，2个字节的定长数字字符域。<br>
 * 服务点条件码，用于和其他关键域来决定消息种类。
 */

public class BaseField25 implements I8583Field {
    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField25.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String,Object> tradeInfo;

    public BaseField25(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    @Override
    public String encode() {
        if (tradeInfo == null){
            XLogUtil.e(TAG, "^_^ encode 输入参数 tradeInfo 为空 ^_^");
            return null;
        }
        //先从输入参数中获取服务点条件码
        String conditionMode = (String) tradeInfo.get(TradeInformationTag.SERVICE_CONDITION_MODE);
        if (!TextUtils.isEmpty(conditionMode)){
            XLogUtil.d(TAG, "^_^ encode: "+TradeInformationTag.SERVICE_CONDITION_MODE+"="+conditionMode+" ^_^");
            return conditionMode;
        }
        //输入参数中无服务点条件码，再通过交易名称获取服务点条件码
        String tradeName = getTradeName();
        if (!TextUtils.isEmpty(tradeName)){
            conditionMode = getConditionModeByName(tradeName);
            XLogUtil.d(TAG, "^_^ encode: "+TradeInformationTag.TRANSACTION_TYPE+"="+tradeName+" && "+TradeInformationTag
                    .SERVICE_CONDITION_MODE+"="+conditionMode+" ^_^");
            if (!TextUtils.isEmpty(conditionMode))
                return conditionMode;
        }

        XLogUtil.d(TAG, "^_^ encode: "+"服务点条件码为空"+" ^_^");
        return null;
    }

    @Override
    public Map<String, Object> decode(String fieldMsg) {
        if (TextUtils.isEmpty(fieldMsg)){
            XLogUtil.e(TAG, "^_^ decode 输入参数 fieldMsg 为空 ^_^");
            return null;
        }
        Map<String,Object> tradeInfo = new HashMap<>(1);
        tradeInfo.put(TradeInformationTag.SERVICE_CONDITION_MODE, fieldMsg);
        XLogUtil.d(TAG, "^_^ decode result:"+tradeInfo+" ^_^");
        return tradeInfo;
    }

    /**
     * 根据业务名称获取服务点条件码，从交易要素表中获取。
     * @param tradeName 交易名称，例如消费
     * @return 服务点条件码
     */
    private String getConditionModeByName(String tradeName){
        TranscationFactor transcationFactor = TranscationFactorTable.getTranscationFactor(tradeName);
        if (transcationFactor == null)
            return null;
        return transcationFactor.getServicePoint();
    }

    private String getTradeName() {
        String tradeName = null;
        ArrayList<String> msgTagList = (ArrayList<String>) tradeInfo.get(ITradeParameter.KEY_MSG_TAGS);
        if (msgTagList != null && msgTagList.size() > 0)
            tradeName = msgTagList.get(0);
        if (TextUtils.isEmpty(tradeName))
            tradeName = (String) tradeInfo.get(TradeInformationTag.TRANSACTION_TYPE);
        return tradeName;
    }
}
