import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Recipe
import net.transitionmanager.domain.RecipeVersion
import net.transitionmanager.service.CookbookService

/**
 * Handles WS calls of the CookbookService.
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
@Secured('isAuthenticated()')
@Slf4j(value='logger', category='grails.app.controllers.WsCookbookController')
class WsCookbookController implements ControllerMethods {

	CookbookService cookbookService

	def createRecipe(String name, String description, String context, Long clonedFrom) {
		try {
			def recipe = cookbookService.createRecipe(name, description, context, clonedFrom)
			renderSuccessJson(recipeId: recipe.id)
		}
		catch (e) {
			handleException e, logger
		}
	}

	def cloneRecipe(Long recipeVersionid, String name, String description) {
		try {
			RecipeVersion rv = cookbookService.cloneRecipe(recipeVersionid, name, description)
			renderSuccessJson(recipeId: rv.recipeId, recipeVersionId: rv.id)
		}
		catch (e) {
			handleException e, logger
		}
	}

	def deleteRecipeOrVersion(Long id) {
		try {
			def version = params.version
			if (version == null) {
				cookbookService.deleteRecipe(id)
			}
			else {
				cookbookService.deleteRecipeVersion(params.id, version)
			}
			renderSuccessJson()
		}
		catch (e) {
			handleException e, logger
		}
	}

	def updateRecipe(Long id) {
		try {
			def json = request.JSON
			cookbookService.updateRecipe(id, json.name, json.description)
			renderSuccessJson()
		}
		catch (e) {
			handleException e, logger
		}
	}

	def saveRecipeVersion(Long id, Long recipeVersionId) {
		try {
			cookbookService.saveOrUpdateWIPRecipe(id, recipeVersionId, params.name, params.description,
					params.sourceCode, params.changelog)
			renderSuccessJson()
		}
		catch (e) {
			handleException e, logger
		}
	}

	/**
	 * Releases a recipe that is WIP
	 */
	def releaseRecipe(Long recipeId) {
		try {
			cookbookService.releaseRecipe(recipeId)
			renderSuccessJson()
		}
		catch (e) {
			handleException e, logger
		}
	}

	/**
	 * Reverts a recipe to the previous release version
	 */
	def revert(Long id) {
		try {
			cookbookService.revertRecipe(id)
			renderSuccessJson()
		}
		catch (e) {
			handleException e, logger
		}
	}

	/**
	 * Obtains the information about a recipe
	 */
	def recipe(Long id, Integer version) {
		try {
			Map result = cookbookService.getRecipe(id, version)
			Recipe recipe = result.recipe
			RecipeVersion rv = result.recipeVersion
			renderSuccessJson(recipeId: recipe.id,
			                  name: recipe.name,
			                  description: recipe.description,
			                  context: recipe.context,
			                  createdBy: result.person.firstName + ' ' + result.person.lastName,
			                  lastUpdated: rv.lastUpdated,
			                  versionNumber: rv.versionNumber,
			                  releasedVersionNumber: recipe.releasedVersion?.versionNumber ?: -1,
			                  recipeVersionId: rv.id,
			                  hasWIP: result.wip != null,
			                  sourceCode: rv.sourceCode,
			                  changelog: rv.changelog,
			                  clonedFrom: (rv.clonedFrom ?: '').toString(),
			                  eventId: result.eventId,
			                  bundleId: result.bundleId,
			                  applicationId: result.applicationId)
		}
		catch (e) {
			handleException e, logger
		}
	}

	def recipeList(String archived, String context) {
		try {
			def results = cookbookService.findRecipes(archived, context, params.search, params.projectType)
			renderSuccessJson(list: results)
		}
		catch (e) {
			handleException e, logger
		}
	}

	def recipeVersionList() {
		try {
			def results = cookbookService.findRecipeVersions(params.id)
			renderSuccessJson(recipeVersions: results)
		}
		catch (e) {
			handleException e, logger
		}
	}

	/**
	 * Validates the syntax of the source code of a recipe
	 */
	def validateSyntax() {
		try {
			def results = cookbookService.validateSyntaxForUser(params.sourceCode)
			if (results == null) {
				renderSuccessJson()
			}
			else {
				renderWarningJson(results)
			}
		}
		catch (e) {
			handleException e, logger
		}
	}

	def archiveRecipe() {
		try {
			cookbookService.archivedUnarchived(params.id, true)
			renderSuccessJson()
		}
		catch (e) {
			handleException e, logger
		}
	}

	def unarchiveRecipe() {
		try {
			cookbookService.archivedUnarchived(params.id, false)
			renderSuccessJson()
		}
		catch (e) {
			handleException e, logger
		}
	}

	def groups() {
		try {
			def groups = cookbookService.getGroups(params.recipeVersionId, params.contextId, params.contextType, params.sourceCode)
			renderSuccessJson(groups: groups)
		}
		catch (e) {
			handleException e, logger
		}
	}

	def defineRecipeContext() {
		try {
			cookbookService.defineRecipeContext(params.recipeId, params.contextId)
			renderSuccessJson()
		}
		catch (e) {
			handleException e, logger
		}
	}

	def deleteRecipeContext() {
		try {
			cookbookService.deleteRecipeContext(params.recipeId)
			renderSuccessJson()
		}
		catch (e) {
			handleException e, logger
		}
	}
}
