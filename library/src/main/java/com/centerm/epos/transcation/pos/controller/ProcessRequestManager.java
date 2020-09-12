package com.centerm.epos.transcation.pos.controller;

import android.text.TextUtils;

import com.centerm.epos.EposApplication;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.redevelop.IProcessRequestAction;
import com.centerm.epos.redevelop.IRedevelopAction;
import com.centerm.epos.transcation.pos.manager.DownloadAIDTrade;
import com.centerm.epos.transcation.pos.manager.DownloadCAPKTrade;
import com.centerm.epos.transcation.pos.manager.DownloadParameterTrade;
import com.centerm.epos.transcation.pos.manager.SignInTrade;
import com.centerm.epos.utils.XLogUtil;

/**
 * Created by yuhc on 2017/5/21.
 * 报文头的处理要求管理
 */

public class ProcessRequestManager {

    private static final String TAG = ProcessRequestManager.class.getSimpleName();

    public static final String NO_PROCESS_REQUEST = "0";
    //下传终端磁条卡、终端参数
    public static final String UPDATE_TERMINAL_PARAM = "1";
    //上传终端状态信息
    public static final String UPLOAD_TERMINAL_STATE = "2";
    //重新签到
    public static final String RESIGN_IN = "3";
    //更新公钥信息
    public static final String UPDATE_IC_PUBKEY = "4";
    //更新IC卡参数
    public static final String UPDATE_IC_PARAMETER = "5";
    //TMS参数下载
    public static final String UPDATE_TMS_PARAMETER = "6";


    /**
     * 检查处理要求是否已定义
     *
     * @param request 处理要求编码
     * @return true 已定义，false 未定义
     */
    private static boolean checkProcessRequest(String request) {
        if (TextUtils.isEmpty(request))
            return false;
        if (UPDATE_TERMINAL_PARAM.equals(request) ||
                UPLOAD_TERMINAL_STATE.equals(request) ||
                RESIGN_IN.equals(request) ||
                UPDATE_IC_PUBKEY.equals(request) ||
                UPDATE_IC_PARAMETER.equals(request) ||
                UPDATE_TMS_PARAMETER.equals(request)) {
            return true;
        }
        IProcessRequestAction requestAction = (IProcessRequestAction) ConfigureManager.getRedevelopAction
                (IRedevelopAction.PROCESS_REQUEST, IProcessRequestAction.class);
        if (requestAction != null) {
            return requestAction.RequestFlagCheck(request);
        }
        return false;
    }


    /**
     * 保存处理要求到数据库或文件中
     *
     * @param request 处理要求编码
     */
    public static void setProcessRequest(String request) {
        if (!checkProcessRequest(request)) {
            XLogUtil.d(TAG, "^_^ 处理要求：" + (request == null ? "空" : request) + "未定义 ^_^");
            return;
        }
        Settings.setValue(EposApplication.getAppContext(), Settings.KEY.PROCESS_REQUEST, request);
    }

    /**
     * 清空处理要求，在完成处理要求的交易后，调用此方法
     */
    public static void clearProcessRequest(String request) {
        if (TextUtils.isEmpty(request)) {
            XLogUtil.e(TAG, "^_^ 请求清空的处理要求：" + (request == null ? "空" : request) + "未定义 ^_^");
            return;
        }
        String data = Settings.getValue(EposApplication.getAppContext(), Settings.KEY.PROCESS_REQUEST, request);
        if (request.equals(data))
            clearProcessRequest();
    }

    /**
     * 清空处理要求
     */
    public static void clearProcessRequest() {
        Settings.setValue(EposApplication.getAppContext(), Settings.KEY.PROCESS_REQUEST, "0");
    }

    /**
     * 是否有处理要求需要进行处理
     *
     * @return true 有 false 没有
     */
    public static boolean isExistProcessRequest() {
        return !NO_PROCESS_REQUEST.equals(Settings.getValue(EposApplication.getAppContext(), Settings.KEY
                .PROCESS_REQUEST, NO_PROCESS_REQUEST));
    }

    /**
     * 获取处理要求标识
     *
     * @return 处理要求
     */
    public static String getProcessRequest() {
        return Settings.getValue(EposApplication.getAppContext(), Settings.KEY.PROCESS_REQUEST, NO_PROCESS_REQUEST);
    }

    /**
     * 执行处理要求标识的交易
     */
    public static boolean activeRequestTrade(ITradeView view, BaseTradePresent present) {
        String requestFlag = getProcessRequest();
        if (TextUtils.isEmpty(requestFlag) || NO_PROCESS_REQUEST.equals(requestFlag)) {
            XLogUtil.d(TAG, "^_^ 没有要处理的前置处理要求 ^_^");
            return false;
        }
        switch (requestFlag) {
            case UPDATE_TERMINAL_PARAM:
                //更新终端参数
                new DownloadParameterTrade().execute(view, present);
                break;
            case RESIGN_IN:
                new SignInTrade().execute(view, present);
                break;
            case UPDATE_IC_PUBKEY:
                new DownloadCAPKTrade().execute(view, present);
                break;
            case UPDATE_IC_PARAMETER:
                new DownloadAIDTrade().execute(view, present);
                break;
            case UPDATE_TMS_PARAMETER:
            case UPLOAD_TERMINAL_STATE:
                return false;
            default:
                IProcessRequestAction requestAction = (IProcessRequestAction) ConfigureManager.getRedevelopAction
                        (IRedevelopAction.PROCESS_REQUEST, IProcessRequestAction.class);
                if (requestAction != null) {
                    return requestAction.doRequestAction(view, present);
                }
                return false;
        }
        return true;
    }

    public static String getRequestTradeCode() {
        String requestFlag = getProcessRequest();
        if (TextUtils.isEmpty(requestFlag) || NO_PROCESS_REQUEST.equals(requestFlag)) {
            XLogUtil.d(TAG, "^_^ 没有要处理的前置处理要求 ^_^");
            return null;
        }
        String tranCode = null;
        switch (requestFlag) {
            case UPDATE_TERMINAL_PARAM:
                //更新终端参数
                tranCode = TransCode.DOWNLOAD_TERMINAL_PARAMETER;
                break;
            case RESIGN_IN:
                tranCode = TransCode.SIGN_IN;
                break;
            case UPDATE_IC_PUBKEY:
                tranCode = TransCode.DOWNLOAD_CAPK;
                break;
            case UPDATE_IC_PARAMETER:
                tranCode = TransCode.DOWNLOAD_AID;
                break;
            case UPDATE_TMS_PARAMETER:
            case UPLOAD_TERMINAL_STATE:
                break;
            default:
                IProcessRequestAction requestAction = (IProcessRequestAction) ConfigureManager.getRedevelopAction
                        (IRedevelopAction.PROCESS_REQUEST, IProcessRequestAction.class);
                if (requestAction != null) {
                    return requestAction.RequestFlag2TradeCode(requestFlag);
                }
                break;
        }
        return tranCode;
    }
}
