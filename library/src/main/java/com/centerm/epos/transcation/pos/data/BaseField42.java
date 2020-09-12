package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;

import com.centerm.epos.EposApplication;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.XLogUtil;

import java.util.HashMap;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.epos.common.TransCode.OBTAIN_TMK;

/**
 * Created by yuhc on 2017/2/8.<br>
 * 域42 受卡方标识码/商户号(Card Acceptor Identification Code)，15个字节的定长的字母、数字和特殊字符。
 */

public class BaseField42 implements I8583Field {

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField42.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String,Object> tradeInfo;

    public BaseField42(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    /**
     * 从业务数据中取出受卡方标识码/商户号，并根据规范要求输出15个Byte的ASC码数据。
     *
     * @return 受卡方标识码/商户号
     */
    @Override
    public String encode() {
        if (tradeInfo == null) {
            XLogUtil.e(TAG, "^_^ encode 输入参数 tradeInfo 为空 ^_^");
            return null;
        }
        String merchantId = (String) tradeInfo.get(TradeInformationTag.MERCHANT_IDENTIFICATION);

        if (TextUtils.isEmpty(merchantId)) {
            //未获取到终端号，再从终端参数中获取
            merchantId = getMerchantIdFromParam();
        }

        XLogUtil.d(TAG, "^_^ encode result:" + merchantId + " ^_^");
        return merchantId;
    }

    /**
     * 从终端参数中获取终端号
     * @return 终端号
     */
    private String getMerchantIdFromParam() {
        return BusinessConfig.getInstance().getIsoField(EposApplication.getAppContext(), 42);
    }

    /**
     * 从域数据中取出平台返回的返回码，并保存到指定TAG
     *
     * @param fieldMsg 域数据
     * @return 业务数据对象，授权标识应答码
     */
    @Override
    public Map<String, Object> decode(String fieldMsg) {
        if (TextUtils.isEmpty(fieldMsg)) {
            XLogUtil.e(TAG, "^_^ decode 输入参数 fieldMsg 为空 ^_^");
            return null;
        }


        String tradeName = (String) tradeInfo.get(TradeInformationTag.TRANSACTION_TYPE);
        //钱宝需求
        if (TextUtils.isEmpty(tradeName) && OBTAIN_TMK.equals(tradeName)) {
            BusinessConfig.getInstance().setIsoField(EposApplication.getAppContext(), 42, fieldMsg);
        }

        Map<String, Object> tradeInfo = new HashMap<>(1);
        tradeInfo.put(TradeInformationTag.MERCHANT_IDENTIFICATION, fieldMsg);
        XLogUtil.d(TAG, "^_^ decode result:" + tradeInfo + " ^_^");
        return tradeInfo;
    }

}
