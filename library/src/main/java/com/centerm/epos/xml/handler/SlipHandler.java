package com.centerm.epos.xml.handler;

import android.text.TextUtils;

import com.centerm.cpay.midsdk.dev.define.printer.PrinterDataItem;
import com.centerm.epos.xml.bean.slip.SlipElement;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.List;

/**
 * author:wanliang527</br>
 * date:2017/2/14</br>
 */
public class SlipHandler extends BaseHandler {

    private int version;
    private List<SlipElement> elements = new ArrayList<>();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (tagStack.size() == 0) {
            if (!xmlTag.Slip.equals(localName)) {
                throwRootTagIllegalException(xmlTag.Slip);
            }
            String v = attributes.getValue(xmlAttrs.version);
            if (TextUtils.isEmpty(v)) {
                throwAttrUndefineException(xmlAttrs.version);
            }
            version = Integer.valueOf(attributes.getValue(xmlAttrs.version));
        }
        if (xmlTag.Item.equals(localName)) {
            String tag = attributes.getValue(xmlAttrs._tag);
            String align = attributes.getValue(xmlAttrs.align);
//            String _default = attributes.getValue(xmlAttrs._default);
            String enLabel = attributes.getValue(xmlAttrs.enLabel);
            String enable = attributes.getValue(xmlAttrs.enable);
            String font = attributes.getValue(xmlAttrs.font);
            String isBold = attributes.getValue(xmlAttrs.isBold);
            String label = attributes.getValue(xmlAttrs.label);
            String type = attributes.getValue(xmlAttrs.type);
            String belongs = attributes.getValue(xmlAttrs.belongs);
            String isWrapValue = attributes.getValue(xmlAttrs.isWrapValue);
            String source = attributes.getValue(xmlAttrs.source);
            String value = attributes.getValue(xmlAttrs.value);
            String condition = attributes.getValue(xmlAttrs.condition);

            String valueFont = attributes.getValue(xmlAttrs.valueFont);
            String valueAlign = attributes.getValue(xmlAttrs.valueAlign);
            String valueIsBold = attributes.getValue(xmlAttrs.valueIsBold);
            String isPrintNull = attributes.getValue(xmlAttrs.isPrintNull);

            if (TextUtils.isEmpty(tag)) {
                throwAttrUndefineException(xmlAttrs._tag);
            }
            SlipElement ele = new SlipElement(tag);
            ele.setVersion(version);
            ele.setAlign(align == null ? PrinterDataItem.Align.LEFT : PrinterDataItem.Align.valueOf(align));
//            ele.setDefValue(_default);
            ele.setEnable("FALSE".equalsIgnoreCase(enable) ? false : true);
            ele.setEnLabel(enLabel);
            ele.setFont(font == null ? SlipElement.FontSize.MEDIUM : SlipElement.FontSize.valueOf(font));
            ele.setBold("TRUE".equalsIgnoreCase(isBold));
            ele.setLabel(label);
            ele.setType(type == null ? SlipElement.Type.TEXT : SlipElement.Type.valueOf(type));
            ele.setBelongs(belongs == null ? SlipElement.Belongs.BOTH : SlipElement.Belongs.valueOf(belongs));
            ele.setWrapValue("TRUE".equalsIgnoreCase(isWrapValue));
            ele.setValueFont(valueFont == null ? SlipElement.FontSize.MEDIUM : SlipElement.FontSize.valueOf(valueFont));
            ele.setValueAlign(valueAlign == null ? PrinterDataItem.Align.LEFT : PrinterDataItem.Align.valueOf(valueAlign));
            ele.setValueBold("TRUE".equalsIgnoreCase(valueIsBold));
            ele.setCondition(condition == null ? null : SlipElement.Condition.valueOf(condition));
            ele.setSource(source == null ? SlipElement.Source.VARIABLE : SlipElement.Source.valueOf(source));
            if (!TextUtils.isEmpty(value)) {
                ele.setValue(value.replace("\\n", "\n"));
            } else {
                ele.setValue("");
            }
            if (!TextUtils.isEmpty(isPrintNull))
                ele.setPrintNull(Boolean.parseBoolean(isPrintNull));
            elements.add(ele);
        }
        tagStack.add(localName);
    }

    public List<SlipElement> getElements() {
        return elements;
    }
}
