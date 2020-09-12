package com.centerm.epos.present.communication;

import android.content.Context;

import com.centerm.epos.EposApplication;
import com.centerm.epos.common.Settings;
import com.centerm.epos.configure.ConfigureManager;

/**
 * Created by yuhc on 2017/4/8.
 */

public class DataExchangerFactory {

    public static DataExchanger getInstance() {
        Context appContext = EposApplication.getAppContext();
        int type = Settings.getCommType(appContext);
        if (Settings.isCommMsgEncrypt(appContext))
            type = ICommunication.COMM_HTTPS;

        switch (type) {
            case ICommunication.COMM_TCP:
                return getDataExchagerObj(ICommunication.COMM_TCP, new TcpCommParameter());
            case ICommunication.COMM_HTTP:
                return getDataExchagerObj(ICommunication.COMM_HTTP, new HttpCommParameter());
            case ICommunication.COMM_UART:
                return getDataExchagerObj(ICommunication.COMM_UART, new UartCommParameter());
            case ICommunication.COMM_HTTPS:
                ICommunicationParameter parameter = (ICommunicationParameter) ConfigureManager.getInstance
                        (appContext).getSubPrjClassInstance(HttpsCommParameter.class);
                if (parameter != null) {
                    return getDataExchagerObj(ICommunication.COMM_HTTPS, parameter);
                }
                return getDataExchagerObj(ICommunication.COMM_HTTPS, new HttpsCommParameter());
            case ICommunication.COMM_TCPS:
                ICommunicationParameter parameter1 = (ICommunicationParameter) ConfigureManager.getInstance
                        (appContext).getSubPrjClassInstance(TcpsCommParameter.class);
                if (parameter1 != null) {
                    return getDataExchagerObj(ICommunication.COMM_TCPS, parameter1);
                }
                return getDataExchagerObj(ICommunication.COMM_TCPS, new TcpsCommParameter());
        }
        return null;
    }

    private static DataExchanger getDataExchagerObj(int mCommType, ICommunicationParameter parameterCallback) {
        DataExchanger dataExchanger = (DataExchanger) ConfigureManager.getInstance
                (EposApplication.getAppContext()).getSubPrjClassInstance(DataExchangerImp.class);
        if (null != dataExchanger) {
            dataExchanger.init(mCommType, parameterCallback);
            return dataExchanger;
        }
        return new DataExchangerImp(mCommType, parameterCallback);
    }
}
