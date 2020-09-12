package com.centerm.epos.redevelop;

/**
 * Created by yuhc on 2017/6/6.
 * MAC计算，各项目可实现各自的算法，并在配置文件中配置。
 */

public interface IMacAlgorithm {

    /**
     * 计算报文的MAC值
     * @param msg  报文数据
     * @return  8字节MAC数据
     */
    String calculateMessageMac(String msg);

}
