package com.centerm.epos.db;

import android.content.Context;

import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.common.ConstDefine;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.redevelop.ICommonManager;
import com.centerm.epos.utils.XLogUtil;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import config.Config;

/**
 * Created by ysd on 2016/12/13.
 *
 * modified by fl on 20170728 使用交易集合来获取相关数据
 */

public class CommonManager<T extends Object> implements ICommonManager {

    protected CommonDao commonDao;
    private static final String TRANSTYPE_COLUMN_NAME = "transType";

    public CommonManager(Class<T> clz, Context context) {
        commonDao = new CommonDao<>(clz, DbHelper.getInstance());
    }

    public CommonManager() {
        commonDao = new CommonDao<>(TradeInfoRecord.class, DbHelper.getInstance());
    }

    private Where getInvalidTrans(Where where) throws SQLException{
        Where wh1 = where.not().eq("transStatus", ConstDefine.TRANS_STATUS_REFUSED).and().not().eq("stateFlag", ConstDefine.TRANS_STATE_ADJUST_UNUSE);
        Where wh2 = where.eq(TRANSTYPE_COLUMN_NAME, TransCode.SALE).and().ne("stateFlag", ConstDefine.TRANS_STATE_ADJUST);
        Where wh5 = where.ne(TRANSTYPE_COLUMN_NAME, TransCode.SALE);
        Where wh3 = where.or(wh2,wh5);
        Where wh4 = where.and(wh1,wh3);

        return wh4;
    }

    //获取借记交易列表
    /*增加过滤脱机拒绝的交易*/
    public List<T> getDebitList() throws SQLException {
        CommonDao commonDao = new CommonDao<>(TradeInfoRecord.class, DbHelper.getInstance());
        Where<T, String> jiejiWhere = commonDao.queryBuilder().where();

        jiejiWhere.or(
                jiejiWhere.eq(TRANSTYPE_COLUMN_NAME, TransCode.SCAN_PAY),
                jiejiWhere.eq(TRANSTYPE_COLUMN_NAME, TransCode.SALE),
                jiejiWhere.eq(TRANSTYPE_COLUMN_NAME, TransCode.SALE_INSTALLMENT),
                //jiejiWhere.eq(TRANSTYPE_COLUMN_NAME, TransCode.SALE_SCAN),
                jiejiWhere.eq(TRANSTYPE_COLUMN_NAME, TransCode.AUTH_COMPLETE),
                jiejiWhere.eq(TRANSTYPE_COLUMN_NAME, TransCode.OFFLINE_SETTLEMENT),
                jiejiWhere.eq(TRANSTYPE_COLUMN_NAME, TransCode.OFFLINE_ADJUST),
                jiejiWhere.eq(TRANSTYPE_COLUMN_NAME, TransCode.E_QUICK),
                jiejiWhere.eq(TRANSTYPE_COLUMN_NAME, TransCode.E_COMMON),
                jiejiWhere.eq(TRANSTYPE_COLUMN_NAME, TransCode.EC_LOAD_OUTER),
                jiejiWhere.eq(TRANSTYPE_COLUMN_NAME, TransCode.ISS_INTEGRAL_SALE),
                jiejiWhere.eq(TRANSTYPE_COLUMN_NAME, TransCode.UNION_INTEGRAL_SALE),
                jiejiWhere.eq(TRANSTYPE_COLUMN_NAME, TransCode.RESERVATION_SALE)
        );

        for (String transCode : TransCode.DEBIT_SETS) {
            jiejiWhere.or(jiejiWhere, jiejiWhere.eq("transType", transCode));
        }
        jiejiWhere.and().ne("transStatus", 0x0800);
        List<T> jiejiList = jiejiWhere.query();

        DbHelper.releaseInstance();
        return jiejiList;
    }

    //获取贷记交易列表
    public List<T> getCreditList() throws SQLException {
        CommonDao commonDao = new CommonDao<>(TradeInfoRecord.class, DbHelper.getInstance());
        Where<T, String> daijiWhere = commonDao.queryBuilder().where();
        daijiWhere.or(
                daijiWhere.eq(TRANSTYPE_COLUMN_NAME, TransCode.SCAN_VOID),
                daijiWhere.eq(TRANSTYPE_COLUMN_NAME, TransCode.VOID),
                daijiWhere.eq(TRANSTYPE_COLUMN_NAME, TransCode.VOID_INSTALLMENT),
                daijiWhere.eq(TRANSTYPE_COLUMN_NAME, TransCode.VOID_SCAN),
                daijiWhere.eq(TRANSTYPE_COLUMN_NAME, TransCode.COMPLETE_VOID),
                daijiWhere.eq(TRANSTYPE_COLUMN_NAME, TransCode.REFUND),
                daijiWhere.eq(TRANSTYPE_COLUMN_NAME, TransCode.REFUND_SCAN),
                daijiWhere.eq(TRANSTYPE_COLUMN_NAME, TransCode.EC_LOAD_CASH),
                daijiWhere.eq(TRANSTYPE_COLUMN_NAME, TransCode.E_REFUND),
                daijiWhere.eq(TRANSTYPE_COLUMN_NAME, TransCode.ISS_INTEGRAL_VOID),
                daijiWhere.eq(TRANSTYPE_COLUMN_NAME, TransCode.UNION_INTEGRAL_VOID),
                daijiWhere.eq(TRANSTYPE_COLUMN_NAME, TransCode.UNION_INTEGRAL_REFUND),
                daijiWhere.eq(TRANSTYPE_COLUMN_NAME, TransCode.MAG_CASH_LOAD),
                daijiWhere.eq(TRANSTYPE_COLUMN_NAME, TransCode.RESERVATION_VOID));

        for (String transCode : TransCode.CREDIT_SETS) {
            daijiWhere.or(daijiWhere, daijiWhere.eq("transType", transCode));
        }
        daijiWhere.and().ne("transStatus", 0x0800);
        List<T> daijiList = daijiWhere.query();
        DbHelper.releaseInstance();
        return daijiList;
    }

    //获取交易明细列表
    public List<T> getTransDetail() throws SQLException {
        CommonDao commonDao = new CommonDao<>(TradeInfoRecord.class, DbHelper.getInstance());
        Where<T, String> where = commonDao.queryBuilder().orderBy("voucherNo", true).where();
        where.and(
                where.ne("cardNo", "-1"),
                where.ne("cardNo", "F"),
                where.ne("cardNo", "R"),
                where.ne("cardNo", "O"),
                where.ne("cardNo", "N"),
                where.ne("cardNo", "PA"),
                where.ne("cardNo", "PB"),
                where.ne("cardNo", "P3"),
                where.ne("cardNo", "P5"));
        List<T> saleDetailList = where.query();
        DbHelper.releaseInstance();
        return saleDetailList;
    }

    //获取批结算被拒绝的交易流水
    public List<T> getRefusedList() throws SQLException {
        CommonDao commonDao = new CommonDao<>(TradeInfoRecord.class, DbHelper.getInstance());
        List refusedList = commonDao.queryBuilder().orderBy("voucherNo", true).where().eq("sendCount", 99).query();
        DbHelper.releaseInstance();
        return refusedList;
    }

    //批结算时获取上送失败的交易流水
    public List<T> getFailList() throws SQLException {
        CommonDao commonDao = new CommonDao<>(TradeInfoRecord.class, DbHelper.getInstance());
        List failList = commonDao.queryBuilder().orderBy("voucherNo", true)
                .where().ge("sendCount", Config.BATCH_MAX_UPLOAD_TIMES).and().ne("sendCount", 99)
                .and().eq("isBatchSuccess", false).query();
        DbHelper.releaseInstance();
        return failList;
    }

    //获取可以批结算的交易流水
    public List<T> getBatchList() throws SQLException {
        CommonDao commonDao = new CommonDao<>(TradeInfoRecord.class, DbHelper.getInstance());
        Where<T, String> where = commonDao.queryBuilder().where();
        where.and(
                where.ne(TRANSTYPE_COLUMN_NAME, TransCode.AUTH),
                where.ne(TRANSTYPE_COLUMN_NAME, TransCode.CANCEL),
                //where.ne("cardNo", "S"),
                where.ne("cardNo", "-1"),
                where.ne("cardNo", "F"),
                where.ne("cardNo", "R"),
                where.ne("cardNo", "O"),
                where.ne("cardNo", "N"),
                where.ne("cardNo", "PA"),
                where.ne("cardNo", "PB"),
                where.ne("cardNo", "P3"),
                where.ne("cardNo", "P5")
        );
        List<T> tradeInfos = where.query();
        DbHelper.releaseInstance();
        return tradeInfos;
    }

    //获取可以批结算的交易流水个数
    /*
    * 注意该数据包涵ic卡脱机拒绝的交易
    * */
    public long getBatchCount() throws SQLException {
        Where where = commonDao.queryBuilder().setCountOf(true).where();
        where.and(
                where.ne(TRANSTYPE_COLUMN_NAME, TransCode.AUTH),
                where.ne(TRANSTYPE_COLUMN_NAME, TransCode.CANCEL),
                //where.ne("cardNo", "S"),
                where.ne("cardNo", "-1"),
                where.ne("cardNo", "F"),
                where.ne("cardNo", "R"),
                where.ne("cardNo", "O"),
                where.ne("cardNo", "N"),
                where.ne("cardNo", "PA"),
                where.ne("cardNo", "PB"),
                where.ne("cardNo", "P3"),
                where.ne("cardNo", "P5")
        );
        PreparedQuery preparedQuery = where.prepare();
        return commonDao.countOf(preparedQuery);
    }

    //获取降序的交易记录用于打印最后一笔交易
    public List<T> getLastTransItem() throws SQLException {
        QueryBuilder infoWhere = commonDao.queryBuilder().orderBy("voucherNo", false);
        Where where = infoWhere.where();
        where.and(
            where.ne("transStatus", 0x0800),/*增加过滤脱机拒绝的交易*/
            where.ne("cardNo", "-1"),
            where.ne("cardNo", "F"),
            where.ne("cardNo", "R"),
            where.ne("cardNo", "O"),
            where.ne("cardNo", "N"),
            where.ne("cardNo", "PA"),
            where.ne("cardNo", "PB"),
            where.ne("cardNo", "P3"),
            where.ne("cardNo", "P5")
        );
        return infoWhere.query();
    }

    //获取有效的批上送交易流水
    public List<T> getListForBatch() throws SQLException {
        Where<T, String> where = commonDao.queryBuilder().where();
        where.and(
                where.ne(TRANSTYPE_COLUMN_NAME, TransCode.CANCEL),
                where.ne(TRANSTYPE_COLUMN_NAME, TransCode.SALE_SCAN),
                where.ne(TRANSTYPE_COLUMN_NAME, "SALE_SCAN_QUERY"),
                where.ne(TRANSTYPE_COLUMN_NAME, "SALE_SCAN_VOID"),
                where.ne(TRANSTYPE_COLUMN_NAME, "SALE_SCAN_VOID_QUERY"),
                where.ne(TRANSTYPE_COLUMN_NAME, "SALE_SCAN_REFUND"),
                where.ne(TRANSTYPE_COLUMN_NAME, "SALE_SCAN_REFUND_QUERY"),
                where.eq("isBatchSuccess", false),
                where.le("sendCount", Config.BATCH_MAX_UPLOAD_TIMES));
        List<T> transInfos = where.query();
        return transInfos;
    }

    //获取有效的批结算退货交易流水
    public List<T> getRefundList() throws SQLException {
        return commonDao.queryBuilder()
                .where().eq(TRANSTYPE_COLUMN_NAME, TransCode.REFUND).eq(TRANSTYPE_COLUMN_NAME, TransCode.E_REFUND)
                .and().eq("isBatchSuccess", false)
                .and().le("sendCount", Config.BATCH_MAX_UPLOAD_TIMES)
                .query();
    }

    //获取有效的批上送交易流水
    /*增加过滤脱机拒绝的交易*/
    public int getBatchSendRecordCount() throws SQLException {
        Where<T, String> where = commonDao.queryBuilder().where();
        where.and(
                where.ne(TRANSTYPE_COLUMN_NAME, TransCode.CANCEL),
                where.eq("isBatchSuccess", true));
        where.and().ne("transStatus", 0x0800);
        List<T> transInfos = where.query();
        return transInfos.size();
    }
    /*
    *获所有的取离线交易信息,有效的离线交易
    */
    public List<T> getOfflineTransList() throws SQLException {
        CommonDao commonDao = new CommonDao<>(TradeInfoRecord.class, DbHelper.getInstance());

        Where<T, String> offlineTrans = commonDao.queryBuilder().where();

        offlineTrans.or( offlineTrans.eq(TRANSTYPE_COLUMN_NAME, TransCode.E_QUICK),
                         offlineTrans.eq(TRANSTYPE_COLUMN_NAME, TransCode.E_COMMON),
                         offlineTrans.eq(TRANSTYPE_COLUMN_NAME, TransCode.OFFLINE_SETTLEMENT));

        offlineTrans.and().ne("transStatus", 0x0800);
        List<T> offlineTransList = offlineTrans.query();

        DbHelper.releaseInstance();
        return offlineTransList;
    }
    /**
     * {@link ConstDefine#OFFLINE_TRANS_STATUS_UPLOAD_SUCCESS} <br/>
     * {@link ConstDefine#OFFLINE_TRANS_STATUS_UPLOAD_FAIL} <br/>
     * {@link ConstDefine#OFFLINE_TRANS_STATUS_UPLOAD_NORES} <br/>
     * {@link ConstDefine#OFFLINE_TRANS_STATU_UPLOAD_SUCCESS} <br/>
     * {@link ConstDefine#OFFLINE_TRANS_STATU_UPLOAD_FAIL} <br/>
     * {@link ConstDefine#OFFLINE_TRANS_STATU_UPLOAD_NORES} <br/>
    * @param iFalg 获取指定状态的离线交易 0x1000-离线交易上送成功，
    * 0x2000-离线交易上送失败  0x4000-后台无应答
    * zhouzhihua 2017-12-26，离线交易增加
    * */
    public List<T> getOfflineTransList(int iFalg) throws SQLException {
        CommonDao commonDao = new CommonDao<>(TradeInfoRecord.class, DbHelper.getInstance());
        int successFlag = iFalg & 0x1000;
        int failFlag = iFalg & 0x2000;
        int norespFlag = iFalg & 0x4000;
        String columnName = "offlineTransUploadStatus";

        String success = ConstDefine.OFFLINE_TRANS_STATUS_UPLOAD_SUCCESS;
        String failed = ConstDefine.OFFLINE_TRANS_STATUS_UPLOAD_FAIL;
        String nores = ConstDefine.OFFLINE_TRANS_STATUS_UPLOAD_NORES;

        Where<T, String> offlineTrans = commonDao.queryBuilder().where();
        XLogUtil.w("getOfflineTransList",String.format(Locale.CHINA,"iFalg=0x%02X,successFlag=0x%02X,failFlag=0x%02X,norespFlag=0x%02X",iFalg,successFlag,failFlag,norespFlag));
        if( (0x2000|0x1000|0x4000) == iFalg ){
            offlineTrans.and(offlineTrans.or(offlineTrans.eq(TRANSTYPE_COLUMN_NAME, TransCode.E_QUICK),
                    offlineTrans.eq(TRANSTYPE_COLUMN_NAME, TransCode.E_COMMON)),
                    offlineTrans.eq(TRANSTYPE_COLUMN_NAME, TransCode.OFFLINE_SETTLEMENT),
                    offlineTrans.or(offlineTrans.eq(columnName, success), offlineTrans.eq(columnName, failed)
                    ,offlineTrans.eq(columnName, nores)));
        }
        else if((0x2000|0x1000) == iFalg ){
            offlineTrans.and(offlineTrans.or(offlineTrans.eq(TRANSTYPE_COLUMN_NAME, TransCode.E_QUICK),
                    offlineTrans.eq(TRANSTYPE_COLUMN_NAME, TransCode.E_COMMON)),
                    offlineTrans.eq(TRANSTYPE_COLUMN_NAME, TransCode.OFFLINE_SETTLEMENT),
                    offlineTrans.or(offlineTrans.eq(columnName, success), offlineTrans.eq(columnName, failed)));
        }
        else if((0x2000|0x4000) == iFalg ){
            offlineTrans.and(offlineTrans.or(offlineTrans.eq(TRANSTYPE_COLUMN_NAME, TransCode.E_QUICK),
                    offlineTrans.eq(TRANSTYPE_COLUMN_NAME, TransCode.E_COMMON)),
                    offlineTrans.eq(TRANSTYPE_COLUMN_NAME, TransCode.OFFLINE_SETTLEMENT),
                    offlineTrans.or(offlineTrans.eq(columnName, success), offlineTrans.eq(columnName, nores)));
        }
        else if((0x4000|0x1000) == iFalg ){
            offlineTrans.and(offlineTrans.or(offlineTrans.eq(TRANSTYPE_COLUMN_NAME, TransCode.E_QUICK),
                    offlineTrans.eq(TRANSTYPE_COLUMN_NAME, TransCode.E_COMMON)),
                    offlineTrans.eq(TRANSTYPE_COLUMN_NAME, TransCode.OFFLINE_SETTLEMENT),
                    offlineTrans.or(offlineTrans.eq(columnName, success), offlineTrans.eq(columnName, nores)));
        }
        else {
            String sFalg = (successFlag == 0) ? ( ( failFlag == 0) ? ( (norespFlag == 0) ? ConstDefine.OFFLINE_TRANS_STATUS_UPLOAD_NORMAL : nores ) : failed ) : success;
            XLogUtil.w("getOfflineTransList",
                    String.format(Locale.CHINA,"else process iFalg=0x%02X,successFlag=0x%02X,failFlag=0x%02X,norespFlag=0x%02X",
                            iFalg,successFlag,failFlag,norespFlag)+" sFalg:"+sFalg);
            offlineTrans.or(offlineTrans.eq(TRANSTYPE_COLUMN_NAME, TransCode.E_QUICK),
                            offlineTrans.eq(TRANSTYPE_COLUMN_NAME, TransCode.E_COMMON),
                            offlineTrans.eq(TRANSTYPE_COLUMN_NAME, TransCode.OFFLINE_SETTLEMENT)).and().eq(columnName, sFalg);
        }
        offlineTrans.and().ne("transStatus", 0x0800);
        List<T> offlineTransList = offlineTrans.query();

        DbHelper.releaseInstance();
        return offlineTransList;
    }
    /*
    * ic卡特别交易，脱机拒绝的交易
    * */
    public List<T> getIccSpecialList() throws SQLException {
        CommonDao commonDao = new CommonDao<>(TradeInfoRecord.class, DbHelper.getInstance());

        Where<T, String> offlineTrans = commonDao.queryBuilder().where();

        offlineTrans.or( offlineTrans.eq(TRANSTYPE_COLUMN_NAME, TransCode.E_QUICK),
                offlineTrans.eq(TRANSTYPE_COLUMN_NAME, TransCode.E_COMMON) );

        offlineTrans.and().eq("transStatus", 0x0800);
        List<T> offlineTransList = offlineTrans.query();

        DbHelper.releaseInstance();
        return offlineTransList;
    }

    //扫码消费交易流水
    public List<T> getScanSaleList() throws SQLException {
        CommonDao commonDao = new CommonDao<>(TradeInfoRecord.class, DbHelper.getInstance());
        Where<T, String> where = commonDao.queryBuilder().orderBy("voucherNo", true).where();
        where.and(
                where.eq(TRANSTYPE_COLUMN_NAME, TransCode.SALE_SCAN),
                where.ne("cardNo", "-1"),
                where.ne("cardNo", "F"),
                where.ne("cardNo", "R"),
                where.ne("cardNo", "O"),
                where.ne("cardNo", "N"),
                where.ne("cardNo", "PA"),
                where.ne("cardNo", "PB"),
                where.ne("cardNo", "P3"),
                where.ne("cardNo", "P5"));
        List<T> saleDetailList = where.query();
        DbHelper.releaseInstance();
        return saleDetailList;
    }

    //扫码撤销交易流水
    public List<T> getScanVoidList() throws SQLException {
        CommonDao commonDao = new CommonDao<>(TradeInfoRecord.class, DbHelper.getInstance());
        Where<T, String> where = commonDao.queryBuilder().orderBy("voucherNo", true).where();
        where.and(
                where.eq(TRANSTYPE_COLUMN_NAME, "SALE_SCAN_VOID"),
                where.ne("cardNo", "-1"),
                where.ne("cardNo", "F"),
                where.ne("cardNo", "R"),
                where.ne("cardNo", "O"),
                where.ne("cardNo", "N"),
                where.ne("cardNo", "PA"),
                where.ne("cardNo", "PB"),
                where.ne("cardNo", "P3"),
                where.ne("cardNo", "P5"));
        List<T> saleDetailList = where.query();
        DbHelper.releaseInstance();
        return saleDetailList;
    }

    //扫码退货交易流水
    public List<T> getScanRefundList() throws SQLException {
        CommonDao commonDao = new CommonDao<>(TradeInfoRecord.class, DbHelper.getInstance());
        Where<T, String> where = commonDao.queryBuilder().orderBy("voucherNo", true).where();
        where.and(
                where.eq(TRANSTYPE_COLUMN_NAME, "SALE_SCAN_REFUND"),
                where.ne("cardNo", "-1"),
                where.ne("cardNo", "F"),
                where.ne("cardNo", "R"),
                where.ne("cardNo", "O"),
                where.ne("cardNo", "N"),
                where.ne("cardNo", "PA"),
                where.ne("cardNo", "PB"),
                where.ne("cardNo", "P3"),
                where.ne("cardNo", "P5"));
        List<T> saleDetailList = where.query();
        DbHelper.releaseInstance();
        return saleDetailList;
    }

}
