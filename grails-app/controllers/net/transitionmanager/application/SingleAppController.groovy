package net.transitionmanager.application

import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.common.EnvironmentService
import net.transitionmanager.controller.ControllerMethods

/**
 * Single App AngularJS 2-4
 *
 * @author Jorge Morayta
 */

@Secured('permitAll')
class SingleAppController implements ControllerMethods {
    EnvironmentService environmentService

    def index() {
        response.setHeader('X-Login-URL', securityService.loginUrl())
        [buildHash: environmentService.buildHash]
    }
}
