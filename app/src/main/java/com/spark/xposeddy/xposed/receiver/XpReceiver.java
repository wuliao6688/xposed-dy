package com.spark.xposeddy.xposed.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;

import com.spark.xposeddy.floatwindow.FloatWindowMgr;
import com.spark.xposeddy.persist.impl.PersistFactory;
import com.spark.xposeddy.util.JSONObjectPack;
import com.spark.xposeddy.util.LaunchUtil;
import com.spark.xposeddy.util.Shell;
import com.spark.xposeddy.util.TraceUtil;
import com.spark.xposeddy.xposed.HookMain;
import com.spark.xposeddy.xposed.phone.NewPhoneThread;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * XpBroadcast广播的接收器
 */
public class XpReceiver extends BroadcastReceiver {

    // 服务器已更新采集达人、视频信息广播
    public static final String RECEIVER_NEW_TASK_ACTION = "dy.aweme.new.task";
    // 向上滑动广播
    public static final String RECEIVER_SWIPE_UP_CMD = "dy.aweme.swipe.up.cmd";
    // 日志广播
    public static final String RECEIVER_LOGS_ACTION = "dy.aweme.logs";
    // 风控广播
    public static final String RECEIVER_RISK_ACTION = "dy.aweme.risk";
    // 定时一键新机广播
    public static final String RECEIVER_NEW_PHONE_TIME_ACTION = "dy.new.phone.time.risk";

    public static final String RISK_AWEME_LIST_MILD = "risk.aweme.list.mild"; // aweme list轻微风控
    public static final String RISK_AWEME_LIST_SEVERE = "risk.aweme.list.severe"; // aweme list严重风控
    public static final String RISK_COMMENT_LIST_MILD = "risk.comment.list.mild"; // 评论列表轻微风控
    public static final String RISK_COMMENT_LIST_SEVERE = "risk.comment.list.severe"; // 评论列表严重风控
    public static final String RISK_DY_CRASH = "risk.dy.crash"; // dy卡死或crash

    private FloatWindowMgr mFloatWindowMgr;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public static void registerReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(RECEIVER_SWIPE_UP_CMD);
        filter.addAction(RECEIVER_LOGS_ACTION);
        filter.addAction(RECEIVER_RISK_ACTION);
        filter.addAction(RECEIVER_NEW_PHONE_TIME_ACTION);
        context.registerReceiver(new XpReceiver(context), filter);
    }

    private XpReceiver(Context context) {
        super();
        TraceUtil.le("XpReceiver创建成功！");
        mFloatWindowMgr = FloatWindowMgr.getSingleInstance(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(RECEIVER_SWIPE_UP_CMD)) {
            boolean monitor = intent.getBooleanExtra("monitor", false);
            boolean isShow = mFloatWindowMgr.isShowLog();
            TraceUtil.d("上滑滑动，LogFloatWindow isShow " + isShow);
            if (isShow) {
                mFloatWindowMgr.showMenu();
            }

            mHandler.postDelayed(() -> {
                swipeUp(context);
            }, 2000);

            // 启动评论监听
            mHandler.postDelayed(() -> {
                if (isShow) {
                    mFloatWindowMgr.showLog();
                    if (monitor) {
                        TraceUtil.d("上滑滑动，start comment monitor");
                        AppBroadcast.sendCommentMonitor(context, true);
                    }
                }
            }, 5000);
        } else if (action.equals(RECEIVER_LOGS_ACTION)) {
            String str = intent.getStringExtra("logs");
            // TraceUtil.e("receive logs: " + str);
            try {
                JSONObject obj = new JSONObject(str);
                mFloatWindowMgr.updateLogData(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (action.equals(RECEIVER_RISK_ACTION)) {
            String str = intent.getStringExtra("risk");
            TraceUtil.d("receive risk: " + str);
            newPhoneAndLaunch(context, FloatWindowMgr.LOG_NEW_PHONE_RISK);
        } else if (action.equals(RECEIVER_NEW_PHONE_TIME_ACTION)) {
            newPhoneAndLaunch(context, FloatWindowMgr.LOG_NEW_PHONE_TIME);
        }
    }

    private void swipeUp(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        width = width / 2;
        height = height * 2 / 3;
        String swipeCmd = String.format(Locale.getDefault(), "input swipe %d %d %d 300", width, height, width);
        TraceUtil.d("上滑滑动，swipeCmd = " + swipeCmd);
        Shell.execCommand(swipeCmd, true);
        int result = Shell.execCommand(swipeCmd, true).result;
        if (result == 0) {
            TraceUtil.d("上滑查看更多视频成功！");
        } else {
            TraceUtil.d("上滑查看更多视频失败！");
        }
    }

    private void newPhoneAndLaunch(Context context, String type) {
        TraceUtil.e("new phone");
        new NewPhoneThread(context, HookMain.PACKAGE_ID_NORM, PersistFactory.getInstance(context), new Runnable() {
            @Override
            public void run() {
                mFloatWindowMgr.updateLogData(JSONObjectPack.getJsonObject(FloatWindowMgr.LOG_NEW_PHONE, type));
                Bundle bundle = new Bundle();
                bundle.putBoolean("monitor", true);
                LaunchUtil.startApp(context, HookMain.PACKAGE_ID_NORM, bundle);
            }
        }).start();
    }
}
