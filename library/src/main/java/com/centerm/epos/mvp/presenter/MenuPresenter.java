package com.centerm.epos.mvp.presenter;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.centerm.cloudsys.sdk.common.utils.NetUtils;
import com.centerm.cloudsys.sdk.common.utils.StringUtils;
import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.common.exception.ErrorCode;
import com.centerm.cpay.midsdk.dev.define.IPrinterDev;
import com.centerm.cpay.midsdk.dev.define.printer.EnumPrinterStatus;
import com.centerm.epos.EposApplication;
import com.centerm.epos.R;
import com.centerm.epos.activity.msn.BaseQPSSettingActivity;
import com.centerm.epos.adapter.MenuAbsListAdapter;
import com.centerm.epos.base.BaseFragmentActivity;
import com.centerm.epos.base.MenuFragment;
import com.centerm.epos.bean.ReverseInfo;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.common.EncryptAlgorithmEnum;
import com.centerm.epos.common.NFCTradeChannel;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.CommonManager;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.event.MessageCode;
import com.centerm.epos.event.PrinteEvent;
import com.centerm.epos.event.SimpleMessageEvent;
import com.centerm.epos.event.TradeMessage;
import com.centerm.epos.fragment.LoginFragment;
import com.centerm.epos.helper.IMenuHelper;
import com.centerm.epos.mvp.listener.StatusListener;
import com.centerm.epos.mvp.model.IMenuBiz;
import com.centerm.epos.mvp.model.MenuBiz;
import com.centerm.epos.mvp.tag.LocalFunctionTags;
import com.centerm.epos.mvp.view.IMenuView;
import com.centerm.epos.net.htttp.JsonKey;
import com.centerm.epos.print.PrintManager;
import com.centerm.epos.print.PrinterProxy;
import com.centerm.epos.printer.BasePrintSlipHelper;
import com.centerm.epos.printer.IPrintRransData;
import com.centerm.epos.printer.IPrintSlipHelper;
import com.centerm.epos.printer.IPrinterCallBack;
import com.centerm.epos.redevelop.ICommonManager;
import com.centerm.epos.redevelop.IRedevelopAction;
import com.centerm.epos.task.AsyncImportParameterTask;
import com.centerm.epos.task.AsyncQueryPrintDataTask;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.ResourceUtils;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.view.AlertDialog;
import com.centerm.epos.view.GridViewPager;
import com.centerm.epos.view.InputNumberDialog;
import com.centerm.epos.xml.bean.RedevelopItem;
import com.centerm.epos.xml.bean.menu.Menu;
import com.centerm.epos.xml.bean.menu.MenuItem;
import com.centerm.epos.xml.bean.process.TradeProcess;
import com.centerm.epos.xml.keys.Keys;

import org.apache.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;
import config.Config;

import static com.centerm.component.pay.cont.JPayComponent.trans_code;
import static com.centerm.epos.common.TransCode.PRINT_IC_INFO;
import static config.BusinessConfig.Key.KEY_POS_SERIAL;


/**
 * author:wanliang527</br>
 * date:2017/3/1</br>
 */
public class MenuPresenter implements IMenuPresenter, IPrinterCallBack {

    private Logger logger = Logger.getLogger(MenuPresenter.class);
    private IMenuBiz menuBiz;
    private IMenuView menuView;
    private IPrintRransData printRransData;
    private int printnum = 0;

    public MenuPresenter(IMenuView menuView) {
        this.menuView = menuView;
        this.menuBiz = new MenuBiz();
    }

    @Override
    public void initTopView() {
        Menu menu = menuBiz.getMenu();
        BaseFragmentActivity activity = menuView.getHostActivity();
        if (menu == null) {
            return;
        }
        //标题设置要在此进行，否则回退时标题无法改变
        if (menu.isHasParent()) {
            activity.showBackBtn();
            activity.hideRightButton();
        } else {
            activity.hideBackBtn();
            activity.showRightButton(Settings.bIsSettingBlueTheme() ? null : menuView.getString(R.string.tip_exit));
        }
        /*
        *@author zhouzhihua
        * topbar上面增加一个ImageView，主界面图片显示
        * */
        View titleView = activity.findViewById(R.id.txtvw_title);
        ImageView imageView = null;
        if (Settings.bIsSettingBlueTheme()) {
            imageView = (ImageView) activity.findViewById(R.id.imageView_menu);
        }
        //设置顶栏类型
        activity.setTopViewType(menu.getTopType());

        if (Settings.bIsSettingBlueTheme()
                && !TextUtils.isEmpty(menu.getIconResName())
                && !menu.isHasParent()
                && (ResourceUtils.getDrawableId(activity, menu.getIconResName()) > 0)) {

            titleView.setVisibility((imageView != null) ? View.GONE : View.VISIBLE);
            if (imageView != null) {
                imageView.setVisibility(View.VISIBLE);
            }
        } else {
            if (imageView != null) {
                imageView.setVisibility(View.GONE);
            }
            titleView.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(menu.getTextResName())) {
                int resId = ResourceUtils.getStringId(menuView.getContext(), menu.getTextResName());
                if (resId > 0) {
                    activity.setTitle(resId);
                } else {
                    activity.setTitle(menu.getChnTag());
                }
            } else {
                activity.setTitle(menu.getChnTag());
            }
        }
    }

    @Override
    public void initLocalData(Menu menu) {
        menuBiz.setMenu(menu);
//        EventBus.getDefault().register(this);
    }

    @Override
    public int getLayoutId() {
        if (menuBiz.getMenu() == null || menuBiz.getMenu().getStructure() == Menu.ViewStructure.LIST) {
            return menuView.getListLayoutId();
        }
        return menuView.getGridLayoutId();
    }

    @Override
    public void onMenuItemClicked(View view, MenuItem item) {
        if (CommonUtils.isFastClick())
            return;
        logger.debug("点击：" + item.getChnTag()+" "+item.getTransCode());
        if(payEntry(item.getTransCode())){
            //外调交易，返回
            return;
        }
        if (item instanceof Menu) {
            //跳转到下一级菜单
            boolean success = menuView.jumpToChildMenu((Menu) item);
            if (!success) {
                menuView.toast(R.string.tip_menu_undefined);
            } else
                release();
        } else {
            if (!isDeviceReady()) {
                menuView.toast(R.string.tip_device_not_ready);
                return;
            }
            boolean preResult = onPreProcess(item);
            //特例：如果Item中含有开关，UI需要在这里进行控制，业务控制在onProcess中
            menuView.onToggleIfExists(view);
            if (preResult) {
                onProcess(item);
            }
        }
    }

    public boolean payEntry(String code){
        boolean hasPayEntry = true;
        Bundle data = new Bundle();
        int requestCode = 0;
        if(TransCode.SALE.equals(code)){
            logger.debug("消费 调用支付组件");
            data.putString("trans_code", "T00001");
            requestCode = 1;
            if (!CommonUtils.tradeEnvironmentCheck(getContext())) {
                logger.debug("^_^ 电池电量低 ^_^");
                ViewUtils.showToast(getContext(), "电量低，请充电！");
                return true;
            }
        }else if(TransCode.VOID.equals(code)){
            logger.debug("消费撤销 调用支付组件");
            data.putString("trans_code", "T00002");
            requestCode = 2;
            if (!CommonUtils.tradeEnvironmentCheck(getContext())) {
                logger.debug("^_^ 电池电量低 ^_^");
                ViewUtils.showToast(getContext(), "电量低，请充电！");
                return true;
            }
        }else if(TransCode.BALANCE.equals(code)){
            logger.debug("余额查询 调用支付组件");
            data.putString("trans_code", "T00004");
            requestCode = 3;
        }else if(TransCode.SIGN_IN.equals(code)){
            logger.debug("签到 调用支付组件");
            data.putString("trans_code", "M00001");
            requestCode = 4;
        }else if(TransCode.SETTLEMENT.equals(code)){
            logger.debug("结算 调用支付组件");
            data.putString("trans_code", "M00002");
            requestCode = 5;
        }else if(TransCode.TRADE_QUERY.equals(code)||TransCode.PRINT_ANY.equals(code)){
            logger.debug("交易查询 调用支付组件");
            data.putString("trans_code", "F00001");
            requestCode = 6;
        }else if(TransCode.PRINT_LAST.equals(code)){
            logger.debug("打印末笔 调用支付组件");
            data.putString("trans_code", "F00002");
            requestCode = 7;
        }else if(TransCode.DOWNLOAD_AID.equals(code)){
            logger.debug("下载IC参数 调用支付组件");
            data.putString("trans_code", "M00004");
            requestCode = 8;
        }else if(TransCode.DOWNLOAD_CAPK.equals(code)){
            logger.debug("下载公钥 调用支付组件");
            data.putString("trans_code", "M00003");
            requestCode = 9;
        }else {
            hasPayEntry = false;
        }
        if(hasPayEntry){
            Intent intent = new Intent();
            String packageName = "com.centerm.epos.ebi";
            String activityName = "com.centerm.component.pay.PayEntryActivity";
            ComponentName comp = new ComponentName(packageName, activityName);
            String oprId = BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), BusinessConfig.Key.KEY_OPER_ID);
            data.putString("oprId",oprId);
            data.putInt(KEY_POS_SERIAL, BusinessConfig.getInstance().getNumber(EposApplication.getAppContext(), KEY_POS_SERIAL));
            intent.putExtras(data);
            intent.setComponent(comp);
            try {
                //menuView.getContext().startActivity(intent);
                ((MenuFragment)menuView).getActivity().startActivityForResult(intent, requestCode);
            }catch (Exception e){
                hasPayEntry = true;
                menuView.toast("交易失败，请检测是否安装支付组件");
            }
        }
        return hasPayEntry;
    }

    @Override
    public boolean isDeviceReady() {
        return menuView.getHostActivity().isDeviceReady();
    }

    @Override
    public boolean[] isNeedTmkOrSignIn(String menuTag) {
        return menuView.getHostActivity().isNeedTmkOrSignin(menuTag);
    }

    @Override
    public boolean onPreProcess(MenuItem item) {
        return true;
    }

    @Override
    public void onProcess(MenuItem item) {
        if (item == null) {
            return;
        }
        boolean success;
        if (item.getProcessFile() == null) {
            logger.debug(item.getChnTag() + "==>未定义流程文件");
            success = onNoProcessDefine(item);
        } else {
            success = beginProcess(item.getTransCode(), item.getProcessFile());
        }
        if (!success) {
            menuView.toast(R.string.tip_process_undefined);
        }
    }

    @Override
    public boolean onNoProcessDefine(MenuItem item) {
        //这里要从配置文件中读取事件响应的处理类
        IMenuHelper menuHelper = (IMenuHelper) ConfigureManager.getRedevelopAction(Keys.obj().redevelop_menu_helper,
                IMenuHelper.class);
        if (menuHelper == null)
            return false;
        return menuHelper.onTriggerMenuItem(MenuPresenter.this, item);
    }

    @Override
    public Menu getMenu() {
        return menuBiz.getMenu();
    }

    @Override
    public MenuAbsListAdapter getListAdapter() {
        return new MenuAbsListAdapter(menuView.getContext(), menuBiz.getMenu());
    }

    @Override
    public GridViewPager.GridPagerAdapter getGridAdapter() {
        return new GridViewPager.GridPagerAdapter(menuView.getContext(), R.layout
                .common_menu_grid_item, menuBiz.getGridAdapterFrom(),
                menuBiz.getGridAdapterTo(), menuBiz.getGridAdapterData(menuView.getContext()));
    }

    @Override
    public void jumpToActivity(Class<? extends Activity> clz) {
        menuView.jumpToActivity(clz);
    }

    @Override
    public void jumpToActivity(Class<? extends Activity> clz, Bundle parameters) {
        if (parameters == null)
            jumpToActivity(clz);
        else
            menuView.jumpToActivity(clz, parameters);
    }

    @Override
    public void jumpToActivity(Class<? extends Activity> clz, Map<String, String> parameterMap) {
        if (parameterMap == null || parameterMap.size() == 0)
            jumpToActivity(clz);
        else
            menuView.jumpToActivity(clz, parameterMap);
    }


    @Override
    public void beginOnlineProcess(String menuTag) {
        ConfigureManager config = ConfigureManager.getInstance(menuView.getContext());
        TradeProcess process = config.getTradeProcess(menuView.getContext(), "online");
        if (process == null) {
            logger.warn("通用联机流程未定义！");
            return;
        }
        menuView.jumpToTrade(menuTag, process);
    }

    @Override
    public void beginOnlineProcess(String tranCode, String process) {
        ConfigureManager config = ConfigureManager.getInstance(menuView.getContext());
        TradeProcess tradeProcess = config.getTradeProcess(menuView.getContext(), process);
        if (tradeProcess == null) {
            logger.warn(tradeProcess + "流程未定义！");
            return;
        }
        menuView.jumpToTrade(tranCode, tradeProcess);
    }

    @Override
    public void setBizFlag(String key, boolean flag) {
        BusinessConfig config = BusinessConfig.getInstance();
        config.setFlag(menuView.getContext(), key, flag);
    }

    @Override
    public boolean getBizFlag(String key) {
        BusinessConfig config = BusinessConfig.getInstance();
        return config.getFlag(menuView.getContext(), key);
    }

    @Override
    public void doClearTradeRecords() {
        final StatusListener<Boolean> listener = new StatusListener<Boolean>() {
            @Override
            public void onFinish(Boolean[] result) {
                menuView.hideLoading();
                if (result[0]) {
                    if (result[1]) {
                        menuView.toast(R.string.tip_clear_data_over);
                    } else {
                        menuView.toast(R.string.tip_clear_data_error);
                    }
                } else {
                    menuView.toast("当前无交易流水");
                }
            }
        };
        menuView.showSelectDialog(R.string.tip_notification, R.string.tip_clear_trans_data,
                new AlertDialog.ButtonClickListener() {
                    @Override
                    public void onClick(AlertDialog.ButtonType button, View v) {
                        if (button == AlertDialog.ButtonType.POSITIVE) {
                            //清空签名数据
                            clearSignFile();
                            BusinessConfig.getInstance().setFlag(getContext(), BusinessConfig.Key.FLAG_ESIGN_STORAGE_WARNING,false);

                            menuView.showLoading("正在交易信息，请稍候");
                            menuBiz.clearTradeRecords(DbHelper.getInstance(), listener);
                            DbHelper.releaseInstance();
                        }
                    }
                });
    }

    private void clearSignFile(){
        //删除签名文件文件
        File fileVoucher = new File(Config.Path.SIGN_PATH);
        if(fileVoucher.isDirectory()&&fileVoucher.exists()){
            File[] subFile = fileVoucher.listFiles();
            for(int i=0;i<subFile.length;i++){
                File deleteFile = subFile[i];
                if(deleteFile.exists()){
                    deleteFile.delete();
                }
            }
        }
    }

    @Override
    public void doClearReverseRecords() {
        menuView.showSelectDialog(R.string.tip_notification, R.string.tip_clear_reverse_data,
                new AlertDialog.ButtonClickListener() {
                    @Override
                    public void onClick(AlertDialog.ButtonType button, View v) {
                        if (button == AlertDialog.ButtonType.POSITIVE) {
                            CommonDao<ReverseInfo> reverseDao = new CommonDao<>(ReverseInfo.class, DbHelper.getInstance());
                            List<ReverseInfo> reverseList = reverseDao.query();
                            if(reverseList==null||reverseList.size()==0){
                                menuView.toast("当前无冲正信息");
                            }else {
                                menuView.showLoading("正在清除冲正信息，请稍候");
                                boolean flag = reverseDao.delete(reverseList);
                                if (flag) {
                                    menuView.toast(R.string.tip_clear_reverse_over);
                                } else {
                                    menuView.toast(R.string.tip_clear_reverse_error);
                                }
                                menuView.hideLoading();
                                DbHelper.releaseInstance();
                            }
                        }
                    }
                });
    }

    private boolean beginProcess(String transCode, String processFile) {
        ConfigureManager config = menuView.getHostActivity().getConfigureManager();
        TradeProcess process = config.getTradeProcess(menuView.getContext(), processFile);
        if (process != null) {
            menuView.jumpToTrade(transCode, process);
            return true;
        } else {
            return false;
        }

       /* String processPath = config.getTradeProcessPath();
        if (!TextUtils.isEmpty(processFile)) {
            process = XmlParser.parseProcess(menuView.getContext(), processPath + "/" + processFile);
        }
        if (process == null) {
            process = config.getTradeProcess(menuView.getContext(), transCode);
        }
        if (process != null) {
            if (transCode.equals(OBTAIN_TMK)) {
                final TradeProcess tradeProcess = process;
                menuView.showSelectDialog(null, "开始下载主密钥", new AlertDialog.ButtonClickListener() {
                    @Override
                    public void onClick(AlertDialog.ButtonType button, View v) {
                        switch (button) {
                            case POSITIVE:
                                menuView.jumpToTrade(transCode, tradeProcess);
                                break;
                        }
                    }
                });
            } else {
                menuView.jumpToTrade(transCode, process);
            }
            return true;
        }
        return false;*/
    }

    @Override
    public IMenuBiz getMenuBiz() {
        return menuBiz;
    }

    @Override
    public Context getContext() {
        return menuView.getContext();
    }

    @Override
    public void release() {
        logger.debug("^_^ EventBus Unregister in " + this.getClass().getSimpleName() + " ^_^");
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void restoreAppConfig() {
        logger.debug("^_^ begin restore app config ^_^");
        ICommonManager commonManager = (ICommonManager) ConfigureManager.getInstance(getContext())
                .getSubPrjClassInstance
                        (new CommonManager());
        try {
            if (commonManager.getBatchCount() > 0) {
                ViewUtils.showToast(getContext(), "请先完成批结算!");
                return;
            }
        } catch (SQLException e) {
            //查询交易流程失败
            logger.error("^_^" + e.getMessage() + "^_^");
            ViewUtils.showToast(getContext(), "查询交易记录异常，请先清空交易记录!");
            return;
        }
        DialogFactory.showMessageDialog(menuView.getContext(), "恢复默认设置", "此操作将清空应用设置！", new AlertDialog
                .ButtonClickListener() {
            @Override
            public void onClick(AlertDialog.ButtonType button, View v) {
                if (button == AlertDialog.ButtonType.POSITIVE) {
                    BusinessConfig.clearConfig();
                    Settings.clearSetting(EposApplication.getAppContext());
                    //导入签购单模板
                    PrintManager printManager = new PrintManager(EposApplication.getAppContext());
                    printManager.importTemplate();
                    ViewUtils.showToast(menuView.getContext(), "成功恢复默认设置！");
                    logger.debug("^_^ end restore app config ^_^");
                } else {
                    logger.debug("^_^ cancel restore app config ^_^");
                }
            }
        }, 30);
    }

    View diglogSingleSelectItem(int index, final Dialog dialog) {
        View view;
        final int indexValue = index;

        LayoutInflater layoutInflater = (LayoutInflater) menuView.getContext().getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);
        view = layoutInflater.inflate(R.layout.dialog_list, null);

        final ListView select_dialog_listview = (ListView) view.findViewById(R.id.select_dialog_list_view);
        select_dialog_listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        select_dialog_listview.setAdapter(new MyBaseAdapter(menuView, EncryptAlgorithmEnum.names(), index));

        select_dialog_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((RadioButton) view.findViewById(R.id.dialog_single_radio_button)).setChecked(true);
                for (int i = 0; i < select_dialog_listview.getChildCount(); i++) {
                    if (i != position) {
                        ((RadioButton) select_dialog_listview.getChildAt(i).findViewById(R.id
                                .dialog_single_radio_button)).setChecked(false);
                    } else {
                        if (indexValue != position) {
                            Settings.setEncryptAlgorithm(menuView.getContext(), EncryptAlgorithmEnum.index2Name
                                    (position));
                            ViewUtils.showToast(menuView.getContext(), "设置成功！");
                        }
                    }
                }
                if (dialog != null) {
                    dialog.cancel();
                }
            }
        });
        return view;
    }

    static class MyBaseAdapter extends BaseAdapter {
        private int iCount, indexKey;
        IMenuView iMenuView;

        LayoutInflater mInflater;
        String[] s;

        class ViewHolder {
            RadioButton radioButton;
            TextView displayInfo;
        }

        MyBaseAdapter(IMenuView iMenuView, String[] s, int index) {
            this.iCount = s.length;
            mInflater = (LayoutInflater) iMenuView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.s = s;
            indexKey = index;
            this.iMenuView = iMenuView;
        }

        @Override
        public int getCount() {
            return iCount;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.dialog_list_items, null);
                holder.radioButton = (RadioButton) convertView.findViewById(R.id.dialog_single_radio_button);
                holder.displayInfo = (TextView) convertView.findViewById(R.id.dialog_single_textview);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.radioButton.setChecked(indexKey == position);
            holder.displayInfo.setText(s[position]);
            return convertView;
        }
    }

    @Override
    public void doLocalFunction(int functionID) {
        final int initIndex;
        switch (functionID) {
            case LocalFunctionTags.QPS_PARAM_CONFIG:
                menuView.jumpToActivity(BaseQPSSettingActivity.class);
                break;
            case LocalFunctionTags.ENCRYPT_ALGORITHM_CONFIG:
                initIndex = EncryptAlgorithmEnum.name2Index(Settings.getEncryptAlgorithm(menuView
                        .getContext()));
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(menuView.getContext());
                if (!Settings.bIsSettingBlueTheme()) {

                    builder.setTitle("加密算法").setSingleChoiceItems(EncryptAlgorithmEnum.names(), initIndex, new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (initIndex != which) {
                                        Settings.setEncryptAlgorithm(menuView.getContext(), EncryptAlgorithmEnum
                                                .index2Name(which));
                                        ViewUtils.showToast(menuView.getContext(), "设置成功！");
                                        dialog.dismiss();
                                    }
                                }
                            }).create().show();
                } else {
                    Dialog dialog = new Dialog(menuView.getContext());
                    dialog.setContentView(diglogSingleSelectItem(initIndex, dialog), new ViewGroup.LayoutParams(
                            menuView.getContext().getResources().getDimensionPixelSize(R.dimen.width_540),
                            menuView.getContext().getResources().getDimensionPixelSize(R.dimen.height_300)));
                    dialog.setTitle("加密算法");
                    dialog.show();
                }
                break;
            case LocalFunctionTags.NFC_TRADE_CHANNEL_CONFIG:
                initIndex = NFCTradeChannel.name2Index(BusinessConfig.getInstance().getValue(menuView.getContext
                        (), BusinessConfig.Key.NFC_TRADE_CHANNEL));
                android.app.AlertDialog.Builder diaBuilder = new android.app.AlertDialog.Builder(menuView.getContext());
                diaBuilder.setTitle("非接交易通道").setSingleChoiceItems(NFCTradeChannel.names(), initIndex, new
                        DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (initIndex != which) {
                                    BusinessConfig.getInstance().setValue(menuView.getContext(), BusinessConfig.Key
                                            .NFC_TRADE_CHANNEL, NFCTradeChannel.index2Name(which));
                                    ViewUtils.showToast(menuView.getContext(), "设置成功！");
                                    dialog.dismiss();
                                }
                            }
                        }).create().show();
                break;
            case LocalFunctionTags.IMPORT_TERMINAL_PARAMETER:
                new AsyncImportParameterTask(menuView.getContext()).execute();
                break;
            case LocalFunctionTags.MAIN_KEY_INDEX_CONFIG:
                final int oldIndex = BusinessConfig.getInstance().getNumber(EposApplication.getAppContext(),
                        BusinessConfig.Key.MAINKEYINDEX);
                final InputNumberDialog numberDialog = new InputNumberDialog(getContext(), "请输入主密钥索引", "" + oldIndex);
                numberDialog.setClickListener(new InputNumberDialog.ButtonClickListener() {
                    @Override
                    public void onClick(InputNumberDialog.ButtonType button, View v) {
                        if (InputNumberDialog.ButtonType.POSITIVE == button) {
                            String indexStr = numberDialog.getInputText();
                            if (TextUtils.isEmpty(indexStr)) {
                                Toast.makeText(getContext(), "请输入主密钥索引", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            long index = Long.parseLong(indexStr);
                            if (index >= 10) {
                                Toast.makeText(getContext(), "主密钥索引超出限制", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (index != oldIndex) {
                                BusinessConfig.getInstance().setNumber(EposApplication.getAppContext(),
                                        BusinessConfig.Key.MAINKEYINDEX, (int) index);
                                //初始化密钥索引
                                CommonUtils.getPinPadDev();
                            }
                            Toast.makeText(getContext(), "主密钥索引设置成功", Toast.LENGTH_SHORT).show();
                            numberDialog.dismiss();
                        }
                    }
                });
                numberDialog.show();
                break;
        }
    }

    @Override
    public boolean beginAutoSign() {
        String operId = BusinessConfig.getInstance().getValue(getContext(), BusinessConfig.Key.KEY_OPER_ID);
        if ("00".equals(operId) || "99".equals(operId)) {
            return false;
        }
        if (Settings.getVersionUpdateInfo(getContext()) != null) {
            //如果当前有新版本，先进行提示，不进行自动签到
            ViewUtils.showToast(menuView.getContext(), "有新版本！");
            return false;
        }
        if (!NetUtils.isNetConnected(getContext())) {
            ViewUtils.showToast(menuView.getContext(), "网络未连接！");
            return false;
        }
        if (!StringUtils.isStrNull(operId)) {
            //普通操作员，首次进入到主界面必须签到（如POS在使用过程中掉电，重新开机后操作员需要重新签到）
            final boolean isSignIn = BusinessConfig.getInstance().getFlag(menuView.getContext(), BusinessConfig.Key
                    .FLAG_SIGN_IN);
            if (!isSignIn)
                beginOnlineProcess(TransCode.SIGN_IN);
            return true;
        }
        return false;
    }

    public boolean beginDownloadParam(String transCode) {
        String operId = BusinessConfig.getInstance().getValue(getContext(), BusinessConfig.Key.KEY_OPER_ID);
        if ("00".equals(operId) || "99".equals(operId)) {
            return false;
        }
        if (Settings.getVersionUpdateInfo(getContext()) != null) {
            //如果当前有新版本，先进行提示，不进行自动签到
            ViewUtils.showToast(menuView.getContext(), "有新版本！");
            return false;
        }
        if (!NetUtils.isNetConnected(getContext())) {
            ViewUtils.showToast(menuView.getContext(), "网络未连接！");
            return false;
        }
        if (!StringUtils.isStrNull(operId)) {
            beginOnlineProcess(transCode);
            return true;
        }
        return false;
    }

    public void initOnResume() {
        logger.debug("^_^ EventBus Register in " + this.getClass().getSimpleName() + " ^_^");
        EventBus.getDefault().register(this);
    }

    protected void setMenuBiz(IMenuBiz menuBiz) {
        this.menuBiz = menuBiz;
    }

    @Override
    public IMenuView getMenuView() {
        return menuView;
    }

    public void setMenuView(IMenuView menuView) {
        this.menuView = menuView;
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleCommonMessage(SimpleMessageEvent event) {
        logger.debug("^_^ EVENT code:" + event.getCode() + " message:" + event.getMessage() + " ^_^");
        switch (event.getCode()) {
            case MessageCode.SHOW_LOGIN_VIEW:
                BaseFragmentActivity activity = menuView.getHostActivity();
                FragmentManager fragmentManager = activity.getFragmentManager();
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                activity.loadFragmentView(new LoginFragment());
                break;
        }
    }

    private PrintManager printManager;
    private IPrintSlipHelper printSlipHelper;
    private Map<String, String> slipItemContent;
    List<List<String>> lists;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleMessage(PrinteEvent event) {
        logger.debug("^_^ EVENT code:" + event.getWhat() + " message:" + event.getMsg() + " ^_^");
        switch (event.getWhat()) {
            case TradeMessage.PRINT_SLIP_LAST:
                final PrinterProxy printerProxy;
                ICommonManager commonManager = (ICommonManager) ConfigureManager.getInstance(getContext())
                        .getSubPrjClassInstance(new CommonManager());
                List<TradeInfoRecord> tradeInfos = null;
                try {
                    tradeInfos = commonManager.getLastTransItem();
                } catch (SQLException e) {
                    e.printStackTrace();
                    return;
                }
                if (null != tradeInfos && tradeInfos.size() > 0) {
                    if (printManager == null)
                        printManager = new PrintManager(getContext());
                    if (printSlipHelper == null)
                        printSlipHelper = (IPrintSlipHelper) ConfigureManager.getSubPrjClassInstance(new
                                BasePrintSlipHelper());

                    if (1 == BusinessConfig.getInstance().getNumber(getContext(), Keys.obj().printnum)) {
                        printSlipHelper.setPrintComplete(true);
                    }
                    printnum++;
                    TradeInfoRecord info = tradeInfos.get(0);
                    String tranCode = info.getTransType();

                    int printState = checkPrinterState();
                    if (printState < 0) {
                        if (printState == -2) {
                            DialogFactory.showSelectDialog(getContext(), "错误", getContext().getString(R.string
                                            .no_paper_tips),
                                    new AlertDialog.ButtonClickListener() {

                                        @Override
                                        public void onClick(AlertDialog.ButtonType button, View v) {
                                            if (AlertDialog.ButtonType.POSITIVE == button)
                                                EventBus.getDefault().post(new PrinteEvent(TradeMessage
                                                        .PRINT_SLIP_LAST));
                                        }
                                    });
                        } else
                            EventBus.getDefault().post(new PrinteEvent(TradeMessage.PRINT_ERROR, "状态异常"));
                        break;
                    }

                    DialogFactory.showLoadingDialog(getContext(), getContext().getString(R.string.tip_printing));
                    if (tranCode.contains("SCAN"))
                        printerProxy = printManager.prepare(event.getSlipOwner(), "saleScanSlip");
                    else
                        printerProxy = actionWithChildProj(tranCode, printManager, event.getSlipOwner());
                    Map<String, String> tradeData = info.convert2Map();
                    if(!TextUtils.isEmpty(info.getUnicom_scna_type())){
                        tradeData.put("pay_type", info.getUnicom_scna_type());
                    }
                    slipItemContent = printSlipHelper.trade2PrintData(tradeData);
                    printerProxy.setTransCode(tranCode)
                            .setValue(slipItemContent)
                            .setICTrade(PRINT_IC_INFO.contains(tranCode))
                            .setReprint(true)
                            .addInterpolator(printSlipHelper)
                            .print();
                } else {
                    ViewUtils.showToast(getContext(), getContext().getString(com.centerm.epos.R.string
                            .tip_no_trade_info));
                }
                break;
            case TradeMessage.PRINT_NEXT_CONFIRM:
                DialogFactory.showPrintDialog(getContext(), new AlertDialog.ButtonClickListener() {
                    @Override
                    public void onClick(AlertDialog.ButtonType button, View v) {
                        if (AlertDialog.ButtonType.NEGATIVE == button) {
                            printSlipHelper.setPrintComplete(true);
                            EventBus.getDefault().post(new PrinteEvent(TradeMessage.PRINT_SLIP_COMPLETE));
                        } else {
                            printnum++;
                            if (printnum >= BusinessConfig.getInstance().getNumber(getContext(), Keys.obj().printnum)) {
                                printSlipHelper.setPrintComplete(true);
                            }
                            EventBus.getDefault().post(new PrinteEvent(TradeMessage.PRINT_SLIP_LAST, 0, null,
                                    PrintManager.SlipOwner.CONSUMER));
                        }
                    }
                });
                break;
            case TradeMessage.PRINT_TRADE_DETAIL:
                new AsyncQueryPrintDataTask(getContext()) {
                    @Override
                    public void onStart() {
                        super.onStart();
                        DialogFactory.showLoadingDialog(getContext(), getContext().getString(R.string
                                .tip_query_flow));
                    }

                    @Override
                    public void onFinish(List<List<TradeInfoRecord>> lists) {
                        super.onFinish(lists);
                        List<TradeInfoRecord> infoDetail = lists.get(2);
                        if (null != infoDetail && infoDetail.size() > 0) {
                            printRransData = getPrint();
                            if (null == printRransData) {
                                ViewUtils.showToast(getContext(), getContext().getString(R.string
                                        .error_tip_no_print_fun));
                                DialogFactory.hideAll();
                                return;
                            }
                            printRransData.open(getContext());
                            printRransData.printDetails(infoDetail);
                        } else {
                            ViewUtils.showToast(getContext(), getContext().getString(R.string
                                    .tip_no_trade_info));

                            DialogFactory.hideAll();
                        }
                    }
                }.execute();
                break;
            case TradeMessage.PRINT_TRADE_SUMMARY:
                try {
                    String s = Settings.getValue(getContext(), Settings.KEY.PREV_BATCH_TOTAL, "");
                    if (!StringUtils.isStrNull(s)) {
                        printRransData = getPrint();
                        if (null == printRransData) {
                            ViewUtils.showToast(getContext(), getContext().getString(R.string.error_tip_no_print_fun));
                            DialogFactory.hideAll();
                            return;
                        }
                        printRransData.open(getContext());
                        printRransData.setBatchListener(this);
                        printRransData.printBatchTotalData(s, true);
                    } else {
                        ViewUtils.showToast(getContext(), getContext().getString(com.centerm.epos.R.string
                                .tip_no_pre_batch_total));
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case TradeMessage.PRINT_SLIP_COMPLETE:
//                gotoNextStep();
                DialogFactory.hideAll();
                printSlipHelper.setPrintComplete(false);
                break;
            case TradeMessage.PRINT_ERROR:
                DialogFactory.hideAll();
                printSlipHelper.setPrintComplete(false);
                ViewUtils.showToast(getContext(), "打印错误：" + event.getMsg());
                break;
        }
    }

    /**
     * 检测子项目是否有实现
     */
    private PrinterProxy actionWithChildProj(String tranCode, PrintManager printManager, PrintManager.SlipOwner owner) {
        IRedevelopAction iPrintSlipDefine = ConfigureManager.getRedevelopAction(Keys.obj().redevelop_print_slip);
        if (iPrintSlipDefine != null) {
            return (PrinterProxy) iPrintSlipDefine.doAction(tranCode, printManager, owner);
        } else {
            return printManager.prepare(owner);
        }
    }

    @Override
    public void onPrinterFirstSuccess() {

    }

    @Override
    public void onPrinterSecondSuccess() {

    }

    @Override
    public void onPrinterThreeSuccess() {

    }

    @Override
    public void onPrinterFirstFail(int errorCode, String errorMsg) {
        if (errorCode == ErrorCode.PRINTER_ERROR.ERR_NO_PAPER)
            errorMsg = "打印机缺纸，请放入打印纸";
        final int eCode = errorCode;
        final String eMsg = errorMsg;
        DialogFactory.showSelectPirntDialog(getContext(),
                "提示",
                errorMsg,
                new com.centerm.epos.view.AlertDialog.ButtonClickListener() {
                    @Override
                    public void onClick(com.centerm.epos.view.AlertDialog.ButtonType button, View
                            v) {
                        switch (button) {
                            case POSITIVE:
                                String s = Settings.getValue(getContext(), Settings.KEY.PREV_BATCH_TOTAL, "");
                                if (!StringUtils.isStrNull(s)) {
                                    try {
                                        printRransData.printBatchTotalData(s, true);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                break;
                            case NEGATIVE:
                                DialogFactory.hideAll();
                                break;
                        }
                    }
                });
    }

    @Override
    public void onPrinterSecondFail(int errorCode, String errorMsg) {

    }

    @Override
    public void onPrinterThreeFail(int errorCode, String errorMsg) {

    }

    /**
     * 检测打印机状态，如果是缺纸，则提示装纸，其它错误则退出打印。
     *
     * @return 小于0则表示失败退出，-2表示缺纸，0表示状态正常
     */
    private int checkPrinterState() {
        EnumPrinterStatus status;
        try {
            IPrinterDev printer = DeviceFactory.getInstance().getPrinterDev();
            status = printer.getPrinterStatus();
            if (EnumPrinterStatus.OK == status)
                return 0;
            if (EnumPrinterStatus.NO_PAPER == status) {
                return -2;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean isManagerTrade(String transCode) {
        if (TextUtils.isEmpty(transCode))
            return false;
        if (TransCode.SIGN_IN.equals(transCode) || TransCode.SIGN_OUT.equals(transCode) ||
                TransCode.DOWNLOAD_TERMINAL_PARAMETER.equals(transCode) || TransCode.DOWNLOAD_CAPK.equals(transCode) ||
                TransCode.DOWNLOAD_AID.equals(transCode) || TransCode.DOWNLOAD_BLACK_CARD_BIN_QPS.equals(transCode) ||
                TransCode.DOWNLOAD_CARD_BIN.equals(transCode) || TransCode.DOWNLOAD_CARD_BIN_QPS.equals(transCode) ||
                TransCode.DOWNLOAD_PARAMS.equals(transCode) || TransCode.DOWNLOAD_PARAMS_FINISHED.equals(transCode) ||
                TransCode.DOWNLOAD_QPS_PARAMS.equals(transCode) || TransCode.REVERSE.equals(transCode) ||
                TransCode.SETTLEMENT.equals(transCode) || TransCode.SETTLEMENT_DONE.equals(transCode) ||
                TransCode.UPLOAD_SCRIPT_RESULT.equals(transCode) || TransCode.POS_STATUS_UPLOAD.equals(transCode)) {
            return true;
        }
        return false;
    }

    /*获取打印类的接口*/
    public IPrintRransData getPrint() {
        ConfigureManager config = ConfigureManager.getInstance(menuView.getHostActivity());
        RedevelopItem redevelop = config.getRedevelopItem(menuView.getHostActivity(), Keys.obj().redevelop_print_data);
        String clzName = redevelop.getClassName();
        try {
            Class clz = Class.forName(clzName);
            Object obj = clz.newInstance();
            if (obj instanceof IPrintRransData) {
                return (IPrintRransData) obj;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
