package com.centerm.epos.ebi.helper;

import android.os.Bundle;

import com.centerm.epos.activity.msn.BaseQueryTradeActivity;
import com.centerm.epos.activity.msn.BaseTMKByICActivity;
import com.centerm.epos.ebi.keys.EbiMenuTag;
import com.centerm.epos.ebi.keys.JsonKey;
import com.centerm.epos.ebi.ui.activity.EbiTMKByICActivity;
import com.centerm.epos.ebi.ui.activity.GtCommunicationSettingsActivity;
import com.centerm.epos.ebi.ui.activity.QueryTradeActivity;
import com.centerm.epos.ebi.ui.activity.ScanCommunicationSettingsActivity;
import com.centerm.epos.ebi.ui.activity.ScanQueryTradeActivity;
import com.centerm.epos.ebi.ui.activity.TMKByICActivity;
import com.centerm.epos.helper.BaseMenuHelper;
import com.centerm.epos.mvp.presenter.IMenuPresenter;
import com.centerm.epos.xml.bean.menu.MenuItem;
import com.centerm.epos.xml.keys.MenuTag;

/**
 * Created by liubit on 2017/12/25.
 * 二次开发新增菜单
 */

public class EBIMenuHelper extends BaseMenuHelper {

    @Override
    public boolean onTriggerMenuItem(IMenuPresenter presenter, MenuItem item) {
        logger.info(getClass().getSimpleName() + "==>onTriggerMenuItem: "+item.getEnTag());
        String tag = item.getEnTag();
        switch (tag) {
        case EbiMenuTag.ORDER_QUERY:
            Bundle bundle = new Bundle();
            bundle.putString(JsonKey.MENU_TAG,tag);
            presenter.jumpToActivity(ScanQueryTradeActivity.class,bundle);
            return true;
        case EbiMenuTag.TRADE_QUERY:
            Bundle bundle2 = new Bundle();
            bundle2.putString(JsonKey.MENU_TAG,tag);
            presenter.jumpToActivity(QueryTradeActivity.class,bundle2);
            return true;
        case EbiMenuTag.DOWNLOAD_POS_MAIN_KEY:
            Bundle paramIC = new Bundle();
            paramIC.putBoolean(BaseTMKByICActivity.IS_MANUAL_INPUT_TMK, false);
            presenter.jumpToActivity(EbiTMKByICActivity.class, paramIC);
            return true;
        case MenuTag.IC_IMPORT_TMK:     //IC卡导入主密钥
            Bundle paramIC2 = new Bundle();
            paramIC2.putBoolean(BaseTMKByICActivity.IS_MANUAL_INPUT_TMK, false);
            presenter.jumpToActivity(TMKByICActivity.class, paramIC2);
            return true;
        case EbiMenuTag.SCAN_COMMUNICATION_SETTINGS:
            Bundle param3 = new Bundle();
            param3.putBoolean(BaseTMKByICActivity.IS_MANUAL_INPUT_TMK, false);
            presenter.jumpToActivity(ScanCommunicationSettingsActivity.class, param3);
            return true;
        case EbiMenuTag.GT_COMMUNICATION_SETTINGS:
            presenter.jumpToActivity(GtCommunicationSettingsActivity.class);
            return true;
        case MenuTag.PRINT_ANY:
                presenter.jumpToActivity(QueryTradeActivity.class);
                return true;
        case MenuTag.CLEAR_REVERSE_RECORDS:
            presenter.doClearReverseRecords();
            return true;
        }
        return super.onTriggerMenuItem(presenter, item);
    }
}
