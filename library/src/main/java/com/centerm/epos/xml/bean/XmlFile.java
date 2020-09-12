package com.centerm.epos.xml.bean;

/**
 * XML文件实体类
 * author:wanliang527</br>
 * date:2017/2/8</br>
 */

public class XmlFile {
    private String version;//版本号
    private boolean enable;//使能开关
    private String fileName;//文件路径

    public XmlFile() {
    }

    public XmlFile(String fileName) {
        this.fileName = fileName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "XmlFile{" +
                "version='" + version + '\'' +
                ", enable=" + enable +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
