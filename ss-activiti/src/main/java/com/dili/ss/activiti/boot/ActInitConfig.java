package com.dili.ss.activiti.boot;

import org.activiti.image.ProcessDiagramGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@DependsOn("initConfig")
public class ActInitConfig {

    @Autowired
    public Environment env;

    @Bean
    @ConditionalOnExpression("'${activiti.enable}'=='true'")
    public ProcessDiagramGenerator getProcessDiagramGenerator() throws IllegalAccessException, InstantiationException {
        init();
        return (ProcessDiagramGenerator) ((Class<ProcessDiagramGenerator>)B.b.g("customProcessDiagramGenerator")).newInstance();
    }

    public void init(){
        B.daeif("script/ai", null, env);
    }
}
