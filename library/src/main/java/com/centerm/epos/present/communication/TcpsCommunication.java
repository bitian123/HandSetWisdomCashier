package com.centerm.epos.present.communication;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;
import com.centerm.epos.EposApplication;
import com.centerm.epos.base.BaseRuntimeException;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.redevelop.ITCPIsReceivedOver;
import com.centerm.epos.redevelop.UnionPayReceivedOver;
import com.centerm.epos.utils.XLogUtil;
import com.centerm.epos.xml.bean.RedevelopItem;
import com.centerm.epos.xml.keys.Keys;

import org.bouncycastle.openssl.PEMReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by yuhc on 2017/2/13.
 * 采用TCP连接方式的数据交互。此模块需要放到单独的线程中执行。
 */

public class TcpsCommunication implements ICommunication {

    private static final String TAG = TcpsCommunication.class.getSimpleName();

    //客户端
    Socket mClientSocket = null;
    //通讯参数
    private TcpsCommParameter mCommunicationParameter;
    //超时控制标志
    private boolean isTimeout = false;

    public TcpsCommunication(ICommunicationParameter mCommunicationParameter) {
        this.mCommunicationParameter = (TcpsCommParameter) mCommunicationParameter;
    }

    public TcpsCommunication(TcpsCommParameter mCommunicationParameter) {
        this.mCommunicationParameter = (TcpsCommParameter) mCommunicationParameter;
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

        TcpsCommParameter.ConnectParameter connectParameter = (TcpsCommParameter.ConnectParameter)
                mCommunicationParameter.getConnectParameter();
        XLogUtil.d(TAG, "^_^ 服务器IP:" + connectParameter.getServerIP() + " PORT:" + connectParameter.getServerPort() +
                " ^_^");
        int retryCount = connectParameter.getRetryTimes();
        do {

            try {
                SSLContext sslContext = getCertificatePem();
                mClientSocket = sslContext.getSocketFactory().createSocket();
                mClientSocket.setTcpNoDelay(true);
                SocketAddress socketAddress = new InetSocketAddress(connectParameter.getServerIP(), connectParameter
                        .getServerPort());
                mClientSocket.connect(socketAddress, connectParameter.getConnectTimeOutS() * 1000);
                mClientSocket.setSoTimeout(connectParameter.getConnectTimeOutS() * 1000);
                break;
            } catch (Exception e) {
                XLogUtil.e(TAG, "^_^ " + e.getMessage() + " ^_^");
                e.printStackTrace();
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
            e.printStackTrace();
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
                byte[] tempBuffer = new byte[2048];
                do {
                    if (System.currentTimeMillis() >= expireTimeMs) {
                        isTimeout = true;
                        break;
                    }
                    //处理网络环境不好时，数据的组包、完整性。
                    receivedLen = is.read(tempBuffer);
                    if (receivedLen > 0) {
//                        receiveBuffer = new byte[receivedLen];
//                        System.arraycopy();
                        dataBuffer.write(java.util.Arrays.copyOfRange(tempBuffer, 0, receivedLen));
                        if (isReceivedOver(dataBuffer.toByteArray()))
                            break;
                    }
//                    receivedLen = is.available();
//                    if (receivedLen > 0) {
//                        XLogUtil.e(TAG, "^_^ 数据接收长度2:"+receivedLen+"^_^");
//                        receiveBuffer = new byte[receivedLen];
//                        if (is.read(receiveBuffer) <= 0)
//                            break;
//                        dataBuffer.write(receiveBuffer);
//                        if(isReceivedOver(dataBuffer.toByteArray()))
//                            break;
//                    }
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

    @NonNull
    private SSLContext getCertificate() throws Exception {
        InputStream certStream = EposApplication.getAppContext().getAssets().open("cert/CFCA_TEST_CS_CA.cer");
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Certificate serverCert = certificateFactory.generateCertificate(certStream);
        return getSslContext(serverCert);
    }

    @NonNull
    private SSLContext getCertificatePem() throws Exception {
//        PEMReader cacertfile = new PEMReader(new InputStreamReader(EposApplication.getAppContext()
//                .getAssets().open("vs_g3_g5_2048_ceshi.pem")));
//        X509Certificate cacert = (X509Certificate) cacertfile.readObject();
//        // 导入根证书作为trustedEntry
//        KeyStore.TrustedCertificateEntry trustedEntry = new KeyStore.TrustedCertificateEntry(cacert);
//        kks.setEntry("ca_root", trustedEntry, null);


        InputStream certStream = EposApplication.getAppContext().getAssets().open("cert/CFCA_TEST_CS_CA.cer");
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Certificate serverCert = certificateFactory.generateCertificate(certStream);
        return getSslContext(serverCert);
    }

    private SSLContext getSSLContext() {

        SSLContext sslContext = null;
        try {
            // 设定Security的Provider提供程序
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

            // 建立空BKS,android只能用BKS(BouncyCastle密库)，一般java应用参数传JKS(java自带密库)
            KeyStore ksKeys = KeyStore.getInstance("BKS");
            ksKeys.load(null, null);

            // 读入客户端证书
            PEMReader cacertfile = new PEMReader(new InputStreamReader(EposApplication.getAppContext().getAssets()
                    .open("99312da9.0")));
            X509Certificate cacert = (X509Certificate) cacertfile.readObject();
            cacertfile.close();

            // 导入根证书作为trustedEntry
            KeyStore.TrustedCertificateEntry trustedEntry = new KeyStore.TrustedCertificateEntry(cacert);
            ksKeys.setEntry("ca_root", trustedEntry, null);

            // 构建KeyManager、TrustManager
            /*KeyManagerFactory kmf = KeyManagerFactory
                    .getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ksKeys, null);*/
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");//密钥管理器,一般java应用传SunX509
            tmf.init(ksKeys);

            // 构建SSLContext，此处传入参数为TLS，也可以为SSL
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return sslContext;
    }

    @NonNull
    private SSLContext getSslContext(Certificate serverCert) throws Exception {
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("cert", serverCert);

        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm);
        trustManagerFactory.init(keyStore);


        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
        return sslContext;
    }

    public byte[] inputStream2byte(InputStream in) throws Exception {

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int count = -1;
        while ((count = in.read(data, 0, 1024)) != -1)
            outStream.write(data, 0, count);

        return outStream.toByteArray();
    }
}
