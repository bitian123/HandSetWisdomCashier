package com.centerm.epos.present.communication;

import com.centerm.epos.net.SequenceHandler;

/**
 * Created by yuhc on 2017/2/13.
 * 数据交换操作的帮助类
 */

public interface DataExchangerInterface {


    public void init(int mCommType, ICommunicationParameter parameterCallback);

    public void doSequenceExchange(String firstTag, byte[] firstData, SequenceHandler handler);

    /**
     * 执行数据交换,目前只考虑短连接
     *
     * @param clienData 待发送数据
     * @return 收到的返回数据
     */
    public byte[] doExchange(byte[] clienData);

}
