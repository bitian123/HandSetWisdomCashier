package com.centerm.epos.base;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.centerm.cloudsys.sdk.common.utils.NetUtils;
import com.centerm.epos.EposApplication;
import com.centerm.epos.R;
import com.centerm.epos.activity.MainActivity;
import com.centerm.epos.adapter.MenuAbsListAdapter;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.fragment.LoginFragment;
import com.centerm.epos.model.BaseTradeParameter;
import com.centerm.epos.model.ITradeParameter;
import com.centerm.epos.mvp.presenter.MenuPresenter;
import com.centerm.epos.mvp.view.IMenuView;
import com.centerm.epos.transcation.pos.controller.AutoSignInController;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.view.AlertDialog;
import com.centerm.epos.view.GridViewPager;
import com.centerm.epos.xml.bean.menu.Menu;
import com.centerm.epos.xml.bean.menu.MenuItem;
import com.centerm.epos.xml.bean.process.TradeProcess;
import com.centerm.epos.xml.keys.Keys;

import java.util.Map;

import config.BusinessConfig;

import static com.centerm.epos.base.BaseActivity.KEY_AUTO_SIGN;

/**
 * 标准菜单页。支持列表式和分页九宫格。
 * author:wanliang527</br>
 * date:2017/2/19</br>
 */
public class MenuFragment extends BaseFragment implements IMenuView {

    private MenuAbsListAdapter absListAdapter;//列表适配器
    //    private GridViewPager.GridPagerAdapter gridPagerAdapter;//九宫格适配器
    private MenuPresenter presenter;

    private boolean isAutoSign = false;

    @Override
    protected void onInitLocalData(Bundle savedInstanceState) {
        setPresenter(new MenuPresenter(this));
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

        if (!Settings.isAppInit(getActivity()))
            return;

        boolean isLock = BusinessConfig.getInstance().getFlag(getContext(), BusinessConfig.Key.KEY_IS_LOCK);
        if (isLock) {
            DialogFactory.showLockDialog(getContext());
        }
    }

    /**
     * @param left   the left margin size
     * @param top    the top margin size
     * @param right  the right margin size
     * @param bottom the bottom margin size
     * @attr ref android.R.styleable#ViewGroup_MarginLayout_layout_marginLeft
     * @attr ref android.R.styleable#ViewGroup_MarginLayout_layout_marginTop
     * @attr ref android.R.styleable#ViewGroup_MarginLayout_layout_marginRight
     * @attr ref android.R.styleable#ViewGroup_MarginLayout_layout_marginBottom
     */
    private void setFragmentContainerMargins(int left, int top, int right, int bottom) {
        if (!Settings.bIsSettingBlueTheme()) {
            return;
        }
        FrameLayout fragContainer = (FrameLayout) getHostActivity().findViewById(R.id.frag_container);

        ViewGroup.LayoutParams paramsF = fragContainer.getLayoutParams();
        ViewGroup.MarginLayoutParams marginLayoutParams = null;
        if (paramsF instanceof ViewGroup.MarginLayoutParams) {
            marginLayoutParams = (ViewGroup.MarginLayoutParams) paramsF;
        } else {
            marginLayoutParams = new ViewGroup.MarginLayoutParams(paramsF);
        }
        marginLayoutParams.setMargins(left, top, right, bottom);
        fragContainer.setLayoutParams(marginLayoutParams);
    }

    /**
     * 主要设置topbar 图标显示的位置
     *
     * @param resid 背景资源
     * @param id    topbar的高度
     *              <p>
     *              zhouzhihua
     */
    private void setTitleBar(@DrawableRes int resid, @DimenRes int id, boolean bIsGridView) {
        if (!Settings.bIsSettingBlueTheme()) {
            return;
        }
        RelativeLayout relativeLayoutTitle = (RelativeLayout) getHostActivity().findViewById(R.id.layout_title);
        relativeLayoutTitle.setBackgroundResource(resid);
        ViewGroup.LayoutParams params = relativeLayoutTitle.getLayoutParams();
        params.height = getResources().getDimensionPixelSize(id);
        relativeLayoutTitle.setLayoutParams(params);
        if (bIsGridView) {
            Button button = (Button) relativeLayoutTitle.findViewById(R.id.btn_title_right);
            button.setBackgroundResource(R.drawable.btn_exit_bg);
            RelativeLayout.LayoutParams buttonLayoutParams = new RelativeLayout.LayoutParams(button.getLayoutParams());
            buttonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);

            buttonLayoutParams.topMargin = getResources().getDimensionPixelSize(R.dimen.margin_72);
            buttonLayoutParams.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.margin_44));
            buttonLayoutParams.width = getResources().getDimensionPixelOffset(R.dimen.width_80);
            buttonLayoutParams.height = getResources().getDimensionPixelOffset(R.dimen.height_80);
            button.setLayoutParams(buttonLayoutParams);

            ImageView imageView_menu = (ImageView) relativeLayoutTitle.findViewById(R.id.imageView_menu);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(imageView_menu.getLayoutParams());

            layoutParams.topMargin = getResources().getDimensionPixelSize(R.dimen.margin_100);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            imageView_menu.setLayoutParams(layoutParams);


            TextView txtvw_title = (TextView) relativeLayoutTitle.findViewById(R.id.txtvw_title);
            RelativeLayout.LayoutParams titleTxtParam = new RelativeLayout.LayoutParams(txtvw_title.getLayoutParams());

            titleTxtParam.topMargin = getResources().getDimensionPixelSize(R.dimen.margin_80);
            titleTxtParam.addRule(RelativeLayout.CENTER_HORIZONTAL);
            txtvw_title.setLayoutParams(titleTxtParam);

            ImageButton imgbtn_back = (ImageButton) relativeLayoutTitle.findViewById(R.id.imgbtn_back);
            RelativeLayout.LayoutParams imgbtn_backParam = new RelativeLayout.LayoutParams(imgbtn_back.getLayoutParams());
            imgbtn_backParam.height = RelativeLayout.LayoutParams.WRAP_CONTENT;

            imgbtn_backParam.topMargin = getResources().getDimensionPixelSize(R.dimen.margin_70);
            imgbtn_back.setLayoutParams(imgbtn_backParam);
        }
    }

    /**
     * @return TopBar的高度
     */
    private int getTopBarHeight() {
        int id;
        RelativeLayout relativeLayoutTitle = (RelativeLayout) getHostActivity().findViewById(R.id.layout_title);
        id = relativeLayoutTitle.getVisibility() == View.VISIBLE ? R.dimen.common_title_height : R.dimen.bg_home_list_height_240;
        return getResources().getDimensionPixelSize(id);
    }

    @Override
    protected void onInitView(View view) {
        presenter.initTopView();
        //初始化列表或九宫格
        final View menuView = view.findViewById(R.id.menu_view);
        if (menuView instanceof AbsListView) {

            setTitleBar(R.drawable.bg_topbar, R.dimen.common_title_height, false);
            setFragmentContainerMargins(0, getTopBarHeight()/*getResources().getDimensionPixelSize(R.dimen.common_title_height)*/, 0, 0);

            absListAdapter = presenter.getListAdapter();
            ((AbsListView) menuView).setAdapter(absListAdapter);
            ((AbsListView) menuView).setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    MenuItem item = absListAdapter.getItem(position);
                    presenter.onMenuItemClicked(view, item);
                }
            });
        } else if (menuView instanceof GridViewPager) {

            setTitleBar(R.drawable.bg_result_suc, R.dimen.height_300, true);
            setFragmentContainerMargins(0, getResources().getDimensionPixelSize(R.dimen.margin_220), 0, 0);

            GridViewPager.GridPagerAdapter gridPagerAdapter = presenter.getGridAdapter();
            ((GridViewPager) menuView).setAdapter(gridPagerAdapter);
            if (!TextUtils.isEmpty(presenter.getMenu().getTransCode()) && "MAIN".equals(presenter.getMenu().getTransCode())) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((GridViewPager) menuView).setOnItemClickListener(new GridViewPager.ItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                MenuItem item = presenter.getMenu().getItem(position);
                                presenter.onMenuItemClicked(view, item);
                            }
                        });
                    }
                }, 1000);
            } else {
                ((GridViewPager) menuView).setOnItemClickListener(new GridViewPager.ItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        MenuItem item = presenter.getMenu().getItem(position);
                        presenter.onMenuItemClicked(view, item);
                    }
                });
            }
        }
    }

    @Override
    public void onToggleIfExists(View view) {
        if (view == null) {
            return;
        }
        CheckBox toggleView = (CheckBox) view.findViewById(R.id.toggle);
        if (toggleView != null) {
            boolean t = toggleView.isChecked();
            toggleView.setChecked(!t);
        }
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
        if (menu == null) {
            return false;
        }
        MenuFragment frag = new MenuFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_MENU, menu);
        frag.setArguments(bundle);
        BaseFragmentActivity act = getHostActivity();
        act.replace(frag).addToBackStack(null).commit();
        act.showBackBtn();
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

        if (!TransCode.NO_AUTOSIGN_TRADE_SETS.contains(transCode)
                && transCode.contains("SCAN") && TextUtils.isEmpty(Settings.getValue(getContext(), "MAK", ""))) {
            presenter.beginDownloadParam("DOWNLOAD_MAIN_KEY");
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

    protected void setPresenter(MenuPresenter presenter) {
        this.presenter = presenter;
        Bundle bundle = getArguments();
        presenter.initLocalData(bundle == null ? null : (Menu) bundle.getParcelable(KEY_MENU));
        if (bundle != null)
            isAutoSign = Boolean.parseBoolean(bundle.getString(KEY_AUTO_SIGN));
    }

    private boolean[] needDownload = {true, true, true, true, true};//防止下载失败又重新下载
    @Override
    public void onResume() {
        presenter.initOnResume();
        super.onResume();

        boolean isMainMenu = "MAIN".equals(((Menu) getArguments().getParcelable(KEY_MENU)).getTransCode());
        //首次安装，进行参数下载，主密钥下载，签到，IC卡公钥下载，IC卡参数下载
        if (false && isMainMenu && needDownload[0]
                && !BusinessConfig.getInstance().getFlag(getContext(), TransDataKey.FLAG_HAS_DOWNLOAD_PARAM)) {
            needDownload[0] = false;
            presenter.beginDownloadParam(TransCode.DOWNLOAD_TERMINAL_PARAMETER);
            return;
        }

        if (false && isMainMenu && needDownload[1]) {
            needDownload[1] = false;
            presenter.beginDownloadParam("DOWNLOAD_MAIN_KEY");
            return;
        }

        if (isAutoSign && needDownload[2]) {
            needDownload[2] = false;
            isAutoSign = false;
            //presenter.beginAutoSign();
            //调用外部签到
            presenter.payEntry(TransCode.SIGN_IN);
            return;
        }

        if (isMainMenu
                && needDownload[3]
                && !AutoSignInController.isNeedAutoSignIn()
                && !BusinessConfig.getInstance().getFlag(getContext(), TransDataKey.FLAG_HAS_DOWNLOAD_CARK_COMMON)) {
            needDownload[3] = false;
            //presenter.beginDownloadParam(TransCode.DOWNLOAD_CAPK);
            //调用外部签到
            presenter.payEntry(TransCode.DOWNLOAD_CAPK);
            return;
        }

        if (isMainMenu
                && needDownload[4]
                && !AutoSignInController.isNeedAutoSignIn()
                && !BusinessConfig.getInstance().getFlag(getContext(), TransDataKey.FLAG_HAS_DOWNLOAD_AID_COMMON)) {
            needDownload[4] = false;
            //presenter.beginDownloadParam(TransCode.DOWNLOAD_AID);
            //调用外部签到
            presenter.payEntry(TransCode.DOWNLOAD_AID);
            return;
        }

    }

    @Override
    public void onPause() {
        presenter.release();
        super.onPause();
    }

}
