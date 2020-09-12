package com.centerm.epos.xml.handler;

import com.centerm.epos.xml.bean.PreferDataPool;
import com.centerm.epos.xml.keys.XmlAttrs;
import com.centerm.epos.xml.keys.XmlTag;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Stack;

/**
 * author:wanliang527</br>
 * date:2017/2/9</br>
 */

public class PreferHandler extends BaseHandler {

    private PreferDataPool bean;

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        tagStack = new Stack<>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (tagStack.size() == 0) {
            if (!xmlTag.Map.equals(localName)) {
                throw new SAXException("The first tag is illegal, must be " + xmlTag.Map + ".");
            }
            bean = new PreferDataPool();
        }
        String key = attributes.getValue(XmlAttrs.obj().key);
        String value = attributes.getValue(XmlAttrs.obj().value);
        if (xmlTag._String.equals(localName)) {
            bean.put(key, value);
        } else if (xmlTag._Int.equals(localName)) {
            int v = Integer.valueOf(value);
            bean.put(key, v);
        } else if (xmlTag._Double.equals(localName)) {
            double v = Double.valueOf(value);
            bean.put(key, v);
        } else if (xmlTag._Long.equals(localName)) {
            long v = Long.valueOf(value);
            bean.put(key, v);
        } else if (xmlTag._Boolean.equals(localName)) {
            boolean v = Boolean.valueOf(value);
            bean.put(key, v);
        } else {
            if (tagStack.size() > 0) {
                logger.warn("\"" + localName + "\" is not a valid data type");
            }
        }
        tagStack.add(localName);
    }

    public PreferDataPool getPreferDataBean() {
        return bean;
    }
}
