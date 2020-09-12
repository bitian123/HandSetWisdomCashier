package com.centerm.epos.bean;

import java.util.List;

/**
 * create by liubit on 2019-09-10
 */
public class GtBannerBean {

    /**
     * code : 0
     * data : ["http://180.168.34.202:8080/upload/20190910/admin/942c4210-2768-4c34-8b7a-d8918fb79b66.jpg"]
     * msg : SUCCESS
     */

    private String code;
    private String msg;
    private List<String> data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }
}
