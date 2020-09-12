package com.centerm.epos.msg;

import java.util.Map;

/**
 * Created by yuhc on 2017/2/23.
 */

public class HttpJsonMessage implements ITransactionMessage {
    @Override
    public Object packMessage(String transTag, Map<String, Object> transData) {
        return null;
    }

    @Override
    public Map<String, Object> unPackMessage(String transTag, Object streamData) {
        return null;
    }
}
