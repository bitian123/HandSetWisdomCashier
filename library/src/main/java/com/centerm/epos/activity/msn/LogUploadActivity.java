package com.centerm.epos.activity.msn;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import com.centerm.epos.EposApplication;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseActivity;
import com.centerm.epos.mvp.view.ISecurityView;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.FileUtils;
import com.centerm.epos.utils.SFTPUtils;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.utils.XLogUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import config.BusinessConfig;
import config.Config;

/**
 * ClassName: LogUploadActivity  日志上传界面
 * <p>
 * 0、选择上传文件的日期
 * 1、对文件夹进行压缩
 * 2、对压缩文件上传
 * 3、删除本地因上传操作生成的压缩文件，避免文件过大
 *
 * @author liuyanwei created at 2019/10/31 10:48
 */
public class LogUploadActivity extends BaseActivity implements ISecurityView, View.OnClickListener {

    /**
     * 日历
     */
    private CalendarView calendarView;
    /**
     * 选择的日期
     */
    private String selectetDate;
    /**
     * 文件日期
     */
    private String fileDate;
    /**
     * 上送文件按钮
     */
    private Button btnCommite;
    /**
     * 日期选择提示
     */
    private TextView tvCommite;
    /**
     * 文件分割线
     */
    private final String splitLine = "/";
    private final String TAG = LogUploadActivity.class.getSimpleName();
    /**
     * 复制后日志目录
     */
    private String srcFilePath;
    /**
     * 日志文件根目录
     */
    private String logPath = "";
    /**
     * 终端号
     */
    private String terminalNo ;
    /**
     * 商户号
     */
    private String mecharNo;

    /**
     * POS序列号SN
     */
    private String posSn;
    /**
     * SFTP工具类
     */
    private SFTPUtils sftp;
    /**
     * SFTP服务器上传地址
     */
    private String remotePath;
    /**
     * SFTP服务器上传文件名称
     */
    private String remoteFileName;
    /**
     * 默认日志路径
     */
    private final static String DEFAULT_LOG_PATH = Config.Path.DEFAULT_LOG_PATH + File.separator;
    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
    /**
     * 本地文件路径
     */
    private final String localPath = DEFAULT_LOG_PATH + EposApplication.getAppContext().getPackageName() ;
    /**
     * 当日本地文件名
     */
    private final String  currentDateFile =formatter.format(new Date()) + "_" + ".log";
    /**
     * 本地上传文件名称
     */
    private String localFileName;

    /**
     * 保留天数：默认90天，可修改
     */
    private int savedLogSavedDays = 90;

    @Override
    public void onClick(View v) {

    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_upload_log;
    }

    @Override
    public void onInitView() {
        setTitle(R.string.title_upload_log);
        calendarView =(CalendarView)findViewById(R.id.calendarView_log);
        btnCommite = (Button) findViewById(R.id.btn_upload);
        tvCommite = (TextView) findViewById(R.id.tv_log_date);
        btnCommite.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_blue_radius_bg,null));
        tvCommite.setText("请选择" + getDate89DaysBefore() + "至" + getCurrentDate() + "之间\n某一天的日志进行上传");
        posSn =  CommonUtils.getSn();


        terminalNo = BusinessConfig.getInstance().getIsoField(context, 41);
        mecharNo= BusinessConfig.getInstance().getIsoField(context, 42);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        selectetDate = formatter.format(new Date());
        fileDate = simpleDateFormat.format(new Date());
        remoteFileName = posSn+ "-" +mecharNo + "-" + terminalNo + "-" + fileDate + ".zip";
        localFileName = posSn+ "-" +mecharNo + "-" + terminalNo + "-" + fileDate + ".zip";
        remotePath = "/upload/lvcheng/"+fileDate;
        initData();
    }

    private void initData() {
        //calendarView 监听事件
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                //显示用户选择的日期
                selectetDate = "" + year + String.format("%02d", month + 1) + String.format("%02d", dayOfMonth);
                XLogUtil.w(TAG,"选择日志日期:"+selectetDate);
                fileDate = "" + year + "-" + String.format("%02d", month + 1) + "-" + String.format("%02d", dayOfMonth);
                remoteFileName = posSn+ "-" +mecharNo + "-" + terminalNo + "-" + fileDate + ".zip";
                localFileName = posSn+ "-" +mecharNo + "-" + terminalNo + "-" + fileDate + ".zip";
                if (!isDateRight(selectetDate)) {
                    ViewUtils.showToast(context,"选择日期区间不对，请重新选择");
                    btnCommite.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_gray_radius_bg,null));
                    btnCommite.setClickable(false);
                    btnCommite.setClickable(false);
                } else {
                    btnCommite.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.btn_blue_radius_bg,null));
                    btnCommite.setClickable(true);
                    btnCommite.setClickable(true);
                }

            }
        });
        /**
         * 上传按钮监听
         */
        btnCommite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hasLogFile()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            XLogUtil.d( TAG, "==========上传日志=======开始====");
                            handler.obtainMessage(0).sendToTarget();
                            XLogUtil.d( TAG, "SFTP开始连接");
                            sftp = new SFTPUtils(SFTPUtils.SFTP_IP, SFTPUtils.USER_NAME, SFTPUtils.USER_PWD);
                            sftp.connect();
                            XLogUtil.d( TAG, "SFTP连接成功");
                            //1、对文件夹进行压缩
                            compressLog();
                            XLogUtil.d(TAG,"remotePath:"+remotePath +" remoteFileName:"+remoteFileName);
                            XLogUtil.d(TAG,"localPath:"+localPath+" localFileName:"+localFileName);
                            //2、对压缩文件上传
                            sftp.uploadFile(remotePath, remoteFileName, localPath, localFileName);

                            XLogUtil.d(TAG, "上传成功");
                            sftp.disconnect();
                            XLogUtil.d( TAG, "断开连接");
                            //3.删除本地压缩文件
                            delFile();
                            handler.obtainMessage(1).sendToTarget();
                            XLogUtil.d(TAG,  "==========上传日志=======结束====");
                        }
                    }).start();

                }else {
                    handler.obtainMessage(2).sendToTarget();
                }
            }
        });


    }


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0:
                    DialogFactory.showLoadingDialog(context, "日志上传中……");
                    XLogUtil.d(TAG,"日志上传中...");
                    break;
                case 1:
                    ViewUtils.showToast(context,"日志上传成功");
                    XLogUtil.d(TAG,"日志上传成功");
                    DialogFactory.hideAll();
                    break;
                case 2:{
                    ViewUtils.showToast(context,"当前日期无日志");
                    XLogUtil.d(TAG,"当前日期 "+selectetDate+" 无日志");
                }break;
                default:
                    break;
            }
        }

        ;
    };


    /**
     * 是否有日志
     * @return
     */
    private boolean hasLogFile(){
        if (!TextUtils.isEmpty(FileUtils.getSDPath())) {
            logPath =  DEFAULT_LOG_PATH + EposApplication.getAppContext().getPackageName() ;
            XLogUtil.d( TAG, "logPath:" + logPath);
        }

        srcFilePath = logPath +splitLine+ selectetDate + "_" + ".log";
        XLogUtil.d( TAG, "srcFilePath:" + srcFilePath);
        boolean isHasLog = FileUtils.hasLog(srcFilePath);
        XLogUtil.d(TAG,"选定日期: "+selectetDate+"  是否有日志:"+isHasLog);
        return isHasLog;
    }


    /**
     * 压缩文件
     */
    private void compressLog() {
        try {
            if (!TextUtils.isEmpty(FileUtils.getSDPath())) {
                logPath =  DEFAULT_LOG_PATH + EposApplication.getAppContext().getPackageName() +splitLine;
            }
            // 压缩文件格式：终端号+日期.zip (例：SN+872221176410001-08001432-2019-11-11.zip)
            String zipFile =  logPath+posSn+ "-"+ mecharNo + "-" + terminalNo + "-" + fileDate + ".zip";
            XLogUtil.d( TAG, "zipFile:" + zipFile);
            srcFilePath =logPath +splitLine+ selectetDate + "_" + ".log";
            XLogUtil.d( TAG, "srcFilePath:" + srcFilePath);
            FileUtils.zipFolder(srcFilePath, zipFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 删除因上传生成的本地文件
     */
    private void delFile() {
        //删除压缩文件
        if (!TextUtils.isEmpty(FileUtils.getSDPath())) {
            logPath =  DEFAULT_LOG_PATH + EposApplication.getAppContext().getPackageName() +splitLine;
        }
        FileUtils.deleteFile(logPath+posSn+ "-"+ mecharNo + "-" + terminalNo + "-" + fileDate + ".zip");
    }

    /**
     * 获取90天前的日期
     *
     *
     * @author liuyanwei
     * created at 2019/10/31 14:44
     * @params returnType
     */

    private String getDate90DaysBefore() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -savedLogSavedDays);
        Date date = calendar.getTime();
        return simpleDateFormat.format(date);
    }
    /**
     * 获取89天前的日期
     *
     * @author liuyanwei
     * created at 2019/10/31 14:44
     * @params returnType
     */

    private String getDate89DaysBefore() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -(savedLogSavedDays-1));
        Date date = calendar.getTime();
        return simpleDateFormat.format(date);
    }

    /**
     * //获取当前时间
     *
     * @author liuyanwei
     * created at 2019/10/31 14:42
     * @params returnType
     */

    private String getCurrentDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);

    }

    /**
     * 选择的日期是否在可选区间内（当前日期-90 到  当日）
     *
     * @author liuyanwei
     * created at 2019/11/5 16:07
     * @params returnType
     */
    private boolean isDateRight(String str) {
        int current, dateBefore90Days, selected;

        current = Integer.parseInt(getCurrentDate());
        dateBefore90Days = Integer.parseInt(getDate90DaysBefore());
        selected = Integer.parseInt(str);
        Log.d("current:", "" + current);
        Log.d("dateBefore90Days:", "" + dateBefore90Days);
        Log.d("selected:", "" + selected);
        if (((selected - dateBefore90Days) > 0) && ((selected - current) <= 0)) {
            return true;
        }
        return false;
    }



    @Override
    public void showResult(String result, boolean isSuccess) {

    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
