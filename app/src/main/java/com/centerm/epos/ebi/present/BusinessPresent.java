package com.centerm.epos.ebi.present;

import android.text.TextUtils;

import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.bean.GtBusinessListBean;
import com.centerm.epos.transcation.pos.constant.JsonKeyGT;
import com.centerm.epos.utils.DataHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * author:liubit</br>
 * date:2019/9/2</br>
 */
public class BusinessPresent extends BaseTradePresent {

    public BusinessPresent(ITradeView mTradeView) {
        super(mTradeView);
    }

    public String getAdditionalPrintData(GtBusinessListBean data){
        String result = "";
        if(data.getMoneyDetailList()!=null&&data.getMoneyDetailList().size()>=0){
            try {
                JSONObject json = new JSONObject();
                json.put("projectName", data.getProjectName());
                json.put("idNo", transDatas.get(JsonKeyGT.idNo));
                json.put("name", transDatas.get(JsonKeyGT.name));
                JSONArray array = new JSONArray();
                for(GtBusinessListBean.MoneyDetailListBean bean : data.getMoneyDetailList()){
                    if(bean.isChecked()){
                        JSONObject item = new JSONObject();
                        item.put("billId", bean.getRoomFullName());
                        Double amt = Double.valueOf(bean.getReadyPayAmt());
                        if(amt!=null&&amt>0){
                            item.put("amt", DataHelper.saved2Decimal(bean.getReadyPayAmt())+"元");
                        }else {
                            item.put("amt", DataHelper.saved2Decimal(bean.getUnpaidAmount())+"元");
                        }
                        array.put(item);
                    }
                }
                json.put("array", array);
                result = json.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public boolean checkMsg(GtBusinessListBean.MoneyDetailListBean item, GtBusinessListBean.MoneyDetailListBean checkedItem){
        boolean result = false;
        if(checkedItem==null||item==null){
            return result;
        }
        try {
            boolean subjectName = TextUtils.equals(item.getSubjectName(), checkedItem.getSubjectName());
            if(subjectName){
                List<String> customList = new ArrayList<>();
                for(GtBusinessListBean.MoneyDetailListBean.CustomListBean customListBean:item.getCustomList()){
                    customList.add(customListBean.getName());
                }
                List<String> checkedList = new ArrayList<>();
                for(GtBusinessListBean.MoneyDetailListBean.CustomListBean checkedistBean:checkedItem.getCustomList()){
                    checkedList.add(checkedistBean.getName());
                }
                if(customList.size()==checkedList.size()){
                    Collections.sort(customList);
                    Collections.sort(checkedList);
                    if(customList.equals(checkedList)){
                        result = true;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public boolean isEmpty(String str){
        boolean isEmpty = false;
        if(TextUtils.isEmpty(str)||"null".equals(str)){
            isEmpty = true;
        }
        return isEmpty;
    }

}
