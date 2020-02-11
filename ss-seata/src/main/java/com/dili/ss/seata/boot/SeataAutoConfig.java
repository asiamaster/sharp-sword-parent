package com.dili.ss.seata.boot;

import io.seata.spring.annotation.GlobalTransactionScanner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@ConditionalOnExpression("'${seata.enable}'=='true'")
public class SeataAutoConfig {

    /**
     * 初始化全局事务扫描器
     * init global transaction scanner
     * seata-spring-boot-starter是使用springboot自动装配来简化seata-all的复杂配置。1.0.0可用于替换seata-all，GlobalTransactionScanner自动初始化（依赖SpringUtils）若其他途径实现GlobalTransactionScanner初始化，请保证io.seata.spring.boot.autoconfigure.util.SpringUtils先初始化；
     * @Return: GlobalTransactionScanner
     */
    @Bean
    @DependsOn({"springUtils"})
    public GlobalTransactionScanner globalTransactionScanner(Environment env){
        return new GlobalTransactionScanner(env.getProperty("spring.application.name"), env.getProperty("spring.cloud.alibaba.seata.tx-service-group"));
    }

    /**
     * http请求拦截器，用于绑定xid
     * @return
     */
    @Bean
    public OncePerRequestFilter seataXidFilter(){
        return new SeataXidFilter();
    }
}
