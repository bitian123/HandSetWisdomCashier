package com.centerm.epos.task;

import android.content.Context;
import android.text.TextUtils;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;
import com.centerm.cpay.midsdk.dev.define.IPinPadDev;
import com.centerm.cpay.midsdk.dev.define.pinpad.EnumWorkKeyType;
import com.centerm.epos.bean.PrinterItem;
import com.centerm.epos.common.EncryptAlgorithmEnum;
import com.centerm.epos.common.ISORespCode;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.StatusCode;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.net.ResponseHandler;
import com.centerm.epos.present.communication.DataExchanger;
import com.centerm.epos.present.communication.DataExchangerFactory;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.CommonUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;

/**
 * 异步签到任务。签到后需要解析报文头，根据报文头的处理要求，做下一步操作
 * author:wanliang527</br>
 * date:2016/12/1</br>
 */

public class AsyncSignTask extends AsyncMultiRequestTask {

    //private Map<String, String> stringMap = new HashMap<>();
    private Map<String, Object> returnMap;

    public AsyncSignTask(Context context, Map<String, Object> dataMap, Map<String, Object> returnMap) {
        super(context, dataMap);
        //initParamTip();
        this.returnMap = returnMap;
        if (this.returnMap == null) {
            this.returnMap = new HashMap<>();
        }
    }

    @Override
    protected String[] doInBackground(String... params) {
//        sleep(LONG_SLEEP);
        Object msgPkg = factory.packMessage(TransCode.SIGN_IN, dataMap);
        ResponseHandler handler = new ResponseHandler() {
            @Override
            public void onSuccess(String statusCode, String msg, byte[] data) {
                Map<String, Object> mapData = factory.unPackMessage(TransCode.SIGN_IN, data);
                returnMap.putAll(mapData);
                String respCode = (String) mapData.get(TradeInformationTag.RESPONSE_CODE);
                ISORespCode isoCode = ISORespCode.codeMap(respCode);
                taskResult[0] = isoCode.getCode();
                taskResult[1] = context.getString(isoCode.getResId());
                if ("00".equals(respCode)) {
                    //更新批次号
                    String iso60 = (String) mapData.get(TradeInformationTag.CUSTOM_INFO_60);
                    if (iso60.length() > 8) {
                        String batch = iso60.substring(2, 8);
                        logger.info("更新批次号==>本地批次号：" + BusinessConfig.getInstance().getBatchNo(context) + "==>平台批次号："
                                + batch);
                        BusinessConfig.getInstance().setBatchNo(context, batch);
                    }
                    //发散工作密钥
                    String workKey = (String) mapData.get(TradeInformationTag.WORK_KEY);
                    ;
                    String pik;
                    String mak;
                    String tdk = null;

                    EncryptAlgorithmEnum encAlg = Settings.getEncryptAlgorithmEnum(context);
                    if (encAlg == EncryptAlgorithmEnum.DES) {
                        pik = workKey.substring(0, 24);
                        mak = workKey.substring(24, 48);
                        if (BusinessConfig.isTrackEncrypt(context))
                            tdk = workKey.substring(48, 72);
                    } else {
                        pik = workKey.substring(0, 40);
                        mak = workKey.substring(40, 80);
                        if (BusinessConfig.isTrackEncrypt(context))
                            tdk = workKey.substring(80, 120);
                    }

                    int keyLenOfStr;
                    if (encAlg == EncryptAlgorithmEnum.DES) {
                        keyLenOfStr = 16;
                    } else {
                        keyLenOfStr = 32;
                    }
                    String pikValue = pik.substring(0, keyLenOfStr);
                    String pikCheckValue = pik.substring(keyLenOfStr, keyLenOfStr + 8);
                    String makValue = mak.substring(0, keyLenOfStr);
                    if ((encAlg != EncryptAlgorithmEnum.DES) && makValue.endsWith("0000000000000000")) {
                        logger.info("MAK为单倍长");
                        String left8Bytes = mak.substring(0, 16);
                        makValue = left8Bytes + left8Bytes;
                    }

                    String makCheckValue = mak.substring(keyLenOfStr, keyLenOfStr + 8);
                    String tdkValue = tdk == null ? null : tdk.substring(0, keyLenOfStr);
                    String tdkCheckValue = tdk == null ? null : tdk.substring(keyLenOfStr, keyLenOfStr + 8);

                    //密钥键盘只支持16 byte的密钥下载，所以要组成16 byte
                    if (encAlg == EncryptAlgorithmEnum.DES) {
                        pikValue = pikValue + pikValue;
                        makValue = makValue + makValue;
                        if (tdkCheckValue != null)
                            tdkValue = tdkValue + tdkValue;
                    }

                    logger.debug("pik == " + pikValue + "   " + pikCheckValue);
                    logger.debug("mak == " + makValue + "   " + makCheckValue);
                    logger.debug("tdk == " + tdkValue + "   " + tdkCheckValue);
                    taskRetryTimes = 0;
                    if (loadWorkKey(pikValue, pikCheckValue, makValue, makCheckValue, tdkValue, tdkCheckValue)) {
                        //重置批结算标识
                        Settings.setValue(context, Settings.KEY.BATCH_SEND_STATUS, "0");
                        String tranDate = (String) mapData.get(TradeInformationTag.TRANS_DATE);
                        if (!TextUtils.isEmpty(tranDate)) {
                            Settings.setValue(context, Settings.KEY.SIGN_IN_DATE, tranDate);
                            if (BusinessConfig.getInstance().getFlag(context, BusinessConfig.Key.TOGGLE_SYNC_TIME)) {
                                try {
                                    Calendar calendar = Calendar.getInstance();
                                    int year = calendar.get(Calendar.YEAR);
                                    String time = (String) mapData.get(TradeInformationTag.TRANS_TIME);
                                    String date = (String) mapData.get(TradeInformationTag.TRANS_DATE);
                                    String dateTime = year + date + time;
                                    logger.debug("^_^ 同步平台时间：" + dateTime + " ^_^");
                                    DeviceFactory.getInstance().getSystemDev().updateSysTime(dateTime);
                                } catch (Exception e) {
                                    logger.error(e.getMessage());
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(String code, String msg, Throwable error) {
                taskResult[0] = code;
                taskResult[1] = msg;
            }
        };
//        client.syncSendData((byte[]) msgPkg, handler);
        try {
//            DataExchanger dataExchanger = new DataExchanger(ICommunication.COMM_TCP, new TcpCommParameter
//                    ());
            DataExchanger dataExchanger = DataExchangerFactory.getInstance();
            byte[] receivedData = dataExchanger.doExchange((byte[]) msgPkg);
            if (receivedData == null) {
                logger.error("^_^ 接收数据失败！ ^_^");
                handler.onFailure("99", "接收数据失败！", null);
            } else {
                handler.onSuccess(null, null, receivedData);
            }
        } catch (Exception e) {
            logger.error("^_^ 数据交换失败：" + e.getMessage() + " ^_^");
            e.printStackTrace();
            taskResult[0] = "99";
            taskResult[1] = "数据交换失败";
        }
        return taskResult;
    }

    /**
     * 对比签购单版本，如果版本不一致，获取最新版本，保存到数据库，并且保留最新版本号
     */
    private void compareAndSaveVersion(String version, String slipVersion) {
        String localVersion = Settings.getSlipVersion(context);
        if (!localVersion.equals(version)) {
            List<PrinterItem> printerItems = new ArrayList<>();
            String[] strings = slipVersion.split("(?=9F..)");
            try {
                for (int i = 1; i < strings.length; i++) {
                    String paramId = strings[i].substring(0, 4);
                    String paramValue = new String(HexUtils.hexStringToByte(strings[i].substring(6, strings[i].length
                            () - 4)), "GBK");
                    String textSize = strings[i].substring(strings[i].length() - 4, strings[i].length() - 2);
                    String range = strings[i].substring(strings[i].length() - 2, strings[i].length());
                    // PrinterItem printerItem = new PrinterItem(stringMap.get(paramId), paramValue, paramId, Integer
                    // .parseInt(textSize), Integer.parseInt(range));
                    PrinterItem printerItem = new PrinterItem(paramValue, paramValue, paramId, Integer.parseInt
                            (textSize), Integer.parseInt(range));
                    printerItems.add(printerItem);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            CommonDao commonDao = new CommonDao(PrinterItem.class, DbHelper.getInstance());
            Settings.setSlipVersion(context, version);
            boolean isDel = commonDao.deleteByWhere(null);
            if (isDel) {
                logger.debug("签购单表清空成功！");
                boolean issave = commonDao.save(printerItems);
                if (issave) {
                    logger.debug("新版本签购单保存成功！");
                }
            }
            DbHelper.releaseInstance();
        }
    }

    /**
     * 发散工作密钥
     *
     * @param pik pik
     * @param pikCheck 校验值
     * @param mak mak
     * @param makCheck 校验值
     * @param tdk mak
     * @param tdkCheck 校验值
     */
    public boolean loadWorkKey(String pik, String pikCheck, String mak, String makCheck, String tdk, String tdkCheck) {
        boolean r1, r2, r3;
        boolean result = false;
        IPinPadDev pinPadDev = CommonUtils.getPinPadDev();
        if (pinPadDev != null) {
            EncryptAlgorithmEnum encAlg = Settings.getEncryptAlgorithmEnum(context);
            r1 = pinPadDev.loadWorkKey(encAlg == EncryptAlgorithmEnum.SM4 ? EnumWorkKeyType.SM4_PIK : EnumWorkKeyType
                    .PIK, pik, pikCheck);
            r2 = pinPadDev.loadWorkKey(encAlg == EncryptAlgorithmEnum.SM4 ? EnumWorkKeyType.SM4_MAK : EnumWorkKeyType
                    .MAK, mak, makCheck);
            if (BusinessConfig.isTrackEncrypt(context)) {
                r3 = pinPadDev.loadWorkKey(encAlg == EncryptAlgorithmEnum.SM4 ? EnumWorkKeyType.SM4_TDK :
                        EnumWorkKeyType.TDK, tdk, tdkCheck);
                result = r1 && r2 && r3;
            } else {
                result = r1 && r2;
            }
        }
        if (result) {
            //工作密钥发散成功
            BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.FLAG_SIGN_IN, true);
        } else {
            //工作密钥发散失败
            StatusCode code = StatusCode.KEY_VERIFY_FAILED;
            taskResult[0] = code.getStatusCode();
            taskResult[1] = context.getString(code.getMsgId());
        }
        return result;
    }
}
