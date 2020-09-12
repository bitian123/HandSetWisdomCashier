package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;

import com.centerm.epos.common.TransCode;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.XLogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuhc on 2017/2/8.<br>
 * 域37 检索参考号(Retrieval Reference Number)，12个字节的定长字符域。<br>
 * POS中心为交易分配的流水号，在应答报文中下传给POS终端作为对账参考号，并用于事后查证。<br>
 * POS中心赋予每笔从POS终端收到的预授权/金融/冲正交易的、在每个清算日内唯一的系统流水号。<br>
 * POS中心用该流水号与消息类型、12域（受卡方所在地时间)和13域（受卡方所在地日期)的组合唯一地标识该笔交易（重复发送的冲正交易将被认为是一笔交易)。<br>
 * POS终端在收到交易应答消息时可获取POS中心的系统流水号，本域的值可作为日后交易查询的依据。
 */

public class BaseField37 implements I8583Field {

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField37.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String,Object> tradeInfo;

    public BaseField37(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    /**
     * 占位，无实际功能。检索参考号为系统返回
     * 撤销类交易需要填充该域 {@link TransCode#TRANS_VOID_ENDWITH}<br/>
     * @return null
     */
    @Override
    public String encode() {
        if (tradeInfo == null){
            XLogUtil.e(TAG, "^_^ encode 输入参数 tradeInfo 为空 ^_^");
            return null;
        }
        String result = null;
        String tradeName = (String) tradeInfo.get(TradeInformationTag.TRANSACTION_TYPE);
        if (TextUtils.isEmpty(tradeName))
            return null;
        switch (tradeName){
            case TransCode.VOID:
            case TransCode.VOID_INSTALLMENT:
            case TransCode.VOID_SCAN:
            case TransCode.REFUND:
            case TransCode.REFUND_SCAN:
            case TransCode.COMPLETE_VOID:
            case TransCode.AUTH_COMPLETE_REVERSE:
            case TransCode.ESIGN_UPLOAD:
            case TransCode.ESIGN_UPLOAD_PART:
            case TransCode.UPLOAD_SCRIPT_RESULT:
            case TransCode.UNION_INTEGRAL_REFUND:
            case TransCode.UNION_INTEGRAL_VOID:
            case TransCode.ISS_INTEGRAL_VOID:
                result = (String) tradeInfo.get(TradeInformationTag.REFERENCE_NUMBER);
        }
        if( TextUtils.isEmpty(result) &&  tradeName.endsWith(TransCode.TRANS_VOID_ENDWITH)){
            result = (String) tradeInfo.get(TradeInformationTag.REFERENCE_NUMBER);
        }
        XLogUtil.d(TAG, "^_^ encode result:" + result + " ^_^");
        return result;
    }

    /**
     * 从域数据中取出平台返回的检索参考号，并保存到指定TAG
     *
     * @param fieldMsg 域数据
     * @return 业务数据对象，检索参考号
     */
    @Override
    public Map<String, Object> decode(String fieldMsg) {
        if (TextUtils.isEmpty(fieldMsg)) {
            XLogUtil.e(TAG, "^_^ decode 输入参数 fieldMsg 为空 ^_^");
            return null;
        }

        Map<String, Object> tradeInfo = new HashMap<>(1);
        tradeInfo.put(TradeInformationTag.REFERENCE_NUMBER, fieldMsg);
        XLogUtil.d(TAG, "^_^ decode result:" + tradeInfo + " ^_^");
        return tradeInfo;
    }
}
