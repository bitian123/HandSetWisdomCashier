package com.centerm.epos.transcation.pos.manager;

import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.base.ITradeView;

/**
 * Created by yuhc on 2017/4/1.
 */

public class SignInTradeChecker implements RunTimeChecker {

    @Override
    public boolean check(ITradeView tradeView, ITradePresent tradePresent) {
        return true;
    }
}
