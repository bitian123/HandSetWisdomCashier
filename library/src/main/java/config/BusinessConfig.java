package config;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.centerm.epos.EposApplication;
import com.centerm.epos.base.SimpleStringTag;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.redevelop.BaseIsTradeOpened;
import com.centerm.epos.redevelop.IIsTradeOpened;
import com.centerm.epos.xml.bean.PreferDataPool;
import com.centerm.epos.xml.keys.Keys;

import static config.BusinessConfig.Key.HOTLINE_KEY;
import static config.BusinessConfig.Key.KEY_BATCH_NO;
import static config.BusinessConfig.Key.KEY_LAST_BIN_NO;
import static config.BusinessConfig.Key.KEY_POS_SERIAL;
import static config.BusinessConfig.Key.PROJECT_ID;
import static config.BusinessConfig.Key.SECURITYPWD;
import static config.BusinessConfig.Key.SETTLEMENT_MERCHANT_CD;
import static config.BusinessConfig.Key.SETTLEMENT_MERCHANT_NAME;
import static config.BusinessConfig.Key.SETTLEMENT_TERMINAL_CD;
import static config.BusinessConfig.Key.TRADE_KEEP_DAY;

/**
 * 业务参数配置
 * author:wanliang527</br>
 * date:2016/10/27</br>
 */

public class BusinessConfig {


    /**
     * 默认的AID参数
     */
    public static final String[] AID = new String[]{
            "9F0608A000000333010101DF0101009F08020020DF1105D84000A800DF1205D84004F800DF130500100000009F1B040000C350DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06100000000000DF1906100000000000DF2006100000000000DF2106100000000000",
            "9F0608A000000333010102DF0101009F08020020DF1105D84000A800DF1205D84004F800DF130500100000009F1B040000C350DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06100000000000DF1906100000000000DF2006100000000000DF2106100000000000",
            "9F0608A000000333010103DF0101009F08020020DF1105D84000A800DF1205D84004F800DF130500100000009F1B040000C350DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06100000000000DF1906100000000000DF2006100000000000DF2106100000000000",
            "9F0608A000000333010106DF0101009F08020020DF1105D84000A800DF1205D84004F800DF130500100000009F1B040000C350DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06100000000000DF1906100000000000DF2006100000000000DF2106100000000000"};
    /**
     * 默认的CAPK参数
     */
    public static final String[] CAPK = new String[]{
            "9F0605A0000003339F220101DF05083230313431323331DF060101DF070101DF028180BBE9066D2517511D239C7BFA77884144AE20C7372F515147E8CE6537C54C0A6A4D45F8CA4D290870CDA59F1344EF71D17D3F35D92F3F06778D0D511EC2A7DC4FFEADF4FB1253CE37A7B2B5A3741227BEF72524DA7A2B7B1CB426BEE27BC513B0CB11AB99BC1BC61DF5AC6CC4D831D0848788CD74F6D543AD37C5A2B4C5D5A93BDF040103DF0314E881E390675D44C2DD81234DCE29C3F5AB2297A0",
            "9F0605A0000003339F220102DF05083230323131323331DF060101DF070101DF028190A3767ABD1B6AA69D7F3FBF28C092DE9ED1E658BA5F0909AF7A1CCD907373B7210FDEB16287BA8E78E1529F443976FD27F991EC67D95E5F4E96B127CAB2396A94D6E45CDA44CA4C4867570D6B07542F8D4BF9FF97975DB9891515E66F525D2B3CBEB6D662BFB6C3F338E93B02142BFC44173A3764C56AADD202075B26DC2F9F7D7AE74BD7D00FD05EE430032663D27A57DF040103DF031403BB335A8549A03B87AB089D006F60852E4B8060",
            "9F0605A0000003339F220103DF05083230323431323331DF060101DF070101DF0281B0B0627DEE87864F9C18C13B9A1F025448BF13C58380C91F4CEBA9F9BCB214FF8414E9B59D6ABA10F941C7331768F47B2127907D857FA39AAF8CE02045DD01619D689EE731C551159BE7EB2D51A372FF56B556E5CB2FDE36E23073A44CA215D6C26CA68847B388E39520E0026E62294B557D6470440CA0AEFC9438C923AEC9B2098D6D3A1AF5E8B1DE36F4B53040109D89B77CAFAF70C26C601ABDF59EEC0FDC8A99089140CD2E817E335175B03B7AA33DDF040103DF031487F0CD7C0E86F38F89A66F8C47071A8B88586F26",
            "9F0605A0000003339F220104DF05083230323531323331DF060101DF070101DF0281F8BC853E6B5365E89E7EE9317C94B02D0ABB0DBD91C05A224A2554AA29ED9FCB9D86EB9CCBB322A57811F86188AAC7351C72BD9EF196C5A01ACEF7A4EB0D2AD63D9E6AC2E7836547CB1595C68BCBAFD0F6728760F3A7CA7B97301B7E0220184EFC4F653008D93CE098C0D93B45201096D1ADFF4CF1F9FC02AF759DA27CD6DFD6D789B099F16F378B6100334E63F3D35F3251A5EC78693731F5233519CDB380F5AB8C0F02728E91D469ABD0EAE0D93B1CC66CE127B29C7D77441A49D09FCA5D6D9762FC74C31BB506C8BAE3C79AD6C2578775B95956B5370D1D0519E37906B384736233251E8F09AD79DFBE2C6ABFADAC8E4D8624318C27DAF1DF040103DF0314F527081CF371DD7E1FD4FA414A665036E0F5E6E5"};
//
// "9F0605A0000003339F220101DF05083230303931323331DF060101DF070101DF028180BBE9066D2517511D239C7BFA77884144AE20C7372F515147E8CE6537C54C0A6A4D45F8CA4D290870CDA59F1344EF71D17D3F35D92F3F06778D0D511EC2A7DC4FFEADF4FB1253CE37A7B2B5A3741227BEF72524DA7A2B7B1CB426BEE27BC513B0CB11AB99BC1BC61DF5AC6CC4D831D0848788CD74F6D543AD37C5A2B4C5D5A93BDF040103DF0314E881E390675D44C2DD81234DCE29C3F5AB2297A0",
//
// "9F0605A0000003339F220102DF05083230313431323331DF060101DF070101DF028190A3767ABD1B6AA69D7F3FBF28C092DE9ED1E658BA5F0909AF7A1CCD907373B7210FDEB16287BA8E78E1529F443976FD27F991EC67D95E5F4E96B127CAB2396A94D6E45CDA44CA4C4867570D6B07542F8D4BF9FF97975DB9891515E66F525D2B3CBEB6D662BFB6C3F338E93B02142BFC44173A3764C56AADD202075B26DC2F9F7D7AE74BD7D00FD05EE430032663D27A57DF040103DF031403BB335A8549A03B87AB089D006F60852E4B8060",
//
// "9F0605A0000003339F220103DF05083230313731323331DF060101DF070101DF0281B0B0627DEE87864F9C18C13B9A1F025448BF13C58380C91F4CEBA9F9BCB214FF8414E9B59D6ABA10F941C7331768F47B2127907D857FA39AAF8CE02045DD01619D689EE731C551159BE7EB2D51A372FF56B556E5CB2FDE36E23073A44CA215D6C26CA68847B388E39520E0026E62294B557D6470440CA0AEFC9438C923AEC9B2098D6D3A1AF5E8B1DE36F4B53040109D89B77CAFAF70C26C601ABDF59EEC0FDC8A99089140CD2E817E335175B03B7AA33DDF040103DF031487F0CD7C0E86F38F89A66F8C47071A8B88586F26",
//
// "9F0605A0000003339F220104DF05083230313731323331DF060101DF070101DF0281F8BC853E6B5365E89E7EE9317C94B02D0ABB0DBD91C05A224A2554AA29ED9FCB9D86EB9CCBB322A57811F86188AAC7351C72BD9EF196C5A01ACEF7A4EB0D2AD63D9E6AC2E7836547CB1595C68BCBAFD0F6728760F3A7CA7B97301B7E0220184EFC4F653008D93CE098C0D93B45201096D1ADFF4CF1F9FC02AF759DA27CD6DFD6D789B099F16F378B6100334E63F3D35F3251A5EC78693731F5233519CDB380F5AB8C0F02728E91D469ABD0EAE0D93B1CC66CE127B29C7D77441A49D09FCA5D6D9762FC74C31BB506C8BAE3C79AD6C2578775B95956B5370D1D0519E37906B384736233251E8F09AD79DFBE2C6ABFADAC8E4D8624318C27DAF1DF040103DF0314F527081CF371DD7E1FD4FA414A665036E0F5E6E5",
//
// "9F0605A0000000039F220101DF05083230303931323331DF060101DF070101DF028180C696034213D7D8546984579D1D0F0EA519CFF8DEFFC429354CF3A871A6F7183F1228DA5C7470C055387100CB935A712C4E2864DF5D64BA93FE7E63E71F25B1E5F5298575EBE1C63AA617706917911DC2A75AC28B251C7EF40F2365912490B939BCA2124A30A28F54402C34AECA331AB67E1E79B285DD5771B5D9FF79EA630B75DF040103DF0314D34A6A776011C7E7CE3AEC5F03AD2F8CFC5503CC",
//
// "9F0605A0000000039F220107DF05083230313231323331DF060101DF070101DF028190A89F25A56FA6DA258C8CA8B40427D927B4A1EB4D7EA326BBB12F97DED70AE5E4480FC9C5E8A972177110A1CC318D06D2F8F5C4844AC5FA79A4DC470BB11ED635699C17081B90F1B984F12E92C1C529276D8AF8EC7F28492097D8CD5BECEA16FE4088F6CFAB4A1B42328A1B996F9278B0B7E3311CA5EF856C2F888474B83612A82E4E00D0CD4069A6783140433D50725FDF040103DF0314B4BC56CC4E88324932CBC643D6898F6FE593B172",
//
// "9F0605A0000000049F220103DF05083230303931323331DF060101DF070101DF028180C2490747FE17EB0584C88D47B1602704150ADC88C5B998BD59CE043EDEBF0FFEE3093AC7956AD3B6AD4554C6DE19A178D6DA295BE15D5220645E3C8131666FA4BE5B84FE131EA44B039307638B9E74A8C42564F892A64DF1CB15712B736E3374F1BBB6819371602D8970E97B900793C7C2A89A4A1649A59BE680574DD0B60145DF040103DF03145ADDF21D09278661141179CBEFF272EA384B13BB",
//
// "9F0605A0000000039F220108DF05083230313431323331DF060101DF070101DF0281B0D9FD6ED75D51D0E30664BD157023EAA1FFA871E4DA65672B863D255E81E137A51DE4F72BCC9E44ACE12127F87E263D3AF9DD9CF35CA4A7B01E907000BA85D24954C2FCA3074825DDD4C0C8F186CB020F683E02F2DEAD3969133F06F7845166ACEB57CA0FC2603445469811D293BFEFBAFAB57631B3DD91E796BF850A25012F1AE38F05AA5C4D6D03B1DC2E568612785938BBC9B3CD3A910C1DA55A5A9218ACE0F7A21287752682F15832A678D6E1ED0BDF040103DF031420D213126955DE205ADC2FD2822BD22DE21CF9A8",
//
// "9F0605A0000000049F220104DF05083230313231323331DF060101DF070101DF028190A6DA428387A502D7DDFB7A74D3F412BE762627197B25435B7A81716A700157DDD06F7CC99D6CA28C2470527E2C03616B9C59217357C2674F583B3BA5C7DCF2838692D023E3562420B4615C439CA97C44DC9A249CFCE7B3BFB22F68228C3AF13329AA4A613CF8DD853502373D62E49AB256D2BC17120E54AEDCED6D96A4287ACC5C04677D4A5A320DB8BEE2F775E5FEC5DF040103DF0314381A035DA58B482EE2AF75F4C3F2CA469BA4AA6C",
//
// "9F0605A0000000039F220109DF05083230313631323331DF060101DF070101DF0281F89D912248DE0A4E39C1A7DDE3F6D2588992C1A4095AFBD1824D1BA74847F2BC4926D2EFD904B4B54954CD189A54C5D1179654F8F9B0D2AB5F0357EB642FEDA95D3912C6576945FAB897E7062CAA44A4AA06B8FE6E3DBA18AF6AE3738E30429EE9BE03427C9D64F695FA8CAB4BFE376853EA34AD1D76BFCAD15908C077FFE6DC5521ECEF5D278A96E26F57359FFAEDA19434B937F1AD999DC5C41EB11935B44C18100E857F431A4A5A6BB65114F174C2D7B59FDF237D6BB1DD0916E644D709DED56481477C75D95CDD68254615F7740EC07F330AC5D67BCD75BF23D28A140826C026DBDE971A37CD3EF9B8DF644AC385010501EFC6509D7A41DF040103DF03141FF80A40173F52D7D27E0F26A146A1C8CCB29046",
//
// "9F0605A0000000049F220105DF05083230313431323331DF060101DF070101DF0281B0B8048ABC30C90D976336543E3FD7091C8FE4800DF820ED55E7E94813ED00555B573FECA3D84AF6131A651D66CFF4284FB13B635EDD0EE40176D8BF04B7FD1C7BACF9AC7327DFAA8AA72D10DB3B8E70B2DDD811CB4196525EA386ACC33C0D9D4575916469C4E4F53E8E1C912CC618CB22DDE7C3568E90022E6BBA770202E4522A2DD623D180E215BD1D1507FE3DC90CA310D27B3EFCCD8F83DE3052CAD1E48938C68D095AAC91B5F37E28BB49EC7ED597DF040103DF0314EBFA0D5D06D8CE702DA3EAE890701D45E274C845",
//
// "9F0605A0000000049F220106DF05083230313631323331DF060101DF070101DF0281F8CB26FC830B43785B2BCE37C81ED334622F9622F4C89AAE641046B2353433883F307FB7C974162DA72F7A4EC75D9D657336865B8D3023D3D645667625C9A07A6B7A137CF0C64198AE38FC238006FB2603F41F4F3BB9DA1347270F2F5D8C606E420958C5F7D50A71DE30142F70DE468889B5E3A08695B938A50FC980393A9CBCE44AD2D64F630BB33AD3F5F5FD495D31F37818C1D94071342E07F1BEC2194F6035BA5DED3936500EB82DFDA6E8AFB655B1EF3D0D7EBF86B66DD9F29F6B1D324FE8B26CE38AB2013DD13F611E7A594D675C4432350EA244CC34F3873CBA06592987A1D7E852ADC22EF5A2EE28132031E48F74037E3B34AB747FDF040103DF0314F910A1504D5FFB793D94F3B500765E1ABCAD72D9",
//
// "9F0605A0000003339F220108DF050420301231DF060101DF070101DF028190B61645EDFD5498FB246444037A0FA18C0F101EBD8EFA54573CE6E6A7FBF63ED21D66340852B0211CF5EEF6A1CD989F66AF21A8EB19DBD8DBC3706D135363A0D683D046304F5A836BC1BC632821AFE7A2F75DA3C50AC74C545A754562204137169663CFCC0B06E67E2109EBA41BC67FF20CC8AC80D7B6EE1A95465B3B2657533EA56D92D539E5064360EA4850FED2D1BFDF040103DF0314EE23B616C95C02652AD18860E48787C079E8E85A",
//
// "9F0605A0000003339F220109DF05083230333031323331DF060101DF070101DF0281B0EB374DFC5A96B71D2863875EDA2EAFB96B1B439D3ECE0B1826A2672EEEFA7990286776F8BD989A15141A75C384DFC14FEF9243AAB32707659BE9E4797A247C2F0B6D99372F384AF62FE23BC54BCDC57A9ACD1D5585C303F201EF4E8B806AFB809DB1A3DB1CD112AC884F164A67B99C7D6E5A8A6DF1D3CAE6D7ED3D5BE725B2DE4ADE23FA679BF4EB15A93D8A6E29C7FFA1A70DE2E54F593D908A3BF9EBBD760BBFDC8DB8B54497E6C5BE0E4A4DAC29E5DF040103DF0314A075306EAB0045BAF72CDD33B3B678779DE1F527",
//
// "9F0605A0000003339F22010ADF05083230333031323331DF060101DF070101DF028180B2AB1B6E9AC55A75ADFD5BBC34490E53C4C3381F34E60E7FAC21CC2B26DD34462B64A6FAE2495ED1DD383B8138BEA100FF9B7A111817E7B9869A9742B19E5C9DAC56F8B8827F11B05A08ECCF9E8D5E85B0F7CFA644EFF3E9B796688F38E006DEB21E101C01028903A06023AC5AAB8635F8E307A53AC742BDCE6A283F585F48EFDF040103DF0314C88BE6B2417C4F941C9371EA35A377158767E4E3",
//
// "9F0605A0000003339F22010BDF05083230333031323331DF060101DF070101DF0281F8CF9FDF46B356378E9AF311B0F981B21A1F22F250FB11F55C958709E3C7241918293483289EAE688A094C02C344E2999F315A72841F489E24B1BA0056CFAB3B479D0E826452375DCDBB67E97EC2AA66F4601D774FEAEF775ACCC621BFEB65FB0053FC5F392AA5E1D4C41A4DE9FFDFDF1327C4BB874F1F63A599EE3902FE95E729FD78D4234DC7E6CF1ABABAA3F6DB29B7F05D1D901D2E76A606A8CBFFFFECBD918FA2D278BDB43B0434F5D45134BE1C2781D157D501FF43E5F1C470967CD57CE53B64D82974C8275937C5D8502A1252A8A5D6088A259B694F98648D9AF2CB0EFD9D943C69F896D49FA39702162ACB5AF29B90BADE005BC157DF040103DF0314BD331F9996A490B33C13441066A09AD3FEB5F66C",
//
// "9F0605A0000003339F220180DF05083230333031323331DF060101DF070101DF028180CCDBA686E2EFB84CE2EA01209EEB53BEF21AB6D353274FF8391D7035D76E2156CAEDD07510E07DAFCACABB7CCB0950BA2F0A3CEC313C52EE6CD09EF00401A3D6CC5F68CA5FCD0AC6132141FAFD1CFA36A2692D02DDC27EDA4CD5BEA6FF21913B513CE78BF33E6877AA5B605BC69A534F3777CBED6376BA649C72516A7E16AF85DF0403010001DF0314A5E44BB0E1FA4F96A11709186670D0835057D35E",
//
// "9F0605A0000000049F2201F6DF05083230303931323331DF060101DF070101DF0281E0A25A6BD783A5EF6B8FB6F83055C260F5F99EA16678F3B9053E0F6498E82C3F5D1E8C38F13588017E2B12B3D8FF6F50167F46442910729E9E4D1B3739E5067C0AC7A1F4487E35F675BC16E233315165CB142BFDB25E301A632A54A3371EBAB6572DEEBAF370F337F057EE73B4AE46D1A8BC4DA853EC3CC12C8CBC2DA18322D68530C70B22BDAC351DD36068AE321E11ABF264F4D3569BB71214545005558DE26083C735DB776368172FE8C2F5C85E8B5B890CC682911D2DE71FA626B8817FCCC08922B703869F3BAEAC1459D77CD85376BC36182F4238314D6C4212FBDD7F23D3DF040103DF0314502909ED545E3C8DBD00EA582D0617FEE9F6F684",
//
// "9F0605A0000003339F220185DF05083230323431323331DF060101DF070101DF0281f8C9242EC6030F10E5225E722AA17D9DC894299233AEC3219B950D4F243AF530FA13E3A31AFAA0D4BF4DE562B6B4C3108AEBBC6CB080F90770D532F241BC1536401E1BF72F9DC1B08933B9BF77403F6A0FB5777BAA4C9BE91574BBBFB521342A20386790512221F477FBC53FF1B6533A015815435410EC272F0A34EA0735C439677D7E46FBA766EC00CED59B6715E3412D6FB8A934BF9D1497A24A6252C52D7586FD66A450FB5D2B4484EC923061439622BC0535316CD4231C13C627BF4D2EDE1C02C802464658F1B9D7FF23A3698510FA90D0C3164942FB359255CD823CB2635B3F167FBDFC900641B970D602A2771A7F4F94DF6D34BE8BBBDF040103DF031410AC0A99C88419D84BF45A0B97E7B7470E01C4F1"};

    //直联,北方
//    public final static String TPDU = "6005010000";
    //直联,南方
    public final static String TPDU = "6006010000";

    //济南
//    public final static String TPDU = "6004500000";
    //东莞
//    public final static String TPDU = "6000020000";
    public final static String HEADER_APP_TYPE = "60";//应用类别：IC卡金融支付类应用
    public final static String HEADER_APP_VERSION = "31";//软件总版本号：银联POS统一版规范版本
    public final static String HEADER_APP_VERSION2 = "000001";//软件分版本号：2010年银联POS规范版本

    public final static String NET_LISCENSE_NO = "3137";//银联入网许可证号
    public final static int CHECK_CARD_TIMEOUT = 60 * 1000;//默认检卡超时时间
    public final static int CHECK_CARD_RETRY_TIMES = 3;//默认检卡重试次数
    public final static int NET_RESP_TIMEOUT = 60 * 1000;//默认网络响应超时时间
    public final static int NET_CONNECT_TIMEOUT = 10 * 1000;//默认网络连接超时时间
    public final static int REVERSE_RETRY_TIMES = 3;//默认冲正重试次数
    public final static double REFUND_AMOUNT_LIMITED = 99999999.99;//退货金额上限
    public final static int PASSWD_RETRY_TIMES = 3;//输密重试次数

    public final static boolean FLAG_ENCRYPT_TRACK_DATA = true;//联机上送时是否加密磁道信息
    public final static boolean FLAG_VOID_NEED_PIN = true;//消费撤销时是否需要输入密码
    public final static boolean FLAG_VOID_NEED_CHECK_CARD = true;//消费撤销时是否需要检卡
    public final static boolean CLSS_CARD_PREFERED = false;// 设置是否挥卡优先
    public final static boolean CAN_USE_ELECTRONIC_SIGN = true;// 是否开启电子签名
    public final static boolean AUTO_SIGN_OUT = true;// 是否批结算后自动签退

    private final static String SP_FILE_NAME = "business_config";//用于存储参数的XML文件名称
    private static BusinessConfig instance;

    /**
     * 参数值对应的Key
     */
    public static class Key {
        public final static String FLAG_PREFER_CLSS = "FLAG_PREFER_CLSS";//挥卡优先
        public final static String FLAG_TRADE_STORAGE_WARNING = "FLAG_TRADE_STORAGE_WARNING";
        public final static String FLAG_ESIGN_STORAGE_WARNING = "FLAG_ESIGN_STORAGE_WARNING";
        //交易流水存储预警，每次交易前需要判断该值是否为true，如果为true，需要强制进行批结算后才可以再次消费
        public final static String FLAG_NEED_UPLOAD_SCRIPT = "FLAG_NEED_UPLOAD_SCRIPT";//需要上送IC卡脚本执行结果的标识，用于下一次联机时上送脚本通知
        public final static String FLAG_SIGN_IN = "FLAG_SIGN_IN";//签到标识
        public final static String FLAG_AUTO_SIGN_OUT = "FLAG_AUTO_SIGN_OUT";//批结算后自动签退的标识
        public final static String FLAG_TEK = "FLAG_TEK";//TEK存在的标识

        public final static String KEY_MCHNT_NAME = "KEY_MCHNT_NAME";//商户名称
        public final static String KEY_MCHNT_ENGLISH_NAME = "KEY_MCHNT_ENGLISH_NAME";//商户英文名称
        public final static String KEY_POS_SERIAL = "KEY_POS_SERIAL";//终端流水号
        public final static String KEY_BATCH_NO = "KEY_BATCH_NO";//当前批次号
        public final static String KEY_MAX_TRANSACTIONS = "KEY_MAX_TRANSACTIONS";//终端存储的最大交易笔数
        public final static String KEY_LAST_BIN_NO = "KEY_LAST_BIN_NO";//最后一条卡BIN编号，用于下次请求时传入
        public final static String KEY_OPER_ID = "KEY_OPER_ID";//当前登录的操作员ID
        public final static String KEY_LAST_OPER_ID = "KEY_LAST_OPER_ID";//上一个操作员ID
        public final static String KEY_NOT_SIGN = "KEY_NOT_SIGN";// 是否免签
        public final static String KEY_NOT_PIN = "KEY_NOT_PIN";// 是否免密
        public final static String KEY_IS_LOCK = "KEY_IS_LOCK";// 是否锁机
        public final static String KEY_IS_BATCH_BUT_NOT_OUT = "KEY_IS_BATCH_BUT_NOT_OUT";// 是否批结算但是未签退
        public final static String KEY_NOT_SIGN_OR_PIN_AMOUNT = "KEY_NOT_SIGN_OR_PIN_AMOUNT";// 小额免签免密金额
        public final static String KEY_MAX_MESSAGE_RETRY_TIMES = "KEY_MAX_MESSAGE_RETRY_TIMES";//消息重发次数


        public final static String SETTLEMENT_MERCHANT_CD = "SETTLEMENT_MERCHANT_CD";//结算商户号
        public final static String SETTLEMENT_MERCHANT_NAME = "SETTLEMENT_MERCHANT_NAME";//结算商户名
        public final static String SETTLEMENT_TERMINAL_CD = "SETTLEMENT_TERMINAL_CD";//结算终端号
        public final static String KEY_LAST_BATCH_NO = "KEY_LAST_BATCH_NO";//上一批次号

        public final static String TOGGLE_SALE = "TOGGLE_SALE";
        public final static String TOGGLE_VOID = "TOGGLE_VOID";
        public final static String TOGGLE_BALANCE = "TOGGLE_BALANCE";
        public final static String TOGGLE_REFUND = "TOGGLE_REFUND";
        public final static String TOGGLE_AUTH = "TOGGLE_AUTH";
        public final static String TOGGLE_AUTH_COMPLETE = "TOGGLE_AUTH_COMPLETE";
        public final static String TOGGLE_CANCEL = "TOGGLE_CANCEL";
        public final static String TOGGLE_COMPLETE_VOID = "TOGGLE_COMPLETE_VOID";


        public final static String TOGGLE_ECASH = "TOGGLE_ECASH";

        public final static String TOGGLE_VOID_CHECKCARD = "TOGGLE_VOID_CHECKCARD";
        public final static String TOGGLE_COMPLETE_VOID_CHECKCARD = "TOGGLE_COMPLETE_VOID_CHECKCARD";
        public final static String TOGGLE_AUTH_COMPLETE_CHECKCARD = "TOGGLE_AUTH_COMPLETE_CHECKCARD";

        public final static String TOGGLE_VOID_INPUTWD = "TOGGLE_VOID_INPUTWD";
        public final static String TOGGLE_AUTH_VOID_INPUTWD = "TOGGLE_AUTH_VOID_INPUTWD";
        public final static String TOGGLE_AUTH_COMPLETE_INPUTWD = "TOGGLE_AUTH_COMPLETE_INPUTWD";
        public final static String TOGGLE_COMPLETE_VOID_INPUTWD = "TOGGLE_COMPLETE_VOID_INPUTWD";
        public final static String TOGGLE_REFUND_INPUTWD = "TOGGLE_REFUND_INPUTWD";

        public final static String TOGGLE_MASTER_PWD_INPUT = "TOGGLE_MASTER_PWD_INPUT";
        public final static String TOGGLE_CARD_NUM_BY_HAND = "TOGGLE_CARD_NUM_BY_HAND";
        public final static String TOGGLE_UPLOAD_BASE_STATION = "TOGGLE_UPLOAD_BASE_STATION";
        public final static String TOGGLE_PRINT_DOCUMENT = "TOGGLE_PRINT_DOCUMENT";

        public final static String MAINKEYINDEX = "MAINKEYINDEX";//主密钥索引

        public final static String SECURITYPWD = "SECURITYPWD";//安全密码

        public final static String OPERATOR_MANAGER_PWD = "operator_99_pwd";    //管理员密码

        public final static String KEEP_CONNECT_ALIVE = "KEEP_CONNECT_ALIVE";   //保持长连接

        public final static String REFUND_AMOUNT_LIMITED = "REFUND_AMOUNT_LIMITED"; //退货最大金额

        public final static String TOGGLE_TRACK_ENCRYPT = "TOGGLE_TRACK_ENCRYPT";   //磁道数据加密
        public final static String TOGGLE_TIP_SUPPORT = "TOGGLE_TIP_SUPPORT";   //小费支持开关
        public final static String TOGGLE_REVERSE_NOW = "TOGGLE_REVERSE_NOW";   //即时冲正，当笔交易异常后马上冲正
        public final static String TOGGLE_SLIP_ENGLISH = "TOGGLE_SLIP_ENGLISH";  //签购单打印英文
        public final static String TOGGLE_BATCH_UPLOAD = "TOGGLE_BATCH_UPLOAD";     //批上送支持
        public final static String TOGGLE_SYNC_TIME = "TOGGLE_SYNC_TIME";       //签到时同步平台时间
        public final static String TOGGLE_EMV_SM = "TOGGLE_EMV_SM";         //内核是否支持国密
        public static final String NFC_TRADE_CHANNEL = "NFC_TRADE_CHANNEL";     //非接交易优先通道
        public static final String TOGGLE_AUTO_PRINT_DETAILS = "TOGGLE_AUTO_PRINT_DETAILS"; //批结算后是否自动打印明细
        public static final String KEY_TRADE_VIEW_OP_TIMEOUT = "KEY_TRADE_VIEW_OP_TIMEOUT"; //交易界面超时时间
        public static final String TOGGLE_SLIP_TITLE_DEFAULT = "TOGGLE_SLIP_TITLE_DEFAULT"; //签购单抬头内容默认
        public static final String KEY_SLIP_TITLE_CONTENT = "KEY_SLIP_TITLE_CONTENT";   //签购单抬头内容
        public static final String ECASH_ERR_RETRY_TIMEOUT = "ECASH_ERR_RETRY_TIMEOUT"; //当笔重刷处理时间T1
        public static final String ECASH_ERR_RECORD_TIMEOUT = "ECASH_ERR_RECORD_TIMEOUT";   //闪卡记录可处理时间T2

        public static final String ECASH_ERR_RECORD_MAX = "ECASH_ERR_RECORD_MAX";   //闪卡记录最大值

        public static final String APN_ACCESS = "APN_ACCESS";   //APN接入点
        public static final String APN_USER_NAME = "APN_USER_NAME"; //APN用户名
        public static final String APN_USER_PASSWORD = "APN_USER_PASSWORD"; //APN密码
        public static final String APN_ENABLE = "APN_ENABLE";   //使用APN专用网络

        public static final String TOGGLE_OFFLINE_AUTH = "TOGGLE_OFFLINE_AUTH";   //离线交易 小额代授权

        public static final String KEY_SIGNATURE_PIC_MAX = "KEY_SIGNATURE_PIC_MAX";   //电子签名BMP图片的最大宽度
        public static final String KEY_APP_VERSION = "KEY_APP_VERSION";//版本号
        public static final String TRADE_KEEP_DAY = "TRADE_KEEP_DAY";//交易记录保存时间

        public final static String PRO_ENR = "PRO_ENR";   //生产环境
        public final static String USE_REVERVE = "USE_REVERVE";   //备用扫码环境开关
        public final static String USE_REVERVE_COMMON = "USE_REVERVE_COMMON";   //备用传统环境开关
        public final static String SCAN_ENCODE_FLAG = "SCAN_ENCODE_FLAG";   //扫码通讯加密 https
        public final static String SET_SN_HAND = "SET_SN_HAND";   //手动设置SN号
        public final static String ENCODE_TYPE = "ENCODE_TYPE";//加密方式 30:明文 31:全报文加密
        public final static String TMK_KEY = "TMK_KEY";
        public final static String TDK_KEY = "TDK_KEY";
        public final static String HOTLINE_KEY = "HOTLINE_KEY";
        public final static String WY_NOTICE_ADDRESS = "WY_NOTICE_ADDRESS";//物业回调地址
        public final static String MODEL = "MODEL";//终端类型
        public final static String PROJECT_ID = "PROJECT_ID";//项目ID
        public final static String E10_SN = "E10_SN";
        public final static String INIT_FLAG = "INIT_FLAG";//是否完成升级初始化标志

    }

    /**
     * 部分参数的默认值
     */
    private class Default {
        public final static boolean flagNotSign = false;
        public final static boolean flagNotPin = false;
        public final static boolean flagIsLock = false;
        public final static String cardBinNo = "000";
        public final static int posSerial = 1;
        public final static String posBatch = "000001";
    }

    public static BusinessConfig getInstance() {
        if (instance == null) {
            synchronized (BusinessConfig.class) {
                if (instance == null) {
                    instance = new BusinessConfig();
                }
            }
        }
        return instance;
    }

    @SuppressLint("ApplySharedPref")
    public void clearConfig(Context context) {
        getDefaultPres(context).edit().clear().commit();
    }

    private SharedPreferences getDefaultPres(Context context) {
        return context.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
    }

    public boolean getFlag(Context context, String key) {
        PreferDataPool defaultPool = ConfigureManager.getInstance(context).getDefaultParamsPool(context);
        boolean def = defaultPool.getBoolean(key, false);
        if (Key.KEY_NOT_SIGN.equals(key)) {
            def = Default.flagNotSign;
        } else if (Key.KEY_NOT_PIN.equals(key)) {
            def = Default.flagNotPin;
        } else if (Key.KEY_IS_LOCK.equals(key)) {
            def = Default.flagIsLock;
        } else if(BusinessConfig.Key.SCAN_ENCODE_FLAG.equals(key)||BusinessConfig.Key.PRO_ENR.equals(key)){
            //扫码 通讯加密和生产环境
            def = true;
        }
        return getDefaultPres(context).getBoolean(key, def);
    }

    public boolean getToggle(Context context, String key) {
        PreferDataPool defaultPool = ConfigureManager.getInstance(context).getDefaultParamsPool(context);
        boolean def = defaultPool.getBoolean(key, true);
        if (Key.FLAG_AUTO_SIGN_OUT.equals(key)) {
            def = defaultPool.getBoolean(Keys.obj().flag_auto_sign_out);
        }
        if (Key.KEY_IS_LOCK.equals(key)) {
            def = Default.flagIsLock;
        }
        return getDefaultPres(context).getBoolean(key, def);
    }

    public void setFlag(Context context, String key, boolean value) {
        getDefaultPres(context).edit().putBoolean(key, value).commit();
    }

    public String getValue(Context context, String key) {
        PreferDataPool defaultPool = ConfigureManager.getInstance(context).getDefaultParamsPool(context);
        String def = defaultPool.getString(key);
        if (KEY_LAST_BIN_NO.equals(key)) {
            def = Default.cardBinNo;
        } else if (KEY_BATCH_NO.equals(key)) {
            def = Default.posBatch;
        } else if (SETTLEMENT_MERCHANT_NAME.equals(key) ||
                SETTLEMENT_MERCHANT_CD.equals(key) ||
                SETTLEMENT_TERMINAL_CD.equals(key)) {
            def = "";
        }else if(HOTLINE_KEY.equals(key)){
            def = Config.DEFAULT_HOTLINE;
        }else if(PROJECT_ID.equals(key)){
            def = Config.DEFAULT_PROJECT_ID;
        }
        return getDefaultPres(context).getString(key, def);
    }

    public void setValue(Context context, String key, String value) {
        getDefaultPres(context).edit().putString(key, value).commit();
    }

    public int getNumber(Context context, String key) {
        PreferDataPool defaultPool = ConfigureManager.getInstance(context).getDefaultParamsPool(context);
        int def = defaultPool.getInt(key);
        if (KEY_POS_SERIAL.equals(key)) {
            def = Default.posSerial;
        }else if (TRADE_KEEP_DAY.equals(key)) {
            def = Config.TRADE_KEEP_DAY;
        }
        return getDefaultPres(context).getInt(key, def);
    }

    public void setNumber(Context context, String key, int num) {
        getDefaultPres(context).edit().putInt(key, num).commit();
    }

    /**
     * 获取保存的8583域数据。其中41域（受卡机终端标识码），42域（受卡方标识码），43域（商户名称）
     *
     * @param context context
     * @param fieldIndex 域的索引值
     * @return 已保存的该域的值
     */
    public String getIsoField(Context context, int fieldIndex) {
        return getDefaultPres(context).getString("ISO_" + fieldIndex, "");
    }

    /**
     * 保存8583数据域
     *
     * @param context context
     * @param fieldIndex 域索引
     * @param value 值
     */
    public void setIsoField(Context context, int fieldIndex, String value) {
        getDefaultPres(context).edit().putString("ISO_" + fieldIndex, value.trim()).apply();
    }

    /**
     * 获取终端流水号
     *
     * @param context context
     * @param autoIncrease 是否自增
     * @param autoSave 自增后是否自动保存
     * @return 当前终端流水号
     */
    public String getPosSerial(Context context, boolean autoIncrease, boolean autoSave) {
        int serial = getNumber(context, KEY_POS_SERIAL);
        if (autoIncrease) {
            if (serial == 999999) {
                serial = 1;
            } else {
                serial++;
            }
        }
        if (autoSave) {
            setNumber(context, KEY_POS_SERIAL, serial);
        }
        String str = Integer.toString(serial);
        StringBuilder sBuilder = new StringBuilder();
        for (int i = 0; i < 6 - str.length(); i++) {
            sBuilder.append("0");
        }
        sBuilder.append(str);
        return sBuilder.toString();
    }

    /**
     * 获取终端流水号
     *
     * @param context context
     * @return 终端流水号
     */
    public String getPosSerial(Context context) {
        return getPosSerial(context, true, true);
    }

    /**
     * 设置终端流水号
     *
     * @param context context
     * @param serial 流水号
     * @return 设置成功返回true，否则返回false
     */
    public boolean setPosSerial(Context context, String serial) {
        try {
            Integer i = Integer.valueOf(serial);
            if (i > 0 && i < 999999) {
                setNumber(context, KEY_POS_SERIAL, i);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取批次号
     *
     * @param context context
     * @return 当前批次号
     */
    public String getBatchNo(Context context) {
        return getValue(context, KEY_BATCH_NO);
    }

    /**
     * 设置批次号
     *
     * @param context context
     * @param value 批次号
     * @return 设置成功返回true，否则返回false
     */
    public boolean setBatchNo(Context context, String value) {
        try {
            Integer i = Integer.valueOf(value);
            if (i > 0 && i < 999999) {
                String str = Integer.toString(i);
                StringBuilder sBuilder = new StringBuilder();
                for (int j = 0; j < 6 - str.length(); j++) {
                    sBuilder.append("0");
                }
                sBuilder.append(str);
                setValue(context, KEY_BATCH_NO, sBuilder.toString());
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取安全密码
     *
     * @param context context
     * @return 当前批次号
     */
    public String getSecurityPwd(Context context) {
        return getValue(context, SECURITYPWD);
    }

    /**
     * 设置安全密码
     *
     * @param context context
     * @return 当前批次号
     */
    public boolean setSecurityPwd(Context context, String newPwd) {
        setValue(context, SECURITYPWD, newPwd);
        return true;
    }

    /**
     * 菜单是否关闭
     *
     * @param menuTag 对应配置文件中的 enTag
     */
    public boolean isTradeOpened(String menuTag) {
        if (TextUtils.isEmpty(menuTag))
            return true;

        IIsTradeOpened iIsTradeOpened = (IIsTradeOpened) ConfigureManager.getSubPrjClassInstance(new BaseIsTradeOpened());

        return iIsTradeOpened.isTradeOpened(menuTag);
    }

    public boolean setTradeClosed(String key, Boolean value) {
        if (TextUtils.isEmpty(key))
            return false;
        return getDefaultPres(EposApplication.getAppContext()).edit().putBoolean(key, value).commit();
    }

    @SuppressLint("ApplySharedPref")
    public static void clearConfig(){
        SharedPreferences configStore = EposApplication.getAppContext().getSharedPreferences(SP_FILE_NAME, Context
                .MODE_PRIVATE);
        String operatorID = configStore.getString(Key.KEY_OPER_ID, "");
        configStore.edit().clear().commit();
        if (!TextUtils.isEmpty(operatorID))
            configStore.edit().putString(Key.KEY_OPER_ID, operatorID).commit();
    }


    /**
     * 读取加密标志
     * @param context   上下文
     * @return  true 要加密
     */
    public static boolean isTrackEncrypt(Context context) {
        return getInstance().getFlag(context, Key.TOGGLE_TRACK_ENCRYPT);
    }

    public static boolean isTrackEncrypt() {
        return isTrackEncrypt(EposApplication.getAppContext());
    }


    /**
     * 设置磁道数据是否要加密
     * @param context   上下文
     * @param isEncrypt 是否加密
     */
    public static void setTrackEncrypt(Context context, boolean isEncrypt) {
        getInstance().setFlag(context, Key.TOGGLE_TRACK_ENCRYPT, isEncrypt);
    }

    public static boolean isTrackEncryKeyExist(){
        return isTrackEncrypt(EposApplication.getAppContext());
    }

}
