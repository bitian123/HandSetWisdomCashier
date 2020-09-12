package com.centerm.epos.transcation.pos.controller;

import android.text.TextUtils;

import com.centerm.epos.EposApplication;
import com.centerm.epos.common.TransCode;

import config.BusinessConfig;

/**
 * Created by yuhc on 2017/8/3.
 * 交易是否用卡
 */

public class ShowCardUIController extends AbsTradeUIController{
    @Override
    public boolean isShowUI(String tradeType) {
        if (TextUtils.isEmpty(tradeType))
            return true;

        String key;
        switch (tradeType){
            case TransCode.VOID:
                key = BusinessConfig.Key.TOGGLE_VOID_CHECKCARD;
                break;
//            case TransCode.CANCEL:
//                key = BusinessConfig.Key.TOGGLE_AUTH_VOID_CHECKCARD;
//                break;
            case TransCode.COMPLETE_VOID:
                key = BusinessConfig.Key.TOGGLE_COMPLETE_VOID_CHECKCARD;
                break;
            default:
                key = null;
        }
        if (TextUtils.isEmpty(key))
            return true;
        return BusinessConfig.getInstance().getToggle(EposApplication.getAppContext(), key);
    }
}
