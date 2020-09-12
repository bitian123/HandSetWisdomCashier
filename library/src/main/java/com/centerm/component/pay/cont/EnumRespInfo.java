package com.centerm.component.pay.cont;

/**
 * author:wanliang527</br>
 * date:2017/3/8</br>
 */
public enum EnumRespInfo {

    OK("00", "成功"),
    PROCESS_UNDEFINED("E01","流程未定义"),
    PARAMS_NULL("E02","空参数"),
    TRANSCODE_ILLEGAL("E03","非法交易码"),
    AMOUNT_ILLEGAL("E04","非法金额"),
    OTHER_PARAMS_ILLEGAL("E05","非法参数"),
    MAPPING_ERROR("E06","参数映射失败"),
    TRANSCODE_NULL_ERROR("E07","交易码参数为空"),
    PAYMENT_CREATE_ERROR("E08","支付渠道创建失败"),
    PAYMENT_NO_SIGN_ERROR("E09","未签到"),
    PAYMENT_ERROR("E99",""),
    PAYMENT_OPERLOGIN("OPERLOGIN","请先进行操作员登录"),
    PAYMENT_CANCEL("E98","取消")
    ;

    private String code;
    private String msg;

    EnumRespInfo(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}
