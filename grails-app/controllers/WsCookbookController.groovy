import grails.converters.JSON

import org.apache.shiro.SecurityUtils
import org.springframework.stereotype.Controller;

/**
 * {@link Controller} for handling WS calls of the {@link CookbookService}
 * 
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
class WsCookbookController {

	def cookbookService
	def securityService
	
	/**
	 * Creates a recipe
	 * Check {@link UrlMappings} for the right call
	 */
	def createRecipe = {
		if (!SecurityUtils.subject.authenticated) {
			ServiceResults.unauthorized(response)
			return
		}
		
		def name = params.name
		def description = params.description
		def context = params.context
		def cloneFrom = params.cloneFrom
		def loginUser = securityService.getUserLogin()
		def currentProject = securityService.getUserCurrentProject()

		try {
			cookbookService.createRecipe(name, description, context, cloneFrom, loginUser, currentProject)

			render(ServiceResults.success() as JSON)
		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (IllegalArgumentException e) {
			render(ServiceResults.fail([
				'name' : 'Name is required', 
				'context' : 'Context is not a valid value'
			]) as JSON)
		}
	}
		
	
	/**
	 * Saves a version of the recipe
	 * Check {@link UrlMappings} for the right call
	 */
	def saveRecipeVersion = {
		render(ServiceResults.success([message : 'save version']) as JSON)
	}
	
	/**
	 * Updates a version of the recipe
	 * Check {@link UrlMappings} for the right call
	 */
	def updateRecipeVersion = {
		render(ServiceResults.success([message : 'update version']) as JSON)
	}
	
	/**
	 * Obtains the information about a recipe
	 * Check {@link UrlMappings} for the right call
	 */
	def recipe = {
		if (!SecurityUtils.subject.authenticated) {
			ServiceResults.unauthorized(response)
			return
		}
		
		def recipeId = params.id
		def recipeVersion = params.version
		
		try {
			def loginUser = securityService.getUserLogin()
			def result = cookbookService.getRecipe(recipeId, recipeVersion, loginUser)

			def dataMap = [:]
			dataMap.recipeId = result.recipe.id
			dataMap.name = result.recipe.name
			dataMap.description = result.recipe.description
			dataMap.context = result.recipe.context
			dataMap.createdBy = result.person.firstName + " " + result.person.lastName
			dataMap.lastUpdated = result.recipeVersion.lastUpdated
			dataMap.versionNumber = (result.recipe.releasedVersion == null) ? -1 : result.recipe.releasedVersion.versionNumber
			dataMap.hasWIP = result.wip != null
			dataMap.sourceCode = result.recipeVersion.sourceCode
			dataMap.changelog = result.recipeVersion.changelog
			dataMap.clonedFrom = (result.recipeVersion.clonedFrom == null) ? '' : result.recipeVersion.clonedFrom.toString()
			
			render(ServiceResults.success(dataMap) as JSON)
		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		}
	}

		
	/**
	 * Lists the recipes of the current user
	 * Check {@link UrlMappings} for the right call
	 */
	def recipeList = {
		if (!SecurityUtils.subject.authenticated) {
			ServiceResults.unauthorized(response)
			return
		}

		def isArchived = params.archived
		def catalogContext = params.context
		def searchText = params.search
		def projectType = params.project

		try {
			def loginUser = securityService.getUserLogin()
			def currentProject = securityService.getUserCurrentProject()
			
			def results = cookbookService.findRecipes(isArchived, catalogContext, searchText, projectType, loginUser, currentProject)

			def dataMap = [:]
			dataMap.list = results

			render(ServiceResults.success(dataMap) as JSON)
		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (IllegalArgumentException e) {
			ServiceResults.methodFailure(response)
		}
	}
}
