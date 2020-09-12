package com.centerm.epos.ebi.task;

import android.content.Context;

import com.centerm.epos.ebi.msg.EbiMessageFactory;
import com.centerm.epos.msg.ITransactionMessage;
import com.centerm.epos.present.communication.DataExchanger;
import com.centerm.epos.present.communication.DataExchangerFactory;
import com.centerm.epos.present.communication.HttpCommParameter;
import com.centerm.epos.present.communication.ICommunication;
import com.centerm.epos.task.BaseAsyncTask;

import java.util.HashMap;
import java.util.Map;

/**
 * 该类用于实现队列式网络请求的异步任务
 * 重写基础版本，改变网络请求方式http
 */

public abstract class EbiAsyncMultiRequestTask extends BaseAsyncTask<String, String[]> {
    protected Map<String, Object> dataMap;
    protected ITransactionMessage factory;
    protected DataExchanger client;
    protected int taskRetryTimes;
    protected final String[] taskResult = new String[3];


    public EbiAsyncMultiRequestTask(Context context, Map<String, Object> dataMap) {
        super(context);
        this.dataMap = new HashMap<>();
        this.dataMap.putAll(dataMap);
        factory = EbiMessageFactory.createMessageByType(EbiMessageFactory.MESSAGE_HTTP_JSON, dataMap);
        //client = DataExchangerFactory.getInstance();
        client = new DataExchanger(ICommunication.COMM_HTTP, new HttpCommParameter());
    }

    @Override
    protected String[] doInBackground(String... params) {
        return taskResult;
    }
}