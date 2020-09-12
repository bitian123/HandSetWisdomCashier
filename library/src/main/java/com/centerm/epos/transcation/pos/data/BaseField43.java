package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;

import com.centerm.epos.EposApplication;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.XLogUtil;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.epos.common.TransCode.OBTAIN_TMK;
import static com.centerm.epos.common.TransDataKey.iso_f43;

/**
 * Created by yuhc on 2017/2/8.<br>
 * 钱宝自定义域42 商户名称。
 */

public class BaseField43 implements I8583Field {

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField43.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String,Object> tradeInfo;

    public BaseField43(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    /**
     * 商户名称 ，并根据规范要求输出40个Byte的ASC码数据。
     *
     * @return 受卡方标识码/商户号
     */
    @Override
    public String encode() {
        XLogUtil.d(TAG, "^_^ do nothing~ ^_^");
        return null;
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
        if (TextUtils.isEmpty(tradeName) && OBTAIN_TMK.equals(tradeName)) {
            BusinessConfig.getInstance().setIsoField(EposApplication.getAppContext(), 43, fieldMsg);
        }

        Map<String, Object> tradeInfo = new HashMap<>(1);
        try {
            int i;
            for (i = fieldMsg.length()-1; i >= 0; i--){
                if (fieldMsg.charAt(i) != ' ')
                    break;
            }
            tradeInfo.put(TradeInformationTag.MERCHANT_NAME, fieldMsg.substring(0,i+1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        XLogUtil.d(TAG, "^_^ decode result:" + tradeInfo + " ^_^");
        return tradeInfo;
    }

}
