import java.util.Map;

import grails.converters.JSON

import org.apache.shiro.SecurityUtils
import org.springframework.stereotype.Controller;

import grails.validation.ValidationException;

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
	 * Check {@link UrlMappings} for the right call
	 */
	def publish = {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}
		
		def id = params.id
		def currentProject = securityService.getUserCurrentProject()

		try {
			def tasksUpdated = taskService.publish(id, loginUser, currentProject)

			render(ServiceResults.success(['tasksUpdated' : tasksUpdated]) as JSON)
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

	/**
	 * Unpublishes a {@link TaskBatch} that has been generated before
	 * Check {@link UrlMappings} for the right call
	 */
	def unpublish = {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}
		
		def id = params.id
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
		} catch (ValidationException e) {
			render(ServiceResults.errorsInValidation(e.getErrors()) as JSON)
		} catch (Exception e) {
			ServiceResults.internalError(response, log, e)
		}
	}
	
	/**
	 * Deletes a {@link TaskBatch}
	 * Check {@link UrlMappings} for the right call
	 */
	def deleteBatch = {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}
		
		def id = params.id
		def currentProject = securityService.getUserCurrentProject()

		try {
			taskService.deleteBatch(id, loginUser, currentProject)

			render(ServiceResults.success() as JSON)
		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (IllegalArgumentException e) {
			ServiceResults.forbidden(response)
		} catch (ValidationException e) {
			render(ServiceResults.errorsInValidation(e.getErrors()) as JSON)
		} catch (Exception e) {
			ServiceResults.internalError(response, log, e)
		}
	}
	
	/**
	 * Generates a set of tasks based on a recipe
	 */
	def generateTasks = {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}
		
		def contextType = params.contextType
		def contextId = params.contextId
		def recipeVersion = params.recipeVersion
		def publishTasks = params.publishTasks

		try {
			def result = taskService.initiateCreateTasksWithRecipe(loginUser, contextType, contextId, recipeVersion, publishTasks);

			render(ServiceResults.success('processId' : contextId) as JSON)
		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (IllegalArgumentException e) {
			ServiceResults.forbidden(response)
		} catch (ValidationException e) {
			render(ServiceResults.errorsInValidation(e.getErrors()) as JSON)
		} catch (Exception e) {
			ServiceResults.internalError(response, log, e)
		}
	}
}
