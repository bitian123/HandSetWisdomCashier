package com.centerm.epos.base;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.EnumSDKType;
import com.centerm.epos.EposApplication;
import com.centerm.epos.common.Settings;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.configure.EposProject;
import com.centerm.epos.function.AppUpgradeUtil;
import com.centerm.epos.print.PrintManager;
import com.centerm.epos.redevelop.BaseSaveLogo;
import com.centerm.epos.redevelop.ISaveLogo;
import com.centerm.epos.security.CpaySecurityTool;
import com.centerm.epos.service.CpayLogService;
import com.centerm.epos.service.UpdateAppVersionService;
import com.centerm.epos.task.AsyncAppInitTask;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.XLogUtil;
import com.centerm.epos.xml.bean.menu.Menu;
import com.centerm.epos.xml.bean.project.ProjectConfig;

import config.LogConfiguration;

/**
 * Created by yuhc on 2017/6/15.
 * 应用程序环境
 */

public class ApplicationEnvironment {

    private static final String TAG = ApplicationEnvironment.class.getSimpleName();



    Context mAppContext;

    public ApplicationEnvironment(Context mAppContext) {
        this.mAppContext = mAppContext;
    }

    public static Context currentContext;

    /**
     * 应用环境初始化
     * @param appContext    应用上下文
     */
    public static void init(Context appContext){
        XLogUtil.d(TAG, "^_^ begin application environment init ^_^");
        LogConfiguration.obtainDefault().configure();
        startLogWatcherService(appContext);
        XLogUtil.setIsConfigured(true);
        DeviceFactory factory = DeviceFactory.getInstance();
        factory.init(appContext, EnumSDKType.CPAY_SDK);
        CpaySecurityTool.getInstance().initRuntime(appContext);
        EposProject.getInstance().fillProjectConfig(ConfigureManager.getInstance(appContext).getProjectConfig());
//        ParameterTest.initTestParam(appContext);

        //初始化项目标识
        boolean initFlag = Settings.isAppInit(appContext);
        PrintManager printManager = new PrintManager(appContext);
        if (initFlag) {
            String projectName = Settings.getProjectName(appContext);
            if (TextUtils.isEmpty(projectName) || !EposProject.getInstance().isProjectExist(projectName)) {
                projectName = EposProject.getInstance().getBaseProjectTag();
            }
            ConfigureManager.getInstance(appContext).setProject(appContext, projectName);
            printManager.checkTemplateVersion();
        }else {
            ConfigureManager manager = ConfigureManager.getInstance(appContext);
            String prjTag = getProjectTag(appContext);
            manager.setProject(appContext, prjTag);
            Settings.setProjectName(appContext, prjTag);
            //导入签购单模板
            printManager.importTemplate();

            //导入打印凭条LOGO
            ISaveLogo saveLogo = (ISaveLogo) ConfigureManager.getSubPrjClassInstance(new BaseSaveLogo());
            saveLogo.save(appContext);

            new AsyncAppInitTask(appContext).doImportQpsBinSheet();
            //导入完成
            Settings.setAppInit(appContext, true);
            //new InitTradeRuntime().start();
        }
        AppUpgradeUtil.getInstance();
        XLogUtil.d(TAG, "^_^ end application environment init ^_^");

    }

    public static void startCheckVersion(final Context context){
        boolean isServiceRun=CommonUtils.isServiceRunning( EposApplication.getAppContext(),"com.centerm.epos.service.UpdateAppVersionService");
        XLogUtil.d(TAG, "^_^ is aapShop ServiceRunning :"+isServiceRun);

        if(!isServiceRun){
            Intent intent=new Intent(context, UpdateAppVersionService.class);
            context.startService(intent);
        }
    }


    /**
     * 运行日志清理服务
     * @param context   应用上下文
     */
    private static void startLogWatcherService(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, CpayLogService.class);
        //日志保留天数30天
        intent.putExtra("KEY_MAX_EXITS_DAYS", 30);
        context.startService(intent);
    }

    private static String getProjectTag(Context appContext) {
        Menu factoryMenu = ConfigureManager.getInstance(appContext).getFactoryMenu();
        if (factoryMenu.getCounts() == 1) {
            return factoryMenu.getItem(0).getEnTag();
        }
        ProjectConfig projectConfig = ConfigureManager.getInstance(appContext).getProjectConfig();
        if (projectConfig != null) {
            return projectConfig.getDefaultPrjTag();
        }

        return EposProject.getInstance().getBaseProjectTag();
    }


}
