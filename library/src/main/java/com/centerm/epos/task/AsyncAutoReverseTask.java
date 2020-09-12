package com.centerm.epos.task;

import android.content.Context;
import android.text.TextUtils;

import com.centerm.cpay.midsdk.dev.common.utils.TlvUtils;
import com.centerm.epos.bean.ReverseInfo;
import com.centerm.epos.common.ISORespCode;
import com.centerm.epos.common.StatusCode;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.msg.PosISO8583Message;
import com.centerm.epos.net.SequenceHandler;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import config.BusinessConfig;

import static com.centerm.epos.common.EmvTagKey.EMVTAG_ATC;
import static com.centerm.epos.common.EmvTagKey.EMVTAG_IAD;
import static com.centerm.epos.common.EmvTagKey.EMVTAG_IFD;
import static com.centerm.epos.common.EmvTagKey.EMVTAG_SCRIPT_RESULT;
import static com.centerm.epos.common.EmvTagKey.EMVTAG_TVR;
import static com.centerm.epos.common.TransDataKey.iso_f55;

/**
 * 异步进行自动冲正任务
 * author:wanliang527</br>
 * date:2016/12/4</br>
 */

public class AsyncAutoReverseTask extends AsyncMultiRequestTask {

    private CommonDao<ReverseInfo> dao;
    private List<ReverseInfo> reverseList;
    private int index;//冲正索引
    private int times;//重试次数
    private String transCode;
    private Map<String, String> dataMap;
    private boolean isAfterTrade;

    public AsyncAutoReverseTask(Context context, Map<String, Object> dataMap) {
        super(context, dataMap);
        this.dataMap = new HashMap<>();
        if (factory instanceof PosISO8583Message)
            ((PosISO8583Message) factory).setRequestDataForIso8583(this.dataMap);
        dao = new CommonDao<>(ReverseInfo.class, DbHelper.getInstance());
        reverseList = dao.query();
        isAfterTrade = false;
    }

    public AsyncAutoReverseTask setIsAfterTrade(boolean isAfterTrade) {
        this.isAfterTrade = isAfterTrade;
        return this;
    }

    @Override
    protected String[] doInBackground(String... params) {
        sleep(SHORT_SLEEP);
        if (reverseList == null || reverseList.size() == 0) {
            logger.warn("冲正表信息为空==>任务结束");
            return super.doInBackground(params);
        }
        logger.debug("开始筛选隔日交易");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        String today = formatter.format(new Date());
        for (int i = 0; i < reverseList.size(); i++) {
            ReverseInfo info = reverseList.get(i);
            String date = info.getTransTime();
            if (date != null && date.length() >= 8) {
                if (!today.equals(date.substring(0, 8))) {
                    logger.warn("隔日交易不冲正==>当前日期：" + today + "==>此笔交易日期：" + info.getTransTime());
                    reverseList.remove(info);
                    dao.delete(info);
                }
            }
        }
        logger.debug("隔日交易筛选完成");
        if (reverseList.size() == 0) {
            logger.warn("冲正表信息为空==>任务结束");
            return super.doInBackground(params);
        }
        publishProgress(-1);//告诉UI层准备开始冲正
        sleep(LONG_SLEEP);
        logger.info("开始冲正==>待冲正总笔数：" + reverseList.size() + "==>当前索引：" + index);
        times = 0;
        ReverseInfo info = reverseList.get(index);
        publishProgress(index + 1, times + 1);
        Object msgPkg = factory.packMessage(initReverseData(info), null);
        SequenceHandler handler = new SequenceHandler() {
            @Override
            protected void onReturn(String reqTag, byte[] respData, String code, String msg) {
                ReverseInfo info = reverseList.get(index);
                taskResult[0] = code;
                taskResult[1] = msg;
                if (respData != null) {
                    Map<String, Object> resp = factory.unPackMessage(transCode, respData);
                    String f39 = (String) resp.get(TradeInformationTag.RESPONSE_CODE);
                    ISORespCode respCode = ISORespCode.codeMap(f39);
                    logger.warn(info.getIso_f11() + "==>冲正结果返回码==>" + f39);
                    if (ISORespCode.ISO0.equals(respCode)
                            || ISORespCode.ISO12.equals(respCode)
                            || ISORespCode.ISO25.equals(respCode)
                            || ISORespCode.ISO40.equals(respCode)) {


                    }
                    //返回其它的应答码时，重复发冲正交易其实已无意义，因为收到应答码一般都是一样的，所以此处都当成功处理。
                    //如果需要其它默认处理，可在此修改，例如打印故障报告单；或直接退出，下次联机时继续发冲正。
                    boolean dbResult = dao.delete(info);
                    logger.info(info.getIso_f11() + "==>冲正成功==>删除冲正表记录==>" + dbResult);
                    if (hasNext()) {
                        logger.info("继续冲正下一笔==>待冲正总笔数：" + reverseList.size() + "==>当前索引：" + index);
                        times = 0;
                        info = reverseList.get(++index);
                        publishProgress(index + 1, times + 1);
                        Object msgPkg = factory.packMessage(initReverseData(info), null);
                        sleep(MEDIUM_SLEEP);
                        sendNext(transCode, (byte[]) msgPkg);
                    }
//                    }else
//                        logger.warn("==>冲正失败");

                    sleep(LONG_SLEEP);
                } else {
                    if (isAfterTrade) {
                        taskResult[0] = StatusCode.UNKNOWN_REASON.getStatusCode();
                        taskResult[1] = "冲正失败";
                        return;
                    }
                    //冲正无响应的情况，尝试再次冲正
                    if (++times < BusinessConfig.getInstance().getNumber(context, BusinessConfig.Key
                            .KEY_MAX_MESSAGE_RETRY_TIMES)) {
                        logger.info(info.getIso_f11() + "==>冲正失败==>" + "尝试再次发起冲正" + times);
                        publishProgress(index + 1, times + 1);
                        Object msgPkg = factory.packMessage(initReverseData(info), null);
                        sleep(MEDIUM_SLEEP);
                        sendNext(transCode, (byte[]) msgPkg);
                    } else {
                        //冲正接收失败的情况下，不删除冲正
//                        boolean dbResult = dao.delete(info);
//                        logger.warn(info.getIso_f11() + "==>冲正次数已超限==>删除冲正表记录==>不再进行冲正==>" + dbResult);
//                        if (hasNext()) {
//                            logger.info("继续冲正下一笔==>待冲正总笔数：" + reverseList.size() + "==>当前索引：" + index);
//                            times = 0;
//                            info = reverseList.get(++index);
//                            publishProgress(index + 1, times + 1);
//                            Object msgPkg = factory.packMessage(initReverseData(info), null);
//                            sleep(MEDIUM_SLEEP);
//                            sendNext(transCode, (byte[]) msgPkg);
//                        }
                        taskResult[0] = "-1";
                        taskResult[1] = "冲正接收失败，请检查网络";
                    }
                }
            }
        };
        if (msgPkg == null) {
            dao.delete(info);
            logger.info("^_^ 冲正报文有问题，直接删除冲正报文 ^_^");
            return super.doInBackground(params);
        }
        client.doSequenceExchange(transCode, (byte[]) msgPkg, handler);
        logger.info("本次冲正结束");
        publishProgress(-2);
        sleep(MEDIUM_SLEEP);
        DbHelper.releaseInstance();
        return super.doInBackground(params);
    }

    private String initReverseData(ReverseInfo reverseInfo) {
        String iso11 = reverseInfo.getIso_f11();
        transCode = reverseInfo.getTransCode() + "_REVERSE";
        logger.debug("冲正信息==>" + reverseInfo.toString());
        logger.info(iso11 + "==>当前冲正交易对应的交易码：" + transCode);
        dataMap.clear();
        dataMap.putAll(reverseInfo.convert2Map());

//        formatICInfo(dataMap);
//        dataMap.put(iso_f11_origin, reverseInfo.getIso_f11());//原流水号
//        dataMap.put(iso_f60_origin, reverseInfo.getIso_f60());//原批次号(60.2域中存储)
        return transCode;
    }

    private void formatICInfo(Map<String, String> dataMap) {
        if (dataMap == null || dataMap.size() == 0 || !dataMap.containsKey(iso_f55))
            return;
        String icInfo = dataMap.get(iso_f55);
        if (TextUtils.isEmpty(icInfo))
            return;
        Map<String, String> stringMap = TlvUtils.tlvToMap(icInfo);
        Map<String, String> reverseIcDataMap = new HashMap<>();
        if (stringMap.containsKey(EMVTAG_TVR)) ;
        reverseIcDataMap.put(EMVTAG_TVR, stringMap.get(EMVTAG_TVR));
        if (stringMap.containsKey(EMVTAG_IFD)) ;
        reverseIcDataMap.put(EMVTAG_IFD, stringMap.get(EMVTAG_IFD));
        if (stringMap.containsKey(EMVTAG_IAD)) ;
        reverseIcDataMap.put(EMVTAG_IAD, stringMap.get(EMVTAG_IAD));
        if (stringMap.containsKey(EMVTAG_ATC)) ;
        reverseIcDataMap.put(EMVTAG_ATC, stringMap.get(EMVTAG_ATC));
        if (stringMap.containsKey(EMVTAG_SCRIPT_RESULT)) ;
        reverseIcDataMap.put(EMVTAG_SCRIPT_RESULT, stringMap.get(EMVTAG_SCRIPT_RESULT));

        dataMap.put(iso_f55, mapToTlvStr(reverseIcDataMap));
    }

    private String mapToTlvStr(Map<String, String> reverseIcDataMap) {
        if (reverseIcDataMap == null || reverseIcDataMap.size() == 0)
            return "";
        StringBuilder stringBuffer = new StringBuilder();
        String value, len;
        Set<Map.Entry<String, String>> reverseSet = reverseIcDataMap.entrySet();
        for (Map.Entry<String, String> item : reverseSet) {
            value = item.getValue();
            if (!TextUtils.isEmpty(value)) {
                len = String.format(Locale.CHINA, "%02x", value.length() / 2);
                stringBuffer.append(item.getKey()).append(len).append(value);
            }
        }
        return stringBuffer.toString();
    }

    private boolean hasNext() {
        if (index + 1 < reverseList.size()) {
            return true;
        }
        return false;
    }


    /*private void updateRetryTimes(ReverseInfo info) {
        int times = info.getRetryTimes();
        if (times < BusinessConfig.getInstance().getNumber(context, BusinessConfig.Key.KEY_MAX_MESSAGE_RETRY_TIMES)) {
            info.setRetryTimes(++times);
            boolean dbResult = dao.update(info);
            logger.info(info.getIso_f11() + "==>更新冲正重试次数==>" + dbResult);
        } else {

        }
    }*/
}
