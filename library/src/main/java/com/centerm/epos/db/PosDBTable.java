package com.centerm.epos.db;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import com.centerm.epos.EposApplication;
import com.centerm.epos.bean.BinData;
import com.centerm.epos.bean.ElecSignInfo;
import com.centerm.epos.bean.Employee;
import com.centerm.epos.bean.PrinterItem;
import com.centerm.epos.bean.QpsBlackBinData;
import com.centerm.epos.bean.ReverseInfo;
import com.centerm.epos.bean.ScriptInfo;
import com.centerm.epos.bean.TradeInfo;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.bean.TradePbocDetail;
import com.centerm.epos.bean.TradePrintData;
import com.centerm.epos.bean.TradeRecordForUpload;
import com.centerm.epos.bean.iso.Iso62Aid;
import com.centerm.epos.bean.iso.Iso62Capk;
import com.centerm.epos.bean.iso.Iso62Qps;
import com.centerm.epos.bean.transcation.InstallmentInformation;
import com.centerm.epos.bean.transcation.RequestMessage;
import com.centerm.epos.utils.XLogUtil;
import com.centerm.epos.xml.bean.slip.SlipElement;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import config.BusinessConfig;
import config.Config;

/**
 * Created by yuhc on 2017/7/31.
 *
 */

public class PosDBTable implements IProjectDBTable {
    private List<Class> clses = new ArrayList<>();

    @Override
    public List<Class> registerDBClass() {
        clses.add(Employee.class);
        clses.add(PrinterItem.class);
        clses.add(ReverseInfo.class);
        clses.add(TradeInfo.class);
        clses.add(ElecSignInfo.class);
        clses.add(TradePrintData.class);
        clses.add(BinData.class);
        clses.add(QpsBlackBinData.class);
        clses.add(Iso62Capk.class);
        clses.add(Iso62Aid.class);
        clses.add(Iso62Qps.class);
        clses.add(SlipElement.class);
        clses.add(TradeInfoRecord.class);
        clses.add(TradePbocDetail.class);
        clses.add(RequestMessage.class);
        clses.add(InstallmentInformation.class);
        clses.add(ScriptInfo.class);/*脚本信息保存，脚本信息只保存一条，上送玩，立刻删掉脚本*/
        return clses;
    }

    @Override
    public void onDBUpdate(DbHelper dbHelper, SQLiteDatabase database, ConnectionSource connectionSource, int
            oldVersion, int newVersion) {
        try {
//            removeTables(connectionSource);
            registerDBClass();
            for (Class cls : clses) {
                TableUtils.createTableIfNotExists(connectionSource, cls);
            }

            if(oldVersion < 23){
                dbHelper.getDao(TradeInfoRecord.class).executeRaw("ALTER TABLE `tb_trade_information` ADD COLUMN " +
                        "superviseFlag TEXT DEFAULT '';");
                dbHelper.getDao(TradeInfoRecord.class).executeRaw("ALTER TABLE `tb_trade_information` ADD COLUMN " +
                        "areaCode TEXT DEFAULT '';");
            }

            if(oldVersion < 22){
                dbHelper.getDao(TradeInfoRecord.class).executeRaw("ALTER TABLE `tb_trade_information` ADD COLUMN " +
                        "templateId TEXT DEFAULT '';");
                dbHelper.getDao(TradeInfoRecord.class).executeRaw("ALTER TABLE `tb_trade_information` ADD COLUMN " +
                        "settlementInfo TEXT DEFAULT '';");
            }
            if(oldVersion < 17){
                dbHelper.getDao(TradeInfoRecord.class).executeRaw("ALTER TABLE `tb_trade_information` ADD COLUMN " +
                        "organizationCode TEXT DEFAULT '';");
                dbHelper.getDao(TradeInfoRecord.class).executeRaw("ALTER TABLE `tb_trade_information` ADD COLUMN " +
                        "originalAuthMode TEXT DEFAULT '';");
            }
            if (oldVersion < 16){
                dbHelper.getDao(TradeInfoRecord.class).executeRaw("ALTER TABLE `tb_trade_information` ADD COLUMN " +
                        "sBalance TEXT DEFAULT '';");
            }
            if (oldVersion < 15) {
                dbHelper.getDao(TradeInfoRecord.class).executeRaw("ALTER TABLE `tb_trade_information` ADD COLUMN " +
                        "iso62Req TEXT DEFAULT '';");
                dbHelper.getDao(TradeInfoRecord.class).executeRaw("ALTER TABLE `tb_trade_information` ADD COLUMN " +
                        "iso62Res TEXT DEFAULT '';");
            }
            /*
            *@author zhouzhihuaoriTermNo
            * 电子现金脱机退货增加 原终端号和原批次号
            * */
            if (oldVersion < 14) {
                dbHelper.getDao(TradeInfoRecord.class).executeRaw("ALTER TABLE `tb_trade_information` ADD COLUMN " +
                        "oriTermNo TEXT DEFAULT '';");
                dbHelper.getDao(TradeInfoRecord.class).executeRaw("ALTER TABLE `tb_trade_information` ADD COLUMN " +
                        "oriBatchNo TEXT DEFAULT '';");
                dbHelper.getDao(TradeInfoRecord.class).executeRaw("ALTER TABLE `tb_trade_information` ADD COLUMN " +
                        "transStatus INTEGER DEFAULT '';");
                dbHelper.getDao(TradeInfoRecord.class).executeRaw("ALTER TABLE `tb_trade_information` ADD COLUMN " +
                        "offlineTransUploadStatus TEXT DEFAULT '';");
                dbHelper.getDao(TradePbocDetail.class).executeRaw("ALTER TABLE `tb_trade_pboc_detail` ADD COLUMN " +
                        "pbocAAC TEXT DEFAULT '';");
            }

            if (oldVersion < 9){
                dbHelper.getDao(TradeInfoRecord.class).executeRaw("ALTER TABLE `tb_trade_information` ADD COLUMN " +
                        "mobile_phone_number TEXT DEFAULT '';");
                dbHelper.getDao(TradeInfoRecord.class).executeRaw("ALTER TABLE `tb_trade_information` ADD COLUMN " +
                        "transYear TEXT DEFAULT '';");
                dbHelper.getDao(TradeInfoRecord.class).executeRaw("ALTER TABLE `tb_trade_information` ADD COLUMN " +
                        "settlmentDate TEXT DEFAULT '';");
            }
            if (oldVersion < 7){
                //SQLite不支持同时添加多列，所以只能一个一个加
                dbHelper.getDao(TradeInfoRecord.class).executeRaw("ALTER TABLE `tb_trade_information` ADD COLUMN " +
                        "reverseFieldInfo TEXT DEFAULT '';");
                dbHelper.getDao(TradeInfoRecord.class).executeRaw("ALTER TABLE `tb_trade_information` ADD COLUMN " +
                        "unicom_scna_type TEXT DEFAULT '';");
            }
            //通知应用进行数据库升级后的处理
            EposApplication.getAppContext().sendBroadcast(new Intent("com.centerm.epos.db.UPDATE_COMPLETED"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(DbHelper dbHelper, SQLiteDatabase database, ConnectionSource connectionSource) {
        database.beginTransaction();
        try {
            final String sql = "INSERT INTO tb_employee (code, password) VALUES ('%s', '%s')";
            database.execSQL(String.format(sql, Config.DEFAULT_ADMIN_ACCOUNT, BusinessConfig.getInstance().getValue
                    (EposApplication.getAppContext(), BusinessConfig.Key.OPERATOR_MANAGER_PWD)));
            database.execSQL(String.format(sql, Config.DEFAULT_MSN_ACCOUNT, Config.DEFAULT_MSN_PWD));
            for (int i = 0; i < 5; i++) {
                database.execSQL(String.format(sql, "0" + (i + 1), "0000"));
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
        }
    }
}
