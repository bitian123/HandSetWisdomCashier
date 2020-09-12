package com.centerm.epos.task;

import android.content.Context;
import android.text.TextUtils;

import com.centerm.epos.bean.QpsBinData;
import com.centerm.epos.common.ISORespCode;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.net.SequenceHandler;
import com.centerm.epos.present.communication.DataExchanger;
import com.centerm.epos.present.communication.DataExchangerFactory;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.epos.common.TransCode.DOWNLOAD_PARAMS_FINISHED;

/**
 * 异步下载卡BIN任务（非卡BIN黑名单）
 * author:wanliang527</br>
 * date:2016/11/30</br>
 */
public abstract class AsyncDownloadQPSCardBinTask extends AsyncMultiRequestTask {

    private CommonDao<QpsBinData> dao;

    public AsyncDownloadQPSCardBinTask(Context context, Map<String, Object> dataMap) {
        super(context, dataMap);
        dao = new CommonDao<>(QpsBinData.class, DbHelper.getInstance());
    }

    @Override
    protected String[] doInBackground(String... params) {
        final String[] result = new String[2];
        dataMap.put(TransDataKey.KEY_PARAMS_TYPE, "6");
        String lastBinNo = BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.KEY_LAST_BIN_NO);
        if (TextUtils.isEmpty(lastBinNo))
            lastBinNo = "000";
        dataMap.put(TradeInformationTag.IC_PARAMETER_CAPD_BIN, lastBinNo);
        dataMap.put(TradeInformationTag.TRANSACTION_TYPE, "0800");
        Object msgPacket = factory.packMessage(TransCode.DOWNLOAD_CARD_BIN_QPS, dataMap);
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
                        case TransCode.DOWNLOAD_CARD_BIN_QPS:
                            if ("00".equals(respCode)) {
                                String flag = iso62.substring(0, 1);
                                String lastNo = iso62.substring(1, 4).trim();
                                List<QpsBinData> binDB = null;
                                Where where;
                                int intLastNo = Integer.parseInt(lastNo);
                                if ("1".equals(flag)) {
                                    //后续无卡BIN下载
                                    List<QpsBinData> binList = QpsBinData.parse(iso62);
                                    for (QpsBinData qpsBinData : binList) {
                                        try {
                                            where = dao.queryBuilder().where();
                                            binDB = where.and(where.eq("type", "B"),
                                                    where.eq("cardBin", qpsBinData.getCardBin()),
                                                    where.eq("cardLen", qpsBinData.getCardLen())).
                                                    query();
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                            binDB = null;
                                        }
                                        if (binDB == null || binDB.size() <= 0)
                                            dao.save(qpsBinData);
                                    }
                                    ++intLastNo;
                                    BusinessConfig.getInstance().setValue(context, BusinessConfig.Key
                                            .KEY_LAST_BIN_NO, intLastNo + "");
                                    publishProgress(0, -1);
//                                    sleep(LONG_SLEEP);
                                    Object pkgMsg = factory.packMessage(DOWNLOAD_PARAMS_FINISHED, dataMap);
                                    sendNext(DOWNLOAD_PARAMS_FINISHED, (byte[]) pkgMsg);
                                } else if ("2".equals(flag)) {
                                    //后续有卡BIN下载
                                    List<QpsBinData> binList = QpsBinData.parse(iso62);
                                    for (QpsBinData qpsBinData : binList) {
                                        try {
                                            where = dao.queryBuilder().where();
                                            binDB = where.and(
                                                    where.eq("type", "B"),
                                                    where.eq("cardBin", qpsBinData.getCardBin()),
                                                    where.eq("cardLen", qpsBinData.getCardLen())
                                            ).query();
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                            binDB = null;
                                        }
                                        if (binDB == null || binDB.size() <= 0)
                                            dao.save(qpsBinData);
                                    }
                                    ++intLastNo;
                                    String formatedLastNo = String.format(Locale.CHINA, "%03d", intLastNo);
                                    BusinessConfig.getInstance().setValue(context, BusinessConfig.Key
                                            .KEY_LAST_BIN_NO, formatedLastNo);
                                    dataMap.put(TradeInformationTag.IC_PARAMETER_CAPD_BIN, formatedLastNo);
                                    publishProgress(0, intLastNo);
//                                    sleep(MEDIUM_SLEEP);
                                    Object msgPacket = factory.packMessage(TransCode.DOWNLOAD_CARD_BIN_QPS, dataMap);
                                    sendNext(TransCode.DOWNLOAD_CARD_BIN_QPS, (byte[]) msgPacket);
                                } else {
                                    //无卡BIN需要更新
                                    publishProgress(0, -2);
                                }
                            }
                            break;
                        case DOWNLOAD_PARAMS_FINISHED:
                            //下载完成后清空起始编号，否则平台会返回96 2017/04/26
                            BusinessConfig.getInstance().setValue(context, BusinessConfig.Key.KEY_LAST_BIN_NO, "000");
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
        DataExchanger dataExchanger = DataExchangerFactory.getInstance();
        dataExchanger.doSequenceExchange(TransCode.DOWNLOAD_CARD_BIN_QPS, (byte[]) msgPacket, handler);
        DbHelper.releaseInstance();
        return result;
    }

}
