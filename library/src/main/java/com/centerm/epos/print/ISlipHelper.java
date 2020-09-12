package com.centerm.epos.print;

import com.centerm.epos.bean.TradeInfo;
import com.centerm.epos.xml.bean.slip.SlipElement;

import java.util.Map;

/**
 * 签购单外部辅助类。
 * author:wanliang527</br>
 * date:2017/2/16</br>
 */
public interface ISlipHelper {

    /**
     * 打印内容映射。将实际业务的值映射成签购单要求的键值对
     *
     * @param map 实际业务键值对
     * @return map 与签购单模板匹配的键值对
     */
    Map<String, String> decodeContent(Map<String, String> map);

    /**
     * 打印内容映射。将交易流水信息映射成签购单要求的键值对
     *
     * @param info 交易流水对象
     * @return 与签购单模板匹配的键值对
     */
    Map<String, String> decodeContent(TradeInfo info);

    /**
     * 键值映射签购单模板的tag
     * @param key 键值
     * @return 签购单模板定义的tag
     */
    String keyMapTag(String key);

}
