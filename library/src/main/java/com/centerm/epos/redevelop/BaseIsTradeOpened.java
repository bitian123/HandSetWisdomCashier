package com.centerm.epos.redevelop;

import android.content.Context;
import android.content.SharedPreferences;

import com.centerm.epos.EposApplication;
import com.centerm.epos.base.SimpleStringTag;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.transcation.pos.constant.MenuControlTag;

import config.BusinessConfig;

/**
 * Created by 94437 on 2017/7/25.
 */

public class BaseIsTradeOpened implements IIsTradeOpened {
    private final static String SP_FILE_NAME = "business_config";//用于存储参数的XML文件名称
    @Override
    public boolean isTradeOpened(String menuTag) {
        if ("AUTHMENU".equals(menuTag)) {
            return BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), MenuControlTag.AUTH)
                    || BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), MenuControlTag.AUTH_COMPLETE)
                    || BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), MenuControlTag.CANCEL)
                    || BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), MenuControlTag.COMPLETE_VOID);
        }
        else if("INTEGRAL_MENU".equals(menuTag)){
            return BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), MenuControlTag.PREFIX+ TransCode.ISS_INTEGRAL_SALE)
                    || BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), MenuControlTag.PREFIX+ TransCode.ISS_INTEGRAL_VOID)
                    || BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), MenuControlTag.PREFIX+ TransCode.UNION_INTEGRAL_SALE)
                    || BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), MenuControlTag.PREFIX+ TransCode.UNION_INTEGRAL_VOID)
                    || BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), MenuControlTag.PREFIX+ TransCode.UNION_INTEGRAL_BALANCE)
                    || BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), MenuControlTag.PREFIX+ TransCode.UNION_INTEGRAL_REFUND);
        }
        else if("RESERVATION_MENU".equals(menuTag)){
            return BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), MenuControlTag.PREFIX+ TransCode.RESERVATION_SALE)
                    || BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), MenuControlTag.PREFIX+ TransCode.RESERVATION_VOID);
        }
        else if("INSTALLMENT_MENU".equals(menuTag)){
            return BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), MenuControlTag.PREFIX+ TransCode.SALE_INSTALLMENT)
                    || BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), MenuControlTag.PREFIX+ TransCode.VOID_INSTALLMENT);
        }
        boolean result = BusinessConfig.getInstance().getToggle(EposApplication.getAppContext(), MenuControlTag.PREFIX + menuTag);
        /*
        * 修改交易控制开关初始化参数没有起作用的bug
        * */
        return getDefaultPres(EposApplication.getAppContext()).getBoolean(MenuControlTag.PREFIX + menuTag, /*true*/result);
    }
    private SharedPreferences getDefaultPres(Context context) {
        return context.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
    }
}
