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
		
		if(moveEventId){
			userPreferenceService.setPreference( "MOVE_EVENT", "${moveEventId}" )
            moveEvent = MoveEvent.findById(moveEventId)
		} else {
            userPreferenceService.loadPreferences("MOVE_EVENT")
            def defaultEvent = getSession().getAttribute("MOVE_EVENT")
            if(defaultEvent.MOVE_EVENT){
            	moveEvent = MoveEvent.findById(defaultEvent.MOVE_EVENT)
            	if( moveEvent?.project?.id != project?.id ){
            		moveEvent = MoveEvent.find("from MoveEvent me where me.project = ? order by me.name asc",[project])
            	}
            } else {
            	moveEvent = MoveEvent.find("from MoveEvent me where me.project = ? order by me.name asc",[project])
            }
        }
        def timeToUpdate = getSession().getAttribute("DASHBOARD_REFRESH")
        if( moveEvent ){
        	userPreferenceService.setPreference("MOVE_EVENT","${moveEvent.id}")
			moveBundleList = MoveBundle.findAll(" FROM MoveBundle mb where moveEvent = ${moveEvent.id} ORDER BY mb.startTime ")				
		}
		return [ moveEventsList : moveEventsList, moveEvent : moveEvent, project : project, projectLogo : projectLogo, 
				 moveBundleList : moveBundleList, timeToUpdate : timeToUpdate ? timeToUpdate.DASHBOARD_REFRESH : "never" ]
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
