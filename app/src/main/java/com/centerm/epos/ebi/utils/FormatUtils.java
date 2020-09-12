package com.centerm.epos.ebi.utils;

import com.centerm.epos.utils.XLogUtil;

import java.util.Locale;

/**
 * Created by liubit on 2017/12/26.
 */

public class FormatUtils {
    private static final String DOT = "\\.";

    /**
     * 0.01 -> 000000000001
     * */
    public static String formatAmount1(String moneyInput){
        String moneyParts[] = moneyInput.split(DOT);
        if (moneyParts.length > 3) {
            XLogUtil.e("getAmount", "^_^ 输入的交易金额不合法，最多只能有一位小数点 ^_^");
            return "";
        }
        //整数部分处理
        long moneyIntegralPart = Long.parseLong(moneyParts[0], 10);
        if (checkIntegralPart(moneyIntegralPart)) return "";
        XLogUtil.d("getAmount", "^_^ 整数值："+moneyIntegralPart+" ^_^");
        //小数部分处理
        String fractionalPartStr = moneyParts[1];
        if (fractionalPartStr.length() > 2)
            fractionalPartStr = fractionalPartStr.substring(0,2);
        Long moneyFractionalPart = Long.parseLong(fractionalPartStr,10);
        XLogUtil.d("getAmount", "^_^ 小数值："+moneyFractionalPart+" ^_^");
        //格式化输出数据：10位整数+2位小数
        return String.format("%010d%02d",moneyIntegralPart,moneyFractionalPart);
    }

    /**
     * 000000000001 -> 0.01
     * */
    public static String formatAmount2(String amount){
        try {
            long balance = Long.parseLong(amount);
            String balanceStr = String.format(Locale.CHINA, "%d.%02d", balance / 100, balance
                    % 100);
            return balanceStr;
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 1.23 -> 123
     * */
    public static String formatAmount3(String moneyInput){
        String moneyParts[] = moneyInput.split(DOT);
        if (moneyParts.length > 3) {
            XLogUtil.e("getAmount", "^_^ 输入的交易金额不合法，最多只能有一位小数点 ^_^");
            return "";
        }
        moneyInput = moneyInput.replace(".", "");
        long result = Long.parseLong(moneyInput);
        if (checkIntegralPart(result)) return "";
        return ""+result;
    }

    private static boolean checkIntegralPart(long moneyInt) {
        if (moneyInt > 9999999999l) {
            XLogUtil.e("getAmount", "^_^ 输入的交易金额不合法，超过最大限额 ^_^");
            return true;
        }
        return false;
    }

}
