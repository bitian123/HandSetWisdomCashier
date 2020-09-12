package com.centerm.epos.task;

import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.widget.Toast;

import com.centerm.epos.R;
import com.centerm.epos.function.TerminalParameter;
import com.centerm.epos.utils.DialogFactory;

/**
 * 导入参数
 */

public class AsyncImportParameterTask extends BaseAsyncTask {

    private TerminalParameter mTerminalParameter;

    public AsyncImportParameterTask(Context context) {
        super(context);
        mTerminalParameter = new TerminalParameter();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        DialogFactory.showLoadingDialog(context, context.getString(R.string.tip_import_parameter), new DialogInterface
                .OnKeyListener(){

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_BACK){
                    if(!mTerminalParameter.stopImport()){
                        Toast.makeText(context, "正在导入终端参数，请稍候！", Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            }
        });
    }

    @Override
    protected Object doInBackground(Object[] params) {
        return mTerminalParameter.importParameter();
    }

    @Override
    public void onFinish(Object o) {
        super.onFinish(o);
        DialogFactory.hideAll();
        if (o instanceof Boolean && (Boolean) o) {
            Toast.makeText(context, "终端参数导入成功！", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, mTerminalParameter.isTerminalByUser() ? "取消终端参数导入" : "终端参数导入失败！", Toast
                    .LENGTH_SHORT).show();
        }
    }
}
