package com.centerm.epos.print.receipt;

import android.graphics.Bitmap;
import android.graphics.Paint;

/**
 * author:wanliang527</br>
 * date:2017/3/6</br>
 */

public class PicElement implements IReceiptElement {

    private String filePath;//图片文件路径
    private int resId;//资源ID
    private Bitmap bmp;//Bitmap数据

    private int width;
    private int height;
    private Paint.Align align = Paint.Align.CENTER;

    public PicElement(int resId) {
        this.resId = resId;
    }

    public PicElement(String filePath) {
        this.filePath = filePath;
    }

    public PicElement(Bitmap bmp) {
        this.bmp = bmp;
        if (bmp != null && !bmp.isRecycled()) {
            this.width = bmp.getWidth();
            this.height = bmp.getHeight();
        }
    }

    public PicElement(String filePath, int width, int height) {
        this.filePath = filePath;
        this.width = width;
        this.height = height;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public Bitmap getBmp() {
        return bmp;
    }

    public void setBmp(Bitmap bmp) {
        this.bmp = bmp;
    }

    public Paint.Align getAlign() {
        return align;
    }

    public void setAlign(Paint.Align align) {
        this.align = align;
    }


}
