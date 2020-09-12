package com.centerm.epos.xml.handler;

import android.text.TextUtils;

import com.centerm.epos.EposApplication;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.xml.bean.TradeItem;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TradeItemHandler extends BaseHandler {

    //xml文件中标签定义
    private final String TAG_TRADE = "trade";
    private final String TAG_ITEM = "Item";
    //属性名称定义
    private final String ATTR_VERSION = "version";
    private final String ATTR_CLASS = "class";
    private final String ATTR_CHECK = "check";
    private final String ATTR_TAG = "tag";

    private Map<String, TradeItem> tradeItemMap = new HashMap<>();

    public Map<String, TradeItem> getTradeItemMap() {
        return tradeItemMap;
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (TAG_TRADE.equals(localName)) {
//            String version = attributes.getValue(ATTR_VERSION);
        } else if (TAG_ITEM.equals(localName)) {

            String tag = replaceVariable(attributes.getValue(ATTR_TAG));
            String checkName = attributes.getValue(ATTR_CHECK);
            String className = attributes.getValue(ATTR_CLASS);
            tradeItemMap.put(tag, new TradeItem(tag, checkName, className));
        }
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }

    final String VARIABLE_PREFIX = "${";
    final String VARIABLE_PREFIX_FOR_REPLACE = "\\$\\{";
    final String VARIABLE_SUFFIX = "}";
    final String VARIABLE_SUFFIX_FOR_REPLACE = "\\}";

    protected String replaceVariable(String oriContent) {

        if (TextUtils.isEmpty(oriContent))
            return oriContent;
        List<String> variables = new ArrayList<>();
        int prefixPos, suffixPos, offsetPos = 0;
        do {
            prefixPos = oriContent.indexOf(VARIABLE_PREFIX, offsetPos);
            if (prefixPos < 0)
                break;
            suffixPos = oriContent.indexOf(VARIABLE_SUFFIX, prefixPos + VARIABLE_PREFIX.length());
            if (suffixPos < 0)
                break;
            String var = oriContent.substring(prefixPos + VARIABLE_PREFIX.length(), suffixPos);
            offsetPos = suffixPos + VARIABLE_SUFFIX.length();
            if (variables.contains(var))
                continue;
            variables.add(var);
        } while (offsetPos < oriContent.length());

        if (variables.size() == 0)
            return oriContent;
        Map<String, String> xmlProperties = ConfigureManager.getInstance(EposApplication.getAppContext())
                .getXmlProperties();
        String result = oriContent;
        for (String variable :
                variables) {
            result = result.replaceAll(VARIABLE_PREFIX_FOR_REPLACE + variable + VARIABLE_SUFFIX_FOR_REPLACE,
                    xmlProperties.get(variable));
        }
        return result;
    }

    private String filteVariable(String content) {
        if (TextUtils.isEmpty(content))
            return "";
        if (content.length() <= (VARIABLE_PREFIX.length() + VARIABLE_SUFFIX.length()))
            return "";
        return content.substring(VARIABLE_PREFIX.length(), content.length() - VARIABLE_SUFFIX.length());
    }

}
