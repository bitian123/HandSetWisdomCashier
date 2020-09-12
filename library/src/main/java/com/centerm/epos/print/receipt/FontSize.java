package com.centerm.epos.print.receipt;

/**
 * author:wanliang527</br>
 * date:2017/3/6</br>
 */

public enum FontSize {
    SMALL(16),//小
    MEDIUM(23),//中
    LARGE(33),//大
    XLARGE(40);//超大
    private int value;

    FontSize(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
