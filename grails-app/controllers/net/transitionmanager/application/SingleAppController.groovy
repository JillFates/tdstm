package net.transitionmanager.application

import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.common.EnvironmentService
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.session.SessionContext

/**
 * Single App AngularJS 2-4
 *
 * @author Jorge Morayta
 */

@Secured('permitAll')
class SingleAppController implements ControllerMethods {
    EnvironmentService environmentService

    /**
     * This controller method is the bootstrap of the Angular application mapped to the /modules path and is called
     * on the first page request. This typically will be the /tdstm/module/auth/login but may be another deep linked
     * page from the menu when the user session has expired or from a bookmarked link or email link, etc. As such the
     * page will be saved to the session and later retrieved during the login sequence.
     * @return the Angular bootstrap app
     */
    def index() {
        String loginUrl = securityService.loginUrl()

        // Save the page requested if it isn't the login form
        if (request.requestURI != loginUrl) {
            SessionContext.setLastPageRequested(session, request.requestURI)
        }

        // Add flag to indicate that the session is not logged in and that the user needs to be redirected to login
        if (! securityService.isLoggedIn()) {
            response.setHeader('X-Login-URL', loginUrl)
        }

        [buildHash: environmentService.buildHash]
    }
}
