package com.centerm.epos.bean;

import com.centerm.epos.bean.transcation.BalancAmount;
import com.centerm.epos.common.ConstDefine;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by yuhc on 2017/2/27.
 * 交易记录信息
 */

@DatabaseTable(tableName = "tb_trade_information")
public class TradeInfoRecord implements Serializable {

    public static final String KEY_COLUMN_NAME = "voucherNo";

    @DatabaseField
    private String merchantName;//商户名称
    @DatabaseField
    private String merchantNo;//商户编号
    @DatabaseField
    private String terminalNo;  //终端编号
    @DatabaseField
    private String acquireInstituteID;   //收单行标识
    @DatabaseField
    private String issueInstituteID;  //发卡行标识
    @DatabaseField
    private String cardNo;//卡号
    @DatabaseField
    private String scanCode;    //扫码代码
    @DatabaseField
    private String cardSequenceNumber;  //卡片序列号
    @DatabaseField
    private String operatorNo;  //操作员编号
    @DatabaseField
    private String transType;//交易类型
    @DatabaseField
    private String cardExpiredDate; //卡有效期
    @DatabaseField
    private String batchNo; //交易批次号
    @DatabaseField(id = true)//作为主键
    private String voucherNo;   //凭证号/终端流水号
    @DatabaseField
    private String scanVoucherNo;   //扫码付款凭证码
    @DatabaseField
    private String transDate;   //交易日期
    @DatabaseField
    private String transTime;   //交易时间
    @DatabaseField
    private String authorizeNo;  //授权码
    @DatabaseField
    private String referenceNo; //检索参考号
    @DatabaseField
    private String amount;//交易金额
    @DatabaseField
    private String bankcardOganization;    //卡组织代码 VISA/CUP
    @DatabaseField
    private String intoAccount; //传入卡卡号
    @DatabaseField
    private String currencyCode; //货币代码
    @DatabaseField
    private String serviceEntryMode;    //服务点方式码 ，记录插/刷/挥卡，是否输入密码
    @DatabaseField
    private String printInfo;   //打印信息，用于定制化打印数据
    @DatabaseField
    private String oriVoucherNum;   //原交易凭证号
    @DatabaseField
    private String oriAuthCode; //原交易授权码
    @DatabaseField
    private String oriTradeDate;    //原交易日期
    @DatabaseField
    private String oriRefereceNum;    //原交易参考号
    @DatabaseField
    private String reverseFieldInfo;    //63域，保留域信息，包括发卡行、收单行、银联、终端等
    @DatabaseField
    private String pbocOriTlvData;  //原始的TLV数据
    @DatabaseField(foreign = true, columnName = "pboc_detail_id", foreignAutoRefresh = true, foreignAutoCreate = true)
    private TradePbocDetail pbocDetail; //PBOC信息

    /**
     * {@link TradeInformationTag#TRANS_STATE_FLAG} <br/>
     * {@link ConstDefine#TRANS_STATE_VOID}<br/>
     * {@link ConstDefine#TRANS_STATE_ADJUST} <br/>
     * {@link #setStateFlag(int)}<br/>
     */
    @DatabaseField
    private int stateFlag;   //交易状态：0x01-已撤销  0x02 离线调整
    @DatabaseField(defaultValue = "0")
    private int sendCount;//上送次数   最多3次   99说明平台返回该笔订单失败
    @DatabaseField(defaultValue = "false")
    private boolean isBatchSuccess;//是否批上送成功 true表示批上送成功

    @DatabaseField
    private String unicom_scna_type;  //联通支付扫码类型

    @DatabaseField
    private String mobile_phone_number; //电话号码，电子签名上送时使用

    @DatabaseField
    private String transYear;    //年份

    @DatabaseField
    private String settlmentDate;    //结算日期

    @DatabaseField
    private String oriTermNo;    //原终端号
    @DatabaseField
    private String oriBatchNo;    //原批次号

    @DatabaseField
    private String templateId; //打印模板

    @DatabaseField
    private String settlementInfo;//结算账户信息


    @DatabaseField
    private String superviseFlag; //监管标志

    @DatabaseField
    private String areaCode;//地区码

    /**
     * 交易状态 0x0800-脱机交易拒绝AAC，0x0400-ARPC错误但仍然接受的交易(是正常交易，根据tvr判断)
     * 目前只用来指示离线拒绝的交易
     * zhouzhihua 2017-12-26
     * {@link TradeInformationTag#TRANS_STATUS_VALUE} <br/>
     */
    @DatabaseField(defaultValue = "0")
    private int transStatus;
    /** "1"-离线交易上送成功，"2"-离线交易上送失败,"4"-后台无应答
    * zhouzhihua 2017-12-26，离线交易增加
     * {@link TradeInformationTag#OFFLINE_TRANS_UPLOAD_STATUS} <br/>
    */
    @DatabaseField(defaultValue = "0")
    private String offlineTransUploadStatus;
    /*因62域用法较多，数据是否保存由实际业务需求决定，打印和显示时需根据实际情况进行数据转换*/
    @DatabaseField
    private String iso62Req; //发送包
    @DatabaseField
    private String iso62Res; //应答包

    @DatabaseField
    private String sBalance; //积分余额、余额 54域的余额值

    @DatabaseField
    private String organizationCode; //原机构授权代码  离线类交易
    /**
     * originalAuthMode 授权方式
     * {@link com.centerm.epos.common.ConstDefine#OFFLINE_AUTH_CODE_AUTH} <br/>
     * {@link com.centerm.epos.common.ConstDefine#OFFLINE_AUTH_CODE_POS} <br/>
     * {@link com.centerm.epos.common.ConstDefine#OFFLINE_AUTH_CODE_TELEPHONE} <br/>
     */
    @DatabaseField
    private String originalAuthMode; //授权方式




    public TradeInfoRecord() {
    }

    public TradeInfoRecord(String transCode, Map<String, Object> dataMap) {
        transType = transCode;
        initValue(dataMap);
    }

    private void initValue(Map<String, Object> mapData) {
        merchantName = (String) mapData.get(TradeInformationTag.MERCHANT_NAME);
        merchantNo = (String) mapData.get(TradeInformationTag.MERCHANT_IDENTIFICATION);
        terminalNo = (String) mapData.get(TradeInformationTag.TERMINAL_IDENTIFICATION);
        acquireInstituteID = (String) mapData.get(TradeInformationTag.ACQUIRER_IDENTIFICATION);
        issueInstituteID = (String) mapData.get(TradeInformationTag.ISSUER_IDENTIFICATION);
        cardNo = (String) mapData.get(TradeInformationTag.BANK_CARD_NUM);
        scanCode = (String) mapData.get(TradeInformationTag.SCAN_CODE);
        cardSequenceNumber = (String) mapData.get(TradeInformationTag.CARD_SEQUENCE_NUMBER);
        operatorNo = (String) mapData.get(TradeInformationTag.OPERATOR_CODE);
        cardExpiredDate = (String) mapData.get(TradeInformationTag.DATE_EXPIRED);
        batchNo = (String) mapData.get(TradeInformationTag.BATCH_NUMBER);
        voucherNo = (String) mapData.get(TradeInformationTag.TRACE_NUMBER);
        scanVoucherNo = (String) mapData.get(TradeInformationTag.SCAN_VOUCHER_NO);
        transDate = (String) mapData.get(TradeInformationTag.TRANS_DATE);
        transTime = (String) mapData.get(TradeInformationTag.TRANS_TIME);
        authorizeNo = (String) mapData.get(TradeInformationTag.AUTHORIZATION_IDENTIFICATION);
        referenceNo = (String) mapData.get(TradeInformationTag.REFERENCE_NUMBER);
        amount = (String) mapData.get(TradeInformationTag.TRANS_MONEY);
        bankcardOganization = (String) mapData.get(TradeInformationTag.BANKCARD_ORGANIZATION);
        intoAccount = (String) mapData.get(TradeInformationTag.TRANSFER_INTO_CARD);
        printInfo = (String) mapData.get(TradeInformationTag.SLIP_VERSION);
        serviceEntryMode = (String) mapData.get(TradeInformationTag.SERVICE_ENTRY_MODE);
        pbocDetail = (TradePbocDetail) mapData.get(TradeInformationTag.TRANS_IC_INFO);
        currencyCode = (String) mapData.get(TradeInformationTag.CURRENCY_CODE);
        pbocOriTlvData = (String) mapData.get(TradeInformationTag.IC_DATA);
        oriAuthCode = (String) mapData.get(TransDataKey.key_oriAuthCode);
        oriRefereceNum = (String) mapData.get(TransDataKey.key_oriReferenceNumber);
        oriTradeDate = (String) mapData.get(TransDataKey.key_oriTransDate);
        oriVoucherNum = (String) mapData.get(TransDataKey.key_oriVoucherNumber);
        reverseFieldInfo = (String) mapData.get(TradeInformationTag.REVERSE_FIELD);
        unicom_scna_type = (String) mapData.get(TradeInformationTag.UNICOM_SCAN_TYPE);
        mobile_phone_number = (String) mapData.get(TradeInformationTag.PHONE_NUMBER);
        transYear = (String) mapData.get(TradeInformationTag.TRANS_YEAR);
        settlmentDate = (String) mapData.get(TradeInformationTag.DATE_SETTLEMENT);
        oriTermNo = (String) mapData.get(TransDataKey.KEY_ORI_TERMINAL_NO);/*脱机退货新增加*/
        oriBatchNo = (String) mapData.get(TransDataKey.KEY_ORI_BATCH_NO);/*脱机退货新增加*/

        String s = (String)mapData.get(TradeInformationTag.TRANS_STATUS_VALUE);
        transStatus = (null == s || s.length() == 0 ) ? 0 : Integer.parseInt(s);
        offlineTransUploadStatus = (String)mapData.get(TradeInformationTag.OFFLINE_TRANS_UPLOAD_STATUS);

        iso62Req = (String) mapData.get(TradeInformationTag.ISO62_REQ);
        iso62Res = (String) mapData.get(TradeInformationTag.ISO62_RES);

        BalancAmount balancAmount = (BalancAmount)mapData.get(TradeInformationTag.BALANC_AMOUNT);
        if( null != balancAmount) {
            sBalance = balancAmount.getAccountType() + balancAmount.getAmountType() +
                       balancAmount.getCurrencyCode() + balancAmount.getAmountSign()
                       + String.format(Locale.CHINA,"%012d",(long)balancAmount.getAmount());
        }

        organizationCode = (String) mapData.get(TradeInformationTag.ORIGINAL_AUTH_ORG_CODE);
        originalAuthMode = (String) mapData.get(TradeInformationTag.ORIGINAL_AUTH_MODE);
        templateId = (String) mapData.get(TradeInformationTag.TEMPLATE_ID);
        settlementInfo = (String) mapData.get(TradeInformationTag.SETTLEMENT_INFO);
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
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

    public String getAcquireInstituteID() {
        return acquireInstituteID;
    }

    public void setAcquireInstituteID(String acquireInstituteID) {
        this.acquireInstituteID = acquireInstituteID;
    }

    public String getIssueInstituteID() {
        return issueInstituteID;
    }

    public void setIssueInstituteID(String issueInstituteID) {
        this.issueInstituteID = issueInstituteID;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getCardSequenceNumber() {
        return cardSequenceNumber;
    }

    public void setCardSequenceNumber(String cardSequenceNumber) {
        this.cardSequenceNumber = cardSequenceNumber;
    }

    public String getOperatorNo() {
        return operatorNo;
    }

    public void setOperatorNo(String operatorNo) {
        this.operatorNo = operatorNo;
    }

    public String getTransType() {
        return transType;
    }

    public void setTransType(String transType) {
        this.transType = transType;
    }

    public String getCardExpiredDate() {
        return cardExpiredDate;
    }

    public void setCardExpiredDate(String cardExpiredDate) {
        this.cardExpiredDate = cardExpiredDate;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getVoucherNo() {
        return voucherNo;
    }

    public void setVoucherNo(String voucherNo) {
        this.voucherNo = voucherNo;
    }

    public String getTransDate() {
        return transDate;
    }

    public void setTransDate(String transDate) {
        this.transDate = transDate;
    }

    public String getTransTime() {
        return transTime;
    }

    public void setTransTime(String transTime) {
        this.transTime = transTime;
    }

    public String getAuthorizeNo() {
        return authorizeNo;
    }

    public void setAuthorizeNo(String authorizeNo) {
        this.authorizeNo = authorizeNo;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getBankcardOganization() {
        return bankcardOganization;
    }

    public void setBankcardOganization(String bankcardOganization) {
        this.bankcardOganization = bankcardOganization;
    }

    public String getIntoAccount() {
        return intoAccount;
    }

    public void setIntoAccount(String intoAccount) {
        this.intoAccount = intoAccount;
    }

    public int getStateFlag() {
        return stateFlag;
    }

    /**
     * @param stateFlag {@link ConstDefine#TRANS_STATE_VOID}<br/>
     * {@link ConstDefine#TRANS_STATE_ADJUST} <br/>
     * {@link #stateFlag}<br/>
     * {@link TradeInformationTag#TRANS_STATE_FLAG} <br/>
     */
    public void setStateFlag(int stateFlag) {
        this.stateFlag = stateFlag;
    }

    public TradePbocDetail getPbocDetail() {
        return pbocDetail;
    }

    public void setPbocDetail(TradePbocDetail pbocDetail) {
        this.pbocDetail = pbocDetail;
    }

    public String getServiceEntryMode() {
        return serviceEntryMode;
    }

    public void setServiceEntryMode(String serviceEntryMode) {
        this.serviceEntryMode = serviceEntryMode;
    }

    public String getPrintInfo() {
        return printInfo;
    }

    public void setPrintInfo(String printInfo) {
        this.printInfo = printInfo;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public boolean isBatchSuccess() {
        return isBatchSuccess;
    }

    public String getPbocOriTlvData() {
        return pbocOriTlvData;
    }

    public void setPbocOriTlvData(String pbocOriTlvData) {
        this.pbocOriTlvData = pbocOriTlvData;
    }

    public void setBatchSuccess(boolean batchSuccess) {
        isBatchSuccess = batchSuccess;
    }

    public int getSendCount() {
        return sendCount;
    }

    public void setSendCount(int sendCount) {
        this.sendCount = sendCount;
    }

    public String getScanCode() {
        return scanCode;
    }

    public void setScanCode(String scanCode) {
        this.scanCode = scanCode;
    }

    public String getScanVoucherNo() {
        return scanVoucherNo;
    }

    public void setScanVoucherNo(String scanVoucherNo) {
        this.scanVoucherNo = scanVoucherNo;
    }

    public String getUnicom_scna_type() {
        return unicom_scna_type;
    }

    public void setUnicom_scna_type(String unicom_scna_type) {
        this.unicom_scna_type = unicom_scna_type;
    }

    public String getOriVoucherNum() {
        return oriVoucherNum;
    }

    public void setOriVoucherNum(String oriVoucherNum) {
        this.oriVoucherNum = oriVoucherNum;
    }

    public String getOriAuthCode() {
        return oriAuthCode;
    }

    public void setOriAuthCode(String oriAuthCode) {
        this.oriAuthCode = oriAuthCode;
    }

    public String getOriTradeDate() {
        return oriTradeDate;
    }

    public void setOriTradeDate(String oriTradeDate) {
        this.oriTradeDate = oriTradeDate;
    }

    public String getOriRefereceNum() {
        return oriRefereceNum;
    }

    public void setOriRefereceNum(String oriRefereceNum) {
        this.oriRefereceNum = oriRefereceNum;
    }

    public String getReverseFieldInfo() {
        return reverseFieldInfo;
    }

    public void setReverseFieldInfo(String reverseFieldInfo) {
        this.reverseFieldInfo = reverseFieldInfo;
    }

    public String getMobile_phone_number() {
        return mobile_phone_number;
    }

    public void setMobile_phone_number(String mobile_phone_number) {
        this.mobile_phone_number = mobile_phone_number;
    }

    public String getTransYear() {
        return transYear;
    }

    public void setTransYear(String transYear) {
        this.transYear = transYear;
    }

    public String getSettlmentDate() {
        return settlmentDate;
    }

    public void setSettlmentDate(String settlmentDate) {
        this.settlmentDate = settlmentDate;
    }


    public String getOriTermNo() {
        return oriTermNo;
    }

    public void setOriTermNo(String oriTermNo) {
        this.oriTermNo = oriTermNo;
    }

    public String getOriBatchNo() {
        return oriBatchNo;
    }

    public void setOriBatchNo(String oriBatchNo) {
        this.oriBatchNo = oriBatchNo;
    }

    public int getTransStatus() {
        return transStatus;
    }

    public void setTransStatus(int transStatus) {
        this.transStatus = transStatus;
    }

    public String getOfflineTransUploadStatus() {
        return offlineTransUploadStatus;
    }

    public void setOfflineTransUploadStatus(String offlineTransUploadStatus) {
        this.offlineTransUploadStatus = offlineTransUploadStatus;
    }

    public String getIso62Req() {
        return iso62Req;
    }

    public void setIso62Req(String iso62Req) {
        this.iso62Req = iso62Req;
    }

    public String getIso62Res() {
        return iso62Res;
    }

    public void setIso62Res(String iso62Reqs) {
        this.iso62Res = iso62Res;
    }


    public String getSBalance() {
        return sBalance;
    }

    public void setSBalance(String sBalance) {
        this.sBalance = sBalance;
    }


    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getSettlementInfo() {
        return settlementInfo;
    }

    public void setSettlementInfo(String settlementInfo) {
        this.settlementInfo = settlementInfo;
    }

    public String getSuperviseFlag() {
        return superviseFlag;
    }

    public void setSuperviseFlag(String superviseFlag) {
        this.superviseFlag = superviseFlag;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public Map<String, String> convert2Map() {
        Map<String, String> map = new HashMap<>();
        map.put(TradeInformationTag.MERCHANT_NAME, merchantName);
        map.put(TradeInformationTag.MERCHANT_IDENTIFICATION, merchantNo);
        map.put(TradeInformationTag.TERMINAL_IDENTIFICATION, terminalNo);
        map.put(TradeInformationTag.ACQUIRER_IDENTIFICATION, acquireInstituteID);
        map.put(TradeInformationTag.ISSUER_IDENTIFICATION, issueInstituteID);
        map.put(TradeInformationTag.BANK_CARD_NUM, cardNo);
        map.put(TradeInformationTag.SCAN_CODE, scanCode);
        map.put(TradeInformationTag.CARD_SEQUENCE_NUMBER, cardSequenceNumber);
        map.put(TradeInformationTag.OPERATOR_CODE, operatorNo);
        map.put(TradeInformationTag.TRANSACTION_TYPE, transType);
        map.put(TradeInformationTag.DATE_EXPIRED, cardExpiredDate);
        map.put(TradeInformationTag.BATCH_NUMBER, batchNo);
        map.put(TradeInformationTag.TRACE_NUMBER, voucherNo);
        map.put(TradeInformationTag.SCAN_VOUCHER_NO, scanVoucherNo);
        map.put(TradeInformationTag.TRANS_DATE, transDate);
        map.put(TradeInformationTag.TRANS_TIME, transTime);
        map.put(TradeInformationTag.AUTHORIZATION_IDENTIFICATION, authorizeNo);
        map.put(TradeInformationTag.REFERENCE_NUMBER, referenceNo);
        map.put(TradeInformationTag.TRANS_MONEY, amount);
        map.put(TradeInformationTag.BANKCARD_ORGANIZATION, bankcardOganization);
        map.put(TradeInformationTag.TRANSFER_INTO_CARD, intoAccount);
        map.put(TradeInformationTag.SLIP_VERSION, printInfo);
        map.put(TradeInformationTag.SERVICE_ENTRY_MODE, serviceEntryMode);
        map.put(TradeInformationTag.CURRENCY_CODE, currencyCode);
        map.put(TradeInformationTag.IC_DATA, pbocOriTlvData);
        map.put(TransDataKey.key_oriVoucherNumber, oriVoucherNum);
        map.put(TransDataKey.key_oriAuthCode, oriAuthCode);
        map.put(TransDataKey.key_oriTransDate, oriTradeDate);
        map.put(TransDataKey.key_oriReferenceNumber, oriRefereceNum);
        map.put(TradeInformationTag.REVERSE_FIELD, reverseFieldInfo);
        map.put(TradeInformationTag.UNICOM_SCAN_TYPE, unicom_scna_type);
        map.put(TradeInformationTag.PHONE_NUMBER, mobile_phone_number);
        map.put(TradeInformationTag.TRANS_YEAR, transYear);
        map.put(TradeInformationTag.DATE_SETTLEMENT, settlmentDate);

        map.put(TransDataKey.KEY_ORI_TERMINAL_NO, oriTermNo);/*脱机退货新增加*/
        map.put(TransDataKey.KEY_ORI_BATCH_NO, oriBatchNo);/*脱机退货新增加*/

        map.put(TradeInformationTag.TRANS_STATUS_VALUE,String.valueOf(transStatus));

        map.put(TradeInformationTag.OFFLINE_TRANS_UPLOAD_STATUS,offlineTransUploadStatus);

        map.put(TradeInformationTag.ISO62_REQ,iso62Req);
        map.put(TradeInformationTag.ISO62_RES,iso62Res);

        map.put(TradeInformationTag.BALANC_AMOUNT,sBalance);

        map.put(TradeInformationTag.TRANS_STATE_FLAG,String.valueOf(stateFlag));/*重打印签购单时需要使用*/

        map.put(TradeInformationTag.ORIGINAL_AUTH_ORG_CODE,organizationCode);
        map.put(TradeInformationTag.ORIGINAL_AUTH_MODE,originalAuthMode);
        map.put(TradeInformationTag.TEMPLATE_ID,templateId);
        map.put(TradeInformationTag.SETTLEMENT_INFO,settlementInfo);

        if (pbocDetail != null)
            map.putAll(pbocDetail.convert2Map());
        return map;
    }

    @Override
    public String toString() {
        return "TradeInfoRecord{" +
                "merchantName='" + merchantName + '\'' +
                ", merchantNo='" + merchantNo + '\'' +
                ", terminalNo='" + terminalNo + '\'' +
                ", acquireInstituteID='" + acquireInstituteID + '\'' +
                ", issueInstituteID='" + issueInstituteID + '\'' +
                ", cardNo='" + cardNo + '\'' +
                ", scanCode='" + scanCode + '\'' +
                ", cardSequenceNumber='" + cardSequenceNumber + '\'' +
                ", operatorNo='" + operatorNo + '\'' +
                ", transType='" + transType + '\'' +
                ", cardExpiredDate='" + cardExpiredDate + '\'' +
                ", batchNo='" + batchNo + '\'' +
                ", voucherNo='" + voucherNo + '\'' +
                ", scanVoucherNo='" + scanVoucherNo + '\'' +
                ", transDate='" + transDate + '\'' +
                ", transTime='" + transTime + '\'' +
                ", authorizeNo='" + authorizeNo + '\'' +
                ", referenceNo='" + referenceNo + '\'' +
                ", amount='" + amount + '\'' +
                ", bankcardOganization='" + bankcardOganization + '\'' +
                ", intoAccount='" + intoAccount + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                ", serviceEntryMode='" + serviceEntryMode + '\'' +
                ", printInfo='" + printInfo + '\'' +
                ", oriVoucherNum='" + oriVoucherNum + '\'' +
                ", oriAuthCode='" + oriAuthCode + '\'' +
                ", oriTradeDate='" + oriTradeDate + '\'' +
                ", oriRefereceNum='" + oriRefereceNum + '\'' +
                ", reverseFieldInfo='" + reverseFieldInfo + '\'' +
                ", pbocOriTlvData='" + pbocOriTlvData + '\'' +
                ", pbocDetail=" + pbocDetail +
                ", stateFlag=" + stateFlag +
                ", sendCount=" + sendCount +
                ", isBatchSuccess=" + isBatchSuccess +
                ", unicom_scna_type='" + unicom_scna_type + '\'' +
                ", mobile_phone_number='" + mobile_phone_number + '\'' +
                ", transYear='" + transYear + '\'' +
                ", settlmentDate='" + settlmentDate + '\'' +
                ", oriTermNo='" + oriTermNo + '\'' +
                ", oriBatchNo='" + oriBatchNo + '\'' +
                ", iso62Req='" + iso62Req + '\'' +
                ", iso62Res='" + iso62Res + '\'' +
                ", sBalance='" + sBalance + '\'' +
                ", organizationCode='" + organizationCode + '\'' +
                ", originalAuthMode='" + originalAuthMode + '\'' +
                ", templateId='" + templateId + '\'' +
                ", settlementInfo='" + settlementInfo + '\'' +
                '}';
    }
}
