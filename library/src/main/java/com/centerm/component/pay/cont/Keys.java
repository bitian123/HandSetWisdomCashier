package com.centerm.component.pay.cont;

/**
 * author:wanliang527</br>
 * date:2017/3/8</br>
 */

public final class Keys {

    public final String resp_code = "resp_code";
    public final String resp_msg = "resp_msg";
    public final String trans_code = "trans_code";
    public final String trans_time = "trans_time";
    public final String trans_amt = "trans_amt";
    public final String caller_id = "caller_id";
    public final String caller_secret = "caller_secret";
    public final String control_bundle = "control_bundle";
    public final String control_info = "control_info";
    public final String input_money_view = "input_money_view";
    public final String signature_view = "signature_view";
    public final String result_view = "result_view";
    public final String print_pages = "print_pages";
    public final String order_no = "order_no";
    public final String plat_trans_no = "plat_trans_ no";
    public final String retri_ref_no = "retri_ref_no";

    private Keys() {
    }

    public static Keys obj() {
        return new Keys();
    }

}
