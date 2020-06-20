package com.dili.ss.boot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class InitConfig {
    public static boolean isInit = false;

    @Autowired
    public Environment env;

    @PostConstruct
    public void init(){
        if(isInit){
            return;
        }
        isInit = true;
        B.daeif("script/i", null, env);
    }
}
