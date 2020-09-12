package com.centerm.epos.xml.handler;

import android.text.TextUtils;

import com.centerm.epos.xml.bean.message.Iso8583FieldProcessItem;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuhc on 2017/3/9.
 * POS数据域处理类解析
 */

public class IsoFieldProcessHandler extends BaseHandler {
    private int version;
    private List<Iso8583FieldProcessItem> items = new ArrayList<>();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (tagStack.size() == 0){
            if (!xmlTag.process.equals(localName)) {
                throwRootTagIllegalException(xmlTag.Slip);
            }
            String v = attributes.getValue(xmlAttrs.version);
            if (TextUtils.isEmpty(v)) {
                throwAttrUndefineException(xmlAttrs.version);
            }
            version = Integer.valueOf(attributes.getValue(xmlAttrs.version));
        }else {
            if (xmlTag.field.equals(localName)){
                String index = attributes.getValue(xmlAttrs.index);
                String name = attributes.getValue(xmlAttrs.name);
                String clz = attributes.getValue(xmlAttrs._class);
                Iso8583FieldProcessItem item = new Iso8583FieldProcessItem();
                if (TextUtils.isEmpty(index))
                    return;
                item.setIndex(Integer.parseInt(index,10));
                item.setName(name);
                item.setProcessClz(clz);
                items.add(item);
            }
        }
        tagStack.add(localName);
    }

    public List<Iso8583FieldProcessItem> getItems() {
        return items;
    }
}
