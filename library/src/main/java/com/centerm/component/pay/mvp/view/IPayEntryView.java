package com.centerm.component.pay.mvp.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.centerm.epos.xml.bean.process.TradeProcess;

/**
 * author:wanliang527</br>
 * date:2017/3/8</br>
 */

public interface IPayEntryView {

    void startAnim(String tip, int drawableId);

    void stopAnim();

    void finish(boolean resultOk, Bundle extras);

    Context getContext();

    void jumpToTrade(String transCode, TradeProcess process, Bundle bundle);

    void jumpToThirdPayment(Intent jBossPaymentIntent, Bundle extras);

    void jumpToLocalFunction(Intent funcIntent, Bundle extras);
}
