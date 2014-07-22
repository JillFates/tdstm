import grails.converters.JSON

class CookbookController {

	def userPreferenceService
	
    def index = {
		render(view: 'index', model: ['userPreferenceService': userPreferenceService])
	}
	
}