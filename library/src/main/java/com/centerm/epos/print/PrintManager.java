package com.centerm.epos.print;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.centerm.epos.EposApplication;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.xml.bean.slip.SlipElement;

import org.apache.log4j.Logger;
import java.util.List;

/**
 * 打印管理类。所有打印类相关功能的统一入口
 * author:wanliang527</br>
 * date:2017/2/15</br>
 */
public class PrintManager {

    private Logger logger = Logger.getLogger(this.getClass());

    private Context context;
    private ConfigureManager configManager;
    private CommonDao<SlipElement> dao;

    public PrintManager(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("PrintManager need a context that cannot be null!");
        }
        this.context = context;
        configManager = ConfigureManager.getInstance(context);
        dao = new CommonDao<>(SlipElement.class, DbHelper.getInstance());
    }

    /**
     * 导入默认模板到数据库中
     *
     * @return 导入成功返回true，否则返回false
     */
    public boolean importTemplate() {
        List<SlipElement> slipElements = configManager.getSlipTemplate(context);
        dao.deleteBySQL("DELETE FROM tb_slip_template");
        boolean result = dao.save(slipElements);
        return result;
    }

    /**
     * 检查打印模板版本是否有变更，如果有变更则重新导入数据库
     */
    public void checkTemplateVersion(){
        List<SlipElement> slipElements = dao.query();
        if (slipElements == null || slipElements.size() == 0){
            logger.debug("^_^ 打印模板为空，从配置文件导入 ^_^");
            importTemplate();
            return;
        }
        List<SlipElement> slipElementXml = configManager.getSlipTemplate(context);
        if(slipElementXml.get(0).getVersion() != slipElements.get(0).getVersion()){
            logger.debug("^_^ 打印模板版本号不一致，从配置文件导入 ^_^");
            importTemplate();
        }
        logger.debug("^_^ 打印模板版本检查完成，版本号：" + slipElementXml.get(0).getVersion() + " ^_^");
    }

    public boolean isTemplateEmpty(){
        List<SlipElement> slipElements = getTemplate();
        return slipElements == null || slipElements.size() == 0;
    }

    private List<SlipElement> getTemplate() {
        return dao.query();
    }

    private List<SlipElement> getTemplate(String slipTag){
        if (TextUtils.isEmpty(slipTag))
            return null;
        Context context = EposApplication.getAppContext();
        return ConfigureManager.getInstance(context).getSlipTemplate(context, slipTag);
    }

    /**
     * 准备开始打印，所有的赋值和格式编辑操作将在代理类中进行
     *
     * @param owner 签购单所属（商户联或持卡人联）
     * @return 打印代理
     */
    public PrinterProxy prepare(SlipOwner owner) {
        if (owner == null) {
            owner = SlipOwner.MERCHANT;
        }
        return new PrinterProxy(context, owner, getTemplate());
    }

    public PrinterProxy prepare(SlipOwner owner, String slipTag) {
        if (owner == null) {
            owner = SlipOwner.MERCHANT;
        }
        return new PrinterProxy(context, owner, getTemplate(slipTag));
    }

    /**
     * 开始编辑签购单模板。
     *
     * @return 签购单模板编辑器
     */
    public TemplateEditor edit() {
        return new TemplateEditor(context);
    }

    /**
     * 签购单持有者，商户或者顾客
     */
    public enum SlipOwner {
        MERCHANT, CONSUMER
    }

    /**
     * 打印状态拦截器
     */
    public interface StatusInterpolator {

        //打印中
        void onPrinting();

        //打印结束
        void onFinish();

        //打印错误
        void onError(int errorCode, String errorInfo);
    }
}
