package test;

import android.content.Context;
import android.util.Log;

import com.centerm.epos.channels.EnumChannel;
//import com.centerm.epos.xml.XmlTag;
import com.centerm.epos.xml.handler.MenuHandler;
import com.centerm.epos.xml.bean.process.TradeProcess;
import com.centerm.epos.xml.handler.ProcessHandler;
import com.google.gson.JsonObject;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * author:wanliang527</br>
 * date:2016/10/21</br>
 */

public class XmlTest {
    private static Logger logger = Logger.getLogger(XmlTest.class);

    public static void test(Context context) {
        testMenuHandler(context);
    }

    public static void testMenuHandler(Context context) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            InputSource is2 = new InputSource(context.getAssets().open("menu/QIANBAO"));
            MenuHandler handler = new MenuHandler();
            reader.setContentHandler(handler);
            reader.parse(is2);
//            Log.w("lwl", handler.getMenu().toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

/*

    public static void testMappingHandler(Context context) {

        JsonObject json = null;

        Map<String, String> map = new HashMap<>();
        map.put(XmlTag.MessageTag.transCode, "T00001");
        map.put(XmlTag.MessageTag.transTime, "201607071234");
        map.put(XmlTag.MessageTag.pinPadId, "CPAY13434343434343");
        map.put(XmlTag.MessageTag.serial, "123456");
        map.put(XmlTag.MessageTag.termId, "D1V0169999999999");
        map.put(XmlTag.MessageTag.random, "324234");
        try {
            SAXParserFactory factory.xml = SAXParserFactory.newInstance();
            SAXParser parser = factory.xml.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            InputSource is2 = new InputSource(context.getAssets().open("msg/define/REQ_SIGN_IN"));
            reader.setContentHandler(new LocalInterfaceHandler(map));
            reader.parse(is2);
            json = ((LocalInterfaceHandler) reader.getContentHandler()).getJson();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        logger.warn("【初始报文：】+" + json);

        try {
            SAXParserFactory factory.xml = SAXParserFactory.newInstance();
            SAXParser parser = factory.xml.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            MappingHandler handler = new MappingHandler(EnumChannel.ZJRC, MappingHandler.MessageType.REQUEST, json);
            reader.setContentHandler(handler);
            InputSource is = new InputSource(context.getAssets().open("msg/mapping/ZJRC/SIGN_IN"));
            reader.parse(is);
            logger.warn(handler.getOutput());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }


    public static void testLocalInterfaceHandler(Context context) {

        Map<String, String> map = new HashMap<>();
        map.put(XmlTag.MessageTag.transCode, "T00001");
        map.put(XmlTag.MessageTag.transTime, "201607071234");
        map.put(XmlTag.MessageTag.pinPadId, "CPAY13434343434343");
        map.put(XmlTag.MessageTag.serial, "123456");
        map.put(XmlTag.MessageTag.termId, "D1V0169999999999");
        map.put(XmlTag.MessageTag.random, "324234");
        try {
            SAXParserFactory factory.xml = SAXParserFactory.newInstance();
            SAXParser parser = factory.xml.newSAXParser();
            XMLReader reader = parser.getXMLReader();
*/
/*            LocalInterfaceHandler handler = new LocalInterfaceHandler(map);
            reader.setContentHandler(handler);
            InputSource is = new InputSource(context.getAssets().open("msg/define/COMM_REQ_HEADER"));
            reader.parse(is);
            logger.warn(handler.getJson());*//*

            InputSource is2 = new InputSource(context.getAssets().open("msg/define/REQ_SIGN_IN"));
            reader.setContentHandler(new LocalInterfaceHandler(map));
            reader.parse(is2);
            logger.warn(((LocalInterfaceHandler) reader.getContentHandler()).getJson());

            */
/*InputSource is3 = new InputSource(context.getAssets().open("msg/define/COMM_MAC"));
            reader.setContentHandler(new LocalInterfaceHandler());
            reader.parse(is3);
            logger.warn(((LocalInterfaceHandler) reader.getContentHandler()).getJson());*//*

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static void testTranasactionHandler(Context context) {
        try {
            SAXParserFactory factory.xml = SAXParserFactory.newInstance();
            SAXParser parser = factory.xml.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            ProcessHandler handler = new ProcessHandler();
            reader.setContentHandler(handler);
            InputSource is;
            is = new InputSource(context.getAssets().open("process/SIGN_IN"));
            reader.parse(is);
            TradeProcess transaction = handler.getTransaction();
            Log.w("test", transaction.getComponentNodeList().toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }
*/


}
