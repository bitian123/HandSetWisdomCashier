package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;

import com.centerm.epos.EposApplication;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.db.CommonManager;
import com.centerm.epos.redevelop.ICommonManager;
import com.centerm.epos.transcation.pos.constant.JsonKeyGT;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.DataHelper;
import com.centerm.epos.utils.XLogUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.centerm.epos.common.TransDataKey.iso_f48;
import static com.centerm.epos.common.TransDataKey.key_batch_upload_count;

/**
 * Created by yuhc on 2017/2/8.<br>
 * 域48 附加数据－私有(Additional Data - Private)，最大322个字节的数据。<br>
 * 用于存放POS批结算时的结算总额、批上送时的交易明细和交易明细总笔数
 */

public class BaseField48 implements I8583Field {

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField48.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String, Object> tradeInfo;

    public BaseField48(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    /**
     * 根据交易类型从业务数据中取出结算总额或交易明细。
     *
     * @return 结算总额或交易明细
     */
    @Override
    public String encode() {
        if (tradeInfo == null) {
            XLogUtil.e(TAG, "^_^ encode 输入参数 tradeInfo 为空 ^_^");
            return null;
        }

        //根据交易类型找到对应的用法
        String tradeId = (String) tradeInfo.get(TradeInformationTag.TRANSACTION_TYPE);
        if (TextUtils.isEmpty(tradeId)) {
            XLogUtil.e(TAG, "^_^ 获取交易类型失败 ^_^");
            return null;
        }

        String message = null;
        // TODO: 2017/2/10 交易类型要与配置匹配
        switch (tradeId) {
            case TransCode.SETTLEMENT:
                //结算，用法一：结算总额
                message = getTransSummary(tradeInfo);
                break;
            case TransCode.TRANS_IC_DETAIL:
            case TransCode.TRANS_CARD_DETAIL:
            case TransCode.TRANS_FEFUND_DETAIL:
                //批上送，用法二：磁条卡交易明细
                message = getTransDetail(tradeInfo);
                break;
            case TransCode.SETTLEMENT_DONE:
                //批上送结束，用法三：交易明细总笔数
                message = getTransCount(tradeInfo);
                break;
            case TransCode.SETTLEMENT_INFO_QUERY:
                //結算账户信息
                message=(String) tradeInfo.get(JsonKeyGT.subjectName);
                break;
//            case "SETTLE_OFFLINE":
//            case "SETTLE_ADJUST":
//                //离线结算/离线调整，用法四：小费金额
//                message = getTransTips(tradeInfo);
//                break;
//            case "PBOC_LOAD_CUSTOM":
//                //非指定账户圈存，用法五：基于PBOC电子钱包/存折标准的非指定账户圈存信息和基于PBOC借/贷记应用的小额支付非指定账户圈存信
//                message = getPbocCustomLoadInfo(tradeInfo);
//                break;
            case TransCode.EC_LOAD_OUTER:/*非指定账户圈存，用法五：基于PBOC电子钱包/存折标准的非指定账户圈存信息和基于PBOC借/贷记应用的小额支付非指定账户圈存信*/
                message = getPbocCustomLoadInfo(tradeInfo);
                break;
            case TransCode.MAG_ACCOUNT_VERIFY:
            case TransCode.MAG_ACCOUNT_LOAD_VERIFY:
                message = "11";
            break; case TransCode.SALE:
                message = (String) tradeInfo.get("subjectName");
                if(!TextUtils.isEmpty(message)){
                    message = message.replace("-","D");
                }
                break;

        }
        XLogUtil.d(TAG, "^_^ encode result:" + message + " ^_^");
        return message;
    }

    /**
     * 占位，平台不返回此域
     *
     * @param fieldMsg null
     * @return null
     */
    @Override
    public Map<String, Object> decode(String fieldMsg) {
        if (TextUtils.isEmpty(fieldMsg)) {
            XLogUtil.e(TAG, "^_^ decode 输入参数 fieldMsg 为空 ^_^");
            return null;
        }

        Map<String, Object> tradeInfo = new HashMap<>(2);
        tradeInfo.put(TradeInformationTag.SETTLEMENT_RESULT, fieldMsg);
        XLogUtil.d(TAG, "^_^ decode result:" + tradeInfo + " ^_^");
        return tradeInfo;
    }

    /**
     * 获取圈存中转入卡（即电子钱包卡或电子现金卡）的服务点输入方式码和IC卡条件代码
     *
     * @param tradeInfo 业务信息
     * @return 域数据
     */
    private String getPbocCustomLoadInfo(Map<String, Object> tradeInfo) {
        // TODO: 2017/2/10
        String entryMode = (String) tradeInfo.get(TransDataKey.KEY_TRANSFER_INTO_CARD_SERVICE_ENTRY_MODE);
        entryMode += "20";
        return entryMode;
    }

    /**
     * 获取小费金额，外卡交易。
     *
     * @param tradeInfo 业务信息
     * @return 域数据
     */
    private String getTransTips(Map<String, Object> tradeInfo) {
        // TODO: 2017/2/10

        return null;
    }

    /**
     * 批上送结束消息时，获取本批所有批上送消息中包含的交易明细总笔数。
     *
     * @param tradeInfo 业务信息
     * @return 域数据
     */
    private String getTransCount(Map<String, Object> tradeInfo) {
        return DataHelper.formatToXLen((String) tradeInfo.get(key_batch_upload_count), 4);
    }

    /**
     * 获取批上送消息中磁条卡交易的明细信息
     *
     * @param tradeInfo 业务信息
     * @return 域数据
     */
    private String getTransDetail(Map<String, Object> tradeInfo) {
        // TODO: 2017/2/10
        return (String) tradeInfo.get(iso_f48);
    }

    /**
     * 获取成功的交易的借记总金额、借记总笔数、贷记总金额、贷记总笔数
     *
     * @param tradeInfo 业务信息
     * @return 域数据
     */
    private String getTransSummary(Map<String, Object> tradeInfo) {
        initAmountAndCount();
        return DataHelper.formatAmount(debitAmount) + DataHelper.formatToXLen
                (debitCount, 3) + DataHelper.formatAmount(creditAmount) + DataHelper
                .formatToXLen(creditCount, 3) + "0"
                + "0000000000000000000000000000000";    //外卡结算总额
    }

    private static double debitAmount;//借记金额
    private static int debitCount;//借记笔数
    private static double creditAmount;//贷记金额
    private static int creditCount;//贷记金额
    private static List<TradeInfoRecord> jiejiList;
    private static List<TradeInfoRecord> daijiList;

    public static void initAmountAndCount() {
        final ICommonManager commonManager = (ICommonManager) ConfigureManager
                .getInstance(EposApplication.getAppContext()).getSubPrjClassInstance(new CommonManager());
        try {
            jiejiList = commonManager.getDebitList();
            daijiList = commonManager.getCreditList();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        sumCreditAmountAndCount();
        sumDebitAmountAndCount();
    }


    /**
     * 借记总金额及总笔数
     */
    public static void sumDebitAmountAndCount() {
        debitAmount = 0.0;
        debitCount = 0;
        if (jiejiList != null && jiejiList.size() > 0) {
            for (TradeInfoRecord info :
                    jiejiList) {
                debitAmount += DataHelper.parseIsoF4(info.getAmount());
            }
            debitCount = jiejiList.size();
        } else {
            XLogUtil.d(TAG, "查询到成功的借记交易为空！");
        }
        debitAmount = DataHelper.formatDouble(debitAmount);
    }


    /**
     * 贷记总金额及总笔数
     */
    public static void sumCreditAmountAndCount() {
        creditAmount = 0.0;
        creditCount = 0;
        if (daijiList != null && daijiList.size() > 0) {
            for (TradeInfoRecord info :
                    daijiList) {
                creditAmount += DataHelper.parseIsoF4(info.getAmount());
            }
            creditCount = daijiList.size();
        } else {
            XLogUtil.d(TAG, "查询到成功的借记交易为空！");
        }
        creditAmount = DataHelper.formatDouble(creditAmount);
    }
}
