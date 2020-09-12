package com.centerm.epos.security;

/**
 * Created by yuhc on 2017/2/14.
 * 导入安全信息，主要是商户号终端号，密钥等
 */

public interface ISecurityData {

    /**
     * 导入数据
     * @return  true 导入成功
     */
    public boolean importData();
}
