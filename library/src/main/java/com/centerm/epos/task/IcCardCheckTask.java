package com.centerm.epos.task;

import android.content.Context;
import android.widget.Toast;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.define.IIcCardDev;
import com.centerm.epos.R;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.utils.XLogUtil;

/**
 * Created by zhouzhihua on 2018/1/9.
 */

public class IcCardCheckTask extends BaseAsyncTask<String,String[]> {
    private Toast toast;
    private IIcCardDev iIcCardDev;
    public IcCardCheckTask(Context context){
        super(context);
    }

    @Override
    public void onProgress(Integer counts, Integer index) {
        if( toast == null ){
            toast = ViewUtils.TipToast.makeText(context, R.string.tip_remove_contact_card,Toast.LENGTH_SHORT);
            toast.show();
        }
        else{
            toast.show();
        }
    }

    @Override
    public void onStart() {
        if( toast == null ){
            toast = ViewUtils.TipToast.makeText(context, R.string.tip_remove_contact_card,Toast.LENGTH_SHORT);
            toast.show();
        }
        else{
            toast.show();
        }
    }

    @Override
    public void onFinish(String[] status) {
    }

    @Override
    protected String[] doInBackground(String... strings) {
        try {
            iIcCardDev = DeviceFactory.getInstance().getIcCardDev();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if( iIcCardDev!= null ) {

            iIcCardDev.open();
            while (iIcCardDev.getStatus()) {
                try {
                    Thread.sleep(2000);
                }catch (InterruptedException e){
                    break;
                }
                publishProgress(1);
            }
            iIcCardDev.close();
        }
        if(toast!=null) toast.cancel();
        toast = null;
        return new String[0];
    }

    @Override
    protected void onCancelled() {
        XLogUtil.w("IcCardCheckTask","IcCardCheckTask onCancelled");
        super.onCancelled();
        if( iIcCardDev!=null ){
            iIcCardDev.close();
        }
        if(toast!=null) toast.cancel();

        toast = null;
        iIcCardDev = null;
    }
}
