package com.centerm.epos.present.transaction;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.common.utils.TlvUtils;
import com.centerm.cpay.midsdk.dev.define.IPbocService;
import com.centerm.cpay.midsdk.dev.define.pboc.EmvTag;
import com.centerm.epos.EposApplication;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.bean.TradePbocDetail;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.CommonManager;
import com.centerm.epos.redevelop.ICommonManager;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.NewEmvTag;
import com.centerm.epos.utils.TlvUtil;
import com.centerm.epos.utils.XLogUtil;
import com.centerm.epos.view.AlertDialog;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.epos.common.TransCode.AUTH_COMPLETE;
import static com.centerm.epos.common.TransCode.REFUND;
import static com.centerm.epos.common.TransCode.REFUND_SCAN;
import static com.centerm.epos.common.TransCode.UNION_INTEGRAL_REFUND;
import static com.centerm.epos.common.TransDataKey.FLAG_IMPORT_AMOUNT;
import static com.centerm.epos.common.TransDataKey.KEY_IC_DATA_PRINT;
import static com.centerm.epos.common.TransDataKey.iso_f39;

/**
 * author:wanliang527</br>
 * date:2017/2/20</br>
 */
public class InputMoneyPresent extends BaseTradePresent {

    public InputMoneyPresent(ITradeView mTradeView) {
        super(mTradeView);
    }

    public void onConfirmClick(final String amt) {

        if (Double.valueOf(amt) == 0) {
            mTradeView.popToast(R.string.tip_input_money2);
            return;
        }
        if ( (REFUND.equals(mTradeInformation.getTransCode()) || TransCode.E_REFUND.equals(mTradeInformation.getTransCode())) && Double.valueOf(amt) > BusinessConfig
                .REFUND_AMOUNT_LIMITED) {
            mTradeView.popToast(R.string.tip_refund_over_limited);
            return;
        }
        String code = mTradeInformation.getTransCode();
        if (REFUND.equals(mTradeInformation.getTransCode()) || TransCode.E_REFUND.equals(mTradeInformation.getTransCode())
                || UNION_INTEGRAL_REFUND.equals(code)) {
            mTradeView.popMessageBox("请确认", "退货金额：" + amt + "元", new AlertDialog.ButtonClickListener() {
                @Override
                public void onClick(AlertDialog.ButtonType button, View v) {
                    if (button.equals(AlertDialog.ButtonType.POSITIVE)) {
                        goNexStep(amt);
                    }
                }
            });
        } else {
            goNexStep(amt);
        }
    }

    @Override
    public boolean isEnableShowingTimeout() {
        return true;
    }

    @Override
    public Object onConfirm(Object paramObj) {
        final String amt = (String) paramObj;
        if (Double.valueOf(amt) == 0) {
            mTradeView.popToast(R.string.tip_input_money2);
            return null;
        }
        double amountLimit = Double.parseDouble(BusinessConfig.getInstance().getValue(mTradeView.getHostActivity(),
                BusinessConfig.Key.REFUND_AMOUNT_LIMITED));
        if ( (REFUND.equals(mTradeInformation.getTransCode())|| TransCode.E_REFUND.equals(mTradeInformation.getTransCode()) ) && Double.valueOf(amt) > amountLimit) {
            mTradeView.popToast(R.string.tip_refund_over_limited);
            return null;
        }
        String code = mTradeInformation.getTransCode();
        code = ( code == null ) ? "" : code;
        if ( REFUND.equals(mTradeInformation.getTransCode()) || REFUND_SCAN.equals(mTradeInformation.getTransCode())
             || TransCode.E_REFUND.equals(mTradeInformation.getTransCode())
                || code.endsWith("REFUND") ) {
            mTradeView.showSelectDialog("请确认", "退货金额：" + amt + "元", new AlertDialog.ButtonClickListener() {
                @Override
                public void onClick(AlertDialog.ButtonType button, View v) {
                    if (button.equals(AlertDialog.ButtonType.POSITIVE)) {
                        goNexStep(amt);
                    }
                }
            });
        }else if( TransCode.MAG_CASH_LOAD.equals(mTradeInformation.getTransCode()) || TransCode.MAG_ACCOUNT_LOAD.equals(mTradeInformation.getTransCode()) ){
            String magLoadAmount = mTradeInformation.getTempMap().get(TradeInformationTag.ISO62_RES);
            magLoadAmount = String.format(Locale.CHINA,"%d.%02d",Long.parseLong(magLoadAmount)/100,Long.parseLong(magLoadAmount)%100);
            if( Double.valueOf(amt) <= Double.valueOf(magLoadAmount) ){
                goNexStep(amt);
            }
            else{
                String amountTip = ("可充值金额:" + magLoadAmount );
                mTradeView.popToast(amountTip);
            }
        }
        else if( TransCode.OFFLINE_SETTLEMENT.equals(mTradeInformation.getTransCode())  ){
            transDatas.put(TradeInformationTag.TRANS_MONEY, amt);
            saveOfflineTrans(mTradeInformation.getTransCode());
            gotoNextStep();
        }
        else {
            goNexStep(amt);
        }
        return null;
    }

    private void goNexStep(String amt) {
        if ("1".equals(transDatas.get(FLAG_IMPORT_AMOUNT))) {
            try {
                IPbocService pbocService = DeviceFactory.getInstance().getPbocService();
                pbocService.importAmount(amt);
                transDatas.put(FLAG_IMPORT_AMOUNT, "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        transDatas.put(TradeInformationTag.TRANS_MONEY, amt);
        if (AUTH_COMPLETE.equals(mTradeInformation.getTransCode())&& !BusinessConfig.getInstance().getFlag(mTradeView.getHostActivity(), BusinessConfig.Key.TOGGLE_AUTH_COMPLETE_INPUTWD)){
            gotoNextStep("2");
        }else {
            gotoNextStep();
        }
    }
    @Override
    public boolean isOpenDataBase() {
        return true;
    }

    private boolean  saveOfflineTrans(String transCode){
        Context context = mTradeView.getContext();

        transDatas.put(TradeInformationTag.TRANSACTION_TYPE, transCode);

        transDatas.put(TradeInformationTag.MERCHANT_NAME, BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), BusinessConfig.Key.KEY_MCHNT_NAME));
        transDatas.put(TradeInformationTag.TRANS_YEAR, String.format(Locale.CHINA, "%04d", Calendar.getInstance().get(Calendar.YEAR)));

        transDatas.put(TradeInformationTag.TERMINAL_IDENTIFICATION, BusinessConfig.getInstance().getIsoField(context, 41));
        transDatas.put(TradeInformationTag.MERCHANT_IDENTIFICATION, BusinessConfig.getInstance().getIsoField(context, 42));


        String posSerial = BusinessConfig.getInstance().getPosSerial(context);
        transDatas.put(TradeInformationTag.TRACE_NUMBER, posSerial);
        transDatas.put(TradeInformationTag.BATCH_NUMBER, BusinessConfig.getInstance().getBatchNo(context));

        String operatorID = BusinessConfig.getInstance().getValue(mTradeView.getContext(), BusinessConfig.Key.KEY_OPER_ID);
        tempMap.put(TradeInformationTag.OPERATOR_CODE, operatorID);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String tradeDate = formatter.format(new Date());

        transDatas.put(TradeInformationTag.TRANS_TIME, tradeDate.substring(8, 14));
        transDatas.put(TradeInformationTag.TRANS_DATE, tradeDate.substring(4, 8));
        transDatas.put(TradeInformationTag.DATE_SETTLEMENT,null);
        transDatas.put(TradeInformationTag.REFERENCE_NUMBER,null);

        transDatas.put(TradeInformationTag.RESPONSE_CODE, "00");

        transDatas.put(TradeInformationTag.CURRENCY_CODE,"156");

        transDatas.put(TradeInformationTag.BANKCARD_ORGANIZATION,transDatas.get(TradeInformationTag.CREDIT_CARD_COMPANY_CODE));

        tempMap.putAll(convertObject2String(transDatas));
        tempMap.put(iso_f39,"00");

        mTradeInformation.setRespDataMap(transDatas);/*离线交易发送包和接受包数据一致*/

        logger.warn("transDatas:" + transDatas.toString());

        if(TransCode.NEED_INSERT_TABLE_SETS.contains(getTradeCode())){
            CommonDao<TradeInfoRecord> tradeDao = new CommonDao<>(TradeInfoRecord.class, dbHelper);
            TradeInfoRecord curTradeInfo = new TradeInfoRecord(transCode, transDatas);
            curTradeInfo.setTransStatus(0);
            tradeDao.save(curTradeInfo);
        }
        this.checkTradeStorage();
        return true;
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
}