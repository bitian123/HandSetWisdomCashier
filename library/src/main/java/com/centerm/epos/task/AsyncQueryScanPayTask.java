package com.centerm.epos.task;

import android.content.Context;

import com.centerm.epos.common.ISORespCode;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.net.SequenceHandler;
import com.centerm.epos.present.communication.DataExchanger;
import com.centerm.epos.present.communication.DataExchangerFactory;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;

import java.util.Map;

import static com.centerm.epos.common.TransCode.SCAN_QUERY;

/**
 * author:wanliang527</br>
 * date:2016/11/22</br>
 */

public abstract class AsyncQueryScanPayTask extends AsyncMultiRequestTask {
    private int totalCounts;

    public AsyncQueryScanPayTask(Context context, Map<String, Object> dataMap) {
        super(context, dataMap);
        totalCounts = 0;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String[] doInBackground(String... params) {
        final String[] result = new String[2];
        Object msgPacket = factory.packMessage(TransCode.SCAN_QUERY, dataMap);
        final SequenceHandler handler = new SequenceHandler() {

            @Override
            protected void onReturn(String reqTag, byte[] respData, String code, String msg) {
                if (respData != null) {
                    Map<String, Object> resp = factory.unPackMessage(reqTag, respData);
                    String respCode = (String) resp.get(TradeInformationTag.RESPONSE_CODE);
                    ISORespCode isoCode = ISORespCode.codeMap(respCode);
                    result[0] = isoCode.getCode();
                    result[1] = context.getString(isoCode.getResId());
                    if ("H9".equals(respCode) && totalCounts < 3) {
                        //等待15秒后，交易结果未知，继续查询
                        sleep(15000);
                        Object msgPacket = factory.packMessage(TransCode.SCAN_QUERY, dataMap);
                        totalCounts++;
                        sendNext(SCAN_QUERY, (byte[]) msgPacket);
                    }
                } else {
                    result[0] = code;
                    result[1] = msg;
                }
            }
        };
        DataExchanger dataExchanger = DataExchangerFactory.getInstance();
        sleep(15000);
        totalCounts++;
        dataExchanger.doSequenceExchange(TransCode.SCAN_QUERY, (byte[]) msgPacket, handler);

        return result;
    }

}
