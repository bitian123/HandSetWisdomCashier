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
 * 域22 服务点输入方式码(Point Of Service Entry Mode)，3个字节的定长数字字符域。<br>
 * 服务点输入方式码，即持卡人数据（如主账户和个人标识码)的输入方式。服务点(Point Of Service)是指交易的各种始发场合。
 */

public class BaseField22 implements I8583Field {

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField22.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String,Object> tradeInfo;

    public BaseField22(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }
    /**
     * 从业务数据中取出服务点输入方式码，并根据规范要求输出3个Byte的ASC码数据。
     * @return  有效期，例如：
     */
    @Override
    public String encode() {
        if (tradeInfo == null){
            XLogUtil.e(TAG, "^_^ encode 输入参数 tradeInfo 为空 ^_^");
            return null;
        }

        String entryMode = (String) tradeInfo.get(TradeInformationTag.SERVICE_ENTRY_MODE);
        if( TransCode.MAG_ACCOUNT_LOAD_VERIFY.equals(tradeInfo.get(TradeInformationTag.TRANSACTION_TYPE)) ){
            entryMode = (String) tradeInfo.get(TransDataKey.KEY_TRANSFER_INTO_CARD_SERVICE_ENTRY_MODE);
        }
        if (TextUtils.isEmpty(entryMode)){
            String transCode = (String) tradeInfo.get(TradeInformationTag.TRANSACTION_TYPE);
            if (!TextUtils.isEmpty(transCode) && (TransCode.VOID_SCAN.equals(transCode) || TransCode.SALE_SCAN.equals
                    (transCode) || TransCode.REFUND_SCAN.equals(transCode))){
                entryMode = "032";
            }else
                XLogUtil.e(TAG, "^_^ 获取服务点输入方式码失败 ^_^ ");
        }else if (entryMode.length() == 2){
            entryMode += "2";
            /*22域只保存了2位数据，还有PIN的指示位没有保存
            * 增加PIN指示位数据保存
            * author:zhouzhihua
            * */
            if( !TransCode.MAG_ACCOUNT_LOAD_VERIFY.equals(tradeInfo.get(TradeInformationTag.TRANSACTION_TYPE)) ){
                tradeInfo.put(TradeInformationTag.SERVICE_ENTRY_MODE,entryMode);
            }
        }

        if( TransCode.MAG_ACCOUNT_LOAD_VERIFY.equals(tradeInfo.get(TradeInformationTag.TRANSACTION_TYPE)) ){
            entryMode = (String) tradeInfo.get(TransDataKey.KEY_TRANSFER_INTO_CARD_SERVICE_ENTRY_MODE);
        }
        if( TransCode.CONTRACT_INFO_QUERY.equals(tradeInfo.get(TradeInformationTag.TRANSACTION_TYPE)) ){
            entryMode = "012";
        }
        return entryMode;
    }

    /**
     * 占位，平台不返回此域
     *
     * @param fieldMsg null
     * @return null
     */
    @Override
    public Map<String, Object> decode(String fieldMsg) {
        return null;
    }
}
