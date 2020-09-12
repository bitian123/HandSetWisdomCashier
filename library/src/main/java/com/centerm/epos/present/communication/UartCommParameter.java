package com.centerm.epos.present.communication;

/**
 * Created by yuhc on 2017/2/13.
 * TCP通讯参数
 */

public class UartCommParameter implements ICommunicationParameter {

    @Override
    public Object getConnectParameter() {
        return null;
    }

    @Override
    public Object getSendParameter() {
        return "10";
    }

    @Override
    public Object getReceiveParameter() {
        return "60";
    }

    @Override
    public Object getDisconnectParam() {
        return null;
    }

}
