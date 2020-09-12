package com.centerm.epos.xml.bean;

/**
 * author:wanliang527</br>
 * date:2017/2/12</br>
 */

public class RedevelopItem {
    private String version;
    private String key;
    private String clssName;
    private int index;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getClassName() {
        return clssName;
    }

    public void setClssName(String clssName) {
        this.clssName = clssName;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return "RedevelopItem{" +
                "version='" + version + '\'' +
                ", key='" + key + '\'' +
                ", clssName='" + clssName + '\'' +
                ", index=" + index +
                '}';
    }
}
