package com.centerm.epos.event;

/**
 * Created by yuhc on 2017/3/17.
 *
 */

public class SimpleMessageEvent<T> {

    private String message;
    private int code;
    private T parameterData;

    public SimpleMessageEvent() {
    }

    public SimpleMessageEvent(int code) {
        this.code = code;
    }

    public SimpleMessageEvent(int code, String message, T parameterData) {
        this.message = message;
        this.code = code;
        this.parameterData = parameterData;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getParameterData() {
        return parameterData;
    }

    public void setParameterData(T parameterData) {
        this.parameterData = parameterData;
    }
}
