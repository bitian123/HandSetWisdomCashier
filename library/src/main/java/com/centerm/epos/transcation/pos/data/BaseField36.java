package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;

import com.centerm.epos.EposApplication;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.XLogUtil;

import java.util.Map;

import config.BusinessConfig;

import static com.centerm.epos.transcation.pos.data.BaseField35.encryptTrackData;

/**
 * Created by yuhc on 2017/2/8.<br>
 * 域36 3磁道数据(Track 3 Data)，最大104个字符。<br>
 * 从第三磁道开始符；后的第一个字符读起，包括域的分隔符，但不包括结束符和LRC符。
 */

public class BaseField36 implements I8583Field {

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField36.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String,Object> tradeInfo;

    public BaseField36(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    /**
     * 从业务数据中取出3磁道数据，并根据规范要求输出最大104个Byte的ASC码数据。
     * @return  3磁道数据
     */
    @Override
    public String encode() {
        if (tradeInfo == null){
            XLogUtil.e(TAG, "^_^ encode 输入参数 tradeInfo 为空 ^_^");
            return null;
        }
        String entryMode = (String) tradeInfo.get(TradeInformationTag.SERVICE_ENTRY_MODE);
        if (TextUtils.isEmpty(entryMode) || !entryMode.startsWith("02")){
            return null;
        }
        String track3Data;
        if( TransCode.MAG_ACCOUNT_LOAD_VERIFY.equals(tradeInfo.get(TradeInformationTag.TRANSACTION_TYPE)) ){
            track3Data = (String) tradeInfo.get(TransDataKey.KEY_TRANSFER_INTO_CARD_TRACK_3_DATA);
        }
        else{
            track3Data = (String) tradeInfo.get(TradeInformationTag.TRACK_3_DATA);
        }
        if (TextUtils.isEmpty(track3Data)){
            XLogUtil.e(TAG, "^_^ 从业务数据中获取3磁道数据失败 ^_^ ");
            return null;
        }
        if (BusinessConfig.isTrackEncrypt(EposApplication.getAppContext())) {
            track3Data = encryptTrackData(track3Data);
            if (TextUtils.isEmpty(track3Data)) {
                XLogUtil.e(TAG, "^_^ 3磁道数据加密失败 ^_^ ");
                return null;
            }
        }
        XLogUtil.d(TAG, "^_^ encode result:" + track3Data + " ^_^");
        return track3Data;
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
