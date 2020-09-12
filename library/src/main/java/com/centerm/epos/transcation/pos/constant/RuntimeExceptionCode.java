package com.centerm.epos.transcation.pos.constant;

/**
 * Created by yuhc on 2017/9/14.
 * 运行时的错误代码
 */

public interface RuntimeExceptionCode {

    /**
     * 连接服务器失败
     */
    int CONNECT_SERVER_FAILED = 1;

    /**
     * 发送数据失败
     */
    int SEND_DATA_FAILED = 2;

    /**
     * 接收数据失败
     */
    int RECEIVE_DATA_FAILED = 3;
}
