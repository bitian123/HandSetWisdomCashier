package com.centerm.epos.ebi.task;

import android.content.Context;
import android.text.TextUtils;

import com.centerm.epos.common.ISORespCode;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.net.SequenceHandler;
import com.centerm.epos.present.communication.DataExchanger;
import com.centerm.epos.present.communication.DataExchangerFactory;
import com.centerm.epos.task.AsyncMultiRequestTask;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.CustomTLVUtil;
import com.centerm.smartpos.util.HexUtil;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.epos.common.TransCode.DOWNLOAD_PARAMS_FINISHED;

/**
 * 异步下载卡BIN任务（非卡BIN黑名单）
 * author:wanliang527</br>
 * date:2016/11/30</br>
 */
public abstract class EbiAsyncDownloadParamTask extends AsyncMultiRequestTask {

    public EbiAsyncDownloadParamTask(Context context, Map<String, Object> dataMap) {
        super(context, dataMap);
    }

    @Override
    protected String[] doInBackground(String... params) {
        final String[] result = new String[2];
        dataMap.put(TransDataKey.KEY_PARAMS_TYPE, "8");
        Object msgPacket = factory.packMessage(TransCode.DOWNLOAD_TERMINAL_PARAMETER, dataMap);
        final SequenceHandler handler = new SequenceHandler() {
            @Override
            protected void onReturn(String reqTag, byte[] respData, String code, String msg) {
                if (respData != null) {
                    Map<String, Object> resp = factory.unPackMessage(reqTag, respData);
                    String respCode = (String) resp.get(TradeInformationTag.RESPONSE_CODE);
                    ISORespCode isoCode = ISORespCode.codeMap(respCode);
                    result[0] = isoCode.getCode();
                    result[1] = context.getString(isoCode.getResId());
                    switch (reqTag) {
                        case TransCode.POS_STATUS_UPLOAD:
                            if ("00".equals(respCode)) {
                                Object msgPacket = factory.packMessage(TransCode.DOWNLOAD_TERMINAL_PARAMETER, dataMap);
                                sendNext(TransCode.DOWNLOAD_TERMINAL_PARAMETER, (byte[]) msgPacket);
                            }
                            break;
                        case TransCode.DOWNLOAD_TERMINAL_PARAMETER:
                            String terminalParam = (String) resp.get(TradeInformationTag.TERMINAL_PARAMETER);
                            logger.debug("终端参数数据为：" + terminalParam);
                            if (!TextUtils.isEmpty(terminalParam)&&!TextUtils.equals("null",terminalParam)) {
                                parseAndKeepParameters(terminalParam);
                                //保存参数下载完成标志
                                BusinessConfig.getInstance().setFlag(context,TransDataKey.FLAG_HAS_DOWNLOAD_PARAM, true);
                                BusinessConfig.getInstance().setIsoField(context, 41, (String) resp.get(TradeInformationTag.TERMINAL_IDENTIFICATION));
                                BusinessConfig.getInstance().setIsoField(context, 42, (String) resp.get(TradeInformationTag.MERCHANT_IDENTIFICATION));
                            }
                            break;
                        case DOWNLOAD_PARAMS_FINISHED:
                            //下载结束报文结果，不关心
                            break;
                    }
                } else {
                    result[0] = code;
                    result[1] = msg;
                }
            }
        };
//        sleep(LONG_SLEEP);
//        SocketClient client = SocketClient.getInstance(context);
//        client.syncSendSequenceData(TransCode.DOWNLOAD_CARD_BIN, (byte[]) msgPacket, handler);
        DataExchanger dataExchanger = DataExchangerFactory.getInstance();
        dataExchanger.doSequenceExchange(TransCode.DOWNLOAD_TERMINAL_PARAMETER, (byte[]) msgPacket, handler);
        return result;
    }

    private void parseAndKeepParameters(String terminalParam) {
        if (TextUtils.isEmpty(terminalParam))
            return;
        final int keyLen = 4;
        int len, offset = 0;
        String key, value;
        while (terminalParam.length() > offset+keyLen){
            key = terminalParam.substring(offset, offset+keyLen);
            key = new String(HexUtil.hexStringToByte(key));
            offset+=keyLen;
            //ASCII 表示，长度要乖2
            len = getSpdbParamLen(key)<<1;
            if(len > 0) {
                value = terminalParam.substring(offset, offset + len);
                offset += len;
                storageParameter(key, value);
            }
        }
    }

    /**
     * 保存参数
     * @param key   参数编码
     * @param value 参数值
     */
    private void storageParameter(String key, String value) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value))
            return;
        byte[] byteValue = HexUtil.hexStringToByte(value);  //提供给交易屏蔽使用。
        String realValue;    //提供给字符参数设置。
        try {
            realValue = new String(byteValue,"GBK").trim();
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage());
            return;
        }
        int index = Integer.valueOf(key, 10);
        BusinessConfig.getInstance().setValue(context, "tag"+index, realValue);
        logger.error("tag"+index+": "+realValue);
        switch (index){
            case 14:
            case 15:
            case 16:
                //电话号码忽略
                break;
            case 22:
                //商户名称（中文简称）
                if (!TextUtils.isEmpty(realValue))
                    BusinessConfig.getInstance().setValue(context, BusinessConfig.Key.KEY_MCHNT_NAME, realValue);
                break;
            case 26:
                //支持的交易类型，交易功能屏蔽开关，需要使用 byteValue值
                // TODO: 2017/6/6 交易屏蔽控制

                break;
            case 27:
                //商户名称（英文简称）
                break;
            case 28:
                //商户号
//                if (realValue.length() == 15)
//                    BusinessConfig.getInstance().setIsoField(context, 42, realValue);
                break;
            case 29:
                //终端号
//                if (realValue.length() == 8)
//                    BusinessConfig.getInstance().setIsoField(context, 41, realValue);
                break;
            case 31:
                //批次号
                break;
            case 32:
            case 33:
            case 34:
                //财务卡号
                break;
            case 35:
                //分期付款支持期数
                break;
            case 36:
                //分期付款限额
                break;
            case 37:
                //消费交易单笔上限
                break;
            case 38:
                //退货交易单笔上限
                break;
            case 39:
                //转帐交易单笔上限
                break;
            case 40:
                //退货交易时间跨度
                break;
        }
    }

    /**
     * 根据键名获取对应参数值的长度
     * @param key   参数编码
     * @return  参数长度
     */
    private int getSpdbParamLen(String key) {
        if (TextUtils.isEmpty(key))
            return 0;
        int index = Integer.valueOf(key,10);
        switch (index){
            case 1:
                //// TODO: 2018/1/24
                //return 3;
            case 2:
            case 3:
            case 4:
            case 5:
                return 2;
            case 11:
            case 12:
                return 2;
            case 13:
                return 1;
            case 14:
            case 15:
            case 16:
            case 17:
                return 14;
            case 18:
                return 1;
            case 19:
                return 2;
            case 20:
                //// TODO: 2018/1/24
                //return 2;
            case 21:
                return 1;
            case 22:
                return 40;
            case 23:
            case 24:
                return 1;
            case 25:
                return 2;
            case 26:
                //TODO: 2018/1/24
                return 2;
//                return 8;
            case 27:
                return 3;
            case 28:
                return 2;
            case 29:
            case 30:
                return 30;
            case 31:
            case 32:
            case 33:
                return 2;
            case 34:
                return 1;
            case 35:
                return 30;
            case 36:
                return 1;
            //case 36:
            //// TODO: 2018/1/24
            //return 2;
            case 37:
            case 38:
                return 25;
            case 39:
                return 2;
            case 40:
                return 1;
            case 41:
                return 24;

        }
        return 0;
    }

}
