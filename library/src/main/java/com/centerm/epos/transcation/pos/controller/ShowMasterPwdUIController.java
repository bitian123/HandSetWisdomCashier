package com.centerm.epos.transcation.pos.controller;

import com.centerm.epos.EposApplication;

import config.BusinessConfig;

/**
 * Created by yuhc on 2017/8/3.
 */

public class ShowMasterPwdUIController extends AbsTradeUIController {
    @Override
    public boolean isShowUI(String tradeType) {
//        if (TextUtils.isEmpty(tradeType))
//            return true;
//
//        String key;
//        switch (tradeType){
//            case TransCode.VOID:
//            case TransCode.REFUND:
//                key = BusinessConfig.Key.TOGGLE_MASTER_PWD_INPUT;
//                break;
//            default:
//                key = null;
//        }
//        if (TextUtils.isEmpty(key))
//            return true;
        return BusinessConfig.getInstance().getToggle(EposApplication.getAppContext(), BusinessConfig.Key
                .TOGGLE_MASTER_PWD_INPUT);
    }
}
