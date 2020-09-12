package com.centerm.epos.print.receipt;

/**
 * author:wanliang527</br>
 * date:2017/3/7</br>
 */

public class TextGroupElement implements IReceiptElement {

    private final static int MIN_SPACING = 20;

    private TextElement leftItem;
    private TextElement rightItem;
    private int spacing = MIN_SPACING;
    private boolean isSingleLine = true;


    public TextGroupElement(TextElement leftItem, TextElement rightItem) {
        if (leftItem != null) {
//            leftItem.setAlign(Paint.Align.LEFT);
        }
        if (rightItem != null) {
//            rightItem.setAlign(Paint.Align.RIGHT);
        }
        this.leftItem = leftItem;
        this.rightItem = rightItem;
    }

    public TextElement getLeftItem() {
        return leftItem;
    }

    public void setLeftItem(TextElement leftItem) {
        this.leftItem = leftItem;
        if (leftItem != null) {
//            leftItem.setAlign(Paint.Align.LEFT);
        }
    }

    public TextElement getRightItem() {
        return rightItem;
    }

    public void setRightItem(TextElement rightItem) {
        this.rightItem = rightItem;
        if (rightItem != null) {
//            rightItem.setAlign(Paint.Align.RIGHT);
        }
    }

    public int getSpacing() {
        return spacing;
    }

    public void setSpacing(int spacing) {
        this.spacing = spacing;
    }

    public boolean isSingleLine() {
        return isSingleLine;
    }

    public void setSingleLine(boolean singleLine) {
        isSingleLine = singleLine;
    }
}
