package com.centerm.epos.activity.msn;

import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.define.ISystemService;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseActivity;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.utils.XLogUtil;
import com.centerm.epos.view.AlertDialog;

import config.BusinessConfig;

/**
 * 《基础版本》
 * 通讯参数设置页面
 * Created by linwenhui on 2016/11/3.
 */

public class BaseNetSettingsActivity extends BaseActivity {

    private static final String TAG = BaseNetSettingsActivity.class.getSimpleName();

    private EditText etAPNAccess, etAPNUserName, etAPNUserPwd;
    private CheckBox cbAPNEnable;
    private BusinessConfig config;
    ISystemService systemService;

    public BaseNetSettingsActivity() {
        config = BusinessConfig.getInstance();
        try {
            systemService = DeviceFactory.getInstance().getSystemDev();
        } catch (Exception e) {
            XLogUtil.i(TAG, " ^_^" + e.getMessage() + " ^_^");
        }
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_configure_net;
    }

    @Override
    public void onInitView() {
        TextView txtvw = (TextView) findViewById(R.id.txtvw_title);
        txtvw.setText(R.string.label_configure_net);
        etAPNAccess = (EditText) findViewById(R.id.et_apn_access);
        etAPNUserName = (EditText) findViewById(R.id.et_apn_username);
        etAPNUserPwd = (EditText) findViewById(R.id.et_apn_password);

        cbAPNEnable = (CheckBox) findViewById(R.id.et_apn_enable);
    }

    @Override
    public void afterInitView() {
        super.afterInitView();
        etAPNAccess.setText(config.getValue(this, BusinessConfig.Key.APN_ACCESS));
        etAPNUserName.setText(config.getValue(this, BusinessConfig.Key.APN_USER_NAME));
        etAPNUserPwd.setText(config.getValue(this, BusinessConfig.Key.APN_USER_PASSWORD));

        final boolean isEnable = BusinessConfig.getInstance().getFlag(this, BusinessConfig.Key.APN_ENABLE);
        changeViewState(isEnable);

        cbAPNEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeViewState(!isEnable);
            }
        });
    }

    private void changeViewState(boolean isEnable) {
        cbAPNEnable.setChecked(isEnable);
        etAPNAccess.setEnabled(isEnable);
        etAPNUserName.setEnabled(isEnable);
        etAPNUserPwd.setEnabled(isEnable);
    }

    public void onSureClick(View v) {
        final String apnAccess = etAPNAccess.getText().toString().trim();
        final String apnUserName = etAPNUserName.getText().toString().trim();
        final String apnUserPwd = etAPNUserPwd.getText().toString().trim();
        if (TextUtils.isEmpty(apnAccess)) {
            ViewUtils.showToast(this, getString(R.string.label_configure_apn_access) + getString(R.string.tip_empty));
            return;
        }

        config.setValue(this, BusinessConfig.Key.APN_USER_NAME, apnUserName);
        config.setValue(this, BusinessConfig.Key.APN_ACCESS, apnAccess);
        config.setValue(this, BusinessConfig.Key.APN_USER_PASSWORD, apnUserPwd);
        config.setFlag(this, BusinessConfig.Key.APN_ENABLE, cbAPNEnable.isChecked());
        ViewUtils.showToast(context, "设置成功");
        DialogFactory.showSelectDialog(this, "提示", "是否立即切换网络", new AlertDialog.ButtonClickListener() {
            @Override
            public void onClick(AlertDialog.ButtonType button, View v) {
                if (button == AlertDialog.ButtonType.POSITIVE) {
                    if(systemService != null) {
                        int[] apnIds = systemService.getAPNids(apnAccess);
                        if (apnIds != null && apnIds.length > 0)
                            systemService.deleteAPN(apnIds[0]);
                        systemService.addAPN(apnAccess, apnUserName, apnUserPwd);
                        apnIds = systemService.getAPNids(apnAccess);
                        if (apnIds != null && apnIds.length > 0) {
                            systemService.setAPN(apnIds[0]);
                            ViewUtils.showToast(context, "网络切换成功");
                            return;
                        }
                    }
                    ViewUtils.showToast(context, "网络切换失败");
                }
                activityStack.pop();
            }
        });
    }

    /**
     * 检测SIM卡是否存在，没插卡就不让进了。通过读取IMSI号进行判断
     * @return  true 检测到SIM卡    false 未检测到
     */
    public static boolean checkEnvironment() {
        try {
            ISystemService systemService = DeviceFactory.getInstance().getSystemDev();
            return !TextUtils.isEmpty(systemService.getIMSI());
        } catch (Exception e) {
            XLogUtil.i(TAG, " ^_^" + e.getMessage() + " ^_^");
        }
        return false;
    }

}
