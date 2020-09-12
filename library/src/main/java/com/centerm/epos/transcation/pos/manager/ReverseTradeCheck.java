package com.centerm.epos.transcation.pos.manager;

import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.bean.ReverseInfo;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.DbHelper;

/**
 * Created by yuhc on 2017/4/3.
 */

public class ReverseTradeCheck implements RunTimeChecker {

    @Override
    public boolean check(ITradeView tradeView, ITradePresent tradePresent) {
        CommonDao<ReverseInfo> reverseDao = new CommonDao<>(ReverseInfo.class, DbHelper.getInstance());
        boolean result = reverseDao.countOf() > 0;
        DbHelper.releaseInstance();
        return result;
    }
}
