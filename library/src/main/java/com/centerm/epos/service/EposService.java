package com.centerm.epos.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.centerm.cloudsys.sdk.common.utils.PackageUtils;
import com.centerm.epos.bean.AppInfo;
import com.centerm.epos.common.Settings;
import com.centerm.epos.net.ResponseHandler;
import com.centerm.epos.net.htttp.DefaultHttpClient;
import com.centerm.epos.net.htttp.request.BaseRequest;
import com.centerm.epos.utils.CommonUtils;
import com.loopj.android.http.RequestParams;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import config.Config;

/**
 * author:wanliang527</br>
 * date:2016/11/29</br>
 */

public class EposService extends Service implements BDLocationListener {

    public final static String ACTION_NEW_APP_VERSION_INFO = "com.centerm.epos.broadcast.newAppVersion";
    private Logger logger = Logger.getLogger(this.getClass());
    private int relocationTimes;
    private long locationInterval = Config.LOCATION_INTERVAL;

    private LocationClient locationClient;
    private Timer timer;
    private TimerTask locationTask;
    private TimerTask versionCheckTask;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    requestAppVersionInfo();
                    break;
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        locationClient = new LocationClient(getApplicationContext());
        locationClient.setLocOption(initLocationOptions());
        locationClient.registerLocationListener(this);
        timer = new Timer();
//        resetLocationTask();
//        resetVersionTask();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationTask != null) {
            locationTask.cancel();
        }
        if (versionCheckTask != null) {
            versionCheckTask.cancel();
        }
        if (timer != null) {
            timer.cancel();
        }
        locationTask = null;
        versionCheckTask = null;
        timer = null;
    }

    private void resetLocationTask() {
        if (locationTask != null) {
            locationTask.cancel();
            locationTask = null;
        }
        locationTask = new TimerTask() {
            @Override
            public void run() {
                logger.info("开始定位==>定位频率==>" + locationInterval / 1000 / (double) 60 + "分钟");
                locationClient.start();
            }
        };
        if (timer != null) {
            timer.schedule(locationTask, 5 * 1000, locationInterval);
        }
    }

    private void resetVersionTask() {
        if (versionCheckTask != null) {
            versionCheckTask.cancel();
            versionCheckTask = null;
        }
        versionCheckTask = new TimerTask() {
            @Override
            public void run() {
                logger.info("开始版本检测==>检测频率==>" + Config.APP_VERSION_CHECK_INTERVAL / 1000 / 60 + "分钟");
                handler.obtainMessage(1).sendToTarget();
            }
        };
        if (Settings.getVersionUpdateInfo(this) == null) {
            timer.schedule(versionCheckTask, 2 * 1000, Config.APP_VERSION_CHECK_INTERVAL);
        } else {
            timer.schedule(versionCheckTask, 10 * 1000, Config.APP_VERSION_CHECK_INTERVAL);
        }
    }


    private LocationClientOption initLocationOptions() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        return option;
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        locationClient.stop();
        int locType = bdLocation.getLocType();
        logger.info("定位完成(61,66,161为成功)==>返回码：" + locType + "==>正在关闭定位");
        logger.info("当前位置信息==>纬度：" + bdLocation.getLatitude() + "==>经度：" + bdLocation.getLongitude());
        if (locType == 61 || locType == 66 || locType == 161) {
            Settings.setValue(this, Settings.KEY.LOCATION_LATITUDE, "" + bdLocation.getLatitude());
            Settings.setValue(this, Settings.KEY.LOCATION_LONGTITUDE, "" + bdLocation.getLongitude());
            if (locationInterval != Config.LOCATION_INTERVAL) {
                locationInterval = Config.LOCATION_INTERVAL;
                resetLocationTask();
            }
        } else {
            Settings.setValue(this, Settings.KEY.LOCATION_LATITUDE, null);
            Settings.setValue(this, Settings.KEY.LOCATION_LONGTITUDE, null);
            if (locationInterval != Config.LOCATION_INTERVAL_SHORT) {
                //定位失败的话，缩短定位间隔时间
                locationInterval = Config.LOCATION_INTERVAL_SHORT;
                resetLocationTask();
            }
        }
    }

    private void requestAppVersionInfo() {
        DefaultHttpClient client = DefaultHttpClient.getInstance();
        BaseRequest request = new BaseRequest();
        request.setUrl(Settings.getVersionCheckUrl(EposService.this));
        RequestParams params = new RequestParams();
        params.put("appName", "CENTERM_K9");
        request.setParams(params);
        ResponseHandler handler = new ResponseHandler() {

            @Override
            public void onSuccess(String statusCode, String msg, byte[] data) {
                AppInfo info = parseAppInfo(msg);
                if (info != null) {
                    String curVersion = PackageUtils.getInstalledVersionName(EposService.this, getPackageName());
                    String platVersion = info.getVersion();
                    if (!CommonUtils.compareToUpdate(platVersion,getApplicationContext())){
                        logger.warn("版本比对==>本地版本号：" + curVersion + "==>平台版本号：" + platVersion + "==>不进行更新提示");
                        Settings.setVersionUpdateInfo(getApplicationContext(), null);
                    }  else {
                        Settings.setVersionUpdateInfo(EposService.this, info);
                        Intent intent = new Intent();
                        intent.setAction(ACTION_NEW_APP_VERSION_INFO);
                        sendBroadcast(intent);
                    }
                }
            }

            @Override
            public void onFailure(String code, String msg, Throwable error) {
            }
        };
        client.get(this, request, handler);
    }

    private AppInfo parseAppInfo(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has("success") && jsonObject.getBoolean("success")) {
                JSONObject message = jsonObject.getJSONObject("message");
                String appName = message.getString("appName");
                String downloadUrl = message.getString("downloadUrl");
                boolean mustUpgradeNow = message.getBoolean("mustUpgradeNow");
                int id = 0;
                if (message.has("id")) {
                    id = message.getInt("id");
                }
                String describe = null;
                if (message.has("describe")) {
                    describe = message.getString("describe");
                }
                String version = null;
                if (message.has("version")) {
                    version = message.getString("version");
                }
                String platform = null;
                if (message.has("platform")) {
                    platform = message.getString("platform");
                }
                AppInfo info = new AppInfo(appName, downloadUrl, id, mustUpgradeNow, describe, version, platform);
                return info;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
