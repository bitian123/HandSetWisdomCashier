package com.centerm.epos.channels.online;

/**
 * Created by yuhc on 2017/3/23.
 */

public class PayChannelBody {

    private String payCode;

    public PayChannelBody() {
    }

    public PayChannelBody(String payCode) {
        this.payCode = payCode;
    }

    public String getPayCode() {
        return payCode;
    }

    public void setPayCode(String payCode) {
        this.payCode = payCode;
    }
}
