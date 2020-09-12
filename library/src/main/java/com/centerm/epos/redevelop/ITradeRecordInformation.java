package com.centerm.epos.redevelop;

/**
 * Created by yuhc on 2017/8/23.
 * 交易记录信息
 */

public interface ITradeRecordInformation {

    /**
     * 清空交易记录
     * @return  true 成功 false 失败
     */
    boolean clearRecord();

    /**
     * 是否有交易记录
     * @return  true 有 false 没有
     */
    boolean isTradeRecordExist();
}
