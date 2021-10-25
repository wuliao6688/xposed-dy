package com.spark.xposeddy.xposed.phone;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.alibaba.fastjson.JSON;
import com.spark.xposeddy.net.impl.ApiUrl;
import com.spark.xposeddy.persist.IPersist;
import com.spark.xposeddy.persist.PersistKey;
import com.spark.xposeddy.persist.impl.PersistFactory;
import com.spark.xposeddy.util.JSONObjectPack;
import com.spark.xposeddy.util.TraceUtil;
import com.spark.xposeddy.util.UniqueCodeUtil;
import com.spark.xposeddy.xposed.HttpHelper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PhoneMgr {
    private static final String PHONE_NUM = "phone_num";
    private static final List<PhoneInfo> mPhoneList;
    private static final String[] mPhoneInfos = new String[]{
            "{\"androidId\":\"522c0e2aa3c8eff6\",\"board\":\"SM-N9760\",\"bootloader\":\"unknown\",\"brand\":\"samsung\",\"buildID\":\"N2G48C\",\"codename\":\"REL\",\"device\":\"aosp\",\"display\":\"N2G48C\",\"fingerprint\":\"google/android_x86/x86:7.1.2/N2G48C/N975FXXU1ASGO:/release-keys\",\"getBSSID\":\"00:AA:81:9a:db:56\",\"getCellLocation\":\"[1028,32305,0]\",\"getDataActivity\":\"0\",\"getDeviceId\":\"865166029262592\",\"getExtraInfo\":\"\\\"A3681\\\"\",\"getIpAddress\":\"1157697708\",\"getLine1Number\":\"\",\"getLocalHost\":\"localhost/127.0.0.1\",\"getMacAddress\":\"00:db:36:9a:56:81\",\"getNetworkId\":\"0\",\"getNetworkOperator\":\"46000\",\"getNetworkOperatorName\":\"CHINA MOBILE\",\"getNetworkType\":\"0\",\"getRadioVersion\":\"\",\"getRssi\":\"-42\",\"getSSID\":\"\\\"A3681\\\"\",\"getSimOperator\":\"46000\",\"getSimOperatorName\":\"China Mobile GSM\",\"getSimSerialNumber\":\"89860012261199491919\",\"getSubscriberId\":\"460006753440678\",\"getSubtype\":\"0\",\"getType\":\"1\",\"getTypeName\":\"WIFI\",\"hardware\":\"android_x86\",\"host\":\"ubuntu\",\"incremental\":\"N975FXXU1ASGO\",\"manufacturer\":\"samsung\",\"model\":\"SM-N9760\",\"product\":\"SM-N9760\",\"release\":\"7.1.2\",\"scanResultsBSSID\":\"00:AA:81:9a:db:56\",\"scanResultsCapabilities\":\"[ESS]\",\"scanResultsFrequency\":\"2447\",\"scanResultsLevel\":\"-43\",\"scanResultsSSID\":\"A3681\",\"sdk\":\"25\",\"sdkInt\":\"25\",\"serial\":\"00108d52\",\"tags\":\"release-keys\",\"time\":\"1616072733000\",\"type\":\"user\",\"user\":\"build\",\"version\":\"09\",\"widthPixels\":\"2.0\"}",
            "{\"androidId\":\"277f2484b2cf0119\",\"board\":\"SM-N9700\",\"bootloader\":\"unknown\",\"brand\":\"samsung\",\"buildID\":\"N2G48C\",\"codename\":\"REL\",\"device\":\"aosp\",\"display\":\"N2G48C\",\"fingerprint\":\"google/android_x86/x86:7.1.2/N2G48C/N975FXXU1ASGO:/release-keys\",\"getBSSID\":\"00:AA:05:b1:db:8f\",\"getCellLocation\":\"[1028,32305,0]\",\"getDataActivity\":\"0\",\"getDeviceId\":\"865166022289287\",\"getExtraInfo\":\"\\\"17A5\\\"\",\"getIpAddress\":\"-1876973376\",\"getLine1Number\":\"\",\"getLocalHost\":\"localhost/127.0.0.1\",\"getMacAddress\":\"00:db:7a:b1:8f:05\",\"getNetworkId\":\"0\",\"getNetworkOperator\":\"46000\",\"getNetworkOperatorName\":\"CHINA MOBILE\",\"getNetworkType\":\"0\",\"getRadioVersion\":\"\",\"getRssi\":\"-42\",\"getSSID\":\"\\\"17A5\\\"\",\"getSimOperator\":\"46000\",\"getSimOperatorName\":\"China Mobile GSM\",\"getSimSerialNumber\":\"89860040238066298908\",\"getSubscriberId\":\"460007550137158\",\"getSubtype\":\"0\",\"getType\":\"1\",\"getTypeName\":\"WIFI\",\"hardware\":\"android_x86\",\"host\":\"ubuntu\",\"incremental\":\"N975FXXU1ASGO\",\"manufacturer\":\"samsung\",\"model\":\"SM-N9700\",\"product\":\"SM-N9700\",\"release\":\"7.1.2\",\"scanResultsBSSID\":\"00:AA:05:b1:db:8f\",\"scanResultsCapabilities\":\"[ESS]\",\"scanResultsFrequency\":\"2457\",\"scanResultsLevel\":\"-40\",\"scanResultsSSID\":\"17A5\",\"sdk\":\"25\",\"sdkInt\":\"25\",\"serial\":\"00945479\",\"tags\":\"release-keys\",\"time\":\"1616072733000\",\"type\":\"user\",\"user\":\"build\",\"version\":\"09\",\"widthPixels\":\"2.0\"}",
            "{\"androidId\":\"1ae73a3f516d49a9\",\"board\":\"SM-G9730\",\"bootloader\":\"unknown\",\"brand\":\"samsung\",\"buildID\":\"N2G48C\",\"codename\":\"REL\",\"device\":\"aosp\",\"display\":\"N2G48C\",\"fingerprint\":\"google/android_x86/x86:7.1.2/N2G48C/N975FXXU1ASGO:/release-keys\",\"getBSSID\":\"00:AA:ba:05:db:12\",\"getCellLocation\":\"[1028,32305,0]\",\"getDataActivity\":\"0\",\"getDeviceId\":\"865166021329910\",\"getExtraInfo\":\"\\\"James\\\"\",\"getIpAddress\":\"251728044\",\"getLine1Number\":\"\",\"getLocalHost\":\"localhost/127.0.0.1\",\"getMacAddress\":\"00:db:a8:05:12:ba\",\"getNetworkId\":\"0\",\"getNetworkOperator\":\"46000\",\"getNetworkOperatorName\":\"CHINA MOBILE\",\"getNetworkType\":\"0\",\"getRadioVersion\":\"\",\"getRssi\":\"-42\",\"getSSID\":\"\\\"James\\\"\",\"getSimOperator\":\"46000\",\"getSimOperatorName\":\"China Mobile GSM\",\"getSimSerialNumber\":\"89860033653369064684\",\"getSubscriberId\":\"460008892667615\",\"getSubtype\":\"0\",\"getType\":\"1\",\"getTypeName\":\"WIFI\",\"hardware\":\"android_x86\",\"host\":\"ubuntu\",\"incremental\":\"N975FXXU1ASGO\",\"manufacturer\":\"samsung\",\"model\":\"SM-G9730\",\"product\":\"SM-G9730\",\"release\":\"7.1.2\",\"scanResultsBSSID\":\"00:AA:ba:05:db:12\",\"scanResultsCapabilities\":\"[ESS]\",\"scanResultsFrequency\":\"2447\",\"scanResultsLevel\":\"-36\",\"scanResultsSSID\":\"James\",\"sdk\":\"25\",\"sdkInt\":\"25\",\"serial\":\"002bc9cd\",\"tags\":\"release-keys\",\"time\":\"1616072733000\",\"type\":\"user\",\"user\":\"build\",\"version\":\"09\",\"widthPixels\":\"2.0\"}",
            "{\"androidId\":\"0352fb39b7b57095\",\"board\":\"GM1910\",\"bootloader\":\"unknown\",\"brand\":\"OnePlus\",\"buildID\":\"N2G48C\",\"codename\":\"REL\",\"device\":\"aosp\",\"display\":\"N2G48C\",\"fingerprint\":\"google/android_x86/x86:7.1.2/N2G48C/N975FXXU1ASGO:/release-keys\",\"getBSSID\":\"00:AA:38:94:db:69\",\"getCellLocation\":\"[1028,32305,0]\",\"getDataActivity\":\"0\",\"getDeviceId\":\"865166021690246\",\"getExtraInfo\":\"\\\"40938\\\"\",\"getIpAddress\":\"822153388\",\"getLine1Number\":\"\",\"getLocalHost\":\"localhost/127.0.0.1\",\"getMacAddress\":\"00:db:09:94:69:38\",\"getNetworkId\":\"0\",\"getNetworkOperator\":\"46000\",\"getNetworkOperatorName\":\"CHINA MOBILE\",\"getNetworkType\":\"0\",\"getRadioVersion\":\"\",\"getRssi\":\"-43\",\"getSSID\":\"\\\"40938\\\"\",\"getSimOperator\":\"46000\",\"getSimOperatorName\":\"China Mobile GSM\",\"getSimSerialNumber\":\"89860034346691847727\",\"getSubscriberId\":\"460006963910613\",\"getSubtype\":\"0\",\"getType\":\"1\",\"getTypeName\":\"WIFI\",\"hardware\":\"android_x86\",\"host\":\"ubuntu\",\"incremental\":\"N975FXXU1ASGO\",\"manufacturer\":\"OnePlus\",\"model\":\"GM1910\",\"product\":\"GM1910\",\"release\":\"7.1.2\",\"scanResultsBSSID\":\"00:AA:38:94:db:69\",\"scanResultsCapabilities\":\"[ESS]\",\"scanResultsFrequency\":\"2457\",\"scanResultsLevel\":\"-38\",\"scanResultsSSID\":\"40938\",\"sdk\":\"25\",\"sdkInt\":\"25\",\"serial\":\"00a9b25c\",\"tags\":\"release-keys\",\"time\":\"1616072733000\",\"type\":\"user\",\"user\":\"build\",\"version\":\"09\",\"widthPixels\":\"2.0\"}",
    };

    static {
        mPhoneList = new ArrayList<>();
        for (String info : mPhoneInfos) {
            mPhoneList.add(JSON.parseObject(info, PhoneInfo.class));
        }
    }

    /**
     * 一键新机
     * 先从网络上获取新机参数
     * 如果失败，再从本机获取缓存的新机参数
     *
     * @param context
     * @return null-本机信息
     */
    public static PhoneInfo newPhoneInfo(Context context, String deviceNum) {
        IPersist persist = PersistFactory.getInstance(context);
        String test = (String) persist.readData(PersistKey.PHONE_INFO_TEST, "");
        if (!TextUtils.isEmpty(test)) {
            TraceUtil.e("test phoneInfo...");
            persist.writeData(PersistKey.PHONE_INFO_TEST, "");
            return JSON.parseObject(test, PhoneInfo.class);
        }

        PhoneInfo info = getNetPhoneInfo(context, deviceNum);
        if (info != null) {
            TraceUtil.e("net phoneInfo...");
            return info;
        }

        int cacheNum = (Integer) persist.readData(PHONE_NUM, -1);
        if (++cacheNum >= mPhoneList.size()) {
            cacheNum = -1;
        }
        PersistFactory.getInstance(context).writeData(PHONE_NUM, cacheNum);

        if (cacheNum == -1) {
            TraceUtil.e("local phoneInfo...");
            return null;
        }
        TraceUtil.e("cache phoneInfo...");
        return getCachePhoneInfo(cacheNum);
    }

    /**
     * 获取具体的本机信息
     *
     * @param context
     * @return
     */
    public static PhoneInfo getLocalPhoneInfo(Context context) {
        PhoneInfo phoneInfo = new PhoneInfo();

        try {
            phoneInfo.setTags(Build.TAGS);
            phoneInfo.setHost(Build.HOST);
            phoneInfo.setUser(Build.USER);
            phoneInfo.setTime(String.valueOf(Build.TIME));
            phoneInfo.setDisplay(Build.DISPLAY);
            phoneInfo.setBootloader(Build.BOOTLOADER);
            phoneInfo.setSerial(Build.SERIAL);
            phoneInfo.setBoard(Build.BOARD);
            phoneInfo.setBrand(Build.BRAND);
            phoneInfo.setDevice(Build.DEVICE);
            phoneInfo.setFingerprint(Build.FINGERPRINT);
            phoneInfo.setHardware(Build.HARDWARE);
            phoneInfo.setManufacturer(Build.MANUFACTURER);
            phoneInfo.setType(Build.TYPE);
            phoneInfo.setModel(Build.MODEL);
            phoneInfo.setProduct(Build.PRODUCT);
            phoneInfo.setBuildID(Build.ID);
            phoneInfo.setRelease(Build.VERSION.RELEASE);
            phoneInfo.setIncremental(Build.VERSION.INCREMENTAL);
            phoneInfo.setCodename(Build.VERSION.CODENAME);
            phoneInfo.setSdk(Build.VERSION.SDK);
            phoneInfo.setSdkInt(String.valueOf(Build.VERSION.SDK_INT));
            phoneInfo.setGetRadioVersion(Build.getRadioVersion());

            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            phoneInfo.setGetDeviceId(telephonyManager.getDeviceId());
            phoneInfo.setGetNetworkOperator(telephonyManager.getNetworkOperator());
            phoneInfo.setGetNetworkOperatorName(telephonyManager.getNetworkOperatorName());
            phoneInfo.setGetNetworkType(String.valueOf(telephonyManager.getNetworkType()));
            phoneInfo.setGetSimOperator(telephonyManager.getSimOperator());
            phoneInfo.setGetSimOperatorName(telephonyManager.getSimOperatorName());
            phoneInfo.setGetSubscriberId(telephonyManager.getSubscriberId());
            phoneInfo.setGetDataActivity(String.valueOf(telephonyManager.getDataActivity()));
            phoneInfo.setVersion(telephonyManager.getDeviceSoftwareVersion());
            phoneInfo.setGetLine1Number(telephonyManager.getLine1Number());
            phoneInfo.setGetSimSerialNumber(telephonyManager.getSimSerialNumber());
            phoneInfo.setGetCellLocation(telephonyManager.getCellLocation().toString());

            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            phoneInfo.setGetExtraInfo(networkInfo.getExtraInfo());
            phoneInfo.setGetReason(networkInfo.getReason());
            phoneInfo.setGetSubtype(String.valueOf(networkInfo.getSubtype()));
            phoneInfo.setGetTypeName(networkInfo.getSubtypeName());
            phoneInfo.setGetType(String.valueOf(networkInfo.getType()));
            phoneInfo.setGetTypeName(networkInfo.getTypeName());

            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            phoneInfo.setGetMacAddress(UniqueCodeUtil.getMacAddress(context));
            phoneInfo.setGetBSSID(wifiInfo.getBSSID());
            phoneInfo.setGetIpAddress(String.valueOf(wifiInfo.getIpAddress()));
            phoneInfo.setGetNetworkId(String.valueOf(wifiInfo.getNetworkId()));
            phoneInfo.setGetSSID(wifiInfo.getSSID());
            phoneInfo.setGetRssi(String.valueOf(wifiInfo.getRssi()));
            // phoneInfo.setGetLocalHost(InetAddress.getLocalHost().toString());
            phoneInfo.setGetLocalHost("localhost/127.0.0.1");

            List<ScanResult> list = wifiManager.getScanResults();
            if (list != null && list.size() > 0) {
                ScanResult result = list.get(0);
                phoneInfo.setScanResultsBSSID(result.BSSID);
                phoneInfo.setScanResultsCapabilities(result.capabilities);
                phoneInfo.setScanResultsFrequency(String.valueOf(result.frequency));
                phoneInfo.setScanResultsLevel(String.valueOf(result.level));
                phoneInfo.setScanResultsSSID(result.SSID);
            }

            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            phoneInfo.setWidthPixels(String.valueOf(metrics.widthPixels));
            phoneInfo.setWidthPixels(String.valueOf(metrics.heightPixels));
            phoneInfo.setWidthPixels(String.valueOf(metrics.density));
            phoneInfo.setWidthPixels(String.valueOf(metrics.densityDpi));
            phoneInfo.setWidthPixels(String.valueOf(metrics.scaledDensity));

            phoneInfo.setAndroidId(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
        } catch (Exception e) {
            TraceUtil.e("phoneRead err: " + e.getMessage());
        }
        return phoneInfo;
    }

    public static PhoneInfo getNetPhoneInfo(Context context, String deviceNum) {
        String domain = (String) PersistFactory.getInstance(context).readData(PersistKey.DOMAIN, "");
        JSONObject obj = new JSONObjectPack()
                .putValue("deviceNum", deviceNum)
                .getJSONObject();
        JSONObject res = HttpHelper.post(ApiUrl.getNewPhoneInfo(domain), obj);
        if (res.optBoolean("success")) {
            String data = res.optString("data");
            TraceUtil.e("getNetPhoneInfo: " + data);
            return JSON.parseObject(data, PhoneInfo.class);
        }
        return null;
    }

    private static int getCacheNum(Context context) {
        return (Integer) PersistFactory.getInstance(context).readData(PHONE_NUM, -1);
    }

    private static PhoneInfo getCachePhoneInfo(int num) {
        if (num >= mPhoneList.size()) {
            return null;
        }
        return mPhoneList.get(num);
    }

}
