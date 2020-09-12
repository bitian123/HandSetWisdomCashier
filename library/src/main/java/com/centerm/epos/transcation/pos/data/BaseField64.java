package com.centerm.epos.transcation.pos.data;

import com.centerm.epos.utils.XLogUtil;

import java.util.Map;

/**
 * Created by yuhc on 2017/2/9.<br>
 * 未定义的数据域处理
 */

public class BaseField64 implements I8583Field {

    private static final String TAG = BaseField64.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String,Object> tradeInfo;

    public BaseField64(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    @Override
    public String encode() {
        XLogUtil.d(TAG, "^_^ nothing encode ^_^");
        return "0000000000000000";
    }

    @Override
    public Map<String, Object> decode(String fieldMsg) {
        XLogUtil.d(TAG, "^_^ nothing decode ^_^");
        return null;
    }
}
