package com.centerm.epos.common;

/**
 * 交易过程中临时数据的键名
 * author:wanliang527</br>
 * date:2016/10/29</br>
 */

public class TransDataKey {

    //    public final static String key_entry_mode = "pos_entry_mode_code";
//    public final static String key_track2 = "track_2_data";
//    public final static String key_track3 = "track_3_data";
//    public final static String key_card_no = "primary_acct_num";
//    public final static String key_amount = "amt_trans";
//    public final static String key_resp_cd = "resp_cd";
//    public final static String key_tmk = "tmk";
//    public final static String key_work_key = "work_key";
//    public final static String key_terminal_id = "card_accptr_termnl_id";
//    public final static String key_mchnt_id = "card_accptr_id";
//    public final static String key_mchnt_name = "mchnt_name";
//    public final static String key_card_seq = "card_seq";
//    public final static String key_expire_dt = "expire_dt";
//    public final static String key_iso55 = "ic_card_data";
//    public final static String key_online_pin = "crypt_pin";
//    public final static String key_slip_version = "slip_version";
//    public final static String key_admin_login_flag = "key_admin_login_flag";

    public final static String keyFlagFallback = "keyFlagFallback";//降级交易的标志
    public final static String keyFlagNoPin = "keyFlagNoPin";//无PIN标志
    public final static String keyBalanceAmt = "keyBalanceAmt";//余额，或者IC第一货币余额
    public final static String keyBalanceAmtCode = "keyBalanceAmtCode";//余额，或者IC第一货币余额 代码
    public final static String keyBalanceAmtSecondCode = "keyBalanceAmtSecondCode";//IC卡第二货币余额 代码
    public final static String keyBalanceAmtSecond = "keyBalanceAmtSecond";//IC卡第二货币余额
    public final static String keyLocalMac = "keyLocalMac";
    public final static String keyIsAdmin = "keyIsAdmin";
    public final static String FLAG_IMPORT_AMOUNT = "FLAG_IMPORT_AMOUNT";//内核等待金额导入的标识
    public final static String FLAG_IMPORT_CARD_CONFIRM_RESULT = "FLAG_IMPORT_CARD_CONFIRM_RESULT";//内核等待卡号信息确认的标识
    public final static String FLAG_IMPORT_PIN = "FLAG_IMPORT_PIN";//内核等待导入PIN的标识
    public final static String FLAG_REQUEST_ONLINE = "FLAG_REQUEST_ONLINE";//内核请求联机的标识
    public final static String FLAG_AUTO_SIGN = "FLAG_AUTO_SIGN";//自动签到的标识

    public final static String KEY_PARAMS_TYPE = "KEY_PARAMS_TYPE";//需要下载的参数类型，1-IC卡公钥下载；2-IC参数下载；3-国密公钥下载；4-非接参数下载；5-卡BIN下载
    public final static String KEY_PARAMS_COUNTS = "KEY_PARAMS_COUNTS";//已经下载的参数条数
    public final static String KEY_CAPK_INFO = "KEY_CAPK_INFO";//CAPK信息（RID和索引）
    public final static String KEY_IC_DATA_PRINT = "KEY_IC_DATA_PRINT";//IC卡数据，用于打印
    public final static String KEY_QPS_FLAG = "KEY_QPS_FLAG";//小额免密业务标识
    public final static String KEY_TRANS_TIME = "KEY_TRANS_TIME";//终端本地交易时间
    public final static String KEY_IC_SCRIPT_RESULT = "KEY_IC_SCRIPT_RESULT";//IC卡脚本执行结果
    public final static String KEY_HOLDER_NAME = "KEY_HOLDER_NAME";//持卡人姓名

    public final static String key_sn = "key_sn";
    public final static String RESET_DOWNLOADPARAM_FLAG = "RESET_DOWNLOADPARAM_FLAG";//重置参数下载标志
    public final static String RESET_DOWNLOADPARAM_FLAG1 = "RESET_DOWNLOADPARAM_FLAG1";//重置参数下载标志
    public final static String FLAG_HAS_DOWNLOAD_PARAM = "FLAG_HAS_DOWNLOAD_PARAM";//是否下载终端参数标志
    public final static String FLAG_HAS_DOWNLOAD_SIGN_MAK = "FLAG_HAS_DOWNLOAD_SIGN_MAK";//是否下载签名主密钥标志
    public final static String FLAG_HAS_DOWNLOAD_CARK = "FLAG_HAS_DOWNLOAD_CARK";//是否下载IC卡公钥标志
    public final static String FLAG_HAS_DOWNLOAD_AID = "FLAG_HAS_DOWNLOAD_AID";//是否下载IC卡参数标志

    public final static String FLAG_HAS_DOWNLOAD_CARK_COMMON = "FLAG_HAS_DOWNLOAD_CARK_COMMON";//是否下载IC卡公钥标志-支付组件
    public final static String FLAG_HAS_DOWNLOAD_AID_COMMON = "FLAG_HAS_DOWNLOAD_AID_COMMON";//是否下载IC卡参数标志-支付组件

    public final static String key_scan_address = "key_scan_address";
    public final static String key_address_reserve = "key_address_reserve";
    public final static String key_scan_address_reserve = "key_scan_address_reserve";
    public final static String key_scan_port = "key_scan_port";
    public final static String key_port_reserve = "key_port_reserve";
    public final static String key_scan_port_reserve = "key_scan_port_reserve";
    public final static String key_scan_address_gt = "key_scan_address_gt";
    public final static String key_scan_port_gt = "key_scan_port_gt";
    public final static String key_scan_address_reserve_gt = "key_scan_address_reserve_gt";
    public final static String key_scan_port_reserve_gt = "key_scan_port_reserve_gt";

    public final static String headerData = "headerdata";
    public final static String iso_f2 = "iso_f2";
    public final static String iso_f3 = "iso_f3";
    public final static String iso_f4 = "iso_f4";
    public final static String iso_f5 = "iso_f5";
    public final static String iso_f6 = "iso_f6";
    public final static String iso_f7 = "iso_f7";
    public final static String iso_f8 = "iso_f8";
    public final static String iso_f9 = "iso_f9";
    public final static String iso_f10 = "iso_f10";
    public final static String iso_f11 = "iso_f11";
    public final static String iso_f11_origin = "iso_f11_origin";//原交易的11域
    public final static String iso_f12 = "iso_f12";
    public final static String iso_f13 = "iso_f13";
    //    public final static String iso_f13_receive = "iso_f13_receive";
    public final static String iso_f14 = "iso_f14";
    public final static String iso_f15 = "iso_f15";
    public final static String iso_f16 = "iso_f16";
    public final static String iso_f17 = "iso_f17";
    public final static String iso_f18 = "iso_f18";
    public final static String iso_f19 = "iso_f19";
    public final static String iso_f20 = "iso_f20";
    public final static String iso_f21 = "iso_f21";
    public final static String iso_f22 = "iso_f22";
    public final static String iso_f23 = "iso_f23";
    public final static String iso_f24 = "iso_f24";
    public final static String iso_f25 = "iso_f25";
    public final static String iso_f26 = "iso_f26";
    public final static String iso_f27 = "iso_f27";
    public final static String iso_f28 = "iso_f28";
    public final static String iso_f29 = "iso_f29";
    public final static String iso_f30 = "iso_f30";
    public final static String iso_f31 = "iso_f31";
    public final static String iso_f32 = "iso_f32";
    public final static String iso_f33 = "iso_f33";
    public final static String iso_f34 = "iso_f34";
    public final static String iso_f35 = "iso_f35";
    public final static String iso_f36 = "iso_f36";
    public final static String iso_f37 = "iso_f37";
    public final static String iso_f38 = "iso_f38";
    public final static String iso_f39 = "iso_f39";
    public final static String iso_f40 = "iso_f40";
    public final static String iso_f41 = "iso_f41";
    public final static String iso_f42 = "iso_f42";
    public final static String iso_f43 = "iso_f43";
    public final static String iso_f44 = "iso_f44";
    public final static String iso_f45 = "iso_f45";
    public final static String iso_f46 = "iso_f46";
    public final static String iso_f47 = "iso_f47";
    public final static String iso_f48 = "iso_f48";
    public final static String iso_f49 = "iso_f49";
    public final static String iso_f50 = "iso_f50";
    public final static String iso_f51 = "iso_f51";
    public final static String iso_f52 = "iso_f52";
    public final static String iso_f53 = "iso_f53";
    public final static String iso_f54 = "iso_f54";
    public final static String iso_f55 = "iso_f55";
    public final static String iso_f55_reverse = "iso_f55_reverse";
    public final static String iso_f56 = "iso_f56";
    public final static String iso_f57 = "iso_f57";
    public final static String iso_f58 = "iso_f58";
    public final static String iso_f59 = "iso_f59";
    public final static String iso_f60 = "iso_f60";
    public final static String iso_f60_origin = "iso_f60_origin";//原交易的60域
    public final static String iso_f61 = "iso_f61";
    public final static String iso_f62 = "iso_f62";
    public final static String iso_f63 = "iso_f63";
    public final static String iso_f64 = "iso_f64";
    public final static String key_flag = "key_flag";
    public final static String key_noPinFlag = "key_noPinFlag";
    public final static String key_noSignFlag = "key_noSignFlag";
    public final static String key_oriTransTime = "key_oriTransTime";
    public final static String key_oriAuthCode = "key_oriAuthCode";
    public final static String key_retryTimes = "key_retryTimes";
    public final static String key_resp_code = "key_resp_code";
    public final static String key_resp_msg = "key_resp_msg";
    public final static String key_is_amount_ok = "key_is_amount_ok";
    public final static String key_batch_upload_count = "key_batch_upload_count";
    public final static String key_oriVoucherNumber = "key_oriVoucherNumber";   //原交易凭证号
    public final static String key_oriReferenceNumber = "key_oriReferenceNumber";   //原交易参考号
    public final static String key_oriTransDate = "key_oriTransDate";   //原交易日期
    /*
    *BUGID:0002279: 进行结算，平台返回对账不平，批上送结束为207，应该为202
    *@author zhouzhihua 2017.11.07
    * */
    public final static String key_is_balance_settle = "key_is_balance_settle";//内卡对账是否平 '1'-平  '0'-不平
    public final static String key_is_balance_settle_foreign = "key_is_balance_settle_foreign";//外卡对账是否平 '1'-平  '0'-不平

    public final static String key_is_load_second_use_card = "key_is_load_second_use_card";//第二次用卡判断 '1'-二次  '0'-


    public final static String KEY_TRANSFER_INTO_CARD_SERVICE_ENTRY_MODE = "KEY_TRANSFER_INTO_CARD_SERVICE_ENTRY_MODE";//转入卡服务点输入方式码

    public final static String KEY_EC_TIPS_CONFIRM = "KEY_EC_TIPS_CONFIRM"; //此标志表示接触电子现金交易金额小于余额

    public final static String KEY_IC_CONTINUE_ONLINE = "KEY_IC_CONTINUE_ONLINE"; //IC卡交易是否可以继续联机，1-联机，其它-等待,目前只用在接触式电子现金充值撤销

    public final static String KEY_ORI_TERMINAL_NO = "KEY_ORI_TERMINAL_NO"; //脱机退货中原批终端号

    public final static String KEY_ORI_BATCH_NO = "KEY_ORI_BATCH_NO"; //脱机退货中原批次号

    public final static String KEY_TRANSFER_INTO_CARD_TRACK_2_DATA = "KEY_TRANSFER_INTO_CARD_TRACK_2_DATA"; //转入卡2磁道信息
    public final static String KEY_TRANSFER_INTO_CARD_TRACK_3_DATA = "KEY_TRANSFER_INTO_CARD_TRACK_3_DATA"; //转入卡3磁道信息
    public final static String KEY_TRANSFER_INTO_CARD_DATE_EXPIRED = "KEY_TRANSFER_INTO_CARD_DATE_EXPIRED"; //转入卡有效期
}
