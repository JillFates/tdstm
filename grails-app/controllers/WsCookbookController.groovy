import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import com.tdsops.common.security.spring.HasPermission
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Recipe
import net.transitionmanager.domain.RecipeVersion
import net.transitionmanager.security.Permission
import net.transitionmanager.service.CookbookService
import net.transitionmanager.service.InvalidParamException

/**
 * Handles WS calls of the CookbookService.
 *
 * @author Esteban Robles Luna <esteban.roblesluna@gmail.com>
 */
@Secured('isAuthenticated()')
@Slf4j(value='logger', category='grails.app.controllers.WsCookbookController')
class WsCookbookController implements ControllerMethods {

	CookbookService cookbookService

	@HasPermission(Permission.RecipeCreate)
	def createRecipe(String name, String description, String context, Long clonedFrom) {
		def recipeVersion = cookbookService.createRecipe(name, description, context, clonedFrom)
		renderSuccessJson(recipeId: recipeVersion.recipe.id)
	}

	@HasPermission(Permission.RecipeCreate)
	def cloneRecipe(Long recipeVersionid, String name, String description) {
		RecipeVersion rv = cookbookService.cloneRecipe(recipeVersionid, name, description)
		renderSuccessJson(recipeId: rv.recipeId, recipeVersionId: rv.id)
	}

	@HasPermission(Permission.RecipeDelete)
	def deleteRecipeOrVersion(Long id) {
		def version = params.version
		if (version == null || version == 0) {
			// The front-end prevents this situation. This is just being extra cautious.
			throw new InvalidParamException('Cannot delete recipe with an invalid version.')
		} else {
			cookbookService.deleteRecipeVersion(params.id, version)
		}
		renderSuccessJson()
	}

	@HasPermission(Permission.RecipeEdit)
	def updateRecipe(Long id) {
		def json = request.JSON
		cookbookService.updateRecipe(id, json.name, json.description)
		renderSuccessJson()
	}

	@HasPermission(Permission.RecipeEdit)
	def saveRecipeVersion(Long id, Long recipeVersionId) {
		cookbookService.saveOrUpdateWIPRecipe(id, recipeVersionId, params.name, params.description,
			params.sourceCode, params.changelog)
		renderSuccessJson()
	}

	/**
	 * Releases a recipe that is WIP
	 */
	@HasPermission(Permission.RecipeRelease)
	def releaseRecipe(Long recipeId) {
		cookbookService.releaseRecipe(recipeId)
		renderSuccessJson()
	}

	/**
	 * Reverts a recipe to the previous release version
	 */
	@HasPermission(Permission.RecipeEdit)
	def revert(Long id) {
		cookbookService.revertRecipe(id)
		renderSuccessJson()
	}

	/**
	 * Obtains the information about a recipe
	 */
	@HasPermission(Permission.RecipeView)
	def recipe(Long id, Integer version) {
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

	@HasPermission(Permission.RecipeView)
	def recipeList(String archived, String context) {
		def results = cookbookService.findRecipes(archived, context, params.search, params.projectType)
		renderSuccessJson(list: results)
	}

	@HasPermission(Permission.RecipeView)
	def recipeVersionList() {
		def results = cookbookService.findRecipeVersions(params.id)
		renderSuccessJson(recipeVersions: results)
	}

	/**
	 * Validates the syntax of the source code of a recipe
	 */
	@HasPermission(Permission.RecipeView)
	def validateSyntax() {
		def results = cookbookService.validateSyntaxForUser(params.sourceCode)
		if (results == null) {
			renderSuccessJson()
		} else {
			renderWarningJson(results)
		}
	}

	@HasPermission(Permission.RecipeEdit)
	def archiveRecipe() {
		cookbookService.archivedUnarchived(params.id, true)
		renderSuccessJson()
	}

	@HasPermission(Permission.RecipeEdit)
	def unarchiveRecipe() {
		cookbookService.archivedUnarchived(params.id, false)
		renderSuccessJson()
	}

	@HasPermission(Permission.RecipeView)
	def groups() {
		def groups = cookbookService.getGroups(params.recipeVersionId, params.contextId, params.contextType, params.sourceCode)
		renderSuccessJson(groups: groups)
	}

	@HasPermission(Permission.RecipeEdit)
	def defineRecipeContext() {
		cookbookService.defineRecipeContext(params.recipeId, params.contextId)
		renderSuccessJson()
	}

	@HasPermission(Permission.RecipeEdit)
	def deleteRecipeContext() {
		cookbookService.deleteRecipeContext(params.recipeId)
		renderSuccessJson()
	}
}
