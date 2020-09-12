package com.centerm.epos.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Environment;
import android.text.TextUtils;

import com.centerm.cloudsys.sdk.common.utils.PackageUtils;
import com.centerm.cloudsys.sdk.common.utils.StringUtils;
import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;
import com.centerm.cpay.midsdk.dev.define.IPinPadDev;
import com.centerm.cpay.midsdk.dev.define.IPrinterDev;
import com.centerm.cpay.midsdk.dev.define.ISystemService;
import com.centerm.cpay.midsdk.dev.define.pinpad.PinPadConfig;
import com.centerm.cpay.midsdk.dev.define.printer.EnumPrinterStatus;
import com.centerm.epos.EposApplication;
import com.centerm.epos.bean.BinData;
import com.centerm.epos.bean.PrinterItem;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.receiver.BatteryReceiver;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import config.BusinessConfig;
import config.KeyIndexConfig;
import jxl.Sheet;
import jxl.Workbook;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * author:wanliang527</br>
 * date:2016/10/28</br>
 */

public class CommonUtils {
    private static long lastClickTime;
    private static Logger logger = Logger.getLogger(CommonUtils.class);
    public static final String SN_CODE = "";//sn号 D1V0590002138
    public static final String ADDRESS_SCAN = "211.144.216.164";
    public static final String PORT_SCAN = "8877";

    public static final String ADDRESS_SCAN_RESERVE = "211.144.212.78";//扫码服务器地址备用
    public static final String PORT_SCAN_RESERVE = "8877";
    public static final String ADDRESS_RESERVE = "211.144.216.168";//服务器地址备用
    public static final String PORT_RESERVE = "3500";

    public static final String ADDRESS_GT = "http://znsk.gtcloud.cn";
    public static final String PORT_GT = "";

    public static final String SALT = "FHUR!HFR&N2H19!#@^@!&*!";

    /**
     * 根据2磁道信息判断是否是IC卡
     *
     * @param track2data 2磁道信息
     * @return IC卡返回true，否则返回false
     */
    public static boolean isIcCard(String track2data) {
        if ("".equals(track2data) || track2data == null) {
            return false;
        }
        if ((!track2data.contains("=")) && (!track2data.contains("D"))) {
            return false;
        }
        String temp[];
        String key;
        if (track2data.contains("=")) {
            temp = track2data.split("=");
            if (5 < temp[1].length()) {
                key = temp[1].substring(4, 5);
            } else {
                return false;
            }
        } else if (track2data.contains("D")) {
            temp = track2data.split("D");
            if (5 < temp[1].length()) {
                key = temp[1].substring(4, 5);
            } else {
                return false;
            }
        } else {
            return false;
        }
        return "2".equals(key) || "6".equals(key);
    }

    /**
     * 加密磁道数据
     *
     * @param trackInfo 待加密磁道信息
     * @return 加密后的磁道信息
     */
    public static String encryTrackData(String trackInfo) {
        if (TextUtils.isEmpty(trackInfo)) {
            logger.warn("磁道信息为空无法加密");
            return null;
        }
        IPinPadDev pinPad = CommonUtils.getPinPadDev();
        if (pinPad == null || TextUtils.isEmpty(trackInfo)) {
            logger.warn("密码键盘异常，无法加密磁道信息，返回全F");
            return "FFFFFFFFFFFFFFFF";
        }
        String track = trackInfo.length() % 2 == 0 ? trackInfo : trackInfo + "0";
        int len = track.length();
        String waitEncryData = null;
        String encryData = null;
        String finalData = null;
        if (len <= 16) {
            waitEncryData = track;
            encryData = HexUtils.bytesToHexString(pinPad.encryData(null, null, waitEncryData));
            finalData = encryData;
        } else if (len == 18) {
            waitEncryData = track.substring(0, 16);
            encryData = HexUtils.bytesToHexString(pinPad.encryData(null, null, waitEncryData));
            finalData = encryData + track.substring(16, 18);
        } else {
            waitEncryData = track.substring(len - 18, len - 2);
            encryData = HexUtils.bytesToHexString(pinPad.encryData(null, null, waitEncryData));
            finalData = track.substring(0, len - 18) + encryData + track.substring(len - 2, len);
        }
        logger.debug("原数据：" + trackInfo);
        logger.debug("待加密数据段：" + waitEncryData);
        logger.debug("加密后数据：" + encryData);
        logger.debug("最终磁道数据：" + finalData);
        return finalData;


   /*     int mod = trackInfo.length() % 16;
        if (mod == 0) {
            trackInfo = DataHelper.addFlast(trackInfo, trackInfo.length() + 16);
        } else {
            trackInfo = DataHelper.addFlast(trackInfo, trackInfo.length() + 16 - mod);
        }*/



       /* logger.debug("原磁道信息：" + trackInfo);
        byte[] originData = HexUtils.hexStringToByte(trackInfo);
        logger.debug("16进制字节数组：" + HexUtils.bytesToHexString(originData));*/
  /*      int len = originData == null ? 0 : originData.length;
        if (len < 8) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                baos.write(originData);
                for (int i = 0; i < 8 - len; i++) {
                    baos.write((byte) 0xFF);
                }
                String tdb = HexUtils.bytesToHexString(baos.toByteArray());
                logger.debug("待加密的数据：" + tdb);
                logger.debug("加密后的数据：" + tdb);
                return tdb;
            } catch (IOException e) {
                e.printStackTrace();
                return trackInfo;
            }
        } else {
            IPinPadDev pinpadDev = CommonUtils.getPinPadDev();
            if (pinpadDev == null) {
                logger.warn("密码键盘异常，无法加密磁道信息，返回全F");
                return "FFFFFFFFFFFFFFFF";
            }
            if (len == 8) {
                logger.debug("待加密数据：" + HexUtils.bytesToHexString(originData));
                String encryData = HexUtils.bytesToHexString(pinpadDev.encryData(null, null, HexUtils
                .bytesToHexString(originData)));
                logger.debug("加密后的数据：" + encryData);
                return encryData;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (len - 10 > 0) {
                //前面的字节不加密
                baos.write(originData, len - 10, len - 9);
            }
            byte[] tdb = new byte[8];
            System.arraycopy(originData, len - 9, tdb, 0, 8);
            logger.debug("待加密的数据：" + HexUtils.bytesToHexString(tdb));
            try {
                baos.write(pinpadDev.encryData(null, null, HexUtils.bytesToHexString(tdb)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            //最后一字节不加密
            baos.write(originData, len - 1, 1);
            String encryData = HexUtils.bytesToHexString(baos.toByteArray());
            logger.debug("加密后的数据：" + encryData);
            return encryData;
        }*/

    }


    /**
     * 获取密码键盘设备对象
     */
    public static IPinPadDev getPinPadDev() {
        try {
            IPinPadDev pinPadDev = DeviceFactory.getInstance().getPinPadDev();
            PinPadConfig config = new PinPadConfig();
            KeyIndexConfig keyConfig = new KeyIndexConfig();
//            EnumChannel channel = EposApplication.posChannel;
//            config.setTmkIndex(keyConfig.getIndex(channel, KeyIndexConfig.KeyType.TMK));
//            config.setMakIndex(keyConfig.getIndex(channel, KeyIndexConfig.KeyType.MAK));
//            config.setTdkIndex(keyConfig.getIndex(channel, KeyIndexConfig.KeyType.TDK));
//            config.setPikIndex(keyConfig.getIndex(channel, KeyIndexConfig.KeyType.PIK));
            config.setTmkIndex(BusinessConfig.getInstance().getNumber(EposApplication.getAppContext(), BusinessConfig
                    .Key
                    .MAINKEYINDEX));//生厂
            config.setMinLen(4);
//            config.setTmkIndex(7);//测试
            pinPadDev.config(config);
            return pinPadDev;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取密码键盘设备对象
     */
    public static IPinPadDev getPinPadDev(Context context) {
        try {
            IPinPadDev pinPadDev = DeviceFactory.getInstance().getPinPadDev();
            PinPadConfig config = new PinPadConfig();
            KeyIndexConfig keyConfig = new KeyIndexConfig();
//            EnumChannel channel = EposApplication.posChannel;
//            config.setTmkIndex(keyConfig.getIndex(channel, KeyIndexConfig.KeyType.TMK));
//            config.setMakIndex(keyConfig.getIndex(channel, KeyIndexConfig.KeyType.MAK));
//            config.setTdkIndex(keyConfig.getIndex(channel, KeyIndexConfig.KeyType.TDK));
//            config.setPikIndex(keyConfig.getIndex(channel, KeyIndexConfig.KeyType.PIK));

            config.setTmkIndex(BusinessConfig.getInstance().getNumber(context, BusinessConfig.Key.MAINKEYINDEX));
            config.setMinLen(4);
            pinPadDev.config(config);
            return pinPadDev;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 判断是否为合法IP
     *
     * @return the ip
     */
    public static boolean isIp(String addr) {
        String[] addrs = addr.split("\\.");
        int len = addrs.length;
        String str = addr.substring(addr.length() - 1, addr.length());
        if (str.equals(".")) {
            return false;
        }
        if (len != 4) {
            return false;
        }
        boolean one = false, other = false;
        try {
            if (Integer.parseInt(addrs[0]) <= 223)
                one = true;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        for (int i = 1; i < len; i++) {
            try {
                if (Integer.parseInt(addrs[i]) < 256) {
                    other = true;
                } else {
                    return false;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                break;
            }
        }
        return one && other;
    }

    /**
     * 判断是否有网络连接
     *
     * @return 网络可用返回true，否则返回false
     */
    public static boolean isNetworkAvailable(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 读取excel数据到数据库里
     */
    public static boolean readExcelToDB(Context context) {
        try {
            List<BinData> datas = new ArrayList<>();
            CommonDao<BinData> commonDao = new CommonDao<>(BinData.class, DbHelper.getInstance());
            InputStream is = context.getAssets().open("binData.xls");
            Workbook book = Workbook.getWorkbook(is);
            book.getNumberOfSheets();
            // 获得第一个工作表对象
            Sheet sheet = book.getSheet(0);
            int rows = sheet.getRows();
            logger.debug("excel表格总共有" + rows + "条数据");
            BinData binData = null;
            for (int i = 1; i < rows; ++i) {
                String cardBinNo = (sheet.getCell(0, i)).getContents();
                String cardBinLenth = (sheet.getCell(1, i)).getContents();
                String cardBin = (sheet.getCell(2, i)).getContents();
                String cardNumberLenth = (sheet.getCell(3, i)).getContents();
                String cardType = (sheet.getCell(4, i)).getContents();
                String orgNo = (sheet.getCell(5, i)).getContents();
                String cardOrg = (sheet.getCell(6, i)).getContents();
                binData = new BinData(cardBinNo.trim(), cardBinLenth.trim(), cardBin.trim(), cardNumberLenth.trim(),
                        cardType.trim(), orgNo.trim(), cardOrg.trim());
                datas.add(binData);
                if (i % 50 == 0) {
                    commonDao.save(datas);
                    datas.clear();
                }
                //保存最后一条卡BIN的编号，用于联机更新BIN表时的起始编号
                if (i == rows - 1) {
                    int no = Integer.valueOf(cardBinNo.trim());
                    logger.info("保存卡BIN表最后一条记录编号==>" + (++no));
                    BusinessConfig.getInstance().setValue(context, BusinessConfig.Key.KEY_LAST_BIN_NO, "" + no);
                }
            }
            datas.clear();
            book.close();
            DbHelper.releaseInstance();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        DbHelper.releaseInstance();
        return false;
    }


    /**
     * 判断终端是否处于充电状态
     *
     * @param context context
     * @return 充电状态返回true，否则返回false
     */
    public static boolean isOnCharging(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        return isCharging;
    }

    public static float getBatteryPercent(Context context) {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, filter);
        //当前剩余电量
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        //电量最大值
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        //电量百分比
        float batteryPct = level / (float) scale;
        return batteryPct;
    }

    public synchronized static boolean isFastClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 1200) {
            logger.warn("快速点击事件，不响应！");
            return true;
        }
        lastClickTime = time;
        return false;
    }

    public static void resetLastClickTime(){
        lastClickTime = 0;
    }

    public static boolean compareToUpdate(String oriVersion, Context context) {
        if (StringUtils.isStrNull(oriVersion) || oriVersion.length() < 3) {
            logger.debug("后台版本数据异常");
            return false;
        }
        String curVersion = PackageUtils.getInstalledVersionName(context, context.getPackageName());
        String platVersion = oriVersion.substring(1, oriVersion.length());
        int[] platVersionNum = stringVersionToNumber(platVersion);
        int[] curVersionNum = stringVersionToNumber(curVersion);
        if (platVersionNum[0] == -1) {
            logger.debug("后台返回版本号异常");
            return false;
        }
        if (curVersionNum[0] == -1) {
            logger.debug("当前版本号异常");
            return false;
        }
        if (platVersionNum[0] > curVersionNum[0]) {
            return true;
        }
        if (platVersionNum[1] > curVersionNum[1]) {
            return true;
        }
        if (platVersionNum[2] > curVersionNum[2]) {
            return true;
        }
        return false;
    }

    private static int[] stringVersionToNumber(String curVersion) {
        String[] strings = curVersion.split("\\.");
        int firstVersion = -1;
        int secVersion = -1;
        int thirdVersion = -1;
        if (StringUtils.isStrNull(strings[0]) || StringUtils.isStrNull(strings[1]) || StringUtils.isStrNull
                (strings[2])) {
            logger.error("版本号数据异常");
            return new int[]{-1, -1, -1};
        }
        try {
            firstVersion = Integer.parseInt(strings[0]);
            secVersion = Integer.parseInt(strings[1]);
            thirdVersion = Integer.parseInt(strings[2]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            logger.error("版本号数据转换异常");
            return new int[]{-1, -1, -1};
        }
        return new int[]{firstVersion, secVersion, thirdVersion};
    }

    public static void enableStatusBar(Context context) {
        if(!isK9()){
            return;
        }
        Object service = context.getSystemService("statusbar");
        try {
            Class<?> statusBarManager = Class.forName("android.app.StatusBarManager");
            Method expand = statusBarManager.getMethod("disable", int.class);
            expand.invoke(service, 0x00000000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void disableStatusBar(Context context) {
        if(!isK9()){
            return;
        }
        Object service = context.getSystemService("statusbar");
        try {
            Class<?> statusBarManager = Class.forName("android.app.StatusBarManager");
            Method expand = statusBarManager.getMethod("disable", int.class);
            expand.invoke(service, 0x00010000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean tradeEnvironmentCheck(Context context) {
        try {
            //return !BatteryReceiver.isLowPower(context) || BatteryReceiver.isCharging(context);
            return !BatteryReceiver.isLowPower(context);
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public static boolean isComponentExist(String componentName) {
        if (TextUtils.isEmpty(componentName)){
            return false;
        }
        PackageManager mPackageManager = EposApplication.getAppContext().getPackageManager();
        try {
            mPackageManager.getApplicationInfo(componentName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取sn号
     * */
    public static String getSn(){
        DeviceFactory factory = DeviceFactory.getInstance();
        try {
            ISystemService service = factory.getSystemDev();
            String sn = service.getTerminalSn();
            return sn;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 检测打印机状态，如果是缺纸，则提示装纸，其它错误则退出打印。
     *
     * @return 小于0则表示失败退出，-2表示缺纸，0表示状态正常
     */
    public static int checkPrinterState() {
        int state = -1;
        try {
            IPrinterDev printer = DeviceFactory.getInstance().getPrinterDev();
            EnumPrinterStatus status = printer.getPrinterStatus();
            if (EnumPrinterStatus.OK == status)
                state = 0;
            if (EnumPrinterStatus.NO_PAPER == status) {
                state = -2;
            }
            if (EnumPrinterStatus.BUSY == status) {
                state = -101;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return state;
    }

    public static boolean isK9(){
        Configuration mConfiguration = EposApplication.getAppContext().getResources().getConfiguration(); //获取设置的配置信息
        int ori = mConfiguration.orientation; //获取屏幕方向
        boolean isK9 = true;
        if (ori == mConfiguration.ORIENTATION_LANDSCAPE) {
            //横屏
            isK9 = false;
        } else if (ori == mConfiguration.ORIENTATION_PORTRAIT) {
            //竖屏
            isK9 = true;
        }
        return isK9;
    }

    public static void getMemory(Context context){
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        //最大分配内存
        int memory = activityManager.getMemoryClass();
        System.out.println("memory: "+memory);
        //最大分配内存获取方法2
        float maxMemory = (float) (Runtime.getRuntime().maxMemory() * 1.0/ (1024 * 1024));
        //当前分配的总内存
        float totalMemory = (float) (Runtime.getRuntime().totalMemory() * 1.0/ (1024 * 1024));
        //剩余内存
        float freeMemory = (float) (Runtime.getRuntime().freeMemory() * 1.0/ (1024 * 1024));
        System.out.println("maxMemory: "+maxMemory);
        System.out.println("totalMemory: "+totalMemory);
        System.out.println("freeMemory: "+freeMemory);
    }

    // 将指定文件写入SD卡,说明一下,应用程序的数据库是存放到/data/data/包名/databases 下面
    public static String toSDWriteFile(Context context, String fileName) throws IOException {
        // 获取应用包名
        String toFile = Environment.getExternalStorageDirectory()+ File.separator+"00_打印测试";
        File mSaveFile = new File(toFile);

        if (!mSaveFile.exists()) {
            mSaveFile.mkdirs();
        }
        String local_file = mSaveFile.getAbsolutePath() + "/" + fileName;

        mSaveFile = new File(local_file);

        if (mSaveFile.exists()) {
            return "";
        }
        mSaveFile.createNewFile();

        // 获取assets下的数据库文件流
        InputStream is = context.getAssets().open(fileName);
        FileOutputStream fos = new FileOutputStream(mSaveFile, true);

        byte[] buffer = new byte[1024];
        int count = 0;
        while ((count = is.read(buffer)) > 0) {
            fos.write(buffer, 0, count);
        }
        mSaveFile = null;
        fos.close();
        is.close();

        return local_file;
    }
    public static boolean isServiceRunning(Context context, String className) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(Integer.MAX_VALUE);
        if (!(serviceList.size() > 0)) {
            return false;
        }

        for (int i = 0; i < serviceList.size(); i++) {
            ActivityManager.RunningServiceInfo serviceInfo = serviceList.get(i);
            ComponentName serviceName = serviceInfo.service;
            if (serviceName.getClassName().equals(className)) {
                return true;
            }
        }
        return false;
    }

    public static void sortChinese (String[] arr){
        Comparator cmp = Collator.getInstance(java.util.Locale.CHINA);
        // 使根据指定比较器产生的顺序对指定对象数组进行排序。
        Arrays.sort(arr, cmp);
    }





}
