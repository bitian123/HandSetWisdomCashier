package com.centerm.epos.print.receipt;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;


/**
 * author:wanliang527</br>
 * date:2017/3/6</br>
 */
public class TextElement implements IReceiptElement {


    private String content = "";
    //    private Typeface typeface = Typeface.DEFAULT;
    private FontSize size = FontSize.MEDIUM;
    private Paint.Align align = Paint.Align.LEFT;
    private boolean isBold = false;
    private boolean isUnderline = false;
    //    private TextPaint paint;
    private GreyLevel grey = GreyLevel.LEVEL2;

    public TextElement(String content) {
        this.content = content;
    }

    public TextElement(String content, FontSize size, Paint.Align align) {
        this.content = content;
        this.size = size;
        this.align = align;
    }

    public String getContent() {
        return content;
    }

    public TextElement setContent(String content) {
        if (TextUtils.isEmpty(content)) {
            this.content = "";
        } else {
            this.content = content;
        }
        return this;
    }

    public FontSize getSize() {
        return size;
    }

    public TextElement setSize(FontSize size) {
        if (size == null) {
            this.size = FontSize.MEDIUM;
        } else {
            this.size = size;
        }
        return this;
    }

    public Paint.Align getAlign() {
        return align;
    }

    public TextElement setAlign(Paint.Align align) {
        if (align == null) {
            this.align = Paint.Align.LEFT;
        } else {
            this.align = align;
        }
        return this;
    }

    public boolean isBold() {
        return isBold;
    }

    public TextElement setBold(boolean bold) {
        isBold = bold;
        return this;
    }

    public boolean isUnderline() {
        return isUnderline;
    }

    public TextElement setUnderline(boolean underline) {
        isUnderline = underline;
        return this;
    }

    @Override
    public String toString() {
        return "TextItem{" +
                "content='" + content + '\'' +
                ", size=" + size +
                ", align=" + align +
                '}';
    }

   /* private void initMeasurePaint() {
        if (paint == null) {
            paint = new TextPaint();
            paint.setAntiAlias(true);
            paint.setTypeface(typeface);
        }
        paint.setTextSize(size.getValue());
    }*/


/*    @Override
    public int getWidth() {
        initMeasurePaint();
        return (int) paint.measureText(content);
    }

    @Override
    public int getHeight() {
        initMeasurePaint();
        Paint.FontMetrics fm = paint.getFontMetrics();
        return (int) (Math.abs(fm.ascent) + Math.abs(fm.descent));
    }*/

    public GreyLevel getGrey() {
        return grey;
    }

    public void setGrey(GreyLevel grey) {
        this.grey = grey;
    }

    public enum GreyLevel {
        LEVEL1(0xFF000000),
        LEVEL2(0xCC000000),
        LEVEL3(0x99000000),
        LEVEL4(0x55000000);

        private int color;

        private GreyLevel(int color) {
            this.color = color;
        }

        public int getColor() {
            return color;
        }
    }


}
