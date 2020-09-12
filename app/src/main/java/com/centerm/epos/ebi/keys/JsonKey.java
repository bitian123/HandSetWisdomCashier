package com.centerm.epos.ebi.keys;

/**
 * Created by liubit on 2017/12/25.
 */

public class JsonKey {

    public static final String head = "head";
    public static final String msgType = "msgType";//交易类型:消费、撤销、退货、查询 的消息类型统一上送0200;签名主密钥获取接口是 0800
    public static final String termidm = "termidm";//机具编号
    public static final String mercode = "mercode";//电银内部商户号
    public static final String termcde = "termcde";//电银内部终端号
    public static final String imei = "imei";
    public static final String sendTime = "sendTime";//YYYYMMDDHHMMSS
    public static final String stationInfo = "stationInfo";//基站信息 格式:MCC|MNC|LAC|CID

    public static final String sign = "sign";

    public static final String request = "request";
    public static final String response = "response";
    public static final String returnData = "returnData";
    public static final String body = "body";
    public static final String merc_id = "merc_id";
    public static final String notice_url = "notice_url";
    public static final String channel = "channel";
    public static final String order_sign = "order_sign";
    public static final String tm_smp = "tm_smp";
    public static final String pay_time = "pay_time";
    public static final String serial_num = "serial_num";

    public static final String mer_order_no = "mer_order_no";//商户订单号
    public static final String out_order_no = "out_order_no";//外部订单号
    public static final String trancde = "trancde";//交易码 非银联:P00 银联:CSU01
    public static final String bar_code = "bar_code";//用户用支付宝、微信生成的付款码
    public static final String pay_amount = "pay_amount";//订单总金额 单位：分
    public static final String undiscountable_amount = "undiscountable_amount";//不参与优惠金额
    public static final String order_name = "order_name";//订单名称
    public static final String pay_type = "pay_type";//支付方式 01:微信 02:支付宝 03:银联
    public static final String isOrderQueryAct = "isOrderQueryAct";//订单查询标志
    public static final String isTradeDetail = "isTradeDetail";//交易详情标志

    public static final String order_desc = "order_desc";//订单描述
    public static final String goods_detail = "goods_detail";//商品明细
    public static final String goods_id = "goods_id";//商品编码
    public static final String goods_name = "goods_name";//商品名称
    public static final String quantity = "quantity";//商品重量
    public static final String price = "price";//商品单价

    public static final String refund_amount = "refund_amount";//退款金额
    public static final String mer_refund_order_no = "mer_refund_order_no";//商户退款单号
    public static final String refund_remark = "refund_remark";//退款备注
    public static final String pay_no = "pay_no";//电银流水号
    public static final String pay_result = "pay_result";//支付结果 I:待支付 S:成功 R:正在执行 F:失败 O:交易关闭
    public static final String pay_source = "pay_source";//物业使用，定值：dy
    public static final String result_desc = "result_desc";//支付描述
    public static final String actual_pay_amount = "actual_pay_amount";//买家实付金额
    public static final String receipt_amount = "receipt_amount";//卖家实收金额
    public static final String revoke_result = "revoke_result";//撤销结果
    public static final String refund_result = "refund_result";//退款结果

    public static final String MAK = "MAK";//主密钥key
    public final static String MENU_TAG = "MENU_TAG";
    public final static String QUERY_FLAG = "QUERY_FLAG";//查询标志-区分是交易完成时的查询还是交易详情中的查询
    public final static String TRANS_RESULT_FLAG = "TRANS_RESULT_FLAG";//交易结果标志，用来保存交易结果

    public final static String ORI_BATCH_NO = "ORI_BATCH_NO";//交易结果标志，用来保存交易结果
    public final static String PROPERTY_FLAG = "PROPERTY_FLAG";//物业扫码下单标志


}
