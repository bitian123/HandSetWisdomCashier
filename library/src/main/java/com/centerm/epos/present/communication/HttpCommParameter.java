package com.centerm.epos.present.communication;

import android.text.TextUtils;
import android.util.Log;

import com.centerm.epos.BuildConfig;
import com.centerm.epos.EposApplication;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.XLogUtil;

import config.BusinessConfig;
import config.Config;

/**
 * Created by yuhc on 2017/2/14.
 * HTTP通讯方式的通讯参数
 */

public class HttpCommParameter implements ICommunicationParameter {

    @Override
    public Object getConnectParameter() {
        StringBuilder builder = new StringBuilder("http://");
        if(BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), BusinessConfig.Key.SCAN_ENCODE_FLAG)){
            builder = new StringBuilder("https://");
        }

        //使用备用扫码地址
        if(BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), BusinessConfig.Key.USE_REVERVE)){
            if(TextUtils.isEmpty(BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), TransDataKey.key_scan_address_reserve))){
                builder.append(CommonUtils.ADDRESS_SCAN_RESERVE);
                builder.append(":");
                builder.append(CommonUtils.PORT_SCAN_RESERVE);
            }else {
                builder.append(BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), TransDataKey.key_scan_address_reserve));
                builder.append(":");
                builder.append(BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), TransDataKey.key_scan_port_reserve));
            }
        }else {
            if(TextUtils.isEmpty(BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), TransDataKey.key_scan_address))){
                builder.append(CommonUtils.ADDRESS_SCAN);
                builder.append(":");
                builder.append(CommonUtils.PORT_SCAN);
            }else {
                builder.append(BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), TransDataKey.key_scan_address));
                builder.append(":");
                builder.append(BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), TransDataKey.key_scan_port));
            }
        }
        XLogUtil.d("HttpCommParameter", builder.toString());
        return builder.toString();
    }

    @Override
    public Object getSendParameter() {
        return "POST";
    }

    @Override
    public Object getReceiveParameter() {
        return null;
    }

    @Override
    public Object getDisconnectParam() {
        return null;
    }

}
