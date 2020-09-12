package com.centerm.epos.transcation.pos.data;

import java.util.Map;

/**
 * Created by yuhc on 2017/2/8.<br>
 * 传统POS规范定义的域数据处理接口。将终端采集到的数据按域要求进行组织，以及数据域的数据解析为业务数据。
 */

public interface I8583Field {

    /**
     * 业务数据组织为规范定义的域数据
     * @param tradeInfo 业务数据
     * @return  域数据
     */
    public String encode();


    /**
     * 域数据解析为业务数据
     * @param fieldMsg  域数据
     * @return  业务数据
     */
    public Map<String,Object> decode(String fieldMsg);
}
