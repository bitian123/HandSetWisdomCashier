package com.centerm.epos.transcation.pos.data;

import android.text.TextUtils;

import com.centerm.epos.EposApplication;
import com.centerm.epos.utils.XLogUtil;

import org.apache.log4j.Logger;

import java.util.Map;

import config.BusinessConfig;

/**
 * Created by liubit on 2018/1/3.<br>
 * 终端信息
 */

public class BaseField56 implements I8583Field {
    private Logger logger = Logger.getLogger(this.getClass());
    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField56.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String,Object> tradeInfo;

    public BaseField56(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    @Override
    public String encode() {
        if (tradeInfo == null){
            XLogUtil.e(TAG, "^_^ encode 输入参数 tradeInfo 为空 ^_^");
            return null;
        }

        return "";
    }

    /**
     * 占位，平台不返回此域
     *
     * @param fieldMsg null
     * @return null
     */
    @Override
    public Map<String, Object> decode(String fieldMsg) {
        if(!TextUtils.isEmpty(fieldMsg)){
            BusinessConfig.getInstance().setValue(EposApplication.getAppContext(), BusinessConfig.Key.PROJECT_ID, fieldMsg);
        }
        XLogUtil.d(TAG, "^_^ decode result:" + fieldMsg + " ^_^");
        return null;
    }

}
