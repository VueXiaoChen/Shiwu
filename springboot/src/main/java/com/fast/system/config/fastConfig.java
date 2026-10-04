package com.fast.system.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 读取项目相关配置
 *
 * 这个类是一个"配置读取器", 专门负责读取application.yml
 * 配置文件中的自定义配置项 可以把这看做系统的"配置文件翻译官"
 */
@Component //告诉spring: "我是一个组件, 请把我放到容器里管理"
@ConfigurationProperties(prefix = "fast") //核心注解: 告诉spring: "请读取项目配置文件中以fast开头的配置项"
public class fastConfig {

    /**
     * 文件上传路径配置
     *
     * 这个变量对应配置文件中的: fast.profile
     * 注意: 这里使用率static静态变量, 这样可以通过类名直接访问
     * 比如: fastConfig.profile
     */
    private static String profile;  // 存储上传文件的基本路径

    public static String getProfile() {
        return profile;
    }

    /**
     * 设置上传路径
     *
     * spring会自动调用这个方法, 把配置文件中的值传进来
     * 过程:
     * 1. spring启动时, 读取配置文件
     * 2. 找到fast.profile = ./file
     * 3. 创建fastConfig对象
     * 4. 调用setProfile("./file")
     *
     * 特别处理：相对路径（如 ./file）会基于项目根目录解析，
     * 而不是基于当前工作目录。这样无论是从 IDEA 启动
     * 还是通过 mvn spring-boot:run 启动, 文件都上传到同一个位置。
     *
     * @param profile 从配置文件中读取到的上传路径
     */
    public void setProfile(String profile) {
        // 如果是相对路径（以 ./ 开头），需要特殊处理
        // 因为 mvn spring-boot:run 的工作目录是 Maven 子模块目录，
        // 而 IDEA 的工作目录是项目根目录，会导致上传路径不一致
        if (profile != null && (profile.startsWith("./") || profile.startsWith(".\\"))) {
            String userDir = System.getProperty("user.dir");
            // 去掉相对路径前缀 ./ 或 .\
            String relativePath = profile.substring(2);

            // 聪明的检测方式：看看当前目录下有没有 pom.xml
            // 有 = 当前在 Maven 子模块里启动（比如 mvn spring-boot:run）
            // 没 = 当前在项目根目录启动（比如 IDEA 直接运行）
            // 这样不管子模块目录叫啥名字，都能正确识别！
            File pomFile = new File(userDir, "pom.xml");
            if (pomFile.exists()) {
                // 回退到项目根目录（Maven 子模块的父目录）
                userDir = new File(userDir).getParent();
            }

            // 拼出最终绝对路径: 项目根目录/file
            profile = userDir + File.separator + relativePath;
        }
        // 把配置值设置到静态变量中
        fastConfig.profile = profile;
    }
}
