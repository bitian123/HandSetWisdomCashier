package com.centerm.epos.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.centerm.epos.EposApplication;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseFragment;
import com.centerm.epos.base.BaseFragmentActivity;
import com.centerm.epos.base.SimpleStringTag;
import com.centerm.epos.bean.ReverseInfo;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.channels.SwitchPayChannel;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.configure.EposProject;
import com.centerm.epos.configure.InitTradeRuntime;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.fragment.LoginFragment;
import com.centerm.epos.function.AppUpgradeUtil;
import com.centerm.epos.mvp.presenter.FactoryMenuPresenter;
import com.centerm.epos.print.PrintManager;
import com.centerm.epos.redevelop.BaseSaveLogo;
import com.centerm.epos.redevelop.ISaveLogo;
import com.centerm.epos.task.AsyncAppInitTask;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DataHelper;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.view.AlertDialog;
import com.centerm.epos.xml.bean.menu.MenuItem;
import com.centerm.epos.xml.keys.Keys;
import com.j256.ormlite.stmt.QueryBuilder;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import config.BusinessConfig;
import config.Config;

import static config.BusinessConfig.Key.KEY_POS_SERIAL;

public class MainActivity extends BaseFragmentActivity {

    public static final int SHOW_LOGIN = 1;
    private boolean initFlag;
    private String projectTag;
    private CommonDao<TradeInfoRecord> tradeInfoDao;
    protected CommonDao<ReverseInfo> reverseDao;

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);

        initFlag = Settings.isAppInit(context);
        String projectName = Settings.getProjectName(context);
        if (TextUtils.isEmpty(projectName) || !EposProject.getInstance().isProjectExist(projectName))
            projectTag = EposProject.getInstance().getBaseProjectTag();
        else
            projectTag = projectName;
    }

    @Override
    public void onInitView() {
        showRightButton(getString(R.string.tip_exit));
        if (!initFlag && EposProject.getInstance().isBaseProject(projectTag)) {
            loadFactoryMenu();
        } else {
            if (initFlag) {
                autoUpgrade();
                new InitTradeRuntime().start();
                loadNextView();
            } else {
                doInitialization();
            }
            //需要延时1.5S，等待远程服务绑定成功。
            new Thread(new SwitchPayChannel(1500)).start();
        }

        //删除PDF文件
        logger.info("删除PDF文件");
        File file = new File(Config.Path.PDF_PATH);
        if(file.isDirectory()&&file.exists()){
            File[] subFile = file.listFiles();
            for(int i=0;i<subFile.length;i++){
                File deleteFile = subFile[i];
                if(deleteFile.exists()){
                    deleteFile.delete();
                }
            }
        }
        //删除收据签名文件文件
        File fileVoucher = new File(Config.Path.VOUCHER_PATH);
        if(fileVoucher.isDirectory()&&fileVoucher.exists()){
            File[] subFile = fileVoucher.listFiles();
            for(int i=0;i<subFile.length;i++){
                File deleteFile = subFile[i];
                if(deleteFile.exists()){
                    deleteFile.delete();
                }
            }
        }

        //删除广告文件 广告文件超过20张清空一次
        File adFile = new File(Config.Path.DOWNLOAD_PATH);
        if(adFile.isDirectory()&&adFile.exists()){
            File[] subFile = adFile.listFiles();
            if(subFile.length>20){
                for(int i=0;i<subFile.length;i++){
                    File deleteFile = subFile[i];
                    if(deleteFile.exists()){
                        deleteFile.delete();
                    }
                }
            }
        }

        tradeInfoDao = new CommonDao<>(TradeInfoRecord.class, dbHelper);
        QueryBuilder<TradeInfoRecord, String> qb = tradeInfoDao.queryBuilder();
        try {
            qb.orderBy("voucherNo", false);
            List<TradeInfoRecord> dataList = qb.query();
            List<TradeInfoRecord> deleteList = new ArrayList<>();
            for(TradeInfoRecord record : dataList){
                if(DataHelper.isTimeout(record.getTransYear(),record.getTransDate())){
                    deleteList.add(record);
                }
            }
            if(deleteList!=null&&deleteList.size()>0){
                logger.info("超过交易保存期限，删除"+deleteList.size()+"条记录");
                tradeInfoDao.delete(deleteList);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        reverseDao = new CommonDao<>(ReverseInfo.class,dbHelper);
        QueryBuilder<ReverseInfo, String> qbReverse = reverseDao.queryBuilder();
        try {
            qbReverse.orderBy(TransDataKey.iso_f11, false);
            List<ReverseInfo> dataList = qbReverse.query();
            List<ReverseInfo> deleteList = new ArrayList<>();
            for(ReverseInfo record : dataList){
                if(DataHelper.isTimeoutReverse(record.getTransTime())){
                    deleteList.add(record);
                }
            }
            if(deleteList!=null&&deleteList.size()>0){
                logger.info("超过交易保存期限，删除"+deleteList.size()+"条冲正");
                reverseDao.delete(deleteList);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            CommonUtils.toSDWriteFile(MainActivity.this, "打印测试.pdf");
        } catch (IOException e) {
            e.printStackTrace();
        }

        BusinessConfig.getInstance().setFlag(EposApplication.getAppContext(), BusinessConfig.Key.FLAG_SIGN_IN,false);

    }

    /**
     * 自动更新app版本
     * @return
     */
    private boolean autoUpgrade() {
        BusinessConfig config = BusinessConfig.getInstance();
        if (!config.getFlag(this, SimpleStringTag.TOGGLE_APP_UPGRADE_SUPPORT))
            return false;
        if (config.getFlag(this, SimpleStringTag.TOGGLE_APP_UPGRADE_WIFI_ONLY) && !AppUpgradeUtil.isWifiConnected
                (this)) {
            return false;
        }
        AppUpgradeUtil appUpgradeUtil = AppUpgradeUtil.getInstance();
        appUpgradeUtil.setConnectTimeout(config.getNumber(this, SimpleStringTag.APP_UPGRADE_CONNECT_TIMEOUT));
        appUpgradeUtil.init(this);
        return true;
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (!Settings.isAppInit(this))
            return;
    }

    /**
     * 进行应用初始化操作
     * todo 初始化操作比较耗时，需要优化成异步进行
     */
    public void doInitialization() {
        ConfigureManager manager = ConfigureManager.getInstance(context);
        manager.setProject(context, projectTag);
        //导入签购单模板
        PrintManager printManager = new PrintManager(context);
        printManager.importTemplate();

        //导入打印凭条LOGO
        ISaveLogo saveLogo = (ISaveLogo) ConfigureManager.getInstance(context).getSubPrjClassInstance(
                new BaseSaveLogo());
        saveLogo.save(context);

        new AsyncAppInitTask(this).doImportQpsBinSheet();
        //导入完成
        Settings.setAppInit(context, true);
        new InitTradeRuntime().start();
        ViewUtils.showToast(context, "初始化完成");

        //顶栏横幅存在一个二次开发点，在做初始化的时候，需要设置该图片信息
        setBannerView();
        loadNextView();
    }

    @Override
    public void onRightButtonClick(View view) {
        super.onRightButtonClick(view);
        tipToExit();
    }

    private void tipToExit() {
        /*
        * @author zhouzhihua
        * 默认提示
        * */
        DialogFactory.showSelectDialog(context, getString(R.string.tip_notification), getString(R.string.tip_confirm_exit), new AlertDialog.ButtonClickListener() {
            @Override
            public void onClick(AlertDialog.ButtonType button, View v) {
                switch (button) {
                    case POSITIVE:
                        BusinessConfig config = BusinessConfig.getInstance();
                        String current = config.getValue(context, BusinessConfig.Key.KEY_OPER_ID);
                        config.setValue(context, BusinessConfig.Key.KEY_LAST_OPER_ID, current);
                        config.setValue(context, BusinessConfig.Key.KEY_OPER_ID, null);
                        replace(new LoginFragment()).commit();
                        break;
                }
            }
        });
    }

    /**
     * 加载工厂菜单
     */
    private void loadFactoryMenu() {
        MenuItem item = getSingleFactoryItem();
        if (item == null)
            super.loadFactoryMenuView();
        else {
            FactoryMenuPresenter.doInitByMainActivity(this, item);
        }
    }

    private void loadNextView() {
        BusinessConfig config = BusinessConfig.getInstance();
        String operId = config.getValue(context, BusinessConfig.Key.KEY_OPER_ID);
//        boolean needOperLogin = getConfigureManager().isOptionFuncEnable(context, Keys.obj().operator_login);
        boolean needOperLogin = !ConfigureManager.getInstance(context).isOptionFuncEnable(context, Keys.obj()
                .back_direct_to_launcher);

        //用于控制普通操作员首次登录时，需要显示登录界面。
        boolean needOperLoginWhenNull = ConfigureManager.getInstance(context).isOptionFuncEnable(context, Keys.obj()
                .needLogin_when_opernull);
        logger.info("当前操作员：" + operId);
        if (TextUtils.isEmpty(operId)) {
            if (needOperLogin || needOperLoginWhenNull) {
                replace(new LoginFragment()).commit();
            } else {
                String defaultId = getConfigureManager().getDefaultParamsPool(context).getString(Keys.obj()
                        .def_oper_id);
                logger.info("正在设置默认操作员号：" + defaultId);
                config.setValue(context, BusinessConfig.Key.KEY_OPER_ID, defaultId);
                //loadMenuView(getConfigureManager().getPrimaryMenu(context));
                loadGTMenuView(getConfigureManager().getPrimaryMenu(context), null);
            }
        } else if (Config.DEFAULT_ADMIN_ACCOUNT.equals(operId)) {
            loadMenuView(getConfigureManager().getThirdlyMenu(context));
        } else if (Config.DEFAULT_MSN_ACCOUNT.equals(operId)) {
            loadMenuView(getConfigureManager().getSecondaryMenu(context));
        } else {
            if (needOperLogin) {
                replace(new LoginFragment()).commit();
            } else {
                //loadMenuView(getConfigureManager().getPrimaryMenu(context));
                loadGTMenuView(getConfigureManager().getPrimaryMenu(context), null);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (CommonUtils.isFastClick())
            return;
        BaseFragment showingFrg = (BaseFragment) getShowingFragment();
        if (showingFrg != null && showingFrg.onBackPressed()) {
            return;
        }
        int stackSize = getFragmentManager().getBackStackEntryCount();
        if (stackSize == 1) {
            hideBackBtn();
        }
        if (stackSize == 0) {
            DialogFactory.showSelectDialog(context, null, "确认退出？", new AlertDialog
                    .ButtonClickListener() {
                @Override
                public void onClick(AlertDialog.ButtonType button, View v) {
                    switch (button) {
                        case POSITIVE:
                            BusinessConfig config = BusinessConfig.getInstance();
                            String current = config.getValue(context, BusinessConfig.Key.KEY_OPER_ID);
                            config.setValue(context, BusinessConfig.Key.KEY_LAST_OPER_ID, current);
                            config.setValue(context, BusinessConfig.Key.KEY_OPER_ID, null);
                            replace(new LoginFragment()).commit();
                            break;
                    }
                }
            });
        } else {
            getFragmentManager().popBackStackImmediate();
        }
    }

    public void setProject(String project) {
        projectTag = project;
        Settings.setProjectName(context, project);
        ConfigureManager.getInstance(context).setProject(context, project);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        logger.info("请求码：" + requestCode+" | 返回码：" + resultCode);
        if(data!=null){
            int posSerial = data.getIntExtra(KEY_POS_SERIAL,-1);
            if(-1 != posSerial){
                logger.debug("同步流水号:"+posSerial);
                BusinessConfig.getInstance().setNumber(EposApplication.getAppContext(), KEY_POS_SERIAL, posSerial);
            }
        }
        if(requestCode==8&&resultCode==88){
            logger.info("AID参数完成下载");
            BusinessConfig.getInstance().setFlag(EposApplication.getAppContext(), TransDataKey.FLAG_HAS_DOWNLOAD_AID_COMMON,true);
        }else if(requestCode==9&&resultCode==99){
            logger.info("公钥参数完成下载");
            BusinessConfig.getInstance().setFlag(EposApplication.getAppContext(), TransDataKey.FLAG_HAS_DOWNLOAD_CARK_COMMON,true);
        }else if(requestCode==5&&resultCode==55){
            BusinessConfig config = BusinessConfig.getInstance();
            String current = config.getValue(context, BusinessConfig.Key.KEY_OPER_ID);
            config.setValue(context, BusinessConfig.Key.KEY_LAST_OPER_ID, current);
            config.setValue(context, BusinessConfig.Key.KEY_OPER_ID, null);
            config.setNumber(EposApplication.getAppContext(), KEY_POS_SERIAL, 1);
            replace(new LoginFragment()).commit();
        }else if(requestCode==4&&resultCode==44){
            logger.info("签到成功");
            BusinessConfig.getInstance().setFlag(EposApplication.getAppContext(), BusinessConfig.Key.FLAG_SIGN_IN,true);
        }
    }

}




