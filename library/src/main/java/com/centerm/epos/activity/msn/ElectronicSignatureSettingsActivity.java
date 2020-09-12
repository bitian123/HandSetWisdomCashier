package com.centerm.epos.activity.msn;

import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseActivity;
import com.centerm.epos.base.SimpleStringTag;
import com.centerm.epos.utils.ViewUtils;

import config.BusinessConfig;

/**
 * 《基础版本》
 * 通讯参数设置页面
 * Created by linwenhui on 2016/11/3.
 */

public class ElectronicSignatureSettingsActivity extends BaseActivity {

    private EditText etPackageMax, etTimeOut, etRetryMax, etStoreMax;
    private CheckBox cbEsignOpen, cbMultiPackage, cbSignPadInner, cbPhoneNumber;

    @Override
    public int onLayoutId() {
        return R.layout.activity_configure_esign;
    }

    @Override
    public void onInitView() {
        TextView txtvw = (TextView) findViewById(R.id.txtvw_title);
        txtvw.setText(R.string.label_configure_esign);
        etPackageMax = (EditText) findViewById(R.id.et_esign_package_max);
        etTimeOut = (EditText) findViewById(R.id.et_sign_timeout);
        etRetryMax = (EditText) findViewById(R.id.et_retry_max);
        etStoreMax = (EditText) findViewById(R.id.et_store_max);

        cbEsignOpen = (CheckBox) findViewById(R.id.cb_esign_open);
        cbMultiPackage = (CheckBox) findViewById(R.id.cb_esign_multi_package);
        cbSignPadInner = (CheckBox) findViewById(R.id.cb_signpad_inner);
        cbPhoneNumber = (CheckBox) findViewById(R.id.cb_phone_num);
    }

    @Override
    public void afterInitView() {
        super.afterInitView();
        BusinessConfig config = BusinessConfig.getInstance();

        etPackageMax.setText(String.valueOf(config.getNumber(this, SimpleStringTag.ESIGN_PACKAGE_LEN_MAX)));
        etTimeOut.setText(String.valueOf(config.getNumber(this, SimpleStringTag.ESIGN_INPUT_TIMEOUT)));
        etRetryMax.setText(String.valueOf(config.getNumber(this, SimpleStringTag.ESIGN_BATCH_REQ_RETRY_TIMES)));
        etStoreMax.setText(String.valueOf(config.getNumber(this, SimpleStringTag.ESIGN_STORE_MAX)));

        cbEsignOpen.setChecked(config.getToggle(this, SimpleStringTag.TOGGLE_ESIGN_SUPPORT));
        cbMultiPackage.setChecked(config.getFlag(this, SimpleStringTag.TOGGLE_ESIGN_MUL_PACKAGE));
        cbSignPadInner.setChecked(config.getToggle(this, SimpleStringTag.TOGGLE_SIGN_PAD_INNER));
        cbPhoneNumber.setChecked(config.getToggle(this, SimpleStringTag.TOGGLE_ESIGN_PHONE_NUMBER));
    }

    public void onSureClick(View v) {
        String tagPackageMax = etPackageMax.getText().toString().trim().replace(" ", "");
        String tagTimeOut = etTimeOut.getText().toString().trim().replace(" ", "");
        String tagRetryMax = etRetryMax.getText().toString().trim();
        String tagStoreMax = etStoreMax.getText().toString().trim();

        if (TextUtils.isEmpty(tagPackageMax)) {
            ViewUtils.showToast(this, R.string.tip_esign_multi_package_null);
            return;
        }
        if (TextUtils.isEmpty(tagTimeOut)) {
            ViewUtils.showToast(this, R.string.tip_sign_timeout_null);
            return;
        }
        if (TextUtils.isEmpty(tagRetryMax)) {
            ViewUtils.showToast(this, R.string.tip_request_time_max_null);
            return;
        }
        if (TextUtils.isEmpty(tagStoreMax)) {
            ViewUtils.showToast(this, R.string.tip_esign_store_max_null);
            return;
        }

        BusinessConfig config = BusinessConfig.getInstance();

        config.setNumber(this, SimpleStringTag.ESIGN_PACKAGE_LEN_MAX, Integer.parseInt(tagPackageMax));
        config.setNumber(this, SimpleStringTag.ESIGN_INPUT_TIMEOUT, Integer.parseInt(tagTimeOut));
        config.setNumber(this, SimpleStringTag.ESIGN_BATCH_REQ_RETRY_TIMES, Integer.parseInt(tagRetryMax));
        config.setNumber(this, SimpleStringTag.ESIGN_STORE_MAX, Integer.parseInt(tagStoreMax));

        config.setFlag(this, SimpleStringTag.TOGGLE_ESIGN_SUPPORT, cbEsignOpen.isChecked());
        config.setFlag(this, SimpleStringTag.TOGGLE_ESIGN_MUL_PACKAGE, cbMultiPackage.isChecked());
        config.setFlag(this, SimpleStringTag.TOGGLE_SIGN_PAD_INNER, cbSignPadInner.isChecked());
        config.setFlag(this, SimpleStringTag.TOGGLE_ESIGN_PHONE_NUMBER, cbPhoneNumber.isChecked());

        ViewUtils.showToast(context, "设置成功");
        activityStack.pop();
    }

}
