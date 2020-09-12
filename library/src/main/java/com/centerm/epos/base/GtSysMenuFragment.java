package com.centerm.epos.base;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.AsyncTask;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.centerm.cloudsys.sdk.common.utils.NetUtils;
import com.centerm.epos.EposApplication;
import com.centerm.epos.R;
import com.centerm.epos.activity.MainActivity;
import com.centerm.epos.activity.msn.AbnormalQueryTradeActivity;
import com.centerm.epos.activity.msn.BaseModifyPwdActivity;
import com.centerm.epos.activity.msn.BaseQueryOperatorActivity;
import com.centerm.epos.activity.msn.DownloadTmkActivity;
import com.centerm.epos.activity.msn.GtMerchantSettingsActivity;
import com.centerm.epos.activity.msn.ReprintActivity;
import com.centerm.epos.activity.msn.SysManageActivity;
import com.centerm.epos.activity.msn.TradeQueryActivity;
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
import com.centerm.epos.task.AsyncBatchSettleDown;
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

import java.util.HashMap;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.epos.base.BaseActivity.KEY_AUTO_SIGN;

/**
 * 绿城系统管理
 * author:liubit</br>
 * date:2019/9/5</br>
 */
public class GtSysMenuFragment extends BaseFragment implements IMenuView {
    private MenuPresenter presenter;

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
        return R.layout.fragment_menu_sys;
    }

    @Override
    protected void onInitView(View view) {
        presenter.initTopView();

        view.findViewById(R.id.mBtn1).setOnClickListener(this);
        view.findViewById(R.id.mBtn2).setOnClickListener(this);
        view.findViewById(R.id.mBtn3).setOnClickListener(this);
        view.findViewById(R.id.mBtn4).setOnClickListener(this);
        view.findViewById(R.id.mBtn5).setOnClickListener(this);
        view.findViewById(R.id.mBtn6).setOnClickListener(this);
        view.findViewById(R.id.mBtn7).setOnClickListener(this);
        view.findViewById(R.id.mBtn8).setOnClickListener(this);
        view.findViewById(R.id.mBtnExit).setOnClickListener(this);

        TextView mHotLine = (TextView) view.findViewById(R.id.mHotLine);
        if(mHotLine!=null){
            String hotLine = BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), BusinessConfig.Key.HOTLINE_KEY);
            if(!TextUtils.isEmpty(hotLine)){
                mHotLine.setText(hotLine);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(CommonUtils.isFastClick()){
            return;
        }
        if(v.getId()==R.id.mBtn1){
            jumpToActivity(SysManageActivity.class);
        }else if(v.getId()==R.id.mBtn2){
            jumpToActivity(GtMerchantSettingsActivity.class);
        }else if(v.getId()==R.id.mBtn3){
            Map<String,String> paramAdmin = new HashMap<>(1);
            paramAdmin.put(BaseModifyPwdActivity.PARAM_TAG, "99");
            Intent intent = new Intent(getContext(), BaseModifyPwdActivity.class);
            for (Map.Entry<String, String> entry : paramAdmin.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
            startActivity(intent);
        }else if(v.getId()==R.id.mBtn4){
            jumpToActivity(DownloadTmkActivity.class);
        }else if(v.getId()==R.id.mBtn5){
            jumpToActivity(BaseQueryOperatorActivity.class);
        }else if(v.getId()==R.id.mBtn6){//异常交易查询
            jumpToActivity(TradeQueryActivity.class);
        }else if(v.getId()==R.id.mBtn7){//结算
            doSettle();
        }else if(v.getId()==R.id.mBtn8){
            jumpToActivity(ReprintActivity.class);
        }else if(v.getId()==R.id.mBtnExit){
            tipToExit();
        }else {
            super.onClick(v);
        }
    }

    private void tipToExit() {
        DialogFactory.showSelectDialog(getActivity(),Settings.bIsSettingBlueTheme() ? getString(R.string.tip_notification) : null, getString(R.string.tip_confirm_exit), new AlertDialog
                .ButtonClickListener() {
            @Override
            public void onClick(AlertDialog.ButtonType button, View v) {
                switch (button) {
                    case POSITIVE:
                        getHostActivity().loadLoginView();
                        break;
                }
            }
        });
    }

    private void doSettle(){
        if (TextUtils.isEmpty(BusinessConfig.getInstance().getIsoField(getContext(), 41)) || TextUtils.isEmpty
                (BusinessConfig.getInstance().getIsoField(getContext(), 42))) {
            ViewUtils.showToast(getContext(), R.string.tip_pls_set_trade_info);
            return;
        }
        new AsyncBatchSettleDown(getActivity()) {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                DialogFactory.showLoadingDialog(getActivity(), getActivity().getString(com.centerm.epos.R
                        .string
                        .tip_query_flow));
            }

            @Override
            public void onFinish(Object o) {
                super.onFinish(o);
                if (o instanceof Boolean && (Boolean) o) {
                    DialogFactory.showSelectDialog(getActivity(),
                            getActivity().getString(com.centerm.epos.R.string.tip_notification),
                            getActivity().getString(com.centerm.epos.R.string.tip_comfirm_batch), new AlertDialog.ButtonClickListener() {
                                @Override
                                public void onClick(AlertDialog.ButtonType button, View v) {
                                    switch (button) {
                                        case POSITIVE:
                                            beginOnlineProcess(TransCode.SETTLEMENT);
                                            break;
                                    }
                                }
                            });
                } else {
                    DialogFactory.hideAll();
                    ViewUtils.showToast(getActivity(),
                            getActivity().getString(com.centerm.epos.R.string.tip_no_trans_flow));
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void beginOnlineProcess(String menuTag) {
        ConfigureManager config = ConfigureManager.getInstance(getActivity());
        TradeProcess process = config.getTradeProcess(getActivity(), "online");
        if (process == null) {
            logger.warn("通用联机流程未定义！");
            return;
        }
        jumpToTrade(menuTag, process);
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
        return R.layout.fragment_menu_grid;
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
        GtSysMenuFragment frag = new GtSysMenuFragment();
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
        Intent intent = new Intent(getContext(), TradeFragmentContainer.class);
        intent.putExtra(BaseActivity.KEY_TRANSCODE, transCode);
        intent.putExtra(BaseActivity.KEY_PROCESS, process);
        ITradeParameter parameter = (ITradeParameter) ConfigureManager.getSubPrjClassInstance(new BaseTradeParameter());
        if (parameter.getParam(transCode) != null)
            intent.putExtra(ITradeParameter.KEY_TRANS_PARAM, parameter.getParam(transCode));
        startActivityForResult(intent, REQ_TRANSACTION);
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
    }

    @Override
    public void onPause() {
        presenter.release();
        super.onPause();
    }
}
