import grails.converters.JSON

import org.apache.shiro.SecurityUtils
import org.springframework.stereotype.Controller;

/**
 * {@link Controller} for handling WS calls of the {@link TaskService}
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
class WsTaskController {

	def taskService
	def securityService

	/**
	 * Publishes a {@link TaskBatch} that has been generated before
	 */
	def publish = {
		if (!SecurityUtils.subject.authenticated) {
			ServiceResults.unauthorized(response)
			return
		}
		
		def id = params.id
		def loginUser = securityService.getUserLogin()
		def currentProject = securityService.getUserCurrentProject()

		try {
			def tasksUpdated = taskService.publish(id, loginUser, currentProject)

			render(ServiceResults.success(['tasksUpdated' : tasksUpdated]) as JSON)
		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (IllegalArgumentException e) {
			ServiceResults.forbidden(response)
		}
	}

	/**
	 * Unpublishes a {@link TaskBatch} that has been generated before
	 */
	def unpublish = {
		if (!SecurityUtils.subject.authenticated) {
			ServiceResults.unauthorized(response)
			return
		}
		
		def id = params.id
		def loginUser = securityService.getUserLogin()
		def currentProject = securityService.getUserCurrentProject()

		try {
			def tasksUpdated = taskService.unpublish(id, loginUser, currentProject)

			render(ServiceResults.success(['tasksUpdated' : tasksUpdated]) as JSON)
		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (IllegalArgumentException e) {
			ServiceResults.forbidden(response)
		}
	}
}
