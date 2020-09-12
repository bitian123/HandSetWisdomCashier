package com.centerm.epos.printer;

import android.content.Context;

import com.centerm.epos.bean.TradeInfoRecord;

import org.json.JSONException;

import java.util.List;

/**
 * Created by ysd on 2017/6/14.
 * 除正常交易凭条外的其他打印接口
 */

public interface IPrintRransData {
    /*开启打印机设备*/
    public boolean open(Context context);

    /*设置打印回调接口*/
    public void setBatchListener(IPrinterCallBack callBack);

    /*打印交易明细数据*/
    public void printDetails(final List<TradeInfoRecord> tradeInfos);

    /*打印结算明细数据*/
    public void printBatchDetailData(List<TradeInfoRecord> tradeInfos);

    /*打印交易汇总数据*/
    public void printDataALLTrans(List<TradeInfoRecord> jiejiLis, List<TradeInfoRecord> daijiLis);

    /*打印交易汇总数据*/
    public void printDataALLTransEx(List<TradeInfoRecord> tradeInfos);

    /*打印结算数据*/
    public void printBatchTotalData(List<TradeInfoRecord> jiejiLis, List<TradeInfoRecord> daijiLis, boolean isPre);

    /*打印结算数据*/
    public void printBatchTotalDataEx(List<TradeInfoRecord> tradeInfos, boolean isPre);

    /*打印上批次汇总数据*/
    public void printBatchTotalData(String gson, boolean isPre) throws JSONException;

    /*打印上送失败数据*/
    public void printFailDetailData(List<TradeInfoRecord> rejuseItems, List<TradeInfoRecord> failItems);
}
