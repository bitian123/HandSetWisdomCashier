package com.centerm.epos.task;

import android.content.Context;

import com.centerm.cpay.midsdk.dev.define.IPinPadDev;
import com.centerm.epos.common.ISORespCode;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.StatusCode;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.msg.MessageFactoryPlus;
import com.centerm.epos.net.ResponseHandler;
import com.centerm.epos.present.communication.DataExchanger;
import com.centerm.epos.present.communication.DataExchangerFactory;
import com.centerm.epos.present.communication.ICommunication;
import com.centerm.epos.present.communication.TcpCommParameter;
import com.centerm.epos.present.transaction.TradingPresent;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.CommonUtils;

import java.util.HashMap;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.epos.common.TransDataKey.iso_f39;
import static com.centerm.epos.common.TransDataKey.iso_f41;
import static com.centerm.epos.common.TransDataKey.iso_f42;
import static com.centerm.epos.common.TransDataKey.iso_f43;
import static com.centerm.epos.common.TransDataKey.iso_f62;

/**
 * author:wanliang527</br>
 * date:2016/12/1</br>
 */

public class AsyncDownloadTmkTask extends AsyncMultiRequestTask {
    private Map<String, Object> returnMap;

    public AsyncDownloadTmkTask(Context context, Map<String, Object> dataMap, Map<String, Object> returnMap) {
        super(context, dataMap);
        this.returnMap = returnMap;
        if (this.returnMap == null) {
            this.returnMap = new HashMap<>();
        }
    }

    @Override
    protected String[] doInBackground(String... params) {
        sleep(LONG_SLEEP);
//        Object msgPkg = factory.pack(TransCode.OBTAIN_TMK, dataMap);

        final Object msgPacket = factory.packMessage(TransCode.OBTAIN_TMK, dataMap);
        if (msgPacket == null) {
            logger.warn("请求报文为空，退出");
            taskResult[1] = StatusCode.PACKAGE_ERROR.getStatusCode();
            taskResult[2] = context.getString(StatusCode.PACKAGE_ERROR.getMsgId());
            return taskResult;
        }
        ResponseHandler respHelper = new ResponseHandler() {
            @Override
            public void onSuccess(String statusCode, String msg, byte[] data) {
//                Map<String, Object> mapData = factory.unPackMessage(TransCode.OBTAIN_TMK, data);
                Map<String, Object> mapData = factory.unPackMessage(TransCode.OBTAIN_TMK, data);
                returnMap.putAll(mapData);
                String respCode = (String) mapData.get(TradeInformationTag.RESPONSE_CODE);
                ISORespCode isoCode = ISORespCode.codeMap(respCode);
                taskResult[0] = isoCode.getCode();
                taskResult[1] = context.getString(isoCode.getResId());
                if ("00".equals(respCode)) {
                    String merchantName = (String) mapData.get(TradeInformationTag.MERCHANT_NAME);
                    String merchantCode = (String) mapData.get(TradeInformationTag.MERCHANT_IDENTIFICATION);
                    String terminalCode = (String) mapData.get(TradeInformationTag.TERMINAL_IDENTIFICATION);
                    BusinessConfig config = BusinessConfig.getInstance();
                    if (null != merchantName && !"".equals(merchantName)) {
                        config.setValue(context, BusinessConfig.Key.SETTLEMENT_MERCHANT_NAME, merchantName);
                        BusinessConfig.getInstance().setIsoField(context, 43, merchantName);
                    }
                    if (null != merchantCode && !"".equals(merchantCode)) {
                        config.setValue(context, BusinessConfig.Key.SETTLEMENT_MERCHANT_CD, merchantCode);
                        BusinessConfig.getInstance().setIsoField(context, 42, merchantCode);
                    }
                    if (null != terminalCode && !"".equals(terminalCode)) {
                        config.setValue(context, BusinessConfig.Key.SETTLEMENT_TERMINAL_CD, terminalCode);
                        BusinessConfig.getInstance().setIsoField(context, 41,terminalCode);
                    }
                    String tmk = (String) mapData.get(TradeInformationTag.SECURITY_KEY);
                    String value = tmk.substring(0, 32);
                    String checkValue = tmk.substring(32, 40);
                    taskRetryTimes = 0;
                    IPinPadDev pinPadDev = CommonUtils.getPinPadDev();
                    boolean result = false;
                    if (pinPadDev != null) {
                        result = pinPadDev.loadTMK2(value, checkValue);
                    }
                    if (result) {
                        returnMap.put(iso_f39, "00");//下载主密钥并发散成功
                        Settings.setTmkExist(context);//设置主密钥存在的标识
                        BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.FLAG_SIGN_IN, false);
                    } else {
                        StatusCode code = StatusCode.KEY_VERIFY_FAILED;
                        taskResult[0] = code.getStatusCode();
                        taskResult[1] = context.getString(code.getMsgId());
                    }
                }
            }

            @Override
            public void onFailure(String code, String msg, Throwable error) {
                taskResult[0] = code;
                taskResult[1] = msg;
            }
        };
//        client.syncSendData((byte[]) msgPkg, handler);
        try {
            DataExchanger dataExchanger = DataExchangerFactory.getInstance();
            byte[] receivedData = dataExchanger.doExchange((byte[]) msgPacket);
            if (receivedData == null) {
                logger.error("^_^ 接收数据失败！ ^_^");
                taskResult[0] = "99";
                taskResult[1] = "接收数据失败！";
                respHelper.onFailure("99", "接收数据失败！", null);
            } else {
                respHelper.onSuccess(StatusCode.SUCCESS.getStatusCode(), context.getString(StatusCode.SUCCESS
                        .getMsgId()), receivedData);
            }
        } catch (Exception e) {
            logger.error("^_^ 数据交换失败：" + e.getMessage() + " ^_^");
            taskResult[0] = StatusCode.DATA_EXCHANGE_ERROR.getStatusCode();
            taskResult[1] = context.getString(StatusCode.DATA_EXCHANGE_ERROR.getMsgId());
        }
        return taskResult;
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }

}
