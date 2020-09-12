package com.centerm.epos.task;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.centerm.epos.EposApplication;
import com.centerm.epos.bean.TradeInfo;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.bean.TradePbocDetail;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.db.CommonManager;
import com.centerm.epos.redevelop.ICommonManager;
import com.centerm.epos.utils.XLogUtil;
import com.centerm.smartpos.util.HexUtil;

import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by ysd on 2016/12/20.
 */

public abstract class AsyncCheckBillTask extends AsyncTask<Void, Integer, List<List<TradeInfoRecord>>> implements CheckBillkListener {
    protected Logger logger = Logger.getLogger(this.getClass());
    private static final String TAG = AsyncCheckBillTask.class.getSimpleName();
//    private final CommonManager commonManager;
    public AsyncCheckBillTask(Context context) {
//        commonManager = new CommonManager(TradeInfoRecord.class, context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        onStart();
    }

    @Override
    protected List<List<TradeInfoRecord>> doInBackground(Void... params) {
        return getUploadTradeLists();
    }
    /*
    * IC卡完整流程的交易才可以判断arpc认证失败仍然承兑的交易
    * 消费类、预授权、指定账户圈存、非指定账户圈存
    * */
    private static boolean bIsArpcErr(TradeInfoRecord tradeInfo){

        String cardType = tradeInfo.getServiceEntryMode().substring(0, 2);
        String tranType = tradeInfo.getTransType();
        TradePbocDetail pbocDetail = tradeInfo.getPbocDetail();

        XLogUtil.d(TAG, "bIsArpcErr " +"cardType:"+cardType+"tranType:"+tranType);
        if( pbocDetail == null || TransCode.EC_LOAD_CASH.equals(tranType) ){
            return false;
        }
        XLogUtil.w(TAG, "pbocDetail:" + pbocDetail.convert2Map() + " getPbocTVR:"+pbocDetail.getPbocTVR());
        if( ("05".equals(cardType) && TransCode.FULL_PBOC_SETS.contains(tranType))
            || ("07".equals(cardType) && (TransCode.EC_LOAD_INNER.equals(tranType) || TransCode.EC_LOAD_OUTER.equals(tranType))) )
        {
            byte[] bArray = HexUtil.hexStringToByte(pbocDetail.getPbocTVR());
            XLogUtil.w(TAG, "bArray:" + String.format(Locale.CHINA,"%02X ",bArray[4]));
            return ((bArray[4]&0x40) == 0x40);
        }
        return false;
    }
    @NonNull
    public static List<List<TradeInfoRecord>> getUploadTradeLists() {
        ICommonManager commonManager = (ICommonManager) ConfigureManager.getInstance(EposApplication.getAppContext()).getSubPrjClassInstance(new CommonManager());
        //根据规范要求，上送数据分为7大类
        List<TradeInfoRecord> magsCardsOffline = new ArrayList<>(); //磁条卡离线交易
        List<TradeInfoRecord> ecOfflineApproved = new ArrayList<>(); //ic卡脱机交易批准
        List<TradeInfoRecord> magsCards = new ArrayList<>(); /*全部磁条卡的请求类联机成功交易明细*/
        List<TradeInfoRecord> refundInfos = new ArrayList<>(); /*通知类交易 磁条卡通知和ic通知*/
        List<TradeInfoRecord> icCards = new ArrayList<>(); /*IC卡交易TC值*/
        List<TradeInfoRecord> ecOfflineDenial = new ArrayList<>(); //ic卡脱机失败
        List<TradeInfoRecord> icCardsArpcErr = new ArrayList<>(); /*IC卡交易TC值*/

        List<List<TradeInfoRecord>> lists = new ArrayList<>();
        try {
            List<TradeInfoRecord> transInfos = commonManager.getListForBatch();
            if (null != transInfos) {
                XLogUtil.d(TAG, "当前批次交易成功的记录有：" + transInfos.size());
                for (TradeInfoRecord tradeInfo : transInfos) {
                    String cardType = "07";
                    if(!TextUtils.isEmpty(tradeInfo.getServiceEntryMode())){
                        cardType = tradeInfo.getServiceEntryMode().substring(0, 2);
                    }
                    String tranType = tradeInfo.getTransType();
                    /*
                    *磁条卡通知和IC卡通知上送格式不同
                    * */
                    if (TransCode.NOTIFY_TRADE_SETS.contains(tranType) ){
                        refundInfos.add(tradeInfo);
                    }else if ( TransCode.AUTH.equals(tranType)
                               || TransCode.EC_LOAD_OUTER.equals(tranType)
                               || TransCode.EC_LOAD_INNER.equals(tranType)
                               || TransCode.EC_LOAD_CASH.equals(tranType)
                               || TransCode.EC_VOID_CASH_LOAD.equals(tranType) ) {
                        //预授权业务，插卡时要上送TC值，否则不用上送
                        if( bIsArpcErr(tradeInfo) ){
                            icCardsArpcErr.add(tradeInfo);
                        }
                        else if ("05".equals(cardType)){
                            icCards.add(tradeInfo);
                        }
                    }else if("07".equals(cardType) && (TransCode.SALE.equals(tranType) || TransCode.SALE_NEED_PIN
                            .equals(tranType)  || TransCode.SALE_INSTALLMENT.equals(tranType))){
                        // TODO: 2017/6/30 目前只考虑挥卡走快速PBOC流程的情况
                        //规范描述：快速PBOC交易不用上送，目前只有消费、闪付凭密和预授权交易。
                        continue;
                    }
                    else if( TransCode.E_QUICK.equals(tranType) || TransCode.E_COMMON.equals(tranType) ){
                        List<TradeInfoRecord> l = ( tradeInfo.getTransStatus() != 0x0800 ) ? ecOfflineApproved : ecOfflineDenial;
                        l.add(tradeInfo);
                    }
                    else if ("05".equals(cardType)
                              && ( TransCode.SALE.equals(tranType) || TransCode.SALE_INSERT.equals(tranType)
                                   || TransCode.SALE_INSTALLMENT.equals(tranType)
                                   || TransCode.ISS_INTEGRAL_SALE.equals(tranType)
                                   || TransCode.UNION_INTEGRAL_SALE.equals(tranType) )) {

                        if(bIsArpcErr(tradeInfo) ){
                            icCardsArpcErr.add(tradeInfo);
                        }
                        else{
                            icCards.add(tradeInfo);
                        }
                    } else {
                        //过滤扫码交易
                        if(!tranType.contains("SCAN")){
                            magsCards.add(tradeInfo);
                        }
                    }
                }
                XLogUtil.d(TAG, "其中退货：" + refundInfos.size());
                XLogUtil.d(TAG, "其中ic卡：" + icCards.size());
                XLogUtil.d(TAG, "ecOfflineApproved：" + ecOfflineApproved.size());
                XLogUtil.d(TAG, "ecOfflineDenial：" + ecOfflineDenial.size());
                XLogUtil.d(TAG, "icCardsArpcErr：" + icCardsArpcErr.size());

            } else {
                XLogUtil.e(TAG, "当前批次没有成功的交易");
            }
            //退货成功的流水
//            refundInfos = commonManager.getRefundList();
//            if (null != refundInfos) {
//                logger.debug("当前批次退货记录：" + refundInfos.size());
//            } else {
//                logger.debug("当前批次没有退货记录");
//            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        lists.add(magsCards);
        lists.add(icCards);
        lists.add(refundInfos);
        /*
        * 电子现金交易增加以下处理
        * */
        lists.add(ecOfflineApproved);
        lists.add(ecOfflineDenial);
        lists.add(icCardsArpcErr);
        return lists;
    }

    @Override
    protected void onPostExecute(List<List<TradeInfoRecord>> lists) {
        super.onPostExecute(lists);
        onFinish(lists);
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onFinish(List<List<TradeInfoRecord>> lists) {

    }
}
