package com.centerm.epos.transcation.pos.manager;

import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.base.ITradeView;

/**
 * Created by yuhc on 2017/4/1.
 * 运行环境检查
 */

public interface RunTimeChecker {

    boolean check(final ITradeView tradeView, final ITradePresent tradePresent);
}
