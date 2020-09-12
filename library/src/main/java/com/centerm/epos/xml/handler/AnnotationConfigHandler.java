package com.centerm.epos.xml.handler;

import com.centerm.epos.xml.bean.RedevelopItem;
import com.centerm.epos.xml.keys.XmlTag;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.HashMap;
import java.util.Map;

/**
 * author:yuhc</br>
 * date:2017/10/16</br>
 */

public class AnnotationConfigHandler extends BaseHandler {

    private String version;
    private Map<String, String> keyMapAnnotationConf = new HashMap<>();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (tagStack.size() == 0) {
            if (!xmlTag.ANNOTION_CONFIG.equals(localName)) {
                throwRootTagIllegalException(XmlTag.obj().ANNOTION_CONFIG);
            }
            version = attributes.getValue(xmlAttrs.version);
        }
        if (xmlTag.Item.equals(localName)) {
            String tag = attributes.getValue(xmlAttrs.tag);
            String packageName = attributes.getValue(xmlAttrs.PACKAGE_NAME);
            keyMapAnnotationConf.put(tag, packageName);
        }
        tagStack.push(localName);
    }

    public Map<String, String> getConfigMap() {
        return keyMapAnnotationConf;
    }
}
