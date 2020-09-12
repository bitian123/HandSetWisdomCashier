package com.centerm.epos.xml.bean.project;

import java.util.List;

/**
 * Created by yuhc on 2017/6/20.
 */

public class ProjectConfig {
    //默认项目
    private String defaultPrjTag;
    //项目配置信息
    private List<ConfigItem> prjItems;

    public ProjectConfig() {
    }

    public String getDefaultPrjTag() {
        return defaultPrjTag;
    }

    public void setDefaultPrjTag(String defaultPrjTag) {
        this.defaultPrjTag = defaultPrjTag;
    }

    public List<ConfigItem> getPrjItems() {
        return prjItems;
    }

    public int getItemCount(){
        if (prjItems == null)
            return 0;
        return prjItems.size();
    }

    public void setPrjItems(List<ConfigItem> prjItems) {
        this.prjItems = prjItems;
    }
}
