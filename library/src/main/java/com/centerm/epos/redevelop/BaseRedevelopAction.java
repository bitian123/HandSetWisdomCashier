package com.centerm.epos.redevelop;

/**
 * Created by yuhc on 2017/11/1.
 * 基础二次开发，什么都不实现，用于子项目的继承，子项目只需要重写需要的方法。
 */

public class BaseRedevelopAction implements IRedevelopAction {
    @Override
    public Object doAction() {
        return null;
    }

    @Override
    public Object doAction(Object parameterObj) {
        return null;
    }

    @Override
    public Object doAction(Object parameter1Obj, Object parameter2Obj) {
        return null;
    }

    @Override
    public Object doAction(Object parameter1Obj, Object parameter2Obj, Object parameter3Obj) {
        return null;
    }
}
