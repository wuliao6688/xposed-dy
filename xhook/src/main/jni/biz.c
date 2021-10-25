#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <pthread.h>
#include <jni.h>
#include <sys/system_properties.h>
#include <android/log.h>
#include "xhook.h"

prop_info* (*old_system_property_find)(const char* name);
int (*old_system_property_get)(const char *name, char *value);
int (*old_system_property_read)(const prop_info *pi, char *name, char *value);
JavaVM *global_jvm;
jobject global_json;
const char* global_name;
prop_info* gloabl_pi;

JNIEnv *get_env(int *attach) {
   if (global_jvm == NULL) return NULL;

   *attach = 0;
   JNIEnv *jni_env = NULL;

   int status = (*global_jvm)->GetEnv(global_jvm, (void **)&jni_env, JNI_VERSION_1_6);

   if (status == JNI_EDETACHED || jni_env == NULL) {
       status = (*global_jvm)->AttachCurrentThread(global_jvm, &jni_env, NULL);
       if (status < 0) {
           jni_env = NULL;
       } else {
           *attach = 1;
       }
  }
   return jni_env;
}

void del_env() {
    if (global_jvm != NULL) {
        (*global_jvm)->DetachCurrentThread(global_jvm);
    }
}

const char* get_value(char* key) {
    int attach = 0;
    JNIEnv* env = get_env(&attach);
    jobject json = global_json;
    if(env == NULL || json == NULL) {
        return "";
    }

    // 获取JSONObject class
    // __android_log_print(3, "===========》", "获取JSONObject class");
    jclass JSONObjectClass = (*env)->GetObjectClass(env, json);

    // 获取JSONObject get方法
    // __android_log_print(3, "===========》", "获取JSONObject get方法");
    jmethodID getID = (*env)->GetMethodID(env, JSONObjectClass, "getString", "(Ljava/lang/String;)Ljava/lang/String;");
    if (getID == NULL) return "";

    // 获取value
    // __android_log_print(3, "===========》", "获取value");
    jstring jkey = (*env)->NewStringUTF(env, key);
    jstring value = (jstring) (*env)->CallObjectMethod(env, json, getID, jkey);
    if (value == NULL) return "";

    // 把value转换为C string
    // __android_log_print(3, "===========》", "把value转换为C string");
    const char *str = (*env)->GetStringUTFChars(env, value, NULL);
    if (str == NULL) return "";

    // 释放jstring value
    // __android_log_print(3, "===========》", "释放jstring value");
    (*env)->ReleaseStringUTFChars(env, value, str);

    if (attach == 1) {
        del_env();
    }

    return str;
}

void match_name(const char *name, char *value) {
    // 暂时屏蔽，避免SDK变化导致crash
    // if(strcmp(name, "ro.build.version.sdk") == 0) {
    //     strcpy(value, (char*)get_value("sdk"));
    // } else 
    if(strcmp(name, "ro.build.version.release") == 0) {
        strcpy(value, (char*)get_value("release"));
    } else if(strcmp(name, "ro.build.version.incremental") == 0) {
        strcpy(value, (char*)get_value("incremental"));
    } else if(strcmp(name, "ro.build.user") == 0) {
        strcpy(value, (char*)get_value("user"));
    } else if(strcmp(name, "ro.build.host") == 0) {
        strcpy(value, (char*)get_value("host"));
    } else if(strcmp(name, "ro.build.fingerprint") == 0) {
        strcpy(value, (char*)get_value("fingerprint"));
    } else if(strcmp(name, "ro.build.id") == 0) {
        strcpy(value, (char*)get_value("buildID"));
    } else if(strcmp(name, "ro.build.type") == 0) {
        strcpy(value, (char*)get_value("type"));
    } else if(strcmp(name, "ro.build.tags") == 0) {
        strcpy(value, (char*)get_value("tags"));
    } else if(strcmp(name, "ro.serialno") == 0) {
        strcpy(value, (char*)get_value("serial"));
    } else if(strcmp(name, "ro.hardware") == 0) {
        strcpy(value, (char*)get_value("hardware"));
    } else if(strcmp(name, "ro.product.model") == 0) {
        strcpy(value, (char*)get_value("model"));
    } else if(strcmp(name, "ro.product.brand") == 0) {
        strcpy(value, (char*)get_value("brand"));
    } else if(strcmp(name, "ro.product.board") == 0) {
        strcpy(value, (char*)get_value("board"));
    } else if(strcmp(name, "ro.product.name") == 0) {
        strcpy(value, (char*)get_value("product"));
    } else if(strcmp(name, "ro.product.device") == 0) {
        strcpy(value, (char*)get_value("device"));
    } else if(strcmp(name, "ro.product.manufacturer") == 0) {
        strcpy(value, (char*)get_value("manufacturer"));
    } else if(strcmp(name, "ro.build.display.id") == 0) {
        strcpy(value, (char*)get_value("display"));
    } else if(strcmp(name, "ro.build.date.utc") == 0) {
        strcpy(value, (char*)get_value("time"));
    }
}

const prop_info* new_system_property_find(const char* name){
    __android_log_print(3, "===========》", "__system_property_find %s",name);
    global_name = name;
    gloabl_pi = old_system_property_find(name);
    return gloabl_pi;
}
   
static int new_system_property_get(const char *name, char *value){
    int ret = old_system_property_get(name,value);
    match_name(name, value);
    __android_log_print(3, "===========》", "__system_property_get %s %s",name,value);
    return ret;
}
 
static int new_system_property_read(const prop_info *pi, char *name, char *value) {
    int ret = old_system_property_read(pi,name,value);
    if(pi == gloabl_pi) {
        match_name(global_name, value);
    }
    __android_log_print(3, "===========》", "__system_property_read %s %s",global_name,value);
    return ret;
}
 
JNIEXPORT void JNICALL Java_com_qiyi_biz_NativeHandler_start(JNIEnv* env, jobject obj, jobject json)
{
    (void)env;
    (void)obj;

    (*env)->GetJavaVM(env, &global_jvm);
    global_json = (*env)->NewGlobalRef(env, json);

    __android_log_print(3, "===========》", "biz NativeHandler start");
    xhook_register("/data/.*\\.so$", "__system_property_find",new_system_property_find,(void**)(&old_system_property_find));
    xhook_register("/data/.*\\.so$", "__system_property_get",new_system_property_get,(void**)(&old_system_property_get));
    xhook_register("/data/.*\\.so$", "__system_property_read",new_system_property_read,(void**)(&old_system_property_read));
 
    xhook_refresh(1);

}

JNIEXPORT void JNICALL Java_com_qiyi_biz_NativeHandler_test(JNIEnv* env, jobject obj)
{
    (void)env;
    (void)obj;
    char value[PROP_VALUE_MAX] = {0};
    __system_property_get("ro.product.model", value);
}
