package com.niroren.riskengine.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ContextHolder implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ContextHolder.applicationContext = applicationContext;
    }

    public static <T> T getBean(Class<T> beanClass) {
        return applicationContext.getBean(beanClass);
    }
}
