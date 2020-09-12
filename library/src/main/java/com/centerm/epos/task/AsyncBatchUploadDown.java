package com.centerm.epos.task;

import android.content.Context;

import com.centerm.epos.common.TransCode;
import com.centerm.epos.net.ResponseHandler;
import com.centerm.epos.present.communication.DataExchanger;
import com.centerm.epos.present.communication.DataExchangerFactory;
import com.centerm.epos.present.communication.ICommunication;
import com.centerm.epos.present.communication.TcpCommParameter;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;

import java.util.Map;

import static com.centerm.epos.common.TransDataKey.iso_f39;

/**
 * Created by ysd on 2016/12/20.
 */

public abstract class AsyncBatchUploadDown extends AsyncMultiRequestTask {
    private String transCode = TransCode.SETTLEMENT_DONE;
    public AsyncBatchUploadDown(Context context, Map<String, Object> dataMap) {
        super(context, dataMap);
    }

    @Override
    protected String[] doInBackground(String... params) {
//        sleep(LONG_SLEEP);
        Object msgPkg = factory.packMessage(transCode, dataMap);
        ResponseHandler handler = new ResponseHandler() {
            @Override
            public void onSuccess(String statusCode, String msg, byte[] data) {
                Map<String, Object> mapData = factory.unPackMessage(transCode, data);
                String respCode = (String) mapData.get(TradeInformationTag.RESPONSE_CODE);
                if ("00".equals(respCode)) {
                    taskResult[0]="00";
                    taskResult[1]="请求成功";

                } else {
                    taskResult[0]="99";
                    taskResult[1]="请求失败";
                    logger.error("上送完成请求返失败（有收到平台返回值）");
                }
            }

            @Override
            public void onFailure(String code, String msg, Throwable error) {
                taskResult[0]="99";
                taskResult[1]="请求失败";
                logger.error("上送完成请求返失败（有收到平台返回值）");
            }
        };
//        client.syncSendData((byte[]) msgPkg, handler);
        try {
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
            taskResult[0] = "99";
            taskResult[1] = e.getMessage();
        }
        return taskResult;
    }
}
