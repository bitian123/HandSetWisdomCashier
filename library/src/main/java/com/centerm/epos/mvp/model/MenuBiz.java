package com.centerm.epos.mvp.model;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.centerm.epos.R;
import com.centerm.epos.bean.ReverseInfo;
import com.centerm.epos.bean.TradeInfo;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.bean.TradePbocDetail;
import com.centerm.epos.bean.TradePrintData;
import com.centerm.epos.bean.transcation.RequestMessage;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.mvp.listener.StatusListener;
import com.centerm.epos.redevelop.ITradeRecordInformation;
import com.centerm.epos.redevelop.TradeRecordInfoImpl;
import com.centerm.epos.utils.ResourceUtils;
import com.centerm.epos.xml.bean.menu.Menu;
import com.centerm.epos.xml.bean.menu.MenuItem;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Boolean.FALSE;

/**
 * author:wanliang527</br>
 * date:2017/3/1</br>
 */

public class MenuBiz implements IMenuBiz {

    private Logger logger = Logger.getLogger(MenuBiz.class);
    private Menu menu;
    private final String TEXT = "text";
    private final String ICON = "icon";
    private final String ITEMS = "items";

    @Override
    public String[] getGridAdapterFrom() {
        return new String[]{TEXT, ICON};
    }

    @Override
    public int[] getGridAdapterTo() {
        return new int[]{R.id.menu_text_show, R.id.menu_icon_show};
    }

    @Override
    public List<Map<String, ?>> getGridAdapterData(Context context) {
        int len = menu.getCounts();
        List<Map<String, ?>> data = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            MenuItem tempMenu = menu.getItem(i);
            Map<String, Object> map = new HashMap<>();
            if (!TextUtils.isEmpty(tempMenu.getTextResName())) {
                int resId = ResourceUtils.getStringId(context, tempMenu.getTextResName());
                if (resId > 0)
                    map.put(TEXT, context.getString(resId));
                else
                    map.put(TEXT, tempMenu.getChnTag());
            } else
                map.put(TEXT, tempMenu.getChnTag());
            if (!TextUtils.isEmpty(tempMenu.getIconResName())) {
                int resId = ResourceUtils.getDrawableId(context, tempMenu.getIconResName());
                if (resId > 0) {
                    map.put(ICON, resId);
                } else {
                    map.put(ICON, R.drawable.ic_launcher);
                }
            } else {
                map.put(ICON, R.drawable.ic_launcher);
            }
            map.put(ITEMS, tempMenu);
            data.add(map);
        }
        return data;
    }

    @Override
    public void setMenu(Menu menu) {
        this.menu = menu;
        if (menu == null) {
            logger.warn("当前菜单为空菜单!");
        }else
            this.menu = menu.filteHideItems();
    }

    @Override
    public Menu getMenu() {
        return this.menu;
    }

    @Override
    public void hasTradeRecords(DbHelper dbHelper, final StatusListener<Boolean> listener) {
        final CommonDao<TradeInfo> dao = new CommonDao<>(TradeInfo.class, dbHelper);
        new AsyncTask<String, String, Boolean>() {

            @Override
            protected Boolean doInBackground(String... params) {
                List<TradeInfo> list = dao.query();
                return list != null && list.size() > 0;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if (listener != null)
                    listener.onFinish(new Boolean[]{aBoolean});
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
    }

    @Override
    public void clearTradeRecords(DbHelper dbHelper, final StatusListener<Boolean> listener) {

        new AsyncTask<String, String, Boolean[]>() {

            @Override
            protected Boolean[] doInBackground(String... params) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Boolean[] result = new Boolean[]{FALSE, FALSE};
                ITradeRecordInformation tradeRecordInformation = (ITradeRecordInformation) ConfigureManager
                        .getSubPrjClassInstance(new TradeRecordInfoImpl());
                result[0] = tradeRecordInformation.isTradeRecordExist();
                if (result[0])
                    result[1] = tradeRecordInformation.clearRecord();
                return result;
            }

            @Override
            protected void onPostExecute(Boolean[] aBoolean) {
                super.onPostExecute(aBoolean);
                if (listener != null)
                    listener.onFinish(aBoolean);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
    }


}
