package com.spark.xposeddy.xposed.phone;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.spark.xposeddy.persist.IPersist;
import com.spark.xposeddy.persist.PersistKey;
import com.spark.xposeddy.persist.impl.PersistFactory;
import com.spark.xposeddy.util.FileUtil;
import com.spark.xposeddy.util.ShellFileUtil;
import com.spark.xposeddy.util.ShellUtil;
import com.spark.xposeddy.util.StringUtil;
import com.spark.xposeddy.util.TraceUtil;
import com.spark.xposeddy.xposed.HttpHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class NewPhoneThread extends Thread {
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Context context;
    private String packName;
    private IPersist persist;
    private Runnable runnable;

    public NewPhoneThread(Context context, String packName, IPersist persist, Runnable runnable) {
        super();
        this.context = context;
        this.persist = persist;
        this.packName = packName;
        this.runnable = runnable;
    }

    @Override
    public void run() {
        super.run();
        Boolean isAutoAirplane = (Boolean) PersistFactory.getInstance(this.context).readData(PersistKey.AUTO_AIRPLANE_STATUS, false);

        // 获取一键新机参数
        String deviceNum = (String) persist.readData(PersistKey.DEVICE_ID, "");
        PhoneInfo info = PhoneMgr.newPhoneInfo(this.context, deviceNum);
        persist.writeData(PersistKey.PHONE_INFO, (info == null ? "" : JSON.toJSONString(info)));
        persist.writeData(PersistKey.PHONE_INFO_TICK, String.valueOf(System.currentTimeMillis()));

        // 打开飞行模式
        if (isAutoAirplane) {
            TraceUtil.e("setAirPlaneMode true");
            ShellUtil.setAirPlaneMode(true);
        }

        // 退出app
        TraceUtil.e("exitApp start tick: " + System.currentTimeMillis() / 1000);
        ShellUtil.exitApp(packName);
        TraceUtil.e("exitApp end tick: " + System.currentTimeMillis() / 1000);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 关闭飞行模式
        if (isAutoAirplane) {
            TraceUtil.e("setAirPlaneMode false");
            ShellUtil.setAirPlaneMode(false);
        }

        // 清除app数据
        TraceUtil.e("clearAppData start tick: " + System.currentTimeMillis() / 1000);
        clearAppData(packName);
        TraceUtil.e("clearAppData end tick: " + System.currentTimeMillis() / 1000);
        ShellUtil.exitApp(packName);

        // 等待网络恢复正常
        waitAvailableNet();

        mHandler.post(() -> {
            if (this.runnable != null) {
                this.runnable.run();
            }
        });
    }

    private boolean clearAppData(String packageName) {
        String baseInner = "/data/user/0/" + packageName + "/";
        String baseExternal = "/storage/emulated/0/Android/data/" + packageName + "/";
        ShellFileUtil.deleteFile(new File(baseExternal));

        // 删除com.snssdk.api
        String snssdkPath = baseExternal.replace(packageName, "com.snssdk.api");
        FileUtil.deleteFile(new File(snssdkPath));

        File baseInnerFile = new File(baseInner);
        if (ShellFileUtil.exists(baseInnerFile)) {
            List<File> fileList = ShellFileUtil.listFiles(new File(baseInner));
            for (File file : fileList) {
                if (!file.getPath().contains("lib ->")) {
                    ShellFileUtil.deleteFile(file);
                }
            }
        }

        return true;
    }

    private void waitAvailableNet() {
        int cnt = 0;
        do {
            String result = HttpHelper.getStr("http://pv.sohu.com/cityjson?ie=utf-8", null);
            if (!TextUtils.isEmpty(result)) {
                String json = "{" + StringUtil.getMidText(result, "{", "}") + "}";
                try {
                    JSONObject jsObj = new JSONObject(json);
                    String ip = jsObj.optString("cip");
                    if (!TextUtils.isEmpty(ip)) {
                        TraceUtil.e("net available, ip = " + ip);
                        break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (++cnt < 6); // 最多执行6次

        if (cnt >= 6) {
            TraceUtil.e("wait net overtime");
        }
    }

}
