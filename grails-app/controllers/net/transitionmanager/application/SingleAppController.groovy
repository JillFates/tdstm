package net.transitionmanager.application

import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.common.EnvironmentService

/**
 * Single App AngularJS 2-4
 *
 * @author Jorge Morayta
 */

@Secured('permitAll')
class SingleAppController {
    EnvironmentService environmentService

    def index() {
        response.setHeader('X-Login-URL', '/tdstm/module/auth/login').toString()
        [buildHash: environmentService.buildHash]
    }
}
