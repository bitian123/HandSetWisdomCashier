package com.centerm.epos.ebi.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by liubit on 2017/12/27.
 */

public enum PayResult {
    //I:待支付 S:成功 R:正在执行 F:失败 O:交易关闭

    I("I", "待支付"),
    S("S", "交易成功"),
    R("R", "处理中"),
    F("F", "交易失败"),
    O("O", "交易关闭"),
    N("N", "订单不存在"),
    PA("PA", "该订单未支付成功"),
    PB("PB", "订单已失败"),
    PC("PC", "交易总金额与原支付金额不一致"),
    PD("PD", "支付渠道签名验证失败"),
    PE("PE", "该订单已发生退款，不能撤销"),
    PH("PH", "该商户未配置支付渠道"),
    PI("PI", "该笔订单不存在撤销交易"),
    PF("PF", "系统错误"),
    P3("P3", "订单已关闭"),
    P4("P4", "余额不足"),
    P5("P5", "订单不存在"),
    P6("P6", "退款金额大于支付金额"),
    P7("P7", "支付渠道通讯异常"),
    P8("P8", "订单号不能重复"),
    P9("P9", "退款单号不能重复");

    private String code;
    private String des;

    PayResult(String code, String des){
        this.code = code;
        this.des = des;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public static List<PayResult> listPayResult(){
        PayResult[] enums = values();
        List<PayResult> list = new ArrayList<>();
        Collections.addAll(list, enums);
        return list;
    }


}
