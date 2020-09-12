package com.centerm.epos.task;

import android.content.Context;
import android.text.TextUtils;

import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.net.SequenceHandler;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.epos.common.TransDataKey.key_is_balance_settle;
import static com.centerm.epos.common.TransDataKey.key_is_balance_settle_foreign;

/**
 * Created by ysd on 2016/12/20.
 */

public class AsyncUploadArpcErrorTask extends AsyncMultiRequestTask {
    private List<TradeInfoRecord> tradeInfos;
    private int index;
    private String transCode = TransCode.TRANS_IC_DETAIL;
    private CommonDao<TradeInfoRecord> tradeDao;
    TradeInfoRecord cardInfo;

    public AsyncUploadArpcErrorTask(Context context, Map<String, Object> dataMap, List<TradeInfoRecord> tradeInfos) {
        super(context, dataMap);
        this.tradeInfos = tradeInfos;
        tradeDao = new CommonDao<>(TradeInfoRecord.class, DbHelper.getInstance());
    }

    @Override
    protected String[] doInBackground(String... params) {
//        sleep(LONG_SLEEP);
        if (tradeInfos == null || tradeInfos.size() == 0) {
            return super.doInBackground(params);
        }
        index = 0;
        cardInfo = tradeInfos.get(index);
        publishProgress(tradeInfos.size(), index + 1);
        initData(cardInfo);
        Object msgPkg = factory.packMessage(transCode, dataMap);
        SequenceHandler handler = new SequenceHandler() {

            @Override
            protected void onReturn(String reqTag, byte[] respData, String code, String msg) {
                sleep(LONG_SLEEP);
                taskResult[0] = code;
                taskResult[1] = msg;
                if (respData != null) {
                    Map<String, Object> resp = factory.unPackMessage(transCode, respData);
                    String respCode = (String) resp.get(TradeInformationTag.RESPONSE_CODE);
                    if ("00".equals(respCode)||"94".equals(respCode)) {
                        logger.error("IC卡第" + (index + 1) + "条记录上送成功");
                        cardInfo.setBatchSuccess(true);
                        //更新上送状态
                        tradeDao.update(cardInfo);
                        if (hasNext()) {
                            cardInfo = tradeInfos.get(++index);
                            publishProgress(tradeInfos.size(), index + 1);
                            initData(cardInfo);
                            Object msgPkg = factory.packMessage(transCode, dataMap);
                            sendNext(transCode, (byte[]) msgPkg);
                        }
                    } else {
                        logger.error("IC卡第" + (index + 1) + "条记录被拒绝");
                        cardInfo.setSendCount(99);
                        //更新上送状态
                        tradeDao.update(cardInfo);
                        if (hasNext()) {
                            cardInfo = tradeInfos.get(++index);
                            publishProgress(tradeInfos.size(), index + 1);
                            initData(cardInfo);
                            Object msgPkg = factory.packMessage(transCode, dataMap);
                            sendNext(transCode, (byte[]) msgPkg);
                        }
                    }
                } else {
                    logger.error("IC卡第" + (index + 1) + "条记录上送失败");
                    if (hasNext()) {
                        cardInfo = tradeInfos.get(++index);
                        publishProgress(tradeInfos.size(), index + 1);
                        initData(cardInfo);
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
        if (index + 1 < tradeInfos.size()) {
            return true;
        }
        return false;
    }
    private String getTransAmount(String amt){
        if (TextUtils.isEmpty(amt)) {
            return null;
        }
        String amountFormat;    //对小数点进行处理

        if (amt.indexOf('.') == -1) {
            long moneyInt = Long.parseLong(amt, 10);
            amountFormat = String.format(Locale.CHINA,"%010d00", moneyInt);
        } else {
            String moneyParts[] = amt.split("\\.");
            if (moneyParts.length > 3) {
                return null;
            }
            long moneyIntegralPart = Long.parseLong(moneyParts[0], 10);//整数部分处理
            String fractionalPartStr = moneyParts[1];//小数部分处理
            if (fractionalPartStr.length() > 2)
                fractionalPartStr = fractionalPartStr.substring(0,2);
            Long moneyFractionalPart = Long.parseLong(fractionalPartStr,10);
            amountFormat = String.format(Locale.CHINA,"%010d%02d",moneyIntegralPart,moneyFractionalPart);//格式化输出数据：10位整数+2位小数
        }
        return amountFormat;
    }
    /**
     * 是否是银联卡
     * @param cardOrg 卡组织代码
     * @return false or true
     */
    private boolean bIsCUPCard(String cardOrg){
        return ( cardOrg == null || "CUP".equals(cardOrg) || "000".equals(cardOrg) ) ;
    }
    private void initData(TradeInfoRecord icCard) {
        dataMap.clear();
        dataMap.put(TradeInformationTag.BANK_CARD_NUM, icCard.getCardNo());//主账号
        dataMap.put(TradeInformationTag.TRANS_MONEY, icCard.getAmount());//交易金额
        dataMap.put(TradeInformationTag.TRACE_NUMBER, icCard.getVoucherNo());//POS终端流水号
        dataMap.put(TradeInformationTag.SERVICE_ENTRY_MODE, icCard.getServiceEntryMode());//服务点输入方式码
        dataMap.put(TradeInformationTag.CARD_SEQUENCE_NUMBER, icCard.getCardSequenceNumber());//卡片序列号
        dataMap.put(TradeInformationTag.TERMINAL_IDENTIFICATION, icCard.getTerminalNo());//受卡机终端标识码
        dataMap.put(TradeInformationTag.MERCHANT_IDENTIFICATION, icCard.getMerchantNo());//受卡方标识码
        if (!icCard.getTransType().equals(TransCode.AUTH_COMPLETE)) {
            dataMap.put(TradeInformationTag.IC_DATA, icCard.getPbocOriTlvData());//IC卡数据域
        }
         /*BUGID:0003651: 对账不平，进行结算，
        *IC卡批上送上送的60.3网络信息码对账平204，对账不平206
        * */
        String iso60_3_NetManagerCode= "204";

        if( !"1".equals(key_is_balance_settle) || !"1".equals(key_is_balance_settle_foreign) ){
            iso60_3_NetManagerCode = "206";
        }
        dataMap.put(TradeInformationTag.CUSTOM_INFO_60, "00" + BusinessConfig.getInstance().getBatchNo(context) +
                iso60_3_NetManagerCode+"60");//60域
        String sAmt = getTransAmount(icCard.getAmount());
        sAmt = (sAmt==null) ? "000000000000" : sAmt;
        dataMap.put(TradeInformationTag.ORIGINAL_MESSAGE, "71"+(bIsCUPCard(icCard.getBankcardOganization()) ? "00" : "01")+"05" + sAmt + "156"+"22");//62域用法七
        dataMap.put(TradeInformationTag.MAC_MESSAGE, "1234567890123456");//mac填充占位
        int count = icCard.getSendCount();
        icCard.setSendCount(++count);
        //更改上送次数
        tradeDao.update(icCard);
    }
}
