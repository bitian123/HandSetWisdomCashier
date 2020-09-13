package com.centerm.epos.ebi.printer;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.centerm.cloudsys.sdk.common.utils.FileUtils;
import com.centerm.epos.EposApplication;
import com.centerm.epos.bean.TradePrintData;
import com.centerm.epos.bean.transcation.InstallmentInformation;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.ebi.keys.JsonKey;
import com.centerm.epos.ebi.msg.GetRequestData;
import com.centerm.epos.ebi.utils.DateUtil;
import com.centerm.epos.event.PrinteEvent;
import com.centerm.epos.event.TradeMessage;
import com.centerm.epos.printer.IPrintSlipHelper;
import com.centerm.epos.transcation.pos.constant.BankNameMap;
import com.centerm.epos.transcation.pos.constant.JsonKeyGT;
import com.centerm.epos.transcation.pos.constant.PrintType;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.DataHelper;
import com.centerm.epos.utils.XLogUtil;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
        String slipTitle = "交易签购单";
        boolean isUseDefaultTitle = config.getToggle(appContext, BusinessConfig.Key.TOGGLE_SLIP_TITLE_DEFAULT);
        if (!isUseDefaultTitle) {
            //获取自定义的凭条抬头内容
            String configSlipTitle = config.getValue(appContext, BusinessConfig.Key.KEY_SLIP_TITLE_CONTENT);
            if (!TextUtils.isEmpty(configSlipTitle))
                slipTitle = configSlipTitle;
        }
        printData.put("1F01", slipTitle);

        final String ROOT = Environment.getExternalStorageDirectory() + File.separator + "EPos";
        final String PRINTLOGO_PATH = ROOT + File.separator + "printlogo";
        //printData.put("1F011", PRINTLOGO_PATH + File.separator + "ebi_print_logo.bmp");

        printData.put("1F04", GetRequestData.getMerName());
        printData.put("1F05", GetRequestData.getMercode());
        printData.put("1F06", GetRequestData.getTermcde());
        if(tradeData.get(TradeInformationTag.OPERATOR_CODE)==null){
            printData.put("1F07", GetRequestData.getOperatorCode());
        }else {
            printData.put("1F07", tradeData.get(TradeInformationTag.OPERATOR_CODE));
        }


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
        if (!TextUtils.isEmpty(tempString)) {
            printData.put("1F10", TransCode.AUTH.equals(transType) ? tempString : DataHelper.formatCardno(tempString));
        }
        tempString = tradeData.get(TradeInformationTag.SCAN_CODE);
        if (!TextUtils.isEmpty(tempString))
            printData.put("1F21", DataHelper.formatCardno(tempString));
        printData.put("1F23", tradeData.get(TradeInformationTag.SCAN_VOUCHER_NO));
        printData.put("1F24", tradeData.get(TradeInformationTag.SCAN_VOUCHER_NO));
        if(TransCode.SALE.equals(transType)||transType.contains("SCAN")){
            if(tradeData.get(TradeInformationTag.TRANSFER_INTO_CARD)!=null||tradeData.get(JsonKey.out_order_no)!=null){
                String out_order_no = tradeData.get(JsonKey.out_order_no);
                if(out_order_no==null){
                    out_order_no = tradeData.get(TradeInformationTag.TRANSFER_INTO_CARD);
                }
                printData.put("1F241", out_order_no);
            }
        }
        /*
        * BUGID:0003649
        * author:zhouzhihua 2017.11.24
        * 修改打印单卡有效期：
        * 1、根据IC有效期规则yymm，如果yy>49,则年19yymm，否则为20yymm；
        * 2、磁条卡有效期比较复杂，ic卡的规则不一定适用；
        * 如果IC卡有效期yy>49正常情况是无法交易的，故有效期前补直接20
        * */
        if( tradeData.get(TradeInformationTag.DATE_EXPIRED) != null
            && (tradeData.get(TradeInformationTag.DATE_EXPIRED).length() == 4) ){
            String year = tradeData.get(TradeInformationTag.DATE_EXPIRED).substring(0, 2);
            String month = tradeData.get(TradeInformationTag.DATE_EXPIRED).substring(2, 4);
            if( tradeData.get(TradeInformationTag.DATE_EXPIRED).equals("0000") ) {
                printData.put("1F11", " ");
            }
            else {
                printData.put("1F11", "20" + year + "/" + month);
            }
        }
        else {
            printData.put("1F11", tradeData.get(TradeInformationTag.DATE_EXPIRED));
        }

        resId = TransCode.codeMapName(transType);
        if(transType.contains("SCAN")){
            printData.put("1F12", EposApplication.getAppContext().getResources().getString(resId));
            printData.put("1F121", GetRequestData.getPayType(tradeData.get(JsonKey.pay_type)));
            printData.put("1F09", "4872");//扫码交易收单行默认使用4872
        }else {
            String tranCardType = getTranCardType(tradeData);
            if(!TextUtils.isEmpty(tranCardType)){
                tranCardType = "  /" +tranCardType;
            }
            printData.put("1F12", EposApplication.getAppContext().getString(resId) + tranCardType);
        }

        printData.put("1F13", GetRequestData.getBatchNo());
        printData.put("1F14", tradeData.get(TradeInformationTag.TRACE_NUMBER));
        tempString = tradeData.get(TradeInformationTag.AUTHORIZATION_IDENTIFICATION) == null ? "" : tradeData.get
                (TradeInformationTag.AUTHORIZATION_IDENTIFICATION);
        //printData.put("1F15", tempString);
        printData.put("1F16", tradeData.get(TradeInformationTag.REFERENCE_NUMBER));
        if(!TextUtils.isEmpty(tradeData.get(TradeInformationTag.TRANS_YEAR))
                &&!TextUtils.isEmpty(tradeData.get(TradeInformationTag.TRANS_DATE))
                &&!TextUtils.isEmpty(tradeData.get(TradeInformationTag.TRANS_TIME))){
            String yearStr = tradeData.get(TradeInformationTag.TRANS_YEAR);
            String dateStr = tradeData.get(TradeInformationTag.TRANS_DATE);
            String timeStr = tradeData.get(TradeInformationTag.TRANS_TIME);
            String dateTimeStr = String.format(Locale.CHINA, "%s/%s/%s %s:%s:%s", yearStr, dateStr.substring(0, 2), dateStr
                    .substring(2, 4), timeStr.substring(0, 2), timeStr.substring(2, 4), timeStr.substring(4, 6));
            printData.put("1F17", dateTimeStr);
        }else if(tradeData.get(TradeInformationTag.TRANSFER_INTO_CARD)!=null||tradeData.get(JsonKey.out_order_no)!=null){
            String out_order_no = tradeData.get(JsonKey.out_order_no);
            if(out_order_no==null){
                out_order_no = tradeData.get(TradeInformationTag.TRANSFER_INTO_CARD);
            }
            if(!TextUtils.isEmpty(out_order_no)&&out_order_no.length()==19){
                String dateTimeStr = DateUtil.formatTime(out_order_no.substring(1,15),"yyyyMMddHHmmss", "yyyy/MM/dd HH:mm:ss");
                printData.put("1F17", dateTimeStr);
            }
        }
        String money = tradeData.get(TradeInformationTag.TRANS_MONEY);
        printData.put("1F18", "RMB " + (TextUtils.isEmpty(money) ? "0.00" : money));


        String type = tradeData.get(TradeInformationTag.TEMPLATE_ID);
        String businessType = tradeData.get(TradeInformationTag.SETTLEMENT_INFO);
        if(businessType!=null){
            if(businessType.contains("|")){
                String account = null;
                String[] accountInfo = null;
                if (!TextUtils.isEmpty(businessType)) {
                    accountInfo = businessType.split("[|]");
                    if (accountInfo[1].contains("公司")) {
                        account = accountInfo[1].substring(accountInfo[1].indexOf("公司") + 2);
                    } else {
                        account = accountInfo[1];
                    }
                }
                if (accountInfo.length > 2) {
                    businessType= accountInfo[2] + accountInfo[0] + account;
                } else {
                    businessType= accountInfo[0] + account;
                }
            }
            XLogUtil.d("type：", type);
            XLogUtil.d("打印业务类型：", businessType);
            if (!TextUtils.isEmpty(type)){
                if(PrintType.TYPE_2.equals(type)){
                    printData.put("1F31",  businessType);
                }
            }
        }


        //添加分期付款信息
        String installmentMsg = null;
        if (TransCode.SALE_INSTALLMENT.equals(transType))
            installmentMsg = getInstallmentPrintStr(tradeData.get(TradeInformationTag.TRACE_NUMBER));
        else if (TransCode.VOID_INSTALLMENT.equals(transType))
            installmentMsg = getInstallmentPrintStr(tradeData.get(TransDataKey.key_oriVoucherNumber));

        printData.put("1F19", installmentMsg == null ? tradeData.get(TradeInformationTag.REFERENCE_INFORMATION) :
                tradeData.get(TradeInformationTag.REFERENCE_INFORMATION) == null ? installmentMsg : tradeData.get
                        (TradeInformationTag.REFERENCE_INFORMATION) + "\n" + installmentMsg);

        if(TransCode.SALE.equals(transType)){
            String additionalStr = null;
            if(tradeData.get(JsonKeyGT.additionalData)!=null){
                additionalStr = tradeData.get(JsonKeyGT.additionalData);
            }else if(tradeData.get(TradeInformationTag.UNICOM_SCAN_TYPE)!=null){
                additionalStr = tradeData.get(TradeInformationTag.UNICOM_SCAN_TYPE);
            }
            if(!TextUtils.isEmpty(additionalStr)){
                try {
                    JSONObject object = new JSONObject(additionalStr);
                    StringBuilder builder = new StringBuilder();
                    builder.append("\n项目名称:"+object.optString("projectName"))
                            .append("\n")
                            .append("姓名:"+object.optString("name"))
                            .append("\n")
                            .append("证件号:"+object.optString("idNo"))
                            .append("\n\n");
                    if(object.optJSONArray("array")!=null){
                        JSONArray array = object.optJSONArray("array");
                        for(int i=0;i<array.length();i++){
                            JSONObject item = array.getJSONObject(i);
                            builder.append(item.optString("billId"))
                                    .append("  ")
                                    .append(item.optString("amt"))
                                    .append("\n");
                        }
                    }
                    printData.put("1F192", builder.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }

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

    private void addRemarkInformation(Map<String, String> tradeData, Map<String, String> printData) {
        if (tradeData == null || printData == null)
            return;
        String transType = tradeData.get(TradeInformationTag.TRANSACTION_TYPE);
        String tempString = tradeData.get(TransDataKey.key_oriAuthCode);
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
        tempString = tradeData.get(TradeInformationTag.REVERSE_FIELD);
        if (!TextUtils.isEmpty(tempString))
            printData.put("1F29", tempString);
    }

    @Nullable
    protected String getBankName(String tempString) {
        if (!TextUtils.isEmpty(tempString) && tempString.length() > 3) {
            String bankName;
            bankName = BankNameMap.getBankName(tempString.substring(0, 4));
            if (!TextUtils.isEmpty(bankName))
                tempString = bankName;
        }
        return tempString;
    }

    protected void addICTradeInformation(String tradeIndex, Map<String, String> printData) {
        try {
            CommonDao<TradePrintData> printDataCommonDao = new CommonDao<>(TradePrintData.class, DbHelper.getInstance());
            List printDatas = printDataCommonDao.queryBuilder().where().eq("iso_f11", tradeIndex).query();
            if (printDatas == null || printDatas.size() == 0) {
                DbHelper.releaseInstance();
                return;
            }
            TradePrintData tradePrintData = (TradePrintData) printDatas.get(0);
            printData.put("2F00", tradePrintData.getTc());
            printData.put("2F01", tradePrintData.getArqc());
            printData.put("2F02", tradePrintData.getTvr());
            printData.put("2F03", tradePrintData.getAid());
            printData.put("2F04", tradePrintData.getAtc());
            printData.put("2F05", tradePrintData.getUmpr_num());
            printData.put("2F06", tradePrintData.getAip());
            printData.put("2F07", tradePrintData.getIad());
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
                } else if ("03".equals(s)) {
                    String transCode = mapData.get(TradeInformationTag.TRANSACTION_TYPE);
                    String transEnName = null;
                    switch (transCode) {
                        case TransCode.SALE_SCAN:
                        case TransCode.SCAN_PAY:
                        case TransCode.SCAN_QUERY:
                            transEnName = "SALE";
                            break;
                        case TransCode.VOID_SCAN:
                        case TransCode.SCAN_VOID:
                            transEnName = "VOID";
                            break;
                        case TransCode.REFUND_SCAN:
                            transEnName = "REFUND";
                            break;
                    }
                    return "  /Scan Code " + transEnName;
                }

            }
        }
        return "";
    }

}
