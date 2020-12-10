package com.dili.ss.seata.boot;

import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 配置http请求过滤器，用于绑定xid
 * 由于spring-cloud-alibaba-seata已经实现(SeataHandlerInterceptor)，该类停用
 * <groupId>com.alibaba.cloud</groupId>
 * <artifactId>spring-cloud-alibaba-seata</artifactId>
 * <version>2.2.0.RELEASE</version>
 */
//@Configuration
//@ConditionalOnExpression("'${seata.enabled}'=='true'")
public class SeataAutoConfig {

    /**
     * http请求过滤器，用于绑定xid
     * @return
     */
    @Bean(name="seataXidFilter")
    public OncePerRequestFilter seataXidFilter(){
        return new SeataXidFilter();
    }

    /**
     * 初始化全局事务扫描器
     * init global transaction scanner
     * seata-spring-boot-starter是使用springboot自动装配来简化seata-all的复杂配置。1.0.0可用于替换seata-all，GlobalTransactionScanner自动初始化（依赖SpringUtils）若其他途径实现GlobalTransactionScanner初始化，请保证io.seata.spring.boot.autoconfigure.util.SpringUtils先初始化；
     * @Return: GlobalTransactionScanner
     */
//    @Bean
//    @DependsOn({"springUtils"})
//    public GlobalTransactionScanner globalTransactionScanner(Environment env){
//        return new GlobalTransactionScanner(env.getProperty("spring.application.name"), env.getProperty("spring.cloud.alibaba.seata.tx-service-group"));
//    }

}
