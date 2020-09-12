package com.centerm.epos.bean.transcation;

import android.text.TextUtils;

import com.centerm.epos.utils.MoneyUtil;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by yuhc on 2017/10/25.
 * 分期付款交易信息
 */

@DatabaseTable(tableName = "tb_trade_installment")
public class InstallmentInformation {

    @DatabaseField(id = true)//作为主键
    private String voucherNo;   //凭证号/终端流水号
    //分期数
    @DatabaseField
    String period;
    //首期还款金额
    @DatabaseField
    String payAmountFirst;
    //还款币种
    @DatabaseField
    String currencyCode;
    //持卡人分期付款手续费
    @DatabaseField
    String feeTotal;
    //分期付款奖励积分
    @DatabaseField
    String point;
    //手续费支付方式
    @DatabaseField
    String payMode;
    //首期手续费
    @DatabaseField
    String feeFirst;
    //每期手续费
    @DatabaseField
    String feeEach;
    //保留使用
    @DatabaseField
    String reserveMessage;

    public String getVoucherNo() {
        return voucherNo;
    }

    public void setVoucherNo(String voucherNo) {
        this.voucherNo = voucherNo;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getPayAmountFirst() {
        return payAmountFirst;
    }

    public void setPayAmountFirst(String payAmountFirst) {
        this.payAmountFirst = payAmountFirst;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getFeeTotal() {
        return feeTotal;
    }

    public void setFeeTotal(String feeTotal) {
        this.feeTotal = feeTotal;
    }

    public String getPoint() {
        return point;
    }

    public void setPoint(String point) {
        this.point = point;
    }

    public String getPayMode() {
        return payMode;
    }

    public void setPayMode(String payMode) {
        this.payMode = payMode;
    }

    public String getFeeFirst() {
        return feeFirst;
    }

    public void setFeeFirst(String feeFirst) {
        this.feeFirst = feeFirst;
    }

    public String getFeeEach() {
        return feeEach;
    }

    public void setFeeEach(String feeEach) {
        this.feeEach = feeEach;
    }

    public String getReserveMessage() {
        return reserveMessage;
    }

    public void setReserveMessage(String reserveMessage) {
        this.reserveMessage = reserveMessage;
    }

    /**
     * 组织分期付款信息，用于打印
     * @return  用于打印的字符串
     */
    public String formatInstallmentInfoForPrint() {

        StringBuilder builder = new StringBuilder();
        if(!TextUtils.isEmpty(getPeriod()))
            builder.append("分期付款期数：").append(getPeriod()).append("\n");
        if(!TextUtils.isEmpty(getPeriod()))
            builder.append("分期付款首期还款金额：").append(MoneyUtil.formatMoney(getPayAmountFirst())).append("\n");
        if(!TextUtils.isEmpty(getPeriod()))
            builder.append("分期付款还款币种：").append(getCurrencyCode()).append("\n");
        if(!TextUtils.isEmpty(getPeriod()))
            builder.append("持卡人分期付款手续费：").append(MoneyUtil.formatMoney(getFeeTotal())).append("\n");
        if(!TextUtils.isEmpty(getPeriod()))
            builder.append("分期付款奖励积分：").append(MoneyUtil.formatMoney(getPoint()));
        //分期支付手续费时，才打印下面的信息
        if (!TextUtils.isEmpty(getPayMode()) && "1".equals(getPayMode())) {
            if(!TextUtils.isEmpty(getPeriod()))
                builder.append("\n分期付款首期手续费：").append(MoneyUtil.formatMoney(getFeeFirst())).append("\n");
            if (!TextUtils.isEmpty(getPeriod()))
                builder.append("分期付款每期手续费：").append(MoneyUtil.formatMoney(getFeeEach()));
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return "InstallmentInformation{" +
                "voucherNo='" + voucherNo + '\'' +
                ", period='" + period + '\'' +
                ", payAmountFirst='" + payAmountFirst + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                ", feeTotal='" + feeTotal + '\'' +
                ", point='" + point + '\'' +
                ", payMode='" + payMode + '\'' +
                ", feeFirst='" + feeFirst + '\'' +
                ", feeEach='" + feeEach + '\'' +
                ", reserveMessage='" + reserveMessage + '\'' +
                '}';
    }
}
