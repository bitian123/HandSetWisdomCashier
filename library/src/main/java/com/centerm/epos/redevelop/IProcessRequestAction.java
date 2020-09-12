package com.centerm.epos.redevelop;

import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;

/**
 * Created by yuhc on 2017/11/1.
 * 报文头处理
 */

public interface IProcessRequestAction {

    /**
     * 请求标志转换为对应的业务代码
     * @param flag  请求标志
     * @return  业务代码
     */
    String RequestFlag2TradeCode(String flag);


    /**
     * 请求标志是否已经定义的校验判断
     * @param flag  请求标志
     * @return  true 已定义  false 未定义
     */
    boolean RequestFlagCheck(String flag);


    /**
     * 执行报文头请求的业务
     * @param tradeView 通讯界面的视图
     * @param tradePresent  交易业务层
     * @return  true 正在执行 false 未执行
     */
    boolean doRequestAction(ITradeView tradeView, BaseTradePresent tradePresent);
}
