package com.centerm.epos.redevelop;

import com.centerm.epos.EposApplication;
import com.centerm.epos.bean.ReverseInfo;
import com.centerm.epos.bean.ScriptInfo;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.bean.TradePbocDetail;
import com.centerm.epos.bean.TradePrintData;
import com.centerm.epos.bean.transcation.InstallmentInformation;
import com.centerm.epos.bean.transcation.RequestMessage;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.transcation.pos.manager.UploadESignatureTradeChecker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuhc on 2017/8/23.
 *
 */

public class TradeRecordInfoImpl implements ITradeRecordInformation {

    @Override
    public boolean clearRecord() {
        final List<CommonDao> tradeDaoList = new ArrayList<>();
        DbHelper dbHelper = DbHelper.getInstance();
        tradeDaoList.add(new CommonDao<>(TradeInfoRecord.class, dbHelper));
        tradeDaoList.add(new CommonDao<>(ReverseInfo.class, dbHelper));
        tradeDaoList.add(new CommonDao<>(TradePbocDetail.class, dbHelper));
        tradeDaoList.add(new CommonDao<>(TradePrintData.class, dbHelper));
        tradeDaoList.add(new CommonDao<>(RequestMessage.class, dbHelper));
        tradeDaoList.add(new CommonDao<>(InstallmentInformation.class, dbHelper));
        tradeDaoList.add(new CommonDao<>(ScriptInfo.class, dbHelper));/*增加脚本信息的删除*/
        /*@author:zhouzhihua
        *增加电子签名图片的删除
        * 2017.11.07
        * */
        UploadESignatureTradeChecker.cleanEsignPic();

        boolean result = false;
        for (CommonDao dao: tradeDaoList) {
            if (dao.countOf() > 0){
                result = dao.deleteByWhere("1=1");
                //删除失败则返回
                if (!result)
                    break;
            }
        }
        DbHelper.releaseInstance();
        return result;
    }

    @Override
    public boolean isTradeRecordExist() {
        CommonDao<TradeInfoRecord> tradeInfoRecordCommonDao = new CommonDao<>(TradeInfoRecord.class, DbHelper.getInstance());
        boolean result = tradeInfoRecordCommonDao.countOf() > 0;
        DbHelper.releaseInstance();
        return result;
    }


}
