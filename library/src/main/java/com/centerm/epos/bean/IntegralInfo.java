package com.centerm.epos.bean;

import com.centerm.epos.EposApplication;

import java.util.Locale;

import config.BusinessConfig;

/**
 * Created by zhouzhihua on 2018/1/3.
 * 积分类交易
 */

public class IntegralInfo {
    private String goodsCode;//商品代码 30
    private String exchangePoints; //兑换积分分数  10
    private String outstandingAmount; //自付金额 12
    private String reserveUse; // ans 50 max Bytes 保留给积分兑换方式使用

    public IntegralInfo(String req , String res) {
        if( req != null ) {
            goodsCode = req.trim();
        }
        if( ( res != null  ) && ( res.length() >= 10) ){
            exchangePoints = res.substring( 0 , 10);
        }
        if( ( res != null  ) && ( res.length() >= 22) ){
            outstandingAmount = res.substring( 10 , 22);
        }
        if( ( res != null  ) && ( res.length() > 22) ){
            reserveUse = res.substring( 22 , res.length() );
        }
    }

    public String getGoodsCode(){
        return goodsCode;
    }

    public String getExchangePoints(){
        return exchangePoints;
    }

    public String getOutstandingAmount(){
        return outstandingAmount;
    }

    public String getReserveUse(){
        return reserveUse;
    }

    public String getExchangePointsAmountFormat(){
        String exchangePoints = "0.00";
        if(this.exchangePoints !=null && this.exchangePoints.length() == 10 ){
            long l = 0;
            try{
                l = Long.parseLong(this.exchangePoints);
            }
            catch(Exception e){
                System.out.println("getExchangePointsAmountFormat error");
            }
            exchangePoints = String.format(Locale.CHINA,"%d.%02d", l/100,l%100);
        }
        return exchangePoints;
    }

    public String getOutstandingAmountFormat(){
        String outstandingAmount = "0.00";
        if(this.outstandingAmount !=null && this.outstandingAmount.length() == 12 ){
            long l = 0;
            try{
                l = Long.parseLong(this.outstandingAmount);
            }
            catch(Exception e){
                System.out.println("getExchangePointsAmountFormat error");
            }
            outstandingAmount = String.format(Locale.CHINA,"%d.%02d", l/100,l%100);
        }
        return outstandingAmount;
    }
//    兑换积分
//    Exchange Points
//    积分余额
//    Points Banlance
//    自付金额
//    Outstanding Amount

    public String getIntegralPrinterData(){
        boolean bIsSupportEnglish = BusinessConfig.getInstance().getToggle(EposApplication.getAppContext(),BusinessConfig.Key.TOGGLE_SLIP_ENGLISH);
        String print = "\n";
        if(bIsSupportEnglish){
            if( getGoodsCode() != null ){
                print += ("商品代码:"+getGoodsCode());
            }
            if(getExchangePoints()!=null){
                print += ("\n"+"兑换积分(Exchange Points):"+getExchangePointsAmountFormat());
            }
            if(getOutstandingAmount()!=null){
                print += ("\n"+"自付金金额(Outstanding Amount):"+getOutstandingAmountFormat());
            }
        }
        else{
            if( getGoodsCode() != null ){
                print += ("商品代码:"+getGoodsCode());
            }
            if(getExchangePoints()!=null){
                print += ("\n"+"兑换积分:"+getExchangePointsAmountFormat());
            }
            if(getOutstandingAmount()!=null){
                print += ("\n"+"自付金金额:"+getOutstandingAmountFormat());
            }
        }
        return print;
    }

}
