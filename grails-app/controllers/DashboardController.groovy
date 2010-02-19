import grails.converters.JSON

class DashboardController {
	
	def userPreferenceService
    
	def index = { 
		
		def projectId = params.projectId
		def project
		def moveEventsList
		def projectLogo
		def moveEvent
		def moveBundleList
		
		if( !projectId ){
			project = Project.findById( getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ )
		} else {
			project = Project.findById( projectId )
		}
		moveEventsList = MoveEvent.findAllByProject(project)
		projectLogo = ProjectLogo.findByProject(project)
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
        if( moveEvent ){
			moveBundleList = MoveBundle.findAll(" FROM MoveBundle mb where moveEvent = ${moveEvent.id} ORDER BY mb.startTime ")				
		}
		return [ moveEventsList : moveEventsList, moveEvent : moveEvent, project : project, 
				projectLogo : projectLogo, moveBundleList : moveBundleList ]
    }
	
	/*---------------------------------------------------------
	 * Will set user preference for DASHBOARD_REFRESH time
	 * @author : Lokanath Reddy
	 * @param  : refresh time 
	 * @return : refresh time 
	 *---------------------------------------------------------*/
	def setTimePreference = {
        def timer = params.timer
        def refreshTime =[]
        if(timer){
            userPreferenceService.setPreference( "DASHBOARD_REFRESH", "${timer}" )
        }
        def timeToRefresh = getSession().getAttribute("DASHBOARD_REFRESH")
        refreshTime <<[refreshTime:timeToRefresh]
        render refreshTime as JSON
	}
}
