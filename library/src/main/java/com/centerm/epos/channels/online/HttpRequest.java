package com.centerm.epos.channels.online;

import android.content.Context;

/**
 * Created by 王玮 on 2016/9/7.
 */
public class HttpRequest<T> {
    private RequestHeader header;
    private T body;
    private String MAC;

    public HttpRequest() {

    }

    public HttpRequest(Context context) {
        header = new RequestHeader(context);
        setHeader(header);
    }

    public RequestHeader getHeader() {
        return header;
    }

    public void setHeader(RequestHeader header) {
        this.header = header;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    public String getMAC() {
        return MAC;
    }

    public void setMAC(String MAC) {
        this.MAC = MAC;
    }
}
