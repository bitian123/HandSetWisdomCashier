package com.centerm.epos.event;

import java.security.PublicKey;

/**
 * Created by yuhc on 2017/9/13.
 * 批结算前的交易上传消费定义
 */

public class UploadTradeMessage {

    public static final int UPLOAD_START = 1;
    public static final int UPLOAD_NEXT = 2;
    public static final int UPLOAD_END = 3;
    public static final int UPLOAD_SCRIPT_RESULT = 4;
    public static final int UPLOAD_ESIGN = 5;
    public static final int UPLOAD_OFFLINE_TRANS = 6;

    private int what;

    public UploadTradeMessage() {
    }

    public UploadTradeMessage(int what) {
        this.what = what;
    }

    public int getWhat() {
        return what;
    }

    public void setWhat(int what) {
        this.what = what;
    }
}
