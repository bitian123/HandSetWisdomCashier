package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;

import com.centerm.cloudsys.sdk.common.utils.StringUtils;
import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;
import com.centerm.cpay.midsdk.dev.define.pinpad.EnumDataEncryMode;
import com.centerm.epos.EposApplication;
import com.centerm.epos.common.EncryptAlgorithmEnum;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.XLogUtil;

import java.util.Arrays;
import java.util.Map;

import config.BusinessConfig;

/**
 * Created by yuhc on 2017/2/8.<br>
 * 域35 2磁道数据(Track 2 Data)，最大37个字符。<br>
 * 它从第二磁道开始符；后的第一个字符读起，包括域的分隔符，但不包括结束符和LRC符。
 */

public class BaseField35 implements I8583Field {

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField35.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String,Object> tradeInfo;

    public BaseField35(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    /**
     * 从业务数据中取出2磁道数据，并根据规范要求输出最大37个Byte的ASC码数据。
     * 如果是持卡人身份验证
     * @return  2磁道数据
     */
    @Override
    public String encode() {
        if (tradeInfo == null){
            XLogUtil.e(TAG, "^_^ encode 输入参数 tradeInfo 为空 ^_^");
            return null;
        }
        String entryMode = (String) tradeInfo.get(TradeInformationTag.SERVICE_ENTRY_MODE);
        if (TextUtils.isEmpty(entryMode)){
            return null;
        }
        String track2Data;
        if( TransCode.MAG_ACCOUNT_LOAD_VERIFY.equals(tradeInfo.get(TradeInformationTag.TRANSACTION_TYPE)) ){
            track2Data = (String) tradeInfo.get(TransDataKey.KEY_TRANSFER_INTO_CARD_TRACK_2_DATA);
        }
        else{
            track2Data = (String) tradeInfo.get(TradeInformationTag.TRACK_2_DATA);
        }

        if (TextUtils.isEmpty(track2Data)){
            XLogUtil.e(TAG, "^_^ 从业务数据中获取2磁道数据失败 ^_^ ");
            return null;
        }
        if (BusinessConfig.isTrackEncrypt(EposApplication.getAppContext())) {
            track2Data = encryptTrackData(track2Data);
            if (TextUtils.isEmpty(track2Data)) {
                XLogUtil.e(TAG, "^_^ 2磁道数据加密失败 ^_^ ");
                return null;
            }
        }
        XLogUtil.d(TAG, "^_^ encode result:" + track2Data + " ^_^");
        return track2Data;
    }

    public static String encryptTrackData(String track2Data) {
        if (TextUtils.isEmpty(track2Data))
            return null;
        final String fillStr = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";
        int offset = track2Data.length()%2 == 1 ? 1 : 2;
        EncryptAlgorithmEnum alg = Settings.getEncryptAlgorithmEnum(EposApplication.getAppContext());
        int tdbLen = (alg == EncryptAlgorithmEnum.SM4 ? 32 : 16);
        String TDB2, formatedTDB;
        int beginIndex = track2Data.length() - tdbLen - offset;
        TDB2 = track2Data.substring(beginIndex < 0 ? 0 : beginIndex, track2Data.length()-offset);
        if (beginIndex < 0)
            formatedTDB = TDB2 + fillStr.substring(0, Math.abs(beginIndex));
        else
            formatedTDB = TDB2;
        try {
            byte[] encryptedData = DeviceFactory.getInstance().getPinPadDev().encryData(alg == EncryptAlgorithmEnum
                            .SM4 ? EnumDataEncryMode.SM4 : EnumDataEncryMode.ECB, null, formatedTDB);
            return ( ( beginIndex >= 0 ) ? track2Data.substring(0, beginIndex) : "")+HexUtils.bytesToHexString(encryptedData)+track2Data.substring(track2Data.length()-offset);
            //return track2Data.replace(TDB2,HexUtils.bytesToHexString(encryptedData));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
