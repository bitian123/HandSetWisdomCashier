package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;

import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.XLogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuhc on 2017/2/8.<br>
 * 域12 受卡方所在地时间(Local Time Of Transaction),6个字节的定长数字字符域<br>
 * 交易发生时，受卡方(平台)所在地时间。格式为hhmmss，其中hh为小时，mm为分，ss为秒。
 */

public class BaseField12 implements I8583Field {

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField12.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String,Object> tradeInfo;

    public BaseField12(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }
    /**
     * 占位，无实际功能。
     * @return null
     */
    @Override
    public String encode() {

        String entryMode = (String) tradeInfo.get(TradeInformationTag.TRANS_TIME);
        XLogUtil.d(TAG, "^_^ encode result:" + entryMode + " ^_^");
        if(!TextUtils.isEmpty(entryMode)){
            return entryMode;
        }else{
            return null;
        }

    }

    /**
     * 从域数据中取出平台返回的交易时间，并保存到指定TAG
     *
     * @param fieldMsg 域数据
     * @return 业务数据对象，交易时间
     */
    @Override
    public Map<String, Object> decode(String fieldMsg) {
        if (TextUtils.isEmpty(fieldMsg)) {
            XLogUtil.e(TAG, "^_^ decode 输入参数 fieldMsg 为空 ^_^");
            return null;
        }

        Map<String, Object> tradeInfo = new HashMap<>(1);
        tradeInfo.put(TradeInformationTag.TRANS_TIME, fieldMsg);
        XLogUtil.d(TAG, "^_^ decode result:" + tradeInfo + " ^_^");
        return tradeInfo;
    }
}
