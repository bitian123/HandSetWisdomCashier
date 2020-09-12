package com.centerm.epos.transcation.pos.constant;

/**
 * Created by yuhc on 2017/9/4.
 * 电子签名的55域TLV标签定义
 */

public interface ESignatureTlvTag {

    /**
     * 一、交易通用信息（以下域均指原交易应答报文）
     */
    //商户名称
    String MERCHANT_NAME = "FF00";

    //交易类型
    String TRANSACTION_TYPE = "FF01";

    //操作员号
    String OPERATOR_ID = "FF02";

    //收单机构
    String RECEIPT_AGENCY = "FF03";

    //发卡机构
    String ISSUE_INSTITUTIONS = "FF04";

    //有效期
    String VALIDE_DATE = "FF05";

    //日期时间
    String DATE_TIME = "FF06";

    //授权码
    String AUTH_CODE = "FF07";

    //小费金额
    String TIP_AMOUNT = "FF08";

    //卡组织
    String CARD_ORGANIZATION = "FF09";

    //交易币种
    String CURRENCY_CODE = "FF0A";

    //持卡人手机号码
    String MOBILE_PHONE_NUMBER = "FF0B";


    /**
     * 二、IC卡有关信息（以下域均指原交易请求报文）
     */
    //应用标签
    String APP_LABLE = "FF30";

    //应用名称
    String APP_NAME = "FF31";

    //应用标识
    String APP_ID = "FF22";

    //应用密文 AC
    String APP_ENCRYPT_DATA = "FF23";

    //充值后卡片余额
    String ECASH_BALANCE = "FF24";

    //转入卡卡号
    String RECEIPT_CARD_NUMBER = "FF25";

    //不可预知数
    String RANDOM = "FF26";

    //应用交互特征
    String AIP = "FF27";

    //终端验证结果
    String TVR = "FF28";

    //交易状态信息
    String TSI = "FF29";

    //应用交易计数器
    String ATC = "FF2A";

    //发卡应用数据
    String IAD = "FF2B";

    /**
     * 三、创新业务信息（以下域均指原交易请求报文）
     */
    //备注信息
    String REFERENCE_INFO = "FF40";

    //分期付款期数
    String INSTALLMENT_PERIOD = "FF41";

    //分期付款首期金额
    String FIRST_AMOUNT = "FF42";

    //分期付款还款币种
    String INSTALLMENT_CURRENCY_CODE = "FF43";

    //持卡人手续费
    String FEE = "FF44";

    //商品代码
    String COMMODITY_CODE = "FF45";

    //兑换积分数
    String EXCHANGE_POINTS = "FF46";

    //积分余额
    String POINTS_BALANCE = "FF57";

    //自付金额
    String PAY_AMOUNT = "FF48";

    //承兑金额
    String ACCEPTANCE_AMOUNT = "FF49";

    //可用余额
    String AVAILABLE_BALANCE = "FF4A";

    //手机号码
    String MASK_PHONE_NUMBER = "FF4B";

    /**
     * 四、原交易信息（以下域均指原交易请求报文）
     */
    //原凭证号
    String ORIGINAL_VOUCHER_NUMBER = "FF60";

    //原批次号
    String ORIGINAL_BATCH_NUMBER = "FF61";

    //原参考号
    String ORIGINAL_REFERENCE_NUMBER = "FF62";

    //原交易日期
    String ORIGINAL_DATE = "FF63";

    //原授权码
    String ORIGINAL_AUTH_CODE = "FF64";

    //原终端号
    String ORIGINAL_TERMINAL_ID = "FF65";


    /**
     * 五、终端统计信息
     */
    //当前交易打印张数
    String SLIP_COUNT = "FF70";

    /**
     * 六、保留信息（以下域均指原交易应答报文）
     */
    //发卡方保留域
    String ISSUE_NOTICE = "FF71";

    //中国银联保留域
    String UNIONPAY_NOTICE = "FF72";

    //受理机构保留域
    String RECEPTION_NOTICE = "FF73";
}
