package com.budjb.hazelcast.session

import grails.artefact.Interceptor
import grails.web.mvc.FlashScope
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder

class HazelcastFlashInterceptor implements Interceptor {
    /**
     * Constructor.
     */
    HazelcastFlashInterceptor() {
        matchAll()
    }

    /**
     * Stores the flash scope in session after all view processing has completed.
     */
    @Override
    void afterView() {
        GrailsWebRequest webRequest = (GrailsWebRequest) RequestContextHolder.currentRequestAttributes()
        FlashScope flashScope = webRequest.getFlashScope()
        session.setAttribute(GrailsApplicationAttributes.FLASH_SCOPE, flashScope)
    }
}
