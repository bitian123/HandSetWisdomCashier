package com.centerm.epos.redevelop;

/**
 * Created by FL on 2017/9/15 16:15.
 * 各项目的接收数据长度判断
 */

public interface ITCPIsReceivedOver {

    boolean isReceivedOver(byte[] receiveData);
}
