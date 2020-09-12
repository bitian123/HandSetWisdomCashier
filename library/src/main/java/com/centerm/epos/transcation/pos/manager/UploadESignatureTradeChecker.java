package com.centerm.epos.transcation.pos.manager;

import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;

import java.io.File;

import config.Config;

/**
 * Created by yuhc on 2017/4/3.
 */

public class UploadESignatureTradeChecker implements RunTimeChecker {

    @Override
    public boolean check(ITradeView tradeView, ITradePresent tradePresent) {
        if(true){
            return false;
        }
        File eSignPicDir = new File(Config.Path.SIGN_PATH);
        if (eSignPicDir.exists()) {
            File[] eSignPicFiles = eSignPicDir.listFiles();
            if (eSignPicFiles == null || eSignPicFiles.length == 0)
                return false;
            String fileName;
            for (int i = 0; i < eSignPicFiles.length; i++) {
                fileName = eSignPicFiles[i].getName();
                if (fileName.endsWith(Config.Path.SIGN_UPLOAD_FAILED_SUFFIX))
                    continue;
                String traceNum = tradePresent.getTransData(TradeInformationTag.TRACE_NUMBER);
                String batchNum = tradePresent.getTransData(TradeInformationTag.BATCH_NUMBER);
                //当笔交易的签名要等到下次联机时才上送
                if (fileName.equals(batchNum + "_" + traceNum + ".png"))
                    continue;
                return true;
            }
        }
        return false;
    }

    public static boolean isEsignPicExist() {
        if(true){
            return false;
        }
        //电子签名是否存在
        File eSignPicDir = new File(Config.Path.SIGN_PATH);
        if (eSignPicDir.exists()) {
            File[] eSignPicFiles = eSignPicDir.listFiles();
            if (eSignPicFiles != null && eSignPicFiles.length > 0)
                return true;
        }
        return false;
    }
    /*
    * 删除所有电子签名的图片
    *
    * */
    public static void cleanEsignPic(){

        File eSignPicDir = new File(Config.Path.SIGN_PATH);

        if (eSignPicDir.exists()) {

            File[] eSignPicFiles = eSignPicDir.listFiles();

            for(File file : eSignPicFiles){

                    if(file.isFile()) {

                        file.delete();
                    }

                }

            }

    }

}
