package com.centerm.epos.transcation.pos.constant;

import com.centerm.epos.common.ConstDefine;

/**
 * Created by yuhc on 2017/2/8.<br>
 * 业务数据标签，标识交易过程中获取到的数据；或域数据解析为业务数据后的标签。
 */

public class TradeInformationTag {

    /**
     * 交易类型代码：0200/0210
     */
    public static final String TRANSACTION_CODE = "Transaction Code";

    /**
     * 交易类型：消费、撤销等
     */
    public static final String TRANSACTION_TYPE = "Transaction Type Identifier";

    /**
     * 银行卡号，最大19 byte 的字符串
     */
    public static final String BANK_CARD_NUM = "Bank Card Number";

    /**
     * 交易处理码
     */
    public static final String TRANSACTION_PROCESS_CODE = "Transaction Processing Code";

    /**
     * 交易金额
     */
    public static final String TRANS_MONEY = "Amount Of Transactions";


    /**
     * 受卡方系统跟踪号/终端交易流水号
     */
    public static final String TRACE_NUMBER = "Terminal Trace Number";

    /**
     * 合同信息
     */
    public static final String CONTRACT_INFO = "Contract Info";

    /**
     * 受卡方所在地时间/平台交易时间
     */
    public static final String TRANS_TIME = "Time Of Transaction";

    /**
     * 受卡方所在地日期/平台交易日期
     */
    public static final String TRANS_DATE = "Date Of Transaction";

    /**
     * 受卡方成所在地年份/终端本地获取
     */
    public static final String TRANS_YEAR = "Year Of Transaction";

    /**
     * 卡有效期
     */
    public static final String DATE_EXPIRED = "Date Of Expired";

    /**
     * 清算日期
     */
    public static final String DATE_SETTLEMENT = "Date Of Settlement";

    /**
     * 服务点输入方式码
     */
    public static final String SERVICE_ENTRY_MODE = "Point Of Service Entry Mode";

    /**
     * 卡序列号
     */
    public static final String CARD_SEQUENCE_NUMBER = "Card Sequence Number";

    /**
     * 服务点条件码
     */
    public static final String SERVICE_CONDITION_MODE = "Service Condition Mode";

    /**
     * 服务点PIN获取码
     */
    public static final String PIN_CAPTURE_CODE = "Service PIN Capture Code";

    /**
     * 受理机构标识码
     */
    public static final String INSTITUTION_ID_CODE = "Institution Identification Code";

    /**
     * 2磁道数据
     */
    public static final String TRACK_2_DATA = "Track 2 Data";

    /**
     * 3磁道数据
     */
    public static final String TRACK_3_DATA = "Track 3 Data";

    /**
     * 检索参考号
     */
    public static final String REFERENCE_NUMBER = "Retrieval Reference Number";

    /**
     * 备注信息，用于打印凭条
     */
    public static final String REFERENCE_INFORMATION = "Retrieval Reference Information";

    /**
     * 授权标识应答码
     */
    public static final String AUTHORIZATION_IDENTIFICATION = "Authorization Identification";

    /**
     * 应答码
     */
    public static final String RESPONSE_CODE = "Response Code";

    /**
     * 冲正原因码
     */
    public static final String REVERSE_CODE = "Reverse Code";

    /**
     * 受卡机终端标识码/终端号
     */
    public static final String TERMINAL_IDENTIFICATION = "Terminal Identification";

    /**
     * 受卡方标识码/商户号
     */
    public static final String MERCHANT_IDENTIFICATION = "Merchant Identification";

    /**
     * 商户名称
     */
    public static final String MERCHANT_NAME = "Merchant Name";

    /**
     * 发卡行标识码
     */
    public static final String ISSUER_IDENTIFICATION = "Issuer Identification";

    /**
     * 收单行标识码
     */
    public static final String ACQUIRER_IDENTIFICATION = "Acquirer Identification";

    /**
     * 交易货币代码
     */
    public static final String CURRENCY_CODE = "Currency Code Of Transaction";

    /**
     * 持卡人密码
     */
    public static final String CUSTOMER_PASSWORD = "PIN Data";

    /**
     * 安全控制信息
     */
    public static final String SECURITY_CONTROL = "Security Control Information";

    /**
     * 余额
     */
    public static final String BALANC_AMOUNT = "Balanc Amount";

    /**
     * IC卡数据域
     */
    public static final String IC_DATA = "IC Related Data";

    /**
     * IC卡数据域
     */
    public static final String IC_DATA_REVERSE = "IC Related Data For Reverse";

    /**
     * PBOC电子钱包标准的交易信息
     */
    public static final String PBOC_ELECTRONIC_DATA = "Pboc Electronic Data";

    /**
     * 网络管理信息码
     */
    public static final String NET_MANAGE_CODE = "Net Manage Code";

    /**
     * 批次号
     */
    public static final String BATCH_NUMBER = "Batch Number";

    /**
     * 原交易授权信息
     */
    public static final String ORIGINAL_MESSAGE = "Original Message";

    /**
     * 操作员代码
     */
    public static final String OPERATOR_CODE = "Operator Code";

    /**
     * 国际信用卡公司代码
     */
    public static final String CREDIT_CODE = "International Credit Code";

    /**
     * 报文MAC
     */
    public static final String MAC_MESSAGE = "Message MAC";

    /**
     * 安全密钥数据
     */
    public static final String SECURITY_KEY = "Security Key";

    /**
     * 工作密钥
     */
    public static final String WORK_KEY = "Work Key";

    /**
     * AID参数
     */
    public static final String TERMINAL_PARAMETER = "Terminal Parameter";

    /**
     * AID参数
     */
    public static final String IC_PARAMETER_AID = "IC Parameter AID";

    /**
     * CAPK参数
     */
    public static final String IC_PARAMETER_CAPK = "IC Parameter CAPK";

    /**
     * BIN参数
     */
    public static final String IC_PARAMETER_CAPD_BIN = "IC Parameter Card BIN";

    /**
     * QPS参数
     */
    public static final String IC_PARAMETER_QPS = "IC Parameter QPS";

    /**
     * 终端状态信息
     */
    public static final String IC_PARAMETER_INDEX = "IC Parameter Index";


    /**
     * 打印凭条版本
     */
    public static final String SLIP_VERSION = "Slip Version";

    /**
     * 60域自定义信息
     */
    public static final String CUSTOM_INFO_60 = "Custom Info 60";

    /**
     * 参数下载类型标识
     */
    public static final String PARAMS_TYPE = "Params Type";

    /**
     * 收单行标识
     */
    public static final String ACQ_INSTITUTE = "Acquire Institute";

    /**
     * 发卡行标识
     */
    public static final String ISS_INSTITUTE = "Issue Institute";

    /**
     * 卡组织代码
     * {@link #CREDIT_CODE} 临时数据不保存数据库<br/>
     * {@link #CREDIT_CARD_COMPANY_CODE} 临时数据不保存数据库<br/>
     * BANKCARD_ORGANIZATION 用来保存卡组织代码数据，数据将被保存到数据库,结算时将用来统计内外卡 <br/>
     */
    public static final String BANKCARD_ORGANIZATION = "Bankcard oganization";

    /**
     * 转入卡卡号
     */
    public static final String TRANSFER_INTO_CARD = "Transfer Into Card";

    /**
     * pboc打印数据
     */
    public static final String TRANS_IC_INFO = "Trans IC Information";

    /**
     * 电子现金业务的TAC值
     */
    public static final String EC_TRANS_TAC = "Trans EC TAC";

    /**
     * 电子现金余额
     */
    public static final String EC_TRANS_BALANCE = "Trans EC Balance";

    /**
     * 对账结果
     */
    public static final String SETTLEMENT_RESULT = "Settlement Result";

    /**
     * 二维码或条形码内容
     */
    public static final String SCAN_CODE = "Scan Code";
    /**
     * 付款凭证号
     */
    public static final String SCAN_VOUCHER_NO = "Scan Voucher NO";

    /**
     * 银行卡类型，true 借记  false 贷记，准贷记
     */
    public static final String BANK_CARD_TYPE = "Bank Card Type";

    /**
     * 报文头数据
     */
    public static final String MSG_HEADER = "Message Header Data";

    public static final String PRIMARYVOUCHERNO = "primaryVoucherNo";   //消费撤销，预授权完成撤销的原交易凭证号

    public static final String PRIMARYREFERENCENO = "primaryReferenceNo";   //退货的原交易参考号

    /**
     * 63域保留信息，包括发卡方保留域，中国银联保留域，受理机构保留域，POS终端保留域
     */
    public static final String REVERSE_FIELD = "Reverse Field";

    public static final String UNICOM_SCAN_TYPE = "unicom scan type";    //

    /**
     * 监管标志
     */
    public static final String SUPERVISE_FLAG = "superviseFlag";

    /**
     * 监管地区
     */
    public static final String AREA_CODE = "areaCode";

    /**
     * 国密密码
     */
    public static final String SM4_PASSWORD = "SM4 passwrod";

    /**
     * 电子签名上送业务，用户输入的电话号码
     */
    public static final String PHONE_NUMBER = "Mobile Phone Number";

    /**
     * 电子签名上送业务，55域中的内容：签购单要素
     */
    public static final String E_SLIP_KEY_DATA = "Electronic Slip Key Data";

    /**
     * 电子签名上送业务，62域的电子签名数据
     */
    public static final String E_SIGNATURE_DATA = "Electronic Signature Data";

    /**
     * 部分电子签名上送业务，60.4 表示是否是最后一个包
     */
    public static final String E_SIGNATURE_UPLOAD_END_FLAG = "Electronic Signature Upload End Flag";

    /**
     * 电子签名文件保存结果
     */
    public static final String STORE_E_SIGN_RESULT = "Store Electronic Signature Result";

    /**
     * 分期付款的分期数
     */
    public static final String INSTALLMENT_PERIOD = "Installment Period";

    /**
     * 分期项目编码
     */
    public static final String INSTALLMENT_CODE = "Installment Code";

    /**
     * 分期支付方式：一次性，分期支付
     */
    public static final String INSTALLMENT_PAY_MODE = "Installment Pay Mode";

    /**
     * 平台返回的分期付款
     */
    public static final String INSTALLMENT_INFORMATION = "Installment Informaiton";

    /**
     * 用于打印的分期付款信息
     */
    public static final String INSTALLMENT_INFORMATION_FOR_PRINT = "Installment Informaiton For Print";

    /**
     * 是否手输卡号
     */
    public static final String IS_CARD_NUM_MANUAL = "Is Card Num Input By Manual";

    /**
     * 结算账户信息
     */
    public static final String SETTLEMENT_INFO = "Settlement Info";

    /**
     * 打印模板id
     */
    public static final String TEMPLATE_ID = "templateId";

    public static final String TRANS_STATUS_VALUE = "TRANS_STATUS_VALUE";//用来指示交易状态 目前只有IC卡脱机交易拒绝用到AAC


    /**
     * "1"-离线交易上送成功，"2"-离线交易上送失败,"4"-后台无应答
     * {@link com.centerm.epos.db.CommonManager#getOfflineTransList(int)} 0x1000-离线交易上送成功，0x2000-离线交易上送失败,0x4000-后台无应答 <br/>
     * {@link com.centerm.epos.bean.TradeInfoRecord#offlineTransUploadStatus} <br/>
     */
    public static final String OFFLINE_TRANS_UPLOAD_STATUS = "OFFLINE_TRANS_UPLOAD_STATUS";

    /**
     * 积分商品项目编码
     */
    //public static final String INTEGRAL_GOODS_CODE = "INTEGRAL GOODS CODE"; //62域用法十三
    //public static final String INTEGRAL_EXCHANGE = "INTEGRAL EXCHANGE"; //62域用法十四

    public static final String ISO62_REQ = "ISO62 REQ"; //62域请求包
    public static final String ISO62_RES = "ISO62 RES"; //62域应答包

    /**
     * {@link com.centerm.epos.bean.TradeInfoRecord#stateFlag} <br/>
     * {@link com.centerm.epos.bean.TradeInfoRecord#setStateFlag(int)} <br/>
     * {@link ConstDefine#TRANS_STATE_VOID}<br/>
     * {@link ConstDefine#TRANS_STATE_ADJUST} <br/>
     */
    public static final String TRANS_STATE_FLAG = "TRANS STATE FLAG"; //交易状态：0x01-已撤销  0x02 离线调整


    public static final String ORIGINAL_AUTH_MODE = "ORIGINAL AUTH MODE"; //原机构授权方式
    public static final String ORIGINAL_AUTH_ORG_CODE = "ORIGINAL AUTH ORG CODE"; //原机构授权机构代码

    public static final String CREDIT_CARD_COMPANY_CODE = TradeInformationTag.CREDIT_CODE; //国际信用卡公司 代码 cup jcb mcc vis mae dcc amx
    public static final String PROPERTY_MSG = "PROPERTY_MSG"; //物业扫码下单 订单信息

    public static final String totalReceivable = "totalReceivable";
    public static final String totalReceived = "totalReceived";
    public static final String totalUnpaidAmount = "totalUnpaidAmount";

}
