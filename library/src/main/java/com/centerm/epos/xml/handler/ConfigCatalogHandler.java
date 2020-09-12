package com.centerm.epos.xml.handler;

import android.text.TextUtils;

import com.centerm.epos.xml.bean.ConfigCatalog;
import com.centerm.epos.xml.bean.XmlFile;
import com.centerm.epos.xml.keys.Keys;
import com.centerm.epos.xml.keys.XmlAttrs;
import com.centerm.epos.xml.keys.XmlTag;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Stack;

/**
 * author:wanliang527</br>
 * date:2017/2/8</br>
 */
public class ConfigCatalogHandler extends BaseHandler {

    private ConfigCatalog bean;


    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        bean = new ConfigCatalog();
        tagStack = new Stack<>();
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
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
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        String project = attributes.getValue(xmlAttrs.project);
        String tag = attributes.getValue(xmlAttrs.tag);
        String path = attributes.getValue(xmlAttrs.filePath);
        String version = attributes.getValue(xmlAttrs.version);
        String enable = attributes.getValue(xmlAttrs.enable);
        if (tagStack.size() == 0) {
            //判断第一个节点，如果第一个节点不符合要求，则停止解析
            if (!XmlTag.obj().Configuration.equals(localName)) {
                throwRootTagIllegalException(xmlTag.Configuration);
            }
            if (TextUtils.isEmpty(project)) {
                throw new SAXException("Please assign the attribute named \"" + xmlAttrs.project + "\" ");
            }
            bean.setProject(project);
        } else {
            XmlFile file = new XmlFile();
            file.setFileName(path);
            file.setVersion(version);
            file.setEnable("false".equalsIgnoreCase(enable) ? false : true);
            bean.putXmlFile(tag, file);
        }
        tagStack.add(localName);
    }

    public ConfigCatalog getBean() {
        return bean;
    }

}
