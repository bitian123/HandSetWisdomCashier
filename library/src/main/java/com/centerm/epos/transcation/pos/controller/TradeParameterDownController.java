package com.centerm.epos.transcation.pos.controller;

import android.text.TextUtils;

import com.centerm.epos.EposApplication;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.transcation.pos.manager.DownloadAIDTrade;
import com.centerm.epos.transcation.pos.manager.DownloadCAPKTrade;
import com.centerm.epos.transcation.pos.manager.DownloadParameterTrade;
import com.centerm.epos.transcation.pos.manager.DownloadQpsBinTrade;
import com.centerm.epos.transcation.pos.manager.DownloadQpsBlackBinTrade;
import com.centerm.epos.transcation.pos.manager.DownloadQpsTrade;
import com.centerm.epos.transcation.pos.manager.ManageTransaction;
import com.centerm.epos.xml.bean.TradeItem;

import java.util.Map;

/**
 * Created by yuhc on 2017/5/10.
 * 业务参数下载控制器，控制IC卡公钥、参数、QPS参数、卡BIN表下载
 */

public class TradeParameterDownController {
    public static final int PARAMETER_DOWNLOAD_COMPLETE = 100;  //表示已完成所有参数下载

    public static final int IC_CAPK_DOWNLOAD_OVER = 1;  //公钥下载结束
    public static final int IC_AID_DOWNLOAD_OVER = 2;   //参数下载结束
    public static final int TERMINAL_PARAM_DOWNLOAD_OVER = 3;   //终端参数下载结束
    public static final int QPS_PARAMETER_DOWNLOAD_OVER = 4; //QPS参数下载结束
    public static final int QPS_BIN_B_DOWNLOAD_OVER = 5;    //BIN表B下载结束
    public static final int QPS_BIN_C_DOWNLOAD_OVER = 6;    //BIN表C下载结束

    private static TradeParameterDownController instance = null;
    ITradeView tradeView;
    BaseTradePresent tradePresent;

    private TradeParameterDownController(ITradeView tradeView, BaseTradePresent tradePresent) {
        this.tradeView = tradeView;
        this.tradePresent = tradePresent;
    }

    public static synchronized TradeParameterDownController getInstance(ITradeView tradeView, BaseTradePresent
            tradePresent) {
        if (instance == null)
            instance = new TradeParameterDownController(tradeView, tradePresent);
        else {
            instance.tradeView = tradeView;
            instance.tradePresent = tradePresent;
        }
        return instance;
    }

    /**
     * 执行下一个参数下载步骤
     *
     * @return true 执行成功，false 执行失败，或参数都已经下载完成
     */
    public boolean switchToParameterDown() {
        int flag = Settings.getTradeParameterFlag();
        String tranCode;
        switch (flag) {
            case 0:
                Settings.setTradePrameterFlag(1);
            case 1:
                tranCode = TransCode.DOWNLOAD_CAPK;
//                new DownloadCAPKTrade().execute(tradeView, tradePresent);
                break;
            case 2:
                tranCode = TransCode.DOWNLOAD_AID;
//                new DownloadAIDTrade().execute(tradeView, tradePresent);
                break;
            case 3:
                tranCode = TransCode.DOWNLOAD_TERMINAL_PARAMETER;
//                new DownloadParameterTrade().execute(tradeView, tradePresent);
                break;
            case 4:
                tranCode = TransCode.DOWNLOAD_QPS_PARAMS;
//                new DownloadQpsTrade().execute(tradeView, tradePresent);
                break;
            case 5:
                tranCode = TransCode.DOWNLOAD_CARD_BIN_QPS;
//                new DownloadQpsBinTrade().execute(tradeView, tradePresent);
                break;
            case 6:
                tranCode = TransCode.DOWNLOAD_BLACK_CARD_BIN_QPS;
//                new DownloadQpsBlackBinTrade().execute(tradeView, tradePresent);
                break;
            default:
                return false;
        }

        if (!TextUtils.isEmpty(tranCode)) {
            Map<String, TradeItem> tradeItemMap = ConfigureManager.getInstance(EposApplication.getAppContext())
                    .getTradeItemMap();
            TradeItem managerTrade = tradeItemMap.get(tranCode);
            try {
                Class clz = Class.forName(managerTrade.getTradeClz());
                ManageTransaction manageTransaction = (ManageTransaction) clz.newInstance();
                manageTransaction.execute(tradeView, tradePresent);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }


    /**
     * 修改下载状态，用于控制下一步要下载的参数
     *
     * @param overType 标志哪一个参数下载完成
     * @return true 变更成功，false 失败
     */
    public boolean modityParameterFlag(int overType) {
        int nextFlag;
        switch (overType) {
            case IC_CAPK_DOWNLOAD_OVER:
            case IC_AID_DOWNLOAD_OVER:
            case QPS_PARAMETER_DOWNLOAD_OVER:
            case QPS_BIN_B_DOWNLOAD_OVER:
            case TERMINAL_PARAM_DOWNLOAD_OVER:
//            case QPS_BIN_C_DOWNLOAD_OVER:
                nextFlag = overType + 1;
                break;
            case QPS_BIN_C_DOWNLOAD_OVER:
//            case TERMINAL_PARAM_DOWNLOAD_OVER:
                nextFlag = PARAMETER_DOWNLOAD_COMPLETE;
                instance = null;
                break;
            default:
                return false;
        }
        Settings.setTradePrameterFlag(nextFlag);
        return true;
    }
}
