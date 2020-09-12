package com.centerm.epos.channels.online;

/**
 * Created by 王玮 on 2016/9/7.
 */
public class ResponseHeader {

    /**
     * version : 01
     * devMask : 1234567890
     * timestamp : 20160629194728
     * token : 123456
     * status : 031500
     * msg : 操作成功
     */

    private String version;
    private String devMask;
    private String timestamp;
    private String token;
    private String status;
    private String msg;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
