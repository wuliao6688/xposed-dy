# 基于Xposed的抖音爬虫，抖音风控后自动一键新机，模拟一个全新的运行环境

### 主要功能列表
> - 采集指定达人最新视频的评论 done
> - 采集指定视频的评论 done
> - 采集直播间互动消息 done
> - 采集直播间礼物消息 done
> - dy风控后自动清除缓存、一键新机、飞行切换ip，模拟一个全新的运行环境 done

### 项目介绍
* 目标版本：dy 15.3.0
- app
  - aidl，跨进程大文本传输
  - java
    - component，界面组件
    - floatwindow，dy爬虫控制悬浮窗
    - net，网络请求模块
    - persist，数据持久化-程序配置
    - repository，数据持久化-最新评论缓存
    - util，常用工具
    - xposed
      - HookMain，hook入口
      - HttpHelper，简易的http库，供hook模块使用
      - dy，dy hook部分
      - phone，一键新机
      - provider 跨进程共享
        - PropertyProvider，跨进程共享程序配置
        - DbProvider，跨进程共享视频评论缓存
      - receiver 跨进程通信
        - AppBroadcast，app对xp发送的广播
        - AppReceiver，AppBroadcast广播的接收器
        - XpBroadcast，xp对外发送的广播
        - XpReceiver，XpBroadcast广播的接收器
        - TestReceiver，用于测试的广播的接收器
- xhook
  - java
    - biz，native hook对应的java代码
    - xhook，iqiyi xhook对应的java代码
  - jni
    - biz，native hook代码
    - xhook，iqiyi xhook库
- build_libs.sh，编译生成xhook动态库
- clean_libs.sh，清除xhook动态库
- 手机查看器_2.0.apk，查看手机设备信息，用于测试一键新机效果

### hook-评论
```
达人uid -> 达人sec_uid -> 达人的视频列表 -> 视频的评论列表
com.ss.android.ugc.aweme.profile.api.o - ProfileManager
com.ss.android.ugc.aweme.profile.api.AwemeApi
com.ss.android.ugc.aweme.comment.api.CommentApi
```

### hook-直播消息
```
com.ss.ugc.live.sdk.message.MessageManager
    - dispatchMessage(IMessage iMessage)

com.bytedance.android.livesdkapi.depend.f.a - MessageType
    public enum MessageType {
        HELLO(0, "Hello"),
        SETTING(0, "Setting"),
        GET_SETTING(0, "GetSettting"),
        REQUEST_RECONNECT(0, "RequestReconnect"),
        DEFAULT(0, "--default--"),
        DIGG(0, "WebcastDiggMessage"),
        GIFT(0, "WebcastGiftMessage"), // 礼物消息
        GIFT_GROUP(0, "GiftGroupMessage"),
        GROUP_SHOW_USER_UPDATE(0, "WebcastGroupShowUserUpdateMessage"),
        EXHIBITION_TOP_LEFT(0, "WebcastExhibitionTopLeftMessage"),
        EXHIBITION_CHAT(0, "WebcastExhibitionChatMessage"),
        SYSTEM(0, "SystemMessage"),
        CHAT(0, "WebcastChatMessage"), // 互动消息
        ...
    }
```

### hook-一键新机
```
java hook:
    Build、Build.VERSION、TelephonyManager、NetworkInfo、WifiInfo、Display
    隐藏类 SystemProperties

native hook:
    __system_property_get
    __system_property_find
    __system_property_read

native hook方案：
    iqiyi xhook + xposed

native hook测试：
    手机查看器_2.0.apk，用于测试native hook效果
```

### 风控处理
```
风控说明：
    设备号，风控后app会自动一键新机
    ip，需要sim卡，风控后通过自动飞行切换ip
    缓存，只给dy获取手机信息权限，不要开存储、定位等权限

风控处理：
    视频列表轻微风控，直接重启dy，重新开始
    视频列表严重风控，自动清除缓存、一键新机，自动飞行切换ip，模拟一个全新的运行环境
```

### 声明
```
本项目仅供学习使用，不用做任何其他途径
```

### 参考
【1】[iqiyi xHook](https://github.com/iqiyi/xHook)
