package com.centerm.epos.transcation.pos.manager;

import android.os.AsyncTask;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.event.SimpleMessageEvent;
import com.centerm.epos.event.TradeMessage;
import com.centerm.epos.fragment.trade.ITradingView;
import com.centerm.epos.task.AsyncDownloadTerminalParamTask;
import com.centerm.epos.transcation.pos.controller.ProcessRequestManager;
import com.centerm.epos.transcation.pos.controller.TradeParameterDownController;

import org.greenrobot.eventbus.EventBus;

import static com.centerm.epos.common.TransDataKey.iso_f39;

/**
 * Created by yuhc on 2017/4/3.
 */

public class DownloadParameterTrade implements ManageTransaction {
    @Override
    public void execute(final ITradeView tradeView, final BaseTradePresent tradePresent) {
        new AsyncDownloadTerminalParamTask(tradeView.getHostActivity(), tradePresent.getTransData()) {

            @Override
            public void onProgress(Integer counts, Integer index) {
                if (!(tradeView instanceof ITradingView))
                    return;
                ITradingView tradingView = (ITradingView) tradeView;
                switch (index) {
                    case -1:
                        tradingView.updateHint("下载完成，正在结束下载");
                        break;
                    default:
                        tradingView.updateHint("正在下载参数");
                        break;
                }
            }

            @Override
            public void onStart() {
                if (tradeView instanceof ITradingView)
                    ((ITradingView) tradeView).updateHint("正在获取终端参数信息");
            }

            @Override
            public void onFinish(String[] status) {
                if ("00".equals(status[0]) && ProcessRequestManager.isExistProcessRequest()) {
                    ProcessRequestManager.clearProcessRequest(ProcessRequestManager.UPDATE_TERMINAL_PARAM);
                }
                int flag = Settings.getTradeParameterFlag();
                if (TransCode.SIGN_IN.equals(tradePresent.getTradeCode()) && flag != TradeParameterDownController
                        .PARAMETER_DOWNLOAD_COMPLETE) {
                    TradeParameterDownController controller = TradeParameterDownController.getInstance(tradeView,
                            tradePresent);
                    controller.modityParameterFlag(TradeParameterDownController.TERMINAL_PARAM_DOWNLOAD_OVER);
                    if (controller.switchToParameterDown())
                        return;
                }
                if ("00".equals(status[0]) && ProcessRequestManager.isExistProcessRequest()) {
                    ProcessRequestManager.clearProcessRequest(ProcessRequestManager.UPDATE_TERMINAL_PARAM);
                    if (!TransCode.DOWNLOAD_TERMINAL_PARAMETER.equals(tradePresent.getTradeCode())) {
                        if (tradeView instanceof ITradingView) {
                            ((ITradingView) tradeView).updateHint(R.string.tip_trading_default);
                            EventBus.getDefault().post(new SimpleMessageEvent<>(TradeMessage.PRE_TASK_CONTINUE));
                            return;
                        }
                    }
                }
                tradePresent.getTempData().put(iso_f39, status[0]);
                tradePresent.putResponseCode(status[0], status[1]);
                if ("00".equals(status[0])) {
                    tradePresent.gotoNextStep("2");
                    tradeView.popToast("终端参数下载成功");
                } else {
                    tradePresent.gotoNextStep();
                }

            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tradePresent.getTradeCode());
    }
}
