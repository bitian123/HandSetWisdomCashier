package com.centerm.epos.transcation.pos.data;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.define.IPinPadDev;
import com.centerm.epos.EposApplication;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.transcation.pos.constant.TlvTag;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.UnionPayTlvUtil;
import com.centerm.epos.utils.XLogUtil;
import com.centerm.smartpos.util.HexUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import config.BusinessConfig;
import config.Config;

/**
 * Created by yuhc on 2017/2/9.<br>
 * 钱宝自定义域
 */

public class BaseField47 implements I8583Field {

    private static final String TAG = BaseField47.class.getSimpleName();
    //SM加密结果为16字节，32个字符
    private static final int PWD_SM_LEN = 32;
    //加密随机因子长度
    private static final int RANDOM_LEN = 6;
    /**
     * 业务数据
     */
    protected Map<String, Object> tradeInfo;

    public BaseField47(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    @Override
    public String encode() {
        if (tradeInfo == null) {
            XLogUtil.e(TAG, "^_^ encode 输入参数 tradeInfo 为空 ^_^");
            return null;
        }
        String customInfo = getIso59();
        XLogUtil.d(TAG, "^_^  encode data:" + customInfo + "^_^");
        return customInfo;
    }

    @Override
    public Map<String, Object> decode(String fieldMsg) {
        if (TextUtils.isEmpty(fieldMsg)) {
            XLogUtil.e(TAG, "^_^ decode 输入参数 fieldMsg 为空 ^_^");
            return null;
        }
        String tradeName = (String) tradeInfo.get(TradeInformationTag.TRANSACTION_TYPE);
        if (TextUtils.isEmpty(tradeName)) {
            XLogUtil.e(TAG, "^_^ decode 获取交易代码失败！ ^_^");
            return null;
        }
        Map<String, String> dataMap = UnionPayTlvUtil.decode(HexUtil.hexStringToByte(fieldMsg));
        if (dataMap == null || dataMap.size() == 0) {
            XLogUtil.e(TAG, "^_^ decode TLV数据解析失败！ ^_^");
            return null;
        }

        Map<String, Object> returnMap = null;
        if (dataMap.containsKey(TlvTag.QRCODE_PAY_VOUCHER)) {
            returnMap = new HashMap<>();
            returnMap.put(TradeInformationTag.SCAN_VOUCHER_NO, dataMap.get(TlvTag.QRCODE_PAY_VOUCHER));
        }
        XLogUtil.d(TAG, "^_^ decode result:" + returnMap + " ^_^");
        return returnMap;
    }

    private String getIso59() {
        String code;
        String transCode = (String) tradeInfo.get(TradeInformationTag.TRANSACTION_TYPE);
        StringBuilder stringBuilder = new StringBuilder();
        String tlvItem;
        if (isUseSMEncrypt()) {
            //添加国密钥密码
            String pwd = (String) tradeInfo.get(TradeInformationTag.CUSTOMER_PASSWORD);
            if (!TextUtils.isEmpty(pwd) && pwd.length() == PWD_SM_LEN) {
                // TODO: 2017/6/23 密码是16进制数据，作为字符串处理，可能会有问题，待验证
                tlvItem = UnionPayTlvUtil.encode(TlvTag.PWD_SM, pwd);
                stringBuilder.append(HexUtil.bytesToHexString(tlvItem.getBytes()));
            }
        }

        if (isNeedTerminalTrade(transCode)) {
            String panData;
            if (TransCode.SALE_SCAN.equals(transCode))
                panData = (String) tradeInfo.get(TradeInformationTag.SCAN_CODE);
            else
                panData = (String) tradeInfo.get(TradeInformationTag.BANK_CARD_NUM);
            //添加终端序列号
            tlvItem = UnionPayTlvUtil.encode(TlvTag.UNIONPAY_SN, getUnionPaySN(panData));
            if (tlvItem != null) {
                stringBuilder.append(HexUtil.bytesToHexString(tlvItem.getBytes()));
            }
        }

        if (transCode.equals(TransCode.SALE_SCAN)) {
            code = (String) tradeInfo.get(TradeInformationTag.SCAN_CODE);
            if (TextUtils.isEmpty(code))
                return null;
            tlvItem = UnionPayTlvUtil.encode(TlvTag.QRCODE_PAY_INFO, code);
            if (tlvItem != null) {
                stringBuilder.append(HexUtil.bytesToHexString(tlvItem.getBytes()));
            }
//            stringBuilder.append("A3");
//            stringBuilder.append(String.format(Locale.CHINA, "%03d", code.length()));
//            stringBuilder.append(code);
//            return stringBuilder.toString();
        } else if (transCode.equals(TransCode.VOID_SCAN) || transCode.equals(TransCode.REFUND_SCAN)) {
            code = (String) tradeInfo.get(TradeInformationTag.SCAN_VOUCHER_NO);
            if (TextUtils.isEmpty(code))
                return null;
            tlvItem = UnionPayTlvUtil.encode(TlvTag.QRCODE_PAY_VOUCHER, code);
            if (tlvItem != null) {
                stringBuilder.append(HexUtil.bytesToHexString(tlvItem.getBytes()));
            }
//            stringBuilder.append("A4");
//            stringBuilder.append(String.format(Locale.CHINA, "%03d", code.length()));
//            stringBuilder.append(code);
//            return stringBuilder.toString();
        }
        XLogUtil.d(TAG, "^_^ field 59 orignale : " + new String(HexUtil.hexStringToByte(stringBuilder.toString())) +
                " ^_^");
        return stringBuilder.toString();
    }

    /**
     * 判断交易是否需要上送序列号，目前只有消费、扫码消费、预授权
     *
     * @param code 交易代码
     * @return true 需要上送
     */
    private boolean isNeedTerminalTrade(String code) {
        if (TransCode.SALE_SCAN.equals(code) || TransCode.SALE.equals(code) || TransCode.AUTH.equals(code))
            return true;

        return false;
    }

    /**
     * 生成银联规范要求的终端序列号数据
     *
     * @param pan 卡号或二维码数据
     * @return 序列号TLV数据
     */
    private String getUnionPaySN(String pan) {
        if (TextUtils.isEmpty(pan) || pan.length() < RANDOM_LEN) {
            XLogUtil.e(TAG, "^_^ 卡号或二维码信息为空或长度小于" + RANDOM_LEN + " ^_^");
            return null;
        }
        String random = obtainRandom(pan);
        if (TextUtils.isEmpty(random)) {
            XLogUtil.e(TAG, "^_^ 获取加密随机因子失败 ^_^");
            return null;
        }
        byte[] terminalSNByte = getTerminalHardwareSn();
        if (terminalSNByte == null) {
            XLogUtil.e(TAG, "^_^ 获取终端序列号失败 ^_^");
            return null;
        }

        String terminalSN = new String(terminalSNByte);
        byte[] snk = getSNK(terminalSN, random);
        if (snk == null || snk.length == 0) {
            XLogUtil.e(TAG, "^_^ 获取终端序列号MAC失败 ^_^");
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(UnionPayTlvUtil.encode(TlvTag.DEVICE_TYPE, Config.TERMINAL_TYPE));
        stringBuilder.append(UnionPayTlvUtil.encode(TlvTag.TERMINAL_SN, terminalSN));
        stringBuilder.append(UnionPayTlvUtil.encode(TlvTag.RANDOM_NUM, random));
        stringBuilder.append(UnionPayTlvUtil.encode(TlvTag.TERMINAL_SN_MAC, snk));
        stringBuilder.append(UnionPayTlvUtil.encode(TlvTag.APP_VERSION, getAppVersionCodeAndFillBlock()));
        return stringBuilder.toString();
    }

    /**
     * 从卡号或二维码支付数据中获取加密钥随机因子
     *
     * @param pan 卡号或二维码支付数据中
     * @return 加密随机因子
     */
    private String obtainRandom(String pan) {
        try {
            return pan.substring(pan.length() - RANDOM_LEN, pan.length());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 是否使用国密算法
     *
     * @return true 是
     */
    private boolean isUseSMEncrypt() {
        return false;
    }

    /**
     * 获取终端序列号
     *
     * @return 终端序列号
     */
    public static byte[] getTerminalHardwareSn() {
        try {
            IPinPadDev pinPadDev = DeviceFactory.getInstance().getPinPadDev();
            return pinPadDev.getHardWareSN();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取SN对应的MAC数据
     *
     * @param data 终端序列号
     * @param random 加密随机因子
     * @return 序列号MAC
     */
    public static byte[] getSNK(String data, String random) {
        try {
            IPinPadDev pinPadDev = DeviceFactory.getInstance().getPinPadDev();
            String snForSDKMacCal = HexUtil.bytesToHexString(data.getBytes());
            String randomForSDKMacCal = HexUtil.bytesToHexString(random.getBytes());
            byte[] snMac = pinPadDev.getMacForSNK(snForSDKMacCal, randomForSDKMacCal);
            //只取前8位
            return snMac.length > 8 ? Arrays.copyOf(snMac, 8) : snMac;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取应用版本：应用程序变更 应保证版本号不重复，当长度不足时，右补空格
     *
     * @return 应用版本号
     */
    public static String getAppVersionCodeAndFillBlock() {
        String version = "01";
        Context appContext = EposApplication.getAppContext();
        try {
            PackageInfo packageInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
            version = "" + packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 8; i++) {
            version += " ";
        }
        version = version.substring(0, 8);
        return version;
    }


}
