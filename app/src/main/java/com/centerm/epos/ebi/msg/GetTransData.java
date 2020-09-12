package com.centerm.epos.ebi.msg;

import android.util.Log;

import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.ebi.common.TransCode;
import com.centerm.epos.ebi.keys.JsonKey;
import com.centerm.epos.ebi.utils.FormatUtils;
import com.centerm.epos.redevelop.ITradeRecordInformation;
import com.centerm.epos.redevelop.TradeRecordInfoImpl;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by liubit on 2017/12/26.
 * 交易组包
 */

public class GetTransData {

    /**
     * 扫码支付
     * */
    public static JSONObject getData_SALE_SCAN(Map<String, Object> transData){
        JSONObject bodyJson = new JSONObject();
        try {
            bodyJson.put(JsonKey.mer_order_no, transData.get(JsonKey.mer_order_no));
            bodyJson.put(JsonKey.bar_code, transData.get(TradeInformationTag.SCAN_CODE));
            bodyJson.put(JsonKey.pay_amount, FormatUtils.formatAmount3((String) transData.get(TradeInformationTag.TRANS_MONEY)));
            bodyJson.put(JsonKey.pay_type, transData.get(JsonKey.pay_type));
            if("03".equals(transData.get(JsonKey.pay_type))){//银联方式
                bodyJson.put(JsonKey.trancde, TransCode.SALE_SCAN_UNION_CODE);
            }else {//微信，支付宝方式
                bodyJson.put(JsonKey.trancde, TransCode.SALE_SCAN_CODE);
            }
            if(transData.get(JsonKey.out_order_no)!=null){
                bodyJson.put(JsonKey.out_order_no, transData.get(JsonKey.out_order_no));
            }
            bodyJson.put(JsonKey.order_name, JsonKey.order_name);
            //bodyJson.put(JsonKey.undiscountable_amount, transData.get(TradeInformationTag.TRANS_MONEY));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return bodyJson;
    }

    /**
     * 扫码支付查询
     * */
    public static JSONObject getData_SALE_SCAN_QUERY(Map<String, Object> transData){
        JSONObject bodyJson = new JSONObject();
        try {
            bodyJson.put(JsonKey.mer_order_no, transData.get(JsonKey.mer_order_no));
            bodyJson.put(JsonKey.pay_no, transData.get(JsonKey.pay_no));
            if("03".equals(transData.get(JsonKey.pay_type))){//银联方式
                bodyJson.put(JsonKey.trancde, TransCode.SALE_SCAN_QUERY_UNION_CODE);
            }else {//微信，支付宝方式
                bodyJson.put(JsonKey.trancde, TransCode.SALE_SCAN_QUERY_CODE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return bodyJson;
    }

    /**
     * 订单撤销
     * */
    public static JSONObject getData_SALE_SCAN_VOID(Map<String, Object> transData){
        JSONObject bodyJson = new JSONObject();
        try {
            bodyJson.put(JsonKey.mer_order_no, transData.get(TradeInformationTag.SCAN_VOUCHER_NO));
            if("03".equals(transData.get(TradeInformationTag.UNICOM_SCAN_TYPE))){//银联方式
                bodyJson.put(JsonKey.trancde, TransCode.SALE_SCAN_VOID_UNION_CODE);
            }else {//微信，支付宝方式
                bodyJson.put(JsonKey.trancde, TransCode.SALE_SCAN_VOID_CODE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return bodyJson;
    }

    /**
     * 订单撤销查询
     * */
    public static JSONObject getData_SALE_SCAN_VOID_QUERY(Map<String, Object> transData){
        JSONObject bodyJson = new JSONObject();
        try {
            bodyJson.put(JsonKey.mer_order_no, transData.get(JsonKey.mer_order_no));
            if("03".equals(transData.get(TradeInformationTag.UNICOM_SCAN_TYPE))){//银联方式
                bodyJson.put(JsonKey.trancde, TransCode.SALE_SCAN_VOID_QUERY_UNION_CODE);
            }else {//微信，支付宝方式
                bodyJson.put(JsonKey.trancde, TransCode.SALE_SCAN_VOID_QUERY_CODE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return bodyJson;
    }

    /**
     * 订单退货
     * */
    public static JSONObject getData_SALE_SCAN_REFUND(Map<String, Object> transData){
        JSONObject bodyJson = new JSONObject();
        try {
            bodyJson.put(JsonKey.mer_order_no, transData.get(JsonKey.mer_refund_order_no));//原订单号
            bodyJson.put(JsonKey.mer_refund_order_no, transData.get(JsonKey.mer_order_no));
            if("03".equals(transData.get(JsonKey.pay_type))){
                bodyJson.put(JsonKey.trancde, TransCode.SALE_SCAN_REFUND_UNION_CODE);//非银联时:P02 银联:CSU03
            }else {
                bodyJson.put(JsonKey.trancde, TransCode.SALE_SCAN_REFUND_CODE);//非银联时:P02 银联:CSU03
            }
            bodyJson.put(JsonKey.refund_amount, FormatUtils.formatAmount3((String) transData.get(TradeInformationTag.TRANS_MONEY)));
            bodyJson.put(JsonKey.refund_remark, "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return bodyJson;
    }

    /**
     * 订单退货查询
     * */
    public static JSONObject getData_SALE_SCAN_REFUND_QUERY(Map<String, Object> transData){
        JSONObject bodyJson = new JSONObject();
        try {
            bodyJson.put(JsonKey.mer_order_no, transData.get(JsonKey.mer_order_no));//原订单号
            bodyJson.put(JsonKey.mer_refund_order_no, transData.get(JsonKey.mer_refund_order_no));//退货的订单号
            if("03".equals(transData.get(JsonKey.pay_type))){
                bodyJson.put(JsonKey.trancde, TransCode.SALE_SCAN_REFUND_QUERY_UNION_CODE);
            }else {
                bodyJson.put(JsonKey.trancde, TransCode.SALE_SCAN_REFUND_QUERY_CODE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return bodyJson;
    }

    /**
     * 4.2.	下单
     * */
    public static JSONObject getData_SALE_PROPERTY(Map<String, Object> transData){
        JSONObject bodyJson = new JSONObject();
        try {
            bodyJson.put(JsonKey.mer_order_no, transData.get(JsonKey.mer_order_no));
            bodyJson.put(JsonKey.trancde, TransCode.SALE_PROPERTY_CODE);
            bodyJson.put(JsonKey.merc_id, GetRequestData.getMercode());
            bodyJson.put(JsonKey.pay_amount, transData.get(JsonKey.pay_amount));
            bodyJson.put(JsonKey.notice_url, transData.get(JsonKey.notice_url));
            bodyJson.put(JsonKey.channel, transData.get(JsonKey.channel));
            bodyJson.put(JsonKey.order_sign, transData.get(JsonKey.order_sign));
            bodyJson.put(JsonKey.tm_smp, transData.get(JsonKey.tm_smp));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return bodyJson;
    }

    /**
     * 4.3.	支付结果通知
     * */
    public static JSONObject getData_PROPERTY_NOTICE(Map<String, Object> transData){
        JSONObject bodyJson = new JSONObject();
        try {
            bodyJson.put(JsonKey.mer_order_no, transData.get(JsonKey.out_order_no));
            bodyJson.put(JsonKey.trancde, TransCode.SALE_PROPERTY_NOTICE_CODE);
            bodyJson.put(JsonKey.merc_id, GetRequestData.getMercode());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return bodyJson;
    }

    public static void clearAllData(){
        //清空流水
        boolean isDel;
        ITradeRecordInformation tradeRecordInformation = (ITradeRecordInformation) ConfigureManager
                .getSubPrjClassInstance(new TradeRecordInfoImpl());
        isDel = tradeRecordInformation.clearRecord();
        if (isDel) {
            Log.e("===", "该批次数据清空完成！");
        } else {
            Log.e("===", "该批次数据清空失败！");
        }
    }
}
