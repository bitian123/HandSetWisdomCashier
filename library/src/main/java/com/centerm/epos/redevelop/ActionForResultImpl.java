package com.centerm.epos.redevelop;

import com.centerm.cloudsys.sdk.common.utils.FileUtils;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.XLogUtil;

import java.io.File;
import java.util.Map;

import config.Config;

/**
 * Created by yuhc on 2017/9/1.
 */

public class ActionForResultImpl implements IActionForResult {
    private static final String TAG = ActionForResultImpl.class.getSimpleName();

    protected Map<String, Object> requestData;
    protected Map<String, Object> respData;

    @Override
    public void doAction(Map<String, Object> requestData, Map<String, Object> respData) {
        if (respData == null)
            return;

        this.requestData = requestData;
        this.respData = respData;

        String respCode = (String) respData.get(TradeInformationTag.RESPONSE_CODE);
        if ("00".equals(respCode)){
            dealWithSuccess();
        }else {
            dealWithFailed();
        }
    }

    /**
     * 交易失败处理
     */
    private void dealWithFailed() {
//        String tranType = (String) requestData.get(TradeInformationTag.TRANSACTION_TYPE);
//        switch (tranType){
//            case TransCode.ESIGN_UPLOAD:
//                //修改上送失败的签名文件名，以标记上送失败
//                File esignFile = getESignPicFile();
//                if (esignFile != null){
//                    String newName = esignFile.getName().replace(".png", Config.Path.SIGN_UPLOAD_FAILED_SUFFIX+"png");
//                    esignFile.renameTo(new File(newName));
//                }
//                break;
//        }
    }

    /**
     * 交易成功处理
     */
    private void dealWithSuccess() {
//        String tranType = (String) requestData.get(TradeInformationTag.TRANSACTION_TYPE);
//        switch (tranType){
//            case TransCode.ESIGN_UPLOAD:
//                //删除上送成功的签名文件
//                File esignFile = getESignPicFile();
//                if (esignFile != null){
//                    if (esignFile.delete())
//                        XLogUtil.d(TAG, "^_^ 删除电子签名成功：" + esignFile.getName() + " ^_^");
//                    else
//                        XLogUtil.d(TAG, "^_^ 删除电子签名失败：" + esignFile.getName() + " ^_^");
//                }
//                break;
//        }
    }


//    private File getESignPicFile(){
//        String tradeIndex = (String) requestData.get(TradeInformationTag.TRACE_NUMBER);
//        String batchIndex = (String) requestData.get(TradeInformationTag.BATCH_NUMBER);
//        String path = Config.Path.SIGN_PATH + File.separator + batchIndex + "_" + tradeIndex + ".png";
//        File esignFile = new File(path);
//        if (esignFile.exists()) {
//            return esignFile;
//        } else {
//            XLogUtil.d(TAG, "签名图片不存在");
//            return null;
//        }
//    }
}
