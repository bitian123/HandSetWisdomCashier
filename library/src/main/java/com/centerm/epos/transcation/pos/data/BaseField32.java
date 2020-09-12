package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;

import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.XLogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuhc on 2017/2/8.<br>
 * 域32 受理机构标识码(Acquiring Institution Identification Code)，最大11个字节数字。<br>
 * 目前用八位数字来标识一个机构，故若此域的长度值不等于8则被认为消息格式出错。
 */

public class BaseField32 implements I8583Field {

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField32.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String,Object> tradeInfo;

    public BaseField32(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    /**
     * 占位，无实际功能。受理机构标识码为系统返回
     * @return null
     */
    @Override
    public String encode() {
        return null;
    }

    /**
     * 从域数据中取出平台返回的受理机构标识码，并保存到指定TAG
     *
     * @param fieldMsg 域数据
     * @return 业务数据对象，受理机构标识码
     */
    @Override
    public Map<String, Object> decode(String fieldMsg) {
        if (TextUtils.isEmpty(fieldMsg)) {
            XLogUtil.e(TAG, "^_^ decode 输入参数 fieldMsg 为空 ^_^");
            return null;
        }

        Map<String, Object> tradeInfo = new HashMap<>(1);
        tradeInfo.put(TradeInformationTag.INSTITUTION_ID_CODE, fieldMsg);
        XLogUtil.d(TAG, "^_^ decode result:" + tradeInfo + " ^_^");
        return tradeInfo;
    }
}
