package com.budjb.hazelcast.session

import com.hazelcast.config.Config
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import grails.core.GrailsApplication
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.filter.DelegatingFilterProxy

import javax.servlet.ServletException

class HazelcastSessionFilter extends DelegatingFilterProxy {
    /**
     * Hazelcast instance name.
     */
    static final String HAZELCAST_INSTANCE_NAME = 'hazelcastSessionInstance'

    /**
     * Grails application.
     */
    GrailsApplication grailsApplication

    /**
     * Logger.
     */
    Logger log = LoggerFactory.getLogger(HazelcastSessionFilter)

    /**
     * Hazelcast instance.
     */
    HazelcastInstance hazelcastInstance

    /**
     * Initializes the filter bean.
     *
     * @throws ServletException
     */
    @Override
    protected void initFilterBean() throws ServletException {
        if (isInstanceEnabled()) {
            hazelcastInstance = Hazelcast.newHazelcastInstance(loadConfig())
        }
        else {
            hazelcastInstance = Hazelcast.getHazelcastInstanceByName(HAZELCAST_INSTANCE_NAME)
            if (!hazelcastInstance) {
                throw new IllegalStateException("no Hazelcast instance with name '${HAZELCAST_INSTANCE_NAME}' was found")
            }
        }

        super.initFilterBean()
    }

    /**
     * Creates a hazelcast configuration based on the the application configuration.
     *
     * @return
     */
    protected Config loadConfig() {
        Map conf = getConfig()

        Config config = new Config()
        config.setInstanceName(HAZELCAST_INSTANCE_NAME)

        // The encryption functionality is a feature only available with the enterprise edition of the library.
        //SymmetricEncryptionConfig encryptionConfig = new SymmetricEncryptionConfig()
        //encryptionConfig.enabled = true
        //encryptionConfig.algorithm = 'PBEWithHmacSHA256AndAES_256'
        //encryptionConfig.salt = 'TODO'
        //encryptionConfig.iterationCount = 1
        //encryptionConfig.password = 'TODO'
        //config.networkConfig.symmetricEncryptionConfig = encryptionConfig

        config.networkConfig.port = getConfigurationValue(Integer, conf.port, config.networkConfig.port)
        config.networkConfig.portAutoIncrement = getConfigurationValue(Boolean, conf.portAutoIncrement, config.networkConfig.portAutoIncrement)

        List<String> interfaces = getConfigurationValue(List, conf.interfaces, [])
        if (interfaces) {
            config.networkConfig.interfaces.enabled = true
            interfaces.each {
                config.networkConfig.interfaces.addInterface(it)
            }
        }

        config.networkConfig.join.multicastConfig.enabled = getConfigurationValue(Boolean, conf.multicast.enabled, true)
        if (config.networkConfig.join.multicastConfig.enabled) {
            String multicastGroup = getConfigurationValue(String, conf.multicast.group)
            Integer multicastPort = getConfigurationValue(Integer, conf.multicast.port)

            config.networkConfig.join.multicastConfig.enabled = true
            if (multicastGroup) {
                config.networkConfig.join.multicastConfig.multicastGroup = multicastGroup
            }
            if (multicastPort) {
                config.networkConfig.join.multicastConfig.multicastPort = multicastPort
            }
        }

        if (getConfigurationValue(Boolean, conf.tcp.enabled)) {
            List<String> members = getConfigurationValue(List, conf.tcp.members, [])

            config.networkConfig.join.tcpIpConfig.enabled = true
            members.each { config.networkConfig.join.tcpIpConfig.addMember(it) }
        }

        if (getConfigurationValue(String, conf.group.name)) {
            config.groupConfig.name = getConfigurationValue(String, conf.group.name)
        }
        if (getConfigurationValue(String, conf.group.password)) {
            config.groupConfig.password = getConfigurationValue(String, conf.group.password)
        }

        log.trace("Hazelcast Session Configuration: ${config.toString()}")

        return config
    }

    /**
     * Returns the root of the application config.
     *
     * @return
     */
    Map getConfig() {
        return grailsApplication.config.hazelcast.session.instance
    }

    /**
     * Returns the given configuration value if it is the proper class type. If it is not,
     * the fallback parameter is returned.
     *
     * @param type Requested class type.
     * @param value Value as configured (or not) in the application config.
     * @param fallback A default value to return if <code>value</code> is not suitable.
     * @return
     */
    protected <T> T getConfigurationValue(Class<T> type, def value, T fallback = null) {
        if (type.isInstance(value)) {
            return value as T
        }
        return fallback
    }

    /**
     * Returns whether a hazelcast instance should be created by the plugin.
     */
    boolean isInstanceEnabled() {
        return getConfigurationValue(Boolean, getConfig().enabled, true)
    }
}
