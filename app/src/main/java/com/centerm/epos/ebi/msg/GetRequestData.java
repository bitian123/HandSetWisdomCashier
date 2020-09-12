package com.centerm.epos.ebi.msg;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;
import com.centerm.cpay.midsdk.dev.define.ISystemService;
import com.centerm.cpay.midsdk.dev.define.pinpad.EnumDataEncryMode;
import com.centerm.cpay.midsdk.dev.define.system.APNBean;
import com.centerm.epos.EposApplication;
import com.centerm.epos.common.EncryptAlgorithmEnum;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.ebi.common.PayResult;
import com.centerm.epos.ebi.common.ScanRefundResult;
import com.centerm.epos.ebi.keys.JsonKey;
import com.centerm.epos.ebi.utils.DateUtil;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DataHelper;
import com.centerm.epos.utils.XLogUtil;
import com.centerm.smartpos.util.HexUtil;

import org.apache.log4j.Logger;

import java.util.Map;

import config.BusinessConfig;

import static com.centerm.cpay.midsdk.dev.define.BaseInterface.logger;
import static com.centerm.epos.common.EncryptAlgorithmEnum.SM4;
import static com.centerm.epos.common.EncryptAlgorithmEnum.nameOf;
import static com.centerm.epos.ebi.common.TransCode.SALE_SCAN;
import static com.centerm.epos.ebi.common.TransCode.SALE_SCAN_QUERY;
import static com.centerm.epos.ebi.common.TransCode.SALE_SCAN_REFUND;
import static com.centerm.epos.ebi.common.TransCode.SALE_SCAN_REFUND_QUERY;
import static com.centerm.epos.ebi.common.TransCode.SALE_SCAN_VOID;
import static com.centerm.epos.ebi.common.TransCode.SALE_SCAN_VOID_QUERY;

/**
 * Created by liubit on 2017/12/25.
 */

public class GetRequestData {
    private static Logger logger = Logger.getLogger(GetRequestData.class);
    private static final String TAG = GetRequestData.class.getSimpleName();

    /**
     * 获取sn号
     * */
    public static String getSn(){
        if(!CommonUtils.isK9()){
            return BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), BusinessConfig.Key.E10_SN);
        }
        DeviceFactory factory = DeviceFactory.getInstance();
        try {
            ISystemService service = factory.getSystemDev();
            String sn = service.getTerminalSn();
            if(BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), BusinessConfig.Key.SET_SN_HAND)){
                if(TextUtils.isEmpty(BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), TransDataKey.key_sn))){
                    sn = CommonUtils.SN_CODE;
                }else {
                    sn = BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), TransDataKey.key_sn);
                }
            }
            return sn;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取imei
     * */
    public static String getImei(){
        DeviceFactory factory = DeviceFactory.getInstance();
        try {
            ISystemService service = factory.getSystemDev();
            String imei = service.getIMEI();
            return imei;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取基站信息
     * 格式:MCC|MNC|LAC|CID
     * */
    @SuppressLint("MissingPermission")
    public static String getStationInfo(Context context){
        String ssbi = "";
        String location="",MncMcc,lac="",cid="";
        //获取sim卡序列号TelephoneManager
        TelephonyManager manager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        CellLocation cel = manager.getCellLocation();
        //获取sim卡的序列卡号
        String simSerialNumber = manager.getSimSerialNumber();
        if(TextUtils.isEmpty(simSerialNumber)){
            return "";
        }
        ssbi += "SS=" + simSerialNumber;
        String operator = manager.getNetworkOperator();
        if(TextUtils.isEmpty(operator)){
            return "";
        }
        logger.info("simSerialNumber = "+simSerialNumber);
        logger.info("operator = "+operator);
        int mcc = Integer.parseInt(operator.substring(0, 3));
        int mnc = Integer.parseInt(operator.substring(3));
        if (mnc > 9) {
            MncMcc = ""+mnc+","+mcc;
        } else {
            MncMcc = "0"+mnc+","+mcc;
        }
        //LAC + CID + MCC + MNC
        logger.debug("MncMcc:"+MncMcc);
        //移动联通 GsmCellLocation
        if (cel instanceof GsmCellLocation) {
            logger.info("移动联通");
            GsmCellLocation gsmCellLocation = (GsmCellLocation) cel;
            int nGSMCID = gsmCellLocation.getCid();
            if (nGSMCID > 0) {
                if (nGSMCID != 65535) {
                    logger.info("cell = " + nGSMCID);
                    logger.info("lac = " + gsmCellLocation.getLac());
                    logger.info("cell = " + Integer.toHexString(nGSMCID));
                    logger.info("lac = " + Integer.toHexString(gsmCellLocation.getLac()));
                    location = Integer.toHexString(nGSMCID)+","+MncMcc+"," + Integer.toHexString(gsmCellLocation.getLac());
                    logger.info("location" + location);
                    lac = gsmCellLocation.getLac()+"";
                    cid = nGSMCID+"";
                }
            }
            //ssbi += "|BI="+location;
            StringBuilder builder = new StringBuilder();
            builder.append(mcc);
            builder.append("|");
            builder.append(DataHelper.formatToXLen(mnc,2));
            builder.append("|");
            builder.append(gsmCellLocation.getLac());
            builder.append("|");
            builder.append(nGSMCID);
            return builder.toString();
        }
        //电信   CdmaCellLocation
        if ((mnc == 3 ||mnc==5||mnc==11) ) {
            logger.info("电信");
            CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) cel;
            if(cdmaCellLocation==null)
                return null;
            int sid = cdmaCellLocation.getSystemId();
            int nid = cdmaCellLocation.getNetworkId();
            int bid = cdmaCellLocation.getBaseStationId();
            logger.info("sid = " + sid);
            logger.info("nid = " + nid);
            logger.info("bid = " + bid);
            logger.info("sid16 = " + Integer.toHexString(sid));
            logger.info("nid16 = " + Integer.toHexString(nid));
            logger.info("bid16 = " + Integer.toHexString(bid));
            location = mcc+","+Integer.toHexString(sid)+"," + Integer.toHexString(nid)+","+Integer.toHexString(bid);
            ssbi += "|BI="+location;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(mcc);
        builder.append("|");
        builder.append(DataHelper.formatToXLen(mnc,2));
        builder.append("|");
        builder.append(lac);
        builder.append("|");
        builder.append(cid);
        return builder.toString();
    }

    /**
     * 获取商户号
     * */
    public static String getMercode(){
        return BusinessConfig.getInstance().getIsoField(EposApplication.getAppContext(), 42);
    }

    /**
     * 获取终端号
     * */
    public static String getTermcde(){
        return BusinessConfig.getInstance().getIsoField(EposApplication.getAppContext(), 41);
    }

    /**
     * 获取商户名
     * */
    public static String getMerName(){
        return BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), BusinessConfig.Key.KEY_MCHNT_NAME);
    }

    /**
     * 获取商户英文名
     * */
    public static String getMerEnglishName(){
        return BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), BusinessConfig.Key.KEY_MCHNT_ENGLISH_NAME);
    }

    /**
     * 生成订单号
     * */
    public static String creatOrderNo(){
        String orderNo = "C"+ DateUtil.getToday("yyyyMMddHHmmss")+getRandom(4);
        return orderNo;
    }

    public static String getRandom(int n){
        StringBuilder strRand = new StringBuilder();
        for(int i=0;i<n;i++){
            strRand.append(String.valueOf((int)(Math.random() * 10)));
        }
        return strRand.toString();
    }

    public static String getMak(){
        return Settings.getValue(EposApplication.getAppContext(), JsonKey.MAK, "");
    }

    /**
     * 流水号
     * */
    public static String getTraceNumber(Map<String, Object> tradeInfo){
        if (tradeInfo == null) {
            XLogUtil.e("getTraceNumber", "^_^ encode 输入参数 tradeInfo 为空 ^_^");
            return "";
        }
        String traceNumber = (String) tradeInfo.get(TradeInformationTag.TRACE_NUMBER);

        if (TextUtils.isEmpty(traceNumber)) {
            //未传入流水号，此处再进行获取
            traceNumber = BusinessConfig.getInstance().getPosSerial(EposApplication.getAppContext());
        }
        tradeInfo.put(TradeInformationTag.TRACE_NUMBER, traceNumber);
        XLogUtil.d("getTraceNumber", "^_^ encode result:" + traceNumber + " ^_^");
        return traceNumber;
    }

    /**
     * 批次号
     * */
    public static String getBatchNo(){
        return BusinessConfig.getInstance().getBatchNo(EposApplication
                .getAppContext());
    }

    /**
     * 操作员
     * */
    public static String getOperatorCode(){
        return BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), BusinessConfig.Key.KEY_OPER_ID);
    }

    /**
     * 获取apn
     * */
    public static String getApn(){
        DeviceFactory factory = DeviceFactory.getInstance();
        try {
            ISystemService service = factory.getSystemDev();
            APNBean apn = service.getDefaultAPN();
            return apn.getApn();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取交易名称
     * */
    public static String getTransName(String transCode, String payType){
        //01:微信 02:支付宝 03:银联
        String transType = "未定义";
        if(TextUtils.isEmpty(transCode)){
            return transType;
        }
        String transName = EposApplication.getAppContext().getResources().getString(com.centerm.epos.common.TransCode.codeMapName(transCode));
        if(!TextUtils.isEmpty(payType)){
            switch (payType){
                case "01":
                    transType = "微信" + transName;
                    break;
                case "02":
                    transType = "支付宝" + transName;
                    break;
                case "03":
                    transType = "银联" + transName;
                    break;
                default:
                    transType = transName;
                    break;
            }
        }else {
            transType = transName;
        }
        return transType;
    }

    public static String getPayType(String payType){
        //01:微信 02:支付宝 03:银联
        String transType = "未定义";
        if(!TextUtils.isEmpty(payType)){
            switch (payType){
                case "01":
                    transType = "微信";
                    break;
                case "02":
                    transType = "支付宝";
                    break;
                case "03":
                    transType = "银联";
                    break;
                default:
                    break;
            }
        }
        return transType;
    }


    /**
     * 获取交易状态
     * */
    public static String getPayStatus(String transCode, String status){
        String payStatus = "未知";
        if(TextUtils.isEmpty(transCode)||TextUtils.isEmpty(status)){
            return payStatus;
        }
        switch (status){
            case "F":
                payStatus = PayResult.F.getDes();
                break;
            case "O":
                payStatus = PayResult.O.getDes();
                break;
            case "R":
                payStatus = PayResult.R.getDes();
                break;
            case "S":
                payStatus = PayResult.S.getDes();
                break;
            case "I":
                payStatus = PayResult.I.getDes();
                break;
            case "N":
            case "P5":
                payStatus = PayResult.P5.getDes();
                break;
            case "P3":
                payStatus = PayResult.P3.getDes();
                break;
            case "P4":
                payStatus = PayResult.P4.getDes();
                break;
            case "P6":
                payStatus = PayResult.P6.getDes();
                break;
            case "P7":
                payStatus = PayResult.P7.getDes();
                break;
            case "P8":
                payStatus = PayResult.P8.getDes();
                break;
            case "P9":
                payStatus = PayResult.P9.getDes();
                break;
            case "PA":
                payStatus = PayResult.PA.getDes();
                break;
            case "PB":
                payStatus = PayResult.PB.getDes();
                break;
            case "PC":
                payStatus = PayResult.PC.getDes();
                break;
            case "PD":
                payStatus = PayResult.PD.getDes();
                break;
            case "PE":
                payStatus = PayResult.PE.getDes();
                break;
            case "PH":
                payStatus = PayResult.PH.getDes();
                break;
            case "PI":
                payStatus = PayResult.PI.getDes();
                break;
            default:
                break;
        }
        return payStatus;
    }

}
