package com.centerm.epos.utils;

import android.text.TextUtils;

import org.apache.log4j.Logger;

import config.LogConfiguration;

/**
 * Created by yuhc on 2017/2/8.
 * 日志工具类，可控制日志的输出和关闭，简化使用过程。
 */

public class XLogUtil {

    /** log开关 */
    public static final boolean SWITCH_LOG = true;
    private static boolean isConfigured = false;

    public static void setIsConfigured(boolean result){
        isConfigured = result;
    }

    public static void d(String tag, String message) {
        if (SWITCH_LOG) {
            Logger LOGGER = getLogger(tag);
            LOGGER.debug(message);
        }
    }

    public static void d(String tag, String message, Throwable exception) {
        if (SWITCH_LOG) {
            Logger LOGGER = getLogger(tag);
            LOGGER.debug(message, exception);
        }
    }

    public static void i(String tag, String message) {
        if (SWITCH_LOG) {
            Logger LOGGER = getLogger(tag);
            LOGGER.info(message);
        }
    }

    public static void i(String tag, String message, Throwable exception) {
        if (SWITCH_LOG) {
            Logger LOGGER = getLogger(tag);
            LOGGER.info(message, exception);
        }
    }

    public static void w(String tag, String message) {
        if (SWITCH_LOG) {
            Logger LOGGER = getLogger(tag);
            LOGGER.warn(message);
        }
    }

    public static void w(String tag, String message, Throwable exception) {
        if (SWITCH_LOG) {
            Logger LOGGER = getLogger(tag);
            LOGGER.warn(message, exception);
        }
    }

    public static void e(String tag, String message) {
        if (SWITCH_LOG) {
            Logger LOGGER = getLogger(tag);
            LOGGER.error(message);
        }
    }

    public static void e(String tag, String message, Throwable exception) {
        if (SWITCH_LOG) {
            Logger LOGGER = getLogger(tag);
            LOGGER.error(message, exception);
        }
    }

    private static Logger getLogger(String tag) {
        if (!isConfigured) {
            LogConfiguration.obtainDefault().configure();
            isConfigured = true;
        }
        Logger logger;
        if (TextUtils.isEmpty(tag)) {
            logger = Logger.getRootLogger();
        } else {
            logger = Logger.getLogger(tag);
        }
        return logger;
    }

}
