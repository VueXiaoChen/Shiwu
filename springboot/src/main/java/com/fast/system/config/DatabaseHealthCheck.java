package com.fast.system.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * 数据库健康检查组件
 * 在应用启动时检查数据库连接配置是否正确
 */
@Component
public class DatabaseHealthCheck implements CommandLineRunner {

    @Autowired
    private Environment env;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("正在检查数据库连接配置...");

        // 从环境变量中获取数据库配置
        String url = env.getProperty("spring.datasource.url");
        String username = env.getProperty("spring.datasource.username");
        String password = env.getProperty("spring.datasource.password");

        // 检查是否所有必需的属性都已配置
        if (url == null || username == null || password == null) {
            System.err.println("✗ 数据库配置缺失或不完整，请检查 application.yml 文件中的 spring.datasource 配置项");
            System.err.println("  必需配置项包括:");
            System.err.println("    - url");
            System.err.println("    - username");
            System.err.println("    - password");

            if (url == null) System.err.println("  缺失: spring.datasource.url");
            if (username == null) System.err.println("  缺失: spring.datasource.username");
            if (password == null) System.err.println("  缺失: spring.datasource.password");

            System.exit(1);
            return;
        }

        // 创建临时数据源进行连接测试
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        // 设置一些基本的连接池参数以确保快速测试
        dataSource.setInitialSize(1);
        dataSource.setMinIdle(1);
        dataSource.setMaxActive(5);
        dataSource.setMaxWait(5000); // 5秒超时

        try {
            // 测试连接
            Connection connection = dataSource.getConnection();
            DatabaseMetaData metaData = connection.getMetaData();

            System.out.println("✓ 数据库连接配置检查通过！");
            System.out.println("  数据库产品名称: " + metaData.getDatabaseProductName());
            System.out.println("  数据库版本: " + metaData.getDatabaseProductVersion());
            System.out.println("  连接URL: " + url);
            System.out.println("  用户名: " + username);

            connection.close();
        } catch (SQLException e) {
            System.err.println("✗ 数据库连接配置存在问题，请检查以下信息：");
            System.err.println("  数据库URL: " + url);
            System.err.println("  用户名: " + username);
            System.err.println("  错误详情: " + e.getMessage());
            System.err.println("\n请确保：");
            System.err.println("  1. MySQL服务已启动");
            System.err.println("  2. 数据库URL配置正确");
            System.err.println("  3. 用户名和密码正确");
            System.err.println("  4. 数据库'fast'已创建");
            System.err.println("  5. 网络连接正常");
            System.err.println("  6. MySQL驱动版本兼容");

            // 关闭应用
            System.exit(1);
        } finally {
            dataSource.close();
        }
    }
}
