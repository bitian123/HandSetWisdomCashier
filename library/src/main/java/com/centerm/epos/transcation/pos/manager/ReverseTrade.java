package com.centerm.epos.transcation.pos.manager;

import android.os.AsyncTask;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.common.StatusCode;
import com.centerm.epos.event.SimpleMessageEvent;
import com.centerm.epos.event.TradeMessage;
import com.centerm.epos.fragment.trade.TradingFragment;
import com.centerm.epos.task.AsyncAutoReverseTask;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by yuhc on 2017/4/3.
 */

public class ReverseTrade implements ManageTransaction {
    private boolean isAfterTrade = false;

    @Override
    public void execute(final ITradeView tradeView, final BaseTradePresent tradePresent) {
        new AsyncAutoReverseTask(tradeView.getHostActivity(), tradePresent.getTransData()) {
            @Override
            public void onProgress(Integer counts, Integer index) {
                super.onProgress(counts, index);
                if (!(tradeView instanceof TradingFragment))
                    return;
                TradingFragment tradingView = (TradingFragment) tradeView;
                if (index == -1) {
                    tradingView.updateHint("冲正中，请稍候...");
                } else if (index == -2) {
                    tradingView.updateHint("冲正已完成，开始交易");
                } else {
                    tradingView.updateHint("正在进行第" + index + "次冲正");
                }
            }

            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onFinish(String[] strings) {
                super.onFinish(strings);
                String code = strings[0];
                if (StatusCode.CONNECTION_EXCEPTION.getStatusCode().equals(code) ||
                        StatusCode.SOCKET_TIMEOUT.getStatusCode().equals(code) ||
                        StatusCode.UNKNOWN_REASON.getStatusCode().equals(code)) {
                    //以上情况认定为网络连接不上，也就没必要继续接下来的交易，直接返回结果
                    if (isAfterTrade){
                        tradeView.popToast(strings[1]);
                        tradePresent.onTransactionQuit();
                    }else {
                        tradePresent.putResponseCode(strings[0], strings[1]);
                        tradePresent.gotoNextStep("99");
                    }
                } else if("-1".equals(code)){//冲正接收失败，直接退出交易
                    tradeView.popToast(strings[1]);
                    tradePresent.onTransactionQuit();
                }else {
                    if (!(tradeView instanceof TradingFragment))
                        return;
                    if (isAfterTrade) {
                        tradeView.popToast("冲正成功");
                        tradePresent.onTransactionQuit();
                    }else {
                         ((TradingFragment) tradeView).updateHint(R.string.tip_trading_default);
                        EventBus.getDefault().post(new SimpleMessageEvent<>(TradeMessage.PRE_TASK_CONTINUE));
                    }
                }
            }
        }.setIsAfterTrade(isAfterTrade).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,tradePresent.getTradeCode());
    }

    public ReverseTrade setIsAfterTrade(boolean isAfterTrade){
        this.isAfterTrade = isAfterTrade;
        return this;
    }
}
