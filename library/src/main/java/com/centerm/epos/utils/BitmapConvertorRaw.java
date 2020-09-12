package com.centerm.epos.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Created by zhouzhihua on 2017/11/30.
 */

public class BitmapConvertorRaw{

    private BitmapConvertorRaw() {
    }

    public static Bitmap createBmp(Bitmap bitmap, int maxSize) {
        double width = (double) bitmap.getWidth();
        double height = (double) bitmap.getHeight();
        if (width > height) {
            if ((double) maxSize > width) {
                maxSize = (int) width;
            }
            height *= (double) maxSize / width;
            width = (double) maxSize;
        } else {
            if ((double) maxSize > height) {
                maxSize = (int) height;
            }
            width *= (double) maxSize / height;
            height = (double) maxSize;
        }
        return Bitmap.createScaledBitmap(bitmap, (int) width, (int) height, true);
    }

    /*@author:zhouzhihua
    * 生成一张BMP格式的图片
    * @return BMP 图片字节流
    * */
    public static byte[] convertBitmap(Bitmap inputBitmap) {
        int width = inputBitmap.getWidth();
        int height = inputBitmap.getHeight();
        int mDataWidth = (width + 31) / 32 * 4 * 8;
        byte[] mDataArray = new byte[mDataWidth * height];
        byte[] mRawBitmapData = new byte[mDataWidth * height / 8];
        int k = 0;

        int rawIndex;
        int columnIndex;
        int file;
        try {
            for(rawIndex = 0; rawIndex < height; ++rawIndex) {
                for(columnIndex = 0; columnIndex < width; ++k) {
                    int bmpFile = inputBitmap.getPixel(columnIndex, rawIndex);
                    file = Color.red(bmpFile);
                    int fileOutputStream = Color.green(bmpFile);
                    int var17 = Color.blue(bmpFile);
                    file = (int)(0.299D * (double)file + 0.587D * (double)fileOutputStream + 0.114D * (double)var17);
                    if(file < 128) {
                        mDataArray[k] = 0;
                    } else {
                        mDataArray[k] = 1;
                    }
                    ++columnIndex;
                }
                if(mDataWidth > width) {
                    for(columnIndex = width; columnIndex < mDataWidth; ++k) {
                        mDataArray[k] = 1;
                        ++columnIndex;
                    }
                }
            }
        } catch (Exception var15) {
            return null;
        }
        rawIndex = 0;
        for(columnIndex = 0; columnIndex < mDataArray.length; columnIndex += 8) {
            byte var16 = mDataArray[columnIndex];
            for(file = 0; file < 7; ++file) {
                var16 = (byte)(var16 << 1 | mDataArray[columnIndex + file]);
            }
            mRawBitmapData[rawIndex] = var16;
            ++rawIndex;
        }
        ByteArrayOutputStream mByteArrayOutputStream = new ByteArrayOutputStream();
        new BitmapConvertorRaw.BMPStream().saveBitmap(mByteArrayOutputStream, mRawBitmapData, width, height);
        return mByteArrayOutputStream.toByteArray();
    }

    public static byte[] bitmapToBmp(Bitmap bitmap, int maxSize) {
        Bitmap mBitmap = null;
        byte[] mByte = null;
        mBitmap = createBmp( bitmap , maxSize);
        if( null == mBitmap ){
            return null;
        }

        mByte = convertBitmap(mBitmap);

        if(!mBitmap.isRecycled()){
            mBitmap.recycle();
        }
        return mByte;
    }
    public static boolean bitmapToBmpInFile(Bitmap bitmap, int maxSize , String path)
    {
        if( null == bitmap || maxSize <= 0 || null == path ){
            return false;
        }
        byte[] mByteArray = bitmapToBmp( bitmap , maxSize);

        if( null == mByteArray ){
            return false;
        }

        File mFile = new File(path);
        if(mFile.exists()){
            mFile.delete();
        }
        FileOutputStream fileOutputStream = null;

        try {
            mFile.createNewFile();
            fileOutputStream = new FileOutputStream(mFile);
            fileOutputStream.write(mByteArray);
            fileOutputStream.flush();/*该接口未实现*/
            fileOutputStream.getFD().sync();/*将缓存数据立刻保存到文件中*/
            fileOutputStream.close();
        } catch (IOException var14) {
            return false;
        }
        return true;
    }

    static class BMPStream {
        private final int BITMAPFILEHEADER_SIZE = 14;
        private final int BITMAPINFOHEADER_SIZE = 40;
        private byte[] bfType = new byte[]{66, 77};
        private int bfSize = 0;
        private int bfReserved1 = 0;
        private int bfReserved2 = 0;
        private int bfOffBits = 62;
        private int biSize = 40;
        private int biWidth = 0;
        private int biHeight = 0;
        private int biPlanes = 1;
        private int biBitCount = 1;
        private int biCompression = 0;
        private int biSizeImage = 0;
        private int biXPelsPerMeter = 0;
        private int biYPelsPerMeter = 0;
        private int biClrUsed = 0;
        private int biClrImportant = 0;
        private byte[] bitmap;
        int scanLineSize = 0;
        private byte[] colorPalette = new byte[]{0, 0, 0, 0, 0, -1, -1, -1};

        public BMPStream() {
        }

        public void saveBitmap(ByteArrayOutputStream fos, byte[] imagePix, int parWidth, int parHeight) {
            try {
                this.save(fos, imagePix, parWidth, parHeight);
            } catch (Exception var6) {
                var6.printStackTrace();
            }

        }

        private void save(ByteArrayOutputStream fos, byte[] imagePix, int parWidth, int parHeight) {
            try {
                this.convertImage(imagePix, parWidth, parHeight);
                this.writeBitmapFileHeader(fos);
                this.writeBitmapInfoHeader(fos);
                this.writePixelArray(fos);
            } catch (Exception var6) {
                var6.printStackTrace();
            }

        }

        private boolean convertImage(byte[] imagePix, int parWidth, int parHeight) {
            this.bitmap = imagePix;
            this.bfSize = 62 + (parWidth + 31) / 32 * 4 * parHeight;
            this.biWidth = parWidth;
            this.biHeight = parHeight;
            this.scanLineSize = ((parWidth * this.biBitCount + 31)>>5)<<2;

            //this.biSizeImage = scanLineSize*biHeight;
            return true;
        }

        private void writeBitmapFileHeader(ByteArrayOutputStream fos) {
            try {
                fos.write(this.bfType);
                fos.write(this.intToDWord(this.bfSize));
                fos.write(this.intToWord(this.bfReserved1));
                fos.write(this.intToWord(this.bfReserved2));
                fos.write(this.intToDWord(this.bfOffBits));
            } catch (Exception var3) {
                var3.printStackTrace();
            }

        }

        private void writeBitmapInfoHeader(ByteArrayOutputStream fos) {
            try {
                fos.write(this.intToDWord(this.biSize));
                fos.write(this.intToDWord(this.biWidth));
                fos.write(this.intToDWord(this.biHeight));
                fos.write(this.intToWord(this.biPlanes));
                fos.write(this.intToWord(this.biBitCount));
                fos.write(this.intToDWord(this.biCompression));
                fos.write(this.intToDWord(this.biSizeImage));
                fos.write(this.intToDWord(this.biXPelsPerMeter));
                fos.write(this.intToDWord(this.biYPelsPerMeter));
                fos.write(this.intToDWord(this.biClrUsed));
                fos.write(this.intToDWord(this.biClrImportant));
                fos.write(this.intToDWord(0));
                fos.write(this.intToDWord(0xFFFFFF));
            } catch (Exception var3) {
                var3.printStackTrace();
            }

        }

        private void writePixelArray(ByteArrayOutputStream fos) {
            try {
                for(int var4 = this.biHeight; var4 > 0; --var4) {
                    for(int k = (var4 - 1) * this.scanLineSize; k < (var4 - 1) * this.scanLineSize + this.scanLineSize; ++k) {
                        fos.write(this.bitmap[k] & 255);
                    }
                }
            } catch (Exception var41) {
                Log.e("BMPFile", var41.toString());
            }

        }

        private byte[] intToWord(int parValue) {
            byte[] retValue = new byte[]{(byte)(parValue & 255), (byte)(parValue >> 8 & 255)};
            return retValue;
        }

        private byte[] intToDWord(int parValue) {
            byte[] retValue = new byte[]{(byte)(parValue & 255), (byte)(parValue >> 8 & 255), (byte)(parValue >> 16 & 255), (byte)(parValue >> 24 & 255)};
            return retValue;
        }
    }


}
