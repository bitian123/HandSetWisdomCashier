package com.centerm.epos.task;

import android.content.Context;
import android.os.AsyncTask;

import com.centerm.epos.bean.TradeInfo;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.db.CommonManager;
import com.centerm.epos.redevelop.ICommonManager;

import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ysd on 2016/12/20.
 */

public abstract class AsyncQueryPrintDataTask extends AsyncTask<Void, Integer, List<List<TradeInfoRecord>>> implements
        CheckBillkListener {
    protected Logger logger = Logger.getLogger(this.getClass());
    private final ICommonManager commonManager;
    protected final static long LONG_SLEEP = 1000;
    public AsyncQueryPrintDataTask(Context context) {
//        commonManager = new CommonManager(TradeInfoRecord.class, context);
        commonManager = (ICommonManager) ConfigureManager.getInstance(context).getSubPrjClassInstance(new CommonManager());
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        onStart();
    }

    @Override
    protected List<List<TradeInfoRecord>> doInBackground(Void... params) {
        sleep(LONG_SLEEP);
        List<TradeInfoRecord> jiejiList = new ArrayList<>();
        List<TradeInfoRecord> daijiList = new ArrayList<>();
        List<TradeInfoRecord> saleDetailList = new ArrayList<>();
        List<TradeInfoRecord> rejestList = new ArrayList<>();
        List<TradeInfoRecord> failList = new ArrayList<>();
        List<TradeInfoRecord> batchDetail = new ArrayList<>();
        List<TradeInfoRecord> scanSaleList = new ArrayList<>();
        List<TradeInfoRecord> scanVoidList = new ArrayList<>();
        List<TradeInfoRecord> scanRefundList = new ArrayList<>();
        List<List<TradeInfoRecord>> lists = new ArrayList<>();
        try {
            jiejiList = commonManager.getDebitList();
            daijiList = commonManager.getCreditList();
            saleDetailList = commonManager.getTransDetail();
            rejestList = commonManager.getRefusedList();
            failList = commonManager.getFailList();
            batchDetail = commonManager.getBatchList();
            scanSaleList = commonManager.getScanSaleList();
            scanVoidList = commonManager.getScanVoidList();
            scanRefundList = commonManager.getScanRefundList();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        lists.add(jiejiList);
        lists.add(daijiList);
        lists.add(saleDetailList);
        lists.add(rejestList);
        lists.add(failList);
        lists.add(batchDetail);
        lists.add(scanSaleList);
        lists.add(scanVoidList);
        lists.add(scanRefundList);
        return lists;
    }
    protected void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onPostExecute(List<List<TradeInfoRecord>> lists) {
        super.onPostExecute(lists);
        onFinish(lists);
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onFinish(List<List<TradeInfoRecord>> lists) {

    }
}
