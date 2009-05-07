import grails.converters.JSON
import org.jsecurity.SecurityUtils
class ClientConsoleController {
	def stateEngineService
	def userPreferenceService
	def workflowService
    def  jdbcTemplate
    def index = { 
    	redirect(action:list,params:params)
    }
	// List of asset for client console
    def list={
        def projectId=params.projectId
        def bundleId = params.moveBundle
        def moveBundleInstance
        def projectMap
        def stateId
        def holdId
        def releaseId
        def reRackId
        def check 
        def appValue=params.application
        def appOwnerValue=params.appOwner
        def appSmeValue=params.appSme
        def sortby = params.sort
        def order = params.order
        def projectInstance = Project.findById( projectId )
        def moveBundleInstanceList = MoveBundle.findAll("from MoveBundle mb where mb.project = ${projectInstance.id} order by mb.name asc")
        if(bundleId){
            moveBundleInstance = MoveBundle.findById(bundleId)
        } else {
            moveBundleInstance = MoveBundle.findByProject(projectInstance)
        }
        def applicationList=AssetEntity.executeQuery("select distinct ae.application from AssetEntity ae where ae.application is not null and ae.project.id="+projectId)
        def appOwnerList=AssetEntity.executeQuery("select distinct ae.appOwner from AssetEntity ae where ae.appOwner is not null and ae.project.id="+projectId)
        def appSmeList=AssetEntity.executeQuery("select distinct ae.appSme from AssetEntity ae where ae.appSme is not null  and ae.project.id="+projectId)
        def query = new StringBuffer("select ae.asset_entity_id as id,ae.application,ae.app_owner as appOwner,ae.app_sme as appSme,ae.asset_name as assetName,GROUP_CONCAT(state_to ORDER BY state_to SEPARATOR ',') as transitions FROM asset_entity ae LEFT JOIN asset_transition at ON (at.asset_entity_id = ae.asset_entity_id) where ae.project_id = $projectId and ae.move_bundle_id = ${moveBundleInstance.id}")
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
        def processTransitionList=[]
        def tempTransitions = []
        def processTransitions= stateEngineService.getTasks("STD_PROCESS")
        processTransitions.each{
        	tempTransitions <<Integer.parseInt(it)
        }
        tempTransitions.sort().each{
        	def processTransition = stateEngineService.getState("STD_PROCESS",it)
            if(processTransition.length() > 5){
                processTransitionList<<[header:processTransition.substring(0,5), title:stateEngineService.getStateLabel("STD_PROCESS",it),transId:stateEngineService.getStateId("STD_PROCESS",processTransition)]
            } else {
                processTransitionList<<[header:processTransition,title:stateEngineService.getStateLabel("STD_PROCESS",it),transId:stateEngineService.getStateId("STD_PROCESS",processTransition)]
            }
        }
        userPreferenceService.loadPreferences("CLIENT_CONSOLE_REFRESH")
        def timeToRefresh = getSession().getAttribute("CLIENT_CONSOLE_REFRESH")
        return [moveBundleInstance:moveBundleInstance,moveBundleInstanceList:moveBundleInstanceList,assetEntityList:assetEntityList,appOwnerList:appOwnerList,applicationList:applicationList,appSmeList:appSmeList,projectId:projectId,processTransitionList:processTransitionList,projectId:projectId,appOwnerValue:appOwnerValue,appValue:appValue,appSmeValue:appSmeValue,timeToRefresh:timeToRefresh ? timeToRefresh.CLIENT_CONSOLE_REFRESH : "never"]
    }
    // To get list of task for an asset through ajax
	def getTask = {
        def stateVal
        def taskList = []
        def temp
        def totalList = []
        def tempTaskList = []
        def assetId = params.assetEntity
        def projectMap = ProjectAssetMap.find("from ProjectAssetMap pam where pam.asset = ${params.assetEntity}")
        if(projectMap != null){
			stateVal = stateEngineService.getState("STD_PROCESS",projectMap.currentStateId)
			temp = stateEngineService.getTasks("STD_PROCESS","MANAGER",stateVal)
        }
        temp.each{
        	tempTaskList << Integer.parseInt(stateEngineService.getStateId("STD_PROCESS",it))
        }
        tempTaskList.sort().each{
        	taskList << stateEngineService.getState("STD_PROCESS",it)
        }
        totalList<<[item:taskList,asset:assetId]
        render totalList as JSON
	}
    // To change the status for an asset
	def changeStatus = {
        def assetId = params.asset
			 
        def assetEnt = AssetEntity.findById(assetId)
        def bundle = assetEnt.moveBundle
        def principal = SecurityUtils.subject.principal
        def loginUser = UserLogin.findByUsername(principal)
        def team = assetEnt.sourceTeam
		     
        def workflow = workflowService.createTransition("STD_PROCESS","MANAGER",params.taskList,assetEnt,bundle,loginUser,team,params.enterNote)
        if(workflow.success){
        	if(params.enterNote != ""){
                def assetComment = new AssetComment()
                assetComment.comment = params.enterNote
                assetComment.commentType = 'issue'
                assetComment.assetEntity = assetEnt
                assetComment.save()
            }
            redirect(action:'list',params:["projectId":params.projectId])
		}else{
        	flash.message = message(code :workflow.message)	
            redirect(action:'list',params:["projectId":params.projectId])
        }
       
	}
	// Refresh time selection 
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
