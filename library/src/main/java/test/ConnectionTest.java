package test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.define.IPrinterDev;
import com.centerm.cpay.midsdk.dev.define.printer.EnumPrinterStatus;
import com.centerm.cpay.midsdk.dev.define.printer.PrintListener;
import com.centerm.cpay.midsdk.dev.define.printer.PrinterDataItem;
import com.centerm.cpay.midsdk.dev.define.printer.task.BitmapTask;
import com.centerm.cpay.midsdk.dev.define.printer.task.PrintTask;
import com.centerm.cpay.midsdk.dev.define.printer.task.StringTask;
import com.centerm.epos.R;
import com.centerm.epos.common.Settings;
import com.centerm.epos.net.ResponseHandler;
import com.centerm.epos.net.SocketClient;
import com.centerm.iso8583.IsoMessage;
import com.centerm.iso8583.bean.FormatInfo;
import com.centerm.iso8583.bean.FormatInfoFactory;
import com.centerm.iso8583.enums.IsoMessageMode;
import com.centerm.iso8583.parse.IsoConfigParser;
import com.centerm.smartpos.util.HexUtil;

import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * author:wanliang527</br>
 * date:2016/10/27</br>
 */

public class ConnectionTest {

    private static Logger logger = Logger.getLogger(ConnectionTest.class);
    private static DeviceFactory factory;
    private static IPrinterDev iPrinterDev;

    public static void test(Context context) {
//        qianBaoTest(context);
    }

    public static void qianBaoTest(Context context) {
//        new SocketThread(context,
//                HexUtils.hexStringToByte("003F600003000060310031FFF1080000200000000000140" +
//                        "000270011990000020030002953657175656E6365204E6F31363331333742354E4C3030303032323638")).start();
//        String str = "003B600003000060310031FFF108000020000000000014000062000299333053657175656E6365204E6F31333131313144315630313630303030303032";
        String str = "0039600003000061320032FFF108000000000000C00014323938303030313538343832393034343131313630303000110000000196000003000000".toUpperCase();
        new SocketThread(context, HexUtil.hexStringToByte(str)).start();

    }

    public static void qianBaoTest2(Context context) {
        SocketClient client = SocketClient.getInstance(context);
        ResponseHandler handler = new ResponseHandler() {
            @Override
            public void onSuccess(String statusCode, String info, byte[] data) {
                logger.warn("成功，statusCode = " + statusCode + "  ，接收数据：" + HexUtil.bytesToHexString(data));
            }

            @Override
            public void onFailure(String statusCode, String info, Throwable error) {
                logger.warn("失败，statusCode = " + statusCode + "  错误信息：" + info);
            }
        };
        client.sendData(HexUtil.hexStringToByte("003F600003000060310031FFF1080000200000000000140000270011990000020030002953657175656E6365204E6F31363331333742354E4C3030303032323638"), handler);
    }


    public static void qianBaoTest3(Context context) {
        Map<String, String> dataMap = new HashMap<>();
//        dataMap.put("headerdata", CommonUtils.combineMsgHeader());
//        dataMap.put("sys_tra_no", Settings.getTerminalSerial(context));
//        dataMap.put("udf_fld", "99");
//        dataMap.put("sw_sys_data", CommonUtils.combineTerminalSnByTLV());

        FormatInfoFactory factory = null;
        IsoConfigParser parser = new IsoConfigParser();
        try {
            factory = parser.parseFromInputStream(context.getAssets().open("msg/mapping/QIANBAO"));
            FormatInfo info = factory.getFormatInfo("002312", IsoMessageMode.PACK);
            IsoMessage message = com.centerm.iso8583.MessageFactory.getIso8583Message().packTrns(dataMap, info);
            byte[] pkg = message.getAllMessageByteData();
            logger.warn(HexUtil.bytesToHexString(pkg));
            SocketClient client = SocketClient.getInstance(context);
            client.sendData(pkg, new ResponseHandler() {
                @Override
                public void onSuccess(String statusCode, String msg, byte[] data) {
                    logger.warn("[返回报文]，" + HexUtil.bytesToHexString(data));
                }

                @Override
                public void onFailure(String code, String msg, Throwable error) {
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }


//        byte[] da = (byte[]) new MessageFactory(context).pack("009911", dataMap);
//        logger.warn("【组报文】" + HexUtils.bytesToHexString(da));
//        SocketClient client = SocketClient.getInstance(context);
//        client.sendData(da, new ResponseHandler() {
//            @Override
//            public void onSuccess(String statusCode, String msg, byte[] data) {
//                logger.warn("[返回报文]，" + HexUtils.bytesToHexString(data));
//            }
//
//            @Override
//            public void onFailure(String code, String msg, Throwable error) {
//
//            }
//        });

    }
    public static void printData(final Context context){
        IPrinterDev iPrinterDev = null;
        try {
            DeviceFactory   factory = DeviceFactory.getInstance();
            iPrinterDev = factory.getPrinterDev();
        } catch (Exception e) {
            e.printStackTrace();
        }

        PrinterDataItem dataItem = new PrinterDataItem("我是中国人");
            PrinterDataItem dataItem1 = new PrinterDataItem("我爱我的祖国");
            List<PrinterDataItem> list = new ArrayList<>();
            list.add(dataItem);
            list.add(dataItem1);
            list.add(new PrinterDataItem("商户名称"));
            list.add(new PrinterDataItem("商户名称1"));
            list.add(new PrinterDataItem("商户名称2"));
            list.add(new PrinterDataItem("商户名称3"));
            list.add(new PrinterDataItem("商户名称4"));
            list.add(new PrinterDataItem("商户名称5"));
            list.add(new PrinterDataItem("商户名称6"));
            list.add(new PrinterDataItem("商户名称7"));
            list.add(new PrinterDataItem("商户名称8"));
            PrintTask printTask = new PrintTask();
            printTask.addTask(new StringTask(list));
        EnumPrinterStatus enumPrinterStatus = iPrinterDev.getPrinterStatus();
        logger.debug("打印机状态："+enumPrinterStatus);
            iPrinterDev.print(printTask, new PrintListener() {
                @Override
                public void onFinish() {
                    IPrinterDev iPrinterDev = null;
                    try {
                        DeviceFactory   factory = DeviceFactory.getInstance();
                        iPrinterDev = factory.getPrinterDev();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    PrinterDataItem dataItem = new PrinterDataItem("我是中国人");
                    PrinterDataItem dataItem1 = new PrinterDataItem("我爱我的祖国");
                    List<PrinterDataItem> list = new ArrayList<>();
                    list.add(dataItem);
                    list.add(dataItem1);
                    list.add(new PrinterDataItem("商户名称"));
                    list.add(new PrinterDataItem("商户名称1"));
                    list.add(new PrinterDataItem("商户名称2"));
                    list.add(new PrinterDataItem("商户名称3"));
                    list.add(new PrinterDataItem("商户名称4"));
                    list.add(new PrinterDataItem("商户名称5"));
                    list.add(new PrinterDataItem("商户名称6"));
                    list.add(new PrinterDataItem("商户名称7"));
                    list.add(new PrinterDataItem("商户名称8"));
                    PrintTask printTask = new PrintTask();
                    printTask.addTask(new StringTask(list));
                   EnumPrinterStatus enumPrinterStatus = iPrinterDev.getPrinterStatus();
                    logger.debug("打印机状态："+enumPrinterStatus);
                    iPrinterDev.print(printTask, new PrintListener() {
                        @Override
                        public void onFinish() {
                            IPrinterDev iPrinterDev = null;
                            try {
                                DeviceFactory   factory = DeviceFactory.getInstance();
                                iPrinterDev = factory.getPrinterDev();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            PrinterDataItem dataItem = new PrinterDataItem("我是中国人");
                            PrinterDataItem dataItem1 = new PrinterDataItem("我爱我的祖国");
                            List<PrinterDataItem> list = new ArrayList<>();
                            list.add(dataItem);
                            list.add(dataItem1);
                            list.add(new PrinterDataItem("商户名称"));
                            list.add(new PrinterDataItem("商户名称1"));
                            list.add(new PrinterDataItem("商户名称2"));
                            list.add(new PrinterDataItem("商户名称3"));
                            list.add(new PrinterDataItem("商户名称4"));
                            list.add(new PrinterDataItem("商户名称5"));
                            list.add(new PrinterDataItem("商户名称6"));
                            list.add(new PrinterDataItem("商户名称7"));
                            list.add(new PrinterDataItem("商户名称8"));
                            PrintTask printTask = new PrintTask();
                            Bitmap bp = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
                            BitmapTask bitmapTask = new BitmapTask(bp,10,300,300);
                            printTask.addTask(new StringTask(list));
                            printTask.addTask(bitmapTask);
                            EnumPrinterStatus enumPrinterStatus = iPrinterDev.getPrinterStatus();
                            logger.debug("打印机状态："+enumPrinterStatus);
                            iPrinterDev.print(printTask, new PrintListener() {
                                @Override
                                public void onFinish() {

                                }

                                @Override
                                public void onError(int i, String s) {

                                }
                            });
                        }

                        @Override
                        public void onError(int i, String s) {

                        }
                    });
                }

                @Override
                public void onError(int i, String s) {

                }
            });

    }

    private static class SocketThread extends Thread {
        private Context context;
        private byte[] data;

        public SocketThread(Context context, byte[] data) {
            this.context = context;
            this.data = data;
        }

        @Override
        public void run() {
            super.run();
            logger.warn("IP== " + Settings.getCommonIp(context));
            logger.warn("端口== " + Settings.getCommonPort(context));
            InetSocketAddress address = new InetSocketAddress(Settings.getCommonIp(context), Settings.getCommonPort(context));
            Socket socket = new Socket();
            try {
                socket.connect(address, 120 * 1000);
                logger.info("[连接成功]");
                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream();
                os.write(data);
                os.flush();
                logger.info("[发送数据]");
                logger.warn(HexUtil.bytesToHexString(data));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) > 0) {
                    baos.write(buffer, 0, len);
                }
                logger.info("[接收数据]");
                logger.warn(HexUtil.bytesToHexString(baos.toByteArray()));

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
