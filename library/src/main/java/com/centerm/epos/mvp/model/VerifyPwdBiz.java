package com.centerm.epos.mvp.model;

import com.centerm.epos.EposApplication;
import com.centerm.epos.bean.Employee;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.DbHelper;

import org.apache.log4j.Logger;

import config.BusinessConfig;
import config.Config;

/**
 * author:wanliang527</br>
 * date:2017/3/2</br>
 */
public class VerifyPwdBiz implements IVerifyPwdBiz {

    private Logger logger = Logger.getLogger(VerifyPwdBiz.class);

    @Override
    public String get00Pwd(DbHelper dbHelper) {
        CommonDao<Employee> dao = new CommonDao<>(Employee.class, dbHelper);
        Employee e = dao.queryForId(Config.DEFAULT_MSN_ACCOUNT);
        if (e == null) {
            logger.warn("数据库中无法找到主管账户信息");
            return Config.DEFAULT_MSN_PWD;
        } else {
            return e.getPassword();
        }
    }

    @Override
    public String get99Pwd(DbHelper dbHelper) {
        CommonDao<Employee> dao = new CommonDao<>(Employee.class, dbHelper);
        Employee e = dao.queryForId(Config.DEFAULT_ADMIN_ACCOUNT);
        if (e == null) {
            logger.warn("数据库中无法找到系统管理员账户信息");
//            return Config.DEFAULT_ADMIN_PWD;
            return BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), BusinessConfig.Key.OPERATOR_MANAGER_PWD);
        } else {
            return e.getPassword();
        }
    }
}
