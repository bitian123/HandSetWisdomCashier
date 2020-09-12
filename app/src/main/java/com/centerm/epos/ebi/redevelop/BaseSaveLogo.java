package com.centerm.epos.ebi.redevelop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.widget.Toast;

import com.centerm.cloudsys.sdk.common.utils.FileUtils;
import com.centerm.epos.ebi.R;
import com.centerm.epos.redevelop.ISaveLogo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Created by 94437 on 2017/7/20.
 */

public class BaseSaveLogo implements ISaveLogo {
    private String TAG = "BaseSaveLogo";
    private Context context;
    @Override
    public void save(Context context) {
        this.context = context;
        saveImg();
    }
    public void saveImg() {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.print_logo);
        savePrintLogo(bitmap);

    }
    private void savePrintLogo(Bitmap bitmap) {
        final String ROOT = Environment.getExternalStorageDirectory() + File.separator
                + "EPos";
        final String PRINTLOGO_PATH = ROOT + File.separator + "printlogo";
        //保存Bitmap,供下个界面使用
        if (!FileUtils.hasSDCard()) {
            Toast.makeText(context, "sd卡已卸载,不能保存图片", Toast.LENGTH_SHORT).show();
        } else {
            File fileDir = new File(PRINTLOGO_PATH);
            if (!fileDir.exists()) {
                FileUtils.createDirectory(fileDir.toString());
            }
            String fileName = "ebi_print_logo.bmp";
            File file = new File(fileDir + File.separator + fileName);
            if (file.exists()) {
                file.delete();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {

            }
            FileOutputStream fOut = null;
            try {
                fOut = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            bitmap = cQuality(bitmap);
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, fOut);

            try {
                fOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            bitmap.recycle();
        }
    }

    private Bitmap cQuality(Bitmap bitmap) {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        int beginRate = 25;
        // 第一个参数 ：图片格式 ，第二个参数： 图片质量，100为最高，0为最差 ，第三个参数：保存压缩后的数据的流
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, bOut);
        while (bOut.size() / 1024 / 1024 > 100) { // 如果压缩后大于100Kb，则提高压缩率，重新压缩
            beginRate -= 10;
            bOut.reset();
            bitmap.compress(Bitmap.CompressFormat.PNG, beginRate, bOut);
        }
        ByteArrayInputStream bInt = new ByteArrayInputStream(bOut.toByteArray());
        Bitmap newBitmap = BitmapFactory.decodeStream(bInt);
        if (newBitmap != null) {
            return newBitmap;
        } else {
            return bitmap;
        }
    }
}
