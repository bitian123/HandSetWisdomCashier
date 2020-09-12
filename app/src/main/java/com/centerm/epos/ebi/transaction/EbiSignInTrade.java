package com.centerm.epos.ebi.transaction;

import android.os.AsyncTask;

import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.ebi.task.EbiEbiAsyncSignTask;
import com.centerm.epos.event.SimpleMessageEvent;
import com.centerm.epos.event.TradeMessage;
import com.centerm.epos.fragment.trade.ITradingView;
import com.centerm.epos.fragment.trade.TradingFragment;
import com.centerm.epos.task.AsyncDownloadTerminalParamTask;
import com.centerm.epos.transcation.pos.controller.ProcessRequestManager;
import com.centerm.epos.transcation.pos.controller.TradeParameterDownController;
import com.centerm.epos.transcation.pos.manager.ManageTransaction;

import org.greenrobot.eventbus.EventBus;

import static com.centerm.epos.common.TransDataKey.iso_f39;

/**
 * Created by yuhc on 2017/4/1.
 * 签到交易
 *
 */

public class EbiSignInTrade implements ManageTransaction {
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

                new EbiEbiAsyncSignTask(tradeView.getContext(), tradePresent.getTransData(), null) {
                    @Override
                    public void onFinish(String[] strings) {
                        super.onFinish(strings);
                        tradePresent.putResponseCode(strings[0], strings[1]);
                        if ("00".equals(strings[0])) {
                            //签到完成，紧接着进行非接参数下载
//                    executeDownloadQps(true);
                            tradeView.popToast(com.centerm.epos.R.string.tip_sign_in_success);

                            if (!TransCode.SIGN_IN.equals(tradePresent.getTradeCode())){
                                //其它业务中激活的签到，需要继续之前的业务
                                if (tradeView instanceof TradingFragment) {
                                    ((TradingFragment) tradeView).updateHint(com.centerm.epos.R.string.tip_trading_default);
                                    EventBus.getDefault().post(new SimpleMessageEvent<>(TradeMessage.PRE_TASK_CONTINUE));
                                    return;
                                }
                            }
                            int flag = Settings.getTradeParameterFlag();
                            flag = 100;
                            if (flag != TradeParameterDownController.PARAMETER_DOWNLOAD_COMPLETE) {
                                if (TradeParameterDownController.getInstance(tradeView,tradePresent).switchToParameterDown())
                                    return;
                            }
                            if (ProcessRequestManager.isExistProcessRequest()){
                                ProcessRequestManager.clearProcessRequest(ProcessRequestManager.RESIGN_IN);
                                if (!TransCode.SIGN_IN.equals(tradePresent.getTradeCode())) {
                                    if (tradeView instanceof TradingFragment) {
                                        ((TradingFragment) tradeView).updateHint(com.centerm.epos.R.string.tip_trading_default);
                                        EventBus.getDefault().post(new SimpleMessageEvent<>(TradeMessage.PRE_TASK_CONTINUE));
                                        return;
                                    }
                                }
                            }
                            tradePresent.gotoNextStep("2");
                        } else {
                            tradePresent.gotoNextStep();
                            tradeView.popToast(com.centerm.epos.R.string.tip_sign_in_failed);
                        }
                    }


                    @Override
                    public void onStart() {
                        super.onStart();
                        if (tradeView instanceof ITradingView)
                            ((ITradingView) tradeView).updateHint("签到中，请稍候...");
                    }

                    @Override
                    public void onProgress(Integer counts, Integer index) {
                        super.onProgress(counts, index);
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,tradePresent.getTradeCode());

            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tradePresent.getTradeCode());

    }
}
