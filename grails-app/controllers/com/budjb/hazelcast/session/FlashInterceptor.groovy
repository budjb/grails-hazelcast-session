package com.budjb.hazelcast.session

import grails.artefact.Interceptor
import grails.web.mvc.FlashScope
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.GrailsApplicationAttributes
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.context.request.RequestContextHolder

import javax.servlet.http.HttpSession

/**
 * An interceptor that ensures that the flash scope is properly stored in the session.
 */
class FlashInterceptor implements Interceptor {
    /**
     * Logger.
     */
    Logger log = LoggerFactory.getLogger(getClass())

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
        FlashScope flashScope = ((GrailsWebRequest) RequestContextHolder.currentRequestAttributes()).getFlashScope()

        if (flashScope.isEmpty()) {
            return
        }

        HttpSession session = request.getSession()
        if (session != null) {
            session.setAttribute(GrailsApplicationAttributes.FLASH_SCOPE, flashScope)
        }
        else {
            log.warn("unable to store flash scope in the session because the session is null")
        }
    }
}
