package com.centerm.epos.ebi.ui.activity;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.epos.EposApplication;
import com.centerm.epos.base.BaseActivity;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.ebi.R;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.ViewUtils;

import config.BusinessConfig;

/**
 * 《基础版本》
 * 通讯参数设置页面
 * Created by linwenhui on 2016/11/3.
 */

public class ScanCommunicationSettingsActivity extends BaseActivity {

    private EditText service, port, sn;
    private EditText service_reserve, port_reserve;
    private CheckBox isSetSnHand,encodeFlag,proEnr,useReserve;

    @Override
    public int onLayoutId() {
        return R.layout.activity_configure_comms_scan;
    }

    @Override
    public void onInitView() {
        TextView txtvw = (TextView) findViewById(R.id.txtvw_title);
        txtvw.setText(com.centerm.epos.R.string.label_configure_comms_scan);
        service = (EditText) findViewById(R.id.extxt_confiure_comms_service);
        service_reserve = (EditText) findViewById(R.id.extxt_confiure_comms_service_reserve);
        port = (EditText) findViewById(R.id.extxt_confiure_comms_port);
        port_reserve = (EditText) findViewById(R.id.extxt_confiure_comms_port_reserve);
        sn = (EditText) findViewById(R.id.extxt_confiure_comms_sn);
        isSetSnHand = (CheckBox) findViewById(R.id.set_sn_hand);
        encodeFlag = (CheckBox) findViewById(R.id.encodeFlag);
        proEnr = (CheckBox) findViewById(R.id.proEnr);
        useReserve = (CheckBox) findViewById(R.id.use_comms_reserve);
        isSetSnHand.setChecked(BusinessConfig.getInstance().getFlag(this, BusinessConfig.Key.SET_SN_HAND));
        encodeFlag.setChecked(BusinessConfig.getInstance().getFlag(this, BusinessConfig.Key.SCAN_ENCODE_FLAG));
        proEnr.setChecked(BusinessConfig.getInstance().getFlag(this, BusinessConfig.Key.PRO_ENR));
        useReserve.setChecked(BusinessConfig.getInstance().getFlag(this, BusinessConfig.Key.USE_REVERVE));
        //service.addTextChangedListener(new LimitTextWatcher());
        if(TextUtils.isEmpty(BusinessConfig.getInstance().getValue(context, TransDataKey.key_scan_address))){
            service.setText(CommonUtils.ADDRESS_SCAN);
            port.setText(CommonUtils.PORT_SCAN);
        }else {
            service.setText(BusinessConfig.getInstance().getValue(context, TransDataKey.key_scan_address));
            port.setText(BusinessConfig.getInstance().getValue(context, TransDataKey.key_scan_port));
        }
        if(TextUtils.isEmpty(BusinessConfig.getInstance().getValue(context, TransDataKey.key_scan_address_reserve))){
            service_reserve.setText(CommonUtils.ADDRESS_SCAN_RESERVE);
            port_reserve.setText(CommonUtils.PORT_SCAN_RESERVE);
        }else {
            service_reserve.setText(BusinessConfig.getInstance().getValue(context, TransDataKey.key_scan_address_reserve));
            port_reserve.setText(BusinessConfig.getInstance().getValue(context, TransDataKey.key_scan_port_reserve));
        }
        if(!TextUtils.isEmpty(CommonUtils.SN_CODE)){
            sn.setText(CommonUtils.SN_CODE);
            isSetSnHand.setChecked(true);
        }else {
            isSetSnHand.setChecked(false);
        }

    }

    public void onSureClick(View v) {
        String tagService = service.getText().toString().trim().replace(" ", "");
        String tagServiceReserve = service_reserve.getText().toString().trim().replace(" ", "");
        String tagPort = port.getText().toString().trim().replace(" ", "");
        String tagPorttagServiceReserve = port_reserve.getText().toString().trim().replace(" ", "");
        String snStr = sn.getText().toString().trim().replace(" ", "");
        if (TextUtils.isEmpty(tagService)) {
            ViewUtils.showToast(this, R.string.tip_configure_comms_service_empty);
            return;
        }
        if (!CommonUtils.isIp(tagService)) {
//            ViewUtils.showToast(this, R.string.tip_configure_comms_service_illegal);
//            return;
        }
        if (TextUtils.isEmpty(snStr)) {
//            ViewUtils.showToast(this, R.string.tip_sn_not_null);
//            return;
        }

        int intPort = 0;
        try {
            intPort = Integer.parseInt(tagPort);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (intPort < 0 || intPort > 65535){
            ViewUtils.showToast(this, R.string.tip_configure_comms_port_illegal);
            return;
        }
        BusinessConfig.getInstance().setValue(context, TransDataKey.key_scan_address, tagService);
        BusinessConfig.getInstance().setValue(context, TransDataKey.key_scan_address_reserve, tagServiceReserve);
        BusinessConfig.getInstance().setValue(context, TransDataKey.key_scan_port, intPort+"");
        BusinessConfig.getInstance().setValue(context, TransDataKey.key_scan_port_reserve, tagPorttagServiceReserve);
        BusinessConfig.getInstance().setValue(context, TransDataKey.key_sn, snStr);
        BusinessConfig.getInstance().setFlag(this, BusinessConfig.Key.SET_SN_HAND, isSetSnHand.isChecked());
        BusinessConfig.getInstance().setFlag(this, BusinessConfig.Key.SCAN_ENCODE_FLAG, encodeFlag.isChecked());
        BusinessConfig.getInstance().setFlag(this, BusinessConfig.Key.PRO_ENR, proEnr.isChecked());
        BusinessConfig.getInstance().setFlag(this, BusinessConfig.Key.USE_REVERVE, useReserve.isChecked());
        ViewUtils.showToast(context, "设置成功");
        activityStack.pop();
    }

    private class LimitTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString();
            int index = service.getSelectionStart();
            if (index >= 2) {
                if (text.charAt(index - 1) == text.charAt(index - 2) && text.charAt(index - 1) == '.') {
                    text = text.substring(0, index - 1);
                    service.setText(text);
                    service.setSelection(index - 1);
                } else {
                    int pos = text.length() - text.lastIndexOf(".") - 1;
                    if (pos > 3) {
                        text = text.substring(0, index - 1);
                        service.setText(text);
                        service.setSelection(index - 1);
                    }
                }
            }
        }
    }

}
