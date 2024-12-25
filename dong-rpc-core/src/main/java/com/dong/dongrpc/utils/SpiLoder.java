package com.dong.dongrpc.utils;

import cn.hutool.core.io.resource.ResourceUtil;
import com.dong.dongrpc.serializer.DongSerializer;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SpiLoder {

    /**
     * 已经加载进来的类（接口名 -> (key， 实现类)）
     */
    private static Map<String, Map<String, Class<?>>> loderMap = new ConcurrentHashMap<>();

    /**
     * 实例缓存，单例模式，（类路径 -> 对象实例）
     */
    private static Map<String, Object> instanceMap = new ConcurrentHashMap<>();

    /**
     * 系统默认的spi配置文件
     */
    private static final String RPC_SYSTEM_SPI_DIR = "META-INF/rpc/system/";

    /**
     * 自定义的spi配置文件
     */
    private static final String RPC_CUSTOM_SPI_DIR = "META-INF/rpc/custom/";

    /**
     * 扫描路径
     */
    private static final String[] SCAN_DIRS = new String[]{RPC_SYSTEM_SPI_DIR, RPC_CUSTOM_SPI_DIR};

    /**
     * 要动态加载哪些类
     */
    private static final List<Class<?>> LOAD_CLASS_LIST = Arrays.asList(DongSerializer.class);

    /**
     * load所有类型
     */
    public static void loadAll() {
        for (Class<?> clazz : LOAD_CLASS_LIST) {
            load(clazz);
        }
    }

    /**
     * 加载某个类型
     * @param locadClass
     * @return
     */
    public static Map<String, Class<?>> load(Class<?> locadClass) {
        log.info("开始加载类: {}", locadClass.getName());
        Map<String, Class<?>> keyClassMap = new HashMap<>();
        // 遍历扫描路径
        for (String scanDir : SCAN_DIRS) {
            System.out.println("当前文件为：" + scanDir + locadClass.getName());
            // 拿到文件路径
            List<URL> resources = ResourceUtil.getResources(scanDir + locadClass.getName());
            // 读取每个文件
            for (URL resource : resources) {
                try{
                    InputStreamReader inputStreamReader = new InputStreamReader(resource.openStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    // 一行一行读
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] strArray = line.split("=");
                        if (strArray.length > 1){
                            String key = strArray[0];
                            String classPath = strArray[1];
                            keyClassMap.put(key, Class.forName(classPath));
                            log.info("加载类成功：{}", classPath);
                        }
                    }
                }catch (Exception e){
                    log.error("加载类失败: {}", locadClass.getName(), e);
                }
            }
        }
        loderMap.put(locadClass.getName(), keyClassMap);
        return keyClassMap;

    }

    /**
     * 获取实例
     * @param tClass
     * @param key
     * @return
     * @param <T>
     */
    public static <T> T getInstance(Class<T> tClass, String key) {
        String tClassName = tClass.getName();
        // 通过名字拿到加载到的所有类
        Map<String, Class<?>> keyClassMap = loderMap.get(tClassName);
        // 校验（todo 这里可以采用懒加载的方式，单例双检锁）
        if (keyClassMap == null || keyClassMap.isEmpty()) {
            throw new RuntimeException(String.format("spiloder 未找到 %s 类型的接口", tClass.getName()));
        }
        // 不包含key
        if (!keyClassMap.containsKey(key)) {
            throw new RuntimeException(String.format("spiloder 未找到 %s 类型的接口", tClass.getName()));
        }
        // 拿出要加载的实现类
        Class<?> implClass = keyClassMap.get(key);
        String implClassName = implClass.getName();
        // 从实例缓存中拿，没有的话创建一个
        if (!instanceMap.containsKey(implClassName)) {
            try {
                instanceMap.put(implClassName, implClass.newInstance());
            } catch (Exception e) {
                throw new RuntimeException(String.format("spiloder 创建 %s 类型的实例失败", implClassName), e);
            }
        }
        return (T) instanceMap.get(implClassName);
    }


}
