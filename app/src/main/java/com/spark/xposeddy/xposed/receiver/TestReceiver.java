package com.spark.xposeddy.xposed.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.spark.xposeddy.persist.PersistKey;
import com.spark.xposeddy.persist.impl.PersistFactory;
import com.spark.xposeddy.util.FileUtil;
import com.spark.xposeddy.util.ShellFileUtil;
import com.spark.xposeddy.util.ShellUtil;
import com.spark.xposeddy.util.TraceUtil;
import com.spark.xposeddy.xposed.HookMain;

import java.io.File;
import java.net.URLDecoder;
import java.util.List;

/**
 * adb shell am broadcast -a dy.test.clear.dy
 * adb shell am broadcast -a dy.test.clear.phoneinfo
 * adb shell am broadcast -a dy.test.phoneinfo.data --es info %7B%22androidId%22%3A%227147ea25c41e4cbb%22%2C%22board%22%3A%22GM1910%22%2C%22bootloader%22%3A%22unknown%22%2C%22brand%22%3A%22OnePlus%22%2C%22buildID%22%3A%22N2G48C%22%2C%22codename%22%3A%22REL%22%2C%22device%22%3A%22aosp%22%2C%22display%22%3A%22N2G48C%22%2C%22fingerprint%22%3A%22google%2Fandroid_x86%2Fx86%3A7.1.2%2FN2G48C%2FN975FXXU1ASGO%3A%2Frelease-keys%22%2C%22getBSSID%22%3A%2200%3AAA%3Ada%3A32%3Adb%3A90%22%2C%22getCellLocation%22%3A%22%5B1028%2C32305%2C0%5D%22%2C%22getDataActivity%22%3A%220%22%2C%22getDeviceId%22%3A%22865166022683430%22%2C%22getExtraInfo%22%3A%22%5C%22dlb%5C%22%22%2C%22getIpAddress%22%3A%22318836908%22%2C%22getLine1Number%22%3A%22%22%2C%22getLocalHost%22%3A%22localhost%2F127.0.0.1%22%2C%22getMacAddress%22%3A%2200%3Adb%3A6d%3A32%3A90%3Ada%22%2C%22getNetworkId%22%3A%220%22%2C%22getNetworkOperator%22%3A%2246000%22%2C%22getNetworkOperatorName%22%3A%22CHINA+MOBILE%22%2C%22getNetworkType%22%3A%220%22%2C%22getRadioVersion%22%3A%22%22%2C%22getRssi%22%3A%22-43%22%2C%22getSSID%22%3A%22%5C%22dlb%5C%22%22%2C%22getSimOperator%22%3A%2246000%22%2C%22getSimOperatorName%22%3A%22China+Mobile+GSM%22%2C%22getSimSerialNumber%22%3A%2289860044490343409030%22%2C%22getSubscriberId%22%3A%22460007956387871%22%2C%22getSubtype%22%3A%220%22%2C%22getType%22%3A%221%22%2C%22getTypeName%22%3A%22WIFI%22%2C%22hardware%22%3A%22android_x86%22%2C%22host%22%3A%22ubuntu%22%2C%22incremental%22%3A%22N975FXXU1ASGO%22%2C%22manufacturer%22%3A%22OnePlus%22%2C%22model%22%3A%22GM1910%22%2C%22product%22%3A%22GM1910%22%2C%22release%22%3A%227.1.2%22%2C%22scanResultsBSSID%22%3A%2200%3AAA%3Ada%3A32%3Adb%3A90%22%2C%22scanResultsCapabilities%22%3A%22%5BESS%5D%22%2C%22scanResultsFrequency%22%3A%222422%22%2C%22scanResultsLevel%22%3A%22-42%22%2C%22scanResultsSSID%22%3A%22dlb%22%2C%22sdk%22%3A%2225%22%2C%22sdkInt%22%3A%2225%22%2C%22serial%22%3A%22004b4bfa%22%2C%22tags%22%3A%22release-keys%22%2C%22time%22%3A%221616072733000%22%2C%22type%22%3A%22user%22%2C%22user%22%3A%22build%22%2C%22version%22%3A%2209%22%2C%22widthPixels%22%3A%222.0%22%7D
 * <p>
 * {"androidId":"c5168e7d516bb031","board":"HD1910","bootloader":"unknown","brand":"OnePlus","buildID":"N2G48C","codename":"REL","device":"aosp","display":"N2G48C","fingerprint":"google/android_x86/x86:7.1.2/N2G48C/N975FXXU1ASGO:/release-keys","getBSSID":"00:AA:ba:56:db:cf","getCellLocation":"[1028,32305,0]","getDataActivity":"0","getDeviceId":"865166026180342","getExtraInfo":"\"TP-LINK\"","getIpAddress":"1258361004","getLine1Number":"","getLocalHost":"localhost/127.0.0.1","getMacAddress":"00:db:de:56:cf:ba","getNetworkId":"0","getNetworkOperator":"46000","getNetworkOperatorName":"CHINA MOBILE","getNetworkType":"0","getRadioVersion":"","getRssi":"-42","getSSID":"\"TP-LINK\"","getSimOperator":"46000","getSimOperatorName":"China Mobile GSM","getSimSerialNumber":"89860089469692880230","getSubscriberId":"460003473010917","getSubtype":"0","getType":"1","getTypeName":"WIFI","hardware":"android_x86","host":"ubuntu","incremental":"N975FXXU1ASGO","manufacturer":"OnePlus","model":"HD1910","product":"HD1910","release":"7.1.2","scanResultsBSSID":"00:AA:ba:56:db:cf","scanResultsCapabilities":"[ESS]","scanResultsFrequency":"2457","scanResultsLevel":"-42","scanResultsSSID":"TP-LINK","sdk":"25","sdkInt":"25","serial":"002b149e","tags":"release-keys","time":"1616072733000","type":"user","user":"build","version":"09","widthPixels":"2.0"}
 * {"androidId":"7147ea25c41e4cbb","board":"GM1910","bootloader":"unknown","brand":"OnePlus","buildID":"N2G48C","codename":"REL","device":"aosp","display":"N2G48C","fingerprint":"google/android_x86/x86:7.1.2/N2G48C/N975FXXU1ASGO:/release-keys","getBSSID":"00:AA:da:32:db:90","getCellLocation":"[1028,32305,0]","getDataActivity":"0","getDeviceId":"865166022683430","getExtraInfo":"\"dlb\"","getIpAddress":"318836908","getLine1Number":"","getLocalHost":"localhost/127.0.0.1","getMacAddress":"00:db:6d:32:90:da","getNetworkId":"0","getNetworkOperator":"46000","getNetworkOperatorName":"CHINA MOBILE","getNetworkType":"0","getRadioVersion":"","getRssi":"-43","getSSID":"\"dlb\"","getSimOperator":"46000","getSimOperatorName":"China Mobile GSM","getSimSerialNumber":"89860044490343409030","getSubscriberId":"460007956387871","getSubtype":"0","getType":"1","getTypeName":"WIFI","hardware":"android_x86","host":"ubuntu","incremental":"N975FXXU1ASGO","manufacturer":"OnePlus","model":"GM1910","product":"GM1910","release":"7.1.2","scanResultsBSSID":"00:AA:da:32:db:90","scanResultsCapabilities":"[ESS]","scanResultsFrequency":"2422","scanResultsLevel":"-42","scanResultsSSID":"dlb","sdk":"25","sdkInt":"25","serial":"004b4bfa","tags":"release-keys","time":"1616072733000","type":"user","user":"build","version":"09","widthPixels":"2.0"}
 */
public class TestReceiver extends BroadcastReceiver {
    // 清除抖音数据
    public static final String RECEIVER_TEST_CLEAR_DY = "dy.test.clear.dy";
    // 清除一键新机数据
    public static final String RECEIVER_TEST_CLEAR_PHONEINFO = "dy.test.clear.phoneinfo";
    // 指定下次一键新机数据
    public static final String RECEIVER_TEST_PHONEINFO_DATA = "dy.test.phoneinfo.data";

    public static void registerReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(RECEIVER_TEST_CLEAR_DY);
        filter.addAction(RECEIVER_TEST_CLEAR_PHONEINFO);
        filter.addAction(RECEIVER_TEST_PHONEINFO_DATA);
        context.registerReceiver(new TestReceiver(), filter);
    }

    private TestReceiver() {
        super();
        TraceUtil.le("TestReceiver创建成功！");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(RECEIVER_TEST_CLEAR_DY)) {
            new ClearThread(HookMain.PACKAGE_ID_NORM).start();
        } else if (action.equals(RECEIVER_TEST_CLEAR_PHONEINFO)) {
            PersistFactory.getInstance(context).writeData(PersistKey.PHONE_INFO, "");
            ShellUtil.exitApp(HookMain.PACKAGE_ID_NORM);
        } else if (action.equals(RECEIVER_TEST_PHONEINFO_DATA)) {
            String info = intent.getStringExtra("info");
            info = URLDecoder.decode(info);
            TraceUtil.e("test phoneInfo = " + info);
            PersistFactory.getInstance(context).writeData(PersistKey.PHONE_INFO_TEST, info);
        }
    }

    public class ClearThread extends Thread {
        private String packName;

        public ClearThread(String packName) {
            super();
            this.packName = packName;
        }

        @Override
        public void run() {
            super.run();
            TraceUtil.e("exitApp start tick: " + System.currentTimeMillis() / 1000);
            ShellUtil.exitApp(packName);
            TraceUtil.e("exitApp end tick: " + System.currentTimeMillis() / 1000);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            TraceUtil.e("clearAppData start tick: " + System.currentTimeMillis() / 1000);
            clearAppData(packName);
            TraceUtil.e("clearAppData end tick: " + System.currentTimeMillis() / 1000);
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
    }
}
