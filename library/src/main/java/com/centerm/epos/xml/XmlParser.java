package com.centerm.epos.xml;

import android.content.Context;

import com.centerm.epos.annotation.GroupConstant;
import com.centerm.epos.bean.TranscationFactor;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.xml.bean.ConfigCatalog;
import com.centerm.epos.xml.bean.DefaultParams;
import com.centerm.epos.xml.bean.PreferDataPool;
import com.centerm.epos.xml.bean.RedevelopItem;
import com.centerm.epos.xml.bean.TradeItem;
import com.centerm.epos.xml.bean.menu.Menu;
import com.centerm.epos.xml.bean.message.Iso8583FieldProcessItem;
import com.centerm.epos.xml.bean.process.TradeProcess;
import com.centerm.epos.xml.bean.project.ProjectConfig;
import com.centerm.epos.xml.bean.slip.SlipElement;
import com.centerm.epos.xml.handler.AnnotationConfigHandler;
import com.centerm.epos.xml.handler.ConfigCatalogHandler;
import com.centerm.epos.xml.handler.IsoFieldProcessHandler;
import com.centerm.epos.xml.handler.MenuHandler;
import com.centerm.epos.xml.handler.ParamsHandler;
import com.centerm.epos.xml.handler.PreferHandler;
import com.centerm.epos.xml.handler.ProcessHandler;
import com.centerm.epos.xml.handler.ProjectConfigHandler;
import com.centerm.epos.xml.handler.PropertiesHandler;
import com.centerm.epos.xml.handler.RedevelopHandler;
import com.centerm.epos.xml.handler.SlipHandler;
import com.centerm.epos.xml.handler.TradeFactorHandler;
import com.centerm.epos.xml.handler.TradeItemHandler;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * XML解析器
 * author:wanliang527</br>
 * date:2016/10/26</br>
 */
public class XmlParser {

    private static Logger logger = Logger.getLogger(XmlParser.class);

//    private final static String MENU_PATH = "menu/";
//    private final static String PROCESS_PATH = "process/";
//    private final static String DEFINE_PATH = "define/";


    public static Menu parseMenu(Context context, String fileName) {
        long start = System.currentTimeMillis();
        logger.debug("开始菜单配置解析：" + fileName);
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            String fileDir = fileName;
            InputSource is2 = new InputSource(context.getAssets().open(fileDir));
            MenuHandler handler = new MenuHandler();
            reader.setContentHandler(handler);
            reader.parse(is2);
            long end = System.currentTimeMillis();
            logger.debug("解析文件：" + fileName + "==>耗时：" + (end - start));
            return handler.getMenu();
        } catch (Exception e) {
//            e.printStackTrace();
            logger.warn("解析文件失败：" + fileName + "==>" + e.toString());
        }
        return null;
    }

    public static ProjectConfig parseProjectConfig(Context context, String fileName) {
        long start = System.currentTimeMillis();
        logger.debug("开始项目配置信息解析：" + fileName);
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            String fileDir = fileName;
            InputSource is2 = new InputSource(context.getAssets().open(fileDir));
            ProjectConfigHandler handler = new ProjectConfigHandler();
            reader.setContentHandler(handler);
            reader.parse(is2);
            long end = System.currentTimeMillis();
            logger.debug("解析文件：" + fileName + "==>耗时：" + (end - start));
            return handler.getProjectConfig();
        } catch (Exception e) {
//            e.printStackTrace();
            logger.warn("解析文件失败：" + fileName + "==>" + e.toString());
        }
        return null;
    }

    public static TradeProcess parseProcess(Context context, String fileName) {
        long start = System.currentTimeMillis();
        logger.debug("开始流程文件解析：" + fileName);
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            InputSource is2 = new InputSource(context.getAssets().open(fileName));
            ProcessHandler handler = new ProcessHandler();
            handler.setAnnotationConfigMap(
                    ConfigureManager.getTradeComponentMapFromAnnotation(GroupConstant.TRADE_VIEW_TAG),
                    ConfigureManager.getTradeComponentMapFromAnnotation(GroupConstant.TRADE_PRESENT_TAG),
                    ConfigureManager.getTradeComponentMapFromAnnotation(GroupConstant.TRADE_CONTROLLE_TAG),
                    ConfigureManager.getTradeComponentMapFromAnnotation(GroupConstant.TRADE_MODEL_TAG));
            reader.setContentHandler(handler);
            reader.parse(is2);
            long end = System.currentTimeMillis();
            logger.debug("解析文件：" + fileName + "==>耗时：" + (end - start));
            return handler.getTransaction();
        } catch (Exception e) {
            logger.warn("解析文件失败：" + fileName + "==>" + e.toString());
        }
        return null;
    }

    public static ConfigCatalog parseConfigCatalog(Context context, String fileName) {
        long start = System.currentTimeMillis();
        logger.debug("开始配置目录解析：" + fileName);
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            InputSource is2 = new InputSource(context.getAssets().open(fileName));
            ConfigCatalogHandler handler = new ConfigCatalogHandler();
            reader.setContentHandler(handler);
            reader.parse(is2);
            long end = System.currentTimeMillis();
            logger.debug("解析文件：" + fileName + "==>耗时：" + (end - start));
            return handler.getBean();
        } catch (Exception e) {
            logger.warn("解析文件失败：" + fileName + "==>" + e.toString());
        }
        return null;
    }

    public static PreferDataPool parsePreferData(Context context, String fileName) {
        long start = System.currentTimeMillis();
        logger.debug("开始自定义SharedPreference解析：" + fileName);
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            InputSource is2 = new InputSource(context.getAssets().open(fileName));
            PreferHandler handler = new PreferHandler();
            reader.setContentHandler(handler);
            reader.parse(is2);
            long end = System.currentTimeMillis();
            logger.debug("解析文件：" + fileName + "==>耗时：" + (end - start));
            return handler.getPreferDataBean();
        } catch (Exception e) {
            logger.warn("解析文件失败：" + fileName + "==>" + e.toString());
        }
        return null;
    }

    public static Map<String, DefaultParams> parseParamsMap(Context context, String fileName) {
        long start = System.currentTimeMillis();
        logger.debug("开始参数集合解析：" + fileName);
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            InputSource is2 = new InputSource(context.getAssets().open(fileName));
            ParamsHandler handler = new ParamsHandler();
            reader.setContentHandler(handler);
            reader.parse(is2);
            long end = System.currentTimeMillis();
            logger.debug("解析文件：" + fileName + "==>耗时：" + (end - start));
            return handler.getParamsMap();
        } catch (Exception e) {
            logger.warn("解析文件失败：" + fileName + "==>" + e.toString());
        }
        return null;
    }

    public static Map<String, RedevelopItem> parseRedevelopMap(Context context, String fileName) {
        long start = System.currentTimeMillis();
        logger.debug("开始二次开发点解析：" + fileName);
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            InputSource is2 = new InputSource(context.getAssets().open(fileName));
            RedevelopHandler handler = new RedevelopHandler();
            reader.setContentHandler(handler);
            reader.parse(is2);
            long end = System.currentTimeMillis();
            logger.debug("解析文件：" + fileName + "==>耗时：" + (end - start));
            return handler.getRedevelopMap();
        } catch (Exception e) {
            logger.warn("解析文件失败：" + fileName + "==>" + e.toString());
        }
        return null;
    }

    public static Map<String, String> parseAnnotationMap(Context context, String fileName) {
        long start = System.currentTimeMillis();
        logger.debug("开始二次开发点解析：" + fileName);
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            InputSource is2 = new InputSource(context.getAssets().open(fileName));
            AnnotationConfigHandler handler = new AnnotationConfigHandler();
            reader.setContentHandler(handler);
            reader.parse(is2);
            long end = System.currentTimeMillis();
            logger.debug("解析文件：" + fileName + "==>耗时：" + (end - start));
            return handler.getConfigMap();
        } catch (Exception e) {
            logger.warn("解析文件失败：" + fileName + "==>" + e.toString());
        }
        return null;
    }

    public static List<SlipElement> parseSlipElements(Context context, String fileName) {
        long start = System.currentTimeMillis();
        logger.debug("开始签购单模板解析：" + fileName);
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            InputSource is2 = new InputSource(context.getAssets().open(fileName));
            SlipHandler handler = new SlipHandler();
            reader.setContentHandler(handler);
            reader.parse(is2);
            long end = System.currentTimeMillis();
            logger.debug("解析文件：" + fileName + "==>耗时：" + (end - start));
            return handler.getElements();
        } catch (Exception e) {
//            e.printStackTrace();
            logger.warn("解析文件失败：" + fileName + "==>" + e.toString());
        }
        return null;
    }

    public static List<Iso8583FieldProcessItem> parseIsoFieldProcess(Context context, String fileName) {
        long start = System.currentTimeMillis();
        logger.debug("开始POS数据域处理类解析：" + fileName);
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            InputSource is2 = new InputSource(context.getAssets().open(fileName));
            IsoFieldProcessHandler handler = new IsoFieldProcessHandler();
            reader.setContentHandler(handler);
            reader.parse(is2);
            long end = System.currentTimeMillis();
            logger.debug("解析文件：" + fileName + "==>耗时：" + (end - start));
            return handler.getItems();
        } catch (Exception e) {
//            e.printStackTrace();
            logger.warn("解析文件失败：" + fileName + "==>" + e.toString());
        }
        return null;
    }

    public static Map<String, String> parseXmlPropertiesMap(Context context, String fileName) {
        long start = System.currentTimeMillis();
        logger.debug("开始XML属性解析：" + fileName);
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            InputSource is2 = new InputSource(context.getAssets().open(fileName));
            PropertiesHandler handler = new PropertiesHandler();
            reader.setContentHandler(handler);
            reader.parse(is2);
            long end = System.currentTimeMillis();
            logger.debug("解析文件：" + fileName + "==>耗时：" + (end - start));
            return handler.getParamsMap();
        } catch (Exception e) {
            logger.warn("解析文件失败：" + fileName + "==>" + e.toString());
        }
        return null;
    }

    public static Map<String, TradeItem> parseTradeItem(Context context, String fileName) {
        long start = System.currentTimeMillis();
        logger.debug("开始管理业务解析：" + fileName);
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            InputSource is2 = new InputSource(context.getAssets().open(fileName));
            TradeItemHandler handler = new TradeItemHandler();
            reader.setContentHandler(handler);
            reader.parse(is2);
            long end = System.currentTimeMillis();
            logger.debug("解析文件：" + fileName + "==>耗时：" + (end - start));
            return handler.getTradeItemMap();
        } catch (Exception e) {
//            e.printStackTrace();
            logger.warn("解析文件失败：" + fileName + "==>" + e.toString());
        }
        return null;
    }

    public static Map<String, TranscationFactor> parseTradeFactor(Context context, String fileName) {
        long start = System.currentTimeMillis();
        logger.debug("开始管理业务解析：" + fileName);
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            InputSource is2 = new InputSource(context.getAssets().open(fileName));
            TradeFactorHandler handler = new TradeFactorHandler();
            reader.setContentHandler(handler);
            reader.parse(is2);
            long end = System.currentTimeMillis();
            logger.debug("解析文件：" + fileName + "==>耗时：" + (end - start));
            return handler.getTranscationFactorTable();
        } catch (Exception e) {
//            e.printStackTrace();
            logger.warn("解析文件失败：" + fileName + "==>" + e.toString());
        }
        return null;
    }
}
