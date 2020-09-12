package com.centerm.epos.present.communication;

/**
 * Created by yuhc on 2017/2/13.
 *  通讯参数回调接口
 */

public interface ICommunicationParameter {

    /**
     * 获取服务器/平台连接参数
     * @return 连接参数
     */
    public Object getConnectParameter();

    /**
     * 获取发送数据的通讯参数
     * @return  发送参数
     */
    public Object getSendParameter();

    /**
     * 获取接收数据时的通讯参数
     * @return  接收参数
     */
    public Object getReceiveParameter();

    /**
     * 获取断开连接的通讯参数
     * @return  断开参数
     */
    public Object getDisconnectParam();
}
