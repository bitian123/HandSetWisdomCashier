package com.centerm.epos.print;


import android.content.Context;
import android.text.TextUtils;

import com.centerm.cpay.midsdk.dev.define.printer.PrinterDataItem;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.xml.bean.slip.SlipElement;

import org.apache.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * 签购单模板编辑器
 * author:wanliang527</br>
 * date:2017/2/17</br>
 */
public class TemplateEditor {
    private Logger logger = Logger.getLogger(TemplateEditor.class);

    private Context context;
    private LinkedHashMap<String, SlipElement> eleMap = new LinkedHashMap<>();
    private LinkedList<SlipElement> eleList = new LinkedList<>();

    private boolean invalid;
    private boolean resetFlag;

    TemplateEditor(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }

        CommonDao<SlipElement> dao = new CommonDao<>(SlipElement.class, DbHelper.getInstance());
        List<SlipElement> elements = dao.query();
        DbHelper.releaseInstance();
        if (elements == null || elements.size() < 1) {
            return;
        }
        eleList.addAll(elements);
        for (int i = 0; i < elements.size(); i++) {
            SlipElement ele = elements.get(i);
            eleMap.put(ele.getTag(), ele);
        }
    }

    public TemplateEditor setLabel(String tag, String label) {
        throwIfInvalid();
        if (eleMap.containsKey(tag)) {
            eleMap.get(tag).setLabel(label);
        }
        return this;
    }

    public TemplateEditor setEnLabel(String tag, String enLabel) {
        throwIfInvalid();
        if (eleMap.containsKey(tag)) {
            eleMap.get(tag).setEnLabel(enLabel);
        }
        return this;
    }

    public TemplateEditor setAlign(String tag, PrinterDataItem.Align align) {
        throwIfInvalid();
        if (eleMap.containsKey(tag)) {
            eleMap.get(tag).setAlign(align);
        }
        return this;
    }

    public TemplateEditor setEnable(String tag, boolean enable) {
        throwIfInvalid();
        if (eleMap.containsKey(tag)) {
            eleMap.get(tag).setEnable(enable);
        }
        return this;
    }

    public TemplateEditor setFont(String tag, SlipElement.FontSize size) {
        throwIfInvalid();
        if (eleMap.containsKey(tag)) {
            eleMap.get(tag).setFont(size);
        }
        return this;
    }

    public TemplateEditor setBold(String tag, boolean bold) {
        throwIfInvalid();
        if (eleMap.containsKey(tag)) {
            eleMap.get(tag).setBold(bold);
        }
        return this;
    }

    public TemplateEditor delete(String tag) {
        throwIfInvalid();
        if (eleMap.containsKey(tag)) {
            SlipElement ele = eleMap.remove(tag);
            eleList.remove(ele);
            resetFlag = true;
        }
        return this;
    }

    /**
     * 添加打印元素到签购单最前面
     *
     * @param ele 元素对象
     * @return 本对象
     */
    public TemplateEditor addFront(SlipElement ele) {
        throwIfInvalid();
        if (ele == null || TextUtils.isEmpty(ele.getTag())) {
            logger.warn("Add slip element failed! The object is illegal");
        } else if (eleMap.containsKey(ele.getTag())) {
            logger.warn("Add slip element failed! Tag \"" + ele.getTag() + "\" is already existed");
        } else {
            eleList.addLast(ele);
            eleMap.put(ele.getTag(), ele);
            resetFlag = true;
        }
        return this;
    }

    /**
     * 添加打印元素到签购单的最后面
     *
     * @param ele 签购单元素
     * @return 本对象
     */
    public TemplateEditor addLast(SlipElement ele) {
        throwIfInvalid();
        if (ele == null || TextUtils.isEmpty(ele.getTag())) {
            logger.warn("Add slip element failed! The object is illegal");
        } else if (eleMap.containsKey(ele.getTag())) {
            logger.warn("Add slip element failed! Tag \"" + ele.getTag() + "\" is already existed");
        } else {
            eleList.addLast(ele);
            eleMap.put(ele.getTag(), ele);
            resetFlag = true;
        }
        return this;
    }

    /**
     * 添加打印元素到指定位置
     *
     * @param index 位置索引
     * @param ele 签购单元素
     * @return 本对象
     */
    public TemplateEditor add(int index, SlipElement ele) {
        throwIfInvalid();
        if (ele == null || TextUtils.isEmpty(ele.getTag())) {
            logger.warn("Add slip element failed! The object is illegal");
        } else if (eleMap.containsKey(ele.getTag())) {
            logger.warn("Add slip element failed! Tag \"" + ele.getTag() + "\" is already existed");
        } else {
            eleList.add(index, ele);
            eleMap.put(ele.getTag(), ele);
            resetFlag = true;
        }
        return this;
    }

    /**
     * 添加打印元素到指定元素的前面
     *
     * @param aheadTag 指定元素的tag
     * @param ele 签购单元素
     * @return 本对象
     */
    public TemplateEditor addElementAhead(String aheadTag, SlipElement ele) {
        throwIfInvalid();
        if (ele == null || TextUtils.isEmpty(ele.getTag())) {
            logger.warn("Add slip element failed! The object is illegal");
        } else if (eleMap.containsKey(ele.getTag())) {
            logger.warn("Add slip element failed! Tag \"" + ele.getTag() + "\" is already existed");
        } else {
            SlipElement aheadEle = eleMap.get(aheadTag);
            if (aheadEle == null) {
                logger.warn("Add slip element failed! Ahead tag \"" + aheadTag + "\" isn;t existed");
            } else {
                eleList.add(eleList.indexOf(aheadEle), ele);
                eleMap.put(ele.getTag(), ele);
                resetFlag = true;
            }
        }
        return this;
    }

    private void throwIfInvalid() {
        if (invalid) {
            throw new IllegalStateException("Editor will be invalid after commit() or apply(), cannot edit anymore!");
        }
    }

    /**
     * 提交签购单模板的更改
     */
    public void commit() {
        CommonDao<SlipElement> dao = new CommonDao<>(SlipElement.class, DbHelper.getInstance());
        dao.deleteBySQL("DELETE FROM tb_slip_template");
        boolean r = dao.save(eleList);
        DbHelper.releaseInstance();
        invalid = true;
        eleList.clear();
        eleMap.clear();
        logger.warn("Save result is " + r);
    }

    public void apply() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                commit();
            }
        }).start();
    }
}
