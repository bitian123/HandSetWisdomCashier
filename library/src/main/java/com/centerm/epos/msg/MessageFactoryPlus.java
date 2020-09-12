package com.centerm.epos.msg;

import java.util.Map;

/**
 * Created by yuhc on 2017/2/23.
 *
 */

public class MessageFactoryPlus {

    //传统POS业务的报文
    public static final int MESSAGE_ISO8583_POS = 1;
    //HTTP网上支付报文
    public static final int MESSAGE_HTTP_JSON = 2;

    public static ITransactionMessage createMessageByType(int type, Map<String,Object> transData){
        switch (type){
            case MESSAGE_ISO8583_POS:
                return new PosISO8583Message(transData);
            case MESSAGE_HTTP_JSON:
                return new HttpJsonMessage();
        }
        return null;
    }
}
