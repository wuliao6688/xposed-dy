package com.spark.xposeddy.xposed.dy;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.spark.xposeddy.floatwindow.FloatWindowMgr;
import com.spark.xposeddy.repository.bean.CommentBean;
import com.spark.xposeddy.util.AndroidUtil;
import com.spark.xposeddy.util.JSONObjectPack;
import com.spark.xposeddy.util.TraceUtil;
import com.spark.xposeddy.xposed.HookMain;
import com.spark.xposeddy.xposed.receiver.AppBroadcast;
import com.spark.xposeddy.xposed.receiver.XpBroadcast;
import com.spark.xposeddy.xposed.dy.api.DyApi;
import com.spark.xposeddy.xposed.dy.api.DyApiRes;
import com.spark.xposeddy.xposed.receiver.AppReceiver;

import org.json.JSONObject;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * hook dy的入口
 */
public class HookDy {

    private static final String fDeviceRegister = "/device_register";
    private DyApi mDyApi;
    private DyApiRes mDyApiRes;
    private LiveMonitor mLiveMonitor;
    private Activity mainActivityObj;
    private boolean monitor;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public void hook(ClassLoader appClassLoader, Context context) {
        try {
            mDyApi = new DyApi(appClassLoader);
            mDyApiRes = new DyApiRes(appClassLoader);
            mLiveMonitor = new LiveMonitor(context);
            hookDeviceId(appClassLoader, context);
            hookMainPage(appClassLoader, context);
            hookDialog(appClassLoader, context);
            hookLive(appClassLoader, context);
            hookNet(appClassLoader, context);
            AppReceiver.registerReceiver(context, mDyApi, mDyApiRes, mLiveMonitor);
            // 验证hook生效没有，只能人工写数值，不能AndroidUtil.getVersionCode(mContext, packageName)
            XpBroadcast.sendFloatWindowLog(context, JSONObjectPack.getJsonObject(FloatWindowMgr.LOG_STATUS, "1.0.11 hook 成功"));
        } catch (Exception e) {
            TraceUtil.xe("hookDy err: " + e.getMessage());
        }
    }

    /**
     * hook dy 主页，风控重启后，重新开始评论监听
     *
     * @param classLoader
     * @param context
     */
    private void hookMainPage(ClassLoader classLoader, Context context) {
        Class MainActivity = XposedHelpers.findClass("com.ss.android.ugc.aweme.main.MainActivity", classLoader);
        XposedHelpers.findAndHookMethod(MainActivity, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                TraceUtil.e("MainActivity onCreate");
                AndroidUtil.getApkFirstInstallTime(context, HookMain.PACKAGE_ID_NORM);
                AndroidUtil.getApkLastUpdateTime(context, HookMain.PACKAGE_ID_NORM);
                mainActivityObj = (Activity) param.thisObject;
            }
        });

        XposedHelpers.findAndHookMethod(MainActivity, "onResume", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                TraceUtil.e("MainActivity onResume");
                monitor = false;
                Intent getIntent = mainActivityObj.getIntent();
                if (getIntent != null) {
                    Bundle bundle = getIntent.getExtras();
                    TraceUtil.e("MainActivity bundle: " + (bundle != null ? bundle.toString() : "null"));
                    if (bundle != null) {
                        monitor = bundle.getBoolean("monitor");
                        if (monitor) {
                            // 如果不需要上滑查看更多视频，直接启动评论监听
                            if (!isSwipeUp(mainActivityObj)) {
                                mainHandler.postDelayed(() -> {
                                    AppBroadcast.sendCommentMonitor(mainActivityObj, true);
                                }, 8000);
                            } else {
                                // 上滑查看更多视频
                                mainHandler.postDelayed(() -> {
                                    int text = mainActivityObj.getResources().getIdentifier("ktt", "id", mainActivityObj.getPackageName());
                                    TextView txt = mainActivityObj.findViewById(text);
                                    if (txt != null) {
                                        if (txt.getVisibility() == View.VISIBLE) {
                                            TraceUtil.d("txt is visible, txt = " + txt.getText());
                                            XpBroadcast.sendSwipeUpCmd(mainActivityObj, true);
                                        } else {
                                            TraceUtil.d("txt is gone");
                                            AppBroadcast.sendCommentMonitor(mainActivityObj, true);
                                        }
                                    } else {
                                        TraceUtil.d("txt is null");
                                        AppBroadcast.sendCommentMonitor(mainActivityObj, true);
                                    }
                                }, 8000);
                            }
                        }
                    }
                }
            }
        });

        XposedHelpers.findAndHookMethod(MainActivity, "onDestroy", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                TraceUtil.e("MainActivity onDestroy");
                mainActivityObj = null;
            }
        });
    }

    /**
     * 设置device_register接口，post 数据不加密
     *
     * @param classLoader
     * @param context
     */
    private void hookDeviceId(final ClassLoader classLoader, final Context context) {
        TraceUtil.e("hookDeviceId");
        // device_register接口，post 数据不加密
        Class AppLogCls = XposedHelpers.findClass("com.ss.android.common.applog.AppLog", classLoader);
        XposedHelpers.findAndHookMethod(AppLogCls, "getLogEncryptSwitch", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                param.setResult(false);
            }
        });
    }

    /**
     * 屏蔽 dy 提示框
     * 个人信息保护指引、青少年模式、升级弹框、上滑查看更多视频
     *
     * @param classLoader
     * @param context
     */
    private void hookDialog(final ClassLoader classLoader, final Context context) {
        TraceUtil.e("hookDialog");
        // 屏蔽dy个人信息保护指引
        Class RemindCls = XposedHelpers.findClass("com.ss.android.ugc.aweme.main.cy", classLoader);
        XposedHelpers.findAndHookMethod(RemindCls, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                TraceUtil.e("个人信息保护指引 dialog onCreate");
                final Dialog dialog = (Dialog) param.thisObject;
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        int confirmId = dialog.getContext().getResources().getIdentifier("beo", "id", dialog.getContext().getPackageName());
                        dialog.findViewById(confirmId).performClick();
                    }
                });
            }
        });

        // 屏蔽dy青少年模式
        Class NewTeenGuideForNormalDialogCls = XposedHelpers.findClass("com.ss.android.ugc.aweme.compliance.protection.teenmode.ui.dialog.NewTeenGuideForNormalDialog", classLoader);
        XposedHelpers.findAndHookMethod(NewTeenGuideForNormalDialogCls, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                TraceUtil.e("青少年模式 dialog onCreate");
                final Dialog dialog = (Dialog) param.thisObject;
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        int confirmId = dialog.getContext().getResources().getIdentifier("abn", "id", dialog.getContext().getPackageName());
                        dialog.findViewById(confirmId).performClick();
                    }
                });
            }
        });

        // 屏蔽dy升级弹框
        Class UpdateXCls = XposedHelpers.findClass("com.ss.android.ugc.aweme.update.w", classLoader);
        XposedHelpers.findAndHookMethod(UpdateXCls, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                TraceUtil.e("升级 dialog onCreate");
                final Dialog dialog = (Dialog) param.thisObject;
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        int cancelId = dialog.getContext().getResources().getIdentifier("ep0", "id", dialog.getContext().getPackageName());
                        dialog.findViewById(cancelId).performClick();
                    }
                });
            }
        });
    }

    /**
     * hook直播间消息
     *
     * @param classLoader
     * @param context
     */
    private void hookLive(final ClassLoader classLoader, final Context context) {
        TraceUtil.e("hookLive");
        Class JSON = XposedHelpers.findClass("com.alibaba.fastjson.JSON", classLoader);
        Class IMessage = XposedHelpers.findClass("com.ss.ugc.live.sdk.message.data.IMessage", classLoader);
        Class MessageManager = XposedHelpers.findClass("com.ss.ugc.live.sdk.message.MessageManager", classLoader);
        XposedHelpers.findAndHookMethod(MessageManager, "dispatchMessage", IMessage, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Object message = param.args[0];
                int type = (int) XposedHelpers.callMethod(message, "getIntType");
                if (type == 6 || type == 12) {
                    String str = (String) XposedHelpers.callStaticMethod(JSON, "toJSONString", message);
                    JSONObject jsObj = new JSONObject(str);
                    // 礼物消息
                    if (type == 6) {
                        JSONObject common = jsObj.getJSONObject("common");
                        String content = common.getString("describe");
                        long create_time = common.getLong("create_time");
                        String room_id = common.getString("room_id");

                        JSONObject user = jsObj.optJSONObject("user");
                        String uid = user.optString("id");
                        String display_id = user.optString("display_id");
                        String nickname = user.optString("nickname");
                        String gender = user.optString("gender");
                        String birthday = user.optString("birthday");
                        birthday = TextUtils.isEmpty(birthday) ? "0" : birthday.split("-")[0];

                        CommentBean bean = new CommentBean();
                        bean.setUid(uid).setAcc(display_id).setNick(nickname).setSex(gender)
                                .setAge(birthday).setWorks_num("0")
                                .setContent(content).setRelevant(room_id).setCreate_time(create_time)
                                .setSource("5");
                        TraceUtil.e("dispatchMessage: gift = " + bean.toString());
                        mLiveMonitor.uploadLiveMsg(bean);
                    }
                    // 互动消息
                    else if (type == 12) {
                        String content = jsObj.getString("content");

                        JSONObject common = jsObj.optJSONObject("common");
                        String room_id = common.getString("room_id");

                        JSONObject user = jsObj.optJSONObject("user");
                        String uid = user.optString("id");
                        String display_id = user.optString("display_id");
                        String nickname = user.optString("nickname");
                        String gender = user.optString("gender");
                        String birthday = user.optString("birthday");
                        birthday = TextUtils.isEmpty(birthday) ? "0" : birthday.split("-")[0];
                        boolean is_admin = user.optJSONObject("user_attr").optBoolean("is_admin");

                        CommentBean bean = new CommentBean();
                        bean.setUid(uid).setAcc(display_id).setNick(nickname).setSex(gender)
                                .setAge(birthday).setWorks_num("0")
                                .setContent(content).setRelevant(room_id)
                                .setSource("3");
                        TraceUtil.e("dispatchMessage: is_admin = " + is_admin + ", chat = " + bean.toString());
                        if (!is_admin) {
                            mLiveMonitor.uploadLiveMsg(bean);
                        }
                    }
                }
            }
        });
    }

    /**
     * hook 网络请求
     *
     * @param classLoader
     * @param context
     */
    private void hookNet(final ClassLoader classLoader, final Context context) {
        TraceUtil.e("hookNet");

        // 获取网络请求的header、cookie
        Class UrlRequestBuilderImplCls = XposedHelpers.findClass("com.ttnet.org.chromium.net.impl.UrlRequestBuilderImpl", classLoader);
        XposedHelpers.findAndHookMethod(UrlRequestBuilderImplCls, "build", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Object obj = param.thisObject;
                String url = (String) XposedHelpers.getObjectField(obj, "mUrl");
                Object headers = XposedHelpers.getObjectField(obj, "mRequestHeaders");
                if (!TextUtils.isEmpty(url)
                        && url.contains(fDeviceRegister)) {
                    TraceUtil.e("req url = " + url);
                }
            }
        });

        // 获取网络请求的结果
        Class SsHttpCallCls = XposedHelpers.findClass("com.bytedance.retrofit2.SsHttpCall", classLoader);
        XposedBridge.hookAllMethods(SsHttpCallCls, "getResponseWithInterceptorChain", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Object ssResponse = param.getResult();
                if (ssResponse == null) {
                    TraceUtil.e("res is null");
                    return;
                }
                Object rawResponse = XposedHelpers.callMethod(ssResponse, "raw");
                String url = (String) XposedHelpers.getObjectField(rawResponse, "url");
                if (!TextUtils.isEmpty(url)
                        && (url.contains(fDeviceRegister) || DyApiRes.isContainsUrl(url))) {
                    int status = XposedHelpers.getIntField(rawResponse, "status");
                    byte[] bodyByte = (byte[]) XposedHelpers.getObjectField(XposedHelpers.getObjectField(rawResponse, "body"), "bytes");
                    String body = new String(bodyByte);

                    if (url.contains(fDeviceRegister)) {
                        TraceUtil.e("res url = " + url + ", status = " + status);
                        TraceUtil.e("res body = " + body);
                        JSONObject device = new JSONObject(body);
                        TraceUtil.e("res device_id = " + device.optString("device_id"));
                    }
                    if (DyApiRes.isContainsUrl(url)) {
                        mDyApiRes.setResponse(url, body);
                    }
                }

            }
        });
    }

    private boolean isSwipeUp(Context context) {
        SharedPreferences sp = context.getSharedPreferences("MainTabPreferences", Context.MODE_PRIVATE);
        boolean isSwipeUp = sp.getBoolean("shouldShowSwipeUpGuide1", (Boolean) true);
        TraceUtil.e("isSwipeUp = " + isSwipeUp);
        return isSwipeUp;
    }
}


