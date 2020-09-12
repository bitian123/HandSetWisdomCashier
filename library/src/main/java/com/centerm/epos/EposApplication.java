package com.centerm.epos;

import android.app.Application;
import android.content.Context;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.define.IPbocService;
import com.centerm.cpay.midsdk.dev.define.pboc.EnumAidCapkOperation;
import com.centerm.epay.crashlogcollector.LogCollector;
import com.centerm.epos.activity.MainActivity;
import com.centerm.epos.base.ApplicationEnvironment;
import com.centerm.epos.common.Settings;

import org.apache.log4j.Logger;

import config.BusinessConfig;

/**
 * author:wanliang527</br>
 * date:2016/10/21</br>
 */

public class EposApplication extends Application {
    private Logger logger = Logger.getLogger(EposApplication.class);
//    public static EnumChannel posChannel;
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        initCrashLogCollector();
        ApplicationEnvironment.init(this);
    }

    public static final Context getAppContext(){
        return mContext;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        DeviceFactory.getInstance().release();
    }

    private void initCrashLogCollector() {
        LogCollector.init(getApplicationContext(), MainActivity.class, ActivityStack.getInstance().getActivityStack());
        //params can
        // be null
    }

    private void importIcParamsInBackground() {
        if (Settings.getValue(getApplicationContext(), Settings.KEY.IC_AID_VERSION, null) == null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        IPbocService pbocService = DeviceFactory.getInstance().getPbocService();
                        logger.info("正在导入默认AID参数");
                        for (int i = 0; i < BusinessConfig.AID.length; i++) {
                            String aidValue = BusinessConfig.AID[i];
                            pbocService.updateAID(EnumAidCapkOperation.UPDATE, aidValue);
                        }
                        Settings.setValue(getApplicationContext(), Settings.KEY.IC_AID_VERSION, "000000");
                        logger.info("导入默认AID参数成功");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        if (Settings.getValue(getApplicationContext(), Settings.KEY.IC_CAPK_VERSION, null) == null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        IPbocService pbocService = DeviceFactory.getInstance().getPbocService();
                        logger.info("正在导入默认CAPK参数");
                        for (int i = 0; i < BusinessConfig.CAPK.length; i++) {
                            String capk = BusinessConfig.CAPK[i];
                            pbocService.updateCAPK(EnumAidCapkOperation.UPDATE, capk);
                        }
                        Settings.setValue(getApplicationContext(), Settings.KEY.IC_CAPK_VERSION, "000000");
                        logger.info("导入默认CAPK参数成功");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }


}
