package com.centerm.epos.model;

import android.os.Bundle;
import android.text.TextUtils;

import com.centerm.epos.EposApplication;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.bean.iso.Iso62Qps;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.DbHelper;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by yuhc on 2017/4/23.
 */

public class TradeModelImpl implements ITradeModel {

    protected Logger logger = Logger.getLogger(this.getClass());
    private static TradeModelImpl tradeModel;
    ITradePresent mTradePresent;
    int resultCode;
    private Iso62Qps qpsParams;//小额免密免签参数
    Bundle bundleOfResult;
    private Bundle tradeParam;//内部执行业务传入的参数
    protected DbHelper dbHelper;
    private boolean isTradeNoPin = false;
    private boolean isTradeSlipNoSign = false;
    private String slipNoSignAmount;

    private TradeModelImpl(ITradePresent tradePresent) {
        mTradePresent = tradePresent;
        dbHelper = OpenHelperManager.getHelper(EposApplication.getAppContext(), DbHelper.class);
    }

    public synchronized static TradeModelImpl getInstance(ITradePresent tradePresent) {
        if (tradeModel == null) {
            tradeModel = new TradeModelImpl(tradePresent);
        }
        if (tradePresent != tradeModel.mTradePresent)
            tradeModel.mTradePresent = tradePresent;
        return tradeModel;
    }

    public static TradeModelImpl getInstance() {
        return getInstance(null);
    }

    public static void release(){
        tradeModel = null;
        OpenHelperManager.releaseHelper();
    }

    @Override
    public boolean isBundleOfResultExist() {
        return bundleOfResult != null;
    }

    @Override
    public void createBundleOfResult() {
        bundleOfResult = new Bundle();
    }

    @Override
    public boolean putDataToBundleOfResult(String key, Object value) {
        if (TextUtils.isEmpty(key) || bundleOfResult == null)
            return false;
        if (value == null)
            bundleOfResult.putString(key, null);
        else if (value instanceof String)
            bundleOfResult.putString(key, (String) value);
        else if (value instanceof Integer)
            bundleOfResult.putInt(key, (Integer) value);
        else if (value instanceof Bundle)
            bundleOfResult.putBundle(key, (Bundle) value);
        else
            return false;

        return true;
    }

    @Override
    public void setResultCode(int code) {
        resultCode = code;
    }

    @Override
    public int getResultCode() {
        return resultCode;
    }

    @Override
    public Bundle getResultBundle() {
        return bundleOfResult;
    }

    @Override
    public Iso62Qps getQpsParams() {
        if (qpsParams == null) {
            CommonDao<Iso62Qps> dao = new CommonDao<>(Iso62Qps.class, dbHelper);
            List<Iso62Qps> qpsList = dao.query();
            if (qpsList != null && qpsList.size() > 0) {
                qpsParams = qpsList.get(0);
                logger.warn("查询小额免密免签参数==>" + qpsParams.toString());
            } else {
                qpsParams = new Iso62Qps();
                logger.debug("未查询到小额免密免签参数==>可进行参数下载");
            }
        }
        return qpsParams;
    }

    public Iso62Qps getQpsParamsFormDb() {
        Iso62Qps qpsParams;
        CommonDao<Iso62Qps> dao = new CommonDao<>(Iso62Qps.class, dbHelper);
        List<Iso62Qps> qpsList = dao.query();
        if (qpsList != null && qpsList.size() > 0) {
            qpsParams = qpsList.get(0);
            logger.warn("查询小额免密免签参数==>" + qpsParams.toString());
        } else {
            qpsParams = new Iso62Qps();
            logger.debug("未查询到小额免密免签参数==>可进行参数下载");
        }

        return qpsParams;
    }

    @Override
    public void setQpsParams(Iso62Qps qpsParams) {
        this.qpsParams = qpsParams;
    }

    @Override
    public boolean isTradeNoPin() {
        return isTradeNoPin;
    }

    @Override
    public void setTradeNoPin(boolean noPin) {
        isTradeNoPin = noPin;
    }

    @Override
    public boolean isTradeSlipNoSign() {
        return isTradeSlipNoSign;
    }

    @Override
    public void setTradeSlipNoSign(boolean noSign) {
        isTradeSlipNoSign = noSign;
    }

    @Override
    public String getSlipNoSignAmount() {
        return slipNoSignAmount;
    }

    @Override
    public void setSlipNoSignAmount(String amount) {
        slipNoSignAmount = amount;
    }

    public Bundle getTradeParam() {
        return tradeParam;
    }

    public void setTradeParam(Bundle tradeParam) {
        this.tradeParam = tradeParam;
    }
}
