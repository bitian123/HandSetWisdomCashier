package com.centerm.epos.mvp.model;

import com.centerm.epos.bean.TradeInfo;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.db.DbHelper;

/**
 * author:wanliang527</br>
 * date:2017/3/2</br>
 */

public interface IInputOriginInfoBiz {
    TradeInfoRecord queryByPosSerial(DbHelper dbHelper, String posSerial);

    TradeInfoRecord queryByPosSerial(DbHelper dbHelper, String posSerial, String transType);

    TradeInfoRecord queryByPosScanVoucherNo(DbHelper dbHelper, String scanVoucherNo);
}
