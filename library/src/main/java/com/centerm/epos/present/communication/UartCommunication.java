package com.centerm.epos.present.communication;

/**
 * Created by yuhc on 2017/2/13.
 * 采用串口方式的数据交互。此模块需要放到单独的线程中执行。
 */

public class UartCommunication implements ICommunication {
    private static final String TAG = UartCommunication.class.getSimpleName();

    //通讯参数
    private ICommunicationParameter mCommunicationParameter;

    public UartCommunication(ICommunicationParameter mCommunicationParameter) {
        this.mCommunicationParameter = mCommunicationParameter;
    }

    @Override
    public Boolean connect() {
        return null;
    }

    @Override
    public int sendData(byte[] data) {
        return 0;
    }

    @Override
    public byte[] receivedData(int requestLen) {
        return new byte[0];
    }

    @Override
    public void disconnect() {

    }

    @Override
    public Boolean isReceivedOver(byte[] receiveData) {
        return null;
    }
}
