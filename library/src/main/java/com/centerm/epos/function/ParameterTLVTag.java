package com.centerm.epos.function;

/**
 * Created by yuhc on 2017/8/18.
 * PC工具下载参数时，TLV格式内容的标签定义
 */

public interface ParameterTLVTag {

    //商户号
    int MECHANT_NUM = 0x81;
    //终端号
    int TERMINAL_NUM = 0x82;
    //商户名称
    int MERCHANT_NAME = 0x83;
    //TPDU
    int COMM_TPDU = 0x84;
    //LOGO地区码
    int AREA_CODE = 0x85;
    //是否打印LOGO
    int SLIP_LOGO = 0x86;
    //电子签名
    int E_SLIP_SIGN = 0x87;
    //打印凭单数设置
    int SLIP_NUM = 0x88;
    //是否预拨号
    int PRE_DIAL_SERVER = 0x89;
    //国密算法
    int SM_SUPPORT = 0x8A;
    //系统管理密码
    int MANAGER_PWD = 0x8B;
    //通讯方式
    int COMM_TYPE = 0x8C;
    //外线号码
    int OUTER_PHONE_NUM = 0x8D;
    //电话号码1
    int SERVER_NUM_1 = 0x8E;
    //电话号码2
    int SERVER_NUM_2 = 0x8F;
    //电话号码3
    int SERVER_NUM_3 = 0x91;
    //服务器IP地址1
    int SERVER_IP_1 = 0x92;
    //服务器端口号1
    int SERVER_PORT_1 = 0x93;
    //服务器IP地址2
    int SERVER_IP_2 = 0x94;
    //服务器端口号2
    int SERVER_PORT_2 = 0x95;
    //APN
    int APN_ACCESS = 0x96;
    //APN USER NAME
    int APN_USER_NAME = 0x97;
    //APN USER PWD
    int APN_USER_PWD = 0x98;
    //连接超时
    int CONNECT_TIMEOUT = 0x99;
    //外接非接读卡器
    int OUTER_WIRELESS_READER = 0x9A;
    //外接读卡器连接口
    int OUTER_READER_INTERFACE = 0x9B;
    //MIS模式
    int IMS_MODE = 0x9C;
    //扫描模块
    int SCANNER_MODULE = 0x9D;

    //完成标识
    int COMM_END = 0x71;

}
