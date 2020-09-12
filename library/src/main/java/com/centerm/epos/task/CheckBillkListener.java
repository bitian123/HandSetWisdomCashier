package com.centerm.epos.task;

import com.centerm.epos.bean.TradeInfo;
import com.centerm.epos.bean.TradeInfoRecord;

import java.util.List;

/**
 * 任务进度监听器
 * author:wanliang527</br>
 * date:2016/12/1</br>
 */

public interface CheckBillkListener{


    /**
     * 任务开始执行
     */
    void onStart();

    /**
     * 任务执行完成
     */
    void onFinish(List<List<TradeInfoRecord>> lists);
}
