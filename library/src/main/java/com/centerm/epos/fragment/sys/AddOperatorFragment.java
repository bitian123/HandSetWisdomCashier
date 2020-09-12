package com.centerm.epos.fragment.sys;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.epos.R;
import com.centerm.epos.activity.E10SysMenuActivity;
import com.centerm.epos.base.BaseFragment;
import com.centerm.epos.bean.Employee;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.utils.ViewUtils;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import config.Config;

/**
 * create by liubit on 2019-09-09
 */
public class AddOperatorFragment extends BaseFragment {
    private int type = Config.OPT_TYPE_CREATE;
    private Dao<Employee, String> employeeCommonDao;
    private TextView txtvwTitle;
    private EditText optNo, pwd, pwdConfirm;
    protected DbHelper dbHelper;

    @Override
    protected void onInitView(View view) {
        type = getActivity().getIntent().getIntExtra(Config.OPT_TYPE_TIP, type);
        txtvwTitle = (TextView) view.findViewById(R.id.txtvw_title);
        optNo = (EditText) view.findViewById(R.id.opt_no);
        pwd = (EditText) view.findViewById(R.id.pwd);
        view.findViewById(R.id.mBtnBack).setOnClickListener(this);
        view.findViewById(R.id.confirm_change).setOnClickListener(this);
        pwdConfirm = (EditText) view.findViewById(R.id.pwd_confirm);
        try {
            dbHelper = OpenHelperManager.getHelper(getActivity(), DbHelper.class);
            employeeCommonDao = dbHelper.getDao(Employee.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        int id = getArguments().getInt(Config.OPERATOR_NUM,-1);
        if(id!=-1){
            id++;
            String idStr = id+"";
            if(id<10){
                idStr = "0"+idStr;
            }
            optNo.setEnabled(false);
            optNo.setText(idStr);
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.mBtnBack){
            finishFragment();
        }else if(v.getId()==R.id.confirm_change){
            onSubmit(v);
        }
    }

    @Override
    public void onDestroy() {
        dbHelper.removeDao(Employee.class);
        employeeCommonDao = null;
        super.onDestroy();
    }

    public final Map<String, String> onValib() {
        String tagOptNo = optNo.getText().toString().trim().replace(" ", "");
        String tagPwd = pwd.getText().toString().trim().replace(" ", "");
        String tagPwdConfirm = pwdConfirm.getText().toString().trim().replace(" ", "");

        if (TextUtils.isEmpty(tagOptNo)) {
            ViewUtils.showToast(getActivity(), R.string.tip_please_input_opt_no);
            return null;
        }
        /*BUGID:0002191 解决 操作员管理可以同时添加01和1一般操作员
         *zhouzhihua modify 2017.11.7
         * */
        tagOptNo = String.format("%02d", Integer.parseInt(tagOptNo));

        if ( tagOptNo.compareTo("01") < 0 || tagOptNo.compareTo("98") > 0 ) {
            ViewUtils.showToast(getActivity(), R.string.tip_opt_no_limit);
            return null;
        }
        if (TextUtils.isEmpty(tagPwd) || tagPwd.length() < 4) {
            ViewUtils.showToast(getActivity(), R.string.tip_please_input_opt_pwd);
            return null;
        }
        if (TextUtils.isEmpty(tagPwdConfirm) || tagPwdConfirm.length() < 4) {
            ViewUtils.showToast(getActivity(), R.string.tip_please_input_opt_pwd);
            return null;
        }
        if (!tagPwd.equals(tagPwdConfirm)) {
            ViewUtils.showToast(getActivity(), R.string.tip_opt_pwd_not_same);
            return null;
        }
        Map<String, String> conditions = new HashMap<>();
        conditions.put(Config.OPT_NO_TIP, tagOptNo);
        conditions.put(Config.OPT_PWD_TIP, tagPwd);
        return conditions;
    }

    public void onSubmit(View v) {
        Map<String, String> conditions = onValib();
        if (conditions == null)
            return;
        Employee employee = new Employee(conditions.get(Config.OPT_NO_TIP), conditions.get(Config.OPT_PWD_TIP));

        if (type == Config.OPT_TYPE_CREATE) {
            Employee emp = null;
            try {
                emp = employeeCommonDao.queryForId(employee.getCode());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (null!=emp) {
                ViewUtils.showToast(getActivity(), R.string.tip_opt_exsit);
            } else {
                boolean ret = false;
                try {
                    ret = employeeCommonDao.create(employee) == 1;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                if (ret) {
                    ViewUtils.showToast(getActivity(), R.string.tip_opt_create_suc);
                    finishFragment();
                } else {
                    ViewUtils.showToast(getActivity(), R.string.tip_opt_create_failure);
                }
            }

        } else if (type == Config.OPT_TYPE_UPDATE) {
            boolean ret = false;
            try {
                ret = employeeCommonDao.update(employee) == 1;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (ret) {
                ViewUtils.showToast(getActivity(), R.string.tip_opt_update_suc);
                finishFragment();
            } else {
                ViewUtils.showToast(getActivity(), R.string.tip_opt_update_failure);
            }
        }
    }

    private void finishFragment(){
        ((E10SysMenuActivity)getActivity()).gotoFragment(new QueryOperatorFragment());
    }

    @Override
    protected int onLayoutId() {
        return R.layout.fragment_add_operator;
    }

}
