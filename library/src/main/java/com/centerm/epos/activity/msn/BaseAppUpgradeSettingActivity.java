package com.centerm.epos.activity.msn;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseActivity;
import com.centerm.epos.base.SimpleStringTag;
import com.centerm.epos.function.AppUpgradeUtil;
import com.centerm.epos.utils.ViewUtils;

import config.BusinessConfig;


/**
 * Created by 94437 on 2017/7/5.
 */

public class BaseAppUpgradeSettingActivity extends BaseActivity {
    private EditText etConnectTimeout;
    private CheckBox cbWifiOnly, cbAppUpgradeEnable;
    private Button btnUpgrade;
    private BusinessConfig config;

    @Override
    public int onLayoutId() {
        return R.layout.activity_app_upgrade_setting;
    }

    @Override
    public void onInitView() {
        TextView txtvw = (TextView) findViewById(R.id.txtvw_title);
        txtvw.setText(R.string.app_upgrade_setting);
        etConnectTimeout = (EditText) findViewById(R.id.et_connect_timeout);
        cbWifiOnly = (CheckBox) findViewById(R.id.cb_wifi_only);
        cbAppUpgradeEnable = (CheckBox) findViewById(R.id.cb_upgrade_enable);
        btnUpgrade = (Button) findViewById(R.id.btn_upgrade);

        initViewContent();
    }

    private void initViewContent() {
        config = BusinessConfig.getInstance();
        etConnectTimeout.setText(String.valueOf(config.getNumber(this, SimpleStringTag.APP_UPGRADE_CONNECT_TIMEOUT)));
        cbWifiOnly.setChecked(config.getFlag(this, SimpleStringTag.TOGGLE_APP_UPGRADE_WIFI_ONLY));
        cbAppUpgradeEnable.setChecked(config.getFlag(this, SimpleStringTag.TOGGLE_APP_UPGRADE_SUPPORT));
    }

    public void onSureClick(View v) {
        if (v.getId() == R.id.btn_confirm) {
            String connectTimeout = etConnectTimeout.getText().toString();
            if (TextUtils.isEmpty(connectTimeout)) {
                ViewUtils.showToast(this, getString(R.string.label_connect_timeout) + getString(R.string.tip_empty));
                return;
            }
            config.setNumber(this, SimpleStringTag.APP_UPGRADE_CONNECT_TIMEOUT, Integer.parseInt(connectTimeout, 10));
            config.setFlag(this, SimpleStringTag.TOGGLE_APP_UPGRADE_WIFI_ONLY, cbWifiOnly.isChecked());
            config.setFlag(this, SimpleStringTag.TOGGLE_APP_UPGRADE_SUPPORT, cbAppUpgradeEnable.isChecked());
            ViewUtils.showToast(this, "设置成功");
            activityStack.pop();
        }else {
            if(AppUpgradeUtil.getInstance().init(this))
                btnUpgrade.setEnabled(false);
        }
    }
}
