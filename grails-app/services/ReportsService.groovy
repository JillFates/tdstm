import java.text.DateFormat
import java.text.SimpleDateFormat
import com.tds.asset.AssetEntity
import com.tds.asset.AssetDependency
import com.tds.asset.AssetComment

class ReportsService {

	def partyRelationshipService
	def jdbcTemplate
	static transactional = true

	def generatePreMoveCheckList(def currProj , def moveEventInstance) {
		
		def projectInstance = Project.findById( currProj )
		def moveBundles = moveEventInstance.moveBundles
		def eventErrorList =[] 
	
  //---------------------------------------for Events and Project ---------------------------------------//
		
		def eventsProjectInfo = getEventsProjectInfo(moveEventInstance,projectInstance,currProj,moveBundles,eventErrorList)

  //---------------------------------------for Event and Bundles ---------------------------------------//
        
		def eventBundleInfo = getEventsBundelsInfo(moveBundles,moveEventInstance,eventErrorList)
		

  //---------------------------------------for Assets and Bundles --------------------------------------//
		
		def assetEntityList = AssetEntity.findAllByMoveBundleInListAndProject(moveBundles,projectInstance,[sort:'assetName'])
		
		def assetsInfo = getAssetInfo(assetEntityList,moveBundles,projectInstance,currProj,moveEventInstance,eventErrorList)

  //---------------------------------------For Teams---------------------------------------------------//
		
		def moveBundleTeamInfo = getMoveBundleTeamInfo(moveBundles, assetEntityList,eventErrorList)

  //---------------------------------------For Transport------------------------------------------------//
  
        def transportInfo = getTransportInfo(assetEntityList,eventErrorList)
		
		Set allErrors = eventErrorList
		def eventErrorString = ''
		if(allErrors.size()>0){
			eventErrorString +="""<span style="color:red;text-align: center;"><h3>There were ${allErrors.size()} sections with Issues or Errors.See Details Below.</h3></span><br/>"""
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
			   'specialInstruction':assetsInfo.specialInstruction,'importantInstruction':assetsInfo.importantInstruction,'eventErrorString':eventErrorString]

	} 
	
	/**
	 * @param moveBundles
	 * @param assetEntityList
	 * @return bundleMap, inValidUsers, teamAssignment, notAssignedToTeam
	 */
	
	def getMoveBundleTeamInfo( moveBundles, assetEntityList,eventErrorList ){
		def bundleMap = []
		moveBundles.each{moveBundle->
			def teamList = []
			def teamsMember = [:]
			def team = ProjectTeam.findAllByMoveBundle(moveBundle,[sort:'name'])
			team.each{teams->
				teamsMember = ['name':teams.name,
							'role':teams.role,
							'teamList':partyRelationshipService.getBundleTeamMembers(teams),
							'assetSize':AssetEntity.findAllByMoveBundle(moveBundle).size(),
							'moveBundle':moveBundle]
				teamList << [teamsMember]
			}
			bundleMap << ["name":moveBundle?.name, "size":team.size(),"teamList":teamList]

		}
		def notAssignedToTeam = []
		assetEntityList.each{assetEntity->
			if(assetEntity['sourceTeamMt']?.teamCode ==null || assetEntity['targetTeamMt']?.teamCode==null){
				notAssignedToTeam << [assetEntity.assetName]
			}
		}
		def teamAssignment = ""
		if(notAssignedToTeam.size()>0){
			teamAssignment+="""<span style="color: red;"><b>MoveTech Assignment : Asset Not Assigned  </b><br></br></span>"""
			eventErrorList << ['Teams']
		}else{
			teamAssignment+="""<span style="color: green;"><b>MoveTech Assignment : OK  </b><br></br></span>"""
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
				userLogin+="""<span style="color: red;"><b>Team Details Check : Team Member name Not Valid</b><br></br></span>"""
				eventErrorList << ['Teams']
			}else{
				userLogin+="""<span style="color: green;"><b>Team Details Check : OK  </b><br></br></span>"""
			}
		return [bundleMap:bundleMap,inValidUsers:inValidUsers,
			teamAssignment:teamAssignment, notAssignedToTeam:notAssignedToTeam,userLogin:userLogin,eventErrorList:eventErrorList]
	}
	
	/**
	* @param assetEntityList,moveBundles,projectInstance
	* @param assetEntityList
	* @return summaryOk,issue,dependenciesOk,dependencies,missingRacks,missedRacks,duplicatesTag,duplicatesAssetTagNames,duplicates,duplicatesAssetNames
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
			counts = counts.replace('[[', '').replace(']]', '').replace(',', '').replace('] [',',').replace('[]', '0')
			summaryOk << [(moveBundle) :  counts ]
		}
		
		def duplicatesAssetNames = jdbcTemplate.queryForList("SELECT asset_name as assetName , count(*) as counts , asset_type as type from asset_entity where project_id = $currProj and asset_name is not null and move_bundle_id in (select move_bundle_id from move_bundle where move_event_id = ${moveEventInstance.id}) GROUP BY asset_name ,asset_type HAVING COUNT(*) > 1")
		
		String duplicates = ""
		Set nullAssetname = jdbcTemplate.queryForList("SELECT asset_name as assetName , count(*) as counts , asset_type as type from asset_entity where project_id = $currProj and asset_name is null and move_bundle_id in (select move_bundle_id from move_bundle where move_event_id = ${moveEventInstance.id}) GROUP BY asset_name ,asset_type HAVING COUNT(*) > 1")
		if(duplicatesAssetNames.size()>0){
			duplicates += """<span style="color: red;"><b>Naming Check: <br></br></span>"""
			eventErrorList << ['Assets']
		}else{
			duplicates += """<span style="color: green;"><b>Naming Check OK <br></br></span>"""
		}
		def blankAssets = ''
		if(nullAssetname.size()>0){
			blankAssets += """<span style="color: red;"><b>Blank Naming Check: <br></br></span>"""
			eventErrorList << ['Assets']
		}else{
		    blankAssets += """<span style="color: green;"><b>Blank Naming Check: OK : No error: "Blank names: 0"<br></br></span>"""
		}
		
		def duplicatesAssetTagNames = jdbcTemplate.queryForList("SELECT asset_tag as tag , count(*) as counts from asset_entity where project_id = $currProj and move_bundle_id in (select move_bundle_id from move_bundle where move_event_id = ${moveEventInstance.id}) GROUP BY asset_tag HAVING COUNT(*) > 1")
		
		String duplicatesTag = ""
		if(duplicatesAssetTagNames.size()>0){
			duplicatesTag += """<span style="color: red;"><b>Asset Tag: <br></br></span>"""
			eventErrorList << ['Assets']
		}else{
			duplicatesTag += """<span style="color: green;"><b>Asset Tag  OK <br></br></span>"""
		}
		
		def missingRacks = AssetEntity.findAll("from AssetEntity asset where  asset.project.id=${currProj} and asset.assetType not in('Application','Files','Database') and asset.moveBundle.moveEvent.id=${moveEventInstance.id} and (sourceRack='' or targetRack='' or sourceRackPosition = '' or targetRackPosition = '') group by asset.assetName").assetName
		
		String missedRacks = ""
		if(missingRacks.size()>0){
			missedRacks += """<span style="color: red;"><b>Asset Details:${missingRacks.size} Servers With Missing Rack info : </b><br></br></span>"""
			eventErrorList << ['Assets']
		}else{
			missedRacks += """<span style="color: Green;"><b>Asset Details: OK </b><br></br></span>"""
			missingRacks = ''
		}
		
		def dependencies = [] 
		if(assetEntityList.size()>0){
			dependencies = AssetDependency.findAllByAssetInListOrDependentInList(assetEntityList,assetEntityList)?.asset?.assetName
		}else{
		    dependencies=[]
		}
		dependencies.sort()
		String dependenciesOk = ""
		if(dependencies.size()>0){
			dependenciesOk +="""<span style="color: red;"><b>Dependency found:${dependencies.size} :<br></br>${dependencies} </b></span>"""
			eventErrorList << ['Assets']
		}else{
			dependenciesOk +="""<span style="color: green;"><b>Dependency -No Dependencies : </b><br></br></span>"""
		}
		
		String assetId = assetEntityList.id
		assetId = assetId.replace("[","('").replace(",","','").replace("]","')")
		
		def issues = AssetComment.findAll("from AssetComment comment where comment.assetEntity.id in ${assetId} and commentType ='issue' and  isResolved =0 order by comment.assetEntity.assetName ")
		def issue = ""
		if(issues.size()>0){
			issue +="""<span style="color: red;"><b>Issues: Unresolved Issues  </b><br></br></span>"""
			eventErrorList << ['Assets']
		}else{
			issue +="""<span style="color: green;"><b>Issues OK  </b><br></br></span>"""
		}
		
		def specialInstruction = AssetComment.findAll("from AssetComment comment where comment.assetEntity.id in ${assetId}  and  mustVerify = 1 order by comment.assetEntity.assetName ")
		def importantInstruction = ""
		if(specialInstruction.size()>0){
			importantInstruction +="""<span style="color: red;"><b>Special Instruction: </b><br></br></span>"""
			eventErrorList << ['Assets']
		}else{
			importantInstruction +="""<span style="color: green;"><b>Special Instruction: OK  </b><br></br></span>"""
		}
		
		
		Set questionedDependencies = AssetDependency.findAll("from AssetDependency dependency where (dependency.asset in $assetId or dependency.dependent in $assetId) and dependency.status='Questioned' and dependency.asset.moveBundle.moveEvent.id = ${moveEventInstance.id} order by dependency.asset.assetName ").asset.assetName
		def questionedDependency = questionedDependencies.toList()
		questionedDependency.sort()
		def questioned = ''
		if(questionedDependency.size()>0){
			questioned +="""<span style="color: red;"><b>Dependencies questioned for ${questionedDependency.size() } assets</b><br></br></span>"""
		}else{
		    questioned +="""<span style="color: green;"><b>Dependencies questioned : OK  </b><br></br></span>"""
		}
		
		
		return[summaryOk:summaryOk,issue:issue,issues:issues,dependenciesOk:dependenciesOk,dependencies:dependencies,missingRacks:missingRacks,
			   missedRacks:missedRacks,duplicatesTag:duplicatesTag,duplicatesAssetTagNames:duplicatesAssetTagNames,duplicates:duplicates,
			   duplicatesAssetNames:duplicatesAssetNames,nullAssetname:nullAssetname,blankAssets:blankAssets,questioned:questioned,
			   questionedDependency:questionedDependency,specialInstruction:specialInstruction,importantInstruction:importantInstruction,
			   eventErrorList:eventErrorList]

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
			workFlowCodeSelected << [(moveEventInstance.name+'(Event)'+'All Bundles have same WorkFlow'):workFlow[0]]
		}else{
			moveBundles.each{
				workFlowCodeSelected << [(it.name+'(Bundle)'):it.workflowCode]
			}
		}
		
		def label = []
		
		String labels
		
		moveBundles.each{moveBundle->
			moveBundleStep = MoveBundleStep.findAllByMoveBundle(moveBundle)
			if(moveBundleStep.size()==0){
				steps << [(moveBundle.name):"No steps created"]
				eventErrorList << ['EventsBundle']
			}else{
				moveBundleStep.each{step->
					label << [
						"${step.label}(${step.planDuration/60+'m'})"
					]
					labels = label.toString()
					labels = labels.replace('[[','').replace('], [',' , ').replace(']]','')
					steps << [(moveBundle.name):labels]
				}
			}
		}
	  return[workFlowCodeSelected:workFlowCodeSelected,steps:steps,eventErrorList:eventErrorList]
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
				eventErrorList << ['Project']
				errorForEventTime += """<span style="color:red" ><b>Move bundle ${it.name} is completing after project completion</b>  </span><br></br>"""
			}else{
				def projectStartTime = 'Not Available'
				def projectEndTime = 'Not Available'
				
					if(it.startTime){
					  projectStartTime  = formatter.format(format.parse(it.startTime.toString()))
					  projectEndTime  = formatter.format(format.parse(it.completionTime.toString()))
					}
					
					errorForEventTime += """<span style="color:green"><b>Event Time Period ${it.name} :OK </b>${projectStartTime} - ${projectEndTime}</span><br></br>"""
				}
		}
		def lastMoveBundleDate = moveEventInstance.moveBundles.completionTime
		lastMoveBundleDate.sort()
		def lastMoveBundleDateSize = lastMoveBundleDate.size()
		def moveEventCompletiondate = lastMoveBundleDate[lastMoveBundleDateSize-1]
		def inPastError = ''
		if(moveEventInstance.inProgress=='true'){
			eventErrorList << ['Project']
			inProgressError += """<span style="color:red" ><b>${moveEventInstance.name} : MoveEvent In Progress </b></span>"""
		}else if(moveEventCompletiondate < projectInstance.startDate) {
		    eventErrorList << ['Project']
		    inProgressError += """<span style="color:red" ><b>${moveEventInstance.name} : MoveEvent In Past </b></span>"""
		}else{
		    inProgressError += """<span style="color:green" ><b>${moveEventInstance.name} : OK </b></span>"""
		}
		
		
		def list = []
		
		def projectStaff = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = $currProj and p.roleTypeCodeFrom = 'PROJECT' ")
		
		projectStaff.each{staff ->
			def map = new HashMap()
			def company = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdTo = $staff.partyIdTo.id and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' ")
			map.put("company", company.partyIdFrom[0])
			map.put("name", staff.partyIdTo.firstName+" "+ staff.partyIdTo.lastName)
			map.put("role", staff.roleTypeCodeTo)
			list<<map

			def user = UserLogin.findByPerson(Person.get(staff.partyIdTo.id))
			
			if(!user){
				eventErrorList << ['Project']
				userLoginError +="""<span style="color:red"><b>${Person.get(staff.partyIdTo.id)} login disabled</b></span><br></br>"""
			}
			if(user?.active=='N'){
				eventErrorList << ['Project']
				userLoginError +="""<span style="color:red"><b>${user} login inactive</b></span><br></br>"""
			}
			
		}
		def query = "from Person s where s.id in (select p.partyIdTo from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom = ${projectInstance.client.id} and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' ) order by s.lastName "
		def personInstanceList = Person.findAll( query )
		if(personInstanceList.size()==0){
			clientAccess += """<span style="color:red"><b>No Client Access</b></span>"""
			eventErrorList << ['Project']
		}else{
			clientAccess += """<span style="color:Green"><b>Client Access :&nbsp;&nbsp; &nbsp;&nbsp;${personInstanceList}</b></span>"""
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
			eventErrorList << ['Transport']
			truckError += """<span style="color: red;"><b>Trucks :No trucks defined</b><br></br></span>"""
		}else{
			truckError+="""<span style="color: green;"><b>Trucks : OK  </b><br></br></span>"""
		}
		
		Set cart = assetEntityList.cart
		cart.remove('')
		cart.remove(null)
		String cartError = ''
		
		if(cart.size()==0){
			eventErrorList << ['Transport']
			cartError += """<span style="color: red;"><b>Carts :No carts defined</b><br></br></span>"""
		}else{
			cartError+="""<span style="color: green;"><b>Carts : OK  </b><br></br></span>"""
		}
		
		Set shelf = assetEntityList.shelf
		shelf.remove('')
		shelf.remove(null)
		
		def shelfError = ''
		
		if(shelf.size()==0){
			eventErrorList << ['Transport']
			shelfError += """<span style="color: red;"><b>Shelves :No Shelves defined</b><br></br></span>"""
		}else{
			shelfError+= """<span style="color: green;"><b>Shelves : OK (${shelf.size()}) </b><br></br></span>"""
		}
		
		return[truckError:truckError,truck:truck,cartError:cartError,cart:cart,shelf:shelf,shelfError:shelfError,
			   eventErrorList:eventErrorList]
	}
}
