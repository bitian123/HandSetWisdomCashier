package com.centerm.epos.activity.msn;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.centerm.epos.R;
import com.centerm.epos.adapter.OperaterAdapter;
import com.centerm.epos.base.BaseActivity;
import com.centerm.epos.bean.Employee;
import com.centerm.epos.common.Settings;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.view.AlertDialog;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;

import config.Config;

/**
 * 《基础版本》
 * 操作员管理界面
 *
 * @author linwenhui
 * @date 2016/10/31.
 */

public class BaseQueryOperatorActivity extends BaseActivity {

    Dao<Employee, String> employeeCommonDao;
    TextView txtvwTitle, search_edit;
    ListView lstvwOpt;
    OperaterAdapter<Employee> adapterOpt;
    private Where whereSql;

    @Override
    public boolean isOpenDataBase() {
        return true;
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_query_operator;
    }

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        try {
            employeeCommonDao = dbHelper.getDao(Employee.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void onSearchClick(View view) {
        String keyWord = search_edit.getText().toString();
        try {
            if ("".equals(keyWord)) {
                adapterOpt.addAll(whereSql.query());
            } else {
                adapterOpt.clear();
                adapterOpt.addAll(employeeCommonDao.queryBuilder().where().eq("code",
                        keyWord).and().ne("code", Config.DEFAULT_MSN_ACCOUNT).and().ne
                        ("code", Config.DEFAULT_ADMIN_ACCOUNT).query());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onInitView() {
        try {
            whereSql = employeeCommonDao.queryBuilder().orderBy("code", true).where().ne("code",
                    Config.DEFAULT_MSN_ACCOUNT).and().ne("code", Config.DEFAULT_ADMIN_ACCOUNT);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initBackBtn();
        //txtvwTitle = (TextView) findViewById(R.id.txtvw_title);
        search_edit = (TextView) findViewById(R.id.search_edit);

        search_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(search_edit.getText())) {
                    logger.debug("search_edit");
                    try {
                        adapterOpt.clear();
                        adapterOpt.addAll(whereSql.query());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        findViewById(R.id.mBtnAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BaseQueryOperatorActivity.this,
                        BaseAddOperatorActivity.class);
                intent.putExtra(Config.OPT_TYPE_TIP, Config.OPT_TYPE_CREATE);
                int accountNum = 0;
                try{
                    accountNum = Integer.parseInt(adapterOpt.getItem(adapterOpt.getCount()-1).getCode());
                }catch (Exception e){
                    e.printStackTrace();
                }
                intent.putExtra(Config.OPERATOR_NUM, accountNum);
                startActivityForResult(intent, 1);
            }
        });
        //txtvwTitle.setText(R.string.label_opt_query);
        lstvwOpt = (ListView) findViewById(R.id.lstvw_opt);
        adapterOpt = new OperaterAdapter<Employee>(this, R.layout.adapter_query_operator, R.id
                .opt_tv, R.id.modify_psw, R.id.del_opt) {
            @Override
            public void convert(final Employee model, ViewHoder hoder) {
                super.convert(model, hoder);
                hoder.txtvw.setText(" " + model.getCode());
                hoder.iv_edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(BaseQueryOperatorActivity.this,
                                BaseModifyPwdActivity.class);
                        intent.putExtra("operaterId", model.getCode());
                        startActivity(intent);
                    }
                });
                hoder.iv_del.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogFactory.showSelectDialog(context, null, getString(R.string
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
                                                ViewUtils.showToast(context, R.string
                                                        .tip_opt_delete_suc);
                                            } else {
                                                ViewUtils.showToast(context, R.string
                                                        .tip_opt_delete_failure);
                                            }
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                            ViewUtils.showToast(context, R.string
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
    protected void onDestroy() {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (1 == requestCode && 1 == resultCode) {
            refrashData();
        }
    }
}
