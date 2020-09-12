package com.centerm.epos.msg;

import java.util.Map;

/**
 * Created by yuhc on 2017/2/23.
 *
 */

public interface ITransactionMessage {

    /**
     * 报文打包
     * @param transTag  交易标识
     * @param transData 业务数据
     * @return  报文
     */
    public Object packMessage(String transTag, Map<String,Object> transData);

    /**
     * 报文解包
     * @param transTag  交易标识
     * @param streamData    原始数据
     * @return  业务数据
     */
    public Map<String,Object> unPackMessage(String transTag, Object streamData);
}
