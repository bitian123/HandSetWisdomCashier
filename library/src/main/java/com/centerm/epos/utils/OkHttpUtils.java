package com.centerm.epos.utils;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.centerm.epos.EposApplication;
import com.centerm.epos.bean.GtBannerBean;
import com.centerm.epos.bean.GtBean2;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.transcation.pos.constant.JsonKeyGT;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import config.BusinessConfig;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * create by liubit on 2019-09-12
 */
public class OkHttpUtils {
    private static final String TAG = "OkHttpUtils";
    public static volatile OkHttpUtils instance;
    Handler handler = new Handler();
    Gson gson = new Gson();

    private OkHttpUtils() {

    }
    public static OkHttpUtils getInstance() {
        if (instance == null) {
            synchronized (OkHttpUtils.class) {
                if (instance == null) {
                    instance = new OkHttpUtils();
                }
            }
        }
        return instance;
    }

    public void uploadFile(String url, Map<String,Object> paramsMap, final OnCallListener callBack) {
        MultipartBody.Builder multipartBody = new MultipartBody.Builder();
        //form 表单上传
        multipartBody.setType(MultipartBody.FORM);
        //拼接参数
        for (String key : paramsMap.keySet()) {
            Object object = paramsMap.get(key);
            if(object instanceof String){
                multipartBody.addFormDataPart(key,object.toString());
            }else if(object instanceof File){
                File file = (File) object;
                multipartBody.addFormDataPart(key,file.getName(),MultipartBody.create(MediaType.parse("multipart/form-data"),file));
            }
        }
        url = getConnectParameter()+url;
        Log.i("OkHttpUtils","url:"+url);
        Log.i("OkHttpUtils","send -> "+paramsMap.toString());
        RequestBody requestBody=multipartBody.build();
        //创建Request对象
        Request request=new Request.Builder().url(url).post(requestBody).build();
        new OkHttpClient.Builder()
                //设置最长读写时间
                .readTimeout(59, TimeUnit.SECONDS)
                .writeTimeout(59, TimeUnit.SECONDS)
                .connectTimeout(59, TimeUnit.SECONDS).build()
                .newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, final IOException e) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callBack.onCall(null);
                                Log.e("OkHttpUtils", "error:"+e.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        try {
                            final String str = response.body().string();
                            Log.e("OkHttpUtils","onResponse -> "+str);
                            final Map<String, Object> dataForJson = new HashMap<>();
                            dataForJson.put(JsonKeyGT.returnData, str);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callBack.onCall(dataForJson);
                                }
                            });
                        }catch (Exception e){
                            Log.e("OkHttpUtils","error:"+e.getMessage());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callBack.onCall(null);
                                }
                            });
                        }

                    }
                });
    }

    public void downloadFile(final File file, String url, final OnCallListener callBack) {
        // 父目录是否存在
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdir();
        }
        // 文件是否存在
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.i("OkHttpUtils","url:"+url);
        Log.i("OkHttpUtils","filePath -> "+file.getAbsolutePath());
        Request request = null;
        try {
            request = new Request.Builder().url(url).get().build();
        }catch (Exception e){
            Log.i(TAG, e.toString());
            callBack.onCall(null);
            return;
        }
        new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS).build()
                .newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, final IOException e) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callBack.onCall(null);
                            }
                        });
                    }

                    @Override
                    public void onResponse(final Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            ResponseBody body = response.body();
                            // 获取文件总长度
                            final long totalLength = body.contentLength();
                            //以流的方式进行读取
                            InputStream inputStream = body.byteStream();
                            FileOutputStream outputStream = new FileOutputStream(file);
                            byte[] buffer = new byte[2048];
                            int len = 0;
                            int num = 0;
                            while ((len = inputStream.read(buffer)) != -1){
                                num+=len;
                                outputStream.write(buffer,0,len);
                                final int finalNum = num;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        //callBack.onProgressBar( finalNum *100/totalLength);
                                    }
                                });
                            }
                            //读取完关闭流
                            outputStream.flush();
                            outputStream.close();
                            inputStream.close();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(file.exists()){
                                        //返回下载文件路径
                                        HashMap<String, Object> map = new HashMap<>();
                                        map.put("path", file.getAbsolutePath());
                                        callBack.onCall(map);
                                    }
                                }
                            });
                        }else {
                            Log.i(TAG, "onResponse: "+response.toString());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callBack.onCall(null);
                                }
                            });
                        }
                    }
                });
    }

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
        return builder.toString();
    }

}
