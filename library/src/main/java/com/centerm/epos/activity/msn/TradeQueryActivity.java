package com.centerm.epos.activity.msn;

import android.content.Intent;
import android.view.View;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseActivity;
import com.centerm.epos.base.TradeFragmentContainer;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.model.BaseTradeParameter;
import com.centerm.epos.model.ITradeParameter;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.xml.bean.process.TradeProcess;

/**
 * 交易查询
 * Created by liubit on 2019/9/5.
 */
public class TradeQueryActivity extends BaseActivity implements View.OnClickListener {

    @Override
    public int onLayoutId() {
        return R.layout.activity_query_trade_record;
    }

    @Override
    public void onInitView() {
        initBackBtn();

        findViewById(R.id.mBtn_abnormal).setOnClickListener(this);
        findViewById(R.id.mBtn_revoke).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if(CommonUtils.isFastClick()){
            return;
        }
        if( v.getId() == R.id.mBtn_abnormal){
            Intent intent = new Intent(TradeQueryActivity.this, AbnormalQueryTradeActivity.class);
            startActivity(intent);
        }else if( v.getId() == R.id.mBtn_revoke){
            startCancel();
        }
    }


    public void startCancel(){
        ConfigureManager config = ConfigureManager.getInstance(TradeQueryActivity.this);
        TradeProcess tradeProcess = config.getTradeProcess(TradeQueryActivity.this, "void.xml");
        if (tradeProcess == null) {
            logger.warn(tradeProcess + "流程未定义！");
            return;
        }
        Intent intent = new Intent(TradeQueryActivity.this, TradeFragmentContainer.class);
        intent.putExtra(BaseActivity.KEY_TRANSCODE, TransCode.VOID);
        intent.putExtra(BaseActivity.KEY_PROCESS, tradeProcess);
        ITradeParameter parameter = (ITradeParameter) ConfigureManager.getSubPrjClassInstance(new BaseTradeParameter());
        intent.putExtra(ITradeParameter.KEY_TRANS_PARAM, parameter.getParam(TransCode.VOID));
        startActivityForResult(intent, REQ_TRANSACTION);
    }

}
