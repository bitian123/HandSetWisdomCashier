package com.centerm.epos.common;

import com.centerm.epos.R;

/**
 * author:wanliang527</br>
 * date:2016/10/27</br>
 */
/**
 * 增加新的冲正响应码 onTradeFailed方法需要相应的进行修改
 * {@link com.centerm.epos.present.transaction.TradingPresent#onTradeFailed(String, String, String)}<br/>
 * author zhouzhihua 冲正修改
 * */
public enum StatusCode {
    SUCCESS("666", R.string.success),
    UNKNOWN_HOST("N001", R.string.error_unknown_host),
    SOCKET_TIMEOUT("N002", R.string.error_socket_timeout),
    CONNECTION_EXCEPTION("N002", R.string.error_connection_exception),
    KEY_VERIFY_FAILED("S001", R.string.error_key_verify_failed),
    INVALID_TERMINAL_SN("E001", R.string.tip_invalid_sn),
    MAC_INVALID("E002", R.string.tip_mac_verify_failed),
    PIN_TIMEOUT("E003", R.string.tip_pin_timeout),
    UNKNOWN_REASON("E900", R.string.error_unknown_reason),
    TRADING_TERMINATES("K001", R.string.error_trading_terminate),
    TRADING_REFUSED("K002", R.string.error_trading_refused),
    TRADING_FALLBACK("K003", R.string.error_trading_fallback),
    TRADING_CHANGE_OTHER_FACE("K004", R.string.error_change_other_face),
    EMV_KERNEL_EXCEPTION("K003", R.string.error_kernel_exception),
    RESIGIN_IN("C001", R.string.tip_sign_in_again),
    DOWNLOAD_TMK("C002", R.string.tip_download_tmk),
    PACKAGE_ERROR("M001", R.string.tip_pakcage_msg_error),
    UNPACKAGE_ERROR("M002", R.string.tip_un_pakcage_msg_error),
    DATA_EXCHANGE_ERROR("Y001", R.string.tip_data_exchange_error),
    IC_PROCESS_ERROR("I001", R.string.tip_ic_data_error),
    AMOUNT_GET_ERROR("E009", R.string.tip_amount_get_error),
    USER_CANCEL("U001", R.string.tip_user_cancel),
    PRUE_PLZ_SELECT_EC_TRANS("E00A",R.string.prue_plz_select_rc_trans),
    AUTH_AMOUNT_NOT_PRUE_EC("E00B",R.string.tip_auth_amount_prue_ec);

    private String statusCode;
    private int msgId;

    StatusCode(String statusCode, int msgId) {
        this.statusCode = statusCode;
        this.msgId = msgId;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }
}
