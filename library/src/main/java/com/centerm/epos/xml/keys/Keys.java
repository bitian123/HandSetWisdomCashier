package com.centerm.epos.xml.keys;

/**
 * author:wanliang527</br>
 * date:2017/2/12</br>
 */

public class Keys {

    public final String primaryMenu = "primaryMenu";
    public final String secondaryMenu = "secondaryMenu";
    public final String thirdlyMenu = "thirdlyMenu";
    public final String shortcut = "shortcut";
    public final String tradeFlow = "tradeFlow";
    public final String functionToggle = "functionToggle";
    public final String redevelop = "redevelop";
    public final String defaultParams = "defaultParams";
    public final String xmlProperties = "properties";
    public final String tradeItem = "manager_trade";
    public final String security = "security";
    public final String saleSlip = "saleSlip";
    public final String operator_login = "operator_login";
    public final String tradeFactor = "tradeFactor";

    public final String script_update = "script_update";

    public final String def_oper_id = "def_oper_id";
    public final String redevelop_menu_helper = "redevelop_menu_helper";
    public final String redevelop_print_data = "redevelop_print_data";
    public final String runtime_init = "redevelop_runtime_init";
    public final String redevelop_load_menu = "redevelop_load_menu";
    public final String redevelop_login_request = "redevelop_login_request";
    public final String redevelop_print_slip = "redevelop_print_slip";
    public final String back_direct_to_launcher = "back_direct_to_launcher";
    public final String redevelop_mac_algorithm = "redevelop_mac_algorithm";
    public final String redevelop_pay_entry_presenter = "redevelop_pay_entry_presenter";
    public final String redevelop_receive_over_algorithm = "redevelop_receive_over_algorithm";//add by fl
    public final String check_merchant_info = "check_merchant_info";
    public final String prior_response_result = "prior_response_result";

    public final String flag_auto_sign_out = "flag_auto_sign_out";
    public final String flag_void_need_pin = "flag_void_need_pin";
    public final String flag_void_need_card = "flag_void_need_card";

    public final String is_mobile_net_only = "is_mobile_net_only";
    public final String keep_result_page = "keep_result_page";

    public final String iso_field_process = "isoFieldProcess";
    public final String iso_msg_config = "isoMsgConfig";
    public final String annotation_config = "annotation-config";

    //POSP平台通讯参数，TPDU
    public final String comm_tpdu = "comm_tpdu";
    //POSP平台通讯参数，IP
    public final String comm_ip = "comm_ip";
    //POSP平台通讯参数，PORT
    public final String comm_port = "comm_port";
    //通讯方式，是否加密
    public final String is_msg_encrypt = "is_msg_encrypt";
    //通讯方式，HTTPS
    public final String comm_type = "comm_type";

    //CPAY管理平台通讯参数，TPDU
    public final String comm_cpay_tpdu = "comm_cpay_tpdu";
    //CPAY管理平台通讯参数，IP
    public final String comm_cpay_ip = "comm_cpay_ip";
    //CPAY管理平台通讯参数，PORT
    public final String comm_cpay_port = "comm_cpay_port";
    //通讯方式，是否加密
    public final String is_cpay_msg_encrypt = "is_cpay_msg_encrypt";
    //通讯方式，HTTPS
    public final String comm_cpay_type = "comm_cpay_type";

    //加密算法
    public final String encrypt_algorithm = "encrypt_algorithm";
    //磁道数据是否要加密
    public final String track_encrypt = "track_encrypt";

    //程序版本前缀
    public final String versionprefix = "versionprefix";

    //项目名称
    public final String project = "project";

    public final String printnum = "printnum";

    public final String needLogin_when_opernull = "needLogin_when_opernull";//  当操作员不为空时， 进入应用时，是否进入菜单界面

    public final String check_merchant_terminal_is_null = "check_merchant_terminal_is_null";
    private static Keys instance;

    private Keys() {
    }

    public static Keys obj() {
        if (instance == null) {
            instance = new Keys();
        }
        return instance;
    }


}
