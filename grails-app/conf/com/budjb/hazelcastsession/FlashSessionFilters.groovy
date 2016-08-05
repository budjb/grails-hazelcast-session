package com.budjb.hazelcastsession

import org.codehaus.groovy.grails.web.servlet.FlashScope
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder

class FlashSessionFilters {
    def filters = {
        all(controller: '*', action: '*') {
            afterView = { Exception e ->
                GrailsWebRequest webRequest = (GrailsWebRequest) RequestContextHolder.currentRequestAttributes()
                FlashScope flashScope = webRequest.getFlashScope()
                session.setAttribute(GrailsApplicationAttributes.FLASH_SCOPE, flashScope)
            }
        }
    }
}
