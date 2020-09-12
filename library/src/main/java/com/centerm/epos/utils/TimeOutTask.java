package com.centerm.epos.utils;

import android.os.CountDownTimer;

/**
 * Created by yuhc on 2017/8/21.
 *
 */

public class TimeOutTask extends CountDownTimer {
    private static final String TAG = TimeOutTask.class.getSimpleName();

    private boolean isTimeout;
    /**
     * @param millisInFuture The number of millis in the future from the call
     * to {@link #start()} until the countdown is done and {@link #onFinish()}
     * is called.
     * @param countDownInterval The interval along the way to receive
     * {@link #onTick(long)} callbacks.
     */
    public TimeOutTask(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
    }

    @Override
    public void onTick(long millisUntilFinished) {
        XLogUtil.d(TAG, "^_^ 1 SECEND PASSED ^_^");
    }

    @Override
    public void onFinish() {
        isTimeout = true;
    }

    public void reset(){
        cancel();
        start();
    }

    public boolean isTimeout() {
        return isTimeout;
    }

    public void setTimeout(boolean timeout) {
        isTimeout = timeout;
    }
}
