package com.centerm.epos.mvp.presenter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.centerm.epos.adapter.MenuAbsListAdapter;
import com.centerm.epos.mvp.model.IMenuBiz;
import com.centerm.epos.mvp.view.IMenuView;
import com.centerm.epos.view.GridViewPager;
import com.centerm.epos.xml.bean.menu.Menu;
import com.centerm.epos.xml.bean.menu.MenuItem;

import java.util.Map;

/**
 * author:wanliang527</br>
 * date:2017/2/28</br>
 */

public interface IMenuPresenter {

    void initTopView();

    void initLocalData(Menu menu);

    int getLayoutId();

    void onMenuItemClicked(View view, MenuItem item);

    boolean isDeviceReady();

    boolean[] isNeedTmkOrSignIn(String menuTag);

    /**
     * 流程预处理
     *
     * @param item 菜单项
     * @return 预处理成功返回true，失败返回false
     */
    boolean onPreProcess(MenuItem item);

    void onProcess(MenuItem item);

    boolean onNoProcessDefine(MenuItem item);

    Menu getMenu();

    MenuAbsListAdapter getListAdapter();

    GridViewPager.GridPagerAdapter getGridAdapter();

    void jumpToActivity(Class<? extends Activity> clz);

    void jumpToActivity(Class<? extends Activity> clz, Bundle parameters);

    void jumpToActivity(Class<? extends Activity> clz, Map<String, String> parameterMap);

    void beginOnlineProcess(String menuTag);

    void beginOnlineProcess(String tranCode, String process);

    void setBizFlag(String key, boolean flag);

    boolean getBizFlag(String key);

    void doClearTradeRecords();

    void doClearReverseRecords();

    IMenuView getMenuView();

    IMenuBiz getMenuBiz();

    Context getContext();

    void release();

    void restoreAppConfig();

    void doLocalFunction(int functionID);

    boolean beginAutoSign();
}
