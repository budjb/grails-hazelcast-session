package com.budjb.hazelcast.session

import com.hazelcast.config.Config
import com.hazelcast.config.MapAttributeConfig
import com.hazelcast.config.MapConfig
import com.hazelcast.config.MapIndexConfig
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import grails.core.GrailsApplication
import org.grails.plugins.hazelcast.HazelcastConfigLoader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.embedded.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.session.hazelcast.HazelcastSessionRepository
import org.springframework.session.hazelcast.PrincipalNameExtractor
import org.springframework.session.hazelcast.config.annotation.web.http.EnableHazelcastHttpSession
import org.springframework.session.web.http.SessionRepositoryFilter
import org.springframework.stereotype.Component

@Component
@EnableHazelcastHttpSession
class GrailsHazelcastSessionConfig {
    /**
     * Grails application bean.
     */
    @Autowired
    GrailsApplication grailsApplication

    /**
     * Creates the Hazelcast instance required for Spring Session to work.
     */
    @Bean
    HazelcastInstance hazelcastInstance(HazelcastConfigLoader hazelcastConfigLoader) {
        String instanceName = getInstanceName()

        Config config = hazelcastConfigLoader.retrieveAndRemoveInstanceConfiguration(instanceName)

        if (config == null) {
            throw new RuntimeException("missing Hazelcast configuration for instance named \"$instanceName\"")
        }

        MapAttributeConfig attributeConfig = new MapAttributeConfig()
        attributeConfig.setName(HazelcastSessionRepository.PRINCIPAL_NAME_ATTRIBUTE)
        attributeConfig.setExtractor(PrincipalNameExtractor.class.getName())

        MapConfig mapConfig = config.getMapConfig("spring:session:sessions")
        mapConfig.addMapAttributeConfig(attributeConfig)
        mapConfig.addMapIndexConfig(new MapIndexConfig(HazelcastSessionRepository.PRINCIPAL_NAME_ATTRIBUTE, false))

        return Hazelcast.newHazelcastInstance(config)
    }

    /**
     * Registers the session repository filter right before the Grails session filter
     * so that the session is properly wrapped.
     */
    @Bean
    FilterRegistrationBean springSessionFilter(SessionRepositoryFilter filter) {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean()
        registrationBean.setFilter(filter)
        registrationBean.setOrder(FilterRegistrationBean.REQUEST_WRAPPER_FILTER_MAX_ORDER + 29)
        return registrationBean
    }

    /**
     * Returns the plugin's configuration tree.
     *
     * @return
     */
    Map getPluginConfiguration() {
        return grailsApplication.getConfig().hazelcast.session
    }

    /**
     * Returns the name of the Hazelcast instance to use.
     *
     * @return
     */
    String getInstanceName() {
        return getConfigurationValue(String, getPluginConfiguration().instanceName)
    }

    /**
     * Returns the given configuration value if it is the proper class type. If it is not,
     * the fallback parameter is returned.
     *
     * @param type
     * @param value
     * @param fallback
     * @return
     */
    protected <T> T getConfigurationValue(Class<T> type, def value, T fallback = null) {
        if (type.isInstance(value)) {
            return value as T
        }
        return fallback
    }
}
