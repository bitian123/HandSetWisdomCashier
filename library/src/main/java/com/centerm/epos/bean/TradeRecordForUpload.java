package com.centerm.epos.bean;

import android.content.ContentValues;
import android.text.TextUtils;

import com.centerm.epos.common.TransCode;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuhc on 2017/8/28.
 * 用于交易记录而记录的内容
 */

@DatabaseTable(tableName = "tb_trade_record_for_upload")
public class TradeRecordForUpload {

    public static final String TABLE_NAME = "tb_trade_record_for_upload";
    public static final String AUTHOR_TAG = "com.centerm.epos.provider.trade";
    public static final String TRADE_RECORD_ITEM = "record/#";
    public static final String TRADE_RECORD = "record";
    public static final String TRADE_RECORD_LAST = "record_last";
    public static final String TRADE_RECORD_FULL = "records";
    public static final String TRADE_RECORD_CLEAR = "record_clear";

    private static final Map<String,String> transIdMap = new HashMap<>();

    @DatabaseField(id = true)   //主键
    private String termTransSeq;    //终端交易流水

    @DatabaseField
    private String trcansationType; //交易类型:1消费 2撤销 3退货

    @DatabaseField
    private String termSn;  //终端序列号

    @DatabaseField
    private String termType;    //终端类型

    @DatabaseField
    private String merchantNo;  //商户号

    @DatabaseField
    private String terminalNo;  //终端号

    @DatabaseField
    private String batchNo; //批次号

    @DatabaseField
    private String locTransTm;  //交易时间

    @DatabaseField
    private String locTransDt;  //交易日期

    @DatabaseField
    private String trcansationFlag; //交易标识(交易状态):2交易成功

    @DatabaseField
    private String outBankAccount;  //转出卡号（主账号）

    @DatabaseField
    private String platprocCd;      //系统参考号

    @DatabaseField
    private String trcansationMoney;    //交易金额

    @DatabaseField
    private String cashierCode; //收银员标识



    static {
        // 1消费 2撤销 3退货 4扫码消费 5扫码撤销 6扫码退货 7预授权 8预授权撤销 9预授权完成 10预授权完成撤销
        transIdMap.put(TransCode.SALE, "1");
        transIdMap.put(TransCode.VOID, "2");
        transIdMap.put(TransCode.REFUND, "3");
        transIdMap.put(TransCode.SALE_SCAN, "4");
        transIdMap.put(TransCode.VOID_SCAN, "5");
        transIdMap.put(TransCode.REFUND_SCAN, "6");
        transIdMap.put(TransCode.AUTH, "7");
        transIdMap.put(TransCode.CANCEL, "8");
        transIdMap.put(TransCode.AUTH_COMPLETE, "9");
        transIdMap.put(TransCode.COMPLETE_VOID, "10");
    }

    public TradeRecordForUpload() {
    }

    public TradeRecordForUpload(TradeInfoRecord tradeInfoRecord) {
        if (tradeInfoRecord != null){
            termTransSeq = tradeInfoRecord.getVoucherNo();
            trcansationType = tradeIdMapper(tradeInfoRecord.getTransType());
            termType = "智能POS";
            merchantNo = tradeInfoRecord.getMerchantNo();
            terminalNo = tradeInfoRecord.getTerminalNo();
            batchNo = tradeInfoRecord.getBatchNo();
            locTransTm = tradeInfoRecord.getTransTime();
            locTransDt = tradeInfoRecord.getTransDate();
            trcansationFlag = "2";  //只记录成功的交易
            outBankAccount = tradeInfoRecord.getCardNo();
            platprocCd = tradeInfoRecord.getReferenceNo();
            trcansationMoney = tradeInfoRecord.getAmount();
            cashierCode = tradeInfoRecord.getOperatorNo();
        }
    }

    private String tradeIdMapper(String transType) {
        return transIdMap.get(transType);
    }

    public String getTermTransSeq() {
        return termTransSeq;
    }

    public void setTermTransSeq(String termTransSeq) {
        this.termTransSeq = termTransSeq;
    }

    public String getTrcansationType() {
        return trcansationType;
    }

    public void setTrcansationType(String trcansationType) {
        this.trcansationType = trcansationType;
    }

    public String getTermSn() {
        return termSn;
    }

    public void setTermSn(String termSn) {
        this.termSn = termSn;
    }

    public String getTermType() {
        return termType;
    }

    public void setTermType(String termType) {
        this.termType = termType;
    }

    public String getMerchantNo() {
        return merchantNo;
    }

    public void setMerchantNo(String merchantNo) {
        this.merchantNo = merchantNo;
    }

    public String getTerminalNo() {
        return terminalNo;
    }

    public void setTerminalNo(String terminalNo) {
        this.terminalNo = terminalNo;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getLocTransTm() {
        return locTransTm;
    }

    public void setLocTransTm(String locTransTm) {
        this.locTransTm = locTransTm;
    }

    public String getLocTransDt() {
        return locTransDt;
    }

    public void setLocTransDt(String locTransDt) {
        this.locTransDt = locTransDt;
    }

    public String getTrcansationFlag() {
        return trcansationFlag;
    }

    public void setTrcansationFlag(String trcansationFlag) {
        this.trcansationFlag = trcansationFlag;
    }

    public String getOutBankAccount() {
        return outBankAccount;
    }

    public void setOutBankAccount(String outBankAccount) {
        this.outBankAccount = outBankAccount;
    }

    public String getPlatprocCd() {
        return platprocCd;
    }

    public void setPlatprocCd(String platprocCd) {
        this.platprocCd = platprocCd;
    }

    public String getTrcansationMoney() {
        return trcansationMoney;
    }

    public void setTrcansationMoney(String trcansationMoney) {
        this.trcansationMoney = trcansationMoney;
    }

    public String getCashierCode() {
        return cashierCode;
    }

    public void setCashierCode(String cashierCode) {
        this.cashierCode = cashierCode;
    }

    public ContentValues convert2ContentValues(){
        ContentValues contentValues = new ContentValues();
        if (!TextUtils.isEmpty(termTransSeq))
            contentValues.put("termTransSeq", termTransSeq);
        if (!TextUtils.isEmpty(trcansationType))
            contentValues.put("trcansationType", trcansationType);
        if (!TextUtils.isEmpty(termSn))
            contentValues.put("termSn", termSn);
        if (!TextUtils.isEmpty(termType))
            contentValues.put("termType", termType);
        if (!TextUtils.isEmpty(merchantNo))
            contentValues.put("merchantNo", merchantNo);
        if (!TextUtils.isEmpty(terminalNo))
            contentValues.put("terminalNo", terminalNo);
        if (!TextUtils.isEmpty(batchNo))
            contentValues.put("batchNo", batchNo);
        if (!TextUtils.isEmpty(locTransTm))
            contentValues.put("locTransTm", locTransTm);
        if (!TextUtils.isEmpty(locTransDt))
            contentValues.put("locTransDt", locTransDt);
        if (!TextUtils.isEmpty(trcansationFlag))
            contentValues.put("trcansationFlag", trcansationFlag);
        if (!TextUtils.isEmpty(outBankAccount))
            contentValues.put("outBankAccount", outBankAccount);
        if (!TextUtils.isEmpty(platprocCd))
            contentValues.put("platprocCd", platprocCd);
        if (!TextUtils.isEmpty(trcansationMoney))
            contentValues.put("trcansationMoney", trcansationMoney);
        if (!TextUtils.isEmpty(cashierCode))
            contentValues.put("cashierCode", cashierCode);
        return contentValues;
    }

    @Override
    public String toString() {
        return "TradeRecordForUpload{" +
                "termTransSeq='" + termTransSeq + '\'' +
                ", trcansationType='" + trcansationType + '\'' +
                ", termSn='" + termSn + '\'' +
                ", termType='" + termType + '\'' +
                ", merchantNo='" + merchantNo + '\'' +
                ", terminalNo='" + terminalNo + '\'' +
                ", batchNo='" + batchNo + '\'' +
                ", locTransTm='" + locTransTm + '\'' +
                ", locTransDt='" + locTransDt + '\'' +
                ", trcansationFlag='" + trcansationFlag + '\'' +
                ", outBankAccount='" + outBankAccount + '\'' +
                ", platprocCd='" + platprocCd + '\'' +
                ", trcansationMoney='" + trcansationMoney + '\'' +
                ", cashierCode='" + cashierCode + '\'' +
                '}';
    }
}
