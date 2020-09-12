package com.centerm.epos.ebi.present;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.PrinterShareMgr;
import com.centerm.epos.utils.ViewUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * author:liubit</br>
 * date:2019/9/2</br>
 */
public class DocumentPresent extends BaseTradePresent {
    private List<String> printList = new ArrayList<>();

    public DocumentPresent(ITradeView mTradeView) {
        super(mTradeView);
    }

    public List<String> getPrintList(){
        return printList;
    }

    public void addPrintTask(List<String> list, int num){
        printList.clear();
        if(num<10){
            for(int i=0;i<num;i++){
                printList.addAll(list);
            }
        }else {
            mTradeView.popToast("打印份数超限");
        }

    }

    public void print(){
        if(printList!=null&&printList.size()>0){
            String p = printList.get(0);
            printList.remove(0);
            logger.error("打印数据："+p);
            getPrinter(p);
        }
    }

    public void print2(){
        if(printList!=null&&printList.size()>0){
            for(String p:printList){
                logger.error("打印数据："+p);
                getPrinter(p);
            }
        }
    }

    public void getPrinter(String p){
        //如果安装了printershare则调用printershare，否则调用打印工厂
        String packageName = "com.dynamixsoftware.printershare";
        Boolean isInstallPrinterShare = CommonUtils.isComponentExist(packageName);
        logger.info("是否安装了printerShare插件："+isInstallPrinterShare);
        if(isInstallPrinterShare){
            PrinterShareMgr.getInstance().printFile(mTradeView.getHostActivity(), p);
        }else{
            openFile(mTradeView.getHostActivity(), p);
        }
    }

    private void openFile(Context context, String path) {
        File file = new File(path);
        if (!file.exists()) {
            Toast.makeText(context, "文件不存在！", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            logger.error("openFile -> "+path);
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //设置intent的Action属性
            intent.setAction(Intent.ACTION_VIEW);
            //获取文件file的MIME类型
            //String type = getMIMEType(file);
            //直接指定为pdf，避免还需要选择【图库】还是【打印工厂】
            String type = "application/pdf";
            //设置intent的data和Type属性。
            intent.setDataAndType(/*uri*/Uri.fromFile(file), type);
            //跳转
            //((Activity)context).startActivityForResult(intent,111);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            logger.error("ActivityNotFoundException -> "+e.toString());
            ViewUtils.showToast(context,"文件打开错误，单据打印失败！");
        }
    }

    private String getMIMEType(File file) {
        String type = "*/*";
        String fName = file.getName();
        //获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex < 0) {
            return type;
        }
        /* 获取文件的后缀名*/
        String end = fName.substring(dotIndex, fName.length()).toLowerCase();
        if (end == "") return type;
        //在MIME和文件类型的匹配表中找到对应的MIME类型。
        for (int i = 0; i < MIME_MapTable.length; i++) { //MIME_MapTable??在这里你一定有疑问，这个MIME_MapTable是什么？
            if (end.equals(MIME_MapTable[i][0]))
                type = MIME_MapTable[i][1];
        }
        return type;
    }

    private String[][] MIME_MapTable = {
            //{后缀名，MIME类型}
            {".3gp", "video/3gpp"},
            {".apk", "application/vnd.android.package-archive"},
            {".asf", "video/x-ms-asf"},
            {".avi", "video/x-msvideo"},
            {".bin", "application/octet-stream"},
            {".bmp", "image/bmp"},
            {".c", "text/plain"},
            {".class", "application/octet-stream"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".doc", "application/msword"},
            {".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {".xls", "application/vnd.ms-excel"},
            {".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {".exe", "application/octet-stream"},
            {".gif", "image/gif"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h", "text/plain"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".jar", "application/java-archive"},
            {".java", "text/plain"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log", "text/plain"},
            {".m3u", "audio/x-mpegurl"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".mp4", "video/mp4"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".mpe", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".mpga", "audio/mpeg"},
            {".msg", "application/vnd.ms-outlook"},
            {".ogg", "audio/ogg"},
            {".pdf", "application/pdf"},
            {".png", "image/png"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            {".prop", "text/plain"},
            {".rc", "text/plain"},
            {".rmvb", "audio/x-pn-realaudio"},
            {".rtf", "application/rtf"},
            {".sh", "text/plain"},
            {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"},
            {".txt", "text/plain"},
            {".wav", "audio/x-wav"},
            {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"},
            {".wps", "application/vnd.ms-works"},
            {".xml", "text/plain"},
            {".z", "application/x-compress"},
            {".zip", "application/x-zip-compressed"},
            {"", "*/*"}
    };
}
