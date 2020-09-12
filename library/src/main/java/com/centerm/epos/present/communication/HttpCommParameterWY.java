package com.centerm.epos.present.communication;

import android.text.TextUtils;
import android.util.Log;

import com.centerm.epos.EposApplication;
import com.centerm.epos.utils.XLogUtil;

import config.BusinessConfig;
import config.Config;

/**
 * Created by yuhc on 2017/2/14.
 * HTTP通讯方式的通讯参数-物业
 */

public class HttpCommParameterWY implements ICommunicationParameter {
    @Override
    public Object getConnectParameter() {
        String url = BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), BusinessConfig.Key.WY_NOTICE_ADDRESS);
        Log.e("===", "url -> "+url);
        if(TextUtils.isEmpty(url)){
            if(BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), BusinessConfig.Key.PRO_ENR)){
                url = Config.WY_NOTICE_ADDRESS_PRO;
            }else {
                url = Config.WY_NOTICE_ADDRESS_TEST;
            }
        }
        XLogUtil.d("HttpCommParameter", url);
        return url;
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
