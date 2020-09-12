package com.centerm.epos.present.transaction;

import com.centerm.epos.base.ITradePresent;

/**
 * author:zhouzhihua</br>
 * date:2017/1/12</br>
 */

public interface IInputCardNumberPresenter extends ITradePresent {
    boolean onConfirmClicked(String... param);
}
