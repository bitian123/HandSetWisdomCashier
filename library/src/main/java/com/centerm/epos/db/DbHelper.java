package com.centerm.epos.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.centerm.epos.EposApplication;
import com.centerm.epos.base.SimpleStringTag;
import com.centerm.epos.common.Settings;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.redevelop.IRedevelopAction;
import com.centerm.epos.utils.XLogUtil;
import com.centerm.epos.xml.bean.slip.SlipElement;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.Config;


/**
 * author:wanliang527</br>
 * date:2016/10/22</br>
 */

public class DbHelper extends OrmLiteSqliteOpenHelper {
    private static final String TAG = DbHelper.class.getSimpleName();

    //项目数据库版本号，使用静态数据保存，以便快速频繁读取。
    private static int mProjectDBVersion = -1;
    // 数据库版本号，版本号由基础框架版本号 + 应用项目版本号组成。支持应用项目版本号变更时，触发数据库更新。
    private final static int VERSION = (Config.DB_VERSION + getProjectDBVersion());

    // 数据库名
    private final static String DB_NAME = Config.DB_NAME;
    private Map<String, Dao> daos = new HashMap<>();

    public DbHelper(Context mCtx) {
        super(mCtx.getApplicationContext(), DB_NAME, null, VERSION);
    }

    //获取项目的数据库版本号
    private static int getProjectDBVersion() {
        if (mProjectDBVersion < 0) {
            IRedevelopAction prjDBVersion = ConfigureManager.getProjectClassInstance(IRedevelopAction
                    .PROJECT_DB_VERSION);
            int prjVersion = 0;
            if (prjDBVersion != null) {
                String version = (String) prjDBVersion.doAction();
                if (!TextUtils.isEmpty(version))
                    prjVersion = Integer.parseInt(version);
            }
            mProjectDBVersion = prjVersion;
        }
        XLogUtil.d(TAG, "^_^ 应用项目数据库版本号：" + mProjectDBVersion + " ^_^");
        return mProjectDBVersion;
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        Log.e("===", "DbHelper onCreate");
        List<Class> clses = new ArrayList<>();
        PosDBTable mPosDBTable = new PosDBTable();
        IProjectDBTable mProjectDBTable = (IProjectDBTable) ConfigureManager.getInstance(EposApplication
                .getAppContext()).getSubPrjClassInstance(PosDBTable.class);

        //保存项目的数据库版本，用于数据库升级时区别基础框架和项目各自的版本号
        Settings.setValue(EposApplication.getAppContext(), SimpleStringTag.PROJECT_DB_VERSION, String.valueOf
                (mProjectDBVersion));

        clses.addAll(mPosDBTable.registerDBClass());
        if (mProjectDBTable != null) {
            List<Class> list = mProjectDBTable.registerDBClass();
            if (list != null)
                clses.addAll(list);
        }

        try {
            for (Class cls : clses) {
                TableUtils.createTable(connectionSource, cls);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        mPosDBTable.onCreate(this, database, connectionSource);
        if (mProjectDBTable != null)
            mProjectDBTable.onCreate(this, database, connectionSource);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        Log.e("===", "DbHelper onUpgrade");
        PosDBTable mPosDBTable = new PosDBTable();
        IProjectDBTable mProjectDBTable = (IProjectDBTable) ConfigureManager.getInstance(EposApplication
                .getAppContext()).getSubPrjClassInstance(PosDBTable.class);


        try {
            //修复因为混淆导致数据库无法插入数据的问题
            TableUtils.dropTable(connectionSource, SlipElement.class, true);
            TableUtils.createTableIfNotExists(connectionSource, SlipElement.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int prjDbVersion = Integer.parseInt(Settings.getValue(EposApplication.getAppContext(), SimpleStringTag
                .PROJECT_DB_VERSION, "0"), 10);

        //注意：要兼容不同版本的数据库
        if (oldVersion - prjDbVersion != Config.DB_VERSION) {
            XLogUtil.d(TAG, "^_^ 基础框架数据库版本号提升：oldVersion = " + (oldVersion - prjDbVersion) + " newVersion = " +
                    Config.DB_VERSION + "" + " " + "^_^");
            //版本变更了才调用实际的处理
            mPosDBTable.onDBUpdate(this, database, connectionSource, oldVersion - prjDbVersion, Config.DB_VERSION);
        } else {
            XLogUtil.d(TAG, "^_^ 基础框架数据库版本未提升 ^_^");
        }

        if (prjDbVersion != mProjectDBVersion) {
            XLogUtil.d(TAG, "^_^ 应用项目数据库版本号提升：oldVersion = " + prjDbVersion + " newVersion = " + (newVersion -
                    Config.DB_VERSION) + "" + " " + "^_^");
            //版本变更了才调用实际的处理
            if (mProjectDBTable != null)
                mProjectDBTable.onDBUpdate(this, database, connectionSource, prjDbVersion, mProjectDBVersion);
            //更新项目数据库版本号
            XLogUtil.d(TAG, "^_^ 存储应用项目数据库版本号为：" + mProjectDBVersion);
            Settings.setValue(EposApplication.getAppContext(), SimpleStringTag.PROJECT_DB_VERSION, String.valueOf
                    (mProjectDBVersion));
        } else {
            XLogUtil.d(TAG, "^_^ 应用项目数据库版本未提升 ^_^");
        }
    }

    public synchronized Dao getDao(Class clazz) throws SQLException {
        String className = clazz.getSimpleName();
        Dao dao = null;
        if (daos.containsKey(className)) {
            dao = daos.get(className);
        }
        if (dao == null) {
            dao = super.getDao(clazz);
            daos.put(className, dao);
        }
        return dao;
    }

    public synchronized void removeDao(Class clazz) {
        String className = clazz.getSimpleName();
        if (daos.containsKey(className)) {
            daos.remove(className);
        }
    }

    @Override
    public void close() {
        super.close();
        daos.clear();
    }

    public static DbHelper getInstance() {
        return OpenHelperManager.getHelper(EposApplication.getAppContext(), DbHelper.class);
    }

    public static void releaseInstance() {
        OpenHelperManager.releaseHelper();
    }
}
