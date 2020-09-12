package com.centerm.epos.redevelop;

import java.util.Map;

/**
 * Created by yuhc on 2017/9/1.
 * 交易结果处理，各项目处理定制需求
 */

public interface IActionForResult {

    void doAction(Map<String, Object> requestData, Map<String, Object> respData);

}
