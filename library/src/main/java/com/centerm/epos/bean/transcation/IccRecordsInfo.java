package com.centerm.epos.bean.transcation;

import com.centerm.cpay.midsdk.dev.common.utils.Utils;
import com.centerm.cpay.midsdk.dev.define.pboc.CardTransLog;
import com.centerm.smartpos.util.HexUtil;

import java.io.Serializable;

/**
 * Created by zhouzhihua on 2017/12/11.
 */

public class IccRecordsInfo  implements Serializable {
    /*
        交易日期 3 bcd
        交易时间  3 bcd
        授权金额  6 bcd
        其他金额  6 bcd
        终端国家代码 2 bcd
        交易货币代码 2 bcd
        商户名称 20 ascii
        交易类型 1 bcd
        ATC     2 bcd
        */
    private String transDate = null;
    private String transTime = null;
    private String authAmt = null;
    private String otherAmt = null;
    private String termCountryCode = null;
    private String curCode = null;
    private String merchName = null;
    private String transType = null;
    private String atc = null;

    public IccRecordsInfo(CardTransLog cardTransLog){
        initRecordsData(cardTransLog);
    }
    private void initRecordsData(CardTransLog cardTransLog){
        setTransDate(cardTransLog.getTransDate());
        setTransTime(cardTransLog.getTransTime());
        setAuthAmt(cardTransLog.getAmt());
        setOtherAmt(cardTransLog.getOtheramt());
        setTermCountryCode(cardTransLog.getCountryCode());
        setCurCode(cardTransLog.getMoneyCode());
        setMerchName(cardTransLog.getMerchantName());
        setTransType(""+cardTransLog.getTranstype());
        setAtc(HexUtil.bcd2str(cardTransLog.getAppTransCount()));
    }

    public void setTransDate(String transDate){
        this.transDate = transDate;
    }
    public void setTransTime(String transTime){
        this.transTime = transTime;
    }
    public void setAuthAmt(String authAmt){
        this.authAmt = authAmt;
    }
    public void setOtherAmt(String otherAmt){
        this.otherAmt = otherAmt;
    }
    public void setTermCountryCode(String termCountryCode){
        this.termCountryCode = termCountryCode;
    }
    public void setCurCode(String curCode){
        this.curCode = curCode;
    }
    public void setMerchName(String merchName){
        this.merchName = merchName;
    }
    public void setTransType(String transType){
        this.transType = transType;
    }
    public void setAtc(String atc){
        this.atc = atc;
    }

    public String getTransDate(){
        return transDate;
    }
    public String getTransTime(){
        return transTime;
    }
    public String getAuthAmt(){
        return authAmt;
    }
    public String getOtherAmt(){
        return otherAmt;
    }
    public String getTermCountryCode(){
        return termCountryCode;
    }
    public String getCurCode(){
        return curCode;
    }
    public String getMerchName(){
        return merchName;
    }
    public String getTransType(){
        return transType;
    }
    public String getAtc(){
        return atc;
    }
}
