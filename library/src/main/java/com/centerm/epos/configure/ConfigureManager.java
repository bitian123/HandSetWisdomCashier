package com.centerm.epos.configure;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.centerm.epos.EposApplication;
import com.centerm.epos.annotation.GroupConstant;
import com.centerm.epos.annotation.ISOField;
import com.centerm.epos.annotation.ManagerTrade;
import com.centerm.epos.annotation.MgrTradeChecker;
import com.centerm.epos.annotation.RedevelopAction;
import com.centerm.epos.annotation.TradeControlle;
import com.centerm.epos.annotation.TradePresent;
import com.centerm.epos.annotation.TradeView;
import com.centerm.epos.bean.TranscationFactor;
import com.centerm.epos.redevelop.IRedevelopAction;
import com.centerm.epos.utils.ClassUtil;
import com.centerm.epos.xml.XmlParser;
import com.centerm.epos.xml.bean.ConfigCatalog;
import com.centerm.epos.xml.bean.DefaultParams;
import com.centerm.epos.xml.bean.PreferDataPool;
import com.centerm.epos.xml.bean.RedevelopItem;
import com.centerm.epos.xml.bean.TradeItem;
import com.centerm.epos.xml.bean.XmlFile;
import com.centerm.epos.xml.bean.menu.Menu;
import com.centerm.epos.xml.bean.message.Iso8583FieldProcessItem;
import com.centerm.epos.xml.bean.process.TradeProcess;
import com.centerm.epos.xml.bean.project.ProjectConfig;
import com.centerm.epos.xml.bean.slip.SlipElement;
import com.centerm.epos.xml.keys.Keys;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import config.Config;

/**
 * 配置管理类
 * author:wanliang527</br>
 * date:2017/2/8</br>
 */
public class ConfigureManager {
    private static ConfigureManager instance;
    private static Logger logger = Logger.getLogger(ConfigureManager.class);
    private static final String CATALOG_FILE_NAME = "catalog.xml";
    private static final String ANNOTATION_CONFIG_FILE = "/transaction/annotation-config.xml";
    private static Context mAppContext;

    private ConfigCatalog baseConfig;//基础版本配置文件目录
    private ConfigCatalog projectConfig;//当前项目对应的配置文件目录

    private PreferDataPool baseFuncToggle;//基础版本业务开关
    private PreferDataPool projectFuncToggle;//当前项目业务开关

    private PreferDataPool paramsPool;//参数默认值参考。具体参数的存放位置、类型控制由基础版本的BusinessConfig类执行，对于子项目而言，只需要定义对应的值。

    private Map<String, DefaultParams> baseParams;//基础版本参数
    private Map<String, DefaultParams> projectParams;//当前项目的参数

    private Map<String, String> xmlProperties;//xml变量值
    private Map<String, TradeItem> tradeItemMap;//管理业务配置

    private Map<Integer, String> fieldProcessClz;

    private String project = EposProject.BASE_PRJ_TAG;//项目，默认指向基础版本

    private Map<String, RedevelopItem> mRedevelopItemMap;//二次开发配置

    private Map<String, String> mTradeViewMap;       //交易流程节点数据的视图界面
    private Map<String, String> mTradePresentMap;    //交易流程节点的业务
    private Map<String, String> mTradeControllMap;    //交易流程节点的控制
    private Map<String, String> mTradeModel;     //交易流程节点的数据模型

    private ConfigureManager() {
    }

    public static ConfigureManager getInstance(Context context) {
        if (instance == null) {
            synchronized (ConfigureManager.class) {
                if (instance == null) {
                    instance = new ConfigureManager();
                    mAppContext = context.getApplicationContext();
                }
            }
        }
        if (instance.baseConfig == null) {
            //基础版本的配置文件目录未加载，先进行加载
            instance.baseConfig = XmlParser.parseConfigCatalog(context, EposProject.BASE_PRJ_TAG + "/" +
                    CATALOG_FILE_NAME);
        }
        if (EposProject.getInstance().isBaseProject(instance.project)) {
            //当前项目完全使用基础项目的配置文件
            instance.projectConfig = instance.baseConfig;
        } else {
            if (instance.projectConfig == null) {
                //开始解析当前项目对应的配置文件
                instance.projectConfig = XmlParser.parseConfigCatalog(context, instance.project + "/" +
                        CATALOG_FILE_NAME);
            }
        }
        return instance;
    }

    /**
     * 设置当前项目。设置项目决定了后续的各项配置。
     *
     * @param context context
     * @param project 项目枚举对象
     * @return 设置成功返回true，如果当前项目无任何改变，则设置失败返回false
     */
    public boolean setProject(Context context, String project) {
        if (this.project != null && !this.project.equals(project)) {
            //项目更改，需要重新解析配置目录
            this.projectConfig = XmlParser.parseConfigCatalog(context, project + "/" + CATALOG_FILE_NAME);
            this.project = project;
            logger.info("The ConfigManager is changing project configuration. The project name is [" + project + "]");
            return true;
        }
        logger.warn("There is no change about project");
        return false;
    }

    public String getProject() {
        return project;
    }


//    public static Object getRedevelopClassInstance(String tagStr) {
//        Map<String, String> redevelopMap = ConfigureManager.getInstance(EposApplication.getAppContext())
//                .getRedevelopProcessFromAnnotion();
//        if (redevelopMap == null || !redevelopMap.containsKey(tagStr))
//            return null;
//        String className = redevelopMap.get(tagStr);
//        if (TextUtils.isEmpty(className))
//            return null;
//        try {
//            Class clzz = Class.forName(className);
//            if (IRedevelopAction.class.isAssignableFrom(clzz))
//                return clzz.newInstance();
//            logger.debug("^_^ 类" + clzz.getName() + "未实现接口：" + IRedevelopAction.class.getName() + "^_^");
//        } catch (Exception e) {
//            logger.info(e.getMessage());
//        }
//        return null;
//    }


    public static Object getRedevelopAction(String tag, Class<?> type) {
        ConfigureManager config = ConfigureManager.getInstance(EposApplication.getAppContext());
        RedevelopItem redevelop = config.getRedevelopItem(EposApplication.getAppContext(), tag);
        if (redevelop == null)
            return null;
        String clzName = redevelop.getClassName();
        try {
            Class clz = Class.forName(clzName);
            if (type == null) {
                type = IRedevelopAction.class;
            }
            if (type.isAssignableFrom(clz)) {
                return clz.newInstance();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static IRedevelopAction getRedevelopAction(String tag) {
        return (IRedevelopAction) getRedevelopAction(tag, null);
    }

    public static Object getSubPrjClassInstance(Object obj) {
        Object prjObj = ConfigureManager.getInstance(EposApplication.getAppContext()).getSubPrjClassInstance(obj
                .getClass());
        if (prjObj == null)
            return obj;
        return prjObj;
    }

    public Object getSubPrjClassInstance(Class clz) {
        if (!EposProject.getInstance().isBaseProject(project)) {
            String packageName = Config.BASE_PACKAGE_NAME;
            String clzName = clz.getName().replace(packageName, packageName + "." + project.toLowerCase());
            //包名修改后，会出现类名一样的问题，直接返回
            if (clz.getName().equals(clzName))
                return null;
            try {
                Class clzz = Class.forName(clzName);
                return clzz.newInstance();
            } catch (Exception e) {
//                e.printStackTrace();
                logger.info(e.getMessage());
            }
        }
        return null;
    }

    /**
     * 从项目的默认包路径(com.centerm.epos.custom.redevelop)中去获取指定类名的实例。类必须实现IRedevelopAction接口
     *
     * @param classSimpleName 类名，不包括限定
     * @return null 获取失败
     */
    public Object getSubPrjClassInstance(String classSimpleName) {
        String clzName = "com.centerm.epos.custom.redevelop." + classSimpleName;
        try {
            Class clz = Class.forName(clzName);
            if (IRedevelopAction.class.isAssignableFrom(clz)) {
                return clz.newInstance();
            }
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        return null;
    }

    public static Object getProjectClassInstance(Class clz) {
        return ConfigureManager.getInstance(EposApplication.getAppContext()).getSubPrjClassInstance(clz);
    }

    public static IRedevelopAction getProjectClassInstance(String clzSimpleName) {
        return (IRedevelopAction) ConfigureManager.getInstance(EposApplication.getAppContext())
                .getSubPrjClassInstance(clzSimpleName);
    }

    public Object getSubPrjClassInstance(Class clz, Object[] paramObjs) {
        if (paramObjs == null || paramObjs.length == 0) {
            return getSubPrjClassInstance(clz);
        }
        if (!EposProject.getInstance().isBaseProject(project)) {
            String packageName = Config.BASE_PACKAGE_NAME;
            String clzName = clz.getName().replace(packageName, packageName + "." + project.toLowerCase());
            try {
                Class clzz = Class.forName(clzName);
                Class[] paramClz = new Class[paramObjs.length];
                for (int i = 0; i < paramObjs.length; i++) {
                    paramClz[i] = paramObjs[i].getClass();
                }
                Constructor constructor = clzz.getConstructor(paramClz);
                return constructor.newInstance(paramObjs);
            } catch (Exception e) {
                logger.info(e.getMessage());
            }
        }
        return null;
    }

    /**
     * 获取主菜单
     *
     * @param context context
     * @return 当前项目对应的主菜单
     */
    public Menu getPrimaryMenu(Context context) {
        String tag = Keys.obj().primaryMenu;
        String fileName = getAbsFileName(projectConfig, tag);
        if (fileName == null) {
            fileName = getAbsFileName(baseConfig, tag);
        }
        return XmlParser.parseMenu(context, fileName);
    }

    /**
     * 获取第二菜单
     *
     * @param context context
     * @return 第二菜单
     */
    public Menu getSecondaryMenu(Context context) {
        String tag = Keys.obj().secondaryMenu;
        String fileName = getAbsFileName(projectConfig, tag);
        if (fileName == null) {
            fileName = getAbsFileName(baseConfig, tag);
        }
        return XmlParser.parseMenu(context, fileName);
    }

    /**
     * 获取第三菜单
     *
     * @param context context
     * @return 第三菜单
     */
    public Menu getThirdlyMenu(Context context) {
        String tag = Keys.obj().thirdlyMenu;
        String fileName = getAbsFileName(projectConfig, tag);
        if (fileName == null) {
            fileName = getAbsFileName(baseConfig, tag);
        }
        return XmlParser.parseMenu(context, fileName);
    }

    /**
     * 获取快捷方式菜单
     *
     * @param context context
     * @return 快捷方式菜单
     */
    public Menu getShortcutMenu(Context context) {
        String tag = Keys.obj().shortcut;
        String fileName = getAbsFileName(projectConfig, tag);
        if (fileName == null) {
            fileName = getAbsFileName(baseConfig, tag);
        }
        return XmlParser.parseMenu(context, fileName);
    }

    private final static String FACTORY_MENU_PATH = "BASE/menu/factory.xml";//出厂菜单文件路径
    private final static String PRJ_FACTORY_MENU_PATH = "MAIN/menu/factory.xml";//子项目配置的菜单文件路径

    public ProjectConfig getProjectConfig() {
        ProjectConfig projectConfig = XmlParser.parseProjectConfig(mAppContext, FACTORY_MENU_PATH);
        ProjectConfig mainPrjConfig = XmlParser.parseProjectConfig(mAppContext, PRJ_FACTORY_MENU_PATH);
        if (projectConfig != null && mainPrjConfig != null && mainPrjConfig.getItemCount() > 0) {
            projectConfig.getPrjItems().addAll(mainPrjConfig.getPrjItems());
            projectConfig.setDefaultPrjTag(mainPrjConfig.getDefaultPrjTag());
        }
        return projectConfig;
    }

    public Menu getFactoryMenu() {
        Menu factoryMenu = XmlParser.parseMenu(mAppContext, FACTORY_MENU_PATH);
        Menu prjConfigMenu = XmlParser.parseMenu(mAppContext, PRJ_FACTORY_MENU_PATH);
        if (factoryMenu != null && prjConfigMenu != null && prjConfigMenu.getCounts() > 0) {
            factoryMenu.getItemList().addAll(prjConfigMenu.getItemList());
        }
        return factoryMenu;
    }

    /**
     * 获取基础版本各可选功能开关配置
     *
     * @param context context
     * @return 基础版本可选功能配置
     */
    private PreferDataPool getBaseFuncToggle(Context context) {
        return XmlParser.parsePreferData(context, getAbsFileName(baseConfig, Keys.obj().functionToggle));
    }


    /**
     * 获取xml文件的绝对路径
     *
     * @param catalog 配置文件目录
     * @param tag 标签
     * @return 对应标签的xml文件的绝对路径。如果项目未定义目录文件，则默认从基础版本的目录文件中读取相关配置
     */
    private String getAbsFileName(ConfigCatalog catalog, String tag) {
        if (catalog == null) {
            logger.info("The Catalog object is null!");
            return null;
        }
        String proj = catalog.getProject();
        XmlFile file = catalog.getXmlFile(tag);
        if (file == null || !file.isEnable()) {
            logger.warn(proj + "==>The xml file of [" + tag + "] is null or disabled");
            return null;
        }
        if (TextUtils.isEmpty(file.getFileName())) {
            logger.warn(proj + "==>The xml file of [" + tag + "] is empty");
            return null;
        }
        String projectName = catalog.getProject();
        String absFile = projectName + "/" + file.getFileName();
        logger.debug(proj + "==>Absolute file name is [" + absFile + "]");
        return absFile;
    }

    /**
     * 根据功能标签判断该功能是否开启。
     *
     * @param context context
     * @param function 功能标签
     * @return 开启返回true，否则返回false
     */
    public boolean isOptionFuncEnable(Context context, String function) {
        if (projectFuncToggle == null) {
            projectFuncToggle = XmlParser.parsePreferData(context, getAbsFileName(baseConfig, Keys.obj()
                    .functionToggle));
            if (projectConfig != null && projectConfig != baseConfig) {
                PreferDataPool funcToggle = XmlParser.parsePreferData(context, getAbsFileName(projectConfig,
                        Keys.obj().functionToggle));
                if (funcToggle != null)
                    projectFuncToggle.putAll(funcToggle);
            }
        }
        logger.warn("isOptionFuncEnable===>" + function + " getBoolean===>"+projectFuncToggle.getBoolean(function, false));
        return projectFuncToggle == null ? false : projectFuncToggle.getBoolean(function, false);
    }

    public boolean isOptionFuncEnable(Context context, String function, boolean defaultValue) {
        if (projectFuncToggle == null) {
            projectFuncToggle = XmlParser.parsePreferData(context, getAbsFileName(baseConfig, Keys.obj()
                    .functionToggle));
            if (projectConfig != null && projectConfig != baseConfig) {
                PreferDataPool funcToggle = XmlParser.parsePreferData(context, getAbsFileName(projectConfig,
                        Keys.obj().functionToggle));
                if (funcToggle != null)
                    projectFuncToggle.putAll(funcToggle);
            }
        }
        return projectFuncToggle == null ? defaultValue : projectFuncToggle.getBoolean(function, defaultValue);
    }

    /**
     * 获取基础版本中，各功能的启用状况
     *
     * @param context context
     * @param function 功能标签
     * @return 开启返回true，否则返回false
     */
    private boolean isFunctionEnableFromBase(Context context, String function) {
        if (baseFuncToggle == null) {
            baseFuncToggle = XmlParser.parsePreferData(context, getAbsFileName(baseConfig, Keys.obj().functionToggle));
        }
        return baseFuncToggle.getBoolean(function, false);
    }

    /**
     * 获取基础版本默认参数
     *
     * @param context context
     * @return 基础版本参数Map
     * @deprecated 弃用
     */
    private Map<String, DefaultParams> getBaseParamsMap(Context context) {
        if (baseParams == null) {
            baseParams = XmlParser.parseParamsMap(context, getAbsFileName(baseConfig, Keys.obj().defaultParams));
        }
        return baseParams;
    }

    /**
     * 获取当前项目的默认参数
     *
     * @param context context
     * @return 当前项目的默认参数，可能为空（如果未定义的话）
     * @deprecated 弃用
     */
    private Map<String, DefaultParams> getProjectParamsMap(Context context) {
        if (projectConfig == null || projectConfig.getXmlFile(Keys.obj().defaultParams) == null) {
            return null;
        }
        if (projectParams == null) {
            projectParams = XmlParser.parseParamsMap(context, getAbsFileName(projectConfig, Keys.obj().defaultParams));
        }
        return projectParams;
    }

    /**
     * @param context Context
     * @param key 键值
     * @return 默认参数值对象
     * @deprecated 弃用
     */
    public DefaultParams getDefaultParams(Context context, String key) {
        Map<String, DefaultParams> map = getProjectParamsMap(context);
        if (map != null && map.containsKey(key)) {
            return map.get(key);
        }
        map = getBaseParamsMap(context);
        DefaultParams params = map.get(key);
        if (params != null) {
            return params;
        }
        return null;
    }


    /**
     * 获取XML配置文件的属性值
     *
     * @param key 键值
     * @return 属性
     */
    public String getXmlProperties(String key) {
        Map<String, String> proper = getXmlProperties();
        return proper == null ? null : proper.get(key);
    }

    public Map<String, String> getXmlProperties() {
        if (baseConfig == null || baseConfig.getXmlFile(Keys.obj().xmlProperties) == null) {
            return null;
        }
        if (xmlProperties == null) {
            xmlProperties = XmlParser.parseXmlPropertiesMap(EposApplication.getAppContext(), getAbsFileName
                    (baseConfig, Keys.obj().xmlProperties));
            if (projectConfig != null && projectConfig != baseConfig && projectConfig.getXmlFile(Keys.obj()
                    .xmlProperties) != null) {
                Map<String, String> projectProperties = XmlParser.parseXmlPropertiesMap(EposApplication.getAppContext
                        (), getAbsFileName(projectConfig, Keys.obj().xmlProperties));
                if (xmlProperties == null)
                    xmlProperties = projectProperties;
                else if (projectProperties != null)
                    xmlProperties.putAll(projectProperties);
            }
        }
        return xmlProperties == null ? null : xmlProperties;
    }


    public TradeItem getTradeItem(String key) {
        Map<String, TradeItem> proper = getTradeItemMap();
        return proper == null ? null : proper.get(key);
    }

    /**
     * 获取管理业务的配置
     *
     * @return 业务配置
     */
    public Map<String, TradeItem> getTradeItemMap() {
        if (baseConfig == null || baseConfig.getXmlFile(Keys.obj().tradeItem) == null) {
            return null;
        }
        if (tradeItemMap == null) {
            tradeItemMap = XmlParser.parseTradeItem(EposApplication.getAppContext(), getAbsFileName
                    (baseConfig, Keys.obj().tradeItem));
            if (projectConfig != null && projectConfig != baseConfig && projectConfig.getXmlFile(Keys.obj()
                    .tradeItem) != null) {
                Map<String, TradeItem> projectTradeItems = XmlParser.parseTradeItem(EposApplication.getAppContext
                        (), getAbsFileName(projectConfig, Keys.obj().tradeItem));
                if (tradeItemMap == null)
                    tradeItemMap = projectTradeItems;
                else if (projectTradeItems != null)
                    tradeItemMap.putAll(projectTradeItems);
            }
            Map<String, TradeItem> annotationTradeItems = getManagerTradeItemFromAnnotion(mAppContext);
            if (annotationTradeItems != null && annotationTradeItems.size() > 0) {
                tradeItemMap.putAll(annotationTradeItems);
            }
        }
        return tradeItemMap;
    }

    /**
     * @param context Context
     * @param key 键
     * @return XML文件中定义的原始值
     * @deprecated 弃用
     */
    public String getDefaultParamsValue(Context context, String key) {
        DefaultParams params = getDefaultParams(context, key);
        if (params != null) {
            return params.getValue();
        }
        return null;
    }

    public PreferDataPool getDefaultParamsPool(Context context) {
        if (paramsPool == null) {
            synchronized (ConfigureManager.class) {
                if (paramsPool == null) {
                    paramsPool = XmlParser.parsePreferData(context, getAbsFileName(baseConfig, Keys.obj()
                            .defaultParams));
                    PreferDataPool proPool = XmlParser.parsePreferData(context, getAbsFileName(projectConfig, Keys.obj()
                            .defaultParams));
                    if (paramsPool != null && proPool != null) {
                        paramsPool.putAll(proPool);
                    }
                }
            }
        }
        return paramsPool;
    }

    /**
     * 获取交易流程对象
     *
     * @param context context
     * @param fileName 交易流程的文件名称
     * @return 交易流程对象
     */
    public TradeProcess getTradeProcess(Context context, String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return null;
        }
        String t = fileName;
        if (!t.endsWith(".xml")) {
            t += ".xml";
        }
        String tradeFlowPath = getAbsFileName(projectConfig, Keys.obj().tradeFlow);
        if (projectConfig != null && !TextUtils.isEmpty(tradeFlowPath)) {
            boolean exist = false;
            try {
                String[] files = context.getAssets().list(tradeFlowPath);
                for (int i = 0; i < files.length; i++) {
                    if (files[i].equalsIgnoreCase(t)) {
                        exist = true;
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (exist) {
                return XmlParser.parseProcess(context, tradeFlowPath + "/" + t.toLowerCase());
            } else {
                logger.warn("当前项目未定义流程文件：" + fileName + "==>将从基础版本中提取流程");
            }
        }
        return XmlParser.parseProcess(context, getAbsFileName(baseConfig, Keys.obj().tradeFlow) + "/" + t.toLowerCase());
    }

    /**
     * 获取交易流程文件定义的路径
     *
     * @return 交易流程文件路径
     */
    public String getTradeProcessPath() {
        return getAbsFileName(projectConfig, Keys.obj().tradeFlow);
    }

    private Map<String, RedevelopItem> getRedevelopMapFromBase(Context context) {
        String xmlFile = getAbsFileName(baseConfig, Keys.obj().redevelop);
        mRedevelopItemMap = XmlParser.parseRedevelopMap(context, xmlFile);
        return mRedevelopItemMap;
    }

    public RedevelopItem getRedevelopItem(Context context, String key) {
        if (mRedevelopItemMap != null && mRedevelopItemMap.size() > 0)
            return mRedevelopItemMap.get(key);
        //填充基础版本的二次开发配置
        getRedevelopMapFromBase(context);
        if (!projectConfig.equals(baseConfig)) {
            String xmlFile = getAbsFileName(projectConfig, Keys.obj().redevelop);
            if (xmlFile != null) {
                Map<String, RedevelopItem> map = XmlParser.parseRedevelopMap(context, xmlFile);
                if (map != null && map.size() > 0) {
                    mRedevelopItemMap.putAll(map);
                }
            }
        }
        mRedevelopItemMap.putAll(getRedevelopProcessFromAnnotion());
        return mRedevelopItemMap == null ? null : mRedevelopItemMap.get(key);
    }

    /**
     * 读取交易要素配置信息
     *
     * @param context 上下文环境
     * @return 配置信息
     */
    public Map<String, TranscationFactor> getTranscationFactorTable(Context context) {
        String xmlFile = getAbsFileName(baseConfig, Keys.obj().tradeFactor);
        Map<String, TranscationFactor> baseFactorMap = XmlParser.parseTradeFactor(context, xmlFile);
        if (projectConfig.equals(baseConfig))
            return baseFactorMap;

        xmlFile = getAbsFileName(projectConfig, Keys.obj().tradeFactor);
        if (!TextUtils.isEmpty(xmlFile)) {
            Map<String, TranscationFactor> prjFactorMap = XmlParser.parseTradeFactor(context, xmlFile);
            if (prjFactorMap != null && prjFactorMap.size() > 0 && baseFactorMap != null)
                baseFactorMap.putAll(prjFactorMap);
        }
        return baseFactorMap;
    }

    /**
     * 获取签购单模板
     *
     * @param context context
     * @return 签购单的元素列表
     */
    public List<SlipElement> getSlipTemplate(Context context) {
        String fileName = getAbsFileName(projectConfig, Keys.obj().saleSlip);
        if (fileName == null) {
            fileName = getAbsFileName(baseConfig, Keys.obj().saleSlip);
        }
        return XmlParser.parseSlipElements(context, fileName);
    }

    public List<SlipElement> getSlipTemplate(Context context, String slipTag) {
        if (TextUtils.isEmpty(slipTag))
            return null;
        String fileName = getAbsFileName(projectConfig, slipTag);
        if (fileName == null) {
            fileName = getAbsFileName(baseConfig, slipTag);
        }
        return XmlParser.parseSlipElements(context, fileName);
    }

    public List<Iso8583FieldProcessItem> getPrjIso8583FieldProcessItems(Context context) {
        List<Iso8583FieldProcessItem> processItems = null;
        String fileName = getAbsFileName(projectConfig, Keys.obj().iso_field_process);
        if (baseConfig.getProject().equals(projectConfig.getProject()))
            return null;
        if (!TextUtils.isEmpty(fileName)) {
            //替换项目配置的数据域处理
            processItems = XmlParser.parseIsoFieldProcess(context, fileName);
        }
        return processItems;
    }

    public List<Iso8583FieldProcessItem> getBaseIso8583FieldProcessItems(Context context) {
        List<Iso8583FieldProcessItem> processItems = null;
        String fileName = getAbsFileName(baseConfig, Keys.obj().iso_field_process);
        if (!TextUtils.isEmpty(fileName)) {
            //替换项目配置的数据域处理
            processItems = XmlParser.parseIsoFieldProcess(context, fileName);
        }

        return processItems;
    }

    /**
     * 获取添加了IsoFile注解的类。
     *
     * @param context 应用上下文
     * @return 域数据处理列表
     */
    public List<Iso8583FieldProcessItem> getIso8583FieldProcessFromAnnotion(Context context) {
        List<Iso8583FieldProcessItem> processItems = null;
        List<String> packageList = getScanPackages(context, GroupConstant.ISO_FIELD_TAG);
        if (packageList == null || packageList.size() == 0) return null;
        for (String packageName : packageList) {
            List<String> classNameList = ClassUtil.scanPackage(packageName);
            if (classNameList == null || classNameList.size() == 0)
                continue;
            processItems = new ArrayList<>();
            Iso8583FieldProcessItem item;
            for (String className : classNameList) {
                try {
                    Class clz = Class.forName(className);
                    if (clz.isAnnotationPresent(ISOField.class)) {
                        ISOField isoField = (ISOField) clz.getAnnotation(ISOField.class);
                        if (isoField != null) {
                            item = new Iso8583FieldProcessItem();
                            item.setIndex(isoField.fieldIndex());
                            item.setName(isoField.name());
                            item.setProcessClz(className);
                            processItems.add(item);
                        }
                    }
                } catch (ClassNotFoundException e) {
                    logger.info(e.getMessage());
                }
            }
        }
        return processItems;
    }

    /**
     * 扫描获取管理类交易的配置信息
     *
     * @param context 上下文
     * @return 配置信息
     */
    public Map<String, TradeItem> getManagerTradeItemFromAnnotion(Context context) {
        Map<String, TradeItem> tradeItems = null;
        List<String> packageList = getScanPackages(context, GroupConstant.MANAGER_TRADE_ITEM_TAG);
        if (packageList == null || packageList.size() == 0) return null;
        for (String packageName : packageList) {
            List<String> classNameList = ClassUtil.scanPackage(packageName);
            if (classNameList == null || classNameList.size() == 0)
                continue;
            tradeItems = new HashMap<>();
            TradeItem item;
            String tag, checker;
            Map<String, String> checkerMap = getCheckMapFromAnnotion(classNameList);
            for (String className : classNameList) {
                try {
                    Class clz = Class.forName(className);
                    if (clz.isAnnotationPresent(ManagerTrade.class)) {
                        ManagerTrade managerTradeAnno = (ManagerTrade) clz.getAnnotation(ManagerTrade.class);
                        if (managerTradeAnno != null) {
                            tag = TextUtils.isEmpty(managerTradeAnno.value()) ? clz.getSimpleName() : managerTradeAnno
                                    .value();
                            checker = managerTradeAnno.checkerClzName();
                            if (checkerMap != null && checkerMap.size() > 0 && !TextUtils.isEmpty(checker))
                                checker = checkerMap.get(checker);
                            item = new TradeItem(tag, checker, clz.getCanonicalName());
                            tradeItems.put(tag, item);
                        }
                    }
                } catch (ClassNotFoundException e) {
                    logger.info(e.getMessage());
                }
            }
        }
        return tradeItems;
    }

    private Map<String, String> getCheckMapFromAnnotion(List<String> classNameList) {
        Map<String, String> checkerMap = new HashMap<>();
        for (String className : classNameList) {
            try {
                Class clz = Class.forName(className);
                if (clz.isAnnotationPresent(MgrTradeChecker.class)) {
                    MgrTradeChecker mgrTradeCheckerAnno = (MgrTradeChecker) clz.getAnnotation(MgrTradeChecker.class);
                    if (mgrTradeCheckerAnno != null) {
                        checkerMap.put(TextUtils.isEmpty(mgrTradeCheckerAnno.value()) ? clz.getSimpleName() :
                                mgrTradeCheckerAnno.value(), clz.getCanonicalName());
                    }
                }
            } catch (ClassNotFoundException e) {
                logger.info(e.getMessage());
            }
        }
        return checkerMap;
    }

    /**
     * 从注解获取二次开发配置的类
     *
     * @return 返回二次开发配置
     */
    public Map<String, RedevelopItem> getRedevelopProcessFromAnnotion() {
        Map<String, RedevelopItem> redevelopProcessClzMap = null;
        List<String> packageList = getScanPackages(EposApplication.getAppContext(), GroupConstant.REDEVELOP_TAG);
        if (packageList == null || packageList.size() == 0) return null;
        for (String packageName : packageList) {
            List<String> classNameList = ClassUtil.scanPackage(packageName);
            if (classNameList == null || classNameList.size() == 0)
                continue;
            redevelopProcessClzMap = new HashMap<>();
            RedevelopItem item;
            for (String className : classNameList) {
                try {
                    Class clz = Class.forName(className);
                    if (clz.isAnnotationPresent(RedevelopAction.class)) {
                        RedevelopAction redevelopAction = (RedevelopAction) clz.getAnnotation(RedevelopAction.class);
                        if (redevelopAction != null) {
                            item = new RedevelopItem();
                            item.setIndex(redevelopAction.index());
                            item.setKey(redevelopAction.value());
                            item.setClssName(className);
                            redevelopProcessClzMap.put(redevelopAction.value(), item);
                        }
                    }
                } catch (ClassNotFoundException e) {
                    logger.info(e.getMessage());
                }
            }
        }
        return redevelopProcessClzMap;
    }

    /**
     * 获取默认配置文件配置的包扫描路径，应用项目未配置扫描包路径时，扫描默认路径
     *
     * @param context 上下文环境
     * @return 基础版本和应用项目的2个配置包名
     */
    @Nullable
    private List<String> getScanPackages(Context context, String annotationTag) {
        List<String> packageList = new ArrayList<>(2);
        Map<String, String> annoConfMap = XmlParser.parseAnnotationMap(context, baseConfig.getProject()
                + ANNOTATION_CONFIG_FILE);
        if (annoConfMap != null && annoConfMap.size() > 0) {
            String packageStr = annoConfMap.get(annotationTag);
            if (!TextUtils.isEmpty(packageStr))
                packageList.add(packageStr);
        }
        if (projectConfig.getProject().equals(baseConfig.getProject()))
            return packageList;
        annoConfMap = XmlParser.parseAnnotationMap(context, projectConfig.getProject()
                + ANNOTATION_CONFIG_FILE);
        if (annoConfMap != null && annoConfMap.size() > 0) {
            String packageStr = annoConfMap.get(annotationTag);
            if (!TextUtils.isEmpty(packageStr))
                packageList.add(packageStr);
        }
        if (packageList.size() == 1) {
            //添加项目默认扫描路径
            String basePackage = packageList.get(0);
            packageList.add(basePackage.replace(Config.BASE_PACKAGE_NAME, Config.BASE_PACKAGE_NAME + "." + project
                    .toLowerCase()));
        }
        return packageList;
    }

    public Map<Integer, String> getFieldProcessClz() {
        if (fieldProcessClz == null) {
            List<Iso8583FieldProcessItem> baseItems = getBaseIso8583FieldProcessItems(mAppContext);
            if (baseItems == null || baseItems.size() == 0) {
                logger.error("^_^ 获取域处理类失败！ ^_^");
                return null;
            }
            Map<Integer, String> mapDatas = simpleList2Map(baseItems);
            baseItems = getPrjIso8583FieldProcessItems(mAppContext);
            if (baseItems != null && baseItems.size() > 0) {
                mapDatas.putAll(simpleList2Map(baseItems));
            }
            baseItems = getIso8583FieldProcessFromAnnotion(mAppContext);
            if (baseItems != null && baseItems.size() > 0) {
                mapDatas.putAll(simpleList2Map(baseItems));
            }
            fieldProcessClz = mapDatas;
        }
        return fieldProcessClz;
    }

    private Map<Integer, String> simpleList2Map(List<Iso8583FieldProcessItem> baseItems) {
        Iterator<Iso8583FieldProcessItem> itemIterator = baseItems.iterator();
        Iso8583FieldProcessItem processItem;
        Map<Integer, String> stringSparseArray = new HashMap<>(64);
        while (itemIterator.hasNext()) {
            processItem = itemIterator.next();
            stringSparseArray.put(processItem.getIndex(), processItem.getProcessClz());
        }
        return stringSparseArray;
    }

    public String getIsoMsgConfigFile() {
        String fileName = getAbsFileName(projectConfig, Keys.obj().iso_msg_config);
        if (fileName == null) {
            fileName = getAbsFileName(baseConfig, Keys.obj().iso_msg_config);
        }
        return fileName;
    }

    public static Map<String, String> getTradeComponentMapFromAnnotation(String tradeTag) {
        return getInstance(EposApplication.getAppContext()).getTradeComponentMap(tradeTag);
    }

    public Map<String, String> getTradeComponentMap(String tradeTag) {
        if (TextUtils.isEmpty(tradeTag))
            return null;
        switch (tradeTag) {
            case GroupConstant.TRADE_VIEW_TAG:
                if (mTradeViewMap != null)
                    return mTradeViewMap;
                break;
            case GroupConstant.TRADE_PRESENT_TAG:
                if (mTradePresentMap != null)
                    return mTradePresentMap;
                break;
            case GroupConstant.TRADE_CONTROLLE_TAG:
                if (mTradeControllMap != null)
                    return mTradeControllMap;
                break;
            case GroupConstant.TRADE_MODEL_TAG:
                if (mTradeModel != null)
                    return mTradeModel;
                break;
            default:
                return null;
        }
        Map<String, String> clzMap = new HashMap<>();
        List<String> packageList = getScanPackages(EposApplication.getAppContext(), tradeTag);
        if (packageList != null && packageList.size() > 0) {
            for (String packageName : packageList) {
                List<String> classNameList = ClassUtil.scanPackage(packageName);
                if (classNameList == null || classNameList.size() == 0)
                    continue;
                for (String className : classNameList) {
                    try {
                        Class clz = Class.forName(className);
                        switch (tradeTag) {
                            case GroupConstant.TRADE_VIEW_TAG:
                                if (clz.isAnnotationPresent(TradeView.class)) {
                                    TradeView tradeAnnotation = (TradeView) clz.getAnnotation(TradeView.class);
                                    if (tradeAnnotation != null) {
                                        clzMap.put(TextUtils.isEmpty(tradeAnnotation.value()) ? clz.getSimpleName() :
                                                tradeAnnotation.value(), className);
                                    }
                                }
                                break;
                            case GroupConstant.TRADE_PRESENT_TAG:
                                if (clz.isAnnotationPresent(TradePresent.class)) {
                                    TradePresent tradeAnnotation = (TradePresent) clz.getAnnotation(TradePresent.class);
                                    if (tradeAnnotation != null) {
                                        clzMap.put(TextUtils.isEmpty(tradeAnnotation.value()) ? clz.getSimpleName() :
                                                tradeAnnotation.value(), className);
                                    }
                                }
                                break;
                            case GroupConstant.TRADE_CONTROLLE_TAG:
                                if (clz.isAnnotationPresent(TradeControlle.class)) {
                                    TradeControlle tradeAnnotation = (TradeControlle) clz.getAnnotation
                                            (TradeControlle.class);
                                    if (tradeAnnotation != null) {
                                        clzMap.put(TextUtils.isEmpty(tradeAnnotation.value()) ? clz.getSimpleName() :
                                                tradeAnnotation.value(), className);
                                    }
                                }
                                break;
                            case GroupConstant.TRADE_MODEL_TAG:

                                break;
                        }
                    } catch (ClassNotFoundException e) {
                        logger.info(e.getMessage());
                    }
                }
            }
        }
        switch (tradeTag) {
            case GroupConstant.TRADE_VIEW_TAG:
                mTradeViewMap = clzMap;
                break;
            case GroupConstant.TRADE_PRESENT_TAG:
                mTradePresentMap = clzMap;
                break;
            case GroupConstant.TRADE_CONTROLLE_TAG:
                mTradeControllMap = clzMap;
                break;
            case GroupConstant.TRADE_MODEL_TAG:
                mTradeModel = clzMap;
                break;
            default:
                return null;
        }
        return clzMap;
    }
}
