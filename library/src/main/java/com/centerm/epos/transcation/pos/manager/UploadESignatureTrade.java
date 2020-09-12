package com.centerm.epos.transcation.pos.manager;

import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.ArrayMap;

import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.fragment.trade.ITradingView;
import com.centerm.epos.task.AsyncUploadESignatureTask;
import com.centerm.epos.transcation.pos.constant.JsonKeyGT;
import com.centerm.epos.utils.DialogFactory;

import java.io.File;
import java.util.Map;

/**
 * Created by yuhc on 2017/4/3.
 */

public class UploadESignatureTrade implements ManageTransaction {

    @Override
    public void execute(final ITradeView tradeView, final BaseTradePresent tradePresent) {
        new UploadESignature(tradeView, tradePresent).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tradePresent
                .getTradeCode());
    }

    public static class UploadESignature extends AsyncUploadESignatureTask {
        private ITradeView tradeView;
        private BaseTradePresent tradePresent;
        private File eSignFile;
        private boolean exit = true;

        public UploadESignature(ITradeView tradeView, BaseTradePresent tradePresent) {
            super(tradeView.getContext(), tradePresent.getTransData());
            this.tradeView = tradeView;
            this.tradePresent = tradePresent;
        }

        public UploadESignature(ITradeView tradeView, BaseTradePresent tradePresent, File file, boolean e) {
            super(tradeView.getContext(), tradePresent.getTransData(),file);
            this.tradeView = tradeView;
            this.tradePresent = tradePresent;
            this.eSignFile = file;
            this.exit = e;
        }

        @Override
        public void onStart() {
            if (!(tradeView instanceof ITradingView))
                return;
            ((ITradingView) tradeView).updateHint("上送电子签名");
        }

        @Override
        public void onProgress(Integer counts, Integer index) {
            if (!(tradeView instanceof ITradingView))
                return;
            ITradingView tradingView = (ITradingView) tradeView;
            tradingView.updateHint("正在上送电子签名(" + index + "/" + counts + ")");
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onFinish(String[] status) {
            if (!(tradeView instanceof ITradingView)){
                logger.debug("tradeView not instanceof ITradingView");
                //交易结果页 点击继续刷卡 上送的电子签名
                if(eSignFile!=null){
                    DialogFactory.hideAll();
                    if(exit){//退出交易
                        tradePresent.onTransactionQuit();
                    }else {//继续刷卡
                        tradeView.getHostActivity().mTradeInformation.getRespDataMap().clear();
                        Map<String, Object> map = new ArrayMap<>();
                        map.put(JsonKeyGT.idType, tradePresent.getTransData().get(JsonKeyGT.idType));
                        map.put(JsonKeyGT.name, tradePresent.getTransData().get(JsonKeyGT.name));
                        map.put(JsonKeyGT.idNo, tradePresent.getTransData().get(JsonKeyGT.idNo));
                        map.put(JsonKeyGT.termSn, tradePresent.getTransData().get(JsonKeyGT.termSn));
                        map.put(JsonKeyGT.isOther, tradePresent.getTransData().get(JsonKeyGT.isOther));
                        tradePresent.getTransData().clear();
                        tradePresent.getTempData().clear();
                        tradePresent.getTransData().putAll(map);
                        tradePresent.gotoNextStep("2");
                    }

                }
                return;
            }
            if ("00".equals(status[0])) {
                ((ITradingView) tradeView).updateHint("上送完成");
            } else {
                ((ITradingView) tradeView).updateHint("上送失败");
            }
            tradePresent.onTransactionQuit();
        }
    }
}
