package com.centerm.epos.transcation.pos.manager;

import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.common.Settings;

/**
 * Created by yuhc on 2017/4/3.
 */

public class SettlementTradeChecker implements RunTimeChecker {

    @Override
    public boolean check(ITradeView tradeView, ITradePresent tradePresent) {
        return "1".equals(Settings.getValue(tradeView.getContext(), Settings.KEY.BATCH_SEND_STATUS, "0"));
    }
}
