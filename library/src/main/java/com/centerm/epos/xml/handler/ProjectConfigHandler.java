package com.centerm.epos.xml.handler;

import android.text.TextUtils;

import com.centerm.epos.xml.bean.project.ConfigItem;
import com.centerm.epos.xml.bean.project.ProjectConfig;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.Stack;

/**
 * author:wanliang527</br>
 * date:2016/10/25</br>
 */

public class ProjectConfigHandler extends BaseHandler {

    private ProjectConfig mProjectConfig;
    private ConfigItem mConfigItem;
    private Stack<String> tagStack = new Stack<>();

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        tagStack = new Stack<>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        String enTag = attributes.getValue(xmlAttrs.enTag);
        String prjID = attributes.getValue(xmlAttrs.projectID);
        String channelID = attributes.getValue(xmlAttrs.channelID);
        String changeAppIcon = attributes.getValue(xmlAttrs.changeAppIcon);

        if (xmlTag.Menu.equals(localName)) {
            mProjectConfig = new ProjectConfig();
            mProjectConfig.setPrjItems(new ArrayList<ConfigItem>());
        } else if (xmlTag.MenuItem.equals(localName)) {
            mConfigItem = new ConfigItem();
        } else {
            throw new SAXException("The tag[" + localName + "] is illegal, must be " + xmlTag.Menu + " or " + xmlTag
                    .MenuItem);
        }
        if (tagStack.size() == 0) {
            //第一个节点，判断是否是menu节点
            if (!xmlTag.Menu.equals(localName)) {
                throw new SAXException("The first tag is illegal, must be " + xmlTag.Menu + ".");
            } else {
                if (mProjectConfig != null) {
                    String defaultPrj = attributes.getValue(xmlAttrs.defaultPrj);
                    mProjectConfig.setDefaultPrjTag(defaultPrj);
                }
            }
        }
        if (mConfigItem != null && mProjectConfig != null) {
            mConfigItem.setPrjTag(enTag);
            mConfigItem.setPrjID(prjID);
            mConfigItem.setChannelID(channelID);
            if (TextUtils.isEmpty(changeAppIcon))
                mConfigItem.setChangeAppIcon(true);
            else
                mConfigItem.setChangeAppIcon(Boolean.parseBoolean(changeAppIcon));
            mProjectConfig.getPrjItems().add(mConfigItem);
            mConfigItem = null;
        }
        tagStack.push(localName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        tagStack = null;
    }

    public ProjectConfig getProjectConfig() {
        return mProjectConfig;
    }
}
