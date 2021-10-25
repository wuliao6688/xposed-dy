package com.spark.xposeddy;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.spark.xposeddy.floatwindow.FloatWindowMgr;
import com.spark.xposeddy.net.Callback;
import com.spark.xposeddy.net.IApiMgr;
import com.spark.xposeddy.net.impl.ApiMgrFactory;
import com.spark.xposeddy.persist.IPersist;
import com.spark.xposeddy.persist.PersistKey;
import com.spark.xposeddy.persist.impl.PersistFactory;
import com.spark.xposeddy.component.ProgressUtil;
import com.spark.xposeddy.util.ShellRootUtil;
import com.spark.xposeddy.util.TraceUtil;
import com.spark.xposeddy.xposed.HookMain;
import com.spark.xposeddy.xposed.phone.HookPhone;
import com.spark.xposeddy.xposed.receiver.TestReceiver;
import com.spark.xposeddy.xposed.receiver.XpReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEditDomain;
    private EditText mEditDevice;
    private EditText mEditType;
    private EditText mEditCommentPage;
    private EditText mEditVideoCount;
    private EditText mEditSampleDiff;
    private EditText mEditCommentThreadNum;
    private EditText mEditCommentSmallInterval;
    private EditText mEditCommentLargeInterval;
    private EditText mEditAwemeSmallInterval;
    private EditText mEditAwemeLargeInterval;
    private EditText mEditNewPhoneInterval;
    private TextView mTvSampleAccounts;
    private TextView mTvSampleVideos;
    private Button mBtnSave;

    private Activity mContext = this;
    private IPersist mPersist;
    private IApiMgr mApi;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPersist = PersistFactory.getInstance(this);
        if (TextUtils.isEmpty((String) mPersist.readData(PersistKey.COMMENT_PAGE, ""))) {
            mPersist.writeData(PersistKey.COMMENT_PAGE, "1");
            mPersist.writeData(PersistKey.VIDEO_COUNT, "5");
            mPersist.writeData(PersistKey.SAMPLE_DIFF, "300");
            mPersist.writeData(PersistKey.COMMENT_THREAD_NUM, "5");
            mPersist.writeData(PersistKey.COMMENT_SMALL_INTERVAL, "1000");
            mPersist.writeData(PersistKey.COMMENT_LARGE_INTERVAL, "5000");
            mPersist.writeData(PersistKey.AWEME_SMALL_INTERVAL, "1000");
            mPersist.writeData(PersistKey.AWEME_LARGE_INTERVAL, "10000");
            mPersist.writeData(PersistKey.NEW_PHONE_INTERVAL, "600");
        }
        if (TextUtils.isEmpty((String) mPersist.readData(PersistKey.SAMPLE_ACCOUNTS, ""))) {
            mPersist.writeData(PersistKey.SAMPLE_ACCOUNTS, "94448819751;94766512160;1041964956135549;87755879771;1587337261757357;88090808523;59118017879;2862721033306827;75121210472;60701509911");
        }
        if (TextUtils.isEmpty((String) mPersist.readData(PersistKey.SAMPLE_VIDEOS, ""))) {
            mPersist.writeData(PersistKey.SAMPLE_VIDEOS, "6925391908025994499");
        }
        initView();

        if (FloatWindowMgr.requestFloatWindowPermission(mContext)) {
            FloatWindowMgr.getSingleInstance(mContext).showMenu();
        }
        if (ShellRootUtil.requestRootPermission(mContext)) {
            HookPhone.injectLibrary(mContext);
        }
        mApi = ApiMgrFactory.getInstance(this);
        XpReceiver.registerReceiver(mContext);
        TestReceiver.registerReceiver(mContext);
        registerReceiver();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterReceiver();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.addCategory(Intent.CATEGORY_HOME);
            startActivity(home);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        int resId = v.getId();
        if (resId == R.id.btn_save) {
            if (ShellRootUtil.requestRootPermission(mContext)) {
                HookPhone.injectLibrary(mContext);
                saveParam();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FloatWindowMgr.onActivityResult(mContext, requestCode, resultCode, data);
    }

    private void initView() {
        setContentView(R.layout.activity_main);

        mEditDomain = findViewById(R.id.edit_domain);
        mEditDevice = findViewById(R.id.edit_device);
        mEditType = findViewById(R.id.edit_type);
        mEditCommentPage = findViewById(R.id.edit_comment_page);
        mEditVideoCount = findViewById(R.id.edit_video_count);
        mEditSampleDiff = findViewById(R.id.edit_sample_diff);
        mEditCommentThreadNum = findViewById(R.id.edit_comment_thread_num);
        mEditCommentSmallInterval = findViewById(R.id.edit_small_interval_time);
        mEditCommentLargeInterval = findViewById(R.id.edit_large_interval_time);
        mEditAwemeSmallInterval = findViewById(R.id.edit_aweme_small_interval);
        mEditAwemeLargeInterval = findViewById(R.id.edit_aweme_large_interval);
        mEditNewPhoneInterval = findViewById(R.id.edit_new_phone_interval);
        mTvSampleAccounts = findViewById(R.id.tv_sample_account);
        mTvSampleVideos = findViewById(R.id.tv_sample_video);

        mBtnSave = findViewById(R.id.btn_save);
        mBtnSave.setOnClickListener(this);

        mEditDomain.setText((String) mPersist.readData(PersistKey.DOMAIN, ""));
        mEditDevice.setText((String) mPersist.readData(PersistKey.DEVICE_ID, ""));
        mEditType.setText((String) mPersist.readData(PersistKey.TYPE_ID, ""));
        mEditCommentPage.setText((String) mPersist.readData(PersistKey.COMMENT_PAGE, ""));
        mEditVideoCount.setText((String) mPersist.readData(PersistKey.VIDEO_COUNT, ""));
        mEditSampleDiff.setText((String) mPersist.readData(PersistKey.SAMPLE_DIFF, ""));
        mEditCommentThreadNum.setText((String) mPersist.readData(PersistKey.COMMENT_THREAD_NUM, ""));
        mEditCommentSmallInterval.setText((String) mPersist.readData(PersistKey.COMMENT_SMALL_INTERVAL, ""));
        mEditCommentLargeInterval.setText((String) mPersist.readData(PersistKey.COMMENT_LARGE_INTERVAL, ""));
        mEditAwemeSmallInterval.setText((String) mPersist.readData(PersistKey.AWEME_SMALL_INTERVAL, ""));
        mEditAwemeLargeInterval.setText((String) mPersist.readData(PersistKey.AWEME_LARGE_INTERVAL, ""));
        mEditNewPhoneInterval.setText((String) mPersist.readData(PersistKey.NEW_PHONE_INTERVAL, ""));
        mTvSampleAccounts.setText((String) mPersist.readData(PersistKey.SAMPLE_ACCOUNTS, ""));
        mTvSampleVideos.setText((String) mPersist.readData(PersistKey.SAMPLE_VIDEOS, ""));

        try {
            PackageInfo packInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            ApplicationInfo appInfo = mContext.getApplicationInfo();
            setTitle(mContext.getResources().getString(appInfo.labelRes) + packInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void saveParam() {
        String domain = mEditDomain.getText().toString().trim();
        String device = mEditDevice.getText().toString().trim();
        String type = mEditType.getText().toString().trim();
        String commentPage = mEditCommentPage.getText().toString().trim();
        String videoCount = mEditVideoCount.getText().toString().trim();
        String sampleDiff = mEditSampleDiff.getText().toString().trim();
        String commentThreadNum = mEditCommentThreadNum.getText().toString().trim();
        String smallIntervalTime = mEditCommentSmallInterval.getText().toString().trim();
        String largeIntervalTime = mEditCommentLargeInterval.getText().toString().trim();
        String awemeSmallInterval = mEditAwemeSmallInterval.getText().toString().trim();
        String awemeLargeInterval = mEditAwemeLargeInterval.getText().toString().trim();
        String newPhoneInterval = mEditNewPhoneInterval.getText().toString().trim();
//        if (TextUtils.isEmpty(domain)) {
//            Toast.makeText(mContext, "服务器地址不能为空", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        mPersist.writeData(PersistKey.DOMAIN, domain);
//
//        if (TextUtils.isEmpty(device)) {
//            Toast.makeText(mContext, "设备编码不能为空", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        mPersist.writeData(PersistKey.DEVICE_ID, device);
//
//        if (TextUtils.isEmpty(type)) {
//            Toast.makeText(mContext, "类型编码不能为空", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        mPersist.writeData(PersistKey.TYPE_ID, type);

        if (TextUtils.isEmpty(commentPage)) {
            Toast.makeText(mContext, "采集评论前N页不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        mPersist.writeData(PersistKey.COMMENT_PAGE, commentPage);

        if (TextUtils.isEmpty(videoCount)) {
            Toast.makeText(mContext, "采集达人前N条不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        mPersist.writeData(PersistKey.VIDEO_COUNT, videoCount);

        if (TextUtils.isEmpty(sampleDiff)) {
            Toast.makeText(mContext, "采集最近N秒不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        mPersist.writeData(PersistKey.SAMPLE_DIFF, sampleDiff);

        if (TextUtils.isEmpty(commentThreadNum)) {
            Toast.makeText(mContext, "评论线程数不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        mPersist.writeData(PersistKey.COMMENT_THREAD_NUM, commentThreadNum);

        if (TextUtils.isEmpty(smallIntervalTime)) {
            Toast.makeText(mContext, "评论小间隔不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        mPersist.writeData(PersistKey.COMMENT_SMALL_INTERVAL, smallIntervalTime);

        if (TextUtils.isEmpty(largeIntervalTime)) {
            Toast.makeText(mContext, "评论大间隔不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        mPersist.writeData(PersistKey.COMMENT_LARGE_INTERVAL, largeIntervalTime);

        if (TextUtils.isEmpty(awemeSmallInterval)) {
            Toast.makeText(mContext, "视频小间隔不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        mPersist.writeData(PersistKey.AWEME_SMALL_INTERVAL, awemeSmallInterval);

        if (TextUtils.isEmpty(awemeLargeInterval)) {
            Toast.makeText(mContext, "视频大间隔不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        mPersist.writeData(PersistKey.AWEME_LARGE_INTERVAL, awemeLargeInterval);

        if (TextUtils.isEmpty(newPhoneInterval)) {
            Toast.makeText(mContext, "一键新机间隔不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        mPersist.writeData(PersistKey.NEW_PHONE_INTERVAL, newPhoneInterval);

        getDeviceTask(device);
    }

    private void getDeviceTask(String device) {
        ProgressUtil.showProgressHUD(mContext, "", false, null);
        mApi.getDeviceTask(device, new Callback<String>() {
            @Override
            public void onSuccess(String data) {
                ProgressUtil.dismissProgressHUD();
                TraceUtil.e("getDeviceTask success, data = " + data);
                try {
                    JSONObject obj = new JSONObject(data);
                    if (obj.has("info")) {
                        JSONObject info = obj.optJSONObject("info");
                        String dyIds = info.optString("dy_ids");
                        // String dyIds = "";
                        String videoUrls = info.optString("video_urls");
                        mPersist.writeData(PersistKey.SAMPLE_ACCOUNTS, dyIds);
                        mPersist.writeData(PersistKey.SAMPLE_VIDEOS, videoUrls);
                        mPersist.writeData(PersistKey.CACHE_PROFILE, "");
                        mPersist.writeData(PersistKey.CACHE_AWEME, "");
                        mTvSampleAccounts.setText(dyIds);
                        mTvSampleVideos.setText(videoUrls);

                        updateDeviceTaskState(device);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String error) {
                ProgressUtil.dismissProgressHUD();
                TraceUtil.e("getDeviceTask fail, error = " + error);
                Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show();
                mPersist.writeData(PersistKey.SAMPLE_ACCOUNTS, "");
                mPersist.writeData(PersistKey.SAMPLE_VIDEOS, "");
                mTvSampleAccounts.setText("");
                mTvSampleVideos.setText("");
            }
        });
    }

    private void updateDeviceTaskState(String device) {
        mApi.updateDeviceTaskState(device, new Callback<String>() {
            @Override
            public void onSuccess(String data) {
                TraceUtil.e("updateDeviceTaskState success, data = " + data);
            }

            @Override
            public void onFailure(String error) {
                TraceUtil.e("updateDeviceTaskState fail, error = " + error);
            }
        });
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (XpReceiver.RECEIVER_NEW_TASK_ACTION.equals(action)) {
                String sampleAccounts = intent.getStringExtra("sampleAccounts");
                // String sampleAccounts = "";
                String sampleVideos = intent.getStringExtra("sampleVideos");
                mPersist.writeData(PersistKey.SAMPLE_ACCOUNTS, sampleAccounts);
                mPersist.writeData(PersistKey.SAMPLE_VIDEOS, sampleVideos);
                mTvSampleAccounts.setText(sampleAccounts);
                mTvSampleVideos.setText(sampleVideos);
            }
        }
    };

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(XpReceiver.RECEIVER_NEW_TASK_ACTION);
        mContext.registerReceiver(mReceiver, filter);
    }

    private void unRegisterReceiver() {
        mContext.unregisterReceiver(mReceiver);
    }
}
