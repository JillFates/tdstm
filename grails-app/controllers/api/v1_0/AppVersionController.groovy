package api.v1_0

import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.service.EnvironmentService

@Secured('permitAll')
class AppVersionController implements ControllerMethods{

    static namespace = 'v1'

    static allowedMethods = [
        index: 'GET'
    ]

    EnvironmentService environmentService


    /**
     * Retrieve and return the build version.
     * @return
     */
    def index() {
        renderSuccessJson([version: environmentService.getVersionText()])
    }
}
