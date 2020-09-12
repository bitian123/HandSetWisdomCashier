package com.centerm.epos.task;

import android.content.Context;

import com.centerm.epos.bean.TradeInfo;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.db.CommonManager;
import com.centerm.epos.redevelop.ICommonManager;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by ysd on 2016/12/20.
 */

public abstract class AsyncBatchSettleDown extends BaseAsyncTask {
    private ICommonManager commonManager;
    public AsyncBatchSettleDown(Context context) {
        super(context);
        commonManager = (ICommonManager) ConfigureManager.getInstance(context).getSubPrjClassInstance(new CommonManager());
    }

    @Override
    protected Object doInBackground(Object[] params) {
//        sleep(LONG_SLEEP);
        List<TradeInfoRecord> tradeInfos = null;
        try {
            tradeInfos = commonManager.getBatchList();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (null != tradeInfos && tradeInfos.size() > 0) {
            return true;
        } else {
            return false;
        }
    }
}
