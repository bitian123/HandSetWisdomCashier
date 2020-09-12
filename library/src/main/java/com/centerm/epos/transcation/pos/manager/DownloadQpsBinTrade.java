package com.centerm.epos.transcation.pos.manager;

import android.os.AsyncTask;

import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.fragment.trade.ITradingView;
import com.centerm.epos.task.AsyncDownloadQPSCardBinTask;
import com.centerm.epos.transcation.pos.controller.TradeParameterDownController;

import static com.centerm.epos.common.TransDataKey.iso_f39;

/**
 * Created by yuhc on 2017/4/3.
 */

public class DownloadQpsBinTrade implements ManageTransaction {
    @Override
    public void execute(final ITradeView tradeView, final BaseTradePresent tradePresent) {
        new AsyncDownloadQPSCardBinTask(tradeView.getHostActivity(), tradePresent.getTransData()) {

            @Override
            public void onProgress(Integer counts, Integer index) {
                if (!(tradeView instanceof ITradingView))
                    return;
                ITradingView tradingView = (ITradingView) tradeView;
                switch (index) {
                    case -1:
                        tradingView.updateHint("下载完成，正在结束下载");
                        break;
                    case -2:
                        tradingView.updateHint("无卡BIN信息需要更新");
                        break;
                    default:
                        tradingView.updateHint("正在下载卡BIN B表信息\n起始号" + index);
                        break;
                }
            }

            @Override
            public void onStart() {
                if (tradeView instanceof ITradingView)
                    ((ITradingView) tradeView).updateHint("正在下载卡BIN信息");
            }

            @Override
            public void onFinish(String[] status) {
                int flag = Settings.getTradeParameterFlag();
                if (TransCode.SIGN_IN.equals(tradePresent.getTradeCode()) && flag != TradeParameterDownController
                        .PARAMETER_DOWNLOAD_COMPLETE) {
                    TradeParameterDownController controller = TradeParameterDownController.getInstance(tradeView,
                            tradePresent);
                    controller.modityParameterFlag(TradeParameterDownController.QPS_BIN_B_DOWNLOAD_OVER);
                    if (controller.switchToParameterDown())
                        return;
                }

                tradePresent.getTempData().put(iso_f39, status[0]);
                tradePresent.putResponseCode(status[0], status[1]);
                if ("00".equals(status[0])) {
                    tradePresent.gotoNextStep("2");
                    tradeView.popToast("免密新增BIN表B下载成功");
                } else {
                    tradePresent.gotoNextStep();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tradePresent.getTradeCode());
    }
}
