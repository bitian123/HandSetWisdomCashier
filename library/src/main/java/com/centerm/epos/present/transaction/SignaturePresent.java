package com.centerm.epos.present.transaction;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;

import com.centerm.cloudsys.sdk.common.utils.FileUtils;
import com.centerm.epos.EposApplication;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.base.SimpleStringTag;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.common.ISORespCode;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.fragment.trade.SignatureFragment;
import com.centerm.epos.net.ResponseHandler;
import com.centerm.epos.present.communication.DataExchanger;
import com.centerm.epos.present.communication.DataExchangerFactory;
import com.centerm.epos.task.AsyncMultiRequestTask;
import com.centerm.epos.transcation.pos.constant.JsonKeyGT;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.BitmapConvertorRaw;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.utils.XLogUtil;
import com.centerm.iso8583.util.DataConverter;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;
import config.Config;

import static config.BusinessConfig.Key.KEY_SIGNATURE_PIC_MAX;

/**
 * Created by yuhc on 2017/9/8.
 */

public class SignaturePresent extends BaseTradePresent {

    public SignaturePresent(ITradeView mTradeView) {
        super(mTradeView);
    }

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        mTradeView.getHostActivity().openPageTimeout(BusinessConfig.getInstance().getNumber(mTradeView.getContext(),
                SimpleStringTag.ESIGN_INPUT_TIMEOUT), "长时间未签名\n是否跳过电子签名");
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
        if (!FileUtils.hasSDCard()) {
            mTradeView.popToast(R.string.tips_nosdcard);
            gotoNextStep();
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String tradeDate = formatter.format(new Date());
            transDatas.put(TradeInformationTag.TRANS_TIME, tradeDate.substring(8, 14));
            transDatas.put(TradeInformationTag.TRANS_DATE, tradeDate.substring(4, 8));
            new SettlementInfoQueryTask(mTradeView.getContext(), getTransData(), bitmap).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        mTradeView.getHostActivity().clearPageTimeout();
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
        Map<String, Object> map = mTradeView.getHostActivity().mTradeInformation.getRespDataMap();
        String block, block1, block2;

        /*
        * 离线类交易没有结算日期和交易参考号，使用批次号和流水号的组合方式生成
        * */
        XLogUtil.d("getContionCode", " getTradeCode = " + getTradeCode());
        if(map!=null){
            XLogUtil.d("getContionCode", " REFERENCE_NUMBER = " + map.get(TradeInformationTag.REFERENCE_NUMBER));
        }else {
            return "000000";
        }

        if( TransCode.MAG_CASH_LOAD.equals(getTradeCode()) || TransCode.MAG_ACCOUNT_LOAD.equals(getTradeCode()) ){
            block = "0000" + map.get(TradeInformationTag.REFERENCE_NUMBER);
        }
        else if( TransCode.E_REFUND.equals(getTradeCode()) || (null == map.get(TradeInformationTag.DATE_SETTLEMENT)
            && null == map.get(TradeInformationTag.REFERENCE_NUMBER)) ){
            block = (String)map.get(TradeInformationTag.BATCH_NUMBER)+ map.get(TradeInformationTag.TRACE_NUMBER)+"0000";
        }
        else if (map.get(TradeInformationTag.DATE_SETTLEMENT) != null) {
            block = (String) map.get(TradeInformationTag.DATE_SETTLEMENT) + map.get(TradeInformationTag.REFERENCE_NUMBER);
        } else {
            block = "0000" + map.get(TradeInformationTag.REFERENCE_NUMBER);
        }

        block1 = block.substring(0, 8);
        block2 = block.substring(8, 16);

        return twoStringXor(block1, block2);
    }

    private String twoStringXor(String str1, String str2) {

        byte b1[] = DataConverter.hexStringToByte(str1);
        byte b2[] = DataConverter.hexStringToByte(str2);
        byte longbytes[], shortbytes[];


        if (b1.length >= b2.length) {
            longbytes = b1;
            shortbytes = b2;
        } else {
            longbytes = b2;
            shortbytes = b1;
        }
        byte xorstr[] = new byte[longbytes.length];
        int i = 0;
        for (; i < shortbytes.length; i++) {
            xorstr[i] = (byte) (shortbytes[i] ^ longbytes[i]);
        }
        for (; i < longbytes.length; i++) {
            xorstr[i] = (byte) (longbytes[i]);
        }
        return DataConverter.bytesToHexString(xorstr);
    }

    public class SettlementInfoQueryTask extends AsyncMultiRequestTask {
        private Map<String, Object> returnMap;
        private Bitmap bitmap;

        public SettlementInfoQueryTask(Context context, Map<String, Object> dataMap, Bitmap mBitmap) {
            super(context, dataMap);
            //initParamTip();
            logger.error("^_^ dataMap: ^_^" + dataMap.toString());
            bitmap = mBitmap;
            this.returnMap = returnMap;
            if (this.returnMap == null) {
                this.returnMap = new HashMap<>();
            }
        }

        @Override
        protected String[] doInBackground(String... params) {
            Object msgPkg = factory.packMessage(TransCode.SETTLEMENT_INFO_QUERY, dataMap);
            ResponseHandler handler = new ResponseHandler() {
                @Override
                public void onSuccess(String statusCode, String msg, byte[] data) {
                    Map<String, Object> mapData = factory.unPackMessage(TransCode.SETTLEMENT_INFO_QUERY, data);
                    returnMap.putAll(mapData);
                    String respCode = (String) mapData.get(TradeInformationTag.RESPONSE_CODE);
                    ISORespCode isoCode = ISORespCode.codeMap(respCode);
                    taskResult[0] = isoCode.getCode();
                    taskResult[1] = context.getString(isoCode.getResId());
                    if ("00".equals(respCode)) {
                        String iso62 = (String) mapData.get(TradeInformationTag.SETTLEMENT_INFO);
                        taskResult[0] = respCode;
                        taskResult[1] = iso62;
                        saveSettlementInfo(iso62);
                        new BackgroundMission(bitmap).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        taskResult[0] = respCode;
                        taskResult[1] = msg;
                    }
                }

                @Override
                public void onFailure(String code, String msg, Throwable error) {
                    taskResult[0] = code;
                    taskResult[1] = msg;
                }
            };

            try {

                DataExchanger dataExchanger = DataExchangerFactory.getInstance();
                byte[] receivedData = dataExchanger.doExchange((byte[]) msgPkg);
                if (receivedData == null) {
                    logger.error("^_^ 接收数据失败！ ^_^");
                    handler.onFailure("99", "接收数据失败！", null);
                } else {
                    handler.onSuccess(null, null, receivedData);
                }
            } catch (Exception e) {
                logger.error("^_^ 数据交换失败：" + e.getMessage() + " ^_^");
                e.printStackTrace();
                taskResult[0] = "99";
                taskResult[1] = "数据交换失败";
            }
            return taskResult;
        }

        @Override
        public void onStart() {
            super.onStart();

        }
    }

    private void saveSettlementInfo(String field62) {
        String account = null;
        String[] accountInfo = null;
        if (!TextUtils.isEmpty(field62)) {
            accountInfo = field62.split("[|]");
            if (accountInfo[1].contains("公司")) {
                account = accountInfo[1].substring(accountInfo[1].indexOf("公司") + 2);
            } else {
                account = accountInfo[1];
            }
        } else {
            mTradeView.popToast("获取结算账户信息失败，请稍后重试");
            return;
        }
        if (accountInfo.length > 2) {
            transDatas.put(TradeInformationTag.SETTLEMENT_INFO, accountInfo[2] + accountInfo[0] + account);
        } else {
            transDatas.put(TradeInformationTag.SETTLEMENT_INFO, accountInfo[0] + account);
        }
        TradeInfoRecord tradeInfo = (TradeInfoRecord) transDatas.get(JsonKeyGT.curTradeInfo);

        DbHelper dbHelper = OpenHelperManager.getHelper(EposApplication.getAppContext(), DbHelper.class);
        CommonDao<TradeInfoRecord> tradeDao = new CommonDao<>(TradeInfoRecord.class, dbHelper);
        Map<String, String> map=new HashMap<>();
        map.put("batchNo",(String) transDatas.get(TradeInformationTag.BATCH_NUMBER)) ;
        map.put("voucherNo",(String) transDatas.get(TradeInformationTag.TRACE_NUMBER)) ;
        List<TradeInfoRecord> tradeInfoRecords = tradeDao.queryByMap(map);

        XLogUtil.e("voucherNo==batchNo ", (String) transDatas.get(TradeInformationTag.BATCH_NUMBER)+"=="+(String) transDatas.get(TradeInformationTag.TRACE_NUMBER));

        if (tradeInfoRecords!=null && tradeInfoRecords.size()>0){
            TradeInfoRecord tradeInfoRecord = tradeInfoRecords.get(0);
            tradeInfoRecord.setSettlementInfo(field62);
            tradeDao.update(tradeInfoRecord);
        }
        if(tradeInfo!=null){
            tradeInfo.setSettlementInfo(field62);
            transDatas.put(JsonKeyGT.curTradeInfo,tradeInfo);
        }
        XLogUtil.d("settlementInfo", (String)transDatas.get(TradeInformationTag.SETTLEMENT_INFO));

    }
    private class BackgroundMission extends AsyncTask<Void, Void, Boolean> {

        private Bitmap esignBitmap;

        public BackgroundMission(Bitmap esignBitmap) {
            this.esignBitmap = esignBitmap;
        }

        @Override
        protected void onPreExecute() {
            if (esignBitmap != null) {
                //DialogFactory.showLoadingDialog(mTradeView.getContext(), "正在保存签名，请稍候...");
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
            String filePath = fileDir + File.separator + config.getValue(mTradeView.getContext(), BusinessConfig.Key
                    .KEY_BATCH_NO)
                    + "_"
                    + mTradeView.getHostActivity().mTradeInformation.getTransDatas().get(TradeInformationTag
                    .TRACE_NUMBER)
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
            if(!BitmapConvertorRaw.bitmapToBmpInFile(esignBitmap, BusinessConfig.getInstance().getNumber(mTradeView.getContext(),KEY_SIGNATURE_PIC_MAX), filePath)){
                return false;
            }

//            byte[] mByteArray = JBigConvertor.bmpFileToJBIGBytes(filePath+".bmp", Config.Path.SIGN_PATH);
//            XLogUtil.d("zhouzhihua", " bmpFileToJBIGBytes = " + mByteArray.length);

            int size = fileDir.list().length;
            XLogUtil.d("tmp", " length = " + size);
            if (size >= config.getNumber(mTradeView.getContext(), SimpleStringTag.ESIGN_STORE_MAX))
                config.setFlag(mTradeView.getContext(), BusinessConfig.Key.FLAG_ESIGN_STORAGE_WARNING, true);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                DialogFactory.hideAll();
            } else
                ViewUtils.showToast(mTradeView.getContext(), "签名保存失败！");
            getTransData().put(TradeInformationTag.STORE_E_SIGN_RESULT, true);
            //gotoNextStep();
            String transCode = getTradeCode();
            if(transCode.equals(TransCode.SALE)){
                if(transDatas.get(JsonKeyGT.secondFlag)==null){
                    //小票影像上传
                    ((SignatureFragment)mTradeView).ticketUpload();
                }else {
                    ((SignatureFragment)mTradeView).printReceipt();
                }
            }else{
                gotoNextStep();
            }


        }
    }

    //获取签购单上的签名文件
    public File getSignFile(){
        File fileDir = new File(Config.Path.SIGN_PATH);
        if (!fileDir.exists()) {
            FileUtils.createDirectory(fileDir.toString());
        }
        BusinessConfig config = BusinessConfig.getInstance();
        String filePath = fileDir + File.separator + config.getValue(mTradeView.getContext(), BusinessConfig.Key
                .KEY_BATCH_NO)
                + "_"
                + mTradeView.getHostActivity().mTradeInformation.getTransDatas().get(TradeInformationTag
                .TRACE_NUMBER)
                + ".png";

        File file = new File(filePath);
        return file;
    }
}
