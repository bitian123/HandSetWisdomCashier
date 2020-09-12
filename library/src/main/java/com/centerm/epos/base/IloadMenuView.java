package com.centerm.epos.base;

import com.centerm.epos.xml.bean.menu.Menu;

import java.util.Map;

/**
 * Created by ysd on 2017/10/26.
 */

public interface IloadMenuView {
    void loadMenuView(Menu menuObj, Map<String, String> param, BaseFragmentActivity activity);
}
