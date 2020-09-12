package com.centerm.epos.utils;

import android.text.TextUtils;

import java.util.Locale;

/**
 * Created by yuhc on 2017/10/25.
 */

public class MoneyUtil {

    private static final String TAG = MoneyUtil.class.getSimpleName();

    public static String formatMoney(String moneyStr) {
        if (TextUtils.isEmpty(moneyStr))
            return moneyStr;
        double moneyDouble;
        try {
            moneyDouble = Double.parseDouble(moneyStr);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
        return String.format(Locale.CHINA, "%.2f", moneyDouble/100);
    }
}
