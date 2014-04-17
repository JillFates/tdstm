import grails.converters.JSON
import grails.validation.ValidationException

import org.hibernate.exception.ConstraintViolationException


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
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}
		
		def name = params.name
		def description = params.description
		def context = params.context
		def cloneFrom = params.clonedFrom
		def currentProject = securityService.getUserCurrentProject()

		try {
			def recipe = cookbookService.createRecipe(name, description, context, cloneFrom, loginUser, currentProject)

			render(ServiceResults.success('recipeId' : recipe.id) as JSON)
		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (ValidationException e) {
			render(ServiceResults.errorsInValidation(e.getErrors()) as JSON)
		} catch (Exception e) {
			ServiceResults.internalError(response, log, e)
		}
	}

	/**
	 * Clone a recipe
	 * Check {@link UrlMappings} for the right call
	 */
	def cloneRecipe = {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}
		
		def recipeVersionid = params.recipeVersionid
		def name = params.name
		def description = params.description
		def currentProject = securityService.getUserCurrentProject()
		
		try {
			def data = cookbookService.cloneRecipe(recipeVersionid, name, description, loginUser, currentProject)

			def dataMap = [:]
			dataMap.recipeId = data.recipe.id
			dataMap.recipeVersionId = data.recipeVersion.id

			render(ServiceResults.success(dataMap) as JSON)
		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (ValidationException e) {
			render(ServiceResults.errorsInValidation(e.getErrors()) as JSON)
		} catch (Exception e) {
			ServiceResults.internalError(response, log, e)
		}
	}

	/**
	 * Deletes a recipe or a recipe version id version is passed
	 * Check {@link UrlMappings} for the right call
	 */
	def deleteRecipeOrVersion = {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}
		
		def id = params.id
		def version = params.version
		def currentProject = securityService.getUserCurrentProject()

		try {
			if (version == null) {
				cookbookService.deleteRecipe(id, loginUser, currentProject)
			} else {
				cookbookService.deleteRecipeVersion(id, version, loginUser, currentProject)
			}

			render(ServiceResults.success() as JSON)
		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (ValidationException e) {
			render(ServiceResults.errorsInValidation(e.getErrors()) as JSON)
		} catch (ConstraintViolationException e) {
			render(ServiceResults.errors(['Can\'t delete the recipe']) as JSON)
		} catch (Exception e) {
			ServiceResults.internalError(response, log, e)
		}
	}
		
	/**
	 * Updates the name and description of an existing Recipe
	 * Check {@link UrlMappings} for the right call
	 */
	def updateRecipe = {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}
		
		def recipeId = params.id
		def json = request.JSON
		def name = json.name
		def description = json.description
		def currentProject = securityService.getUserCurrentProject()

		try {
			cookbookService.updateRecipe(recipeId, name, description, loginUser, currentProject)

			render(ServiceResults.success() as JSON)
		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (ValidationException e) {
			render(ServiceResults.errorsInValidation(e.getErrors()) as JSON)
		} catch (Exception e) {
			ServiceResults.internalError(response, log, e)
		}
	}
	
	/**
	 * Saves a version of the recipe
	 * Check {@link UrlMappings} for the right call
	 */
	def saveRecipeVersion = {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}

		def recipeId = params.id
		def recipeVersionId = params.recipeVersionId
		def name = params.name
		def description = params.description
		def sourceCode = params.sourceCode
		def changelog = params.changelog
		def currentProject = securityService.getUserCurrentProject()

		try {
			cookbookService.saveOrUpdateWIPRecipe(recipeId, recipeVersionId, name, description, sourceCode, changelog, loginUser, currentProject)

			render(ServiceResults.success() as JSON)
		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (ValidationException e) {
			render(ServiceResults.errorsInValidation(e.getErrors()) as JSON)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (Exception e) {
			ServiceResults.internalError(response, log, e)
		}
	}
	
	/**
	 * Releases a recipe that is WIP
	 * Check {@link UrlMappings} for the right call
	 */
	def releaseRecipe = {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}

		def recipeId = params.recipeId
		def currentProject = securityService.getUserCurrentProject()

		try {
			cookbookService.releaseRecipe(recipeId, loginUser, currentProject)

			render(ServiceResults.success() as JSON)
		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (ValidationException e) {
			render(ServiceResults.errorsInValidation(e.getErrors()) as JSON)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (Exception e) {
			ServiceResults.internalError(response, log, e)
		}
	}
	
	/**
	 * Reverts a recipe to the previous release version
	 * Check {@link UrlMappings} for the right call
	 */
	def revert = {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}

		def recipeVersionId = params.id
		def currentProject = securityService.getUserCurrentProject()

		try {
			cookbookService.revertRecipe(recipeVersionId, loginUser, currentProject)

			render(ServiceResults.success() as JSON)
		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (ValidationException e) {
			render(ServiceResults.errorsInValidation(e.getErrors()) as JSON)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (Exception e) {
			ServiceResults.internalError(response, log, e)
		}
	}
	
	/**
	 * Obtains the information about a recipe
	 * Check {@link UrlMappings} for the right call
	 */
	def recipe = {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}
		
		def recipeId = params.id
		def recipeVersion = params.version
		
		try {
			def result = cookbookService.getRecipe(recipeId, recipeVersion, loginUser)

			def dataMap = [:]
			dataMap.recipeId = result.recipe.id
			dataMap.name = result.recipe.name
			dataMap.description = result.recipe.description
			dataMap.context = result.recipe.context
			dataMap.createdBy = result.person.firstName + " " + result.person.lastName
			dataMap.lastUpdated = result.recipeVersion.lastUpdated
			dataMap.versionNumber = result.recipeVersion.versionNumber
			dataMap.releasedVersionNumber = (result.recipe.releasedVersion == null) ? -1 : result.recipe.releasedVersion.versionNumber
			dataMap.recipeVersionId = result.recipeVersion.id
			dataMap.hasWIP = result.wip != null
			dataMap.sourceCode = result.recipeVersion.sourceCode
			dataMap.changelog = result.recipeVersion.changelog
			dataMap.clonedFrom = (result.recipeVersion.clonedFrom == null) ? '' : result.recipeVersion.clonedFrom.toString()
			
			render(ServiceResults.success(dataMap) as JSON)
		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (ValidationException e) {
			render(ServiceResults.errorsInValidation(e.getErrors()) as JSON)
		} catch (Exception e) {
			ServiceResults.internalError(response, log, e)
		}
	}

		
	/**
	 * Lists the recipes of the current user
	 * Check {@link UrlMappings} for the right call
	 */
	def recipeList = {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}

		def isArchived = params.archived
		def catalogContext = params.context
		def searchText = params.search
		def projectType = params.projectType
		def currentProject = securityService.getUserCurrentProject()

		try {
			def results = cookbookService.findRecipes(isArchived, catalogContext, searchText, projectType, loginUser, currentProject)
			def dataMap = [:]
			dataMap.list = results

			render(ServiceResults.success(dataMap) as JSON)
		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (ValidationException e) {
			render(ServiceResults.errorsInValidation(e.getErrors()) as JSON)
		} catch (IllegalArgumentException e) {
			ServiceResults.methodFailure(response)
		} catch (Exception e) {
			ServiceResults.internalError(response, log, e)
		}
	}

	/**
	 * List of RecipeVersion objects for a given recipe id.
	 * 
	 * Check {@link UrlMappings} for the right call
	 */
	def recipeVersionList = {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}
		def currentProject = securityService.getUserCurrentProject()

		def recipeId = params.id

		try {
			def results = cookbookService.findRecipeVersions(recipeId, currentProject)
			def dataMap = [:]
			dataMap.recipeVersions = results

			render(ServiceResults.success(dataMap) as JSON)
		} catch (UnauthorizedException e) {
			ServiceResults.methodFailure(response)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (ValidationException e) {
			render(ServiceResults.errorsInValidation(e.getErrors()) as JSON)
		} catch (IllegalArgumentException e) {
			ServiceResults.methodFailure(response)
		} catch (Exception e) {
			ServiceResults.internalError(response, log, e)
		}
	}

	/**
	 * Validates the syntax of the source code of a recipe
	 * Check {@link UrlMappings} for the right call
	 */
	def validateSyntax = {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}

		def sourceCode = params.sourceCode
		def currentProject = securityService.getUserCurrentProject()

		try {
			
			def results = cookbookService.validateSyntaxForUser(sourceCode, loginUser, currentProject)

			if (results == null) {
				render(ServiceResults.success() as JSON)
			} else {
				render(ServiceResults.warnings(results) as JSON)
			}

		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (ValidationException e) {
			render(ServiceResults.errorsInValidation(e.getErrors()) as JSON)
		} catch (Exception e) {
			ServiceResults.internalError(response, log, e)
		}
	}
	
	/**
	 * Archive recipes
	 * Check {@link UrlMappings} for the right call
	 */
	def archiveRecipe = {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}

		def id = params.id
		def currentProject = securityService.getUserCurrentProject()

		try {
			cookbookService.archivedUnarchived(id, true, loginUser, currentProject)
			render(ServiceResults.success() as JSON)
		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (ValidationException e) {
			render(ServiceResults.errorsInValidation(e.getErrors()) as JSON)
		} catch (Exception e) {
			ServiceResults.internalError(response, log, e)
		}
	}
	
	/**
	 * Unarchive recipes
	 * Check {@link UrlMappings} for the right call
	 */
	def unarchiveRecipe = {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}

		def id = params.id
		def currentProject = securityService.getUserCurrentProject()

		try {
			cookbookService.archivedUnarchived(id, false, loginUser, currentProject)
			render(ServiceResults.success() as JSON)
		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (ValidationException e) {
			render(ServiceResults.errorsInValidation(e.getErrors()) as JSON)
		} catch (Exception e) {
			ServiceResults.internalError(response, log, e)
		}
	}
	
	/**
	 * List the groups
	 * Check {@link UrlMappings} for the right call
	 */
	def groups = {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}

		def recipeVersionId = params.recipeVersionId
		def contextId = params.contextId
		def currentProject = securityService.getUserCurrentProject()

		try {
			def groups = cookbookService.getGroups(recipeVersionId, contextId, loginUser, currentProject)
			render(ServiceResults.success('groups' : groups) as JSON)
		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (ValidationException e) {
			render(ServiceResults.errorsInValidation(e.getErrors()) as JSON)
		} catch (Exception e) {
			ServiceResults.internalError(response, log, e)
		}
	}
}
