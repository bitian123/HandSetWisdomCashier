package com.centerm.epos.xml.handler;

import com.centerm.epos.xml.bean.menu.Menu;
import com.centerm.epos.xml.bean.menu.MenuItem;

import org.apache.http.util.TextUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Stack;

/**
 * author:wanliang527</br>
 * date:2016/10/25</br>
 */

public class MenuHandler extends BaseHandler {

    private Menu rootMenu;
    private Stack<MenuItem> itemStack = new Stack<>();
    private Stack<String> tagStack = new Stack<>();

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        itemStack = new Stack<>();
        tagStack = new Stack<>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        String chnTag = attributes.getValue(xmlAttrs.chnTag);
        String enTag = attributes.getValue(xmlAttrs.enTag);
        String iconResName = attributes.getValue(xmlAttrs.iconRes);
        String textResName = attributes.getValue(xmlAttrs.textRes);
        String isShow = attributes.getValue(xmlAttrs.isShow);
        String viewStructure = attributes.getValue(xmlAttrs.structure);
        String process = attributes.getValue(xmlAttrs.process);
        String code = attributes.getValue(xmlAttrs.code);
        String topView = attributes.getValue(xmlAttrs.topView);
        String style = attributes.getValue(xmlAttrs.itemStyle);

        MenuItem item;
        if (xmlTag.Menu.equals(localName)) {
            item = new Menu();
        } else if (xmlTag.MenuItem.equals(localName)) {
            item = new MenuItem();
        } else {
            throw new SAXException("The tag[" + localName + "] is illegal, must be " + xmlTag.Menu + " or " + xmlTag.MenuItem);
        }
        if (tagStack.size() == 0) {
            //第一个节点，判断是否是menu节点
            if (!xmlTag.Menu.equals(localName)) {
                throw new SAXException("The first tag is illegal, must be " + xmlTag.Menu + ".");
            } else {
                rootMenu = (Menu) item;
            }
        }
        item.setChnTag(chnTag);
        item.setEnTag(enTag);
        item.setIconResName(TextUtils.isEmpty(iconResName) ? ("ic_menu_" + enTag.toLowerCase()) : iconResName);
        item.setTextResName(TextUtils.isEmpty(textResName) ? ("menu_" + enTag.toLowerCase()) : textResName);
        item.setShow("false".equalsIgnoreCase(isShow) ? false : true);
//        item.setProcessFile(TextUtils.isEmpty(process) ? (enTag + ".xml") : process);
        item.setProcessFile(process);
        item.setTransCode(TextUtils.isEmpty(code) ? enTag : code);
        if ("TOGGLE".equalsIgnoreCase(style)) {
            item.setViewStyle(MenuItem.Style.TOGGLE);
        } else {
            item.setViewStyle(MenuItem.Style.DEF);
        }
        if (item instanceof Menu) {
            Menu menuItem = (Menu) item;
            menuItem.setStructure("LIST".equalsIgnoreCase(viewStructure) ? Menu.ViewStructure.LIST : Menu.ViewStructure.GRID);
            if ("TITLE".equalsIgnoreCase(topView)) {
                menuItem.setTopType(Menu.TopViewType.TITLE);
            } else if ("BANNER".equalsIgnoreCase(topView)) {
                menuItem.setTopType(Menu.TopViewType.BANNER);
            } else if ("MIX".equalsIgnoreCase(topView)) {
                menuItem.setTopType(Menu.TopViewType.MIX);
            } else if ("NONE".equalsIgnoreCase(topView)) {
                menuItem.setTopType(Menu.TopViewType.NONE);
            } else {
                menuItem.setTopType(Menu.TopViewType.TITLE);
            }
        }
        itemStack.push(item);
        tagStack.push(localName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        MenuItem item = itemStack.pop();
//        logger.info("endElement==>" + localName + "==>" + item.getChnTag());
        if (itemStack.size() > 0) {
            MenuItem temp = itemStack.peek();
            boolean check = temp instanceof Menu;
            if (check) {
                item.setHasParent(true);
                ((Menu) temp).add(item);
            }
        }
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        itemStack = null;
        tagStack = null;
//        logger.warn(rootMenu);
    }


    public Menu getMenu() {
        return rootMenu;
    }
}
