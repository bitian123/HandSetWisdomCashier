package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;

import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.XLogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuhc on 2017/2/8.<br>
 * 域58 PBOC电子钱包标准的交易信息（PBOC_ELECTRONIC_DATA），最大100个字节的字母、数字字符、特殊符号。<br>
 * 本域在IC卡圈存交易中存放用于计算MAC1、MAC2的数据。在脱机消费中存放用于计算TAC的数据。<br>
 * 报文域中第一、二字节为ASCII码表示的用法标志, 用法以相应的英文缩写标识。
 */

public class BaseField58 implements I8583Field {

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField58.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String,Object> tradeInfo;

    public BaseField58(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    /**
     * 从业务数据中取出PBOC电子钱包标准的交易信息，并根据规范要求输出。
     *
     * @return PBOC电子钱包标准的交易信息
     */
    @Override
    public String encode() {
        if (tradeInfo == null) {
            XLogUtil.e(TAG, "^_^ encode 输入参数 tradeInfo 为空 ^_^");
            return null;
        }
        String icData = (String) tradeInfo.get(TradeInformationTag.PBOC_ELECTRONIC_DATA);
        if (TextUtils.isEmpty(icData)) {
            XLogUtil.e(TAG, "^_^ PBOC电子钱包标准的交易信息为空 ^_^");
            return null;
        }
        String pbocIcData = getPrefixData(tradeInfo) + icData;
        XLogUtil.d(TAG, "^_^ encode result:" + pbocIcData + " ^_^");
        return pbocIcData;
    }

    /**
     * 获取PBOC电子钱包标准信息的类型标识。<br>
     * RQ load ReQuest IC卡的圈存请求（包括指定账户圈存、非指定账户圈存、现金充值）<br>
     * load ResPonse IC卡的圈存应答（包括指定账户圈存、非指定账户圈存、现金充值）<br>
     * 脱机消费请求
     * @param tradeInfo 业务数据
     * @return 业务类型标识
     */
    private String getPrefixData(Map<String, Object> tradeInfo) {
        String transType = (String) tradeInfo.get(TradeInformationTag.TRANSACTION_TYPE);
        String usageTag = null;
        // TODO: 2017/2/12 与配置文件定义的交易标识一致？

        switch (transType){
            case "LOAD_ECASH":
                usageTag = "RQ";
                break;
            case "SALE_ECASH_OFFLINE":
                usageTag = "TA";
                break;
        }
        return usageTag;
    }

    /**
     * 从域数据中取出平台返回的PBOC电子钱包标准的交易信息，并保存到指定TAG
     *
     * @param fieldMsg 域数据
     * @return 业务数据对象，PBOC电子钱包标准的交易信息
     */
    @Override
    public Map<String, Object> decode(String fieldMsg) {
        if (TextUtils.isEmpty(fieldMsg)) {
            XLogUtil.e(TAG, "^_^ decode 输入参数 fieldMsg 为空 ^_^");
            return null;
        }
        if (!"RP".equals(fieldMsg.substring(0,2))){
            XLogUtil.e(TAG, "^_^ PBOC电子钱包信息类型校验失败 ^_^");
            return null;
        }
        Map<String, Object> tradeInfo = new HashMap<>(1);
        tradeInfo.put(TradeInformationTag.PBOC_ELECTRONIC_DATA, fieldMsg.substring(2));
        XLogUtil.d(TAG, "^_^ decode result:" + tradeInfo + " ^_^");
        return tradeInfo;
    }

}
