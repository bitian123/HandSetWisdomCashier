package com.centerm.epos.present.communication;

import android.text.TextUtils;
import android.util.Log;

import com.centerm.cloudsys.sdk.common.utils.MD5Utils;
import com.centerm.epos.EposApplication;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.XLogUtil;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import config.BusinessConfig;

import static java.lang.Thread.sleep;

/**
 * Created by yuhc on 2017/2/13.
 * 采用http通讯方式的数据交互。此模块需要放到单独的线程中执行。
 */

public class HttpCommunication implements ICommunication {
    private static final String TAG = HttpCommunication.class.getSimpleName();

    //通讯参数
    private ICommunicationParameter mCommunicationParameter;
    //待发送数据
    private String mDataOfSend;
    //发送方式
    private String mSendModel;
    //服务器的URL
    private String mServeUrl;

    public HttpCommunication(ICommunicationParameter mCommunicationParameter) {
        this.mCommunicationParameter = mCommunicationParameter;
    }

    /**
     * 只进行一些参数的检查
     *
     * @return true检查通过
     */
    @Override
    public Boolean connect() {
        mServeUrl = (String) mCommunicationParameter.getConnectParameter();
        if (TextUtils.isEmpty(mServeUrl)) {
            XLogUtil.e(TAG, "^_^ 服务器地址为空！^_^");
            return false;
        }
        return true;
    }

    /**
     * 保存待发送的数据和参数
     *
     * @param data 数据
     * @return true数据和参数保存成功
     */
    @Override
    public int sendData(byte[] data) {
        mDataOfSend = new String(data);
        mSendModel = (String) mCommunicationParameter.getSendParameter();
        XLogUtil.d(TAG, "^_^ 发送数据：" + mDataOfSend);
        XLogUtil.d(TAG, "^_^ 发送方式：" + mSendModel + " ^_^");
        return data.length;
    }

    /**
     * 执行http的数据收发，忽略输入参数
     *
     * @param requestLen 忽略
     * @return 接收到的数据
     */
    @Override
    public byte[] receivedData(int requestLen) {
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setReadTimeout(28, TimeUnit.SECONDS);
        okHttpClient.setReadTimeout(128, TimeUnit.SECONDS);
        okHttpClient.setConnectTimeout(28, TimeUnit.SECONDS);
        Request request;
        switch (mSendModel) {
            case "POST":
                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                        mDataOfSend);
                request = new Request.Builder().url(mServeUrl).post(requestBody).build();

                break;
            case "GET":
                String connectedUrl = mServeUrl+"?"+ mDataOfSend;
                request = new Request.Builder().url(connectedUrl).build();
                XLogUtil.d(TAG, "^_^ GET方式请求的URL:"+connectedUrl+" ^_^");
                break;
            default:
                XLogUtil.e(TAG, "^_^ 请求方式错误！ ^_^");
                return null;
        }
        //优化 发送失败的情况下需要重试两次
        tryTime = 0;
        return send(okHttpClient);
    }

    private int tryTime = 0;//重发次数
    private static final int TRY_MAX_TIME = 3;//最大重发次数 18
    private byte[]  send(OkHttpClient okHttpClient) {
        try {
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                    mDataOfSend);
            String md5Verify = MD5Utils.getMD5Str(mDataOfSend+CommonUtils.SALT).toLowerCase();
            XLogUtil.d(TAG, "^_^ md5Verify："+md5Verify);
            Request request = new Request.Builder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("appid", "298A0F1469BA43269BCECA89C84CB1DA")
                    .addHeader("md5Verify", md5Verify)
                    .url(mServeUrl).post(requestBody).build();
            Response response = getRes(okHttpClient, request);
            if(response.isSuccessful()){
                String content = response.body().string();
                XLogUtil.d(TAG, "^_^ 接收到的返回数据："+content);
                return content.getBytes();
            }
        } catch (IOException e) {
            XLogUtil.e(TAG, "^_^ 通讯失败："+e.getMessage()+" ^_^");
            exchangeIpAddress();
            tryTime++;
            if(tryTime<TRY_MAX_TIME){
                try {
                    sleep(833);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
                XLogUtil.e(TAG, "^_^ 通讯失败 重新发起请求: "+tryTime+"^_^");
                mServeUrl = (String) mCommunicationParameter.getConnectParameter();
                return send(okHttpClient);
            }
            return null;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public void exchangeIpAddress(){
        boolean is = BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), BusinessConfig.Key.USE_REVERVE);
        if(is){
            XLogUtil.e(TAG, "^_^ 扫码交易 当前使用备用地址 通讯失败 切换为 主地址 ^_^");
        }else {
            XLogUtil.e(TAG, "^_^ 扫码交易 当前使用主地址 通讯失败 切换为 备用地址 ^_^");
        }
        BusinessConfig.getInstance().setFlag(EposApplication.getAppContext(), BusinessConfig.Key.USE_REVERVE, !is);
    }


    public Response getRes(OkHttpClient okHttpClient,Request request) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        Response response = null;
        //https 去掉服务器端证书验证
        //if(BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), BusinessConfig.Key.SCAN_ENCODE_FLAG)){
        if(true){
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {

                }
            }}, new SecureRandom());
            okHttpClient.setSslSocketFactory(sc.getSocketFactory());
            response = okHttpClient.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            }).newCall(request).execute();
        }else {
            response = okHttpClient.newCall(request).execute();
        }
        return response;
    }

    @Override
    public void disconnect() {

    }

    @Override
    public Boolean isReceivedOver(byte[] receiveData) {
        return null;
    }
}
