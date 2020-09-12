package com.centerm.epos.mvp.listener;

/**
 * 通用状态回调器
 * author:wanliang527</br>
 * date:2017/3/1</br>
 */
public abstract class StatusListener<T> {

    public abstract void onFinish(T[] result);

    public void onError(int code) {
    }
}
