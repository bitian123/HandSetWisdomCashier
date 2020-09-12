package com.centerm.epos.receiver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;

import com.centerm.epos.common.Settings;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.configure.EposProject;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.mvp.model.MenuBiz;
import com.centerm.epos.print.PrintManager;
import com.centerm.epos.utils.XLogUtil;

import java.util.List;

import config.BusinessConfig;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by yuhc on 2017/3/24.
 */

public class BootCompleteReceiver extends BroadcastReceiver {
    private static final String TAG = BootCompleteReceiver.class.getSimpleName();
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        XLogUtil.d(TAG, "^_^ " + "onReceive: running... " + " ^_^");

        if(!Settings.isAppInit(context))
            return;

        String projectName = Settings.getProjectName(context);
        if (TextUtils.isEmpty(projectName) || !EposProject.getInstance().isProjectExist(projectName))
            return;
        ConfigureManager.getInstance(context).setProject(context, projectName);

        Settings.setValue(context, Settings.KEY.SIGN_IN_DATE, "");
        BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.FLAG_SIGN_IN, false);

        this.context = context;
        resetOperatorStatus();
        String newPayChannel = Settings.getNewPayChannel(context);
        if (TextUtils.isEmpty(newPayChannel)) {
            XLogUtil.d(TAG, "^_^ " + "onReceive: new pay channel is null" + " ^_^");
            return;
        }

        String projectTag = EposProject.getInstance().getProjectTagByChannelID(newPayChannel);
        Settings.setProjectName(context, projectTag);
        ConfigureManager.getInstance(context).setProject(context, projectTag);
        doInitialization();
        switchAppIconAndLable();
        XLogUtil.d(TAG, "^_^ " + "onReceive: change pay channel to " + projectTag + " ^_^");

        // TODO: 2017/3/24 添加测试商户号和终端号
//        ParameterTest.initTestParam(context);
    }

    private void resetOperatorStatus() {
        String oper = BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.KEY_OPER_ID);
        if (!TextUtils.isEmpty(oper)) {
            XLogUtil.d(TAG, "^_^ 重置操作员登录状态 ^_^");
            BusinessConfig.getInstance().setValue(context, BusinessConfig.Key.KEY_OPER_ID, "");
        }
    }

    /**
     * 进行应用初始化操作
     * todo 初始化操作比较耗时，需要优化成异步进行
     */
    public void doInitialization() {
        //恢复默认参数
        XLogUtil.d(TAG, "^_^ " + "doInitialization: parameter reset ing ... " + " ^_^");
        BusinessConfig.getInstance().clearConfig(context);
        Settings.clearSetting(context);
        XLogUtil.d(TAG, "^_^ " + "doInitialization: trade db is clear ing ..." + " ^_^");
        new MenuBiz().clearTradeRecords(DbHelper.getInstance(), null);
        DbHelper.releaseInstance();
        //导入签购单模板
        PrintManager printManager = new PrintManager(context);
        printManager.importTemplate();
        //导入默认密钥

    }

    public void switchAppIconAndLable() {
        String prjMainAliasClassName = genPrjMainActivityClassName();
        if (TextUtils.isEmpty(prjMainAliasClassName))
            return;
        PackageManager pm = context.getPackageManager();
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        pm.setComponentEnabledSetting(intent.getComponent(),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(new ComponentName(context, prjMainAliasClassName),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

//        重启launcher，刷新桌面图标和文字和显示
        restartLauncher(context, pm);
    }

    private String genPrjMainActivityClassName() {
        String packageName = context.getPackageName();
        Intent it = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        String className = it.getComponent().getClassName();
        String[] spitStr = className.split("\\.");
        String prjName = ConfigureManager.getInstance(context).getProject().toLowerCase();
        String prjMainAliasClassName = null;
        if (spitStr.length > 1) {
            prjMainAliasClassName = packageName + "." + prjName + "." + spitStr[spitStr.length - 1];
        }
        XLogUtil.d(TAG, "^_^ " + prjMainAliasClassName + " ^_^");
        return prjMainAliasClassName;
    }

    private void restartLauncher(Context context, PackageManager pm) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        List<ResolveInfo> resolves = pm.queryIntentActivities(intent, 0);
        ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        for (ResolveInfo res : resolves) {
            if (res.activityInfo != null) {
                am.killBackgroundProcesses(res.activityInfo.packageName);
                XLogUtil.d(TAG, "^_^ launcher package name: " + res.activityInfo.packageName + " ^_^");
            }
        }
        am.killBackgroundProcesses("com.centerm.cpay.launcher");
    }
}
