package com.centerm.epos.common;

import com.centerm.epos.R;

import org.apache.log4j.Logger;

/**
 * author:wanliang527</br>
 * date:2016/11/3</br>
 */

public enum ISORespCode {

    ISO0("00", "A", R.string.tip_trade_success),
    ISO1("01", "C", R.string.tip_iso1),
    ISO3("03", "C", R.string.tip_iso3),
    ISO4("04", "D", R.string.tip_iso4),
    ISO5("05", "C", R.string.tip_iso5),
    ISO6("06", "C", R.string.tip_sign_in_again),//钱宝自定义
    ISO7("07", "C", R.string.tip_iso7),
    ISO8("08", "C", R.string.tip_invalid_sn),//钱宝自定义
    ISO9("09", "C", R.string.tip_iso9),
    ISO10("10", "A", R.string.tip_iso10),
    ISO11("11", "A", R.string.tip_iso11),
    ISO12("12", "C", R.string.tip_iso12),
    ISO13("13", "B", R.string.tip_iso13),
    ISO14("14", "B", R.string.tip_iso14),
    ISO15("15", "C", R.string.tip_iso15),
    ISO17("17", "C", R.string.tip_iso17),
    ISO18("18", "C", R.string.tip_iso18),//钱宝自定义
    ISO19("19", "C", R.string.tip_iso19),
    ISO20("20", "C", R.string.tip_iso20),
    ISO21("21", "C", R.string.tip_iso21),
    ISO22("22", "C", R.string.tip_iso22),
    ISO23("23", "C", R.string.tip_iso23),
    ISO25("25", "C", R.string.tip_iso25),
    ISO26("26", "C", R.string.tip_iso26),
    ISO29("29", "C", R.string.tip_iso29),
    ISO30("30", "C", R.string.tip_iso30),
    ISO34("34", "C", R.string.tip_iso34),
    ISO38("38", "C", R.string.tip_iso38),
    ISO40("40", "C", R.string.tip_iso40),
    ISO41("41", "C", R.string.tip_iso41),
    ISO43("43", "C", R.string.tip_iso43),
    ISO45("45", "C", R.string.tip_iso45),
    ISO51("51", "C", R.string.tip_iso51),
    ISO54("54", "C", R.string.tip_iso54),
    ISO55("55", "C", R.string.tip_iso55),
    ISO57("57", "C", R.string.tip_iso57),
    ISO58("58", "C", R.string.tip_iso58),
    ISO59("59", "C", R.string.tip_iso59),
    ISO61("61", "C", R.string.tip_iso61),
    ISO62("62", "C", R.string.tip_iso62),
    ISO64("64", "C", R.string.tip_iso64),
    ISO65("65", "C", R.string.tip_iso65),
    ISO68("68", "C", R.string.tip_iso68),
    ISO75("75", "C", R.string.tip_iso75),
    ISO90("90", "C", R.string.tip_iso90),
    ISO91("91", "C", R.string.tip_iso91),
    ISO92("92", "C", R.string.tip_iso92),
    ISO94("94", "C", R.string.tip_iso94),
    ISO96("96", "C", R.string.tip_iso96),
    ISO97("97", "C", R.string.tip_iso97),
    ISO98("98", "C", R.string.tip_iso98),
    ISO99("99", "C", R.string.tip_iso99),
    ISOA0("A0", "B", R.string.tip_isoA0),
    ISOA1("A1", "B", R.string.tip_isoA1),
    ISOA2("A2", "B", R.string.tip_isoA2),
    ISOA3("A3", "B", R.string.tip_isoA3),
    ISOA4("A4", "B", R.string.tip_isoA4),
    ISOA5("A5", "B", R.string.tip_isoA5),
    ISOA6("A6", "B", R.string.tip_isoA6),
    ISOA7("A7", "B", R.string.error_tips_scan_timeout),
    ISO_UNKNOWN("E111", "C", R.string.tip_iso_unknown);

    private String code;//代码
    private String type;//类别
    private int resId;//提示

    ISORespCode(String code, String type, int resId) {
        this.code = code;
        this.type = type;
        this.resId = resId;
    }

    public static ISORespCode codeMap(String code) {
        ISORespCode[] values = ISORespCode.values();
        for (int i = 0; i < values.length; i++) {
            if (values[i].code.equals(code)) {
                return values[i];
            }
        }
        Logger logger = Logger.getLogger(ISORespCode.class);
        logger.warn("错误码" + code + "未定义");
        ISO_UNKNOWN.code = code;
        return ISO_UNKNOWN;
    }

    public String getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    public int getResId() {
        return resId;
    }
}
