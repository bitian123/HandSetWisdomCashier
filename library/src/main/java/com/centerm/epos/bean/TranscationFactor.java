package com.centerm.epos.bean;

/**
 * Created by yuhc on 2017/2/9.
 * 交易要素
 */

public class TranscationFactor {
    private String transName;       //交易名称
    private String messageTypeRequest;     //消息类型请求
    private String messageTypeResponse;     //消息类型响应
    private String processCode;     //交易处理码（3域）
    private String servicePoint;    //服务点条件码(25域)
    private String tradeCode;       //交易类型码（60.1域）
    private Boolean isReverse;      //是否冲正

    public TranscationFactor() {
    }

    public TranscationFactor(String transName, String messageTypeRequest, String messageTypeResponse, String
            processCode, String servicePoint, String tradeCode, Boolean isReverse) {
        this.transName = transName;
        this.messageTypeRequest = messageTypeRequest;
        this.messageTypeResponse = messageTypeResponse;
        this.processCode = processCode;
        this.servicePoint = servicePoint;
        this.tradeCode = tradeCode;
        this.isReverse = isReverse;
    }

    public String getMessageTypeResponse() {
        return messageTypeResponse;
    }

    public void setMessageTypeResponse(String messageTypeResponse) {
        this.messageTypeResponse = messageTypeResponse;
    }

    public String getTransName() {
        return transName;
    }

    public void setTransName(String transName) {
        this.transName = transName;
    }

    public String getMessageTypeRequest() {
        return messageTypeRequest;
    }

    public void setMessageTypeRequest(String messageTypeRequest) {
        this.messageTypeRequest = messageTypeRequest;
    }

    public String getProcessCode() {
        return processCode;
    }

    public void setProcessCode(String processCode) {
        this.processCode = processCode;
    }

    public String getServicePoint() {
        return servicePoint;
    }

    public void setServicePoint(String servicePoint) {
        this.servicePoint = servicePoint;
    }

    public String getTradeCode() {
        return tradeCode;
    }

    public void setTradeCode(String tradeCode) {
        this.tradeCode = tradeCode;
    }

    public Boolean getReverse() {
        return isReverse;
    }

    public void setReverse(Boolean reverse) {
        isReverse = reverse;
    }
}
