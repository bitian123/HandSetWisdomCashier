package com.centerm.epos.utils;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import org.apache.log4j.Logger;

import java.io.File;

/**
 * <p>description：</p>
 * <p>author：Leon</p>
 * <p>date：2019/7/18 0018</p>
 * <p>e-mail：deadogone@gmail.com</p>
 */
public class PrinterShareMgr {
    private static PrinterShareMgr mgr;
    private static final String packageName = "com.dynamixsoftware.printershare";
    private static final String PDF = "pdf";
    private static final String DOC = "doc";
    private static final String JPG = "jpg";
    private static final String HTML = "html";
    private static final String HTM = "htm";
    private static final String DOCX = "docx";
    private static final String TXT = "txt";
    private static final String JPEG = "jpeg";
    private static final String GIF = "gif";
    private static final String PNG = "png";
    protected org.apache.log4j.Logger logger = Logger.getLogger(this.getClass());

    public static PrinterShareMgr getInstance() {
        if (mgr == null) {
            mgr = new PrinterShareMgr();
        }
        return mgr;
    }

    private PrinterShareMgr() {
    }

    public void printFile(Context context, String filePath) {

        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            Toast.makeText(context, "文件不存在！", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            logger.error("openPinterShareFile -> " + filePath);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri data = Uri.fromFile(new File(filePath));
            ComponentName comp = null;
            if (filePath.endsWith(PDF)) {
                intent.setDataAndType(data, "application/pdf");
                comp = new ComponentName(packageName, packageName + ".ActivityPrintPDF");
            } else if (filePath.endsWith(DOC) || filePath.endsWith(DOCX) || filePath.endsWith(TXT)) {
                intent.setDataAndType(data, "application/doc");
                comp = new ComponentName(packageName, packageName + ".ActivityPrintDocuments");
            } else if (filePath.endsWith(JPG) || filePath.endsWith(JPEG) || filePath.endsWith(GIF) || filePath.endsWith(PNG)) {
                intent.setDataAndType(data, "image/jpeg");
                comp = new ComponentName(packageName, packageName + ".ActivityPrintPictures");
            } else if (filePath.endsWith(HTML) || filePath.endsWith(HTM)) {
                intent.setDataAndType(data, "text/html");
                comp = new ComponentName(packageName, packageName + ".ActivityWeb");
            }
            intent.setComponent(comp);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            ViewUtils.showToast(context, "文件打开错误，单据打印失败！");
        }
    }
}
