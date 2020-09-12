package com.centerm.epos.transcation.pos.controller;

import com.centerm.cloudsys.sdk.common.utils.FileUtils;
import com.centerm.epos.EposApplication;
import com.centerm.epos.base.SimpleStringTag;
import com.centerm.epos.model.TradeModelImpl;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;

import java.io.File;
import java.util.Map;

import config.BusinessConfig;
import config.Config;

/**
 * Created by yuhc on 2017/8/3.
 * 签名板显示控制
 */

public class ShowInputPhoneUIController extends AbsTradeUIController {
    @Override
    public boolean isShowUI(String tradeType) {
//        String batchNum = (String) mTradeData.get(TradeInformationTag.BATCH_NUMBER);
//        String flowNo = (String) mTradeData.get(TradeInformationTag.TRACE_NUMBER);
//        String path = Config.Path.SIGN_PATH + File.separator +batchNum+"_"+flowNo+".png";

        boolean hasESignFile = false;
        if(mTradeData.containsKey(TradeInformationTag.STORE_E_SIGN_RESULT))
            hasESignFile = (boolean) mTradeData.get(TradeInformationTag.STORE_E_SIGN_RESULT);
        //打开输入电话号码的开关并且签名存在的情况下，才显示电话号码输入界面
        if (BusinessConfig.getInstance().getToggle(EposApplication.getAppContext(), SimpleStringTag
                .TOGGLE_ESIGN_PHONE_NUMBER) && hasESignFile){
            return true;
        }
        return false;
    }
}
