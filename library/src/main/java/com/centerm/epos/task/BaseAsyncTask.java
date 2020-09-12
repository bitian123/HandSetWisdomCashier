package com.centerm.epos.task;

import android.content.Context;
import android.os.AsyncTask;

import com.centerm.epos.EposApplication;

import org.apache.log4j.Logger;

/**
 * 异步任务基础类
 * author:wanliang527</br>
 * date:2016/12/1</br>
 */

public abstract class BaseAsyncTask<Params, Result> extends AsyncTask<Params, Integer, Result> implements ITaskListener<Result> {
    protected Logger logger = Logger.getLogger(this.getClass());
    protected final static long SHORT_SLEEP = 300;
    protected final static long MEDIUM_SLEEP = 500;
    protected final static long LONG_SLEEP = 1000;

    protected Context context;

    public BaseAsyncTask(Context context) {
        this.context = context == null ? EposApplication.getAppContext() : context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        onStart();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (values == null || values.length == 0) {
            onProgress(0, 0);
        } else {
            if (values.length == 1) {
                onProgress(0, values[0]);
            } else {
                onProgress(values[0], values[1]);
            }
        }
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
        onFinish(result);
    }

    protected void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onProgress(Integer counts, Integer index) {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onFinish(Result result) {

    }
}
