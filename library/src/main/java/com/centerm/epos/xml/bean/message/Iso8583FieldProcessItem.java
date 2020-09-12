package com.centerm.epos.xml.bean.message;

/**
 * Created by yuhc on 2017/3/9.
 * POS报文的数据域处理类配置
 */

public class Iso8583FieldProcessItem {

    //数据域索引
    private int index;
    //数据域标识
    private String name;
    //域处理的类名
    private String processClz;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProcessClz() {
        return processClz;
    }

    public void setProcessClz(String processClz) {
        this.processClz = processClz;
    }

    @Override
    public String toString() {
        return "^_^ Iso8583FieldProcessItem: index = "+index+
                " name = "+name+
                " processClz = "+processClz+
                " ^_^";
    }
}
