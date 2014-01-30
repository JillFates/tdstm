import grails.converters.JSON
import org.apache.shiro.SecurityUtils


class WsCookbookController {

	def cookbookService
	
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
		} catch (IllegalArgumentException e) {
			log.info("Exception catched")
			response.sendError(403, 'Forbidden')
		}
	}
}
