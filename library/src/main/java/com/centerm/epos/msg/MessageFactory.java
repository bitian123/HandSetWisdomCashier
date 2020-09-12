package com.centerm.epos.msg;

import android.content.Context;

import com.centerm.epos.channels.EnumChannel;
import com.centerm.epos.common.Settings;

import java.util.Map;

/**
 * 报文工厂
 * author:wanliang527</br>
 * date:2016/10/27</br>
 */

public class MessageFactory {
    private Context context;
    private EnumChannel channel;

    public MessageFactory(Context context) {
        this.context = context;
        channel = EnumChannel.valueOf(Settings.getProjectName(context));
    }

    public Object pack(String transCode, Map<String, String> data) {
        switch (channel) {
            case QIANBAO:
//                return new QianBaoMsgFactory().pack(context, transCode, data);
        }
        return null;
    }

    public Map<String, String> unpack(String transCode, Object data) {
        switch (channel) {
            case QIANBAO:
//                return new QianBaoMsgFactory().unPack(context, transCode, data);
        }
        return null;
    }


}
