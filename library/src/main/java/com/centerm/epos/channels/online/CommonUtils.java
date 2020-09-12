package com.centerm.epos.channels.online;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Base64;

import com.centerm.cloudsys.sdk.common.utils.MD5Utils;
import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.EnumDeviceType;
import com.centerm.cpay.midsdk.dev.define.IPinPadDev;
import com.centerm.cpay.midsdk.dev.define.ISystemService;
import com.centerm.cpay.midsdk.dev.define.pinpad.EnumMacType;
import com.centerm.epos.EposApplication;
import com.centerm.epos.msg.PosISO8583Message;
import com.centerm.epos.security.CpaySecurityTool;

import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Created by 王玮 on 2016/6/16.
 */
public class CommonUtils {
    private static Logger logger = Logger.getLogger(CommonUtils.class);

    public final static byte[] MASK = new byte[]{(byte) 0x13, (byte) 0x43, (byte) 0x45, (byte) 0x35
            , (byte) 0x56, (byte) 0x23, (byte) 0x15, (byte) 0x35, (byte) 0x56, (byte) 0x58, (byte) 0x43,
            (byte) 0x55, (byte) 0x32, (byte) 0x45, (byte) 0x33, (byte) 0x44, (byte) 0x55, (byte) 0x35,
            (byte) 0x56, (byte) 0x23, (byte) 0x15, (byte) 0x34, (byte) 0x45, (byte) 0x35, (byte) 0x34,
            (byte) 0x66, (byte) 0x33, (byte) 0x43, (byte) 0x77, (byte) 0x18, (byte) 0x19, (byte) 0x37};

    public static String getDevMask(String terminalSn) {
        if (terminalSn == null) {
            return null;
        }
        byte[] snBytes = terminalSn.getBytes();
        int len = snBytes.length;

        int maxLen = MASK.length;
        byte[] result = new byte[len < maxLen ? len : maxLen];
        for (int i = 0; (i < len && i < maxLen); i++) {
            result[i] = (byte) (MASK[i] ^ snBytes[i]);
        }
        byte[] devMask = Base64.encode(result, Base64.NO_WRAP);
        return new String(devMask);
    }

    public static Method systemProperties_get = null;

    public static String getAndroidOsSystemProperties(String key) {
        String ret;
        try {
            systemProperties_get = Class.forName("android.os.SystemProperties").getMethod("get", String.class);
            if ((ret = (String) systemProperties_get.invoke(null, key)) != null)
                return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return "";
    }

    /**
     * 获取硬件物理地址，需要声明权限android.permission.ACCESS_WIFI_STATE
     *
     * @param context
     * @return 如果没有连接上Wifi，返回null
     */
    public static String getMacAddress(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        String mac = info.getMacAddress();
        if (mac != null) {
            mac = mac.toUpperCase();
        }
        return mac;
    }

    /**
     * 获取当前时间
     *
     * @return
     */
    public static String getCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String str = formatter.format(curDate);
        return str;
    }

    /**
     * 获取6位随机数
     *
     * @return
     */
    public static String getRandom() {
        String smstext = "";
        for (int i = 0; i < 6; i++) {
            smstext = smstext + getRandom(9);
        }
        return smstext;
    }

    /**
     * 获取0-max的随机数
     *
     * @param max 最大值
     * @return
     */
    public static String getRandom(int max) {
        Random random = new Random();
        int a = random.nextInt(max + 1);
        return a + "";
    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    public static String getVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    public static int getVersionCode(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            int versionCode = info.versionCode;
            return versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String getString(Object o) {
        StringBuffer sb = new StringBuffer();
        sb.append("该Bean的字符串数据[");
        Field[] farr = o.getClass().getDeclaredFields();
        for (Field field : farr) {
            try {
                field.setAccessible(true);
                sb.append(field.getName());
                sb.append("=");
                sb.append(field.get(o));
                sb.append("|");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        sb.append("]");
        return sb.toString();
    }


    public final static int MAC_KEY_INDEX = 2;//计算MAC的key索引
    /**
     * 计算MAC
     *
     * @param message 报文
     * @return mac mac值
     */
    public static String calculateMac(String message) {
        if (TextUtils.isEmpty(message))
            return "";
        try {
            return CpaySecurityTool.getInstance().getIVirtualPinPad().calculateMac(MAC_KEY_INDEX, message);
        } catch (RemoteException e) {
            logger.error("error:"+e.getMessage());
            return "";
        }
    }

    private static final String DEV_MASK_KEY = "DEVMASK";
    /**
     * 计算设备标识
     * @return
     */
    public static String generateDevMask(){
        String devMask = SharedUtil.getString(EposApplication.getAppContext(), DEV_MASK_KEY, null);
        if (TextUtils.isEmpty(devMask)) {
            ISystemService systemService = (ISystemService) DeviceFactory.getInstance()
                    .getDevice(EnumDeviceType.SYSTEM_SERVICE);
            String sn = systemService.getTerminalSn();
            devMask = CommonUtils.getDevMask(sn);
            //保存devMask到sharePre
            SharedUtil.putString(EposApplication.getAppContext(), DEV_MASK_KEY, devMask);
        }
        logger.debug("Dev Mask: "+devMask);
        return devMask;
    }
}


