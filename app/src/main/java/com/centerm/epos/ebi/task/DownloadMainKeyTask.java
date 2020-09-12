package com.centerm.epos.ebi.task;

import android.content.Context;

import com.centerm.cloudsys.sdk.common.utils.MD5Utils;
import com.centerm.epos.common.Settings;
import com.centerm.epos.ebi.bean.LoadMakBean;
import com.centerm.epos.ebi.keys.JsonKey;
import com.centerm.epos.ebi.msg.BaseHttpJsonMessage;
import com.centerm.epos.ebi.utils.SecurityUtil;
import com.centerm.epos.net.ResponseHandler;
import com.centerm.epos.present.communication.DataExchanger;
import com.centerm.epos.present.communication.HttpCommParameter;
import com.centerm.epos.present.communication.ICommunication;

import java.util.Map;

import config.BusinessConfig;

import static com.centerm.epos.ebi.common.TransCode.DOWNLOAD_MAIN_KEY;

/**
 * Created by FL on 2017/9/14 10:48.
 * 异步签到任务,修改xml报文方式
 */

public class DownloadMainKeyTask extends EbiAsyncMultiRequestTask {

    public DownloadMainKeyTask(Context context, Map<String, Object> dataMap, Map<String, Object> returnMap) {
        super(context, dataMap);

    }

    @Override
    protected String[] doInBackground(String... params) {
        Object msgPkg = factory.packMessage(DOWNLOAD_MAIN_KEY, dataMap);
        logger.debug("json报文下载签名密钥");
        ResponseHandler handler = new ResponseHandler() {
            @Override
            public void onSuccess(String statusCode, String msg, byte[] data) {
                Map<String, Object> mapData = factory.unPackMessage(DOWNLOAD_MAIN_KEY, data);
                LoadMakBean returnData = (LoadMakBean) mapData.get(JsonKey.returnData);
                taskResult[0] = returnData.getBody().getResponse().getStatus();
                taskResult[1] = returnData.getBody().getResponse().getStatus_msg();
                if("00".equals(returnData.getBody().getResponse().getStatus())){
                    String returnKey = returnData.getBody().getResponse().getEncSAKey();
                    String SAkey = SecurityUtil.decryptECB(BaseHttpJsonMessage.CAkey, returnKey);
                    SAkey = SecurityUtil.toStringHex(SAkey);
                    String mainKey = MD5Utils.getMD5Str(BaseHttpJsonMessage.CAkey+SAkey);
                    //logger.debug("主密钥："+mainKey);
                    Settings.setValue(context, JsonKey.MAK, mainKey);

                }
            }

            @Override
            public void onFailure(String code, String msg, Throwable error) {
                taskResult[0] = code;
                taskResult[1] = msg;
            }
        };

        try {
            DataExchanger dataExchanger = new DataExchanger(ICommunication.COMM_HTTP, new HttpCommParameter());
            byte[] receivedData = dataExchanger.doExchange((byte[]) msgPkg);
            if (receivedData == null) {
                logger.error("^_^ 接收数据失败！ ^_^");
                handler.onFailure("99", "接收数据失败！", null);
            } else {
                handler.onSuccess(null, null, receivedData);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("^_^ 数据交换失败：" + e.toString() + " ^_^");
            logger.error("^_^ 数据交换失败：" + e.getMessage() + " ^_^");
            taskResult[0] = "99";
            taskResult[1] = "数据交换失败";
        }
        return taskResult;
    }

}
