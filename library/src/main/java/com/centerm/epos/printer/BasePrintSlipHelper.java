package com.centerm.epos.printer;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.centerm.cloudsys.sdk.common.utils.FileUtils;
import com.centerm.epos.EposApplication;
import com.centerm.epos.R;
import com.centerm.epos.bean.IntegralInfo;
import com.centerm.epos.bean.TradePrintData;
import com.centerm.epos.bean.transcation.BalancAmount;
import com.centerm.epos.bean.transcation.InstallmentInformation;
import com.centerm.epos.bean.transcation.TradeInformation;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.event.PrinteEvent;
import com.centerm.epos.event.TradeMessage;
import com.centerm.epos.transcation.pos.constant.BankNameMap;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.DataHelper;
import com.centerm.epos.utils.XLogUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import config.BusinessConfig;
import config.Config;

/**
 * Created by yuhc on 2017/4/5.
 */

public class BasePrintSlipHelper implements IPrintSlipHelper {
    private static final String TAG = BasePrintSlipHelper.class.getSimpleName();
    private boolean isPrintComplete = false;

    @Override
    public Map<String, String> trade2PrintData(Map<String, String> tradeData) {
        Map<String, String> printData = new HashMap<>();
        int resId;
        String tempString;

        BusinessConfig config = BusinessConfig.getInstance();
        Context appContext = EposApplication.getAppContext();
        String slipTitle = "POS签购单";
        boolean isUseDefaultTitle = config.getToggle(appContext, BusinessConfig.Key.TOGGLE_SLIP_TITLE_DEFAULT);
        if (!isUseDefaultTitle) {
            //获取自定义的凭条抬头内容
            String configSlipTitle = config.getValue(appContext, BusinessConfig.Key.KEY_SLIP_TITLE_CONTENT);
            if (!TextUtils.isEmpty(configSlipTitle))
                slipTitle = configSlipTitle;
        }
        printData.put("1F01", slipTitle);

        printData.put("1F04", tradeData.get(TradeInformationTag.MERCHANT_NAME));
        printData.put("1F05", tradeData.get(TradeInformationTag.MERCHANT_IDENTIFICATION));
        printData.put("1F06", tradeData.get(TradeInformationTag.TERMINAL_IDENTIFICATION));
        printData.put("1F07", tradeData.get(TradeInformationTag.OPERATOR_CODE));

        tempString = tradeData.get(TradeInformationTag.ISS_INSTITUTE);
        if (TextUtils.isEmpty(tempString))
            tempString = tradeData.get(TradeInformationTag.ISSUER_IDENTIFICATION);
        tempString = getBankName(tempString);
        printData.put("1F08", tempString);
        tempString = tradeData.get(TradeInformationTag.ACQ_INSTITUTE);
        if (TextUtils.isEmpty(tempString))
            tempString = tradeData.get(TradeInformationTag.ACQUIRER_IDENTIFICATION);
        tempString = getBankName(tempString);
        printData.put("1F09", tempString);
        tempString = tradeData.get(TradeInformationTag.BANK_CARD_NUM);

        String transType = tradeData.get(TradeInformationTag.TRANSACTION_TYPE);
        String inputMode = "/"+ getTranCardType(tradeData);
        /*
         * 卡号后面增加用卡方式
         */
        if (!TextUtils.isEmpty(tempString)) {
            printData.put("1F10", TransCode.AUTH.equals(transType) ? (DataHelper.formatCardNumBySpace(tempString)+inputMode) :
                    (DataHelper.formatCardno(tempString)+inputMode));
        }
        tempString = tradeData.get(TradeInformationTag.SCAN_CODE);
        if (!TextUtils.isEmpty(tempString))
            printData.put("1F21", DataHelper.formatCardno(tempString));
        printData.put("1F23", tradeData.get(TradeInformationTag.SCAN_VOUCHER_NO));
        printData.put("1F24", tradeData.get(TradeInformationTag.SCAN_VOUCHER_NO));
        /*
        * BUGID:0003649
        * author:zhouzhihua 2017.11.24
        * 修改打印单卡有效期：
        * 1、根据IC有效期规则yymm，如果yy>49,则年19yymm，否则为20yymm；
        * 2、磁条卡有效期比较复杂，ic卡的规则不一定适用；
        * 如果IC卡有效期yy>49正常情况是无法交易的，故有效期前补直接20
        * */
        if (tradeData.get(TradeInformationTag.DATE_EXPIRED) != null
                && (tradeData.get(TradeInformationTag.DATE_EXPIRED).length() == 4)) {
            String year = tradeData.get(TradeInformationTag.DATE_EXPIRED).substring(0, 2);
            String month = tradeData.get(TradeInformationTag.DATE_EXPIRED).substring(2, 4);
            if (tradeData.get(TradeInformationTag.DATE_EXPIRED).equals("0000")) {
                printData.put("1F11", " ");
            } else {
                printData.put("1F11", "20" + year + "/" + month);
            }
        } else {
            printData.put("1F11", tradeData.get(TradeInformationTag.DATE_EXPIRED));
        }

        resId = TransCode.codeMapName(transType);
        if(BusinessConfig.getInstance().getToggle(EposApplication.getAppContext(),BusinessConfig.Key.TOGGLE_SLIP_ENGLISH)) {
            printData.put("1F12", EposApplication.getAppContext().getString(resId) + "(" + EposApplication.getAppContext().getString(TransCode.codeMapNameEn(transType)) + ")");
        }else{
            printData.put("1F12", EposApplication.getAppContext().getString(resId));
        }
        printData.put("1F13", tradeData.get(TradeInformationTag.BATCH_NUMBER));
        printData.put("1F14", tradeData.get(TradeInformationTag.TRACE_NUMBER));
        tempString = tradeData.get(TradeInformationTag.AUTHORIZATION_IDENTIFICATION) == null ? "" : tradeData.get
                (TradeInformationTag.AUTHORIZATION_IDENTIFICATION);
        printData.put("1F15", tempString);
        printData.put("1F16", tradeData.get(TradeInformationTag.REFERENCE_NUMBER));
        String yearStr = tradeData.get(TradeInformationTag.TRANS_YEAR);
        String dateStr = tradeData.get(TradeInformationTag.TRANS_DATE);
        String timeStr = tradeData.get(TradeInformationTag.TRANS_TIME);
        String dateTimeStr = String.format(Locale.CHINA, "%s/%s/%s %s:%s:%s", yearStr, dateStr.substring(0, 2), dateStr
                .substring(2, 4), timeStr.substring(0, 2), timeStr.substring(2, 4), timeStr.substring(4, 6));
        printData.put("1F17", dateTimeStr);
        String money = tradeData.get(TradeInformationTag.TRANS_MONEY);
        printData.put("1F18", "RMB " + (TextUtils.isEmpty(money) ? "0.00" : money));

        //添加分期付款信息
        String string = referenceInformation(transType,tradeData);

        printData.put("1F19", string == null ? tradeData.get(TradeInformationTag.REFERENCE_INFORMATION) :
                tradeData.get(TradeInformationTag.REFERENCE_INFORMATION) == null ? string : tradeData.get
                        (TradeInformationTag.REFERENCE_INFORMATION) + "\n" + string);

        printData.put(TradeInformationTag.TRANSACTION_TYPE,transType);/*增加一个标签，用于判断交易类型*/

        addICTradeInformation(tradeData.get(TradeInformationTag.TRACE_NUMBER), printData);
        addRemarkInformation(tradeData, printData);

        if (TextUtils.isEmpty(printData.get("1F22"))) {
            //非免签时，再打印
            String signPicPath = ifElecSignThenGono(tradeData.get(TradeInformationTag.TRACE_NUMBER), tradeData.get
                    (TradeInformationTag.BATCH_NUMBER));
            if (TextUtils.isEmpty(signPicPath))
                printData.put("1F20", "\n\n");
            else
                printData.put("1F30", signPicPath);
        }
        return printData;
    }
    private String getReservationSalePrintfData(Map<String, String> tradeData){
        String string = tradeData.get(TradeInformationTag.ISO62_REQ);

        if( string == null ){
            return null;
        }
        int iLen = EposApplication.getAppContext().getResources().getInteger(R.integer.phone_number_max_len);
        String phoneString = string.substring(2,iLen).trim();
        XLogUtil.w("getReservationSalePrintfData","phoneString:"+phoneString);

        byte[] bytes = phoneString.getBytes();

        int iPadLen = (bytes.length > 6) ? (bytes.length-6) : (bytes.length-3);

        for( int i = 0 ; i < iPadLen ; i ++ ){
            bytes[i+3] = '*';
        }
        return ("\n手机号:"+ new String(bytes));
    }

    /**
     * 增加备注打印信息
     * @param transType 交易类型 <br/>
     * @param tradeData 交易数据 <br/>
     * @return 备注打印信息 <br/>
     */
    private String referenceInformation(String transType, Map<String, String> tradeData ){
        String string = "";
        int stateFlag;
        try {
            stateFlag = Integer.parseInt(tradeData.get(TradeInformationTag.TRANS_STATE_FLAG));
            XLogUtil.w(this.getClass().getSimpleName(),"TRANS_STATE_FLAGH:"+tradeData.get(TradeInformationTag.TRANS_STATE_FLAG));
        }catch (NumberFormatException e){
            stateFlag = 0;
            XLogUtil.w(this.getClass().getSimpleName(),"TRANS_STATE_FLAG get error !!!:"+tradeData.get(TradeInformationTag.TRANS_STATE_FLAG));
        }
        boolean bIsSupportEnglish = BusinessConfig.getInstance().getToggle(EposApplication.getAppContext(),BusinessConfig.Key.TOGGLE_SLIP_ENGLISH);
        if( stateFlag == 1 ){
            string = "\n" + (bIsSupportEnglish?"本交易已被撤销(VOIDED)":"本交易已被撤销");
        }
        else if(stateFlag == 2){
            string = "\n" + (bIsSupportEnglish?"本交易已被调整(ADJUSTED)":"本交易已被调整");
        }

        switch(transType){
            case TransCode.SALE_INSTALLMENT:
                string += getInstallmentPrintStr(tradeData.get(TradeInformationTag.TRACE_NUMBER));
                break;
            case TransCode.VOID_INSTALLMENT:
                string += getInstallmentPrintStr(tradeData.get(TransDataKey.key_oriVoucherNumber));
                break;
            case TransCode.ISS_INTEGRAL_SALE:
            case TransCode.UNION_INTEGRAL_SALE:
                IntegralInfo integralInfo = new IntegralInfo(tradeData.get(TradeInformationTag.ISO62_REQ),tradeData.get(TradeInformationTag.ISO62_RES));
                string += integralInfo.getIntegralPrinterData();
                break;
            case TransCode.RESERVATION_SALE:
                string += getReservationSalePrintfData(tradeData);
                break;
        }
        return string;
    }

    private void addRemarkInformation(Map<String, String> tradeData, Map<String, String> printData) {
        if (tradeData == null || printData == null)
            return;
        String transType = tradeData.get(TradeInformationTag.TRANSACTION_TYPE);
        /*
        * 离线结算不打印原授权码
        * */
        String tempString = TransCode.OFFLINE_SETTLEMENT.equals(transType) ? "" : tradeData.get(TransDataKey.key_oriAuthCode);
        if (!TextUtils.isEmpty(tempString))
            printData.put("1F28", tempString);
        tempString = tradeData.get(TransDataKey.key_oriReferenceNumber);
        if (!TextUtils.isEmpty(tempString))
            printData.put("1F26", tempString);
        tempString = tradeData.get(TransDataKey.key_oriTransDate);
        if (!TextUtils.isEmpty(tempString))
            printData.put("1F27", tempString);
        tempString = tradeData.get(TransDataKey.key_oriVoucherNumber);
        if (!TextUtils.isEmpty(tempString))
            printData.put("1F25", tempString + (transType.startsWith("VOID") ? "\n" : ""));

        XLogUtil.w("zhouzhihua","tradeData:"+tradeData);
        /*脱机退货使用*/
        tempString = tradeData.get(TransDataKey.KEY_ORI_TERMINAL_NO);
        if (!TextUtils.isEmpty(tempString))
            printData.put("1F2D", tempString);

        /*脱机退货使用*/
        tempString = tradeData.get(TransDataKey.KEY_ORI_BATCH_NO);
        if (!TextUtils.isEmpty(tempString))
            printData.put("1F2E", tempString);

        /*
        * 交易时后台可能返回余额值需要进行打印
        * author zhouzhihua
        * */
        if( tradeData.get(TradeInformationTag.BALANC_AMOUNT) != null ) {
            BalancAmount balancAmount = new BalancAmount(tradeData.get(TradeInformationTag.BALANC_AMOUNT));
            tradeData.put(TransDataKey.keyBalanceAmt,balancAmount.getAmountFormat());
        }

        String transCode = tradeData.get(TradeInformationTag.TRANSACTION_TYPE);
        XLogUtil.w("zhouzhihua","transCode:"+transCode);
        if( transCode.equals(TransCode.E_COMMON)
            || transCode.equals(TransCode.E_QUICK) ){
            tempString = tradeData.get(TradeInformationTag.EC_TRANS_BALANCE);
            XLogUtil.w("zhouzhihua","EC_TRANS_BALANCE:"+tempString);
            if( null != tempString )
                printData.put("1F2A", tempString);

        }else if( transCode.equals(TransCode.EC_LOAD_CASH)
                  || transCode.equals(TransCode.EC_LOAD_INNER)
                  || transCode.equals(TransCode.EC_LOAD_OUTER)){
            tempString = tradeData.get(TradeInformationTag.EC_TRANS_BALANCE);
            XLogUtil.w("zhouzhihua","EC_TRANS_BALANCE:"+tempString);
            if( null != tempString )
                printData.put("1F2C", tempString);
        }else if( transCode.equals(TransCode.ISS_INTEGRAL_SALE)
                  || transCode.equals(TransCode.ISS_INTEGRAL_VOID)
                  || transCode.equals(TransCode.UNION_INTEGRAL_VOID)
                  || transCode.equals(TransCode.UNION_INTEGRAL_SALE)
                  || transCode.equals(TransCode.UNION_INTEGRAL_REFUND) ){
            tempString = tradeData.get(TransDataKey.keyBalanceAmt);
            if( null != tempString )
                printData.put("1F50", tempString);
        }
        else {
            tempString = tradeData.get(TransDataKey.keyBalanceAmt);
            XLogUtil.w("zhouzhihua","keyBalanceAmt:"+tempString);
            if( null != tempString )
                printData.put("1F2B", tempString);
        }

        tempString = tradeData.get(TradeInformationTag.TRANSFER_INTO_CARD);
        if( null != tempString )
            printData.put("1F2F", tempString);


        /*附加信息栏*/
        tempString = tradeData.get(TradeInformationTag.REVERSE_FIELD);
        if (!TextUtils.isEmpty(tempString))
            printData.put("1F29", tempString);

    }


    @Nullable
    protected String getBankName(String tempString) {
        if (!TextUtils.isEmpty(tempString) && tempString.length() > 3) {
            String bankName;
//            tempString = tempString.trim();
            bankName = BankNameMap.getBankName(tempString.substring(0, 4));
            if (!TextUtils.isEmpty(bankName))
                tempString = bankName;
        }
        return tempString;
    }
    private boolean bIsEcLoadTransType(String transType)
    {
        return ( (null != transType) && (TransCode.EC_LOAD_CASH.equals(transType)
                                        || TransCode.EC_LOAD_INNER.equals(transType)
                                        || TransCode.EC_LOAD_OUTER.equals(transType)
                                        || TransCode.EC_VOID_CASH_LOAD.equals(transType)));
    }

    protected void addICTradeInformation(String tradeIndex, Map<String, String> printData) {
        try {
            CommonDao<TradePrintData> printDataCommonDao = new CommonDao<>(TradePrintData.class, DbHelper.getInstance
                    ());
            List printDatas = printDataCommonDao.queryBuilder().where().eq("iso_f11", tradeIndex).query();
            if (printDatas == null || printDatas.size() == 0) {
                DbHelper.releaseInstance();
                return;
            }
            String transType = printData.get(TradeInformationTag.TRANSACTION_TYPE);
            TradePrintData tradePrintData = (TradePrintData) printDatas.get(0);
            if( !bIsEcLoadTransType(transType) ) {
                printData.put("2F00", tradePrintData.getTc());
                printData.put("2F01", tradePrintData.getArqc());
                printData.put("2F02", tradePrintData.getTvr());
            }
            printData.put("2F03", tradePrintData.getAid());
            printData.put("2F04", tradePrintData.getAtc());
            if( !bIsEcLoadTransType(transType) ) {
                printData.put("2F05", tradePrintData.getUmpr_num());
                printData.put("2F06", tradePrintData.getAip());
                printData.put("2F07", tradePrintData.getIad());
            }
            if (!TextUtils.isEmpty(tradePrintData.getAmount()) && Double.valueOf(tradePrintData.getAmount()) > 0)
                printData.put("1F22", "交易金额不足" + tradePrintData.getAmount() + "元,无需签名\n");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        DbHelper.releaseInstance();
    }

    protected String getInstallmentPrintStr(String tradeIndex) {

        try {
            CommonDao<InstallmentInformation> printDataCommonDao = new CommonDao<>(InstallmentInformation.class, new
                    DbHelper(EposApplication.getAppContext()));
            List printDatas = printDataCommonDao.queryBuilder().where().eq("voucherNo", tradeIndex).query();
            if (printDatas == null || printDatas.size() == 0)
                return null;
            InstallmentInformation information = (InstallmentInformation) printDatas.get(0);
            return "\n" + information.formatInstallmentInfoForPrint();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 是否支持电子签名，且能读取到图片文件
     */
    protected String ifElecSignThenGono(String tradeIndex, String batchIndex) {
        if (TextUtils.isEmpty(tradeIndex) || TextUtils.isEmpty(batchIndex))
            return null;
//        Bitmap bitmap;
        if (Settings.getValue(EposApplication.getAppContext(), Settings.KEY.CAN_USE_ELECTRONIC_SIGN, BusinessConfig
                .CAN_USE_ELECTRONIC_SIGN)) {
            String path = Config.Path.SIGN_PATH + File.separator + batchIndex + "_" + tradeIndex + ".png";
            if (FileUtils.getFileSize(path) > 0) {
//                XLogUtil.d(TAG, "签名图片存在");
//                try {
//                    FileInputStream inputStream = new FileInputStream(path);
//                    bitmap = BitmapFactory.decodeStream(inputStream);
//                    return bitmap;
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
                return path;
            } else {
                XLogUtil.d(TAG, "签名图片不存在");
            }
        } else {
            XLogUtil.d(TAG, "电子签名不开启");
        }
        return null;
    }

    @Override
    public void setPrintComplete(boolean printComplete) {
        isPrintComplete = printComplete;
    }

    @Override
    public void onPrinting() {

    }

    @Override
    public void onFinish() {
        PrinteEvent event = new PrinteEvent();
        if (isPrintComplete) {
            event.setWhat(TradeMessage.PRINT_SLIP_COMPLETE);
        } else
            event.setWhat(TradeMessage.PRINT_NEXT_CONFIRM);
        EventBus.getDefault().post(event);
    }

    @Override
    public void onError(int errorCode, String errorInfo) {
        PrinteEvent event = new PrinteEvent(TradeMessage.PRINT_ERROR);
        String retInfo;
        if (TextUtils.isEmpty(errorInfo))
            retInfo = "" + errorCode;
        else
            retInfo = errorInfo;
        event.setMsg(retInfo);
        EventBus.getDefault().post(event);
    }
    /**
    * 获取用卡的类型 该类型跟在卡号后面
     * 二维码码支付不需要打印输入方式
     * {@link #trade2PrintData}<br/>
    * */
    @Override
    public String getTranCardType(Map<String, String> mapData) {
        if (null != mapData) {
            String serverCode = mapData.get(TradeInformationTag.SERVICE_ENTRY_MODE);
            if (null != serverCode && serverCode.length() > 0) {
                String s = serverCode.substring(0, 2);
                if (s.equals("02")) {
                    return "S";
                } else if (s.equals("05")) {
                    return "I";
                } else if (s.equals("07")) {
                    return "C";
                } else if ("01".equals(s)) {
                    return "M";
                } else if("92".equals(s)){
                    return "N";
                }else if ("03".equals(s)) {
//                    String transCode = mapData.get(TradeInformationTag.TRANSACTION_TYPE);
//                    String transEnName = null;
//                    switch (transCode) {
//                        case TransCode.SALE_SCAN:
//                        case TransCode.SCAN_PAY:
//                        case TransCode.SCAN_QUERY:
//                            transEnName = "SALE";
//                            break;
//                        case TransCode.VOID_SCAN:
//                        case TransCode.SCAN_VOID:
//                            transEnName = "VOID";
//                            break;
//                        case TransCode.REFUND_SCAN:
//                            transEnName = "REFUND";
//                            break;
//                    }
//                    return "Scan Code " + transEnName;
                    return "N";
                }

            }
        }
        return "未知";
    }
}
