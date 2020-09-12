package com.centerm.epos.base;

/**
 * Created by yuhc on 2017/8/31.
 */

public interface SimpleStringTag {

    String TOGGLE_ESIGN_SUPPORT = "TOGGLE_ESIGN_SUPPORT";    //电子签名开头
    String TOGGLE_ESIGN_MUL_PACKAGE = "TOGGLE_ESIGN_MUL_PACKAGE";   //电子签名上送多包支持
    String TOGGLE_SIGN_PAD_INNER = "TOGGLE_SIGN_PAD_INNER";     //内置签名板
    String TOGGLE_ESIGN_PHONE_NUMBER = "TOGGLE_ESING_PHONE_NUMBER"; //电话号码

    String ESIGN_PACKAGE_LEN_MAX = "ESIGN_PACKAGE_LEN_MAX"; //电子签名上送时的最大包长度
    String ESIGN_INPUT_TIMEOUT = "ESIGN_INPUT_TIMEOUT";     //签名输入超时
    String ESIGN_BATCH_REQ_RETRY_TIMES = "ESIGN_BATCH_REQ_RETRY_TIMES"; //批量请求重试次数
    String ESIGN_STORE_MAX = "ESIGN_STORE_MAX"; //最大存储笔数

    String TOGGLE_APP_UPGRADE_SUPPORT = "TOGGLE_APP_UPGRADE_SUPPORT";   //程序自动更新开关
    String TOGGLE_APP_UPGRADE_WIFI_ONLY = "TOGGLE_APP_UPGRADE_WIFI_ONLY";   //仅支持WIFI环境下自动更新
    String APP_UPGRADE_CONNECT_TIMEOUT = "APP_UPGRADE_CONNECT_TIMEOUT"; //连接服务器超时

    String APP_THEME_TAG = "APP_THEME_TAG"; //主题，红色或蓝色

    String PROJECT_DB_VERSION = "PROJECT_DB_VERSION";   //应用项目数据库的版本号
}
