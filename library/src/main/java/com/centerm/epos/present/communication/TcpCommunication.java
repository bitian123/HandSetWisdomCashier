package com.centerm.epos.present.communication;

import android.content.Context;
import android.os.CountDownTimer;
import android.text.TextUtils;

import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;
import com.centerm.epos.EposApplication;
import com.centerm.epos.base.BaseRuntimeException;
import com.centerm.epos.common.Settings;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.redevelop.ITCPIsReceivedOver;
import com.centerm.epos.redevelop.UnionPayReceivedOver;
import com.centerm.epos.utils.XLogUtil;
import com.centerm.epos.xml.bean.RedevelopItem;
import com.centerm.epos.xml.keys.Keys;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import config.BusinessConfig;

/**
 * Created by yuhc on 2017/2/13.
 * 采用TCP连接方式的数据交互。此模块需要放到单独的线程中执行。
 */

public class TcpCommunication implements ICommunication {

    private static final String TAG = TcpCommunication.class.getSimpleName();

    //客户端
    Socket mClientSocket = null;
    //通讯参数
    private TcpCommParameter mCommunicationParameter;
    //超时控制标志
    private boolean isTimeout = false;

    public TcpCommunication(ICommunicationParameter mCommunicationParameter) {
        this.mCommunicationParameter = (TcpCommParameter) mCommunicationParameter;
    }

    /**
     * 连接服务器
     *
     * @return true 成功
     */
    @Override
    public Boolean connect() {
        if (mClientSocket != null) {
            XLogUtil.d(TAG, "^_^ 服务器已连接！^_^");
            return true;
        }

        TcpCommParameter.ConnectParameter connectParameter = (TcpCommParameter.ConnectParameter)
                mCommunicationParameter.getConnectParameter();
        XLogUtil.d(TAG, "^_^ 服务器IP:" + connectParameter.getServerIP() + " PORT:" + connectParameter.getServerPort() +
                " ^_^");
        int retryCount = connectParameter.getRetryTimes();
        do {

            try {
                mClientSocket = new Socket();
                mClientSocket.setTcpNoDelay(true);
                SocketAddress socketAddress = new InetSocketAddress(connectParameter.getServerIP(), connectParameter
                        .getServerPort());
                mClientSocket.connect(socketAddress, connectParameter.getConnectTimeOutS() * 1000);
                mClientSocket.setSoTimeout(connectParameter.getConnectTimeOutS() * 1000);
                break;
            } catch (IOException e) {
                XLogUtil.e(TAG, "^_^ " + e.getMessage() + " ^_^");
                exchangeIpAddress();
                connectParameter.setServerIP(Settings.getCommonIp(EposApplication.getAppContext()));
                connectParameter.setServerPort(Settings.getCommonPort(EposApplication.getAppContext()));
            }
        } while (retryCount-- > 0);
        if (retryCount > 0)
            return true;
        mClientSocket = null;
        return false;
    }

    @Override
    public int sendData(byte[] data) {
        if (mClientSocket == null) {
            XLogUtil.e(TAG, "^_^ 服务器未连接！^_^");
            return -1;
        }
        if (mClientSocket.isOutputShutdown()) {
            XLogUtil.e(TAG, "^_^ 数据发送已被关闭！^_^");
            return -2;
        }
        if (data == null || data.length == 0) {
            XLogUtil.e(TAG, "^_^ 待发送的数据为空 ^_^");
            return 0;
        }
        try {
            OutputStream os = mClientSocket.getOutputStream();
            os.write(data);
            os.flush();
        } catch (IOException e) {
            XLogUtil.e(TAG, "^_^ " + e.getMessage() + " ^_^");
            return -3;
        }
        XLogUtil.d(TAG, "^_^ 发送数据：" + HexUtils.bytesToHexString(data));
        return data.length;
    }

    @Override
    public byte[] receivedData(int requestLen) {
        if (mClientSocket == null) {
            XLogUtil.e(TAG, "^_^ 服务器未连接！^_^");
            throw new BaseRuntimeException(-1, "服务器未连接！");
        }
        if (mClientSocket.isInputShutdown()) {
            XLogUtil.e(TAG, "^_^ 服务器已关闭数据发送！^_^");
            throw new BaseRuntimeException(-2, "服务器已关闭数据发送！");
        }

        //获取接收数据的超时时间
        String timeOutStr = (String) mCommunicationParameter.getReceiveParameter();
        int timeOutS;
        if (TextUtils.isEmpty(timeOutStr))
            timeOutS = 60;
        else {
            timeOutS = Integer.parseInt(timeOutStr, 10);
            if (timeOutS == 0)
                timeOutS = 60;
        }
        XLogUtil.d(TAG, "^_^ 正在接收数据...... ^_^");
        byte[] receiveBuffer;
        int receivedLen;
//        CountDownTimer receiveTimer = new CommunicationTimer(timeOutS*1000, 1000);

        long expireTimeMs = timeOutS * 1000 + System.currentTimeMillis();
        ByteArrayOutputStream dataBuffer = new ByteArrayOutputStream();
        try {
            InputStream is = mClientSocket.getInputStream();
            if (requestLen > 0) {
                // TODO: 2017/2/14  收取指定长度的数据

            } else {
//                receiveTimer.start();
                isTimeout = false;
                do {
                    if (System.currentTimeMillis() >= expireTimeMs) {
                        isTimeout = true;
                        break;
                    }
                    //处理网络环境不好时，数据的组包、完整性。
                    receivedLen = is.available();
                    if (receivedLen > 0) {
                        receiveBuffer = new byte[receivedLen];
                        if (is.read(receiveBuffer) <= 0)
                            break;
                        dataBuffer.write(receiveBuffer);
                        if (isReceivedOver(dataBuffer.toByteArray()))
                            break;
                    }
                } while (!isTimeout);
//                if (!isTimeout)
//                    receiveTimer.cancel();
                dataBuffer.close();
            }
        } catch (Exception e) {
            XLogUtil.e(TAG, "^_^ 数据接收失败:" + e.getMessage() + "^_^");
            throw new BaseRuntimeException(-2, "数据接收失败:" + e.getMessage());
        }
        if (isTimeout) {
            XLogUtil.e(TAG, "^_^ 数据接收超时 ^_^");
            throw new BaseRuntimeException(-3, "数据接收超时");
        }
        XLogUtil.d(TAG, "^_^ 接收到的数据：" + HexUtils.bytesToHexString(dataBuffer.toByteArray()));
        return dataBuffer.toByteArray();
    }

    public void exchangeIpAddress(){
        boolean is = BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), BusinessConfig.Key.USE_REVERVE_COMMON);
        if(is){
            XLogUtil.e(TAG, "^_^ 当前使用备用地址 通讯失败 切换为 主地址 ^_^");
        }else {
            XLogUtil.e(TAG, "^_^ 当前使用主地址 通讯失败 切换为 备用地址 ^_^");
        }
        BusinessConfig.getInstance().setFlag(EposApplication.getAppContext(), BusinessConfig.Key.USE_REVERVE_COMMON, !is);
    }

    @Override
    public void disconnect() {
        if (mClientSocket == null) {
            XLogUtil.d(TAG, "^_^ 无连接 ^_^");
            return;
        }
        try {
            mClientSocket.close();
            mClientSocket = null;
        } catch (IOException e) {
            XLogUtil.e(TAG, "^_^ 断开连接失败:" + e.getMessage() + "^_^");
            throw new BaseRuntimeException(-1, "断开连接失败:" + e.getMessage());
        }
    }

    /**
     * 判断数据是否接收完整
     *
     * @param receiveData 已接收的数据
     * @return true 接收完整
     */
    @Override
    public Boolean isReceivedOver(byte[] receiveData) {
//        if (receiveData == null || receiveData.length < 2)
//            return false;
//        int longPrex = HexUtils.bytes2short(receiveData)+2;
//        if (receiveData.length < longPrex)
//            return false;

        ITCPIsReceivedOver itcpIsReceivedOver = getReceivedOver();
        if (itcpIsReceivedOver != null)
            return itcpIsReceivedOver.isReceivedOver(receiveData);
        else {
            return false;
        }

    }

    public ITCPIsReceivedOver getReceivedOver() {
        Context appContext = EposApplication.getAppContext();
        ITCPIsReceivedOver itcpIsReceivedOver = null;
        RedevelopItem calMacItem = ConfigureManager.getInstance(appContext).getRedevelopItem(appContext, Keys
                .obj().redevelop_receive_over_algorithm);
        if (calMacItem == null)
            itcpIsReceivedOver = new UnionPayReceivedOver();
        else {
            try {
                Object clz = Class.forName(calMacItem.getClassName()).newInstance();
                if (clz instanceof ITCPIsReceivedOver)
                    itcpIsReceivedOver = (ITCPIsReceivedOver) clz;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return itcpIsReceivedOver;
    }

    private class CommunicationTimer extends CountDownTimer {

        /**
         * @param millisInFuture The number of millis in the future from the call
         * to {@link #start()} until the countdown is done and {@link #onFinish()}
         * is called.
         * @param countDownInterval The interval along the way to receive
         * {@link #onTick(long)} callbacks.
         */
        public CommunicationTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            isTimeout = false;
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            isTimeout = true;
        }
    }
}
