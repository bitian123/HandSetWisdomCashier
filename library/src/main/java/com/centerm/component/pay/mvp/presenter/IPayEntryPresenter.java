package com.centerm.component.pay.mvp.presenter;

import android.os.Bundle;

import com.centerm.component.pay.mvp.view.IPayEntryView;
import com.centerm.epos.mvp.listener.StatusListener;

/**
 * author:wanliang527</br>
 * date:2017/3/8</br>
 */

public interface IPayEntryPresenter {

    void onPrepare(Bundle bundle, StatusListener<String> listener);

    /*   String[] onCheckParams(Bundle bundle);

       String[] onCheckStatus();

       Bundle newBundle(String respCode, String respMsg);*/
    void initPresenter(IPayEntryView view);

    void onProcess(Bundle extras);

    void onFinish(String[] result);

    Bundle mapJBossRespDatas(Bundle extras);

    boolean postProcess();
}
