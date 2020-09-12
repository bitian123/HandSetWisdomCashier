package com.centerm.epos.bean.transcation;

import com.centerm.epos.transcation.pos.constant.TranscationConstant;

import java.util.Locale;

/**
 * Created by yuhc on 2017/2/12.
 * 余额信息
 */

public class BalancAmount {
    /**
     * 账户类型
     */
    private String accountType;

    /**
     * 余额类型
     */
    private String amountType;

    /**
     * 货币代码
     */
    private String currencyCode;

    /**
     * 余额符号
     */
    private char amountSign;

    /**
     * 余额
     */
    private long amount;

    public BalancAmount() {
    }

    public BalancAmount(String accountType, String amountType, String currencyCode, char amountSign, long amount) {
        this.accountType = accountType;
        this.amountType = amountType;
        this.currencyCode = currencyCode;
        this.amountSign = amountSign;
        this.amount = amount;
    }
    /*
    *
    * */
    public BalancAmount(String iso54) {
        if( iso54 != null ){
            accountType = iso54.substring(0,2);
            amountType = iso54.substring(2,4);
            currencyCode = iso54.substring(4,7);
            amountSign=iso54.charAt(7);
            amount = Long.parseLong(iso54.substring(8,20));
        }
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getAmountType() {
        return amountType;
    }

    public void setAmountType(String amountType) {
        this.amountType = amountType;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public char getAmountSign() {
        return amountSign;
    }

    public void setAmountSign(char amountSign) {
        this.amountSign = amountSign;
    }

    public long getAmount() {
        return amount;
    }

    public String getAmountFormat(){

        return String.format(Locale.CHINA,"%s%d.%02d",getAmountSign()== TranscationConstant.BALANCE_POSITIVE_CHAR ? "":"-",getAmount()/100,getAmount()%100);
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "账户类型：" + accountType + " 余额类型：" + amountType + " 货币代码：" + currencyCode
                + " 余额符号：" + amountSign + " 余额：" + amount;
    }
}
