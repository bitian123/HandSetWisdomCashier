package com.centerm.epos.task;

import android.content.Context;
import android.text.TextUtils;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.define.IPbocService;
import com.centerm.cpay.midsdk.dev.define.pboc.EnumAidCapkOperation;
import com.centerm.epos.bean.iso.Iso62Capk;
import com.centerm.epos.common.ISORespCode;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.net.SequenceHandler;
import com.centerm.epos.present.communication.DataExchanger;
import com.centerm.epos.present.communication.DataExchangerFactory;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.transcation.pos.manager.DownloadCAPKTrade;
import com.centerm.epos.utils.SM3;
import com.centerm.epos.utils.TlvUtil;
import com.centerm.epos.utils.XLogUtil;
import com.centerm.smartpos.util.HexUtil;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.epos.common.TransCode.DOWNLOAD_CAPK;
import static com.centerm.epos.common.TransCode.DOWNLOAD_PARAMS_FINISHED;
import static com.centerm.epos.common.TransCode.POS_STATUS_UPLOAD;
import static com.centerm.epos.common.TransDataKey.KEY_PARAMS_COUNTS;
import static com.centerm.epos.common.TransDataKey.KEY_PARAMS_TYPE;

/**
 * author:wanliang527</br>
 * date:2016/11/21</br>
 */

public abstract class AsyncDownloadCapkTask extends AsyncMultiRequestTask {
    private static final String TAG = AsyncDownloadCapkTask.class.getSimpleName();

    private LinkedList<Iso62Capk> infoList;
    private CommonDao<Iso62Capk> dao;
    private int index;
    private int totalCounts;

    public AsyncDownloadCapkTask(Context context, Map<String, Object> dataMap) {
        super(context, dataMap);
        dao = new CommonDao<>(Iso62Capk.class, DbHelper.getInstance());
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        infoList = new LinkedList<>();
        dao.deleteByWhere("id IS NOT NULL");
    }

    @Override
    protected String[] doInBackground(String... params) {
        final String[] result = new String[2];
        //先进行终端状态上送
        dataMap.put(KEY_PARAMS_TYPE, dataMap.get(DownloadCAPKTrade.CAPK_TYPE));//参数类型
        dataMap.put(KEY_PARAMS_COUNTS, "0");//已获取到的参数信息条数，用于继续发起请求
        Object msgPacket = factory.packMessage(TransCode.POS_STATUS_UPLOAD, dataMap);

        final SequenceHandler handler = new SequenceHandler() {

            private void startDownload() {
//                sleep(SHORT_SLEEP);
                int counts = infoList.size();
                if (counts > 0) {
                    Iso62Capk capk = infoList.removeFirst();
                    dataMap.put(TradeInformationTag.IC_PARAMETER_CAPK, capk.getRID() + capk.getIndex());
                    Object pkgMsg = factory.packMessage(DOWNLOAD_CAPK, dataMap);
                    logger.debug("总数==>" + totalCounts + "==>当前开始下载==>" + index + "==>" + dataMap.get
                            (TradeInformationTag.IC_PARAMETER_CAPK));
                    publishProgress(totalCounts, ++index);//进度通知
                    sendNext(DOWNLOAD_CAPK, (byte[]) pkgMsg);
                } else {
                    publishProgress(totalCounts, -1);//下载结束
                    Object pkgMsg = factory.packMessage(DOWNLOAD_PARAMS_FINISHED, dataMap);
                    logger.debug("公钥下载结束，准备发送下载结束报文");
                    sendNext(DOWNLOAD_PARAMS_FINISHED, (byte[]) pkgMsg);
//                    sleep(MEDIUM_SLEEP);
//                    publishProgress(totalCounts, -1);//下载结束
//                    sleep(MEDIUM_SLEEP);
                    publishProgress(totalCounts, -2);//开始导入
                    onImportOneTime();
                    BusinessConfig.getInstance().setFlag(context, TransDataKey.FLAG_HAS_DOWNLOAD_CARK, true);
                }
            }

            @Override
            protected void onReturn(String reqTag, byte[] respData, String code, String msg) {
                if (respData != null) {
                    Map<String, Object> resp = factory.unPackMessage(reqTag, respData);
                    String respCode = (String) resp.get(TradeInformationTag.RESPONSE_CODE);
                    ISORespCode isoCode = ISORespCode.codeMap(respCode);
                    result[0] = isoCode.getCode();
                    result[1] = context.getString(isoCode.getResId());
                    String iso62;
                    switch (reqTag) {
                        case POS_STATUS_UPLOAD:
                            iso62 = (String) resp.get(TradeInformationTag.IC_PARAMETER_INDEX);
                            if ("00".equals(respCode)) {
                                String result = iso62.substring(0, 2);
                                String values = null;
                                if (iso62.length() > 2) {
                                    values = iso62.substring(2, iso62.length());
                                }
                                if ("01".equals(result) || "03".equals(result)) {//有参数，且一个报文就能存下
                                    logger.info("公钥信息获取成功==>无更多公钥信息==>准备下载");
                                    String[] arr = values == null ? new String[]{} : values.split("9F06");
                                    for (int i = 0; i < arr.length; i++) {
                                        if (TextUtils.isEmpty(arr[i]) || arr[i].length() < 2) {
                                            logger.warn("CAPK==>" + arr[i] + "==>非法");
                                            continue;
                                        }
                                        Iso62Capk capk = new Iso62Capk("9F06" + arr[i]);
                                        infoList.add(capk);
                                    }
                                    totalCounts = infoList.size();
                                    startDownload();//开始下载具体公钥的参数
                                } else if ("02".equals(result)) {//有公钥参数，一个报文存不下，需要再次发送请求
                                    logger.info("公钥信息获取成功==>继续获取更多");
                                    int counts = values.length() / 46;
                                    for (int i = 0; i < counts; i++) {
                                        Iso62Capk capk = new Iso62Capk(values.substring(i * 46, (i + 1) * 46));
                                        infoList.add(capk);
                                    }
                                    dataMap.put(KEY_PARAMS_COUNTS, "" + infoList.size());
                                    Object pkgMsg = factory.packMessage(POS_STATUS_UPLOAD, dataMap);
                                    sendNext(POS_STATUS_UPLOAD, (byte[]) pkgMsg);//继续获取公钥参数信息
                                } else {//无公钥参数
                                    logger.warn("无公钥信息");
                                }
                            }
                            break;
                        case DOWNLOAD_CAPK:
                            iso62 = (String) resp.get(TradeInformationTag.IC_PARAMETER_CAPK);
                            if ("00".equals(respCode)) {
                                String result = iso62.substring(0, 2);
                                String values = null;
                                if (iso62.length() > 2) {
                                    values = iso62.substring(2, iso62.length());
                                }
                                if ("31".equals(result)) {
                                    onSave(values);
                                }
                                //继续下一条的下载
                                startDownload();
                            }
                            break;
                        case DOWNLOAD_PARAMS_FINISHED:
                            //下载结束报文结果，不关心
                            break;
                    }
                } else {
                    result[0] = code;
                    result[1] = msg;
                }
            }
        };
//        SocketClient client = SocketClient.getInstance(context);
//        client.syncSendSequenceData(TransCode.POS_STATUS_UPLOAD, (byte[]) msgPacket, handler);
        DataExchanger dataExchanger = DataExchangerFactory.getInstance();
        dataExchanger.doSequenceExchange(TransCode.POS_STATUS_UPLOAD, (byte[]) msgPacket, handler);
        DbHelper.releaseInstance();
        return result;
    }

    /**
     * 保存公钥到数据库，用于后续一次性导入到终端
     *
     * @param capk capk
     * @return 保存成功返回true，失败返回false
     */
    private boolean onSave(String capk) {
        Iso62Capk bean = new Iso62Capk(capk);
        boolean r = dao.save(bean);
        return r;
    }

    /**
     * 一次性导入已下载好公钥参数
     */
    private boolean onImportOneTime() {
        List<Iso62Capk> list = dao.query();
        logger.info("正在导入公钥参数==>待导入的条数==>" + (list == null ? 0 : list.size()));
        if (list != null && list.size() > 0) {
            try {
                IPbocService pbocService = DeviceFactory.getInstance().getPbocService();
                pbocService.updateCAPK(EnumAidCapkOperation.CLEAR, null);//清空所有的公钥参数
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (int i = 0; i < list.size(); i++) {
                Iso62Capk capkBean = list.get(i);
                String capk = capkBean.getCapk();
                boolean r = false;
                try {
                    IPbocService pbocService = DeviceFactory.getInstance().getPbocService();
                    capk = changeCASMHashData(capk);
                    r = pbocService.updateCAPK(EnumAidCapkOperation.UPDATE, capk);//更新终端的公钥参数
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (r) {
                    logger.info("导入公钥参数成功==>" + capkBean.getRID() + capkBean.getIndex() + "==>删除数据库记录");
                    dao.delete(capkBean);
                } else {
                    logger.warn("导入公钥参数失败==>" + capkBean.getRID() + capkBean.getIndex());
                    capkBean.setImportTimes(capkBean.getImportTimes() + 1);
                    dao.update(capkBean);
                }
            }
        }
//        sleep(LONG_SLEEP);
//        publishProgress(totalCounts, -3);//导入完成
//        sleep(1000);
        return false;
    }

    public String changeCASMHashData(String caStr) {
        try {
            Map<String, String> caMap = TlvUtil.tlvToMap(caStr);
            String resultCa;
            String tagDF06 = caMap.get("DF06");//哈希算法标识；
            String tagDF07 = caMap.get("DF07");//算法标识；
            boolean isNeedReplace = false;
            if ("11".equals(tagDF06) && "04".equals(tagDF07)) {
                caMap.put("DF06", "07");
                isNeedReplace = true;
                XLogUtil.d(TAG, "changeCASMHashData: need fix");
            } else if ("04".equals(tagDF07) && "07".equals(tagDF06)) {
                isNeedReplace = true;
                XLogUtil.d(TAG, "changeCASMHashData: need fix2");
            }
            String smData = "";
            if (isNeedReplace) {//是否需要计算哈希校验值
                String temp = caMap.get("9F06") + caMap.get("9F22") + caMap.get("DF02");
                try {
                    smData = HexUtil.bytesToHexString(SM3.hash(HexUtil.hexStringToByte(temp)));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            caMap.put("DF03", smData);

            if (isNeedReplace) {
                XLogUtil.d(TAG, "changeCASMHashData: " + caMap);
                resultCa = TlvUtil.mapToTlv(caMap);
                XLogUtil.d(TAG, "changeCASMHashData: " + resultCa);
            } else {
                resultCa = caStr;
            }
            return resultCa;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
