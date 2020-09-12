package com.centerm.epos.task;

import android.content.Context;

import com.centerm.epos.bean.iso.Iso62Qps;
import com.centerm.epos.common.ISORespCode;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.model.TradeModelImpl;
import com.centerm.epos.net.SequenceHandler;
import com.centerm.epos.present.communication.DataExchanger;
import com.centerm.epos.present.communication.DataExchangerFactory;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;

import java.util.Map;

import static com.centerm.epos.common.TransCode.DOWNLOAD_PARAMS_FINISHED;

/**
 * 异步下载非接参数任务
 * author:wanliang527</br>
 * date:2016/11/23</br>
 */

public abstract class AsyncDownloadQpsTask extends AsyncMultiRequestTask {

    private CommonDao<Iso62Qps> dao;

    public AsyncDownloadQpsTask(Context context, Map<String, Object> dataMap) {
        super(context, dataMap);
        dao = new CommonDao<>(Iso62Qps.class, DbHelper.getInstance());
    }

    @Override
    protected String[] doInBackground(String... params) {
        final String[] result = new String[2];
        dataMap.put(TransDataKey.KEY_PARAMS_TYPE, "4");
        Object msgPacket = factory.packMessage(TransCode.DOWNLOAD_QPS_PARAMS, dataMap);
        if (msgPacket == null) {
            result[0] = "99";
            result[1] = "数据组包失败";
            return result;
        }
        final SequenceHandler handler = new SequenceHandler() {
            @Override
            protected void onReturn(String reqTag, byte[] respData, String code, String msg) {
                if (respData != null) {
                    Map<String, Object> resp = factory.unPackMessage(reqTag, respData);
                    String respCode = (String) resp.get(TradeInformationTag.RESPONSE_CODE);
                    ISORespCode isoCode = ISORespCode.codeMap(respCode);
                    result[0] = isoCode.getCode();
                    result[1] = context.getString(isoCode.getResId());
                    String iso62;
                    boolean dbResult;
                    switch (reqTag) {
                        case TransCode.DOWNLOAD_QPS_PARAMS:
                            iso62 = (String) resp.get(TradeInformationTag.IC_PARAMETER_QPS);
                            if ("00".equals(respCode)) {
                                //请求成功
                                publishProgress(0, -1);
                                Iso62Qps qps = new Iso62Qps(iso62);
                                dbResult = dao.deleteByWhere("id IS NOT NULL");
                                dbResult = dbResult && dao.save(qps);
                                if (dbResult) {
                                    logger.info("更新小额免密免签参数成功==>" + qps.toString());
                                    TradeModelImpl.getInstance().setQpsParams(qps);
                                } else {
                                    logger.warn("更新小额免密免签参数失败");
                                }
//                                sleep(LONG_SLEEP);
                                dataMap.put(TradeInformationTag.TRANSACTION_CODE, "0800");
                                Object pkgMsg = factory.packMessage(DOWNLOAD_PARAMS_FINISHED, dataMap);
                                sendNext(TransCode.DOWNLOAD_PARAMS_FINISHED, (byte[]) pkgMsg);
                            }
                            break;
                        case TransCode.DOWNLOAD_PARAMS_FINISHED:
                            //下载结束报文结果，不关心
                            break;
                    }
                } else {
                    result[0] = code;
                    result[1] = msg;
                }
            }
        };
//        sleep(LONG_SLEEP);
//        SocketClient client = SocketClient.getInstance(context);
//        client.syncSendSequenceData(TransCode.DOWNLOAD_QPS_PARAMS, (byte[]) msgPacket, handler);
        DataExchanger dataExchanger = DataExchangerFactory.getInstance();
        dataExchanger.doSequenceExchange(TransCode.DOWNLOAD_QPS_PARAMS, (byte[]) msgPacket, handler);
        DbHelper.releaseInstance();
        return result;
    }


}
