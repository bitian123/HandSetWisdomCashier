package com.centerm.epos.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by yuhc on 2017/2/26.
 *
 */

public class ConverUtil {

    public static Map<String, String> convertObject2String(Map<String, Object> objMap) {
        if (objMap == null || objMap.size() == 0)
            return null;
        Map<String, String> strMap = new HashMap<>();
        Iterator<Map.Entry<String, Object>> iterator = objMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            if (entry.getValue() instanceof String)
                strMap.put(entry.getKey(), (String) entry.getValue());
        }
        return strMap;
    }
}
