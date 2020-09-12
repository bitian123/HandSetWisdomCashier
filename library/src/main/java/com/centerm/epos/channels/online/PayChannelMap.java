package com.centerm.epos.channels.online;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuhc on 2017/3/23.
 */

public class PayChannelMap {
    private static final Map<String,String> payChannelMap = new HashMap<>();

    public static final String UNIONPAY = "BASE";

    static {
        payChannelMap.put("BASE", "2");


    }

    public static String getChannelCodeByName(String channelName){
        if (TextUtils.isEmpty(channelName))
            return null;
        return payChannelMap.get(channelName);
    }
}
