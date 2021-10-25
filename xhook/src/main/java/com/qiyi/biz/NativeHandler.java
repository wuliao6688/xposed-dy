package com.qiyi.biz;

import org.json.JSONObject;

/**
 * Created by caikelun on 18/01/2018.
 */

public class NativeHandler {
    private static final NativeHandler ourInstance = new NativeHandler();

    public static NativeHandler getInstance() {
        return ourInstance;
    }

    private NativeHandler() {
    }

    public native void start(JSONObject obj);

    /**
     * 测试native hook 功能
     */
    public native void test();
}
