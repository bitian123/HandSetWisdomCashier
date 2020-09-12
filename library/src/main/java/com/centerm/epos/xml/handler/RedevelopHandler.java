package com.centerm.epos.xml.handler;

import com.centerm.epos.xml.bean.RedevelopItem;
import com.centerm.epos.xml.keys.XmlTag;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.HashMap;
import java.util.Map;

/**
 * author:wanliang527</br>
 * date:2017/2/12</br>
 */

public class RedevelopHandler extends BaseHandler {

    private String version;
    private Map<String, RedevelopItem> keyMapRedevelop = new HashMap<>();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (tagStack.size() == 0) {
            if (!xmlTag.Redevelop.equals(localName)) {
                throwRootTagIllegalException(XmlTag.obj().Redevelop);
            }
            version = attributes.getValue(xmlAttrs.version);
        }
        if (xmlTag.Item.equals(localName)) {
            String tag = attributes.getValue(xmlAttrs.tag);
            String index = attributes.getValue(xmlAttrs.index);
            String cls = attributes.getValue(xmlAttrs._class);
            RedevelopItem item = new RedevelopItem();
            item.setIndex(Integer.valueOf(index));
            item.setVersion(version);
            item.setKey(tag);
            item.setClssName(cls);
            keyMapRedevelop.put(tag, item);
        }
        tagStack.push(localName);
    }

    public Map<String, RedevelopItem> getRedevelopMap() {
        return keyMapRedevelop;
    }
}
