package com.qiyi.biz;

import android.content.Context;

import com.qiyi.xhook.XHook;

import org.json.JSONObject;

/**
 * Created by caikelun on 18/01/2018.
 */

public class Biz {
    private static final Biz ourInstance = new Biz();
    private static boolean inited = false;

    public static Biz getInstance() {
        return ourInstance;
    }

    private Biz() {
    }

    public synchronized boolean init(Context ctx) {
        if(inited) {
            return true;
        }

        inited = XHook.getInstance().init(ctx);
        return inited;
    }

    public synchronized boolean isInited() {
        return inited;
    }

    public synchronized void start(JSONObject obj) {
        com.qiyi.biz.NativeHandler.getInstance().start(obj);
    }

    public synchronized void test() {
        com.qiyi.biz.NativeHandler.getInstance().test();
    }
}
