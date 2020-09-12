package com.centerm.epos.xml.keys;

/**
 * XML文件的tag，命名规则采用大驼峰规则
 * author:wanliang527</br>
 * date:2017/2/8</br>
 */

public class XmlTag {
    //    public final String project = "project";
//    public final String function = "function";
//    public final String primary_menu = "primary_menu";
//    public final String secondary_menu = "secondary_menu";
//    public final String thirdly_menu = "thirdly_menu";
//    public final String shortcut = "shortcut";
//    public final String transaction = "transaction";
//    public final String trade_flow = "trade_flow";
//    public final String function_toggle = "function_toggle";
//    public final String parameter = "parameter";
//    public final String redevelop = "redevelop";
//    public final String default_params = "default_params";
//    public final String preset = "preset";
//    public final String slip = "slip";
//    public final String prefix = "prefix";
//    public final String content = "content";
//    public final String message = "message";
//    public final String iso8583 = "iso8583";
    public final String Configuration = "Configuration";
    public final String Map = "Map";
    public final String _Int = "Int";
    public final String _Boolean = "Boolean";
    public final String _Double = "Double";
    public final String _String = "String";
    public final String _Long = "Long";

    public final String Menu = "Menu";
    public final String MenuItem = "MenuItem";

    public final String factor = "factor";

    public final String Parameter = "Parameter";
    public final String Group = "Group";
    public final String Item = "Item";
    public final String Redevelop = "Redevelop";
    public final String Slip = "Slip";

    public final String process = "process";
    public final String field = "field";

    public final String ANNOTION_CONFIG = "annotation-epos";

    private static XmlTag instance;

    private XmlTag() {
    }

    public static XmlTag obj() {
        if (instance == null) {
            instance = new XmlTag();
        }
        return instance;
    }

}
