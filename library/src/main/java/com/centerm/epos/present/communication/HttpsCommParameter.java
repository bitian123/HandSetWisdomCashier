package com.centerm.epos.present.communication;

import com.centerm.epos.EposApplication;
import com.centerm.epos.common.Settings;

/**
 * Created by yuhc on 2017/2/14.
 * HTTP通讯方式的通讯参数
 */

public class HttpsCommParameter implements ICommunicationParameter {
    @Override
    public Object getConnectParameter() {
        return "https://"+ Settings.getCommonIp(EposApplication.getAppContext())+":" + Settings.getCommonPort
                (EposApplication.getAppContext());
    }

    @Override
    public Object getSendParameter() {
        return null;
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
