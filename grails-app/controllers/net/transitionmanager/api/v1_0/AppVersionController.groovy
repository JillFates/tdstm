package net.transitionmanager.api.v1_0

import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods

@Secured('permitAll')
class AppVersionController implements ControllerMethods{

    static namespace = 'v1'

    static allowedMethods = [
        index: 'GET'
    ]

    net.transitionmanager.common.EnvironmentService environmentService


    /**
     * Retrieve and return the build version.
     * @return
     */
    def index() {
        renderSuccessJson([version: environmentService.getVersionText()])
    }
}
