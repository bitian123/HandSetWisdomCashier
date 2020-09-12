package com.centerm.epos.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.cloudsys.sdk.common.utils.PackageUtils;
import com.centerm.cpay.midsdk.dev.define.IPinPadDev;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseActivity;
import com.centerm.epos.base.MenuActivity;
import com.centerm.epos.bean.Employee;
import com.centerm.epos.common.Settings;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.task.AsyncAppInitTask;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.ViewUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;
import config.Config;

/**
 * @author linwenhui
 * @date 2016/11/2.
 */
public class LoginActivity extends BaseActivity {
    private EditText edtxtAccount, edtxtPwd;
    private CommonDao<Employee> employeeCommonDao;

    @Override
    public boolean isOpenDataBase() {
        return true;
    }

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        employeeCommonDao = new CommonDao<>(Employee.class, dbHelper);
    }

    @Override
    public int onLayoutId() {
        //// TODO: 2017/2/26 二次开发点（布局不同） by lwl
        int layoutId = getLayoutId("activity_login");
        if (layoutId <= 0) {
            layoutId = R.layout.activity_login;
        }
        return layoutId;
    }

    @Override
    public void onInitView() {
        edtxtAccount = (EditText) findViewById(R.id.account_edit);
        edtxtPwd = (EditText) findViewById(R.id.pwd_edit);
        edtxtAccount.addTextChangedListener(new CutPassword());

        TextView versionShow = (TextView) findViewById(R.id.version_show);
        versionShow.setText("程序版本：V" + PackageUtils.getInstalledVersionName(context,
                getPackageName()));
    }

    @Override
    public void afterInitView() {
        super.afterInitView();
        boolean firstLaunch = Settings.getValue(context, Settings.KEY.FIRST_TIME_LOADING, true);
        if (firstLaunch) {
            new AsyncAppInitTask(context) {
                @Override
                public void onStart() {
                    DialogFactory.showLoadingDialog(LoginActivity.this, "程序初始化中，请稍候...");
                }

                @Override
                public void onFinish(Object o) {
                    DialogFactory.hideAll();
                    ViewUtils.showToast(LoginActivity.this, "初始化完成！");
                }

                @Override
                public void onProgress(Integer counts, Integer index) {
                }
            }.execute("");
        }
    }

    public void onLoginClick(View v) {
        String tagAccount = edtxtAccount.getText().toString().trim().replace(" ", "");
        String tagPwd = edtxtPwd.getText().toString().trim().replace(" ", "");

        if (TextUtils.isEmpty(tagAccount) || tagAccount.length() < 2) {
            ViewUtils.showToast(this, R.string.tip_please_input_account);
            return;
        }
        //系统管理员8位密码，主管操作员6位密码，一般操作员4位密码
        if (TextUtils.isEmpty(tagPwd)) {
            ViewUtils.showToast(this, R.string.label_login_input_pwd_empth);
            return;
        } else if (tagPwd.length() < 4) {
            ViewUtils.showToast(this, R.string.tip_pwd_length_illegal);
            return;
        }
        Map<String, String> conditions = new HashMap<>();
        conditions.put("code", tagAccount);
        List<Employee> employees = employeeCommonDao.queryByMap(conditions);
        if (employees != null && !employees.isEmpty()) {
            conditions.put("password", tagPwd);
            employees = employeeCommonDao.queryByMap(conditions);
            if (employees != null && !employees.isEmpty()) {
                if (Config.DEFAULT_ADMIN_ACCOUNT.equals(tagAccount)) {
                    //系统管理员账号
                    ViewUtils.showToast(context, R.string.tip_login_admin_suc);
                    jumpToManagerView(true);
                    BusinessConfig.getInstance().setValue(context, BusinessConfig.Key
                            .KEY_OPER_ID, tagAccount);
                } else if (Config.DEFAULT_MSN_ACCOUNT.equals(tagAccount)) {
                    //主管操作员
                    ViewUtils.showToast(context, R.string.tip_login_msn_suc);
                    //如果当前不存在TEK，则导入TEK
                    if (!BusinessConfig.getInstance().getFlag(context, BusinessConfig.Key
                            .FLAG_TEK)) {
                        importTEK();
                    }
                    jumpToManagerView(false);
                    BusinessConfig.getInstance().setValue(context, BusinessConfig.Key
                            .KEY_OPER_ID, tagAccount);
                } else {

//                    Intent intent = new Intent(context, TradeFragmentContainer.class);
//                    intent.putExtra(KEY_TRANSCODE, TransCode.SALE);
//                    intent.putExtra(FLAG_AUTO_SIGN, true);
//                    startActivity(intent);

                    //普通操作员
                    if (Settings.hasTmk(context)) {
                        ViewUtils.showToast(context, R.string.tip_login_opt_suc);
                        //未签退情况下，操作员直接退出，如果与上次登录的操作员号不同则要求重新签到
                        String operator = BusinessConfig.getInstance().getValue(context,
                                BusinessConfig.Key.KEY_OPER_ID);
                        if (!tagAccount.equals(operator)) {
                            BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key
                                    .FLAG_SIGN_IN, false);
                        }
                        BusinessConfig.getInstance().setValue(context, BusinessConfig.Key
                                .KEY_OPER_ID, tagAccount);
                        jumpToMain();
                    } else {
                        DialogFactory.showMessageDialog(context, null, getString(R.string
                                .tip_login_to_down));
                    }
                }
            } else {
                ViewUtils.showToast(this, R.string.tip_login_pwd_error);
                edtxtPwd.setText("");
            }
        } else {
            ViewUtils.showToast(this, R.string.tip_account_not_exist);
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
            if (s.length() < 2)
                return;
            String tagAccount = edtxtAccount.getText().toString().trim().replace(" ", "");
            String tagPwd = edtxtPwd.getText().toString().trim().replace(" ", "");
            if (Config.DEFAULT_ADMIN_ACCOUNT.equals(tagAccount)) {
                edtxtPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
            } else if (Config.DEFAULT_MSN_ACCOUNT.equals(tagAccount)) {
                edtxtPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
                if (tagPwd.length() > 6) {
                    edtxtPwd.setText(tagPwd.substring(0, 6));
                }
            } else {
                edtxtPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
                if (tagPwd.length() > 4) {
                    edtxtPwd.setText(tagPwd.substring(0, 4));
                }
            }
        }
    }

    private void jumpToMain() {
        Intent intent = new Intent(context, MainActivity.class);
        activityStack.pop();
        startActivity(intent);
    }

    private void jumpToManagerView(boolean isAdmin) {
        Intent intent = new Intent(context, MenuActivity.class);
        if (isAdmin) {
            intent.putExtra(KEY_USER_FLAG, 1);
        } else {
            intent.putExtra(KEY_USER_FLAG, 2);
        }
        activityStack.pop();
        startActivity(intent);
    }

    private void importTEK() {
        if (isDeviceReady()) {
            IPinPadDev pinPadDev = CommonUtils.getPinPadDev();
            if (pinPadDev != null) {
                String channelName = Settings.getStringMetaData(context, "ENVIRONMENT");
                logger.debug("Ready to import TEK, the channle name is " + channelName);
                String tek;
                if ("PRODUCT".equals(channelName)) {
                    tek = "E911A168DA6A3F9981F3FF5E7E77341F";
                } else {
                    tek = "96964CD2509D486DF21F2D15E5400701";
                }
                boolean r = pinPadDev.loadTEK(tek, null);
                logger.info("Result of import TEK " + r);
                if (r) {
                    BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.FLAG_TEK,
                            true);
                } else {
                    BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.FLAG_TEK,
                            false);
                }
            } else {
                logger.warn("Import TEK failed, beacuse the SDK is not ready");
            }
        }
    }
}
