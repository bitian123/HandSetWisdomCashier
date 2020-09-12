package com.centerm.epos.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.centerm.epos.bean.TradeRecordForUpload;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.utils.XLogUtil;

import java.util.Locale;

/**
 * Created by yuhc on 2017/8/28.
 * 交易记录数据共享
 */

public class TradeRecContentProvider extends ContentProvider {

    private static final String TAG = TradeRecContentProvider.class.getSimpleName();

    private static final int RECORD = 1;
    private static final int RECORDS = 2;
    private static final int LAST_RECORD = 3;

    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        //查询类
        MATCHER.addURI(TradeRecordForUpload.AUTHOR_TAG, TradeRecordForUpload.TRADE_RECORD_ITEM, RECORD);
        MATCHER.addURI(TradeRecordForUpload.AUTHOR_TAG, TradeRecordForUpload.TRADE_RECORD_LAST, LAST_RECORD);
        MATCHER.addURI(TradeRecordForUpload.AUTHOR_TAG, TradeRecordForUpload.TRADE_RECORD_FULL, RECORDS);
        //添加类
        MATCHER.addURI(TradeRecordForUpload.AUTHOR_TAG, TradeRecordForUpload.TRADE_RECORD, RECORD);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable
            String[] selectionArgs, @Nullable String sortOrder) {
        XLogUtil.d(TAG, "^_^ query ^_^");
        SQLiteDatabase database = DbHelper.getInstance().getReadableDatabase();
        Cursor result;
        switch (MATCHER.match(uri)) {
            case RECORDS:
                result = database.query(TradeRecordForUpload.TABLE_NAME, projection, selection, selectionArgs, null,
                        null, sortOrder);
                break;
            case LAST_RECORD:
                //获取最后一条记录
                String sql = "select * from " + TradeRecordForUpload.TABLE_NAME + " order by termTransSeq desc limit" +
                        "(1)";
                result = database.rawQuery(sql, null);
                break;
            case RECORD:
                long traceNumber = ContentUris.parseId(uri);
                String where = "termTransSeq=" + String.format(Locale.CHINA, "'%06d'", traceNumber);
                if (!TextUtils.isEmpty(selection)) {
                    where = selection + " and " + where;
                }
                result = database.query(TradeRecordForUpload.TABLE_NAME, projection, where, selectionArgs, null, null,
                        sortOrder);
                break;
            default:
                DbHelper.releaseInstance();
                throw new IllegalArgumentException("Unkwon Uri:" + uri.toString());
        }
        DbHelper.releaseInstance();
        return result;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (MATCHER.match(uri)) {
            case RECORD:
            case LAST_RECORD:
                return "vnd.android.cursor.item/record";
            case RECORDS:
                return "vnd.android.cursor.dir/record";
            default:
                throw new IllegalArgumentException("Unkwon Uri:" + uri.toString());
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        XLogUtil.d(TAG, "^_^ insert ^_^");
        SQLiteDatabase sqLiteDatabase = DbHelper.getInstance().getReadableDatabase();
        switch (MATCHER.match(uri)) {
            case RECORD:
                sqLiteDatabase.insert(TradeRecordForUpload.TABLE_NAME, null, values);
                break;
            default:
                DbHelper.releaseInstance();
                throw new IllegalArgumentException("Unkwon Uri:" + uri.toString());
        }
        DbHelper.releaseInstance();
        return uri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        XLogUtil.d(TAG, "^_^ delete ^_^");
        SQLiteDatabase sqLiteDatabase = DbHelper.getInstance().getReadableDatabase();
        switch (MATCHER.match(uri)) {
            case RECORD:
                sqLiteDatabase.delete(TradeRecordForUpload.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                DbHelper.releaseInstance();
                throw new IllegalArgumentException("Unkwon Uri:" + uri.toString());
        }
        DbHelper.releaseInstance();
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable
            String[] selectionArgs) {
        XLogUtil.d(TAG, "^_^ update ^_^");
        return 0;
    }

    public static void notifyInsertedRecord(Context appContext) {
        Uri uri = Uri.parse("content://" + TradeRecordForUpload.AUTHOR_TAG + "/" + TradeRecordForUpload
                .TRADE_RECORD_LAST);
        XLogUtil.d(TAG, "^_^ Notify URI -> " + uri + " ^_^");
        appContext.getContentResolver().notifyChange(uri, null);
    }

    public static void notifySettlementOver(Context appContext) {
        Uri uri = Uri.parse("content://" + TradeRecordForUpload.AUTHOR_TAG + "/" + TradeRecordForUpload
                .TRADE_RECORD_CLEAR);
        XLogUtil.d(TAG, "^_^ Notify URI -> " + uri + " ^_^");
        appContext.getContentResolver().notifyChange(uri, null);
    }

}
