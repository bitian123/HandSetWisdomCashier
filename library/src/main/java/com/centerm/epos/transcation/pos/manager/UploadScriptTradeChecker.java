package com.centerm.epos.transcation.pos.manager;

import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.bean.ScriptInfo;
import com.centerm.epos.bean.TradeInfo;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.utils.XLogUtil;

import java.sql.SQLException;
import java.util.List;

import config.BusinessConfig;

/**
 * Created by yuhc on 2017/4/3.
 */

public class UploadScriptTradeChecker implements RunTimeChecker {

    @Override
    public boolean check(ITradeView tradeView, ITradePresent tradePresent) {

        XLogUtil.w("isScriptExist","FLAG_NEED_UPLOAD_SCRIPT:"+BusinessConfig.getInstance().getFlag(tradeView.getContext(), BusinessConfig.Key
                .FLAG_NEED_UPLOAD_SCRIPT));
        return BusinessConfig.getInstance().getFlag(tradeView.getContext(), BusinessConfig.Key
                .FLAG_NEED_UPLOAD_SCRIPT) && isScriptExist();
    }

    public static boolean isScriptExist() {
        CommonDao<ScriptInfo> dao = new CommonDao<>(ScriptInfo.class, DbHelper.getInstance());
        List<TradeInfo> tradeList = null;

        try {
            tradeList = dao.queryBuilder().where().eq("scriptResult", 1).or().eq("scriptResult", 2).query();
            XLogUtil.w("isScriptExist:",""+tradeList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        DbHelper.releaseInstance();
        if (tradeList == null || tradeList.size() == 0) {
            return false;
        }
        return true;
    }
}
