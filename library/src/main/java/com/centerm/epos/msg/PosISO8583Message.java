package com.centerm.epos.msg;

import android.text.TextUtils;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;
import com.centerm.cpay.midsdk.dev.define.pinpad.EnumDataEncryMode;
import com.centerm.epos.EposApplication;
import com.centerm.epos.common.EncryptAlgorithmEnum;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.model.ITradeParameter;
import com.centerm.epos.redevelop.IRedevelopAction;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.transcation.pos.controller.ProcessRequestManager;
import com.centerm.epos.transcation.pos.data.BaseField35;
import com.centerm.epos.transcation.pos.data.I8583Field;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.SecurityTool;
import com.centerm.epos.xml.transaction.XmlConfigParse;
import com.centerm.smartpos.util.HexUtil;

import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.epos.common.TransCode.MAG_ACCOUNT_LOAD_VERIFY;
import static com.centerm.epos.common.TransCode.SALE_SCAN;
import static com.centerm.epos.common.TransCode.TRANS_CARD_DETAIL;
import static config.BusinessConfig.Key.TDK_KEY;
import static config.BusinessConfig.Key.TMK_KEY;

/**
 * Created by yuhc on 2017/2/23.
 * 传统POS的8385包报文处理
 */

public class PosISO8583Message extends BaseIso8583Message implements ITransactionMessage {
    private static final String TAG = PosISO8583Message.class.getSimpleName();

    private final static String MCT_FILE_PATH = ConfigureManager.getInstance(EposApplication.getAppContext())
            .getIsoMsgConfigFile();
    private static Logger logger = Logger.getLogger(PosISO8583Message.class);
    private static Map<String, Object> transData;

    private static final String FIELD_TAG_PREFIX = "iso_f";

    private Map<String, String> requestDataForIso8583;
    private Map<Integer,String> isoFieldClz;
    private String transCode = "";

    public PosISO8583Message(Map<String, Object> transDatas) {
        this.transData = transDatas;
        isoFieldClz = ConfigureManager.getInstance(EposApplication.getAppContext()).getFieldProcessClz();
    }

    public Map<String, String> getRequestDataForIso8583() {
        return requestDataForIso8583;
    }

    public void setRequestDataForIso8583(Map<String, String> requestDataForIso8583) {
        this.requestDataForIso8583 = requestDataForIso8583;
    }
    private String transCodeExchange(String code)
    {
        /*联盟积分查询和联盟积分退货 8583包和余额查询、退货一致
        * 积分交易添加
        * */
        switch (code)
        {
            case TransCode.UNION_INTEGRAL_BALANCE : return TransCode.BALANCE;
            case TransCode.UNION_INTEGRAL_REFUND: return TransCode.REFUND;

            case TransCode.UNION_INTEGRAL_SALE:
            case TransCode.ISS_INTEGRAL_SALE:
                return TransCode.INTEGRAL_SALE;

            case TransCode.UNION_INTEGRAL_VOID:
            case TransCode.ISS_INTEGRAL_VOID:
                return TransCode.INTEGRAL_VOID;

            case MAG_ACCOUNT_LOAD_VERIFY:
                return TransCode.MAG_ACCOUNT_VERIFY;

            case TransCode.RESERVATION_VOID:
                return TransCode.VOID;
        }
        return code;
    }

    @Override
    public Object packMessage(String transTag, Map<String, Object> transData) {
        String code = transTag;
        transCode = transTag;
        if (transData != null && transData.size() > 0){
            this.transData.putAll(transData);
        }
        if (transTag.endsWith(TransCode.REVERSE) || transTag.equals(TransCode.UPLOAD_SCRIPT_RESULT) ) {
            //冲正类业务处理
            if (requestDataForIso8583 == null)
                return null;
        } else {
            if (TransCode.DOWNLOAD_AID.equals(transTag)
                    || TransCode.DOWNLOAD_TERMINAL_PARAMETER.equals(transTag)
                    || TransCode.DOWNLOAD_CAPK.equals(transTag)
                    || TransCode.DOWNLOAD_QPS_PARAMS.equals(transTag)) {
                code = TransCode.DOWNLOAD_PARAMS;
                transData.put(TradeInformationTag.PARAMS_TYPE, transTag);
            }
            transData.put(TradeInformationTag.TRANSACTION_TYPE, code);
            /*联盟积分查询和联盟积分退货 8583包和余额查询、退货一致
            * 积分交易添加
            * */
            code = transCodeExchange(code);


            //针对配置多次交互的业务，获取本次交互的报文标识
            code = getMsgTagByTranTag(transData, code, false);
            int[] tradeFieldNums = XmlConfigParse.parseMsgPackFieldNums(EposApplication.getAppContext(), MCT_FILE_PATH,
                    code);
            if (tradeFieldNums == null || tradeFieldNums.length == 0) {
                logger.error("^_^ 打包数据时，获取所需数据域失败！^_^");
                return null;
            }
//        StringBuilder stringBuilder = new StringBuilder().append("^_^ 组包所需数据域：");
//        for (int i = 0; i < tradeFieldNums.length; i++) {
//            stringBuilder.append(tradeFieldNums[i]).append(" ");
//        }
//        logger.trace(stringBuilder.toString() + " ^_^");

            requestDataForIso8583 = new HashMap<>();
            String value;
            for (int i = 0; i < tradeFieldNums.length; i++) {
                try {
                    Class<?> clz = Class.forName(getFieldClassName(tradeFieldNums[i]));
                    Class[] parameterTypes = {java.util.Map.class};
                    Constructor constructor = clz.getConstructor(parameterTypes);
                    I8583Field i8583Field = (I8583Field) constructor.newInstance(transData);
                    value = i8583Field.encode();
                    if (!TextUtils.isEmpty(value)) {
                        requestDataForIso8583.put(FIELD_TAG_PREFIX + tradeFieldNums[i], value);
                    }
                } catch (Exception e) {
                    logger.error("^_^ " + tradeFieldNums[i] + "域数据生成失败：" + e.getMessage() + " ^_^");
                }
            }
        }
        IRedevelopAction msgHeadData = ConfigureManager.getRedevelopAction(IRedevelopAction.MSG_HEAD_DATA);
        requestDataForIso8583.put(TransDataKey.headerData, msgHeadData == null ? getHeaderData("0") : (String)
                msgHeadData.doAction("0"));
        try {
//            byte[] msg = addMessageLen(mapToIso8583(EposApplication.getAppContext(), MCT_FILE_PATH, code,
//                    requestDataForIso8583));
            byte[] msg = mapToIso8583(EposApplication.getAppContext(), MCT_FILE_PATH, code, requestDataForIso8583);
            logger.debug(HexUtils.bytesToHexString(msg));
            if("31".equals(getEncodeType())){
                String msgStr = HexUtils.bytesToHexString(msg);
                String encrypeStr = msgStr.substring(78);
                encrypeStr = HexUtils.bytesToHexString(
                        DeviceFactory.getInstance().getPinPadDev().encryData(EnumDataEncryMode.ECB, null, SecurityTool.formatLen(encrypeStr)));
                String result = msgStr.substring(0,78)+encrypeStr;
                return addMessageLen(HexUtils.hexStringToByte(result));
            }else {
                return addMessageLen(msg);
            }
        } catch (Exception e) {
            return null;
        }

    }

    private String getFieldClassName(int tradeFieldNum) {
        return isoFieldClz.get(tradeFieldNum);
//        return FIELD_CLASS_NAME_PREFIX + tradeFieldNum;
    }

    @Override
    public Map<String, Object> unPackMessage(String transTag, Object streamData) {
        if (transData == null)
            return null;
        String code = transTag;
        if (TransCode.DOWNLOAD_AID.equals(transTag)
                || TransCode.DOWNLOAD_TERMINAL_PARAMETER.equals(transTag)
                || TransCode.DOWNLOAD_CAPK.equals(transTag)
                || TransCode.DOWNLOAD_QPS_PARAMS.equals(transTag)) {
            code = TransCode.DOWNLOAD_PARAMS;
            transData.put(TradeInformationTag.PARAMS_TYPE, transTag);
        }
        transData.put(TradeInformationTag.TRANSACTION_TYPE, code);

        /*联盟积分查询和联盟积分退货 8583包和余额查询、退货一致
        * 积分交易添加
        * */
        code = transCodeExchange(code);

        //全报文解密
        if("31".equals(getEncodeType())){
            String enResult = HexUtils.bytesToHexString((byte[]) streamData);
            logger.debug(BusinessConfig.getInstance().getValue(EposApplication.getAppContext(),TDK_KEY));
            String result = SecurityTool.decrypt3DES(
                    BusinessConfig.getInstance().getValue(EposApplication.getAppContext(),TDK_KEY),
                    SecurityTool.formatLen(enResult.substring(82))
            );
//            try {
//                result = HexUtils.bytesToHexString(
//                        DeviceFactory.getInstance().getPinPadDev().encryData(EnumDataEncryMode.ECB, null, SecurityTool.formatLen(enResult.substring(82))));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            String lenStr = enResult.substring(74,82);
            lenStr = new String(HexUtils.hexStringToByte(lenStr));
            int len = Integer.parseInt(lenStr);
            result = enResult.substring(0,82)+result.substring(0,len*2);
            logger.debug("decode: "+result);
            streamData = HexUtils.hexStringToByte(result);
        }

        //针对配置多次交互的业务，获取本次交互的报文标识
        code = getMsgTagByTranTag(transData, code, true);
        Map<String, String> isoData = iso8583ToMap(EposApplication.getAppContext(), MCT_FILE_PATH, code,
                removeMessaageLen((byte[]) streamData));
        if (isoData == null) {
            return null;
        }

        int[] tradeFieldNums = XmlConfigParse.parseMsgUnPackFieldNums(EposApplication.getAppContext(), MCT_FILE_PATH,
                code);
        if (tradeFieldNums == null || tradeFieldNums.length == 0) {
            logger.error("^_^ 解包数据时，获取所需数据域失败！^_^");
            return null;
        }

        Map<String, Object> dataForIso8583 = new HashMap<>();
        Map<String, Object> fieldData;
        for (int i = 0; i < tradeFieldNums.length; i++) {
            try {
                Class<?> clz = Class.forName(getFieldClassName(tradeFieldNums[i]));
                Class[] parameterTypes = {java.util.Map.class};
                Constructor constructor = clz.getConstructor(parameterTypes);
                I8583Field i8583Field = (I8583Field) constructor.newInstance(transData);
                fieldData = i8583Field.decode(isoData.get(FIELD_TAG_PREFIX + tradeFieldNums[i]));
                if (fieldData != null && fieldData.size() > 0) {
                    dataForIso8583.putAll(fieldData);
                }
            } catch (Exception e) {
                logger.error("^_^ " + tradeFieldNums[i] + "域数据生成失败：" + e.getMessage() + " ^_^");
            }
        }
        dealWithMsgHeader(dataForIso8583, isoData);
        return dataForIso8583;
    }

    /**
     * 记录处理要求
     * @param
     */
    private void dealWithMsgHeader(Map<String, Object> dataForIso8583, Map<String, String> isoData) {
        String headData = isoData.get(TransDataKey.headerData);
        dataForIso8583.put(TradeInformationTag.MSG_HEADER, headData);
        if (TextUtils.isEmpty(headData) || headData.length() < 16)
            return;
        char tradeFalg = headData.charAt(15);
        logger.debug("^_^ 报文头处理要求："+tradeFalg+" ^_^");
        ProcessRequestManager.setProcessRequest(""+tradeFalg);
    }


    private String getHeaderData(String dealReq) {
        String status = "0";
        String mercherNO = "000000000000000";
        String termnalNO = "00000000";
        if(!TextUtils.isEmpty(BusinessConfig.getInstance().getIsoField(EposApplication.getAppContext(), 42))){
            mercherNO = BusinessConfig.getInstance().getIsoField(EposApplication.getAppContext(), 42);
            termnalNO = BusinessConfig.getInstance().getIsoField(EposApplication.getAppContext(), 41);
        }
        return Settings.getCommonTPDU(EposApplication.getAppContext()) //TPDU
                + BusinessConfig.HEADER_APP_TYPE//应用类型
                + BusinessConfig.HEADER_APP_VERSION //软件总版本号
                + status //终端状态
                + dealReq //处理要求
                + BusinessConfig.HEADER_APP_VERSION2//软件分版本号
                + getEncodeType()//加密方式
                + HexUtil.bytesToHexString(mercherNO.getBytes())//商户号
                + HexUtil.bytesToHexString(termnalNO.getBytes())//终端号
                + "00000000"//明文长度，在组包的时候会重新计算，这里只是占位
                ;
    }

    private String getMsgTagByTranTag(Map<String, Object> transData, String tranTag, boolean isRemove) {
        if (tranTag.endsWith("_REVERSE"))
            return tranTag;
        ArrayList<String> msgTags = (ArrayList<String>) transData.get(ITradeParameter.KEY_MSG_TAGS);
        if (msgTags != null && msgTags.size()>0){
            tranTag = msgTags.get(0);
            if (isRemove)
                msgTags.remove(0);
        }
        return tranTag;
    }

    /**
     * 加密方式 30:明文 31:全报文加密
     * POS终端类型为必须加密传输的终端在签到、参数下载远程主密钥下载时，可以使用明文
     * */
    public String getEncodeType(){
        if(!BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), BusinessConfig.Key.ENCODE_TYPE)){
            return "30";
        }else {
            if(TransCode.OBTAIN_TMK.equals(transCode)
                    ||TransCode.POS_STATUS_UPLOAD.equals(transCode)
                    ||TransCode.DOWNLOAD_CAPK.equals(transCode)
                    ||TransCode.DOWNLOAD_AID.equals(transCode)
                    ||TransCode.DOWNLOAD_PARAMS_FINISHED.equals(transCode)
                    ||TransCode.TRANS_FEFUND_DETAIL.equals(transCode)
                    ||TransCode.REFUND.equals(transCode)
                    ||TransCode.DOWNLOAD_TERMINAL_PARAMETER.equals(transCode)
                    ||TransCode.SIGN_IN.equals(transCode)){
                return "30";
            }
            return "31";
        }
    }
}
