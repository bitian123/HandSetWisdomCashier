package com.centerm.epos.ebi.keys;

/**
 * Created by FL on 2017/9/30 15:01.
 * 对应3级菜单选项
 */

public interface EbiMenuTag {

    String ORDER_QUERY = "ORDER_QUERY";//扫码支付

    String TOGGLE_ALI_SCAN = "TOGGLE_ALI_SCAN";

    String TOGGLE_WEI_SCAN = "TOGGLE_WEI_SCAN";

    String TOGGLE_SCAN_PRE_CAMERA = "TOGGLE_SCAN_PRE_CAMERA";//是否使用前置摄像头

    String QUERY_SCAN_SALE = "QUERY_SCAN_SALE";//移动支付交易查询菜单

    String QUERY_SCAN_PAY = "QUERY_SCAN_PAY";//扫码付查询菜单

    String TRADE_QUERY = "TRADE_QUERY";//管理-交易查询
    String WIFI_SETTINGS = "WIFI_SETTINGS";//wifi设置
    String TMS_SETTINGS = "TMS_SETTINGS";//TMS设置
    String UART_IMPORT_TMK = "UART_IMPORT_TMK"; //串口导入主密钥菜单
    String DOWNLOAD_MAIN_KEY = "DOWNLOAD_MAIN_KEY";//下载签名密钥
    String DOWNLOAD_POS_MAIN_KEY = "DOWNLOAD_POS_MAIN_KEY";//POS主密钥远程下载
    String SCAN_COMMUNICATION_SETTINGS = "SCAN_COMMUNICATION_SETTINGS";//扫码地址
    String GT_COMMUNICATION_SETTINGS = "GT_COMMUNICATION_SETTINGS";//绿城地址

}
