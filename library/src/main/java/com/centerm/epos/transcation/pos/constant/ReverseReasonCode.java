package com.centerm.epos.transcation.pos.constant;

/**
 * Created by yuhc on 2017/9/14.
 * 冲正原因码
 */

public interface ReverseReasonCode {

    /**
     * POS终端在时限内未能收到POS中心的应答
     */
    String TIME_OUT = "98";


    /**
     * POS终端收到POS中心的批准应答消息，但由于POS机故障无法完成交易
     */
    String TERMINAL_ERROR = "96";


    /**
     * POS终端对收到POS中心的批准应答消息，验证MAC出错
     */
    String MAC_CHECK_FAILED = "A0";

    /**
     * 其他情况引发的冲正
     */
    String OTHER = "06";
}
