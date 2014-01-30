import grails.converters.JSON

class CookbookController {

    def index = {
    	
	}

	def taskList = {
		def mapo = [
			[
				recipeId: 'T-180-45',
				description: 'Tasks for initial phase (45 days)	',
				context: 'Event',
				createdBy: 'John',
				lastUpdated: '1/5/14 12:51pm',
				versionNumber: 6,
				hasWIP: ''
			],
			[
				recipeId: 'T-135-45',
				description: 'Tasks for initial phase (45 days)',
				context: 'Bundle',
				createdBy: 'Craig',
				lastUpdated: '1/10/14 5:22am',
				versionNumber: 3,
				hasWIP: ''
			],
			[
				recipeId: 'T-090-60',
				description: 'Tasks for initial phase (45 days)',
				context: 'Bundle',
				createdBy: 'Wim',
				lastUpdated: '1/9/14 4:17pm',
				versionNumber: 2,
				hasWIP: 'Yes'
			],
			[
				recipeId: 'T-135-45',
				description: 'Tasks for initial phase (45 days)',
				context: 'Bundle',
				createdBy: 'Craig',
				lastUpdated: '1/12/14 3:12am',
				versionNumber: 1,
				hasWIP: ''
			],
			[
				recipeId: 'T-0-Wave001',
				description: 'Wave 001 Runbook',
				context: 'Event',
				createdBy: 'John',
				lastUpdated: '1/13/14 10:44pm',
				versionNumber: 12,
				hasWIP: 'Yes'
			]
		]

    	render mapo as JSON
	}
}
