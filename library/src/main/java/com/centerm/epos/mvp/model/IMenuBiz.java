package com.centerm.epos.mvp.model;

import android.content.Context;

import com.centerm.epos.db.DbHelper;
import com.centerm.epos.mvp.listener.StatusListener;
import com.centerm.epos.xml.bean.menu.Menu;

import java.util.List;
import java.util.Map;

/**
 * author:wanliang527</br>
 * date:2017/2/28</br>
 */
public interface IMenuBiz {

    String[] getGridAdapterFrom();

    int[] getGridAdapterTo();

    List<Map<String, ?>> getGridAdapterData(Context context);

    void setMenu(Menu menu);

    Menu getMenu();

    void hasTradeRecords(DbHelper dbHelper, StatusListener<Boolean> listener);

    void clearTradeRecords(DbHelper dbHelper, StatusListener<Boolean> listener);
}
