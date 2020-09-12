package com.centerm.epos.present.transaction;

import java.util.List;

/**
 * Created by yuhc on 2017/2/24.
 * 显示结果界面的业务处理
 */

public interface IResult {

    /**
     * 取返回码
     * @return  返回码
     */
    public String getResponseCode();

    /**
     * 取描述信息
     * @return  描述信息
     */
    public String getResponseMessage();

    /**
     * 交易结果
     * @return  true 成功
     */
    public boolean isSuccess();

    /**
     * 结果标题
     * @return  标题字符串
     */
    public String getTradeTitle();

    /**
     * 详细信息
     * @return 详细内容
     */
    public List<ItemViewData> getItemViewData();

    class ItemViewData {
        String tip;
        String content;
        boolean isAddDivider;

        public ItemViewData(String tip, String content, boolean isAddDivider) {
            this.tip = tip;
            this.content = content;
            this.isAddDivider = isAddDivider;
        }

        public String getTip() {
            return tip;
        }

        public void setTip(String tip) {
            this.tip = tip;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public boolean isAddDivider() {
            return isAddDivider;
        }

        public void setAddDivider(boolean addDivider) {
            isAddDivider = addDivider;
        }
    }
}
