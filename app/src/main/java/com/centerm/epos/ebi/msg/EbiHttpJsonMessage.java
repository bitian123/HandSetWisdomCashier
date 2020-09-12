package com.centerm.epos.ebi.msg;

import android.text.TextUtils;

import com.centerm.epos.bean.WyBean;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.ebi.bean.LoadMakBean;
import com.centerm.epos.ebi.bean.PropertyBean;
import com.centerm.epos.ebi.bean.SaleScanResult;
import com.centerm.epos.ebi.bean.ScanRefundBean;
import com.centerm.epos.ebi.bean.ScanVoidBean;
import com.centerm.epos.ebi.keys.JsonKey;
import com.centerm.epos.msg.ITransactionMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import static com.centerm.epos.ebi.common.TransCode.DOWNLOAD_MAIN_KEY;
import static com.centerm.epos.ebi.common.TransCode.PROPERTY_NOTICE;
import static com.centerm.epos.ebi.common.TransCode.SALE_PROPERTY;
import static com.centerm.epos.ebi.common.TransCode.SALE_SCAN_QUERY;
import static com.centerm.epos.ebi.common.TransCode.SALE_SCAN_REFUND;
import static com.centerm.epos.ebi.common.TransCode.SALE_SCAN_REFUND_QUERY;
import static com.centerm.epos.ebi.common.TransCode.SALE_SCAN_VOID;
import static com.centerm.epos.ebi.common.TransCode.SALE_SCAN_VOID_QUERY;

/**
 * Created by liubit on 2017/12/25.
 * 报文组织-解析类
 */

public class EbiHttpJsonMessage extends BaseHttpJsonMessage implements ITransactionMessage {

    @Override
    public Object packMessage(String transTag, Map<String, Object> transData) {
        try {
            if(com.centerm.epos.ebi.common.TransCode.PROPERTY_NOTICE.equals(transTag)){
                //发送到物业平台的请求
                //return getTransRequestWY(transData).getBytes();
                return getRequest(transTag, transData).getBytes();
            }else {
                return getRequest(transTag, transData).getBytes();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String, Object> unPackMessage(String transTag, Object streamData) {
        Map<String, Object> dataForJson = new HashMap<>();
        String result = new String((byte[]) streamData);
        if(TextUtils.equals(DOWNLOAD_MAIN_KEY, transTag)){//签到
            LoadMakBean bean = gson.fromJson(result, new TypeToken<LoadMakBean>() {}.getType());
            dataForJson.put(JsonKey.returnData, bean);
        }else if(TextUtils.equals(TransCode.SALE_SCAN, transTag)){//扫码支付
            SaleScanResult bean = gson.fromJson(result, new TypeToken<SaleScanResult>() {}.getType());
            dataForJson.put(JsonKey.returnData, bean);
        }else if(TextUtils.equals(SALE_SCAN_QUERY, transTag)){//扫码支付查询
            SaleScanResult bean = gson.fromJson(result, new TypeToken<SaleScanResult>() {}.getType());
            dataForJson.put(JsonKey.returnData, bean);
        }else if(TextUtils.equals(SALE_SCAN_VOID, transTag)){//订单撤销
            ScanVoidBean bean = gson.fromJson(result, new TypeToken<ScanVoidBean>() {}.getType());
            dataForJson.put(JsonKey.returnData, bean);
        }else if(TextUtils.equals(SALE_SCAN_VOID_QUERY, transTag)){//订单撤销查询
            ScanVoidBean bean = gson.fromJson(result, new TypeToken<ScanVoidBean>() {}.getType());
            dataForJson.put(JsonKey.returnData, bean);
        }else if(TextUtils.equals(SALE_SCAN_REFUND, transTag)){//退货
            ScanRefundBean bean = gson.fromJson(result, new TypeToken<ScanRefundBean>() {}.getType());
            dataForJson.put(JsonKey.returnData, bean);
        }else if(TextUtils.equals(SALE_SCAN_REFUND_QUERY, transTag)){//退货查询
            ScanRefundBean bean = gson.fromJson(result, new TypeToken<ScanRefundBean>() {}.getType());
            dataForJson.put(JsonKey.returnData, bean);
        }else if(TextUtils.equals(SALE_PROPERTY, transTag)){//物业下单
            PropertyBean bean = gson.fromJson(result, new TypeToken<PropertyBean>() {}.getType());
            dataForJson.put(JsonKey.returnData, bean);
        } else if(TextUtils.equals(PROPERTY_NOTICE, transTag)){//物业下单
            //WyBean bean = gson.fromJson(result, new TypeToken<WyBean>() {}.getType());
            SaleScanResult bean = gson.fromJson(result, new TypeToken<SaleScanResult>() {}.getType());
            dataForJson.put(JsonKey.returnData, bean);
        }
        return dataForJson;
    }

}
