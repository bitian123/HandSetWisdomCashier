package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;

import com.centerm.epos.EposApplication;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.XLogUtil;

import java.util.HashMap;
import java.util.Map;

import config.BusinessConfig;

/**
 * Created by yuhc on 2017/2/8.<br>
 * 域11 受卡方系统跟踪号(System Trace Audit Number)。6个字节的定长数字字符域。终端流水号<br>
 * POS为每一笔交易产生的顺序编号。POS每上送一次交易此号码增加1。POS流水号为6位数字，值从1至999999循环使用。
 */

public class BaseField11 implements I8583Field {

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField11.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String,Object> tradeInfo;

    public BaseField11(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    /**
     * 从业务数据中取出受卡方系统跟踪号，并根据规范要求输出6个Byte的ASC码数据。
     * @return 受卡方系统跟踪号/终端流水号
     */
    @Override
    public String encode() {
        if (tradeInfo == null) {
            XLogUtil.e(TAG, "^_^ encode 输入参数 tradeInfo 为空 ^_^");
            return null;
        }
        String traceNumber = (String) tradeInfo.get(TradeInformationTag.TRACE_NUMBER);

        if (TextUtils.isEmpty(traceNumber)) {
            //未传入流水号，此处再进行获取
            traceNumber = getTraceNumber();
        }
        tradeInfo.put(TradeInformationTag.TRACE_NUMBER, traceNumber);
        XLogUtil.d(TAG, "^_^ encode result:" + traceNumber + " ^_^");
        return traceNumber;
    }


    /**
     * 获取终端交易流水号
     *
     * @return 交易流水号
     */
    private String getTraceNumber() {
        return BusinessConfig.getInstance().getPosSerial(EposApplication.getAppContext());
    }

    /**
     * 从域数据中取出终端流水号，并保存到指定TAG
     *
     * @param fieldMsg 域数据
     * @return 业务数据对象
     */
    @Override
    public Map<String, Object> decode(String fieldMsg) {
        if (TextUtils.isEmpty(fieldMsg)) {
            XLogUtil.e(TAG, "^_^ decode 输入参数 fieldMsg 为空 ^_^");
            return null;
        }

//        Map<String, Object> tradeInfo = new HashMap<>(1);
//        tradeInfo.put(TradeInformationTag.TRACE_NUMBER, fieldMsg);
//        XLogUtil.d(TAG, "^_^ decode result:" + tradeInfo + " ^_^");
//        return tradeInfo;

        //modify by yuhc 20180101
        //为提高终端的容错性能，忽略平台返回的终端流水号。
        //因为个别平台不规范，返回的流水号与终端上送的不一致；而终端流水号作为交易信息存储的主键，必须要保证正确性，否则会出现问题；终端
        //流水号一般是由终端管理。
        XLogUtil.d(TAG, "^_^ decode result:" + fieldMsg + " ^_^");
        return null;
    }
}
