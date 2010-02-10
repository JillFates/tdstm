class DashboardController {
	def userPreferenceService
    def index = { 
		def projectId = params.projectId
		def project
		def moveEventsList
		def projectLogo
		def moveEvent
		if(projectId){
			project = Project.findById( getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ )
			moveEventsList = MoveEvent.findAllByProject(project)
			projectLogo = ProjectLogo.findByProject(project)
		}
		def moveEventId = params.moveEvent
		if(!moveEventId){
			moveEventId = getSession().getAttribute( "MOVE_EVENT" )?.MOVE_EVENT
		}
		if(moveEventId){
			moveEvent = MoveEvent.get(moveEventId)
			userPreferenceService.setPreference("MOVE_EVENT",moveEventId)
		} else if(moveEventsList){
			moveEvent = moveEventsList?.get(0)
		}
        userPreferenceService.loadPreferences("MOVE_EVENT")
		if( !moveEvent && moveEventsList){
			userPreferenceService.setPreference("MOVE_EVENT","${moveEventsList[0].id}")
			moveEvent = "${moveEventsList[0].id}"
		}
		return [moveEventsList : moveEventsList, moveEvent : moveEvent, project : project, projectLogo :projectLogo ]
    }
}
