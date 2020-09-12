package com.centerm.epos.task;

import android.content.Context;
import android.text.TextUtils;

import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.net.SequenceHandler;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.DataHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.epos.common.TransDataKey.iso_f11;
import static com.centerm.epos.common.TransDataKey.iso_f41;
import static com.centerm.epos.common.TransDataKey.iso_f42;
import static com.centerm.epos.common.TransDataKey.iso_f48;
import static com.centerm.epos.common.TransDataKey.iso_f60;

/**
 * Created by ysd on 2016/12/20.
 */

public class AsyncUploadMagsDataTask extends AsyncMultiRequestTask {
    private List<TradeInfoRecord> tradeInfos;
    private int index;
    private String transCode = TransCode.TRANS_CARD_DETAIL;
    private CommonDao<TradeInfoRecord> tradeDao;
    private List<String> strings = new ArrayList<>();
    private List<TradeInfoRecord> objArray1 = new ArrayList<>();
    private List<List<TradeInfoRecord>> objArray2 = new ArrayList<>();

    public AsyncUploadMagsDataTask(Context context, Map<String, Object> dataMap, List<TradeInfoRecord> tradeInfos) {
        super(context, dataMap);
        this.tradeInfos = tradeInfos;
        tradeDao = new CommonDao<>(TradeInfoRecord.class, DbHelper.getInstance());
        initMagsCardData();
    }

    private void initMagsCardData() {
        StringBuffer buffer = new StringBuffer();
        strings.clear();
        objArray2.clear();
        objArray1.clear();
        TradeInfoRecord info;
        String RMB, cardNum;
        String moneyType;
        if (null != tradeInfos && tradeInfos.size() > 0) {
            for (int i = 1; i <= tradeInfos.size(); i++) {
                info = tradeInfos.get(i - 1);
                RMB = info.getCurrencyCode();
                if (null != RMB && "156".equals(RMB)) {
                    moneyType = "00";
                } else {
                    moneyType = "01";
                }
                buffer.append(moneyType);
                buffer.append(info.getVoucherNo());
                cardNum = info.getCardNo();
                if (TextUtils.isEmpty(cardNum))
                    cardNum = info.getScanVoucherNo();
                if (TextUtils.isEmpty(cardNum))
                    cardNum = "";
                buffer.append(DataHelper.formatToXLen(cardNum, 20));
                buffer.append(String.format(Locale.CHINA, "%012d", Long.parseLong(info.getAmount().replace(".", ""))));
                objArray1.add(info);
                if (i % 8 == 0) {
                    strings.add("08" + buffer.toString());
                    List<TradeInfoRecord> tempInfos = new ArrayList<>();
                    tempInfos.addAll(objArray1);
                    objArray2.add(tempInfos);
                    buffer.delete(0, buffer.length());
                    objArray1.clear();
                }
            }
            if (buffer.length() > 0) {
                int count = buffer.length() / 40;
                strings.add("0" + count + buffer.toString());
                List<TradeInfoRecord> tempInfos = new ArrayList<>();
                tempInfos.addAll(objArray1);
                objArray2.add(tempInfos);
            }
            buffer.delete(0, buffer.length());
            objArray1.clear();
        }

    }

    @Override
    protected String[] doInBackground(String... params) {
        sleep(LONG_SLEEP);
        if (strings == null || strings.size() == 0) {
            return super.doInBackground(params);
        }
        index = 0;
        initData(strings.get(index));
        publishProgress(strings.size(), index + 1);
        Object msgPkg = factory.packMessage(transCode, dataMap);
        SequenceHandler handler = new SequenceHandler() {

            @Override
            protected void onReturn(String reqTag, byte[] respData, String code, String msg) {
//                sleep(LONG_SLEEP);
                taskResult[0] = code;
                taskResult[1] = msg;
                if (respData != null) {
                    Map<String, Object> resp = factory.unPackMessage(transCode, respData);
                    String respCode = (String) resp.get(TradeInformationTag.RESPONSE_CODE);
                    if ("00".equals(respCode)) {
                        logger.error("磁条卡第" + (index + 1) + "批记录上送成功");
                        List<TradeInfoRecord> infos = objArray2.get(index);
                        for (TradeInfoRecord info :
                                infos) {
                            info.setBatchSuccess(true);
                            tradeDao.update(info);
                        }
                        if (hasNext()) {
                            initData(strings.get(++index));
                            publishProgress(strings.size(), index + 1);
                            Object msgPkg = factory.packMessage(transCode, dataMap);
                            sendNext(transCode, (byte[]) msgPkg);
                        }
                    } else {
                        logger.error("磁条卡第" + (index + 1) + "批记录被拒绝");
                        List<TradeInfoRecord> infos = objArray2.get(index);
                        for (TradeInfoRecord info :
                                infos) {
                            info.setSendCount(99);
                            tradeDao.update(info);
                        }
                        if (hasNext()) {
                            initData(strings.get(++index));
                            publishProgress(strings.size(), index + 1);
                            Object msgPkg = factory.packMessage(transCode, dataMap);
                            sendNext(transCode, (byte[]) msgPkg);
                        }
                    }
                } else {
                    logger.error("磁条卡第" + (index + 1) + "批记录上送失败");
                    if (hasNext()) {
                        initData(strings.get(++index));
                        publishProgress(strings.size(), index + 1);
                        Object msgPkg = factory.packMessage(transCode, dataMap);
                        sendNext(transCode, (byte[]) msgPkg);
                    }
                }
            }
        };
        client.doSequenceExchange(transCode, (byte[]) msgPkg, handler);
        DbHelper.releaseInstance();
        return super.doInBackground(params);
    }

    private boolean hasNext() {
        if (index + 1 < strings.size()) {
            return true;
        }
        return false;
    }

    private void initData(String f48) {
        dataMap.clear();
        dataMap.put(iso_f11, BusinessConfig.getInstance().getPosSerial(context));//POS终端流水号，11域
        dataMap.put(iso_f41, BusinessConfig.getInstance().getIsoField(context, 41));//商户号和终端号要使用签到后的本机参数
        dataMap.put(iso_f42, BusinessConfig.getInstance().getIsoField(context, 42));
        dataMap.put(iso_f48, f48);
        dataMap.put(iso_f60, "00" + BusinessConfig.getInstance().getBatchNo(context) + "201");//60域
        List<TradeInfoRecord> infos = objArray2.get(index);
        for (TradeInfoRecord info :
                infos) {
            int count = info.getSendCount();
            info.setSendCount(++count);
            //更改上送次数
            tradeDao.update(info);
        }
    }
}
