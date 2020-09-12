package com.centerm.epos.mvp.model;

import com.centerm.epos.db.DbHelper;

/**
 * author:wanliang527</br>
 * date:2017/3/2</br>
 */

public interface IVerifyPwdBiz {

    String get00Pwd(DbHelper dbHelper);

    String get99Pwd(DbHelper dbHelper);



}
