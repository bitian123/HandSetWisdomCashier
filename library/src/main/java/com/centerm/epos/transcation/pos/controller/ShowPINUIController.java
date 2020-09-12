package com.centerm.epos.transcation.pos.controller;

import android.text.TextUtils;

import com.centerm.epos.EposApplication;
import com.centerm.epos.common.TransCode;

import config.BusinessConfig;

/**
 * Created by yuhc on 2017/8/3.
 * 是否输入密码控制
 */

public class ShowPINUIController extends AbsTradeUIController {
    @Override
    public boolean isShowUI(String tradeType) {
        if (TextUtils.isEmpty(tradeType))
            return true;

        String key;
        switch (tradeType){
            case TransCode.RESERVATION_VOID:
            case TransCode.ISS_INTEGRAL_VOID:
            case TransCode.UNION_INTEGRAL_VOID:
            case TransCode.VOID_INSTALLMENT:
            case TransCode.VOID:
                key = BusinessConfig.Key.TOGGLE_VOID_INPUTWD;
                break;
            case TransCode.CANCEL:
                key = BusinessConfig.Key.TOGGLE_AUTH_VOID_INPUTWD;
                break;
            case TransCode.AUTH_COMPLETE:
                key = BusinessConfig.Key.TOGGLE_AUTH_COMPLETE_INPUTWD;
                break;
            case TransCode.COMPLETE_VOID:
                key = BusinessConfig.Key.TOGGLE_COMPLETE_VOID_INPUTWD;
                break;

            default:
                key = null;
        }
        if (TextUtils.isEmpty(key))
            return true;
        return BusinessConfig.getInstance().getToggle(EposApplication.getAppContext(), key);
    }
}
