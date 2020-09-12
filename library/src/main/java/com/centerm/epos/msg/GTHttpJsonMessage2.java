package com.centerm.epos.msg;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.centerm.epos.base.ReceivedQueryBean;
import com.centerm.epos.bean.GtBannerBean;
import com.centerm.epos.bean.GtBean;
import com.centerm.epos.bean.GtBean2;
import com.centerm.epos.bean.GtBusinessListBean;
import com.centerm.epos.bean.PrintReceiptBean;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.transcation.pos.constant.JsonKeyGT;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liubit on 2017/12/25.
 * 报文组织-解析类
 */

public class GTHttpJsonMessage2 extends BaseGTHttpJsonMessage implements ITransactionMessage {

    @Override
    public Object packMessage(String transTag, Map<String, Object> transData) {
        try {
            return getRequest(transTag, transData).getBytes();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String, Object> unPackMessage(String transTag, Object streamData) {
        Map<String, Object> dataForJson = new HashMap<>();
        String result = new String((byte[]) streamData);
        if(TextUtils.equals(TransCode.picQuery, transTag)){
            GtBannerBean bean = JSON.parseObject(result, GtBannerBean.class);
            dataForJson.put(JsonKeyGT.returnData, bean);
        }else if(TextUtils.equals(TransCode.isAuthorization, transTag)){
            GtBean bean = JSON.parseObject(result, GtBean.class);
            dataForJson.put(JsonKeyGT.returnData, bean);
        }else if(TextUtils.equals(TransCode.staffVerify, transTag)){
            GtBean bean = JSON.parseObject(result, GtBean.class);
            dataForJson.put(JsonKeyGT.returnData, bean);
        }else if(TextUtils.equals(TransCode.unpaidQuery, transTag)){
            GtBusinessListBean bean = JSON.parseObject(result, GtBusinessListBean.class);
            dataForJson.put(JsonKeyGT.returnData, bean);
        }else if(TextUtils.equals(TransCode.receivedQuery, transTag)){
            ReceivedQueryBean bean = JSON.parseObject(result, ReceivedQueryBean.class);
            dataForJson.put(JsonKeyGT.returnData, bean);
        }else if(TransCode.orderSync.equals(transTag)){
            GtBean2 bean = JSON.parseObject(result, GtBean2.class);
            dataForJson.put(JsonKeyGT.returnData, bean);
        }else if(TransCode.ticketUpload.equals(transTag)){
            GtBean2 bean = JSON.parseObject(result, GtBean2.class);
            dataForJson.put(JsonKeyGT.returnData, bean);
        }else if(TransCode.repeatPrintReceipt.equals(transTag)){
            PrintReceiptBean bean = JSON.parseObject(result, PrintReceiptBean.class);
            dataForJson.put(JsonKeyGT.returnData, bean);
        }else if(TransCode.generalReceipts.equals(transTag)){
            GtBean2 bean = JSON.parseObject(result, GtBean2.class);
            dataForJson.put(JsonKeyGT.returnData, bean);
        }else {
            GtBannerBean bean = JSON.parseObject(result, GtBannerBean.class);
            dataForJson.put(JsonKeyGT.returnData, bean);
        }
        return dataForJson;
    }

}
