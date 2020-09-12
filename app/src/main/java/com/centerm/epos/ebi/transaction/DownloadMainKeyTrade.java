package com.centerm.epos.ebi.transaction;

import android.os.AsyncTask;

import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.ebi.R;
import com.centerm.epos.ebi.task.DownloadMainKeyTask;
import com.centerm.epos.fragment.trade.ITradingView;
import com.centerm.epos.transcation.pos.manager.ManageTransaction;

/**
 * Created by liubit on 2017/12/26.
 * 下载签名主密钥交易
 */

public class DownloadMainKeyTrade implements ManageTransaction {
    @Override
    public void execute(final ITradeView tradeView, final BaseTradePresent tradePresent) {
        new DownloadMainKeyTask(tradeView.getContext(), tradePresent.getTransData(), null) {
            @Override
            public void onFinish(String[] strings) {
                super.onFinish(strings);
                tradePresent.putResponseCode(strings[0], strings[1]);
                if ("00".equals(strings[0])) {
                    tradeView.popToast(R.string.tip_download_mainkey_success);
                    tradePresent.gotoNextStep("2");
                } else {
                    tradePresent.gotoNextStep();
                    tradeView.popToast(R.string.tip_download_mainkey_failed);
                }
            }


            @Override
            public void onStart() {
                super.onStart();
                if (tradeView instanceof ITradingView)
                    ((ITradingView) tradeView).updateHint("下载签名密钥中，请稍候...");
            }

            @Override
            public void onProgress(Integer counts, Integer index) {
                super.onProgress(counts, index);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,tradePresent.getTradeCode());
    }
}
