package com.centerm.epos.base;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.epos.ActivityStack;
import com.centerm.epos.EposApplication;
import com.centerm.epos.R;
import com.centerm.epos.activity.LoginActivity;
import com.centerm.epos.channels.EnumChannel;
import com.centerm.epos.common.Settings;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.configure.EposProject;
import com.centerm.epos.db.CommonManager;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.redevelop.ICommonManager;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.ResourceUtils;
import com.centerm.epos.utils.StopWatch;
import com.centerm.epos.view.AlertDialog;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import org.apache.log4j.Logger;

import java.sql.SQLException;

import config.BusinessConfig;
import config.Config;

/**
 * author:wanliang527</br>
 * date:2016/10/21</br>
 */
public abstract class BaseActivity extends AppCompatActivity {

    //    public static final int REQ_REVERSE = 0x12;//前往联机交互界面
    public static final int REQ_INPUT_DIREOTOR_PWD = 0x13;//前往输入主管密码的界面
    public static final int REQ_TRANSACTION = 0x14;//前往交易界面
    public static final int REQ_LOCAL_FUNCTION = 0x15;//前往本地功能

    public static final int REQ_JBOSS_PAYMENT = 0x20; //前往杰埔实支付渠道

    //    public static final String KEY_RECREATE_FLAG = "KEY_RECREATE_FLAG";
    public static final String KEY_AUTO_SIGN = "KEY_AUTO_SIGN";
    public static final String KEY_OUT_BUNDLE= "KEY_OUT_BUNDLE";//外部传进来的Bundle数据（作为支付组件时，会有该值）
    public static final String KEY_NEED_ACT_RESULT= "KEY_NEED_ACT_RESULT";//交易完成后，需要返回结果

    public static final String KEY_PROCESS = "KEY_PROCESS";
    public static final String KEY_MENU = "KEY_MENU";
    public static final String KEY_TRANSCODE = "KEY_TRANSCODE";
    public static final String KEY_ORIGIN_INFO = "KEY_ORIGIN_INFO";
    public static final String KEY_TRADE_INFO = "KEY_TRADE_INFO";
    public static final String KEY_USER_FLAG = "KEY_USER_FLAG";//登录用户标识，1-系统管理员；2-主管操作员
    public static final String KEY_INSERT_SALE_FLAG = "KEY_INSERT_SALE_FLAG";//插卡消费标识（插卡消费与消费使用的是同一个流程以及同样的交易码）
    public static final String KEY_CLSS_FORCE_PIN_FLAG = "KEY_CLSS_FORCE_PIN_FLAG";//闪付凭密标识
    public static final String KEY_UI_STATUS = "KEY_UI_STATUS";//ui界面是如何产生的 是 jumpToNext 或者 jumpToPre


    public static boolean reverseFlag;//冲正标识，决定下次联机前是否进行冲正

    protected Logger logger = Logger.getLogger(this.getClass());
    protected Context context;
    protected ActivityStack activityStack = ActivityStack.getInstance();
    protected EnumChannel posChannel;
    protected DbHelper dbHelper;
    private boolean isPause;
    //    private AppUpdateReceiver appUpdateReceiver;
    private long lastKeyEventTime;//上一次实体按键的时间
    private StringBuilder secretCode = new StringBuilder();//暗码组合器

    protected long pageTimeout = Config.PAGE_TIMEOUT;
//    protected GlobalTouchListener touchListener;
    protected StopWatch stopWatch;
    protected StopWatch.TimeoutHandler timeoutHandler;
    //==================================================================================================

    /**
     * 获取项目的配置管理
     *
     * @return 配置管理对象
     * @date 2017-02-17
     */
    public ConfigureManager getConfigureManager() {
        return ConfigureManager.getInstance(context);
    }

    /**
     * 根据当前项目获取布局文件的ID。
     * 布局文件命名规则：[项目代号]_[基础版本文件名称]
     * 例如：基础版本中文件命名为activity_login.xml，对应钱宝项目的布局文件命名为c001_activity_login.xml
     *
     * @param commonName 基础版本中布局文件名称
     * @return 当前项目的布局ID，可能为0，此时需要上层自行处理
     */
    protected int getLayoutId(String commonName) {
        //基础版本则直接返回，不用再获取项目的资源ID
        if(EposProject.getInstance().isBaseProject(getConfigureManager().getProject()))
            return -1;
        String prefix = EposProject.getInstance().getId(getConfigureManager().getProject());
        return ResourceUtils.getLayoutId(context, prefix.toLowerCase() + "_" + commonName);
    }

    protected int getDrawableId(String commonName) {
        if(EposProject.getInstance().isBaseProject(getConfigureManager().getProject()))
            return -1;
        String prefix = EposProject.getInstance().getId(getConfigureManager().getProject());
        return ResourceUtils.getDrawableId(context, prefix.toLowerCase() + "_" + commonName);
    }

    @Override
    protected void onResume() {
        isPause = false;
        super.onResume();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN && stopWatch != null){
            if (stopWatch.isRunning())
                stopWatch.reset();
        }
        return super.onTouchEvent(event);
    }

    /**
     * 初始化数据对象，在{@method onInitView}和{@method onLayoutId}之前进行
     */
    public void onInitLocalData(Bundle savedInstanceState) {
//        posChannel = EnumProject.valueOf(Settings.getProjectName(this));
    }

    public void afterInitView() {
    }

    public DbHelper getDbHelper() {
        return dbHelper;
    }

    /**
     * 在此方法中返回Activty的布局ID
     *
     * @return 返回该Activity界面布局ID
     */
    public abstract int onLayoutId();

    /**
     * 初始化界面视图
     */
    public abstract void onInitView();

    /**
     * @return 是否开启数据库模块
     */
    public boolean isOpenDataBase() {
        return false;
    }

    public void openPageTimeout() {
        if(Config.isEnableShowingTimeout){
            return;
        }
        if (stopWatch == null) {
            stopWatch = new StopWatch(context, this.pageTimeout);
        }
        if (timeoutHandler == null) {
            timeoutHandler = new StopWatch.TimeoutHandler() {
                @Override
                public void onTimeout() {
                    stopWatch.stop();//停止计时任务
                    if (context == null) {
                        logger.error("^_^ activity 已经销毁，还进行超时处理！ ^_^");
                        return;
                    }
                    AlertDialog dialog = DialogFactory.showSelectDialog(context, "提示", "长时间未操作\n是否返回主界面", new AlertDialog.ButtonClickListener() {
                        @Override
                        public void onClick(AlertDialog.ButtonType button, View v) {
                            switch (button) {
                                case POSITIVE:
//                                    onBackPressed();
                                    activityStack.pop();
                                    break;
                                case NEGATIVE:
                                    stopWatch.start();//重新开始计时任务
                                    break;
                            }
                        }
                    }, 30);
                    dialog.setAutoPerformPositive(true);
                }
            };
        }
        stopWatch.setTimeoutHandler(timeoutHandler);
        stopWatch.start();
//        if (touchListener == null) {
//            touchListener = new GlobalTouchListener() {
//                @Override
//                public void onTouch(int i) {
//                    stopWatch.reset();//计时复位
//                }
//            };
//        }
//        try {
//            final ISystemService systemService = DeviceFactory.getInstance().getSystemDev();
//            systemService.addGlobalTouchListener(touchListener);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public void jumpMethod() {
    }

//    public void openElecPageTimeout(int timeOutS) {
//        if (stopWatch == null) {
//            stopWatch = new StopWatch(context, timeOutS == 0 ? this.pageTimeout : timeOutS);
//        }
//        if (timeoutHandler == null) {
//            timeoutHandler = new StopWatch.TimeoutHandler() {
//                @Override
//                public void onTimeout() {
//                    stopWatch.stop();//停止计时任务
//                    AlertDialog dialog = DialogFactory.showSelectDialog(context, "提示", "长时间未签名\n是否跳过电子签名", new AlertDialog.ButtonClickListener() {
//                        @Override
//                        public void onClick(AlertDialog.ButtonType button, View v) {
//                            switch (button) {
//                                case POSITIVE:
//                                    jumpMethod();
//                                    break;
//                                case NEGATIVE:
//                                    stopWatch.start();//重新开始计时任务
//                                    break;
//                            }
//                        }
//                    }, 30);
//                    dialog.setAutoPerformPositive(true);
//                }
//            };
//        }
//        stopWatch.setTimeoutHandler(timeoutHandler);
//        stopWatch.start();
//        if (touchListener == null) {
//            touchListener = new GlobalTouchListener() {
//                @Override
//                public void onTouch(int i) {
//                    stopWatch.reset();//计时复位
//                }
//            };
//        }
//        try {
//            final ISystemService systemService = DeviceFactory.getInstance().getSystemDev();
//            systemService.addGlobalTouchListener(touchListener);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public void closePageTimeout() {
//        if (touchListener != null) {
//            try {
//                ISystemService systemService = DeviceFactory.getInstance().getSystemDev();
//                systemService.removeGlobalTouchListener(touchListener);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
        if (stopWatch != null) {
            stopWatch.stop();
        }
    }

    private final void OpenDatabase() {
        if (isOpenDataBase())
            dbHelper = OpenHelperManager.getHelper(this.getApplicationContext(), DbHelper.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*OpenDatabase()最开始初始化，防止数据库还未初始化，Fragment使用数据
        *导致程序崩溃，zhouzhihua modify
        * */
        OpenDatabase();
        super.onCreate(savedInstanceState);
        /*
        * @author zhouzhihua
        * 状态栏透明化
        * */
        Settings.getUIChannel(this);
        if( Settings.bIsSettingBlueTheme() ) {
            setStatusBarUpperAPI21();
        }

        context = this;
        activityStack.push(this);
         //OpenDatabase();
        onInitLocalData(savedInstanceState);
        int layoutId = onLayoutId();
        if (layoutId > 0) {
            setContentView(layoutId);
            _initView();
        }
        onInitView();
        afterInitView();
        CommonUtils.resetLastClickTime();
    }

    private final void _initView() {
        View v = findViewById(R.id.imgbtn_back);
        if (v != null)
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
    }


    /**
     * 检测终端设备是否准备就绪，准备就绪即可开始交易
     *
     * @return 如果准备就绪，返回true，否则返回false
     */
    public boolean isDeviceReady() {
        DeviceFactory factory = DeviceFactory.getInstance();
        return factory.isAvailable();
    }

    public boolean[] isNeedTmkOrSignin(String menuTag) {
        String iso41 = BusinessConfig.getInstance().getIsoField(context, 41);
        String iso42 = BusinessConfig.getInstance().getIsoField(context, 42);
        String iso43 = BusinessConfig.getInstance().getIsoField(context, 43);
        boolean needDownloadTmk = TextUtils.isEmpty(iso41) || TextUtils.isEmpty(iso42) || TextUtils.isEmpty(iso43);
        boolean needSigned = !BusinessConfig.getInstance().getFlag(context, BusinessConfig.Key.FLAG_SIGN_IN);
        switch (menuTag) {
//            case XmlTag.MenuTag.DOWNLOAD_CAPK:
//            case XmlTag.MenuTag.DOWNLOAD_AID:
//            case XmlTag.MenuTag.DOWNLOAD_CARD_BIN:
//            case XmlTag.MenuTag.DOWNLOAD_QPS_PARAMS:
//                return new boolean[]{needDownloadTmk, false};
//            case XmlTag.MenuTag.BALANCE:
//            case XmlTag.MenuTag.SALE:
//            case XmlTag.MenuTag.VOID:
//            case XmlTag.MenuTag.REFUND:
//            case XmlTag.MenuTag.AUTH:
//            case XmlTag.MenuTag.AUTH_COMPLETE:
//            case XmlTag.MenuTag.AUTH_SETTLEMENT:
//            case XmlTag.MenuTag.CANCEL:
//            case XmlTag.MenuTag.COMPLETE_VOID:
//            case XmlTag.MenuTag.SALE_BY_INSERT:
//            case XmlTag.MenuTag.QUICK_SALE_NEED_PASWD:
//            case XmlTag.MenuTag.QUICK_AUTH_NEED_PASWD:
//                return new boolean[]{needDownloadTmk, needSigned};
            default:
                return new boolean[]{false, false};
        }
    }


    protected boolean isNeedSignOut(String menuTag) {
        boolean needSignOut = BusinessConfig.getInstance().getFlag(context, BusinessConfig.Key.KEY_IS_BATCH_BUT_NOT_OUT);
        switch (menuTag) {
//            case XmlTag.MenuTag.POS_SIGN_IN:
//            case XmlTag.MenuTag.BALANCE:
//            case XmlTag.MenuTag.SALE:
//            case XmlTag.MenuTag.VOID:
//            case XmlTag.MenuTag.REFUND:
//            case XmlTag.MenuTag.AUTH:
//            case XmlTag.MenuTag.AUTH_COMPLETE:
//            case XmlTag.MenuTag.AUTH_SETTLEMENT:
//            case XmlTag.MenuTag.CANCEL:
//            case XmlTag.MenuTag.COMPLETE_VOID:
//            case XmlTag.MenuTag.SALE_BY_INSERT:
//            case XmlTag.MenuTag.QUICK_SALE_NEED_PASWD:
//            case XmlTag.MenuTag.QUICK_AUTH_NEED_PASWD:
//                return needSignOut;
            default:
                return false;
        }
    }

    @Override
    public void onBackPressed() {
        if (CommonUtils.isFastClick()) {
            logger.debug("==>重复的onBackPressed事件，不响应！");
            return;
        }
        activityStack.pop(false);
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onPause() {
        isPause = true;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (isOpenDataBase()) {
            OpenHelperManager.releaseHelper();
            dbHelper = null;
        }
        context = null;
//        unRegisterAppUpdateNotification();
        super.onDestroy();

    }

    @Override
    public void setTitle(int titleId) {
        TextView titleShow = (TextView) findViewById(R.id.txtvw_title);
        if (titleShow == null) {
            logger.warn(this.getClass().getSimpleName() + "==>设置标题失败");
            return;
        }
        titleShow.setText(titleId);
    }

    public boolean isPause() {
        return isPause;
    }

    /**
     * 隐藏整个标题栏
     */
    public void hideTitleBar() {
        View view = findViewById(R.id.layout_title);
        if (view != null) {
            view.setVisibility(View.GONE);
        } else {
            logger.warn("==>隐藏标题栏失败");
        }
    }

    public void showTitleBar() {
        View view = findViewById(R.id.layout_title);
        if (view != null) {
            view.setVisibility(View.VISIBLE);
        } else {
            logger.warn("==>显示标题栏失败");
        }
    }

    /**
     * 隐藏返回按钮
     */
    public void hideBackBtn() {
        ImageButton backBtn = (ImageButton) findViewById(R.id.imgbtn_back);
        if (backBtn == null) {
            logger.warn(this.getClass().getSimpleName() + "==>隐藏返回按钮失败");
            return;
        }
        backBtn.setVisibility(View.GONE);
    }

    /**
     * 显示返回按钮
     */
    public void showBackBtn() {
        final ImageButton backBtn = (ImageButton) findViewById(R.id.imgbtn_back);
        if (backBtn == null) {
            logger.warn(this.getClass().getSimpleName() + "==>显示返回按钮失败");
            return;
        }
        backBtn.setVisibility(View.VISIBLE);
    }

    /**
     * 显示右侧的功能按钮，按钮点击事件由{@link #onRightButtonClick(View)}方法触发
     *
     * @param label 按钮标签
     */
    public void showRightButton(String label) {
        Button button = (Button) findViewById(R.id.btn_title_right);
        if (button != null) {
            button.setVisibility(View.VISIBLE);
            button.setText(label);
        } else {
            logger.warn(this.getClass().getSimpleName() + "==>显示[" + label + "]按钮失败");
        }
    }

    public void hideRightButton() {
        Button button = (Button) findViewById(R.id.btn_title_right);
        if (button != null) {
            button.setVisibility(View.GONE);
        }
    }

    public void onRightButtonClick(View view) {
    }

    /**
     * 结束当前界面并跳转到登录界面
     */
    protected void jumpToLogin() {
        //在此处处理，在用户签退或者退出时，将操作员账号置空
        BusinessConfig config = BusinessConfig.getInstance();
        config.setValue(context, BusinessConfig.Key.KEY_OPER_ID, null);
        Intent intent = new Intent(context, LoginActivity.class);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                activityStack.removeExcept(LoginActivity.class);
            }
        }, 300);
        startActivity(intent);
    }

    protected void jumpToAppUpdate() {
//        Intent intent = new Intent(context, AppUpdateActivity.class);
//        startActivity(intent);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            //点击空白处将输入法隐藏
            if (isShouldHideInput(v, ev)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context
                        .INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }


    private boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_HOME:
                logger.info("^_^ home key is pressed but ignored by BaseActivity ^_^");
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }
    /*
    *系统字体改变，应用字体不会随之改变
    * BUGID:0002163
     */

    @Override
    public Resources getResources() {
        Resources res = super.getResources();

        Configuration config = super.getResources().getConfiguration();//new Configuration();
        //config.setToDefaults();
        config.fontScale = 1;
        res.updateConfiguration(config,res.getDisplayMetrics() );
        return res;
    }
    /*
    *Android5.0版本以上
    * @author zhouzhihua
    * */
    private void setStatusBarUpperAPI21() {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
    }
    /*
    *@author:zhouzhihua add 2017.11.08
    * 增加是否存在交易流水的判断
    * */
    public boolean bIsHaveVoucher()
    {
        long counts = 0;

        try {
            ICommonManager commonManager = (ICommonManager) ConfigureManager.getInstance(this).getSubPrjClassInstance(new CommonManager());
            counts = commonManager.getBatchCount();
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return (counts > 0);
    }
    public void initBackBtn(){
        if(findViewById(R.id.mBtnReturn)!=null){
            findViewById(R.id.mBtnReturn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }
        setHotline();
    }

    public void setHotline(){
        TextView mHotLine = (TextView) findViewById(R.id.mHotLine);
        if(mHotLine!=null){
            String hotLine = BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), BusinessConfig.Key.HOTLINE_KEY);
            if(!TextUtils.isEmpty(hotLine)){
                mHotLine.setText(hotLine);
            }
        }

    }

}