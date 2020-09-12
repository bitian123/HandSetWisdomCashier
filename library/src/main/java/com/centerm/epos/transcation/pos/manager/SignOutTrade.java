package com.centerm.epos.transcation.pos.manager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.common.Settings;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.fragment.trade.ITradingView;
import com.centerm.epos.model.TradeModelImpl;
import com.centerm.epos.redevelop.ITradeRecordInformation;
import com.centerm.epos.redevelop.TradeRecordInfoImpl;
import com.centerm.epos.task.AsyncAutoSignOut;
import com.centerm.epos.utils.ViewUtils;

import config.BusinessConfig;

/**
 * Created by yuhc on 2017/4/1.
 */

public class SignOutTrade implements ManageTransaction {
    private ITradingView mTradingIntf;
    private ITradeView mTradeView;
    @Override
    public void execute(final ITradeView tradeView, final BaseTradePresent tradePresent) {
        this.mTradeView = tradeView;
        if ((tradeView instanceof ITradingView)) {
            mTradingIntf = (ITradingView) tradeView;
        }
        new AsyncAutoSignOut(mTradeView.getContext(), tradePresent.getTransData()) {
            @Override
            public void onStart() {
                super.onStart();
                if (null!=mTradingIntf)
                    mTradingIntf.updateHint("签退中，请稍候...");
            }

            @Override
            public void onFinish(String[] strings) {
                super.onFinish(strings);
                if ("00".equals(strings[0])) {
                    BusinessConfig.getInstance().setFlag(mTradeView.getHostActivity(), BusinessConfig.Key
                            .FLAG_SIGN_IN, false);
                    BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.KEY_IS_BATCH_BUT_NOT_OUT, false);
                    BusinessConfig.getInstance().setNumber(context, BusinessConfig.Key.KEY_POS_SERIAL, 1);
                    if ("2".equals(Settings.getValue(context, Settings.KEY.BATCH_SEND_STATUS, "0")))
                        Settings.setValue(context, Settings.KEY.BATCH_SEND_STATUS, "0");
                    mTradeView.popToast(R.string.tip_sign_out);
                    clearAllData();
                    logger.debug("签退成功");
                    Bundle bundle = TradeModelImpl.getInstance(null).getResultBundle();
                    if(bundle != null && bundle.size() > 0)
                        tradePresent.gotoNextStep();
                    else
                        tradePresent.jumpToLogin();
                } else {
                    tradePresent.gotoNextStep("2");
                    logger.error("签退请求失败");
                    ViewUtils.showToast(mTradeView.getHostActivity(), R.string.tip_sign_out_fail);

                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void clearAllData(){
        //清空流水
        boolean isDel;
        ITradeRecordInformation tradeRecordInformation = (ITradeRecordInformation) ConfigureManager
                .getSubPrjClassInstance(new TradeRecordInfoImpl());
        isDel = tradeRecordInformation.clearRecord();
        if (isDel) {
            Log.e("===", "该批次数据清空完成！");
        } else {
            Log.e("===", "该批次数据清空失败！");
        }
    }
}
