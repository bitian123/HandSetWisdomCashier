package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;

import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.XLogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuhc on 2017/2/8.<br>
 * 域14 卡有效期(Date Of Expired),4个字节的定长数字字符域<br>
 * 银行卡的有效期。格式为YYMM，其中YY为年份，MM为月份。对于IC卡，取自tag5F24
 */

public class BaseField14 implements I8583Field {

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField14.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String,Object> tradeInfo;

    public BaseField14(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    /**
     * 从业务数据中取出银行卡有效期，并根据规范要求输出4个Byte的ASC码数据。
     * @return  有效期，例如：0101
     */
    @Override
    public String encode() {
        if (tradeInfo == null){
            XLogUtil.e(TAG, "^_^ encode 输入参数 tradeInfo 为空 ^_^");
            return null;
        }
        String dateExpired = (String) tradeInfo.get(TradeInformationTag.DATE_EXPIRED);
        if( TransCode.MAG_ACCOUNT_LOAD_VERIFY.equals(tradeInfo.get(TradeInformationTag.TRANSACTION_TYPE)) ){
            dateExpired = (String) tradeInfo.get(TransDataKey.KEY_TRANSFER_INTO_CARD_DATE_EXPIRED);
        }
        if (TextUtils.isEmpty(dateExpired)){
            XLogUtil.e(TAG, "^_^ 获取银行卡有效期失败 ^_^ ");
        }
        XLogUtil.d(TAG, "^_^ encode result:"+dateExpired+" ^_^ ");
        return dateExpired;
    }

    /**
     * 从域数据中取出平台返回的卡有效期，并保存到指定TAG
     *
     * @param fieldMsg 域数据
     * @return 业务数据对象，卡有效期
     */
    @Override
    public Map<String, Object> decode(String fieldMsg) {
        if (TextUtils.isEmpty(fieldMsg)) {
            XLogUtil.e(TAG, "^_^ decode 输入参数 fieldMsg 为空 ^_^");
            return null;
        }

        Map<String, Object> tradeInfo = new HashMap<>(1);
        if (!"0000".equals(fieldMsg))
            tradeInfo.put(TradeInformationTag.DATE_EXPIRED, fieldMsg);

        XLogUtil.d(TAG, "^_^ decode result:" + tradeInfo + " ^_^");
        return tradeInfo;
    }
}
