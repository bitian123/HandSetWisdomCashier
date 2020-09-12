package com.centerm.component.pay.cont;

/**
 * Created by yuhc on 2017/4/22.
 */

public interface JPayComponent {
    String WE_CHAT_PAY = "wechat";
    String ALIPAY_PAY = "alipay";

    String trans_tp = "trans_tp";
    String trans_code = "trans_code";
    String trans_amt = "trans_amt";
    String caller_id = "caller_id";
    String caller_secret = "caller_secret";
    String goods_abs = "goods_abs";
    String goods_detail = "goods_detail";
    String goods_tag = "goods_tag";
    String attach = "attach";
    String control_info = "control_info";
    String print_pages = "print_pages";

    String discountable_amount = "discountable_amount";
    String undiscountable_amount = "undiscountable_amount";

    String resp_code = "resp_code";
    String resp_msg = "resp_msg";
    String RESULT_OK = "00";

    String ORDER_NO = "order_no";
    String TRANS_TIME = "trans_time";
}
