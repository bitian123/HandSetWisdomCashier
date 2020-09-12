package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;

import com.centerm.epos.EposApplication;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.XLogUtil;

import java.util.HashMap;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.epos.common.TransCode.OBTAIN_TMK;

/**
 * Created by yuhc on 2017/2/8.<br>
 * 域41 受卡机终端标识码/终端号(Card Acceptor Terminal Identification)，8个字节的定长的字母、数字和特殊字符。<br>
 * POS终端的标识码。该标识码在POS中心的网络中唯一标识一个终端，不能重复。<br>
 * 受卡机终端标识码是个关键的数据域。POS中心及发卡方在收到消息后应保存该值，并在应答消息中原样返回给POS终端。<br>
 * POS用该值和11域（受卡方系统跟踪号)、42域（受卡方标识码)、60.2域（批次号)一起匹配原始请求消息。
 */

public class BaseField41 implements I8583Field {

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField41.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String,Object> tradeInfo;

    public BaseField41(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    /**
     * 从业务数据中取出受卡机终端标识码/终端号，并根据规范要求输出8个Byte的ASC码数据。
     * @return 受卡机终端标识码/终端号
     */
    @Override
    public String encode() {
        if (tradeInfo == null) {
            XLogUtil.e(TAG, "^_^ encode 输入参数 tradeInfo 为空 ^_^");
            return null;
        }

        String terminalId = (String) tradeInfo.get(TradeInformationTag.TERMINAL_IDENTIFICATION);

        if (TextUtils.isEmpty(terminalId)) {
            //未获取到终端号，再从终端参数中获取
            terminalId = getTerminalIdFromParam();
        }

        XLogUtil.d(TAG, "^_^ encode result:" + terminalId + " ^_^");
        return terminalId;
    }

    /**
     * 从终端参数中获取终端号
     * @return 终端号
     */
    private String getTerminalIdFromParam() {
        return BusinessConfig.getInstance().getIsoField(EposApplication.getAppContext(), 41);
    }

    /**
     * 从域数据中取出平台返回的返回码，并保存到指定TAG
     *
     * @param fieldMsg 域数据
     * @return 业务数据对象，授权标识应答码
     */
    @Override
    public Map<String, Object> decode(String fieldMsg) {
        if (TextUtils.isEmpty(fieldMsg)) {
            XLogUtil.e(TAG, "^_^ decode 输入参数 fieldMsg 为空 ^_^");
            return null;
        }

        String tradeName = (String) tradeInfo.get(TradeInformationTag.TRANSACTION_TYPE);
        //钱宝需求
        if (TextUtils.isEmpty(tradeName) && OBTAIN_TMK.equals(tradeName)) {
            BusinessConfig.getInstance().setIsoField(EposApplication.getAppContext(), 41, fieldMsg);
        }

        Map<String, Object> tradeInfo = new HashMap<>(1);
        tradeInfo.put(TradeInformationTag.TERMINAL_IDENTIFICATION, fieldMsg);
        XLogUtil.d(TAG, "^_^ decode result:" + tradeInfo + " ^_^");
        return tradeInfo;
    }

}
