package com.dili.ss.component;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Created by asiam on 2018/3/23 0023.
 */
@Component
public class InitApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            ((Class)B.b.g("cleaner")).getMethod("execute").invoke(null);
        } catch (Exception e) {
        }
    }


}