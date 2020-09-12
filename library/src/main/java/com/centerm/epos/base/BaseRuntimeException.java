package com.centerm.epos.base;

/**
 * Created by yuhc on 2017/2/13.
 *
 */

public class BaseRuntimeException extends RuntimeException {

    private int errCode;

    public BaseRuntimeException(int errCode) {
        this.errCode = errCode;
    }

    public BaseRuntimeException(int errCode, String detailMessage) {
        super(detailMessage);
        this.errCode = errCode;
    }

    public int getErrCode() {
        return errCode;
    }

}
