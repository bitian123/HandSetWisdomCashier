package com.centerm.epos.msg;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.centerm.epos.base.ReceivedQueryBean;
import com.centerm.epos.bean.GtBannerBean;
import com.centerm.epos.bean.GtBean;
import com.centerm.epos.bean.GtBean2;
import com.centerm.epos.bean.GtBusinessListBean;
import com.centerm.epos.bean.PrintReceiptBean;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.transcation.pos.constant.JsonKeyGT;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liubit on 2017/12/25.
 * 报文组织-解析类
 */

public class GTHttpJsonMessage extends BaseGTHttpJsonMessage implements ITransactionMessage {
    protected Gson gson = new Gson();

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
            GtBannerBean bean = gson.fromJson(result, new TypeToken<GtBannerBean>() {}.getType());
            dataForJson.put(JsonKeyGT.returnData, bean);
        }else if(TextUtils.equals(TransCode.isAuthorization, transTag)){
            GtBean bean = gson.fromJson(result, new TypeToken<GtBean>() {}.getType());
            dataForJson.put(JsonKeyGT.returnData, bean);
        }else if(TextUtils.equals(TransCode.staffVerify, transTag)){
            GtBean bean = gson.fromJson(result, new TypeToken<GtBean>() {}.getType());
            dataForJson.put(JsonKeyGT.returnData, bean);
        }else if(TextUtils.equals(TransCode.unpaidQuery, transTag)){
            GtBusinessListBean bean = gson.fromJson(result, new TypeToken<GtBusinessListBean>() {}.getType());
            dataForJson.put(JsonKeyGT.returnData, bean);
        }else if(TextUtils.equals(TransCode.receivedQuery, transTag)){
            //ReceivedQueryBean bean = gson.fromJson(result, new TypeToken<ReceivedQueryBean>() {}.getType());
            ReceivedQueryBean bean = new ReceivedQueryBean();
            try {
                JSONObject json = new JSONObject(result);
                bean.setRespCode(json.optString("respCode"));
                bean.setRespMsg(json.optString("respMsg"));
                bean.setCompanyId(json.optString("companyId"));
                bean.setIdType(json.optInt("idType"));
                bean.setProjectId(json.optString("projectId"));
                bean.setProjectName(json.optString("projectName"));
                JSONArray array = json.optJSONArray("queryLists");
                if(array!=null&&array.length()>0){
                    List<ReceivedQueryBean.QueryListsBean> lists = new ArrayList<>();
                    for(int i=0;i<array.length();i++){
                        JSONObject object = array.optJSONObject(i);
                        ReceivedQueryBean.QueryListsBean queryListsBean = new ReceivedQueryBean.QueryListsBean();
                        queryListsBean.setAmountReceivable(object.optDouble("amountReceivable"));
                        queryListsBean.setAmountReceived(object.optDouble("amountReceived"));
                        queryListsBean.setUnpaidAmount(object.optDouble("unpaidAmount"));
                        queryListsBean.setBusinessId(object.optString("businessId"));
                        queryListsBean.setBusinessType(object.optString("businessType"));
                        queryListsBean.setMainOrderId(object.optString("mainOrderId"));
                        queryListsBean.setMoneyType(object.optInt("moneyType"));
                        queryListsBean.setPayMethod(object.optString("payMethod"));
                        queryListsBean.setPaymentItemName(object.optString("paymentItemName"));
                        queryListsBean.setPaymentPlanId(object.optString("paymentPlanId"));
                        queryListsBean.setReceivableDate(object.optString("receivableDate"));
                        queryListsBean.setRoomFullName(object.optString("roomFullName"));
                        queryListsBean.setRoomId(object.optString("roomId"));
                        queryListsBean.setSubjectName(object.optString("subjectName"));
                        queryListsBean.setBillCode(object.optString("billCode"));
                        queryListsBean.setPrintTime(object.optString("printTime"));
                        queryListsBean.setPayDate(object.optString("payDate"));
                        queryListsBean.setChecked(false);
                        JSONArray customArray = object.optJSONArray("customList");
                        List<ReceivedQueryBean.QueryListsBean.CustomListBean> customLists = new ArrayList<>();
                        for(int j=0;j<customArray.length();j++){
                            JSONObject custom = customArray.optJSONObject(j);
                            ReceivedQueryBean.QueryListsBean.CustomListBean customListBean = new ReceivedQueryBean.QueryListsBean.CustomListBean();
                            customListBean.setIdNo(custom.optString("idNo"));
                            customListBean.setIdType(custom.optInt("idType"));
                            customListBean.setName(custom.optString("name"));
                            customListBean.setSubOrderId(custom.optString("subOrderId"));
                            customLists.add(customListBean);
                        }
                        queryListsBean.setCustomList(customLists);
                        lists.add(queryListsBean);
                    }
                    bean.setQueryLists(lists);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            dataForJson.put(JsonKeyGT.returnData, bean);
        }else if(TransCode.orderSync.equals(transTag)){
            GtBean2 bean = gson.fromJson(result, new TypeToken<GtBean2>() {}.getType());
            dataForJson.put(JsonKeyGT.returnData, bean);
        }else if(TransCode.ticketUpload.equals(transTag)){
            GtBean2 bean = gson.fromJson(result, new TypeToken<GtBean2>() {}.getType());
            dataForJson.put(JsonKeyGT.returnData, bean);
        }else if(TransCode.repeatPrintReceipt.equals(transTag)){
            PrintReceiptBean bean = gson.fromJson(result, new TypeToken<PrintReceiptBean>() {}.getType());
            dataForJson.put(JsonKeyGT.returnData, bean);
        }else if(TransCode.generalReceipts.equals(transTag)){
            GtBean2 bean = gson.fromJson(result, new TypeToken<GtBean2>() {}.getType());
            dataForJson.put(JsonKeyGT.returnData, bean);
        }else {
            GtBannerBean bean = gson.fromJson(result, new TypeToken<GtBannerBean>() {}.getType());
            dataForJson.put(JsonKeyGT.returnData, bean);
        }
        return dataForJson;
    }

}