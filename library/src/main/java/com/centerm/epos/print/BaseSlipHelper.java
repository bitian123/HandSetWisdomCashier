package com.centerm.epos.print;

import com.centerm.epos.bean.TradeInfo;

import org.apache.log4j.Logger;

import java.util.Map;

/**
 * author:wanliang527</br>
 * date:2017/2/16</br>
 */
public class BaseSlipHelper implements ISlipHelper {
    protected Logger logger = Logger.getLogger(this.getClass());

    @Override
    public Map<String, String> decodeContent(Map<String, String> map) {
        return map;
    }

    @Override
    public Map<String, String> decodeContent(TradeInfo info) {
        //// TODO: 2017/2/17 交易流水转换签购单键值对，未实现
        return null;
    }

    @Override
    public String keyMapTag(String key) {
        switch (key) {
            default:
                return key;
        }
    }
}
