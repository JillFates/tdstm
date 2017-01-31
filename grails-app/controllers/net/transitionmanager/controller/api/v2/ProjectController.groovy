package net.transitionmanager.controller.api.v2

import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController
import net.transitionmanager.domain.Project
import net.transitionmanager.service.api.ProjectGormService

@Secured('isAuthenticated()')
class ProjectController extends RestfulController {
    static namespace = 'v2'
    static responseFormats = ['json']

    ProjectController() {
        super(Project)
    }

    ProjectGormService projectGormService

    @Override
    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        def projects = projectGormService.findAllIdAndNameAndProjectCodeProjections(params)
        respond projects, model: [("${resourceName}Count".toString()): countResources()]
    }

    @HasPermission('ProjectStaffShow')
    def show() {
        respond queryForResource(params.id)
    }
}
