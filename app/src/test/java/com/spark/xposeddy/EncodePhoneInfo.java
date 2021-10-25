package com.spark.xposeddy;

import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * encodeURIComponent(''.replace(/\\/g,"\\\\"))
 */
public class EncodePhoneInfo {
    private static final String info = "{\"androidId\":\"7147ea25c41e4cbb\",\"board\":\"GM1910\",\"bootloader\":\"unknown\",\"brand\":\"OnePlus\",\"buildID\":\"N2G48C\",\"codename\":\"REL\",\"device\":\"aosp\",\"display\":\"N2G48C\",\"fingerprint\":\"google/android_x86/x86:7.1.2/N2G48C/N975FXXU1ASGO:/release-keys\",\"getBSSID\":\"00:AA:da:32:db:90\",\"getCellLocation\":\"[1028,32305,0]\",\"getDataActivity\":\"0\",\"getDeviceId\":\"865166022683430\",\"getExtraInfo\":\"\\\"dlb\\\"\",\"getIpAddress\":\"318836908\",\"getLine1Number\":\"\",\"getLocalHost\":\"localhost/127.0.0.1\",\"getMacAddress\":\"00:db:6d:32:90:da\",\"getNetworkId\":\"0\",\"getNetworkOperator\":\"46000\",\"getNetworkOperatorName\":\"CHINA MOBILE\",\"getNetworkType\":\"0\",\"getRadioVersion\":\"\",\"getRssi\":\"-43\",\"getSSID\":\"\\\"dlb\\\"\",\"getSimOperator\":\"46000\",\"getSimOperatorName\":\"China Mobile GSM\",\"getSimSerialNumber\":\"89860044490343409030\",\"getSubscriberId\":\"460007956387871\",\"getSubtype\":\"0\",\"getType\":\"1\",\"getTypeName\":\"WIFI\",\"hardware\":\"android_x86\",\"host\":\"ubuntu\",\"incremental\":\"N975FXXU1ASGO\",\"manufacturer\":\"OnePlus\",\"model\":\"GM1910\",\"product\":\"GM1910\",\"release\":\"7.1.2\",\"scanResultsBSSID\":\"00:AA:da:32:db:90\",\"scanResultsCapabilities\":\"[ESS]\",\"scanResultsFrequency\":\"2422\",\"scanResultsLevel\":\"-42\",\"scanResultsSSID\":\"dlb\",\"sdk\":\"25\",\"sdkInt\":\"25\",\"serial\":\"004b4bfa\",\"tags\":\"release-keys\",\"time\":\"1616072733000\",\"type\":\"user\",\"user\":\"build\",\"version\":\"09\",\"widthPixels\":\"2.0\"}";

    public static void main(String[] args) {
        String encode = URLEncoder.encode(info);
        System.out.println(encode);
//        String decode = URLDecoder.decode(encode);
//        System.out.println(decode);
    }
}
