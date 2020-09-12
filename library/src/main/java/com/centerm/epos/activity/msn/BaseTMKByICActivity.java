package com.centerm.epos.activity.msn;

import android.content.Context;
import android.os.AsyncTask;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;
import com.centerm.cpay.midsdk.dev.define.IIcCardDev;
import com.centerm.cpay.midsdk.dev.define.IPinPadDev;
import com.centerm.epay.keyboard.HexNumberKeyboard;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseActivity;
import com.centerm.epos.common.EncryptAlgorithmEnum;
import com.centerm.epos.common.Settings;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.iso8583.util.SecurityUtil;
import com.centerm.smartpos.util.HexUtil;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

/**
 * 主密钥从IC卡下载，广东银联方案
 * Created by ysd on 2016/11/30.
 */
public class BaseTMKByICActivity extends BaseActivity {
    private static final String TAG = BaseTMKByICActivity.class.getSimpleName();
    private static final long TIME_OUT_S = 180000;
    public static final String IS_MANUAL_INPUT_TMK = "is manual input tmk";

    private static final int KEY_LEN = 32;  //密钥长度16字节，32字符
    private static final int PWD_LEN = 6;   //密码长度6个字符
    private static final int CHECK_VALUE_LEN = 8;   //密钥校验值长度4个字节，8个字符

    Boolean isManualInputTMK = false;
    IIcCardDev iIcCardDev;
    private TextView tvPwd, tvInputTMK, tvCheckValue;
    private TextView tvTMKIndex;
    private EditText etTMKIndex, etInputTMK, etCheckValue;
    private EditText etKeyCardPwd;
    private Button btnTips;
    boolean isTransactionKeyCardIn = false;
    String keyCardPwd;
    String tmkIndexStr;
    String keyEncryptedStr, keyCheckValue;

    @Override
    public int onLayoutId() {
        return R.layout.activity_ic_tmk_setting;
    }

    @Override
    public void onInitView() {
        TextView txtvw = (TextView) findViewById(R.id.txtvw_title);
        txtvw.setText(R.string.label_tmk);

        isManualInputTMK = getIntent().getExtras().getBoolean(IS_MANUAL_INPUT_TMK, false);
        initContentView(isManualInputTMK);
    }

    private void initContentView(Boolean isManualInputTMK) {
        tvPwd = (TextView) findViewById(R.id.tx_pwd);
        tvPwd.setVisibility(View.VISIBLE);
        etKeyCardPwd = (EditText) findViewById(R.id.extxt_key_card_pwd);
        etKeyCardPwd.setVisibility(View.VISIBLE);
        btnTips = (Button) findViewById(R.id.btn_tips);
        if (isManualInputTMK) {
            tvPwd.setText(R.string.pls_input_weihu_pwd);
            tvInputTMK = (TextView) findViewById(R.id.textView_input_key);
            tvInputTMK.setVisibility(View.VISIBLE);
            tvCheckValue = (TextView) findViewById(R.id.textView_input_check);
            tvCheckValue.setVisibility(View.VISIBLE);
            etInputTMK = (EditText) findViewById(R.id.extxt_key_encrypted);
            etInputTMK.setVisibility(View.VISIBLE);
            etCheckValue = (EditText) findViewById(R.id.extxt_key_check);
            etCheckValue.setVisibility(View.VISIBLE);
            btnTips.setText(R.string.pls_insert_weihu_card);
            initEditWithKeyboard(this, findViewById(R.id.keyboard_view), etInputTMK);
            initEditWithKeyboard(this, findViewById(R.id.keyboard_view), etCheckValue);
            isTransactionKeyCardIn = true;
        } else {
            tvTMKIndex = (TextView) findViewById(R.id.textView);
            tvTMKIndex.setVisibility(View.VISIBLE);
            etTMKIndex = (EditText) findViewById(R.id.extxt_key_index);
            etTMKIndex.setVisibility(View.VISIBLE);
        }
    }

    private void initEditWithKeyboard(final Context context, final View view, final EditText editText) {

        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                HexNumberKeyboard kb = new HexNumberKeyboard(context, view, editText);
                if (android.os.Build.VERSION.SDK_INT <= 10)
                    editText.setInputType(InputType.TYPE_NULL);
                else {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    try {
                        Class<EditText> cls = EditText.class;
                        Method setShowSoftInputOnFocus;
                        setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                        setShowSoftInputOnFocus.setAccessible(true);
                        setShowSoftInputOnFocus.invoke(editText, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                kb.showKeyboard();
                return false;
            }
        });
    }

    @Override
    public void afterInitView() {
        super.afterInitView();
        try {
            iIcCardDev = DeviceFactory.getInstance().getIcCardDev();
            new CheckICCardInPosition(true).execute();
        } catch (Exception e) {
            e.printStackTrace();
            ViewUtils.showToast(this, "界面初始化异常：" + e.getMessage());
            activityStack.pop();
        }
    }

    public void onSureClick(View v) {
        keyCardPwd = etKeyCardPwd.getText().toString();
        if (TextUtils.isEmpty(keyCardPwd) || keyCardPwd.length() != PWD_LEN) {
            ViewUtils.showToast(this, "请输入"+PWD_LEN+"位密码！");
            return;
        }
        if (isManualInputTMK) {
            keyEncryptedStr = etInputTMK.getText().toString();
            if (TextUtils.isEmpty(keyEncryptedStr) || keyEncryptedStr.length() != KEY_LEN) {
                ViewUtils.showToast(this, "请输入"+KEY_LEN+"位密钥数据！");
                return;
            }
            keyCheckValue = etCheckValue.getText().toString();
            if (TextUtils.isEmpty(keyCheckValue) || keyCheckValue.length() != CHECK_VALUE_LEN){
                ViewUtils.showToast(this, "请输入"+CHECK_VALUE_LEN+"位校验值！");
                return;
            }
        } else {
            if (!isTransactionKeyCardIn) {
                tmkIndexStr = etTMKIndex.getText().toString();
                if (TextUtils.isEmpty(tmkIndexStr) || Long.parseLong(tmkIndexStr) > 4294967295L) {
                    ViewUtils.showToast(this, "请输入正确的的密钥序号！");
                    return;
                }
            }
        }
        btnTips.setEnabled(false);
        if (!isManualInputTMK)
            tvPwd.setText(R.string.pls_input_weihu_pwd);

        if (!isTransactionKeyCardIn)
            new ExecuteKeyICCard().execute();
        else
            new ExecuteManagerICCard().execute();
    }

    private final byte[] psamSuccess = new byte[]{(byte) 0x90, 0x00};

    private boolean checkRespBytes(byte[] retBuf) {
        if (retBuf == null || retBuf.length < 2) {
            return false;
        }
        byte[] respCode = Arrays.copyOfRange(retBuf, retBuf.length - 2, retBuf.length);
        if (!Arrays.equals(respCode, psamSuccess)) {
            return false;
        }
        return true;
    }


    class ExecuteKeyICCard extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            byte[] retBuf = null;
            String errorTips = null;
            boolean result = false;

            iIcCardDev.open();
            do {
                if (!iIcCardDev.getStatus()) {
                    errorTips = "未检测到IC卡插入！";
                    break;
                }

                retBuf = iIcCardDev.reset();

                //复位返回的数据为：3BB794008131FE4553504B32349000F6，所以不能够校验最后2位
//                if (!checkRespBytes(retBuf)) {
//                    errorTips = "密钥存储卡的复位失败！";
//                    break;
//                }
                Log.d(TAG, "密钥存储卡的复位成功:" + HexUtil.bytesToHexString(retBuf));

                //检验密码
                retBuf = iIcCardDev.send(HexUtil.hexStringToByte("0020008108" + keyCardPwd + "FFFFFFFFFF"));
                if (!checkRespBytes(retBuf)) {
                    errorTips = "密钥存储卡的密码校验失败！";
                    break;
                }
                Log.d(TAG, "密钥存储卡的密码校验成功");

                //选择存放密钥的二进制文件
                retBuf = iIcCardDev.send(HexUtil.hexStringToByte("00A4000C02EF09"));
                if (!checkRespBytes(retBuf)) {
                    errorTips = "密钥存储卡的密钥文件选择失败！";
                    break;
                }
                Log.d(TAG, "密钥文件选择成功");

                //读出密钥条数
                retBuf = iIcCardDev.send(HexUtil.hexStringToByte("00B0000003"));
                if (!checkRespBytes(retBuf)) {
                    errorTips = "密钥存储卡的密码条数读取失败！";
                    break;
                }
                Log.d(TAG, "读取密钥条数返回数据：" + HexUtil.bytesToHexString(retBuf));

                int totalNum = Integer.parseInt(new String(Arrays.copyOf(retBuf, retBuf.length - 2)));
                if (totalNum == 0) {
                    errorTips = "读取密钥条数为0";
                    break;
                }
                //使用long比较，防止前面有填充0的情况
                long keyIndex = Long.parseLong(tmkIndexStr);
                String cmd;
                int i;
                StringBuilder builder;
                for (i = 0; i < totalNum; i++) {
                    //读出密钥内容
                    cmd = "00B0" + String.format(Locale.CHINA, "%04X", 3 + i * 24) + "18";
                    retBuf = iIcCardDev.send(HexUtil.hexStringToByte(cmd));
                    if (!checkRespBytes(retBuf)) {
                        errorTips = "密钥存储卡：读取第" + i + "条密钥失败！";
                        break;
                    }
                    Log.d(TAG, "密钥存储卡：读取第" + i + "条密钥返回数据：" + HexUtil.bytesToHexString(retBuf));
                    builder = new StringBuilder();
                    builder.append(String.format(Locale.CHINA, "%03d", retBuf[0]&0xff));
                    builder.append(String.format(Locale.CHINA, "%02d", retBuf[1]&0xff));
                    builder.append(String.format(Locale.CHINA, "%03d", retBuf[2]&0xff));
                    builder.append(String.format(Locale.CHINA, "%03d", retBuf[3]&0xff));
                    Log.d(TAG, "密钥存储卡：密钥序号 " + builder.toString());
                    if (keyIndex == Long.parseLong(builder.toString()))
                        break;
                }
                if (i >= totalNum) {
                    errorTips = "未找到指定序号" + keyIndex + "的密钥！";
                    break;
                }
                result = true;
            } while (false);

            if (result) {
                keyEncryptedStr = HexUtil.bcd2str(Arrays.copyOfRange(retBuf, 4, retBuf.length - 2));
                Log.d(TAG, "截取的密钥密文：" + keyEncryptedStr);
            } else {
                Log.e(TAG, errorTips);
            }
            iIcCardDev.close();
            return errorTips;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (!TextUtils.isEmpty(s)) {
                ViewUtils.showToast(BaseTMKByICActivity.this, s);
                activityStack.pop();
            } else {
                ViewUtils.showToast(BaseTMKByICActivity.this, "密钥读取成功，请更换密钥卡！");
                tvTMKIndex.setVisibility(View.GONE);
                etTMKIndex.setVisibility(View.GONE);
                etKeyCardPwd.setText("");
                btnTips.setText("密钥读取成功，请更换密钥卡...");
                isTransactionKeyCardIn = true;
                new CheckICCardInPosition(false).execute();
            }
        }
    }

    class ExecuteManagerICCard extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String resultTips = "主密钥成功导入！";
            do {
                if (TextUtils.isEmpty(keyEncryptedStr)) {
                    resultTips = "密钥密文为空！";
                    break;
                }
                //key + checkvalue
                if (keyEncryptedStr.length() != (isManualInputTMK?32:40)) {
                    resultTips = "加密密钥长度错误！";
                    break;
                }
                byte[] retBuf = null;
                iIcCardDev.open();
                if (!iIcCardDev.getStatus()) {
                    resultTips = "未检测到IC卡插入！";
                    break;
                }

                retBuf = iIcCardDev.reset();
                //复位返回的数据为：3BB794008131FE4553504B32349000F6，所以不能够校验最后2位
//                if (!checkRespBytes(retBuf)) {
//                    resultTips = "传输密钥卡的复位失败！";
//                    break;
//                }
                Log.d(TAG, "传输密钥卡的复位成功:" + HexUtil.bytesToHexString(retBuf));

//                Log.d(TAG, "PWD:"+keyCardPwd+" KEY:"+keyEncryptedStr+ " CHECKVALUE:"+keyCheckValue);
                //检验密码
                retBuf = iIcCardDev.send(HexUtil.hexStringToByte("0020008108" + keyCardPwd + "FFFFFFFFFF"));
                if (!checkRespBytes(retBuf)) {
                    resultTips = "传输密钥卡的密码校验失败！";
                    break;
                }
                Log.d(TAG, "传输密钥卡的密码校验成功");

                //选择文件
                retBuf = iIcCardDev.send(HexUtil.hexStringToByte("00A4000C02EF08"));
                if (!checkRespBytes(retBuf)) {
                    resultTips = "传输密钥卡的文件选择失败！";
                    break;
                }
                Log.d(TAG, "传输密钥卡文件选择成功");

                //读有效期
                retBuf = iIcCardDev.send(HexUtil.hexStringToByte("00B0000008"));
                if (!checkRespBytes(retBuf)) {
                    resultTips = "传输密钥卡的有效期读取失败！";
                    break;
                }
                String expiredDateStr = new String(Arrays.copyOf(retBuf, retBuf.length - 2));
                Log.d(TAG, "传输密钥卡有效期读取成功：" + expiredDateStr);
                try {
                    Date expiredData = new SimpleDateFormat("yyyyMMdd").parse(expiredDateStr);
                    if (expiredData.getTime() < new Date().getTime()) {
                        resultTips = "维护卡已过期：" + expiredDateStr;
                        break;
                    }
                } catch (ParseException e) {
                    resultTips = e.getMessage();
                    break;
                }

                //解密密钥
                retBuf = iIcCardDev.send(HexUtil.hexStringToByte("80F8010210" + keyEncryptedStr.substring(0, 32) +
                        "10"));
                if (!checkRespBytes(retBuf)) {
                    resultTips = "密钥解密失败！";
                    break;
                }
                Log.d(TAG, "密钥解密成功：" + HexUtil.bytesToHexString(retBuf));

                //密钥校验
                byte[] keyPlain = Arrays.copyOf(retBuf, retBuf.length - 2);
                byte[] checkValue;
                if (isManualInputTMK){
                    checkValue = HexUtils.hexStringToByte(keyCheckValue);
                }else
                    checkValue = HexUtil.hexStringToByte(keyEncryptedStr.substring(32, keyEncryptedStr.length()));
                byte[] calCheckValue = SecurityUtil.encrype3Des(keyPlain, new byte[]{0, 0, 0, 0, 0, 0, 0, 0});
                Log.d(TAG, "计算得出的校验值：" + HexUtil.bytesToHexString(calCheckValue));
                if (!Arrays.equals(checkValue, Arrays.copyOf(calCheckValue, calCheckValue.length - 4))) {
                    resultTips = "密钥校验失败！";
                    break;
                }
                Log.d(TAG, "主密钥明文校验成功！");

                String keyPlainStr = HexUtil.bytesToHexString(keyPlain);
                try {
                    EncryptAlgorithmEnum encAlg = Settings.getEncryptAlgorithmEnum(context);
                    IPinPadDev pinPadDev = CommonUtils.getPinPadDev();
                    if (encAlg == EncryptAlgorithmEnum.SM4)
                        pinPadDev.loadSM4TMK(keyPlainStr, null);
                    else
                        pinPadDev.loadTMK(keyPlainStr, null);
                    Log.d(TAG, "主密钥下载成功！");
                } catch (Exception e) {
                    resultTips = "密码键盘下载主密钥失败：" + e.getMessage();
                }
            } while (false);
            iIcCardDev.close();
            return resultTips;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (!TextUtils.isEmpty(s))
                ViewUtils.showToast(BaseTMKByICActivity.this, s);
            activityStack.pop();
        }
    }

    class CheckICCardInPosition extends AsyncTask<Void, Void, Boolean> {
        boolean isCheckIn = true;

        public CheckICCardInPosition(boolean isCheckIn) {
            this.isCheckIn = isCheckIn;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            iIcCardDev.open();
            long begin = System.currentTimeMillis();
            while (System.currentTimeMillis() - begin < TIME_OUT_S) {
                if (isCheckIn) {
                    //检测插卡
                    if (iIcCardDev.getStatus()) {
                        iIcCardDev.close();
                        return true;
                    }
                } else {
                    //检测拔卡
                    if (!iIcCardDev.getStatus()) {
                        iIcCardDev.close();
                        return true;
                    }
                }
            }
            iIcCardDev.close();
            return false;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            if (b) {
                if (isCheckIn) {
                    btnTips.setText("确认");
                    btnTips.setEnabled(true);
                } else {
                    btnTips.setText(R.string.pls_insert_weihu_card);
                    new CheckICCardInPosition(true).execute();
                }
            } else {
                ViewUtils.showToast(BaseTMKByICActivity.this, "操作超时！");
                activityStack.pop();
            }
        }
    }
}
