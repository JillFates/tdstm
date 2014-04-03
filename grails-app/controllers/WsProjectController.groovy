import grails.converters.JSON
import grails.validation.ValidationException

import com.tdsops.tm.enums.domain.ProjectSortProperty
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdsops.tm.enums.domain.SortOrder

/**
 * {@link Controller} for handling WS calls of the {@link ProjectsService}
 *
 * @author Diego Scarpa <diego.scarpa@bairesdev.com>
 */
class WsProjectController {

	def projectService
	def securityService
	
	/**
	 * Gets the projects associated to a user
	 */
	def userProjects = {

		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}
		def projectHasPermission = RolePermissions.hasPermission("ShowAllProjects")

		def projectStatus = ProjectStatus.valueOfParam(params.status)
		projectStatus = projectStatus?projectStatus:ProjectStatus.ANY;

		def searchParams = [:]
		searchParams.maxRows = params.maxRows
		searchParams.currentPage = params.currentPage
		searchParams.sortOn = ProjectSortProperty.valueOfParam(params.sortOn)
		searchParams.sortOrder = SortOrder.valueOfParam(params.sortOrder)
		
		try {
			def projects = projectService.getUserProjects(loginUser, projectHasPermission, projectStatus, searchParams)
			def dataMap = [:]
			def results = []
			projects.each() { project ->
				def dto = [:]
				dto.id = project.id
				dto.name = project.name
				dto.description = project.description
				dto.projectCode = project.projectCode
				dto.status = project.getStatus()
				dto.completionDate = project.completionDate
				dto.clientId = project.client.id
				dto.clientName = project.client.name
				results.add(dto)
			}

			dataMap.projects = results 

			render(ServiceResults.success(dataMap) as JSON) 
		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (ValidationException e) {
			render(ServiceResults.errorsInValidation(e.getErrors()) as JSON)
		} catch (IllegalArgumentException e) {
			ServiceResults.forbidden(response)
		} catch (Exception e) {
			ServiceResults.internalError(response, log, e)
		}
	}

}
