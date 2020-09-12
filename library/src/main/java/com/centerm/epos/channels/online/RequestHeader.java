package com.centerm.epos.channels.online;

import android.content.Context;
import android.text.TextUtils;

/**
 * Created by 王玮 on 2016/6/21.
 */
public class RequestHeader {


    /**
     * version : 01
     * devMask : 1234567890123456
     * IMEI : 1234567890123456
     * netMark : 1234567890123456
     * timestamp : 20160101010101
     * token : 123456
     */

    private String version;
    private String devMask;
    private String imei;
    private String netMark;
    private String timestamp;
    private String token;

    public RequestHeader(Context context) {
        setVersion(CommonUtils.getVersion(context));//获取当前版本号
        setDevMask(CommonUtils.generateDevMask());
        setImei(PhoneInfo.init(context).getIMEI());//获取Imei
        String netMark = PhoneInfo.init(context).getIMSI();//获取IMSI
        if (!TextUtils.isEmpty(netMark)) {
            setNetMark(netMark);
        } else {
            netMark = "000" + CommonUtils.getMacAddress(context);
            netMark = netMark.replace(":", "");
            setNetMark(netMark);
        }
        setTimestamp(CommonUtils.getCurrentTime());//初始化的时候获取当前时间戳
        setToken(CommonUtils.getRandom());//获取随机数
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDevMask() {
        return devMask;
    }

    public void setDevMask(String devMask) {
        this.devMask = devMask;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getNetMark() {
        return netMark;
    }

    public void setNetMark(String netMark) {
        this.netMark = netMark;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
