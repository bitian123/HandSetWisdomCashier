package com.centerm.epos.mvp.model;

import android.text.TextUtils;

import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.DbHelper;
import com.j256.ormlite.stmt.QueryBuilder;

import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

/**
 * author:wanliang527</br>
 * date:2017/3/2</br>
 */

public class InputOriginInfoBiz implements IInputOriginInfoBiz {

    private Logger logger = Logger.getLogger(this.getClass());

    @Override
    public TradeInfoRecord queryByPosSerial(DbHelper dbHelper, String posSerial) {
        CommonDao<TradeInfoRecord> dao = new CommonDao<>(TradeInfoRecord.class, dbHelper);
        QueryBuilder builder = dao.queryBuilder();
        List<TradeInfoRecord> tradeInfos = null;
        try {
            tradeInfos = builder.where().eq("voucherNo", posSerial).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (tradeInfos == null || tradeInfos.size() == 0) {
            return null;
        }
        return tradeInfos.get(0);
    }

    @Override
    public TradeInfoRecord queryByPosSerial(DbHelper dbHelper, String posSerial, String transType) {
        if (TextUtils.isEmpty(transType))
            return queryByPosSerial(dbHelper, posSerial);
        CommonDao<TradeInfoRecord> dao = new CommonDao<>(TradeInfoRecord.class, dbHelper);
        QueryBuilder builder = dao.queryBuilder();
        List<TradeInfoRecord> tradeInfos = null;
        try {
            tradeInfos = builder.where().eq("voucherNo", posSerial).and().eq("transType", transType).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (tradeInfos == null || tradeInfos.size() == 0) {
            return null;
        }
        return tradeInfos.get(0);
    }

    @Override
    public TradeInfoRecord queryByPosScanVoucherNo(DbHelper dbHelper, String scanVoucherNo) {
        CommonDao<TradeInfoRecord> dao = new CommonDao<>(TradeInfoRecord.class, dbHelper);
        QueryBuilder builder = dao.queryBuilder();
        List<TradeInfoRecord> tradeInfos = null;
        try {
            tradeInfos = builder.where().eq("scanVoucherNo", scanVoucherNo).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (tradeInfos == null || tradeInfos.size() == 0) {
            return null;
        }
        return tradeInfos.get(0);
    }
}
