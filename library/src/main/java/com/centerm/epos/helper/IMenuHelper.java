package com.centerm.epos.helper;

import com.centerm.epos.mvp.presenter.IMenuPresenter;
import com.centerm.epos.xml.bean.menu.MenuItem;

/**
 * author:wanliang527</br>
 * date:2016/10/26</br>
 */
public interface IMenuHelper {

    boolean onTriggerMenuItem(IMenuPresenter presenter, MenuItem item);

}
