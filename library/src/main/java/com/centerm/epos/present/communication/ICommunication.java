package com.centerm.epos.present.communication;

/**
 * Created by yuhc on 2017/2/13.
 * 数据交互接口，包含通讯的整个过程：连接、发送、接收、断开等功能。
 */

public interface ICommunication {

    int COMM_TCP = 1;
    int COMM_HTTP = 2;
    int COMM_UART = 3;
    int COMM_HTTPS = 4;
    int COMM_TCPS = 5;

    /**
     * 连接中心/服务器
     *
     * @return true成功，false失败
     */
    Boolean connect();

    /**
     * 发送数据
     *
     * @param data 数据
     * @return 成功发送的Byte数
     */
    int sendData(byte[] data);

    /**
     * 接收数据
     *
     * @param requestLen 请求接收的数据长度
     * @return 收到的数据
     */
    byte[] receivedData(int requestLen);

    /**
     * 断开连接
     */
    void disconnect();

    /**
     * 判断数据是否接收完整
     *
     * @param receiveData 已接收的数据
     * @return true 完整，false 没收完
     */
    Boolean isReceivedOver(byte[] receiveData);
}
