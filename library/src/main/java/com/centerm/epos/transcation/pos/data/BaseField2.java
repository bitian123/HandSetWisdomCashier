package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;

import com.centerm.epos.common.TransCode;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.XLogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuhc on 2017/2/8.<br>
 * 域2：主账号(Primary Account Number)，银行卡号。
 */

public class BaseField2 implements I8583Field {

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField2.class.getSimpleName();
    protected Map<String,Object> tradeInfo;

    public BaseField2(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    /**
     * 从业务数据中取出银行卡号，并根据规范要求输出最大19个Byte的ASC码数据。
     * @return  卡号
     */
    @Override
    public String encode() {
        if (tradeInfo == null){
            XLogUtil.e(TAG, "^_^ encode 输入参数 tradeInfo 为空 ^_^");
            return null;
        }
        String cardNum = (String) tradeInfo.get(TradeInformationTag.BANK_CARD_NUM);

        if( TransCode.MAG_ACCOUNT_LOAD_VERIFY.equals(tradeInfo.get(TradeInformationTag.TRANSACTION_TYPE)) ){
            cardNum = (String) tradeInfo.get(TradeInformationTag.TRANSFER_INTO_CARD);
        }
        else if(TransCode.RESERVATION_SALE.equals(tradeInfo.get(TradeInformationTag.TRANSACTION_TYPE))){
            cardNum = null;/*预约消费不带卡号*/
        }
        if (TextUtils.isEmpty(cardNum)){
            XLogUtil.e(TAG, "^_^ 获取卡号失败 ^_^ ");
            return null;
        }

        // 2017/2/8  卡号长度值校验，暂不做严格检查
        XLogUtil.d(TAG, "^_^ encode result:"+cardNum+" ^_^");
        return cardNum;
    }

    /**
     * 从域数据中取出卡号，并保存到指定TAG
     * @param fieldMsg  域数据
     * @return  业务数据对象
     */
    @Override
    public Map<String,Object> decode(String fieldMsg) {
        if (TextUtils.isEmpty(fieldMsg)){
            XLogUtil.e(TAG, "^_^ decode 输入参数 fieldMsg 为空 ^_^");
            return null;
        }
        // 2017/2/8  卡号长度值校验，暂不做严格检查


        Map<String,Object> tradeInfo = new HashMap<>(1);
        tradeInfo.put(TradeInformationTag.BANK_CARD_NUM, fieldMsg);

        XLogUtil.d(TAG, "^_^ decode result:"+tradeInfo+" ^_^");
        return tradeInfo;
    }
}
