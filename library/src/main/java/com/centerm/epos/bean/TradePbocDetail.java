package com.centerm.epos.bean;

import android.text.TextUtils;

import com.centerm.epos.common.EmvTagKey;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuhc on 2017/2/28.
 * 交易记录的IC卡信息
 */

@DatabaseTable(tableName = "tb_trade_pboc_detail")
public class TradePbocDetail implements Serializable {
    public static final String KEY_COLUMN_NAME = "id";

    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField
    private String pbocUnpredictableNumber; //不可预知数,55域 Tag9F37
    @DatabaseField
    private String pbocAIP;   //应用交互特征,55域 Tag9F82
    @DatabaseField
    private String pbocTxnCert; //IC卡交易证书, TC
    @DatabaseField
    private String pbocTVR; //终端验证结果（TVR）
    @DatabaseField
    private String pbocTSI; //交易状态信息
    @DatabaseField
    private String pbocAID; //应用标识
    @DatabaseField
    private String pbocATC; //应用交易计数器
    @DatabaseField
    private String pbocIAD; //发卡行应用数据
    @DatabaseField
    private String pbocAppLable;    //应用标签
    @DatabaseField
    private String pbocAppName; //应用首选名称
    @DatabaseField
    private String pbocTAC; //交易验证码
    @DatabaseField
    private String pbocECA; //充值后卡片余额
    @DatabaseField
    private String pbocARQC;    //授权请求密文
    @DatabaseField
    private String pbocScriptResult;    //脚本执行结果    DF31
    @DatabaseField
    private String pbocCID; //应用密文类型 00-AAC，40-TC ， 80-ARQC
    @DatabaseField
    private String pbocAAC; //应用认证密文 AAC交易拒绝

    public TradePbocDetail() {
    }

    public TradePbocDetail(byte[] tlvByteData) {
    }

    public TradePbocDetail(String tlvStrData) {
    }

    public TradePbocDetail(Map<String, String> tlvMapData) {
        if (tlvMapData != null) {
            init(tlvMapData);
        }
    }

    private void init(Map<String, String> tlvMapData) {
        pbocUnpredictableNumber = tlvMapData.get(EmvTagKey.EMVTAG_RND_NUM);

        pbocAIP = tlvMapData.get(EmvTagKey.EMVTAG_AIP);
        fillAC(tlvMapData);
        pbocTVR = tlvMapData.get(EmvTagKey.EMVTAG_TVR);
        pbocTSI = tlvMapData.get(EmvTagKey.EMVTAG_TSI);
        pbocAID = tlvMapData.get(EmvTagKey.EMVTAG_AID);
        pbocATC = tlvMapData.get(EmvTagKey.EMVTAG_ATC);
        pbocIAD = tlvMapData.get(EmvTagKey.EMVTAG_IAD);
        pbocAppLable = tlvMapData.get(EmvTagKey.EMVTAG_APP_LABEL);
        pbocAppName = tlvMapData.get(EmvTagKey.EMVTAG_APP_NAME);
        pbocScriptResult = tlvMapData.get(EmvTagKey.EMVTAG_SCRIPT_RESULT);

//        以下内容是，电子现金业务的圈存和消费交易会生成的数据。
//        pbocTAC =
//        pbocECA =
    }

    private void fillAC(Map<String, String> tlvMapData) {
        String cid = tlvMapData.get(EmvTagKey.EMVTAG_CID);
        if (!TextUtils.isEmpty(cid)) {
            pbocCID = cid;
            int type = Integer.parseInt(cid, 16) >>> 6;
            if (type == 1)
                pbocTxnCert = tlvMapData.get(EmvTagKey.EMVTAG_AC);
            else if (type == 2)
                pbocARQC = tlvMapData.get(EmvTagKey.EMVTAG_AC);
            else
                pbocAAC = tlvMapData.get(EmvTagKey.EMVTAG_AC);
        }
    }

    /**
     * 保存电子现金业务相关数据
     */
    public void setECInfo(Map<String, Object> ecDataMap) {
        if (ecDataMap == null)
            return;
        pbocTAC = (String) ecDataMap.get(TradeInformationTag.EC_TRANS_TAC);
        pbocECA = (String) ecDataMap.get((TradeInformationTag.EC_TRANS_BALANCE));
    }

    public void setECInfoEx(Map<String, String> ecDataMap) {
        if (ecDataMap == null)
            return;
        pbocTAC = ecDataMap.get(TradeInformationTag.EC_TRANS_TAC);
        pbocECA = ecDataMap.get((TradeInformationTag.EC_TRANS_BALANCE));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPbocUnpredictableNumber() {
        return pbocUnpredictableNumber;
    }

    public void setPbocUnpredictableNumber(String pbocUnpredictableNumber) {
        this.pbocUnpredictableNumber = pbocUnpredictableNumber;
    }

    public String getPbocAIP() {
        return pbocAIP;
    }

    public void setPbocAIP(String pbocAIP) {
        this.pbocAIP = pbocAIP;
    }

    public String getPbocTxnCert() {
        return pbocTxnCert;
    }

    public void setPbocTxnCert(String pbocTxnCert) {
        this.pbocTxnCert = pbocTxnCert;
    }

    public String getPbocTVR() {
        return pbocTVR;
    }

    public void setPbocTVR(String pbocTVR) {
        this.pbocTVR = pbocTVR;
    }

    public String getPbocTSI() {
        return pbocTSI;
    }

    public void setPbocTSI(String pbocTSI) {
        this.pbocTSI = pbocTSI;
    }

    public String getPbocAID() {
        return pbocAID;
    }

    public void setPbocAID(String pbocAID) {
        this.pbocAID = pbocAID;
    }

    public String getPbocATC() {
        return pbocATC;
    }

    public void setPbocATC(String pbocATC) {
        this.pbocATC = pbocATC;
    }

    public String getPbocIAD() {
        return pbocIAD;
    }

    public void setPbocIAD(String pbocIAD) {
        this.pbocIAD = pbocIAD;
    }

    public String getPbocAppLable() {
        return pbocAppLable;
    }

    public void setPbocAppLable(String pbocAppLable) {
        this.pbocAppLable = pbocAppLable;
    }

    public String getPbocAppName() {
        return pbocAppName;
    }

    public void setPbocAppName(String pbocAppName) {
        this.pbocAppName = pbocAppName;
    }

    public String getPbocTAC() {
        return pbocTAC;
    }

    public void setPbocTAC(String pbocTAC) {
        this.pbocTAC = pbocTAC;
    }

    public String getPbocECA() {
        return pbocECA;
    }

    public void setPbocECA(String pbocECA) {
        this.pbocECA = pbocECA;
    }

    public String getPbocARQC() {
        return pbocARQC;
    }

    public void setPbocARQC(String pbocARQC) {
        this.pbocARQC = pbocARQC;
    }

    public String getPbocScriptResult() {
        return pbocScriptResult;
    }

    public void setPbocScriptResult(String pbocScriptResult) {
        this.pbocScriptResult = pbocScriptResult;
    }

    public String getPbocCID() {
        return pbocCID;
    }

    public void setPbocCID(String pbocCID) {
        this.pbocCID = pbocCID;
    }

    public String getPbocAAC() {
        return pbocAAC;
    }

    public void setPbocAAC(String pbocAAC) {
        this.pbocAAC = pbocAAC;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public Map<String, String> convert2Map() {
        Map<String, String> map = new HashMap<>();
        if (!TextUtils.isEmpty(pbocUnpredictableNumber))
            map.put(EmvTagKey.EMVTAG_RND_NUM, pbocUnpredictableNumber);
        if (!TextUtils.isEmpty(pbocAID))
            map.put(EmvTagKey.EMVTAG_AID, pbocAID);
        /*
        * 增加AAC处理
        * */
        if( !TextUtils.isEmpty(pbocAAC) ){
            map.put(EmvTagKey.EMVTAG_AC, pbocAAC);
        }else if (TextUtils.isEmpty(pbocTxnCert)) {
            if (!TextUtils.isEmpty(pbocARQC))
                map.put(EmvTagKey.EMVTAG_AC, pbocARQC);
        }else {
            if (!TextUtils.isEmpty(pbocTxnCert))
                map.put(EmvTagKey.EMVTAG_AC, pbocTxnCert);
        }

        if (!TextUtils.isEmpty(pbocAIP))
            map.put(EmvTagKey.EMVTAG_AIP, pbocAIP);
        if (!TextUtils.isEmpty(pbocTVR))
            map.put(EmvTagKey.EMVTAG_TVR, pbocTVR);
        if (!TextUtils.isEmpty(pbocTSI))
            map.put(EmvTagKey.EMVTAG_TSI, pbocTSI);
        if (!TextUtils.isEmpty(pbocAID))
            map.put(EmvTagKey.EMVTAG_AID, pbocAID);
        if (!TextUtils.isEmpty(pbocATC))
            map.put(EmvTagKey.EMVTAG_ATC, pbocATC);
        if (!TextUtils.isEmpty(pbocIAD))
            map.put(EmvTagKey.EMVTAG_IAD, pbocIAD);
        if (!TextUtils.isEmpty(pbocAppLable))
            map.put(EmvTagKey.EMVTAG_APP_LABEL, pbocAppLable);
        if (!TextUtils.isEmpty(pbocAppName))
            map.put(EmvTagKey.EMVTAG_APP_NAME, pbocAppName);
        if (!TextUtils.isEmpty(pbocTAC))
            map.put(TradeInformationTag.EC_TRANS_TAC, pbocTAC);
        if (!TextUtils.isEmpty(pbocECA))
            map.put(TradeInformationTag.EC_TRANS_BALANCE, pbocECA);
        if (!TextUtils.isEmpty(pbocScriptResult))
            map.put(EmvTagKey.EMVTAG_SCRIPT_RESULT, pbocScriptResult);
        if (!TextUtils.isEmpty(pbocCID))
            map.put(EmvTagKey.EMVTAG_CID, pbocCID);
        return map;
    }
}
