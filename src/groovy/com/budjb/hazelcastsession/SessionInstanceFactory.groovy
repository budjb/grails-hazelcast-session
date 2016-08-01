package com.budjb.hazelcastsession

import com.hazelcast.config.Config
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.beans.factory.config.AbstractFactoryBean

@Slf4j
class SessionInstanceFactory extends AbstractFactoryBean<HazelcastInstance> {
    /**
     * Hazelcast instance name.
     */
    static final String HAZELCAST_INSTANCE_NAME = 'hazelcastSessionInstance'

    /**
     * Grails application.
     */
    GrailsApplication grailsApplication

    /**
     * Constructor.
     */
    SessionInstanceFactory() {
        setSingleton(true)
    }

    /**
     * Returns the class type that this factory creates.
     *
     * @return
     */
    @Override
    Class<?> getObjectType() {
        return HazelcastInstance
    }

    /**
     *
     * @return
     * @throws Exception
     */
    @Override
    protected HazelcastInstance createInstance() throws Exception {
        return Hazelcast.newHazelcastInstance(loadConfig())
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

        log.debug("Hazelcast Session Configuration: ${config.toString()}")

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
}
