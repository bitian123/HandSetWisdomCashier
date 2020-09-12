package com.centerm.epos.transcation.pos.controller;

import java.util.Map;

/**
 * Created by yuhc on 2017/8/3.
 *
 */

public abstract class AbsTradeUIController implements ITradeUIController {

    protected Map<String, Object> mTradeData;

    protected Map<String, String> mTempData;

    @Override
    public void setTransctionData(Map<String, Object> tradeData) {
        mTradeData = tradeData;
    }

    @Override
    public void setTransctionTempData(Map<String, String> tempData) {
        mTempData = tempData;
    }
}
