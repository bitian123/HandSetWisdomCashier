package com.centerm.component.pay.mvp.model;

import android.os.Bundle;

/**
 * author:wanliang527</br>
 * date:2017/3/8</br>
 */

public interface IPayEntryModel {
    
    Bundle newBundle(String respCode, String respMsg);

    boolean isResultOk(String s);

    String[] onCheckParams(Bundle param);

    String transCodeMapping(String string);

    String processFileMapping(String transCode);

    int transCodeMappingJBoss(String transCode);
}
