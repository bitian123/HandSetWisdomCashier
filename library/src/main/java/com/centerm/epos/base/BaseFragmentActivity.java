package com.centerm.epos.base;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.centerm.cloudsys.sdk.common.utils.StringUtils;
import com.centerm.epos.EposApplication;
import com.centerm.epos.R;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.fragment.FactoryModeFragment;
import com.centerm.epos.fragment.LoginFragment;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.xml.bean.RedevelopItem;
import com.centerm.epos.xml.bean.menu.Menu;
import com.centerm.epos.xml.bean.menu.MenuItem;
import com.centerm.epos.xml.bean.project.ProjectConfig;
import com.centerm.epos.xml.keys.Keys;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;

/**
 * 采用Fragment+Activity结构的基础类。提供Fragment的增、删、改等方法。
 * author:wanliang527</br>
 * date:2017/2/19</br>
 */
public abstract class BaseFragmentActivity extends BaseActivity {

    private Menu.TopViewType topViewType;
    private FragmentTransaction fragTransaction;
    protected Fragment showingFragment;

    @Override
    public boolean isOpenDataBase() {
        return true;
    }

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_base_fragment;
    }

    @Override
    public void afterInitView() {
        super.afterInitView();
        setBannerView();
        if(findViewById(R.id.hotline_show)!=null){
            TextView hotline_show = (TextView) findViewById(R.id.hotline_show);
            String hotLine = "<font><small>客服电话:</small></font><font color='#4495f1'>"+
                    BusinessConfig.getInstance().getValue(EposApplication.getAppContext(),
                            BusinessConfig.Key.HOTLINE_KEY)+"</font>";
            hotline_show.setText(Html.fromHtml(hotLine));
        }
    }

    protected void setBannerView() {
        //// TODO: 2017/2/27 二次开发点(图片资源不同) by lwl
        ImageView topBanner = (ImageView) findViewById(R.id.top_banner);
        int srcId = getDrawableId("bg_home_list");
        if (topBanner != null && srcId > 0) {
            topBanner.setImageResource(srcId);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        TextView titleShow = (TextView) findViewById(R.id.txtvw_title);
        if ((Menu.TopViewType.TITLE == topViewType || Menu.TopViewType.MIX == topViewType) && titleShow != null) {
            titleShow.setText(title);
        }
    }

    @Override
    public void setTitle(int titleId) {
        TextView titleShow = (TextView) findViewById(R.id.txtvw_title);
        if ((Menu.TopViewType.TITLE == topViewType || Menu.TopViewType.MIX == topViewType) && titleShow != null) {
            titleShow.setText(titleId);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        fragTransaction = null;
    }

    /**
     * 设置顶部视图的类型，标题栏或者横幅可供选择
     *
     * @param type 顶部视图类型
     */
    public void setTopViewType(Menu.TopViewType type) {
        logger.info("正在设置顶栏类型：" + type);
        View titleView = findViewById(R.id.layout_title);
        View bannerView = findViewById(R.id.top_banner);
        topViewType = type;
        if (type == null) {
            if (titleView != null) {
                titleView.setVisibility(View.GONE);
            }
            if (bannerView != null) {
                bannerView.setVisibility(View.GONE);
            }
        } else if (Menu.TopViewType.TITLE == type) {
            if (titleView != null) {
                titleView.setVisibility(View.VISIBLE);
            }
            if (bannerView != null) {
                bannerView.setVisibility(View.GONE);
            }
        } else if (Menu.TopViewType.BANNER == type) {
            if (titleView != null) {
                titleView.setVisibility(View.GONE);
            }
            if (bannerView != null) {
                bannerView.setVisibility(View.VISIBLE);
            }
        } else if (Menu.TopViewType.MIX == type) {
            if (titleView != null) {
                titleView.setVisibility(View.VISIBLE);
            }
            if (bannerView != null) {
                bannerView.setVisibility(View.VISIBLE);
            }
        } else {
            if (titleView != null) {
                titleView.setVisibility(View.GONE);
            }
            if (bannerView != null) {
                bannerView.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 增Fragment。{@link BaseFragmentActivity#commit()}后生效。
     *
     * @param fragment frag
     * @return 本对象
     */
    protected BaseFragmentActivity add(Fragment fragment) {
        initTransactionIfNeeded();
        fragTransaction.add(R.id.frag_container, fragment);
        showingFragment = fragment;
        return this;
    }

    /**
     * 删Fragment。{@link BaseFragmentActivity#commit()}后生效。
     *
     * @param fragment frag
     * @return 本对象
     */
    public BaseFragmentActivity remove(Fragment fragment) {
        initTransactionIfNeeded();
        fragTransaction.remove(fragment);
        if (showingFragment == fragment) {
            showingFragment = null;
        }
        return this;
    }

    /**
     * 改Fragment。{@link BaseFragmentActivity#commit()}后生效。
     *
     * @param fragment frag
     * @return 本对象
     */
    public BaseFragmentActivity replace(Fragment fragment) {
        initTransactionIfNeeded();
        fragTransaction.replace(R.id.frag_container, fragment);
        showingFragment = fragment;
        return this;
    }

    protected Fragment getShowingFragment() {
        return showingFragment;
    }

    /**
     * 将当前对栈的操作序列加入回退栈，以便后续流程回退。
     *
     * @param name 当前操作序列的名称
     * @return 本对象
     */
    public BaseFragmentActivity addToBackStack(String name) {
        initTransactionIfNeeded();
        fragTransaction.addToBackStack(name);
        return this;
    }

    private void initTransactionIfNeeded() {
        if (fragTransaction == null) {
            fragTransaction = getFragmentManager().beginTransaction();
        }
    }

    /**
     * 提交对Fragment的操作栈
     */
    public void commit() {
        try{
            if (fragTransaction != null) {
                fragTransaction.commit();
            }
            fragTransaction = null;
        }catch (IllegalStateException e){
            ViewUtils.showToast(context, "数据错误,请稍后重试");
        }

    }

    public void commitAllowingStateLoss() {
        if (fragTransaction != null) {
            fragTransaction.commitAllowingStateLoss();
        }
        fragTransaction = null;
    }

    /**
     * 加载菜单界面
     *
     * @param menuObj 菜单对象
     */
    public void loadMenuView(Menu menuObj) {
     /*   Fragment fragment = new MenuFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_MENU, menuObj);
        fragment.setArguments(bundle);
        replace(fragment).commit();*/
        loadMenuView(menuObj,null);
    }

    public void loadFragmentView(Fragment fragmentView) {
        if (fragmentView == null)
            return;
        if(showingFragment != null && (fragmentView.getClass() == showingFragment.getClass()))
            return;
        replace(fragmentView).commit();
    }

    /**
     * 如果子项目没有配置，则根据默认的MenuFragment进行加载，否则根据子项目的MenuFragment进行菜单加载，
     * @param menuObj
     * @param param
     */
    public void loadMenuView(Menu menuObj, Map<String, String> param) {
        ConfigureManager config = ConfigureManager.getInstance(context);
        RedevelopItem redevelop = config.getRedevelopItem(context, Keys.obj().redevelop_load_menu);
        if (null == redevelop) {
            Fragment fragment = new MenuFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(KEY_MENU, menuObj);
            if (param != null && param.size() > 0) {
                Iterator<Map.Entry<String, String>> iterator = param.entrySet().iterator();
                Map.Entry<String, String> entity;
                while (iterator.hasNext()) {
                    entity = iterator.next();
                    bundle.putString(entity.getKey(), entity.getValue());
                }
            }
            fragment.setArguments(bundle);
            replace(fragment).commit();
            return;
        }
        String clzName = redevelop.getClassName();
        if (!StringUtils.isStrNull(clzName)) {
            try {
                Class clz = Class.forName(clzName);
                Object obj = clz.newInstance();
                if (obj instanceof IloadMenuView) {
                    ((IloadMenuView) obj).loadMenuView(menuObj,param,this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 加载绿城首页
     */
    public void loadGTMenuView(Menu menuObj, Map<String, String> param) {
        Fragment fragment = new GTMenuFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_MENU, menuObj);
        if (param != null && param.size() > 0) {
            Iterator<Map.Entry<String, String>> iterator = param.entrySet().iterator();
            Map.Entry<String, String> entity;
            while (iterator.hasNext()) {
                entity = iterator.next();
                bundle.putString(entity.getKey(), entity.getValue());
            }
        }
        fragment.setArguments(bundle);
        replace(fragment).commit();
    }

    /**
     * 加载绿城系统设置界面
     */
    public void loadGTSysMenuView() {
        Fragment fragment = new GtSysMenuFragment();
        replace(fragment).commit();
    }

    /**
     * 加载绿城首页
     */
    public void loadLoginView() {
        Fragment fragment = new LoginFragment();
        replace(fragment).commit();
    }

    /**
     * 加载工厂模式的菜单界面
     */
    protected void loadFactoryMenuView() {
        Menu factoryMenu = getFactoryMenu();
        Fragment fragment = new FactoryModeFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_MENU, factoryMenu);
        fragment.setArguments(bundle);
        replace(fragment).commit();
    }

    private Menu getFactoryMenu() {
        return ConfigureManager.getInstance(this).getFactoryMenu();
    }

    private MenuItem getFactoryMenu(Menu menu, String prjTag) {
        if (menu == null || TextUtils.isEmpty(prjTag))
            return null;
        List<MenuItem> menuItems = menu.getItemList();
        if (menuItems == null || menuItems.size() == 0)
            return null;
        for (MenuItem item : menuItems) {
            if (prjTag.equalsIgnoreCase(item.getEnTag()))
                return item;
        }
        return null;
    }

    /**
     * 当只有一个选项时，返回该选项内容；否则返回为空。
     *
     * @return 菜单项内容
     */
    protected MenuItem getSingleFactoryItem() {
        Menu factoryMenu = getFactoryMenu();
        if (factoryMenu.getCounts() == 1)
            return factoryMenu.getItem(0);
        ProjectConfig projectConfig = ConfigureManager.getInstance(this).getProjectConfig();
        if (projectConfig != null && !TextUtils.isEmpty(projectConfig.getDefaultPrjTag()))
            return getFactoryMenu(factoryMenu, projectConfig.getDefaultPrjTag());
        return null;
    }
}
