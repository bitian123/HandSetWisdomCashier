package com.centerm.epos.redevelop;

import com.centerm.epos.common.TransCode;

import static com.centerm.epos.common.TransCode.CANCEL;
import static com.centerm.epos.common.TransCode.COMPLETE_VOID;
import static com.centerm.epos.common.TransCode.EC_VOID_CASH_LOAD;
import static com.centerm.epos.common.TransCode.ISS_INTEGRAL_VOID;
import static com.centerm.epos.common.TransCode.RESERVATION_SALE;
import static com.centerm.epos.common.TransCode.UNION_INTEGRAL_VOID;
import static com.centerm.epos.common.TransCode.VOID;
import static com.centerm.epos.common.TransCode.VOID_INSTALLMENT;
import static com.centerm.epos.common.TransCode.VOID_SCAN;

/**
 * Created by 94437 on 2017/7/27.
 */

public class BaseIsUpdateOriginInfo implements IIsUpdateOriginInfo{
    @Override
    public boolean getNeedUpdate(String transCode) {
        switch (transCode) {
            case VOID://消费撤销
            case VOID_SCAN://扫码撤销
            case CANCEL://预授权撤销
            case COMPLETE_VOID://预授权完成撤销
            case VOID_INSTALLMENT://分期消费撤销
            case EC_VOID_CASH_LOAD://电子现金充值撤销
            case ISS_INTEGRAL_VOID:
            case UNION_INTEGRAL_VOID:
            case TransCode.RESERVATION_VOID:
            case "SALE_SCAN_VOID":
            case "SALE_SCAN_VOID_QUERY":
            return true;
            default:
                return false;
        }
    }
}
