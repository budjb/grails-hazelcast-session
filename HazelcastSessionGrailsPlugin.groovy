import com.budjb.hazelcastsession.SessionInstanceFactory
import com.hazelcast.core.Hazelcast
import com.hazelcast.web.SessionListener
import com.hazelcast.web.spring.SpringAwareWebFilter
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.session.SessionRegistryImpl

/**
 * Hazelcast session plugin descriptor class.
 */
class HazelcastSessionGrailsPlugin {
    /**
     * Plugin version.
     */
    def version = "0.1.7"

    /**
     * Version of Grails the plugin is meant for.
     */
    def grailsVersion = "2.0 > *"

    /**
     * Load order.
     */
    def loadAfter = ['spring-security-core', 'logging']

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
                    'param-name'('instance-name')
                    'param-value'(SessionInstanceFactory.HAZELCAST_INSTANCE_NAME)
                }
                if (getSessionTTL(application) != null) {
                    'init-param' {
                        'param-name'('session-ttl-seconds')
                        'param-value'(getSessionTTL(application).toString())
                    }
                }
                if (isSessionSticky(application) != null) {
                    'init-param' {
                        'param-name'('sticky-session')
                        'param-value'(isSessionSticky(application).toString())
                    }
                }
                if (getCookieName(application) != null) {
                    'init-param' {
                        'param-name'('cookie-name')
                        'param-value'(getCookieName(application))
                    }
                }
                if (getCookieDomain(application) != null) {
                    'init-param' {
                        'param-name'('cookie-domain')
                        'param-value'(getCookieDomain(application))
                    }
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
     * Register managed Spring beans.
     */
    def doWithSpring = {
        if (!isPluginEnabled(application)) {
            return
        }

        'sessionRegistry'(SessionRegistryImpl)

        if (isInstanceEnabled(application)) {
            'hazelcastSessionCache'(SessionInstanceFactory) {
                grailsApplication = ref('grailsApplication')
            }
        }
    }

    /**
     * Do a sanity check to ensure that the required hazelcast instance exists.
     */
    def doWithApplicationContext = { applicationContext ->
        if (isPluginEnabled(application) && !Hazelcast.getHazelcastInstanceByName(SessionInstanceFactory.HAZELCAST_INSTANCE_NAME)) {
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
        return getConfigurationValue(Boolean, getPluginConfiguration(application).enabled, true)
    }

    /**
     * Returns the session TTL.
     *
     * @return
     */
    Integer getSessionTTL(GrailsApplication application) {
        return getConfigurationValue(Integer, getPluginConfiguration(application).ttl)
    }

    /**
     * Returns whether the session is configured  as sticky by the load balancer.
     *
     * @return
     */
    Boolean isSessionSticky(GrailsApplication application) {
        return getConfigurationValue(Boolean, getPluginConfiguration(application).sticky)
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
     * Returns whether a hazelcast instance should be created by the plugin.
     */
    boolean isInstanceEnabled(GrailsApplication application) {
        return getConfigurationValue(Boolean, getPluginConfiguration(application).instance.enabled, true)
    }
}
