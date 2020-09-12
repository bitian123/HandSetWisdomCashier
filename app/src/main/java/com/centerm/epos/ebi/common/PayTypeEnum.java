package com.centerm.epos.ebi.common;

import com.centerm.epos.ebi.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by FL on 2017/9/22 09:20.
 * 支付类型
 */

public enum PayTypeEnum {
    CARD(0, R.string.pay_type_card, R.mipmap.ic_saoma),
    WEI(1,R.string.pay_type_wei,R.mipmap.ic_pay_wechat),
    ALI(2,R.string.pay_type_ali,R.mipmap.ic_pay_alipay),
    UNION(3,R.string.pay_type_union,R.mipmap.ic_pay_yinlian);

    private int index;
    private int name;
    private int res;

    PayTypeEnum(int index, int name, int res){
        this.index = index;
        this.name = name;
        this.res = res;
    }


    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getName() {
        return name;
    }

    public void setName(int name) {
        this.name = name;
    }

    public int getRes() {
        return res;
    }

    public void setRes(int res) {
        this.res = res;
    }

    public static List<PayTypeEnum> listPayType(){
        PayTypeEnum[] enums = values();
        List<PayTypeEnum> list = new ArrayList<>();
        Collections.addAll(list, enums);
        return list;
    }

    public static List<PayTypeEnum> listPayTypeScan(boolean hasScanCommon){
        PayTypeEnum[] enums = values();
        List<PayTypeEnum> list = new ArrayList<>();
        Collections.addAll(list, enums);
        if(!hasScanCommon)
            list.remove(3);
        list.remove(0);

        return list;
    }
}
