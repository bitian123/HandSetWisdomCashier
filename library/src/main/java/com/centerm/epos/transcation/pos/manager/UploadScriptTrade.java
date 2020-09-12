package com.centerm.epos.transcation.pos.manager;

import android.os.AsyncTask;

import com.centerm.epos.EposApplication;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.event.SimpleMessageEvent;
import com.centerm.epos.event.TradeMessage;
import com.centerm.epos.fragment.trade.ITradingView;
import com.centerm.epos.task.AsyncUploadScriptTask;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;

import config.BusinessConfig;

/**
 * Created by yuhc on 2017/4/3.
 */

public class UploadScriptTrade implements ManageTransaction {
    @Override
    public void execute(final ITradeView tradeView, final BaseTradePresent tradePresent) {
        new UploadScript(tradeView, tradePresent.getTransData()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                tradePresent.getTradeCode());
    }

    public static class UploadScript extends AsyncUploadScriptTask {
        ITradeView tradeView;

        public UploadScript(ITradeView tradeView, Map<String, Object> dataMap) {
            super(tradeView.getContext(), dataMap);
            this.tradeView = tradeView;
        }

        @Override
        public void onProgress(Integer counts, Integer index) {
            if (!(tradeView instanceof ITradingView))
                return;
            ((ITradingView) tradeView).updateHint("上送IC卡脚本处理结果\n" + index + "/" + counts);
        }

        @Override
        public void onStart() {
            if (!(tradeView instanceof ITradingView))
                return;
            ((ITradingView) tradeView).updateHint("上送IC卡脚本处理结果");
        }

        @Override
        public void onFinish(String[] status) {
            if (!(tradeView instanceof ITradingView))
                return;
            /*
            *脚本上送完成重置脚本
            * */
            if( "00".equals(status[0]) ) {
                BusinessConfig.getInstance().setFlag(EposApplication.getAppContext(), BusinessConfig.Key.FLAG_NEED_UPLOAD_SCRIPT, false);
                ((ITradingView) tradeView).updateHint("脚本上送成功");
            }
            else if("01".equals(status[0])){ /*后台无应答*/
                BusinessConfig.getInstance().setFlag(EposApplication.getAppContext(), BusinessConfig.Key.FLAG_NEED_UPLOAD_SCRIPT, false);
                ((ITradingView) tradeView).updateHint("脚本上送失败");
            }
            else{
                ((ITradingView) tradeView).updateHint("脚本上送失败");
            }
            EventBus.getDefault().post(new SimpleMessageEvent<>("02".equals(status[0]) ? TradeMessage.PRE_TASK_COMM_TERMINATE: TradeMessage.PRE_TASK_CONTINUE));
        }
    }
}
