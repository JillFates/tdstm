class DashboardController {
	def userPreferenceService
    def index = { 
		def projectId = params.projectId
		def project
		def moveEvents
		def projectLogo
		if(projectId){
			project = Project.findById( getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ )
			moveEvents = MoveEvent.findAllByProject(project)
			projectLogo = ProjectLogo.findByProject(project)
		}
		
        userPreferenceService.loadPreferences("MOVE_EVENT")
		def moveEvent = userPreferenceService.getPreference("MOVE_EVENT")
		if( !moveEvent && moveEvents){
			userPreferenceService.setPreference("MOVE_EVENT","${moveEvents[0].id}")
			moveEvent = "${moveEvents[0].id}"
		}
		return [moveEvents : moveEvents, moveEvent : moveEvent, project : project, projectLogo :projectLogo ]
    }
}
