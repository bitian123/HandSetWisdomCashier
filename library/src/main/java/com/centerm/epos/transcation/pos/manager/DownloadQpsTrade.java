package com.centerm.epos.transcation.pos.manager;

import android.os.AsyncTask;

import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.fragment.trade.ITradingView;
import com.centerm.epos.task.AsyncDownloadQpsTask;
import com.centerm.epos.transcation.pos.controller.TradeParameterDownController;

import static com.centerm.epos.common.TransDataKey.iso_f39;

/**
 * Created by yuhc on 2017/4/3.
 */

public class DownloadQpsTrade implements ManageTransaction {
    @Override
    public void execute(final ITradeView tradeView, final BaseTradePresent tradePresent) {
        new AsyncDownloadQpsTask(tradeView.getHostActivity(), tradePresent.getTransData()) {

            @Override
            public void onProgress(Integer counts, Integer index) {
                if (index == -1) {
                    if (tradeView instanceof ITradingView)
                        ((ITradingView) tradeView).updateHint("下载完成，正在结束下载");
                }
            }

            @Override
            public void onStart() {
                if (tradeView instanceof ITradingView)
                    ((ITradingView) tradeView).updateHint("正在下载非接参数");
            }

            @Override
            public void onFinish(String[] status) {
                if ("00".equals(status[0]))
                    tradeView.getHostActivity().resetQpsConditionFlags();
                int flag = Settings.getTradeParameterFlag();
                if (TransCode.SIGN_IN.equals(tradePresent.getTradeCode()) && flag != TradeParameterDownController
                        .PARAMETER_DOWNLOAD_COMPLETE) {
                    TradeParameterDownController controller = TradeParameterDownController.getInstance(tradeView,
                            tradePresent);
                    controller.modityParameterFlag(TradeParameterDownController.QPS_PARAMETER_DOWNLOAD_OVER);
                    if (controller.switchToParameterDown())
                        return;
                }
                tradePresent.getTempData().put(iso_f39, status[0]);
                tradePresent.putResponseCode(status[0], status[1]);
                if ("00".equals(status[0])) {
                    tradePresent.gotoNextStep("2");
                    tradeView.popToast("非接业务参数下载成功");
                } else {
                    tradePresent.gotoNextStep();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tradePresent.getTradeCode());
    }
}
