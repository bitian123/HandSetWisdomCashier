package com.centerm.epos.transcation.pos.manager;

import android.os.AsyncTask;

import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.fragment.trade.ITradingView;
import com.centerm.epos.task.AsyncQueryScanPayTask;

import static com.centerm.epos.common.TransDataKey.iso_f39;

/**
 * Created by yuhc on 2017/4/3.
 */

public class QueryScanResultTrade implements ManageTransaction {
    @Override
    public void execute(final ITradeView tradeView, final BaseTradePresent tradePresent) {
        new AsyncQueryScanPayTask(tradeView.getHostActivity(), tradePresent.getTransData()) {

            @Override
            public void onStart() {
                if (tradeView instanceof ITradingView)
                    ((ITradingView) tradeView).updateHint("正在查询扫码支付结果...");
            }

            @Override
            public void onFinish(String[] status) {
                tradePresent.getTempData().put(iso_f39, status[0]);
                tradePresent.putResponseCode(status[0], status[1]);
                tradePresent.gotoNextStep();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tradePresent.getTradeCode());
    }
}
