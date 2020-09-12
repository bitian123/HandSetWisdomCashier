package com.centerm.epos.redevelop;

import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;

/**
 * Created by FL on 2017/9/15 16:21.
 */

public class UnionPayReceivedOver implements ITCPIsReceivedOver {
    @Override
    public boolean isReceivedOver(byte[] receiveData) {
        if (receiveData == null || receiveData.length < 2)
            return false;
        int longPrex = HexUtils.bytes2short(receiveData) + 2;
        if (receiveData.length < longPrex)
            return false;

        return true;
    }
}
