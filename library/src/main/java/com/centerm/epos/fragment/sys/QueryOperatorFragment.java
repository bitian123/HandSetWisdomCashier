package com.centerm.epos.fragment.sys;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.centerm.epos.R;
import com.centerm.epos.activity.E10SysMenuActivity;
import com.centerm.epos.adapter.OperaterAdapter;
import com.centerm.epos.base.BaseFragment;
import com.centerm.epos.bean.Employee;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.view.AlertDialog;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;

import config.Config;

import static com.centerm.epos.activity.msn.BaseModifyPwdActivity.PARAM_TAG;

/**
 * create by liubit on 2019-09-06
 */
public class QueryOperatorFragment extends BaseFragment {
    Dao<Employee, String> employeeCommonDao;
    ListView lstvwOpt;
    OperaterAdapter<Employee> adapterOpt;
    private Where whereSql;
    protected DbHelper dbHelper;

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        try {
            dbHelper = OpenHelperManager.getHelper(getActivity(), DbHelper.class);
            employeeCommonDao = dbHelper.getDao(Employee.class);
            whereSql = employeeCommonDao.queryBuilder().orderBy("code", true).where().ne("code",
                    Config.DEFAULT_MSN_ACCOUNT).and().ne("code", Config.DEFAULT_ADMIN_ACCOUNT);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onInitView(View view) {
        view.findViewById(R.id.mBtnAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddOperatorFragment fragment = new AddOperatorFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(Config.OPERATOR_NUM, adapterOpt.getCount());
                fragment.setArguments(bundle);
                ((E10SysMenuActivity)getActivity()).gotoFragment(fragment);
            }
        });
        lstvwOpt = (ListView) view.findViewById(R.id.lstvw_opt);
        adapterOpt = new OperaterAdapter<Employee>(getActivity(), R.layout.adapter_query_operator,
                R.id.opt_tv, R.id.modify_psw, R.id.del_opt) {
            @Override
            public void convert(final Employee model, ViewHoder hoder) {
                super.convert(model, hoder);
                hoder.txtvw.setText(" " + model.getCode());
                hoder.iv_edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ModifyPwFragment fragment = new ModifyPwFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString(PARAM_TAG, model.getCode());
                        fragment.setArguments(bundle);
                        ((E10SysMenuActivity)getActivity()).gotoFragment(fragment);
                    }
                });
                hoder.iv_del.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogFactory.showSelectDialog(getActivity(), null, getString(R.string
                                .tip_confirm_del_opt), new AlertDialog.ButtonClickListener() {
                            @Override
                            public void onClick(AlertDialog.ButtonType button, View v) {
                                switch (button) {
                                    case POSITIVE:
                                        try {
                                            boolean isDel = employeeCommonDao.deleteById(model
                                                    .getCode()) == 1;
                                            if (isDel) {
                                                refrashData();
                                                ViewUtils.showToast(getActivity(), R.string
                                                        .tip_opt_delete_suc);
                                            } else {
                                                ViewUtils.showToast(getActivity(), R.string
                                                        .tip_opt_delete_failure);
                                            }
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                            ViewUtils.showToast(getActivity(), R.string
                                                    .tip_opt_delete_failure);
                                        }
                                        break;
                                }
                            }
                        });
                    }
                });
            }
        };
        lstvwOpt.setAdapter(adapterOpt);
        try {
            adapterOpt.addAll(whereSql.query());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        dbHelper.removeDao(Employee.class);
        employeeCommonDao = null;
        super.onDestroy();
    }

    private void refrashData() {
        if (null != adapterOpt) {
            try {
                adapterOpt.clear();
                adapterOpt.addAll(whereSql.query());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected int onLayoutId() {
        return R.layout.fragment_query_operator;
    }
}
