package com.centerm.epos.utils;
/**
 * Copyright 2014, Fujian Centerm Information Co.,Ltd.  All right reserved.
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF  FUJIAN CENTERM PAY CO.,
 * LTD.  THE CONTENTS OF THIS FILE MAY NOT BE DISCLOSED TO THIRD
 * PARTIES, COPIED OR DUPLICATED IN ANY FORM, IN WHOLE OR IN PART,
 * WITHOUT THE PRIOR WRITTEN PERMISSION OF  FUJIAN CENTERM PAY CO., LTD.
 * <p>
 * TLV函数
 * Edit History:
 * <p>
 * 2014/09/11 - Created by Xrh.
 * <p>
 * Edit History：
 * <p>
 * 2014/10/22 - Modified by Xrh.
 * L字段长度改为无符号整型
 */

import android.util.Log;

import com.centerm.smartpos.util.HexUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * tlv工具类
 *
 */
public class TlvUtil {

    /**
     * tlv格式字符串解析成MAP对象
     * @param tlv tlv格式数据
     * @return
     */
    public static Map<String, String> tlvToMap(String tlv) {
        return tlvToMap(HexUtil.hexStringToByte(tlv));
    }

    /**
     * 若tag标签的第一个字节后四个bit为“1111”,则说明该tag占两个字节
     * 例如“9F33”;否则占一个字节，例如“95”
     * @param tlv
     * @return
     */
    public static Map<String, String> tlvToMap(byte[] tlv) {
        Map<String, String> map = new HashMap<String, String>();
        int index = 0;
        while (index < tlv.length) {
            if ((tlv[index] & 0x1F) == 0x1F) { //tag双字节
                byte[] tag = new byte[2];
                System.arraycopy(tlv, index, tag, 0, 2);
                index += 2;

                int length = 0;
                if (tlv[index] >> 7 == 0) {     //表示该L字段占一个字节
                    length = tlv[index];    //value字段长度
                    index++;
                } else { //表示该L字段不止占一个字节

                    int lenlen = tlv[index] & 0x7F; //获取该L字段占字节长度
                    index++;

                    for (int i = 0; i < lenlen; i++) {
                        length = length << 8;
                        length += tlv[index] & 0xff;  //value字段长度 &ff转为无符号整型
                        index++;
                    }
                }
                if (length > 10000){
                    return map;
                }
                byte[] value = new byte[length];
                System.arraycopy(tlv, index, value, 0, length);
                index += length;
                map.put(HexUtil.bcd2str(tag), HexUtil.bcd2str(value));
            } else { //tag单字节
                byte[] tag = new byte[1];
                System.arraycopy(tlv, index, tag, 0, 1);
                index++;

                int length = 0;
                if (tlv[index] >> 7 == 0) {    //表示该L字段占一个字节
                    length = tlv[index]; //value字段长度
                    index++;
                } else { //表示该L字段不止占一个字节

                    int lenlen = tlv[index] & 0x7F; //获取该L字段占字节长度
                    index++;

                    for (int i = 0; i < lenlen; i++) {
                        length = length << 8;
                        length += tlv[index] & 0xff;  //value字段长度&ff转为无符号整型
                        index++;
                    }
                }
                if (length > 10000){
                    return map;
                }
                byte[] value = new byte[length];
                System.arraycopy(tlv, index, value, 0, length);
                index += length;
                map.put(HexUtil.bcd2str(tag), HexUtil.bcd2str(value));
            }
        }

        return map;
    }

    public static String encodingTLV(Map tlvMap) {
        String str = "";
        Iterator iter = tlvMap.entrySet().iterator();
        String tag = "";
        String length = "";
        String value = "";
        Map.Entry entry;
        while (iter.hasNext()) {
            entry = (Map.Entry) iter.next();
            tag = (String) entry.getKey();
            value = (String) entry.getValue();
            length = String.valueOf(Integer.parseInt(String.valueOf(value.length() / 2), 16));
            length = length.length() == 1 ? "0" + length : length;
            str += tag + length + value;
        }
        return str;
    }

    public static String mapToTlv(Map<String, String> map) {
        return HexUtil.bytesToHexString(mapToTlvBytes(map));

    }

    public static byte[] mapToTlvBytes(Map<String, String> map) {
        int len = 0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            len += entry.getKey().length();
            if (entry.getValue() != null) {

                int lenght = entry.getValue().length();
                if (lenght < 127 && lenght > 0) {
                    len += 2;
                } else {
                    if (lenght < 255) {
                        len += 4;
                    } else {
                        if (lenght < 65535) {
                            len += 6;
                        } else {
                            Log.e("", "长度超出");
                        }
                    }
                }
                len += entry.getValue().length();
            } else {
                Log.e("", "值为空");
                continue;
            }
        }
        byte[] tlvData = new byte[len / 2];
        int pos = 0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            byte[] key = HexUtil.hexStringToByte(entry.getKey());
            System.arraycopy(key, 0, tlvData, pos, key.length);
            pos += key.length;
            if (entry.getValue() != null) {
                byte[] value = HexUtil.hexStringToByte(entry.getValue());
                int lenght = value.length;
                if (lenght < 127 && lenght > 0) {
                    tlvData[pos] = (byte) value.length;
                    pos++;
                } else {
                    if (lenght < 255) {
                        tlvData[pos] = (byte) 0x81;
                        pos++;
                        tlvData[pos] = (byte) value.length;
                        pos++;
                    } else {
                        if (lenght < 65535) {
                            tlvData[pos] = (byte) 0x82;
                            pos++;
                            tlvData[pos] = (byte) ((lenght >> 8) & 0xFF);
                            pos++;
                            tlvData[pos] = (byte) (lenght & 0xFF);
                            pos++;
                        } else {
                            Log.e("", "长度超出");
                            continue;
                        }
                    }
                }
                System.arraycopy(value, 0, tlvData, pos, value.length);
                pos += value.length;
            } else {
                tlvData[pos] = 0;
                pos++;
            }
        }
        return tlvData;
    }

}