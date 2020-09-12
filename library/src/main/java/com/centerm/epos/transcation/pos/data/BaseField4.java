package com.centerm.epos.transcation.pos.data;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.XLogUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by yuhc on 2017/2/9.<br>
 * 域4：交易金额(Amount Of Transactions),交易金额，交易金额不包括任何手续费。<br>
 * 用法：交易金额的币种由域49—交易币种(Currency Code Of Transaction)表示。若为人民币则交易金额的单位是人民币的分。<br>
 * 示例：POS消费金额为1000元，则交易金额应为000000100000 <br>
 * 说明：当交易币种为外币时，如果该币种没有小数位，则该域的值代表实际交易金额；如果该币种有两个小数位，则表示方法同人民币；<br>
 * 若有三个小数位，则最后一个小数位必须为零。
 */

public class BaseField4 implements I8583Field {
    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField4.class.getSimpleName();

    private static final String DOT = "\\.";
    /**
     * 业务数据
     */
    protected Map<String,Object> tradeInfo;

    public BaseField4(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    /**
     * 输入的交易金额转化为12个数字表示的数据。主要是对小数点进行处理，只取小数点后2位，小数点前面最多取10位。
     *
     */
    @SuppressLint("DefaultLocale")
    @Override
    public String encode() {
        if (tradeInfo == null) {
            XLogUtil.e(TAG, "^_^ encode 输入参数 tradeInfo 为空 ^_^");
            return null;
        }
        String moneyInput = (String) tradeInfo.get(TradeInformationTag.TRANS_MONEY);
        if (TextUtils.isEmpty(moneyInput)) {
            XLogUtil.e(TAG, "^_^ 金额输入为空 ^_^");
            return null;
        }

        XLogUtil.d(TAG, "^_^ 输入金额为："+moneyInput+" ^_^");
        String amountFormat;
        //对小数点进行处理
        if (moneyInput.indexOf('.') == -1) {
            long moneyInt = Long.parseLong(moneyInput, 10);
            if (checkIntegralPart(moneyInt)) return null;
            //格式化输出数据：10位整数+2位填充小数
            amountFormat = String.format("%010d00", moneyInt);
        } else {
            String moneyParts[] = moneyInput.split(DOT);
            if (moneyParts.length > 3) {
                XLogUtil.e(TAG, "^_^ 输入的交易金额不合法，最多只能有一位小数点 ^_^");
                return null;
            }
            //整数部分处理
            long moneyIntegralPart = Long.parseLong(moneyParts[0], 10);
            if (checkIntegralPart(moneyIntegralPart)) return null;
            XLogUtil.d(TAG, "^_^ 整数值："+moneyIntegralPart+" ^_^");
            //小数部分处理
            String fractionalPartStr = moneyParts[1];
            if (fractionalPartStr.length() > 2)
                fractionalPartStr = fractionalPartStr.substring(0,2);
            Long moneyFractionalPart = Long.parseLong(fractionalPartStr,10);
            XLogUtil.d(TAG, "^_^ 小数值："+moneyFractionalPart+" ^_^");
            //格式化输出数据：10位整数+2位小数
            amountFormat = String.format("%010d%02d",moneyIntegralPart,moneyFractionalPart);
        }
        XLogUtil.d(TAG, "^_^ encode result:" + amountFormat + " ^_^");
        return amountFormat;
    }

    private boolean checkIntegralPart(long moneyInt) {
        if (moneyInt > 9999999999l) {
            XLogUtil.e(TAG, "^_^ 输入的交易金额不合法，超过最大限额 ^_^");
            return true;
        }
        return false;
    }

    /**
     * 12位的交易金额转化为点分金额，例如：000000000100转化为1.00
     * @param fieldMsg  域数据
     * @return 业务数据：金额
     */
    @Override
    public Map<String, Object> decode(String fieldMsg) {
        if (TextUtils.isEmpty(fieldMsg)){
            XLogUtil.e(TAG, "^_^ decode 输入参数 fieldMsg 为空 ^_^");
            return null;
        }

        long amountLong = Long.parseLong(fieldMsg,10);
        String amountStr = String.format(Locale.CHINA,"%d.%02d",amountLong/100,amountLong%100);
        Map<String,Object> tradeInfo = new HashMap<>(1);
        tradeInfo.put(TradeInformationTag.TRANS_MONEY, amountStr);
        XLogUtil.d(TAG, "^_^ decode result:"+tradeInfo+" ^_^");
        return tradeInfo;
    }
}
