package com.centerm.epos.base;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.centerm.cloudsys.sdk.common.utils.NetUtils;
import com.centerm.epos.EposApplication;
import com.centerm.epos.R;
import com.centerm.epos.activity.MainActivity;
import com.centerm.epos.bean.GtBannerBean;
import com.centerm.epos.bean.GtBean;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.fragment.LoginFragment;
import com.centerm.epos.function.AppUpgradeForLiandiShopUtil;
import com.centerm.epos.model.BaseTradeParameter;
import com.centerm.epos.model.ITradeParameter;
import com.centerm.epos.mvp.presenter.GTMenuPresenter;
import com.centerm.epos.mvp.view.IMenuView;
import com.centerm.epos.transcation.pos.constant.JsonKeyGT;
import com.centerm.epos.transcation.pos.controller.AutoSignInController;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.OkHttpUtils;
import com.centerm.epos.utils.OnCallListener;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.view.AlertDialog;
import com.centerm.epos.view.TipDialog;
import com.centerm.epos.xml.bean.menu.Menu;
import com.centerm.epos.xml.bean.process.TradeProcess;
import com.centerm.epos.xml.keys.Keys;
import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;
import config.Config;

import static com.centerm.epos.base.BaseActivity.KEY_AUTO_SIGN;

/**
 * 绿城菜单首页
 * author:liubit</br>
 * date:2019/8/30</br>
 */
public class GTMenuFragment extends BaseFragment implements IMenuView {

    private GTMenuPresenter presenter;
    private boolean isAutoSign = false;
    private SliderLayout sliderLayout;
    private PagerIndicator indicator;
    private Button mBtnIdCard,mBtnOther;
    private TextView mHotLine;
    private boolean hasBanner = false;

    @Override
    protected void onInitLocalData(Bundle savedInstanceState) {
        setPresenter(new GTMenuPresenter(this));
        /*
         * 菜单界面禁止状态栏下拉，zhouzhihua modif
         */
        //CommonUtils.disableStatusBar(getHostActivity());
    }

    @Override
    protected int onLayoutId() {
        return presenter.getLayoutId();
    }

    @Override
    protected void afterInitView() {
        super.afterInitView();

        if (!Settings.isAppInit(getActivity())){
            return;
        }
        boolean isLock = BusinessConfig.getInstance().getFlag(getContext(), BusinessConfig.Key.KEY_IS_LOCK);
        if (isLock) {
            DialogFactory.showLockDialog(getContext());
        }

        if (!CommonUtils.tradeEnvironmentCheck(getContext())) {
            logger.debug("^_^ 电池电量低，同步机具状态 ^_^");
            syncPosSts("1");
        }else if(0!=CommonUtils.checkPrinterState()){
            logger.debug("^_^ 打印机异常，同步机具状态 ^_^");
            syncPosSts("1");
            if(CommonUtils.checkPrinterState() == -101) {
                logger.error("打印异常，退出程序，请做末笔打印");
                android.os.Process.killProcess(android.os.Process.myPid());    //获取PID
                System.exit(0);
            }
        }else {
            syncPosSts("0");
        }

    }

    @Override
    protected void onInitView(View view) {
        presenter.initTopView();

        mHotLine = (TextView) view.findViewById(R.id.mHotLine);
        String hotLine = BusinessConfig.getInstance().getValue(EposApplication.getAppContext(),
                BusinessConfig.Key.HOTLINE_KEY);
        if(!TextUtils.isEmpty(hotLine)){
            mHotLine.setText(hotLine);
        }
        mHotLine.setOnClickListener(this);
        mBtnIdCard = (Button) view.findViewById(R.id.mBtnIdCard);
        mBtnOther = (Button) view.findViewById(R.id.mBtnOther);
        mBtnIdCard.setOnClickListener(this);
        mBtnOther.setOnClickListener(this);
        view.findViewById(R.id.mBtnExit).setOnClickListener(this);

        sliderLayout = (SliderLayout) view.findViewById(R.id.slider_layout);
        indicator = (PagerIndicator) view.findViewById(R.id.page_indicator);

        sliderLayout.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        sliderLayout.setDuration(Config.AD_SCROLL_TIME);
        if (indicator != null) {
            sliderLayout.setCustomIndicator(indicator);
        }
        sliderLayout.stopAutoCycle();

    }

    @Override
    public void onClick(View v) {
        if (CommonUtils.isFastClick()) {
            return;
        }
        if (v.getId() == R.id.mBtnIdCard) {
            AppUpgradeForLiandiShopUtil.getInstance().checkNewVersion(getContext(),new AppUpgradeForLiandiShopUtil.CheckVersion() {
                @Override
                public void hasNoNewVersion() {

                    if (Looper.myLooper() != Looper.getMainLooper()) {
                        // If we finish marking off of the main thread, we need to
                        // actually do it on the main thread to ensure correct ordering.
                        Handler mainThread = new Handler(Looper.getMainLooper());
                        mainThread.post(new Runnable() {
                            @Override
                            public void run() {
                                presenter.beginOnlineProcess(TransCode.SALE, "gt_sale.xml");
                            }
                        });
                        return;
                    }

                }
            });

        }else if(v.getId() == R.id.mBtnOther){
            if (checkTradeState(TransCode.SALE)){
                return;
            }
            AppUpgradeForLiandiShopUtil.getInstance().checkNewVersion(getContext(),new AppUpgradeForLiandiShopUtil.CheckVersion() {
                @Override
                public void hasNoNewVersion() {
                    if (Looper.myLooper() != Looper.getMainLooper()) {
                        // If we finish marking off of the main thread, we need to
                        // actually do it on the main thread to ensure correct ordering.
                        Handler mainThread = new Handler(Looper.getMainLooper());
                        mainThread.post(new Runnable() {
                            @Override
                            public void run() {
                                artificialAuth();
                            }
                        });
                        return;
                    }

                }
            });

        }else if(v.getId() == R.id.mBtnExit){
            tipToExit();
        }else if(v.getId() == R.id.mHotLine){

        }else {
            super.onClick(v);
        }
    }





    private void setDefaultBanner(){
        if(getActivity()==null){
            return;
        }
        DefaultSliderView sliderView = new DefaultSliderView(getActivity());
        sliderView.image(R.drawable.banner_pic).error(R.drawable.banner_error).empty(R.drawable.banner_loading);
        if(getContext()!=null&&sliderLayout!=null) {
            sliderLayout.addSlider(sliderView);
        }
    }

    private void picQuery(){
        Map<String,Object> dataMap = new HashMap<>();
        dataMap.put(JsonKeyGT.projectId, BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), BusinessConfig.Key.PROJECT_ID));
        sendData(false, TransCode.picQuery, dataMap, new OnCallListener() {
            @Override
            public void onCall(Map<String, Object> resultMap) {
                hasBanner = true;
                if(resultMap!=null){
                    GtBannerBean bean = (GtBannerBean) resultMap.get(JsonKeyGT.returnData);
                    if("0".equals(bean.getCode())){
                        refreshAdView(bean.getData());
                    }else {
                        setDefaultBanner();
                    }
                }else {
                    setDefaultBanner();
                }
            }
        });
    }

    private void refreshAdView(List<String> urls) {
        sliderLayout.setVisibility(View.VISIBLE);
        sliderLayout.removeAllSliders();

        if(urls!=null&&urls.size()>0){
            for(int i=0;i<urls.size();i++){
                String url = urls.get(i);
                String[] strs = url.split("/");
                String picName = strs[strs.length - 1];
                downloadPic(picName, url);
            }
        }else {
            setDefaultBanner();
        }

    }

    private void refreshAdView2(List<String> urls) {
        sliderLayout.setVisibility(View.VISIBLE);
        sliderLayout.removeAllSliders();

        if(urls!=null&&urls.size()>0){
            for(int i=0;i<urls.size();i++){
                if(i<5) {
                    DefaultSliderView sliderView = new DefaultSliderView(getActivity());
                    sliderView.image(urls.get(i)).error(R.drawable.banner_error).empty(R.drawable.banner_loading);
                    sliderLayout.addSlider(sliderView);
                }
            }
        }else {
            setDefaultBanner();
        }

    }

    private void downloadPic(final String fileName, final String url){
        final File file = new File(Config.Path.DOWNLOAD_PATH, fileName);
        if(file.exists()){
            logger.info("文件已存在，直接显示:"+file.getName());
            loadPic(file);
            return;
        }
        OkHttpUtils.getInstance().downloadFile(file, url, new OnCallListener() {
            @Override
            public void onCall(Map<String, Object> result) {
                try {
                    if(result!=null){
                        logger.info(fileName+" -> 下载完成 -> "+result.get("path"));
                        File downloadFile = new File((String) result.get("path"));
                        loadPic(downloadFile);
                    }else {
                        logger.info(fileName+" -> 下载失败 -> "+file);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    popToast("通讯异常");
                }

            }
        });
    }

    private void loadPic(File file){
        if(getContext()!=null&&sliderLayout!=null&&getActivity()!=null) {
            DefaultSliderView sliderView = new DefaultSliderView(getActivity());
            sliderView.image(file).error(R.drawable.banner_error).empty(R.drawable.banner_loading);
            sliderLayout.addSlider(sliderView);
        }
    }


    private void syncPosSts(String sta){
        Map<String,Object> dataMap = new HashMap<>();
        dataMap.put(JsonKeyGT.termSn, CommonUtils.getSn());
        dataMap.put(JsonKeyGT.termSts, sta);
        sendData(false, TransCode.syncPosSts, dataMap, new OnCallListener() {
            @Override
            public void onCall(Map<String, Object> resultMap) {

            }
        });
    }

    private void artificialAuth(){
        Map<String,Object> dataMap = new HashMap<>();
        dataMap.put(JsonKeyGT.projectId, BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), BusinessConfig.Key.PROJECT_ID));
        dataMap.put(JsonKeyGT.termSn, CommonUtils.getSn());
        sendData(true, TransCode.isAuthorization, dataMap, new OnCallListener() {
            @Override
            public void onCall(Map<String, Object> resultMap) {
                if(resultMap!=null){
                    GtBean bean = (GtBean) resultMap.get(JsonKeyGT.returnData);
                    if("0".equals(bean.getCode())){//需授权
                        showAuthDialog();
                    }else if("12".equals(bean.getCode())){//不需要授权
                        presenter.beginOnlineProcess(TransCode.SALE, "gt_sale_other_no_auth.xml");
                    }else {
                        popToast(bean.getMsg());
                    }
                }else {
                    popToast("通讯异常，请重试");
                }
            }
        });
    }

    private void showAuthDialog(){
        DialogFactory.showTipDialog(getActivity(), "温馨提示", "请联系工作人员进行授权", new TipDialog.ButtonClickListener() {
            @Override
            public void onClick(TipDialog.ButtonType button, View v) {
                presenter.beginOnlineProcess(TransCode.SALE, "gt_sale_other.xml");
            }
        },false);
    }

    private void tipToExit() {
        DialogFactory.showSelectDialog(getActivity(), getString(R.string.tip_notification), getString(R.string.tip_confirm_exit), new AlertDialog
                .ButtonClickListener() {
            @Override
            public void onClick(AlertDialog.ButtonType button, View v) {
                switch (button) {
                    case POSITIVE:
                        BusinessConfig config = BusinessConfig.getInstance();
                        String current = config.getValue(getActivity(), BusinessConfig.Key.KEY_OPER_ID);
                        config.setValue(getActivity(), BusinessConfig.Key.KEY_LAST_OPER_ID, current);
                        config.setValue(getActivity(), BusinessConfig.Key.KEY_OPER_ID, null);
                        getHostActivity().replace(new LoginFragment()).commit();
                        break;
                }
            }
        });
    }

    @Override
    public void onToggleIfExists(View view) {

    }

    @Override
    public int getGridLayoutId() {
        return R.layout.fragment_menu_grid;
    }

    @Override
    public int getListLayoutId() {
        return R.layout.fragment_menu_list;
    }

    @Override
    public void showLoading(String tip) {
        DialogFactory.showLoadingDialog(getContext(), tip);
    }

    @Override
    public void hideLoading() {
        DialogFactory.hideAll();
    }

    @Override
    public void showSelectDialog(String title, String msg, AlertDialog.ButtonClickListener listener) {
        DialogFactory.showSelectDialog(getContext(), title, msg, listener);
    }

    @Override
    public void showSelectDialog(int title, int msg, AlertDialog.ButtonClickListener listener) {
        String t = getString(title);
        String m = getString(msg);
        this.showSelectDialog(t, m, listener);
    }

    @Override
    public void showSelectDialog(int title, String msg, AlertDialog.ButtonClickListener listener) {
        String t = getString(title);
        this.showSelectDialog(t, msg, listener);
    }

    @Override
    public void hideDialog() {
        DialogFactory.hideAll();
    }

    @Override
    public void toast(String content) {
        ViewUtils.showToast(getContext(), content);
    }

    @Override
    public void toast(int id) {
        ViewUtils.showToast(getContext(), id);
    }

    @Override
    public boolean jumpToChildMenu(Menu menu) {
        return true;
    }

    @Override
    public void jumpToTrade(String transCode, TradeProcess process) {
        if (checkTradeState(transCode)) return;

        Intent intent = new Intent(getContext(), TradeFragmentContainer.class);
        intent.putExtra(BaseActivity.KEY_TRANSCODE, transCode);
        intent.putExtra(BaseActivity.KEY_PROCESS, process);
        ITradeParameter parameter = (ITradeParameter) ConfigureManager.getSubPrjClassInstance(new BaseTradeParameter());
        if (parameter.getParam(transCode) != null)
            intent.putExtra(ITradeParameter.KEY_TRANS_PARAM, parameter.getParam(transCode));
        startActivityForResult(intent, REQ_TRANSACTION);
    }

    /**
     * 检查交易运行时环境
     *
     * @param transCode 交易代码
     * @return true 检查失败，false 检查成功
     */
    private boolean checkTradeState(String transCode) {
        if (!CommonUtils.tradeEnvironmentCheck(getContext())) {
            logger.debug("^_^ 电池电量低 ^_^");
            ViewUtils.showToast(getContext(), "电量低，请充电！");
            return true;
        }

        boolean needOperLogin = !ConfigureManager.getInstance(getContext()).isOptionFuncEnable
                (getContext(), Keys.obj().check_merchant_info);
        if (needOperLogin) {
            if (TextUtils.isEmpty(BusinessConfig.getInstance().getIsoField(getContext(), 41)) || TextUtils.isEmpty
                    (BusinessConfig.getInstance().getIsoField(getContext(), 42))) {
                ViewUtils.showToast(getContext(), R.string.tip_pls_set_trade_info);
                return true;
            }
        }

        if (!NetUtils.isNetConnected(getContext())) {
            ViewUtils.showToast(EposApplication.getAppContext(), "网络未连接！");
            return true;
        }

        if (!TransCode.NO_AUTOSIGN_TRADE_SETS.contains(transCode) && AutoSignInController.isNeedAutoSignIn()) {
            ViewUtils.showToast(getContext(), "正在进行自动签到！");
            presenter.beginOnlineProcess(TransCode.SIGN_IN);
            return true;
        }

        if (!TransCode.NO_AUTOSIGN_TRADE_SETS.contains(transCode)
                && BusinessConfig.getInstance().getFlag(getContext(), TransDataKey.FLAG_HAS_DOWNLOAD_PARAM)
                && !BusinessConfig.getInstance().getFlag(getContext(), TransDataKey.FLAG_HAS_DOWNLOAD_CARK)) {
            presenter.beginDownloadParam(TransCode.DOWNLOAD_CAPK);
            return true;
        }

        if (!TransCode.NO_AUTOSIGN_TRADE_SETS.contains(transCode)
                && BusinessConfig.getInstance().getFlag(getContext(), TransDataKey.FLAG_HAS_DOWNLOAD_PARAM)
                && !BusinessConfig.getInstance().getFlag(getContext(), TransDataKey.FLAG_HAS_DOWNLOAD_AID)) {
            presenter.beginDownloadParam(TransCode.DOWNLOAD_AID);
            return true;
        }
//        Settings.setValue(getContext(), Settings.KEY.BATCH_SEND_STATUS, "0");
//        BusinessConfig.getInstance().setFlag(getContext(), BusinessConfig.Key.FLAG_ESIGN_STORAGE_WARNING, false);
        //流水超上限，先批ls结算后再交易。
        if (!presenter.isManagerTrade(transCode)) {
            String tipInfo = null;
            if (BusinessConfig.getInstance().getFlag(getContext(), BusinessConfig.Key.FLAG_TRADE_STORAGE_WARNING))
                tipInfo = "交易记录已满,请结算后开始交易";
            else if (BusinessConfig.getInstance().getFlag(getContext(), BusinessConfig.Key.FLAG_ESIGN_STORAGE_WARNING))
                tipInfo = "签名图片已满,请结算后继续交易";
            if (!TextUtils.isEmpty(tipInfo)) {
                DialogFactory.showSelectDialog(getContext(), null, tipInfo, new AlertDialog
                        .ButtonClickListener() {
                    @Override
                    public void onClick(AlertDialog.ButtonType button, View v) {
                        switch (button) {
                            case POSITIVE:
//                                Settings.setValue(getContext(), Settings.KEY.BATCH_SEND_STATUS, "1");
                                presenter.beginOnlineProcess(TransCode.SETTLEMENT);
                                break;
                        }
                    }
                });
                return true;
            }
        }
        String batchState = Settings.getValue(getContext(), Settings.KEY.BATCH_SEND_STATUS, "0");
        if ("2".equals(batchState) && !TransCode.SIGN_OUT.equals(transCode)) {
            String errTips = getString(R.string.tip_batch_over_please_sign_out);
            logger.debug("^_^ " + errTips + " ^_^");
//            ViewUtils.showToast(getContext(), errTips);
            getHostActivity().jumpToLogin();
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == MainActivity.SHOW_LOGIN) {
            clearFragmentStack();
            getHostActivity().replace(new LoginFragment()).commit();
        }
    }

    private void clearFragmentStack() {
        if (getHostActivity().getFragmentManager().getBackStackEntryCount() > 0)
            getHostActivity().getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @Override
    public void jumpToActivity(Class<? extends Activity> clz) {
        Intent intent = new Intent(getContext(), clz);
        startActivity(intent);
    }

    @Override
    public void jumpToActivity(Class<? extends Activity> clz, Bundle parameters) {
        Intent intent = new Intent(getContext(), clz);
        intent.putExtras(parameters);
        startActivity(intent);
    }

    @Override
    public void jumpToActivity(Class<? extends Activity> clz, Map<String, String> parameterMap) {
        Intent intent = new Intent(getContext(), clz);
        for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
            intent.putExtra(entry.getKey(), entry.getValue());
        }
        startActivity(intent);
    }

    protected void setPresenter(GTMenuPresenter presenter) {
        this.presenter = presenter;
        Bundle bundle = getArguments();
        presenter.initLocalData(bundle == null ? null : (Menu) bundle.getParcelable(KEY_MENU));
        if (bundle != null)
            isAutoSign = Boolean.parseBoolean(bundle.getString(KEY_AUTO_SIGN));
    }

    private boolean[] needDownload = {true, true, true, true};//防止下载失败又重新下载

    @Override
    public void onResume() {
        presenter.initOnResume();
        super.onResume();
        ApplicationEnvironment.startCheckVersion(getContext());
        if (isAutoSign && needDownload[0]) {
            needDownload[0] = false;
            isAutoSign = false;
            presenter.beginAutoSign();
            return;
        }

        if (needDownload[1]
                && !AutoSignInController.isNeedAutoSignIn()
                && !BusinessConfig.getInstance().getFlag(getContext(), TransDataKey.FLAG_HAS_DOWNLOAD_CARK)) {
            needDownload[1] = false;
            presenter.beginDownloadParam(TransCode.DOWNLOAD_CAPK);
            return;
        }

        if (needDownload[2]
                && !AutoSignInController.isNeedAutoSignIn()
                && !BusinessConfig.getInstance().getFlag(getContext(), TransDataKey.FLAG_HAS_DOWNLOAD_AID)) {
            needDownload[2] = false;
            presenter.beginDownloadParam(TransCode.DOWNLOAD_AID);
            return;
        }

        if(hasBanner){
            if (indicator != null) {
                sliderLayout.startAutoCycle();
            }
        }else {
            picQuery();
        }

    }

    @Override
    public void onPause() {
        presenter.release();
        super.onPause();
        if (indicator != null) {
            sliderLayout.stopAutoCycle();
        }
    }

    public void popToast(final String content){
        if(getActivity()==null){
            return;
        }
        if (Looper.getMainLooper().getThread() == Thread.currentThread())
            ViewUtils.showToast(getActivity(), content);
        else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ViewUtils.showToast(getActivity(), content);
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        sliderLayout.stopAutoCycle();
        super.onDestroy();
    }
}
