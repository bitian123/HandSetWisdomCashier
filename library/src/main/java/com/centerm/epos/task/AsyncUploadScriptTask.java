package com.centerm.epos.task;

import android.content.Context;

import com.centerm.epos.EposApplication;
import com.centerm.epos.bean.ScriptInfo;
import com.centerm.epos.common.StatusCode;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.msg.PosISO8583Message;
import com.centerm.epos.net.SequenceHandler;
import com.centerm.epos.present.communication.DataExchanger;
import com.centerm.epos.present.communication.DataExchangerFactory;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;


/**
 * author:wanliang527</br>
 * date:2016/11/29</br>
 */

public abstract class AsyncUploadScriptTask extends AsyncMultiRequestTask {

    private CommonDao<ScriptInfo> dao;
    private List<ScriptInfo> tradeList;
    private int index = 0;
    private int size;
    private Map<String, String> dataMap;
    private int uploadMaxTimes;
    private boolean bIsSuccess = false;
    private String mMsg , sVoucher;

    public AsyncUploadScriptTask(Context context, Map<String, Object> dataMap) {
        super(context, dataMap);
        dao = new CommonDao<>(ScriptInfo.class, DbHelper.getInstance());
        this.dataMap = new HashMap<>();

        if (factory instanceof PosISO8583Message)
            ((PosISO8583Message) factory).setRequestDataForIso8583(this.dataMap);
    }

    private void initUploadScriptData(int index,boolean bIsGenVoucher){
        dataMap.clear();
        this.dataMap.putAll(tradeList.get(index).convert2Map());
        //dataMap.put(TransDataKey.iso_f11_origin, dataMap.get(TransDataKey.iso_f11));
        dataMap.put(TransDataKey.iso_f60_origin, dataMap.get(TransDataKey.iso_f60));
        dataMap.put(TransDataKey.iso_f61, dataMap.get(TransDataKey.iso_f61));
        if(bIsGenVoucher) {
            sVoucher = BusinessConfig.getInstance().getPosSerial(EposApplication.getAppContext());
        }
        dataMap.put( TransDataKey.iso_f11, (sVoucher==null) ?  BusinessConfig.getInstance().getPosSerial(EposApplication.getAppContext()) : sVoucher );
    }

    @Override
    protected String[] doInBackground(String... params) {
        int iTimes;

        do {
            try {
                tradeList = dao.queryBuilder().where().eq("scriptResult", 1).or().eq("scriptResult", 2).query();
            } catch (SQLException e) {
                e.printStackTrace();
                taskResult[0] = "01";
                break;
            }
            initUploadScriptData(index, true);

            uploadMaxTimes = BusinessConfig.getInstance().getNumber(context, BusinessConfig.Key.KEY_MAX_MESSAGE_RETRY_TIMES);
            uploadMaxTimes = (uploadMaxTimes <= 0) ? 3 : uploadMaxTimes;
            bIsSuccess = false;

            for (iTimes = 0; (iTimes < (uploadMaxTimes + 1)) && (!bIsSuccess); iTimes++) {
                sleep(MEDIUM_SLEEP);

                if (tradeList == null || tradeList.size() == 0) {
                    sleep(LONG_SLEEP);
                    taskResult[0] = "00";
                    break;
                }
                size = tradeList.size();
                publishProgress(size, index + 1);
                initUploadScriptData(index, false);
                Object msgPacket = factory.packMessage(TransCode.UPLOAD_SCRIPT_RESULT, null);
                if (msgPacket == null) {
                    dao.deleteByWhere("1=1");
                    taskResult[0] = "01";
                    break;
                }
                final SequenceHandler handler = new SequenceHandler() {
                    @Override
                    protected void onReturn(String reqTag, byte[] respData, String code, String msg) {
                        mMsg = msg;
                        if (respData != null) {
                            Map<String, Object> resp = factory.unPackMessage(reqTag, respData);
                            String respCode = (String) resp.get(TradeInformationTag.RESPONSE_CODE);
                            logger.info("脚本执行结果上送结果==>返回码==>" + respCode);
                            if ("00".equals(respCode)) {//通知上送完成
                                ScriptInfo trade = tradeList.get(index);
                                trade.setScriptResult(3);
                                dao.update(trade);
                                bIsSuccess = true;
                            }
                        }
                    }
                };
                DataExchanger dataExchanger = DataExchangerFactory.getInstance();
                dataExchanger.doSequenceExchange(TransCode.UPLOAD_SCRIPT_RESULT, (byte[]) msgPacket, handler);
                logger.info("脚本执行结果上送结果==>uploadMaxTimes==>" + uploadMaxTimes + " current iTimes:"+iTimes);
                logger.info("脚本执行结果上送结果==>mMsg==>" + mMsg);
                logger.info("脚本执行结果上送结果==>bIsSuccess==>" + bIsSuccess);

                if (mMsg.equals(EposApplication.getAppContext().getString(StatusCode.SOCKET_TIMEOUT.getMsgId()))) {
                    taskResult[0] = "02";
                    break; /*联机服务器超时*/
                }
                if (bIsSuccess) {
                    taskResult[0] = "00";
                    break;
                }
                if (iTimes >= uploadMaxTimes) {
                    bIsSuccess = true;
                    taskResult[0] = "01";
                    break;
                }
            }
        }while(false);
        logger.info("脚本执行结果上送结果==>taskResult[0]==>" + taskResult[0]);
        if( bIsSuccess ){ dao.deleteByWhere("1=1"); }
        DbHelper.releaseInstance();
        return taskResult;
    }
}
