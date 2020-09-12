package com.centerm.epos.xml.bean.slip;


import android.graphics.Bitmap;
import android.text.TextUtils;

import com.centerm.cpay.midsdk.dev.define.printer.PrinterDataItem;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 签购单元素实体类
 * author:wanliang527</br>
 * date:2017/2/15</br>
 */
@DatabaseTable(tableName = "tb_slip_template")
public class SlipElement {
    @DatabaseField
    private int version;

    @DatabaseField(id = true)
    private String tag;

    @DatabaseField
    private String label;

    @DatabaseField
    private String enLabel;

    @DatabaseField
    private int intAlign = -1;
//    private PrinterDataItem.Align align;

    @DatabaseField
    private String defValue;

    @DatabaseField
    private boolean enable = true;

    @DatabaseField
    private int intFontSize = -1;
//    private FontSize font;

    @DatabaseField
    private boolean isBold = false;

    @DatabaseField
    private boolean isPrintNull = true;

    @DatabaseField
    private boolean isWrapValue = false;

    @DatabaseField
    private int intValueFontSize = -1;
//    private FontSize valueFont;

    @DatabaseField
    private int intValueAlign = -1;
//    private PrinterDataItem.Align valueAlign;

    @DatabaseField
    private boolean isValueBold = false;

    @DatabaseField
    private int intType = -1;
//    private Type type;

    @DatabaseField
    private int intBelongs = -1;
//    private Belongs belongs;

    @DatabaseField
    private int intConditon = -1;

    private Bitmap bitmap;
    @DatabaseField
    private String value;

    @DatabaseField
    private int intSource = -1;
//    private Source source;


    public SlipElement() {
    }

    public SlipElement(String tag) {
        this.tag = tag;
    }

    public SlipElement(String tag, String label, String value) {
        this.tag = tag;
        this.label = label;
        this.value = value;
        if (TextUtils.isEmpty(label)) {
            setSource(Source.CONSTANT);
        } else {
            setSource(Source.VARIABLE);
        }
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getEnLabel() {
        return enLabel;
    }

    public void setEnLabel(String enLabel) {
        this.enLabel = enLabel;
    }

    public PrinterDataItem.Align getAlign() {
        if (intAlign < 0) {
            return PrinterDataItem.Align.LEFT;
        }
        if (intAlign < PrinterDataItem.Align.values().length) {
            return PrinterDataItem.Align.values()[intAlign];
        }
        return PrinterDataItem.Align.LEFT;
    }

    public void setAlign(PrinterDataItem.Align align) {
        if (align == null) {
            align = PrinterDataItem.Align.LEFT;
        }
//        this.align = align;
        this.intAlign = align.ordinal();
    }

    public String getDefValue() {
        return defValue;
    }

    public void setDefValue(String defValue) {
        this.defValue = defValue;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isPrintNull() {
        return isPrintNull;
    }

    public void setPrintNull(boolean printNull) {
        isPrintNull = printNull;
    }

    public FontSize getFont() {
        if (intFontSize < 0) {
            return FontSize.MEDIUM;
        }
        if (intFontSize < FontSize.values().length) {
            return FontSize.values()[intFontSize];
        }
        return FontSize.MEDIUM;
    }

    public void setFont(FontSize font) {
        if (font == null) {
            font = FontSize.MEDIUM;
        }
//        this.font = font;
        this.intFontSize = font.ordinal();
    }

    public boolean isBold() {
        return isBold;
    }

    public void setBold(boolean bold) {
        isBold = bold;
    }

    public boolean isWrapValue() {
        return isWrapValue;
    }

    public void setWrapValue(boolean wrapValue) {
        isWrapValue = wrapValue;
    }

    public FontSize getValueFont() {
        if (intValueFontSize < 0) {
            return FontSize.MEDIUM;
        }
        if (intValueFontSize < FontSize.values().length) {
            return FontSize.values()[intValueFontSize];
        }
        return FontSize.MEDIUM;
    }

    public void setValueFont(FontSize valueFont) {
        if (valueFont == null) {
            valueFont = FontSize.MEDIUM;
        }
//        this.valueFont = valueFont;
        this.intValueFontSize = valueFont.ordinal();
    }

    public PrinterDataItem.Align getValueAlign() {
        if (intValueAlign < 0) {
            return PrinterDataItem.Align.LEFT;
        }
        if (intValueAlign < PrinterDataItem.Align.values().length) {
            return PrinterDataItem.Align.values()[intValueAlign];
        }
        return PrinterDataItem.Align.LEFT;
    }

    public void setValueAlign(PrinterDataItem.Align valueAlign) {
        if (valueAlign == null) {
            valueAlign = PrinterDataItem.Align.LEFT;
        }
//        this.valueAlign = valueAlign;
        this.intValueAlign = valueAlign.ordinal();
    }

    public Type getType() {
        if (intType < 0) {
            return Type.TEXT;
        }
        if (intType < Type.values().length) {
            return Type.values()[intType];
        }
        return Type.TEXT;
    }

    public void setType(Type type) {
        if (type == null) {
            type = Type.TEXT;
        }
//        this.type = type;
        this.intType = type.ordinal();
    }

    public Belongs getBelongs() {
        if (intBelongs < 0) {
            return Belongs.BOTH;
        }
        if (intBelongs < Belongs.values().length) {
            return Belongs.values()[intBelongs];
        }
        return Belongs.BOTH;
    }

    public void setBelongs(Belongs belongs) {
        if (belongs == null) {
            belongs = Belongs.BOTH;
        }
//        this.belongs = belongs;
        this.intBelongs = belongs.ordinal();
    }

    public Condition getCondition(){
        if (intConditon < 0 || intConditon >= Condition.values().length)
            return null;
        return Condition.values()[intConditon];
    }

    public void setCondition(Condition condition){
        if (condition == null) {
            intConditon = -1;
            return;
        }
        intConditon = condition.ordinal();
    }

    public Source getSource() {
        if (intSource < 0) {
            return Source.VARIABLE;
        }
        if (intSource < Source.values().length) {
            return Source.values()[intSource];
        }
        return Source.VARIABLE;
    }

    public void setSource(Source source) {
        if (source == null) {
            source = Source.VARIABLE;
        }
        this.intSource = source.ordinal();
    }


    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isValueBold() {
        return isValueBold;
    }

    public void setValueBold(boolean valueBold) {
        isValueBold = valueBold;
    }

    public enum FontSize {
        SMALL, MEDIUM, LARGE
    }

    public enum Type {
        TEXT, BARCODE, PIC
    }

    public enum Belongs {
        MERCHANT, CARD_HOLDER, BOTH
    }

    public enum Source {
        CONSTANT, VARIABLE, CLASS
    }

    public enum Condition{
        IC_TRADE, RE_PRINT
    }

    @Override
    public String toString() {
        return "SlipElement{" +
                "version=" + version +
                ", tag='" + tag + '\'' +
                ", label='" + label + '\'' +
                ", enLabel='" + enLabel + '\'' +
                ", align=" + intAlign +
                ", defValue='" + defValue + '\'' +
                ", enable=" + enable +
                ", font=" + intFontSize +
                ", isBold=" + isBold +
                ", isWrapValue=" + isWrapValue +
                ", valueFont=" + intValueFontSize +
                ", valueAlign=" + intValueAlign +
                ", type=" + intType +
                ", belongs=" + intBelongs +
                ", printNull=" + isPrintNull +
                '}';
    }
}
