package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;

import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;
import com.centerm.epos.EposApplication;
import com.centerm.epos.common.EncryptAlgorithmEnum;
import com.centerm.epos.common.Settings;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.XLogUtil;

import jxl.biff.ByteArray;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuhc on 2017/2/8.<br>
 * 域52 个人标识码数据/密码(PIN Data)，8个字节的定长二进制数域 <br>
 * 持卡人的个人密码的密文。
 */

public class BaseField52 implements I8583Field {

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField52.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String,Object> tradeInfo;

    public BaseField52(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    /**
     * 从业务数据中取出密码，并根据规范要求输出8个字节的定长二进制数。
     *
     * @return 货币代码
     */
    @Override
    public String encode() {
        if (tradeInfo == null) {
            XLogUtil.e(TAG, "^_^ encode 输入参数 tradeInfo 为空 ^_^");
            return null;
        }
        Object customerPWDObj = tradeInfo.get(TradeInformationTag.CUSTOMER_PASSWORD);
        if (customerPWDObj == null){
            XLogUtil.e(TAG, "^_^ 获取持卡人密码失败 ^_^");
            return null;
        }
        String password = null;
        if (customerPWDObj instanceof ByteArray){
            byte[] pwdBytes = ((ByteArray)customerPWDObj).getBytes();
            //密码密文肯定是8的倍数，除非是国密算法
            if (pwdBytes == null || (pwdBytes.length != 8 && pwdBytes.length != 16)){
                XLogUtil.e(TAG, "^_^ 个人密码BYTE类型数据校验失败 ^_^");
                return null;
            }
            password = HexUtils.bytesToHexString(pwdBytes);
        }else if (customerPWDObj instanceof String){
            password = (String) customerPWDObj;
            if (TextUtils.isEmpty(password)) {
                XLogUtil.e(TAG, "^_^ 个人密码为空 ^_^");
                return null;
            }
        }
        if(EncryptAlgorithmEnum.SM4 == Settings.getEncryptAlgorithmEnum(EposApplication.getAppContext())){
            tradeInfo.put(TradeInformationTag.SM4_PASSWORD, password);
            //国密算法的密码数据移到62域上送，此域填充全0
            password = "0000000000000000";
        }
        XLogUtil.d(TAG, "^_^ encode result:" + password + " ^_^");
        return password;
    }

    /**
     *
     * @param fieldMsg 域数据
     * @return null
     */
    @Override
    public Map<String, Object> decode(String fieldMsg) {
        XLogUtil.d(TAG, "^_^ decode running but do nothing ^_^");
        return null;
    }

}
