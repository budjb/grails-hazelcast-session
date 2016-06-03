import com.hazelcast.web.SessionListener
import com.hazelcast.web.spring.SpringAwareWebFilter
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
        if (!isPluginEnabled()) {
            log.info("not loading hazelcast session plugin due to application configuration")
        }

        def contextParam = xml.'context-param'

        contextParam[contextParam.size() - 1] + {
            'filter' {
                'filter-name'('hazelcast-session-filter')
                'filter-class'(SpringAwareWebFilter.class.getName())
                'init-param' {
                    'param-name'('session-ttl-seconds')
                    'param-value'(getSessionTTL().toString())
                }
                'init-param' {
                    'param-name'('sticky-session')
                    'param-value'(isSessionSticky().toString())
                }
                if (getCookieName()) {
                    'init-param' {
                        'param-name'('cookie-name')
                        'param-value'(getCookieName())
                    }
                }
                if (getCookieDomain()) {
                    'init-param' {
                        'param-name'('cookie-domain')
                        'param-value'(getCookieDomain())
                    }
                }
                'init-param' {
                    'param-name'('instance-name')
                    'param-value'('hazelcastSessionCache')
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
        'sessionRegistry'(SessionRegistryImpl)
    }

    /**
     * Returns the plugin's configuration tree.
     *
     * @return
     */
    Map getPluginConfiguration() {
        return application.getConfig().hazelcast.session
    }

    /**
     * Returns whether the plugin is enabled.
     *
     * @return
     */
    boolean isPluginEnabled() {
        def enabled = getPluginConfiguration().enabled
        if (enabled instanceof Boolean) {
            return enabled
        }
        return true
    }

    /**
     * Returns the session TTL.
     *
     * @return
     */
    int getSessionTTL() {
        def ttl = getPluginConfiguration().ttl

        if (ttl instanceof Integer) {
            return ttl
        }

        return 1800 // 30 minutes
    }

    /**
     * Returns whether the session is configured  as sticky by the load balancer.
     *
     * @return
     */
    boolean isSessionSticky() {
        def sticky = getPluginConfiguration().sticky

        if (sticky instanceof Boolean) {
            return sticky
        }

        return true
    }

    /**
     * Returns the name of the cookie to use for hazelcast sessions.
     *
     * @return
     */
    String getCookieName() {
        def name = getPluginConfiguration().cookie.name

        if (name instanceof String) {
            return name
        }

        return null
    }

    /**
     * Returns the domain of the cookie to use for hazelcast sessions.
     *
     * @return
     */
    String getCookieDomain() {
        def name = getPluginConfiguration().cookie.domain

        if (name instanceof String) {
            return name
        }

        return null
    }
}
