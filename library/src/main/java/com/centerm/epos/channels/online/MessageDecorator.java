package com.centerm.epos.channels.online;

import com.centerm.cloudsys.sdk.common.log.Log4d;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * HTTP报文修饰类
 */
public class MessageDecorator {

    private final static Log4d LOGGER = Log4d.getDefaultInstance();
    private final static String TAG = MessageDecorator.class.getSimpleName();

    public static Map<String, String> jsonToMap(String message) {
//        LOGGER.debug(TAG, "正在Json转Map");
//        LOGGER.info(TAG, "传入报文：" + message);
        Map<String, String> map = new HashMap<>();
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(message, JsonObject.class);
        if (json.has("header") && json.get("header") instanceof JsonObject) {
            jsonToMap(map, json.getAsJsonObject("header"));
        }
        if (json.has("body") && json.get("body") instanceof JsonObject) {
            jsonToMap(map, json.getAsJsonObject("body"));
        }

//        LOGGER.info(TAG, "转成map:" + map.toString());
        return map;
    }

    private static void json2Map(Map<String, String> map, String json) {
        if (map == null || json == null) {
            return;
        }
    }

    private static void jsonToMap(Map<String, String> map, JsonObject json) {
        if (map == null || json == null) {
            return;
        }
        for (Iterator<Map.Entry<String, JsonElement>> iterator = json.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, JsonElement> next = iterator.next();
            String key = next.getKey();
            String value = null;
            JsonElement ele = next.getValue();
            try {
                if (ele instanceof JsonArray || ele instanceof JsonObject) {
                    value = ele.toString();
                } else {
                    value = ele.getAsString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (value == null || value.trim().equals("")) {
                continue;
            }
            map.put(key, value);
        }
    }


    /**
     * 除去数组中的空值和签名参数
     *
     * @param sArray 签名参数组
     * @return 去掉空值与签名参数后的新签名参数组
     */
    private static Map<String, String> paraFilter(Map<String, String> sArray) {

        Map<String, String> result = new HashMap<String, String>();

        if (sArray == null || sArray.size() <= 0) {
            return result;
        }

        for (String key : sArray.keySet()) {
            String value = sArray.get(key);
            if (value == null || value.equals("") || key.equalsIgnoreCase("sign")
                    || key.equalsIgnoreCase("sign_type")) {
                continue;
            }
            result.put(key, value);
        }

        return result;
    }

    /**
     * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
     *
     * @param params 需要排序并参与字符拼接的参数组
     * @return 拼接后字符串
     */
    private static String createLinkString(Map<String, String> params) {
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        String prestr = "";
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);
            if (i == keys.size() - 1) {//拼接时，不包括最后一个&字符
                prestr = prestr + key + "=" + value;
            } else {
                prestr = prestr + key + "=" + value + "&";
            }
        }
//        LOGGER.info(TAG, "排序拼接后的参数：" + prestr);
        return prestr;
    }

    /**
     * 格式化报文参数，输出可以用来计算MAC的字符串
     *
     * @param message
     * @return
     */
    public static String formatParamsString(String message) {
        Map<String, String> map = jsonToMap(message);
        return createLinkString(map);
    }

}
