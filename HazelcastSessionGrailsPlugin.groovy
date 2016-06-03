import com.hazelcast.config.Config
import com.hazelcast.core.Hazelcast
import com.hazelcast.web.SessionListener
import com.hazelcast.web.spring.SpringAwareWebFilter
import grails.util.Metadata
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.session.SessionRegistryImpl

/**
 * Hazelcast session plugin descriptor class.
 */
class HazelcastSessionGrailsPlugin {
    /**
     * Name of the hazelcast instance required for the session cluster.
     */
    static final String HAZELCAST_INSTANCE_NAME = 'hazelcastSessionInstance'

    /**
     * Plugin version.
     */
    def version = "0.1.1"

    /**
     * Version of Grails the plugin is meant for.
     */
    def grailsVersion = "2.0 > *"

    /**
     * Plugin title.
     */
    def title = "Hazelcast Session Clustering Plugin"

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
     * Logger.
     */
    Logger log = LoggerFactory.getLogger('com.budjb.hazelcastsession.HazelcastSessionGrailsPlugin')

    /**
     * Modify the web.xml file at runtime.
     */
    def doWithWebDescriptor = { xml ->
        if (!isPluginEnabled(application)) {
            println("\nNot loading hazelcast session plugin due to application configuration.")
            return
        }

        def contextParam = xml.'context-param'

        contextParam[contextParam.size() - 1] + {
            'filter' {
                'filter-name'('hazelcast-session-filter')
                'filter-class'(SpringAwareWebFilter.class.getName())
                'init-param' {
                    'param-name'('session-ttl-seconds')
                    'param-value'(getSessionTTL(application).toString())
                }
                'init-param' {
                    'param-name'('sticky-session')
                    'param-value'(isSessionSticky(application).toString())
                }
                if (getCookieName(application)) {
                    'init-param' {
                        'param-name'('cookie-name')
                        'param-value'(getCookieName(application))
                    }
                }
                if (getCookieDomain(application)) {
                    'init-param' {
                        'param-name'('cookie-domain')
                        'param-value'(getCookieDomain(application))
                    }
                }
                'init-param' {
                    'param-name'('instance-name')
                    'param-value'(HAZELCAST_INSTANCE_NAME)
                }
            }

            'filter-mapping' {
                'filter-name'('hazelcast-session-filter')
                'url-pattern'('/*')
                'dispatcher'('FORWARD')
                'dispatcher'('INCLUDE')
                'dispatcher'('REQUEST')
            }

            'listener' {
                'listener-class'(SessionListener.class.getName())
            }
        }
    }

    /**
     * Registers a session registry.
     */
    def doWithSpring = {
        if (!isPluginEnabled(application)) {
            return
        }

        'sessionRegistry'(SessionRegistryImpl)

        String multicastGroup = getInstanceMulticastGroup(application)
        Integer multicastPort = getInstanceMulticastPort(application)
        String groupName = getInstanceGroupName(application)
        String groupPassword = getInstanceGroupPassword(application)
        Config config = new Config()
        config.setInstanceName(HAZELCAST_INSTANCE_NAME)
        config.networkConfig.portAutoIncrement = true
        config.networkConfig.join.multicastConfig.enabled = true
        if (multicastGroup) {
            config.networkConfig.join.multicastConfig.multicastGroup = multicastGroup
        }
        if (multicastPort) {
            config.networkConfig.join.multicastConfig.multicastPort = multicastPort
        }
        config.groupConfig.name = groupName
        if (groupPassword) {
            config.groupConfig.password = groupPassword
        }

        hazelcastSessionInstance(Hazelcast) { beanDefinition ->

            beanDefinition.constructorArgs = [config]
            beanDefinition.factoryMethod = 'newHazelcastInstance'
            beanDefinition.singleton = true
        }
    }

    /**
     * Do a sanity check to ensure that the required hazelcast instance exists.
     */
    def doWithApplicationContext = { applicationContext ->
        if (isPluginEnabled(application) && !Hazelcast.getHazelcastInstanceByName(HAZELCAST_INSTANCE_NAME)) {
            throw new IllegalStateException("no Hazelcast instance with name 'hazelcastSessionInstance' found")
        }
    }

    /**
     * Returns the plugin's configuration tree.
     *
     * @return
     */
    Map getPluginConfiguration(GrailsApplication application) {
        return application.getConfig().hazelcast.session
    }

    /**
     * Returns whether the plugin is enabled.
     *
     * @return
     */
    boolean isPluginEnabled(GrailsApplication application) {
        return getConfigurationValue(Boolean, getPluginConfiguration(application), true)
    }

    /**
     * Returns the session TTL.
     *
     * @return
     */
    int getSessionTTL(GrailsApplication application) {
        return getConfigurationValue(Integer, getPluginConfiguration(application).ttl, 1800)
    }

    /**
     * Returns whether the session is configured  as sticky by the load balancer.
     *
     * @return
     */
    boolean isSessionSticky(GrailsApplication application) {
        return getConfigurationValue(Boolean, getPluginConfiguration(application).sticky, true)
    }

    /**
     * Returns the name of the cookie to use for hazelcast sessions.
     *
     * @return
     */
    String getCookieName(GrailsApplication application) {
        return getConfigurationValue(String, getPluginConfiguration(application).cookie.name)
    }

    /**
     * Returns the domain of the cookie to use for hazelcast sessions.
     *
     * @return
     */
    String getCookieDomain(GrailsApplication application) {
        return getConfigurationValue(String, getPluginConfiguration(application).cookie.domain)
    }

    /**
     * Returns the plugin's hazelcast instance config.
     */
    Map getPluginInstanceConfig(GrailsApplication application) {
        return getPluginConfiguration(application).instance
    }

    /**
     * Returns the instance multicast group.
     */
    String getInstanceMulticastGroup(GrailsApplication application) {
        return getConfigurationValue(String, getPluginInstanceConfig(application).multicast.group)
    }

    /**
     * Returns the instance multicast port
     */
    private Integer getInstanceMulticastPort(GrailsApplication application) {
        return getConfigurationValue(Integer, getPluginInstanceConfig(application).multicast.port)
    }

    /**
     * Returns the instance group name.
     */
    String getInstanceGroupName(GrailsApplication application) {
        return getConfigurationValue(String, getPluginInstanceConfig(application).group.name, application.metadata.get(Metadata.APPLICATION_NAME) + "-sessions")
    }

    /**
     * Returns the instance group password.
     */
    String getInstanceGroupPassword(GrailsApplication application) {
        return getConfigurationValue(String, getPluginInstanceConfig(application).group.password)
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
