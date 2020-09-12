package com.centerm.epos.channels.online;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Created by 王玮 on 2016/5/31.
 */
public class PhoneInfo {
    private static TelephonyManager telephonyManager;
    private static Context cxt;
    /**
     * 国际移动用户识别码
     */
    private static String IMSI;
    private static PhoneInfo instance;

    public static PhoneInfo init(Context context) {
        instance = new PhoneInfo();
        telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return instance;
    }

    private PhoneInfo() {
    }

    public static String getIMEI() {
        return telephonyManager.getDeviceId();
    }

    public static String getICCID() {
        return telephonyManager.getSimSerialNumber();
    }

    public static String getIMSI() {
        return telephonyManager.getSubscriberId();
    }

    public static String getSimOperator() {
        return telephonyManager.getSimOperator();
    }


    /**
     * 获取运营商识别号
     */
    public static String getServiceProvider() {
        String NativePhoneNumber = null;
        NativePhoneNumber = telephonyManager.getNetworkOperator();
        return NativePhoneNumber;
    }

    /**
     * 获取电话号码
     */
    public static String getNativePhoneNumber() {
        String NativePhoneNumber = null;
        NativePhoneNumber = telephonyManager.getLine1Number();
        return NativePhoneNumber;
    }

    /**
     * 获取电话号码
     */
    public static String getNetworkOperatorName() {
        String NativePhoneNumber = null;
        NativePhoneNumber = telephonyManager.getNetworkOperatorName();
        return NativePhoneNumber;
    }

    /**
     * 获取手机服务商信息
     */
    public static String getProvidersName() {
        String ProvidersName = "N/A";
        try {
            IMSI = telephonyManager.getSubscriberId();
            // IMSI号前面3位460是国家，紧接着后面2位00 02是中国移动，01是中国联通，03是中国电信。
            if (IMSI.startsWith("46000") || IMSI.startsWith("46002")) {
                ProvidersName = "中国移动";
            } else if (IMSI.startsWith("46001")) {
                ProvidersName = "中国联通";
            } else if (IMSI.startsWith("46003")) {
                ProvidersName = "中国电信";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ProvidersName;
    }
}
