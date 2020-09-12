package com.centerm.epos.security;

/**
 * Created by yuhc on 2017/2/14.
 * 预置密钥导入接口
 */

public interface IPresetKey {

    /**
     * 导入密钥到软件密码键盘中
     * @return  true 导入成功
     */
    public boolean importKey();
}
