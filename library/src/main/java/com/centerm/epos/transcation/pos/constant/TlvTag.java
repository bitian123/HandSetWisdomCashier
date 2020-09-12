package com.centerm.epos.transcation.pos.constant;

/**
 * Created by yuhc on 2017/6/23.
 * 银联TLV数据标签定义
 */

public class TlvTag {

    //SM 算法加密 PIN 数据
    public static final String PWD_SM = "A1";

    //终端硬件序列号及密文数据
    public static final String UNIONPAY_SN = "A2";

    //扫码付信息域
    public static final String QRCODE_PAY_INFO = "A3";

    //扫码付付款凭证码
    public static final String QRCODE_PAY_VOUCHER = "A4";

    /**
     * 终端硬件序列号及密文数据的二级TLV数据标签定义
     */
    //设备类型
    public static final String DEVICE_TYPE = "01";

    //终端硬件序列号
    public static final String TERMINAL_SN = "02";

    //加密随机因子
    public static final String RANDOM_NUM = "03";

    //硬件序列号密文数据
    public static final String TERMINAL_SN_MAC = "04";

    //应用程序版本号
    public static final String APP_VERSION = "05";
}
