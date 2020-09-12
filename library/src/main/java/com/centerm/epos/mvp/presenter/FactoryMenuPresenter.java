package com.centerm.epos.mvp.presenter;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.view.View;

import com.centerm.epos.activity.MainActivity;
import com.centerm.epos.channels.SwitchPayChannel;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.configure.EposProject;
import com.centerm.epos.mvp.view.IFactoryMenuView;
import com.centerm.epos.utils.XLogUtil;
import com.centerm.epos.view.AlertDialog;
import com.centerm.epos.xml.bean.menu.MenuItem;

import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * author:wanliang527</br>
 * date:2017/3/1</br>
 */
public class FactoryMenuPresenter extends MenuPresenter {
    private static final String TAG = FactoryMenuPresenter.class.getSimpleName();
    private IFactoryMenuView menuView;

    public FactoryMenuPresenter(IFactoryMenuView menuView) {
        super(menuView);
        this.menuView = menuView;
    }

    @Override
    public void onMenuItemClicked(View view, MenuItem item) {
        final MenuItem copy = item;
        getMenuView().showSelectDialog("提示", "请确认选择【" + item.getChnTag() + "】", new AlertDialog.ButtonClickListener() {
            @Override
            public void onClick(AlertDialog.ButtonType button, View v) {
                switch (button) {
                    case POSITIVE:
                        try {
                            doInitByMainActivity(menuView.getMainActivity(), copy);
                        } catch (Exception e) {
                            XLogUtil.e(TAG, "^ ^ " + e.getMessage() + " ^_^");
                            getMenuView().toast("应用配置信息有误！");
                        }
                        break;
                }
            }
        });
    }

    public static void doInitByMainActivity(MainActivity activity, MenuItem item) {
        activity.setProject(item.getEnTag());
        activity.doInitialization();
        if(EposProject.getInstance().isChangeAppIcon(item.getEnTag()))
            switchAppIconAndLable(activity);
        new Thread(new SwitchPayChannel(0)).start();
    }

    public static void switchAppIconAndLable(Activity activity) {
        if (EposProject.getInstance().isBaseProject(ConfigureManager.getInstance(activity).getProject()))
            return;
        String prjMainAliasClassName = genPrjMainActivityClassName(activity);
        if (TextUtils.isEmpty(prjMainAliasClassName))
            return;

//        try {
//            Class.forName(prjMainAliasClassName);
//        } catch (ClassNotFoundException e) {
//            XLogUtil.d(TAG, "^_^ 未定义项目启动Activity：" + e.getMessage() + " ^_^");
//            return;
//        }

        PackageManager pm = activity.getPackageManager();
        pm.setComponentEnabledSetting(activity.getComponentName(),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(new ComponentName(activity, prjMainAliasClassName),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        //重启launcher，刷新桌面图标和文字和显示
        restartLauncher(activity, pm);
    }

    /**
     * 重启Launcher
     *
     * @param context 上下文
     * @param pm 包管理器
     */
    public static void restartLauncher(Context context, PackageManager pm) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        List<ResolveInfo> resolves = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo res : resolves) {
            if (res.activityInfo != null) {
                ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
                am.killBackgroundProcesses(res.activityInfo.packageName);
            }
        }
    }

    /**
     * 生成子项目的桌面图标和应用名称所依赖的类名。
     *
     * @param activity 基础版本的启动页
     * @return 子项目启动页别名配置的类名
     */
    public static String genPrjMainActivityClassName(Activity activity) {
        String packageName = activity.getPackageName();
        String className = activity.getComponentName().getClassName();
        String[] spitStr = className.split("\\.");
        String prjName = ConfigureManager.getInstance(activity).getProject().toLowerCase();
        String prjMainAliasClassName = null;
        if (spitStr.length > 1) {
            prjMainAliasClassName = packageName + "." + prjName + "." + spitStr[spitStr.length - 1];
        }
        XLogUtil.d("genPrjMainActivityClassName", "^_^ " + prjMainAliasClassName + " ^_^");
        return prjMainAliasClassName;
    }


}
