package com.centerm.epos.task;

import android.content.Context;
import android.text.TextUtils;

import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;
import com.centerm.epos.EposApplication;
import com.centerm.epos.R;
import com.centerm.epos.base.SimpleStringTag;
import com.centerm.epos.bean.IntegralInfo;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.bean.TradePbocDetail;
import com.centerm.epos.bean.transcation.BalancAmount;
import com.centerm.epos.bean.transcation.InstallmentInformation;
import com.centerm.epos.common.ConstDefine;
import com.centerm.epos.common.ISORespCode;
import com.centerm.epos.common.StatusCode;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.CommonManager;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.net.SequenceHandler;
import com.centerm.epos.redevelop.ICommonManager;
import com.centerm.epos.transcation.pos.constant.ESignatureTlvTag;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.util.pensigner.JBigConvertor;
import com.centerm.epos.utils.XLogUtil;
import com.centerm.smartpos.util.HexUtil;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import config.BusinessConfig;
import config.Config;

/**
 * Created by yuhc on 2017/9/1.
 * 电子签名上送
 */

public class AsyncUploadESignatureTask extends AsyncMultiRequestTask {
    private static final String TAG = AsyncUploadESignatureTask.class.getSimpleName();

    private File uploadingFile;
    private String esignJBIGStr;
    private int partPackageIndex;
    private int totalPackageCount;
    private int retryCount;
    //当前交易的流水号和批次号
    private String traceNumber;
    private String batchNumber;
    private boolean timeFlag = true;//每次只上送一个电子签名

    protected CommonDao<TradeInfoRecord> tradeInfoRecordCommonDao;
    CommonDao<InstallmentInformation> installCommonDao ;
    private File[] esignFiles;
    private int fileIndex;
    private boolean isUploadAll = false;    //是否批量上送所有签名
    private int uploadMode = ConstDefine.ELEC_SIGN_UPLOAD_MODE_0;
    private File eSignFile;


    public AsyncUploadESignatureTask(Context context, Map<String, Object> dataMap) {
        super(context, dataMap);
        traceNumber = (String) dataMap.get(TradeInformationTag.TRACE_NUMBER);
        batchNumber = (String) dataMap.get(TradeInformationTag.BATCH_NUMBER);
        fileIndex = 0;
        DbHelper dbHelper = DbHelper.getInstance();
        tradeInfoRecordCommonDao = new CommonDao<>(TradeInfoRecord.class, dbHelper);
        installCommonDao = new CommonDao<>(InstallmentInformation.class, dbHelper);
    }

    public AsyncUploadESignatureTask(Context context, Map<String, Object> dataMap,File file) {
        super(context, dataMap);
        eSignFile = file;
        traceNumber = (String) dataMap.get(TradeInformationTag.TRACE_NUMBER);
        batchNumber = (String) dataMap.get(TradeInformationTag.BATCH_NUMBER);
        fileIndex = 0;
        DbHelper dbHelper = DbHelper.getInstance();
        tradeInfoRecordCommonDao = new CommonDao<>(TradeInfoRecord.class, dbHelper);
        installCommonDao = new CommonDao<>(InstallmentInformation.class, dbHelper);
    }

    public void setUploadAll(boolean uploadAll) {
        isUploadAll = uploadAll;
    }

    /**
     *
     * @param uploadMode 参考{@link ConstDefine#ELEC_SIGN_UPLOAD_MODE_0}<br/>
     *                   {@link ConstDefine#ELEC_SIGN_UPLOAD_MODE_1}<br/>
     *                   {@link ConstDefine#ELEC_SIGN_UPLOAD_MODE_2} <br/>
     */
    public void setUploadMode(int uploadMode) {
        this.uploadMode = uploadMode;
    }

    private String[] elecSignatureUploadNormal(String... params){
        uploadingFile = getESignPicFile();
        do {
            if (!generateTransData()) {
                logger.error("^_^ 获取签名数据失败 ^_^");
                taskResult[0] = "99";
                taskResult[1] = "获取签名数据失败";
                /*@author:zhouzhihua
                * 找不到流水的签名图片直接删掉
                * */
                if( (uploadingFile != null) && uploadingFile.isFile() ) {
                    uploadingFile.delete();
                }
//                DbHelper.releaseInstance();
//                return taskResult;
                continue;
            }
            if (isUploadAll) {
                publishProgress(esignFiles.length, fileIndex + 1);
            } else if (partPackageIndex != 0 && totalPackageCount != 1)
                publishProgress(totalPackageCount, partPackageIndex);

            Object msgPkg = factory.packMessage(TransCode.ESIGN_UPLOAD, dataMap);
            SequenceHandler handler = new SequenceHandler() {

                @Override
                protected void onReturn(String reqTag, byte[] respData, String code, String msg) {
                    XLogUtil.d(TAG, "电子签名onReturn：" + (respData==null) + " ^_^");
                    if (respData == null) {
                        taskResult[0] = code;
                        taskResult[1] = msg;
                        /*
                        * BUGID:0004099: 平台设置电子签名无应答，第一笔消费的电子签名上送失败后，后续再消费依旧会一直上送第一笔的电子签名
                        * 超时电子签名，默认为上送失败
                        * */
                        if( !msg.equals(EposApplication.getAppContext().getString(StatusCode.SOCKET_TIMEOUT.getMsgId())) ) {
                            onFailure(code, msg);
                        }
                        return;
                    }
                    Map<String, Object> mapData = factory.unPackMessage(partPackageIndex == 1 ? TransCode.ESIGN_UPLOAD :
                            TransCode.ESIGN_UPLOAD_PART, respData);
                    String respCode = (String) mapData.get(TradeInformationTag.RESPONSE_CODE);
                    ISORespCode isoCode = ISORespCode.codeMap(respCode);
                    taskResult[0] = respCode;
                    taskResult[1] = context.getString(isoCode.getResId());
                    if ("00".equals(respCode)) {
                        if (partPackageIndex < totalPackageCount && !timeFlag) {
                            genJBigInformation();

                            if (isUploadAll)
                                publishProgress(esignFiles.length, fileIndex + 1);
                            else if (totalPackageCount != 1)
                                publishProgress(totalPackageCount, partPackageIndex);

                            byte[] packageBytes = (byte[]) factory.packMessage(TransCode.ESIGN_UPLOAD_PART, dataMap);
                            sendNext(TransCode.ESIGN_UPLOAD_PART, packageBytes);
                        } else {
                            //删除上送成功的签名文件
                            if (uploadingFile != null) {
                                if (uploadingFile.delete())
                                    XLogUtil.d(TAG, "^_^ 删除电子签名成功：" + uploadingFile.getName() + " ^_^");
                                else
                                    XLogUtil.d(TAG, "^_^ 删除电子签名失败：" + uploadingFile.getName() + " ^_^");
                            }
                        }
                    } else {
                        onFailure(taskResult[0], taskResult[1]);
                    }
                }

                void onFailure(String code, String msg) {
                    //修改上送失败的签名文件名，以标记上送失败
                    if (uploadingFile != null && !uploadingFile.getName().endsWith(Config.Path.SIGN_UPLOAD_FAILED_SUFFIX)) {

                        String newName = uploadingFile.getPath().replace(".png", Config.Path.SIGN_UPLOAD_FAILED_SUFFIX);
                        uploadingFile.renameTo(new File(newName));

                    }
                    taskResult[0] = code;
                    taskResult[1] = msg;
                }
            };
            client.doSequenceExchange(TransCode.ESIGN_UPLOAD, (byte[]) msgPkg, handler);
            if ( (!isUploadAll) && (uploadMode!=ConstDefine.ELEC_SIGN_UPLOAD_MODE_1) ){ break; }
        } while ( getNextFile() || ((uploadMode!=ConstDefine.ELEC_SIGN_UPLOAD_MODE_1) && (uploadMode!=ConstDefine.ELEC_SIGN_UPLOAD_MODE_2) && retryUpload() ) );
        DbHelper.releaseInstance();
        return taskResult;
    }


    TradeInfoRecord getLastTradeInfoRecord(){
        ICommonManager commonManager = (ICommonManager) ConfigureManager.getInstance(EposApplication.getAppContext()).getSubPrjClassInstance(new CommonManager());
        try {
            List<TradeInfoRecord> lastList = commonManager.getLastTransItem();
            if( lastList != null && lastList.size() > 0 ){
                return lastList.get(0);
            }
        }catch (SQLException e){
            XLogUtil.e(getClass().getSimpleName(), "获取最后一笔交易失败!!!");
        }
        return null;
    }

    /**
     * @param params  <br/>
     * {@link ConstDefine#ELEC_SIGN_UPLOAD_MODE_0}<br/>
     * {@link ConstDefine#ELEC_SIGN_UPLOAD_MODE_1}<br/>
     * {@link ConstDefine#ELEC_SIGN_UPLOAD_MODE_2} <br/>
     */
    @Override
    protected String[] doInBackground(String... params) {

        boolean bIsUploadAll ;

        if( uploadMode == ConstDefine.ELEC_SIGN_UPLOAD_MODE_1 ){
            TradeInfoRecord tradeInfoRecord = getLastTradeInfoRecord();
            batchNumber = tradeInfoRecord.getBatchNo();
            traceNumber = tradeInfoRecord.getVoucherNo();
            XLogUtil.w(getClass().getSimpleName(), "ELEC_SIGN_UPLOAD_MODE_1:"+batchNumber+"_"+traceNumber);
        }
        bIsUploadAll = (uploadMode == ConstDefine.ELEC_SIGN_UPLOAD_MODE_1) ? false : ( (uploadMode == ConstDefine.ELEC_SIGN_UPLOAD_MODE_2) ? true : isUploadAll );

        setUploadAll(bIsUploadAll);
        return elecSignatureUploadNormal(params);
    }

    private boolean retryUpload() {
        if (isUploadAll) {
            int retryMax = BusinessConfig.getInstance().getNumber(context, SimpleStringTag.ESIGN_BATCH_REQ_RETRY_TIMES);
            File dirFile = new File(Config.Path.SIGN_PATH);
            if (dirFile.list().length > 0) {
                if (retryCount++ < retryMax) {
                    esignFiles = null;
                    fileIndex = 0;
                    uploadingFile = getESignPicFile();
                    return uploadingFile != null;
                }
                clearESignFiles(dirFile);
            }
        }
        return false;
    }

    private void clearESignFiles(File dirFile) {
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++)
            files[i].delete();
    }

    private boolean getNextFile() {
        fileIndex++;
        esignJBIGStr = null;
        uploadingFile = getESignPicFile();
        XLogUtil.w(getClass().getSimpleName(),"getNextFile:"+uploadingFile);
        return uploadingFile != null;
    }
    private String getTransAmount(String amt){
        if (TextUtils.isEmpty(amt)) {
            return null;
        }
        String amountFormat;    //对小数点进行处理

        if (amt.indexOf('.') == -1) {
            long moneyInt = Long.parseLong(amt, 10);
            amountFormat = String.format(Locale.CHINA,"%010d00", moneyInt);
        } else {
            String moneyParts[] = amt.split("\\.");
            if (moneyParts.length > 3) {
                return null;
            }
            long moneyIntegralPart = Long.parseLong(moneyParts[0], 10);//整数部分处理
            String fractionalPartStr = moneyParts[1];//小数部分处理
            if (fractionalPartStr.length() > 2)
                fractionalPartStr = fractionalPartStr.substring(0,2);
            Long moneyFractionalPart = Long.parseLong(fractionalPartStr,10);
            amountFormat = String.format(Locale.CHINA,"%010d%02d",moneyIntegralPart,moneyFractionalPart);//格式化输出数据：10位整数+2位小数
        }
        return amountFormat;
    }
    private InstallmentInformation getInstallmentInfo(String tradeIndex) {
        XLogUtil.w(this.getClass().getSimpleName(),"InstallmentInformation tradeIndex:"+tradeIndex);
        try {
            InstallmentInformation information = null;

            List installData = installCommonDao.queryBuilder().where().eq("voucherNo", tradeIndex).query();
            if ( !(installData == null || installData.size() == 0 ) ) {
                information = (InstallmentInformation) installData.get(0);
            }
            //DbHelper.releaseInstance();
            return information;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    private String getReservationSalePhone(TradeInfoRecord tradeInfoRecord){
        String string = tradeInfoRecord.getIso62Req();

        if( string == null ){
            return null;
        }
        int iLen = EposApplication.getAppContext().getResources().getInteger(R.integer.phone_number_max_len);
        String phoneString = string.substring(2,iLen).trim();
        byte[] bytes = phoneString.getBytes();

        int iPadLen = (bytes.length > 6) ? (bytes.length-6) : (bytes.length-3);

        for( int i = 0 ; i < iPadLen ; i ++ ){
            bytes[i+3] = '*';
        }
        return new String(bytes);
    }
    private boolean generateTransData() {
        if (uploadingFile == null || (uploadingFile.isFile() && uploadingFile.length()==0) )
            return false;
        String voucherNum = (String) dataMap.get(TradeInformationTag.TRACE_NUMBER);
        TradeInfoRecord tradeInfoRecord = null;
        try {
            List recordList = tradeInfoRecordCommonDao.queryBuilder().where().eq(TradeInfoRecord
                    .KEY_COLUMN_NAME, voucherNum).query();
            tradeInfoRecord = (TradeInfoRecord) recordList.get(0);
        } catch (Exception e) {
            XLogUtil.e(TAG, "^_^ 读取交易记录失败，流水号：" + voucherNum + "\n" + e.getMessage() + " ^_^");

        }
        if (tradeInfoRecord == null)
            return false;

        fillISOFieldData(tradeInfoRecord);

        //添加到数据缓存中，用于报文组织
        dataMap.putAll(tradeInfoRecord.convert2Map());
        Map<String, String> slipElements = new LinkedHashMap<>();

        String transType = tradeInfoRecord.getTransType();/*获取交易类型*/
        //添加交易通用信息
        slipElements.put(ESignatureTlvTag.MERCHANT_NAME, tradeInfoRecord.getMerchantName());
        slipElements.put(ESignatureTlvTag.TRANSACTION_TYPE, context.getString(TransCode.codeMapName(tradeInfoRecord
                .getTransType())));
        slipElements.put(ESignatureTlvTag.OPERATOR_ID, tradeInfoRecord.getOperatorNo());
        slipElements.put(ESignatureTlvTag.RECEIPT_AGENCY, tradeInfoRecord.getAcquireInstituteID());
        slipElements.put(ESignatureTlvTag.ISSUE_INSTITUTIONS, tradeInfoRecord.getIssueInstituteID());
        slipElements.put(ESignatureTlvTag.VALIDE_DATE, tradeInfoRecord.getCardExpiredDate());
        String dateStr = "";
        String yearStr = tradeInfoRecord.getTransYear();
        if(TextUtils.isEmpty(tradeInfoRecord.getTransYear())){
            yearStr = new SimpleDateFormat("yyyy").format(new Date());
        }
        if(!TextUtils.isEmpty(tradeInfoRecord.getTransDate())&&!TextUtils.isEmpty(tradeInfoRecord.getTransTime())){
            dateStr = yearStr + tradeInfoRecord.getTransDate()
                    + tradeInfoRecord.getTransTime();
            if(!TextUtils.isEmpty(dateStr)){
                slipElements.put(ESignatureTlvTag.DATE_TIME, dateStr);
            }
        }
        slipElements.put(ESignatureTlvTag.AUTH_CODE, tradeInfoRecord.getAuthorizeNo());

        // TODO: 2017/9/4  小费金额、卡组织
        if(TransCode.OFFLINE_ADJUST_TIP.equals(transType)){
            //slipElements.put(ESignatureTlvTag.TIP_AMOUNT, );
        }
        if( TransCode.NOTIFY_TRADE_SETS.contains(transType) || TransCode.OFFLINE_ADJUST.equals(transType)
                || TransCode.OFFLINE_SETTLEMENT.equals(transType) || TransCode.OFFLINE_ADJUST_TIP.equals(transType)  ) {
            /*
            * 卡组织代码只有3个字节
            * */
            String org = tradeInfoRecord.getBankcardOganization();
            if( org != null && org.length() == 3 ) {
                slipElements.put(ESignatureTlvTag.CARD_ORGANIZATION, org);
            }
        }
        /*交易币种ascii，区别与分期消费的 还款币种*/
        if( tradeInfoRecord.getCurrencyCode() != null) {
            slipElements.put(ESignatureTlvTag.CURRENCY_CODE, tradeInfoRecord.getCurrencyCode());
        }
        slipElements.put(ESignatureTlvTag.MOBILE_PHONE_NUMBER, tradeInfoRecord.getMobile_phone_number());


        //IC卡有关信息
//        CommonDao<TradePbocDetail> pbocDetailDao = new CommonDao<>(TradePbocDetail.class, ePosDbHelper);
//        pbocDetailDao.refresh(tradeInfoRecord.getPbocDetail());
        TradePbocDetail pbocDetailRec = tradeInfoRecord.getPbocDetail();

//        TradePrintData pbocDetailRec = null;
//        CommonDao<TradePrintData> tradePrintDataCommonDao = new CommonDao<>(TradePrintData.class, ePosDbHelper);
//        try {
//            List printDataList = tradePrintDataCommonDao.queryBuilder().where().eq(TradePrintData
//                            .KEY_COLUMN_NAME, voucherNum).query();
//            pbocDetailRec = (TradePrintData) printDataList.get(0);
//        } catch (Exception e) {
//            XLogUtil.e(TAG, "^_^ 读取IC卡信息失败，流水号：" + voucherNum + "\n" + e.getMessage() + " ^_^");
//        }
        if (pbocDetailRec != null) {
            slipElements.put(ESignatureTlvTag.APP_LABLE, pbocDetailRec.getPbocAppLable());
            slipElements.put(ESignatureTlvTag.APP_NAME, pbocDetailRec.getPbocAppName());
            slipElements.put(ESignatureTlvTag.APP_ID, pbocDetailRec.getPbocAID());
            slipElements.put(ESignatureTlvTag.APP_ENCRYPT_DATA, pbocDetailRec.getPbocARQC());
            // TODO: 2017/9/4  圈存类和脱机消费处理：充值后卡片余额、转入卡卡号、不可预知数、应用交互特征、终端验证结果、交易状态信息、应用交易计数器、发卡应用数据

            slipElements.put(ESignatureTlvTag.ECASH_BALANCE,getTransAmount(pbocDetailRec.getPbocECA()));
            slipElements.put(ESignatureTlvTag.RECEIPT_CARD_NUMBER,tradeInfoRecord.getIntoAccount());
            // IC卡脱机交易标签，入网认证时IC卡脱机交易不支持电子签名
//            slipElements.put(ESignatureTlvTag.RANDOM, pbocDetailRec.getPbocUnpredictableNumber());
//            slipElements.put(ESignatureTlvTag.AIP, pbocDetailRec.getPbocAIP());
//            slipElements.put(ESignatureTlvTag.TVR, pbocDetailRec.getPbocTVR());
//            slipElements.put(ESignatureTlvTag.TSI, pbocDetailRec.getPbocTSI());
//            slipElements.put(ESignatureTlvTag.ATC, pbocDetailRec.getPbocATC());
//            slipElements.put(ESignatureTlvTag.IAD, pbocDetailRec.getPbocIAD());
        }

        //添加创新业务信息
        String reverseStr = tradeInfoRecord.getReverseFieldInfo();
        if (!TextUtils.isEmpty(reverseStr))
            slipElements.put(ESignatureTlvTag.REFERENCE_INFO, reverseStr.replace("\n", ""));

        // TODO: 2017/9/4 分期付款期数、分期付款首期金额、分期付款还款币种、持卡人手续费、商品代码、兑换积分数、积分余额、自付金额、承兑金额\可用余额手机号码

        InstallmentInformation information = getInstallmentInfo(tradeInfoRecord.getVoucherNo());
        if( null != information ) {
            slipElements.put(ESignatureTlvTag.INSTALLMENT_PERIOD,information.getPeriod() );
            slipElements.put(ESignatureTlvTag.FIRST_AMOUNT, information.getFeeFirst());
            /*还款币种上送时2字节bcd码，在这里先补位防止 bcd转换时 出现 右补零*/
            slipElements.put(ESignatureTlvTag.INSTALLMENT_CURRENCY_CODE, "0"+information.getCurrencyCode() );
            slipElements.put(ESignatureTlvTag.FEE, "0".equals(information.getPayMode()) ? information.getFeeTotal() : information.getFeeEach() );
        }

        if( TransCode.ISS_INTEGRAL_SALE.equals(transType) || TransCode.UNION_INTEGRAL_SALE.equals(transType) ) {
            IntegralInfo integralInfo = new IntegralInfo(tradeInfoRecord.getIso62Req(), tradeInfoRecord.getIso62Res());
            slipElements.put(ESignatureTlvTag.COMMODITY_CODE, integralInfo.getGoodsCode());
            slipElements.put(ESignatureTlvTag.EXCHANGE_POINTS, integralInfo.getExchangePoints());

            if( tradeInfoRecord.getSBalance() != null ) {
                BalancAmount balancAmount = new BalancAmount(tradeInfoRecord.getSBalance());
                slipElements.put(ESignatureTlvTag.POINTS_BALANCE, String.format(Locale.CHINA, "%012d", (long) balancAmount.getAmount()));
            }

            slipElements.put(ESignatureTlvTag.PAY_AMOUNT, integralInfo.getOutstandingAmount());

        }
//        slipElements.put(ESignatureTlvTag.ACCEPTANCE_AMOUNT, );
//        slipElements.put(ESignatureTlvTag.AVAILABLE_BALANCE, );
        if( TransCode.RESERVATION_SALE.equals(transType) ) {
            String sPhone = getReservationSalePhone(tradeInfoRecord);
            if( sPhone != null )    slipElements.put(ESignatureTlvTag.MASK_PHONE_NUMBER, sPhone);
        }

        //添加原交易信息
        slipElements.put(ESignatureTlvTag.ORIGINAL_VOUCHER_NUMBER, tradeInfoRecord.getOriVoucherNum());

        // TODO: 2017/9/4 原批次号、原终端号（电子现金退货交易时出现）
        slipElements.put(ESignatureTlvTag.ORIGINAL_BATCH_NUMBER,tradeInfoRecord.getOriBatchNo() );
        slipElements.put(ESignatureTlvTag.ORIGINAL_TERMINAL_ID, tradeInfoRecord.getOriTermNo());
       /*  FF62	原参考号	Cn,6	37域	C，退货类（不含电子现金退货）交易出现
        *  FF63	原交易日期	Cn,2	61域	C，退货类交易（包括电子现金退货）出现
        */
        if(!TransCode.E_REFUND.equals(transType)) {
            slipElements.put(ESignatureTlvTag.ORIGINAL_REFERENCE_NUMBER, tradeInfoRecord.getOriRefereceNum());
            slipElements.put(ESignatureTlvTag.ORIGINAL_DATE, tradeInfoRecord.getOriTradeDate());
        }
        /*FF64	原授权码	aN,6	38域	C，预授权撤销、预授权完成（请求）、预授权完成（请求）撤销时输入的原预授权码*/
        if( TransCode.CANCEL.equals(transType)
                || TransCode.COMPLETE_VOID.equals(transType)
                || TransCode.AUTH_COMPLETE.equals(transType) ) {
            slipElements.put(ESignatureTlvTag.ORIGINAL_AUTH_CODE, tradeInfoRecord.getOriAuthCode());
        }

        //添加终端统计信息
//        slipElements.put(ESignatureTlvTag.SLIP_COUNT, "02");

        // TODO: 2017/9/4 添加保留信息 终端不上送？？？
//        slipElements.put(ESignatureTlvTag.ISSUE_NOTICE, );
//        slipElements.put(ESignatureTlvTag.RECEPTION_NOTICE, );
//        slipElements.put(ESignatureTlvTag.UNIONPAY_NOTICE, );

        translate2ESinTLVData(slipElements);
        genJBigInformation();
        return true;
    }

    private void fillISOFieldData(TradeInfoRecord tradeInfoRecord) {
        dataMap.put(TradeInformationTag.DATE_SETTLEMENT, tradeInfoRecord.getSettlmentDate());
        dataMap.put(TradeInformationTag.REFERENCE_NUMBER, tradeInfoRecord.getReferenceNo());
    }

    private boolean genJBigInformation() {
        //最大数据量
        int packageLenMax = BusinessConfig.getInstance().getNumber(context, SimpleStringTag.ESIGN_PACKAGE_LEN_MAX) * 2;

        if (esignJBIGStr == null) {
            if(!uploadingFile.exists()){
                return false;
            }
            esignJBIGStr = bitmaptoJBJGString(uploadingFile.getPath());
            partPackageIndex = 0;
            totalPackageCount = (esignJBIGStr.length() + packageLenMax - 1) / packageLenMax;
        }

        String dataForUpload;
        if (totalPackageCount == 1) {
            dataForUpload = esignJBIGStr;
            partPackageIndex = 1;
            //800表明后续没有“部分电子签字报文”
            dataMap.put(TradeInformationTag.NET_MANAGE_CODE, "800");
        } else {
            //分包处理，分包上送
            int beginOffset = partPackageIndex * packageLenMax;
            dataForUpload = esignJBIGStr.substring(beginOffset, Math.min(beginOffset + packageLenMax, esignJBIGStr
                    .length()));
            partPackageIndex++;
            //801表明当前是签字的第一个包、后续还有“部分电子签字报文”
            dataMap.put(TradeInformationTag.NET_MANAGE_CODE, "80" + partPackageIndex);
            dataMap.put(TradeInformationTag.E_SIGNATURE_UPLOAD_END_FLAG, partPackageIndex == totalPackageCount ? "9" : "8");
        }
        dataMap.put(TradeInformationTag.E_SIGNATURE_DATA, dataForUpload);
        return true;
    }

    /**
     * map数据转换为TLV格式数据 2 byte Tag + 1 byte HEX LEN + data
     *
     * @param slipElements map数据
     */
    private void translate2ESinTLVData(Map<String, String> slipElements) {
        StringBuilder sb = new StringBuilder();
        String key, value;
        byte[] valueBytes;
        int valueLen;
        Set<Map.Entry<String, String>> elements = slipElements.entrySet();
        for (Map.Entry<String, String> entry : elements) {
            key = entry.getKey();
            value = entry.getValue();
            if (TextUtils.isEmpty(value))
                continue;
            try {
                valueBytes = value.getBytes("GBK");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                continue;
            }
            sb.append(key);
            switch (key) {
                case ESignatureTlvTag.MERCHANT_NAME:
                case ESignatureTlvTag.TRANSACTION_TYPE:
                case ESignatureTlvTag.RECEIPT_AGENCY:
                case ESignatureTlvTag.ISSUE_INSTITUTIONS:
                case ESignatureTlvTag.AUTH_CODE:
                case ESignatureTlvTag.CARD_ORGANIZATION:
                case ESignatureTlvTag.CURRENCY_CODE:
                case ESignatureTlvTag.REFERENCE_INFO:
                case ESignatureTlvTag.COMMODITY_CODE:
                case ESignatureTlvTag.MASK_PHONE_NUMBER:
                case ESignatureTlvTag.ORIGINAL_AUTH_CODE:
                case ESignatureTlvTag.ORIGINAL_TERMINAL_ID:
                case ESignatureTlvTag.ISSUE_NOTICE:
                case ESignatureTlvTag.RECEPTION_NOTICE:
                case ESignatureTlvTag.UNIONPAY_NOTICE:
                    //ANS 格式
                    valueLen = valueBytes.length;
                    break;
                default:
                    //CN 格式
                    valueLen = (valueBytes.length + 1) / 2;
            }
            sb.append(String.format(Locale.CHINA, "%02X", valueLen));
            if (valueLen == valueBytes.length)
                //ANS 格式要进行转换，数据域被配置为BCD，即组织报文数据时会被压缩
                sb.append(HexUtil.bytesToHexString(valueBytes));
            else {
                if (valueBytes.length % 2 != 0) {
                    if ( key.equals(ESignatureTlvTag.MOBILE_PHONE_NUMBER) || key.equals(ESignatureTlvTag.RECEIPT_CARD_NUMBER) )/*根据bctc要求转入卡号左补0*/
                        //参考C933终端，左补0
                        value = "0" + value;
                    else
                        value = value + "0";
                }
                sb.append(value);
            }
        }
        XLogUtil.d(TAG, "^_^ 电子签单55域数据：" + sb.toString() + " ^_^");
        dataMap.put(TradeInformationTag.E_SLIP_KEY_DATA, sb.toString());
    }

    private File getESignPicFile() {
//        String path = Config.Path.SIGN_PATH + File.separator + batchIndex + "_" + tradeIndex + ".png";
        if(eSignFile!=null){
           return eSignFile;
        }
        if (esignFiles == null) {
            File dirFile = new File(Config.Path.SIGN_PATH);
            if (!dirFile.exists()) {
                XLogUtil.d(TAG, "签名文件存放路径不存在");
                return null;
            }
            esignFiles = dirFile.listFiles();
        }
        if (esignFiles == null || esignFiles.length == 0 || fileIndex >= esignFiles.length) {
            XLogUtil.d(TAG, "签名图片不存在");
            return null;
        }

        File esignPic = esignFiles[fileIndex];
        String[] fileNames = esignPic.getName().replace(".png", "").split("_");

        if ( (!isUploadAll) || (uploadMode == ConstDefine.ELEC_SIGN_UPLOAD_MODE_2) ) {
            //获取未上送的，且不是当笔交易的签名文件
            do {
                XLogUtil.w(TAG, "fileIndex============>>"+fileIndex);
                esignPic = esignFiles[fileIndex];
                fileNames = esignPic.getName().replace(".png", "").split("_");
                if (fileNames.length == 2) {
                    //leng == 3 代表是上送失败的签名文件；流水号和批次号不等于当笔交易的签名才上送
                    if (!fileNames[0].equals(batchNumber) || !fileNames[1].equals(traceNumber))
                        break;
                }
                fileIndex++;
            } while (fileIndex < esignFiles.length/*fileNames.length*/);
            if ( fileIndex >= esignFiles.length/*fileNames.length*/)
                return null;
        } else {
            if (fileNames.length < 2) {
                XLogUtil.d(TAG, "签名图片文件名错误");
                return null;
            }
        }
        dataMap.put(TradeInformationTag.BATCH_NUMBER, fileNames[0]);
        dataMap.put(TradeInformationTag.TRACE_NUMBER, fileNames[1]);
        return esignPic;
    }

    public static String bitmaptoJBJGString(String bmpFilePath) {
        String jbigStr;
        byte[] jbigData = JBigConvertor.bmpFileToJBIGBytes(bmpFilePath, Config.Path.SIGN_PATH);
        jbigStr = HexUtils.bcd2str(jbigData);
        return jbigStr.replaceAll(" ", "");
    }
}
