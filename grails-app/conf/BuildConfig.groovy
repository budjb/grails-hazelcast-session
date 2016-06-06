grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.project.dependency.resolver = "maven"
grails.project.dependency.resolution = {
    inherits("global")
    log "warn"
    repositories {
        grailsCentral()
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        compile 'com.hazelcast:hazelcast:3.6.3'
        compile 'com.hazelcast:hazelcast-wm:3.6.3'

        // This is from the Spring Security plugin.
        compile "org.springframework.security:spring-security-web:3.2.9.RELEASE", {
            excludes 'aopalliance', 'commons-codec', 'commons-logging', 'fest-assert', 'groovy', 'hsqldb',
                'jcl-over-slf4j', 'junit', 'logback-classic', 'mockito-core', 'powermock-api-mockito',
                'powermock-api-support', 'powermock-core', 'powermock-module-junit4',
                'powermock-module-junit4-common', 'powermock-reflect', 'spock-core', 'spring-beans',
                'spring-context', 'spring-core', 'spring-expression', 'spring-jdbc',
                'spring-test', 'spring-tx', 'spring-web', 'spring-webmvc', 'tomcat-servlet-api'
        }
    }

    plugins {
        build ":release:3.0.1", {
            export = false
        }
    }
}
