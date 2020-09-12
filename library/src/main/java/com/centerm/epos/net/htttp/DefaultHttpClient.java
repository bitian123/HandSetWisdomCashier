package com.centerm.epos.net.htttp;

import android.content.Context;
import android.text.TextUtils;

import com.centerm.epos.net.ResponseHandler;
import com.centerm.epos.net.htttp.request.BaseRequest;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.MySSLSocketFactory;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;

import config.Config;

/**
 * Created by ysd on 2016/11/25.
 */

public class DefaultHttpClient {
    private Logger logger = Logger.getLogger(DefaultHttpClient.class);
    private final static String DEFAULT_CHARSET = "utf-8";
    private AsyncHttpClient innerClient;
    private static DefaultHttpClient instance;
    private String charset = DEFAULT_CHARSET;

    private DefaultHttpClient() {
        innerClient = new AsyncHttpClient();
        innerClient.setConnectTimeout(Config.HTTP_CONNECTION_TIMEOUT);
        innerClient.setResponseTimeout(Config.HTTP_RESPONSE_TIMEOUT);
    }

    public static DefaultHttpClient getInstance() {
        if (instance == null) {
            synchronized (DefaultHttpClient.class) {
                if (instance == null) {
                    instance = new DefaultHttpClient();
                }
            }
        }
        return instance;
    }

    /**
     * post一个HTTP请求
     */
    public void post(Context context, final BaseRequest request, final ResponseHandler handler) {
        final String url = request.getUrl();
        if (context == null || url == null) {
            logger.warn("[请求失败] - Context或者请求地址为空");
            return;
        }
        logger.info("[正在发送请求] - " + request.toString());
        final RequestParams params = request.getParams();
        if (isHttps(url)) {
            innerClient.setSSLSocketFactory(createSSLSocketFactory());
        }
//        else {
//            innerClient.setSSLSocketFactory(null);
//        }
        innerClient.post(context, url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                logger.debug("[请求地址] - " + url);
                logger.debug("[请求参数] - " + params);
                String response = null;
                try {
                    response = new String(bytes, charset);
                    logger.debug("[返回报文] - " + new String(bytes, charset));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                handler.onSuccess(i + "", response, bytes);
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                logger.debug("[请求地址] - " + url);
                logger.debug("[请求参数] - " + params);
                logger.warn("[请求失败] - 响应码：" + i);
                if (throwable != null) {
                    throwable.printStackTrace();
                }
                if (null != handler) {
                    handler.onFailure(i + "", null, throwable);
                }
            }
        });
    }

    public void get(Context context, final BaseRequest request, final ResponseHandler handler) {
        final String url = request.getUrl();
        if (context == null || url == null) {
            logger.warn("[请求失败] - Context或者请求地址为空");
            return;
        }
        logger.info("[正在发送请求] - " + request.toString());
        if (isHttps(url)) {
            innerClient.setSSLSocketFactory(createSSLSocketFactory());
        }
        final RequestParams params = request.getParams();
        innerClient.get(context, url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                logger.debug("[请求地址] - " + url);
                logger.debug("[请求参数] - " + params);
                String response = null;
                try {
                    response = new String(bytes, charset);
                    logger.debug("[返回报文] - " + new String(bytes, charset));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                handler.onSuccess(i + "", response, bytes);
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                logger.debug("[请求地址] - " + url);
                logger.debug("[请求参数] - " + params);
                logger.warn("[请求失败] - 响应码：" + i);
                if (throwable != null) {
                    throwable.printStackTrace();
                }
                if (null != handler) {
                    handler.onFailure(i + "", null, throwable);
                }
            }
        });
    }

    /**
     * 开始下载
     *
     * @param context  context
     * @param fileUrl  文件下载地址
     * @param filePath 文件存储路径
     * @param handler  回调接收器
     */
    public void download(Context context, final String fileUrl, final String filePath, final FileHandler handler) {
        innerClient.cancelRequestsByTAG(filePath, true);
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        FileAsyncHttpResponseHandler innerHandler = new FileAsyncHttpResponseHandler(file) {

            private long lastSendTime;

            @Override
            public void onCancel() {
                super.onCancel();
                logger.info(filePath + "==>取消下载");
                if (handler != null) {
                    handler.onCanceled();
                }
            }

            @Override
            public void onStart() {
                super.onStart();
                logger.info(filePath + "==>开始下载");
                lastSendTime = 0;
                if (handler != null) {
                    handler.onStart(fileUrl);
                }
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                long now = System.currentTimeMillis();
                if (now - lastSendTime > 300 || bytesWritten == totalSize) {
                    lastSendTime = now;
                    logger.debug(filePath + "==>下载进度==>" + (bytesWritten / (double) totalSize) * 100 + "%");
                    if (handler != null) {
                        handler.onProgress(bytesWritten, totalSize);
                    }
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, Throwable throwable, File file) {
                logger.warn(filePath + "==>下载失败");
                if (handler != null) {
                    handler.onFailure(i, throwable);
                }
            }

            @Override
            public void onSuccess(int i, Header[] headers, File file) {
                logger.info(filePath + "==>下载成功");
                if (handler != null) {
                    handler.onSuccess(file);
                }
            }
        };
        innerClient.get(context, fileUrl, innerHandler).setTag(filePath);
    }

    public void cancel(String reqTag) {
        innerClient.cancelRequestsByTAG(reqTag, true);
    }


    private SSLSocketFactory createSSLSocketFactory() {
        MySslSocketFactory sf = null;
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            sf = new MySslSocketFactory(trustStore);
            sf.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sf;
    }

    public boolean isHttps(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        return url.contains("https:");
    }


}
