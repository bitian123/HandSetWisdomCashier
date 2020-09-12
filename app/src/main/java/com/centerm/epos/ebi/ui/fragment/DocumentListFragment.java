package com.centerm.epos.ebi.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.centerm.epos.EposApplication;
import com.centerm.epos.adapter.ObjectBaseAdapter;
import com.centerm.epos.base.BaseTradeFragment;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.base.ReceivedQueryBean;
import com.centerm.epos.bean.PrintReceiptBean;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.ebi.R;
import com.centerm.epos.ebi.msg.GetRequestData;
import com.centerm.epos.ebi.present.DocumentPresent;
import com.centerm.epos.transcation.pos.constant.JsonKeyGT;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DataHelper;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.OkHttpUtils;
import com.centerm.epos.utils.OnCallListener;
import com.centerm.epos.utils.OnConfirmListener;
import com.centerm.epos.view.AlertDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;
import config.Config;

/**
 * 单据打印列表
 * author:liubit</br>
 * date:2019/9/5</br>
 */
public class DocumentListFragment extends BaseTradeFragment implements View.OnClickListener {
    private ImageView mIvTip;
    private CheckBox mCheckBox;
    private ListView mListView;
    private List<ReceivedQueryBean.QueryListsBean> list = new ArrayList<>();
    private DocumentAdapter adapter;
    private TextView mTvPrintNum,mTvRoomId;
    private RelativeLayout mRlTotal;
    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener;
    private int printNum = 0;
    private ReceivedQueryBean data;
    private DocumentPresent present;
    private List<String> printList = new ArrayList<>();
    private int total = 0;
    private int successNum = 0;
    private int failNum = 0;

    @Override
    protected ITradePresent newTradePresent() {
        present = new DocumentPresent(this);
        return present;
    }

    @Override
    protected void onInitView(View view) {
        initFinishBtnlistener(view);
        mCheckBox = (CheckBox) view.findViewById(R.id.mCheckBox);
        mListView = (ListView) view.findViewById(R.id.mListView);
        mRlTotal = (RelativeLayout) view.findViewById(R.id.mRlTotal);
        mTvPrintNum = (TextView) view.findViewById(R.id.mTvPrintNum);
        mTvRoomId = (TextView) view.findViewById(R.id.mTvRoomId);

        //非身份证验证进入
        if(mTradePresent.getTransData().get("isOther")!=null){
            mIvTip = (ImageView) view.findViewById(R.id.mIvTip);
            mIvTip.setBackground(getActivity().getResources().getDrawable(R.drawable.print_step2));
        }

        view.findViewById(R.id.mBtnPrint).setOnClickListener(this);
        view.findViewById(R.id.mBtnPrintMore).setOnClickListener(this);

        onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    mRlTotal.setVisibility(View.VISIBLE);
                }else {
                    mRlTotal.setVisibility(View.GONE);
                }
                if(list!=null&&list.size()>0){
                    for(int i=0;i<list.size();i++){
                        list.get(i).setChecked(b);
                    }
                }else {
                    mRlTotal.setVisibility(View.GONE);
                }
                updateList();
            }
        };
        mCheckBox.setOnCheckedChangeListener(onCheckedChangeListener);

        receivedQuery();
    }

    private void initListView(){
        mTvRoomId.setText(data.getProjectName());
        if(data.getQueryLists()!=null&&data.getQueryLists().size()>0){
            list = data.getQueryLists();
        }

        adapter = new DocumentAdapter(getActivity());
        adapter.addAll(list);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                for(int j=0;j<list.size();j++){
                    if(i!=j) {
                        list.get(j).setChecked(false);
                    }
                }
                boolean pre = list.get(i).isChecked();
                list.get(i).setChecked(!pre);
                updateList();
            }
        });
    }

    private void receivedQuery(){
        mTradePresent.getTransData().put(JsonKeyGT.projectId, BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), BusinessConfig.Key.PROJECT_ID));
        mTradePresent.getTransData().put(JsonKeyGT.termSn, GetRequestData.getSn());
        sendData(true, TransCode.receivedQuery, mTradePresent.getTransData(), new OnCallListener() {
            @Override
            public void onCall(Map<String, Object> result) {
                if(result!=null){
                    data = (ReceivedQueryBean) result.get(JsonKeyGT.returnData);
                    if("0".equals(data.getRespCode())){
                        initListView();
                    }else {
                        popToast(data.getRespMsg());
                    }
                }else {
                    popToast("通讯异常，请重试");
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (CommonUtils.isFastClick())
            return;
        if (v.getId() == R.id.mBtnPrint) {
            repeatPrintReceipt(1);
        }else if(v.getId() == R.id.mBtnPrintMore){
            editPrintNum();
        }else {
            super.onClick(v);
        }
    }

    public void repeatPrintReceipt(final int num){
        successNum = 0;
        failNum = 0;
        printList.clear();
        String orderNo = "";
        for(ReceivedQueryBean.QueryListsBean bean:list){
            if(bean.isChecked()){
                orderNo = bean.getMainOrderId();
            }
        }
        if(TextUtils.isEmpty(orderNo)){
            popToast("请选择需要打印的数据");
            return;
        }
        Map<String,Object> dataMap = new HashMap<>();
        dataMap.put(JsonKeyGT.orderNo, orderNo);
        DialogFactory.showLoadingDialog(getActivity(), "正在获取回执单\n请稍侯");
        sendData(false, TransCode.repeatPrintReceipt, dataMap, new OnCallListener() {
            @Override
            public void onCall(Map<String, Object> result) {
                if(result!=null){
                    PrintReceiptBean bean = (PrintReceiptBean) result.get(JsonKeyGT.returnData);
                    if("0".equals(bean.getRespCode())){
                        getPDF(bean.getBody(),num);
                    }else {
                        popToast(bean.getRespMsg());
                    }
                }else {
                    popToast("通讯异常，请重试");
                }
            }
        });
    }

    private void editPrintNum(){
        DialogFactory.showEditPrintNumDialog(getActivity(), new OnConfirmListener(){
            @Override
            public void onConfirm(Object object) {
                String numStr = (String) object;
                int num = Integer.parseInt(numStr);
                repeatPrintReceipt(num);
            }
        });
    }

    public void getPDF_old(List<PrintReceiptBean.BodyBean> bodyBeans,int num){
        printList.clear();
        total = bodyBeans.size();
        boolean hasPdf = false;
        for(PrintReceiptBean.BodyBean bodyBean:bodyBeans){
            if(TextUtils.isEmpty(bodyBean.getUrl())||"null".equals(bodyBean.getUrl())){
                failNum++;
                continue;
            }else {
                String suffix = ".pdf";
                try {
                    String[] strs = bodyBean.getImage().split("\\.");
                    if(strs!=null&&strs.length>1){
                        suffix = "."+strs[strs.length-1];
                    }
                }catch (Exception e){
                    logger.error(e.toString());
                }
                printPDF(bodyBean.getSubOrderId() + suffix, bodyBean.getImage(),num);
                hasPdf = true;
            }
        }
        if(hasPdf){
            DialogFactory.showLoadingDialog(getActivity(), "正在下载单据\n请稍侯");
        }else {
            popToast("无可打印单据");
        }
    }

    public void getPDF(List<PrintReceiptBean.BodyBean> bodyBeans,int num){
        printList.clear();
        total = 0;
        boolean hasPdf = false;
        for(PrintReceiptBean.BodyBean bodyBean:bodyBeans){
            for(int i=0;i<bodyBean.getPrint().size();i++){
                total++;
                PrintReceiptBean.BodyBean.PrintBean printBean = bodyBean.getPrint().get(i);
                String suffix = ".pdf";
                try {
                    String[] strs = printBean.getImage().split("\\.");
                    if(strs!=null&&strs.length>1){
                        suffix = "."+strs[strs.length-1];
                    }
                }catch (Exception e){
                    logger.error(e.toString());
                }
                printPDF(bodyBean.getSubOrderId()+"_print_"+(i+1)+suffix,printBean.getImage(),num);
                hasPdf = true;
            }
        }
        if(hasPdf){
            DialogFactory.showLoadingDialog(getActivity(), "正在下载单据\n请稍侯");
        }else {
            popToast("无可打印单据");
        }
    }

    private void printPDF(final String fileName, final String url,final int num){
        File file = new File(Config.Path.PDF_PATH, fileName);
        OkHttpUtils.getInstance().downloadFile(file, url, new OnCallListener() {
            @Override
            public void onCall(Map<String, Object> result) {
                try {
                    if(result!=null){
                        successNum++;
                        logger.info(fileName+" -> 下载完成 -> "+result.get("path"));
                        printList.add((String) result.get("path"));
                    }else {
                        failNum++;
                        logger.error("单据下载失败，请重试");
                    }
                    logger.info("下载成功："+successNum);
                    logger.info("下载失败："+failNum);
                    logger.info("下载总量："+total);
                    if(successNum+failNum==total){
                        DialogFactory.hideAll();
                        if(successNum>0){
                            present.addPrintTask(printList,num);
                            present.print();
                        }else {
                            popToast("无可打印单据");
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    popToast("通讯异常");
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle("单据打印");

        if(list!=null&&list.size()>0){
            for(int i=0;i<list.size();i++){
                list.get(i).setChecked(false);
            }
        }
        mRlTotal.setVisibility(View.GONE);
        updateList();

        if(present.getPrintList().size()>0){
            present.print();
        }
    }

    @Override
    public boolean onBacKeyPressed() {
        logger.info("实体键返回");
        tipToExit();
        return true;
    }

    private void tipToExit(){
        DialogFactory.showSelectDialog(getActivity(), getString(com.centerm.epos.R.string.tip_notification), "确认退出业务？", new AlertDialog
                .ButtonClickListener() {
            @Override
            public void onClick(AlertDialog.ButtonType button, View v) {
                switch (button) {
                    case POSITIVE:
                        getHostActivity().finish();
                        break;
                }
            }
        });
    }

    private void updateList(){
        if(list!=null&&list.size()>0){
            boolean isAll = true;
            boolean showRl = false;
            int n = 0;
            for(ReceivedQueryBean.QueryListsBean bean : list){
                if(bean.isChecked()){
                    n++;
                    showRl = true;
                }else {
                    isAll = false;
                }
            }
            printNum = n;
            mTvPrintNum.setText(printNum+"笔");
            if(!isAll){
                mCheckBox.setOnCheckedChangeListener(null);
                mCheckBox.setChecked(false);
                mCheckBox.setOnCheckedChangeListener(onCheckedChangeListener);
            }
            if(showRl){
                mRlTotal.setVisibility(View.VISIBLE);
            }else {
                mRlTotal.setVisibility(View.GONE);
            }
            adapter.notifyDataSetChanged();
        }
    }

    private class DocumentAdapter extends ObjectBaseAdapter<ReceivedQueryBean.QueryListsBean> {
        public DocumentAdapter(Context mCtx) {
            super(mCtx);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ReceivedQueryBean.QueryListsBean bean = getItem(position);
            final ViewHolder holder;
            if(convertView==null){
                int layoutId = R.layout.v_document_item;
                convertView = getHostActivity().getLayoutInflater().inflate(layoutId, null);
                holder = new ViewHolder();
                holder.mTvSettlement = (TextView) convertView.findViewById(R.id.mTvSettlement);
                holder.mMoneyType = (TextView) convertView.findViewById(R.id.mMoneyType);
                holder.mBillId = (TextView) convertView.findViewById(R.id.mBillId);
                holder.mName = (TextView) convertView.findViewById(R.id.mName);
                holder.mTvPayTime = (TextView) convertView.findViewById(R.id.mTvPayTime);
                holder.mTvPaidAmt = (TextView) convertView.findViewById(R.id.mTvPaidAmt);
                holder.mCbItem = (CheckBox) convertView.findViewById(R.id.mCbItem);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.mTvPayTime.setText("付款日期: "+bean.getPayDate());
            holder.mBillId.setText("款项名称: "+bean.getPaymentItemName());
            holder.mName.setText("票据号码: "+bean.getBillCode());
            holder.mTvSettlement.setText("打印次数: "+bean.getPrintTime());
            holder.mTvPaidAmt.setText("付款金额: "+DataHelper.saved2Decimal(bean.getAmountReceived())+"元");
            holder.mCbItem.setChecked(bean.isChecked());
            return convertView;
        }
    }

    private static class ViewHolder {
        TextView mTvSettlement;
        TextView mMoneyType;
        TextView mBillId;
        TextView mName;
        TextView mTvPayTime;
        TextView mTvPaidAmt;
        CheckBox mCbItem;
    }

    @Override
    public int onLayoutId() {
        return R.layout.fragment_document_list;
    }


}
