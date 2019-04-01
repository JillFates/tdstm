package net.transitionmanager.project

import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.ProjectSortProperty
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdsops.tm.enums.domain.SortOrder
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.Permission
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.service.ProjectService

/**
 * Handles WS calls of the ProjectsService
 */
@Secured('isAuthenticated()')
@Slf4j
class WsProjectController implements ControllerMethods {

	ProjectService projectService

	/**
	 * Gets the projects associated to a user
	 */
	def userProjects() {
		def projectStatus = ProjectStatus.lookup(params.status) ?: ProjectStatus.ACTIVE
		// TODO : JPM 2/2018 : This should be a Command Object
		Map searchParams = [
			maxRows: params.maxRows,
			currentPage: params.currentPage,
			sortOn: ProjectSortProperty.valueOfParam(params.sortOn),
			sortOrder: SortOrder.valueOfParam(params.sortOrder)
		]
		def projects = projectService.getUserProjects(securityService.hasPermission(Permission.ProjectShowAll), projectStatus, searchParams)
		Map dataMap = [:]
		List results = []
		projects.each { project ->
			results.add(name: project.name, description: project.description, clientId: project.client.id,
				id: project.id, projectCode: project.projectCode, status: project.getStatus(),
				clientName: project.client.name, completionDate: project.completionDate)
		}

		dataMap.projects = results

		renderSuccessJson(dataMap)
	}

	/**
	 * Returns a list of projects, and their licence data.
	 *
	 * @return A list of projects, and their licence data.
	 */
	@HasPermission(Permission.ProjectView)
	def projects() {
		ProjectStatus projectStatus =  ProjectStatus.ANY
		if (params.status) {
			projectStatus = ProjectStatus.lookup(params.status)
			if (! projectStatus) {
				throw new InvalidParamException('Invalid value for parameter status, options are: ANY, ACTIVE or COMPLETED')
			}
		}
		renderSuccessJson(projectService.projects( projectStatus ))
	}
}
