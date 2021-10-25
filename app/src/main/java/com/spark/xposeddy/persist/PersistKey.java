package com.spark.xposeddy.persist;

public class PersistKey {
    // 服务器地址
    public static final String DOMAIN = "domain";
    // 设备编码
    public static final String DEVICE_ID = "device_id";
    // 类型编码
    public static final String TYPE_ID = "type_id";
    // 需要采集的视频评论页数
    public static final String COMMENT_PAGE = "comment_page";
    // 需要采集的达人视频数
    public static final String VIDEO_COUNT = "video_count";
    // 采集多少秒以内的评论，单位 s
    public static final String SAMPLE_DIFF = "sample_diff";
    // 采集评论的线程数
    public static final String COMMENT_THREAD_NUM = "comment_thread_num";
    // 评论采集与评论采集之间的间隔，单位 ms
    public static final String COMMENT_SMALL_INTERVAL = "comment_small_interval";
    // 评论采集完一圈后执行间隔，单位 s
    public static final String COMMENT_LARGE_INTERVAL = "comment_large_interval";
    // 视频更新与视频更新之间的小间隔，单位 s
    public static final String AWEME_SMALL_INTERVAL = "aweme_small_interval";
    // 视频更新完一圈后执行间隔，单位 s
    public static final String AWEME_LARGE_INTERVAL = "aweme_large_interval";
    // 一键新机执行间隔，单位 s
    public static final String NEW_PHONE_INTERVAL = "new_phone_interval";

    // 需要采集的达人列表
    public static final String SAMPLE_ACCOUNTS = "sample_accounts";
    // 需要采集的视频列表
    public static final String SAMPLE_VIDEOS = "sample_videos";

    // 自动飞行是否启用
    public static final String AUTO_AIRPLANE_STATUS = "auto_airplane_status";

    // 缓存需要采集达人列表的信息
    public static final String CACHE_PROFILE = "cache_profile";
    // 缓存所有需要采集的视频
    public static final String CACHE_AWEME = "cache_aweme";
    // 缓存一键改机的设备信息
    public static final String PHONE_INFO = "phone_info";
    // 缓存一键改机的时间
    public static final String PHONE_INFO_TICK = "phone_info_tick";
    // 缓存测试一键改机的设备信息
    public static final String PHONE_INFO_TEST = "phone_info_test";

}
