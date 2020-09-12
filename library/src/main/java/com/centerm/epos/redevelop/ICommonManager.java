package com.centerm.epos.redevelop;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by 94437 on 2017/7/25.
 */

public interface ICommonManager {
    public List getDebitList()throws SQLException;
    public List getCreditList()throws SQLException;
    public List getTransDetail()throws SQLException;
    public List getRefusedList()throws SQLException;
    public List getFailList()throws SQLException;
    public List getBatchList()throws SQLException;
    public long getBatchCount()throws SQLException;
    public List getLastTransItem()throws SQLException;
    public List getListForBatch()throws SQLException;
    public List getRefundList()throws SQLException;
    public int getBatchSendRecordCount()throws SQLException;
    public List getOfflineTransList() throws SQLException;
    public List getOfflineTransList(int iFalg) throws SQLException;

    public List getScanSaleList()throws SQLException;
    public List getScanVoidList()throws SQLException;
    public List getScanRefundList()throws SQLException;
}
