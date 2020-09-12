package com.centerm.epos.activity.msn;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.epos.EposApplication;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseActivity;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.present.communication.ICommunication;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.ViewUtils;

import java.util.Set;

import config.BusinessConfig;

/**
 * 《基础版本》
 * 通讯参数设置页面
 * Created by linwenhui on 2016/11/3.
 */

public class BaseCommunicationSettingsActivity extends BaseActivity {

    private EditText service, port, tpdu;
    private EditText service_reserve, port_reserve;
    private CheckBox isCommEncrypt, mIsConnectAlive,mBoxmEncodeType,mBoxAddressReserve;

    @Override
    public int onLayoutId() {
        return R.layout.activity_configure_comms;
    }

    @Override
    public void onInitView() {
        TextView txtvw = (TextView) findViewById(R.id.txtvw_title);
        txtvw.setText(R.string.label_configure_comms);
        service = (EditText) findViewById(R.id.extxt_confiure_comms_service);
        service_reserve = (EditText) findViewById(R.id.service_reserve);
        port = (EditText) findViewById(R.id.extxt_confiure_comms_port);
        port_reserve = (EditText) findViewById(R.id.port_reserve);
        tpdu = (EditText) findViewById(R.id.extxt_confiure_comms_tpdu);
        service.addTextChangedListener(new LimitTextWatcher());
        service.setText(Settings.getCommonIp2(this));
        port.setText(Settings.getCommonPort2(this) + "");
        tpdu.setText(Settings.getCommonTPDU(this));
        isCommEncrypt = (CheckBox) findViewById(R.id.auto_sign_out);
        mIsConnectAlive = (CheckBox) findViewById(R.id.cb_connect_alive);
        mBoxmEncodeType = (CheckBox) findViewById(R.id.mBoxmEncodeType);
        mBoxAddressReserve = (CheckBox) findViewById(R.id.mBoxAddressReserve);

        if(TextUtils.isEmpty(BusinessConfig.getInstance().getValue(context, TransDataKey.key_address_reserve))){
            service_reserve.setText(CommonUtils.ADDRESS_RESERVE);
            port_reserve.setText(CommonUtils.PORT_RESERVE);
        }else {
            service_reserve.setText(BusinessConfig.getInstance().getValue(context, TransDataKey.key_address_reserve));
            port_reserve.setText(BusinessConfig.getInstance().getValue(context, TransDataKey.key_port_reserve));
        }
        mBoxAddressReserve.setChecked(BusinessConfig.getInstance().getFlag(this, BusinessConfig.Key.USE_REVERVE_COMMON));
    }

    @Override
    public void afterInitView() {
        super.afterInitView();
//        int type = Settings.getValue(EposApplication.getAppContext(), Settings.KEY.COMM_TYPE, ICommunication.COMM_HTTPS);
        isCommEncrypt.setChecked(Settings.isCommMsgEncrypt(EposApplication.getAppContext()));
        mIsConnectAlive.setChecked(BusinessConfig.getInstance().getFlag(this, BusinessConfig.Key.KEEP_CONNECT_ALIVE));
        mBoxmEncodeType.setChecked(BusinessConfig.getInstance().getFlag(this, BusinessConfig.Key.ENCODE_TYPE));
    }

    public void onSureClick(View v) {
        String tagService = service.getText().toString().trim().replace(" ", "");
        String tagService_reserve = service_reserve.getText().toString().trim().replace(" ", "");
        String tagPort = port.getText().toString().trim().replace(" ", "");
        String tagPort_reserve = port_reserve.getText().toString().trim().replace(" ", "");
        String tagTpdu = tpdu.getText().toString().trim();
        if (TextUtils.isEmpty(tagService)) {
            ViewUtils.showToast(this, R.string.tip_configure_comms_service_empty);
            return;
        }
        if (!CommonUtils.isIp(tagService)) {
            ViewUtils.showToast(this, R.string.tip_configure_comms_service_illegal);
            return;
        }
        if (TextUtils.isEmpty(tagPort)) {
            ViewUtils.showToast(this, R.string.tip_configure_comms_port_empty);
            return;
        }
        if (TextUtils.isEmpty(tagTpdu)) {
            ViewUtils.showToast(this, R.string.tip_configure_comms_tpdu_empty);
            return;
        }
        if (tagTpdu.length() != 10){
            ViewUtils.showToast(this, R.string.tip_configure_comms_tpdu_short);
            return;
        }
        if (!CommonUtils.isIp(tagService_reserve)) {
            ViewUtils.showToast(this, R.string.tip_configure_comms_service_illegal_reserve);
            return;
        }
        if (TextUtils.isEmpty(tagPort_reserve)) {
            ViewUtils.showToast(this, R.string.tip_configure_comms_port_empty_reserve);
            return;
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
        Settings.setCommonIp(this, tagService);
        Settings.setCommonPort(this, intPort);
        Settings.setCommonTPDU(this, tagTpdu);

        BusinessConfig.getInstance().setValue(context, TransDataKey.key_address_reserve, tagService_reserve);
        BusinessConfig.getInstance().setValue(context, TransDataKey.key_port_reserve, tagPort_reserve);
        BusinessConfig.getInstance().setFlag(this, BusinessConfig.Key.USE_REVERVE_COMMON, mBoxAddressReserve.isChecked());
//        if (isCommEncrypt.isChecked()) {
//            Settings.setValue(context, Settings.KEY.COMM_TYPE, ICommunication.COMM_HTTPS);
//        }
//        else {
//            Settings.setValue(context, Settings.KEY.COMM_TYPE, Settings.getDefaultCommType(context));
//        }
        Settings.setCommMsgEncrypt(context, isCommEncrypt.isChecked());
        BusinessConfig.getInstance().setFlag(this, BusinessConfig.Key.KEEP_CONNECT_ALIVE, mIsConnectAlive.isChecked());
        BusinessConfig.getInstance().setFlag(this, BusinessConfig.Key.ENCODE_TYPE, mBoxmEncodeType.isChecked());
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
