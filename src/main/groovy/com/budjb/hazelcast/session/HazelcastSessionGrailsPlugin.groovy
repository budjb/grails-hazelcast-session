package com.budjb.hazelcast.session

import com.hazelcast.web.SessionListener
import com.hazelcast.web.spring.SpringAwareWebFilter
import grails.plugins.Plugin
import grails.util.Metadata
import org.springframework.boot.context.embedded.FilterRegistrationBean
import org.springframework.boot.context.embedded.ServletListenerRegistrationBean
import org.springframework.security.core.session.SessionRegistryImpl

import javax.servlet.DispatcherType

class HazelcastSessionGrailsPlugin extends Plugin {
    /**
     * Version of Grails the plugin is meant for.
     */
    def grailsVersion = "3.1 > *"

    /**
     * Load order.
     */
    def loadAfter = ['spring-security-core', 'logging']

    /**
     * Plugin author.
     */
    def author = "Bud Byrd"

    /**
     * Plugin email.
     */
    def authorEmail = "bud.byrd@gmail.com"

    /**
     * Plugin description.
     */
    def description = 'Configures a Grails application to use Hazelcast for its session storage.'

    /**
     * Plugin documentation.
     */
    def documentation = "https://budjb.github.io/grails-hazelcast-session"

    /**
     * License.
     */
    def license = "APACHE"

    /**
     * Issues.
     */
    def issueManagement = [system: "GITHUB", url: "https://github.com/budjb/grails-hazelcast-session/issues"]

    /**
     * SCM.
     */
    def scm = [url: "https://github.com/budjb/grails-hazelcast-session"]

    /**
     * Register managed Spring beans.
     */
    @Override
    Closure doWithSpring() {
        { ->
            if (!isPluginEnabled()) {
                println("\nNot loading hazelcast session plugin due to application configuration.")
                return
            }

            hazelcastSessionListener(ServletListenerRegistrationBean) {
                listener = new SessionListener()
            }

            hazelcastSessionFilterRegistrationBean(FilterRegistrationBean, new SpringAwareWebFilter(getFilterInitParams()), []) {
                name = 'hazelcast-session-filter'
                urlPatterns = ['/*']
                dispatcherTypes = EnumSet.of(DispatcherType.FORWARD, DispatcherType.INCLUDE, DispatcherType.REQUEST)
            }

            sessionRegistry(SessionRegistryImpl)
        }
    }

    /**
     * Returns the plugin's configuration tree.
     *
     * @return
     */
    Map getPluginConfiguration() {
        return getConfig().hazelcast.session
    }

    /**
     * Returns whether the plugin is enabled.
     *
     * @return
     */
    boolean isPluginEnabled() {
        return getConfigurationValue(Boolean, getPluginConfiguration().enabled, true)
    }

    /**
     * Returns the session TTL.
     *
     * @return
     */
    Integer getSessionTTL() {
        return getConfigurationValue(Integer, getPluginConfiguration().ttl)
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
     * Returns whether the session is configured  as sticky by the load balancer.
     *
     * @return
     */
    Boolean isSessionSticky() {
        return getConfigurationValue(Boolean, getPluginConfiguration().sticky)
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
    public <T> T getConfigurationValue(Class<T> type, def value, T fallback = null) {
        if (type.isInstance(value)) {
            return value as T
        }
        return fallback
    }

    /**
     * Returns the hazelcast session init params as configured by the application config.
     *
     * @return
     */
    Properties getFilterInitParams() {
        Properties properties = new Properties()

        properties.setProperty('map-name', "${Metadata.current.getApplicationName()}-sessions")

        if (getInstanceName()) {
            properties.setProperty('instance-name', getInstanceName())
        }

        if (getSessionTTL() != null) {
            properties.setProperty('session-ttl-seconds', getSessionTTL().toString())
        }

        if (isSessionSticky() != null) {
            properties.setProperty('sticky-session', isSessionSticky().toString())
        }

        return properties
    }
}
