package com.centerm.epos.ebi.transaction.checker;

import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.transcation.pos.manager.RunTimeChecker;

/**
 * Created by liubit on 2018/1/3.
 */

public class DownloadMainKeyTradeChecker implements RunTimeChecker {

    @Override
    public boolean check(ITradeView tradeView, ITradePresent tradePresent) {
        return true;
    }
}