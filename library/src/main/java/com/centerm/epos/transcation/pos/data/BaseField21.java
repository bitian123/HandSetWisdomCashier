package com.centerm.epos.transcation.pos.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.define.ISystemService;
import com.centerm.epos.EposApplication;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.BytesUtil;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DataHelper;
import com.centerm.epos.utils.XLogUtil;

import org.apache.log4j.Logger;

import java.util.Map;

import config.BusinessConfig;

/**
 * Created by liubit on 2018/1/3.<br>
 * 终端信息
 */

public class BaseField21 implements I8583Field {
    private Logger logger = Logger.getLogger(this.getClass());
    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField21.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String,Object> tradeInfo;

    public BaseField21(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }
    /**
     * 从业务数据中取出服务点输入方式码，并根据规范要求输出3个Byte的ASC码数据。
     * @return  有效期，例如：
     */
    @Override
    public String encode() {
        if (tradeInfo == null){
            XLogUtil.e(TAG, "^_^ encode 输入参数 tradeInfo 为空 ^_^");
            return null;
        }
        String sn = CommonUtils.SN_CODE;
        String currBs = "";//基站信息
        String apnInfo = "192.168.17.102";//APN信息

        if(CommonUtils.isK9()){
            DeviceFactory factory = DeviceFactory.getInstance();
            try {
                ISystemService service = factory.getSystemDev();
                sn = service.getTerminalSn();
                if(BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), BusinessConfig.Key.SET_SN_HAND)){
                    if(TextUtils.isEmpty(BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), TransDataKey.key_sn))){
                        sn = CommonUtils.SN_CODE;
                    }else {
                        sn = BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), TransDataKey.key_sn);
                    }
                }
                apnInfo = DataHelper.getSimSerialNumber();
                Log.d(TAG, "apn1: "+apnInfo);
                if(TextUtils.isEmpty(apnInfo)){
                    apnInfo = service.getIpAddr();
                    Log.d(TAG, "apn2: "+apnInfo);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            sn = CommonUtils.getSn();
            apnInfo = DataHelper.getSimSerialNumber();
            Log.d(TAG, "apn1: "+apnInfo);
            if(TextUtils.isEmpty(apnInfo)){
                apnInfo = "0.0.0.0";
                Log.d(TAG, "apn2: "+apnInfo);
            }
        }
        if(!TextUtils.isEmpty(CommonUtils.SN_CODE)){
            sn = CommonUtils.SN_CODE;
        }
        if(BusinessConfig.getInstance().getToggle(EposApplication.getAppContext(), BusinessConfig.Key.TOGGLE_UPLOAD_BASE_STATION)){
            //currBs = getSSBI(EposApplication.getAppContext());
            currBs = getStationInfo(EposApplication.getAppContext());
        }
        String s = appendLen(sn) + appendLen(currBs) + appendLen(apnInfo);
        logger.info("^_^ 21 encode result:" + s + " ^_^");
        return s;
    }

    public static String appendLen(String content){
        if(TextUtils.isEmpty(content)||TextUtils.equals("null", content)){
            return "00";
        }
        int len = content.length();
        String dataLen = DataHelper.fillLeftZero(""+len,2);
        return dataLen+content;
    }

    /**
     * 占位，平台不返回此域
     *
     * @param fieldMsg null
     * @return null
     */
    @Override
    public Map<String, Object> decode(String fieldMsg) {
        return null;
    }

    public static String getRerseIso21(){
        String sn = "";
        String currBs = "";//基站信息
        String apnInfo = "192.168.17.102";//APN信息

        DeviceFactory factory = DeviceFactory.getInstance();
        try {
            ISystemService service = factory.getSystemDev();
            sn = service.getTerminalSn();
            if(BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), BusinessConfig.Key.SET_SN_HAND)){
                if(TextUtils.isEmpty(BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), TransDataKey.key_sn))){
                    sn = CommonUtils.SN_CODE;
                }else {
                    sn = BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), TransDataKey.key_sn);
                }
            }
            apnInfo = DataHelper.getSimSerialNumber();
            Log.d(TAG, "apn: "+apnInfo);
            if(TextUtils.isEmpty(apnInfo)){
                apnInfo = service.getIpAddr();
                Log.d(TAG, "apn: "+apnInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //currBs = getSSBI(EposApplication.getAppContext());
        String s = appendLen(sn) + appendLen(currBs) + appendLen(apnInfo);
        return s;
    }

    //获取sim卡的msi和基站信息
    @SuppressLint("MissingPermission")
    public String getSSBI(Context context){
//        if(CommonUtils.isWifi(context)){
//            return null;
//        }
        String ssbi = "";
        String location="",MncMcc,lac="",cid="";
        //获取sim卡序列号TelephoneManager
        TelephonyManager manager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        CellLocation cel = manager.getCellLocation();
        //获取sim卡的序列卡号
        String simSerialNumber = manager.getSimSerialNumber();
        if(TextUtils.isEmpty(simSerialNumber)){
            return null;
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
                    logger.info("location = " + location);
                    lac = gsmCellLocation.getLac()+"";
                    cid = nGSMCID+"";
                }
            }
            ssbi += "|BI="+location;
            return ssbi;
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
        //return ssbi;
        return lac+cid+mcc+mnc;
    }

    /**
     * 获取基站信息
     * 格式:MCC|MNC|LAC|CID
     * */
    @SuppressLint("MissingPermission")
    public String getStationInfo(Context context){
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
                    logger.info("location = " + location);
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
}
