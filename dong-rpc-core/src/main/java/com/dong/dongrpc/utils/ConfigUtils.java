package com.dong.dongrpc.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;

/**
 * 配置工具类
 */
public class ConfigUtils {

    /**
     * 加载配置
     * @param tclass
     * @param prefix
     * @return
     * @param <T>
     */
    public static <T> T loadConfig(Class<T> tclass, String prefix) {
        return loadConfig(tclass, prefix, "");
    }

    /**
     * 加载配置，可以区分环境
     * @param tclass
     * @param prefix
     * @param environment
     * @return
     * @param <T>
     */
    public static <T> T loadConfig(Class<T> tclass, String prefix, String environment) {
        return loadConfig(tclass, prefix, environment, "properties");
    }

    /**
     * 加载配置，可以区分文件类型
     * @param tclass
     * @param prefix
     * @param environment
     * @return
     * @param <T>
     */
    public static <T> T loadConfig(Class<T> tclass, String prefix, String environment, String fileType) {
        StringBuilder configFileBuilder = new StringBuilder("application");
        if (StrUtil.isNotBlank(environment)) {
            configFileBuilder.append("-").append(environment);
        }
        configFileBuilder.append(".").append(fileType);
        Props props = new Props(configFileBuilder.toString());
//        props.autoLoad(true);
        return props.toBean(tclass, prefix);
    }
}
