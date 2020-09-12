package com.centerm.epos.task;

import android.content.Context;

import com.centerm.epos.msg.ITransactionMessage;
import com.centerm.epos.msg.MessageFactory;
import com.centerm.epos.msg.MessageFactoryPlus;
import com.centerm.epos.net.SocketClient;
import com.centerm.epos.present.communication.DataExchanger;
import com.centerm.epos.present.communication.DataExchangerFactory;
import com.centerm.epos.present.communication.ICommunication;
import com.centerm.epos.present.communication.TcpCommParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * 该类用于实现队列式网络请求的异步任务
 * author:wanliang527</br>
 * date:2016/11/23</br>
 */

public abstract class AsyncMultiRequestTask extends BaseAsyncTask<String, String[]> {
    protected Map<String, Object> dataMap;
    protected ITransactionMessage factory;
    protected DataExchanger client;
    protected int taskRetryTimes;
    protected final String[] taskResult = new String[3];


    public AsyncMultiRequestTask(Context context, Map<String, Object> dataMap) {
        super(context);
        this.dataMap = new HashMap<>();
        this.dataMap.putAll(dataMap);
        factory = MessageFactoryPlus.createMessageByType(MessageFactoryPlus.MESSAGE_ISO8583_POS,
                dataMap);
        client = DataExchangerFactory.getInstance();
    }

    @Override
    protected String[] doInBackground(String... params) {
        return taskResult;
    }


}
