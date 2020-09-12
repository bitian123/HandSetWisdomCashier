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
import com.centerm.epos.task.AsyncSignTask;
import com.centerm.epos.transcation.pos.controller.ProcessRequestManager;
import com.centerm.epos.transcation.pos.controller.TradeParameterDownController;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by yuhc on 2017/4/1.
 */

public class SignInTrade implements ManageTransaction {
    @Override
    public void execute(final ITradeView tradeView, final BaseTradePresent tradePresent) {
        new AsyncSignTask(tradeView.getContext(), tradePresent.getTransData(), null) {
            @Override
            public void onFinish(String[] strings) {
                super.onFinish(strings);
                tradePresent.putResponseCode(strings[0], strings[1]);
                if ("00".equals(strings[0])) {
                    //签到完成，紧接着进行非接参数下载
//                    executeDownloadQps(true);
                    tradeView.popToast(R.string.tip_sign_in_success);

                    if (!TransCode.SIGN_IN.equals(tradePresent.getTradeCode())) {
                        //其它业务中激活的签到，需要继续之前的业务
                        if (tradeView instanceof ITradingView) {
                            ((ITradingView) tradeView).updateHint(R.string.tip_trading_default);
                            EventBus.getDefault().post(new SimpleMessageEvent<>(TradeMessage.PRE_TASK_CONTINUE));
                            return;
                        }
                    }
                    int flag = Settings.getTradeParameterFlag();
                    if (flag != TradeParameterDownController.PARAMETER_DOWNLOAD_COMPLETE) {
                        if (TradeParameterDownController.getInstance(tradeView, tradePresent).switchToParameterDown())
                            return;
                    }
                    if (ProcessRequestManager.isExistProcessRequest()) {
                        ProcessRequestManager.clearProcessRequest(ProcessRequestManager.RESIGN_IN);
                        if (!TransCode.SIGN_IN.equals(tradePresent.getTradeCode())) {
                            if (tradeView instanceof ITradingView) {
                                ((ITradingView) tradeView).updateHint(R.string.tip_trading_default);
                                EventBus.getDefault().post(new SimpleMessageEvent<>(TradeMessage.PRE_TASK_CONTINUE));
                                return;
                            }
                        }
                    }
                    tradePresent.gotoNextStep("2");
                } else {
                    tradePresent.gotoNextStep();
                    tradeView.popToast(R.string.tip_sign_in_failed);
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
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tradePresent.getTradeCode());
    }
}
