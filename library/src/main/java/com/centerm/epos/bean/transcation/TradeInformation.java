package com.centerm.epos.bean.transcation;

import android.content.Context;

import com.centerm.cpay.midsdk.dev.define.IPbocService;
import com.centerm.cpay.midsdk.dev.define.pboc.TransParams;
import com.centerm.epos.EposApplication;
import com.centerm.epos.common.NFCTradeChannel;
import com.centerm.epos.xml.bean.process.TradeProcess;

import java.util.Map;

import config.BusinessConfig;

/**
 * Created by yuhc on 2017/2/15.
 * 交易信息
 */

public class TradeInformation {

    private String transCode;//交易码
    private TradeProcess tradeProcess;//交易流程对象

    //三个标识的优先级从高到低：forceInsert > forcePin > preferClss
    private boolean forcePin = false;//闪付凭密标识
    private boolean forceInsert = false;//强制插卡标识
    private boolean preferClss = true;//优先挥卡标识

    private Map<String, String> dataMap;//交易数据集合
    private Map<String, String> tempMap;//临时数据集合
    private Map<String, Object> transDatas; //业务数据
    private Map<String, Object> respDataMap; //业务数据
    private IPbocService pbocService;//PBOC服务
    private TransParams pbocParams;//PBOC参数

    public TradeInformation() {
    }

    public TradeInformation(String transCode, TradeProcess tradeProcess) {
        this.transCode = transCode;
        this.tradeProcess = tradeProcess;
    }

    public Map<String, Object> getTransDatas() {
        return transDatas;
    }

    public void setTransDatas(Map<String, Object> transDatas) {
        this.transDatas = transDatas;
    }

    public String getTransCode() {
        return transCode;
    }

    public void setRespDataMap(Map<String, Object> respDataMap) {
        this.respDataMap = respDataMap;
    }

    public Map<String, Object> getRespDataMap() {
        return respDataMap;
    }

    public void setTransCode(String transCode) {
        this.transCode = transCode;
    }

    public TradeProcess getTradeProcess() {
        return tradeProcess;
    }

    public void setTradeProcess(TradeProcess tradeProcess) {
        this.tradeProcess = tradeProcess;
    }

    public boolean isForcePin() {
        return forcePin;
    }

    public void setForcePin(boolean forcePin) {
        this.forcePin = forcePin;
    }

    public boolean isForceInsert() {
        return forceInsert;
    }

    public void setForceInsert(boolean forceInsert) {
        this.forceInsert = forceInsert;
    }

    public boolean isPreferClss() {
        return preferClss;
    }

    public void setPreferClss(boolean preferClss) {
        this.preferClss = preferClss;
    }

    public Map<String, String> getDataMap() {
        return tradeProcess == null ? null : tradeProcess.getDataMap();
    }

    public void setDataMap(Map<String, String> dataMap) {
        this.dataMap = dataMap;
    }

    public Map<String, String> getTempMap() {
        return tradeProcess == null ? null : tradeProcess.getTempMap();
    }

    public void setTempMap(Map<String, String> tempMap) {
        this.tempMap = tempMap;
    }

    public IPbocService getPbocService() {
        return pbocService;
    }

    public void setPbocService(IPbocService pbocService) {
        this.pbocService = pbocService;
    }

    public TransParams getPbocParams() {
        return pbocParams;
    }

    public void setPbocParams(TransParams pbocParams) {
        Context appContext = EposApplication.getAppContext();
        if(NFCTradeChannel.OFFLINE == NFCTradeChannel.nameOf(BusinessConfig.getInstance().getValue(appContext,
                BusinessConfig.Key.NFC_TRADE_CHANNEL))) {
            pbocParams.setSupportEc(true);
            pbocParams.setForceOnline(false);
        }else {
            pbocParams.setSupportEc(false);
            pbocParams.setForceOnline(true);
        }
        pbocParams.setSupportSm(BusinessConfig.getInstance().getToggle(appContext, BusinessConfig.Key.TOGGLE_EMV_SM));
        this.pbocParams = pbocParams;
    }
}
