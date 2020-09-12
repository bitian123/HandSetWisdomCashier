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

public class ParamsHandler extends BaseHandler {

    private String version;
    private String gFileName;
    private String gName;
    private Map<String, DefaultParams> keyMapParams = new HashMap<>();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (tagStack.size() == 0) {
            if (!xmlTag.Parameter.equals(localName)) {
                throwRootTagIllegalException(XmlTag.obj().Parameter);
            }
            version = attributes.getValue(xmlAttrs.version);
        }
        if (xmlTag.Group.equals(localName)) {
            gFileName = attributes.getValue(xmlAttrs.fileName);
            gName = attributes.getValue(xmlAttrs.name);
        } else if (xmlTag.Item.equals(localName)) {
            DefaultParams params = new DefaultParams();
            params.setVersion(version);
            params.setCategory(gName);
            params.setFileName(gFileName);

            String fileName = attributes.getValue(xmlAttrs.fileName);
            String key = attributes.getValue(xmlAttrs.key);
            if (TextUtils.isEmpty(key)) {
                throwAttrUndefineException(xmlAttrs.key);
            }
            String value = attributes.getValue(xmlAttrs.value);
            if (TextUtils.isEmpty(value)) {
                throwAttrUndefineException(xmlAttrs.value);
            }
            String index = attributes.getValue(xmlAttrs.index);
            String type = attributes.getValue(xmlAttrs.type);

            if (!TextUtils.isEmpty(fileName)) {
                params.setFileName(fileName);
            }
            params.setKey(key);
            params.setValue(value);
            params.setIndex(Integer.valueOf(index));
            if ("HEX".equalsIgnoreCase(type)) {
                params.setType(DefaultParams.DataType.HEX);
            } else if ("BCD".equalsIgnoreCase(type)) {
                params.setType(DefaultParams.DataType.BCD);
            } else if ("TEXT".equalsIgnoreCase(type)) {
                params.setType(DefaultParams.DataType.TEXT);
            } else if ("BOOLEAN".equalsIgnoreCase(type)) {
                params.setType(DefaultParams.DataType.BOOLEAN);
            }
            keyMapParams.put(key, params);
        }
        tagStack.push(localName);
    }

    public Map<String, DefaultParams> getParamsMap() {
        return keyMapParams;
    }


}
