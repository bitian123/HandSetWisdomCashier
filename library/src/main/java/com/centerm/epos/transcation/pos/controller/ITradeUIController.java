package com.centerm.epos.transcation.pos.controller;

import java.util.Map;

/**
 * Created by yuhc on 2017/8/3.
 * 交易界面控制UI
 */

public interface ITradeUIController {

    /**
     * 是否显示界面
     * @param tradeType 交易类型
     * @return  true 显示
     */
    boolean isShowUI(String tradeType);

    /**
     * 设置交易数据
     * @param tradeData 交易数据
     */
    void setTransctionData(Map<String, Object> tradeData);

    /**
     * 设置交易数据
     * @param tempData 交易数据
     */
    void setTransctionTempData(Map<String, String> tempData);
}
