package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;

import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.XLogUtil;

import java.util.Map;

/**
 * Created by yuhc on 2017/2/8.<br>
 * 域26 服务点PIN获取码(Point Of Service PIN Capture Code)，2个字节的定长数字字符域。<br>
 * 服务点设备所允许输入的个人密码明文的最大长度。4-12
 */

public class BaseField26 implements I8583Field {

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField26.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String,Object> tradeInfo;

    public BaseField26(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    /**
     * 从业务数据中取出服务点PIN获取码，并根据规范要求输出2个Byte的ASC码数据。
     * @return  服务点PIN获取码，例如：06
     */
    @Override
    public String encode() {
        if (tradeInfo == null){
            XLogUtil.e(TAG, "^_^ encode 输入参数 tradeInfo 为空 ^_^");
            return null;
        }
        boolean hasPin = !TextUtils.isEmpty((CharSequence) tradeInfo.get(TradeInformationTag.CUSTOMER_PASSWORD));
        if (!hasPin){
            XLogUtil.e(TAG, "^_^ 未输入PIN ^_^");
            return null;
        }
        String pinCaptureCode = (String) tradeInfo.get(TradeInformationTag.PIN_CAPTURE_CODE);
        if (TextUtils.isEmpty(pinCaptureCode)){
            XLogUtil.e(TAG, "^_^ 从业务数据中获取服务点PIN获取码/密码最大长度失败 ^_^ ");
            pinCaptureCode = getGeneralCaptureCode();
        }
        return pinCaptureCode;
    }

    /**
     * 生厂环境一般密码长度为6，所以此处直接返回"06"。
     * @return
     */
    private String getGeneralCaptureCode() {
        return "06";
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
