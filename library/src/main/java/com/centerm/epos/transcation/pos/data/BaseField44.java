package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;

import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.XLogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuhc on 2017/2/8.<br>
 * 域44 附加响应数据(Additional Response Data)，最大25个字节的数据。<br>
 * 在交易响应消息中返回接收机构和收单机构的标识码。
 */

public class BaseField44 implements I8583Field {

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField44.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String,Object> tradeInfo;

    public BaseField44(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    /**
     * 占位，无实际功能。附加响应数据为系统返回
     *
     * @return null
     */
    @Override
    public String encode() {
        return null;
    }

    /**
     * 从域数据中取出平台返回的附加响应数据，并解析出接收机构标识码和收单机构标识码，并保存到指定TAG
     *
     * @param fieldMsg 域数据
     * @return 业务数据对象，接收机构标识码和收单机构标识码
     */
    @Override
    public Map<String, Object> decode(String fieldMsg) {
        if (TextUtils.isEmpty(fieldMsg)) {
            XLogUtil.e(TAG, "^_^ decode 输入参数 fieldMsg 为空 ^_^");
            return null;
        }

        Map<String, Object> tradeInfo = new HashMap<>(2);
        //前11 Byte为接收机构标识码/发卡行标识码
        if (fieldMsg.length()>=11)
            tradeInfo.put(TradeInformationTag.ISSUER_IDENTIFICATION, fieldMsg.substring(0,11));
        //后11 Byte为收单机构标识码/商户结算行标识码
        if (fieldMsg.length()>=22)
            tradeInfo.put(TradeInformationTag.ACQUIRER_IDENTIFICATION, fieldMsg.substring(11));

        XLogUtil.d(TAG, "^_^ decode result:" + tradeInfo + " ^_^");
        return tradeInfo;
    }
}
