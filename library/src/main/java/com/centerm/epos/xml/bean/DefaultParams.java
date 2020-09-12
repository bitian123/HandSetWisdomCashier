package com.centerm.epos.xml.bean;

import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;

import org.apache.log4j.Logger;

/**
 * author:wanliang527</br>
 * date:2017/2/10</br>
 */

public class DefaultParams {

    private Logger logger = Logger.getLogger(DefaultParams.class);

    private String version;//参数版本
    private String category;//参数类别，对应它的父节点名称
    private String fileName;//参数文件名
    private String key;//参数键名
    private String value;//参数值

    private int index;//参数ID，定义时必须为int类型，否则无法解析
    private DataType type = DataType.TEXT;//参数数据类型，默认为text

    public enum DataType {
        HEX,
        BCD,
        TEXT,
        BOOLEAN,
        INTEGER,
        DOUBLE
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] toHex() {
        if (getType() == DataType.BCD || getType() == DataType.HEX) {
            return HexUtils.hexStringToByte(getValue());
        }
        return null;
    }

    public int toInt() {
        if (getType() == DataType.INTEGER) {
            return Integer.valueOf(getValue());
        }
        return 0;
    }

    public double toDouble() {
        if (getType() == DataType.DOUBLE) {
            return Double.valueOf(getValue());
        }
        return 0.0;
    }

    public boolean toBoolean() {
        if (getType() == DataType.BOOLEAN) {
            return Boolean.valueOf(getValue());
        }
        return false;
    }

    @Override
    public String toString() {
        if (getType() == DataType.TEXT) {
            return getValue();
        } else {
            return "DefaultParams{" +
                    "version='" + version + '\'' +
                    ", category='" + category + '\'' +
                    ", fileName='" + fileName + '\'' +
                    ", key='" + key + '\'' +
                    ", value='" + value + '\'' +
                    ", index=" + index +
                    ", type=" + type +
                    '}';
        }
    }
}
