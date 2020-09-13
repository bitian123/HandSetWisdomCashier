package com.centerm.epos.ebi;

import com.centerm.epos.EposApplication;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.ebi.redevelop.BaseSaveLogo;
import com.centerm.epos.redevelop.ISaveLogo;
import com.centerm.epos.utils.CrashHandler;

/**
 * Created by liubit on 2017/12/24.
 */

public class App extends EposApplication{

    @Override
    public void onCreate() {
        super.onCreate();

        //导入打印凭条LOGO
        ISaveLogo saveLogo = (ISaveLogo) ConfigureManager.getInstance(this).getSubPrjClassInstance(new
                BaseSaveLogo());
        saveLogo.save(this);
        CrashHandler.getInstance().init(this);


    }

}
