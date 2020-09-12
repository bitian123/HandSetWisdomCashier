package com.centerm.epos.mvp.presenter;

import com.centerm.epos.base.ITradePresent;

/**
 * author:wanliang527</br>
 * date:2017/3/2</br>
 */

public interface IInputOriginInfoPresenter extends ITradePresent {

    boolean onConfirmClicked(String scanVoucher, String posSerial, String platSerial, String date, String authCode,
                             String authDate, String cardDate);

    /**
     * 是否需要输入卡有效期
     * @return  true需要输入
     */
    boolean isInputCardValideDate();
}
