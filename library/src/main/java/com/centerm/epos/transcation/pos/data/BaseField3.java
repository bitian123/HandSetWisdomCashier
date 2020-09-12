package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;

import com.centerm.epos.bean.TranscationFactor;
import com.centerm.epos.bean.transcation.TradeInformation;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.model.ITradeParameter;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.transcation.pos.constant.TranscationFactorTable;
import com.centerm.epos.utils.XLogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuhc on 2017/2/9.<br>
 * 域3 交易处理码(Transaction Processing Code)由六位数字组成。<br>
 *     第1和第2位表示交易类别。第3和第4位表示受借记和查询，以及转出账户的账户类型。第5和第6位表示受贷记以及转入账户的账户类型。
 */

public class BaseField3 implements I8583Field {
    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField3.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String,Object> tradeInfo;

    public BaseField3(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    @Override
    public String encode() {
        if (tradeInfo == null){
            XLogUtil.e(TAG, "^_^ encode 输入参数 tradeInfo 为空 ^_^");
            return null;
        }
        String tradeName = getTradeName();
        //先从输入参数中获取交易处理码
        String processCode = (String) tradeInfo.get(TradeInformationTag.TRANSACTION_PROCESS_CODE);
        if (!TextUtils.isEmpty(processCode)){
            if (!TextUtils.isEmpty(tradeName)){
                if( tradeName.equals(TransCode.SETTLEMENT_INFO_QUERY)){
                    processCode = "000000";
                }
            }
            XLogUtil.d(TAG, "^_^ encode: "+TradeInformationTag.TRANSACTION_PROCESS_CODE+"="+processCode+" ^_^");
            return processCode;
        }
        //输入参数中无交易处理码，再通过交易名称获取交易处理码
        if (!TextUtils.isEmpty(tradeName)){
            if( tradeName.equals(TransCode.UPLOAD_SCRIPT_RESULT) ){
                processCode = (String)tradeInfo.get(TransDataKey.iso_f3);
            }else if(tradeName.equals(TransCode.SALE_RESULT_QUERY)){
                processCode = "000010";
            }else if(tradeName.equals(TransCode.SETTLEMENT_INFO_QUERY)){
                processCode = "000000";
            } else {
                processCode = getProcessCodeByName(tradeName);
            }
            XLogUtil.d(TAG, "^_^ encode: "+TradeInformationTag.TRANSACTION_TYPE+"="+tradeName+" && "+TradeInformationTag
                    .TRANSACTION_PROCESS_CODE+"="+processCode+" ^_^");
            if (!TextUtils.isEmpty(processCode))
                return processCode;
        }

        XLogUtil.d(TAG, "^_^ encode: "+"交易处理码为空"+" ^_^");
        return null;
    }

    @Override
    public Map<String, Object> decode(String fieldMsg) {
        if (TextUtils.isEmpty(fieldMsg)){
            XLogUtil.e(TAG, "^_^ decode 输入参数 fieldMsg 为空 ^_^");
            return null;
        }
        Map<String,Object> tradeInfo = new HashMap<>(1);
        tradeInfo.put(TradeInformationTag.TRANSACTION_PROCESS_CODE, fieldMsg);
        XLogUtil.d(TAG, "^_^ decode result:"+tradeInfo+" ^_^");
        return tradeInfo;
    }

    /**
     * 根据业务名称获取交易处理码，从交易要素表中获取。
     * @param tradeName 交易名称，例如消费
     * @return 交易处理码
     */
    private String getProcessCodeByName(String tradeName){
        TranscationFactor transcationFactor = TranscationFactorTable.getTranscationFactor(tradeName);
        if (transcationFactor == null)
            return null;
        return transcationFactor.getProcessCode();
    }

    private String getTradeName() {
        String tradeName = null;
        ArrayList<String> msgTagList = (ArrayList<String>) tradeInfo.get(ITradeParameter.KEY_MSG_TAGS);
        if (msgTagList != null && msgTagList.size() > 0)
            tradeName = msgTagList.get(0);
        if (TextUtils.isEmpty(tradeName))
            tradeName = (String) tradeInfo.get(TradeInformationTag.TRANSACTION_TYPE);
        return tradeName;
    }
}
