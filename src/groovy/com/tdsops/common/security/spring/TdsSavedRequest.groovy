package com.tdsops.common.security.spring

import org.springframework.security.web.PortResolver
import org.springframework.security.web.savedrequest.DefaultSavedRequest

import javax.servlet.http.HttpServletRequest

/**
 * This class is to store the HTTP request itself. Used by AbstractAuthenticationProcessingFilter
 * for redirection after successful authentication (SEC-29).
 * The difference from its ancestor is that it return the referer as the redirect URL instead of the
 * original requested URL.
 */
class TdsSavedRequest extends DefaultSavedRequest {
    private String refererURL;

    TdsSavedRequest(HttpServletRequest request, PortResolver portResolver) {
        super(request, portResolver)
    }

    TdsSavedRequest(HttpServletRequest request, PortResolver portResolver, String refererURL) {
        super(request, portResolver)
        this.refererURL = refererURL;
    }

    @Override
    String getRedirectUrl() {
        return refererURL;
    }
}
