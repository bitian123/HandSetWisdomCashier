package com.centerm.epos.ebi.msg;

import android.text.TextUtils;

import com.centerm.cloudsys.sdk.common.utils.MD5Utils;
import com.centerm.epos.EposApplication;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.ebi.App;
import com.centerm.epos.ebi.keys.JsonKey;
import com.centerm.epos.ebi.utils.DateUtil;
import com.centerm.epos.ebi.utils.RSAUtils;
import com.centerm.epos.ebi.utils.SecurityUtil;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.DataHelper;
import com.centerm.epos.utils.RSAUtils2;
import com.google.gson.Gson;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import config.BusinessConfig;

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
 */

public class BaseHttpJsonMessage {
    protected Logger logger = Logger.getLogger(getClass());
    protected Gson gson = new Gson();
    public static String CAkey;
    protected String transCode;

    public String getRequest(String transTag, Map<String, Object> transData) throws JSONException {
        transCode = transTag;
        if(TextUtils.equals(DOWNLOAD_MAIN_KEY, transTag)){
            return getDownMakRequest(transData);
        }else {
            return getTransRequest(transData);
        }
    }

    /**
     * 下载主密钥请求头
     * */
    private String getDownMakRequest(Map<String, Object> transData) throws JSONException {
        JSONObject data = new JSONObject();
        data.put(JsonKey.head, getRequestHeader(true,transData));
        data.put(JsonKey.body, getDownMakBody());
        return data.toString();
    }

    private String getTransRequest(Map<String, Object> transData) throws JSONException {
        JSONObject data = new JSONObject();
        JSONObject head = getRequestHeader(false, transData);
        JSONObject body = getTransBody(transData);
        data.put(JsonKey.head, head);
        data.put(JsonKey.body, body);
        data.put(JsonKey.sign, getSign(head, body));
        return data.toString();
    }

    public String getTransRequestWY(Map<String, Object> transData) throws JSONException {
        JSONObject bodyJson = new JSONObject();
        bodyJson.put(JsonKey.mer_order_no, transData.get(JsonKey.out_order_no));
        bodyJson.put(JsonKey.pay_result, "S");
        bodyJson.put(JsonKey.result_desc, "success");
        try {
            if(transData.get(JsonKey.pay_time)==null){
                logger.error(transData);
                bodyJson.put(JsonKey.pay_time, ""+transData.get(TransDataKey.KEY_TRANS_TIME)+transData.get(TradeInformationTag.TRANS_TIME));
            }else {
                bodyJson.put(JsonKey.pay_time, DateUtil.formatTime((String) transData.get(JsonKey.pay_time), "yyyy-MM-dd HH:mm:ss", "yyyyMMddHHmmss"));
            }
            bodyJson.put(JsonKey.sign, RSAUtils2.signRSA(bodyJson));
        }catch (Exception e){}
        bodyJson.put(JsonKey.trancde, com.centerm.epos.ebi.common.TransCode.SALE_PROPERTY_NOTICE_CODE);
        bodyJson.put(JsonKey.pay_source, "dy");
        bodyJson.put(JsonKey.serial_num, transData.get(TradeInformationTag.TRACE_NUMBER));
        bodyJson.put(JsonKey.pay_amount, transData.get(TradeInformationTag.TRANS_MONEY));
        return bodyJson.toString();
    }

    /**
     * 交易请求头
     * */
    private JSONObject getRequestHeader(boolean isDownMak, Map<String, Object> transData){
        JSONObject headJson = new JSONObject();
        try {
            headJson.put(JsonKey.mercode, GetRequestData.getMercode());
            headJson.put(JsonKey.termcde, GetRequestData.getTermcde());
            headJson.put(JsonKey.termidm, GetRequestData.getSn());
            headJson.put(JsonKey.imei, GetRequestData.getImei());
            headJson.put(JsonKey.sendTime, transData.get(JsonKey.sendTime));
            headJson.put(JsonKey.stationInfo, GetRequestData.getStationInfo(App.getAppContext()));
            if(isDownMak){
                headJson.put(JsonKey.msgType, "0800");
            }else {
                headJson.put(JsonKey.msgType, "0200");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return headJson;
    }

    private JSONObject getDownMakBody(){
        JSONObject bodyJson = new JSONObject();
        try {
            CAkey = GetRequestData.getRandom(6)+DateUtil.getNowTime();
            CAkey = MD5Utils.getMD5Str(CAkey);
            String request = GetRequestData.getMercode()+GetRequestData.getTermcde()+CAkey;
            InputStream inPublic = EposApplication.getAppContext().getAssets().open("cert/pub.txt");
            if(BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), BusinessConfig.Key.PRO_ENR)){
                inPublic = EposApplication.getAppContext().getAssets().open("cert/scrsapub.key");
                logger.info("使用scrsapub.key");
            }else {
                logger.info("使用pub.txt");
            }
            RSAUtils.loadPublicKey(inPublic);
            request = RSAUtils.encryptWithRSA(request);
            bodyJson.put(JsonKey.request, request);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bodyJson;
    }

    private JSONObject getTransBody(Map<String, Object> transData){
        switch (transCode){
            case TransCode.SALE_SCAN:
                return GetTransData.getData_SALE_SCAN(transData);
            case SALE_SCAN_QUERY:
                return GetTransData.getData_SALE_SCAN_QUERY(transData);
            case SALE_SCAN_VOID:
                return GetTransData.getData_SALE_SCAN_VOID(transData);
            case SALE_SCAN_VOID_QUERY:
                return GetTransData.getData_SALE_SCAN_VOID_QUERY(transData);
            case SALE_SCAN_REFUND:
                return GetTransData.getData_SALE_SCAN_REFUND(transData);
            case SALE_SCAN_REFUND_QUERY:
                return GetTransData.getData_SALE_SCAN_REFUND_QUERY(transData);
            case SALE_PROPERTY:
                return GetTransData.getData_SALE_PROPERTY(transData);
            case PROPERTY_NOTICE:
                return GetTransData.getData_PROPERTY_NOTICE(transData);
        }
        return new JSONObject();
    }

    /**
     * 参与签名的数据有
     * 商户号，终端号，imei编号 以及消息体里的商户订单号，商户退款单号，交易码，付款码，订单金额
     * mercode|termcde|imei|mer_order_no|trancde|pay_amount|sha_key
     * 需按此顺序进行拼接
     * */
    private String getSign(JSONObject head, JSONObject body){
        StringBuilder builder = new StringBuilder();
        builder.append(checkValue(head, JsonKey.mercode));
        builder.append(checkValue(head, JsonKey.termcde));
        builder.append(checkValue(head, JsonKey.imei));
        builder.append(checkValue(body, JsonKey.mer_order_no));
        builder.append(checkValue(body, JsonKey.mer_refund_order_no));
        builder.append(checkValue(body, JsonKey.trancde));
        builder.append(checkValue(body, JsonKey.refund_amount));
        builder.append(checkValue(body, JsonKey.bar_code));
        builder.append(checkValue(body, JsonKey.pay_amount));
        builder.append(checkValue(body, JsonKey.channel));
        builder.append(Settings.getValue(EposApplication.getAppContext(), JsonKey.MAK, ""));
        String sign = SecurityUtil.SHA256(builder.toString()).toUpperCase();
        return sign;
    }

    private String checkValue(JSONObject data, String key){
        if(data.has(key)){
            try {
                return data.getString(key);
            } catch (JSONException e) {
                e.printStackTrace();
                return "";
            }
        }
        return "";
    }

}
