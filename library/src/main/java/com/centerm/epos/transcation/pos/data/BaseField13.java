package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;

import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.XLogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuhc on 2017/2/8.<br>
 * 域12 受卡方所在地日期(Local Date Of Transaction),4个字节的定长数字字符域<br>
 * 交易发生时，受卡方(平台)所在地日期。格式为MMDD，其中MM为月份（01－12），DD为日(01－31)。
 */

public class BaseField13 implements I8583Field {

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField13.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String,Object> tradeInfo;

    public BaseField13(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    /**
     * 占位，无实际功能。交易日期为系统返回
     * @return null
     */
    @Override
    public String encode() {
        String entryMode = (String) tradeInfo.get(TradeInformationTag.TRANS_DATE);
        XLogUtil.d(TAG, "^_^ encode result:" + entryMode + " ^_^");
        if(!TextUtils.isEmpty(entryMode)){
            return entryMode;
        }else{
            return null;
        }
    }

    /**
     * 从域数据中取出平台返回的交易日期，并保存到指定TAG
     *
     * @param fieldMsg 域数据
     * @return 业务数据对象，交易日期
     */
    @Override
    public Map<String, Object> decode(String fieldMsg) {
        if (TextUtils.isEmpty(fieldMsg)) {
            XLogUtil.e(TAG, "^_^ decode 输入参数 fieldMsg 为空 ^_^");
            return null;
        }

        Map<String, Object> tradeInfo = new HashMap<>(1);
        tradeInfo.put(TradeInformationTag.TRANS_DATE, fieldMsg);
        XLogUtil.d(TAG, "^_^ decode result:" + tradeInfo + " ^_^");
        return tradeInfo;
    }
}
