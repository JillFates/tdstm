package com.tdsops.common.security.spring

import com.tdssrc.grails.WebUtil
import net.transitionmanager.service.CoreService
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.web.PortResolver
import org.springframework.security.web.PortResolverImpl
import org.springframework.security.web.savedrequest.DefaultSavedRequest
import org.springframework.security.web.savedrequest.HttpSessionRequestCache
import org.springframework.security.web.savedrequest.SavedRequest
import org.springframework.security.web.util.matcher.AnyRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

/**
 * A enhanced version of the Spring Security HttpSessionRequestCache
 *
 * This class saves the last visited page if request method is a GET, if request is an Ajax request
 * we must ensure the referer is from internal application and saves the referer as last visited page.
 * Being said that, after a security exception like session expiration, application will take the user back
 * to the last visited page after a successful login.
 */
class TdsHttpSessionRequestCache extends HttpSessionRequestCache {
    public static final String SAVED_REQUEST = "SPRING_SECURITY_SAVED_REQUEST"
    public static final String SESSION_EXPIRED = "sessionExpired"
    protected final Log logger = LogFactory.getLog(this.getClass())

    private PortResolver portResolver = new PortResolverImpl()
    private boolean createSessionAllowed = true
    private RequestMatcher requestMatcher = AnyRequestMatcher.INSTANCE

    private CoreService coreService

    /**
     * Stores the current request, provided the configuration properties allow it.
     */
    @Override
    void saveRequest(HttpServletRequest request, HttpServletResponse response) {
        if (requestMatcher.matches(request)) {
            SavedRequest savedRequest = null

            if (createSessionAllowed || request.getSession(false) != null) {
                // Store the HTTP request itself. Used by AbstractAuthenticationProcessingFilter
                // for redirection after successful authentication (SEC-29)

                HttpSession session = request.getSession()
                if (WebUtil.isAjax(request)) {
                    // Ajax, saves referer header value
                    String referer = request.getHeader(HttpHeaders.REFERER)
                    if (referer && referer.toLowerCase().startsWith(getServerBaseURL())) {
                        savedRequest = new TdsSavedRequest(request, portResolver, referer)
                    }
                } else {
                    if (HttpMethod.GET.toString() == request.getMethod()) {
                        savedRequest = new DefaultSavedRequest(request, portResolver)
                    }
                }

                if (savedRequest) {
                    logger.debug("SavedRequest added to Session: " + savedRequest)
                    session.setAttribute(SAVED_REQUEST, savedRequest)
                    session.setAttribute(SESSION_EXPIRED, true)
                }
            }
        } else {
            logger.debug("Request not saved as configured RequestMatcher did not match")
        }
    }

    void setCoreService(CoreService coreService) {
        this.coreService = coreService
    }

    String getServerBaseURL() {
        coreService.getApplicationUrl()
    }
}
