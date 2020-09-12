package com.centerm.epos.channels.helper;

import android.content.Context;

import com.centerm.epos.ActivityStack;

import org.apache.log4j.Logger;

import java.util.Map;

/**
 * author:wanliang527</br>
 * date:2016/10/29</br>
 */

public abstract class BaseRespHelper {

    protected Logger logger = Logger.getLogger(this.getClass());
    protected ActivityStack activityStack;

    private String transCode;

    public BaseRespHelper(String transCode) {
        this.transCode = transCode;
        activityStack = ActivityStack.getInstance();
    }

    public abstract void onRespSuccess(Object present, Map<String, Object> data);

    public abstract void onRespFailed(Object present, String statusCode, String msg);

    public String getTransCode() {
        return transCode;
    }

    public void setTransCode(String transCode) {
        this.transCode = transCode;
    }

    protected String getString(Context context, int resId) {
        return context.getResources().getString(resId);
    }


}
