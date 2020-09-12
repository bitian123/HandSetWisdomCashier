package com.centerm.epos.task;

import android.content.Context;
import android.text.TextUtils;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;
import com.centerm.cpay.midsdk.dev.define.pinpad.EnumDataEncryMode;
import com.centerm.epos.EposApplication;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.bean.transcation.RequestMessage;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.net.SequenceHandler;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.DataHelper;
import com.centerm.epos.utils.SecurityTool;
import com.centerm.smartpos.util.HexUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import config.BusinessConfig;

import static config.BusinessConfig.Key.TDK_KEY;

/**
 * Created by ysd on 2016/12/20.
 */

public class AsyncUploadRefundDataTask extends AsyncMultiRequestTask {
    private List<TradeInfoRecord> tradeInfos;
    private int index;
    private String transCode = TransCode.TRANS_FEFUND_DETAIL;
    private CommonDao<TradeInfoRecord> tradeDao;
    private CommonDao<RequestMessage> requestMessageCommonDao;
    private TradeInfoRecord cardInfo;

    public AsyncUploadRefundDataTask(Context context, Map<String, Object> dataMap, List<TradeInfoRecord> tradeInfos) {
        super(context, dataMap);
        this.tradeInfos = tradeInfos;
        DbHelper dbHelper = DbHelper.getInstance();
        tradeDao = new CommonDao<>(TradeInfoRecord.class, dbHelper);
        requestMessageCommonDao = new CommonDao<>(RequestMessage.class, dbHelper);
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
        Object msgPkg = initData(cardInfo);
        SequenceHandler handler = new SequenceHandler() {

            @Override
            protected void onReturn(String reqTag, byte[] respData, String code, String msg) {
                sleep(LONG_SLEEP);
                taskResult[0] = code;
                taskResult[1] = msg;
                if (respData != null) {
                    Map<String, Object> resp = factory.unPackMessage(transCode, respData);
                    String respCode = (String) resp.get(TradeInformationTag.RESPONSE_CODE);
                    if ("00".equals(respCode)) {
                        logger.error("退货第" + (index + 1) + "条记录上送成功");
                        cardInfo.setBatchSuccess(true);
                        //更新上送状态
                        tradeDao.update(cardInfo);
                        if (hasNext()) {
                            cardInfo = tradeInfos.get(++index);
                            publishProgress(tradeInfos.size(), index + 1);
                            Object msgPkg = initData(cardInfo);
                            if (msgPkg != null)
                                sendNext(transCode, (byte[]) msgPkg);
                        }
                    } else {
                        logger.error("退货第" + (index + 1) + "条记录被拒绝");
                        cardInfo.setSendCount(99);
                        //更新上送状态
                        tradeDao.update(cardInfo);
                        if (hasNext()) {
                            cardInfo = tradeInfos.get(++index);
                            publishProgress(tradeInfos.size(), index + 1);
                            Object msgPkg = initData(cardInfo);
                            if (msgPkg != null)
                                sendNext(transCode, (byte[]) msgPkg);
                        }
                    }
                } else {
                    logger.error("退货第" + (index + 1) + "条记录上送失败");
                    if (hasNext()) {
                        cardInfo = tradeInfos.get(++index);
                        publishProgress(tradeInfos.size(), index + 1);

                        Object msgPkg = initData(cardInfo);
                        if (msgPkg != null)
                            sendNext(transCode, (byte[]) msgPkg);
                    }
                }
            }
        };
        if (msgPkg != null)
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

    private byte[] initData(TradeInfoRecord refundInfo) {
        Map<String, String> condition = new HashMap<>();
        condition.put(RequestMessage.KEY_FIELD_NAME, refundInfo.getVoucherNo());
        List<RequestMessage> messageList = requestMessageCommonDao.queryByMap(condition);
        if (messageList == null || messageList.size() == 0)
            return null;
        RequestMessage message = messageList.get(0);

        if (message == null)
            return null;
        int count = refundInfo.getSendCount();
        refundInfo.setSendCount(++count);
        //更改上送次数
        tradeDao.update(refundInfo);

        try {
//            if(BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), BusinessConfig.Key.ENCODE_TYPE)){
//                message.setRequestMessage(changeMsgForUploadEn(message.getRequestMessage()));
//            }
            return changeMsgForUpload(message.getRequestMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String  changeMsgForUploadEn(String message){
        String encrypeStr = message.substring(82);
        logger.error(encrypeStr);
        String enMsgStr = message.substring(82);
        String result = SecurityTool.decrypt3DES(
                BusinessConfig.getInstance().getValue(EposApplication.getAppContext(),TDK_KEY),
                SecurityTool.formatLen(enMsgStr)
        );
        String lenStr = message.substring(74,82);
        lenStr = new String(HexUtils.hexStringToByte(lenStr));
        int len = Integer.parseInt(lenStr);
        result = message.substring(0,82)+result.substring(0,len*2);
        logger.debug("decode: "+result);
        return result;
    }

    private byte[] changeMsgForUpload(String message) throws Exception {
        if (TextUtils.isEmpty(message))
            return null;
        int messageOffset = 0;
        boolean isHasMac = false;
        logger.error(message);
        StringBuilder newMsgStrBuffer = new StringBuilder(message.length());
        //长度 = 原长度 - 8 byte mac
        int newMsgLen = Integer.parseInt(message.substring(messageOffset, messageOffset + 4), 16) - 8;
        newMsgStrBuffer.append(String.format(Locale.CHINA, "%04x", newMsgLen).toUpperCase());
        messageOffset += 4;

        //添加TPDU和报文头: 5 Byte TPDU + 15 Byte head
        String tpduAndHead = message.substring(messageOffset, messageOffset + 78);
        String len = new String(HexUtils.hexStringToByte(tpduAndHead.substring(tpduAndHead.length()-8)));
        int dataLen = Integer.parseInt(len)-8;
        len = DataHelper.fillLeftZero(dataLen+"",4);
        len = HexUtils.bytesToHexString(len.getBytes());

        newMsgStrBuffer.append(tpduAndHead.substring(0,tpduAndHead.length()-8)+len);
        messageOffset += 78;

        //添加消息类型：2 Byte Msg Type
        newMsgStrBuffer.append("0320");
        messageOffset += 4;

        //添加bitmap ：8 Byte BITMAP，最后一位（64域）要置为0，因为不上送MAC
        newMsgStrBuffer.append(message.substring(messageOffset, messageOffset + 15));
        messageOffset += 15;
        //最后一位置为0
        String lastBitMapChar = message.substring(messageOffset, messageOffset + 1);
        byte lastHalfByte = (byte) Integer.parseInt(lastBitMapChar, 16);
        if (isHasMac = (lastHalfByte % 2 != 0)) {
            newMsgStrBuffer.append(String.format(Locale.CHINA, "%X", lastHalfByte & 0xFE));
        } else
            newMsgStrBuffer.append(lastHalfByte);
        messageOffset += 1;

        //添加剩余数据，除去MAC值
        newMsgStrBuffer.append(message.substring(messageOffset, message.length() - (isHasMac ? 16 : 0)));

        //全报文加密
//        if(BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), BusinessConfig.Key.ENCODE_TYPE)){
//            String encrypeStr = newMsgStrBuffer.toString().substring(82);
//            encrypeStr = HexUtils.bytesToHexString(
//                    DeviceFactory.getInstance().getPinPadDev().encryData(EnumDataEncryMode.ECB, null, SecurityTool.formatLen(encrypeStr)));
//            String result = newMsgStrBuffer.toString().substring(0,82)+encrypeStr;
//            return HexUtil.hexStringToByte(result);
//        }
        return HexUtil.hexStringToByte(newMsgStrBuffer.toString());
    }
}
