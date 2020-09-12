package com.centerm.epos.present.communication;

import com.centerm.epos.EposApplication;
import com.centerm.epos.common.Settings;

/**
 * Created by yuhc on 2017/2/13.
 * TCP通讯参数
 */

public class TcpsCommParameter implements ICommunicationParameter {

    @Override
    public Object getConnectParameter() {
//        return new ConnectParameter("172.20.10.6", 9540, 10);
        return new ConnectParameter(Settings.getCommonIp(EposApplication.getAppContext()), Settings.getCommonPort
                (EposApplication.getAppContext()), 3, 2);
    }

    @Override
    public Object getSendParameter() {
        return "10";
    }

    @Override
    public Object getReceiveParameter() {
        return "60";
    }

    @Override
    public Object getDisconnectParam() {
        return null;
    }



    public class ConnectParameter {
        /**
         * 服务器IP
         */
        private String serverIP;

        /**
         * 服务器端口
         */
        private int serverPort;

        /**
         * 连接超时
         */
        private int connectTimeOutS;

        /**
         * 重新连接次数
         */
        private int retryTimes=0;

        public ConnectParameter() {
        }

        public ConnectParameter(String serverIP, int serverPort, int connectTimeOutS, int retryTimes) {
            this.serverIP = serverIP;
            this.serverPort = serverPort;
            this.connectTimeOutS = connectTimeOutS;
            this.retryTimes = retryTimes;
        }

        public ConnectParameter(String serverIP, int serverPort, int connectTimeOutS) {
            this.serverIP = serverIP;
            this.serverPort = serverPort;
            this.connectTimeOutS = connectTimeOutS;
        }

        public String getServerIP() {
            return serverIP;
        }

        public int getRetryTimes() {
            return retryTimes;
        }

        public void setRetryTimes(int retryTimes) {
            this.retryTimes = retryTimes;
        }

        public void setServerIP(String serverIP) {
            this.serverIP = serverIP;
        }

        public int getServerPort() {
            return serverPort;
        }

        public void setServerPort(int serverPort) {
            this.serverPort = serverPort;
        }

        public int getConnectTimeOutS() {
            return connectTimeOutS;
        }

        public void setConnectTimeOutS(int connectTimeOutS) {
            this.connectTimeOutS = connectTimeOutS;
        }
    }
}
