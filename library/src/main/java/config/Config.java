package config;

import android.os.Environment;

import com.centerm.epos.R;

import java.io.File;

/**
 * 通用配置
 * author:wanliang527</br>
 * date:2016/10/22</br>
 */

public class Config {
    //钱宝测试、仿真环境的TEK明文 96964CD2509D486DF21F2D15E5400701   634515529536
    public final static String SECRET_KEY_CODE = "11222131";
    public final static String SECRET_KEY_CODE2 = "33333333";//打印中dialog退出密钥

    public final static boolean DEBUG_FLAG = true;
    public final static String DEBUG_SN = "";//D1V0160000085//D1V0160000083//D1V0160002359//D1V0160002343
    //    public final static String ESIGN_UPLOAD_ADDRESS = "http://180.168.175.246:20117/mis/PicUploadAction.asp";//开发测试
//    public final static String ESIGN_UPLOAD_ADDRESS = "http://180.168.175.246:20103/mis/PicUploadAction.asp";//仿真测试
//    public final static String VERSION_CHECK_ADDRESS = "http://180.168.175.246:20121/beta/app/api/app/android/version";//应用版本检测的地址
    public final static int DEFAULT_MENU_ITEM_ICON = R.drawable.ic_launcher;

    public final static String WY_NOTICE_ADDRESS_TEST = "https://101.200.53.16/lfapi/api/pos/payResultNotice";
    public final static String WY_NOTICE_ADDRESS_PRO = "https://101.200.41.178/lfapi/api/pos/payResultNotice";

    public final static String DB_NAME = "epos.db";
    public final static int DB_VERSION = 23;
    public final static long TRADE_INFO_LIST_PAGE_SIZE = 20;
    public final static int MAX_RETRY_TIMES = 3;
    public final static long LOCATION_INTERVAL = 10 * 60 * 1000;//上一次定位成功，后续10分钟定位一次
    public final static long LOCATION_INTERVAL_SHORT = 1 * 60 * 1000;//上一次定位失败，后续1分钟定位一次
    public final static long APP_VERSION_CHECK_INTERVAL = 10 * 60 * 1000;//应用版本检测服务间隔
    public final static long UPLOAD_FAIL_ELEC_SIGN_INTERVAL = 1 * 60 * 1000;//检测上传电子签名图片
    public final static long PAGE_TIMEOUT = 30*1000;//页面超时时间

    public final static String TERMINAL_TYPE = "04";    //设备类型:智能 POS

//    public final static int E_SIGNATURE_PIC_MAX = 320;  //电子签名BMP图片的最大宽度

    public final static String BASE_PACKAGE_NAME = "com.centerm.epos";  //基础版本的包名
    public final static String DEFAULT_HOTLINE = "4007008010";  //客服电话
    public final static String DEFAULT_PROJECT_ID = "1";  //默认项目ID
    public final static boolean isEnableShowingTimeout = true;
    public final static int TIME_OUT = 180;
    public final static int TRADE_KEEP_DAY = 10;
    public final static int ROW_MAX_LENTH = 16;


    public static class Path {
        //E10->/storage/sdcard0/EPos
        //K9 -> /storage/emulated/0/EPos
        public final static String ROOT = Environment.getExternalStorageDirectory() + File.separator + "EPos";
        public final static String DOWNLOAD_PATH = ROOT + File.separator + "files";//下载文件存放目录
        public final static String RECEIPT_PATH = ROOT + File.separator + "receipts";//电子签购单存放目录
        public final static String SIGN_PATH = ROOT + File.separator + "elecsign";//电子签名图片存放目录
        public final static String VOUCHER_PATH = ROOT + File.separator + "voucher";//签购单图片存放目录
        public final static String SIGN_UPLOAD_FAILED_SUFFIX = "_failed.png";//电子签名上送失败文件后缀名
        public final static String CODE_PATH = ROOT + File.separator + "code";//二维码存放路径
        public final static String CODE_FILE_NAME = "erweima.png";//二维码文件名
        public final static String PDF_PATH = ROOT+File.separator +"pdf";//二维码文件名
//        public final static String DEFAULT_LOG_PATH = Environment.getExternalStorageDirectory() + File.separator
//                + "CentermAppLog"
//                + File.separator
//                + "com.centerm.epos";

        public final static String DEFAULT_LOG_PATH = Environment.getExternalStorageDirectory() + File.separator+"CentermAppLog";
    }

    public static class DebugToggle {
        public final static boolean UPLOAD_IC_SCRIPT = false;//是否开启IC卡脚本通知业务
    }

    public static class XML {
        public final static String PROCESS_PATH = "process/";
        public final static String MSG_DEFINE_PATH = "msg/define/";
        public final static String MSG_ADAPTER_PATH = "msg/adapter/";
    }

    public final static String SUPER_ADMIN_ACCOUNT = "88";//超级管理员账号
    public final static String SUPER_ADMIN_PWD = "12345678";//超级管理员密码
    public final static String DEFAULT_ADMIN_ACCOUNT = "99";//系统管理员账号
    public final static String DEFAULT_ADMIN_PWD = "12345678";//系统管理员密码
    public final static String DEFAULT_MSN_ACCOUNT = "00";//主管帐号
    public final static String DEFAULT_MSN_PWD = "123456";//主管密码

    public final static String OPT_NO_TIP = "OPT_NO_TIP";
    public final static String OPT_PWD_TIP = "OPT_PWD_TIP";
    public final static String OPT_TYPE_TIP = "OPT_TYPE_TIP";
    public final static String OPERATOR_NUM = "OPERATOR_NUM";

    public final static int OPT_TYPE_UPDATE = 1;
    public final static int OPT_TYPE_CREATE = 2;
    public final static int BATCH_MAX_UPLOAD_TIMES = 4;
    public final static int PRINT_NEXT_TIME = 4;//打印时自动打印下一联时间
    public final static int HTTP_RESPONSE_TIMEOUT = 60 * 1000;//默认响应超时时间
    public final static int HTTP_CONNECTION_TIMEOUT = 5 * 1000;//默认连接超时时间

    public final static long AD_SCROLL_TIME = 3 *1000;//广告自动轮播的时间

    public static final String appId = "1E69D2306DAFDA1E4603087562F6A2861D5DEC4244E8877CE303A510BC1E9EB091ACA45DABD9F29D";
    public static final String appSecret = "1240744630B92DC9EA48EB5F76A3A8D6FF323279A2C1A06F7B2FCAA3B02C9C369ECB880CC921A178";

}
