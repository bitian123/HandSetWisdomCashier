package com.centerm.epos.ebi.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by liubit on 2017/12/27.
 */

public enum ScanRefundResult {
    //S：退款成功  R：处理中 F：退款失败

    S("S", "成功"),
    R("R", "处理中"),
    F("F", "失败"),
    N("N", "订单不存在");

    private String code;
    private String des;

    ScanRefundResult(String code, String des){
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

    public static List<ScanRefundResult> listPayResult(){
        ScanRefundResult[] enums = values();
        List<ScanRefundResult> list = new ArrayList<>();
        Collections.addAll(list, enums);
        return list;
    }


}
