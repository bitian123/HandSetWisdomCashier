package com.centerm.epos.transcation.pos.constant;

import android.text.TextUtils;

import com.centerm.epos.bean.TranscationFactor;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.utils.XLogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuhc on 2017/2/9.
 * 根据规范定义的交易要素表
 */

public class TranscationFactorTable {
    //日志头
    private static final String TAG = TranscationFactorTable.class.getSimpleName();
    //要素表
    private static final Map<String, TranscationFactor> mFactorTable = new HashMap<>();

    // TODO: 2017/2/9 交易要素表，配置要从XML中获取
    static {
        /*交易名称 消息类型请求 消息类型响应 交易处理码（3域）服务点条件码(25域) 交易类型码（60.1域）是否冲正*/
        mFactorTable.put(TransCode.SALE, new TranscationFactor("消费", "0200", "0210", "000000", "00", "22", true));
        mFactorTable.put(TransCode.SALE_SCAN, new TranscationFactor("扫码付消费", "0200", "0210", "000000", "00", "22",
                true));
        mFactorTable.put(TransCode.VOID, new TranscationFactor("消费撤销", "0200", "0210", "200000", "00", "23", true));
        mFactorTable.put(TransCode.VOID_SCAN, new TranscationFactor("扫码付撤销", "0200", "0210", "200000", "00", "23", true));
        mFactorTable.put(TransCode.BALANCE, new TranscationFactor("余额查询", "0200", "0210", "310000", "00", "01", false));
        mFactorTable.put(TransCode.REFUND, new TranscationFactor("退货", "0220", "0230", "200000", "00", "25", false));
        mFactorTable.put(TransCode.REFUND_SCAN, new TranscationFactor("扫码付退货", "0220", "0230", "200000", "00", "25",
                false));
        mFactorTable.put(TransCode.AUTH, new TranscationFactor("预授权", "0100", "0110", "030000", "06", "10", true));
        mFactorTable.put(TransCode.CANCEL, new TranscationFactor("预授权撤销", "0100", "0110", "200000", "06", "11",
                true));
        mFactorTable.put(TransCode.AUTH_COMPLETE, new TranscationFactor("预授权完成", "0200", "0210", "000000", "06",
                "20",true));
        mFactorTable.put(TransCode.COMPLETE_VOID, new TranscationFactor("预授权完成撤销", "0200", "0210", "200000",
                "06", "21", true));
        mFactorTable.put(TransCode.SALE_INSTALLMENT, new TranscationFactor("分期消费", "0200", "0210", "000000", "64",
                "22", true));
        mFactorTable.put(TransCode.VOID_INSTALLMENT, new TranscationFactor("分期消费撤销", "0200", "0210", "200000", "64", "23", true));

        mFactorTable.put(TransCode.EC_LOAD_INNER, new TranscationFactor("指定账户圈存", "0200", "0210", "600000", "91", "45", false));
        mFactorTable.put(TransCode.EC_LOAD_CASH, new TranscationFactor("现金充值", "0200", "0210", "630000", "91", "46", false));
        /*服务点条件码(25域)
        * 非指定账户圈存 根据转出卡填写
        * */
        mFactorTable.put(TransCode.EC_LOAD_OUTER, new TranscationFactor("非指定账户圈存", "0200", "0210", "620000", "91", "47", false));
        mFactorTable.put(TransCode.EC_VOID_CASH_LOAD, new TranscationFactor("现金充值撤销", "0200", "0210", "170000", "91", "51", true));
        /*脱机退货*/
        mFactorTable.put(TransCode.E_REFUND, new TranscationFactor("脱机退货", "0220", "0230", "200000", "00", "27", false));

        mFactorTable.put(TransCode.IC_OFFLINE_UPLOAD, new TranscationFactor("脱机上送", "0200", "0210", "000000", "00", "36", false));
        mFactorTable.put(TransCode.IC_OFFLINE_UPLOAD_SETTLE, new TranscationFactor("脱机上送", "0200", "0210", "000000", "00", "36", false));

        mFactorTable.put(TransCode.UNION_INTEGRAL_BALANCE, new TranscationFactor("联盟积分查询", "0200", "0210", "310000", "65", "03", false));
        mFactorTable.put(TransCode.UNION_INTEGRAL_REFUND, new TranscationFactor("联盟积分退货", "0220", "0230", "200000", "00", "25", false));

        mFactorTable.put(TransCode.ISS_INTEGRAL_SALE, new TranscationFactor("发卡行积分消费", "0200", "0210", "000000", "65", "22", true));
        mFactorTable.put(TransCode.UNION_INTEGRAL_SALE, new TranscationFactor("联盟积分消费", "0200", "0210", "000000", "65", "22", true));

        mFactorTable.put(TransCode.ISS_INTEGRAL_VOID, new TranscationFactor("发卡行积分撤销", "0200", "0210", "200000", "65", "23", true));
        mFactorTable.put(TransCode.UNION_INTEGRAL_VOID, new TranscationFactor("联盟积分撤销", "0200", "0210", "200000", "65", "23", true));

        mFactorTable.put(TransCode.MAG_CASH_LOAD, new TranscationFactor("磁条卡现金充值", "0200", "0210", "630000", "00", "48", false));

        mFactorTable.put(TransCode.MAG_ACCOUNT_VERIFY, new TranscationFactor("持卡人身份验证", "0100", "0110", "330000", "00", "01", false));
        mFactorTable.put(TransCode.MAG_ACCOUNT_LOAD_VERIFY, new TranscationFactor("持卡人身份验证", "0100", "0110", "330000", "00", "01", false));
        mFactorTable.put(TransCode.MAG_CASH_LOAD_CONFIRM, new TranscationFactor("磁条卡现金充值确认", "0200", "0210", "630000", "00", "48", false));

        mFactorTable.put(TransCode.MAG_ACCOUNT_LOAD, new TranscationFactor("磁条卡账户充值", "0200", "0210", "400000", "66", "49", false));


        mFactorTable.put(TransCode.RESERVATION_SALE, new TranscationFactor("预约消费", "0200", "0210", "000000", "67", "54", true));
        mFactorTable.put(TransCode.RESERVATION_VOID, new TranscationFactor("预约撤销", "0200", "0210", "000000", "67", "53", true));

        mFactorTable.put(TransCode.OFFLINE_SETTLEMENT, new TranscationFactor("离线结算", "0220", "0230", "000000", "00", "30", false));
        mFactorTable.put(TransCode.OFFLINE_ADJUST, new TranscationFactor("结算调整", "0220", "0230", "000000", "00", "32", false));
        mFactorTable.put(TransCode.CONTRACT_INFO_QUERY, new TranscationFactor("合同信息查询", "0700", "0710", "000000", "92", "01", false));
        mFactorTable.put(TransCode.SETTLEMENT_INFO_QUERY, new TranscationFactor("结算账户信息查询", "0720", "0730", "000000", "00", "01", false));


    }

    /**
     * 根据交易名称获取交易要素信息
     *
     * @param transType 交易名称
     * @return 交易要素
     */
    public static TranscationFactor getTranscationFactor(String transType) {
        if (TextUtils.isEmpty(transType)) {
            XLogUtil.e(TAG, "^_^ " + "getTranscationFactor 的输入参数为空" + " ^_^");
            return null;
        }
        TranscationFactor transcationFactor = mFactorTable.get(transType);
        if (transcationFactor == null) {
            XLogUtil.e(TAG, "^_^ 交易要素表中未找到" + transType + "对应的配置" + " ^_^");
            return null;
        }
        return transcationFactor;
    }

    public static void putTradeFactor(String key, TranscationFactor mTranscationFactor) {
        if (TextUtils.isEmpty(key) || mTranscationFactor == null)
            return;
        mFactorTable.put(key, mTranscationFactor);
    }

    public static void putTradeFactor(Map<String, TranscationFactor> factorMap) {
        if (factorMap == null || factorMap.size() == 0)
            return;
        mFactorTable.putAll(factorMap);
    }
}
