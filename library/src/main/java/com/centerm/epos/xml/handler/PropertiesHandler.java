package com.centerm.epos.xml.handler;

import android.text.TextUtils;

import com.centerm.epos.xml.bean.DefaultParams;
import com.centerm.epos.xml.keys.XmlTag;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认参数解析
 * author:wanliang527</br>
 * date:2017/2/10</br>
 */

public class PropertiesHandler extends BaseHandler {

    private Map<String, String> keyMapParams = new HashMap<>();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (tagStack.size() == 0) {
            if (!xmlTag.Map.equals(localName)) {
                throwRootTagIllegalException(XmlTag.obj().Parameter);
            }
//            version = attributes.getValue(xmlAttrs.version);
        }
        if (xmlTag.Item.equals(localName)) {
            String key = attributes.getValue(xmlAttrs.key);
            if (TextUtils.isEmpty(key)) {
                throwAttrUndefineException(xmlAttrs.key);
            }
            String value = attributes.getValue(xmlAttrs.value);
            if (TextUtils.isEmpty(value)) {
                throwAttrUndefineException(xmlAttrs.value);
            }
            keyMapParams.put(key, value);
        }
        tagStack.push(localName);
    }

    public Map<String, String> getParamsMap() {
        return keyMapParams;
    }

}
