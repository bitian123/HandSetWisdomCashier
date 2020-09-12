package com.centerm.epos.activity.msn;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.centerm.epos.EposApplication;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseActivity;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.xml.bean.PreferDataPool;

import java.util.Locale;

import config.BusinessConfig;

/**
 * 《基础版本》
 * 交易设置界面
 * Created by ysd on 2016/11/30.
 */
public class BaseTradeOtherControlActivity extends BaseActivity {

    private EditText etRefundAmountMax,extxt_trade_time;
    private CheckBox cbManualCardNo;
    private CheckBox cbMasterPwd,cb_upload_apn_info,cb_print_document;
    private BusinessConfig config;

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        config = BusinessConfig.getInstance();
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_other_control_setting;
    }

    @Override
    public void onInitView() {
        TextView txtvw = (TextView) findViewById(R.id.txtvw_title);
        txtvw.setText(R.string.label_other_trade_control);
        etRefundAmountMax = (EditText) findViewById(R.id.extxt_refund_max);
        extxt_trade_time = (EditText) findViewById(R.id.extxt_trade_time);
        cbManualCardNo = (CheckBox) findViewById(R.id.cb_manual_card_no);
        cbMasterPwd = (CheckBox) findViewById(R.id.cb_master_pwd);
        cb_upload_apn_info = (CheckBox) findViewById(R.id.cb_upload_apn_info);
        cb_print_document = (CheckBox) findViewById(R.id.cb_print_document);
        Button modifyTrans = (Button) findViewById(R.id.modify_trans);
        modifyTrans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String refundAmountMax = etRefundAmountMax.getText().toString().trim();
                String amount = formatMoney(refundAmountMax);
                if (TextUtils.isEmpty(amount)){
                    Toast.makeText(context, "请输入正确的退货金额!", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    int day = Integer.parseInt(extxt_trade_time.getText().toString());
                    if(day<=0){
                        Toast.makeText(context, "请输入正确的交易记录保存时间", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    config.setNumber(context, BusinessConfig.Key.TRADE_KEEP_DAY, day);
                }catch (Exception e){
                    Toast.makeText(context, "请输入正确的交易记录保存时间", Toast.LENGTH_SHORT).show();
                    return;
                }
                config.setValue(context, BusinessConfig.Key.REFUND_AMOUNT_LIMITED, amount);
                config.setFlag(context, BusinessConfig.Key.TOGGLE_MASTER_PWD_INPUT, cbMasterPwd.isChecked());
                config.setFlag(context, BusinessConfig.Key.TOGGLE_UPLOAD_BASE_STATION, cb_upload_apn_info.isChecked());
                config.setFlag(context, BusinessConfig.Key.TOGGLE_PRINT_DOCUMENT, cb_print_document.isChecked());
                ViewUtils.showToast(context, getString(R.string.tip_save_success));
                activityStack.pop();
            }
        });
    }

    @Override
    public void afterInitView() {
        super.afterInitView();
        String refundMax = config.getValue(context, BusinessConfig.Key.REFUND_AMOUNT_LIMITED);
        boolean isManualCardNo = config.getFlag(context, BusinessConfig.Key.TOGGLE_CARD_NUM_BY_HAND);
        boolean isMasterPwd = config.getFlag(context, BusinessConfig.Key.TOGGLE_MASTER_PWD_INPUT);
        boolean isUploadApnInfo = config.getToggle(context, BusinessConfig.Key.TOGGLE_UPLOAD_BASE_STATION);
        boolean isPrintDocument = config.getToggle(context, BusinessConfig.Key.TOGGLE_PRINT_DOCUMENT);
        int day = config.getNumber(context, BusinessConfig.Key.TRADE_KEEP_DAY);
        etRefundAmountMax.setText(refundMax);
        extxt_trade_time.setText(day+"");
        cbManualCardNo.setChecked(isManualCardNo);
        cbMasterPwd.setChecked(isMasterPwd);
        cb_upload_apn_info.setChecked(isUploadApnInfo);
        cb_print_document.setChecked(isPrintDocument);
    }

    private String formatMoney(String etContent) {
        if (TextUtils.isEmpty(etContent))
            return etContent;
        double moneyDouble;
        try {
            moneyDouble = Double.parseDouble(etContent);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
        return String.format(Locale.CHINA, "%.2f", moneyDouble);
    }
}
