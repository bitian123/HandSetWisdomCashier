package com.centerm.epos.xml.bean;

import com.centerm.epos.xml.keys.Keys;
import com.centerm.epos.xml.keys.XmlTag;

import java.util.HashMap;
import java.util.Map;

/**
 * author:wanliang527</br>
 * date:2017/2/8</br>
 */

public class ConfigCatalog {

    private String project;//项目名称
    private String tradeFlowPath;//交易流程文件目录
    private Map<String, XmlFile> tagMapXml = new HashMap<>();

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

/*    public XmlFile getShortcutFile() {
        return tagMapXml.get(Keys.obj().shortcut);
    }

    public void setShortcutFile(XmlFile shortcutFile) {
    }

    public XmlFile getThirdlyMenuFile() {
        return tagMapXml.get(Keys.obj().thirdlyMenu);
    }

    public void setThirdlyMenuFile(XmlFile thirdlyMenuFile) {
    }

    public XmlFile getSecondaryMenuFile() {
        return tagMapXml.get(Keys.obj().secondaryMenu);
    }

    public void setSecondaryMenuFile(XmlFile secondaryMenuFile) {
    }

    public XmlFile getPrimaryMenuFile() {
        return tagMapXml.get(Keys.obj().primaryMenu);
    }

    public void setPrimaryMenuFile(XmlFile primaryMenuFile) {
    }*/

    public void putXmlFile(String tag, XmlFile file) {
        tagMapXml.put(tag, file);
    }

    public XmlFile getXmlFile(String tag) {
        return tagMapXml.get(tag);
    }

/*    public String getTradeFlowPath() {
        XmlFile file = tagMapXml.get("trade_flow");
        String path = getProject() + (file == null ? null : file.getFileName());
        return path;
    }

    public void setTradeFlowPath(String tradeFlowPath) {
        this.tradeFlowPath = tradeFlowPath;
    }*/

}
