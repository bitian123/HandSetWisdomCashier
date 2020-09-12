package com.centerm.epos.utils;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by yuhc on 2017/5/16.
 *
 */

public class CustomTLVUtil {

    /**
     * 浦发TLV标签数据
     * @param mapData
     * @return
     */
    public static String generateSpdbTLVMsg(Map<Integer, String> mapData) {
        if (mapData == null || mapData.size() == 0)
            return null;
        StringBuilder stringBuilder = new StringBuilder();

        Set<Map.Entry<Integer, String>> keySets = mapData.entrySet();
        Iterator<Map.Entry<Integer,String>> iterator = keySets.iterator();
        Map.Entry<Integer,String> entry;
        while (iterator.hasNext()){
            entry = iterator.next();
            stringBuilder.append(String.format(Locale.CHINA, "%03d", entry.getKey()));
            stringBuilder.append(String.format(Locale.CHINA, "%03d", entry.getValue().length()));
            stringBuilder.append(entry.getValue());
        }
        return stringBuilder.toString();
    }

    public static Map<Integer,String> parseSpdbTlvMsg(String tlvMsg){
        if (TextUtils.isEmpty(tlvMsg))
            return null;
        Map<Integer,String> resultMap = new HashMap<>();
        int key, len, offset = 0;
        String strPointer;

        do {
            strPointer = tlvMsg.substring(offset, offset + 3);
            key = Integer.valueOf(strPointer, 10);
            offset += 3;
            strPointer = tlvMsg.substring(offset, offset + 3);
            len = Integer.valueOf(strPointer, 10);
            offset += 3;
            strPointer = tlvMsg.substring(offset, offset + len);
            offset += len;
            resultMap.put(key, strPointer);
        }while (offset+6 < tlvMsg.length());
        return resultMap;
    }

}
