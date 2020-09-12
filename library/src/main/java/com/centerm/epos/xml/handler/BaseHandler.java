package com.centerm.epos.xml.handler;


import com.centerm.epos.xml.keys.XmlAttrs;
import com.centerm.epos.xml.keys.XmlTag;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Stack;

/**
 * author:wanliang527</br>
 * date:2016/10/18</br>
 */
public class BaseHandler extends DefaultHandler {
    protected Logger logger = Logger.getLogger(this.getClass());
    StringBuilder sBuilder;
    Stack<String> tagStack;
    XmlTag xmlTag = XmlTag.obj();
    XmlAttrs xmlAttrs = XmlAttrs.obj();

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        sBuilder = new StringBuilder();
        tagStack = new Stack<>();
    }

    void throwRootTagIllegalException(String correctTag) throws SAXException {
        throw new SAXException("The first tag is illegal, must be " + correctTag + ".");
    }

    void throwAttrUndefineException(String attr) throws SAXException {
        throw new SAXException("The attribute \"" + attr + "\" must define in XML file, and the value cannot be empty. Please check!");
    }

}
