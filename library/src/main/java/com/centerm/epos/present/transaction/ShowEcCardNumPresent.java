package com.centerm.epos.present.transaction;

import android.content.Context;
import com.centerm.cpay.midsdk.dev.common.utils.TlvUtils;
import com.centerm.cpay.midsdk.dev.define.IPbocService;
import com.centerm.cpay.midsdk.dev.define.pboc.EmvTag;
import com.centerm.cpay.midsdk.dev.define.pboc.EnumPbocResultType;
import com.centerm.epos.EposApplication;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.bean.TradePbocDetail;
import com.centerm.epos.bean.TradePrintData;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.StatusCode;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.CommonManager;
import com.centerm.epos.redevelop.ICommonManager;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.NewEmvTag;
import com.centerm.epos.utils.TlvUtil;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.epos.common.TransDataKey.iso_f39;


/**
 * author:wanliang527</br>
 * date:2017/2/20</br>
 */
public class ShowEcCardNumPresent extends BaseTradePresent {

    IPbocService pbocService = null;

    public ShowEcCardNumPresent(ITradeView mTradeView) {
        super(mTradeView);
        pbocService = mTradeInformation.getPbocService();
    }

    public void onConfirm(){
        mTradeInformation.getPbocService().importResult(EnumPbocResultType.CARD_INFO_CONFIRM, true);
        mTradeView.popLoading(Settings.bIsSettingBlueTheme() ? R.string.tip_ic_Read_processing : R.string.tip_ic_on_processing);
    }

    @Override
    public boolean onPbocImportPin(boolean bIsOffLinePin){
        mTradeInformation.setTransCode(TransCode.SALE);
        pbocService.importPIN(false, null);
        gotoNextStep();
        return true;
    }

    /**
     * @return 是否开启数据库模块
     */
    public boolean isOpenDataBase() {
        return true;
    }

    public List<EmvTag.Tag> getCardInfoTags() {
        List<EmvTag.Tag> tagList = new ArrayList<>();
        tagList.add(EmvTag.Tag._57);//二磁
        tagList.add(EmvTag.Tag._5F24);//卡片失效日期
        tagList.add(EmvTag.Tag._5F34);//卡序列号
        tagList.add(EmvTag.Tag._9F79);
        tagList.add(EmvTag.Tag._82);
        return tagList;
    }

    /**
     * 将ic卡数据保存到数据库
     */
    private void saveIcOfflinePrintData( Map<String, String> tempMap ) {
        String unKnown = null;
        String aid = null;
        String tc = null;
        String iad = null;
        String atc = null;
        String tvr = null;
        String tsi = null;
        String aip = null,csn = null;
        String limitAmount = null;

        String icData = tempMap.get(TransDataKey.KEY_IC_DATA_PRINT);
        if (null != icData) {
            Map<String, String> stringMap = TlvUtils.tlvToMap(icData);
            //不可预知数
            unKnown = stringMap.get("9F37");
            aid = stringMap.get("4F");
            tc = stringMap.get("9F26");
            iad = stringMap.get("9F10");
            atc = stringMap.get("9F36");
            tvr = stringMap.get("95");
            tsi = stringMap.get("9B");
            aip = stringMap.get("82");
        }
        TradePrintData tradePrintData = new TradePrintData();
        if (null != tempMap.get(TradeInformationTag.TRACE_NUMBER)) {
            tradePrintData.setIso_f11(tempMap.get(TradeInformationTag.TRACE_NUMBER));
        }
        if (null != tc) {
            tradePrintData.setTc(tc);
        }
        if (null != tvr) {
            tradePrintData.setTvr(tvr);
        }
        if (null != aid) {
            tradePrintData.setAid(aid);
        }
        if (null != atc) {
            tradePrintData.setAtc(atc);
        }
        if (null != tsi) {
            tradePrintData.setTsi(tsi);
        }
        if (null != unKnown) {
            tradePrintData.setUmpr_num(unKnown);
        }
        if (null != aip) {
            tradePrintData.setAip(aip);
        }
        if (null != iad) {
            tradePrintData.setIad(iad);
        }
        if (null != limitAmount) {
            tradePrintData.setAmount(limitAmount);
        }

        tradePrintData.setNoNeedSign(false);
        tradePrintData.setNoNeedPin(false);
        tradePrintData.setRePrint(false);
        CommonDao<TradePrintData> printDataCommonDao = new CommonDao<>(TradePrintData.class, dbHelper);
        printDataCommonDao.save(tradePrintData);
    }
    /**
     * 检查交易记录是否已经存满了，如果满了则置位标志
     */
    private void checkTradeStorage() {
        try {
            ICommonManager commonManager = (ICommonManager) ConfigureManager.getInstance(mTradeView.getHostActivity()).getSubPrjClassInstance(new CommonManager());
            long counts = commonManager.getBatchCount();
            long config = BusinessConfig.getInstance().getNumber(mTradeView.getHostActivity(),
                    BusinessConfig.Key.KEY_MAX_TRANSACTIONS);
            logger.info("已存储成功流水数量==>" + counts + "==>终端最大存储数量==>" + config);
            if (counts >= config) {
                logger.warn("交易流水数量超限==>下次联机前将进行批结算");
                BusinessConfig.getInstance().setFlag(mTradeView.getHostActivity(), BusinessConfig
                        .Key.FLAG_TRADE_STORAGE_WARNING, true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private Map<String, String> convertObject2String(Map<String, Object> objMap) {
        if (objMap == null || objMap.size() == 0)
            return null;
        Map<String, String> strMap = new HashMap<>();
        Iterator<Map.Entry<String, Object>> iterator = objMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            if (entry.getValue() instanceof String)
                strMap.put(entry.getKey(), (String) entry.getValue());
        }
        return strMap;
    }

    private boolean saveIccOffLineTrans(boolean bIsApproved){
        Context context = null;
        Map<String, String> cardInfo = pbocService.readKernelData(this.getCardInfoTags());
        context = mTradeView.getContext();

        logger.info("IC卡卡片信息读取成功：" + cardInfo.toString());

        String balance = cardInfo.get("9F79");

        if( null != balance ) {
            balance = String.format(Locale.CHINA, "%d.%02d", Long.parseLong(balance) / 100, Long.parseLong(balance) % 100);
        }
        transDatas.put(TradeInformationTag.EC_TRANS_BALANCE,balance);

        String tag57 = cardInfo.get("57");
        transDatas.put(TradeInformationTag.BANK_CARD_NUM, tag57.split("D")[0]);
        transDatas.put(TradeInformationTag.TRANSACTION_TYPE, getTradeCode());

        transDatas.put(TradeInformationTag.MERCHANT_NAME, BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), BusinessConfig.Key.KEY_MCHNT_NAME));
        transDatas.put(TradeInformationTag.TRANS_YEAR, String.format(Locale.CHINA, "%04d", Calendar.getInstance().get(Calendar.YEAR)));

        transDatas.put(TradeInformationTag.TERMINAL_IDENTIFICATION, BusinessConfig.getInstance().getIsoField(context, 41));
        transDatas.put(TradeInformationTag.MERCHANT_IDENTIFICATION, BusinessConfig.getInstance().getIsoField(context, 42));

        transDatas.put(TradeInformationTag.CARD_SEQUENCE_NUMBER,cardInfo.get("5F34"));

        String posSerial = BusinessConfig.getInstance().getPosSerial(context);
        transDatas.put(TradeInformationTag.TRACE_NUMBER, posSerial);
        transDatas.put(TradeInformationTag.BATCH_NUMBER, BusinessConfig.getInstance().getBatchNo(context));

        String operatorID = BusinessConfig.getInstance().getValue(mTradeView.getContext(), BusinessConfig.Key.KEY_OPER_ID);
        tempMap.put(TradeInformationTag.OPERATOR_CODE, operatorID);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String tradeDate = formatter.format(new Date());

        transDatas.put(TradeInformationTag.TRANS_TIME, tradeDate.substring(8, 14)); //保存脱机交易时间
        transDatas.put(TradeInformationTag.TRANS_DATE, tradeDate.substring(4, 8)); //保存脱机交易时间
        transDatas.put(TradeInformationTag.TRACK_2_DATA, tag57);
        transDatas.put(TradeInformationTag.DATE_SETTLEMENT,null);
        transDatas.put(TradeInformationTag.REFERENCE_NUMBER,null);

        String expiry = cardInfo.get("5F24");
        if (expiry != null && expiry.length() == 6) {
            expiry = expiry.substring(0, 4);
        }
        transDatas.put(TradeInformationTag.DATE_EXPIRED, expiry);
        transDatas.put(TradeInformationTag.CARD_SEQUENCE_NUMBER, cardInfo.get("5F34"));

        String iso55 = pbocService.readTlvKernelData(NewEmvTag.getF55TagsOffLine()); //读55域数据

        Map<String,String> map = TlvUtils.tlvToMap(iso55);
        map.put("9F27",bIsApproved ? "40" : "00");
        iso55 = TlvUtil.mapToTlv(map);
        transDatas.put(TradeInformationTag.IC_DATA, iso55 + ( bIsApproved ? "8A025931" : "8A025A31" ));//电子现金手动补Y1 ,拒绝Z1

        String print = pbocService.readTlvKernelData(EmvTag.getTagsForPrint()); //读取打印数据
        print += "8202"+cardInfo.get("82");
        logger.debug("ic卡打印信息+55域为：" + print + "/" + iso55);
        transDatas.put(TradeInformationTag.RESPONSE_CODE, "00");

        transDatas.put(TransDataKey.KEY_IC_DATA_PRINT, print);
        transDatas.put(TradeInformationTag.CURRENCY_CODE,"156");

        transDatas.put(TradeInformationTag.BANKCARD_ORGANIZATION,"CUP");
        transDatas.put(TradeInformationTag.CREDIT_CODE,"CUP");

        tempMap.putAll(convertObject2String(transDatas));
        mTradeInformation.setRespDataMap(transDatas);/*离线交易发送包和接受包数据一致*/
        this.saveIcOfflinePrintData(tempMap);

        logger.warn("transDatas:" + transDatas.toString());

        if(TransCode.NEED_INSERT_TABLE_SETS.contains(getTradeCode())){
            CommonDao<TradeInfoRecord> tradeDao = new CommonDao<>(TradeInfoRecord.class, dbHelper);
            TradeInfoRecord curTradeInfo = new TradeInfoRecord(getTradeCode(), transDatas);
            TradePbocDetail pbocDetail = new TradePbocDetail(TlvUtils.tlvToMap(tempMap.get(TransDataKey.KEY_IC_DATA_PRINT)));
            curTradeInfo.setTransStatus(bIsApproved ? 0 : 0x0800);
            logger.warn("pbocDetail:" + pbocDetail.convert2Map());
            curTradeInfo.setPbocDetail(pbocDetail);
            curTradeInfo.getPbocDetail().setECInfoEx(tempMap);
            tradeDao.save(curTradeInfo);
        }
        this.checkTradeStorage();
        return true;
    }
    /*
    * 脱机消费拒绝,脱机拒绝的交易在批结算时要上送拒绝的交易
    * CID 9f27 0x00  AAC
    * */
    @Override
    public boolean onPbocTradeRefused(){
        if( getTradeCode().equals(TransCode.E_QUICK)
            || getTradeCode().equals(TransCode.E_COMMON) ){
            logger.info("脱机消费 onPbocTradeRefused");
            saveIccOffLineTrans(false);
            putResponseCode(StatusCode.TRADING_REFUSED);
            gotoNextStep("99");
            return true;
        }
        return false;
    }
    /*
    * 脱机消费批准
    * CID 9f27 0x40 TC
    * */
    @Override
    public boolean onPbocTradeApproved(){
        if( getTradeCode().equals(TransCode.E_QUICK)
            || getTradeCode().equals(TransCode.E_COMMON) ){
            logger.info("脱机消费 onPbocTradeApproved");
            saveIccOffLineTrans(true);
            putResponseCode("00","电子现金消费成功");
            gotoNextStep("2");
            return true;
        }
        return false;
    }

}
