package com.centerm.epos.xml.bean.project;

/**
 * Created by yuhc on 2017/6/20.
 * 项目配置信息
 */

public class ConfigItem {
    //项目标识
    private String prjTag;
    //项目ID
    private String prjID;
    //渠道ID
    private String channelID;
    //是否改变桌面图标
    private Boolean isChangeAppIcon;

    public ConfigItem() {
    }

    public ConfigItem(String prjTag, String prjID, String channelID) {
        this.prjTag = prjTag;
        this.prjID = prjID;
        this.channelID = channelID;
        isChangeAppIcon = true;
    }

    public String getPrjTag() {
        return prjTag;
    }

    public void setPrjTag(String prjTag) {
        this.prjTag = prjTag;
    }

    public String getPrjID() {
        return prjID;
    }

    public void setPrjID(String prjID) {
        this.prjID = prjID;
    }

    public String getChannelID() {
        return channelID;
    }

    public void setChannelID(String channelID) {
        this.channelID = channelID;
    }

    public Boolean isChangeAppIcon() {
        return isChangeAppIcon;
    }

    public void setChangeAppIcon(Boolean changeAppIcon) {
        isChangeAppIcon = changeAppIcon;
    }
}
