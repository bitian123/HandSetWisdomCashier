package com.centerm.epos.transcation.pos.controller;

import com.centerm.epos.EposApplication;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.common.Settings;
import com.centerm.epos.transcation.pos.manager.SignInTrade;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import config.BusinessConfig;

/**
 * Created by yuhc on 2017/5/23.
 */

public class AutoSignInController {
    private static final String TAG = AutoSignInController.class.getSimpleName();

    /**
     * 处理自动签到，用于每天首次交易前判断，终端重启后判断
     *
     * @return true处理完成 false处理失败
     */
    public static boolean processAutoSignIn(ITradeView view, BaseTradePresent present) {
        if (isNeedAutoSignIn()) {
            new SignInTrade().execute(view, present);
            return true;
        }
        return false;
    }

    /**
     * 检查是否需要自动签到，通过签到时间进行判断，如果是隔天了或是日期为空，则要激活签到
     */
    public static boolean isNeedAutoSignIn() {
        if(!BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), BusinessConfig.Key.FLAG_SIGN_IN))
            return true;
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMdd", Locale.CHINA);
        String dateStr = dateFormat.format(new Date(System.currentTimeMillis()));
        String signinDate = Settings.getValue(EposApplication.getAppContext(), Settings.KEY.SIGN_IN_DATE, "");
        if (!signinDate.equals(dateStr)) {
            return true;
        }
        return false;
    }
}
