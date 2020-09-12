package com.centerm.epos.transcation.pos.manager;


import android.os.AsyncTask;

import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.event.SimpleMessageEvent;
import com.centerm.epos.event.TradeMessage;
import com.centerm.epos.event.UploadTradeMessage;
import com.centerm.epos.fragment.trade.ITradingView;
import com.centerm.epos.utils.XLogUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Map;

import config.BusinessConfig;

/**
 * Created by yuhc on 2017/9/12.
 * 结算前上送的交易：签名、脱机业务、脚本通知等
 */

public class UploadTradeBeforeSettlementTrade implements ManageTransaction {
    private static final String TAG = UploadTradeBeforeSettlementTrade.class.getSimpleName();
    private ITradeView tradeView;
    private BaseTradePresent tradePresent;

    public UploadTradeBeforeSettlementTrade() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void execute(ITradeView tradeView, BaseTradePresent tradePresent) {
        this.tradeView = tradeView;
        this.tradePresent = tradePresent;
        EventBus.getDefault().post(new UploadTradeMessage(UploadTradeMessage.UPLOAD_START));
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleMessage(UploadTradeMessage event) {
        XLogUtil.d(TAG, "^_^ EVENT  what:" + event.getWhat() + " ^_^");
        switch (event.getWhat()) {
            case UploadTradeMessage.UPLOAD_START:
                EventBus.getDefault().post(new UploadTradeMessage(UploadTradeMessage.UPLOAD_OFFLINE_TRANS));
                break;
            case UploadTradeMessage.UPLOAD_OFFLINE_TRANS:
                if( UploadOfflineTradeChecker.isOfflineTransExist() ){
                    new UploadOfflineTrans(tradeView, tradePresent,0).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, TransCode.OFFLINE_UPLOAD);
                }
                else{
                    EventBus.getDefault().post(new UploadTradeMessage(UploadTradeMessage.UPLOAD_SCRIPT_RESULT));
                }
                break;
            case UploadTradeMessage.UPLOAD_SCRIPT_RESULT:
                //上传脚本结果通知
                if (UploadScriptTradeChecker.isScriptExist()) {
                    new UploadScript(tradeView, tradePresent.getTransData()).executeOnExecutor(AsyncTask
                            .THREAD_POOL_EXECUTOR, TransCode.UPLOAD_SCRIPT_RESULT);
                } else
                    EventBus.getDefault().post(new UploadTradeMessage(UploadTradeMessage.UPLOAD_ESIGN));
                break;
            case UploadTradeMessage.UPLOAD_ESIGN:
                if (UploadESignatureTradeChecker.isEsignPicExist()) {
                    UploadESignature uploadESignature = new UploadESignature(tradeView, tradePresent);
                    uploadESignature.setUploadAll(true);
                    uploadESignature.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, TransCode.UPLOAD_ESIGN);
                } else
                    EventBus.getDefault().post(new UploadTradeMessage(UploadTradeMessage.UPLOAD_END));
                break;
            case UploadTradeMessage.UPLOAD_END:
                EventBus.getDefault().unregister(this);
                //清空交易数据，避免影响批结算交易
                tradePresent.getTransData().clear();
                EventBus.getDefault().post(new SimpleMessageEvent<>(TradeMessage.PRE_TASK_CONTINUE));
                break;
        }
    }
    private class UploadOfflineTrans extends UploadOfflineTrade.UploadOffline{

        public UploadOfflineTrans(ITradeView tradeView, BaseTradePresent tradePresent,int transFag)
        {
            super(tradeView, tradePresent, transFag);
        }

        @Override
        public void onFinish(String[] status) {
            if (!(tradeView instanceof ITradingView))
                return;
            if ("00".equals(status[0])) {
                ((ITradingView) tradeView).updateHint("上送完成");
            } else {
                ((ITradingView) tradeView).updateHint("上送失败");
            }
            EventBus.getDefault().post(new UploadTradeMessage(UploadTradeMessage.UPLOAD_SCRIPT_RESULT));
        }
    }

    /**
     * 上送脚本结果通知
     */
    private class UploadScript extends UploadScriptTrade.UploadScript {

        public UploadScript(ITradeView tradeView, Map<String, Object> dataMap) {
            super(tradeView, dataMap);
        }

        @Override
        public void onFinish(String[] status) {
            if (!(tradeView instanceof ITradingView))
                return;
            ((ITradingView) tradeView).updateHint("上送完成");
            EventBus.getDefault().post(new UploadTradeMessage(UploadTradeMessage.UPLOAD_ESIGN));
        }
    }

    private class UploadESignature extends UploadESignatureTrade.UploadESignature {

        public UploadESignature(ITradeView tradeView, BaseTradePresent tradePresent) {
            super(tradeView, tradePresent);
        }

        @Override
        public void onFinish(String[] status) {
            if (!(tradeView instanceof ITradingView))
                return;
            if ("00".equals(status[0])) {
                ((ITradingView) tradeView).updateHint("上送完成");
            } else {
                ((ITradingView) tradeView).updateHint("上送失败");
            }
            BusinessConfig.getInstance().setFlag(tradeView.getContext(), BusinessConfig.Key
                    .FLAG_ESIGN_STORAGE_WARNING, false);
            EventBus.getDefault().post(new UploadTradeMessage(UploadTradeMessage.UPLOAD_END));
        }
    }
}
