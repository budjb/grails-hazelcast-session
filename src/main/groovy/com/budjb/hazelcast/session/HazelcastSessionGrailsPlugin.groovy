package com.budjb.hazelcast.session

import com.hazelcast.core.Hazelcast
import com.hazelcast.web.SessionListener
import grails.plugins.Plugin
import org.springframework.boot.context.embedded.DelegatingFilterProxyRegistrationBean
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

            Map initParams = getFilterInitParams()
            delegateHazelcastSessionFilter(DelegatingFilterProxyRegistrationBean, 'hazelcastSessionFilter', []) {
                name = 'hazelcast-session-filter'
                initParameters = initParams
                urlPatterns = ['/*']
                dispatcherTypes = EnumSet.of(DispatcherType.FORWARD, DispatcherType.INCLUDE, DispatcherType.REQUEST)
            }

            hazelcastSessionFilter(HazelcastSessionFilter) {
                delegate.grailsApplication = grailsApplication

            }

            sessionRegistry(SessionRegistryImpl)
        }
    }

    /**
     * Do a sanity check to ensure that the required hazelcast instance exists.
     */
    void doWithApplicationContext() {
        if (isPluginEnabled() && !Hazelcast.getHazelcastInstanceByName(HazelcastSessionFilter.HAZELCAST_INSTANCE_NAME)) {
            throw new IllegalStateException("no Hazelcast instance with name 'hazelcastSessionInstance' found")
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
     * Returns whether the session is configured  as sticky by the load balancer.
     *
     * @return
     */
    Boolean isSessionSticky() {
        return getConfigurationValue(Boolean, getPluginConfiguration().sticky)
    }

    /**
     * Returns the name of the cookie to use for hazelcast sessions.
     *
     * @return
     */
    String getCookieName() {
        return getConfigurationValue(String, getPluginConfiguration().cookie.name)
    }

    /**
     * Returns the domain of the cookie to use for hazelcast sessions.
     *
     * @return
     */
    String getCookieDomain() {
        return getConfigurationValue(String, getPluginConfiguration().cookie.domain)
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
    Map getFilterInitParams() {
        Map params = [
            'instance-name': HazelcastSessionFilter.HAZELCAST_INSTANCE_NAME
        ]

        if (getSessionTTL() != null) {
            params.put('session-ttl-seconds', getSessionTTL().toString())
        }

        if (isSessionSticky() != null) {
            params.put('sticky-session', isSessionSticky().toString())
        }

        if (getCookieName() != null) {
            params.put('cookie-name', getCookieName())
        }

        if (getCookieDomain() != null) {
            params.put('cookie-domain', getCookieDomain())
        }

        return params
    }
}
