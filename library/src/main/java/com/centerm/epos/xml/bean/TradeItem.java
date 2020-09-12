package com.centerm.epos.xml.bean;

/**
 * Created by yuhc on 2017/4/1.
 * 管理类业务配置
 */

public class TradeItem {
    String tradeTag;
    String checkerClz;
    String tradeClz;

    public TradeItem(String tradeTag, String checkerClz, String tradeClz) {
        this.tradeTag = tradeTag;
        this.checkerClz = checkerClz;
        this.tradeClz = tradeClz;
    }

    public String getTradeTag() {
        return tradeTag;
    }

    public void setTradeTag(String tradeTag) {
        this.tradeTag = tradeTag;
    }

    public String getCheckerClz() {
        return checkerClz;
    }

    public void setCheckerClz(String checkerClz) {
        this.checkerClz = checkerClz;
    }

    public String getTradeClz() {
        return tradeClz;
    }

    public void setTradeClz(String tradeClz) {
        this.tradeClz = tradeClz;
    }
}
