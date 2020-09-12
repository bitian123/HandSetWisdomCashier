package com.centerm.epos.function;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.centerm.cloudsys.sdk.common.utils.FileUtils;
import com.centerm.cpay.appcloud.remote.IVersionInfoCallback;
import com.centerm.cpay.appcloud.remote.IVersionInfoProvider;
import com.centerm.cpay.appcloud.remote.VersionInfo;
import com.centerm.epos.EposApplication;
import com.centerm.epos.event.UpgradeAppMessage;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.FileMD5;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.utils.XLogUtil;
import com.centerm.epos.view.AlertDialog;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

/**
 * Created by yuhc on 2017/9/18.
 * 程序升级
 */

public class AppUpgradeUtil {

    private static final String TAG = AppUpgradeUtil.class.getSimpleName();
    public static final String APK_DOWN_DIR = "/EPos/Payment";


    private static AppUpgradeUtil appUpgradeUtil;
    private Context appContext;
    private WeakReference<Context> viewContext;
    AppCloudServiceConnection connection;
    private IVersionInfoProvider versionInfoProvider;
    private String apkDownDir;
    private int connectTimeout;

    boolean isShowErrorTip;
    VersionInfo mNewVersionInfo;

    ProgressDialog mProgressDialog;


    public static AppUpgradeUtil getInstance() {
        if (appUpgradeUtil == null) {
            synchronized (AppUpgradeUtil.class) {
                if (appUpgradeUtil == null)
                    appUpgradeUtil = new AppUpgradeUtil();
            }
        }
        return appUpgradeUtil;
    }

    public AppUpgradeUtil() {
        connectTimeout = 0;
        appContext = EposApplication.getAppContext();
        connection = new AppCloudServiceConnection();
    }

    public boolean isShowErrorTip() {
        return isShowErrorTip;
    }

    public void setShowErrorTip(boolean showErrorTip) {
        isShowErrorTip = showErrorTip;
    }

    public void setApkDownDir(String apkDownDir) {
        this.apkDownDir = apkDownDir;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * 初始化升级功能，绑定应用商店服务
     *
     * @return true 初始化成功
     */
    public boolean init(Context context) {
        XLogUtil.d(TAG, "^_^ 绑定应答商店服务 ^_^");
        viewContext = new WeakReference<>(context);

        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        if (versionInfoProvider == null) {
            Intent intent = new Intent();
            intent.setAction("com.centerm.cpay.appcloud.REMOTE_SERVICE");
            intent.setPackage("com.centerm.cpay.applicationshop");
            return appContext.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }
        EventBus.getDefault().post(new UpgradeAsyncMessage(UpgradeAppMessage.CHECK_NEW_VERSION));
        return true;
    }

    /**
     * 检测应用新版本
     *
     * @return true 检测成功
     */
    public boolean checkNewVersion() {
        XLogUtil.d(TAG, "^_^ 检测新版本 ^_^");
        try {
            versionInfoProvider.getLatestVersion(appContext.getPackageName(), new IVersionInfoCallback.Stub() {
                @Override
                public void onSuccess(VersionInfo info) throws RemoteException {
                    XLogUtil.d(TAG, "【获取成功】" + info.toString());
                    PackageManager manager = appContext.getPackageManager();
                    PackageInfo versioninfo = null;
                    try {
                        versioninfo = manager.getPackageInfo(appContext.getPackageName(), 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    int versionCode = versioninfo.versionCode;
                    XLogUtil.d(TAG, "【新版本：】" + info.getVersionCode() + " 【当前版本：】" + versionCode);
                    if (info.getVersionCode() > versionCode) {
                        mNewVersionInfo = info;
                        EventBus.getDefault().post(new UpgradeUIMessage(UpgradeAppMessage.CHECK_RESULT));
                    } else {
                        File apkDir = new File(getApkDownDir());
                        if (apkDir.exists()) {
                            File[] files = apkDir.listFiles();
                            if (files != null && files.length > 0) {
                                for (int i = 0; i < files.length; i++)
                                    files[i].delete();
                            }
                        }
                    }
                }

                @Override
                public void onError(int errorCode, String errorInfo) throws RemoteException {
                    XLogUtil.d(TAG, "【获取失败】错误码：" + errorCode + "  错误信息：" + errorInfo);
                    EventBus.getDefault().post(new UpgradeUIMessage(UpgradeAppMessage.DOWNLOAD_ERROR, errorInfo));
                }
            });
        } catch (RemoteException e) {
            XLogUtil.e(TAG, "^_^ " + e.getMessage() + " ^_^");
            EventBus.getDefault().post(new UpgradeUIMessage(UpgradeAppMessage.DOWNLOAD_ERROR, "未知错误"));
        }
        return true;
    }

    /**
     * 获取下载文件完整路径，文件名称规范：versionCode + _ + Epos.apk
     *
     * @param versionCode 版本号
     * @return 完整路径
     */
    @NonNull
    private String getApkFileFullName(int versionCode) {
        return FileUtils.getSDCardRootPath() + (TextUtils.isEmpty(apkDownDir) ? APK_DOWN_DIR : apkDownDir) + File
                .separator + versionCode + "_Epos.apk";
    }

    /**
     * 获取文件下载的目录
     *
     * @return 目录
     */
    private String getApkDownDir() {
        return FileUtils.getSDCardRootPath() + (TextUtils.isEmpty(apkDownDir) ? APK_DOWN_DIR : apkDownDir);
    }

    /**
     * 下载新版本软件
     */
    private void downloadAndInstallNewVersion() {
        downLoadFile();
    }


    /**
     * 下载文件
     */
    private void downLoadFile() {
        final File apkFile = new File(getApkFileFullName(mNewVersionInfo.getVersionCode()));

        OkHttpClient okHttpClient = new OkHttpClient();
        if (connectTimeout != 0)
            okHttpClient.setConnectTimeout(connectTimeout, TimeUnit.SECONDS);

//        okHttpClient.setReadTimeout(20, TimeUnit.SECONDS);
//        okHttpClient.setWriteTimeout(10, TimeUnit.SECONDS);

        long downloadLength = apkFile.length();
        final Request request = new Request.Builder()
                .addHeader("RANGE", "bytes=" + downloadLength + "-")    //断点续传头信息
                .url(mNewVersionInfo.getDownloadUrl())
                .build();

        final Call call = okHttpClient.newCall(request);
        call.enqueue(new ResponseCallBack(apkFile));
    }

    /**
     * 校验已经下载好的文件，如果已经下载好了则直接安装。
     *
     * @return null 已下载完并进行了安装
     */
    @Nullable
    private File checkDownloadedFile() {
        File apkDir = new File(getApkDownDir());
        if (!apkDir.exists())
            apkDir.mkdirs();
        final File apkFile = new File(getApkFileFullName(mNewVersionInfo.getVersionCode()));
        if (apkFile.exists() && apkFile.length() > 0) {
            //文件存在处理，不合法的删除，校验通过则安装
            String[] partNames = apkFile.getName().split("_");
            if (partNames.length != 2) {
                XLogUtil.d(TAG, "^_^ 文件名错误，删除文件，重新下载 ^_^");
                apkFile.delete();
            }
            if (Integer.parseInt(partNames[0]) != mNewVersionInfo.getVersionCode()) {
                XLogUtil.d(TAG, "^_^ 已下载文件与新版本不匹配，删除文件，重新下载 ^_^");
                apkFile.delete();
            }
            if (mNewVersionInfo.getSize() == apkFile.length()) {
                try {
                    if (mNewVersionInfo.getAppMd5().equals(FileMD5.getFileMD5String(apkFile.getPath()))) {
                        installApk();
                        return null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                XLogUtil.d(TAG, "^_^ 已下载文件MD5校验失败或文件读取异常，删除文件，重新下载 ^_^");
                apkFile.delete();
            }
        }
        return apkFile;
    }

    /**
     * 安装程序
     */
    private void installApk() {
        Uri uri = Uri.fromFile(new File(getApkFileFullName(mNewVersionInfo.getVersionCode())));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        if (viewContext.get() != null)
            viewContext.get().startActivity(intent);
    }

    /**
     * 应答商店服务连接回调，成功则返回版本检测的服务接口。
     */
    public class AppCloudServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            XLogUtil.d(TAG, "^_^ 服务已连接 ^_^");
            versionInfoProvider = IVersionInfoProvider.Stub.asInterface(service);
            EventBus.getDefault().post(new UpgradeAsyncMessage(UpgradeAppMessage.CHECK_NEW_VERSION));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            XLogUtil.d(TAG, "服务已断开");
        }


    }


    /**
     * 程序升级的消息处理
     *
     * @param event 消息类型
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleMessage(UpgradeUIMessage event) {
        XLogUtil.d(TAG, "^_^ EVENT  code:" + event.getWhat() + " ^_^");
        switch (event.getWhat()) {
            case UpgradeAppMessage.CHECK_RESULT:
                if (viewContext.get() != null)
                    DialogFactory.showSelectDialog(viewContext.get(), "提示", "检测到新版本，是否更新？", new ButtonClickListener());
                break;
            case UpgradeAppMessage.DOWNLOAD_ERROR:
                if (isShowErrorTip && viewContext.get() != null && !TextUtils.isEmpty(event.getMessage())) {
                    ViewUtils.showToast(viewContext.get(), event.getMessage());
                }
                EventBus.getDefault().post(new UpgradeUIMessage(UpgradeAppMessage.DOWNLOAD_COMPELETE));
                break;
            case UpgradeAppMessage.SHOW_TIP:
                if (viewContext.get() != null && !TextUtils.isEmpty(event.getMessage()))
                    ViewUtils.showToast(viewContext.get(), event.getMessage());
                break;
            case UpgradeAppMessage.DOWNLOAD_COMPELETE:
                releaseResource();
                EventBus.getDefault().unregister(this);
                break;
        }
    }

    /**
     * 释放资源
     */
    private void releaseResource() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void handleBackgroundMessage(UpgradeAsyncMessage event) {
        XLogUtil.d(TAG, "^_^ EVENT  code:" + event.getWhat() + " ^_^");
        switch (event.getWhat()) {
            case UpgradeAppMessage.CHECK_NEW_VERSION:
                checkNewVersion();
                break;
            case UpgradeAppMessage.DOWNLOAD_NEW_APP:
                downloadAndInstallNewVersion();
                break;
        }
    }


    private class ResponseCallBack implements Callback {
        private File apkFile;

        public ResponseCallBack(File apkFile) {
            this.apkFile = apkFile;
        }

        @Override
        public void onFailure(Request request, IOException e) {
            EventBus.getDefault().post(new UpgradeUIMessage(UpgradeAppMessage.DOWNLOAD_ERROR, e.getMessage()));
        }

        @Override
        public void onResponse(Response response) throws IOException {
            InputStream is = null;
            byte[] buf = new byte[2048];
            int len;
            FileOutputStream fos = null;
            try {
                long total = response.body().contentLength();
                XLogUtil.d(TAG, "total------>" + total);
                mProgressDialog.setMax((int) total);
                long current = apkFile.length();
                XLogUtil.d(TAG, "from------>" + current);
                is = response.body().byteStream();
                fos = new FileOutputStream(apkFile, true);
                while ((len = is.read(buf)) != -1) {
                    current += len;
                    fos.write(buf, 0, len);
//                    XLogUtil.d(TAG, "current------>" + current);
                    mProgressDialog.setProgress((int) current);
                }
                fos.flush();
                EventBus.getDefault().post(new UpgradeUIMessage(UpgradeAppMessage.SHOW_TIP, "下载成功，正在安装"));
                mProgressDialog.dismiss();
                if (mNewVersionInfo.getAppMd5().equals(FileMD5.getFileMD5String(apkFile.getPath()))) {
                    installApk();
                    return;
                }
            } catch (IOException e) {
                XLogUtil.d(TAG, e.toString());
                if (viewContext.get() != null)
                    EventBus.getDefault().post(new UpgradeUIMessage(UpgradeAppMessage.DOWNLOAD_ERROR, "下载失败"));
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                    XLogUtil.d(TAG, e.toString());
                }
            }
        }
    }

    private class ButtonClickListener implements AlertDialog.ButtonClickListener {
        @Override
        public void onClick(AlertDialog.ButtonType button, View v) {
            if (AlertDialog.ButtonType.NEGATIVE == button)
                EventBus.getDefault().post(new UpgradeUIMessage(UpgradeAppMessage.DOWNLOAD_COMPELETE));
            else {
                if (checkDownloadedFile() == null)
                    EventBus.getDefault().post(new UpgradeUIMessage(UpgradeAppMessage
                            .DOWNLOAD_COMPELETE));
                else {
                    if (viewContext.get() != null) {
                        mProgressDialog = new ProgressDialog(viewContext.get());
                        mProgressDialog.setMessage("正在下载");
                        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        mProgressDialog.setCancelable(false);
                        mProgressDialog.setCanceledOnTouchOutside(false);
                        mProgressDialog.show();
                    }
                    EventBus.getDefault().post(new UpgradeAsyncMessage(UpgradeAppMessage
                            .DOWNLOAD_NEW_APP));
                }
            }
        }
    }


    /**
     * 检测wifi是否已经连接
     *
     * @param context 环境上下文
     * @return true 已连接
     */
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context
                .CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifiNetworkInfo.isConnected();
    }

    /**
     * UI线程消息
     */
    private class UpgradeUIMessage extends UpgradeAppMessage {
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
}
