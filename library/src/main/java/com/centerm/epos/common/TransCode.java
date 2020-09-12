package com.centerm.epos.common;

import android.text.TextUtils;

import com.centerm.epos.EposApplication;
import com.centerm.epos.R;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * author:wanliang527</br>
 * date:2016/10/30</br>
 */

/**
 * 需要更新原交易信息需要以下方法，撤销类交易需要注意
 * {@link com.centerm.epos.redevelop.BaseIsUpdateOriginInfo#getNeedUpdate(String)}
 */

public class TransCode {

    private static Logger logger = Logger.getLogger(TransCode.class);

    public final static String TERMINAL_INIT = "TERMINAL_INIT"; //终端初始化
    public final static String OBTAIN_TMK = "OBTAIN_TMK";//获取主密钥
    public final static String SIGN_IN = "POS_SIGN_IN";//签到
    public final static String SIGN_OUT = "POS_SIGN_OUT";//签退
    public final static String ESIGN_UPLOAD = "ESIGN_UPLOAD";//电子签名上送
    public final static String ESIGN_UPLOAD_PART = "ESIGN_UPLOAD_PART";//部分电子签名上送
    public final static String DOWNLOAD_CAPK = "DOWNLOAD_CAPK";//公钥下载
    public final static String DOWNLOAD_AID = "DOWNLOAD_AID";//IC卡参数下载
    public final static String DOWNLOAD_TERMINAL_PARAMETER = "DOWNLOAD_TERMINAL_PARAMETER";//终端参数下载
    public final static String DOWNLOAD_QPS_PARAMS = "DOWNLOAD_QPS_PARAMS";//非接参数下载
    public final static String DOWNLOAD_CARD_BIN = "DOWNLOAD_CARD_BIN";//卡BIN参数下载
    public final static String DOWNLOAD_CARD_BIN_QPS = "DOWNLOAD_CARD_BIN_QPS";//卡BIN参数下载,免密新增卡
    public final static String DOWNLOAD_BLACK_CARD_BIN_QPS = "DOWNLOAD_BLACK_CARD_BIN_QPS";//卡BIN黑名单下载,免密新增卡
    public final static String POS_STATUS_UPLOAD = "POS_STATUS_UPLOAD";//POS终端状态上送
    public final static String DOWNLOAD_PARAMS = "DOWNLOAD_PARAMS";//IC卡公钥/参数/TMS参数/卡BIN黑名单下载
    public final static String DOWNLOAD_PARAMS_FINISHED = "DOWNLOAD_PARAMS_FINISHED";//IC卡公钥/参数/TMS参数/卡BIN黑名单下载结束
    public final static String UPLOAD_SCRIPT_RESULT = "UPLOAD_SCRIPT_RESULT";//IC卡脚本结果上送
    public final static String BALANCE = "BALANCE";//余额查询
    public final static String SALE = "SALE";//消费
    public final static String SALE_SCAN = "SALE_SCAN";//扫码消费
    public final static String SALE_INSERT = "SALE_INSERT";//插卡消费
    public final static String SALE_INSTALLMENT = "SALE_INSTALLMENT";//分期消费
    public final static String SALE_NEED_PIN = "SALE_NEED_PIN";//闪付凭密
    public final static String VOID = "VOID";//消费撤销
    public static final String VOID_INSTALLMENT = "VOID_INSTALLMENT";//分期消费撤销
    public static final String VOID_SCAN = "VOID_SCAN";//扫码撤销
    public final static String REFUND = "REFUND";//退货
    public final static String REFUND_SCAN = "REFUND_SCAN";//扫码退货
    public final static String AUTH = "AUTH";//预授权
    public final static String AUTH_NEED_PIN = "AUTH_NEED_PIN";//预授权凭密
    public final static String CANCEL = "CANCEL";//预授权撤销
    public final static String AUTH_COMPLETE = "AUTH_COMPLETE";//预授权完成请求
    public final static String AUTH_SETTLEMENT = "AUTH_SETTLEMENT";//预授权完成通知
    public final static String COMPLETE_VOID = "COMPLETE_VOID";//预授权完成撤销
    public final static String ECHO_TEST = "ECHO_TEST";//回响测试

    //电子现金类
    public final static String E_QUICK = "E_QUICK"; //快速消费
    public final static String E_COMMON = "E_COMMON"; //普通消费
    public final static String E_LOAD = "E_LOAD"; //圈存
    public final static String E_UNLOAD = "E_UNLOAD"; //圈提
    public final static String E_UNLOAD_CONFIRM = "E_UNLOAD_CONFIRM"; //圈提确认
    public final static String E_BALANCE = "E_BALANCE"; //余额查询
    public final static String E_UPLOAD = "E_UPLOAD"; //脱机上送
    public final static String E_REFUND = "E_REFUND"; //脱机退货
    public final static String EC_LOAD_RECORDS = "EC_LOAD_RECORDS"; //电子现金圈存日志
    public final static String EC_TRANS_RECORDS = "EC_TRANS_RECORDS"; //电子现交易明细



    public final static String TRANS_VOID_ENDWITH = "_VOID"; //撤销类交易 均以_VOID 结尾 zhouzhihua modify
    //圈存
    public final static String EC_LOAD_CASH = "EC_LOAD_CASH"; //现金圈存
    public final static String EC_LOAD_INNER = "EC_LOAD_INNER"; //指定账户圈存
    public final static String EC_LOAD_OUTER = "EC_LOAD_OUTER"; //非指定账户圈存
    public final static String EC_VOID_CASH_LOAD = "EC_VOID_CASH_LOAD"; //现金充值撤销
    public final static String EC_VOID_CASH_LOAD_REVERSE = "EC_VOID_CASH_LOAD_REVERSE"; //现金充值撤销冲正

    /*积分类交易*/
//    public final static String INTEGRAL_SALE_REVERSE = "INTEGRAL_SALE_REVERSE";
//    public final static String INTEGRAL_VOID_REVERSE = "INTEGRAL_VOID_REVERSE";
    public final static String INTEGRAL_SALE = "INTEGRAL_SALE";
    public final static String INTEGRAL_VOID = "INTEGRAL_VOID";
    public final static String ISS_INTEGRAL_SALE = "ISS_INTEGRAL_SALE";
    public final static String UNION_INTEGRAL_SALE = "UNION_INTEGRAL_SALE";

    public final static String ISS_INTEGRAL_SALE_REVERSE = "ISS_INTEGRAL_SALE_REVERSE";
    public final static String UNION_INTEGRAL_SALE_REVERSE = "UNION_INTEGRAL_SALE_REVERSE";

    public final static String ISS_INTEGRAL_VOID = "ISS_INTEGRAL_VOID";
    public final static String UNION_INTEGRAL_VOID = "UNION_INTEGRAL_VOID";

    public final static String ISS_INTEGRAL_VOID_REVERSE = "ISS_INTEGRAL_VOID_REVERSE";
    public final static String UNION_INTEGRAL_VOID_REVERSE = "UNION_INTEGRAL_VOID_REVERSE";

    public final static String UNION_INTEGRAL_BALANCE = "UNION_INTEGRAL_BALANCE"; //联盟积分查询
    public final static String UNION_INTEGRAL_REFUND = "UNION_INTEGRAL_REFUND"; //联盟积分退货

    /*
    * 磁条卡充值
    * */
    public final static String MAG_ACCOUNT_VERIFY = "MAG_ACCOUNT_VERIFY"; //磁条卡充值验证
    public final static String MAG_CASH_LOAD = "MAG_CASH_LOAD"; //磁条卡现金充值
    public final static String MAG_CASH_LOAD_CONFIRM = "MAG_CASH_LOAD_CONFIRM"; //磁条卡现金充值确认
    public final static String MAG_ACCOUNT_LOAD = "MAG_ACCOUNT_LOAD"; // 磁条卡账户充值 该交易必须输入转出卡密码

    public final static String MAG_ACCOUNT_LOAD_VERIFY = "MAG_ACCOUNT_LOAD_VERIFY"; //等同于 MAG_ACCOUNT_VERIFY 报文一致为了区分交易的流程

    /**
     * 预约类交易 RESERVATION 预约消费不使用卡，卡号由后台返回
     */
    public final static String RESERVATION_SALE = "RESERVATION_SALE";
    public final static String RESERVATION_VOID = "RESERVATION_VOID";

    public final static String RESERVATION_SALE_REVERSE = "RESERVATION_SALE_REVERSE";
    public final static String RESERVATION_VOID_REVERSE = "RESERVATION_VOID_REVERSE";


    public final static String REVERSE = "REVERSE";//冲正类交易
    public final static String SALE_REVERSE = "SALE_REVERSE";//消费冲正
    public final static String SALE_INSTALLMENT_REVERSE = "SALE_INSTALLMENT_REVERSE";//分期消费冲正
    public final static String SALE_SCAN_REVERSE = "SALE_SCAN_REVERSE";//扫码付冲正
    public final static String SCAN_PAY_REVERSE = "SCAN_PAY_REVERSE";//浦发扫码付冲正
    public final static String VOID_REVERSE = "VOID_REVERSE";//消费撤销冲正
    public final static String VOID_INSTALLMENT_REVERSE = "VOID_INSTALLMENT_REVERSE";//分期消费撤销冲正
    public final static String AUTH_REVERSE = "AUTH_REVERSE";//预授权冲正
    public final static String CANCEL_REVERSE = "CANCEL_REVERSE";//预授权撤销冲正
    public final static String AUTH_COMPLETE_REVERSE = "AUTH_COMPLETE_REVERSE";//预授权完成请求冲正
    public final static String COMPLETE_VOID_REVERSE = "COMPLETE_VOID_REVERSE";//预授权完成撤销冲正
    public final static String TRADE_QUERY = "TRADE_QUERY";
    public final static String PRINT_ANY = "PRINT_ANY";

    //public final static String E_QUICK_REVERSE = "E_QUICK_REVERSE";//快速支付冲正
    //public final static String E_COMMON_REVERSE = "E_COMMON_REVERSE";//普通消费冲正

    public final static String MOTO_SALE              = "MOTO_SALE";
    public final static String MOTO_VOID              = "MOTO_VOID";
    public final static String MOTO_REFUND            = "MOTO_REFUND";
    public final static String MOTO_AUTH              = "MOTO_AUTH";
    public final static String MOTO_CANCEL            = "MOTO_CANCEL";
    public final static String MOTO_AUTH_COMPLETE     = "MOTO_AUTH_COMPLETE";
    public final static String MOTO_COMPLETE_VOID     = "MOTO_COMPLETE_VOID";
    public final static String MOTO_AUTH_SETTLEMENT   = "MOTO_AUTH_SETTLEMENT";
    public final static String MOTO_CARDHOLDER_VERIFY = "MOTO_CARDHOLDER_VERIFY";

    public final static String SETTLEMENT = "SETTLEMENT";//批结算
    public final static String SETTLEMENT_DONE = "SETTLEMENT_DONE";//批上送结束
    public final static String TRANS_IC_DETAIL = "TRANS_IC_DETAIL";//IC卡联机交易明细上送
    public final static String TRANS_CARD_DETAIL = "TRANS_CARD_DETAIL";//磁条卡联机交易明细上送
    public final static String TRANS_FEFUND_DETAIL = "TRANS_FEFUND_DETAIL";//磁条卡联机交易明细上送

    public final static String UPDATE_TMK = "UPDATE_TMK";   //更新主密钥
    public final static String UPLOAD_ESIGN = "UPLOAD_ESIGN";   //更新电子签名
    //浦发总行添加的扫码交易
    public final static String SCAN_PAY = "SCAN_PAY";   //扫码支付
    public final static String SCAN_QUERY = "SCAN_QUERY";   //扫码查询
    public final static String SCAN_VOID = "SCAN_VOID";     //扫码撤销

    public final static String PRINT_LAST = "PRINT_LAST";//打印最后一笔
    public final static String PRINT_DETAIL = "PRINT_DETAIL";//打印交易明细
    public final static String DOWNLOAD_TER_PARAM = "DOWNLOAD_TER_PARAM";   //联通项目参数下载

    public final static String PRINT_BATCH_SUMMARY = "PRINT_BATCH_SUMMARY";//打印上批次总计
    public final static String UPLOAD_TRADE_BEFORE_SETTLEMENT = "UPLOAD_TRADE_BEFORE_SETTLEMENT";   //批结算前上送交易

    public final static String OFFLINE_SETTLEMENT = "OFFLINE_SETTLEMENT"; //离线结算

    public final static String OFFLINE_ADJUST = "OFFLINE_ADJUST"; //离线调整

    public final static String OFFLINE_ADJUST_TIP = "OFFLINE_ADJUST_TIP"; //离线调整 消费交易

    public final static String IC_OFFLINE_UPLOAD = "IC_OFFLINE_UPLOAD";//ic卡离线交易上送

    public final static String OFFLINE_UPLOAD = "OFFLINE_UPLOAD";//离线交易上送

    public final static String IC_OFFLINE_UPLOAD_SETTLE = "IC_OFFLINE_UPLOAD_SETTLE";//IC卡离线批上送
    public final static String OFFLINE_SETTLEMENT_UPLOAD_SETTLE = "OFFLINE_SETTLEMENT_UPLOAD_SETTLE";
    public final static String OFFLINE_ADJUST_UPLOAD_SETTLE = "OFFLINE_ADJUST_UPLOAD_SETTLE"; //离线调整
    public final static String OFFLINE_ADJUST_TIP_UPLOAD_SETTLE = "OFFLINE_ADJUST_TIP_UPLOAD_SETTLE"; //离线调整 消费交易

    public final static String CONTRACT_INFO_QUERY = "CONTRACT_INFO_QUERY";//合同信息查询
    public final static String SETTLEMENT_INFO_QUERY = "SETTLEMENT_INFO_QUERY";//结算账户查询

    //绿城接口
    public final static String picQuery = "picQuery";//机具查询对应项目轮播图
    public final static String staffVerify = "staffVerify";//OA账户密码使用限制
    public final static String isAuthorization = "isAuthorization";//是否需要授权接口
    public final static String unpaidQuery = "unpaidQuery";//未收款查询
    public final static String receivedQuery = "receivedQuery";//已收款查询接口
    public final static String orderSync = "orderSync";//主子订单信息同步
    public final static String authCheck = "authCheck";//三要素认证接口
    public final static String fingerRegister = "fingerRegister";//指纹注册接口
    public final static String fingerVerify = "fingerVerify";//指纹验证接口
    public final static String ticketUpload = "ticketUpload";//小票影像上传
    public final static String printReceipt = "printReceipt";//打印回执单
    public final static String repeatPrintReceipt = "repeatPrintReceipt";//重打印回执单
    public final static String syncPosSts = "syncPosSts";//POS状态同步接口
    public final static String generalReceipts = "generalReceipts";//是否支持普通收款查询接口

    public final static String SALE_RESULT_QUERY = "SALE_RESULT_QUERY";//9.2.30 消费结果查询

    public static Set<String> CAUSE_REVERSE_SETS = new HashSet<>();//可能引发冲正的交易类型
    public static Set<String> NO_MAC_SETS = new HashSet<>();//无需计算或者验证MAC的交易类型
    public static Set<String> REVERSE_SETS = new HashSet<>();//冲正类交易
    public static Set<String> NEED_INSERT_TABLE_SETS = new HashSet<>();//需要插入到交易表中的交易
    public static Set<String> SIGNED_BEFORE_TRADING_SETS = new HashSet<>();//交易前必须签到过
    public static Set<String> DEBIT_SETS = new HashSet<>();//借记类交易
    public static Set<String> CREDIT_SETS = new HashSet<>();//贷记类交易
    public static Set<String> ONLINE_SETS = new HashSet<>();//所有的联机交易
    public static Set<String> PRINT_IC_INFO = new HashSet<>();//打印IC卡信息
    public static Set<String> NOTIFY_TRADE_SETS = new HashSet<>();//通知类交易，记录发送报文，批上送时直接修改。

    public static Set<String> NO_AUTOSIGN_TRADE_SETS = new HashSet<>(); //不触发自动签到的交易

    public static Map<String, Integer> TRANS_NAME_MAP = new HashMap<>();    //交易名称表

    public static Map<String, Integer> TRANS_ENGLISH_NAME_MAP = new HashMap<>();    //交易英文名称表 用来打印的交易类型

    public static Set<String> FULL_PBOC_SETS = new HashSet<>();//完整流程PBOC

    static {
        PRINT_IC_INFO.add(SALE);
        PRINT_IC_INFO.add(AUTH);
        PRINT_IC_INFO.add(SALE_INSTALLMENT);
        PRINT_IC_INFO.add(E_QUICK);
        PRINT_IC_INFO.add(E_COMMON);
        PRINT_IC_INFO.add(EC_LOAD_CASH);
        PRINT_IC_INFO.add(EC_LOAD_INNER);
        PRINT_IC_INFO.add(EC_LOAD_OUTER);
        PRINT_IC_INFO.add(EC_VOID_CASH_LOAD);
        PRINT_IC_INFO.add(ISS_INTEGRAL_SALE);
        PRINT_IC_INFO.add(UNION_INTEGRAL_SALE);
        PRINT_IC_INFO.add("SALE_PROPERTY");

        CAUSE_REVERSE_SETS.add(SALE);
        //CAUSE_REVERSE_SETS.add(SALE_SCAN);
        CAUSE_REVERSE_SETS.add(SALE_INSTALLMENT);
        CAUSE_REVERSE_SETS.add(VOID);
        CAUSE_REVERSE_SETS.add(VOID_INSTALLMENT);
        CAUSE_REVERSE_SETS.add(AUTH);
        CAUSE_REVERSE_SETS.add(CANCEL);
        CAUSE_REVERSE_SETS.add(AUTH_COMPLETE);
        CAUSE_REVERSE_SETS.add(COMPLETE_VOID);
        CAUSE_REVERSE_SETS.add(EC_VOID_CASH_LOAD);

        CAUSE_REVERSE_SETS.add(ISS_INTEGRAL_SALE);
        CAUSE_REVERSE_SETS.add(UNION_INTEGRAL_SALE);
        CAUSE_REVERSE_SETS.add(ISS_INTEGRAL_VOID);
        CAUSE_REVERSE_SETS.add(UNION_INTEGRAL_VOID);

        CAUSE_REVERSE_SETS.add(RESERVATION_SALE);
        CAUSE_REVERSE_SETS.add(RESERVATION_VOID);


        //CAUSE_REVERSE_SETS.add(E_QUICK);


        NO_MAC_SETS.add(OBTAIN_TMK);
        NO_MAC_SETS.add(SIGN_IN);
        NO_MAC_SETS.add(SIGN_OUT);
        NO_MAC_SETS.add(SETTLEMENT);
        NO_MAC_SETS.add(SETTLEMENT_DONE);
        NO_MAC_SETS.add(TRANS_IC_DETAIL);
        NO_MAC_SETS.add(TRANS_CARD_DETAIL);
        NO_MAC_SETS.add(TRANS_FEFUND_DETAIL);
        NO_MAC_SETS.add(POS_STATUS_UPLOAD);
        NO_MAC_SETS.add(DOWNLOAD_PARAMS);
        NO_MAC_SETS.add(DOWNLOAD_CAPK);
        NO_MAC_SETS.add(DOWNLOAD_AID);
        NO_MAC_SETS.add(DOWNLOAD_QPS_PARAMS);
        NO_MAC_SETS.add(DOWNLOAD_CARD_BIN);
        NO_MAC_SETS.add(DOWNLOAD_CARD_BIN_QPS);
        NO_MAC_SETS.add(DOWNLOAD_BLACK_CARD_BIN_QPS);
        NO_MAC_SETS.add(DOWNLOAD_PARAMS_FINISHED);
        NO_MAC_SETS.add(ECHO_TEST);

        REVERSE_SETS.add(REVERSE);
        REVERSE_SETS.add(SALE_REVERSE);
        REVERSE_SETS.add(SALE_INSTALLMENT_REVERSE);
        REVERSE_SETS.add(SALE_SCAN_REVERSE);
        REVERSE_SETS.add(SCAN_PAY_REVERSE);
        REVERSE_SETS.add(VOID_REVERSE);
        REVERSE_SETS.add(VOID_INSTALLMENT_REVERSE);
        REVERSE_SETS.add(AUTH_REVERSE);
        REVERSE_SETS.add(CANCEL_REVERSE);
        REVERSE_SETS.add(AUTH_COMPLETE_REVERSE);
        REVERSE_SETS.add(COMPLETE_VOID_REVERSE);
        REVERSE_SETS.add(EC_VOID_CASH_LOAD_REVERSE);

        REVERSE_SETS.add(ISS_INTEGRAL_SALE);
        REVERSE_SETS.add(UNION_INTEGRAL_SALE);
        REVERSE_SETS.add(ISS_INTEGRAL_VOID);
        REVERSE_SETS.add(UNION_INTEGRAL_VOID);
        REVERSE_SETS.add(RESERVATION_SALE);
        REVERSE_SETS.add(RESERVATION_VOID);

        //REVERSE_SETS.add(E_QUICK_REVERSE);

        NEED_INSERT_TABLE_SETS.add(SALE);
        NEED_INSERT_TABLE_SETS.add(SALE_RESULT_QUERY);
        NEED_INSERT_TABLE_SETS.add(SALE_INSTALLMENT);
        NEED_INSERT_TABLE_SETS.add(SALE_SCAN);
        NEED_INSERT_TABLE_SETS.add(SCAN_PAY);
//        NEED_INSERT_TABLE_SETS.add(BALANCE);
        NEED_INSERT_TABLE_SETS.add(VOID);
        NEED_INSERT_TABLE_SETS.add(VOID_INSTALLMENT);
        NEED_INSERT_TABLE_SETS.add(VOID_SCAN);
        NEED_INSERT_TABLE_SETS.add(SCAN_VOID);
        NEED_INSERT_TABLE_SETS.add(REFUND);
        NEED_INSERT_TABLE_SETS.add(REFUND_SCAN);
        NEED_INSERT_TABLE_SETS.add(AUTH);
        NEED_INSERT_TABLE_SETS.add(CANCEL);
        NEED_INSERT_TABLE_SETS.add(AUTH_COMPLETE);
        NEED_INSERT_TABLE_SETS.add(AUTH_SETTLEMENT);
        NEED_INSERT_TABLE_SETS.add(COMPLETE_VOID);

        NEED_INSERT_TABLE_SETS.add(OFFLINE_SETTLEMENT); //离线结算
        NEED_INSERT_TABLE_SETS.add(OFFLINE_ADJUST); //离线调整
        NEED_INSERT_TABLE_SETS.add(OFFLINE_ADJUST_TIP);


        NEED_INSERT_TABLE_SETS.add(E_QUICK); //快速支付
        NEED_INSERT_TABLE_SETS.add(E_COMMON); //普通支付
        NEED_INSERT_TABLE_SETS.add(EC_LOAD_CASH);
        NEED_INSERT_TABLE_SETS.add(EC_LOAD_INNER);
        NEED_INSERT_TABLE_SETS.add(EC_LOAD_OUTER);
        NEED_INSERT_TABLE_SETS.add(EC_VOID_CASH_LOAD);
        NEED_INSERT_TABLE_SETS.add(E_REFUND);
        NEED_INSERT_TABLE_SETS.add(UNION_INTEGRAL_REFUND);

        NEED_INSERT_TABLE_SETS.add(ISS_INTEGRAL_SALE);
        NEED_INSERT_TABLE_SETS.add(UNION_INTEGRAL_SALE);
        NEED_INSERT_TABLE_SETS.add(ISS_INTEGRAL_VOID);
        NEED_INSERT_TABLE_SETS.add(UNION_INTEGRAL_VOID);
        NEED_INSERT_TABLE_SETS.add(MAG_CASH_LOAD);
        NEED_INSERT_TABLE_SETS.add(MAG_ACCOUNT_LOAD);

        NEED_INSERT_TABLE_SETS.add(RESERVATION_SALE);
        NEED_INSERT_TABLE_SETS.add(RESERVATION_VOID);

        SIGNED_BEFORE_TRADING_SETS.add(BALANCE);
        SIGNED_BEFORE_TRADING_SETS.add(SALE);
        SIGNED_BEFORE_TRADING_SETS.add(VOID);
        SIGNED_BEFORE_TRADING_SETS.add(REFUND);
        SIGNED_BEFORE_TRADING_SETS.add(AUTH);
        SIGNED_BEFORE_TRADING_SETS.add(AUTH_COMPLETE);
        SIGNED_BEFORE_TRADING_SETS.add(AUTH_SETTLEMENT);
        SIGNED_BEFORE_TRADING_SETS.add(CANCEL);
        SIGNED_BEFORE_TRADING_SETS.add(COMPLETE_VOID);

        SIGNED_BEFORE_TRADING_SETS.add(OFFLINE_SETTLEMENT); //离线结算
        SIGNED_BEFORE_TRADING_SETS.add(OFFLINE_ADJUST); //离线调整
        /*
        * 电子现金类交易
        * */
        SIGNED_BEFORE_TRADING_SETS.add(E_COMMON);       //电子现金接触式 普通支付
        SIGNED_BEFORE_TRADING_SETS.add(E_QUICK);        //电子现金非接触式 快速支付
        SIGNED_BEFORE_TRADING_SETS.add(E_BALANCE);      //电子现金 脱机余额
        SIGNED_BEFORE_TRADING_SETS.add(EC_LOAD_CASH);    //现金圈存
        SIGNED_BEFORE_TRADING_SETS.add(EC_LOAD_INNER);   //指定账户圈存
        SIGNED_BEFORE_TRADING_SETS.add(EC_LOAD_OUTER);   //非指定账户圈存
        SIGNED_BEFORE_TRADING_SETS.add(EC_VOID_CASH_LOAD);
        SIGNED_BEFORE_TRADING_SETS.add(E_REFUND);

        /*
        * 积分类
        * */
        SIGNED_BEFORE_TRADING_SETS.add(UNION_INTEGRAL_BALANCE);
        SIGNED_BEFORE_TRADING_SETS.add(UNION_INTEGRAL_REFUND);

        SIGNED_BEFORE_TRADING_SETS.add(ISS_INTEGRAL_SALE);
        SIGNED_BEFORE_TRADING_SETS.add(UNION_INTEGRAL_SALE);
        SIGNED_BEFORE_TRADING_SETS.add(ISS_INTEGRAL_VOID);
        SIGNED_BEFORE_TRADING_SETS.add(UNION_INTEGRAL_VOID);

        SIGNED_BEFORE_TRADING_SETS.add(RESERVATION_SALE);
        SIGNED_BEFORE_TRADING_SETS.add(RESERVATION_VOID);


        DEBIT_SETS.add(SALE);
        DEBIT_SETS.add(SALE_INSTALLMENT);
        //DEBIT_SETS.add(SALE_SCAN);
        DEBIT_SETS.add(SCAN_PAY);
//        DEBIT_SETS.add(AUTH);     //预授权不参与结算
        DEBIT_SETS.add(AUTH_COMPLETE);
        DEBIT_SETS.add(AUTH_SETTLEMENT);

        DEBIT_SETS.add(OFFLINE_SETTLEMENT); //离线结算
        DEBIT_SETS.add(OFFLINE_ADJUST);//离线调整
        DEBIT_SETS.add(E_QUICK);//快速支付
        DEBIT_SETS.add(E_COMMON);//普通支付
        DEBIT_SETS.add(EC_LOAD_OUTER);//非指定账户圈存
        DEBIT_SETS.add(ISS_INTEGRAL_SALE);
        DEBIT_SETS.add(UNION_INTEGRAL_SALE);
        DEBIT_SETS.add(RESERVATION_SALE);

        CREDIT_SETS.add(VOID);
        CREDIT_SETS.add(VOID_INSTALLMENT);
        CREDIT_SETS.add(VOID_SCAN);
        CREDIT_SETS.add(SCAN_VOID);
        CREDIT_SETS.add(REFUND);
        CREDIT_SETS.add(REFUND_SCAN);
        CREDIT_SETS.add(COMPLETE_VOID);
//        CREDIT_SETS.add(CANCEL);      //预授权撤销不参与结算
        CREDIT_SETS.add(EC_LOAD_CASH);
        /*
        * 积分类
        * */
        CREDIT_SETS.add(UNION_INTEGRAL_REFUND);/*联盟积分退货*/

        CREDIT_SETS.add(ISS_INTEGRAL_VOID);
        CREDIT_SETS.add(UNION_INTEGRAL_VOID);
        CREDIT_SETS.add(MAG_CASH_LOAD);
        CREDIT_SETS.add(RESERVATION_VOID);



        NOTIFY_TRADE_SETS.add(REFUND);
        NOTIFY_TRADE_SETS.add(REFUND_SCAN);
        NOTIFY_TRADE_SETS.add(E_REFUND);
        NOTIFY_TRADE_SETS.add(UNION_INTEGRAL_REFUND);

        NO_AUTOSIGN_TRADE_SETS.add(SIGN_IN);
        NO_AUTOSIGN_TRADE_SETS.add(SIGN_OUT);
        NO_AUTOSIGN_TRADE_SETS.add(OBTAIN_TMK);
        NO_AUTOSIGN_TRADE_SETS.add(TERMINAL_INIT);
        NO_AUTOSIGN_TRADE_SETS.add(DOWNLOAD_BLACK_CARD_BIN_QPS);
        NO_AUTOSIGN_TRADE_SETS.add(DOWNLOAD_CARD_BIN_QPS);
        NO_AUTOSIGN_TRADE_SETS.add(DOWNLOAD_CARD_BIN);
        NO_AUTOSIGN_TRADE_SETS.add(DOWNLOAD_AID);
        NO_AUTOSIGN_TRADE_SETS.add(DOWNLOAD_CAPK);
        NO_AUTOSIGN_TRADE_SETS.add(DOWNLOAD_PARAMS);
        NO_AUTOSIGN_TRADE_SETS.add(DOWNLOAD_PARAMS_FINISHED);
        NO_AUTOSIGN_TRADE_SETS.add(DOWNLOAD_QPS_PARAMS);
        NO_AUTOSIGN_TRADE_SETS.add(DOWNLOAD_TER_PARAM);
        NO_AUTOSIGN_TRADE_SETS.add(DOWNLOAD_TERMINAL_PARAMETER);

        FULL_PBOC_SETS.add(SALE);
        FULL_PBOC_SETS.add(SALE_NEED_PIN);
        FULL_PBOC_SETS.add(SALE_INSERT);
        FULL_PBOC_SETS.add(AUTH);
        FULL_PBOC_SETS.add(BALANCE);
        FULL_PBOC_SETS.add(SALE_INSTALLMENT);
        FULL_PBOC_SETS.add(E_COMMON);
        FULL_PBOC_SETS.add(EC_LOAD_CASH);
        FULL_PBOC_SETS.add(EC_LOAD_INNER);
        FULL_PBOC_SETS.add(EC_LOAD_OUTER);
        FULL_PBOC_SETS.add(EC_VOID_CASH_LOAD);
        FULL_PBOC_SETS.add(UNION_INTEGRAL_BALANCE);

        FULL_PBOC_SETS.add(ISS_INTEGRAL_SALE);
        FULL_PBOC_SETS.add(UNION_INTEGRAL_SALE);
        FULL_PBOC_SETS.add("SALE_PROPERTY");


        TRANS_NAME_MAP.put(UNION_INTEGRAL_BALANCE,R.string.union_integral_balance);
        TRANS_NAME_MAP.put(UNION_INTEGRAL_REFUND,R.string.union_integral_refund);

        TRANS_NAME_MAP.put(ISS_INTEGRAL_SALE,R.string.iss_integral_sale);
        TRANS_NAME_MAP.put(UNION_INTEGRAL_SALE,R.string.union_integral_sale);

        TRANS_NAME_MAP.put(ISS_INTEGRAL_VOID,R.string.iss_integral_void);
        TRANS_NAME_MAP.put(UNION_INTEGRAL_VOID,R.string.union_integral_void);

        TRANS_NAME_MAP.put(MAG_CASH_LOAD,R.string.mag_cash_load);
        TRANS_NAME_MAP.put(MAG_ACCOUNT_VERIFY,R.string.mag_cash_load);
        TRANS_NAME_MAP.put(MAG_ACCOUNT_LOAD,R.string.mag_account_load);
        TRANS_NAME_MAP.put(MAG_CASH_LOAD_CONFIRM,R.string.mag_cash_load);
        TRANS_NAME_MAP.put(MAG_ACCOUNT_LOAD_VERIFY,R.string.mag_account_load);
        TRANS_NAME_MAP.put(RESERVATION_SALE,R.string.reservation_sale);
        TRANS_NAME_MAP.put(RESERVATION_VOID,R.string.reservation_void);
        TRANS_NAME_MAP.put(SETTLEMENT, R.string.settlement_name);
        TRANS_NAME_MAP.put(SETTLEMENT_DONE, R.string.settlement_name);
        TRANS_NAME_MAP.put(TRANS_IC_DETAIL, R.string.trans_detail_upload_name);
        TRANS_NAME_MAP.put(TRANS_CARD_DETAIL, R.string.trans_detail_upload_name);
        TRANS_NAME_MAP.put(TRANS_FEFUND_DETAIL, R.string.trans_detail_upload_name);


        TRANS_ENGLISH_NAME_MAP.put(TransCode.SALE_SCAN,R.string.trans_sale_en);
        TRANS_ENGLISH_NAME_MAP.put(TransCode.SCAN_PAY,R.string.trans_sale_en);
        TRANS_ENGLISH_NAME_MAP.put(TransCode.SCAN_QUERY,R.string.trans_sale_en);
        TRANS_ENGLISH_NAME_MAP.put(TransCode.VOID_SCAN,R.string.trans_void_en);
        TRANS_ENGLISH_NAME_MAP.put(TransCode.SCAN_VOID,R.string.trans_void_en);
        TRANS_ENGLISH_NAME_MAP.put(TransCode.REFUND_SCAN,R.string.trans_refund_en);

        TRANS_ENGLISH_NAME_MAP.put(TransCode.SALE,R.string.trans_sale_en);
        TRANS_ENGLISH_NAME_MAP.put(TransCode.VOID,R.string.trans_void_en);
        TRANS_ENGLISH_NAME_MAP.put(TransCode.REFUND,R.string.trans_refund_en);
        TRANS_ENGLISH_NAME_MAP.put(TransCode.AUTH,R.string.trans_auth_en);
        TRANS_ENGLISH_NAME_MAP.put(TransCode.AUTH_COMPLETE,R.string.trans_auth_complete_en);
        TRANS_ENGLISH_NAME_MAP.put(TransCode.CANCEL,R.string.trans_cancel_en);
        TRANS_ENGLISH_NAME_MAP.put(TransCode.COMPLETE_VOID,R.string.trans_complete_void_en);
        TRANS_ENGLISH_NAME_MAP.put(TransCode.BALANCE,R.string.trans_balance_en);
        TRANS_ENGLISH_NAME_MAP.put(TransCode.SALE_INSERT,R.string.trans_sale_en);
        TRANS_ENGLISH_NAME_MAP.put(TransCode.SALE_NEED_PIN, R.string.trans_sale_en);
        TRANS_ENGLISH_NAME_MAP.put(TransCode.AUTH_NEED_PIN, R.string.trans_auth_en);
        TRANS_ENGLISH_NAME_MAP.put(TransCode.SALE_INSTALLMENT, R.string.trans_sale_installment_en);
        TRANS_ENGLISH_NAME_MAP.put(TransCode.VOID_INSTALLMENT, R.string.trans_void_installment_en);
        TRANS_ENGLISH_NAME_MAP.put(TransCode.E_QUICK, R.string.e_quick_en);//快速消费
        TRANS_ENGLISH_NAME_MAP.put(TransCode.E_COMMON, R.string.e_common_en);//普通消费
        TRANS_ENGLISH_NAME_MAP.put(TransCode.E_LOAD, R.string.e_load_en);//圈存
        TRANS_ENGLISH_NAME_MAP.put(TransCode.E_BALANCE, R.string.e_balance_en);//余额查询
        TRANS_ENGLISH_NAME_MAP.put(TransCode.E_REFUND, R.string.e_refund_en);//脱机退货
        TRANS_ENGLISH_NAME_MAP.put(TransCode.EC_TRANS_RECORDS, R.string.ec_trans_records_en);//电子现交易明细
        TRANS_ENGLISH_NAME_MAP.put(TransCode.EC_LOAD_CASH, R.string.ec_load_cash_en); //现金圈存
        TRANS_ENGLISH_NAME_MAP.put(TransCode.EC_LOAD_INNER, R.string.ec_load_inner_en); //指定账户圈存
        TRANS_ENGLISH_NAME_MAP.put(TransCode.EC_LOAD_OUTER, R.string.ec_load_outer_en); //非指定账户圈存
        TRANS_ENGLISH_NAME_MAP.put(TransCode.EC_VOID_CASH_LOAD, R.string.ec_void_cash_load_en); //现金充值撤销
        TRANS_ENGLISH_NAME_MAP.put(TransCode.RESERVATION_SALE, R.string.reservation_sale_en);
        TRANS_ENGLISH_NAME_MAP.put(TransCode.RESERVATION_VOID, R.string.reservation_void_en);
        TRANS_ENGLISH_NAME_MAP.put(TransCode.ISS_INTEGRAL_SALE, R.string.iss_integral_sale_en);
        TRANS_ENGLISH_NAME_MAP.put(TransCode.UNION_INTEGRAL_SALE, R.string.union_integral_sale_en);
        TRANS_ENGLISH_NAME_MAP.put(TransCode.ISS_INTEGRAL_VOID, R.string.iss_integral_void_en);
        TRANS_ENGLISH_NAME_MAP.put(TransCode.UNION_INTEGRAL_VOID, R.string.union_integral_void_en);
        TRANS_ENGLISH_NAME_MAP.put(TransCode.UNION_INTEGRAL_BALANCE, R.string.union_integral_balance_en);
        TRANS_ENGLISH_NAME_MAP.put(TransCode.UNION_INTEGRAL_REFUND, R.string.union_integral_refund_en);
        TRANS_ENGLISH_NAME_MAP.put(TransCode.MAG_CASH_LOAD, R.string.mag_cash_load_en);
        TRANS_ENGLISH_NAME_MAP.put(TransCode.MAG_ACCOUNT_LOAD,R.string.mag_account_load_en);

        TRANS_ENGLISH_NAME_MAP.put(TransCode.OFFLINE_SETTLEMENT,R.string.trans_offline_settlement_en);
        TRANS_ENGLISH_NAME_MAP.put(TransCode.OFFLINE_ADJUST,R.string.trans_offline_adjust_en);
        TRANS_ENGLISH_NAME_MAP.put(TransCode.OFFLINE_ADJUST_TIP,R.string.trans_offline_adjust_en);
    }

    public static int codeMapNameEn(String transCode){
        int id = R.string.unknown_en;

        id = (TRANS_ENGLISH_NAME_MAP.get(transCode)==null) ? id : TRANS_ENGLISH_NAME_MAP.get(transCode);
        return id;
    }

    public static int codeMapName(String transCode) {

        if (transCode == null) {
            logger.warn(transCode + "==>未知交易名称");
            return R.string.unknown;
        }
        switch (transCode) {
            case SALE:
                return R.string.trans_sale;
            case SALE_INSTALLMENT:
                return R.string.trans_sale_installment;
            case SALE_SCAN:
            case SCAN_PAY:
            case SCAN_QUERY:
                return R.string.trans_sale_scan;
            case SALE_INSERT:
                return R.string.trans_sale_insert;
            case SALE_NEED_PIN:
                return R.string.trans_sale_need_pin;
            case BALANCE:
                return R.string.trans_balance;
            case VOID:
                return R.string.trans_void;
            case VOID_INSTALLMENT:
                return R.string.trans_void_installment;
            case VOID_SCAN:
                return R.string.trans_void_scan;
            case REFUND:
                return R.string.trans_refund;
            case REFUND_SCAN:
                return R.string.trans_refund_scan;
            case AUTH:
                return R.string.trans_auth;
            case AUTH_NEED_PIN:
                return R.string.trans_auth_need_pin;
            case AUTH_SETTLEMENT:
                return R.string.trans_auth_complete_notify;
            case AUTH_COMPLETE:
                return R.string.trans_auth_complete;
//                return R.string.trans_auth_settlement;
            case CANCEL:
                return R.string.trans_cancel;
            case COMPLETE_VOID:
                return R.string.trans_complete_void;
            case SIGN_IN:
                return R.string.trans_sign_in;
            case DOWNLOAD_CAPK:
                return R.string.trans_download_capk;
            case DOWNLOAD_AID:
                return R.string.trans_download_aid;
            case DOWNLOAD_CARD_BIN:
                return R.string.trans_download_card_bin;
            case DOWNLOAD_QPS_PARAMS:
                return R.string.trans_download_qps;
            case OFFLINE_SETTLEMENT: //离线结算
                return R.string.trans_offline_settlement;
            case OFFLINE_ADJUST: //离线调整
            case OFFLINE_ADJUST_TIP:
                return R.string.trans_offline_adjust;
            case E_QUICK: return R.string.e_quick;

            case E_COMMON: return R.string.e_common;

            case E_LOAD: return R.string.e_load;

            case E_UNLOAD: return R.string.e_unload;

            case E_BALANCE: return R.string.e_balance;

            case E_UPLOAD: return R.string.e_unload;

            case EC_LOAD_CASH: return R.string.ec_load_cash;

            case EC_LOAD_INNER: return R.string.ec_load_inner;

            case EC_LOAD_OUTER: return R.string.ec_load_outer;

            case EC_VOID_CASH_LOAD:  return R.string.ec_void_cash_load;

            case E_REFUND: return R.string.e_refund;

            case EC_LOAD_RECORDS : return R.string.ec_load_records;

            case EC_TRANS_RECORDS : return R.string.ec_trans_records;

            default:
                Integer nameResId = TRANS_NAME_MAP.get(transCode);
                if (nameResId != null)
                    return nameResId;
                logger.warn(transCode + "==>未知交易名称");
                return R.string.unknown;
        }
    }

    /**
     * 获取交易名称
     * */
    public static String getTransName(String transCode, String payType){
        //01:微信 02:支付宝 03:银联
        String transType = "未定义";
        if(TextUtils.isEmpty(transCode)){
            return transType;
        }
        String transName = EposApplication.getAppContext().getResources().getString(com.centerm.epos.common.TransCode.codeMapName(transCode));
        if(!TextUtils.isEmpty(payType)){
            switch (payType){
                case "01":
                    transType = "微信" + transName;
                    break;
                case "02":
                    transType = "支付宝" + transName;
                    break;
                case "03":
                    transType = "银联" + transName;
                    break;
                default:
                    transType = transName;
                    break;
            }
        }else {
            transType = transName;
        }
        return transType;
    }


}
