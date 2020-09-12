package com.centerm.epos.present.communication;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;
import com.centerm.epos.EposApplication;
import com.centerm.epos.utils.XLogUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;


/**
 * Created by yuhc on 2017/4/7.
 */

public class HttpsCommunication implements ICommunication {
    private static final String TAG = HttpsCommunication.class.getSimpleName();

    //通讯参数
    private ICommunicationParameter mCommunicationParameter;
    HttpsURLConnection mHttpsURLConnection;

    public HttpsCommunication(ICommunicationParameter mCommunicationParameter) {
        this.mCommunicationParameter = mCommunicationParameter;
    }

    @Override
    public Boolean connect() {
        if (TextUtils.isEmpty((CharSequence) mCommunicationParameter.getConnectParameter())) {
            XLogUtil.e(TAG, "^_^ 服务器地址为空！^_^");
            return false;
        }

        XLogUtil.d(TAG, "^_^ 服务器参数:" + mCommunicationParameter.getConnectParameter() +
                " ^_^");
        try {
            SSLContext sslContext = getCertificate();
            URL url = new URL((String) mCommunicationParameter.getConnectParameter());
            openAndInitHttps(sslContext, url);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public int sendData(byte[] data) {
        if (data == null || data.length == 0)
            return 0;

        try {
            XLogUtil.d(TAG, "^_^ 发送数据：" + HexUtils.bytesToHexString(data));
            mHttpsURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
            OutputStream outputStream = mHttpsURLConnection.getOutputStream();
            outputStream.write(data);
            outputStream.flush();
            outputStream.close();
            return data.length;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public byte[] receivedData(int requestLen) {
        byte[] retData = null;
        try {
//            if (HttpURLConnection.HTTP_OK == mHttpsURLConnection.getResponseCode())
            {
                InputStream inputStream = mHttpsURLConnection.getInputStream();
                retData = inputStream2byte(inputStream);
                inputStream.close();
                XLogUtil.d(TAG, "^_^ 接收到的数据：" + HexUtils.bytesToHexString(retData));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retData;
    }

    @Override
    public void disconnect() {
        mHttpsURLConnection.disconnect();
        mHttpsURLConnection = null;
    }

    @Override
    public Boolean isReceivedOver(byte[] receiveData) {
        return null;
    }


    @NonNull
    private SSLContext getCertificate() throws Exception {
        InputStream certStream = EposApplication.getAppContext().getAssets().open("cert/cacert.pem");
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Certificate serverCert = certificateFactory.generateCertificate(certStream);
        return getSslContext(serverCert);
    }

    private void openAndInitHttps(SSLContext sslContext, URL url) throws IOException {

        mHttpsURLConnection = (HttpsURLConnection) url.openConnection();
        mHttpsURLConnection.setSSLSocketFactory(sslContext.getSocketFactory());
        mHttpsURLConnection.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory
                .ALLOW_ALL_HOSTNAME_VERIFIER);

        mHttpsURLConnection.setRequestProperty("Connection", "close");
        mHttpsURLConnection.setReadTimeout(60 * 1000);
        mHttpsURLConnection.setConnectTimeout(6 * 1000);
        mHttpsURLConnection.setRequestMethod("POST");
        mHttpsURLConnection.setRequestProperty("Content-Type", "x-ISO-TPDU/x-auth");
        //fix 使用HttpURLConnection设置请求超时时间，请求超时时会导致自动重发
        mHttpsURLConnection.setChunkedStreamingMode(0);
        mHttpsURLConnection.setDoInput(true);
        mHttpsURLConnection.setDoOutput(true);
        mHttpsURLConnection.setUseCaches(false);
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
