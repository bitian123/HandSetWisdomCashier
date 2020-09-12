package com.centerm.epos.net;

import com.centerm.epos.EposApplication;
import com.centerm.epos.base.BaseRuntimeException;
import com.centerm.epos.common.StatusCode;
import com.centerm.epos.present.communication.DataExchanger;
import com.centerm.epos.transcation.pos.constant.RuntimeExceptionCode;

import org.apache.log4j.Logger;

/**
 * 队列式网络请求的回调接收器{@link SocketClient#sendSequenceData(byte[], String, SequenceHandler)}
 * author:wanliang527</br>
 * date:2016/11/20</br>
 */

public abstract class SequenceHandler {

    private Logger logger = Logger.getLogger(SequenceHandler.class);
    private DataExchanger client;
    private ResponseHandler handler;
    private String currentReqTag;
    private boolean isSync;

    public void bindClient(DataExchanger client, boolean isSync) {
        this.client = client;
        this.isSync = isSync;
        handler = new ResponseHandler() {
            @Override
            public void onSuccess(String statusCode, String msg, byte[] data) {
                _return(currentReqTag, data, statusCode, msg);
            }

            @Override
            public void onFailure(String code, String msg, Throwable error) {
                _return(currentReqTag, null, code, msg);
            }
        };
    }

    private void _return(String reqTag, byte[] respData, String code, String msg) {
        logger.info("队列式网络请求==>[" + reqTag + "]结果返回==>" + (respData != null));
        onReturn(reqTag, respData, code, msg);
    }

    protected abstract void onReturn(String reqTag, byte[] respData, String code, String msg);

    public void sendNext(String reqTag, byte[] data) {
        logger.info("队列式网络请求==>开始发送[" + reqTag + "]");
        if (data == null) {
            logger.warn("队列式网络请求==>发送数据为空==>结束");
            finish();
            return;
        }
        currentReqTag = reqTag;
        try {
            byte[] receivedData = client.doExchange(data);
            if (receivedData == null) {
                logger.error("^_^ 接收数据失败！ ^_^");
                handler.onFailure("99", "接收数据失败！", null);
            } else {
                handler.onSuccess(StatusCode.SUCCESS.getStatusCode(),
                        EposApplication.getAppContext().getString(StatusCode.SUCCESS.getMsgId()),
                        receivedData);
            }
        } catch (Exception e) {
            logger.error("^_^ 数据交换失败：" + e.getMessage() + " ^_^");
            String msg = EposApplication.getAppContext().getString(StatusCode.DATA_EXCHANGE_ERROR.getMsgId());
            if( e instanceof BaseRuntimeException ){
                BaseRuntimeException baseError = (BaseRuntimeException) e;
                if( baseError.getErrCode() == RuntimeExceptionCode.CONNECT_SERVER_FAILED ){
                    msg = EposApplication.getAppContext().getString(StatusCode.SOCKET_TIMEOUT.getMsgId());
                }
            }
            handler.onFailure("99", msg , e);
        }
    }

    public void finish() {
        logger.info("队列式网络请求==>结束");
    }
}
