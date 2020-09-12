package com.centerm.epos.transcation.pos.data;

import java.util.Map;

/**
 * Created by yuhc on 2017/2/9.
 *
 */

public class SimpleField implements I8583Field {

    /**
     * 业务数据
     */
    private Map<String,Object> tradeInfo;

    public SimpleField(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    @Override
    public String encode() {
        return null;
    }

    @Override
    public Map<String, Object> decode(String fieldMsg) {
        return null;
    }
}
