package com.centerm.epos.bean.transcation;

/**
 * Created by yuhc on 2017/2/12.
 * 原始交易信息
 */

public class OriginalMessage {
    /**
     * 原始交易批次号
     */
    Long batchNumber;

    /**
     * 原始交易POS流水号
     */
    Long traceNumber;

    /**
     * 原始交易日期
     */
    String date;
    /**
     * 原始交易时间
     */
    String time;
    /**
     * 原交易授权方式代码
     */
    String authCode;

    /**
     * 原交易授权机构代码
     */
    String organizationCode;

    /*
    * 脱机退货原终端号
    * */
    String termNo;

    public OriginalMessage() {
    }

    public OriginalMessage(Long batchNumber, Long traceNumber, String date) {
        this.batchNumber = batchNumber;
        this.traceNumber = traceNumber;
        this.date = date;
    }
    public OriginalMessage(Long batchNumber, Long traceNumber, String date,String time) {
        this.batchNumber = batchNumber;
        this.traceNumber = traceNumber;
        this.date = date;
        this.time = time;
    }

    public OriginalMessage(Long batchNumber, Long traceNumber, String date, String authCode, String
            organizationCode) {
        this.batchNumber = batchNumber;
        this.traceNumber = traceNumber;
        this.date = date;
        this.authCode = authCode;
        this.organizationCode = organizationCode;
    }

    public Long getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(Long batchNumber) {
        this.batchNumber = batchNumber;
    }

    public Long getTraceNumber() {
        return traceNumber;
    }

    public void setTraceNumber(Long traceNumber) {
        this.traceNumber = traceNumber;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getOrganizationCode() {
        return organizationCode;
    }

    public void setOrganizationCode(String organizationCode) {
        this.organizationCode = organizationCode;
    }

    public void setTermNo(String termNo) {
        this.termNo = termNo;
    }
    public String getTermNo() {
        return termNo;
    }
}
