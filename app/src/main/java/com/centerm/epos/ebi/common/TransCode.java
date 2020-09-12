package com.centerm.epos.ebi.common;

import com.centerm.epos.common.ITransCode;
import com.centerm.epos.ebi.R;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by liubit on 2017/12/25.
 */

public class TransCode implements ITransCode {
    public static final String SALE_SCAN = "SALE_SCAN";//扫码支付
    public static final String SALE_SCAN_QUERY = "SALE_SCAN_QUERY";//查询支付状态
    public static final String SALE_SCAN_VOID = "SALE_SCAN_VOID";//订单撤销
    public static final String SALE_SCAN_VOID_QUERY = "SALE_SCAN_VOID_QUERY";//订单撤销查询
    public static final String SALE_SCAN_REFUND = "SALE_SCAN_REFUND";//订单退货
    public static final String SALE_SCAN_REFUND_QUERY = "SALE_SCAN_REFUND_QUERY";//查询退款状态
    public static final String ORDER_QUERY = "ORDER_QUERY";//订单查询
    public static final String DOWNLOAD_MAIN_KEY = "DOWNLOAD_MAIN_KEY";//下载签名密钥
    public static final String SALE_PROPERTY = "SALE_PROPERTY";//物业扫码下单
    public static final String PROPERTY_NOTICE = "PROPERTY_NOTICE";//物业扫码下单支付结果通知

    public static final String SALE_SCAN_CODE = "P00";//扫码支付
    public static final String SALE_SCAN_UNION_CODE = "CSU01";//扫码支付-银联 CSU01
    public static final String SALE_SCAN_QUERY_CODE = "PF0";//查询支付状态-银联
    public static final String SALE_SCAN_QUERY_UNION_CODE = "QRY1";//查询支付状态 QRY1
    public static final String SALE_SCAN_VOID_CODE = "P01";//订单撤销
    public static final String SALE_SCAN_VOID_UNION_CODE = "CSU02";//订单撤销-银联 CSU02
    public static final String SALE_SCAN_VOID_QUERY_CODE = "PF1";//订单撤销查询
    public static final String SALE_SCAN_VOID_QUERY_UNION_CODE = "QRY2";//订单撤销查询-银联 QRY2
    public static final String SALE_SCAN_REFUND_CODE = "P02";//订单退货
    public static final String SALE_SCAN_REFUND_UNION_CODE = "CSU03";//订单退货 CSU03
    public static final String SALE_SCAN_REFUND_QUERY_CODE = "PF2";//查询退款状态
    public static final String SALE_SCAN_REFUND_QUERY_UNION_CODE = "QRY3";//查询退款状态-银联 QRY3
    public static final String SALE_PROPERTY_CODE = "GY01";//物业扫码下单 GY01
    public static final String SALE_PROPERTY_NOTICE_CODE = "GY02";//物业扫码下单结果通知 GY01


    @Override
    public Set<String> registerPrintICInfoTrade() {
        return null;
    }

    @Override
    public Set<String> removePrintICInfoTrade() {
        return null;
    }

    @Override
    public Set<String> registerReverseTrade() {
        return null;
    }

    @Override
    public Set<String> removeReverseTrade() {
        return null;
    }

    @Override
    public Set<String> registerManagerTrade() {
        return null;
    }

    @Override
    public Set<String> removeManagerTrade() {
        return null;
    }

    @Override
    public Set<String> registerTradeForRecord() {
        Set<String> set = new HashSet<>();
        set.add(SALE_SCAN);
        set.add(SALE_SCAN_QUERY);
        set.add(SALE_SCAN_VOID);
        set.add(SALE_SCAN_VOID_QUERY);
        set.add(SALE_SCAN_REFUND);
        set.add(SALE_SCAN_REFUND_QUERY);
        return set;
    }

    @Override
    public Set<String> removeTradeForRecord() {
        return null;
    }

    @Override
    public Set<String> registerTradeForDebit() {
        return null;
    }

    @Override
    public Set<String> removeTradeForDebit() {
        return null;
    }

    @Override
    public Set<String> registerTradeForCredit() {
        return null;
    }

    @Override
    public Set<String> removeTradeForCredit() {
        return null;
    }

    @Override
    public Set<String> registerTradeDiscardAutoSign() {
        Set<String> set = new HashSet<>();
        set.add(DOWNLOAD_MAIN_KEY);
        return set;
    }

    @Override
    public Map<String, Integer> registerTradeName() {
        Map<String,Integer> tradeNameMap = new HashMap<>();
        tradeNameMap.put(SALE_SCAN_QUERY, R.string.sale_scan_query);
        tradeNameMap.put(SALE_SCAN_VOID, R.string.sale_scan_void);
        tradeNameMap.put(SALE_SCAN_VOID_QUERY, R.string.sale_scan_void);
        tradeNameMap.put(SALE_SCAN_REFUND,R.string.sale_scan_refund);
        tradeNameMap.put(SALE_SCAN_REFUND_QUERY,R.string.sale_scan_refund);
        tradeNameMap.put(ORDER_QUERY,R.string.order_query);
        tradeNameMap.put(DOWNLOAD_MAIN_KEY,R.string.tip_download_mainkey);
        tradeNameMap.put(com.centerm.epos.common.TransCode.OBTAIN_TMK, R.string.tip_download_pos_mainkey);
        tradeNameMap.put(com.centerm.epos.common.TransCode.DOWNLOAD_TERMINAL_PARAMETER, R.string.label_tms_download_pos);
        tradeNameMap.put(com.centerm.epos.common.TransCode.E_COMMON, R.string.label_e_common);
        tradeNameMap.put(com.centerm.epos.common.TransCode.E_REFUND, R.string.label_e_refund);
        tradeNameMap.put(SALE_PROPERTY, R.string.label_sale_property);
        return tradeNameMap;
    }

    @Override
    public Map<String, Integer> registerTradeNameEn() {
        return null;
    }

    @Override
    public Set<String> registerTradeForFullPboc() {
        return null;
    }

    @Override
    public Set<String> removeTradeForFullPboc() {
        return null;
    }

    public static boolean checkTradeState(String f39){
        boolean state = "00".equals(f39) || "0".equals(f39) || "11".equals(f39) || "A2".equals(f39)
                || "A4".equals(f39) || "A5".equals(f39) || "A6".equals(f39);
        return state;
    }
}
