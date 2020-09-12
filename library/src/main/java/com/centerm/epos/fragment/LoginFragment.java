package com.centerm.epos.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.centerm.cloudsys.sdk.common.utils.NetUtils;
import com.centerm.epos.ActivityStack;
import com.centerm.epos.EposApplication;
import com.centerm.epos.R;
import com.centerm.epos.activity.MainActivity;
import com.centerm.epos.base.ApplicationEnvironment;
import com.centerm.epos.base.BaseFragment;
import com.centerm.epos.base.BaseFragmentActivity;
import com.centerm.epos.bean.Employee;
import com.centerm.epos.bean.GtBean2;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.function.AppUpgradeForLiandiShopUtil;
import com.centerm.epos.redevelop.ActionInLoginViewShow;
import com.centerm.epos.redevelop.BaseAppVersion;
import com.centerm.epos.redevelop.IActionAfterLocalLogin;
import com.centerm.epos.redevelop.IActionInLoginViewShowing;
import com.centerm.epos.redevelop.IAppVersion;
import com.centerm.epos.transcation.pos.constant.JsonKeyGT;
import com.centerm.epos.transcation.pos.controller.AutoSignInController;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.OnCallListener;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.view.SelectModeDialog;
import com.centerm.epos.xml.bean.menu.Menu;
import com.centerm.epos.xml.keys.Keys;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;
import config.Config;

import static com.centerm.epos.base.BaseActivity.KEY_AUTO_SIGN;
import static config.BusinessConfig.Key.KEY_POS_SERIAL;

/**
 * author:wanliang527</br>
 * date:2017/2/19</br>
 */
public class LoginFragment extends BaseFragment implements View.OnClickListener {
    private EditText accountEdit, pwdEdit;
    private CommonDao<Employee> employeeDao;
    private BaseFragmentActivity activity;
    private boolean isCommonPay = false;
    private ImageView mIvBg;
    private LinearLayout mLl1,mLl2;
    private Button btn_login;
    private boolean isOpenCommonPay = false;

    @Override
    protected void onInitLocalData(Bundle savedInstanceState) {
        activity = getHostActivity();
        /*
         * 签到界面可以使用状态栏，zhouzhihua modify
         *
         */
        CommonUtils.enableStatusBar(activity);
        employeeDao = new CommonDao<>(Employee.class, activity.getDbHelper());
        ApplicationEnvironment.currentContext= getContext();
        ApplicationEnvironment.startCheckVersion(getContext());

    }

    /**
     * @param left the left margin size
     * @param top the top margin size
     * @param right the right margin size
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

    @Override
    protected void onInitView(View view) {
        //隐藏顶栏
        activity.setTopViewType(Menu.TopViewType.NONE);
        setFragmentContainerMargins(0, 0, 0, 0);
        view.findViewById(R.id.btn_login).setOnClickListener(this);
        mIvBg = (ImageView) view.findViewById(R.id.mIvBg);
        mLl1 = (LinearLayout) view.findViewById(R.id.mLl1);
        mLl2 = (LinearLayout) view.findViewById(R.id.mLl2);
        btn_login = (Button) view.findViewById(R.id.btn_login);
        accountEdit = (EditText) view.findViewById(R.id.account_edit);
        pwdEdit = (EditText) view.findViewById(R.id.pwd_edit);
        accountEdit.addTextChangedListener(new CutPassword());
        TextView versionShow = (TextView) view.findViewById(R.id.version_show);
        IAppVersion appVersion = (IAppVersion) ConfigureManager.getSubPrjClassInstance(new BaseAppVersion());
        String version = appVersion.getVersionName(getContext());
        versionShow.setText(version);
        versionShow.setOnClickListener(this);

        TextView mHotLine = (TextView) view.findViewById(R.id.mHotLine);
        String hotLine = BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), BusinessConfig.Key.HOTLINE_KEY);
        if(!TextUtils.isEmpty(hotLine)){
            mHotLine.setText(hotLine);
        }
        mHotLine.setOnClickListener(this);

        IActionInLoginViewShowing action = (IActionInLoginViewShowing) ConfigureManager.getProjectClassInstance
                (ActionInLoginViewShow.class);
        if (action != null){
            action.execute(getHostActivity());
        }
        showView(false);


    }

    private void showView(boolean show){
        if(show){
            mIvBg.setVisibility(View.VISIBLE);
            mLl1.setVisibility(View.VISIBLE);
            mLl2.setVisibility(View.VISIBLE);
            btn_login.setVisibility(View.VISIBLE);
        }else {
            mIvBg.setVisibility(View.INVISIBLE);
            mLl1.setVisibility(View.INVISIBLE);
            mLl2.setVisibility(View.INVISIBLE);
            btn_login.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (NetUtils.isNetConnected(getContext())) {
            if(getActivity() instanceof MainActivity){
                DialogFactory.showLoadingDialog(getActivity(), "通讯中，请稍侯");
                //延时800ms执行
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        generalReceipts();
                    }
                },800);
            }
        }else {
            ViewUtils.showToast(EposApplication.getAppContext(), "网络未连接！");
            showView(true);
        }
    }

    private void generalReceipts(){
        Map<String,Object> dataMap = new HashMap<>();
        dataMap.put(JsonKeyGT.termSn, CommonUtils.getSn());
        sendData(false, TransCode.generalReceipts, dataMap, new OnCallListener() {
            @Override
            public void onCall(Map<String, Object> result) {
                if(result!=null){
                    GtBean2 bean = (GtBean2) result.get(JsonKeyGT.returnData);
                    logger.debug("getRespCode"+bean.getRespCode());
                    if("0".equals(bean.getRespCode())){
                        isOpenCommonPay = true;
                        showSelectDialog();
                    }else if("25".equals(bean.getRespCode())){
                        //未开通普通收款
                        showView(true);
                    }else {
                        showView(true);
                    }
                }else {
                    //ViewUtils.showToast(getActivity(),"通讯异常，请重试");
                    showView(true);
                }
            }
        });
    }

    private void showSelectDialog(){
        showView(false);
        DialogFactory.showSelectModeDialog(getActivity(),
                new SelectModeDialog.ButtonClickListener() {
                    @Override
                    public void onClick(SelectModeDialog.ButtonType button, View v) {
                        switch (button) {
                            case POSITIVE://普通收款
                                gotoCommonLogin();
                                break;
                            case NEGATIVE://智慧收银
                                DialogFactory.hideAll();
                                showView(true);
                                break;
                        }
                    }
                },
                new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                        if(i==KeyEvent.KEYCODE_BACK){
                            DialogFactory.hideAll();
                            ActivityStack.getInstance().RemoveAll();
                        }
                        return true;
                    }
                });
    }

    private void gotoCommonLogin(){
        Bundle data = new Bundle();
        int requestCode = 11;
        data.putString("trans_code", "F00004");
        Intent intent = new Intent();
        String packageName = "com.centerm.epos.ebi";
        String activityName = "com.centerm.component.pay.PayEntryActivity";
        ComponentName comp = new ComponentName(packageName, activityName);
        int posSerial = BusinessConfig.getInstance().getNumber(EposApplication.getAppContext(), KEY_POS_SERIAL);
        logger.info("智慧收银流水号："+posSerial);
        data.putInt(KEY_POS_SERIAL, posSerial);
        intent.putExtras(data);
        intent.setComponent(comp);
        try {
            getActivity().startActivityForResult(intent, requestCode);
        }catch (Exception e){
            ViewUtils.showToast(getActivity(),"交易失败，请检测是否安装支付组件");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        DialogFactory.hideAll();
    }

    public void onLoginClick() {
        Context context = getContext();

        String tagAccount = accountEdit.getText().toString().trim().replace(" ", "");
        String tagPwd = pwdEdit.getText().toString().trim().replace(" ", "");

        if (TextUtils.isEmpty(tagAccount) || tagAccount.length() < 2) {
            ViewUtils.showToast(context, R.string.tip_please_input_account);
            return;
        }
        //系统管理员8位密码，主管操作员6位密码，一般操作员4位密码
        if (TextUtils.isEmpty(tagPwd)) {
            ViewUtils.showToast(context, R.string.label_login_input_pwd_empth);
            return;
        } else if (tagPwd.length() < 4) {
            ViewUtils.showToast(context, R.string.tip_pwd_length_illegal);
            return;
        }
        if("00".equals(tagAccount)){
            ViewUtils.showToast(context, R.string.tip_account_not_exist);
            return;
        }
        Map<String, String> conditions = new HashMap<>();
        conditions.put("code", tagAccount);
        List<Employee> employees = employeeDao.queryByMap(conditions);
        if (employees != null && !employees.isEmpty()) {
            conditions.put("password", tagPwd);
            employees = employeeDao.queryByMap(conditions);
            if (employees != null && !employees.isEmpty()) {
                //普通操作员登录，进行支付环境校验
                if (!Config.DEFAULT_ADMIN_ACCOUNT.equals(tagAccount) && !Config.DEFAULT_MSN_ACCOUNT.equals(tagAccount)){
                    if (!checkTradeEnviroment())
                        return;
                }
                //添加联机登录功能
                if (actionAfterLocalLogin(tagAccount)){
                    switchAfterLoginOK(tagAccount);
                }
            } else {
                ViewUtils.showToast(context, R.string.tip_login_pwd_error);
                pwdEdit.setText("");
            }
        } else {
            if(Config.SUPER_ADMIN_ACCOUNT.equals(tagAccount)){
                if (Config.SUPER_ADMIN_PWD.equals(tagPwd)) {
                    Menu menu = activity.getConfigureManager().getThirdlyMenu(getContext());
                    activity.loadMenuView(menu);
                    return;
                }else {
                    ViewUtils.showToast(context, "密码不正确");
                }
            }else {
                ViewUtils.showToast(context, R.string.tip_account_not_exist);
            }

        }
    }

    /**
     * 检测app版本是否可更新,没有新版本则登录
     */
    public void  checkUpdate(){
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
                            onLoginClick();
                        }
                    });
                    return;
                }

            }
        });
    }

    /**
     * 本地操作员登录成功后，执行项目配置的操作。
     * @param tagAccount    操作员编号
     * @return  true 执行完成   false 等待项目处理完成
     */
    private boolean actionAfterLocalLogin(final String tagAccount) {
        IActionAfterLocalLogin loginRequest = (IActionAfterLocalLogin) ConfigureManager.getRedevelopAction(Keys.obj()
                        .redevelop_login_request, IActionAfterLocalLogin.class);
        if (loginRequest != null) {
            loginRequest.doAction(getHostActivity(), tagAccount, getLoginRequestCallBack(tagAccount));
            return false;
        } else {
            return true;
        }
    }

    /**
     * 执行完项目的任务后，恢复执行原功能点的回调处理类
     * @param tagAccount    操作员编号
     * @return  回调类
     */
    @NonNull
    private IActionAfterLocalLogin.IActionCallBack getLoginRequestCallBack(final String tagAccount) {
        return new IActionAfterLocalLogin.IActionCallBack() {
            @Override
            public void resumeAfterAction() {
                switchAfterLoginOK(tagAccount);
            }
        };
    }

    /**
     * 登录成功后，根据操作员编号跳转到指定界面
     * @param tagAccount    操作员编号
     */
    private void switchAfterLoginOK(String tagAccount) {
        if (Config.DEFAULT_ADMIN_ACCOUNT.equals(tagAccount)) {
            //ViewUtils.showToast(getContext(), R.string.tip_login_admin_suc);
            //Menu menu = activity.getConfigureManager().getThirdlyMenu(getContext());
            //activity.loadMenuView(menu);

            if(isCommonPay){
                //Menu menu = activity.getConfigureManager().getThirdlyMenu(getContext());
                //activity.loadMenuView(menu);
                accountEdit.setText("");
                pwdEdit.setText("");

                Bundle data = new Bundle();
                int requestCode = 10;
                data.putString("trans_code", "F00003");
                Intent intent = new Intent();
                String packageName = "com.centerm.epos.ebi";
                String activityName = "com.centerm.component.pay.PayEntryActivity";
                ComponentName comp = new ComponentName(packageName, activityName);
                String oprId = BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), BusinessConfig.Key.KEY_OPER_ID);
                data.putString("oprId",oprId);
                data.putInt(KEY_POS_SERIAL, BusinessConfig.getInstance().getNumber(EposApplication.getAppContext(), KEY_POS_SERIAL));
                intent.putExtras(data);
                intent.setComponent(comp);
                getActivity().startActivityForResult(intent, requestCode);
            }else {
                activity.loadGTSysMenuView();
            }

        }
//        else if (Config.DEFAULT_MSN_ACCOUNT.equals(tagAccount)) {
//            ViewUtils.showToast(getContext(), R.string.tip_login_msn_suc);
//            Menu menu = activity.getConfigureManager().getSecondaryMenu(getContext());
//            activity.loadMenuView(menu);
//        }
        else {
            checkSignEnv(getContext(), tagAccount);
            BusinessConfig.getInstance().setValue(getContext(), BusinessConfig.Key.KEY_OPER_ID, tagAccount);
            //普通操作员
            Menu menu = activity.getConfigureManager().getPrimaryMenu(getContext());
            Map<String, String> params = new HashMap<>();
            params.put(KEY_AUTO_SIGN, "true");
            if(isCommonPay){
                activity.loadMenuView(menu, params);
            }else {
                activity.loadGTMenuView(menu, params);
            }
        }
    }

    /**
     * 检查交易环境，目前只检查商户号和终端号是否已经设置
     *
     * @return true检查通过 false检查失败
     */
    private boolean checkTradeEnviroment() {
        if (TextUtils.isEmpty(BusinessConfig.getInstance().getIsoField(getContext(), 41)) || TextUtils.isEmpty
                (BusinessConfig.getInstance().getIsoField(getContext(), 42))) {
//            ViewUtils.showToast(getContext(), R.string.tip_pls_set_trade_info);
//            return false;
        }
        return true;
    }


    private void checkSignEnv(Context context, String tagAccount) {
        String operator = BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.KEY_OPER_ID);
        if (!tagAccount.equals(operator) || AutoSignInController.isNeedAutoSignIn()) {
            BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.FLAG_SIGN_IN, false);
        }
    }

    @Override
    protected int onLayoutId() {
        //// TODO: 2017/2/26 二次开发点（布局不同） by lwl
        int layoutId = getLayoutId("fragment_login");
        if (layoutId <= 0) {
            layoutId = R.layout.fragment_login;
        }
        return layoutId;
    }

    private int count = 0;
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_login) {
            //检测app版本是否可更新,没有新版本则登录
            if(CommonUtils.isFastClick()){
                logger.debug("==>快速点击事件，不响应！");
                return;
            }
            checkUpdate();
        }else if(v.getId()==R.id.version_show){
            count++;
            if(count==10){
                Menu menu = activity.getConfigureManager().getThirdlyMenu(getContext());
                activity.loadMenuView(menu);
            }
        }else if(v.getId()==R.id.mHotLine){
            //new AsyncImportTmkTask(getActivity(), TmkParameterImport.SignInCode).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            //new AsyncImportTmkTask(getActivity(), TmkParameterImport.QueryCode).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public class CutPassword implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() < 2){
                return;
            }
            String tagAccount = accountEdit.getText().toString().trim().replace(" ", "");
            String tagPwd = pwdEdit.getText().toString().trim().replace(" ", "");
            if (Config.DEFAULT_ADMIN_ACCOUNT.equals(tagAccount)||Config.SUPER_ADMIN_ACCOUNT.equals(tagAccount)) {
                pwdEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
            } else if (Config.DEFAULT_MSN_ACCOUNT.equals(tagAccount)) {
                pwdEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
                if (tagPwd.length() > 6) {
                    pwdEdit.setText(tagPwd.substring(0, 6));
                }
            } else {
                pwdEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
                if (tagPwd.length() > 4) {
                    pwdEdit.setText(tagPwd.substring(0, 4));
                }
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        if(isOpenCommonPay){
            accountEdit.setText("");
            pwdEdit.setText("");
            showSelectDialog();
        }else {
            ActivityStack.getInstance().RemoveAll();
        }
        return true;
    }


}
