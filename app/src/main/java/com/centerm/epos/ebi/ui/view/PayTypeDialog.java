package com.centerm.epos.ebi.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.centerm.epos.adapter.ObjectBaseAdapter;
import com.centerm.epos.ebi.R;
import com.centerm.epos.ebi.common.PayTypeEnum;

import org.apache.log4j.Logger;

import static com.centerm.epos.ebi.common.PayTypeEnum.listPayType;
import static com.centerm.epos.ebi.common.PayTypeEnum.listPayTypeScan;


/**
 * Created by FL on 2017/9/22 09:40.
 * 选择支付方式对话框
 */

public class PayTypeDialog extends Dialog implements View.OnClickListener {
    protected Logger logger = Logger.getLogger(this.getClass());
    private Context context;
    private ListView listView;
    private boolean hasCard = true;
    private boolean hasScanCommon = true;

    public PayTypeDialog(Context context) {
        super(context);
        this.context = context;
        init();
    }
    public PayTypeDialog(Context context, boolean hasCard) {
        super(context);
        this.context = context;
        this.hasCard = hasCard;
        init();
    }
    public PayTypeDialog(Context context, boolean hasCard, boolean hasScanCommon) {
        super(context);
        this.context = context;
        this.hasCard = hasCard;
        this.hasScanCommon = hasScanCommon;
        init();
    }

    private void init(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_pay_type);
        PayTypeAdapter adapter = new PayTypeAdapter(context);
        logger.debug("hasCard=>"+hasCard);
        adapter.addAll(hasCard?listPayType():listPayTypeScan(hasScanCommon));
        listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        this.setCanceledOnTouchOutside(false);
    }


    @Override
    public void onClick(View view) {

    }

    public PayTypeDialog setOnCancel(OnCancelListener listener){
        this.setOnCancelListener(listener);
        return this;
    }

    public PayTypeDialog setItemOnClick(AdapterView.OnItemClickListener listener){
        listView.setOnItemClickListener(listener);
        return this;
    }

    private class PayTypeAdapter extends ObjectBaseAdapter<PayTypeEnum> {

        public PayTypeAdapter(Context mCtx) {
            super(mCtx);
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            PayTypeEnum data = getItem(i);
            int layoutId = R.layout.v_pay_type_item;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(layoutId, null);
            }
            ImageView payImage = (ImageView) convertView.findViewById(R.id.pay_type_image);
            TextView payText = (TextView) convertView.findViewById(R.id.pay_type_text);
            payImage.setImageDrawable(context.getResources().getDrawable(data.getRes()));
            payText.setText(data.getName());
            return convertView;
        }
    }

}
