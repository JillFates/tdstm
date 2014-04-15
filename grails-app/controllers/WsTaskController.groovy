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
		
		def recipeVersionId = params.recipeVersionId
		def contextId = params.contextId
		def publishTasks = params.publishTasks

		try {
			def result = taskService.initiateCreateTasksWithRecipe(loginUser, recipeVersionId, contextId, publishTasks);

			render(ServiceResults.success('jobId' : result.jobId) as JSON)
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
	 * Used to lookup a TaskBatch by the Context and Recipe regardless of the recipe version
	 * @param contextId - the record id number of the context that the TaskBatch was generated for
	 * @param recipeId - the record id of the recipe used to generate the TaskBatch
	 * @return A taskBatch object if found or null
	 */
	def findTaskBatchByRecipeAndContext = {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}
		
		def recipeId = params.recipeId
		def contextId = params.contextId
		def logs = params.logs
		def currentProject = securityService.getUserCurrentProject()
		
		try {
			def result = taskService.findTaskBatchByRecipeAndContext(recipeId, contextId, logs, loginUser, currentProject);

			render(ServiceResults.success('taskBatch' : result) as JSON)
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
	 * List the {@link TaskBatch} using the parameters passed in the request
	 * Check {@link UrlMappings} for the right call
	 */
	def listTaskBatches = {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}
		
		def recipeId = params.recipeId
		def limitDays = params.limitDays
		def currentProject = securityService.getUserCurrentProject()
		
		try {
			def result = taskService.listTaskBatches(recipeId, limitDays, loginUser, currentProject);

			render(ServiceResults.success('list' : result) as JSON)
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
	 * Gets a {@link TaskBatch} based on a id
	 * Check {@link UrlMappings} for the right call
	 */
	def getTaskBatch = {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}
		
		def id = params.id
		def currentProject = securityService.getUserCurrentProject()

		try {
			def taskBatch = taskService.getTaskBatch(id, loginUser, currentProject)

			render(ServiceResults.success(['taskBatch' : taskBatch]) as JSON)
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
