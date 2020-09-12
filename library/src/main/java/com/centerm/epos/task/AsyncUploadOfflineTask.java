package com.centerm.epos.task;

import android.content.Context;
import android.text.TextUtils;

import com.centerm.epos.EposApplication;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.common.ConstDefine;
import com.centerm.epos.common.ISORespCode;
import com.centerm.epos.common.StatusCode;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.CommonManager;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.net.SequenceHandler;
import com.centerm.epos.redevelop.ICommonManager;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.XLogUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;

/**
 * Created by zhouzhihua on 2017/9/1.
 * 离线交易上送，包括ic卡脱机交易和离线结算离线调整的交易
 */
public class AsyncUploadOfflineTask extends AsyncMultiRequestTask {
    private static final String TAG = AsyncUploadOfflineTask.class.getSimpleName()+" ";
    private List<TradeInfoRecord> infoList ;
    private String transCode = TransCode.IC_OFFLINE_UPLOAD; /*默认为IC卡脱机*/
    private int index = 0;
    private CommonDao<TradeInfoRecord> tradeDao;
    private int uploadTimes = 0, uploadMaxTimes = 0 , transFag = -1;
    private boolean bIsTerminate = false; /*如果后台全部应答-可终止交易，如果上送过程中除无法连接服务器的异常-需要继续上传数据*/
    private boolean bIsInSettle = false;/*是否在结算*/

    private List<TradeInfoRecord> initInfoList(int transFag) {
        ICommonManager commonManager = (ICommonManager) ConfigureManager.getInstance(context).getSubPrjClassInstance(new CommonManager());
        try {

            XLogUtil.w(TAG,"getOfflineTransList(0):"+commonManager.getOfflineTransList(0).size());
            XLogUtil.w(TAG,"getOfflineTransList(1):"+commonManager.getOfflineTransList(0x1000).size());
            XLogUtil.w(TAG,"getOfflineTransList(2):"+commonManager.getOfflineTransList(0x2000).size());
            XLogUtil.w(TAG,"getOfflineTransList(4):"+commonManager.getOfflineTransList(0x4000).size());

            return (transFag == -1) ? commonManager.getOfflineTransList() : commonManager.getOfflineTransList(transFag);
        }catch (SQLException e){
            XLogUtil.e(TAG,"get offline trade list error!!!");
        }
        return null;
    }
    /**
     * author zhouzhihua
     * @param context
     * @param dataMap 未使用
     * 上送所有的离线交易，不管是否已经上送还是上送失败全部上送
     */
    public AsyncUploadOfflineTask(Context context, Map<String, Object> dataMap) {
        super(context, dataMap);
        uploadMaxTimes = BusinessConfig.getInstance().getNumber(context, BusinessConfig.Key.KEY_MAX_MESSAGE_RETRY_TIMES);
        uploadMaxTimes = (uploadMaxTimes<= 0) ? 1 : uploadMaxTimes;
        tradeDao = new CommonDao<>(TradeInfoRecord.class, DbHelper.getInstance());
        transFag = -1;
    }

    /**
     * author zhouzhihua
     * @param context
     * @param dataMap 未使用
     * @param transFag 0-未上送的离线交易，0x1000-离线交易上送成功，0x2000-离线交易上送失败,0x4000-后台无应答
     */

    public AsyncUploadOfflineTask(Context context, Map<String, Object> dataMap, int transFag) {
        super(context, dataMap);
        uploadMaxTimes = BusinessConfig.getInstance().getNumber(context, BusinessConfig.Key.KEY_MAX_MESSAGE_RETRY_TIMES);
        uploadMaxTimes = (uploadMaxTimes<= 0) ? 1 : uploadMaxTimes;
        tradeDao = new CommonDao<>(TradeInfoRecord.class, DbHelper.getInstance());
        this.transFag = transFag;
    }
    private void setRespondInfo(String code, String msg) {
        taskResult[0] = code;
        taskResult[1] = msg;
        logger.warn(TAG + "setRespondInfo:"+code);
    }

    /**
     * 增加解析数据异常的处理 <br/>
     * @param transCode 交易类型 <br/>
     * @param respData 后台相应数据 <br/>
     */
    private void haveRespondDealProcessing(String transCode , Object respData){
        Map<String, Object> mapData = factory.unPackMessage(transCode, respData);
        if( null != mapData ) {
            String respCode = (String) mapData.get(TradeInformationTag.RESPONSE_CODE);
            logger.warn(TAG + "respCode:" + respCode);
            ISORespCode isoCode = ISORespCode.codeMap(respCode);
            setRespondInfo((respCode.equals("00") || respCode.equals("94")) ? "00" : respCode, context.getString(isoCode.getResId()));
            TradeInfoRecord tradeInfoRecord = infoList.get(index);
            tradeInfoRecord.setOfflineTransUploadStatus((respCode.equals("00") || respCode.equals("94")) ? ConstDefine.OFFLINE_TRANS_STATUS_UPLOAD_SUCCESS : ConstDefine.OFFLINE_TRANS_STATUS_UPLOAD_SUCCESS);

            String string ;
            string = (String)mapData.get(TradeInformationTag.DATE_SETTLEMENT);
            if(!TextUtils.isEmpty(string)) { tradeInfoRecord.setSettlmentDate(string); }

            string = (String)mapData.get(TradeInformationTag.REFERENCE_NUMBER);
            if(!TextUtils.isEmpty(string)) { tradeInfoRecord.setReferenceNo(string); }

            string = (String)mapData.get(TradeInformationTag.AUTHORIZATION_IDENTIFICATION);
            if(!TextUtils.isEmpty(string)) { tradeInfoRecord.setAuthorizeNo(string); }

            string = (String)mapData.get(TradeInformationTag.ISSUER_IDENTIFICATION);
            if(!TextUtils.isEmpty(string)) { tradeInfoRecord.setIssueInstituteID(string); }

            string = (String)mapData.get(TradeInformationTag.ACQUIRER_IDENTIFICATION);
            if(!TextUtils.isEmpty(string)) { tradeInfoRecord.setAcquireInstituteID(string); }

            string = (String)mapData.get(TradeInformationTag.CURRENCY_CODE);
            if(!TextUtils.isEmpty(string)) { tradeInfoRecord.setCurrencyCode(string); }

            string = (String)mapData.get(TradeInformationTag.CREDIT_CODE);
            if(!TextUtils.isEmpty(string)) { tradeInfoRecord.setBankcardOganization(string); }

            string = (String)mapData.get(TradeInformationTag.REVERSE_FIELD);
            if(!TextUtils.isEmpty(string)) { tradeInfoRecord.setReverseFieldInfo(string); }

            if (getSettleFlag() && (!(respCode.equals("00") || respCode.equals("94")))) {
                tradeInfoRecord.setSendCount(99);
            }
            tradeDao.update(tradeInfoRecord);
        }
        else{
            if( getSettleFlag() ){
                TradeInfoRecord tradeInfoRecord = infoList.get(index);
                tradeInfoRecord.setSendCount(99);
                tradeDao.update(tradeInfoRecord);
            }
            else{
                /*解析数据异常上送次数满将交易设置后台无应答的方式*/
                if( uploadTimes == getUploadMaxTimes() ) {
                    TradeInfoRecord tradeInfoRecord = infoList.get(index);
                    tradeInfoRecord.setOfflineTransUploadStatus(ConstDefine.OFFLINE_TRANS_STATUS_UPLOAD_NORES);
                    tradeDao.update(tradeInfoRecord);
                }
                bIsTerminate = false;
            }
            logger.warn(TAG + " haveRespondDealProcessing index:"+ index + "接收包解析错误" );
        }
        logger.warn(TAG + " haveRespondDealProcessing index:"+ index );
    }
    private void notRespondDealProcessing(String msg , Object respData){
        if( uploadTimes == getUploadMaxTimes() ){
            TradeInfoRecord tradeInfoRecord = infoList.get(index);
            tradeInfoRecord.setOfflineTransUploadStatus(ConstDefine.OFFLINE_TRANS_STATUS_UPLOAD_NORES);
            tradeDao.update(tradeInfoRecord);
        }
        logger.warn(TAG + " notRespondDealProcessing equals:"+ (uploadTimes==getUploadMaxTimes()));
    }

    private class OfflineTransSequenceHandler extends SequenceHandler{

        @Override
        protected void onReturn(String reqTag, byte[] respData, String code, String msg) {
            sleep(SHORT_SLEEP);
            setRespondInfo(code,msg);
            logger.warn(TAG + " getUploadMaxTimes:"+ getUploadMaxTimes() +" uploadTimes:"+uploadTimes);
            logger.warn(TAG + "code:"+code+ " msg:"+msg + " respData:"+respData);
            if( null == respData ){
                logger.error(TAG + " 接收数据为null" );
                if( (!getSettleFlag()) && msg.equals(EposApplication.getAppContext().getString(StatusCode.SOCKET_TIMEOUT.getMsgId())) ) {
                    return ;
                }
                bIsTerminate = false;
                notRespondDealProcessing(msg, respData);
            }
            else{
                haveRespondDealProcessing(transCode, respData);
            }

            logger.warn(TAG + "index:"+index);
            Map<String,Object> dataMap = formUpLoadData(++index);
            if( dataMap != null ) {
                Object packMsg = factory.packMessage(transCode, dataMap);
                sendNext(transCode,(byte[])packMsg);
            }
        }
    }
    /*
    * 组离线上送数据包，离线交易包括IC卡脱机交易和离线交易
    * ic卡脱机交易类型统一转化为TransCode.IC_OFFLINE_UPLOAD
    * */
    private Map<String,Object> formUpLoadData(int index){
        logger.warn(TAG + "formUpLoadData index:"+index+"  infoList.size():"+infoList.size());
        if( index >= infoList.size() ){
            logger.warn(TAG + "formUpLoadData null");
            return null;
        }
        Map<String,Object> dataMap = new HashMap<>();
        TradeInfoRecord tradeInfoRecord = infoList.get(index);
        logger.warn(TAG + "tradeInfoRecord:"+tradeInfoRecord);
        this.transCode = tradeInfoRecord.getTransType();
        /*IC卡脱机交易转化交易类型*/
        this.transCode = ( this.transCode.equals(TransCode.E_COMMON) || this.transCode.equals(TransCode.E_QUICK) ) ? TransCode.IC_OFFLINE_UPLOAD : this.transCode;
        if( getSettleFlag() ) {
            this.transCode = TransCode.IC_OFFLINE_UPLOAD.equals(this.transCode) ? TransCode.IC_OFFLINE_UPLOAD_SETTLE : this.transCode;
        }

        dataMap.putAll(tradeInfoRecord.convert2Map());

        publishProgress(infoList.size(), index + 1);

        if( getSettleFlag() ) {
            int cnt = infoList.get(index).getSendCount();
            infoList.get(index).setSendCount(++cnt);
        }

        logger.warn(TAG + "transCode" + transCode);
        return dataMap;
    }

    private int getUploadMaxTimes(){
        return uploadMaxTimes;
    }

    private String[] uploadOfflineTrans(String... params){
        bIsTerminate = false;
        logger.warn(TAG + "doInBackground getUploadMaxTimes: " + getUploadMaxTimes()+" taskResult:"+ taskResult[1]);
        for( uploadTimes = 0 ; uploadTimes <= getUploadMaxTimes() ; uploadTimes++ ) {
            index = 0;
            infoList = initInfoList(this.transFag);
            logger.warn(TAG + "doInBackground ================ > taskResult:" + taskResult[1] + " this.transFag:"+this.transFag);
            /*
            * 连接服务器异常，直接退出
            * */
            if ( (null == infoList) || (0 == infoList.size())
                    || EposApplication.getAppContext().getString(StatusCode.SOCKET_TIMEOUT.getMsgId()).equals(taskResult[1])
                    || bIsTerminate) {
                break;
            }
            bIsTerminate = true;
            logger.warn(TAG + "initInfoList: " + infoList.size());
            Map<String, Object> dataMap = formUpLoadData(index);
            if (dataMap != null) {
                Object packMsg = factory.packMessage(this.transCode, dataMap);
                SequenceHandler sequenceHandler = new OfflineTransSequenceHandler();
                client.doSequenceExchange(this.transCode, (byte[]) packMsg, sequenceHandler);/*第一次联机交易数据上送*/
            }
        }
        DbHelper.getInstance();
        return taskResult;
    }

    public AsyncUploadOfflineTask setInfoList(List<TradeInfoRecord> infoList){
        this.infoList = infoList;
        return this;
    }
    public AsyncUploadOfflineTask setSettleFlag(boolean bIsInSettle){
        this.bIsInSettle = bIsInSettle;
        return this;
    }

    public boolean getSettleFlag(){
        return this.bIsInSettle;
    }

    private String[] uploadOfflineTransWithListInfo(String... params){

        index = 0;
        logger.warn(TAG + "uploadOfflineTransWithListInfo ================ > ");

        logger.warn(TAG + "initInfoList: " + infoList.size());
        Map<String, Object> dataMap = formUpLoadData(index);
        if (dataMap != null) {
            Object packMsg = factory.packMessage(this.transCode, dataMap);
            SequenceHandler sequenceHandler = new OfflineTransSequenceHandler();
            client.doSequenceExchange(this.transCode, (byte[]) packMsg, sequenceHandler);/*第一次联机交易数据上送*/
        }
        DbHelper.getInstance();
        return taskResult;
    }

    @Override
    protected String[] doInBackground(String... params) {
        String[] sArray ;
        if( infoList == null ) {
            sArray = this.uploadOfflineTrans(params);
        } else{
            sArray = this.uploadOfflineTransWithListInfo(params);
        }
        this.infoList = null; this.tradeDao = null;
        this.index = 0; this.bIsTerminate = false; this.bIsInSettle = false;
        return sArray;
    }
}
