import grails.converters.JSON

import org.apache.shiro.SecurityUtils


class WsCookbookController {

	def cookbookService
	
	def recipe = {
		if (!SecurityUtils.subject.authenticated) {
			response.sendError(401, 'Unauthorized error')
			return
		}
		
		def recipeId = params.id
		def recipeVersion = params.version
		
		try {
			def result = cookbookService.getRecipe(recipeId, recipeVersion)

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
			dataMap.clonedFrom = (result.recipeVersion.clonedFrom == null) ? "" : result.recipeVersion.clonedFrom.toString()
			
			def renderMap = [:]
			renderMap.status = "success"
			renderMap.data = dataMap

			render(renderMap as JSON)
		} catch (UnauthorizedException e) {
			response.sendError(403, 'Forbidden')
		} catch (EmptyResultException e) {
			response.sendError(424, 'Method Failure')
		}
	}
	
	def recipeList = {
		if (!SecurityUtils.subject.authenticated) {
			response.sendError(401, 'Unauthorized error')
			return
		}

		def isArchived = params.archived
		def catalogContext = params.context
		def searchText = params.search
		def projectType = params.project

		try {
			def results = this.cookbookService.findRecipes(isArchived, catalogContext, searchText, projectType)

			def dataMap = [:]
			dataMap.list = results

			def renderMap = [:]
			renderMap.status = "success"
			renderMap.data = dataMap

			render(renderMap as JSON)
		} catch (UnauthorizedException e) {
			response.sendError(403, 'Forbidden')
		} catch (EmptyResultException e) {
			response.sendError(424, 'Method Failure')
		}
	}
}
