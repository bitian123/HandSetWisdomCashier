package com.centerm.epos.event;

import com.centerm.epos.print.PrintManager;

/**
 * Created by yuhc on 2017/5/5.
 */

public class PrinteEvent {
    private int what;
    private int code;
    private String msg;
    PrintManager.SlipOwner slipOwner;

    public PrinteEvent() {
    }

    public PrinteEvent(int what) {
        this.what = what;
    }

    public PrinteEvent(int what, String msg) {
        this.what = what;
        this.msg = msg;
    }

    public PrinteEvent(int what, int code, String msg, PrintManager.SlipOwner slipOwner) {
        this.what = what;
        this.code = code;
        this.msg = msg;
        this.slipOwner = slipOwner;
    }

    public int getWhat() {
        return what;
    }

    public void setWhat(int what) {
        this.what = what;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public PrintManager.SlipOwner getSlipOwner() {
        return slipOwner;
    }

    public void setSlipOwner(PrintManager.SlipOwner slipOwner) {
        this.slipOwner = slipOwner;
    }
}
