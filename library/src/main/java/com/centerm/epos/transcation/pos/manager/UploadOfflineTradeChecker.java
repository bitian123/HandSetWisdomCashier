package com.centerm.epos.transcation.pos.manager;

import com.centerm.epos.EposApplication;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.CommonManager;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.redevelop.ICommonManager;
import com.centerm.epos.utils.XLogUtil;



public class UploadOfflineTradeChecker implements RunTimeChecker {

    @Override
    public boolean check(ITradeView tradeView, ITradePresent tradePresent) {

        try {
            ICommonManager commonManager = (ICommonManager) ConfigureManager.getInstance(EposApplication.getAppContext()).getSubPrjClassInstance(new CommonManager());
            return (commonManager.getOfflineTransList(0)!=null)&&(commonManager.getOfflineTransList(0).size() > 0) ;

        }catch (Exception e){
            XLogUtil.e("offline num", "获取脱机交易笔数失败!!!");
        }
        return false;
    }

    public static boolean isOfflineTransExist() {
        try {
            ICommonManager commonManager = (ICommonManager) ConfigureManager.getInstance(EposApplication.getAppContext()).getSubPrjClassInstance(new CommonManager());
            return (commonManager.getOfflineTransList(0)!=null)&&(commonManager.getOfflineTransList(0).size() > 0) ;
        }catch (Exception e){
            XLogUtil.e("offline num", "isScriptExist error!!!");
        }
        return false;
    }
}
