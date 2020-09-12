package com.centerm.epos.net;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.centerm.epos.common.StatusCode;
import com.centerm.epos.common.Settings;
import com.centerm.smartpos.util.HexUtil;

import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * author:wanliang527</br>
 * date:2016/10/27</br>
 */

public class SocketClient {
    private final static String KEY_STATUS_CODE = "KEY_STATUS_CODE";
    private final static String KEY_ERROR_INFO = "KEY_ERROR_INFO";
    private final static String KEY_RETURN_DATA = "KEY_RETURN_DATA";
    private final static String KEY_THROWABLE = "KEY_THROWABLE";

    private final static int MSG_SUCCESS = 0x100;
    private final static int MSG_FAILED = 0x101;
//    private final static int MSG_CONNECT_SUCCESS = 0x102;
//    private final static int MSG_CONNECT_SUCCESS = 0x103;

    private Logger logger = Logger.getLogger(SocketClient.class);
    private Context context;
    private Map<String, SocketThread> socketPool;
    private static SocketClient instance;

    private SocketClient() {
    }

    public static SocketClient getInstance(Context context) {
        if (instance == null) {
            synchronized (SocketClient.class) {
                if (instance == null) {
                    instance = new SocketClient();
                }
            }
        }
        instance.context = context.getApplicationContext();
        return instance;
    }

    public void sendData(byte[] data, ResponseHandler handler) {
        new SocketThread(new InnerHandler(context.getMainLooper(), handler), data).start();
    }

    public void syncSendData(byte[] data, ResponseHandler handler) {
        Socket socket;
        logger.info("[开始发送数据]==>IP==>" + Settings.getCommonIp(context) + "==>PORT==>" + Settings.getCommonPort(context));
        InetSocketAddress address = new InetSocketAddress(Settings.getCommonIp(context), Settings.getCommonPort(context));
        socket = new Socket();
        try {
            int respTimeout = Settings.getRespTimeout(context);
            int connectTimeout = Settings.getConnectTimeout(context);
            logger.debug("[正在连接中...]==>连接超时==>" + connectTimeout + "==>响应超时==>" + respTimeout);
            socket.connect(address, connectTimeout);
            socket.setSoTimeout(respTimeout);
            logger.debug("[连接成功]");
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            os.write(data);
            os.flush();
            logger.debug("[正在发送数据...]==>" + HexUtil.bytesToHexString(data));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
                if (isReceiveComplete(baos.toByteArray())) {
                    logger.debug("流数据已接收完成");
                    break;
                } else {
                    logger.warn("流数据未接收完整，继续接收...");
                }
            }
            byte[] returnData = baos.toByteArray();
            baos.close();
            if (data == null || data.length == 0) {
                logger.warn("[接收数据为空]");
                throw new SocketTimeoutException();
            }
            logger.debug("[接收到数据]==>" + HexUtil.bytesToHexString(returnData));
            cancel(socket);
            handler.onSuccess(StatusCode.SUCCESS.getStatusCode(), getString(StatusCode.SUCCESS), returnData);
        } catch (UnknownHostException e) {
            logger.warn(e.toString());
            handler.onFailure(StatusCode.UNKNOWN_HOST.getStatusCode(), getString(StatusCode.UNKNOWN_HOST), e);
            cancel(socket);
        } catch (ConnectException e) {
            logger.warn(e.toString());
            handler.onFailure(StatusCode.CONNECTION_EXCEPTION.getStatusCode(), getString(StatusCode.CONNECTION_EXCEPTION), e);
            cancel(socket);
        } catch (SocketTimeoutException e) {
            logger.warn(e.toString());
            handler.onFailure(StatusCode.SOCKET_TIMEOUT.getStatusCode(), getString(StatusCode.SOCKET_TIMEOUT), e);
            cancel(socket);
        } catch (SocketException e) {
            logger.warn(e.toString());
            handler.onFailure(StatusCode.SOCKET_TIMEOUT.getStatusCode(), getString(StatusCode.SOCKET_TIMEOUT), e);
            cancel(socket);
        } catch (Exception e) {
            e.printStackTrace();
            handler.onFailure(StatusCode.UNKNOWN_REASON.getStatusCode(), getString(StatusCode.UNKNOWN_REASON), e);
            cancel(socket);
        }
    }

    private boolean isReceiveComplete(byte[] receviedData) {
        if (receviedData == null)
            return false;
        long receivedLen = receviedData.length;
        if (receivedLen < 3)
            return false;
        long len = (receviedData[0] << 8) | (receviedData[1] & 0x00ff);
        logger.debug("报文总长度：" + len);
        if (receivedLen >= len + 2)
            return true;
        return false;
    }

    public void sendSequenceData(String firstTag, byte[] firstData, SequenceHandler handler) {
        if (handler == null) {
            logger.warn("回调接收器为空，不发送数据");
            return;
        }
//        handler.bindClient(this, false);
        handler.sendNext(firstTag, firstData);
    }

    public void syncSendSequenceData(String firstTag, byte[] firstData, SequenceHandler handler) {
        if (handler == null) {
            logger.warn("回调接收器为空，不发送数据");
            return;
        }
//        handler.bindClient(this, true);
        handler.sendNext(firstTag, firstData);
    }

    private String getString(StatusCode error) {
        return context.getResources().getString(error.getMsgId());
    }

    private void cancel(Socket socket) {
        if (socket != null) {
            try {
                logger.debug("[关闭Socket]");
                socket.close();
                socket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class SocketThread extends Thread {
        private Handler handler;
        private byte[] data;
        private Socket socket;

        public SocketThread(Handler handler, byte[] data) {
            this.handler = handler;
            this.data = data;
        }

        @Override
        public void run() {
            super.run();
            logger.info("[开始发送数据]==>IP==>" + Settings.getCommonIp(context) + "==>PORT==>" + Settings.getCommonPort(context));
            InetSocketAddress address = new InetSocketAddress(Settings.getCommonIp(context), Settings.getCommonPort(context));
            socket = new Socket();
            try {
                int respTimeout = Settings.getRespTimeout(context);
                int connectTimeout = Settings.getConnectTimeout(context);
                logger.debug("[正在连接中...]==>连接超时==>" + connectTimeout + "==>响应超时==>" + respTimeout);
                socket.connect(address, connectTimeout);
                socket.setSoTimeout(respTimeout);
                logger.debug("[连接成功]");
                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream();
                os.write(data);
                os.flush();
                logger.debug("[正在发送数据...]==>" + HexUtil.bytesToHexString(data));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) > 0) {
                    baos.write(buffer, 0, len);
                    if (isReceiveComplete(baos.toByteArray())) {
                        logger.debug("流数据已接收完成");
                        break;
                    } else {
                        logger.warn("流数据未接收完整，继续接收...");
                    }
                }
                byte[] data = baos.toByteArray();
                baos.close();
                if (data == null || data.length == 0) {
                    logger.warn("[接收数据为空]");
                    throw new SocketTimeoutException();
                }
                logger.debug("[接收到数据]==>" + HexUtil.bytesToHexString(data));
                sendMessage(handler, MSG_SUCCESS, StatusCode.SUCCESS.getStatusCode(), getString(StatusCode.SUCCESS), data, null);
                cancel(socket);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                sendMessage(handler, MSG_FAILED, StatusCode.UNKNOWN_HOST.getStatusCode(), getString(StatusCode.UNKNOWN_HOST), null, e);
                cancel(socket);
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                sendMessage(handler, MSG_FAILED, StatusCode.SOCKET_TIMEOUT.getStatusCode(), getString(StatusCode.SOCKET_TIMEOUT), null, e);
                cancel(socket);
            } catch (ConnectException e) {
                e.printStackTrace();
                sendMessage(handler, MSG_FAILED, StatusCode.CONNECTION_EXCEPTION.getStatusCode(), getString(StatusCode.CONNECTION_EXCEPTION), null, e);
                cancel(socket);
            } catch (Exception e) {
                e.printStackTrace();
                sendMessage(handler, MSG_FAILED, StatusCode.UNKNOWN_REASON.getStatusCode(), getString(StatusCode.UNKNOWN_REASON), null, e);
                cancel(socket);
            }
        }

        private void sendMessage(Handler handler, int what, String statusCode, String errorInfo, byte[] returnData, Throwable error) {
            Message msg = new Message();
            msg.what = what;
            Bundle bundle = new Bundle();
            bundle.putString(KEY_STATUS_CODE, statusCode);
            bundle.putString(KEY_ERROR_INFO, errorInfo);
            bundle.putByteArray(KEY_RETURN_DATA, returnData);
            bundle.putSerializable(KEY_THROWABLE, error);
            msg.setData(bundle);
            handler.sendMessage(msg);
        }

    }

    private class InnerHandler extends Handler {
        private ResponseHandler outterHandler;

        public InnerHandler(Looper looper, ResponseHandler handler) {
            super(looper);
            this.outterHandler = handler;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String statusCode = data.getString(KEY_STATUS_CODE);
            String info = data.getString(KEY_ERROR_INFO);
            byte[] returnData = data.getByteArray(KEY_RETURN_DATA);
            Throwable throwable = (Throwable) data.getSerializable(KEY_THROWABLE);
            switch (msg.what) {
                case MSG_SUCCESS:
                    if (outterHandler != null) {
                        outterHandler.onSuccess(statusCode, info, returnData);
                    }
                    break;
                case MSG_FAILED:
                    if (outterHandler != null) {
                        outterHandler.onFailure(statusCode, info, throwable);
                    }
                    break;
            }
        }
    }
}
