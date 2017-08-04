package com.budjb.hazelcast.session

import grails.artefact.Interceptor
import grails.web.mvc.FlashScope
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.GrailsApplicationAttributes
import org.springframework.web.context.request.RequestContextHolder

/**
 * An interceptor that ensures that the flash scope is properly stored in the session.
 */
class FlashInterceptor implements Interceptor {
    /**
     * Match all requests.
     */
    FlashInterceptor() {
        matchAll()
    }

    /**
     * After the view has finished rendering, grab the flash scope data and store it in the request's session.
     */
    void afterView() {
        GrailsWebRequest webRequest = (GrailsWebRequest) RequestContextHolder.currentRequestAttributes()
        FlashScope flashScope = webRequest.getFlashScope()
        request.getSession().setAttribute(GrailsApplicationAttributes.FLASH_SCOPE, flashScope)
    }
}
