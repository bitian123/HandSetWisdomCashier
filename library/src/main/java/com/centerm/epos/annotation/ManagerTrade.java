package com.centerm.epos.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by yuhc on 2017/10/16.
 * 管理类业务处理类的标注
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface ManagerTrade {
    //业务代码
    String value() default "";
    //校验处理类名
    String checkerClzName() default "";
}
