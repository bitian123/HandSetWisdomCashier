package com.centerm.epos.bean.transcation;

/**
 * Created by yuhc on 2017/2/12.
 * 安全控制信息
 */

public class SecurityControlInformation {
    /**
     * PIN加密方法,0：PIN不出现  1：ANSI X9.8 Format（不带主账号信息） 2：ANSI X9.8 Format（带主账号信息）
     */
    private int pinFormat;

    /**
     * 加密算法标志   0：单倍长密钥算法  6：双倍长密钥算法
     */
    private int encryptionMethod;

    /**
     * 磁道加密标志    0：不加密   1：加密
     */
    private int trackEncryption;

    public SecurityControlInformation() {
    }

    public SecurityControlInformation(int pinFormat, int encryptionMethod, int trackEncryption) {
        this.pinFormat = pinFormat;
        this.encryptionMethod = encryptionMethod;
        this.trackEncryption = trackEncryption;
    }

    public int getPinFormat() {
        return pinFormat;
    }

    public void setPinFormat(int pinFormat) {
        this.pinFormat = pinFormat;
    }

    public int getEncryptionMethod() {
        return encryptionMethod;
    }

    public void setEncryptionMethod(int encryptionMethod) {
        this.encryptionMethod = encryptionMethod;
    }

    public int getTrackEncryption() {
        return trackEncryption;
    }

    public void setTrackEncryption(int trackEncryption) {
        this.trackEncryption = trackEncryption;
    }
}
