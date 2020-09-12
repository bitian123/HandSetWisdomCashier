package com.centerm.epos.present.communication;

/**
 * Created by yuhc on 2017/2/13.
 * 数据交换，发送接收数据。
 */

public interface IDataExchange {

    /**
     * 执行数据交换
     * @param clientData    客户端待发送数据
     * @return  服务器返回数据
     */
    public byte[] execute(byte[] clientData);
}
