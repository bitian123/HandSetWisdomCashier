package com.centerm.epos.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by yuhc on 2017/10/16.
 * 收单POS的8583报文数据域处理类的标注
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface RedevelopAction {

    //二次开发标识
    String value() default "";

    //索引，目前没有用
    int index() default 0;
}
