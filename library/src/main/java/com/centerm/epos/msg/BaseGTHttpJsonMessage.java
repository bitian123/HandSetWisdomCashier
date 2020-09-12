package com.centerm.epos.msg;

import android.util.Log;

import com.centerm.epos.EposApplication;
import com.centerm.epos.bean.GtBusinessListBean;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.transcation.pos.constant.JsonKeyGT;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DataHelper;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import config.BusinessConfig;

/**
 * Created by liubit on 2017/12/25.
 */

public class BaseGTHttpJsonMessage {
    protected Logger logger = Logger.getLogger(getClass());
    protected String transCode;

    public String getRequest(String transTag, Map<String, Object> transData) throws JSONException {
        transCode = transTag;
        if(TransCode.picQuery.equals(transTag)){
            return projectId(transData);
        }else if(TransCode.isAuthorization.equals(transTag)){
            return isAuthorization(transData);
        }else if(TransCode.staffVerify.equals(transTag)){
            return staffVerify(transData);
        }else if(TransCode.unpaidQuery.equals(transTag)){
            return unpaidQuery(transData);
        }else if(TransCode.receivedQuery.equals(transTag)){
            return receivedQuery(transData);
        }else if(TransCode.fingerRegister.equals(transTag)){
            return fingerRegister(transData);
        }else if(TransCode.fingerVerify.equals(transTag)){
            return fingerVerify(transData);
        }else if(TransCode.orderSync.equals(transTag)){
            return orderSync(transData);
        }else if(TransCode.printReceipt.equals(transTag)){
            return printReceipt(transData);
        }else if(TransCode.authCheck.equals(transTag)){
            return authCheck(transData);
        }else if(TransCode.repeatPrintReceipt.equals(transTag)){
            return repeatPrintReceipt(transData);
        }else if(TransCode.syncPosSts.equals(transTag)){
            return syncPosSts(transData);
        }else if(TransCode.generalReceipts.equals(transTag)){
            return generalReceipts(transData);
        }
        return "";
    }

    private String projectId(Map<String, Object> transData) throws JSONException {
        JSONObject data = new JSONObject();
        data.put(JsonKeyGT.projectId, transData.get(JsonKeyGT.projectId));
        return data.toString();
    }

    private String isAuthorization(Map<String, Object> transData) throws JSONException {
        JSONObject data = new JSONObject();
        //data.put(JsonKeyGT.projectId, transData.get(JsonKeyGT.projectId));
        data.put(JsonKeyGT.termSn, transData.get(JsonKeyGT.termSn));
        return data.toString();
    }

    private String staffVerify(Map<String, Object> transData) throws JSONException {
        JSONObject data = new JSONObject();
        data.put(JsonKeyGT.oaAccount, transData.get(JsonKeyGT.oaAccount));
        data.put(JsonKeyGT.oaPassword, transData.get(JsonKeyGT.oaPassword));
        return data.toString();
    }

    private String unpaidQuery(Map<String, Object> transData) throws JSONException {
        JSONObject data = new JSONObject();
        data.put(JsonKeyGT.idType, Integer.parseInt((String)transData.get(JsonKeyGT.idType)));
        data.put(JsonKeyGT.name, transData.get(JsonKeyGT.name));
        data.put(JsonKeyGT.idNo, transData.get(JsonKeyGT.idNo));
        //data.put(JsonKeyGT.projectId, transData.get(JsonKeyGT.projectId));
        data.put(JsonKeyGT.termSn, transData.get(JsonKeyGT.termSn));
        return data.toString();
    }

    private String receivedQuery(Map<String, Object> transData) throws JSONException {
        JSONObject data = new JSONObject();
        data.put(JsonKeyGT.idType, Integer.parseInt((String)transData.get(JsonKeyGT.idType)));
        data.put(JsonKeyGT.name, transData.get(JsonKeyGT.name));
        data.put(JsonKeyGT.idNo, transData.get(JsonKeyGT.idNo));
        //data.put(JsonKeyGT.projectId, transData.get(JsonKeyGT.projectId));
        data.put(JsonKeyGT.termSn, transData.get(JsonKeyGT.termSn));
        return data.toString();
    }

    private String authCheck(Map<String, Object> transData) throws JSONException {
        JSONObject data = new JSONObject();
        data.put(JsonKeyGT.mercId, BusinessConfig.getInstance().getIsoField(EposApplication.getAppContext(), 42));//商户号
        data.put(JsonKeyGT.mercNm, BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), BusinessConfig.Key.KEY_MCHNT_NAME));//商户名称
        data.put(JsonKeyGT.eqId, CommonUtils.getSn());//SN
        data.put(JsonKeyGT.ipAdr, DataHelper.getIp());//IP地址

        JSONArray array = new JSONArray();
        List<GtBusinessListBean.MoneyDetailListBean.CustomListBean> listBean =
                (List<GtBusinessListBean.MoneyDetailListBean.CustomListBean>) transData.get(JsonKeyGT.customList);
        for(GtBusinessListBean.MoneyDetailListBean.CustomListBean bean:listBean){
            JSONObject item = new JSONObject();
            item.put(JsonKeyGT.bankNo, transData.get(JsonKeyGT.bankNo));
            item.put(JsonKeyGT.idNm, bean.getName());
            item.put(JsonKeyGT.idNo, bean.getIdNo());
            array.put(item);
        }
        data.put(JsonKeyGT.userList, array);
        return data.toString();
    }

    private String fingerRegister(Map<String, Object> transData) throws JSONException {
        JSONObject data = new JSONObject();
        data.put(JsonKeyGT.oaAccount, transData.get(JsonKeyGT.oaAccount));
        data.put(JsonKeyGT.oaPassword, transData.get(JsonKeyGT.oaPassword));
        data.put(JsonKeyGT.userId, transData.get(JsonKeyGT.userId));
        data.put(JsonKeyGT.oaName, transData.get(JsonKeyGT.oaName));
        data.put(JsonKeyGT.idNo, transData.get(JsonKeyGT.idNo));
        data.put(JsonKeyGT.fingerReg, transData.get(JsonKeyGT.fingerReg));
        data.put(JsonKeyGT.fingerVer, transData.get(JsonKeyGT.fingerVer));
        return data.toString();
    }

    private String fingerVerify(Map<String, Object> transData) throws JSONException {
        JSONObject data = new JSONObject();
        data.put(JsonKeyGT.oaAccount, transData.get(JsonKeyGT.oaAccount));
        data.put(JsonKeyGT.fingerVer, transData.get(JsonKeyGT.fingerVer));
        return data.toString();
    }

    private String orderSync(Map<String, Object> transData) throws JSONException {
        JSONObject data = new JSONObject();
        data.put(JsonKeyGT.mainOrderId, transData.get(JsonKeyGT.mainOrderId));
        data.put(JsonKeyGT.projectId, transData.get(JsonKeyGT.projectId));
        data.put(JsonKeyGT.companyId, transData.get(JsonKeyGT.companyId));
        data.put(JsonKeyGT.projectName, transData.get(JsonKeyGT.projectName));
        data.put(JsonKeyGT.merchantId, transData.get(JsonKeyGT.merchantId));
        data.put(JsonKeyGT.name, transData.get(JsonKeyGT.name));
        data.put(JsonKeyGT.idType, transData.get(JsonKeyGT.idType));
        data.put(JsonKeyGT.idNo, transData.get(JsonKeyGT.idNo));
        data.put(JsonKeyGT.cardNo, transData.get(JsonKeyGT.cardNo));
        data.put(JsonKeyGT.termSn, transData.get(JsonKeyGT.termSn));
        data.put(JsonKeyGT.superviseFlag, transData.get(JsonKeyGT.superviseFlag));
        data.put(JsonKeyGT.area, transData.get(JsonKeyGT.area));
        data.put(JsonKeyGT.areaCode, transData.get(JsonKeyGT.areaCode));
        data.put(JsonKeyGT.contractNo, transData.get(JsonKeyGT.contractNo));
        data.put(JsonKeyGT.sign, transData.get(JsonKeyGT.sign));
        data.put(JsonKeyGT.moneyDetailList, transData.get(JsonKeyGT.moneyDetailList));
        return data.toString();
    }

    private String printReceipt(Map<String, Object> transData) throws JSONException {
        JSONObject data = new JSONObject();
        data.put(JsonKeyGT.projectId, transData.get(JsonKeyGT.projectId));
        data.put(JsonKeyGT.orderNo, transData.get(JsonKeyGT.orderNo));
        //data.put(JsonKeyGT.file, transData.get(JsonKeyGT.file));
        data.put(JsonKeyGT.client, transData.get(JsonKeyGT.client));
        data.put(JsonKeyGT.buyer, transData.get(JsonKeyGT.buyer));
        return data.toString();
    }

    private String repeatPrintReceipt(Map<String, Object> transData) throws JSONException {
        JSONObject data = new JSONObject();
        data.put(JsonKeyGT.orderNo, transData.get(JsonKeyGT.orderNo));
        return data.toString();
    }

    private String syncPosSts(Map<String, Object> transData) throws JSONException {
        JSONObject data = new JSONObject();
        data.put(JsonKeyGT.termSn, transData.get(JsonKeyGT.termSn));
        data.put(JsonKeyGT.termSts, transData.get(JsonKeyGT.termSts));
        return data.toString();
    }

    private String generalReceipts(Map<String, Object> transData) throws JSONException {
        JSONObject data = new JSONObject();
        data.put(JsonKeyGT.termSn, transData.get(JsonKeyGT.termSn));
        return data.toString();
    }
}
