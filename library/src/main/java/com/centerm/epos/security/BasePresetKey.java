package com.centerm.epos.security;

/**
 * Created by yuhc on 2017/2/14.
 * 导入预置密钥到软件密码键盘中。
 */

public class BasePresetKey implements IPresetKey {

    /**
     * 硬编码导入密钥，更加安全的方式：密钥卡、密钥POS、PC工具等？
     * 安全问题？或者使用密码键盘的三级密钥体系
     * @return  导入结果
     */
    @Override
    public boolean importKey() {
        byte[] key = new byte[]{0x11,0x22,0x33,0x44,0x55,0x66,0x77, (byte) 0x88,(byte) 0x99,(byte) 0xaa,(byte) 0xbb,(byte) 0xcc,(byte) 0xdd,(byte) 0xee,(byte) 0xff,0x00};
        return storeKeyinfo(key);
    }

    /**
     * 保存密钥到软件密码键盘中
     * @param key 密钥
     * @return  true 保存成功
     */
    private boolean storeKeyinfo(byte[] key){
        // TODO: 2017/2/14 存储到软件密码键盘中

        return true;
    }
}
