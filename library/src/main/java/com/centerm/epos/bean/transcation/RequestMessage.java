package com.centerm.epos.bean.transcation;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by yuhc on 2017/7/3.
 * 保存交易请求报文，用于批上送通知交易
 */

@DatabaseTable(tableName = "tb_request_msg")
public class RequestMessage {

    public static final String KEY_FIELD_NAME = "tradeIndex";

    @DatabaseField(id = true)//作为主键
    private String tradeIndex;//受卡方系统跟踪号（终端流水号）

    @DatabaseField
    private String requestMessage;//请求报文

    public RequestMessage() {
    }

    public RequestMessage(String tradeIndex, String requestMessage) {
        this.tradeIndex = tradeIndex;
        this.requestMessage = requestMessage;
    }

    public String getTradeIndex() {
        return tradeIndex;
    }

    public void setTradeIndex(String tradeIndex) {
        this.tradeIndex = tradeIndex;
    }

    public String getRequestMessage() {
        return requestMessage;
    }

    public void setRequestMessage(String requestMessage) {
        this.requestMessage = requestMessage;
    }
}
