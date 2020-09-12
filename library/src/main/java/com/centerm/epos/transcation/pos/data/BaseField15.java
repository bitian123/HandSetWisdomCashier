package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;

import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.XLogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuhc on 2017/2/8.<br>
 * 域15 清算日期(Date Of Settlement)，4个字节的定长数字字符域。<br>
 * POS中心和发卡方之间的交易结算日期。格式为MMDD，其中MM为月份，DD为日。
 */

public class BaseField15 implements I8583Field {

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField15.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String,Object> tradeInfo;

    public BaseField15(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }
    /**
     * 占位，无实际功能。清算日期为系统返回
     * @return null
     */
    @Override
    public String encode() {
        if (tradeInfo == null) {
            XLogUtil.e(TAG, "^_^ encode 输入参数 tradeInfo 为空 ^_^");
            return null;
        }
        String dateOfSettlement = (String) tradeInfo.get(TradeInformationTag.DATE_SETTLEMENT);
        XLogUtil.d(TAG, "^_^ encode result:" + dateOfSettlement + " ^_^");
        return dateOfSettlement;
    }

    /**
     * 从域数据中取出平台返回的清算日期，并保存到指定TAG
     *
     * @param fieldMsg 域数据
     * @return 业务数据对象，交易清算日期
     */
    @Override
    public Map<String, Object> decode(String fieldMsg) {
        if (TextUtils.isEmpty(fieldMsg)) {
            XLogUtil.e(TAG, "^_^ decode 输入参数 fieldMsg 为空 ^_^");
            return null;
        }

        Map<String, Object> tradeInfo = new HashMap<>(1);
        tradeInfo.put(TradeInformationTag.DATE_SETTLEMENT, fieldMsg);
        XLogUtil.d(TAG, "^_^ decode result:" + tradeInfo + " ^_^");
        return tradeInfo;
    }
}
