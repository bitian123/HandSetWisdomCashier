package com.centerm.epos.present.communication;

import android.text.TextUtils;

import com.centerm.epos.EposApplication;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.XLogUtil;

import config.BusinessConfig;

/**
 * Created by yuhc on 2017/2/14.
 * 绿城 HTTP通讯方式的通讯参数
 */

public class HttpCommParameterGT implements ICommunicationParameter {
    public String transCode = "";

    public HttpCommParameterGT(String t){
        transCode = t;
    }

    @Override
    public Object getConnectParameter() {
        StringBuilder builder = new StringBuilder();
        if(TextUtils.isEmpty(BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), TransDataKey.key_scan_address_gt))){
            builder.append(CommonUtils.ADDRESS_GT);
            if(!TextUtils.isEmpty(CommonUtils.PORT_GT)){
                builder.append(":");
                builder.append(CommonUtils.PORT_GT);
            }
        }else {
            builder.append(BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), TransDataKey.key_scan_address_gt));
            if(!TextUtils.isEmpty(BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), TransDataKey.key_scan_port_gt))){
                builder.append(":");
                builder.append(BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), TransDataKey.key_scan_port_gt));
            }
        }
        if(TransCode.fingerRegister.equals(transCode)||TransCode.fingerVerify.equals(transCode)){
            builder.append("/shdy_greentown/rest/finger/");
        }else {
            builder.append("/shdy_greentown/rest/forPos/");
        }
        builder.append(transCode);

        XLogUtil.d("HttpCommParameterGT", builder.toString());
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
