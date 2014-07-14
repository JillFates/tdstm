import grails.converters.JSON

import java.text.SimpleDateFormat

import org.apache.commons.lang.StringUtils

import com.tds.asset.Application
import com.tds.asset.AssetComment
import com.tds.asset.AssetDependency
import com.tds.asset.AssetDependencyBundle
import com.tds.asset.AssetEntity
import com.tds.asset.AssetType
import com.tds.asset.AssetTransition
import com.tds.asset.Database
import com.tds.asset.Files
import com.tdsops.tm.enums.domain.*
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WebUtil
import com.tds.asset.AssetType

class MoveBundleController {

	def commentService
	def jdbcTemplate
	def moveBundleService
	def partyRelationshipService
	def securityService
	def stateEngineService
	def stepSnapshotService
    def taskService
	def userPreferenceService
    
	protected static String dependecyBundlingAssetType = "('server','vm','blade','Application','Files','Database','Appliance','Storage')"  
	
	def index = { redirect(action:list,params:params) }

	// the delete, save and update actions only accept POST requests
	def allowedMethods = [delete:'POST', save:'POST', update:'POST']

	def list = {
	}
	
	/**
	 * Used to generate the List for Bundles using jqgrid.
	 * @return : list of bundles as JSON
	 */
	def listJson = {
		def sortIndex = params.sidx ?: 'name'
		def sortOrder  = params.sord ?: 'asc'
		def maxRows = Integer.valueOf(params.rows)
		def currentPage = Integer.valueOf(params.page) ?: 1
		def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows
		def project = securityService.getUserCurrentProject()
		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
		def dueFormatter = new SimpleDateFormat("MM/dd/yyyy")
		
		def startDates = params.startTime ? MoveBundle.findAll("from MoveBundle where project =:project and startTime like '%${params.startTime}%'",[project:project])?.startTime : []
		def completionDates = params.completionTime ? MoveBundle.findAll("from MoveBundle where project =:project and completionTime like '%${params.completionTime}%'",[project:project])?.completionTime : []
		
		def bundleList = MoveBundle.createCriteria().list(max: maxRows, offset: rowOffset) {
				eq('project',project)
				if (params.name)
					ilike('name', "%${params.name}%")
				if (params.description)
					ilike('description', "%${params.description}%")
				if (params.useForPlanning)
					eq('useForPlanning', (params.useForPlanning.equalsIgnoreCase('Y') ? true : false))
				if (startDates) 
					'in'('startTime' , startDates)
				if (completionDates)
					'in'('completionTime' , completionDates)
				
				if(sortIndex!='assetQty')	
					order(sortIndex, sortOrder).ignoreCase()
		}
		def totalRows = bundleList.totalCount
		def numberOfPages = Math.ceil(totalRows / maxRows)
		
		//Sorting by assetQuantity per page
		if(sortIndex=='assetQty')
			bundleList.sort{(sortOrder=='desc' ? -it.assetQty : it.assetQty)}
		
		def results = bundleList?.collect {
			[ cell: [it.name, it.description, (it.useForPlanning ? 'Y' : 'N'), it.assetQty, 
				(it.startTime ? dueFormatter.format(TimeUtil.convertInToUserTZ(it.startTime, tzId)):''),
				(it.completionTime ? dueFormatter.format(TimeUtil.convertInToUserTZ(it.completionTime, tzId)):'')],
				 id: it.id]
			}

		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]

		render jsonData as JSON
	}
	def show = {
		userPreferenceService.loadPreferences("MOVE_EVENT")
		def moveBundleId = params.id

		moveBundleId = moveBundleId ? moveBundleId : session.getAttribute("CURR_BUNDLE")?.CURR_BUNDLE;
		if(moveBundleId){

			def moveBundleInstance = MoveBundle.get( moveBundleId )
			//request.getSession(false).setAttribute("MOVEBUNDLE",moveBundleInstance)
			def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ

			if(!moveBundleInstance) {
				flash.message = "MoveBundle not found with id ${moveBundleId}"
				redirect(action:list)
			} else {
				userPreferenceService.setPreference( "CURR_BUNDLE", "${moveBundleInstance.id}" )
				def projectManager = partyRelationshipService.getPartyToRelationship( "PROJ_BUNDLE_STAFF", moveBundleInstance.id, "MOVE_BUNDLE", "PROJ_MGR" )
				//PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_BUNDLE_STAFF' and p.partyIdFrom = $moveBundleInstance.id and p.roleTypeCodeFrom = 'MOVE_BUNDLE' and p.roleTypeCodeTo = 'PROJ_MGR' ")
				def moveManager = partyRelationshipService.getPartyToRelationship( "PROJ_BUNDLE_STAFF", moveBundleInstance.id, "MOVE_BUNDLE", "MOVE_MGR" )
				//PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_BUNDLE_STAFF' and p.partyIdFrom = $moveBundleInstance.id and p.roleTypeCodeFrom = 'MOVE_BUNDLE' and p.roleTypeCodeTo = 'MOVE_MGR' ")


				// get the list of Manual Dashboard Steps that are associated to moveBundle.project
				def moveBundleSteps = MoveBundleStep.findAll('FROM MoveBundleStep mbs WHERE mbs.moveBundle = :mb ORDER BY mbs.transitionId',[mb:moveBundleInstance])
				def dashboardSteps = []

				moveBundleSteps .each{
					def stepSnapshot = StepSnapshot.findAll("FROM StepSnapshot ss WHERE ss.moveBundleStep = :msb ORDER BY ss.dateCreated DESC",[msb:it, max:1])
					dashboardSteps << [moveBundleStep : it, stepSnapshot : stepSnapshot[0] ]
				}
				def showHistoryButton = false
				def bundleTransition = AssetTransition.findAll("FROM AssetTransition at WHERE at.assetEntity in (SELECT ae.id FROM AssetEntity ae WHERE ae.moveBundle = ${moveBundleInstance.id})" )

				if( bundleTransition.size() > 0 )
					showHistoryButton = true

				return [ moveBundleInstance : moveBundleInstance, projectId:projectId, projectManager: projectManager,
					moveManager: moveManager, dashboardSteps:dashboardSteps, showHistoryButton : showHistoryButton ]
			}
		} else {
			redirect(action:list)
		}
	}

	def delete = {
		def moveBundleInstance = MoveBundle.get( params.id )
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		if(moveBundleInstance) {
			AssetEntity.withTransaction { status ->
				try{
					// Update asset associations
					AssetEntity.executeUpdate("UPDATE AssetEntity SET moveBundle = null WHERE moveBundle = ?",[moveBundleInstance])
					// Delete Bundle and associations
					moveBundleService.deleteMoveBundleAssociates(moveBundleInstance)

					moveBundleInstance.delete(flush:true)

					flash.message = "MoveBundle ${moveBundleInstance} deleted"
					redirect(action:list)

				}catch(Exception ex){
					status.setRollbackOnly()
					flash.message = "Unable to Delete MoveBundle Assosiated with Teams "+ex
					redirect(action:list)
				}
			}
		} else {
			flash.message = "MoveBundle not found with id ${params.id}"
			redirect(action:list)
		}
	}
	def deleteBundleAndAssets = {
		def moveBundleInstance = MoveBundle.get( params.id )
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		if(moveBundleInstance) {
			AssetEntity.withTransaction { status ->
				try{
					// Update asset associations
					moveBundleService.deleteBundleAssetsAndAssociates(moveBundleInstance)
					// Delete Bundle and associations
					moveBundleService.deleteMoveBundleAssociates(moveBundleInstance)

					moveBundleInstance.delete(flush:true)
					flash.message = "MoveBundle ${moveBundleInstance} deleted"
					redirect(action:list)

				}catch(Exception ex){
					status.setRollbackOnly()
					flash.message = "Unable to Delete MoveBundle Assosiated with Teams "+ex
					redirect(action:list)
				}
			}
		} else {
			flash.message = "MoveBundle not found with id ${params.id}"
			redirect(action:list)
		}
	}
	def edit = {
		def moveBundleInstance = MoveBundle.get( params.id )
		stateEngineService.loadWorkflowTransitionsIntoMap(moveBundleInstance.workflowCode, 'project')
		def project = securityService.getUserCurrentProject()
		if(!moveBundleInstance) {
			flash.message = "MoveBundle not found with id ${params.id}"
			redirect(action:list)
		} else {
			def managers = partyRelationshipService.getProjectStaff( project.id )
			def projectManager = partyRelationshipService.getPartyToRelationship( "PROJ_BUNDLE_STAFF", moveBundleInstance.id, "MOVE_BUNDLE", "PROJ_MGR" )
			//PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_BUNDLE_STAFF' and p.partyIdFrom = $moveBundleInstance.id and p.roleTypeCodeFrom = 'MOVE_BUNDLE' and p.roleTypeCodeTo = 'PROJ_MGR' ")
			def moveManager = partyRelationshipService.getPartyToRelationship( "PROJ_BUNDLE_STAFF", moveBundleInstance.id, "MOVE_BUNDLE", "MOVE_MGR" )
			//PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_BUNDLE_STAFF' and p.partyIdFrom = $moveBundleInstance.id and p.roleTypeCodeFrom = 'MOVE_BUNDLE' and p.roleTypeCodeTo = 'MOVE_MGR' ")
			if( projectManager != null ){
				projectManager = projectManager.partyIdTo.id
			}
			if( moveManager != null ){
				moveManager = moveManager.partyIdTo.id
			}


			//get the all Dashboard Steps that are associated to moveBundle.project
			def allDashboardSteps = moveBundleService.getAllDashboardSteps( moveBundleInstance )
			def remainingSteps = allDashboardSteps.remainingSteps
			def workflowCodes = stateEngineService.getWorkflowCode()
			def rooms = Room.findAllByProject( project )


			return [ moveBundleInstance : moveBundleInstance, projectId: project.id, managers: managers, projectManager: projectManager,
				moveManager: moveManager, dashboardSteps: allDashboardSteps.dashboardSteps?.sort{it["step"].id}, remainingSteps : remainingSteps, workflowCodes:workflowCodes,
				rooms:rooms]

		}
	}

	def update = {

		// TODO : Security : Get User's project and attempt to find the project before blindly updating it

		def moveBundleInstance = MoveBundle.get( params.id )
		def projectManagerId = params.projectManager
		def moveManagerId = params.moveManager
		if( moveBundleInstance ) {
			moveBundleInstance.name = params.name
			moveBundleInstance.description = params.description
			moveBundleInstance.workflowCode = params.workflowCode
			if(params.useForPlanning){
				moveBundleInstance.useForPlanning = true
			}else{
				moveBundleInstance.useForPlanning = false
			}
			if(params.moveEvent.id){
				moveBundleInstance.moveEvent = MoveEvent.get(params.moveEvent.id)
			} else {
				moveBundleInstance.moveEvent = null
			}
			moveBundleInstance.operationalOrder = params.operationalOrder ? Integer.parseInt(params.operationalOrder) : 1
			def formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a")
			def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
			def startTime = params.startTime
			def completionTime = params.completionTime
			
			moveBundleInstance.startTime = startTime? GormUtil.convertInToGMT(formatter.parse( startTime ), tzId) : null
			
			moveBundleInstance.completionTime =  completionTime ? GormUtil.convertInToGMT(formatter.parse( completionTime ), tzId) : null

			// TODO : SECURITY : Should be confirming that the rooms belong to the moveBundle.project instead of blindly assigning plus should be
			// validating that the rooms even exist.			
			moveBundleInstance.sourceRoom = params.sourceRoom ? Room.read( params.sourceRoom ) : null
			moveBundleInstance.targetRoom = params.targetRoom ? Room.read( params.targetRoom ) : null
			
			if(moveBundleInstance.validate(true) && moveBundleInstance.save() ) {
				stateEngineService.loadWorkflowTransitionsIntoMap(moveBundleInstance.workflowCode, 'project')
				def stepsList = stateEngineService.getDashboardSteps( moveBundleInstance.workflowCode )
				stepsList.each{
					def checkbox = params["checkbox_"+it.id]
					if(checkbox  && checkbox == 'on'){
						def moveBundleStep = moveBundleService.createMoveBundleStep(moveBundleInstance, it.id, params)
						def tasksCompleted = params["tasksCompleted_"+it.id] ? Integer.parseInt(params["tasksCompleted_"+it.id]) : 0
						def calcMethod = params["calcMethod_"+it.id]
						if(calcMethod == "L"){
							stepSnapshotService.createLinearSnapshot( moveBundleInstance.id, moveBundleStep.id )
						} else {
							stepSnapshotService.createManualSnapshot( moveBundleInstance.id, moveBundleStep.id, tasksCompleted )
						}
					} else {
						def moveBundleStep = MoveBundleStep.findByMoveBundleAndTransitionId(moveBundleInstance , it.id)
						if( moveBundleStep ){
							moveBundleService.deleteMoveBundleStep( moveBundleStep )
						}
					}
				}

				//def projectManegerInstance = Party.findById( projectManagerId )
				def updateMoveBundlePMRel = partyRelationshipService.updatePartyRelationshipPartyIdTo("PROJ_BUNDLE_STAFF", moveBundleInstance.id, "MOVE_BUNDLE", projectManagerId, "PROJ_MGR" )
				def updateMoveBundleMMRel = partyRelationshipService.updatePartyRelationshipPartyIdTo("PROJ_BUNDLE_STAFF", moveBundleInstance.id, "MOVE_BUNDLE", moveManagerId, "MOVE_MGR" )
				flash.message = "MoveBundle ${moveBundleInstance} updated"
				//redirect(action:show,params:[id:moveBundleInstance.id, projectId:projectId])
				redirect(action:show,id:moveBundleInstance.id)
			} else {
				//	get the all Dashboard Steps that are associated to moveBundle.project
				def allDashboardSteps = moveBundleService.getAllDashboardSteps( moveBundleInstance )
				def remainingSteps = allDashboardSteps.remainingSteps
				
				moveBundleInstance.discard()
				def project = securityService.getUserCurrentProject()
				def managers = partyRelationshipService.getProjectStaff( project.id )
				def projectManager = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_BUNDLE_STAFF' and p.partyIdFrom = $moveBundleInstance.id and p.roleTypeCodeFrom = 'MOVE_BUNDLE' and p.roleTypeCodeTo = 'PROJ_MGR' ")
				def moveManager = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_BUNDLE_STAFF' and p.partyIdFrom = $moveBundleInstance.id and p.roleTypeCodeFrom = 'MOVE_BUNDLE' and p.roleTypeCodeTo = 'MOVE_MGR' ")
				def workflowCodes = stateEngineService.getWorkflowCode()
				def rooms = Room.findAllByProject( project )
				render(view:'edit',model:[moveBundleInstance:moveBundleInstance, projectId: project.id, managers: managers, projectManager: projectManagerId,
							moveManager: moveManagerId, dashboardSteps:allDashboardSteps.dashboardSteps, remainingSteps : remainingSteps,rooms:rooms,workflowCodes:workflowCodes ])
			}
		}
		else {
			flash.message = "MoveBundle not found with id ${params.id}"
			redirect(action:edit,id:params.id)
		}
	}

	def create = {
		def moveBundleInstance = new MoveBundle()
		def project = securityService.getUserCurrentProject()
		def workflowCodes = stateEngineService.getWorkflowCode()
		moveBundleInstance.properties = params
		def managers = partyRelationshipService.getProjectStaff( project.id )
		def rooms = Room.findAllByProject( project )
		return ['moveBundleInstance':moveBundleInstance, managers: managers ,projectInstance:project,workflowCodes:workflowCodes, rooms:rooms]

	}

	def save = {

		def formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a")
		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
		def startTime = params.startTime
		def completionTime = params.completionTime
		if(startTime){
			params.startTime =  GormUtil.convertInToGMT(formatter.parse( startTime ), tzId)
		}
		if(completionTime){
			params.completionTime =  GormUtil.convertInToGMT(formatter.parse( completionTime ), tzId)
		}
		
		params.sourceRoom = params.sourceRoom ? Room.read( params.sourceRoom ) : null 
		
		params.targetRoom = params.targetRoom ? Room.read( params.targetRoom ) : null

		def moveBundleInstance = new MoveBundle(params)
		def project = securityService.getUserCurrentProject()
		def projectManager = params.projectManager
		def moveManager = params.moveManager
		def managers = partyRelationshipService.getProjectStaff( project.id )
		if(params.useForPlanning){
			moveBundleInstance.useForPlanning = true
		}else{
			moveBundleInstance.useForPlanning = false
		}
		if(!moveBundleInstance.hasErrors() && moveBundleInstance.save()) {
			if( projectManager != null && projectManager != ""){
				def projectManegerInstance = Party.findById( projectManager )
				def pmPartyRelation = partyRelationshipService.savePartyRelationship( "PROJ_BUNDLE_STAFF", moveBundleInstance, "MOVE_BUNDLE", projectManegerInstance, "PROJ_MGR")
			}
			if( moveManager != null && moveManager != "" ){
				def moveManegerInstance = Party.findById( moveManager )
				def mmPartyRelation = partyRelationshipService.savePartyRelationship( "PROJ_BUNDLE_STAFF", moveBundleInstance, "MOVE_BUNDLE", moveManegerInstance, "MOVE_MGR")
			}

			flash.message = "MoveBundle ${moveBundleInstance} created"
			redirect(action:show,params:[id:moveBundleInstance.id])
		}
		else {
			def workflowCodes = stateEngineService.getWorkflowCode()
			def rooms = Room.findAllByProject( project )
			render(view:'create',model:[moveBundleInstance:moveBundleInstance, managers: managers, projectManager: projectManager, moveManager: moveManager,workflowCodes:workflowCodes,rooms:rooms])
		}
	}

	/**
	 * Used to create StepSnapshot records for specified MoveBundle/MoveBundleStep.  
	 * @param moveBundleId
	 * @param moveBundleStepId 
	 * @param tasksCompleted value 0-100 representing % completed
	 * @return Returns 200 okay or appropriate error message
	 */
	def createManualStep = {

		try {
			def moveBundleId = Integer.parseInt(params.moveBundleId)
			def moveBundleStepId = Integer.parseInt(params.moveBundleStepId)
			def tasksCompleted = Integer.parseInt(params.tasksCompleted)
			if ( tasksCompleted < 0 || tasksCompleted > 100 ) {
				response.sendError( 400, "Bad Request P")
				// render("400 Bad Request")
				return false
			}

			def result = stepSnapshotService.createManualSnapshot( moveBundleId, moveBundleStepId, tasksCompleted)

			if (result == 200)
				render ("Record created")
			else
				response.sendError( result , "Error ${result}" )
		} catch(NumberFormatException nfe) {
			nfe.printStackTrace()
			response.sendError( 400, "Bad Request NFE")
		}

	}
	/* if the checkbox is subsequently checked and the form submitted, a new MoveBundleStep shall be created for that transition.
	 *  @param moveBundleId
	 * 	@param transitionId
	 * 	@return  new moveBundleStep
	 */
	def createMoveBundleStep = {
		def moveBundle = MoveBundle.get( params.moveBundleId )
		def transitionId = Integer.parseInt( params.transitionId )
		def moveBundleStep = MoveBundleStep.findByMoveBundleAndTransitionId(moveBundle , transitionId)
		if( !moveBundleStep ){
			moveBundleStep = new MoveBundleStep(moveBundle:moveBundle, transitionId:transitionId, calcMethod:"L")
			moveBundleStep.label = stateEngineService.getDashboardLabel( moveBundle.workflowCode, transitionId )
			if ( !moveBundleStep.validate() || !moveBundleStep.save(flush:true) ) {
				def etext = "Unable to create moveBundleStep" +
						GormUtil.allErrorsString( moveBundleStep )
				response.sendError( 500, "Validation Error")
				log.error etext
			}
		}
		render moveBundleStep
	}
	/*-----------------------------------------------------
	 * remote function to verify stepSnapshot records for a list of steps.
	 * if there are more than one snapshots associated with any of the step in list 
	 * then return failure otherwise success.
	 * @param  : moveBundleId, list of unchecked steps
	 * @return : success / failure
	 *---------------------------------------------------*/
	def checkStepSnapshotRecord = {
		def steps = params.steps
		def moveBundle = MoveBundle.findById( params.moveBundleId )
		def transitionIds
		def message = "success"
		if(steps){
			transitionIds =	steps.split(",")
		}
		transitionIds.each{ transitionId ->
			def moveBundleStep = MoveBundleStep.findByMoveBundleAndTransitionId( moveBundle, transitionId )
			if(moveBundleStep){
				def stepSnapshot = StepSnapshot.findAllByMoveBundleStep( moveBundleStep )
				if(stepSnapshot.size() > 1){
					message = "failure"
					return
				}
			}
		}
		render message;
	}

	def projectMoveBundles = {
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def moveBundlesList
		if(projectId){
			moveBundlesList = MoveBundle.findAllByProject(Project.get(projectId),[sort:'name',order:'asc'])
		}
		render moveBundlesList as JSON
	}
	/*
	 * Clear any transitions for the assets in that bundle. 
	 * @param : bundleId, ProjectId
	 * */
	def clearBundleAssetHistory = {
		def bundleId = params.id
		if(bundleId){
			def moveBundle = MoveBundle.get( bundleId )
			AssetTransition.executeUpdate("DELETE FROM AssetTransition at WHERE at.assetEntity in (SELECT ae.id FROM AssetEntity ae WHERE ae.moveBundle = ${moveBundle.id})" )
			AssetEntity.executeUpdate("UPDATE AssetEntity ae SET ae.currentStatus = null WHERE ae.moveBundle = ?", [moveBundle])
			ProjectAssetMap.executeUpdate("DELETE FROM ProjectAssetMap WHERE asset in (SELECT ae.id FROM AssetEntity ae WHERE ae.moveBundle = ${moveBundle.id})")
			MoveBundleStep.executeUpdate("UPDATE MoveBundleStep mbs SET mbs.actualStartTime = null , mbs.actualCompletionTime = null WHERE mbs.moveBundle = ?", [moveBundle])
			stepSnapshotService.process( bundleId )
		}
		redirect(action:show,params:[id:params.id])
	}
	/**
	 * 
	 */
	def planningStats = {
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def tzId = session.getAttribute( "CURR_TZ" )?.CURR_TZ
		def project = Project.get(projectId)
		def bundleTimeformatter = new SimpleDateFormat("dd-MMM")
		def appList = []
		def assetList = []
		def eventStartDate = [:]
		
		def unassignedPlan = AssetEntityPlanStatus.UNASSIGNED
		def assignedPlan = AssetEntityPlanStatus.ASSIGNED
		def confirmedPlan = AssetEntityPlanStatus.CONFIRMED
		def movedPlan = AssetEntityPlanStatus.MOVED
		
		def app = AssetType.APPLICATION.toString()
		def db = AssetType.DATABASE.toString()
		def files = AssetType.FILES.toString()
		def server = AssetType.SERVER.toString()
		def vm = AssetType.VM.toString()
		def blade = AssetType.BLADE.toString()
		def appliance = AssetType.APPLIANCE.toString()
		
		// Get list of all of the MoveBundles that are used for Planning
		def moveBundleList = MoveBundle.findAllByProjectAndUseForPlanning(project, true, [sort:'startTime'])

		// Nothing to report if there are no planning bundles so we'll just return with zeros for everything
		if (moveBundleList.size()==0) {
			render(model:[], view:'planningStats_NoBundles')
			return
		}

		// Get the list of Move Events and sort on the start date 
		List moveEventList = moveBundleList*.moveEvent.unique()
		moveEventList.remove(null)
		moveEventList = moveEventList.sort { 
			def start = it.getEventTimes().start
			(start ? "$start-${it.name}" : it.name)
		}

		// Forming query for multi-uses
		def baseWhere = 'WHERE ae.project=:project AND ae.moveBundle IN (:moveBundles)'
		def selectCount = 'SELECT count(ae)'
		def countArgs = [project:project, moveBundles:moveBundleList]

		def countQuery = "$selectCount FROM AssetEntity ae $baseWhere"
		def appQuery = "FROM Application ae $baseWhere"
		def appCountQuery = "$selectCount $appQuery"
		def dbQuery = "FROM Database ae $baseWhere"
		def dbCountQuery = "$selectCount $dbQuery"
		def filesQuery = "FROM Files ae $baseWhere"
		def filesCountQuery = "$selectCount $filesQuery"
		def phyStorageQuery = "FROM AssetEntity ae $baseWhere AND ae.assetClass=:assetClass AND ae.assetType IN (:type)"
		def phyStorageCountQuery = "$selectCount $phyStorageQuery"
		def deviceQuery = "FROM AssetEntity ae $baseWhere AND ae.assetClass=:assetClass AND ae.assetType IN (:type)"
		def deviceCountQuery = "$selectCount $deviceQuery"	
		def otherCountQuery = "$selectCount FROM AssetEntity ae $baseWhere AND ae.assetClass=:assetClass AND COALESCE(ae.assetType,'') NOT IN (:type)"

		def apps = Application.findAll(appQuery, countArgs)
		def applicationCount = apps.size()

		def databaseCount = Database.executeQuery(dbCountQuery, countArgs)[0]
		def fileCount = Files.executeQuery(filesCountQuery, countArgs)[0]
		def phyStorageCount = AssetEntity.executeQuery(phyStorageCountQuery, countArgs + [assetClass:AssetClass.DEVICE, type:AssetType.getStorageTypes()])[0]
		def assetCount = AssetEntity.executeQuery( deviceCountQuery, countArgs + [assetClass:AssetClass.DEVICE, type:AssetType.getAllServerTypes()] )[0]
		def physicalCount = AssetEntity.executeQuery( deviceCountQuery, countArgs + [assetClass:AssetClass.DEVICE, type:AssetType.getPhysicalServerTypes()] )[0]		
		def virtualCount = AssetEntity.executeQuery( deviceCountQuery, countArgs + [assetClass:AssetClass.DEVICE, type:AssetType.getVirtualServerTypes()] )[0]		
		def otherAssetCount= AssetEntity.executeQuery( otherCountQuery, countArgs + [assetClass:AssetClass.DEVICE, type:AssetType.getAllServerTypes()] )[0]

 		// Get the list of apps and servers assigned to planning bundles
		def applicationsOfPlanningBundle = Application.findAll(appQuery, countArgs) 
		def appDependenciesCount = applicationsOfPlanningBundle ? AssetDependency.countByAssetInList(applicationsOfPlanningBundle) : 0
		def serversOfPlanningBundle = AssetEntity.findAll(deviceQuery, countArgs + [ assetClass:AssetClass.DEVICE, type:AssetType.getAllServerTypes()])
		def serverDependenciesCount = serversOfPlanningBundle ? AssetDependency.countByAssetInList(serversOfPlanningBundle) : 0

		def assignedAssetCount
		def assignedApplicationCount
        def dbList = []
		def phyStorageList = []
		def filesList = []
		def otherTypeList = []
		def openTasks = []
		
		moveEventList.each{ moveEvent->
			// fetching bundles for current moveEvent which was set 'true' for useForPlanning 
			def moveBundles = moveEvent.moveBundles?.findAll {it.useForPlanning}
			def eventWiseArgs = [project:project, moveBundles:moveBundles]
			
			def eventDates = moveEvent.getEventTimes()
			eventStartDate << [(moveEvent.id):(eventDates.start ? bundleTimeformatter.format( GormUtil.convertInToGMT( eventDates.start, tzId) ) : 'TBD')]
			
			// Fetching application count that are assigned to current move event
			assignedApplicationCount = moveBundles ? Application.executeQuery(appCountQuery, eventWiseArgs)[0] : 0
			appList << ['count':assignedApplicationCount , 'moveEvent':moveEvent.id]
			
			// fetching physicalAsset (e.g. 'Server',blade ) and virtualAsset (e.g. 'VM') count that are assigned to current move-event .
			def physicalAssetCount = moveBundles ? AssetEntity.executeQuery(deviceCountQuery, eventWiseArgs + [ assetClass:AssetClass.DEVICE, type:AssetType.getPhysicalServerTypes() ])[0] : 0
			def virtualAssetCount = moveBundles ? AssetEntity.executeQuery(deviceCountQuery, eventWiseArgs + [ assetClass:AssetClass.DEVICE, type:AssetType.getVirtualServerTypes() ])[0] : 0
			def allServersCount = moveBundles ? AssetEntity.executeQuery(deviceCountQuery, eventWiseArgs + [ assetClass:AssetClass.DEVICE, type:AssetType.getAllServerTypes() ])[0] : 0
			
			assetList << [physicalCount:physicalAssetCount, virtualAssetCount:virtualAssetCount, count:allServersCount, moveEvent:moveEvent.id]
			
			def dbCount = moveBundles ? Database.executeQuery(dbCountQuery, eventWiseArgs)[0] : 0
			dbList << [moveEvent:moveEvent.id , count:dbCount]
						
			def phyStoragesCount = moveBundles ? AssetEntity.executeQuery(phyStorageCountQuery, eventWiseArgs + [ assetClass:AssetClass.DEVICE, type:AssetType.getStorageTypes() ])[0] : 0
			phyStorageList << ['moveEvent':moveEvent.id , 'count':phyStoragesCount]
			
			def filesCount = moveBundles ? Files.executeQuery(filesCountQuery, eventWiseArgs)[0] : 0
			filesList << ['moveEvent':moveEvent.id , 'count':filesCount]
			
			def otherCount = moveBundles ? AssetEntity.executeQuery(otherCountQuery, eventWiseArgs + [ assetClass:AssetClass.DEVICE, type:AssetType.getAllServerTypes() ])[0] : 0
			otherTypeList << [ moveEvent:moveEvent.id , count:otherCount ]
			
			def openIssues = AssetComment.findAll("FROM AssetComment a where a.project = :project and a.commentType = :type and a.status IN (:status) \
				and a.moveEvent = :event AND a.isPublished=true", [project:project, type:AssetCommentType.TASK, 
				status: [AssetCommentStatus.READY,AssetCommentStatus.STARTED,AssetCommentStatus.PENDING], event:moveEvent] )
			
			openTasks << [moveEvent:moveEvent.id , count:openIssues.size()]			
		}
		
		// ----------------------------------------------------------------------------
		// Get the totals count for assigned and unassigned assets
		// ----------------------------------------------------------------------------

		// Find Move bundles that are not assigned to a Event and is the bundle is used for planning
		def unassignedMoveBundles = MoveBundle.findAll("FROM MoveBundle mb WHERE mb.moveEvent IS NULL \
			AND mb.useForPlanning = true AND mb.project = :project ", [project:project])
		def assetTypeQuery = 'AND ae.assetType IN (:type)'

		// Construct the query that will include counts of non-event bundles if any exist just the bundle being NULL
		def assetCountQueryArgs = [ project:project ]
		def unassignedMBQuery = 'ae.moveBundle IS NULL'
		if (unassignedMoveBundles) {
			 unassignedMBQuery = "(ae.moveBundle IN (:unassignedMoveBundles) OR $unassignedMBQuery)"
			 assetCountQueryArgs.unassignedMoveBundles = unassignedMoveBundles
		}

		// Get DB Counts
		def unAssignedDBCountQuery = "SELECT COUNT(ae) FROM Database ae WHERE ae.project=:project AND $unassignedMBQuery"
		def unassignedDbCount = Database.executeQuery(unAssignedDBCountQuery, assetCountQueryArgs)[0]
		
		// Get Storage Counts
		def unAssignedFilesCountQuery = "SELECT COUNT(ae) FROM Files ae WHERE ae.project=:project AND $unassignedMBQuery"
		def unassignedFilesCount = Files.executeQuery(unAssignedFilesCountQuery, assetCountQueryArgs)[0]
		
		def unAssignedPhyStorgCountQuery = "SELECT COUNT(ae) FROM AssetEntity ae WHERE ae.project=:project AND ae.assetClass=:assetClass AND $unassignedMBQuery $assetTypeQuery"
		def unAssignedPhyStorgCount = AssetEntity.executeQuery(unAssignedPhyStorgCountQuery,  assetCountQueryArgs + [assetClass:AssetClass.DEVICE, type:AssetType.getStorageTypes() ])[0]

		// Get Application Counts
		def unAssignedAppsCountQuery = "SELECT COUNT(ae) FROM Application ae WHERE ae.project=:project AND $unassignedMBQuery"
		def unassignedAppCount =  Application.executeQuery(unAssignedAppsCountQuery, assetCountQueryArgs)[0] 
		def totalAssignedApp = applicationCount - unassignedAppCount
		
		def latencyQuery = "$unAssignedAppsCountQuery AND ae.latency=:latency"
		def likelyLatency = Application.executeQuery(latencyQuery, assetCountQueryArgs + [latency:'N'])[0]
		def unlikelyLatency = Application.executeQuery(latencyQuery, assetCountQueryArgs + [latency:'Y'])[0]
		def unknownLatency = applicationCount - likelyLatency - unlikelyLatency

		// Add AssetClass param that will be used for the rest of the queries
		assetCountQueryArgs.assetClass = AssetClass.DEVICE

		// Get counts on devices/vms
		def unAssignedCountQuery = "SELECT COUNT(ae) FROM AssetEntity ae WHERE ae.project=:project AND ae.assetClass=:assetClass $assetTypeQuery AND $unassignedMBQuery"
		def unassignedAssetCount = AssetEntity.executeQuery(unAssignedCountQuery, assetCountQueryArgs + [ type:AssetType.getAllServerTypes() ] )[0]	
		def unassignedPhysialAssetCount = AssetEntity.executeQuery(unAssignedCountQuery, assetCountQueryArgs + [ type:AssetType.getPhysicalServerTypes() ] )[0]
		def unassignedVirtualAssetCount = AssetEntity.executeQuery(unAssignedCountQuery, assetCountQueryArgs + [ type:AssetType.getVirtualServerTypes() ] )[0]

		def assignedPhysicalAsset = physicalCount - unassignedPhysialAssetCount
		def assignedVirtualAsset = virtualCount - unassignedVirtualAssetCount

		// TODO - this is unnecessary and could just load the map
		def totalPhysicalAssetCount = physicalCount
		def totalVirtualAssetCount = virtualCount

		// Get the Others Count
		def unAssignedOtherCountQuery = "SELECT COUNT(ae) FROM AssetEntity ae WHERE ae.project=:project AND ae.assetClass=:assetClass AND " +
			"$unassignedMBQuery AND COALESCE(ae.assetType,'') NOT IN (:type)"
		def unassignedOtherCount = AssetEntity.executeQuery(unAssignedOtherCountQuery, assetCountQueryArgs + [ type:AssetType.getAllServerTypes() ] )[0]

		// ------------------------------------
		// Calculate the Plan Status values
		// ------------------------------------
		//Calculating Moved Applications which planStatus is Moved
		//TODO-Lok : Need to take planStatus from ENUM but as it is dynamic now so left for John's Review
		int movedAppCount = moveBundleList ? apps.findAll{it.planStatus == movedPlan}.size() : 0
		movedAppCount = countAppPercentage(applicationCount, movedAppCount)
		
		// Assigned Apps  
		int assignedAppCount = moveBundleList ? apps.findAll{it.planStatus in [movedPlan, assignedPlan, confirmedPlan]}.size() : 0
		assignedAppCount = countAppPercentage(applicationCount, assignedAppCount)
		
		// Confirmed Apps
		int confirmedAppCount = moveBundleList ? apps.findAll{it.planStatus in [movedPlan, confirmedPlan]}.size() : 0
		confirmedAppCount = countAppPercentage(applicationCount, confirmedAppCount)
		
		// Calculating App count of which runbook status is done
		// TODO : @John, can we have runbookStatus as enum 
		def appDoneCount = moveBundleList ? apps.findAll{it.moveBundle?.moveEvent?.runbookStatus=='Done'}.size() : 0
		def percAppDoneCount = countAppPercentage(applicationCount, appDoneCount)
		
		
		int percentagePhysicalAssetCount = moveBundleList ? AssetEntity.executeQuery(deviceCountQuery + " AND ae.planStatus='$movedPlan'", 
			countArgs + [ assetClass:AssetClass.DEVICE, type:AssetType.getPhysicalServerTypes() ] )[0] : 0
		

		// Quick closure for calculating the percentage below
		def percOfCount = { count, total ->
			( total > 0 ? Math.round(count/total*100)  : 0 )
		}
		
		def planStatusMovedQuery = " AND ae.planStatus='$movedPlan'"
		int percentagevirtualAssetCount = moveBundleList ? AssetEntity.executeQuery(deviceCountQuery + planStatusMovedQuery, 
			countArgs + [ assetClass:AssetClass.DEVICE, type:AssetType.getVirtualServerTypes()] )[0] : 0
		
		if (unassignedPhysialAssetCount==assignedPhysicalAsset){
			percentagePhysicalAssetCount = 0;
		} else {
			percentagePhysicalAssetCount = percOfCount(percentagePhysicalAssetCount, assignedPhysicalAsset)
		}
		
		if (unassignedVirtualAssetCount==assignedVirtualAsset){
			percentagevirtualAssetCount = 0;
		} else {
			percentagevirtualAssetCount = percOfCount(percentagevirtualAssetCount, assignedVirtualAsset)
		}
		
		int percentageDBCount = moveBundleList ? Database.executeQuery(dbCountQuery + planStatusMovedQuery, countArgs)[0] : 0
		
		percentageDBCount = percOfCount(percentageDBCount, databaseCount)
		
		int percentagePhyStorageCount = moveBundleList ? AssetEntity.executeQuery(deviceCountQuery + " AND ae.planStatus='$movedPlan'",
			countArgs + [ assetClass:AssetClass.DEVICE, type:AssetType.getStorageTypes() ] )[0] : 0
		
		percentagePhyStorageCount = percOfCount(percentagePhyStorageCount, phyStorageCount)
		
		int percentageFilesCount = moveBundleList ? Files.executeQuery(filesCountQuery + planStatusMovedQuery, countArgs)[0] : 0
		percentageFilesCount = percOfCount(percentageFilesCount, fileCount)
		
		int percentageOtherCount = moveBundleList ? AssetEntity.executeQuery(otherCountQuery + planStatusMovedQuery, 
			countArgs+[ assetClass:AssetClass.DEVICE, type:AssetType.getAllServerTypes()])[0] : 0
		percentageOtherCount = percOfCount(percentageOtherCount, otherAssetCount)

		def likelyLatencyCount=0;
		def unlikelyLatencyCount=0;
		def unknownLatencyCount=0;
		
		def pendingAppDependenciesCount = applicationsOfPlanningBundle ? 
			AssetDependency.countByAssetInListAndStatusInList(applicationsOfPlanningBundle,['Unknown','Questioned']) : 0
		
		def pendingServerDependenciesCount = serversOfPlanningBundle ? 
		AssetDependency.countByAssetInListAndStatusInList(serversOfPlanningBundle,['Unknown','Questioned']) : 0


		def assetDependencyList = jdbcTemplate.queryForList("select dependency_bundle as dependencyBundle from  asset_dependency_bundle \
			where project_id = $projectId  group by dependency_bundle order by dependency_bundle  limit 48")
		
		def formatter = new SimpleDateFormat("MMM dd,yyyy hh:mm a");
		String time
		def date = AssetDependencyBundle.findByProject(project,[sort:"lastUpdated",order:"desc"])?.lastUpdated
		time = date ? formatter.format(date) : ''
		
		def today = new Date()
		def issueQuery = "from AssetComment a  where a.project =:project and a.category in (:category) and a.status != :status and a.commentType =:type AND a.isPublished = true"
		def issueArgs = [project:project, status:AssetCommentStatus.COMPLETED, type:AssetCommentType.TASK.toString()]
		
		def openIssue =  AssetComment.findAll(issueQuery,issueArgs << [category : AssetComment.discoveryCategories]).size()
		def dueOpenIssue = AssetComment.findAll(issueQuery +' and a.dueDate < :dueDate ',issueArgs<< [category : AssetComment.discoveryCategories, dueDate:today]).size()
		def issues = AssetComment.findAll("FROM AssetComment a where a.project = :project and a.commentType = :type and a.status =:status  \
			and a.category in (:category) AND a.isPublished = true",[project:project, type:AssetCommentType.TASK, status: AssetCommentStatus.READY , category: AssetComment.planningCategories])
		def generalOverDue = AssetComment.findAll(issueQuery +' and a.dueDate < :dueDate ',issueArgs<< [category : AssetComment.planningCategories, dueDate:today]).size()

		def dependencyConsoleList = []
		assetDependencyList.each{ dependencyBundle ->
			def assetDependentlist = AssetDependencyBundle.findAllByDependencyBundleAndProject(dependencyBundle.dependencyBundle,project)
			def appCount = assetDependentlist.findAll{ it.asset.assetType == app }.size()
			def serverCount = assetDependentlist.findAll{ it.asset.assetType in [server, blade] }.size()
			def vmCount = assetDependentlist.findAll{ it.asset.assetType == vm }.size()
			
			dependencyConsoleList << ['dependencyBundle':dependencyBundle.dependencyBundle, 'appCount':appCount, 'serverCount':serverCount, 'vmCount':vmCount]
		}
		
		def depBundleIDCountSQL = "select count(distinct dependency_bundle) from asset_dependency_bundle where project_id = $projectId"
        def dependencyBundleCount = jdbcTemplate.queryForInt(depBundleIDCountSQL)	
		
		// Remove the param 'type' that was used for a while above
		countArgs.remove('type')

		def validationQuery = ' AND ae.validation=:validation'
		def validateCountQuery = countQuery + validationQuery + ' AND ae.assetType IN (:type) AND ae.assetClass=:assetClass'
		def appValidateCountQuery = appCountQuery + validationQuery
		def dbValidateCountQuery = dbCountQuery + validationQuery
		def filesValidateCountQuery = filesCountQuery + validationQuery
		
		// TODO - This section could be couple of queries instead of 10
		def dependencyScan = Application.executeQuery(appValidateCountQuery, countArgs+[validation:'DependencyScan'] )[0]
		def validated = Application.executeQuery(appValidateCountQuery, countArgs+[validation:'Validated'])[0]
		def dependencyReview = Application.executeQuery(appValidateCountQuery, countArgs+[validation:'DependencyReview'])[0]
		def bundleReady = Application.executeQuery(appValidateCountQuery, countArgs+[validation:'BundleReady'])[0]
		
		countArgs << [validation:'Discovery']
		def appToValidate = Application.executeQuery(appValidateCountQuery, countArgs)[0]
		def dbToValidate = Database.executeQuery(dbValidateCountQuery, countArgs)[0]
		def fileToValidate = Files.executeQuery(filesValidateCountQuery, countArgs)[0]
		def phyStorageToValidate = AssetEntity.executeQuery(validateCountQuery, countArgs+[ assetClass:AssetClass.DEVICE, type:AssetType.getStorageTypes() ])[0]
		def psToValidate = AssetEntity.executeQuery(validateCountQuery, countArgs+[ assetClass:AssetClass.DEVICE, type:AssetType.getPhysicalServerTypes() ])[0]
		def vsToValidate = AssetEntity.executeQuery(validateCountQuery, countArgs+[ assetClass:AssetClass.DEVICE, type:AssetType.getVirtualServerTypes() ])[0]
		
		def otherValidateQuery = countQuery + validationQuery + ' AND ae.assetType NOT IN (:type) AND ae.assetClass=:assetClass'
		def otherToValidate = AssetEntity.executeQuery(otherValidateQuery, countArgs+[ assetClass:AssetClass.DEVICE, type:AssetType.getAllServerTypes() ])[0]
		
		def percentageAppToValidate = applicationCount ? percOfCount(appToValidate, applicationCount) : 100
		def percentageBundleReady = applicationCount ? percOfCount(bundleReady, applicationCount) : 0
		def percentagePSToValidate= physicalCount ? percOfCount(psToValidate, physicalCount) :100 
		def percentageVMToValidate= virtualCount ? percOfCount(vsToValidate, virtualCount) : 100
		def percentageDBToValidate= databaseCount ? percOfCount(dbToValidate, databaseCount) :100
		def percentageStorToValidate=fileCount ? percOfCount(fileToValidate, fileCount) :100
		def percentageOtherToValidate= otherAssetCount ? percOfCount(otherToValidate, otherAssetCount) :100
		def percentageUnassignedAppCount = applicationCount ? percOfCount(unassignedAppCount, applicationCount) :100
		
		return [			
			appList:appList, applicationCount:applicationCount, unassignedAppCount:unassignedAppCount, appToValidate:appToValidate, 
			physicalCount:physicalCount, 
			unassignedPhysialAssetCount:unassignedPhysialAssetCount, 
			percentagePhysicalAssetCount:percentagePhysicalAssetCount, 
			psToValidate:psToValidate, 
			virtualCount:virtualCount, 
			unassignedVirtualAssetCount:unassignedVirtualAssetCount, 
			percentagevirtualAssetCount:percentagevirtualAssetCount, 
			vsToValidate:vsToValidate, 
			dbList:dbList, dbCount:databaseCount, 
			unassignedDbCount:unassignedDbCount, 
			percentageDBCount:percentageDBCount, 
			dbToValidate:dbToValidate, 
			// Files (aka Storage)
			filesList:filesList, fileCount:fileCount, 
			unassignedFilesCount:unassignedFilesCount, 
			percentageFilesCount:percentageFilesCount, 
			fileToValidate:fileToValidate,
			unAssignedPhyStorgCount:unAssignedPhyStorgCount,
			phyStorageCount:phyStorageCount,
			phyStorageList:phyStorageList,
			phyStorageToValidate:phyStorageToValidate,
			percentagePhyStorageCount:percentagePhyStorageCount,
			// Other
			otherTypeList:otherTypeList, 
			otherAssetCount:otherAssetCount, 
			unassignedOtherCount:unassignedOtherCount, 
			percentageOtherCount:percentageOtherCount, 
			otherToValidate:otherToValidate, 
			
			assetList:assetList, assetCount:assetCount, unassignedAssetCount:unassignedAssetCount, 
			
			likelyLatency:likelyLatency, likelyLatencyCount:likelyLatencyCount, 
			unknownLatency:unknownLatency, unknownLatencyCount:unknownLatencyCount, 
			unlikelyLatency:unlikelyLatency, unlikelyLatencyCount:unlikelyLatencyCount, 
			
			appDependenciesCount:appDependenciesCount, pendingAppDependenciesCount:pendingAppDependenciesCount, 
			serverDependenciesCount:serverDependenciesCount, pendingServerDependenciesCount:pendingServerDependenciesCount, 
			
			project:project, 
			moveEventList:moveEventList, 
			moveBundleList:moveBundleList, 
			dependencyConsoleList:dependencyConsoleList, 
			dependencyBundleCount:dependencyBundleCount, 
			planningDashboard:'planningDashboard', 
			eventStartDate:eventStartDate, 
			date:time, 
			
			issuesCount:issues.size(), 
			openIssue:openIssue, dueOpenIssue:dueOpenIssue, 
			openTasks:openTasks, generalOverDue:generalOverDue, 
			
			dependencyScan:dependencyScan, dependencyReview:dependencyReview, validated:validated, bundleReady:bundleReady,
			movedAppCount:movedAppCount, assignedAppCount:assignedAppCount, confirmedAppCount:confirmedAppCount, 
			percAppDoneCount:percAppDoneCount, percentageAppToValidate:percentageAppToValidate,
			percentageBundleReady:percentageBundleReady,
			
			percentagePSToValidate:percentagePSToValidate, 
			percentageVMToValidate:percentageVMToValidate, 
			percentageDBToValidate:percentageDBToValidate, 
			percentageDBToValidate:percentageDBToValidate,
			percentageStorToValidate:percentageStorToValidate, 
			percentageOtherToValidate:percentageOtherToValidate,
			percentageUnassignedAppCount:percentageUnassignedAppCount
		]
	}
	
	/**
	 * Control function to render the Dependency Analyzer (was Dependency Console)
	 */
	def dependencyConsole = {
	
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		Date start = new Date()
		def assignedGroup = params.assinedGroup ?: userPreferenceService.getPreference("AssignedGroup") 
		if(!assignedGroup)
			assignedGroup = "1"
		userPreferenceService.setPreference( "AssignedGroup", assignedGroup)
		def map = moveBundleService.dependencyConsoleMap(projectId, params.bundle, assignedGroup)
		
		//log.info "dependencyConsole() : moveBundleService.dependencyConsoleMap() took ${TimeUtil.elapsed(start)}"
		return map
	}
	
	/*
	 * Controller to render the Dependency Bundle Details
	 */
	def dependencyBundleDetails = { 
		render(template:"dependencyBundleDetails") 
	}
	
	/**
	 * Controller that generates the Dependency Groups and displays the results
	 */
	def generateDependency = {
		
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		
		String connectionTypes = WebUtil.checkboxParamAsString( request.getParameterValues( "connection" ) )
		String statusTypes = WebUtil.checkboxParamAsString( request.getParameterValues( "status" ) )
		
		def isChecked = params.saveDefault
		// Generate the Dependency Groups
		flash.message = moveBundleService.generateDependencyGroups(projectId, connectionTypes, statusTypes, isChecked)

		// Now get the model and display results
		def isAssigned = userPreferenceService.getPreference( "AssignedGroup" )?: "1"
		render(template:'dependencyBundleDetails', model:moveBundleService.dependencyConsoleMap(projectId, params.bundle, isAssigned) )
	}
	
	/**
	 * Assigns one or more assets to a specified bundle
	 */
	def saveAssetsToBundle={
		def assetArray = params.assetVal
		def moveBundleInstance = MoveBundle.findById(Integer.parseInt(params.moveBundle))
		session.ASSIGN_BUNDLE = params.moveBundle
		def assetList = assetArray.split(",")
		assetList.each{assetId->
			def assetInstance = AssetEntity.get(assetId)
			assetInstance.moveBundle = moveBundleInstance
			assetInstance.planStatus = params.planStatus
			if(!assetInstance.save(flush:true)){
				assetInstance.errors.allErrors.each{
			          log.error it 		
				}
			}
			
		}
		forward(controller:"assetEntity",action:"getLists", params:[entity:params.assetType,dependencyBundle:session.getAttribute('dependencyBundle')])
   }
	
	/**
	 * To generate Moveday Tasks for a specified Bundle
	 * @param - bundleId - Id of the Bundle to generate the tasks for that bundle
	 * @return - error Message - if any else success Message
	 * TODO -- THIS METHOD CAN BE REMOVED AS IT IS NOT USED ANY MORE
	 */
	def createTask = {
		
		def bundleId = params.bundleId
		def bundle = MoveBundle.get(bundleId)
        def errMsg = ""

		if (bundle.getAssetQty() == 0 ) {
			errMsg = "No assets are assigned to current bundle (${bundle.name}). As such no action was taken."
		} else {
			
            def bundleMoveEvent = bundle.moveEvent
            def userLogin = securityService.getUserLogin()
            def project = securityService.getUserCurrentProject()
            def person = userLogin.person

			// Get last task # used
			def lastTask = jdbcTemplate.queryForInt("SELECT MAX(task_number) FROM asset_comment WHERE project_id = ${project.id}")
			
			// Create the Begin Event Task
			def commentToBegin = new AssetComment(
				taskNumber:++lastTask,
				comment:'Begin Event',
			    moveEvent:bundleMoveEvent,  
				category:AssetCommentCategory.SHUTDOWN,
				Status: AssetCommentStatus.PENDING, 
				commentType:AssetCommentType.TASK, 
				project:project, 
				estStart:bundle.startTime, estFinish:bundle.completionTime, duration:0,
				createdBy:person, assignedTo:person, role:'PROJ_MGR',
				autoGenerated:true
			)
			if (!commentToBegin.save(flush:true)) {
				log.error "Failed to create Begin Event task (bundle id $bundleId): " + GormUtil.allErrorsString(commentToBegin)
				errMsg = "Failed to create Begin Event Task. Process Failed"
			}
			if (!errMsg) {
				def commentToComplete = new AssetComment(
					taskNumber:++lastTask,
					comment:'Event Completed',
					project:project,
				    moveEvent:bundleMoveEvent, 
					commentType:AssetCommentType.TASK, 
					category:AssetCommentCategory.STARTUP,
					Status:AssetCommentStatus.PENDING,
					duration:0, 
					assignedTo:person, role:'PROJ_MGR',
					createdBy:person,
					autoGenerated:true
				)
				if (!commentToComplete.save(flush:true)) {
					log.error "Failed to create Event Completed task (bundle id $bundleId): " + GormUtil.allErrorsString(commentToComplete)
					errMsg = "Failed to create Event Completed Task. Process Failed"
				}
                if (!errMsg) {
                    def bundledAssets = AssetEntity.findAll("from AssetEntity a where a.moveBundle = :bundle and a.project =:project\
						and a.assetType not in('application', 'database', 'files', 'VM')", [bundle:bundle,project:project])
                    def bundleworkFlow = bundle.workflowCode
                    def workFlow = Workflow.findByProcess(bundleworkFlow)
					// Find all sets that can be applied to servers
                    def workFlowSteps = WorkflowTransition.findAllByWorkflow(workFlow,[sort:'transId'])	
                    workFlowSteps = workFlowSteps.findAll{ 
	  					! [ WorkflowTransitionId.HOLD, WorkflowTransitionId.READY, WorkflowTransitionId.COMPLETED, WorkflowTransitionId.TERMINATED
						].contains(it.transId)}
	
					// Create the end of transit task that will be used for Off-Truck tasks afterward
                    def results = taskService.createTaskBasedOnWorkflow(
	   					[	taskNumber:++lastTask,
							workflow:workFlowSteps.find{it.transId == WorkflowTransitionId.TRANSPORT}, 
							bundleMoveEvent:bundleMoveEvent,
                        	project:project, person:person, bundle:bundle
						] )
                    def transportTask = results.stepTask

					def previousTask
					
					// Iterate over each Asset and create the various work flow steps
                    bundledAssets.each{asset->
                        workFlowSteps.eachWithIndex{ workflow, index->
                            if (workflow.transId != WorkflowTransitionId.TRANSPORT) {
                                results = taskService.createTaskBasedOnWorkflow( 
									[	taskNumber:++lastTask, 
										workflow:workflow, 
										bundleMoveEvent:bundleMoveEvent, assetEntity:asset,
                                    	project:project, person:person, bundle:bundle
									] )
                                def stepTask = results.stepTask
                                errMsg = results.errMsg
                                if (index==0) {
									// Create dependency on Begin Move task
                                    commentService.saveAndUpdateTaskDependency(stepTask, commentToBegin, null, null)
                                } else if(index==workFlowSteps.size()-1) {
									// Create task dependency on previous task and Completed task
                                    commentService.saveAndUpdateTaskDependency(commentToComplete, stepTask, null, null)
									commentService.saveAndUpdateTaskDependency(stepTask, previousTask, null, null)
                                } else if(previousTask){
									// Create task dependency on previous task
									commentService.saveAndUpdateTaskDependency(stepTask, previousTask, null, null) 
								}
								previousTask = stepTask
                            } else {
								// Create dependency on Transport Ending step
                                commentService.saveAndUpdateTaskDependency(transportTask, previousTask, null, null)
                                previousTask = transportTask
                            }
                        }
                    }
                }
                if (!errMsg) {
                    bundle.tasksCreated = true
                    if ( !bundle.save() ) {
                        log.error "Exception while updating bundle.tasksCreated = true \n"+GormUtil.allErrorsString(bundle)
						errMsg = "An unexpected error occurred while updated bundle"
                    }
                }
			}
		}
	    render errMsg ? errMsg : "Generated Tasks for Bundle - ${bundle.name} successfully."
	}
    
    /**
     * To delete all generated task for bundle
     * @Param : bundleId - Id of bundle for which generated task needs to be deleted
     * @return : void
     */
    def deleteWorkflowTasks = {
        def bundleId = params.bundleId
        def bundle = MoveBundle.get(bundleId)
        def errMsg = ""
        if(bundle.getAssetQty() > 0 ){
            def event = bundle.moveEvent
            def project = securityService.getUserCurrentProject()
            taskService.deleteBundleWorkflowTasks(bundle)
            errMsg = "Tasks Deleted Successfully."
        } else {
            errMsg = "No Asset Assigned to current Bundle - ${bundle.name}. So Process Terminated."
        }
        render errMsg
    }
	
	/**
	 * This method is used to calculate percentage of Filtered Apps on Total Planned apps.
	 * @param totalAppCount : Total count of Application that is in Planned Bundle
	 * @param filteredAppCount : This is filtered app based on PlanStatus
	 * @return : Percentage of Calculated app
	 */
	def countAppPercentage(def totalAppCount, def filteredAppCount){
		return totalAppCount ? Math.round((filteredAppCount/totalAppCount)*100) : 0
	}
	
	/**
	 * This method is used to set compactControl preference
	 * @param prefFor
	 * @param selected
	 * @return selected
	 */
	def setCompactControlPref ={
		def key = params.prefFor
		def selected=params.selected
		if(selected){
			userPreferenceService.setPreference( key, selected )
			session.setAttribute(key,selected)
		}
		render selected
	}
}

