package net.transitionmanager.controller.api.v1

import com.tdsops.common.security.spring.HasPermission
import grails.rest.RestfulController
import net.transitionmanager.domain.Project
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.api.ProjectGormService

class ProjectController extends RestfulController {
    static namespace = 'v1'
    static responseFormats = ['json']

    ProjectController() {
        super(Project)
    }

    ProjectGormService projectGormService

    SecurityService securityService

    @Override
    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        def projects = projectGormService.findAllIdAndNameAndProjectCodeAndDescriptionProjections(params)
        respond projects, model: [("${resourceName}Count".toString()): countResources()]
    }

    @HasPermission('Foo')
    def show() {
        respond queryForResource(params.id)
    }
}
