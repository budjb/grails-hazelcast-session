package com.budjb.hazelcast.session

import grails.plugins.Plugin

class HazelcastSessionGrailsPlugin extends Plugin {
    /**
     * Version of Grails the plugin is meant for.
     */
    def grailsVersion = "3.1 > *"

    /**
     * Load order.
     */
    def loadAfter = ['spring-security-core', 'logging', 'hazelcast']

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

    @Override
    Closure doWithSpring() {
        { ->
            grailsHazelcastSessionConfig(GrailsHazelcastSessionConfig)
        }
    }
}
