package com.centerm.epos.present.transaction;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.centerm.cloudsys.sdk.common.utils.FileUtils;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.base.SimpleStringTag;
import com.centerm.epos.bean.PrintReceiptBean;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.fragment.trade.SignatureAbnormalFragment;
import com.centerm.epos.fragment.trade.SignatureFragment;
import com.centerm.epos.transcation.pos.constant.JsonKeyGT;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.BitmapConvertorRaw;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.OkHttpUtils;
import com.centerm.epos.utils.OnCallListener;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.utils.XLogUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;
import config.Config;

import static config.BusinessConfig.Key.KEY_SIGNATURE_PIC_MAX;

/**
 * Created by yuhc on 2017/9/8.
 */

public class SignatureAbnormalPresent extends BaseTradePresent {
    private Map<String, Object> map;
    private SignatureAbnormalFragment fragment;
    private List<String> printList = new ArrayList<>();
    private int total = 0;
    private int successNum = 0;
    private int failNum = 0;

    public SignatureAbnormalPresent(ITradeView mTradeView) {
        super(mTradeView);
    }

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {

    }

    @Override
    public Object onConfirm(Object paramObj) {
        Bitmap signBitmap = (Bitmap) paramObj;
        if (signBitmap == null)
            return false;
        saveESignAndGoNext(signBitmap);
        return true;
    }

    private void saveESignAndGoNext(final Bitmap bitmap) {
        //保存Bitmap,供下个界面使用
        new BackgroundMission(bitmap).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private Bitmap cQuality(Bitmap bitmap) {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        int beginRate = 20;
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

    public String getContionCode() {
        return "";
    }

    private class BackgroundMission extends AsyncTask<Void, Void, Boolean> {

        private Bitmap esignBitmap;

        public BackgroundMission(Bitmap esignBitmap) {
            this.esignBitmap = esignBitmap;
        }

        @Override
        protected void onPreExecute() {
            if (esignBitmap != null) {
                DialogFactory.showLoadingDialog(fragment.getActivity(), "正在保存签名，请稍候...");
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (esignBitmap == null)
                return false;
            File fileDir = new File(Config.Path.SIGN_PATH);
            if (!fileDir.exists()) {
                FileUtils.createDirectory(fileDir.toString());
            }
            BusinessConfig config = BusinessConfig.getInstance();
            String filePath = fileDir + File.separator +
                    config.getValue(fragment.getActivity(), BusinessConfig.Key.KEY_BATCH_NO)
                    + "_"
                    + map.get("iso_f11")
                    + ".png";
            if(transDatas.get(JsonKeyGT.secondFlag)!=null){
                filePath = (String) transDatas.get(JsonKeyGT.secondFlag);
            }
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
            transDatas.put(JsonKeyGT.signFileName, filePath);
//            JBigConvertor.bitmap2BMPFile(mTradeView.getContext(), esignBitmap,BusinessConfig.getInstance().getNumber(mTradeView.getContext(),KEY_SIGNATURE_PIC_MAX), filePath);
            /*@author:zhouzhihua
            * 优化签名图片保存速度
            * 交易打印过程中断电导致签名图片损坏 JBigConvertor.bitmap2BMPFile BitmapConvertorRaw.bitmapToBmpInFile 均已经验证 两种保存方式都会出现
            * 但是重打印过程断电图片不会损坏，只有在交易完成后打印的时候才会出现。图片打印完成后断电图片不会损坏。
            * */
            if(!BitmapConvertorRaw.bitmapToBmpInFile(esignBitmap, BusinessConfig.getInstance().getNumber(fragment.getActivity(),KEY_SIGNATURE_PIC_MAX), filePath)){
                return false;
            }

//            byte[] mByteArray = JBigConvertor.bmpFileToJBIGBytes(filePath+".bmp", Config.Path.SIGN_PATH);
//            XLogUtil.d("zhouzhihua", " bmpFileToJBIGBytes = " + mByteArray.length);

            int size = fileDir.list().length;
            XLogUtil.d("tmp", " length = " + size);
            if (size >= config.getNumber(fragment.getActivity(), SimpleStringTag.ESIGN_STORE_MAX))
                config.setFlag(fragment.getActivity(), BusinessConfig.Key.FLAG_ESIGN_STORAGE_WARNING, true);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                DialogFactory.hideAll();
            } else
                ViewUtils.showToast(fragment.getActivity(), "签名保存失败！");
            getTransData().put(TradeInformationTag.STORE_E_SIGN_RESULT, true);
            //gotoNextStep();
            if(transDatas.get(JsonKeyGT.secondFlag)==null){
                //小票影像上传
                fragment.ticketUpload();
            }else {
                fragment.printReceipt();
            }

        }
    }

    public void init(SignatureAbnormalFragment f, Map<String, Object> m) {
        this.map = m;
        fragment = f;
    }

    public void printPDF(PrintReceiptBean data){
        printList.clear();
        if(data!=null){
            total = data.getBody().size();
            if(total>0){
                DialogFactory.showLoadingDialog(fragment.getActivity(), "正在下载单据\n请稍侯");
            }
            for(PrintReceiptBean.BodyBean bodyBean:data.getBody()){
                getPDF(bodyBean.getSubOrderId()+".pdf", bodyBean.getUrl());
            }
        }
    }

    private void getPDF(final String fileName, final String url){
        File file = new File(Config.Path.PDF_PATH, fileName);
        OkHttpUtils.getInstance().downloadFile(file, url, new OnCallListener() {
            @Override
            public void onCall(Map<String, Object> result) {
                try {
                    if(result!=null){
                        successNum++;
                        logger.info(fileName+" -> 下载完成 -> "+result.get("path"));
                        printList.add((String) result.get("path"));
                    }else {
                        failNum++;
                        logger.error("单据下载失败");
                    }
                    logger.debug("successNum:"+successNum);
                    logger.debug("failNum:"+failNum);
                    logger.debug("total:"+total);
                    if(successNum+failNum==total){
                        DialogFactory.hideAll();
                        if(successNum>0){
                            print();
                        }else {
                            ViewUtils.showToast(fragment.getActivity(), "无可打印单据");
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public void print(){
        if(printList!=null&&printList.size()>0){
            for(String p:printList){
                logger.error("打印数据："+p);
                openFile(fragment.getActivity(), p);
            }
        }
    }

    private void openFile(Context context, String path) {
        File file = new File(path);
        if (!file.exists()) {
            Toast.makeText(context, "文件不存在！", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //设置intent的Action属性
            intent.setAction(Intent.ACTION_VIEW);
            //获取文件file的MIME类型
            String type = getMIMEType(file);
            //设置intent的data和Type属性。
            intent.setDataAndType(/*uri*/Uri.fromFile(file), type);
            //跳转
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
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

    //获取签购单上的签名文件
    public File getSignFile(){
        File fileDir = new File(Config.Path.SIGN_PATH);
        if (!fileDir.exists()) {
            FileUtils.createDirectory(fileDir.toString());
        }
        BusinessConfig config = BusinessConfig.getInstance();
        String filePath = fileDir + File.separator +
                config.getValue(fragment.getActivity(), BusinessConfig.Key.KEY_BATCH_NO)
                + "_"
                + map.get("iso_f11")
                + ".png";

        File file = new File(filePath);
        return file;
    }

}
