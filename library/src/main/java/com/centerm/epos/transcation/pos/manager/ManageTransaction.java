package com.centerm.epos.transcation.pos.manager;


import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;

import java.util.Map;

/**
 * Created by yuhc on 2017/4/1.
 */

public interface ManageTransaction {

    void execute(final ITradeView tradeView, final BaseTradePresent tradePresent);
}
