package com.centerm.epos.print;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.centerm.cloudsys.sdk.common.utils.FileUtils;
import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.define.IPrinterDev;
import com.centerm.cpay.midsdk.dev.define.printer.EnumBarcodeType;
import com.centerm.cpay.midsdk.dev.define.printer.PrintListener;
import com.centerm.cpay.midsdk.dev.define.printer.PrinterDataItem;
import com.centerm.cpay.midsdk.dev.define.printer.task.BarcodeTask;
import com.centerm.cpay.midsdk.dev.define.printer.task.BitmapTask;
import com.centerm.cpay.midsdk.dev.define.printer.task.PrintTask;
import com.centerm.cpay.midsdk.dev.define.printer.task.StringTask;
import com.centerm.epos.EposApplication;
import com.centerm.epos.xml.bean.slip.SlipElement;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;

/**
 * 打印代理类。
 * author:wanliang527</br>
 * date:2017/2/16</br>
 */
public class PrinterProxy {

    private Logger logger = Logger.getLogger(PrinterProxy.class);

    private final static int SMALL_FONT_LINE_HEIGHT = 26;
    private String transCode;
    private Context context;
    private PrintManager.SlipOwner owner;
    private LinkedHashMap<String, SlipElement> eleMap = new LinkedHashMap<>();
    private LinkedList<SlipElement> eleList = new LinkedList<>();
    private boolean isReprint = false;//是否是重打印
    private boolean isICTrade = false;  //是否是IC卡交易
    private boolean isPrintEnglishLable;    //是否打印英文内容
    private PrintManager.StatusInterpolator interpolator;//状态拦截器，用于向上层传递打印状态

    PrinterProxy(Context context, PrintManager.SlipOwner owner, List<SlipElement> elements) {
        this.context = context;
        this.owner = owner;
        if (elements != null) {
            for (int i = 0; i < elements.size(); i++) {
                SlipElement ele = elements.get(i);
                eleMap.put(ele.getTag(), ele);
                eleList.add(ele);
            }
        }
        isPrintEnglishLable = BusinessConfig.getInstance().getToggle(EposApplication.getAppContext(),
                BusinessConfig.Key.TOGGLE_SLIP_ENGLISH);

    }

    public PrinterProxy setReprint(boolean reprint) {
        isReprint = reprint;
        return this;
    }

    public PrinterProxy setICTrade(boolean ICTrade) {
        isICTrade = ICTrade;
        return this;
    }

    public PrinterProxy setTransCode(String transCode) {
        this.transCode = transCode;
//        if(!TextUtils.isEmpty(transCode)){
//            if(transCode.contains("SCAN")){
//                isPrintEnglishLable = false;
//            }else {
//                isPrintEnglishLable = true;
//            }
//        }
        return this;
    }

    /**
     * 根据打印模板，给各个打印元素赋值。
     *
     * @param map 数据集合，各个元素的键值需要和配置文件中的保持一致
     * @return 本对象
     */
    public PrinterProxy setValue(Map<String, String> map) {
        if (map != null && map.size() > 0) {
            int sizeA = eleMap.size();
            int sizeB = map.size();
            if (sizeA < sizeB) {
                for (Iterator<Map.Entry<String, SlipElement>> iterator = eleMap.entrySet().iterator(); iterator
                        .hasNext(); ) {
                    Map.Entry<String, SlipElement> entry = iterator.next();
                    String key = entry.getKey();
                    SlipElement ele = entry.getValue();
                    if (map.get(key) != null)
                        ele.setValue(map.get(key));
                }
            } else {
                for (Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext(); ) {
                    Map.Entry<String, String> entry = iterator.next();
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (value != null && eleMap.get(key) != null)
                        eleMap.get(key).setValue(value);
                }
            }
        }
        return this;
    }

    /**
     * 给指定tag的元素进行赋值
     *
     * @param tag 元素tag
     * @param value 需要打印的值
     * @return 本对象
     */
    public PrinterProxy setValue(String tag, String value) {
        if (eleMap.containsKey(tag)) {
            eleMap.get(tag).setValue(value);
        }
        return this;
    }

    /**
     * 设置指定tag元素的标签
     *
     * @param tag 元素tag
     * @param newLabel 新的标签
     * @return 本对象
     */
    public PrinterProxy setLabel(String tag, String newLabel) {
        if (eleMap.containsKey(tag)) {
            eleMap.get(tag).setLabel(newLabel);
        }
        return this;
    }

    /**
     * 设置指定tag元素的对齐方式
     *
     * @param tag 元素tag
     * @param align 对齐方式
     * @return 本对象
     */
    public PrinterProxy setAlign(String tag, PrinterDataItem.Align align) {
        if (eleMap.containsKey(tag)) {
            eleMap.get(tag).setAlign(align);
        }
        return this;
    }

    /**
     * 设置指定tag元素是否进行打印
     *
     * @param tag 元素tag
     * @param enable true为打印，false为不打印
     * @return 本对象
     */
    public PrinterProxy setEnLabel(String tag, boolean enable) {
        if (eleMap.containsKey(tag)) {
            eleMap.get(tag).setEnable(enable);
        }
        return this;
    }

    /**
     * 设置指定tag元素的字号大小
     *
     * @param tag 元素tag
     * @param newSize 新的字号大小
     * @return 本对象
     */
    public PrinterProxy setFontSize(String tag, SlipElement.FontSize newSize) {
        if (eleMap.containsKey(tag)) {
            eleMap.get(tag).setFont(newSize);
        }
        return this;
    }

    /**
     * 设置指定tag元素是否加粗打印
     *
     * @param tag 元素tag
     * @param isBold 是否加粗
     * @return 本对象
     */
    public PrinterProxy setBold(String tag, boolean isBold) {
        if (eleMap.containsKey(tag)) {
            eleMap.get(tag).setBold(isBold);
        }
        return this;
    }

    /**
     * 设置指定tag元素打印输出为图片类型
     *
     * @param tag 元素tag
     * @param bitmap Bitmap
     * @return 本对象
     */
    public PrinterProxy setEleBitmap(String tag, Bitmap bitmap) {
        if (eleMap.containsKey(tag)) {
            SlipElement ele = eleMap.get(tag);
            ele.setType(SlipElement.Type.PIC);
            ele.setBitmap(bitmap);
        }
        return this;
    }

    /**
     * 标识本次打印为重打印
     *
     * @param flag 重打印标识
     * @return 本对象
     */
    public PrinterProxy setReprintFlag(boolean flag) {
        this.isReprint = flag;
        return this;
    }

    /**
     * 添加状态拦截器，用于上层监听打印状态
     *
     * @param interpolator 拦截器
     * @return 本对象
     */
    public PrinterProxy addInterpolator(PrintManager.StatusInterpolator interpolator) {
        this.interpolator = interpolator;
        return this;
    }

    /**
     * 添加打印元素到签购单最前面
     *
     * @param ele 元素对象
     * @return 本对象
     */
    public PrinterProxy addElementAtFront(SlipElement ele) {
        if (ele == null || TextUtils.isEmpty(ele.getTag())) {
            logger.warn("Add slip element failed! The object is illegal");
        } else if (eleMap.containsKey(ele.getTag())) {
            logger.warn("Add slip element failed! Tag \"" + ele.getTag() + "\" is already existed");
        } else {
            eleList.addFirst(ele);
            eleMap.put(ele.getTag(), ele);
        }
        return this;
    }

    /**
     * 添加打印元素到签购单的最后面
     *
     * @param ele 签购单元素
     * @return 本对象
     */
    public PrinterProxy addElementAtLast(SlipElement ele) {
        if (ele == null || TextUtils.isEmpty(ele.getTag())) {
            logger.warn("Add slip element failed! The object is illegal");
        } else if (eleMap.containsKey(ele.getTag())) {
            logger.warn("Add slip element failed! Tag \"" + ele.getTag() + "\" is already existed");
        } else {
            eleList.addLast(ele);
            eleMap.put(ele.getTag(), ele);
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
    public PrinterProxy addElement(int index, SlipElement ele) {
        if (ele == null || TextUtils.isEmpty(ele.getTag())) {
            logger.warn("Add slip element failed! The object is illegal");
        } else if (eleMap.containsKey(ele.getTag())) {
            logger.warn("Add slip element failed! Tag \"" + ele.getTag() + "\" is already existed");
        } else {
            eleList.add(index, ele);
            eleMap.put(ele.getTag(), ele);
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
    public PrinterProxy addElementAhead(String aheadTag, SlipElement ele) {
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
            }
        }
        return this;
    }

    /**
     * 是否支持电子签名，且能读取到图片文件
     */
    private Bitmap getElecSignPic(SlipElement element) {
        if (!element.isEnable() || !element.getType().equals(SlipElement.Type.PIC)) {
            return null;
        }
        if (PrintManager.SlipOwner.MERCHANT.equals(owner) && SlipElement.Belongs.CARD_HOLDER.equals(element
                .getBelongs())) {
            //当前打印的是商户联，持卡人联的特有元素不打印
            return null;
        } else if (PrintManager.SlipOwner.CONSUMER.equals(owner) && SlipElement.Belongs.MERCHANT.equals(element
                .getBelongs())) {
            //当前打印的是持卡人联，商户联的特有元素不打印
            return null;
        }
        if (element.getCondition() != null) {
            switch (element.getCondition()) {
                case IC_TRADE:
                    if (!isICTrade)
                        return null;
                    break;
                case RE_PRINT:
                    if (!isReprint)
                        return null;
                    break;
            }
        }
        String path = element.getValue();
        if (TextUtils.isEmpty(path))
            return null;
        Bitmap bitmap;
        if (FileUtils.getFileSize(path) > 0) {
            logger.debug("签名图片存在");
            try {
                FileInputStream inputStream = new FileInputStream(path);
                bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            logger.debug("签名图片不存在");
        }
        return null;
    }

    /**
     * 开始打印
     */
    public void print() {
        boolean isStringTaskBreak = false;
        PrintTask task = new PrintTask();
        List<PrinterDataItem> itemList = new ArrayList<>();
        Bitmap signBitmap = null;
        task.addTask(new StringTask(itemList));
        for (int i = 0; i < eleList.size(); i++) {
            SlipElement ele = eleList.get(i);
            if (!ele.isEnable()) {
                //该元素屏蔽打印
                continue;
            }
            switch (ele.getType()) {
                case TEXT:
                    if (isStringTaskBreak) {
                        itemList = new ArrayList<>();
                        task.addTask(new StringTask(itemList));
                        isStringTaskBreak = false;
                    }
                    List<PrinterDataItem> thisItem = convert2TextItem(ele);
                    if (thisItem != null) {
                        itemList.addAll(thisItem);
                    }
                    break;
                case BARCODE:
                    task.addTask(new BarcodeTask(ele.getValue(), EnumBarcodeType.CODE128));
                    isStringTaskBreak = true;
                    break;
                case PIC:
//                    task.addTask(new BitmapTask(ele.getBitmap(), 0, 0, 0));
                    Bitmap bitmap = getElecSignPic(ele);
                    if (bitmap != null) {
                        signBitmap = bitmap;
                        task.addTask(new BitmapTask(bitmap, 0, 0, 0));
                        isStringTaskBreak = true;
                    }
                    break;
            }
        }
        if (interpolator != null) {
            interpolator.onPrinting();
        }
        DeviceFactory factory = DeviceFactory.getInstance();
        IPrinterDev dev = null;
        try {
            dev = factory.getPrinterDev();
            dev.print(task, new PrintListener() {
                @Override
                public void onFinish() {
                    if (interpolator != null) {
                        interpolator.onFinish();
                    }
                }

                @Override
                public void onError(int i, String s) {
                    if (interpolator != null) {
                        interpolator.onError(i, s);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (dev == null && interpolator != null) {
            interpolator.onError(-1, "打印机异常，请重试");
        }
    }

    private List<PrinterDataItem> convert2TextItem(SlipElement element) {
        if (!element.isEnable() || !element.getType().equals(SlipElement.Type.TEXT)) {
            return null;
        }
        if (PrintManager.SlipOwner.MERCHANT.equals(owner) && SlipElement.Belongs.CARD_HOLDER.equals(element
                .getBelongs())) {
            //当前打印的是商户联，持卡人联的特有元素不打印
            return null;
        } else if (PrintManager.SlipOwner.CONSUMER.equals(owner) && SlipElement.Belongs.MERCHANT.equals(element
                .getBelongs())) {
            //当前打印的是持卡人联，商户联的特有元素不打印
            return null;
        }
        if (element.getCondition() != null) {
            switch (element.getCondition()) {
                case IC_TRADE:
                    if (!isICTrade)
                        return null;
                    break;
                case RE_PRINT:
                    if (!isReprint)
                        return null;
                    break;
            }
        }
        if (!element.isPrintNull() && TextUtils.isEmpty(element.getValue())) {
            return null;
        }
        List<PrinterDataItem> itemList = new ArrayList<>();
        if (element.isWrapValue()) {
            //如果标签和值分两行显示
            StringBuilder builder = new StringBuilder();
            if (!TextUtils.isEmpty(element.getLabel())) {
                builder.append(element.getLabel());
                if (isPrintEnglishLable && !TextUtils.isEmpty(element.getEnLabel())) {
                    builder.append("(")
                            .append(element.getEnLabel())
                            .append("):");
                } else {
                    builder.append(":");
                }
            }
            PrinterDataItem labelItem = new PrinterDataItem(builder.toString());
            switch (element.getFont()) {
                case SMALL:
                    labelItem.setFontSize(PrinterDataItem.FONT_SIZE_SMALL);
                    labelItem.setLineHeight(SMALL_FONT_LINE_HEIGHT);
                    break;
                case LARGE:
                    labelItem.setFontSize(PrinterDataItem.FONT_SIZE_HEIGHT_LARGE);
                    break;
//                default:
//                    labelItem.setFontSize(PrinterDataItem.FONT_SIZE_LARGE);
//                    break;
            }
            labelItem.setAlign(element.getAlign());
            labelItem.setBold(element.isBold());
            itemList.add(labelItem);

            String value = element.getValue();
            PrinterDataItem valueItem = new PrinterDataItem(value);
            switch (element.getValueFont()) {
                case SMALL:
                    valueItem.setFontSize(PrinterDataItem.FONT_SIZE_SMALL);
                    valueItem.setLineHeight(SMALL_FONT_LINE_HEIGHT);
                    break;
                case LARGE:
                    valueItem.setFontSize(PrinterDataItem.FONT_SIZE_HEIGHT_LARGE);
                    break;
            }
            valueItem.setAlign(element.getValueAlign());
            valueItem.setBold(element.isValueBold());
            itemList.add(valueItem);
        } else {
            StringBuilder builder = new StringBuilder();
            if (!TextUtils.isEmpty(element.getLabel())) {
                builder.append(element.getLabel());
                if (isPrintEnglishLable && !TextUtils.isEmpty(element.getEnLabel())) {
                    builder.append("(")
                            .append(element.getEnLabel() == null ? "" : element.getEnLabel())
                            .append("):");
                } else {
                    builder.append(":");
                }
            }
            String value = element.getValue();
            builder.append(value == null ? "" : value);
            PrinterDataItem item = new PrinterDataItem(builder.toString());
            item.setAlign(element.getAlign());
            switch (element.getFont()) {
                case SMALL:
                    item.setFontSize(PrinterDataItem.FONT_SIZE_SMALL);
                    item.setLineHeight(SMALL_FONT_LINE_HEIGHT);
                    break;
                case LARGE:
                    item.setFontSize(PrinterDataItem.FONT_SIZE_HEIGHT_LARGE);
                    break;
            }
            item.setBold(element.isBold());
            itemList.add(item);
        }
        return itemList;
    }

}
