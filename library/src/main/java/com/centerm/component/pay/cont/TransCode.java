package com.centerm.component.pay.cont;

import java.util.HashSet;
import java.util.Set;

/**
 * author:wanliang527</br>
 * date:2017/3/8</br>
 */

public final class TransCode {

    public final String SALE = "T00001";//消费
    public final String VOID = "T00002";//消费撤销
    public final String REFUND = "T00003";//退货
    public final String BALANCE = "T00004";//余额查询
    public final String AUTH = "T00005";//预授权
    public final String CANCEL = "T00006";//预授权撤销
    public final String AUTH_COMPLETE = "T00007";//预授权完成
    public final String AUTH_COMPLETE_CANCEL = "T00008";//预授权完成撤销

    public final String SALE_SCAN = "T00009";//银联扫码付

    public final String WX_PAY = "CW001";//微信支付（含主扫和被扫）
    public final String WX_PAY_SCAN = "CW002";//微信主扫
    public final String WX_PAY_SCANED = "CW003";//微信被扫
    public final String WX_REFUND = "CW004";//微信退款

    public final String ALI_PAY = "CA001";//支付宝支付（含主扫和被扫）
    public final String ALI_PAY_SCAN = "CA002";//支付宝主扫
    public final String ALI_PAY_SCANED = "CA003";//支付宝被扫
    public final String ALI_REFUND = "CA004";//支付宝退款

    public final String COMMON_SCAN = "CC001";//通用主扫
    public final String COMMON_SCANED = "CC002";//一码付

    public final String SIGN_IN = "M00001"; //签到
    public final String SETTLE = "M00002";  //结算

    public final String REPRINT_SLIP = "F00001";    //重打任意一笔
    public final String REPRINT_LAST_SLIP = "F00002";    //重打最后一笔
    public final String ADMIN_SETTINGS = "F00003";//99管理员

    private Set<String> sets;

    private TransCode() {
        sets = new HashSet<>();
        sets.add(SALE);
        sets.add(VOID);
        sets.add(BALANCE);
        sets.add(REFUND);
        sets.add(AUTH);
        sets.add(CANCEL);
        sets.add(AUTH_COMPLETE);
        sets.add(AUTH_COMPLETE_CANCEL);
        sets.add(SALE_SCAN);
        sets.add(WX_PAY);
        sets.add(WX_PAY_SCAN);
        sets.add(WX_PAY_SCANED);
        sets.add(WX_REFUND);
        sets.add(ALI_PAY);
        sets.add(ALI_PAY_SCAN);
        sets.add(ALI_PAY_SCANED);
        sets.add(ALI_REFUND);
        sets.add(COMMON_SCAN);
        sets.add(COMMON_SCANED);

        sets.add(SIGN_IN);
        sets.add(SETTLE);
        sets.add(REPRINT_SLIP);
        sets.add(REPRINT_LAST_SLIP);
    }

    public boolean exist(String code){
        return sets.contains(code);
    }

    public static TransCode obj() {
        return new TransCode();
    }

}
