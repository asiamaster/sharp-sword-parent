package com.dili.ss.boot;

import com.alibaba.druid.pool.DruidDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * 针对druid监控的数据源配置
 * Created by asiam on 2017/2/24 0024.
 */
//@Configuration
//@ConditionalOnExpression("'${druid-filter.enable}'=='true'")
public class DruidConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(DruidConfig.class);

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;
    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;
    @Value("${spring.datasource.initialSize:1}")
    private int initialSize;
    @Value("${spring.datasource.minIdle:0}")
    private int minIdle;
    @Value("${spring.datasource.maxActive:8}")
    private int maxActive;
    @Value("${spring.datasource.maxWait:-1}")
    private int maxWait;
    @Value("${spring.datasource.validationQuery:select 1}")
    private String validationQuery;
    @Value("${spring.datasource.testOnBorrow:false}")
    private boolean testOnBorrow;
    @Value("${spring.datasource.testOnReturn:false}")
    private boolean testOnReturn;
    @Value("${spring.datasource.testWhileIdle:true}")
    private boolean testWhileIdle;
    @Value("${spring.datasource.timeBetweenEvictionRunsMillis:60000}")
    private long timeBetweenEvictionRunsMillis;
    @Value("${spring.datasource.minEvictableIdleTimeMillis:1800000}")
    private long minEvictableIdleTimeMillis;
    @Value("${spring.datasource.maxEvictableIdleTimeMillis:25200000}")
    private long maxEvictableIdleTimeMillis;
//    是否自动回收超时连接
    @Value("${spring.datasource.removeAbandoned:true}")
    private boolean removeAbandoned;
    @Value("${spring.datasource.removeAbandonedTimeout:300}")
    private int removeAbandonedTimeout;
    @Value("${spring.datasource.logAbandoned:false}")
    private boolean logAbandoned;
    @Value("${spring.datasource.filters}")
    private String filters;
    @Value("${spring.datasource.poolPreparedStatements:true}")
    private boolean poolPreparedStatements;
    @Value("${spring.datasource.maxPoolPreparedStatementPerConnectionSize:10}")
    private int maxPoolPreparedStatementPerConnectionSize;

    @Bean
    @Primary
    public DataSource dataSource() {
        DruidDataSource datasource = new DruidDataSource();

        datasource.setDriverClassName(driverClassName);
        datasource.setUrl(url);
        datasource.setUsername(username);
        datasource.setPassword(password);
        //其它配置
        datasource.setInitialSize(initialSize);
        datasource.setMinIdle(minIdle);
        datasource.setMaxActive(maxActive);
        datasource.setMaxWait(maxWait);
        datasource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        datasource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        datasource.setValidationQuery(validationQuery);
        datasource.setTestWhileIdle(testWhileIdle);
        datasource.setTestOnBorrow(testOnBorrow);
        datasource.setTestOnReturn(testOnReturn);
        datasource.setRemoveAbandoned(removeAbandoned);
        datasource.setLogAbandoned(logAbandoned);
        datasource.setRemoveAbandonedTimeout(removeAbandonedTimeout);
        datasource.setPoolPreparedStatements(poolPreparedStatements);
        datasource.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);
        try {
            datasource.setFilters(filters);
        } catch (SQLException e) {
            LOGGER.error("druid configuration initialization filter", e);
        }
        return datasource;
    }

//    @Bean("dataSource")
//    @Primary
//    @ConfigurationProperties(prefix = "spring.datasource")
//    public DruidDataSource druidDataSource() {
//        DruidDataSource druidDataSource = new DruidDataSource();
//        return druidDataSource;
//    }

}
