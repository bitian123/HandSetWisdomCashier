package com.centerm.epos.activity.msn;

import android.graphics.ColorFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseActivity;
import com.centerm.epos.common.Settings;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.xml.bean.PreferDataPool;
import com.centerm.epos.xml.keys.Keys;

import config.BusinessConfig;
import config.Config;

/**
 * 《基础版本》
 * 交易设置界面
 * Created by ysd on 2016/11/30.
 */
public class BaseTradeSettingsActivity extends BaseActivity {

    private EditText reSendTime, etTrdeViewOpTimeout, etSlipTitle;
    private CheckBox isAutoSignOut, cbAutoPrintDetails, cbSlipTitleDefault;
    private LinearLayout llSlipTitle;
    private BusinessConfig config;

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        config = BusinessConfig.getInstance();
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_transation_setting;
    }

    @Override
    public void onInitView() {
        TextView txtvw = (TextView) findViewById(R.id.txtvw_title);
        txtvw.setText(R.string.label_transation);
        Button modifyTrans = holdView();
        modifyTrans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reSendTimeStr = reSendTime.getText().toString().trim();
                String viewOpTimeout = etTrdeViewOpTimeout.getText().toString().trim();
                String slipTitle = etSlipTitle.getText().toString().trim();
                boolean isSlipTitleDefault = cbSlipTitleDefault.isChecked();
                int reSendTimes = Integer.parseInt(reSendTimeStr);
                if ("".equals(reSendTimeStr)) {
                    ViewUtils.showToast(context, getString(R.string.tip_resend_msg_count));
                    return;
                }
                if ("".equals(viewOpTimeout)) {
                    ViewUtils.showToast(context, getString(R.string.tip_trade_view_op_timeout));
                    return;
                }
                if (!isSlipTitleDefault && TextUtils.isEmpty(slipTitle)){
                    ViewUtils.showToast(context, getString(R.string.tip_slip_title_not_null));
                    return;
                }
                if (reSendTimes < 1 || reSendTimes > 3) {
                    ViewUtils.showToast(context, getString(R.string.tip_resend_msg_times));
                    return;
                }
                config.setNumber(context, BusinessConfig.Key.KEY_MAX_MESSAGE_RETRY_TIMES, Integer.parseInt(reSendTimeStr));
                config.setNumber(context, BusinessConfig.Key.KEY_TRADE_VIEW_OP_TIMEOUT, Integer.parseInt(viewOpTimeout));
                config.setFlag(context, BusinessConfig.Key.FLAG_AUTO_SIGN_OUT, isAutoSignOut.isChecked());
                config.setFlag(context, BusinessConfig.Key.TOGGLE_AUTO_PRINT_DETAILS, cbAutoPrintDetails.isChecked());
                config.setFlag(context, BusinessConfig.Key.TOGGLE_SLIP_TITLE_DEFAULT, isSlipTitleDefault);
                if (!isSlipTitleDefault){
                    config.setValue(context, BusinessConfig.Key.KEY_SLIP_TITLE_CONTENT, slipTitle);
                }
                ViewUtils.showToast(context, getString(R.string.tip_save_success));
                activityStack.pop();
            }
        });
        cbSlipTitleDefault.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked){
                    llSlipTitle.setVisibility(View.VISIBLE);
                }else
                    llSlipTitle.setVisibility(View.GONE);
            }
        });
    }

    private Button holdView() {
        reSendTime = (EditText) findViewById(R.id.extxt_resend_time);
        etTrdeViewOpTimeout = (EditText) findViewById(R.id.extxt_trade_view_timeout);
        etSlipTitle = (EditText) findViewById(R.id.extxt_slip_title);
        isAutoSignOut = (CheckBox) findViewById(R.id.auto_sign_out);
        cbAutoPrintDetails = (CheckBox) findViewById(R.id.auto_print_details);
        cbSlipTitleDefault = (CheckBox) findViewById(R.id.cb_slip_title_default);
        llSlipTitle = (LinearLayout) findViewById(R.id.ll_slip_title);
        return (Button) findViewById(R.id.modify_trans);
    }

    @Override
    public void afterInitView() {
        super.afterInitView();
        int retryCount = config.getNumber(context, BusinessConfig.Key.KEY_MAX_MESSAGE_RETRY_TIMES);
        int timeout = config.getNumber(context, BusinessConfig.Key.KEY_TRADE_VIEW_OP_TIMEOUT);
        boolean autoSignOut = config.getFlag(context, BusinessConfig.Key.FLAG_AUTO_SIGN_OUT);
        boolean autoPrintDetails = config.getToggle(context, BusinessConfig.Key.TOGGLE_AUTO_PRINT_DETAILS);
        boolean isDefault = config.getToggle(context, BusinessConfig.Key.TOGGLE_SLIP_TITLE_DEFAULT);

        reSendTime.setText(String.valueOf(retryCount));
        etTrdeViewOpTimeout.setText(String.valueOf(timeout));
        isAutoSignOut.setChecked(autoSignOut);
        cbAutoPrintDetails.setChecked(autoPrintDetails);
        cbSlipTitleDefault.setChecked(isDefault);
        if (!isDefault){
            llSlipTitle.setVisibility(View.VISIBLE);
            String titleContent = config.getValue(context, BusinessConfig.Key.KEY_SLIP_TITLE_CONTENT);
            etSlipTitle.setText(titleContent);
        }
    }
}
