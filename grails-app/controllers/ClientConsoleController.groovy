import grails.converters.JSON
class ClientConsoleController {
	def stateEngineService
	def userPreferenceService
    def  jdbcTemplate
    def index = { 
    	redirect(action:list,params:params)
    }
    def list={
        def projectId=params.projectId
        def projectMap
        def stateId
        def holdId
        def releaseId
        def reRackId
        def check 
        def assetCheckList = []
        def appValue=params.application
        def appOwnerValue=params.appOwner
        def appSmeValue=params.appSme
        def sortby = params.sort
        def order = params.order
        def applicationList=AssetEntity.executeQuery("select distinct ae.application from AssetEntity ae where ae.application is not null and ae.project.id="+projectId)
        def appOwnerList=AssetEntity.executeQuery("select distinct ae.appOwner from AssetEntity ae where ae.appOwner is not null and ae.project.id="+projectId)
        def appSmeList=AssetEntity.executeQuery("select distinct ae.appSme from AssetEntity ae where ae.appSme is not null and ae.project.id="+projectId)
        def query = new StringBuffer("select ae.asset_entity_id as id,ae.application,ae.app_owner as appOwner,ae.app_sme as appSme,ae.asset_name as assetName,GROUP_CONCAT(state_to ORDER BY state_to SEPARATOR ',') as transitions FROM asset_entity ae LEFT JOIN asset_transition at ON (at.asset_entity_id = ae.asset_entity_id) where ae.project_id = $projectId ")
        if(appValue!="" && appValue!= null){
        	query.append(" and ae.application ='$appValue'")
        }
        if(appOwnerValue!="" && appOwnerValue!= null){
        	query.append(" and ae.app_owner='$appOwnerValue'")
        }
        if(appSmeValue!="" && appSmeValue!= null){
        	query.append(" and ae.app_sme='$appSmeValue'")
        }
        query.append(" GROUP BY ae.asset_entity_id")
        
        if(sortby != "" && sortby != null){
        	query.append(" order by $sortby")
        }
        if(order != "" && order != null){
        	query.append(" $order ")
        }
        def resultList=jdbcTemplate.queryForList(query.toString())
        def assetEntityList=[]
        resultList.each{
        	def transitions
        	if(it.transitions){
        		transitions =it.transitions.tokenize(',')
        	}
        	def assetEntity = AssetEntity.get(it.id)
        	projectMap = ProjectAssetMap.findByAsset(assetEntity)
        	stateId = projectMap.currentStateId
        	holdId = Integer.parseInt(stateEngineService.getStateId("STD_PROCESS","Hold"))
        	releaseId = Integer.parseInt(stateEngineService.getStateId("STD_PROCESS","Release"))
        	reRackId = Integer.parseInt(stateEngineService.getStateId("STD_PROCESS","Reracked"))
        	if((stateId > holdId && stateId < releaseId) || (stateId > reRackId)){
        		check = true
        	}else{
        		check = false
        	}
        	assetEntityList << [id: it.id, application:it.application,appOwner:it.appOwner,appSme:it.appSme,assetName:it.assetName,transitions:transitions,checkVal:check]
        }
        def processTransitions=stateEngineService.getTasks("STD_PROCESS")
        def processTransitionList=[]
        processTransitions.sort().each{
        	def processTransition = stateEngineService.getState("STD_PROCESS",Integer.parseInt(it))
            if(processTransition.length() > 5){
                processTransitionList<<[header:processTransition.substring(0,5), transId:stateEngineService.getStateId("STD_PROCESS",processTransition)]
            } else {
                processTransitionList<<[header:processTransition, transId:stateEngineService.getStateId("STD_PROCESS",processTransition)]
            }
        }
        userPreferenceService.loadPreferences("CLIENT_CONSOLE_REFRESH")
        def timeToRefresh = getSession().getAttribute("CLIENT_CONSOLE_REFRESH")
        return [assetEntityList:assetEntityList,appOwnerList:appOwnerList,applicationList:applicationList,appSmeList:appSmeList,projectId:projectId,processTransitionList:processTransitionList,projectId:projectId,appOwnerValue:appOwnerValue,appValue:appValue,appSmeValue:appSmeValue,timeToRefresh:timeToRefresh ? timeToRefresh.CLIENT_CONSOLE_REFRESH : "never"]
    }
	
	def getTask = {
        def stateVal
        def taskList
        def totalList = []
        def assetId = params.assetEntity
        def projectMap = ProjectAssetMap.find("from ProjectAssetMap pam where pam.asset = ${params.assetEntity}")
        if(projectMap != null){
			stateVal = stateEngineService.getState("STD_PROCESS",projectMap.currentStateId)
			taskList = stateEngineService.getTasks("STD_PROCESS","MANAGER",stateVal)
        }
        totalList<<[item:taskList,asset:assetId]
        render totalList as JSON
	}
	
	def changeStatus = {
			
        def changeId = stateEngineService.getStateId("STD_PROCESS",params.taskList)
        ProjectAssetMap.executeUpdate("update ProjectAssetMap pam set pam.currentStateId = $changeId where pam.asset = ${params.asset}")
        redirect(action:'list',params:["projectId":params.projectId])
	}
	def setTimePreference = {
        def timer = params.timer
        def refreshTime =[]
        if(timer){
            userPreferenceService.setPreference( "CLIENT_CONSOLE_REFRESH", "${timer}" )
        }
        def timeToRefresh = getSession().getAttribute("CLIENT_CONSOLE_REFRESH")
        refreshTime <<[refreshTime:timeToRefresh]
        render refreshTime as JSON
	}
}
