package com.centerm.epos.fragment.trade;

/**
 * Created by yuhc on 2017/2/22.
 * 数据交互界面
 */

public interface ITradingView {

    /**
     * 更新提示信息
     * @param content
     */
    public void updateHint(String content);

    public void updateHint(int stringID);
}
