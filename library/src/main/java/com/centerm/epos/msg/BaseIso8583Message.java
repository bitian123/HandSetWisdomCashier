package com.centerm.epos.msg;

import android.content.Context;

import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;
import com.centerm.cpay.midsdk.dev.define.IPinPadDev;
import com.centerm.cpay.midsdk.dev.define.pinpad.EnumMacType;
import com.centerm.dev.util.SecurityUtil;
import com.centerm.epos.EposApplication;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.redevelop.IMacAlgorithm;
import com.centerm.epos.redevelop.UnionPayMacAlgorithm;
import com.centerm.epos.transcation.pos.data.BaseField20;
import com.centerm.epos.transcation.pos.data.BaseField21;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DataHelper;
import com.centerm.epos.xml.bean.RedevelopItem;
import com.centerm.epos.xml.keys.Keys;
import com.centerm.iso8583.IsoMessage;
import com.centerm.iso8583.bean.Field;
import com.centerm.iso8583.bean.FieldDataParseBean;
import com.centerm.iso8583.bean.FormatInfo;
import com.centerm.iso8583.bean.FormatInfoFactory;
import com.centerm.iso8583.bean.Head;
import com.centerm.iso8583.enums.IsoMessageMode;
import com.centerm.iso8583.parse.IsoConfigParser;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import config.Config;

/**
 * author:wanliang527</br>
 * date:2016/10/28</br>
 */

public class BaseIso8583Message {

    protected Logger logger = Logger.getLogger(getClass());

    protected byte[] mapToIso8583(Context context, String mctFile, String transCode, Map<String, String> data) {
        FormatInfoFactory factory;
        IsoConfigParser parser = new IsoConfigParser();
        String code = transCode;
        if (TransCode.DOWNLOAD_AID.equals(transCode)
                || TransCode.DOWNLOAD_CAPK.equals(transCode)
                || TransCode.DOWNLOAD_QPS_PARAMS.equals(transCode)) {
            code = TransCode.DOWNLOAD_PARAMS;
        }
        try {
            factory = parser.parseFromInputStream(context.getAssets().open(mctFile));
            FormatInfo info = factory.getFormatInfo(code, IsoMessageMode.PACK);
            if(code.contains("REVERSE")){
                //冲正交易，添加20，21域数据
                data.put(TransDataKey.iso_f20, BaseField20.getRerseIso20(code));
                data.put(TransDataKey.iso_f21, BaseField21.getRerseIso21());
            }
            IsoMessage message = com.centerm.iso8583.MessageFactory.getIso8583Message().packTrns(data, info);

            //修改47域数据长度格式
            if(message.getFieldMap().get(47)!=null){
                String iso47 = HexUtils.bytesToHexString(message.getFieldMap().get(47));
                iso47 = iso47.substring(4);
                int len = iso47.length()/2;
                String lenStr = DataHelper.fillLeftZero(len+"", 4);
                iso47 = lenStr+iso47;
                Map<Integer, byte[]> map = message.getFieldMap();
                map.put(47, HexUtils.hexStringToByte(iso47));
                message.setFieldMap(map);
            }

            //报文头添加数据明文长度
            int len = HexUtils.bytesToHexString(message.getAllFieldData()).length()/2+10;
            String dataLen = DataHelper.fillLeftZero(""+len, 4);
            dataLen = HexUtils.bytesToHexString(dataLen.getBytes());
            String head = HexUtils.bytesToHexString(message.getHeader());
            head = head.substring(0, head.length()-8);
            message.setHeader(HexUtils.hexStringToByte(head+dataLen));

            byte[] messageData = message.getAllMessageByteData();
            if (messageData != null && !TransCode.NO_MAC_SETS.contains(code)) {
                byte[] macBlock = splitMacBlock(messageData);
                //非管理类交易需要添加MAC域
                IMacAlgorithm macAlgorithm = getMacAlgorithm();
                if (macAlgorithm != null && macBlock != null) {
//                    byte[] macData = HexUtils.hexStringToByte(pinPadDev.getMac(EnumMacType.BOC_EXTENED, HexUtils
//                            .bytesToHexString(splitMacBlock(messageData)), null));
                    String mac = macAlgorithm.calculateMessageMac(HexUtils.bytesToHexString(macBlock));
                    byte[] macData = HexUtils.hexStringToByte(mac);
                    data.put(TransDataKey.iso_f64, mac);
                    logger.info(code + "==>MAC计算结果==>" + mac);
                    System.arraycopy(macData, 0, messageData, messageData.length - 8, 8);
                } else {
                    logger.warn(code + "==>MAC计算失败");
                }
            }
            return messageData;
        } catch (Exception e) {
            logger.error("^_^ 报文组织错误："+e.getMessage()+" ^_^");
            e.printStackTrace();
        }
        return null;
    }

    protected Map<String, String> iso8583ToMap(Context context, String mctFile, String transCode, byte[] iso8583) {
        IsoConfigParser xmlParser = new IsoConfigParser();
        FormatInfoFactory formatInfoFactory;
        Map<String, String> map = null;
        String code = transCode;
        if (TransCode.DOWNLOAD_AID.equals(transCode)
                || TransCode.DOWNLOAD_CAPK.equals(transCode)
                || TransCode.DOWNLOAD_QPS_PARAMS.equals(transCode)) {
            code = TransCode.DOWNLOAD_PARAMS;
        }
        try {
            byte[] macBlock = splitMacBlock(iso8583);
            if (macBlock != null && !TransCode.NO_MAC_SETS.contains(code)) {
                IMacAlgorithm macAlgorithm = getMacAlgorithm();
                if (macAlgorithm != null) {
                    String macData = macAlgorithm.calculateMessageMac(HexUtils.bytesToHexString(macBlock));
//                    String macData = pinPadDev.getMac(EnumMacType.BOC_EXTENED, HexUtils.bytesToHexString(macBlock), null);
                    logger.info(code + "==>Local MAC==>" + macData);
                    byte[] respMac = new byte[8];
                    if (iso8583.length < 8) {
                        System.arraycopy(iso8583, 0, respMac, 0, iso8583.length);
                    } else {
                        System.arraycopy(iso8583, iso8583.length - 8, respMac, 0, 8);
                    }
                    logger.info(code + "==>Respone MAC==>" + HexUtils.bcd2str(respMac));
                    if (macData.equals(HexUtils.bcd2str(respMac))) {
                        formatInfoFactory = xmlParser.parseFromInputStream(context
                                .getAssets().open(mctFile));
                        FormatInfo formatInfo = formatInfoFactory.getFormatInfo(code, IsoMessageMode.UNPACK);
                        map = com.centerm.iso8583.MessageFactory.getIso8583Message().unPackTrns(iso8583, formatInfo);
                    } else {
                        logger.error(code + "校验错，请重新签到" );
                        //MAC 校验错，直接返回空
                        return null;
                    }
                    map.put(TransDataKey.keyLocalMac, macData);
                    map.put(TransDataKey.iso_f64, HexUtils.bytesToHexString(respMac));
                } else {
                    logger.warn(code + "==>计算本地MAC失败==>pinPadDev is null");
                    map.put(TransDataKey.keyLocalMac, "");
                }
            } else {
                logger.warn(code + "==>该交易类型无需计算MAC");
                formatInfoFactory = xmlParser.parseFromInputStream(context
                        .getAssets().open(mctFile));
                FormatInfo formatInfo = formatInfoFactory.getFormatInfo(code, IsoMessageMode.UNPACK);
                map = com.centerm.iso8583.MessageFactory.getIso8583Message().unPackTrns(iso8583, formatInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 获取MAC算法
     * @return
     */
    private IMacAlgorithm getMacAlgorithm() {
        Context appContext = EposApplication.getAppContext();
        IMacAlgorithm macAlgorithm = null;//非管理类交易需要验证MAC
        RedevelopItem calMacItem = ConfigureManager.getInstance(appContext).getRedevelopItem(appContext, Keys
                .obj().redevelop_mac_algorithm);
        if (calMacItem == null)
            macAlgorithm = new UnionPayMacAlgorithm();
        else {
            try {
                Object clz = Class.forName(calMacItem.getClassName()).newInstance();
                if (clz instanceof IMacAlgorithm)
                    macAlgorithm = (IMacAlgorithm) clz;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return macAlgorithm;
    }

    /**
     * 分离需要计算MAC的数据域
     *
     * @param returnData 后台返回数据
     * @return 需要计算MAC的数据域
     */
    private byte[] splitMacBlock(byte[] returnData) {
        int len1 = 39;//TPDU+报文头，压缩后39字节
        int len2 = 2;//消息类型，压缩后2字节
        int len3 = 8;//位图，压缩后8字节
        int len4 = 8;//mac域，8字节
        int len = returnData.length;
        if (len < len1 + len4) {
            logger.warn("报文长度非法，无法计算MAC");
            return returnData;
        }
        byte macBit = returnData[len1+len2+len3-1];
        if ((macBit & 0x01) == 0)
            return null;
        byte[] macBlock = new byte[len - len1 - len4];
//        logger.debug("未截取前的数据==> " + HexUtils.bytesToHexString(returnData));
        System.arraycopy(returnData, len1, macBlock, 0, macBlock.length);
        logger.debug("截取需要计算MAC的数据段==> " + HexUtils.bytesToHexString(macBlock));
        return macBlock;
    }


/*    protected String combineTerminalSn() {
        DeviceFactory factory.xml = DeviceFactory.getInstance();
        try {
            ISystemService service = factory.xml.getSystemDev();
            String tag = "Sequence No";
            String sn = service.getTerminalSn();
            int len = (BusinessConfig.NET_LISCENSE_NO + sn).length();
            String str = tag + len + sn;
            logger.warn("Sn TLV == " + str);
            String str1 = HexUtils.bytesToHexString(str.getBytes());
            logger.warn("转成16进制：" + str1);
            return str1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }*/

    /**
     * 添加两字节报文长度
     *
     * @param message 原报文
     * @return 新报文
     */
    public byte[] addMessageLen(byte[] message) {
        int iLen = message.length;
        byte[] targets = new byte[]{(byte) (iLen / 256), (byte) (iLen % 256)};
        byte[] msg = new byte[iLen + 2];

        System.arraycopy(targets, 0, msg, 0, 2); // 拷贝长度
        System.arraycopy(message, 0, msg, 2, iLen); // 拷贝报文
        return msg;
    }

    /**
     * 去除两字节报文长度
     *
     * @param message 原报文
     * @return 新报文
     */
    public byte[] removeMessaageLen(byte[] message) {
        if (message == null || message.length < 2) {
            return message;
        }
        byte[] msg = new byte[message.length - 2];
        System.arraycopy(message, 2, msg, 0, msg.length);
        return msg;
    }

}
