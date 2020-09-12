package com.centerm.epos.transcation.pos.manager;

import android.os.AsyncTask;

import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.fragment.trade.TradingFragment;
import com.centerm.epos.task.AsyncUploadOfflineTask;

/**
 * Created by zhouzhihua on 2017/4/3.
 */

public class UploadOfflineTrade implements ManageTransaction {
    @Override
    public void execute(final ITradeView tradeView, final BaseTradePresent tradePresent) {
        new UploadOffline(tradeView, tradePresent).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tradePresent
                .getTradeCode());
    }

    public static class UploadOffline extends AsyncUploadOfflineTask {
        private ITradeView tradeView;
        private BaseTradePresent tradePresent;

        /**
         * 默认上送未上送的交易
         * @param tradeView
         * @param tradePresent
         */
        public UploadOffline(ITradeView tradeView, BaseTradePresent tradePresent) {
            super(tradeView.getContext(), tradePresent.getTransData() , 0 );
            this.tradeView = tradeView;
            this.tradePresent = tradePresent;
        }
        public UploadOffline(ITradeView tradeView, BaseTradePresent tradePresent,int transFag) {
            super(tradeView.getContext(), tradePresent.getTransData(),transFag);
            this.tradeView = tradeView;
            this.tradePresent = tradePresent;
        }

        @Override
        public void onStart() {
            if (!(tradeView instanceof TradingFragment))
                return;
            ((TradingFragment) tradeView).updateHint("上送离线交易");
        }

        @Override
        public void onProgress(Integer counts, Integer index) {
            if (!(tradeView instanceof TradingFragment))
                return;
            TradingFragment tradingView = (TradingFragment) tradeView;
            tradingView.updateHint("正在上送离线交易(" + index + "/" + counts + ")");
        }

        @Override
        public void onFinish(String[] status) {
            if (!(tradeView instanceof TradingFragment))
                return;
            if ("00".equals(status[0])) {
                ((TradingFragment) tradeView).updateHint("上送完成");
            } else {
                ((TradingFragment) tradeView).updateHint("上送失败");
            }
            tradePresent.onTransactionQuit();
        }
    }
}
