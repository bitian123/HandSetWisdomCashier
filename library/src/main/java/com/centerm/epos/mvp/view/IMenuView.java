package com.centerm.epos.mvp.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.centerm.epos.base.BaseFragmentActivity;
import com.centerm.epos.view.AlertDialog;
import com.centerm.epos.xml.bean.menu.Menu;
import com.centerm.epos.xml.bean.process.TradeProcess;

import java.util.Map;

/**
 * author:wanliang527</br>
 * date:2017/2/28</br>
 */

public interface IMenuView {

    int getGridLayoutId();

    int getListLayoutId();

    void showLoading(String tip);

    void hideLoading();

    void showSelectDialog(String title, String msg, AlertDialog.ButtonClickListener listener);

    void showSelectDialog(int title, int msg, AlertDialog.ButtonClickListener listener);

    void showSelectDialog(int title, String msg, AlertDialog.ButtonClickListener listener);

    void hideDialog();

    void toast(String content);

    void toast(int id);

    boolean jumpToChildMenu(Menu menu);

    void jumpToTrade(String transCode, TradeProcess process);

    void jumpToActivity(Class<? extends Activity> clz);

    void jumpToActivity(Class<? extends Activity> clz, Bundle parameters);

    void jumpToActivity(Class<? extends Activity> clz, Map<String, String> parameterMap);

    void onToggleIfExists(View itemView);

    Context getContext();

    String getString(int id);

    BaseFragmentActivity getHostActivity();


}
