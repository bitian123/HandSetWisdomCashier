package com.centerm.epos.channels.online;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.util.TypeUtils;
import com.centerm.epos.EposApplication;
import com.centerm.epos.utils.XLogUtil;

import java.util.Map;

/**
 * Created by yuhc on 2017/3/23.
 */

public class CpayMessageFactory {
    private static final String TAG = CpayMessageFactory.class.getSimpleName();

    public static String createRequestMsg(String channel) {
        if (TextUtils.isEmpty(channel)){
            XLogUtil.e(TAG, "^_^ " + "createRequestMsg:channel is null" + " ^_^");
            return null;
        }
        String message;
        //解决bean转json时，KEY值首字母被自动转换为小写的问题。
        TypeUtils.compatibleWithJavaBean = true;
        HttpRequest<PayChannelBody> httpRequest = new HttpRequest<>(EposApplication.getAppContext());
        httpRequest.setBody(new PayChannelBody(channel));
        String bodyNoMAC = JSON.toJSONString(httpRequest);
        XLogUtil.d(TAG, "^_^ " + "createRequestMsg:body without MAC = " + bodyNoMAC + " ^_^");
        String mac = CommonUtils.calculateMac(bodyNoMAC);
        XLogUtil.d(TAG, "^_^ " + "createRequestMsg:MAC = " + mac + " ^_^");
        httpRequest.setMAC(mac);
        message = JSON.toJSONString(httpRequest, SerializerFeature.WriteNullStringAsEmpty);
        XLogUtil.d(TAG, "^_^ " + "createRequestMsg:body = " + message + " ^_^");
        return message;
    }

    public static HttpResponse parseResponseMsg(String responseMsg){
        if (TextUtils.isEmpty(responseMsg)){
            XLogUtil.e(TAG, "^_^ " + "parseResponseMsg:response message is null" + " ^_^");
            return null;
        }
        XLogUtil.d(TAG, "^_^ " + "parseResponseMsg:" + responseMsg + " ^_^");
        return JSON.parseObject(responseMsg, HttpResponse.class);
    }
}
