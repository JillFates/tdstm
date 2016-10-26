import com.tdsops.tm.enums.domain.ProjectSortProperty
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdsops.tm.enums.domain.SortOrder
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.SecurityService

/**
 * Handles WS calls of the ProjectsService.
 *
 * @author Diego Scarpa <diego.scarpa@bairesdev.com>
 */
@Secured('isAuthenticated()')
@Slf4j(value='logger', category='grails.app.controllers.WsProjectController')
class WsProjectController implements ControllerMethods {

	ProjectService projectService
	SecurityService securityService

	/**
	 * Gets the projects associated to a user
	 */
	def userProjects() {

		def projectStatus = ProjectStatus.valueOfParam(params.status) ?: ProjectStatus.ANY

		def searchParams = [:]
		searchParams.maxRows = params.maxRows
		searchParams.currentPage = params.currentPage
		searchParams.sortOn = ProjectSortProperty.valueOfParam(params.sortOn)
		searchParams.sortOrder = SortOrder.valueOfParam(params.sortOrder)

		try {
			def projects = projectService.getUserProjects(securityService.hasPermission("ShowAllProjects"), projectStatus, searchParams)
			def dataMap = [:]
			def results = []
			projects.each { project ->
				results.add(name: project.name, description: project.description, clientId: project.client.id,
				            id: project.id, projectCode: project.projectCode, status: project.getStatus(),
				            clientName: project.client.name, completionDate: project.completionDate)
			}

			dataMap.projects = results

			renderSuccessJson(dataMap)
		}
		catch (e) {
			handleException e, logger
		}
	}
}
