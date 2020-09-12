package com.centerm.epos.present.transaction;

import com.centerm.epos.base.ITradePresent;

/**
 * author:wanliang527</br>
 * date:2017/3/2</br>
 */

public interface IInputCommonInfoPresenter extends ITradePresent {

    boolean onConfirmClicked(String goodsCode);

    boolean onConfirmClicked(String... param);

    boolean bIsIntegralTrans();

    boolean bIsMagCashLoad();
    /*持卡人证件号 磁条卡现金充值*/
    String getMagCertNo(int id);


}
