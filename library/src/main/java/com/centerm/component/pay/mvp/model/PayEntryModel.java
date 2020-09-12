package com.centerm.component.pay.mvp.model;

import android.icu.math.BigDecimal;
import android.os.Bundle;
import android.text.TextUtils;

import com.centerm.component.pay.cont.Keys;
import com.centerm.component.pay.cont.EnumRespInfo;
import com.centerm.component.pay.cont.TransCode;

import org.apache.log4j.Logger;


/**
 * author:wanliang527</br>
 * date:2017/3/8</br>
 */
public class PayEntryModel implements IPayEntryModel {

    private Logger logger = Logger.getLogger(getClass());

    @Override
    public Bundle newBundle(String respCode, String respMsg) {
        Keys k = Keys.obj();
        Bundle bundle = new Bundle();
        bundle.putString(k.resp_code, respCode);
        bundle.putString(k.resp_msg, respMsg);
        return bundle;
    }

    @Override
    public boolean isResultOk(String s) {
        return EnumRespInfo.OK.getCode().equals(s);
    }

    @Override
    public String[] onCheckParams(Bundle param) {
        if (param == null) {
            logger.warn("【支付组件】参数校验不通过==>Bundle参数为空");
            return new String[]{EnumRespInfo.PARAMS_NULL.getCode(), EnumRespInfo.PARAMS_NULL.getMsg()};
        }
        Keys k = Keys.obj();
        //控制参数校验
        Bundle controlBundle = param.getBundle(k.control_bundle);
        if (controlBundle == null) {
            controlBundle = new Bundle();
            param.putBundle(k.control_bundle, controlBundle);
        }
        if (!controlBundle.containsKey(k.input_money_view)) {
            controlBundle.putBoolean(k.input_money_view, true);
        }
        if (!controlBundle.containsKey(k.result_view)) {
            controlBundle.putBoolean(k.result_view, true);
        }
        if (!controlBundle.containsKey(k.signature_view)) {
            controlBundle.putBoolean(k.signature_view, false);
        }
        byte printPages = param.getByte(k.print_pages, (byte) -1);
        if (printPages < 0) {
            //签购单打印联数控制，仅对二维码业务生效，取值范围0,1,2,3，默认为0
            printPages = 0;
            param.putByte(k.print_pages, printPages);
        }

        //交易参数校验
        String transCode = param.getString(k.trans_code);
        double transAmt = param.getDouble(k.trans_amt);
        String callerId = param.getString(k.caller_id);
        String callerSecret = param.getString(k.caller_secret);
        TransCode codeObj = TransCode.obj();
        if (TextUtils.isEmpty(transCode) || !codeObj.exist(transCode)) {
            logger.warn("【支付组件】参数校验不通过==>交易码非法");
            return new String[]{EnumRespInfo.TRANSCODE_ILLEGAL.getCode(), EnumRespInfo.TRANSCODE_ILLEGAL.getMsg()};
        }
        if (!controlBundle.getBoolean(k.input_money_view) && !transCode.equals(codeObj.BALANCE) && !checkAmt
                (transAmt)) {
            logger.warn("【支付组件】参数校验不通过==>金额非法");
            return new String[]{EnumRespInfo.AMOUNT_ILLEGAL.getCode(), EnumRespInfo.AMOUNT_ILLEGAL.getMsg()};
        }
        if (TextUtils.isEmpty(callerId) || TextUtils.isEmpty(callerSecret)) {
            logger.warn("【支付组件】参数校验不通过==>调用者ID或调用者掩码非法");
            return new String[]{EnumRespInfo.OTHER_PARAMS_ILLEGAL.getCode(), EnumRespInfo.OTHER_PARAMS_ILLEGAL.getMsg()};
        }
        return new String[]{EnumRespInfo.OK.getCode(), EnumRespInfo.OK.getMsg()};
    }

    private boolean checkAmt(double amt) {
        if (amt <= 0 || amt > 99999999.99) {
            logger.warn("11111");
            return false;
        }
        String string = new java.math.BigDecimal(String.valueOf(amt)).toString();
        String[] ss = string.split("\\.");
        if (ss.length == 2 && ss[1].length() > 2) {
            return false;
        }
        return true;
    }


    @Override
    public String transCodeMapping(String transCode) {
        TransCode compCodeSets = TransCode.obj();
        if (compCodeSets.SALE.equals(transCode)) {
            return com.centerm.epos.common.TransCode.SALE;
        } else if (compCodeSets.SALE_SCAN.equals(transCode)) {
            return com.centerm.epos.common.TransCode.SALE_SCAN;
        } else if (compCodeSets.VOID.equals(transCode)) {
            return com.centerm.epos.common.TransCode.VOID;
        } else if (compCodeSets.REFUND.equals(transCode)) {
            return com.centerm.epos.common.TransCode.REFUND;
        } else if (compCodeSets.AUTH.equals(transCode)) {
            return com.centerm.epos.common.TransCode.AUTH;
        } else if (compCodeSets.CANCEL.equals(transCode)) {
            return com.centerm.epos.common.TransCode.CANCEL;
        } else if (compCodeSets.AUTH_COMPLETE.equals(transCode)) {
            return com.centerm.epos.common.TransCode.AUTH_COMPLETE;
        } else if (compCodeSets.BALANCE.equals(transCode)) {
            return com.centerm.epos.common.TransCode.BALANCE;
        }else if (compCodeSets.AUTH_COMPLETE_CANCEL.equals(transCode)){
            return com.centerm.epos.common.TransCode.COMPLETE_VOID;
        }else if (compCodeSets.SIGN_IN.equals(transCode)){
            return com.centerm.epos.common.TransCode.SIGN_IN;
        }else if (compCodeSets.SETTLE.equals(transCode)){
            return com.centerm.epos.common.TransCode.SETTLEMENT;
        }
        return null;
    }

    @Override
    public String processFileMapping(String transCode) {
        TransCode compCodeSets = TransCode.obj();
        if (compCodeSets.SALE.equals(transCode)) {
            return "sale.xml";
        } else if (compCodeSets.SALE_SCAN.equals(transCode)) {
            return "sale_scan.xml";
        }else if (compCodeSets.VOID.equals(transCode)) {
            return "void.xml";
        } else if (compCodeSets.REFUND.equals(transCode)) {
            return "refund.xml";
        } else if (compCodeSets.AUTH.equals(transCode)) {
            return "auth.xml";
        } else if (compCodeSets.CANCEL.equals(transCode)) {
            return "cancel.xml";
        } else if (compCodeSets.AUTH_COMPLETE.equals(transCode)) {
            return "auth_complete.xml";
        } else if (compCodeSets.BALANCE.equals(transCode)) {
            return "balance.xml";
        } else if (compCodeSets.AUTH_COMPLETE_CANCEL.equals(transCode)) {
            return "auth_complete_void.xml";
        }else if (compCodeSets.SIGN_IN.equals(transCode) || compCodeSets.SETTLE.equals(transCode)){
            return "online.xml";
        }
        return null;
    }

    @Override
    public int transCodeMappingJBoss(String transCode) {
        TransCode compCodeSets = TransCode.obj();
        if (compCodeSets.WX_PAY_SCAN.equals(transCode)) {
            return 11;
        }else if (compCodeSets.ALI_PAY_SCAN.equals(transCode)){
            return 21;
        }
        return -1;
    }

}
