package com.centerm.epos.present.transaction;

import android.view.View;

import com.centerm.cpay.midsdk.dev.define.cardreader.EnumReadCardType;

/**
 * Created by yuhc on 2017/2/15.
 * 检卡界面相关的业务操作
 */

public interface ICheckCard {

    /**
     * 获取卡类型
     * @return  卡类型
     */
    public EnumReadCardType getCardType();


    void onStartScanCode();

    void onGetScanCode(String code);

    boolean isSupportScan();

    boolean isEnableInputCardNumber();

    void onButtonConfirm(int viewId, String data);
}
