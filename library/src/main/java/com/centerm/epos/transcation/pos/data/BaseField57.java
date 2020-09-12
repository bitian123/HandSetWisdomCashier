package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.transcation.pos.constant.JsonKeyGT;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.DataHelper;
import com.centerm.epos.utils.XLogUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuhc on 2017/2/8.<br>
 * 域63 自定义域(Reserved Private)，最大163个字节的数据。<br>
 * 用法一：国际信用卡公司代码    用法二：操作员代码
 */

public class BaseField57 implements I8583Field {

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField57.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String, Object> tradeInfo;

    public BaseField57(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    /**
     * 从业务数据中取出原始交易信息，并根据规范要求输出。
     *
     * @return 原始交易信息
     */
    @Override
    public String encode() {
        if (tradeInfo == null) {
            XLogUtil.e(TAG, "^_^ encode 输入参数 tradeInfo 为空 ^_^");
            return null;
        }
        String transType = (String) tradeInfo.get(TradeInformationTag.TRANSACTION_TYPE);
        if (TextUtils.isEmpty(transType)) {
            XLogUtil.e(TAG, "^_^ 获取业务数据失败 ^_^");
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        if (TransCode.SALE.equals(transType) || TransCode.AUTH.equals(transType)) {
            //设备类型
            stringBuilder.append("04");
            //厂商编号
            stringBuilder.append("000006");
            //设备类型
            stringBuilder.append("04");
            //序列号
            String sn = "";
            try {
                sn = DeviceFactory.getInstance().getSystemDev().getTerminalSn();
                stringBuilder.append(sn);
            } catch (Exception e) {

            }
            String fillStr = "                                                  ";
            stringBuilder.append(fillStr.substring(0, 50 - sn.length()));
        } else if (TransCode.CONTRACT_INFO_QUERY.equals(transType)) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String tradeDate = formatter.format(new Date());
            String subjectName = (String) tradeInfo.get(JsonKeyGT.subjectName);
            String contractNo = (String) tradeInfo.get(JsonKeyGT.contractNo);
            stringBuilder.append("FD")
                    .append("HF")
                    .append(DataHelper.fillRightSpace(contractNo, 20))
                    .append(tradeDate.substring(0, 6))
                    .append(DataHelper.fillLeftZero("" + subjectName.length(), 3))
                    .append(subjectName)
                    .append("#");
        }
        XLogUtil.d(TAG, "^_^ encode result:" + stringBuilder.toString() + " ^_^");
        return stringBuilder.toString();
    }

    /**
     * {@link com.centerm.epos.bean.TradeInfoRecord#bankcardOganization} <br/>
     * {@link TradeInformationTag#BANKCARD_ORGANIZATION} 用来保存卡组织代码数据，数据将被保存到数据库   <br/>
     * {@link TradeInformationTag#CREDIT_CODE} 临时数据不保存数据库<br/>
     * {@link TradeInformationTag#CREDIT_CARD_COMPANY_CODE} 临时数据不保存数据库<br/>
     *
     * @param fieldMsg 域数据
     * @return 国际信用卡代码
     */
    @Override
    public Map<String, Object> decode(String fieldMsg) {
        if (TextUtils.isEmpty(fieldMsg)) {
            XLogUtil.e(TAG, "^_^ decode 输入参数 fieldMsg 为空 ^_^");
            return null;
        }
        Map<String, Object> tradeData = new HashMap<>(1);
        String tranType = (String) tradeInfo.get(TradeInformationTag.TRANSACTION_TYPE);
        if (TransCode.SETTLEMENT.equals(tranType)) {
            tradeData.put(TradeInformationTag.OPERATOR_CODE, fieldMsg);
        } else {
            tradeData.put(TradeInformationTag.CREDIT_CODE, fieldMsg.substring(0, 3));
            tradeData.put(TradeInformationTag.BANKCARD_ORGANIZATION, fieldMsg.substring(0, 3));
        }

        XLogUtil.d(TAG, "^_^ decode result:" + tradeInfo + " ^_^");
        return tradeData;
    }

}
