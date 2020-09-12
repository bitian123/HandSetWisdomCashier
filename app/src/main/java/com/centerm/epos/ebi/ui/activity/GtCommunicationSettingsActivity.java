package com.centerm.epos.ebi.ui.activity;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.epos.base.BaseActivity;
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

public class GtCommunicationSettingsActivity extends BaseActivity {

    private EditText service, port;
    private EditText service_reserve, port_reserve;

    @Override
    public int onLayoutId() {
        return R.layout.activity_configure_comms_gt;
    }

    @Override
    public void onInitView() {
        TextView txtvw = (TextView) findViewById(R.id.txtvw_title);
        txtvw.setText(com.centerm.epos.R.string.label_configure_comms_scan);
        service = (EditText) findViewById(R.id.extxt_confiure_comms_service);
        port = (EditText) findViewById(R.id.extxt_confiure_comms_port);
        service_reserve = (EditText) findViewById(R.id.service_reserve);
        port_reserve = (EditText) findViewById(R.id.port_reserve);

        if(TextUtils.isEmpty(BusinessConfig.getInstance().getValue(context, TransDataKey.key_scan_address_gt))){
            service.setText(CommonUtils.ADDRESS_GT);
            port.setText(CommonUtils.PORT_GT);
        }else {
            service.setText(BusinessConfig.getInstance().getValue(context, TransDataKey.key_scan_address_gt));
            port.setText(BusinessConfig.getInstance().getValue(context, TransDataKey.key_scan_port_gt));
        }
//        if(TextUtils.isEmpty(BusinessConfig.getInstance().getValue(context, TransDataKey.key_scan_address_reserve_gt))){
//            service_reserve.setText(CommonUtils.ADDRESS_GT);
//            port_reserve.setText(CommonUtils.PORT_GT);
//        }else {
//            service_reserve.setText(BusinessConfig.getInstance().getValue(context, TransDataKey.key_scan_address_reserve_gt));
//            port_reserve.setText(BusinessConfig.getInstance().getValue(context, TransDataKey.key_scan_port_reserve_gt));
//        }
    }

    public void onSureClick(View v) {
        String tagService = service.getText().toString().trim().replace(" ", "");
        if (TextUtils.isEmpty(tagService)) {
            ViewUtils.showToast(this, R.string.tip_configure_comms_service_empty);
            return;
        }
        String tagPort = port.getText().toString().trim().replace(" ", "");
        int intPort = 0;
        try {
            intPort = Integer.parseInt(tagPort);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
//        if (intPort < 0 || intPort > 65535){
//            ViewUtils.showToast(this, R.string.tip_configure_comms_port_illegal);
//            return;
//        }

//        String tagService2 = service_reserve.getText().toString().trim().replace(" ", "");
//        String tagPort2 = port_reserve.getText().toString().trim().replace(" ", "");
//        if (TextUtils.isEmpty(tagService2)) {
//            ViewUtils.showToast(this, R.string.tip_configure_comms_service_illegal_reserve);
//            return;
//        }
//
//        int intPort2 = 0;
//        try {
//            intPort2 = Integer.parseInt(tagPort2);
//        } catch (NumberFormatException e) {
//            e.printStackTrace();
//        }
//        if (intPort2 < 0 || intPort2 > 65535){
//            ViewUtils.showToast(this, R.string.tip_configure_comms_port_illegal);
//            return;
//        }

        BusinessConfig.getInstance().setValue(context, TransDataKey.key_scan_address_gt, tagService);
        if(intPort!=0) {
            BusinessConfig.getInstance().setValue(context, TransDataKey.key_scan_port_gt, intPort + "");
        }else {
            BusinessConfig.getInstance().setValue(context, TransDataKey.key_scan_port_gt, "");
        }
        //BusinessConfig.getInstance().setValue(context, TransDataKey.key_scan_address_reserve_gt, tagService2);
        //BusinessConfig.getInstance().setValue(context, TransDataKey.key_scan_port_reserve_gt, intPort2+"");
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
