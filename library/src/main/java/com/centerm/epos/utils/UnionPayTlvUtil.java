package com.centerm.epos.utils;

import android.text.TextUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by yuhc on 2017/6/23.
 * 传统POS，59域TLV数据格式化处理工具
 */

public class UnionPayTlvUtil {

    private static final String TAG = UnionPayTlvUtil.class.getSimpleName();
    private static final int TAG_LENGTH = 2;
    private static final int LEN_LENGTH = 3;
    private static final int VALUE_LEN_MAX = 1024;
    /**
     * TLV数据编码，其中tag标签数据属性为an2，子域长度数据属性为n3
     * @param tag   标签
     * @param value 内容
     * @return tlv字符串
     */
    public static String encode(String tag, String value){
        if (TextUtils.isEmpty(tag) || TextUtils.isEmpty(value)) {
            XLogUtil.e(TAG, "^_^ tag or value is null ^_^");
            return null;
        }
        if (tag.length() != TAG_LENGTH){
            XLogUtil.e(TAG, "^_^ tag length should be 2, but received "+tag.length()+" ^_^");
            return null;
        }
        return tag + String.format(Locale.CHINA, "%0"+LEN_LENGTH+"d", value.length()) + value;
    }

    public static String encode(String tag, byte[] value){
        if (TextUtils.isEmpty(tag) || value == null || value.length == 0) {
            XLogUtil.e(TAG, "^_^ tag or value is null ^_^");
            return null;
        }
        if (tag.length() != TAG_LENGTH){
            XLogUtil.e(TAG, "^_^ tag length should be 2, but received "+tag.length()+" ^_^");
            return null;
        }
        return tag + String.format(Locale.CHINA, "%0"+LEN_LENGTH+"d", value.length) + new String(value);
    }

    /**
     * TLV数据编码，其中tag标签数据属性为an2，子域长度数据属性为n3
     * @param dataMap   待编码的业务数据
     * @return  tlv字符串
     */
    public static String encode(Map<String, String> dataMap){
        if (dataMap == null || dataMap.size() == 0) {
            XLogUtil.e(TAG, "^_^ dataMap is null or size is 0^_^");
            return null;
        }
        Set<Map.Entry<String,String>> dataSet = dataMap.entrySet();
        StringBuilder strBuffer = new StringBuilder();
        String tlvStr;
        for (Map.Entry<String, String> entry : dataSet) {
            tlvStr = encode(entry.getKey(), entry.getValue());
            if (!TextUtils.isEmpty(tlvStr))
                strBuffer.append(tlvStr);
        }
        return strBuffer.toString();
    }

    /**
     * TLV数据解码，TAG 2个字符；LEN 3个字符
     * @param tlvString TLV字符串
     * @return  map数据
     */
    public static Map<String, String> decode(String tlvString){
        if (TextUtils.isEmpty(tlvString)){
            XLogUtil.e(TAG, "^_^ tlvString is null ^_^");
            return null;
        }
        Map<String, String> decodeResult = new HashMap<>();
        int len, offset = 0;
        String tagContainer,valueContainer;
        do{
            try {
                tagContainer = tlvString.substring(offset, offset+TAG_LENGTH);
                offset += TAG_LENGTH;
                len = Integer.parseInt(tlvString.substring(offset, offset+LEN_LENGTH), 10);
                offset += LEN_LENGTH;
                valueContainer = tlvString.substring(offset, offset+len);
                offset += len;
                decodeResult.put(tagContainer,valueContainer);
            } catch (Exception e) {
                XLogUtil.i(TAG, "^_^ TLV字符串数据解析越界异常 ^_^");
                break;
            }
        }while (offset < tlvString.length());
        return decodeResult;
    }

    public static Map<String, String> decode(byte[] tlvBytes) {
        if (tlvBytes == null || tlvBytes.length == 0)
            return null;
        return decode(new String(tlvBytes));
    }
}
