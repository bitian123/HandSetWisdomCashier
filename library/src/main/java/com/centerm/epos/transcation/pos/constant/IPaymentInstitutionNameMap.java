package com.centerm.epos.transcation.pos.constant;

/**
 * Created by yuhc on 2017/7/11.
 * 支付机构名称对应表
 */

public interface IPaymentInstitutionNameMap {

    /**
     * 通过标识码获取支付机构名称
     * @param institutionID
     * @return 支付机构名称
     */
    String getInstitutionName(String institutionID);

}
