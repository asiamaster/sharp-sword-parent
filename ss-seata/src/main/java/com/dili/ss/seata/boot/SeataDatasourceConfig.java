package com.dili.ss.seata.boot;

import com.alibaba.druid.pool.DruidDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ConditionalOnExpression("'${seata.enable}'=='true'&&!'${spring.datasource.url:}'.equals('')")
public class SeataDatasourceConfig {

    @Autowired
    private DataSourceProperties dataSourceProperties;

    @Bean("druidDataSourceProperties")
    public Map<String, String> druidDataSourceProperties() {
        return new HashMap<>();
    }

    //seata使用的代理数据源
    //由于两个dataSource的Bean和MapperAutoConfiguration有冲突
    //这里使用Map的形式来注入druidDataSourceProperties
    //seata-spring-boot-starter默认开启数据源自动代理，用户若再手动配置DataSourceProxy将会导致异常。
    @Primary
    @Bean("dataSource")
    public DataSourceProxy dataSource(@Qualifier("druidDataSourceProperties") Map<String, String> druidDataSourceProperties) throws InvocationTargetException, IllegalAccessException {
        DruidDataSource druidDataSource = new DruidDataSource();
        BeanUtils.copyProperties(dataSourceProperties, druidDataSource);
        org.apache.commons.beanutils.BeanUtils.populate(druidDataSource, druidDataSourceProperties);
        return new DataSourceProxy(druidDataSource);
    }

}
