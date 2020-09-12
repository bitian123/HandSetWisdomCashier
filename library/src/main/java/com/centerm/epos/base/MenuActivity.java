package com.centerm.epos.base;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.TextView;

import com.centerm.epos.R;
import com.centerm.epos.adapter.MenuAbsListAdapter;
import com.centerm.epos.bean.ReverseInfo;
import com.centerm.epos.channels.EnumChannel;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.view.AlertDialog;
import com.centerm.epos.view.GridViewPager;
import com.centerm.epos.xml.XmlParser;
import com.centerm.epos.xml.bean.menu.Menu;
import com.centerm.epos.xml.bean.menu.MenuItem;
import com.centerm.epos.xml.bean.process.TradeProcess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import com.centerm.epos.helper.QianBaoMenuHelper;
//import com.centerm.epos.xml.XmlTag;


/**
 * 菜单界面
 * author:wanliang527</br>
 * date:2016/10/26</br>
 */

public class MenuActivity extends BaseActivity {
    protected Menu menu;
    private MenuAbsListAdapter absListAdapter;
    private GridViewPager.GridPagerAdapter gridPagerAdapter;
    private MenuItem waitForExecuteItem;
    private CommonDao<ReverseInfo> reverseDao;

    @Override
    protected void onResume() {
        super.onResume();
//        updateReverseFlag();
//        if (isReceiveAppUpdateEvent()) {
//            tipToUpdateApp();
//        }
    }


    @Override
    public boolean isOpenDataBase() {
        return true;
    }

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        Intent intent = getIntent();
        menu = intent.getParcelableExtra(KEY_MENU);
      /*  int acctFlag = intent.getIntExtra(KEY_USER_FLAG, 0);
        if (menu == null) {
            logger.warn(this.getClass().getSimpleName() + "，Menu对象需要从文件中解析");
            //MainActivity的menu对象由XML解析
            EnumChannel channel = EnumChannel.valueOf(Settings.getProjectName(this));
//            menu = XmlParser.parseMenu(context, channel);
            if (this instanceof MainActivity) {
                //屏蔽管理员入口
                menu.removeItem("SUPER_MANAGEMENT");
                menu.removeItem("SYS_MANAGEMENT");
            } else if (acctFlag == 1) {
                //系统管理员菜单界面
                menu = (Menu) findMenuItem("SYS_MANAGEMENT");
//                registerAppUpdateNotification();
            } else if (acctFlag == 2) {
                //主管操作员菜单界面
                menu = (Menu) findMenuItem("SUPER_MANAGEMENT");
//                registerAppUpdateNotification();
            }
        }*/
    }

    @Override
    public int onLayoutId() {
        int id = R.layout.fragment_menu_list;
        if (menu != null) {
            id = menu.getStructure().equals(Menu.ViewStructure.GRID) ? R.layout
                    .fragment_menu_grid : id;
        }
        return id;
    }

    @Override
    public void onInitView() {
        View rootView = findViewById(R.id.root_view);
        hideTitleBar();
      /*  if (this instanceof MainActivity) {
            //设置背景
            rootView.setBackgroundColor(getResources().getColor(R.color.secondary_bg));
        }*/
        TextView txtvw = (TextView) findViewById(R.id.txtvw_title);
        if (txtvw != null) {
            if (!TextUtils.isEmpty(menu.getTextResName())) {
                int resId = getResources().getIdentifier(menu.getTextResName(), "string",
                        getPackageName());
                if (resId > 0)
                    txtvw.setText(resId);
                else
                    txtvw.setText(menu.getChnTag());
            } else
                txtvw.setText(menu.getChnTag());
        }
        View view = findViewById(R.id.menu_view);
        if (view instanceof AbsListView) {
            absListAdapter = new MenuAbsListAdapter(context, menu);
            ((AbsListView) view).setAdapter(absListAdapter);
            ((AbsListView) view).setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    MenuItem item = absListAdapter.getItem(position);
                    onMenuItemClick(view, item);
                }
            });
          /*  if ("MANAGEMENT".equals(menu.getEnTag())
                    || "OTHER".equals(menu.getEnTag())
                    || "PRINT".equals(menu.getEnTag())
                    || "SIGN_IN".equals(menu.getEnTag())
                    || "QUICK_PAY_NEED_PASWD".equals(menu.getEnTag())
                    || "MODIFY_PASWD".equals(menu.getEnTag())
                    || "OTHER_SETUP".equals(menu.getEnTag())
                    || "DOWNLOAD_FUNCTION".equals(menu.getEnTag())) {
                findViewById(R.id.top_banner).setVisibility(View.GONE);
                showTitleBar();
            }*/
        } else if (view instanceof GridViewPager) {
            int len = menu.getCounts();
            List<Map<String, ?>> data = new ArrayList<>();
            final String TEXT = "text";
            final String ICON = "icon";
            final String ITEMS = "items";
            for (int i = 0; i < len; i++) {
                MenuItem tempMenu = menu.getItem(i);
                Map<String, Object> map = new HashMap<>();
                if (!TextUtils.isEmpty(tempMenu.getTextResName())) {
                    int resId = getResources().getIdentifier(tempMenu.getTextResName(), "string",
                            getPackageName());
                    if (resId > 0)
                        map.put(TEXT, getString(resId));
                    else
                        map.put(TEXT, tempMenu.getChnTag());
                } else
                    map.put(TEXT, tempMenu.getChnTag());
                if (!TextUtils.isEmpty(tempMenu.getIconResName())) {
                    int resId = getResources().getIdentifier(tempMenu.getIconResName(),
                            "drawable", getPackageName());
                    if (resId > 0) {
                        map.put(ICON, resId);
                    } else map.put(ICON, R.drawable.ic_launcher);
                } else map.put(ICON, R.drawable.ic_launcher);
                map.put(ITEMS, tempMenu);
                data.add(map);
            }
            gridPagerAdapter = new GridViewPager.GridPagerAdapter(this, R.layout
                    .common_menu_grid_item, new String[]{TEXT, ICON},
                    new int[]{R.id.menu_text_show, R.id.menu_icon_show}, data);
            ((GridViewPager) view).setAdapter(gridPagerAdapter);
            ((GridViewPager) view).setOnItemClickListener(new GridViewPager.ItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    MenuItem item = menu.getItem(position);
                    onMenuItemClick(view, item);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_INPUT_DIREOTOR_PWD:
                if (resultCode == RESULT_OK) {
                    jumpToMenu((Menu) waitForExecuteItem);
                    waitForExecuteItem = null;
                }
                break;
        }

    }

    @Override
    public void onBackPressed() {
        if ("SUPER_MANAGEMENT".equals(menu.getEnTag())
                || "SYS_MANAGEMENT".equals(menu.getEnTag())) {
            DialogFactory.showSelectDialog(context, null, "确认退出？", new AlertDialog
                    .ButtonClickListener() {
                @Override
                public void onClick(AlertDialog.ButtonType button, View v) {
                    switch (button) {
                        case POSITIVE:
                            //activityStack.pop();
                            jumpToLogin();
                            break;
                    }
                }
            });
        } else {
            super.onBackPressed();
        }
    }

    public void setBannerVisibility(int visibility) {
        findViewById(R.id.top_banner).setVisibility(visibility);
    }

    private MenuItem findMenuItem(String enTag) {
        for (MenuItem item : menu.getItemList()) {
            if (item.getEnTag().equals(enTag)) {
                return item;
            }
        }
        return null;
    }

    protected void onMenuItemClick(View view, final MenuItem item) {
        if (CommonUtils.isFastClick()) {
            logger.debug("==>重复的onBackPressed事件，不响应！");
            return;
        }
        logger.debug("点击：" + item.getChnTag());
        //是否属于子菜单项
        if (item instanceof Menu) {
            logger.debug("点击：" + "是否属于Menu");
           /* if (XmlTag.MenuTag.SUPER_MANAGEMENT.equals(item.getEnTag())) {
                //要求输入主管密码
                Intent intent = new Intent(context, InputDirectorPwdActivity.class);
                waitForExecuteItem = item;
                startActivityForResult(intent, REQ_INPUT_DIREOTOR_PWD);
                return;
            }*/
            //跳转到下一级菜单
            boolean success = jumpToMenu((Menu) item);
            if (!success) {
                ViewUtils.showToast(context, R.string.tip_menu_undefined);
            }
        } else {
            if (!isDeviceReady()) {
                ViewUtils.showToast(context, R.string.tip_device_not_ready);
                return;
            }
//            if (!XmlTag.MenuTag.OFFLINE_MENU.contains(item.getEnTag())) {
//                //清除交易流水可以在无网络下操作，故排除
//                if (!NetUtils.isNetConnected(context) && !item.getEnTag().equals(XmlTag.MenuTag
//                        .CLEAR_TRADE_SERIAL)) {
//                    ViewUtils.showToast(context, R.string.tip_network_unavailable);
//                    return;
//                }
//                if (!CommonUtils.isOnCharging(context) && CommonUtils.getBatteryPercent(context)
//                        < 0.15f) {
//                    DialogFactory.showMessageDialog(context, "电量不足", "请连接电源后进行交易", new
//                            AlertDialog.ButtonClickListener() {
//
//                                @Override
//                                public void onClick(AlertDialog.ButtonType button, View v) {
//                                }
//                            });
//                    return;
//                }
//            }
            boolean[] bArray = isNeedTmkOrSignin(item.getEnTag());
            if (bArray[0]) {
                ViewUtils.showToast(context, R.string.tip_download_tmk);
                return;
            } else if (bArray[1]) {
                DialogFactory.showSelectDialog(context, null, "请签到后开始交易", new AlertDialog
                        .ButtonClickListener() {

                    @Override
                    public void onClick(AlertDialog.ButtonType button, View v) {
                        switch (button) {
                            case POSITIVE:
                                beginProcess(TransCode.SIGN_IN, "SIGN_IN");
                                break;
                        }
                    }
                });
                return;
            }

            if (isNeedSignOut(item.getEnTag())) {
                DialogFactory.showSelectDialog(context, null, "批结算完成，请签退！", new AlertDialog
                        .ButtonClickListener() {
                    @Override
                    public void onClick(AlertDialog.ButtonType button, View v) {
                        switch (button) {
                            case POSITIVE:
                                Intent intent = new Intent(context, TradeFragmentContainer.class);
                                intent.putExtra(KEY_TRANSCODE, TransCode.SIGN_OUT);
                                context.startActivity(intent);
                                break;
                        }
                    }
                });
                return;
            }

//            //流水超上限，先批结算后再交易。
//            if (XmlTag.MenuTag.TRADING_AFTER_SETTLEMENT_MENU.contains(item.getEnTag())
//                    && BusinessConfig.getInstance().getFlag(context, BusinessConfig.Key
//                    .FLAG_TRADE_STORAGE_WARNING)) {
//                DialogFactory.showSelectDialog(context, null, "请结算后开始交易", new AlertDialog
//                        .ButtonClickListener() {
//                    @Override
//                    public void onClick(AlertDialog.ButtonType button, View v) {
//                        switch (button) {
//                            case POSITIVE:
//                                Intent intent = new Intent(context, TradingActivity.class);
//                                intent.putExtra(KEY_TRANSCODE, TransCode.SETTLEMENT);
//                                context.startActivity(intent);
//                                break;
//                        }
//                    }
//                });
            return;
        }
       /*     // TODO: 2016/11/22 脚本结果通知和冲正不要放在这里执行，应该放到TradingActivity执行，执行完成后继续执行原交易
            if (reverseFlag && !(TransCode.SIGN_IN.equals(item.getTransCode()))) {
                //签到之前不进行自动冲正
                Intent intent = new Intent(context, TradingActivity.class);
                intent.putExtra(KEY_TRANSCODE, TransCode.REVERSE);
                startActivityForResult(intent, REQ_REVERSE);
                return;
            }*/
        //特例：如果Item中含有开关，UI需要在这里进行控制，业务控制在onProcess中
        onToggleIfExists(view);
        onProcess(item);
//        }
    }

    private void onToggleIfExists(View view) {
        CheckBox toggleView = (CheckBox) view.findViewById(R.id.toggle);
        if (toggleView != null) {
            boolean t = toggleView.isChecked();
            toggleView.setChecked(!t);
        }
    }

    private void onProcess(MenuItem item) {
        if (item == null) {
            return;
        }
        //进入具体业务流程
        String processFile = item.getProcessFile();
        //优先响应有流程定义的事件
        boolean success = beginProcess(item.getTransCode(), processFile);
        logger.debug("onProcess:" + success);
        if (!success) {
            //没有流程定义，继续寻找事件响应
            success = onNoProcessDefine(item);
        }
        if (!success) {
            ViewUtils.showToast(context, R.string.tip_process_undefined);
        }
    }

    /**
     * 启动交易流程
     *
     * @param processFile 流程定义文件
     * @return 启动成功返回true，失败返回false
     */
    protected boolean beginProcess(String transCode, String processFile) {
        TradeProcess process = XmlParser.parseProcess(context, processFile);
        if (process != null) {
            final Intent intent = new Intent();
            intent.setAction(process.getFirstComponentNode().getComponentName());
//            intent.putExtra(KEY_PROCESS, process);
//            if (transCode.equals(XmlTag.MenuTag.SALE_BY_INSERT)) {
//                //插卡消费
//                intent.putExtra(KEY_TRANSCODE, TransCode.SALE);
//                intent.putExtra(KEY_INSERT_SALE_FLAG, true);
//            } else if (transCode.equals(XmlTag.MenuTag.QUICK_SALE_NEED_PASWD)) {
//                //消费凭密
//                intent.putExtra(KEY_TRANSCODE, TransCode.SALE);
//                intent.putExtra(KEY_CLSS_FORCE_PIN_FLAG, true);
//            } else if (transCode.equals(XmlTag.MenuTag.QUICK_AUTH_NEED_PASWD)) {
//                //预授权凭密
//                intent.putExtra(KEY_TRANSCODE, TransCode.AUTH);
//                intent.putExtra(KEY_CLSS_FORCE_PIN_FLAG, true);
//            } else {
//                intent.putExtra(KEY_TRANSCODE, transCode);
//            }
          /*  if (transCode.equals(XmlTag.MenuTag.OBTAIN_TMK)) {
                DialogFactory.showSelectDialog(context, null, "开始下载主密钥", new AlertDialog
                        .ButtonClickListener() {

                    @Override
                    public void onClick(AlertDialog.ButtonType button, View v) {
                        switch (button) {
                            case POSITIVE:
                                startActivity(intent);
                                break;
                        }
                    }
                });
            } else {
                startActivity(intent);
            }*/
            return true;
        }
        logger.warn(processFile + ", 流程未定义");
        return false;
    }

    /**
     * 跳转到子菜单
     *
     * @param menu 菜单实体对象
     * @return 跳转成功返回ture，失败返回fale
     */
    protected boolean jumpToMenu(Menu menu) {
        if (menu == null) {
            logger.warn("无法跳转菜单，菜单为空");
            return false;
        }
        Intent intent = new Intent(this, MenuActivity.class);
        intent.putExtra(KEY_MENU, menu);
        startActivity(intent);
        return true;
    }

    /**
     * 没有进行流程定义的菜单项在此方法中统一调度和处理
     *
     * @param item 菜单项
     * @return 处理成功返回true，否则返回false
     */
    protected boolean onNoProcessDefine(MenuItem item) {
        EnumChannel posChannel = EnumChannel.valueOf(Settings.getProjectName(context));
        switch (posChannel) {
            case QIANBAO:
                return false;
//                return new QianBaoMenuHelper().onTriggerMenuItem(this, item);
        }
        return false;
    }

    /**
     * 更新冲正标识
     *
     * @return true代表是，则需要先进行冲正交易才能进行其它交易；否则为false
     */
    private void updateReverseFlag() {
        if (reverseDao == null) {
            reverseDao = new CommonDao<>(ReverseInfo.class, dbHelper);
        }
        long counts = reverseDao.countOf();
        if (counts > 0) {
            BaseActivity.reverseFlag = true;
        } else {
            BaseActivity.reverseFlag = false;
        }
        logger.info("==>查询冲正表记录数==>" + counts + "==>更新冲正标志==>" + reverseFlag);
    }
}
