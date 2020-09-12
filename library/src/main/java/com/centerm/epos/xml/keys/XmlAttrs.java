package com.centerm.epos.xml.keys;

/**
 * XML文件的属性值，命名规则采用小驼峰规则
 * author:wanliang527</br>
 * date:2017/2/8</br>
 */

public class XmlAttrs {

    public final String project = "project";
    public final String tag = "tag";
    public final String filePath = "filePath";
    public final String version = "version";
    public final String enable = "enable";
    public final String dir = "dir";
//    public final String primary_menu = "primary_menu";
//    public final String secondary_menu = "secondary_menu";
//    public final String thirdly_menu = "thirdly_menu";
//    public final String shortcut = "shortcut";
    public final String chnTag = "chnTag";
    public final String enTag = "enTag";
    public final String iconRes = "iconRes";
    public final String textRes = "textRes";
    public final String isShow = "isShow";
    public final String structure = "structure";
    public final String process = "process";
    public final String code = "code";
    public final String topView = "topView";
    public final String itemStyle = "itemStyle";

    public final String projectID = "prjID";
    public final String channelID = "channelID";
    public final String defaultPrj = "default";
    public final String changeAppIcon = "changeAppIcon";

    public final String key = "key";
    public final String value = "value";
    public final String fileName = "fileName";
    public final String name = "name";
    public final String source = "source";
    public final String type = "type";
    public final String _class = "class";
    public final String _tag = "_tag";
    public final String align = "align";
    public final String _default = "_default";
    public final String enLabel = "enLabel";
    public final String font = "font";
    public final String isBold = "isBold";
    public final String label = "label";
    public final String belongs = "belongs";
    public final String isWrapValue = "isWrapValue";
    public final String valueFont = "valueFont";
    public final String valueAlign = "valueAlign";
    public final String valueIsBold = "valueIsBold";

    public final String tradeName = "trade_name";
    public final String msgReqType = "msg_req_type";
    public final String msgRespType = "msg_resp_type";
    public final String processCode = "process_code";
    public final String servicePoint = "service_point";
    public final String tradeCode = "trade_code";
    public final String tradeReverse = "reverse";

    private static XmlAttrs instance;
    public final String index = "index";
    public String condition = "condition";
    public String isPrintNull = "isPrintNull";


    public final String PACKAGE_NAME = "basePackage";

    private XmlAttrs() {
    }

    public static XmlAttrs obj() {
        if (instance == null) {
            instance = new XmlAttrs();
        }
        return instance;
    }

}
