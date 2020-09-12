package com.centerm.epos.event;

/**
 * Created by yuhc on 2017/9/13.
 * 批结算前的交易上传消费定义
 */

public class UpgradeAppMessage {

    public static final int DOWNLOAD_NEW_APP = 1;
    public static final int DOWNLOAD_ERROR = 2;
    public static final int DOWNLOAD_COMPELETE = 3;
    public static final int CHECK_NEW_VERSION = 4;
    public static final int CHECK_RESULT = 5;
    public static final int SHOW_TIP = 6;

    private int what;
    private String message;

    public UpgradeAppMessage() {
    }

    public UpgradeAppMessage(int what) {
        this.what = what;
    }

    public UpgradeAppMessage(int what, String message) {
        this.what = what;
        this.message = message;
    }

    public int getWhat() {
        return what;
    }

    public void setWhat(int what) {
        this.what = what;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
