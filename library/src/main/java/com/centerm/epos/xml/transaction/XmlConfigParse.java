package com.centerm.epos.xml.transaction;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.text.TextUtils;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.util.Arrays;

/**
 * Created by yuhc on 2017/2/23.
 * 交易报文配置解析
 */

public class XmlConfigParse {
    private static final String TAG = XmlConfigParse.class.getSimpleName();

    public static final String PROCESS_TAG = "process";
    public static final String MSG_BODY_TAG = "body";
    public static final String FIELD_TAG = "field";

    public static final String MSG_TYPE_PACK = "PACK";
    public static final String MSG_TYPE_UNPACK = "UNPACK";
    /**
     * 从XML配置文件中获取交易所需的数据域
     * @param context   下上文，用于获取ASSERT下的资源
     * @param mctFilePath   配置文件路径
     * @param tradeCode 交易代码
     * @param packType  组包或是解包
     * @return  数据域
     */
    private static int[] parseFieldNumsFromXml(Context context, String mctFilePath, String tradeCode, String packType) {
        if (context == null || TextUtils.isEmpty(mctFilePath) || TextUtils.isEmpty(tradeCode)){
            Log.e(TAG, "^_^ parseFieldNumsFromXml方法的输入参数校验失败！ ^_^");
            return null;
        }

        int[] fieldArray = new int[128];
        int fieldCount = 0;
        try {
            InputStream is = context.getAssets().open(mctFilePath);
            XmlPullParser xmlResourceParser = XmlPullParserFactory.newInstance().newPullParser();
            xmlResourceParser.setInput(is,"UTF-8");

            int searchFlag = 0;
            String tagName;
            int evenType = xmlResourceParser.getEventType();
            while (evenType != XmlResourceParser.END_DOCUMENT) {
                switch (evenType){
                    case XmlPullParser.START_TAG:
                        tagName = xmlResourceParser.getName();
                        if (PROCESS_TAG.equals(tagName)
                                && tradeCode.equals(xmlResourceParser.getAttributeValue(0))
                                && packType.equals(xmlResourceParser.getAttributeValue(1))) {
                            searchFlag = 1;
                            Log.d(TAG, "^_^ found step1 key process ^_^");
                        }else if(1 == searchFlag && MSG_BODY_TAG.equals(tagName)) {
                            searchFlag = 2;
                            Log.d(TAG, "^_^ found step2 key body ^_^");
                        }else if (2 == searchFlag && FIELD_TAG.equals(tagName)){
                            Log.d(TAG, "^_^ found trade field" + ":" + xmlResourceParser.getAttributeValue(0)+ " ^_^");
                            fieldArray[fieldCount++] = Integer.parseInt(xmlResourceParser.getAttributeValue(0),10);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (2 == searchFlag && MSG_BODY_TAG.equals(xmlResourceParser.getName())) {
                            Log.d(TAG, "^_^ found end body fields count:" + fieldCount + "^_^");
                            if (fieldCount > 0) {
                                return Arrays.copyOf(fieldArray, fieldCount);
                            }
                            return null;
                        }
                        break;
                }
                evenType = xmlResourceParser.next();
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从XML配置文件中获取交易报文组包所需的数据域
     * @param context   下上文，用于获取ASSERT下的资源
     * @param xmlFilePath   配置文件路径
     * @param tradeCode 交易代码
     * @return  数据域
     */
    public static int[] parseMsgPackFieldNums(Context context, String xmlFilePath, String tradeCode){
        return parseFieldNumsFromXml(context, xmlFilePath, tradeCode, MSG_TYPE_PACK);
    }

    /**
     * 从XML配置文件中获取交易报文解包所需的数据域
     * @param context   下上文，用于获取ASSERT下的资源
     * @param xmlFilePath   配置文件路径
     * @param tradeCode 交易代码
     * @return  数据域
     */
    public static int[] parseMsgUnPackFieldNums(Context context, String xmlFilePath, String tradeCode){
        return parseFieldNumsFromXml(context, xmlFilePath, tradeCode, MSG_TYPE_UNPACK);
    }

}
