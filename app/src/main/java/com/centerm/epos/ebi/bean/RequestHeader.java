package com.centerm.epos.ebi.bean;

import java.io.Serializable;

/**
 * Created by liubit on 2017/12/25.
 */

public class RequestHeader implements Serializable{
    /**
     * msgType : 0210
     * termidm : lipingjhpaytest
     * mercode : 872880015200001
     * termcde : 12345678
     * imei : 869612028790724
     * sendTime : 20171225164748
     */

    private String msgType;
    private String termidm;
    private String mercode;
    private String termcde;
    private String imei;
    private String sendTime;

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getTermidm() {
        return termidm;
    }

    public void setTermidm(String termidm) {
        this.termidm = termidm;
    }

    public String getMercode() {
        return mercode;
    }

    public void setMercode(String mercode) {
        this.mercode = mercode;
    }

    public String getTermcde() {
        return termcde;
    }

    public void setTermcde(String termcde) {
        this.termcde = termcde;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    @Override
    public String toString() {
        return "RespHeader{" +
                "msgType='" + msgType + '\'' +
                ", termidm='" + termidm + '\'' +
                ", mercode='" + mercode + '\'' +
                ", termcde='" + termcde + '\'' +
                ", imei='" + imei + '\'' +
                ", sendTime='" + sendTime + '\'' +
                '}';
    }

}
