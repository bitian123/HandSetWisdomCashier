package com.centerm.epos.channels;

import android.os.SystemClock;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.centerm.epos.EposApplication;
import com.centerm.epos.channels.online.CpayMessageFactory;
import com.centerm.epos.channels.online.HttpResponse;
import com.centerm.epos.channels.online.PayChannelBody;
import com.centerm.epos.common.Settings;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.configure.EposProject;
import com.centerm.epos.present.communication.DataExchanger;
import com.centerm.epos.present.communication.DataExchangerImp;
import com.centerm.epos.present.communication.ICommunication;
import com.centerm.epos.present.communication.ICommunicationParameter;
import com.centerm.epos.utils.XLogUtil;


/**
 * Created by yuhc on 2017/3/23.
 */

public class SwitchPayChannel implements Runnable {
    private static final String TAG = SwitchPayChannel.class.getSimpleName();
    private int mDelayTimsMs = 0;

    public SwitchPayChannel(int mDelayTimsMs) {
        this.mDelayTimsMs = mDelayTimsMs;
    }

    public static void queryPayChannelInf() {
        String project = ConfigureManager.getInstance(EposApplication.getAppContext()).getProject();
        XLogUtil.d(TAG, "^_^ " + "queryPayChannelInf: channel name " + project + " ^_^");
        String resMessage = CpayMessageFactory.createRequestMsg(EposProject.getInstance().getChannelId(project));
        DataExchanger dataExchanger = getDataExchagerObj(ICommunication.COMM_HTTP, new CpayCommParamete());
        byte[] resp;
        try {
            resp = dataExchanger.doExchange(resMessage.getBytes());
        } catch (Exception e) {
            XLogUtil.e(TAG, "^_^ " + "queryPayChannelInf:" + e.getMessage() + " ^_^");
            return;
        }
        if (resp == null || resp.length == 0) {
            XLogUtil.e(TAG, "^_^ " + "queryPayChannelInf:received data error" + " ^_^");
            return;
        }
        HttpResponse httpResponse = CpayMessageFactory.parseResponseMsg(new String(resp));
        if (httpResponse == null) {
            XLogUtil.e(TAG, "^_^ " + "queryPayChannelInf:parse response message error" + " ^_^");
            return;
        }
        if (!checkRespMessage(httpResponse)) {
            XLogUtil.e(TAG, "^_^ " + "queryPayChannelInf: response message check failed" + " ^_^");
            return;
        }
        JSONObject payChannelJSONObject = (JSONObject) httpResponse.getBody();
        if (payChannelJSONObject == null) {
            XLogUtil.d(TAG, "^_^ " + "queryPayChannelInf: response message body is null" + " ^_^");
            return;
        }
        PayChannelBody payChannelBody = payChannelJSONObject.toJavaObject(PayChannelBody.class);
        String payCode = payChannelBody.getPayCode();
        if (EposProject.getInstance().getChannelId(project).equals(payCode)) {
            XLogUtil.d(TAG, "^_^ " + "queryPayChannelInf:response channel code is " + payCode + " same with local ^_^");
            return;
        }

        String channelName = checkNewPayChannel(payCode);
        XLogUtil.d(TAG, "^_^ " + "queryPayChannelInf: new pay channel name is " + channelName + " ^_^");
        if (!TextUtils.isEmpty(channelName)) {
            if (channelName.equals(Settings.getNewPayChannel(EposApplication.getAppContext()))) {
                XLogUtil.d(TAG, "^_^ " + "queryPayChannelInf: new pay channel is already recorded" + " ^_^");
                return;
            }
            Settings.setNewPayChannel(channelName);
        }
    }

    private static DataExchanger getDataExchagerObj(int mCommType, ICommunicationParameter parameterCallback) {
        DataExchanger dataExchanger = (DataExchanger) ConfigureManager.getInstance
                (EposApplication.getAppContext()).getSubPrjClassInstance(DataExchangerImp.class);
        if (null != dataExchanger) {
            dataExchanger.init(mCommType, parameterCallback);
            return dataExchanger;
        }
        return new DataExchangerImp(mCommType, parameterCallback);
    }

    private static String checkNewPayChannel(String payCode) {
        if (!EposProject.getInstance().isPayChannelProjectExist(payCode)) {
            XLogUtil.e(TAG, "^_^ " + "queryPayChannelInf:response channel is not support ^_^");
            return null;
        }
        return EposProject.getInstance().getProjectTagByChannelID(payCode);
    }

    /**
     * 返回报文校验
     */
    private static boolean checkRespMessage(HttpResponse httpResponse) {
        String MAC = httpResponse.getMAC();
        if (TextUtils.isEmpty(MAC)) {
            XLogUtil.e(TAG, "^_^ " + "queryPayChannelInf:MAC IS NULL" + " ^_^");
            return false;
        }
        //其它校验


        return true;
    }

    @Override
    public void run() {
        if (mDelayTimsMs != 0)
            SystemClock.sleep(mDelayTimsMs);
//        queryPayChannelInf();
    }

    private static class CpayCommParamete implements ICommunicationParameter {

        @Override
        public Object getConnectParameter() {
            return "http://192.168.48.42:6930/epos/payment/upload";
        }

        @Override
        public Object getSendParameter() {
            return "POST";
        }

        @Override
        public Object getReceiveParameter() {
            return null;
        }

        @Override
        public Object getDisconnectParam() {
            return null;
        }
    }
}
