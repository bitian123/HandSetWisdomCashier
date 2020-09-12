package com.centerm.epos.channels.online;

/**
 * Created by 王玮 on 2016/9/7.
 */
public class HttpResponse<T> {
    private ResponseHeader header;
    private T body;
    private String MAC;

    public ResponseHeader getHeader() {
        return header;
    }

    public void setHeader(ResponseHeader header) {
        this.header = header;
    }

    public String getMAC() {
        return MAC;
    }

    public void setMAC(String MAC) {
        this.MAC = MAC;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return CommonUtils.getString(header) + CommonUtils.getString(body) + MAC;
    }

}
