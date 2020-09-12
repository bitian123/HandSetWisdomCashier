package com.centerm.epos.configure;

import android.text.TextUtils;

import com.centerm.epos.xml.bean.project.ConfigItem;
import com.centerm.epos.xml.bean.project.ProjectConfig;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by yuhc on 2017/6/21.
 *
 */

public class EposProject {

    public static final String BASE_PRJ_TAG = "BASE";
    private Map<String,ConfigItem> mProjectConfig;
    private static EposProject instance;

    private EposProject(){
        mProjectConfig = new HashMap<>();
    }

    public static EposProject getInstance(){
        if (instance != null)
            return instance;
        synchronized (EposProject.class){
            if (instance == null){
                instance = new EposProject();
            }
        }
        return instance;
    }

    public void fillProjectConfig(ProjectConfig projectConfig){
        if (projectConfig != null && projectConfig.getItemCount() > 0){
            List<ConfigItem> configItems = projectConfig.getPrjItems();
            for (ConfigItem item : configItems) {
                if (!TextUtils.isEmpty(item.getPrjTag()))
                    mProjectConfig.put(item.getPrjTag(), item);
            }
        }
    }

    public String getBaseProjectTag(){
        return BASE_PRJ_TAG;
    }

    public boolean isBaseProject(String prjTag){
        return BASE_PRJ_TAG.equalsIgnoreCase(prjTag);
    }

    public boolean isProjectExist(String prjTag){
        return mProjectConfig.containsKey(prjTag);
    }

    public boolean isPayChannelProjectExist(String channelTag){
        if (TextUtils.isEmpty(channelTag))
            return false;
        Collection<ConfigItem> items = mProjectConfig.values();
        for (ConfigItem item : items) {
            if (channelTag.equalsIgnoreCase(item.getChannelID()))
                return true;
        }
        return false;
    }

    public String getId(String prjTag) {
        ConfigItem item = mProjectConfig.get(prjTag);
        if (item == null)
            return "";
        return item.getPrjID();
    }

    public String getChannelId(String prjTag){
        ConfigItem item = mProjectConfig.get(prjTag);
        if (item == null)
            return "";
        return item.getChannelID();
    }

    public String getProjectTagByChannelID(String payCode) {
        if (TextUtils.isEmpty(payCode))
            return null;
        Set<Map.Entry<String,ConfigItem>> set = mProjectConfig.entrySet();
        for (Map.Entry<String,ConfigItem> encry: set) {
            if(payCode.equalsIgnoreCase(encry.getValue().getChannelID()))
                return encry.getKey();
        }
        return null;
    }

    public boolean isChangeAppIcon(String prjTag){
        return mProjectConfig.get(prjTag).isChangeAppIcon();
    }
}
