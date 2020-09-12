package com.centerm.epos.task;

import android.content.Context;
import android.text.TextUtils;

import com.centerm.epos.bean.BinData;
import com.centerm.epos.common.ISORespCode;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.net.SequenceHandler;
import com.centerm.epos.net.SocketClient;
import com.centerm.epos.present.communication.DataExchanger;
import com.centerm.epos.present.communication.DataExchangerFactory;
import com.centerm.epos.present.communication.ICommunication;
import com.centerm.epos.present.communication.TcpCommParameter;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;

import java.util.List;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.epos.common.TransCode.DOWNLOAD_PARAMS_FINISHED;
import static com.centerm.epos.common.TransDataKey.iso_f39;
import static com.centerm.epos.common.TransDataKey.iso_f62;

/**
 * 异步下载卡BIN任务（非卡BIN黑名单）
 * author:wanliang527</br>
 * date:2016/11/30</br>
 */
public abstract class AsyncDownloadCardBinTask extends AsyncMultiRequestTask {

    private CommonDao<BinData> dao;

    public AsyncDownloadCardBinTask(Context context, Map<String, Object> dataMap) {
        super(context, dataMap);
        dao = new CommonDao<>(BinData.class, DbHelper.getInstance());
    }

    @Override
    protected String[] doInBackground(String... params) {
        final String[] result = new String[2];
        dataMap.put(TransDataKey.KEY_PARAMS_TYPE, "5");
        String lastBinNo = BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.KEY_LAST_BIN_NO);
        if (TextUtils.isEmpty(lastBinNo))
            lastBinNo = "000";
        dataMap.put(TradeInformationTag.IC_PARAMETER_CAPD_BIN, lastBinNo);
        dataMap.put(TradeInformationTag.TRANSACTION_TYPE, "0800");
        Object msgPacket = factory.packMessage(TransCode.DOWNLOAD_CARD_BIN, dataMap);
        final SequenceHandler handler = new SequenceHandler() {
            @Override
            protected void onReturn(String reqTag, byte[] respData, String code, String msg) {
                if (respData != null) {
                    Map<String, Object> resp = factory.unPackMessage(reqTag, respData);
                    String respCode = (String) resp.get(TradeInformationTag.RESPONSE_CODE);
                    ISORespCode isoCode = ISORespCode.codeMap(respCode);
                    result[0] = isoCode.getCode();
                    result[1] = context.getString(isoCode.getResId());
                    String iso62 = (String) resp.get(TradeInformationTag.IC_PARAMETER_CAPD_BIN);
                    logger.debug("iso62数据为：" + iso62);
                    int times = 0;//卡BIN下载次数
                    boolean dbResult;
                    switch (reqTag) {
                        case TransCode.DOWNLOAD_CARD_BIN:
                            if ("00".equals(respCode)) {
                                String flag = iso62.substring(0, 1);
                                String lastNo = iso62.substring(1, 6).trim();
                                int intLastNo = Integer.parseInt(lastNo);
                                if ("1".equals(flag)) {
                                    //后续无卡BIN下载
                                    List<BinData> binList = BinData.parse(iso62);
                                    dao.save(binList);
                                    ++intLastNo;
                                    BusinessConfig.getInstance().setValue(context, BusinessConfig.Key.KEY_LAST_BIN_NO, intLastNo + "");
                                    publishProgress(0, -1);
//                                    sleep(LONG_SLEEP);
                                    Object pkgMsg = factory.packMessage(DOWNLOAD_PARAMS_FINISHED, dataMap);
                                    sendNext(DOWNLOAD_PARAMS_FINISHED, (byte[]) pkgMsg);
                                } else if ("2".equals(flag)) {
                                    //后续有卡BIN下载
                                    List<BinData> binList = BinData.parse(iso62);
                                    dao.save(binList);
                                    ++intLastNo;
                                    BusinessConfig.getInstance().setValue(context, BusinessConfig.Key.KEY_LAST_BIN_NO, intLastNo + "");
                                    dataMap.put(TradeInformationTag.IC_PARAMETER_CAPD_BIN, intLastNo + "");
                                    publishProgress(0, intLastNo);
//                                    sleep(MEDIUM_SLEEP);
                                    Object msgPacket = factory.packMessage(TransCode.DOWNLOAD_CARD_BIN, dataMap);
                                    sendNext(TransCode.DOWNLOAD_CARD_BIN, (byte[]) msgPacket);
                                } else {
                                    //无卡BIN需要更新
                                    publishProgress(0, -2);
                                }
                            }
                            break;
                        case DOWNLOAD_PARAMS_FINISHED:
                            BusinessConfig.getInstance().setValue(context, BusinessConfig.Key.KEY_LAST_BIN_NO,"000");
                            //下载结束报文结果，不关心
                            break;
                    }
                } else {
                    result[0] = code;
                    result[1] = msg;
                }
            }
        };
        publishProgress(0, Integer.valueOf(lastBinNo));
//        sleep(LONG_SLEEP);
//        SocketClient client = SocketClient.getInstance(context);
//        client.syncSendSequenceData(TransCode.DOWNLOAD_CARD_BIN, (byte[]) msgPacket, handler);
        DataExchanger dataExchanger = DataExchangerFactory.getInstance();
        dataExchanger.doSequenceExchange(TransCode.DOWNLOAD_CARD_BIN, (byte[]) msgPacket, handler);
        DbHelper.releaseInstance();
        return result;
    }

}
