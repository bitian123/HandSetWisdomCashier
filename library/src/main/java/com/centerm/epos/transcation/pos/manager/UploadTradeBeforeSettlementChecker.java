package com.centerm.epos.transcation.pos.manager;

import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.base.ITradeView;

/**
 * Created by yuhc on 2017/4/3.
 */

public class UploadTradeBeforeSettlementChecker implements RunTimeChecker {

    @Override
    public boolean check(ITradeView tradeView, ITradePresent tradePresent) {
//        if (UploadESignatureTradeChecker.isEsignPicExist())
//            return true;
//
//        // TODO: 2017/9/12 脱机业务 、脚本通知等业务
//        if (UploadScriptTradeChecker.isScriptExist())
//            return true;
//
//        if (UploadOfflineTradeChecker.isOfflineTransExist()){
//            return true;
//        }
        return false;
    }
}
