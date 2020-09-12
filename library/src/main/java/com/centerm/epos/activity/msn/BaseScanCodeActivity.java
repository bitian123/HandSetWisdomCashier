package com.centerm.epos.activity.msn;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.centerm.epos.R;
import com.centerm.smartzbar.aidl.qrscan.CameraBeanZbar;
import com.centerm.smartzbar.aidl.qrscan.QuickScannerZbar;
import com.centerm.smartzbar.qrscan.base.QuickScanLibraryCallbackZbar;
import com.centerm.smartzbar.qrscan.base.camera.CameraManagerZbar;

import java.util.HashMap;

public class BaseScanCodeActivity extends Activity implements View.OnClickListener{

    private MyHandler mHandler;
    private SurfaceView surfaceView;
    static final int MSG_WHAT_OVER = 0x123;
    ImageView image_line;
    TranslateAnimation animation;
    //TextView light_control_tv;
    ImageButton light_control_img = null, return_img_button = null;
    boolean bIsLight = false;
    CameraBeanZbar cameraBeanZbar;
    TextView bottomTextView;

    private void scanBarCode(Activity activity) {
        short bestWidth = 640;
        short bestHeight = 480;
        byte spinDegree = 90;
        SurfaceHolder surfaceHolder;

        HashMap externalMap = new HashMap();
        cameraBeanZbar = new CameraBeanZbar(0, bestWidth, bestHeight, 5, 60L, spinDegree, 1);
        cameraBeanZbar.setExternalMap(externalMap);
        QuickScannerZbar quickScanZbar = new QuickScannerZbar();
        quickScanZbar.libInit(activity);
        surfaceHolder = surfaceView.getHolder();
        quickScanZbar.quickScan(activity, cameraBeanZbar,new QuickScanLibraryCallbackZbar() {
                                        @Override
                                        public void onReceivedScanResult(String data) {
                                            // TODO Auto-generated method stub
                                            Message msg = new Message();
                                            msg.what = MSG_WHAT_OVER;
                                            msg.obj = data;
                                            mHandler.sendMessage(msg);
                                        } }, surfaceHolder, null);
    }

    @Override
    public void onClick(View v)
    {
        if( v.getId() == R.id.light_control_img ){
            if(bIsLight){
                closeLight();
            }
            else{
                openLight();
            }
            //light_control_tv.setText(bIsLight ? "关闭" : "打开");
            light_control_img.setImageResource((bIsLight) ? R.drawable.openlight:R.drawable.closelight);
        }
        else if( R.id.return_img_button == v.getId() ){
            finish();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        scanBarCode(BaseScanCodeActivity.this);

        TranslateAnimation animation = new TranslateAnimation(0, 0.0F, 0, 0.0F, 0, 0.0F, 2, 0.9F);

        animation.setDuration(2000);
        animation.setRepeatCount(-1);
        animation.setRepeatMode(Animation.RESTART);
        animation.setInterpolator(new LinearInterpolator());
        image_line.setAnimation(animation);
        animation.startNow();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_capture);
        surfaceView = (SurfaceView) findViewById(R.id.preview_view);
//        RelativeLayout saoyisaoRelativeLayout = (RelativeLayout)findViewById(R.id.saoyisaoRelativeLayout);
//        String s = getIntent().getStringExtra(ScanCodeTrade.SCAN_LINE_IMAGE_NAME);
//        s = getIntent().getStringExtra(ScanCodeTrade.SCAN_SQUARE_IMAGE_NAME);
//        long timeOut = getIntent().getLongExtra(ScanCodeTrade.SCAN_OVER_TIME,60L);

        image_line = (ImageView) findViewById(R.id.image_line);

        //light_control_tv = (TextView) findViewById(R.id.light_control_tv);
        light_control_img = (ImageButton)findViewById(R.id.light_control_img);
        light_control_img.setOnClickListener(this);

        return_img_button = (ImageButton)findViewById(R.id.return_img_button);
        return_img_button.setOnClickListener(this);

        bottomTextView = (TextView)findViewById(R.id.bottomTextView);
        bottomTextView.setText("将取景框对准二维码，即可自动扫描");
        mHandler = new MyHandler();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler = null;
        if( animation != null ) {
            animation.cancel();
        }
        closeLight();
    }

    public void returnScanResult(String data)
    {
        Intent intent = new Intent();
        intent.putExtra("txtResult",data);
        setResult(8,intent);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_HOME:
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    public void openLight(){
        bIsLight = true;
        if( CameraManagerZbar.get() != null) {
            CameraManagerZbar.get().openLight();
        }
    }
    public void closeLight(){
        bIsLight = false;
        if( CameraManagerZbar.get() != null) {
            CameraManagerZbar.get().offLight();
        }
    }


    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_WHAT_OVER:
                    returnScanResult((String)msg.obj);
                    break;
            }
        }
    }
}
