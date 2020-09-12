package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;

import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.XLogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuhc on 2017/2/8.<br>
 * 域49 交易货币代码(Currency Code Of Transaction)，3个字节的定长字符域。<br>
 * 交易所用货币的代码,人民币的货币代码为156，所有预授权/金融类交易消息中用本域标识交易币种.
 */

public class BaseField49 implements I8583Field {

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField49.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String,Object> tradeInfo;

    public BaseField49(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    /**
     * 从业务数据中取出货币代码，并根据规范要求输出15个Byte的ASC码数据。
     *
     * @return 货币代码
     */
    @Override
    public String encode() {
        if (tradeInfo == null) {
            XLogUtil.e(TAG, "^_^ encode 输入参数 tradeInfo 为空 ^_^");
            return null;
        }
        String currentCode = (String) tradeInfo.get(TradeInformationTag.CURRENCY_CODE);

        if (TextUtils.isEmpty(currentCode)) {
            //未获取到货币代码，返回默认货币代码
            currentCode = getDefaultCurrentCode();
        }

        XLogUtil.d(TAG, "^_^ encode result:" + currentCode + " ^_^");
        return currentCode;
    }

    /**
     * 获取默认的货币代码
     * @return 人民币代码
     */
    private String getDefaultCurrentCode() {
        return "156";
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

        Map<String, Object> tradeInfo = new HashMap<>(1);
        tradeInfo.put(TradeInformationTag.CURRENCY_CODE, fieldMsg);
        XLogUtil.d(TAG, "^_^ decode result:" + tradeInfo + " ^_^");
        return tradeInfo;
    }

}
