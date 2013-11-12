import java.text.DateFormat
import java.text.SimpleDateFormat
import com.tds.asset.AssetEntity
import com.tds.asset.AssetDependency
import com.tds.asset.AssetComment
import com.tds.asset.Application

class ReportsService {

	def partyRelationshipService
	def jdbcTemplate
    def grailsApplication

	static transactional = true

	def generatePreMoveCheckList(def currProj , def moveEventInstance) {
		
		def projectInstance = Project.findById( currProj )
		def moveBundles = moveEventInstance.moveBundles.sort{it.name}
		def eventErrorList =[] 
	
  //---------------------------------------for Events and Project ---------------------------------------//
		
		def eventsProjectInfo = getEventsProjectInfo(moveEventInstance,projectInstance,currProj,moveBundles,eventErrorList)

  //---------------------------------------for Event and Bundles ---------------------------------------//
        
		def eventBundleInfo = getEventsBundelsInfo(moveBundles,moveEventInstance,eventErrorList)
		

  //---------------------------------------for Assets and Bundles --------------------------------------//
		
		def assetEntityList = AssetEntity.findAllByMoveBundleInListAndProject(moveBundles,projectInstance,[sort:'assetName'])
		
		def assetsInfo = getAssetInfo(assetEntityList,moveBundles,projectInstance,currProj,moveEventInstance,eventErrorList)

  //---------------------------------------For Teams---------------------------------------------------//
		
		def moveBundleTeamInfo = getMoveBundleTeamInfo(moveEventInstance, assetEntityList,eventErrorList)

  //---------------------------------------For Transport------------------------------------------------//
  
        def transportInfo = getTransportInfo(assetEntityList,eventErrorList)
		
		def modelInfo = getModelInfo(moveEventInstance,eventErrorList)
		
		Set allErrors = eventErrorList
		def eventErrorString = ''
		if(allErrors.size()>0){
			eventErrorString +="""<span style="color:red;text-align: center;"><h2>There were ${allErrors.size()} sections with Issues (see details below).</h2></span><br/>"""
		}else{
		    eventErrorString +="""<span style="color:green;text-align: center;"><h3>No preparation issues for this event</h3></span>"""
		}
		return['project':projectInstance,'time':eventsProjectInfo.time,'moveEvent':moveEventInstance,'errorForEventTime':eventsProjectInfo.errorForEventTime,
			   'inProgressError':eventsProjectInfo.inProgressError,'userLoginError':eventsProjectInfo.userLoginError,'clientAccess':eventsProjectInfo.clientAccess,
			   'list':eventsProjectInfo.list,'workFlowCodeSelected':eventBundleInfo.workFlowCodeSelected,'steps':eventBundleInfo.steps,'moveBundleSize':moveBundles.size(),
			   'moveBundles':moveBundles,'summaryOk':assetsInfo.summaryOk,'duplicatesAssetNames':assetsInfo.duplicatesAssetNames,'duplicates':assetsInfo.duplicates,
			   'duplicatesTag':assetsInfo.duplicatesTag,'duplicatesAssetTagNames':assetsInfo.duplicatesAssetTagNames,'missedRacks':assetsInfo.missedRacks,
			   'missingRacks':assetsInfo.missingRacks,'dependenciesOk':assetsInfo.dependenciesOk,'issue':assetsInfo.issue,'issueMap':assetsInfo.issues,
			   'bundleMap':moveBundleTeamInfo.bundleMap,'notAssignedToTeam':moveBundleTeamInfo.notAssignedToTeam,'teamAssignment':moveBundleTeamInfo.teamAssignment,
			   'inValidUsers':moveBundleTeamInfo.inValidUsers,'userLogin':moveBundleTeamInfo.userLogin,'truckError':transportInfo.truckError,'truck':transportInfo.truck,
			   'cartError':transportInfo.cartError,'cart':transportInfo.cart,'shelf':transportInfo.shelf,'shelfError':transportInfo.shelfError,'nullAssetname':assetsInfo.nullAssetname,
			   'blankAssets':assetsInfo.blankAssets ,'questioned':assetsInfo.questioned,'questionedDependency':assetsInfo.questionedDependency,
			   'specialInstruction':assetsInfo.specialInstruction,'importantInstruction':assetsInfo.importantInstruction,'eventErrorString':eventErrorString,
			   'dashBoardOk':eventBundleInfo.dashBoardOk,'allErrors':allErrors,'nullAssetTag':assetsInfo.nullAssetTag,'blankAssetTag':assetsInfo.blankAssetTag,'modelList':modelInfo.modelList,'modelError':modelInfo.modelError,
			   'eventIssues':assetsInfo.eventIssues,'nonAssetIssue':assetsInfo.nonAssetIssue]

	}
	
	/**
	 * Calculates the data to be used by the generateApplicationConflicts view to create a report of applications with issues in a bundle
	 * @param int currProj - The id of the user's curret project
	 * @param int moveBundleId - The id of the moveBundle to generate the report for
	 * @param boolean conflicts - If true, apps with dependencies in other moveBundles will be shown
	 * @param boolean unresolved - If true, apps with dependencies with status 'Questioned' or 'Unknown' will be shown
	 * @return Map - The parameters used by the view to generate the report
	 */
	def genApplicationConflicts(def currProj , def moveBundleId, def conflicts, def unresolved, def planning) {
		def projectInstance = Project.findById( currProj )
		ArrayList appList = new ArrayList()
		def appsInBundle
		log.info "****bundle:${moveBundleId} conflicts:${conflicts} unresolved:${unresolved} planning:${planning} "//type:${MoveBundle.findAllByProjectAndUseOfPlanning(projectInstance, true).class} list:${MoveBundle.findAllByProjectAndUseOfPlanning(projectInstance, true).toList()}"
		
		if(planning) {
			appsInBundle = Application.findAllByMoveBundleInList(MoveBundle.findAllByProjectAndUseOfPlanning(projectInstance, true).toList())
		} else {
			appsInBundle = Application.findAllByMoveBundle(MoveBundle.findById(moveBundleId))
		}
		log.info "${appsInBundle}"
		appsInBundle.each {
			def showApp = false
			def dependsOnList = AssetDependency.findAllByAsset(it)
			def supportsList = AssetDependency.findAllByDependent(it)
			def dependsOnIssueCount = 0
			def supportsIssueCount = 0
			
			dependsOnList.each {
				def conflictIssue = (it.asset.moveBundle?.id != it.dependent.moveBundle?.id) && ( it.status in ['Validated','Questioned','Unknown'] )
				def statusIssue = it.status in ['Questioned','Unknown']
				showApp = showApp || ( conflicts && conflictIssue ) || ( unresolved && statusIssue )
				
				if(conflictIssue || statusIssue)
					dependsOnIssueCount++
			}
			
			supportsList.each {
				def conflictIssue = (it.asset.moveBundle?.id != it.dependent.moveBundle?.id) && ( it.status in ['Validated','Questioned','Unknown'] )
				def statusIssue = it.status in ['Questioned','Unknown']
				showApp = showApp || ( conflicts && conflictIssue ) || ( unresolved && statusIssue )
				
				if(conflictIssue || statusIssue)
					supportsIssueCount++
			}
			
			if(showApp || ( ! conflicts && ! unresolved ) )
				appList.add([ 'app':it, 'dependsOnList':dependsOnList, 'supportsList':supportsList, 'dependsOnIssueCount':dependsOnIssueCount, 'supportsIssueCount':supportsIssueCount ])
		}
		
		return['project':projectInstance, 'appList':appList, 'moveBundle':(moveBundleId.isNumber()) ? (MoveBundle.findById(moveBundleId)) : (moveBundleId), 'columns':9]
	}
	
	/**
	 * @param moveBundles
	 * @param assetEntityList
	 * @return bundleMap, inValidUsers, teamAssignment, notAssignedToTeam
	 */
	
	def getMoveBundleTeamInfo( event, assetEntityList,eventErrorList){
		def moveBundles = event.moveBundles.sort{it.name}
		def project = event.project
		def bundleMap = []
		if ( project.runbookOn == 0 ) {
			moveBundles.each{moveBundle->
				def teamList = []
				def team = ProjectTeam.findAllByMoveBundle(moveBundle,[sort:'name'])
				team.each{teams->
					teamList << ['name':teams.name,
								'role':teams.role,
								'teamList':partyRelationshipService.getBundleTeamMembers(teams),
								'assetSize':AssetEntity.findAllByMoveBundle(moveBundle).size(),
								'moveBundle':moveBundle]
				}
				bundleMap << ["name":moveBundle?.name, "size":team.size(),"teamList":teamList]
			}
		} else {
			def functions = RoleType.findAllByDescriptionIlike("Staff%")
			functions.each { func->
				bundleMap << ["name":func.description, "code":func.id, 
								"assignedStaff":partyRelationshipService.getProjectStaffByFunction(func, project), 
								"tasks":AssetComment.countByMoveEventAndRole(event, func.id)
								]
			}
			bundleMap = bundleMap.findAll{it.assignedStaff.size()>0 || it.tasks>0}
		}
		
		def assetList = AssetEntity.findAllByMoveBundleInListAndAssetTypeNotInList(moveBundles,['Application','Database','Files'])
		
		def notAssignedToTeam = []
		assetList.each{assetEntity->
			if(assetEntity['sourceTeamMt']?.teamCode ==null || assetEntity['targetTeamMt']?.teamCode==null){
				notAssignedToTeam << [assetEntity.assetName]
			}
		}
		
		def teamAssignment = ""
		if(notAssignedToTeam.size()>0){
			teamAssignment+="""<span style="color: red;"><b>MoveTech Assignment: Asset Not Assigned  </b><br></br></span>"""
			eventErrorList << 'Teams'
		}else{
			teamAssignment+="""<span style="color: green;"><b>MoveTech Assignment: OK  </b><br></br></span>"""
		}
		
		Set inValidUsers = []
		bundleMap.teamList.teamList.each{lists->
			lists.id[0].each{personId->
				personId.each{ids->
					if(UserLogin.findByPerson(Person.get(ids))?.lastLogin==null||UserLogin.findByPerson(Person.get(ids))?.active=='N'){
						inValidUsers << [Person.get(ids)]
					}
				}
			}

		}
		
		def userLogin =""
		if(inValidUsers.size()>0){
			userLogin+="""<span style="color: red;"><b>Team Details Check: Team Member name Not Valid</b><br></br></span>"""
			eventErrorList << 'Teams'
		}else{
			userLogin+="""<span style="color: green;"><b>Team Details Check: OK  </b><br></br></span>"""
		}
		return [bundleMap:bundleMap,inValidUsers:inValidUsers,
			teamAssignment:teamAssignment, notAssignedToTeam:notAssignedToTeam,userLogin:userLogin,eventErrorList:eventErrorList]
	}
	
	/**
	* @param assetEntityList,moveBundles,projectInstance
	* @param assetEntityList
	* @return summaryOk,issue,dependenciesOk,dependencies,missingRacks,missedRacks,duplicatesTag,duplicatesAssetTagNames,
	*          duplicates,duplicatesAssetNames
	*/
	
	def getAssetInfo(assetEntityList,moveBundles,projectInstance,currProj,moveEventInstance,eventErrorList){

		def summaryOk = [:]
		Set assetType
		def typeCount
		def types = []
		
		moveBundles.each{moveBundle->
			def assetEntity = AssetEntity.findAllByMoveBundleAndProject(moveBundle,projectInstance)
			typeCount = AssetEntity.executeQuery("select count(*) , assetType  from AssetEntity where moveBundle = ${moveBundle.id} group by assetType ")
			String counts = typeCount.toString()
			counts = counts.replace('[[', '').replace(']]', '').replace(',', '').replace('] [',' , ').replace('[]', '0')
			summaryOk << [(moveBundle) :  counts ]
		}
		
		def duplicatesAssetNames = jdbcTemplate.queryForList("""SELECT asset_name as assetName , count(*) as counts , asset_type as type 
                                    from asset_entity where project_id = $currProj and asset_name is not null and move_bundle_id in 
                                    (select move_bundle_id from move_bundle where move_event_id = ${moveEventInstance.id}) 
                                    GROUP BY asset_name ,asset_type HAVING COUNT(*) > 1""")
		
		String duplicates = ""
		def nullAssetname = jdbcTemplate.queryForList("""SELECT asset_name as assetName ,asset_tag as tag,  asset_type as type from asset_entity 
                                                        where project_id = $currProj and asset_name is null and move_bundle_id in 
                                                        (select move_bundle_id from move_bundle where move_event_id = ${moveEventInstance.id}) """
                                                        )
		if(duplicatesAssetNames.size()>0){
			duplicates += """<span style="color: red;"><b>Naming Check: <br></br></span>"""
			eventErrorList << 'Assets'
		}else{
			duplicates += """<span style="color: green;"><b>Naming Check: OK <br></br></span>"""
		}
		def blankAssets = ''
		if(nullAssetname.size()>0){
			blankAssets += """<span style="color: red;"><b>Blank Naming Check: <br></br></span>"""
			eventErrorList << 'Assets'
		}else{
		    blankAssets += """<span style="color: green;"><b>Blank Naming Check: OK: No error: "Blank names: 0"<br></br></span>"""
		}
		
		def duplicatesAssetTagNames = jdbcTemplate.queryForList("""SELECT asset_tag as tag , count(*) as counts from asset_entity 
                                       where project_id = $currProj and asset_tag is not null and asset_type in ('Server','VM','Blade') 
                                       and move_bundle_id in (select move_bundle_id from move_bundle where move_event_id = ${moveEventInstance.id}) 
                                        GROUP BY asset_tag HAVING COUNT(*) > 1""")
		
		String duplicatesTag = ""
		
		if(duplicatesAssetTagNames.size()>0){
			duplicatesTag += """<span style="color: red;"><b>Asset Tag: <br></br></span>"""
			eventErrorList << 'Assets'
		}else{
			duplicatesTag += """<span style="color: green;"><b>Asset Tag: OK <br></br></span>"""
		}
		def nullAssetTag =  jdbcTemplate.queryForList("""SELECT asset_name as assetName ,asset_tag as tag,  asset_type as type 
                            from asset_entity where project_id = $currProj and asset_tag is null and asset_type in ('Server','VM','Blade') 
                            and move_bundle_id in (select move_bundle_id from move_bundle where move_event_id = ${moveEventInstance.id}) """)
		def blankAssetTag = ''
		if(nullAssetTag.size()>0){
			blankAssetTag += """<span style="color: red;"><b>Blank Tag Check: ${nullAssetTag.size()} assets with no asset tags <br></br></span>"""
			eventErrorList << 'Assets'
		}else{
			blankAssetTag += """<span style="color: green;"><b>Blank Tag Check: OK: No error: "Blank tag: 0"<br></br></span>"""
		}
		
		def missingRacks = AssetEntity.findAll("from AssetEntity asset where  asset.project.id=${currProj} and asset.assetType not in (:type) \
                            and asset.moveBundle.moveEvent = :event and \
                            (sourceRack='' or targetRack='' or sourceRackPosition = '' or targetRackPosition = '') \
                            group by asset.assetName",[type:['Application','Files','Database','VM'], event:moveEventInstance]).assetName
		
		String missedRacks = ""
		if(missingRacks.size()>0){
			missedRacks += """<span style="color: red;"><b>Asset Details: ${missingRacks.size} Servers With Missing Rack info: </b><br></br></span>"""
			eventErrorList << 'Assets'
		}else{
			missedRacks += """<span style="color: Green;"><b>Asset Details: OK </b><br></br></span>"""
			missingRacks = ''
		}
		
		def dependencies = [] 
		if(assetEntityList.size()>0){
			dependencies = AssetDependency.findAllByDependentInList(assetEntityList,assetEntityList)?.asset?.assetName
		}else{
		    dependencies=[]
		}
		dependencies.sort()
		String dependenciesOk = ""
		if(dependencies.size()>0){
			dependenciesOk +="""<span style="color: red;"><b>Dependency found: ${dependencies.size} Dependency Found:<br></br></b></span> 
                                    <div style="margin-left:50px;"> ${dependencies.toString().replace('[','').replace(']','')}</div>
                                """
			eventErrorList << 'Assets'
		}else{
			dependenciesOk +="""<span style="color: green;"><b>Dependency: OK-No Dependencies: </b><br></br></span>"""
		}
		
		def assetId = assetEntityList.id
		
		def categories = ['shutdown','moveday','startup','physical','physical-source','physical-target']
		def issues = assetId ? AssetComment.findAll("from AssetComment comment where comment.assetEntity.id in (:assetIds) and commentType ='issue' \
            and  isResolved =0 and comment.category not in (:categories) order by comment.assetEntity.assetName ",
            [assetIds:assetId, categories:categories]) : []
		def issue = ""
		if(issues.size()>0){
			issue +="""<span style="color: red;"><b>Asset Issues: Unresolved Issues  </b><br></br></span>"""
			eventErrorList << 'Assets'
		}else{
			issue +="""<span style="color: green;"><b>Asset Issues: OK  </b><br></br></span>"""
		}
		
		def specialInstruction = assetId ? AssetComment.findAll("from AssetComment comment where comment.assetEntity.id in (:assetIds)  and  mustVerify = 1 order by comment.assetEntity.assetName ",
                                                        [assetIds : assetId]) : []
		def importantInstruction = ""
		if(specialInstruction.size()>0){
			importantInstruction +="""<span style="color: red;"><b>Special Instruction: </b><br></br></span>"""
			eventErrorList << 'Assets'
		}else{
			importantInstruction +="""<span style="color: green;"><b>Special Instruction: OK  </b><br></br></span>"""
		}
		
		
		Set questionedDependencies = assetId ? AssetDependency.findAll("from AssetDependency dependency where (dependency.asset.id in (:assetIds) or \
            dependency.dependent.id in (:assetIds)) and dependency.status in (:statuses) and dependency.asset.moveBundle.moveEvent = :event \
            order by dependency.asset.assetName ",[assetIds : assetId, event:moveEventInstance, statuses:['Questioned','Unknown']]).asset.assetName : []

		def questionedDependency = questionedDependencies.toList()
		questionedDependency.sort()
		def questioned = ''
		if(questionedDependency.size()>0){
			questioned +="""<span style="color: red;"><b>Dependencies Questioned for ${questionedDependency.size() } assets:</b><br></br></span>"""
		}else{
		    questioned +="""<span style="color: green;"><b>Dependencies Questioned: OK  </b><br></br></span>"""
		}
		def nonAssetIssue = AssetComment.findAll("from AssetComment a where a.moveEvent = :event and a.category not in(:categories) ",
                                                [event: moveEventInstance, categories:categories])
		def eventIssues = ''
		if(nonAssetIssue.size()>0){
			eventIssues +="""<span style="color: red;"><b>Event Issues: </b><br></br></span>"""
		}else{
			eventIssues +="""<span style="color: green;"><b>Event Issues: OK  </b><br></br></span>"""
		}
		
		
		return[summaryOk:summaryOk,issue:issue,issues:issues,dependenciesOk:dependenciesOk,dependencies:dependencies,missingRacks:missingRacks,
			   missedRacks:missedRacks,duplicatesTag:duplicatesTag,duplicatesAssetTagNames:duplicatesAssetTagNames,duplicates:duplicates,
			   duplicatesAssetNames:duplicatesAssetNames,nullAssetname:nullAssetname,blankAssets:blankAssets,questioned:questioned,
			   questionedDependency:questionedDependency,specialInstruction:specialInstruction,importantInstruction:importantInstruction,
			   eventErrorList:eventErrorList,nullAssetTag:nullAssetTag,blankAssetTag:blankAssetTag,eventIssues:eventIssues,nonAssetIssue:nonAssetIssue]

	}
	
	/**
	* @param moveBundles,moveEventInstance
	* @param Events 
	* @return workFlowCodeSelected,steps
	*/
	
	def getEventsBundelsInfo(moveBundles,moveEventInstance,eventErrorList){
		Set workFlowCode = moveBundles.workflowCode
		def workFlow = moveBundles.workflowCode
		def workFlowCodeSelected = [:]
		def steps = [:]
		def moveBundleStep
		
		if(workFlowCode.size()==1){
			workFlowCodeSelected << [(moveEventInstance.name+'  (Event)  '+'  All Bundles have same WorkFlow  '):workFlow[0]]
		}else{
			moveBundles.each{
				workFlowCodeSelected << [(it.name+'(Bundle)'+'  '+'  Uses WorkFlow ') : it.workflowCode]
			}
		}
		
		
		
		String labels
		def dashBoardOk = []
		moveBundles.each{moveBundle->
			def label = []
			moveBundleStep = MoveBundleStep.findAllByMoveBundle(moveBundle,[sort:'transitionId'])
			if(moveBundleStep.size()==0){
				steps << [(moveBundle.name):"No steps created"]
				eventErrorList << 'EventsBundle'
				dashBoardOk <<['No steps created']
			}else{
				moveBundleStep.each{step->
					label << [
						"${step.label}(${step.planDuration/60+'m'})"
					]
					labels = label.toString()
					labels = labels.replace('[[','').replace('], [',' , ').replace(']]','')
					steps << [(moveBundle.name):labels]
				}
				
				dashBoardOk += """<span style="color:green" ><b>Dashboard OK: </b></span>"""
			}
		}
	  return[workFlowCodeSelected:workFlowCodeSelected,steps:steps,eventErrorList:eventErrorList,dashBoardOk:dashBoardOk]
	}
	/**
	* @param moveEventInstance,projectInstance,currProj
	* @param Events
	* @return time,moveEventInstance,errorForEventTime,inProgressError,userLoginError,clientAccess,list
	*/
	def getEventsProjectInfo(moveEventInstance,projectInstance,currProj,moveBundles,eventErrorList){
		
		def date = new Date()
		def formatter = new SimpleDateFormat("MMM dd,yyyy hh:mm a");
		def format = new SimpleDateFormat("yyyy-mm-dd hh:mm:sss");
		String time = formatter.format(date);
		def errorForEventTime = ""
		def inProgressError = ""
		def clientAccess = ""
		def userLoginError = ""

		moveBundles.each{
			if(it.startTime > projectInstance.startDate && it.completionTime > projectInstance.completionDate){
				eventErrorList << 'Project'
				errorForEventTime += """<span style="color:red" ><b>Move bundle ${it.name} is completing after project completion</b>  </span><br></br>"""
			}else{
				def projectStartTime = 'Not Available'
				def projectEndTime = 'Not Available'
				
					if(it.startTime){
					  projectStartTime  = formatter.format(format.parse(it.startTime.toString()))
					  projectEndTime  = formatter.format(format.parse(it.completionTime.toString()))
					}
					
					errorForEventTime += """<span style="color:green"><b>Event Time Period ${it.name}: OK </b>${projectStartTime} - ${projectEndTime}</span><br></br>"""
				}
		}
		def lastMoveBundleDate = moveEventInstance.moveBundles.completionTime
		lastMoveBundleDate.sort()
		def lastMoveBundleDateSize = lastMoveBundleDate.size()
		def moveEventCompletiondate
		if(lastMoveBundleDateSize>0){
		   moveEventCompletiondate = lastMoveBundleDate[lastMoveBundleDateSize-1]
		}
		def inPastError = ''
		if(moveEventInstance.inProgress=='true'){
			eventErrorList << 'Project'
			inProgressError += """<span style="color:red" ><b>${moveEventInstance.name}: MoveEvent In Progress </b></span>"""
		}else if(moveEventCompletiondate < projectInstance.startDate) {
		    eventErrorList << 'Project'
		    inProgressError += """<span style="color:red" ><b>${moveEventInstance.name}: MoveEvent In Past </b></span>"""
		}else{
		    inProgressError += """<span style="color:green" ><b>${moveEventInstance.name}: OK </b></span>"""
		}
		
		
		def list = partyRelationshipService.getProjectStaff(currProj)
        list.sort{ a, b -> a.company[0]?.toString() <=> b.company[0]?.toString() ?: a.role?.toString() <=> b.role?.toString() }
		
		def projectStaff = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = $currProj and p.roleTypeCodeFrom = 'PROJECT' ")
		
		projectStaff.each{staff ->
			def user = UserLogin.findByPerson(Person.get(staff.partyIdTo.id))
			
			if(!user){
				eventErrorList << 'Project'
				userLoginError +="""<span style="color:red;margin-left:50px;"><b>${Person.get(staff.partyIdTo.id)} login disabled</b></span><br></br>"""
			}
			if(user?.active=='N'){
				eventErrorList << 'Project'
				userLoginError +="""<span style="color:red;margin-left:50px;"><b>${user} login inactive</b></span><br></br>"""
			}
			
		}
		def query = "from Person s where s.id in (select p.partyIdTo from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom = ${projectInstance.client.id} and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' ) order by s.lastName "
		def personInstanceList = Person.findAll( query )
		if(personInstanceList.size()==0){
			clientAccess += """<span style="color:red"><b>No Client Access</b></span>"""
			eventErrorList << 'Project'
		}else{
			clientAccess += """<span style="color:Green"><b> Client Access:&nbsp;${personInstanceList}</b></span>"""
		}
		return[time:time,moveEvent:moveEventInstance,errorForEventTime:errorForEventTime,
			   inProgressError:inProgressError,userLoginError:userLoginError,clientAccess:clientAccess,list:list,
			   eventErrorList:eventErrorList]
	}
	
	/**
	* @param assetEntityList
	* @param Transport
	* @return truckError,truck
	*/
	
	def getTransportInfo(assetEntityList,eventErrorList){
		
		Set truck = assetEntityList.truck
		truck.remove('')
		truck.remove(null)
		String truckError = ''
		
		if(truck.size()==0){
			eventErrorList << 'Transport'
			truckError += """<span style="color: red;"><b>Trucks: No trucks defined</b><br></br></span>"""
		}else{
			truckError+="""<span style="color: green;"><b>Trucks: OK  </b><br></br></span>"""
		}
		
		Set cart = assetEntityList.cart
		cart.remove('')
		cart.remove(null)
		String cartError = ''
		
		if(cart.size()==0){
			eventErrorList << 'Transport'
			cartError += """<span style="color: red;"><b>Carts: No carts defined</b><br></br></span>"""
		}else{
			cartError+="""<span style="color: green;"><b>Carts: OK (${cart.size()}) </b><br></br></span>"""
		}
		
		Set shelf = assetEntityList.shelf
		shelf.remove('')
		shelf.remove(null)
		
		def shelfError = ''
		
		if(shelf.size()==0){
			eventErrorList << 'Transport'
			shelfError += """<span style="color: red;"><b>Shelves: No Shelves defined</b><br></br></span>"""
		}else{
			shelfError+= """<span style="color: green;"><b>Shelves: OK (${shelf.size()}) </b><br></br></span>"""
		}
		
		return[truckError:truckError,truck:truck,cartError:cartError,cart:cart,shelf:shelf,shelfError:shelfError,
			   eventErrorList:eventErrorList]
	}
	
	def getModelInfo(moveEventInstance,eventErrorList){
		Set modelLists = AssetEntity.findAll('from AssetEntity a where a.model.modelStatus = ? and a.model.usize = ? and a.moveBundle.moveEvent =? order by a.model.modelName asc',['new',1,moveEventInstance]).modelName
		def modelList = []
		modelList = modelLists.toList()
		//def modelList = Model.findAllByModelStatusAndUsize('new',1,[sort:'modelName']).modelName
		def modelError = ''
		
		if(modelList.size()>0){
			eventErrorList << 'Model'
			modelError+="""<span style="color: red;margin-left:50px;"><b>${modelList.size()}: un-validated models used : </b><br></br></span>"""
		}else{
	    	modelError+="""<span style="color: green;margin-left:50px;"><b>Model: OK  </b><br></br></span>"""
		}
		return[modelList:modelList,modelError:modelError]
		
	}

	/**
	 * Attempts to generate a dot graph file based on application graph property configurations from the 
	 * dotText passed to the method.
	 * @param String filenamePrefix - a prefix used when creating the filename that will include the datetime plus a random #
	 * @param String dotText - the dot sytax used to define the graph
	 * @return String the URI to access the resulting file
	 * @throws RuntimeException when the generation fails, the exception message will contain the output from the dot command
	 */
	def generateDotGraph( filenamePrefix, dotText ) {

		def tmpDir = grailsApplication.config.graph.tmpDir
		def targetDir = grailsApplication.config.graph.targetDir
		def targetURI = grailsApplication.config.graph.targetURI
		def dotExec = grailsApplication.config.graph?.graphviz?.dotCmd
		def graphType = grailsApplication.config.graph?.graphviz?.graphType
		def deleteDotFile = grailsApplication.config.graph?.containsKey('deleteDotFile') ? grailsApplication.config.graph.deleteDotFile : true
		def random = org.apache.commons.lang.math.RandomUtils.nextInt()
		def filename = "$filenamePrefix-${new Date().format('yyyyMMdd-HHmmss')}-$random"
		def imgFilename = "${filename}.${graphType}"				

		// log.info "dot: $dotText"

		// Create the dot file
		def dotFN = "${tmpDir}${filename}.dot"
		def dotFile = new File(dotFN);
		dotFile << dotText

		def sout = new StringBuffer()
		def serr = new StringBuffer()		
		def cmd = "${dotExec} -T${graphType} -v -o ${targetDir}${imgFilename} ${dotFile}"
		log.info "generateDotGraph: about to execute command: $cmd"
		def proc = cmd.execute()
		proc.consumeProcessOutput(sout, serr)
	 	proc.waitForOrKill(150000)
	 	log.info "generateDotGraph: process stdout=$sout"
	 	log.info "generateDotGraph: process stderr=$serr"
	
		if (proc.exitValue() == 0) {
			// Delete the dot file because we don't need it and configured to delete it automatically
			if (deleteDotFile) dotFile.delete()
			return "${targetURI}${imgFilename}"
		} else {
			def errFile = new File("${targetDir}${filename}.err")
			errFile << "exit code:\n\n${ proc.exitValue()}\n\nstderr:\n${serr}\n\nstdout:\n${sout}"

			throw new RuntimeException("Exit code: ${ proc.exitValue()}\n stderr: ${serr}\n stdout: ${sout}")
		}
	}

}
