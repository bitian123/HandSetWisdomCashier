package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;

import com.centerm.epos.bean.transcation.BalancAmount;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.XLogUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuhc on 2017/2/8.<br>
 * 域54 余额(Balanc Amount)，最大20个字节的数据。<br>
 * 表示持卡人的账户可用余额.
 */

public class BaseField54 implements I8583Field {

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField54.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String,Object> tradeInfo;

    public BaseField54(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    /**
     * @return null
     */
    @Override
    public String encode() {
        XLogUtil.d(TAG, "^_^ encode running but do nothing ^_^");
        return null;
    }

    /**
     * 从域数据中取出平台返回的余额信息，并保存到指定TAG
     *
     * @param fieldMsg 域数据
     * @return 业务数据对象，余额信息
     */
    @Override
    public Map<String, Object> decode(String fieldMsg) {
        if (TextUtils.isEmpty(fieldMsg)) {
            XLogUtil.e(TAG, "^_^ decode 输入参数 fieldMsg 为空 ^_^");
            return null;
        }

        BalancAmount balancAmount = new BalancAmount();
        try {
            balancAmount.setAccountType(fieldMsg.substring(0,2));
            balancAmount.setAmountType(fieldMsg.substring(2,4));
            balancAmount.setCurrencyCode(fieldMsg.substring(4,7));
            balancAmount.setAmountSign(fieldMsg.charAt(7));
            balancAmount.setAmount(Long.parseLong(fieldMsg.substring(8,20)));
        } catch (Exception e) {
            XLogUtil.e(TAG, "^_^ 余额信息解析失败,错误原因："+e.getMessage()+" ^_^");
            return null;
        }

        Map<String, Object> tradeInfo = new HashMap<>(1);
        tradeInfo.put(TradeInformationTag.BALANC_AMOUNT, balancAmount);
        XLogUtil.d(TAG, "^_^ decode result:" + balancAmount + " ^_^");
        return tradeInfo;
    }

}
