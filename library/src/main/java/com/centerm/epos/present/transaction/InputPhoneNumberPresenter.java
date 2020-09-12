package com.centerm.epos.present.transaction;

import android.os.Bundle;
import android.text.TextUtils;

import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.bean.TradeInfo;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;

import config.BusinessConfig;

/**
 * Created by yuhc on 2017/9/1.
 *
 */

public class InputPhoneNumberPresenter extends BaseTradePresent {
    protected ITradeView view;

    public InputPhoneNumberPresenter(ITradeView mTradeView) {
        super(mTradeView);
        view = mTradeView;
    }

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        mTradeView.getHostActivity().openPageTimeout(BusinessConfig.getInstance().getNumber(mTradeView.getContext(),
                BusinessConfig.Key.KEY_TRADE_VIEW_OP_TIMEOUT), "长时间未操作\n是否跳过电话号码输入");
    }

    @Override
    public boolean isOpenDataBase() {
        return true;
    }

    public boolean onConfirmClicked(String phoneNumber) {
        if (!TextUtils.isEmpty(phoneNumber)) {
            transDatas.put(TradeInformationTag.PHONE_NUMBER, phoneNumber);
            save2Db(phoneNumber);
        }
        mTradeView.getHostActivity().clearPageTimeout();
        gotoNextStep();
        return true;
    }

    private void save2Db(String phoneNumber) {
        CommonDao<TradeInfoRecord> tradeInfoRecordCommonDao = new CommonDao<>(TradeInfoRecord.class, dbHelper);
        TradeInfoRecord record = tradeInfoRecordCommonDao.queryForId(transDatas.get(TradeInformationTag.TRACE_NUMBER));
        if (record != null){
            record.setMobile_phone_number(phoneNumber);
            tradeInfoRecordCommonDao.update(record);
        }
    }
}
