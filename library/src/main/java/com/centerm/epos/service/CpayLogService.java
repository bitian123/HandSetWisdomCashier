package com.centerm.epos.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.centerm.cloudsys.sdk.common.utils.FileUtils;

import org.apache.log4j.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * CPAY Log管理服务类。主要用于日志清理，防止日志文件堆积，占用大量SD卡空间。
 * Cpay应用日志存放目录：mnt/sdcard/CentermAppLog/[package_name]，正常情况下，该目录下不允许存放其它文件。
 * <p>
 * 1、该服务生命周期的开始和结束由各应用自行控制。
 * 2、日志检测和清理策略
 * （1）检测：
 * ————设定【日志文件空间】，可设定范围10~50MB，默认20MB。
 * ————服务启动检测+轮询检测，“轮询时间”可配置，默认12个小时。
 * ————判断日志目录下的总文件大小，若超出设定的日志文件空间，开始运行清理任务
 * （2）清理：
 * ————日志文件“保存天数”可配置，默认7天，范围1~30
 * ————开始清理应用保存天数外的日志文件，若遍历目录之后未发现符合的日志文件，则进入下一步判断；
 * ————判断应用日志目录下总文件大小，如果总文件大小超过【100MB】，则按照文件【最后修改时间】由后向前开始清理日志，直到小于【100MB】
 */
public class CpayLogService extends Service {


    public final static String KEY_MAX_FILE_SIZE = "KEY_MAX_FILE_SIZE";//日志文件最大存储空间，单位：MB
    public final static String KEY_MAX_EXITS_DAYS = "KEY_MAX_EXITS_DAYS";//日志文件最多存储天数，单位：天
    public final static String KEY_CHECK_INTERVAL = "KEY_CHECK_INTERVAL";//检测时间间隔，单位：小时

    private Logger logger = Logger.getLogger(CpayLogService.class);
    private final static String PATH = "CentermAppLog";

    private final static long FORCE_CLEAN_SIZE = 100L * 1024L * 1024L;//强制清理的最大空间大小

    private final static long DEFAULT_SIZE = 20L * 1024L * 1024L;//默认20M
    private final static long DEFAULT_TIME = 7L * 24L * 60L * 60L * 1000L;//默认7天
    private final static long DEFAULT_DELAY_TIME = 12L * 60L * 60L * 1000L;//默认轮询为12个小时
    private long configSize = DEFAULT_SIZE;
    private long configTime = DEFAULT_TIME;
    private long delayTimes = DEFAULT_DELAY_TIME;
    private long forceCleanLimited = FORCE_CLEAN_SIZE;

    //    private String packageName;
    private Timer timer;
    private TimerTask timerTask;
    private Thread cleanThread;
    private static boolean debug = false;

    @Override
    public void onCreate() {
        super.onCreate();
        logger.info("Log service onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int size = intent.getIntExtra(KEY_MAX_FILE_SIZE, 0);//最大存储空间
            if (size < 10) {
                size = 20;
            } else if (size > 50) {
                size = 50;
            }
            configSize = (long) size * 1024L * 1024L;
            int day = intent.getIntExtra(KEY_MAX_EXITS_DAYS, 0);//最大存储天数
            if (day < 1) {
                day = 7;
            } else if (day > 30) {
                day = 30;
            }
            configTime = (long) day * 24 * 60 * 60 * 1000;
            int times = intent.getIntExtra(KEY_CHECK_INTERVAL, 0);//检测间隔
            if (times < 1) {
                times = 12;
            } else if (times > 24) {
                times = 24;
            }
            delayTimes = (long) times * 60 * 60 * 1000;
//            delayTimes = 60 * 1000;
        }
        if (debug) {
            //调试模式，1M，2天，1分钟
            configSize = 1 * 1024 * 1024;
            configTime = (long) 30 * 24 * 60 * 60 * 1000;
            delayTimes = 60 * 1000;
            forceCleanLimited = 30 * 1024 * 1024;
        }
        logger.info("Log service onStart");
        logger.info("MaxFileSize:" + configSize + ">>>MaxExistsDays:" + configTime + ">>>CheckInterval:" + delayTimes);
        startTimer();
        return START_REDELIVER_INTENT;
    }


    public void initTimer() {
        stopTimer();
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
//                if (TextUtils.isEmpty(packageName)) {
//                    return;
//                }
                check(getPackageName());
            }
        };
    }

    public void startTimer() {//启动计时器,这里计时器可能有问题，我先这么写。不知道这么长的计时器会有啥问题。。
        initTimer();
        timer.schedule(timerTask, 0, delayTimes);
    }

    public void stopTimer() {//关闭计时器
        if (timer != null)
            timer.cancel();
        if (cleanThread != null && !cleanThread.isInterrupted()) {
            try {
                cleanThread.interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        logger.info("Log service onDestroy");
        stopTimer();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static boolean isDebug() {
        return debug;
    }

    public static void setDebug(boolean debug) {
        CpayLogService.debug = debug;
    }

    /**
     * 检测部门的老员工是否占用了太多的公司资源，如果超过了一定的资源，就开除！
     */
    private void check(String packageName) {
        Long localSize = 0l;
        String path = FileUtils.getSDCardRootPath() + File.separator +
                PATH + File.separator + packageName;//获取应用的路径
        File file = new File(path);
        if (!file.exists()) {//不存在，则结束此次检测
            logger.warn("应用日志目录不存在，结束检测");
            return;
        } else {
            localSize = FileUtils.getDirSize(file);//获取该目录下的文件大小
            logger.info("日志目录大小：" + FileUtils.formatSize(localSize) + ">>>设定大小：" + FileUtils.formatSize(configSize));
            if (localSize >= configSize) {//容量还未达标，走人
                logger.warn("日志文件超限，准备开始清理");
                Long result = localSize - configSize;
                //文件大小超出最大限制100MB，强制清理，不受最少存放天数限制
                if (localSize >= forceCleanLimited) {
                    killLogs(file, result, true);
                } else {
                    killLogs(file, result, false);
                }
            }
        }
    }

    public static long getDirSize(File dir) {
        if (dir == null) {
            return 0;
        }
        if (!dir.isDirectory()) {
            return 0;
        }
        long dirSize = 0;
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                dirSize += file.length();
            }
//            else if (file.isDirectory()) {
//                dirSize += file.length();
//                dirSize += getDirSize(file); // 递归调用继续统计
//            }
        }
        return dirSize;
    }

    /**
     * 清理一些部门顽固老员工！
     */
    private void killLogs(final File file, final Long result, final boolean forceDelete) {
        logger.info("Force clean flag >>> " + forceDelete);
        File[] files = file.listFiles();
        final List<File> oldFiles = getOldFile(files);
        if (oldFiles == null) {
            return;
        }
        try {
            cleanThread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    logger.info("开始清理日志");
                    long start = System.currentTimeMillis();
                    Long killSize = 0l;
                    for (int i = 0; i < oldFiles.size(); i++) {
                        File file1 = oldFiles.get(i);
                        long lastModifyTime = file1.lastModified();//最晚修改时间
                        long nowTime = System.currentTimeMillis();//现在的时间
                        long resultDay = nowTime - lastModifyTime;//算出时间差
                        if (resultDay < configTime && !forceDelete) {//算了，公司只剩下这些刚进来的老员工了，得了。。
                            logger.debug("Now：" + nowTime + ">>>LastModifyTime：" + lastModifyTime + ">>>" +
                                    file1.getName() + ">>>不满足最小存放天数，不清理");
                            break;
                        }
                        long currentSize = FileUtils.getDirSize(file);
                        if (currentSize <= configSize) {//算了，公司资源还需要平衡，这次就先放了这些剩下的老员工吧
                            logger.info("当前日志空间大小:" + FileUtils.formatSize(currentSize) + "，满足最小空间限制，结束！");
                            break;
                        } else {
                            logger.debug("清理文件：" + file1.getName() + ">>>文件大小：" + FileUtils.formatSize(file1.length()));
//                            killSize += file1.length();
                            if (file1.isFile()) {
                                file1.delete();//开除了！
                            }
                            currentSize = FileUtils.getDirSize(file);
                            logger.debug("清理后日志空间大小：" + FileUtils.formatSize(currentSize));
                        }
                    }
                    logger.info("日志清理结束，耗时：" + (System.currentTimeMillis() - start) + "ms");
                }
            };
            cleanThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取按照最近修改的文件的顺序获取文件列表
     */
    public static List<File> getOldFile(File[] allFiles) {
        if (allFiles == null && allFiles.length == 0) {
            return null;
        }
        List<File> list = Arrays.asList(allFiles);
        Collections.sort(list, new Comparator<File>() {//按最晚修改的文件倒排序
            public int compare(File file, File newFile) {
                if (file.lastModified() < newFile.lastModified()) {
                    return -1;
                } else if (file.lastModified() == newFile.lastModified()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        /*for (int i = 0; i < list.size(); i++) {
            File file = list.get(i);
            String fileName = file.getName();
            if (fileName.contains("crash")
                    || !fileName.endsWith(".txt")) {
                list.remove(i--);
            }
        }*/
//        Log.w("test",list.toString());
        return list;
    }

}
