package com.centerm.epos.net;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import org.apache.log4j.Logger;

/**
 * author: wanliang527</br>
 * date:2016/10/10</br>
 */

public class NetClient {
    private Logger logger = Logger.getLogger(this.getClass());
    private static NetClient instance;
    private Context context;

    private Handler handler = new Handler(context.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };


    private NetClient() {
    }

    public static NetClient getInstance(Context context) {
        if (instance == null) {
            synchronized (NetClient.class) {
                if (instance == null) {
                    instance = new NetClient();
                }
            }
        }
        instance.context = context.getApplicationContext();
        return instance;
    }



}
