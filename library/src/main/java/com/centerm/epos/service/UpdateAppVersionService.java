package com.centerm.epos.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.centerm.epos.base.ApplicationEnvironment;
import com.centerm.epos.function.AppUpgradeForLiandiShopUtil;
import com.centerm.epos.utils.DateHelper;
import com.centerm.epos.utils.XLogUtil;

import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 16240
 */
public class UpdateAppVersionService extends Service {
    /**
     * apk版本校验
     */
    public static ScheduledExecutorService checkVersionScheduledExecutorService;
    /**
     * TODO 这里生产上要调整为00，晚上00点开始执行
     * 定时任务时间 小时: 默认00:00 +10分钟随机离散 即00点开始
     */
    public static int FIX_TIME_TASK_HOUR = 23;
    /**
     * 定时任务时间 分钟: 0
     */
    public static int FIX_TIME_TASK_MIN = 0;
    /**
     * 定时任务时间 秒钟: 0
     */
    public static final int FIX_TIME_TASK_SEC = 0;
    /**
     * 离散任务时间 分钟: 10
     */
    public static int RANDOM_TIME_TASK_MIN = 10;

    /**
     * 离散任务时间 秒: 59
     */
    public static int RANDOM_TIME_TASK_SEC = 59;
    /**
     *  间隔24小时
     */
    public static int ONE_DAY = 24* 60 *60 * 1000 ;


    public UpdateAppVersionService(){
    }

    private static final String TAG = "UpdateAppVersionService";


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startCheckVersion( ApplicationEnvironment.currentContext);
        return START_REDELIVER_INTENT;
    }

    /**
     * 定时检测应用版本
     * @param context   应用上下文
     */
    /**
     * 定时检测应用版本
     * @param context   应用上下文
     */
    public static  void startCheckVersion(final Context context) {
        XLogUtil.d("startCheckVersion","===启动定时任务=====");
        //每天的02:30:00执行任务
        Random random = new Random();
        long delay = DateHelper.calcDelay(FIX_TIME_TASK_HOUR, FIX_TIME_TASK_MIN+random.nextInt(RANDOM_TIME_TASK_MIN), FIX_TIME_TASK_SEC+random.nextInt(RANDOM_TIME_TASK_SEC));
//        long delay = DateHelper.calcDelay(FIX_TIME_TASK_HOUR, FIX_TIME_TASK_MIN, FIX_TIME_TASK_SEC);
        long period = DateHelper.ONE_DAY;
        if (checkVersionScheduledExecutorService != null) {
            checkVersionScheduledExecutorService.shutdown();
        }
        //初始化定时任务服务
        ScheduledThreadPoolExecutor checkVersionScheduledExecutorService = new ScheduledThreadPoolExecutor(5);
        checkVersionScheduledExecutorService.scheduleAtFixedRate(new Runnable(){
            @Override
            public void run() {
                AppUpgradeForLiandiShopUtil.getInstance().init(context);
            }
        }, delay, period, TimeUnit.MILLISECONDS);
        XLogUtil.d(TAG,"===启动定时任务==成功===");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        XLogUtil.d(TAG,"===停止定时任务===");
        checkVersionScheduledExecutorService.shutdown();
        Intent sevice = new Intent(this, UpdateAppVersionService.class);
        this.startService(sevice);
    }
}
