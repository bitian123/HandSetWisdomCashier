package com.centerm.epos.present.transaction;

import android.content.res.Resources;
import android.text.TextUtils;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.XLogUtil;

import java.util.Locale;
import java.util.Map;

public class InputCardNumberPresenter extends BaseTradePresent implements IInputCardNumberPresenter {

    protected ITradeView view;
    protected Map<String, Object> dataMap ;

    public InputCardNumberPresenter(ITradeView mTradeView) {
        super(mTradeView);
        view = mTradeView;
        dataMap = mTradeInformation.getTransDatas();
    }
    @Override
    public boolean onConfirmClicked(String... param) {
        Resources resources = view.getContext().getResources();
        if( TextUtils.isEmpty(param[0]) ){
            view.popToast("请输入卡号");
            return false;
        }
        if( param[0].length() < resources.getInteger(R.integer.card_number_min_len)){
            view.popToast(String.format(Locale.CHINA,"卡号长度不能小于%d",resources.getInteger(R.integer.card_number_min_len)));
            return false;
        }
        if( param[0].length() > resources.getInteger(R.integer.card_number_max_len)){
            view.popToast(String.format(Locale.CHINA,"卡号长度不能大于%d",resources.getInteger(R.integer.card_number_max_len)));
            return false;
        }

        if( TextUtils.isEmpty(param[1])|| param[1].length() != resources.getInteger(R.integer.date_max_len) ){
            view.popToast(String.format(Locale.CHINA,"有效期长度%d(格式:年年月月)",resources.getInteger(R.integer.date_max_len)));
            return false;
        }
        int month;
        try {
            month = Integer.parseInt(param[1].substring(2, 4));
        }catch (NumberFormatException e){
            month = 0;
        }
        if( !(month > 0 && month < 13) ){
            view.popToast("有效期格式错");
            return false;
        }
        dataMap.put(TradeInformationTag.BANK_CARD_NUM,param[0]);
        dataMap.put(TradeInformationTag.DATE_EXPIRED,param[1]);
        dataMap.put(TradeInformationTag.SERVICE_ENTRY_MODE,"01");
        gotoNextStep();
        return true;
    }
}
