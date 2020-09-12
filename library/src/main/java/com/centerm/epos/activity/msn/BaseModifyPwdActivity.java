package com.centerm.epos.activity.msn;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseActivity;
import com.centerm.epos.bean.Employee;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.utils.ViewUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.Config;

/**
 * @author linwenhui
 *         修改密码界面，包括主管和操作员
 * @date 2016/10/28.
 */
public class BaseModifyPwdActivity extends BaseActivity {

    public static final String PARAM_TAG = "operaterId";
    public TextView mTvShowID;
    private EditText editOldPwd;
    private EditText editNewPwd;
    private EditText editRenewPwd;

    private CommonDao<Employee> employeeCommonDao;
    private String optId;
    private int maxLenth = 0;

    @Override
    public boolean isOpenDataBase() {
        return true;
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_change_pwd_msn_gt;
    }

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        employeeCommonDao = new CommonDao<>(Employee.class, dbHelper);
    }

    @Override
    public void onInitView() {
        initBackBtn();
        optId = getIntent().getStringExtra(PARAM_TAG);
        if (TextUtils.isEmpty(optId)){
            Toast.makeText(context, "操作员号参数为空！", Toast.LENGTH_SHORT).show();
            finish();
        }
        mTvShowID = (TextView) findViewById(R.id.mTvShowID);
        editOldPwd = (EditText) findViewById(R.id.edtxt_old_pwd);
        editNewPwd = (EditText) findViewById(R.id.edtxt_new_pwd);
        editRenewPwd = (EditText) findViewById(R.id.edtxt_renew_pwd);
        mTvShowID.setText("操作员号"+optId);
        if ("00".equals(optId)) {
            //title.setText(R.string.label_change_pwd_msn);
            editOldPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
            editNewPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
            editRenewPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
            maxLenth = 6;
        } else if ("99".equals(optId)) {
            //title.setText(R.string.label_change_pwd_sys);
            editOldPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
            editNewPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
            editRenewPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
            maxLenth = 8;
        } else {
            //title.setText(R.string.label_change_opt_pwd);
            editOldPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
            editNewPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
            editRenewPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
            maxLenth = 4;
        }
    }

    @Override
    protected void onDestroy() {
        dbHelper.removeDao(Employee.class);
        employeeCommonDao = null;
        super.onDestroy();
    }

    public final String[] onValibPwd() {
        final String oldPwd = editOldPwd.getText().toString().trim().replace(" ", "");
        final String pwd = editNewPwd.getText().toString().trim().replace(" ", "");
        final String rePwd = editRenewPwd.getText().toString().trim().replace(" ", "");
        if (TextUtils.isEmpty(oldPwd)) {
            ViewUtils.showToast(this, R.string.tip_old_pwd_empty);
            return null;
        }
        if (TextUtils.isEmpty(pwd)) {
            ViewUtils.showToast(this, R.string.tip_new_pwd_empty);
            return null;
        }
        if (TextUtils.isEmpty(rePwd)) {
            ViewUtils.showToast(this, R.string.tip_renew_pwd_empty);
            return null;
        }
        if (!pwd.equals(rePwd)) {
            ViewUtils.showToast(this, R.string.tip_pwd_not_same);
            return null;
        }
        if (pwd.length() != maxLenth) {
            ViewUtils.showToast(this, "新密码需为" + maxLenth + "位");
            return null;
        }
        if (rePwd.length() != maxLenth) {
            ViewUtils.showToast(this, "新密码需为" + maxLenth + "位");
            return null;
        }
        return new String[]{oldPwd, pwd};

    }


    public void onChangePwd(View v) {
        String[] pwds = onValibPwd();
        if (pwds != null) {

            Map<String, String> conditions = new HashMap<>();
            conditions.put("code", optId);
            conditions.put("password", pwds[0]);
            List<Employee> employees = employeeCommonDao.queryByMap(conditions);
            if (employees != null && !employees.isEmpty()) {
                Employee employee = employees.get(0);
                employee.setPassword(pwds[1]);
                final boolean res = employeeCommonDao.update(employee);
                if (res) {
                    ViewUtils.showToast(this, R.string.tip_pwd_suc);
                    onBackPressed();
                }
            } else {
                ViewUtils.showToast(this, R.string.tip_pwd_old_pwd_error);
            }
        }
    }


}
