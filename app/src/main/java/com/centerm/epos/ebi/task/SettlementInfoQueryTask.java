package com.centerm.epos.ebi.task;

import android.content.Context;

import com.centerm.epos.task.AsyncMultiRequestTask;

import java.util.Map;

public class SettlementInfoQueryTask extends AsyncMultiRequestTask {

    public SettlementInfoQueryTask(Context context, Map<String, Object> dataMap) {
        super(context, dataMap);
        //initParamTip();
        logger.error("^_^ dataMap: ^_^" + dataMap.toString());
    }

    @Override
    public void onStart() {
        super.onStart();

    }
}

