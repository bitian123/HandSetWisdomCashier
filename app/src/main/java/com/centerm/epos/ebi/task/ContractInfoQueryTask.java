package com.centerm.epos.ebi.task;

import android.content.Context;

import com.centerm.epos.common.ISORespCode;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.net.ResponseHandler;
import com.centerm.epos.present.communication.DataExchanger;
import com.centerm.epos.present.communication.DataExchangerFactory;
import com.centerm.epos.task.AsyncMultiRequestTask;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import config.BusinessConfig;

/**
 * Created by FL on 2017/9/14 10:48.
 * 异步合同信息查询任务,修改xml报文方式
 */

public class ContractInfoQueryTask extends AsyncMultiRequestTask {
    private Map<String, Object> returnMap;

    public ContractInfoQueryTask(Context context, Map<String, Object> dataMap, Map<String, Object> returnMap) {
        super(context, dataMap);
        //initParamTip();
        logger.error("^_^ dataMap: ^_^"+dataMap.toString());
        this.returnMap = returnMap;
        if (this.returnMap == null) {
            this.returnMap = new HashMap<>();
        }
    }

    @Override
    protected String[] doInBackground(String... params) {
        Object msgPkg = factory.packMessage(TransCode.CONTRACT_INFO_QUERY, dataMap);
        ResponseHandler handler = new ResponseHandler() {
            @Override
            public void onSuccess(String statusCode, String msg, byte[] data) {
                Map<String, Object> mapData = factory.unPackMessage(TransCode.CONTRACT_INFO_QUERY, data);
                returnMap.putAll(mapData);
                String respCode = (String) mapData.get(TradeInformationTag.RESPONSE_CODE);
                String traceNo = (String) mapData.get(TradeInformationTag.TRACE_NUMBER);
                ISORespCode isoCode = ISORespCode.codeMap(respCode);
                taskResult[0] = isoCode.getCode();
                taskResult[1] = context.getString(isoCode.getResId());
                if ("00".equals(respCode)) {
                    String iso63 = (String) mapData.get(TradeInformationTag.CONTRACT_INFO);
                    taskResult[0] = respCode;
                    taskResult[1] = iso63;
                    taskResult[2]= (String) dataMap.get(TradeInformationTag.TRACE_NUMBER);

                }else{
                    taskResult[0] = respCode;
                    taskResult[1] = msg;
                }
            }

            @Override
            public void onFailure(String code, String msg, Throwable error) {
                taskResult[0] = code;
                taskResult[1] = msg;
            }
        };

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
            e.printStackTrace();
            taskResult[0] = "99";
            taskResult[1] = "数据交换失败";
        }
        return taskResult;
    }

    @Override
    public void onStart() {
        super.onStart();

    }
}
