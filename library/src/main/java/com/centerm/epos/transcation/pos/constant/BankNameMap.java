package com.centerm.epos.transcation.pos.constant;

import android.text.TextUtils;

import com.centerm.epos.EposApplication;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.utils.XLogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuhc on 2017/4/26.
 */

public class BankNameMap {
    private static final String TAG = BankNameMap.class.getSimpleName();

    private static Map<String,String> mBankNameMap = new HashMap<>();

    static {
        mBankNameMap.put("0102", "工商银行");
        mBankNameMap.put("0103", "农业银行");
        mBankNameMap.put("0104", "中国银行");
        mBankNameMap.put("0105", "建设银行");
        mBankNameMap.put("0100", "邮储银行");
        mBankNameMap.put("0301", "交通银行");
        mBankNameMap.put("0302", "中信银行");
        mBankNameMap.put("0303", "光大银行");
        mBankNameMap.put("0304", "华夏银行");
        mBankNameMap.put("0305", "民生银行");
        mBankNameMap.put("0306", "广发银行");
        mBankNameMap.put("0307", "深发银行");
        mBankNameMap.put("0308", "招商银行");
        mBankNameMap.put("0309", "兴业银行");
        mBankNameMap.put("0310", "浦发银行");
        mBankNameMap.put("0403", "平安银行");
        mBankNameMap.put("0311", "北京银行");
        mBankNameMap.put("0401", "上海银行");
    }

    public static String getBankName(String bankID){
        if (TextUtils.isEmpty(bankID)){
            XLogUtil.e(TAG, "传入的银行代码为空！");
            return "";
        }
        if (mBankNameMap.containsKey(bankID))
            return mBankNameMap.get(bankID);
        Object object = ConfigureManager.getInstance(EposApplication.getAppContext()).getSubPrjClassInstance(BankNameMap
                .class);
        if (object == null || !(object instanceof IPaymentInstitutionNameMap))
            return "";
        return ((IPaymentInstitutionNameMap)object).getInstitutionName(bankID);
    }
}
