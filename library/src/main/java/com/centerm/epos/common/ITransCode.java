package com.centerm.epos.common;

import java.util.Map;
import java.util.Set;

/**
 * Created by yuhc on 2017/6/8.
 */

public interface ITransCode {

    /**
     * 注册需要打印IC卡信息的交易/业务
     */
    Set<String> registerPrintICInfoTrade();

    Set<String> removePrintICInfoTrade();

    /**
     * 注册需要冲正的交易/业务
     */
    Set<String> registerReverseTrade();

    Set<String> removeReverseTrade();

    /**
     * 登记管理类业务，或无需计算MAC的业务
     */
    Set<String> registerManagerTrade();

    Set<String> removeManagerTrade();

    /**
     * 登记需要保存
     */
    Set<String> registerTradeForRecord();

    Set<String> removeTradeForRecord();

    /**
     * 登记借记交易，用于结算统计
     */
    Set<String> registerTradeForDebit();

    Set<String> removeTradeForDebit();

    /**
     * 登记贷记交易，用于结算统计
     */
    Set<String> registerTradeForCredit();

    Set<String> removeTradeForCredit();


    /**
     * 注册不会触发自动签到的交易
     */
    Set<String> registerTradeDiscardAutoSign();

    /**
     * 登记交易名称
     *
     * @return 名称
     */
    Map<String, Integer> registerTradeName();

    /**
     *用于签购单打印消息类型,英文名称
     * author zhouzhihua 2018.01.11
     *{@link TransCode#TRANS_ENGLISH_NAME_MAP}<br/>
     * @return 名称
     */
    Map<String, Integer> registerTradeNameEn();

    /**
     * 登记完整PBOC流程交易
     */
    Set<String> registerTradeForFullPboc();

    Set<String> removeTradeForFullPboc();

}
