package com.centerm.epos.security;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.centerm.cpay.securitysuite.aidl.IVirtualPinPad;
import com.centerm.epos.EposApplication;
import com.centerm.epos.utils.XLogUtil;

/**
 * Created by yuhc on 2017/3/23.
 */

public class CpaySecurityTool {
    private static final String TAG = CpaySecurityTool.class.getSimpleName();

    private IVirtualPinPad iVirtualPinPad;
    private static CpaySecurityTool instance = null;

    private CpaySecurityTool(){
    }

    public static CpaySecurityTool getInstance() {
        synchronized (CpaySecurityTool.class) {
          if (instance == null) {
              instance = new CpaySecurityTool();
          }
      }
      return instance;
    }

    public void initRuntime(Context context){
        Intent intent = new Intent("com.centerm.cpay.securitysuite.AIDL_SERVICE");
        intent.setPackage("com.centerm.cpay.securitysuite");
        context.bindService(intent, new CpaySecurityConnect(), Context.BIND_AUTO_CREATE);
    }

    public IVirtualPinPad getIVirtualPinPad() {
        return iVirtualPinPad;
    }

    private class CpaySecurityConnect implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            XLogUtil.d(TAG, "^_^ 安全套件绑定成功 ^_^");
            iVirtualPinPad = IVirtualPinPad.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            XLogUtil.d(TAG, "^_^ 安全套件断开连接 ^_^");
        }
    }
}
