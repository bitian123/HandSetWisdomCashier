package com.centerm.epos.task;

import android.content.Context;
import com.centerm.epos.net.SequenceHandler;
import java.util.List;
import java.util.Map;

/**
 * Created by zhouzhihua on 2017/12/29
 * 离线交易上送，包括ic卡脱机交易和离线结算离线调整的交易
 */
public class AsyncUploadCommonTask<T> extends AsyncMultiRequestTask {
    private static final String TAG = AsyncUploadCommonTask.class.getSimpleName()+" ";
    private List<T> infoList = null;
    private String transCode = null ;
    private int uploadTimes = 0, uploadMaxTimes = 0 ;
    AsyncUploadCommonTaskProcessing asyncUploadCommonTaskProcessingCallBack = null;

    /**
     * author zhouzhihua
     * @param context
     * @param dataMap 未使用
     */
    public AsyncUploadCommonTask(Context context, Map<String, Object> dataMap) {
        super(context, dataMap);
    }

    public interface AsyncUploadCommonTaskProcessing {

        public void haveRespondDealProcessing(String transCode , Object respData);
        public void notRespondDealProcessing(String msg , Object respData);
    }

    public AsyncUploadCommonTask setTList(List<T> l ){
        infoList = l;
        return this;
    }

    public AsyncUploadCommonTask setCallBack(AsyncUploadCommonTaskProcessing callBack){
        asyncUploadCommonTaskProcessingCallBack = callBack;
        return this;
    }
    public AsyncUploadCommonTask setMaxUploadTimes(int uploadMaxTimes){
        this.uploadMaxTimes = (uploadMaxTimes <= 0) ? 1 : uploadMaxTimes;
        return this;
    }

    private void setRespondInfo(String code, String msg) {
        taskResult[0] = code;
        taskResult[1] = msg;
        logger.warn(TAG + "setRespondInfo:"+code);
    }

    private class CommonTransSequenceHandler extends SequenceHandler{

        @Override
        protected void onReturn(String reqTag, byte[] respData, String code, String msg) {
            sleep(SHORT_SLEEP);
            setRespondInfo(code,msg);
            logger.warn(TAG + " getUploadMaxTimes:"+ getUploadMaxTimes() +" uploadTimes:"+uploadTimes);
            logger.warn(TAG + "code:"+code+ " msg:"+msg + " respData:"+respData);
            if( null == respData ){
                asyncUploadCommonTaskProcessingCallBack.notRespondDealProcessing(msg, respData);
            }
            else{
                asyncUploadCommonTaskProcessingCallBack.haveRespondDealProcessing(transCode, respData);
            }
        }
    }

    private int getUploadMaxTimes(){
        return uploadMaxTimes;
    }
    @Override
    protected String[] doInBackground(String... params) {
       return super.doInBackground(params);
    }

}
