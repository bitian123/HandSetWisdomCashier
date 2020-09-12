package com.centerm.epos.present.communication;

import com.centerm.epos.EposApplication;
import com.centerm.epos.base.BaseRuntimeException;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.net.SequenceHandler;
import com.centerm.epos.transcation.pos.constant.RuntimeExceptionCode;
import com.centerm.epos.utils.XLogUtil;

import config.BusinessConfig;

/**
 * Created by yuhc on 2017/2/13.
 * 数据交换操作的帮助类
 */

public class DataExchangerImp extends DataExchanger {
    private static final String TAG = DataExchangerImp.class.getSimpleName();

    private ICommunication mCommunicater;
    //通讯方式：1 TCP; 2 http; 3 uart;
    int mCommType;
    //通讯参数
    ICommunicationParameter mParameterCallback;

    public DataExchangerImp(int mCommType, ICommunicationParameter parameterCallback) {
        super(mCommType, parameterCallback);
        this.mCommType = mCommType;
        mParameterCallback = parameterCallback;
        mCommunicater = createCommunicater();
    }


    @Override
    public void init(int mCommType, ICommunicationParameter parameterCallback) {

    }

    @Override
    public void doSequenceExchange(String firstTag, byte[] firstData, SequenceHandler handler) {
        if (handler == null) {
            XLogUtil.e(TAG, "回调接收器为空，不发送数据");
            return;
        }
        handler.bindClient(this, true);
        handler.sendNext(firstTag, firstData);
    }

    /**
     * 执行数据交换,目前只考虑短连接
     *
     * @param clienData 待发送数据
     * @return 收到的返回数据
     */
    @Override
    public byte[] doExchange(byte[] clienData) {
        int count;

        if (!mCommunicater.connect()) {
            XLogUtil.e(TAG, "^_^ 连接服务器失败！ ^_^");
            //exchangeIpAddress();
            throw new BaseRuntimeException(RuntimeExceptionCode.CONNECT_SERVER_FAILED, "连接服务器失败");
        }
        //有数据就发送。无数据则直接进入接收
        if (clienData != null && clienData.length > 0) {
            count = mCommunicater.sendData(clienData);
            if (count != clienData.length) {
                XLogUtil.e(TAG, "^_^ 发送数据失败！ ^_^");
                mCommunicater.disconnect();
                throw new BaseRuntimeException(RuntimeExceptionCode.SEND_DATA_FAILED, "发送数据失败");
            }
        }

        byte[] receiveBuffer = mCommunicater.receivedData(0);
        if (receiveBuffer == null || receiveBuffer.length == 0) {
            XLogUtil.e(TAG, "^_^ 接收数据失败！ ^_^");
            mCommunicater.disconnect();
            //exchangeIpAddress();
            throw new BaseRuntimeException(RuntimeExceptionCode.RECEIVE_DATA_FAILED, "接收数据失败");
        }

        //设置为长连接时不主动断开
        if (!BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), BusinessConfig.Key
                .KEEP_CONNECT_ALIVE))
            mCommunicater.disconnect();
        return receiveBuffer;
    }

    /**
     * 根据通讯类型生成实际的通讯模块
     *
     * @return 通讯模块
     */
    private ICommunication createCommunicater() {
        XLogUtil.d(TAG, "^_^ 请求通讯类型为：" + mCommType + " ^_^");
        switch (mCommType) {
            case ICommunication.COMM_TCP:
                return new TcpCommunication(mParameterCallback);
            case ICommunication.COMM_HTTP:
                return new HttpCommunication(mParameterCallback);
            case ICommunication.COMM_UART:
                return new UartCommunication(mParameterCallback);
            case ICommunication.COMM_HTTPS:
                ICommunication communication = (ICommunication) ConfigureManager.getInstance(EposApplication
                        .getAppContext()).getSubPrjClassInstance(HttpsCommunication.class, new
                        Object[]{mParameterCallback});
                if (communication != null) {
                    return communication;
                }
                return new HttpsCommunication(mParameterCallback);
            case ICommunication.COMM_TCPS:
                ICommunication communication1 = (ICommunication) ConfigureManager.getInstance(EposApplication
                        .getAppContext()).getSubPrjClassInstance(TcpsCommunication.class, new
                        Object[]{mParameterCallback});
                if (communication1 != null) {
                    return communication1;
                }
                return new TcpsCommunication(mParameterCallback);
        }
        return null;
    }
}
