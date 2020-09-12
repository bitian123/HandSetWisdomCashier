package com.centerm.epos.transcation.pos.controller;

import com.centerm.epos.EposApplication;
import com.centerm.epos.base.SimpleStringTag;
import com.centerm.epos.model.TradeModelImpl;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.CommonUtils;

import config.BusinessConfig;

/**
 * Created by yuhc on 2017/8/3.
 * 签名板显示控制
 */

public class ShowSignPadUIController extends AbsTradeUIController {
    @Override
    public boolean isShowUI(String tradeType) {
        if (TradeModelImpl.getInstance().isTradeSlipNoSign())
            return false;
        if(!CommonUtils.isK9()){
            return true;
        }
        if (mTempData != null) {
            String responCode = mTempData.get(TradeInformationTag.RESPONSE_CODE);
            if ("00".equals(responCode))
                return BusinessConfig.getInstance().getToggle(EposApplication.getAppContext(), SimpleStringTag.TOGGLE_ESIGN_SUPPORT);

        }
        return false;
    }
}
