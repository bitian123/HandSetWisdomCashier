package com.centerm.epos.activity.msn;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseActivity;
import com.centerm.epos.utils.ViewUtils;

import config.BusinessConfig;

/**
 * 《基础版本》
 * 商户设置界面
 * Created by ysd on 2016/11/30.
 */
public class BaseECashExceptionSettingsActivity extends BaseActivity {

    private EditText etRetryTime;
    private EditText etRecordTime;
    private EditText etRecordMax;

    private String retryTimes;
    private String recordTimes;
    private String recordMax;


    @Override
    public int onLayoutId() {
        return R.layout.activity_ecash_err_setting;
    }

    @Override
    public void onInitView() {
        TextView txtvw = (TextView) findViewById(R.id.txtvw_title);
        txtvw.setText(R.string.label_ecash_error);
        etRetryTime = (EditText) findViewById(R.id.et_retry_timeout);
        etRecordTime = (EditText) findViewById(R.id.et_record_timeout);
        etRecordMax = (EditText) findViewById(R.id.et_record_max);
    }

    @Override
    public void afterInitView() {
        super.afterInitView();
        BusinessConfig config = BusinessConfig.getInstance();
        retryTimes = String.valueOf(config.getNumber(context, BusinessConfig.Key.ECASH_ERR_RETRY_TIMEOUT));
        recordTimes = String.valueOf(config.getNumber(context, BusinessConfig.Key.ECASH_ERR_RECORD_TIMEOUT));
        recordMax = String.valueOf(config.getNumber(context, BusinessConfig.Key.ECASH_ERR_RECORD_MAX));

        etRetryTime.setText(retryTimes);
        etRecordTime.setText(recordTimes);
        etRecordMax.setText(recordMax);
    }

    public void onSureClick(View v) {

        retryTimes = etRetryTime.getText().toString().trim();
        recordTimes = etRecordTime.getText().toString().trim();
        recordMax = etRecordMax.getText().toString().trim();

        if (TextUtils.isEmpty(retryTimes)) {
            ViewUtils.showToast(this, getString(R.string.label_ecash_retry_timeout) + getString(R.string.tip_empty));
            return;
        }
        if (TextUtils.isEmpty(recordTimes)) {
            ViewUtils.showToast(this, getString(R.string.label_ecash_error_record_timeout) + getString(R.string
                    .tip_empty));
            return;
        }
        if (TextUtils.isEmpty(recordMax)) {
            ViewUtils.showToast(this, getString(R.string.label_ecash_error_record_max) + getString(R.string.tip_empty));
            return;
        }

        BusinessConfig config = BusinessConfig.getInstance();
        config.setNumber(context, BusinessConfig.Key.ECASH_ERR_RETRY_TIMEOUT, Integer.parseInt(retryTimes, 10));
        config.setNumber(context, BusinessConfig.Key.ECASH_ERR_RECORD_TIMEOUT, Integer.parseInt(recordTimes, 10));
        config.setNumber(context, BusinessConfig.Key.ECASH_ERR_RECORD_MAX, Integer.parseInt(recordMax, 10));

        ViewUtils.showToast(context, "设置成功");
        activityStack.pop();
    }

}
