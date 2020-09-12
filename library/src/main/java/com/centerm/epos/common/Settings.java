package com.centerm.epos.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.centerm.epos.EposApplication;
import com.centerm.epos.activity.MainActivity;
import com.centerm.epos.base.SimpleStringTag;
import com.centerm.epos.bean.AppInfo;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.present.communication.ICommunication;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.XLogUtil;
import com.centerm.epos.xml.bean.PreferDataPool;
import com.centerm.epos.xml.keys.Keys;

import org.apache.log4j.Logger;

import config.BusinessConfig;

import static com.centerm.epos.common.Settings.KEY.FLAG_TMK_EXIST;
import static com.centerm.epos.common.Settings.KEY.NEW_CHANNEL;

/**
 * author: linwanliang</br>
 * date:2016/7/19</br>
 * 应用偏好设置信息，业务类参数设置请逐步迁移到{@link BusinessConfig}
 */
public class Settings {

    private static Logger logger = Logger.getLogger(Settings.class);
    private static String uiChannel;


    public static class KEY {
        public final static String PROJECT_NAME = "PROJECT_NAME";//项目名称
        public final static String FLAG_INIT = "FLAG_INIT";//应用初始化的标识

        public final static String NEW_CHANNEL = "NEW CHANNEL";//需要切换的项目标识

        public final static String COMMON_IP = "COMMON_IP";//IP
        public final static String COMMON_PORT = "COMMON_PORT";//端口
        public final static String COMMON_TPDU = "COMMON_TPDU";//TPDU
        public final static String COMMON_MSG_ENCRYPT = "COMMON_MSG_ENCRYPT";//通讯加密

        public final static String CPAY_COMMON_IP = "CPAY_COMMON_IP";//IP
        public final static String CPAY_COMMON_PORT = "CPAY_COMMON_PORT";//端口
        public final static String CPAY_COMMON_TPDU = "CPAY_COMMON_TPDU";//TPDU
        public final static String CPAY_COMMON_MSG_ENCRYPT = "CPAY_COMMON_MSG_ENCRYPT";//通讯加密

        public final static String SLIP_IP = "SLIP_IP";//签购单上传IP
        public final static String SLIP_PORT = "SLIP_PORT";//签购单上传端口
        public final static String SLIP_UPLOAD_URL = "SLIP_UPLOAD_URL";//签购单上传URL
        public final static String VERSION_PORT = "VERSION_PORT";//版本检测端口
        public final static String VERSION_CHECK_URL = "VERSION_CHECK_URL";//应用版本检测URL
        public final static String CLSS_CARD_PREFERED = "CLSS_CARD_PREFERED";//挥卡优先
        public final static String NET_RESP_TIMEOUT = "NET_RESP_TIMEOUT";//网络响应超时时间
        public final static String NET_CONNECT_TIMEOUT = "NET_CONNECT_TIMEOUT";//网络连接超时时间
        public final static String NORMAL_EXIT_FLAG = "NORMAL_EXIT_FLAG";//程序正常退出的标志
        public final static String AUTO_SIGN_OUT = "AUTO_SIGN_OUT";//是否自动签退
        public final static String SLIP_VERSION = "SLIP_VERSION";//签购单版本号
        public final static String OPER_ID = "OPER_ID";//当前操作员
        public final static String PRINT_THREE = "PRINT_THREE";//是否三联打印
        public final static String OFF_LINE_CONSUME = "OFF_LINE_CONSUME";//是否三联打印
        public final static String COMM_TYPE = "COMM TYPE";//类型
        public final static String CPAY_COMM_TYPE = "CPAY COMM TYPE";//CPAY管理平台通讯类型

        //批结算过程中的状态指示：1 结算前上送交易 2 上送结算信息  3 对账不平后交易上送
        public final static String SETTLEMENT_STATUS = "SETTLEMENT_STATUS";
        public final static String BATCH_SEND_STATUS = "BATCH_SEND_STATUS";//0批结算初始化 1 批结未完成 2批结算完成
        public final static String PREV_BATCH_TOTAL = "PREV_BATCH_TOTAL";//上批次总计gson串
        public final static String BATCH_SEND_RETURN_DATA = "BATCH_SEND_RETURN_DATA";//对账返回数据
        public final static String IS_BATCH_EQUELS = "IS_BATCH_EQUELS";//是否对账平
        public final static String IC_AID_VERSION = "IC_AID_VERSION";//AID参数版本
        public final static String IC_CAPK_VERSION = "IC_CAPK_VERSION";//公钥参数版本
        public final static String FLAG_TMK_EXIST = "FLAG_TMK_EXIST";//TMK存在的标识
        public final static String CAN_USE_ELECTRONIC_SIGN = "CAN_USE_ELECTRONIC_SIGN";//启动电子签名
        public final static String FIRST_TIME_LOADING = "FIRST_TIME_LOADING";//启动电子签名
        public final static String REVERSE_RETRY_TIMES = "REVERSE_RETRY_TIMES";//冲正重试次数
        public final static String LOCATION_LATITUDE = "LOCATION_LATITUDE";//纬度
        public final static String LOCATION_LONGTITUDE = "LOCATION_LONGTITUDE";//经度
        public static final String QPS_BIN_EXISTS = "QPS_BIN_EXISTS";

        public static final String FLAG_MOBILE_NET = "FLAG_MOBILE_NET";   //网络类型，只使用移动网络

        //业务参数标志，控制初始业务参数的下载情况，包括IC卡公钥、参数、非接、卡BIN等
        public static final String TRADE_PARAMETER_FLAG = "TRADE_PARAMATER_FLAG";

        public static final String PROCESS_REQUEST = "PROCESS_REQUEST_FLAG";
        //记录签到时间，用于控制自动签到
        public static final String SIGN_IN_DATE = "SIGN_IN_DATE";

        //加密算法，DES 3DES SM4
        public static final String ENCRYPT_ALGORITHM = "encrypt algorithm";

        public final static String UI_CHANNEL_VALUE = "UI_CHANNEL_VALUE";//UI
    }

    private static SharedPreferences getDefaultPres(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static String getStringMetaData(Context context, String key) {
        ApplicationInfo info;
        try {
            info = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager
                    .GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return info.metaData.getString(key);
    }

    private static int getIntMetaData(Context context, String key) {
        ApplicationInfo info = null;
        try {
            info = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager
                    .GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
        int value = info.metaData.getInt(key);
        return value;
    }

    public static void clearSetting(Context context) {
        SharedPreferences.Editor editor = getDefaultPres(context).edit();
        boolean isAppInit = isAppInit(context);
        String projectName = getProjectName(context);
        String prjDbVersion = getValue(context, SimpleStringTag.PROJECT_DB_VERSION, "0");
        editor.clear();
        if (isAppInit)
            editor.putBoolean(KEY.FLAG_INIT, true);
        if (!TextUtils.isEmpty(projectName))
            editor.putString(KEY.PROJECT_NAME, projectName);
        if (!TextUtils.isEmpty(prjDbVersion))
            editor.putString(SimpleStringTag.PROJECT_DB_VERSION, prjDbVersion);
        editor.commit();
    }

    /**
     * 获取当前渠道名称
     *
     * @param context Context
     * @return AID参数版本
     */
    public static String getProjectName(Context context) {
        String channel = getDefaultPres(context).getString(KEY.PROJECT_NAME, null);
        if (TextUtils.isEmpty(channel)) {
            channel = getStringMetaData(context, KEY.PROJECT_NAME);
        }
        return channel;
    }

    /**
     * 设置当前渠道（异步）
     *
     * @param context Context
     * @param projectName 渠道
     */
    public static void setProjectName(Context context, String projectName) {
        if (projectName != null) {
            getDefaultPres(context).edit().putString(KEY.PROJECT_NAME, projectName).commit();
        }
    }

    public static void setVersionUpdateInfo(Context context, AppInfo info) {
        if (context == null) {
            return;
        }
        Context appContext = context.getApplicationContext();
        SharedPreferences sp = appContext.getSharedPreferences("app_version", Context.MODE_PRIVATE);
        if (info != null) {
            sp.edit().putString("appName", info.getAppName())
                    .putString("downloadUrl", info.getDownloadUrl())
                    .putInt("id", info.getId())
                    .putBoolean("forceUpdate", info.isForceUpdate())
                    .putString("describe", info.getDescribe())
                    .putString("version", info.getVersion())
                    .putString("platform", info.getPlatform())
                    .commit();
        } else {
            sp.edit().clear().commit();
        }
    }

    public static AppInfo getVersionUpdateInfo(Context context) {
        if (context == null) {
            return null;
        }
        Context appContext = context.getApplicationContext();
        SharedPreferences sp = appContext.getSharedPreferences("app_version", Context.MODE_PRIVATE);
        String appName = sp.getString("appName", null);
        String downloadUrl = sp.getString("downloadUrl", null);
        if (TextUtils.isEmpty(appName) || TextUtils.isEmpty(downloadUrl)) {
            return null;
        }
        AppInfo info = new AppInfo(
                sp.getString("appName", null),
                sp.getString("downloadUrl", null),
                sp.getInt("id", -1),
                sp.getBoolean("forceUpdate", false),
                sp.getString("describe", null),
                sp.getString("version", null),
                sp.getString("platform", null));
        return info;
    }

    /**
     * 获取IP地址
     *
     * @param context Context
     * @return IP地址
     */
    public static String getCommonIp(Context context) {
        if(BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), BusinessConfig.Key.USE_REVERVE_COMMON)){
            if(TextUtils.isEmpty(BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), TransDataKey.key_address_reserve))){
                return CommonUtils.ADDRESS_RESERVE;
            }else {
                return BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), TransDataKey.key_address_reserve);
            }
        }
        PreferDataPool defaultPool = ConfigureManager.getInstance(context).getDefaultParamsPool(context);
        String value = getDefaultPres(context).getString(KEY.COMMON_IP, defaultPool.getString(Keys.obj().comm_ip));
        if (value == null) {
            value = "";
        }
        return value;
    }

    /**
     * 获取IP地址
     *
     * @param context Context
     * @return IP地址
     */
    public static String getCommonIp2(Context context) {
        PreferDataPool defaultPool = ConfigureManager.getInstance(context).getDefaultParamsPool(context);
        String value = getDefaultPres(context).getString(KEY.COMMON_IP, defaultPool.getString(Keys.obj().comm_ip));
        if (value == null) {
            value = "";
        }
        return value;
    }

    public static String getCpayCommonIp(Context context) {
        PreferDataPool defaultPool = ConfigureManager.getInstance(context).getDefaultParamsPool(context);
        String value = getDefaultPres(context).getString(KEY.CPAY_COMMON_IP, defaultPool.getString(Keys.obj()
                .comm_cpay_ip));
        if (value == null) {
            value = "";
        }
        return value;
    }

    /**
     * 设置IP地址（同步）
     *
     * @param context Context
     * @param ip IP
     */
    public static void setCommonIp(Context context, String ip) {
        if (!TextUtils.isEmpty(ip)) {
            getDefaultPres(context).edit().putString(KEY.COMMON_IP, ip).commit();
        }
    }

    public static void setCpayCommonIp(Context context, String ip) {
        if (!TextUtils.isEmpty(ip)) {
            getDefaultPres(context).edit().putString(KEY.CPAY_COMMON_IP, ip).commit();
        }
    }


    public static String getCommonTPDU(Context context) {
        PreferDataPool defaultPool = ConfigureManager.getInstance(context).getDefaultParamsPool(context);
        String tpdu = getDefaultPres(context).getString(KEY.COMMON_TPDU, defaultPool.getString(Keys.obj().comm_tpdu));
        if (TextUtils.isEmpty(tpdu))
            return "";
        return tpdu;
    }

    public static String getCpayCommonTPDU(Context context) {
        PreferDataPool defaultPool = ConfigureManager.getInstance(context).getDefaultParamsPool(context);
        String tpdu = getDefaultPres(context).getString(KEY.CPAY_COMMON_TPDU, defaultPool.getString(Keys.obj()
                .comm_cpay_tpdu));
        if (TextUtils.isEmpty(tpdu))
            return "";
        return tpdu;
    }


    public static void setCommonTPDU(Context context, String tpdu) {
        if (!TextUtils.isEmpty(tpdu)) {
            getDefaultPres(context).edit().putString(KEY.COMMON_TPDU, tpdu).commit();
        }
    }

    public static void setCpayCommonTPDU(Context context, String tpdu) {
        if (!TextUtils.isEmpty(tpdu)) {
            getDefaultPres(context).edit().putString(KEY.CPAY_COMMON_TPDU, tpdu).commit();
        }
    }

    /**
     * 获取端口号
     *
     * @param context Context
     * @return 服务器端口号
     */
    public static int getCommonPort(Context context) {
        if(BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), BusinessConfig.Key.USE_REVERVE_COMMON)){
            String port = "0000";
            if(TextUtils.isEmpty(BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), TransDataKey.key_port_reserve))){
                port = CommonUtils.PORT_RESERVE;
            }else {
                port = BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), TransDataKey.key_port_reserve);
            }
            return Integer.parseInt(port);
        }
        PreferDataPool defaultPool = ConfigureManager.getInstance(context).getDefaultParamsPool(context);
        int value = getDefaultPres(context).getInt(KEY.COMMON_PORT, defaultPool.getInt(Keys.obj().comm_port));
        if (value < 0) {
            value = 0;
        }
        return value;
    }

    /**
     * 获取端口号
     *
     * @param context Context
     * @return 服务器端口号
     */
    public static int getCommonPort2(Context context) {
        PreferDataPool defaultPool = ConfigureManager.getInstance(context).getDefaultParamsPool(context);
        int value = getDefaultPres(context).getInt(KEY.COMMON_PORT, defaultPool.getInt(Keys.obj().comm_port));
        if (value < 0) {
            value = 0;
        }
        return value;
    }

    public static int getCpayCommonPort(Context context) {
        PreferDataPool defaultPool = ConfigureManager.getInstance(context).getDefaultParamsPool(context);
        int value = getDefaultPres(context).getInt(KEY.CPAY_COMMON_PORT, defaultPool.getInt(Keys.obj().comm_cpay_port));
        if (value < 0) {
            value = 0;
        }
        return value;
    }


    public static void setCommMsgEncrypt(Context context, boolean isEncrypt) {
        getDefaultPres(context).edit().putBoolean(KEY.COMMON_MSG_ENCRYPT, isEncrypt).commit();
    }

    public static void setCommCpayMsgEncrypt(Context context, boolean isEncrypt) {
        getDefaultPres(context).edit().putBoolean(KEY.CPAY_COMMON_MSG_ENCRYPT, isEncrypt).commit();
    }

    public static boolean isCommMsgEncrypt(Context context) {
        PreferDataPool defaultPool = ConfigureManager.getInstance(context).getDefaultParamsPool(context);
        boolean value = getDefaultPres(context).getBoolean(KEY.COMMON_MSG_ENCRYPT, defaultPool.getBoolean(Keys.obj()
                .is_msg_encrypt));
        return value;
    }

    public static boolean isCommCpayMsgEncrypt(Context context) {
        PreferDataPool defaultPool = ConfigureManager.getInstance(context).getDefaultParamsPool(context);
        boolean value = getDefaultPres(context).getBoolean(KEY.CPAY_COMMON_MSG_ENCRYPT, defaultPool.getBoolean(Keys
                .obj().is_cpay_msg_encrypt));
        return value;
    }

    public static int getCommType(Context context) {
        PreferDataPool defaultPool = ConfigureManager.getInstance(context).getDefaultParamsPool(context);
        return getDefaultPres(context).getInt(KEY.COMM_TYPE, mapCommTypeValue(defaultPool.getString(Keys.obj()
                .comm_type)));
    }

    public static int getDefaultCommType(Context context){
        PreferDataPool defaultPool = ConfigureManager.getInstance(context).getDefaultParamsPool(context);
        return mapCommTypeValue(defaultPool.getString(Keys.obj().comm_type));
    }

    private static int mapCommTypeValue(String string) {
        if (TextUtils.isEmpty(string))
            return ICommunication.COMM_TCP;
        switch (string.toLowerCase()) {
            case "http":
                return ICommunication.COMM_HTTP;
            case "https":
                return ICommunication.COMM_HTTPS;
            case "tcp":
                return ICommunication.COMM_TCP;
            case "tcps":
                return ICommunication.COMM_TCPS;
            case "uart":
                return ICommunication.COMM_UART;
        }
        return ICommunication.COMM_TCP;
    }

    public static int getCpayCommType(Context context) {
        PreferDataPool defaultPool = ConfigureManager.getInstance(context).getDefaultParamsPool(context);
        return getDefaultPres(context).getInt(KEY.CPAY_COMM_TYPE, mapCommTypeValue(defaultPool.getString(Keys
                .obj().comm_cpay_type)));
    }

    /**
     * 设置端口号（同步）
     *
     * @param context Context
     * @param port 端口号
     */
    public static void setCommonPort(Context context, int port) {
        if (!(port < 0 || port > 65535)) {
            getDefaultPres(context).edit().putInt(KEY.COMMON_PORT, port).commit();
        }
    }

    public static void setCpayCommonPort(Context context, int port) {
        if (!(port < 0 || port > 65535)) {
            getDefaultPres(context).edit().putInt(KEY.CPAY_COMMON_PORT, port).commit();
        }
    }

    public static boolean isMobileNetworkOnly(Context context) {
        PreferDataPool defaultPool = ConfigureManager.getInstance(context).getDefaultParamsPool(context);
        boolean result = getDefaultPres(context).getBoolean(KEY.FLAG_MOBILE_NET, defaultPool.getBoolean(Keys.obj()
                .is_mobile_net_only));
        logger.debug("^_^ isMobileNetworkOnly:" + result + " ^_^");
        return result;
    }

    public static void setMobileNetworkOnly(Context context, boolean isOnly) {
        getDefaultPres(context).edit().putBoolean(KEY.FLAG_MOBILE_NET, isOnly).commit();
    }

    /**
     * 获取签购单上送IP地址
     *
     * @param context Context
     * @return IP地址
     */
    public static String getSlipIp(Context context) {
        String value = getDefaultPres(context).getString(KEY.SLIP_IP, null);
        if (value == null) {
            value = getStringMetaData(context, KEY.SLIP_IP);
        }
        return value;
    }

    /**
     * 设置签购单上送IP地址
     *
     * @param context Context
     * @param ip IP
     */
    public static void setSlipIp(Context context, String ip) {
        if (!TextUtils.isEmpty(ip)) {
            getDefaultPres(context).edit().putString(KEY.SLIP_IP, ip).commit();
        }
    }


    /**
     * 设置签购单上送端口号（同步）
     *
     * @param context Context
     * @param port 端口号
     */
    public static void setSlipPort(Context context, int port) {
        if (!(port < 0 || port > 65535)) {
            getDefaultPres(context).edit().putInt(KEY.SLIP_PORT, port).commit();
        }
    }

    /**
     * 获取签购单上送端口号
     *
     * @param context Context
     * @return 服务器端口号
     */
    public static int getSlipPort(Context context) {
        int value = getDefaultPres(context).getInt(KEY.SLIP_PORT, -1);
        if (value == -1) {
            value = getIntMetaData(context, KEY.SLIP_PORT);
        }
        return value;
    }

    public static String getSlipUploadUrl(Context context) {
//        return "http://" + getSlipIp(context) + ":" + getSlipPort(context) + "/mis/PicUploadAction.asp";
        String url = getDefaultPres(context).getString(KEY.SLIP_UPLOAD_URL, null);
        if (TextUtils.isEmpty(url)) {
            url = getStringMetaData(context, KEY.SLIP_UPLOAD_URL);
            return url;
        }
        return url;
    }

    public static void setSlipUploadUrl(Context context, String url) {
        getDefaultPres(context).edit().putString(KEY.SLIP_UPLOAD_URL, url).commit();
    }


    /**
     * 获取版本检测端口号
     *
     * @param context Context
     * @return 服务器端口号
     */
    public static int getVersionPort(Context context) {
        int value = getDefaultPres(context).getInt(KEY.VERSION_PORT, -1);
        if (value == -1) {
            value = getIntMetaData(context, KEY.VERSION_PORT);
        }
        return value;
    }

    /**
     * 设置版本检测端口号（同步）
     *
     * @param context Context
     * @param port 端口号
     */
    public static void setVersionPort(Context context, int port) {
        if (!(port < 0 || port > 65535)) {
            getDefaultPres(context).edit().putInt(KEY.VERSION_PORT, port).commit();
        }
    }

    public static String getVersionCheckUrl(Context context) {
//        return "http://" + getCommonIp(context) + ":" + getVersionPort(context) + "/beta/app/api/app/android/version";
        String url = getDefaultPres(context).getString(KEY.VERSION_CHECK_URL, null);
        if (TextUtils.isEmpty(url)) {
            return getStringMetaData(context, KEY.VERSION_CHECK_URL);
        }
        return url;
    }

    public static void setVersionCheckUrl(Context context, String url) {
        getDefaultPres(context).edit().putString(KEY.VERSION_CHECK_URL, url).commit();
    }

    /**
     * 获取网络响应超时时间
     *
     * @param context Context
     * @return 网络超时时间
     */
    public static int getRespTimeout(Context context) {
        return getDefaultPres(context).getInt(KEY.NET_RESP_TIMEOUT, BusinessConfig.NET_RESP_TIMEOUT);
    }

    /**
     * 设置网络响应超时时间
     *
     * @param context Context
     * @param timeout 超时时间（毫秒）
     */
    public static void setRespTimeout(Context context, int timeout) {
        if (timeout > 0) {
            getDefaultPres(context).edit().putInt(KEY.NET_RESP_TIMEOUT, timeout).apply();
        }
    }

    /**
     * 获取连接超时时间
     *
     * @param context context
     * @return 连接超时时间
     */
    public static int getConnectTimeout(Context context) {
        return getDefaultPres(context).getInt(KEY.NET_CONNECT_TIMEOUT, BusinessConfig.NET_CONNECT_TIMEOUT);
    }

    /**
     * 设置连接超时时间
     *
     * @param context context
     * @return 连接超时时间
     */
    public static void setConnectTimeout(Context context, int timeout) {
        if (timeout > 0) {
            getDefaultPres(context).edit().putInt(KEY.NET_CONNECT_TIMEOUT, timeout).apply();
        }
    }


    /**
     * 判断程序上次退出是否为正常退出
     *
     * @param context Context
     * @return 是否正常退出的标志
     */
    public static boolean isLastNormalExit(Context context) {
        boolean isNormal = getDefaultPres(context).getBoolean(KEY.NORMAL_EXIT_FLAG, false);
        if (isNormal) {
            resetNormalExitFlag(context);
        }
        logger.info("上次程序是否正常退出：" + isNormal);
        return isNormal;
    }

    /**
     * 重置程序正常退出的标志
     *
     * @param context Context
     */
    private static void resetNormalExitFlag(Context context) {
        getDefaultPres(context).edit().putBoolean(KEY.NORMAL_EXIT_FLAG, false).commit();
    }

    /**
     * 将程序标识为正常退出，该方法需在{@link MainActivity#onDestroy()}方法中调用
     * 如果程序非正常退出，下次进入时需要重新签到
     */
    public static void setNormalExitFlag(Context context) {
        getDefaultPres(context).edit().putBoolean(KEY.NORMAL_EXIT_FLAG, true).commit();
    }

    /**
     * 获取签购单版本号
     *
     * @param context Context
     * @return 签购单版本号
     */
    public static String getSlipVersion(Context context) {
        return getDefaultPres(context).getString(KEY.SLIP_VERSION, "000000000000");
    }

    /**
     * 设置当前签购单版本号
     *
     * @param context Context
     * @param slipVersion 签购单版本号
     */
    public static void setSlipVersion(Context context, String slipVersion) {
        logger.info("正在保存新签购单版本：" + slipVersion);
        getDefaultPres(context).edit().putString(KEY.SLIP_VERSION, slipVersion).apply();
    }

    /**
     * 获取是否打印三联
     *
     * @param context Context
     */
    public static String getPrintThree(Context context) {
        return getDefaultPres(context).getString(KEY.PRINT_THREE, "N");
    }

    /**
     * 设置是否打印三联
     *
     * @param context Context
     * @param isPrintThree 是否打印三联
     */
    public static void setPrintThree(Context context, String isPrintThree) {
        getDefaultPres(context).edit().putString(KEY.PRINT_THREE, isPrintThree).apply();
    }

    /**
     * 获取是否脱机消费
     *
     * @param context Context
     */
    public static String getOffLineConsume(Context context) {
        return getDefaultPres(context).getString(KEY.OFF_LINE_CONSUME, "N");
    }

    /**
     * 设置是否脱机消费
     *
     * @param context Context
     * @param isOffLineConsume 是否脱机消费
     */
    public static void setOffLineConsume(Context context, String isOffLineConsume) {
        getDefaultPres(context).edit().putString(KEY.OFF_LINE_CONSUME, isOffLineConsume).apply();
    }

    /**
     * 获取保存的8583域数据。其中41域（受卡机终端标识码），42域（受卡方标识码），43域（商户名称）
     *
     * @param context context
     * @param fieldIndex 域的索引值
     * @return 已保存的该域的值
     */
    public static String getIsoField(Context context, int fieldIndex) {
        return getDefaultPres(context).getString("Iso" + fieldIndex, null);
    }

    /**
     * 保存8583数据域
     *
     * @param context    context
     * @param fieldIndex 域索引
     * @param value      值
     */
/*    public static void setIsoField(Context context, int fieldIndex, String value) {
        getDefaultPres(context).edit().putString("Iso" + fieldIndex, value.trim()).apply();
    }*/

    /**
     * 获取当前操作员ID。如果当前操作员ID为空，则判断为操作员未登录，无法进行交易
     *
     * @param context context
     * @return 操作员ID
     */
    public static String getOperId(Context context) {
        return getDefaultPres(context).getString(KEY.OPER_ID, null);
    }

    public static void setOperId(Context context, String id) {
        getDefaultPres(context).edit().putString(KEY.OPER_ID, id).commit();
    }

    public static void setValue(Context context, String key, String value) {
        getDefaultPres(context).edit().putString(key, value).apply();
    }

    public static String getValue(Context context, String key, String defValue) {
        return getDefaultPres(context).getString(key, defValue);
    }

    public static void setValue(Context context, String key, boolean value) {
        getDefaultPres(context).edit().putBoolean(key, value).apply();
    }

    public static boolean getValue(Context context, String key, boolean defValue) {
        return getDefaultPres(context).getBoolean(key, defValue);
    }

    public static void setValue(Context context, String key, int count) {
        getDefaultPres(context).edit().putInt(key, count).apply();
    }

    public static int getValue(Context context, String key, int count) {
        return getDefaultPres(context).getInt(key, count);
    }

    /**
     * 获取TMK存在的标识
     *
     * @param context context
     * @return 是否有TMK
     */
    public static boolean hasTmk(Context context) {
        return getDefaultPres(context).getBoolean(FLAG_TMK_EXIST, false);
    }

    /**
     * 设置TMK已存在
     */
    public static void setTmkExist(Context context) {
        getDefaultPres(context).edit().putBoolean(FLAG_TMK_EXIST, true).apply();
    }

    public static boolean isAppInit(Context context) {
        return getValue(context, KEY.FLAG_INIT, false);
    }

    public static void setAppInit(Context context, boolean flag) {
        setValue(context, KEY.FLAG_INIT, flag);
    }

    public static void setNewPayChannel(String channelName) {
        setValue(EposApplication.getAppContext(), NEW_CHANNEL, channelName);
    }

    public static String getNewPayChannel(Context context) {
        return getValue(context, NEW_CHANNEL, null);
    }

    /**
     * 获取业务参数控制标志，目前完整的参数包含 IC卡公钥下载->IC卡参数下载->QPS参数下载->BIN表B下载->BIN表C下载
     *
     * @return 0   无业务参数
     * 1   正在进行IC卡公钥下载
     * 2   正在进行IC卡参数下载
     * 3   正在进行QPS参数下载
     * 4   正在进行BIN表B下载
     * 5   正在进行BIN表C下载
     * 100   完成所有业务参数下载
     */
    public static int getTradeParameterFlag() {
        return getValue(EposApplication.getAppContext(), KEY.TRADE_PARAMETER_FLAG, 0);
    }

    public static void setTradePrameterFlag(int newFlag) {
        setValue(EposApplication.getAppContext(), KEY.TRADE_PARAMETER_FLAG, newFlag);
    }

    /**
     * 获取加密算法标识
     *
     * @param context 上下文
     * @return 算法
     */
    public static String getEncryptAlgorithm(Context context) {
        PreferDataPool defaultPool = ConfigureManager.getInstance(context).getDefaultParamsPool(context);
        return getDefaultPres(context).getString(KEY.ENCRYPT_ALGORITHM, defaultPool.getString(Keys.obj()
                .encrypt_algorithm));
    }

    public static EncryptAlgorithmEnum getEncryptAlgorithmEnum(Context context) {
        PreferDataPool defaultPool = ConfigureManager.getInstance(context).getDefaultParamsPool(context);
        String alg = getDefaultPres(context).getString(KEY.ENCRYPT_ALGORITHM, defaultPool.getString(Keys.obj()
                .encrypt_algorithm));
        return EncryptAlgorithmEnum.nameOf(alg);
    }

    /**
     * 设置加密算法
     *
     * @param context 上下文
     * @param algorithm 算法
     */
    public static void setEncryptAlgorithm(Context context, String algorithm) {
        if (TextUtils.isEmpty(algorithm) || !("DES".equalsIgnoreCase(algorithm) || "3DES".equalsIgnoreCase(algorithm)
                || "SM4".equalsIgnoreCase(algorithm))) {
            return;
        }
        getDefaultPres(context).edit().putString(KEY.ENCRYPT_ALGORITHM, algorithm).commit();
    }

    public static boolean isSupportElesign() {
        boolean result = BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), SimpleStringTag
                .TOGGLE_ESIGN_SUPPORT);
        return result;
    }

    /**
     * 获取当前渠道名称
     *默认为蓝色
     * @param context Context
     * @return UI类型
     */
    public static String getUIChannel(Context context) {
        uiChannel = getStringMetaData(context, KEY.UI_CHANNEL_VALUE);
//        uiChannel = BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), SimpleStringTag
//                .APP_THEME_TAG);
        XLogUtil.d("getUIChannel"," "+uiChannel);
        uiChannel = (uiChannel==null) ? "blue" : uiChannel;

        return uiChannel;
    }

    public static boolean bIsSettingBlueTheme()
    {
        /*
        * 防止直接调用该接口
        * */
        if( null == uiChannel ){
            uiChannel = "blue" ;
            XLogUtil.w("bIsSettingBlueTheme","uiChannel init is null !!!");
        }

        return uiChannel.toLowerCase().equals("blue");
    }
}
