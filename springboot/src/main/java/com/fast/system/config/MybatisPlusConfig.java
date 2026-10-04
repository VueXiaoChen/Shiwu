package com.fast.system.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus 配置
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * MyBatis Plus 拦截器
     * 添加分页插件，支持物理分页
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页拦截器，指定数据库类型为 MySQL
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 设置合理的分页：pageNum<=0 时查第一页，pageNum>总页数时查最后一页
        paginationInterceptor.setOverflow(true);
        interceptor.addInnerInterceptor(paginationInterceptor);
        return interceptor;
    }
}
