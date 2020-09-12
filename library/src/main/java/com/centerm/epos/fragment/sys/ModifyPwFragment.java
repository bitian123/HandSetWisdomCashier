package com.centerm.epos.fragment.sys;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.epos.R;
import com.centerm.epos.activity.E10SysMenuActivity;
import com.centerm.epos.base.BaseFragment;
import com.centerm.epos.bean.Employee;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.utils.ViewUtils;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.centerm.epos.activity.msn.BaseModifyPwdActivity.PARAM_TAG;

/**
 * create by liubit on 2019-09-06
 */
public class ModifyPwFragment extends BaseFragment {
    private EditText editOldPwd;
    private EditText editNewPwd;
    private EditText editRenewPwd;
    private TextView mTvOperNum;
    private Button mBtnBack;
    protected DbHelper dbHelper;
    private CommonDao<Employee> employeeCommonDao;
    private String optId;
    private int maxLenth = 0;

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        dbHelper = OpenHelperManager.getHelper(this.getContext(), DbHelper.class);
        employeeCommonDao = new CommonDao<>(Employee.class, dbHelper);
    }

    @Override
    protected void onInitView(View view) {
        mTvOperNum = (TextView) view.findViewById(R.id.mTvOperNum);
        editOldPwd = (EditText) view.findViewById(R.id.edtxt_old_pwd);
        editNewPwd = (EditText) view.findViewById(R.id.edtxt_new_pwd);
        editRenewPwd = (EditText) view.findViewById(R.id.edtxt_renew_pwd);
        view.findViewById(R.id.mBtnModify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onChangePwd(view);
            }
        });

        optId = getArguments().getString(PARAM_TAG);
        mTvOperNum.setText("操作员号"+optId);

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

            mBtnBack = (Button) view.findViewById(R.id.mBtnBack);
            mBtnBack.setVisibility(View.VISIBLE);
            mBtnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((E10SysMenuActivity)getActivity()).gotoFragment(new QueryOperatorFragment());
                }
            });
        }

    }

    @Override
    public void onDestroyView() {
        dbHelper.removeDao(Employee.class);
        employeeCommonDao = null;
        super.onDestroyView();
    }

    public final String[] onValibPwd() {
        final String oldPwd = editOldPwd.getText().toString().trim().replace(" ", "");
        final String pwd = editNewPwd.getText().toString().trim().replace(" ", "");
        final String rePwd = editRenewPwd.getText().toString().trim().replace(" ", "");
        if (TextUtils.isEmpty(oldPwd)) {
            ViewUtils.showToast(getActivity(), R.string.tip_old_pwd_empty);
            return null;
        }
        if (TextUtils.isEmpty(pwd)) {
            ViewUtils.showToast(getActivity(), R.string.tip_new_pwd_empty);
            return null;
        }
        if (TextUtils.isEmpty(rePwd)) {
            ViewUtils.showToast(getActivity(), R.string.tip_renew_pwd_empty);
            return null;
        }
        if (!pwd.equals(rePwd)) {
            ViewUtils.showToast(getActivity(), R.string.tip_pwd_not_same);
            return null;
        }
        if (pwd.length() != maxLenth) {
            ViewUtils.showToast(getActivity(), "新密码需为" + maxLenth + "位");
            return null;
        }
        if (rePwd.length() != maxLenth) {
            ViewUtils.showToast(getActivity(), "新密码需为" + maxLenth + "位");
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
                    ViewUtils.showToast(getActivity(), R.string.tip_pwd_suc);
                    editNewPwd.setText("");
                    editOldPwd.setText("");
                    editRenewPwd.setText("");
                    if(mBtnBack!=null){
                        ((E10SysMenuActivity)getActivity()).gotoFragment(new QueryOperatorFragment());
                    }
                }
            } else {
                ViewUtils.showToast(getActivity(), R.string.tip_pwd_old_pwd_error);
            }
        }
    }

    @Override
    protected int onLayoutId() {
        return R.layout.fragment_modify_pw;
    }
}
