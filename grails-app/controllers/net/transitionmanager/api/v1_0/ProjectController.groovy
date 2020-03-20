package net.transitionmanager.api.v1_0

import com.tdsops.tm.enums.domain.ProjectStatus
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods

/**
 * A controller for getting project data, from an API.
 */
@Secured('isAuthenticated()')
class ProjectController implements ControllerMethods{

    static namespace = 'v1'

    static allowedMethods = [
        index: 'GET'
    ]


    /**
     * This gets the users projects.
     *
     * @return the projects as JSON.
     */
    def index() {
        ProjectStatus projectStatus =  ProjectStatus.ANY
        render view: "/common/listAsJson", model: [data:projectService.projects( projectStatus)]
    }
}
