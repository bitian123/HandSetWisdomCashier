package com.centerm.epos.present.transaction;

import com.centerm.epos.base.ITradePresent;

/**
 * author:wanliang527</br>
 * date:2017/3/2</br>
 */

public interface IInputInstallmentInfoPresenter extends ITradePresent {

    boolean onConfirmClicked(String installmentPeriod, String code, int installmentType);


}
