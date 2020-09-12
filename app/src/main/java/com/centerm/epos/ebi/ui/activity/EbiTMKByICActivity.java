package com.centerm.epos.ebi.ui.activity;

import android.content.Context;
import android.content.Intent;
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
import com.centerm.epos.base.TradeFragmentContainer;
import com.centerm.epos.common.EncryptAlgorithmEnum;
import com.centerm.epos.common.Settings;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.model.BaseTradeParameter;
import com.centerm.epos.model.ITradeParameter;
import com.centerm.epos.transcation.pos.data.BaseField62;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.xml.bean.process.TradeProcess;
import com.centerm.iso8583.util.SecurityUtil;
import com.centerm.smartpos.util.HexUtil;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.centerm.epos.common.TransCode.OBTAIN_TMK;

/**
 * 通过IC密钥卡，POS主密钥远程下载
 * Created by liubit on 2018/1/5.
 */
public class EbiTMKByICActivity extends BaseActivity {
    private static final String TAG = EbiTMKByICActivity.class.getSimpleName();
    private static final long TIME_OUT_S = 180000;
    public static final String IS_MANUAL_INPUT_TMK = "is manual input tmk";

    private static final int PWD_LEN = 6;   //密码长度6个字符

    Boolean isManualInputTMK = false;
    IIcCardDev iIcCardDev;
    private TextView tvPwd, tvInputTMK, tvCheckValue;
    private TextView tvTMKIndex,mTvTip;
    private EditText etTMKIndex, etInputTMK, etCheckValue;
    private EditText etKeyCardPwd;
    private Button btnTips;
    boolean isTransactionKeyCardIn = false;
    String keyCardPwd;
    String hasTime = "";
    String checkValue = "650C9943C6A7645F";
    String icNO = "";
    String tmk = "5B223EB871D7EDDA6D837222FEC0E030";//650C9943C6A7645F
    private StringBuilder builder = new StringBuilder();

    @Override
    public int onLayoutId() {
        return R.layout.activity_ic_tmk_setting;
    }

    @Override
    public void onInitView() {
        TextView txtvw = (TextView) findViewById(R.id.txtvw_title);
        txtvw.setText(com.centerm.epos.ebi.R.string.label_tmk_download_pos);
        mTvTip = (TextView) findViewById(R.id.mTvTip);

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
        tvTMKIndex = (TextView) findViewById(R.id.textView);
        tvTMKIndex.setVisibility(View.GONE);
        etTMKIndex = (EditText) findViewById(R.id.extxt_key_index);
        etTMKIndex.setVisibility(View.GONE);
        mTvTip.setVisibility(View.VISIBLE);

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
        if(TextUtils.equals("确认", btnTips.getText().toString())){
            keyCardPwd = etKeyCardPwd.getText().toString();
            if (TextUtils.isEmpty(keyCardPwd) || keyCardPwd.length() != PWD_LEN) {
                ViewUtils.showToast(this, "请输入"+PWD_LEN+"位密码！");
                return;
            }
            if (!isTransactionKeyCardIn)
                new ExecuteKeyICCard().execute();
            else
                new ExecuteManagerICCard().execute();
        }else if(TextUtils.equals("下载主密钥", btnTips.getText().toString())){
            startProcess();
        }

    }

    private void startProcess(){
        String transCode = OBTAIN_TMK;
        ConfigureManager config = ConfigureManager.getInstance(this);
        TradeProcess process = config.getTradeProcess(this, "online2");
        if (process == null) {
            logger.warn("通用联机流程未定义！");
            return;
        }
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("IC_NO", icNO);
        dataMap.put("HAS_TIME", hasTime);
        process.setTransDatas(dataMap);
        //启动交易流程
        Intent intent = new Intent(this, TradeFragmentContainer.class);
        intent.putExtra(BaseActivity.KEY_TRANSCODE, transCode);
        intent.putExtra(BaseActivity.KEY_PROCESS, process);
        intent.putExtra(BaseActivity.KEY_NEED_ACT_RESULT, true);
        ITradeParameter parameter = (ITradeParameter) ConfigureManager.getSubPrjClassInstance(new BaseTradeParameter());
        if (parameter.getParam(transCode) != null)
            intent.putExtra(ITradeParameter.KEY_TRANS_PARAM, parameter.getParam(transCode));
        startActivityForResult(intent, REQ_TRANSACTION);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        logger.error(BaseField62.SecurityKey);
        if(!TextUtils.isEmpty(BaseField62.SecurityKey)&&BaseField62.SecurityKey.length()==48){
            tmk = BaseField62.SecurityKey.substring(0,32);
            checkValue = BaseField62.SecurityKey.substring(32,48);
            BaseField62.SecurityKey = "";
            logger.error("主密钥密文tmk:"+tmk);
            logger.error("checkValue:"+checkValue);
            new ExecuteManagerICCard().execute();
        }else {
            activityStack.pop();
        }
    }

    class ExecuteKeyICCard extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            byte[] returnBuf = null;
            String errorTips = null;

            iIcCardDev.open();
            do {
                if (!iIcCardDev.getStatus()) {
                    errorTips = "未检测到IC卡插入！";
                    break;
                }

                returnBuf = iIcCardDev.reset();
                if(returnBuf==null){
                    errorTips = "密钥存储卡的复位失败";
                    return errorTips;
                }
                Log.d(TAG, "密钥存储卡的复位成功:" + HexUtil.bytesToHexString(returnBuf));

                //选择应用目录
                returnBuf = iIcCardDev.send(HexUtils.hexStringToByte("00A40000023F20"));
                Log.d(TAG, "发送选择应用目录指令，返回数据:" + HexUtil.bytesToHexString(returnBuf));
                if(returnBuf==null||!HexUtil.bytesToHexString(returnBuf).contains("9000")){
                    errorTips = "发送选择应用目录指令失败！";
                    break;
                }

                //验证口令
                returnBuf = iIcCardDev.send(HexUtils.hexStringToByte("0020000003"+keyCardPwd));
                Log.d(TAG, "验证口令，返回数据:" + HexUtil.bytesToHexString(returnBuf));
                if(returnBuf==null||!HexUtil.bytesToHexString(returnBuf).contains("9000")){
                    errorTips = "验证口令失败！";
                    break;
                }

                //读IC卡剩余授权次数 80F3210002
                returnBuf = iIcCardDev.send(HexUtils.hexStringToByte("80F3210002"));
                Log.d(TAG, "读IC卡剩余授权次数指令，返回数据:" + HexUtil.bytesToHexString(returnBuf));
                if(returnBuf==null||!HexUtil.bytesToHexString(returnBuf).contains("9000")){
                    errorTips = "读IC卡剩余授权次数失败！";
                    break;
                }else {
                    hasTime = HexUtil.bytesToHexString(returnBuf).substring(0,4);
                    Log.d(TAG, "IC卡剩余授权次数:" + hasTime);
                    builder.append("IC卡剩余授权次数: " + Integer.parseInt(hasTime,16)+"\n\n");

                }

                //读主密钥的CheckValue 80F5210008
                returnBuf = iIcCardDev.send(HexUtils.hexStringToByte("80F5210008"));
                Log.d(TAG, "读主密钥的CheckValue，返回数据:" + HexUtil.bytesToHexString(returnBuf));
                if(returnBuf==null||!HexUtil.bytesToHexString(returnBuf).contains("9000")){
                    errorTips = "读主密钥的CheckValue失败！";
                    break;
                }else {
                    checkValue = HexUtil.bytesToHexString(returnBuf).substring(0,16);
                    Log.d(TAG, "读主密钥的CheckValue:" + checkValue);
                }

                //读二进制文件 进入到应用目录下 00B0820008
                returnBuf = iIcCardDev.send(HexUtils.hexStringToByte("00B0820008"));
                Log.d(TAG, "进入到应用目录下，返回数据:" + HexUtil.bytesToHexString(returnBuf));
                if(returnBuf==null||!HexUtil.bytesToHexString(returnBuf).contains("9000")){
                    errorTips = "进入到应用目录下失败！";
                    break;
                }else {
                    icNO = HexUtil.bytesToHexString(returnBuf).substring(0,16);
                    Log.d(TAG, "IC卡编号:" + icNO);
                    builder.append("IC卡编号: " + new String(HexUtils.hexStringToByte(icNO)));

                }

            } while (false);

            iIcCardDev.close();
            return errorTips;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (!TextUtils.isEmpty(s)) {
                ViewUtils.showToast(EbiTMKByICActivity.this, s);
                activityStack.pop();
            } else {
                mTvTip.setText(builder.toString());
                btnTips.setText("下载主密钥");
            }
        }
    }

    class ExecuteManagerICCard extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String resultTips = "主密钥成功导入！";
            do {
                if (TextUtils.isEmpty(tmk)) {
                    resultTips = "密钥为空！";
                    break;
                }
                byte[] retBuf = null;
                byte[] returnBuf = null;
                iIcCardDev.open();
                if (!iIcCardDev.getStatus()) {
                    resultTips = "未检测到IC卡插入！";
                    break;
                }

                retBuf = iIcCardDev.reset();

                //选择应用目录
                returnBuf = iIcCardDev.send(HexUtils.hexStringToByte("00A40000023F20"));
                Log.d(TAG, "发送选择应用目录指令，返回数据:" + HexUtil.bytesToHexString(returnBuf));
                if(returnBuf==null||!HexUtil.bytesToHexString(returnBuf).contains("9000")){
                    resultTips = "发送选择应用目录指令失败！";
                    break;
                }

                //验证口令
                returnBuf = iIcCardDev.send(HexUtils.hexStringToByte("0020000003"+keyCardPwd));
                Log.d(TAG, "验证口令，返回数据:" + HexUtil.bytesToHexString(returnBuf));
                if(returnBuf==null||!HexUtil.bytesToHexString(returnBuf).contains("9000")){
                    resultTips = "验证口令失败！";
                    break;
                }

                returnBuf = iIcCardDev.send(HexUtils.hexStringToByte("00B0820008"));
                Log.d(TAG, "进入到应用目录下，返回数据:" + HexUtil.bytesToHexString(returnBuf));
                if(returnBuf==null||!HexUtil.bytesToHexString(returnBuf).contains("9000")){
                    resultTips = "进入到应用目录下失败！";
                    break;
                }

                //校验口令 0020000003123456
                returnBuf = iIcCardDev.send(HexUtils.hexStringToByte("0020000003123456"));
                Log.d(TAG, "校验口令，返回数据:" + HexUtil.bytesToHexString(returnBuf));
                if(returnBuf==null||!HexUtil.bytesToHexString(returnBuf).contains("9000")){
                    resultTips = "校验口令失败！";
                    break;
                }

                //密钥密文解密 80FC210014
                returnBuf = iIcCardDev.send(HexUtils.hexStringToByte("80FC210014"+tmk+checkValue.substring(0,8)));
                if(returnBuf==null||!HexUtil.bytesToHexString(returnBuf).contains("9000")){
                    resultTips = "密钥密文解密失败！";
                    if(returnBuf!=null){
                        Log.d(TAG, "密钥密文解密，返回数据:" + HexUtil.bytesToHexString(returnBuf));
                    }
                    break;
                }else {
                    tmk = HexUtil.bytesToHexString(returnBuf).substring(0,32);
                    retBuf = HexUtils.hexStringToByte(tmk);
                    Log.d(TAG, "密钥明文:" + tmk);
                    //BusinessConfig.getInstance().setValue(context, BusinessConfig.Key.TMK_KEY, tmk);
                }

                //Log.d(TAG, "密钥解密成功：" + HexUtil.bytesToHexString(retBuf));

                String checkValueStr = checkValue;
                //密钥校验
                byte[] keyPlain = Arrays.copyOf(retBuf, retBuf.length);
                byte[] checkValue;
                checkValue = HexUtils.hexStringToByte(checkValueStr);
                byte[] calCheckValue = SecurityUtil.encrype3Des(keyPlain, new byte[]{0, 0, 0, 0, 0, 0, 0, 0});
                Log.d(TAG, "计算得出的校验值：" + HexUtil.bytesToHexString(calCheckValue));
                if (!Arrays.equals(checkValue, calCheckValue)) {
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
                ViewUtils.showToast(EbiTMKByICActivity.this, s);
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
                ViewUtils.showToast(EbiTMKByICActivity.this, "操作超时！");
                activityStack.pop();
            }
        }
    }

}
