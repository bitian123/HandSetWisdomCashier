package com.centerm.epos.task;

import android.content.Context;

import com.centerm.epos.common.ISORespCode;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.net.SequenceHandler;
import com.centerm.epos.present.communication.DataExchanger;
import com.centerm.epos.present.communication.DataExchangerFactory;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.XLogUtil;

import java.util.Map;

import static com.centerm.epos.common.TransCode.SCAN_QUERY;

/**
 * author:wanliang527</br>
 * date:2016/11/22</br>
 */

public abstract class AsyncMagLoadConfirmTask extends AsyncMultiRequestTask {
    private Map<String, Object> repMap;

    private String sTransCode = TransCode.MAG_CASH_LOAD;

    public AsyncMagLoadConfirmTask(Context context, Map<String, Object> dataMap) {
        super(context, dataMap);
        //sTransCode = TransCode.MAG_CASH_LOAD.equals(dataMap.get(TradeInformationTag.TRANSACTION_TYPE)) ? TransCode.MAG_CASH_LOAD : TransCode.MAG_ACCOUNT_LOAD;
    }

    public AsyncMagLoadConfirmTask(Context context, Map<String, Object> dataMap,Map<String, Object> repMap) {
        super(context, dataMap);
        this.repMap = repMap;
        //sTransCode = TransCode.MAG_CASH_LOAD.equals(dataMap.get(TradeInformationTag.TRANSACTION_TYPE)) ? TransCode.MAG_CASH_LOAD : TransCode.MAG_ACCOUNT_LOAD;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String[] doInBackground(String... params) {
        final String[] result = new String[3];
        Object msgPacket = factory.packMessage(TransCode.MAG_CASH_LOAD_CONFIRM, dataMap);
        final SequenceHandler handler = new SequenceHandler() {

            @Override
            protected void onReturn(String reqTag, byte[] respData, String code, String msg) {
                if (respData != null) {
                    Map<String, Object> resp = factory.unPackMessage(reqTag, respData);
                    String respCode = (String) resp.get(TradeInformationTag.RESPONSE_CODE);
                    ISORespCode isoCode = ISORespCode.codeMap(respCode);
                    result[0] = isoCode.getCode();
                    result[1] = context.getString(isoCode.getResId());

                    repMap.putAll(resp);
                    repMap.put(TradeInformationTag.TRANSACTION_TYPE,TransCode.MAG_CASH_LOAD.equals(sTransCode) ? TransCode.MAG_CASH_LOAD : TransCode.MAG_ACCOUNT_LOAD);
                } else {
                    result[0] = code;
                    result[1] = msg;
                    result[2] = msg;
                }
            }
        };
        DataExchanger dataExchanger = DataExchangerFactory.getInstance();
        dataExchanger.doSequenceExchange(TransCode.MAG_CASH_LOAD_CONFIRM, (byte[]) msgPacket, handler);
        return result;
    }

}
