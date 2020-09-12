package com.centerm.epos.ebi.msg;

import com.centerm.epos.msg.ITransactionMessage;
import com.centerm.epos.msg.PosISO8583Message;

import java.util.Map;

/**
 * Created by liubit on 2017/12/25.
 */

public class EbiMessageFactory  {
    public static final int MESSAGE_ISO8583_POS = 1;
    public static final int MESSAGE_HTTP_JSON = 2;
    //江苏农信融合支付XML支付报文
    public static final int MESSAGE_HTTP_XML = 3;

    public static ITransactionMessage createMessageByType(int type, Map<String, Object> transData) {
        switch(type) {
            case 1:
                return new PosISO8583Message(transData);
            case 2:
                return new EbiHttpJsonMessage();
            case 3:

            default:
                return null;
        }
    }
}
