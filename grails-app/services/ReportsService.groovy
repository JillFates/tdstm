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
		def date = new Date()
		def formatter = new SimpleDateFormat("MMM dd,yyyy hh:mm a");
		String time = formatter.format(date);

		def errorForEventTime = ""
		def moveBundles = moveEventInstance.moveBundles
		def inProgressError
		def clientAccess
		def userLoginError = ""

		moveBundles.each{
			if(it.startTime > projectInstance.startDate && it.completionTime > projectInstance.completionDate){
				errorForEventTime += """<span style="color:red" >Move bundle ${it.name} is completing after project completion  </span><br></br>"""
			}else{
				errorForEventTime += """<span style="color:green"><b>Event Time Period ${it.name} :OK </b>${it.startTime} - ${it.completionTime}</span><br></br>"""
			}
		}
		if(moveEventInstance.inProgress=='true'){
			inProgressError = "Event In Progress"
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
				userLoginError +="""<span style="color:red"><b>${Person.get(staff.partyIdTo.id)} login disabled</b></span><br></br>"""
			}
			if(user?.active=='N'){
				userLoginError +="""<span style="color:red"><b>${user} login inactive</b></span><br></br>"""
			}
		}
		def query = "from Person s where s.id in (select p.partyIdTo from PartyRelationship p where p.partyRelationshipType = 'STAFF' and p.partyIdFrom = ${projectInstance.client.id} and p.roleTypeCodeFrom = 'COMPANY' and p.roleTypeCodeTo = 'STAFF' ) order by s.lastName "
		def personInstanceList = Person.findAll( query )
		if(personInstanceList.size()==0){
			clientAccess = "No Client Access"
		}

		//for Event and Bundles ----------------------------------------------------------------//

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

  //for Assets and Bundles ----------------------------------------------------------------//
		
		def assetEntityList = AssetEntity.findAllByMoveBundleInListAndProject(moveBundles,projectInstance,[sort:'assetName'])
		
		def assetsInfo = getAssetInfo(assetEntityList,moveBundles,projectInstance,currProj,moveEventInstance)

  //---------------------------------------For Teams----------------------------------------------//
		def moveBundleTeamInfo = getMoveBundleTeamInfo(moveBundles, assetEntityList)

 //---------------------------------------For Transport-----------------------------------------------//
        Set truck = assetEntityList.truck
		truck.remove('')
		truck.remove(null)
		String truckError = ''
		if(truck.size()==0){
			truckError += """<span style="color: red;"><b>Trucks :No trucks defined</b><br></br></span>"""
		}else{
			truckError+="""<span style="color: green;"><b>Trucks : OK  </b><br></br></span>"""
		}

		return['project':projectInstance,'time':time,'moveEvent':moveEventInstance,'errorForEventTime':errorForEventTime,
			'inProgressError':inProgressError,'userLoginError':userLoginError,'clientAccess':clientAccess,'list':list,
			'workFlowCodeSelected':workFlowCodeSelected,'steps':steps,'moveBundleSize':moveBundles.size(),'moveBundles':moveBundles,'summaryOk':assetsInfo.summaryOk,
			'duplicatesAssetNames':assetsInfo.duplicatesAssetNames,'duplicates':assetsInfo.duplicates,'duplicatesTag':assetsInfo.duplicatesTag,
			'duplicatesAssetTagNames':assetsInfo.duplicatesAssetTagNames,'missedRacks':assetsInfo.missedRacks,'missingRacks':assetsInfo.missingRacks,
			'dependenciesOk':assetsInfo.dependenciesOk,'issue':assetsInfo.issue,'issueMap':assetsInfo.issues,'bundleMap':moveBundleTeamInfo.bundleMap,
			'notAssignedToTeam':moveBundleTeamInfo.notAssignedToTeam,'teamAssignment':moveBundleTeamInfo.teamAssignment,
			'inValidUsers':moveBundleTeamInfo.inValidUsers,'userLogin':moveBundleTeamInfo.userLogin,'truckError':truckError,'truck':truck]

	}
	
	/**
	 * @param moveBundles
	 * @param assetEntityList
	 * @return bundleMap, inValidUsers, teamAssignment, notAssignedToTeam
	 */
	
	def getMoveBundleTeamInfo( moveBundles, assetEntityList ){
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
			if(assetEntity['sourceTeamMt']?.teamCode == null || assetEntity['targetTeamMt']?.teamCode==null){
				notAssignedToTeam << [assetEntity.assetName]
			}
		}
		def teamAssignment = ""
		if(notAssignedToTeam.size()>0){
			teamAssignment+="""<span style="color: red;"><b>MoveTech Assignment : Asset Not Assigned  </b><br></br></span>"""
		}else{
			teamAssignment+="""<span style="color: green;"><b>MoveTech Assignment : OK  </b><br></br></span>"""
		}
		Set inValidUsers = []
		bundleMap.teamList.teamList.each{lists->
			lists.id[0].each{personId->
				personId.each{ids->
					if(UserLogin.findByPerson(Person.get(ids))?.lastLogin!=null){
						inValidUsers << [Person.get(ids)]
					}
				}
			}

		}
		def userLogin =""
			if(inValidUsers.size()>0){
				userLogin+="""<span style="color: red;"><b>Team Details Check : Team Member name Not Valid</b><br></br></span>"""
			}else{
				userLogin+="""<span style="color: green;"><b>Team Details Check : OK  </b><br></br></span>"""
			}
		return [bundleMap:bundleMap,inValidUsers:inValidUsers,
			teamAssignment:teamAssignment, notAssignedToTeam:notAssignedToTeam,userLogin:userLogin]
	}
	
	/**
	* @param assetEntityList,moveBundles,projectInstance
	* @param assetEntityList
	* @return summaryOk,issue,dependenciesOk,dependencies,missingRacks,missedRacks,duplicatesTag,duplicatesAssetTagNames,duplicates,duplicatesAssetNames
	*/
	
	def getAssetInfo(assetEntityList,moveBundles,projectInstance,currProj,moveEventInstance){

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
		
		def duplicatesAssetNames = jdbcTemplate.queryForList("SELECT asset_name as assetName , count(*) as counts from asset_entity where project_id = $currProj GROUP BY asset_name HAVING COUNT(*) > 1")
		
		String duplicates = ""
		if(duplicatesAssetNames.size()>0){
			duplicates += """<span style="color: red;"><b>Naming Check: <br></br></span>"""
		}else{
			duplicates += """<span style="color: green;"><b>Naming Check OK <br></br></span>"""
		}
		
		def duplicatesAssetTagNames = jdbcTemplate.queryForList("SELECT asset_tag as tag , count(*) as counts from asset_entity where project_id = $currProj GROUP BY asset_tag HAVING COUNT(*) > 1")
		
		String duplicatesTag = ""
		if(duplicatesAssetTagNames.size()>0){
			duplicatesTag += """<span style="color: red;"><b>Asset Tag: <br></br></span>"""
		}else{
			duplicatesTag += """<span style="color: green;"><b>Asset Tag  OK <br></br></span>"""
		}
		
		def missingRacks = AssetEntity.findAll("from AssetEntity asset where  asset.project.id=${currProj} and assetType not in('Application','Files','Database','Blades') and asset.moveBundle.moveEvent.id=${moveEventInstance.id} and (sourceRack='' or targetRack='' or sourceRackPosition = '' or targetRackPosition = '')  ").assetName
		
		String missedRacks = ""
		if(missingRacks.size()>0){
			missedRacks += """<span style="color: red;"><b>Asset Details:${missingRacks.size} Servers With Missing Rack info : </b><br></br></span>"""
		}else{
			missedRacks += """<span style="color: Green;"><b>Asset Details: OK </b><br></br></span>"""
			missingRacks = ''
		}
		
		def dependencies = []
		if(assetEntityList.size()>0){
			dependencies = AssetDependency.findAllByAssetInListOrDependentInList(assetEntityList,assetEntityList)?.asset?.assetName
		}
		
		String dependenciesOk = ""
		if(dependencies.size()>0){
			dependenciesOk +="""<span style="color: red;"><b>Dependency found:${dependencies.size} :${dependencies} </b><br></br></span>"""
		}else{
			dependenciesOk +="""<span style="color: green;"><b>Dependency -No Dependencies : </b><br></br></span>"""
		}
		
		String assetId = assetEntityList.id
		assetId = assetId.replace("[","('").replace(",","','").replace("]","')")
		
		def issues = AssetComment.findAll("from AssetComment comment where comment.assetEntity.id in ${assetId} and commentType ='issue' and  isResolved =0 order by comment.assetEntity.assetName ")
		def issue = ""
		if(issues.size()>0){
			issue +="""<span style="color: red;"><b>Issues: Unresolved Issues  </b><br></br></span>"""
		}else{
			issue +="""<span style="color: green;"><b>Issues OK  </b><br></br></span>"""
		}
		
		return[summaryOk:summaryOk,issue:issue,dependenciesOk:dependenciesOk,dependencies:dependencies,missingRacks:missingRacks,
			   missedRacks:missedRacks,duplicatesTag:duplicatesTag,duplicatesAssetTagNames:duplicatesAssetTagNames,duplicates:duplicates,
			   duplicatesAssetNames:duplicatesAssetNames]

	}
}
