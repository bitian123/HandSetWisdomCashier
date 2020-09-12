package com.centerm.epos.printer;

import com.centerm.epos.print.PrintManager;

import java.util.Map;

/**
 * Created by yuhc on 2017/7/11.
 */

public interface IPrintSlipHelper extends PrintManager.StatusInterpolator{

    Map<String, String> trade2PrintData(Map<String, String> tradeData);

    void setPrintComplete(boolean printComplete);

    String getTranCardType(Map<String, String> mapData);
}
