package com.budjb.hazelcast.session

import com.hazelcast.web.WebFilter
import org.springframework.beans.factory.FactoryBean

class WebFilterFactoryBean implements FactoryBean<WebFilter>  {
    Properties filterProperties = new Properties()

    @Override
    WebFilter getObject() throws Exception {
        return new WebFilter(filterProperties)
    }

    @Override
    Class<?> getObjectType() {
        return WebFilter
    }

    @Override
    boolean isSingleton() {
        return true
    }
}
