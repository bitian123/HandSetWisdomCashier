package com.centerm.epos.present.transaction;

/**
 * Created by yuhc on 2017/2/21.
 *
 */

public interface IInputPassword {

    /**
     * 是否要显示金额
     * @return true显示
     */
    public boolean isShowAmount();

    /**
     * 是否显示卡号
     * @return  true 显示
     */
    public boolean isShowBankCardNum();

    /**
     * 获取已输入的密码个数
     * @return  个数
     */
    public int getPinLen();
}
