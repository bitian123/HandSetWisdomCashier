package com.centerm.epos.function;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.appinterface.update.AppJson;
import com.appinterface.update.AppState;
import com.appinterface.update.ReturnCode;
import com.appinterface.update.UpdateHelper;
import com.appinterface.update.UpdateListener;
import com.centerm.epos.EposApplication;
import com.centerm.epos.event.UpgradeAppMessage;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.utils.XLogUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;


/**
 * Created by yuhc on 2017/9/18.
 * 程序升级,联迪应用市场
 */
public class AppUpgradeForLiandiShopUtil {

    private static final String TAG = AppUpgradeForLiandiShopUtil.class.getSimpleName();
    public static final String APK_DOWN_DIR = "/EPos/Payment";


    private static AppUpgradeForLiandiShopUtil appUpgradeUtil;
    private Context appContext;
    private WeakReference<Context> viewContext;
    private Context mContext;

    boolean isShowErrorTip;
    AppJson mNewVersionInfo;

    ProgressDialog mProgressDialog;


    public static AppUpgradeForLiandiShopUtil getInstance() {
        if (appUpgradeUtil == null) {
            synchronized (AppUpgradeForLiandiShopUtil.class) {
                if (appUpgradeUtil == null) {
                    appUpgradeUtil = new AppUpgradeForLiandiShopUtil();
                }
            }
        }
        return appUpgradeUtil;
    }

    public AppUpgradeForLiandiShopUtil() {
        appContext = EposApplication.getAppContext();
    }


    /**
     * 初始化升级功能，绑定应用商店服务
     *
     * @return true 初始化成功
     */
    public boolean init(Context context) {
        XLogUtil.d(TAG, "^_^ 初始化 ^_^");

        viewContext = new WeakReference<>(context);
        mContext = context;
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        EventBus.getDefault().post(new UpgradeAsyncMessage(UpgradeAppMessage.CHECK_NEW_VERSION));
        return true;
    }

    /**
     * 版本校验监听
     */
    UpdateListener mCheckVersionListener = new UpdateListener() {
        @Override
        public void onCheckUpdateResult(String packageName, int returnCode, AppJson appJson) {
            XLogUtil.d(TAG, "-mCheckVersionListener:" + packageName + ", -returnCode:" + returnCode + "-info：" + appJson.getVersionCode());
            if (ReturnCode.CODE_GET_UPDATE == returnCode) {
                PackageManager manager = appContext.getPackageManager();
                PackageInfo versioninfo = null;
                try {
                    versioninfo = manager.getPackageInfo(appContext.getPackageName(), 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                int versionCode = versioninfo.versionCode;
                XLogUtil.i(TAG, "【新版本：】" + appJson.getVersionCode() + " 【当前版本：】" + versionCode);
                if (Integer.parseInt(appJson.getVersionCode()) > versionCode) {
                    mNewVersionInfo = appJson;
                    downloadAndInstallNewVersion();
                }
            }
        }

        @Override
        public void onStateChanged(String packageName, int state, String stateMsg) {
            XLogUtil.d(TAG, "-mCheckVersionListener_onStateChanged-packageName:" + packageName + ", -state:" + state + ", -stateMsg:" + stateMsg);
            EventBus.getDefault().post(new UpgradeUIMessage(UpgradeAppMessage.DOWNLOAD_NEW_APP));
        }
    };

    final UpdateListener mDownloadListener = new UpdateListener() {
        @Override
        public void onCheckUpdateResult(String packageName, int returnCode, AppJson appJson) {
            XLogUtil.d(TAG, "-mDownloadListener:" + packageName + ", -returnCode:" + returnCode + "-info：" + appJson.getVersionCode());

        }

        @Override
        public void onProgress(String packageName, final int progress) {
            XLogUtil.i(TAG, "-onProgress-packageName:" + packageName + ", -progress:" + progress);
            Message message = handler.obtainMessage();
            message.what = UpgradeAppMessage.SHOW_TIP;
            message.obj = progress;
            handler.sendMessage(message);

        }

        @Override
        public void onStateChanged(String packageName, int state, String stateMsg) {
            XLogUtil.d(TAG, "-onStateChanged-packageName:" + packageName + ", -state:" + state + ", -stateMsg:" + stateMsg);
            if (AppState.STATE_DOWNLOAD_CANCELLED == state) {
                XLogUtil.i(TAG, "下载已取消");
            } else if (AppState.STATE_DOWNLOAD_COMPLETED == state) {
                XLogUtil.i(TAG, "下载完成");
                installApk();
            } else if (AppState.STATE_DOWNLOAD_FAILED == state) {
                XLogUtil.e(TAG, "新版本app下载失败");
            } else if (AppState.STATE_DOWNLOAD_ING == state) {
                XLogUtil.e(TAG, "新版本app正在下载");
                EventBus.getDefault().post(new UpgradeUIMessage(UpgradeAppMessage.DOWNLOAD_NEW_APP));
            } else if (AppState.STATE_INSTALL_ING == state) {
                XLogUtil.e(TAG, "新版本app正在安装");
            } else if (AppState.STATE_INSTALL_WAITING == state) {
                XLogUtil.e(TAG, "等待安装");
            } else if (AppState.STATE_INSTALL_COMPLETED == state) {
                XLogUtil.e(TAG, "安装完成");
            } else if (AppState.STATE_INSTALL_FAILED == state) {
                XLogUtil.e(TAG, "安装失败");
            } else if (AppState.STATE_INSTALL_CANCELLED == state) {
//                XLogUtil.e(TAG, "安装已取消");
            }

        }
    };



    private Handler handler = new Handler(Looper.getMainLooper()) {
        @NonNull
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == UpgradeAppMessage.SHOW_TIP) {
                int process = (int) msg.obj;
                showDownLoading(process);
            }
        }
    };

    public void showDownLoading(int progress) {
        viewContext = new WeakReference<>(mContext);
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(viewContext.get());
        }
        mProgressDialog.setMessage("正在下载");
        if (progress < 100) {
            mProgressDialog.setProgress(progress);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
        } else {
            EventBus.getDefault().post(new UpgradeUIMessage(UpgradeAppMessage.SHOW_TIP, "下载成功，正在安装"));
            dissMissDialog();
        }
    }

    /**
     * 关闭dialog释放资源
     */
    private void dissMissDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    /**
     * 检测应用新版本
     *
     * @return true 检测成功
     */
    public void checkNewVersion(Context context, final CheckVersion checkVersion) {
        XLogUtil.d(TAG, "^_^ 检测新版本 ^_^");
        mContext = context;
        viewContext = new WeakReference<>(mContext);
        UpdateHelper.checkUpdate(viewContext.get(), null, appContext.getPackageName(), new UpdateListener() {
            @Override
            public void onCheckUpdateResult(String packageName, int returnCode, AppJson appJson) {
                super.onCheckUpdateResult(packageName, returnCode, appJson);
                XLogUtil.d(TAG, "-onCheckUpdateResult-packageName222:" + packageName + ", -returnCode:" + returnCode + "-info：" + appJson.getVersionCode());
                if (ReturnCode.CODE_GET_UPDATE == returnCode) {
                    PackageManager manager = appContext.getPackageManager();
                    PackageInfo versioninfo = null;
                    try {
                        versioninfo = manager.getPackageInfo(appContext.getPackageName(), 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    int versionCode = versioninfo.versionCode;
                    XLogUtil.i(TAG, "【新版本：】" + appJson.getVersionCode() + " 【当前版本：】" + versionCode);
                    if (Integer.parseInt(appJson.getVersionCode()) > versionCode) {
                        mNewVersionInfo = appJson;
                        downloadAndInstallNewVersion();
                    }
                } else {
                    checkVersion.hasNoNewVersion();
                }
            }
        });
    }




    /**
     * 检测应用新版本
     *
     * @return true 检测成功
     */
    public void checkNewVersion() {
        XLogUtil.d(TAG, "^_^ 检测新版本 ^_^");
        UpdateHelper.checkUpdate(viewContext.get(), null, appContext.getPackageName(), mCheckVersionListener);
    }


    /**
     * 下载新版本软件
     */
    private void downloadAndInstallNewVersion() {
        XLogUtil.d(TAG, "^_^ 下载新版本软件 ^_^");
        UpdateHelper.download(appContext, null, mNewVersionInfo.getPackName(), false, mDownloadListener);
    }


    /**
     * 安装程序
     */
    private void installApk() {
        UpdateHelper.install(appContext, null, mNewVersionInfo.getPackName(), mDownloadListener);
    }


    /**
     * 程序升级的消息处理
     *
     * @param event 消息类型
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleMessageNew(UpgradeUIMessage event) {
        XLogUtil.d(TAG, "^_^ EVENT  code:" + event.getWhat() + " ^_^");
        switch (event.getWhat()) {
            case UpgradeAppMessage.CHECK_RESULT:
                if (viewContext.get() != null) {
                    EventBus.getDefault().post(new UpgradeAsyncMessage(UpgradeAppMessage.DOWNLOAD_NEW_APP));
                }
                break;
            case UpgradeAppMessage.DOWNLOAD_ERROR:
                if (isShowErrorTip && viewContext.get() != null && !TextUtils.isEmpty(event.getMessage())) {
                    ViewUtils.showToast(viewContext.get(), event.getMessage());
                }
                EventBus.getDefault().post(new UpgradeUIMessage(UpgradeAppMessage.DOWNLOAD_COMPELETE));
                break;
            case UpgradeAppMessage.SHOW_TIP:
                if (viewContext.get() != null && !TextUtils.isEmpty(event.getMessage())) {
                    ViewUtils.showToast(viewContext.get(), event.getMessage());
                }
                installApk();
                break;
            case UpgradeAppMessage.DOWNLOAD_COMPELETE:
                dissMissDialog();
                EventBus.getDefault().unregister(this);
                break;
            default:
        }
    }



    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void handleBackgroundMessage(UpgradeAsyncMessage event) {
        XLogUtil.d(TAG, "^_^ EVENT  code:" + event.getWhat() + " ^_^");
        switch (event.getWhat()) {
            case UpgradeAppMessage.CHECK_NEW_VERSION:
                checkNewVersion();
                break;
        }
    }


    /**
     * UI线程消息
     */
    public static class UpgradeUIMessage extends UpgradeAppMessage {

        public UpgradeUIMessage(int what, String message) {
            super(what, message);
        }

        public UpgradeUIMessage(int what) {
            super(what);
        }
    }

    /**
     * 后台线程消息
     */
    private class UpgradeAsyncMessage extends UpgradeAppMessage {
        public UpgradeAsyncMessage(int what) {
            super(what);
        }
    }

    /**
     * 校验app版本
     */
    public interface CheckVersion {
        /**
         * 没有新版本
         */
        void hasNoNewVersion();
    }


}
