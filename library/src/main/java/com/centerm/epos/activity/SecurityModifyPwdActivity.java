package com.centerm.epos.activity;


import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseActivity;
import com.centerm.epos.bean.Employee;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.mvp.presenter.ISecurityModifyPwdPresenter;
import com.centerm.epos.mvp.presenter.SecurityModifyPwdPresenter;
import com.centerm.epos.mvp.view.IModifySecurityPwdView;
import com.centerm.epos.utils.ViewUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;
import config.Config;

/**
 * Created by 94437 on 2017/6/28.
 */

public class SecurityModifyPwdActivity extends BaseActivity implements IModifySecurityPwdView{
    private EditText editOldPwd;
    private EditText editNewPwd;
    private EditText editRenewPwd;

    private ISecurityModifyPwdPresenter iSecurityModifyPwdPresenter;

    @Override
    public boolean isOpenDataBase() {
        return true;
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_change_pwd_msn;
    }

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
    }

    @Override
    public void onInitView() {
        TextView title = (TextView) findViewById(R.id.txtvw_title);
        editOldPwd = (EditText) findViewById(R.id.edtxt_old_pwd);
        editNewPwd = (EditText) findViewById(R.id.edtxt_new_pwd);
        editRenewPwd = (EditText) findViewById(R.id.edtxt_renew_pwd);

        title.setText("安全密码修改");
        editOldPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        editNewPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        editRenewPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    public void onChangePwd(View v) {
        final String oldPwd = editOldPwd.getText().toString().trim().replace(" ", "");
        final String pwd = editNewPwd.getText().toString().trim().replace(" ", "");
        final String rePwd = editRenewPwd.getText().toString().trim().replace(" ", "");

        iSecurityModifyPwdPresenter = new SecurityModifyPwdPresenter(this, this);
        iSecurityModifyPwdPresenter.changeSecurityPwd(oldPwd, pwd, rePwd);
    }

    @Override
    public void showResult(String result, boolean isSuccess) {
        ViewUtils.showToast(this, result);
        if(isSuccess){
            activityStack.pop();
        }
    }

    @Override
    public void showResult(int result, boolean isSuccess) {
        ViewUtils.showToast(this, result);
        if(isSuccess){
            activityStack.pop();
        }
    }
}
