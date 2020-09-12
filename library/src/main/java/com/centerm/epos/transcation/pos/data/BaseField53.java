package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;

import com.centerm.epos.EposApplication;
import com.centerm.epos.bean.transcation.SecurityControlInformation;
import com.centerm.epos.common.EncryptAlgorithmEnum;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.XLogUtil;

import java.util.Map;

import config.BusinessConfig;

/**
 * Created by yuhc on 2017/2/8.<br>
 * 域53 安全控制信息(Security Related Control Information )，16个字节的定长数字字符域。<br>
 * 在交易类消息中，该域用于标识PIN和磁道信息加密的类型:PIN加密方法、加密算法标志、磁道加密标志.
 */

public class BaseField53 implements I8583Field {

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField53.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String, Object> tradeInfo;

    public BaseField53(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    /**
     * 从业务数据中取出货币代码，并根据规范要求输出15个Byte的ASC码数据。
     *
     * @return 货币代码
     */
    @Override
    public String encode() {
        if (tradeInfo == null) {
            XLogUtil.e(TAG, "^_^ encode 输入参数 tradeInfo 为空 ^_^");
            return null;
        }
//        Object transData = tradeInfo.get(TradeInformationTag.SECURITY_CONTROL);
//        SecurityControlInformation securityControl = null;
//        if (transData == null || !(transData instanceof SecurityControlInformation)){
//            securityControl = getDefaultSecurityControl();
//        }else
//            securityControl = (SecurityControlInformation) transData;

//        String encodeBuffer = String.format(Locale.CHINA, "%d%d%d0000000000000",securityControl.getPinFormat(),
//                securityControl.getTrackEncryption(),securityControl.getTrackEncryption());

        boolean hasPin = !TextUtils.isEmpty((CharSequence) tradeInfo.get(TradeInformationTag.CUSTOMER_PASSWORD));
        boolean hasTrackData = !TextUtils.isEmpty((CharSequence) tradeInfo.get(TradeInformationTag.TRACK_2_DATA));
        String encodeBuffer = getIso53(hasPin, hasTrackData, BusinessConfig.getInstance().getToggle(EposApplication
                .getAppContext(), BusinessConfig.Key.TOGGLE_TRACK_ENCRYPT));
        XLogUtil.d(TAG, "^_^ encode result:" + encodeBuffer + " ^_^");
        return encodeBuffer;
    }

    /**
     * 获取默认的安全控制信息
     *
     * @return ANSI X9.8 Format（带主账号信息） + 双倍长密钥算法 + 磁道加密
     */
    private SecurityControlInformation getDefaultSecurityControl() {
        SecurityControlInformation securityControlInformation = new SecurityControlInformation(2, 6, 1);
        return securityControlInformation;
    }

    /**
     * @param fieldMsg 域数据
     * @return null
     */
    @Override
    public Map<String, Object> decode(String fieldMsg) {
        XLogUtil.d(TAG, "^_^ decode running but do nothing ^_^");
        return null;
    }

    /**
     * 组合53域数据（安全控制信息）
     *手机无卡预约消费 无卡号 无磁道数据
     * @param hasPin 是否有PIN（对应PIN加密方法）
     * @param trackEncryptFlag 磁道加密标志
     * @return 组合户的53域数据
     */
    private String getIso53(boolean hasPin, boolean hasTrackData, boolean trackEncryptFlag) {
        String mTransCode = (String)tradeInfo.get(TradeInformationTag.TRANSACTION_TYPE);
        boolean isEncryptPan = (!TransCode.RESERVATION_SALE.equals(mTransCode));

        if (!hasPin && ( !hasTrackData && isEncryptPan ) )
            return null;
        StringBuilder stringBuilder = new StringBuilder();
        /*
            1：ANSI X9.8 Format（不带主账号信息）
            2：ANSI X9.8 Format（带主账号信息）
        */
        stringBuilder.append(hasPin ? (isEncryptPan? "2" : "1" ) : "0");

        EncryptAlgorithmEnum alg = Settings.getEncryptAlgorithmEnum(EposApplication.getAppContext());
        stringBuilder.append(alg == EncryptAlgorithmEnum.DES ? "0" : alg == EncryptAlgorithmEnum.TRIPLE_DES ? "6" :
                "3");
        String entryMode = (String) tradeInfo.get(TradeInformationTag.SERVICE_ENTRY_MODE);
        //不为空且不是手输卡号

        if (!TextUtils.isEmpty(entryMode) && !entryMode.startsWith("01")) {
            stringBuilder.append( (trackEncryptFlag && isEncryptPan) ? "1" : "0");
        } else
            stringBuilder.append("0");

        for (int i = 0; i < 13; i++) {
            stringBuilder.append("0");
        }
        return stringBuilder.toString();
    }
}
