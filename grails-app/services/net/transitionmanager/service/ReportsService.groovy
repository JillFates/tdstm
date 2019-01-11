package net.transitionmanager.service

import com.tds.asset.Application
import com.tds.asset.AssetComment
import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.AssetType
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WebUtil
import grails.gorm.transactions.Transactional
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveBundleStep
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.PartyRelationship
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.ProjectTeam
import net.transitionmanager.domain.RoleType
import net.transitionmanager.domain.UserLogin
import org.apache.commons.lang3.RandomUtils
import org.apache.commons.lang3.StringUtils
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.jdbc.core.JdbcTemplate

@Transactional
class ReportsService implements ServiceMethods {

	JdbcTemplate jdbcTemplate
	PartyRelationshipService partyRelationshipService
	RunbookService runbookService
	TaskService taskService

	@Transactional(readOnly = true)
	def generatePreMoveCheckList(projectId, MoveEvent moveEvent, boolean viewUnpublished = false) {
		Project project = Project.get(projectId)
		List<MoveBundle> moveBundles = moveEvent.moveBundles.sort { it.name }
		List<String> eventErrorList = []

  //---------------------------------------for Events and Project ---------------------------------------//

		def eventsProjectInfo = getEventsProjectInfo(moveEvent, project, projectId, moveBundles, eventErrorList)

  //---------------------------------------for Event and Bundles ---------------------------------------//

		def eventBundleInfo = getEventsBundelsInfo(moveBundles, moveEvent, eventErrorList)

  //---------------------------------------for Assets and Bundles --------------------------------------//

		List<AssetEntity> assetEntityList = AssetEntity.findAllByMoveBundleInListAndProject(moveBundles, project, [sort: 'assetName'])

		def assetsInfo = getAssetInfo(assetEntityList, moveBundles, project, projectId, moveEvent, eventErrorList, viewUnpublished)

  //---------------------------------------For Teams---------------------------------------------------//

		def moveBundleTeamInfo = getMoveBundleTeamInfo(moveEvent, assetEntityList, eventErrorList)

  //---------------------------------------For Transport------------------------------------------------//

		def transportInfo = getTransportInfo(assetEntityList,eventErrorList)
		def modelInfo = getModelInfo(moveEvent, eventErrorList)

		Set allErrors = eventErrorList
		String eventErrorString
		if (allErrors) {
			eventErrorString = """<span style="color:red;text-align: center;"><h2>There were ${allErrors.size()} sections with Issues (see details below).</h2></span><br/>"""
		} else {
			eventErrorString = """<span style="color:green;text-align: center;"><h3>No preparation issues for this event</h3></span>"""
		}

  //---------------------------------------For Task Analysis------------------------------------------------//

		def taskAnalysisInfo = getTaskAnalysisInfo(moveEvent, eventErrorList, viewUnpublished)

		[time: eventsProjectInfo.time, moveEvent: moveEvent, userLoginError: eventsProjectInfo.userLoginError,
		 errorForEventTime: eventsProjectInfo.errorForEventTime, newsBarModeError: eventsProjectInfo.newsBarModeError,
		 project: project, clientAccess: eventsProjectInfo.clientAccess, list: eventsProjectInfo.list,
		 workFlowCodeSelected: eventBundleInfo.workFlowCodeSelected, steps: eventBundleInfo.steps,
		 moveBundleSize: moveBundles.size(), moveBundles: moveBundles, summaryOk: assetsInfo.summaryOk,
		 duplicatesAssetNames: assetsInfo.duplicatesAssetNames, duplicates: assetsInfo.duplicates,
		 duplicatesTag: assetsInfo.duplicatesTag, duplicatesAssetTagNames: assetsInfo.duplicatesAssetTagNames,
		 missedRacks: assetsInfo.missedRacks, missingRacks: assetsInfo.missingRacks,
		 dependenciesOk: assetsInfo.dependenciesOk, issue: assetsInfo.issue, issueMap: assetsInfo.issues,
		 bundleMap: moveBundleTeamInfo.bundleMap, notAssignedToTeam: moveBundleTeamInfo.notAssignedToTeam,
		 teamAssignment: moveBundleTeamInfo.teamAssignment, inValidUsers: moveBundleTeamInfo.inValidUsers,
		 userLogin: moveBundleTeamInfo.userLogin, truckError: transportInfo.truckError, truck: transportInfo.truck,
		 cartError: transportInfo.cartError, cart: transportInfo.cart, shelf: transportInfo.shelf,
		 shelfError: transportInfo.shelfError, nullAssetname: assetsInfo.nullAssetname, questioned: assetsInfo.questioned,
		 blankAssets: assetsInfo.blankAssets, questionedDependency: assetsInfo.questionedDependency,
		 specialInstruction: assetsInfo.specialInstruction, importantInstruction: assetsInfo.importantInstruction,
		 eventErrorString: eventErrorString, dashBoardOk: eventBundleInfo.dashBoardOk, allErrors: allErrors,
		 nullAssetTag: assetsInfo.nullAssetTag, blankAssetTag: assetsInfo.blankAssetTag, modelList: modelInfo.modelList,
		 modelError: modelInfo.modelError, eventIssues: assetsInfo.eventIssues, nonAssetIssue: assetsInfo.nonAssetIssue,
		 dependenciesNotValid: assetsInfo.dependenciesNotValid, cyclicalsError: taskAnalysisInfo.cyclicalsError,
		 cyclicalsRef: taskAnalysisInfo.cyclicalsRef, startsError: taskAnalysisInfo.startsError,
		 startsRef: taskAnalysisInfo.startsRef, sinksError: taskAnalysisInfo.sinksError,
		 sinksRef: taskAnalysisInfo.sinksRef, personAssignErr: taskAnalysisInfo.personAssignErr,
		 personTasks: taskAnalysisInfo.personTasks, taskerrMsg: taskAnalysisInfo.exceptionString]
	}

	/**
	 * Calculates the data to be used by the generateApplicationConflicts view to create a report of applications
	 * with issues in a bundle
	 * @param projectId  The id of the user's current project
	 * @param moveBundleId - The id of the moveBundle to generate the report for
	 * @param conflicts - If true, apps with dependencies in other moveBundles will be shown
	 * @param unresolved - If true, apps with dependencies with status 'Questioned' or 'Unknown' will be shown
	 * @param int assetCap - number of assets to be retrieved.
	 * @return Map - The parameters used by the view to generate the report
	 */
	def genApplicationConflicts(projectId, moveBundleId, boolean conflicts, boolean unresolved, boolean missing,
	                            boolean planning, ownerId, int assetCap) {
		Project project = Project.get(projectId)
		List appList = []
		def appsInBundle
		def currAppOwner
		log.debug "****bundle:$moveBundleId conflicts:$conflicts unresolved:$unresolved planning:$planning missing: $missing"

		if(planning) {
			appsInBundle = Application.findAllByMoveBundleInList(
					MoveBundle.findAllByProjectAndUseForPlanning(project, true), [max: assetCap])
		} else {
			appsInBundle = Application.findAllByMoveBundle(MoveBundle.load(moveBundleId), [max: assetCap])
		}

		if (ownerId != 'null') {
			currAppOwner = Person.get(ownerId)
			appsInBundle = appsInBundle.findAll{it.appOwner==currAppOwner}
		}

		log.debug "${appsInBundle}"
		appsInBundle.each {
			boolean showApp = false
			List<AssetDependency> dependsOnList = AssetDependency.findAllByAsset(it)
			List<AssetDependency> supportsList = AssetDependency.findAllByDependent(it)

			if (conflicts && !unresolved && !missing) {
				showApp = true
			} else {
				// Check for missing dependencies if showApp is false
				if(missing){
					if (!dependsOnList & !supportsList) {
						showApp = true
					}
				}
				// Check for bundleConflicts if showApp is false
				if(!showApp && conflicts){
					def conflictIssue = dependsOnList.find{(it.asset.moveBundle?.id != it.dependent.moveBundle?.id) && ( it.status in ['Validated','Questioned','Unknown'] )}
					if(!conflictIssue){
						conflictIssue = supportsList.find{(it.asset.moveBundle?.id != it.dependent.moveBundle?.id) && ( it.status in ['Validated','Questioned','Unknown'] )}
					}
					if(conflictIssue){
						showApp = true
					}
				}
				// Check for unResolved Dependencies if showApp is false
				if(!showApp && unresolved){
					def statusIssue = dependsOnList.find{it.status in ['Questioned','Unknown']}
					if(!statusIssue){
						statusIssue = supportsList.find{it.status in ['Questioned','Unknown']}
					}
					if(statusIssue){
						showApp = true
					}
				}
			}

			if (showApp) {
				appList.add(app: it, dependsOnList: dependsOnList, supportsList: supportsList,
				            dependsOnIssueCount: dependsOnList.size(), supportsIssueCount: supportsList.size())
			}
		}

		[project: project, appList: appList, columns: 9, currAppOwner: currAppOwner ?: 'All',
		 moveBundle: moveBundleId.isNumber() ? MoveBundle.get(moveBundleId) : moveBundleId]
	}

	/**
	 * @return bundleMap, inValidUsers, teamAssignment, notAssignedToTeam
	 */
	def getMoveBundleTeamInfo( event, assetEntityList,eventErrorList) {
		List<MoveBundle> moveBundles = event.moveBundles.sort { it.name }
		Project project = event.project
		def bundleMap = []
		if ( project.runbookOn == 0 ) {
			moveBundles.each{moveBundle->
				List<ProjectTeam> teams = ProjectTeam.findAllByMoveBundle(moveBundle,[sort: 'name'])
				List<Map> teamList = teams.collect { ProjectTeam team ->
					[teamList: partyRelationshipService.getBundleTeamMembers(team), name: team.name,
					 assetSize: AssetEntity.countByMoveBundle(moveBundle), moveBundle: moveBundle, role: team.role]
				}
				bundleMap << [name: moveBundle?.name, size: teams.size(), teamList: teamList]
			}
		} else {
			// This can not be replaced with  partyRelationshipService.getStaffingRoles()
			RoleType.findAllByDescriptionIlike("Staff%").each { RoleType func ->
				bundleMap << [name: func.description, code: func.id,
				              assignedStaff: partyRelationshipService.getProjectStaffByFunction(func, project),
				              tasks: AssetComment.countByMoveEventAndRole(event, func.id)]
			}
			bundleMap = bundleMap.findAll { it.assignedStaff || it.tasks }
		}

		def notAssignedToTeam = []
		String teamAssignment
		if (notAssignedToTeam) {
			teamAssignment = redSpan('MoveTech Assignment: Asset Not Assigned')
			eventErrorList << 'Teams'
		}
		else {
			teamAssignment = greenSpan('MoveTech Assignment: OK')
		}

		Set inValidUsers = []
		for (lists in bundleMap.teamList.teamList) {
			for (personId in lists.id[0]) {
				for (id in personId) {
					Person person = Person.get(id)
					UserLogin user = person.userLogin
					if (user?.lastLogin == null || user?.active == 'N') {
						inValidUsers << [person]
					}
				}
			}
		}

		String userLogin
		if (inValidUsers) {
			userLogin = redSpan('Team Details Check: Team Member name Not Valid')
			eventErrorList << 'Teams'
		}
		else {
			userLogin = greenSpan('Team Details Check: OK')
		}

		[bundleMap: bundleMap, inValidUsers: inValidUsers, teamAssignment: teamAssignment,
		 notAssignedToTeam: notAssignedToTeam, userLogin: userLogin, eventErrorList: eventErrorList]
	}

	/**
	 * @return summaryOk, issue, dependenciesOk, dependencies, missingRacks, missedRacks, duplicatesTag,
	 *         duplicatesAssetTagNames, duplicates, duplicatesAssetNames
	 */
	def getAssetInfo(assetEntityList, List<MoveBundle> moveBundles, Project project, currProj, MoveEvent moveEvent,
	                 List<String> eventErrorList, boolean viewUnpublished = false) {

		Map<MoveBundle, String> summaryOk = [:]

		for (MoveBundle moveBundle in moveBundles) {
			String counts = AssetEntity.executeQuery('''
				select count(*), assetType from AssetEntity
				where moveBundle=:moveBundle group by assetType
			''', [moveBundle: moveBundle]).toString()
			summaryOk[moveBundle] = counts.replace('[[', '').replace(']]', '').replace(',', '').replace('] [',' , ').replace('[]', '0')
		}

		List<Map> duplicatesAssetNames = jdbcTemplate.queryForList('''
			SELECT asset_name as assetName, count(*) as counts, asset_type as type
         from asset_entity
         where project_id=?
           and asset_name is not null
           and move_bundle_id in (select move_bundle_id from move_bundle where move_event_id=?)
         GROUP BY asset_name, asset_type HAVING COUNT(*) > 1
		''', currProj.toLong(), moveEvent.id)

		List<Map> nullAssetname = jdbcTemplate.queryForList('''
			SELECT asset_name as assetName, asset_tag as tag, asset_type as type
			from asset_entity
			where project_id=?
			  and asset_name is null
			  and move_bundle_id in (select move_bundle_id from move_bundle where move_event_id=?)
		''', currProj.toLong(), moveEvent.id)

		String duplicates
		if (duplicatesAssetNames) {
			duplicates = redSpan('Naming Check: ')
			eventErrorList << 'Assets'
		}
		else {
			duplicates = greenSpan('Naming Check: OK')
		}

		String blankAssets
		if (nullAssetname) {
			blankAssets = redSpan('Blank Naming Check:')
			eventErrorList << 'Assets'
		}
		else {
			blankAssets = greenSpan('Blank Naming Check: OK')
		}

		List<Map> duplicatesAssetTagNames = jdbcTemplate.queryForList('''
			SELECT asset_tag as tag, count(*) as counts
			from asset_entity
			where project_id=?
			  and asset_tag is not null
			  and asset_type in ('Server','VM','Blade')
			  and move_bundle_id in (select move_bundle_id from move_bundle where move_event_id=?)
			GROUP BY asset_tag HAVING COUNT(*) > 1
		''', currProj.toLong(), moveEvent.id)

		String duplicatesTag
		if (duplicatesAssetTagNames) {
			duplicatesTag = redSpan('Asset Tag: ')
			eventErrorList << 'Assets'
		}else{
			duplicatesTag = greenSpan('Asset Tag: OK')
		}

		List<Map> nullAssetTag = jdbcTemplate.queryForList('''
			SELECT asset_name as assetName, asset_tag as tag, asset_type as type
			from asset_entity
			where project_id=?
			  and asset_tag is null
			  and asset_type in ('Server','VM','Blade')
			  and move_bundle_id in (select move_bundle_id from move_bundle where move_event_id=?)
		''', currProj.toLong(), moveEvent.id)

		String blankAssetTag
		if (nullAssetTag) {
			blankAssetTag = redSpan('Blank Tag Check: ' + nullAssetTag.size() + ' assets with no asset tags')
			eventErrorList << 'Assets'
		}
		else {
			blankAssetTag = greenSpan('Blank Naming Tag: OK')
		}

		List<String> missingRacks = AssetEntity.executeQuery('''
			from AssetEntity asset
			where asset.project.id=:projectId
			  and asset.assetType not in (:type)
			  and asset.moveBundle.moveEvent=:event
			  and (rackSource is null or rackTarget is null or sourceRackPosition = '' or targetRackPosition = '')
			  group by asset.assetName
		''', [projectId: currProj.toLong(), type: ['Application', 'Files', 'Database', 'VM'], event: moveEvent]).assetName

		String missedRacks
		if (missingRacks) {
			missedRacks = redSpan('Asset Details: ' + missingRacks.size() + ' Servers With Missing Rack info:')
			eventErrorList << 'Assets'
		}
		else {
			missedRacks = greenSpan('Asset Details: OK')
			missingRacks = []
		}

		List<String> dependencies = assetEntityList ? AssetDependency.findAllByDependentInList(assetEntityList)*.asset*.assetName : []
		List<Long> assetIds = assetEntityList*.id
		dependencies.sort()
		List<AssetDependency> depsStatusNotValid = assetIds ? AssetDependency.executeQuery('''
			from AssetDependency dependency
			where (dependency.asset.id in (:assetIds)
			   or dependency.dependent.id in (:assetIds))
			  and dependency.status=:status
			order by dependency.asset.assetName
		''', [assetIds: assetIds, status: 'Validated']) : []
		Set assetsWithDep = depsStatusNotValid.asset + depsStatusNotValid.dependent
		def assetsWithOutDep = assetEntityList - assetsWithDep.toList()

		String dependenciesNotValid
		if (assetsWithOutDep) {
			dependenciesNotValid = redSpan('Assets without dependency: ' + assetsWithOutDep.size() + ' Assets:') +
				'<div style="margin-left:50px;"> ' + WebUtil.listAsMultiValueString(assetsWithOutDep.assetName) + '</div>'
		}
		else {
			dependenciesNotValid = redSpan('Assets without dependency: 0 Assets')
		}

		String dependenciesOk
		if (dependencies) {
			dependenciesOk = greenSpan('Dependency found: ' + dependencies.size + ' Dependency Found:')
			eventErrorList << 'Assets'
		}
		else {
			dependenciesOk = redSpan('Dependency: OK-No Dependencies:')
		}

		def publishedValues = viewUnpublished ? [true, false] : [true]
		def categories = ['shutdown','moveday','startup','physical','physical-source','physical-target']
		def issues = assetIds ? AssetComment.executeQuery('''
			from AssetComment comment
			where comment.assetEntity.id in (:assetIds)
			  and commentType ='issue'
			  and dateResolved = null 
			  and comment.category not in (:categories)
			  and comment.isPublished IN (:publishedValues)
			order by comment.assetEntity.assetName
		''', [assetIds: assetIds, categories: categories, publishedValues: publishedValues]) : []

		String issue
		if (issues) {
			issue = redSpan('Asset Tasks: Unresolved Tasks')
			eventErrorList << 'Assets'
		}
		else {
			issue = greenSpan('Asset Tasks: OK')
		}

		def specialInstruction = assetIds ? AssetComment.executeQuery('''
			from AssetComment comment
			where comment.assetEntity.id in (:assetIds)
			  and mustVerify = 1
			  and comment.isPublished IN (:publishedValues)
			order by comment.assetEntity.assetName
		''', [assetIds: assetIds, publishedValues: publishedValues]) : []

		String importantInstruction
		if (specialInstruction) {
			importantInstruction = redSpan('Special Instruction:')
			eventErrorList << 'Assets'
		}
		else {
			importantInstruction = greenSpan('Special Instruction: OK')
		}

		Set questionedDependencies = assetIds ? AssetDependency.executeQuery('''
			from AssetDependency dependency
			where (dependency.asset.id in (:assetIds)
			   or dependency.dependent.id in (:assetIds))
			  and dependency.status in (:statuses)
			  and dependency.asset.moveBundle.moveEvent = :event
			order by dependency.asset.assetName
		''', [assetIds: assetIds, event: moveEvent, statuses: ['Questioned', 'Unknown']]).asset.assetName : []

		List questionedDependency = questionedDependencies.sort()
		String questioned
		if (questionedDependency) {
			questioned = redSpan('Dependencies Questioned for ' + questionedDependency.size() + ' assets:')
		}
		else {
			questioned = greenSpan('Dependencies Questioned: OK')
		}

		def nonAssetIssue = AssetComment.executeQuery('''
			from AssetComment
			where moveEvent = :event
			  and category in(:categories)
			  and dateResolved = null
			  and isPublished IN (:publishedValues)
		''', [event: moveEvent, categories: ['general', 'discovery', 'planning', 'walkthru'],
		      publishedValues: publishedValues])

		String eventIssues
		if (nonAssetIssue) {
			eventIssues = redSpan('Event Tasks:')
		}
		else {
			eventIssues = greenSpan('Event Tasks: OK')
		}

		[summaryOk: summaryOk, issue: issue, issues: issues, dependenciesOk: dependenciesOk, dependencies: dependencies,
		 missingRacks: missingRacks, missedRacks: missedRacks, duplicatesTag: duplicatesTag, duplicates: duplicates,
		 duplicatesAssetTagNames: duplicatesAssetTagNames, duplicatesAssetNames: duplicatesAssetNames,
		 nullAssetname: nullAssetname, blankAssets: blankAssets, questioned: questioned, eventIssues: eventIssues,
		 questionedDependency: questionedDependency, specialInstruction: specialInstruction, nullAssetTag: nullAssetTag,
		 importantInstruction: importantInstruction, eventErrorList: eventErrorList, blankAssetTag: blankAssetTag,
		 nonAssetIssue: nonAssetIssue, dependenciesNotValid: dependenciesNotValid]
	}

	def getEventsBundelsInfo(moveBundles, MoveEvent moveEvent, eventErrorList) {
		Set workFlowCode = moveBundles.workflowCode
		def workFlow = moveBundles.workflowCode
		def workFlowCodeSelected = [:]
		def steps = [:]

		if (workFlowCode.size() == 1) {
			workFlowCodeSelected[moveEvent.name + '  (Event)   All Bundles have same WorkFlow  '] = workFlow[0]
		} else {
			moveBundles.each {
				workFlowCodeSelected[it.name + '(Bundle)    Uses WorkFlow '] = it.workflowCode
			}
		}

		List<String> dashBoardOk = []
		moveBundles.each { moveBundle->
			List<String> labels = []
			def moveBundleStep = MoveBundleStep.findAllByMoveBundle(moveBundle, [sort: 'transitionId'])
			if (!moveBundleStep) {
				steps[moveBundle.name] = "No steps created"
				eventErrorList << 'EventsBundle'
				dashBoardOk << 'No steps created'
			} else {
				moveBundleStep.each { step->
					labels << step.label + '(' + (step.planDuration / 60) + 'm)'
					steps[moveBundle.name] = labels.toString().replace('[[', '').replace('], [', ' , ').replace(']]', '')
				}

				dashBoardOk << greenSpan('Dashboard OK:')
			}
		}

		[workFlowCodeSelected: workFlowCodeSelected, steps: steps,
		 eventErrorList: eventErrorList, dashBoardOk:dashBoardOk]
	}

	/**
	* @return time,moveEvent,errorForEventTime,newsBarModeError,userLoginError,clientAccess,list
	*/
	def getEventsProjectInfo(MoveEvent moveEvent, Project project, currProj, moveBundles, List<String> eventErrorList) {

		Date date = new Date()
		String time = TimeUtil.formatDateTime(date, TimeUtil.FORMAT_DATE_TIME_8)

		String errorForEventTime = ''
		moveBundles.each {
			if (it.startTime > project.startDate && it.completionTime > project.completionDate) {
				eventErrorList << 'Project'
				errorForEventTime += redSpan('Move bundle ' + it.name + ' is completing after project completion')
			}
			else {
				def projectStartTime = 'Not Available'
				def projectEndTime = 'Not Available'

				if (it.startTime) {
					projectStartTime  = TimeUtil.formatDateTime(it.startTime, TimeUtil.FORMAT_DATE_TIME_8)
				}
				if (it.completionTime) {
					projectEndTime  = TimeUtil.formatDateTime(it.completionTime, TimeUtil.FORMAT_DATE_TIME_8)
				}

				errorForEventTime += """<span style="color:green"><b>Event Time Period $it.name: OK </b>$projectStartTime - $projectEndTime</span><br></br>"""
			}
		}

		def lastMoveBundleDate = moveEvent.moveBundles.completionTime
		lastMoveBundleDate.sort()
		def lastMoveBundleDateSize = lastMoveBundleDate.size()
		def moveEventCompletiondate
		if (lastMoveBundleDateSize > 0) {
			moveEventCompletiondate = lastMoveBundleDate[-1]
		}

		String newsBarModeError
		if (moveEvent.newsBarMode == 'on') {
			eventErrorList << 'Project'
			newsBarModeError = redSpan(moveEvent.name + ': MoveEvent In Progress')
		} else if (moveEventCompletiondate < project.startDate) {
			eventErrorList << 'Project'
			newsBarModeError = redSpan(moveEvent.name + ': MoveEvent In Past', '', false)
		} else {
			newsBarModeError = greenSpan(moveEvent.name + ': OK', '', false)
		}

		List<Map> list = partyRelationshipService.getProjectStaff(currProj)
		list.sort { a, b -> a.company?.toString() <=> b.company?.toString() ?: a.role?.toString() <=> b.role?.toString() }

		def projectStaff = PartyRelationship.executeQuery('''
			from PartyRelationship
			where partyRelationshipType = 'PROJ_STAFF'
			  and partyIdFrom.id=?
			  and roleTypeCodeFrom = 'ROLE_PROJECT'
		''', [currProj.toLong()])

		String userLoginError = ''
		projectStaff.each { staff ->
			Person person = staff.partyIdTo
			UserLogin user = person.userLogin

			if (!user) {
				eventErrorList << 'Project'
				userLoginError += redSpan(person.toString() + ' login disabled', 'margin-left:50px;')
			}
			else if (user.active=='N') {
				eventErrorList << 'Project'
				userLoginError += greenSpan(user.toString() + ' login inactive', 'margin-left:50px;')
			}
		}

		List<Person> persons = Person.executeQuery('''
			from Person where
			id in (select p.partyIdTo from PartyRelationship p
			       where p.partyRelationshipType='STAFF'
			         and p.partyIdFrom.id=:clientId
			         and p.roleTypeCodeFrom= 'ROLE_COMPANY'
			         and p.roleTypeCodeTo='ROLE_STAFF')
			order by lastName
		''', [clientId: project.clientId])

		String clientAccess
		if (!persons) {
			clientAccess = redSpan('No Client Access', '', false)
			eventErrorList << 'Project'
		}
		else {
			clientAccess = greenSpan('Client Access:&nbsp;' + persons, '', false)
		}

		[time: time, moveEvent: moveEvent, errorForEventTime: errorForEventTime, newsBarModeError: newsBarModeError,
		 userLoginError: userLoginError, clientAccess: clientAccess, list: list, eventErrorList: eventErrorList]
	}

	def getTransportInfo(assetEntityList, List<String> eventErrorList) {
		Set trucks = []
		String truckError = transportError(assetEntityList, 'truck', eventErrorList, 'Trucks', trucks)
		Set carts = []
		String cartError = transportError(assetEntityList, 'cart', eventErrorList, 'Carts', carts)
		Set shelves = []
		String shelfError = transportError(assetEntityList, 'shelf', eventErrorList, 'Shelves', shelves)

		[truckError: truckError, truck: trucks, cartError: cartError, cart: carts,
		 shelf: shelves, shelfError: shelfError, eventErrorList: eventErrorList]
	}

	private String transportError(assetEntityList, String propertyName, List<String> eventErrorList, String type, Set instances) {
		instances.addAll assetEntityList[propertyName]
		instances.remove('')
		instances.remove(null)

		if (!instances) {
			eventErrorList << 'Transport'
			return redSpan(type + ': No ' + type.toLowerCase() + ' defined')
		}

		greenSpan(type + ': OK (' + instances.size() + ')')
	}

	def getModelInfo(MoveEvent moveEvent, List<String> eventErrorList) {
		Collection modelList = AssetEntity.executeQuery('''
			select model.modelName
			from AssetEntity
			where model.modelStatus=?
			  and model.usize=?
			  and moveBundle.moveEvent=?
			order by model.modelName asc
		''', ['new', 1, moveEvent])

		String modelError
		if (modelList) {
			eventErrorList << 'Model'
			modelError = redSpan(modelList.size().toString() + ': un-validated models used : ', 'margin-left:50px;')
		} else {
			modelError = greenSpan('Model: OK', 'margin-left:50px;')
		}

		[modelList: modelList, modelError: modelError]
	}

	/**
	 * Attempts to generate a dot graph file based on application graph property configurations from the
	 * dotText passed to the method.
	 * @param String filenamePrefix - a prefix used when creating the filename that will include the datetime plus a random #
	 * @param String dotText - the dot sytax used to define the graph
	 * @return String the URI to access the resulting file
	 * @throws RuntimeException when the generation fails, the exception message will contain the output from the dot command
	 *
	 * @deprecated As of release 4.1.4, replaced by {@link GraphvizService#generateSVGFromDOT()}
	 */
	@Deprecated
	def generateDotGraph( filenamePrefix, dotText ) {

		def conf = grailsApplication.config.graph
		String tmpDir = conf.tmpDir
		String targetDir = conf.targetDir
		String targetURI = conf.targetURI
		def dotExec = conf?.graphviz?.dotCmd
		def graphType = conf?.graphviz?.graphType
		def deleteDotFile = conf?.containsKey('deleteDotFile') ? conf.deleteDotFile : true
		def random = RandomUtils.nextInt()
		String filename = filenamePrefix + '-' + new Date().format('yyyyMMdd-HHmmss') + '-' + random
		String imgFilename = filename + '.' + graphType

		// log.info "dot: $dotText"

		// Create the dot file
		def dotFile = new File(tmpDir, filename + '.dot')
		dotFile << dotText

		def sout = new StringBuilder()
		def serr = new StringBuilder()
		String cmd = "$dotExec -T$graphType -v -o $targetDir$imgFilename $dotFile"
		log.info "generateDotGraph: about to execute command: $cmd"
		def proc = cmd.execute()
		proc.consumeProcessOutput(sout, serr)
	 	proc.waitForOrKill(150000)
	 	log.info "generateDotGraph: process stdout=$sout"
	 	log.info "generateDotGraph: process stderr=$serr"

		if (proc.exitValue() != 0) {
			def errFile = new File(targetDir, filename + '.err')
			errFile << "exit code:\n\n${proc.exitValue()}\n\nstderr:\n$serr\n\nstdout:\n$sout"
			throw new RuntimeException("Exit code: ${proc.exitValue()}\n stderr: $serr\n stdout: $sout")
		}

			// Delete the dot file because we don't need it and configured to delete it automatically
		if (deleteDotFile) dotFile.delete()
			return targetURI + imgFilename
	}

	/**
	 * Get the smeList from moveBundle & Project
	 */
	List<Person> getSmeList(moveBundleId, boolean forSme) {
		Project project = securityService.userCurrentProject
		def apps
		if(moveBundleId && moveBundleId !='useForPlanning'){
			apps = Application.findAllByMoveBundleAndProject(MoveBundle.load(moveBundleId), project)
		}
		else if (moveBundleId == 'useForPlanning') {
			apps = Application.findAllByProjectAndMoveBundleInList(project, MoveBundle.getUseForPlanningBundlesByProject(project))
		}
		else {
			apps = Application.findAllByProject(project)
		}

		Set<Person> smes = []
		if (forSme) {
			smes.addAll apps*.sme
			smes.addAll apps*.sme2
		}
		else {
			smes.addAll apps*.appOwner
		}
		smes.remove(null)

		return smes.sort { it.lastName }
	}

	/**
	 *  Used to generate server conflicts Report.
	 */
	def genServerConflicts(Project project, moveBundleId, bundleConflicts, unresolvedDep, runsOn, vmSupport, planning, GrailsParameterMap params, int assetCap) {
		List assetList = []
		def assetsInBundle = []
		int maxR = assetCap // params.int('rows', 50)
		int ofst = params.int('offset', 0)
		int appCount = params.int('appCount', 0)
		log.debug "****bundle:$moveBundleId bundleConflicts:$bundleConflicts unresolvedDep:$unresolvedDep RunsOn:$runsOn  vmSupport:$vmSupport planning:$planning "
		def bundles
		if(planning) {
			bundles = MoveBundle.findAllByProjectAndUseForPlanning(project, true)
		}
		else {
			bundles = [MoveBundle.get(moveBundleId)]
		}
		if(bundles){
			assetsInBundle = AssetEntity.executeQuery('''
				FROM AssetEntity
				WHERE moveBundle IN (:bundles)
				  AND assetType IN (:types)
				ORDER BY assetName
			''', [bundles: bundles, types: AssetType.serverTypes], [max: maxR, offset: ofst])
			if (!appCount)
				appCount = AssetEntity.executeQuery('''
					FROM AssetEntity
					WHERE moveBundle IN (:bundles)
					  AND assetType IN (:types)
				''', [bundles: bundles, types: AssetType.serverTypes]).size()
		}

		log.debug "$assetsInBundle"
		def titleString = new StringBuilder()
		if(bundleConflicts){
			titleString.append('Bundle Conflicts')
		}
		if(unresolvedDep){
			titleString.append(', UnResolved Dependencies')
		}
		if(runsOn){
			titleString.append(', No Runs On')
		}
		if(vmSupport){
			titleString.append(', VM With NO support')
		}
		if( !bundleConflicts && !unresolvedDep && !runsOn && !vmSupport){
			titleString.append('All')
		}
		assetsInBundle.each{asset->
			boolean showAsset = false
			def dependsOnList = AssetDependency.findAllByAsset(asset)
			def supportsList = AssetDependency.findAllByDependent(asset)
			String header = ' '
			// skipt the asset if there is no deps and support
			if (!dependsOnList && !supportsList) {
				return
			}

			if( !bundleConflicts && !unresolvedDep && !runsOn && !vmSupport){
				showAsset = true
			} else {
				// Check for vm No support if showAsset is true
				if(asset.assetType=='VM' && !supportsList && vmSupport){
					header = 'No VM support?'
					showAsset = true
				}
				// Check for bundleConflicts if showAsset is false
				if(!showAsset && bundleConflicts){
					def conflictIssue = dependsOnList.find{(it.asset.moveBundle?.id != it.dependent.moveBundle?.id) && ( it.status in ['Validated','Questioned','Unknown'] )}
					if(!conflictIssue){
						conflictIssue = supportsList.find{(it.asset.moveBundle?.id != it.dependent.moveBundle?.id) && ( it.status in ['Validated','Questioned','Unknown'] )}
					}
					if(conflictIssue){
						showAsset = true
					}
				}
				// Check for unResolved Dependencies if showAsset is false
				if(!showAsset && unresolvedDep){
					def statusIssue = dependsOnList.find{it.status in ['Questioned','Unknown']}
					if(!statusIssue){
						statusIssue = supportsList.find{it.status in ['Questioned','Unknown']}
					}
					if(statusIssue){
						showAsset = true
					}
				}

				// Check for Run On if showAsset is false
				if(!showAsset && runsOn){
					if (!supportsList.find { it.type == 'Runs On' }) {
						showAsset = true
						header='No applications?'
					}
				}
			}

			if (showAsset) {
				assetList.add(app: asset, dependsOnList: dependsOnList, supportsList: supportsList, header:header,
					           dependsOnIssueCount: dependsOnList.size, supportsIssueCount: supportsList.size)
			}
		}

		[project: project, appList: assetList, title: StringUtils.stripStart(titleString.toString(), ","),
		 moveBundle: moveBundleId.isNumber() ? MoveBundle.get(moveBundleId) : "Planning Bundles", columns: 9,
		 maxR: maxR, ofst: ofst + maxR, bundleConflicts: bundleConflicts, noRunsOn: params.noRuns,
		 unresolvedDependencies: params.unresolvedDep, vmWithNoSupport: params.vmWithNoSupport,
		 moveBundleId: params.moveBundle, appCount:appCount]
	}

	def genDatabaseConflicts(moveBundleId, bundleConflicts, unresolvedDep, noApps, dbSupport, planning, int assetCap) {
		Project project = securityService.userCurrentProject
		List assetList = []
		def assetsInBundle = []
		log.debug "****bundle:$moveBundleId bundleConflicts:$bundleConflicts unresolvedDep:$unresolvedDep noApps:$noApps  dbSupport:$dbSupport planning:$planning "

		def bundles
		if(planning) {
			bundles = MoveBundle.findAllByProjectAndUseForPlanning(project, true)
		} else {
			bundles = [MoveBundle.get(moveBundleId)]
		}
		if(bundles){
			assetsInBundle = AssetEntity.executeQuery('''
				FROM AssetEntity
				WHERE moveBundle IN (:bundles)
				  AND assetType=:type
				ORDER BY assetName
			''', [bundles: bundles, type: AssetType.DATABASE.toString()], [max: assetCap])
		}

		def titleString = new StringBuilder()
		if(bundleConflicts){
			titleString.append('Bundle Conflicts')
		}
		if(unresolvedDep){
			titleString.append(', UnResolved Dependencies')
		}
		if(noApps){
			titleString.append(', No Applications')
		}
		if(dbSupport){
			titleString.append(', DB With NO support')
		}
		if( !bundleConflicts && !unresolvedDep && !noApps && !dbSupport){
			titleString.append('All')
		}
		assetsInBundle.each{asset->
			boolean showDb = false
			def dependsOnList = AssetDependency.findAllByAsset(asset)
			def supportsList = AssetDependency.findAllByDependent(asset)
			def noAppSupport = !supportsList.asset.assetType.contains(AssetType.APPLICATION.toString())

			String header = ''

			if(!supportsList){
				header += ' No DB support?'
			}
			if(noAppSupport){
				header +=' No applications?'
			}

			// skip the asset if there is no deps and support
			if (!dependsOnList && !supportsList) {
				return
			}

			if( !bundleConflicts && !unresolvedDep && !noApps && !dbSupport){
				showDb = true
			} else {
				// Check for vm No support if showDb is true
				if(!supportsList && dbSupport){
					showDb = true
				}
				// Check for bundleConflicts if showDb is false
				if(!showDb && bundleConflicts){
					def conflictIssue = dependsOnList.find{(it.asset.moveBundle?.id != it.dependent.moveBundle?.id) && ( it.status in ['Validated','Questioned','Unknown'] )}
					if(!conflictIssue){
						conflictIssue = supportsList.find{(it.asset.moveBundle?.id != it.dependent.moveBundle?.id) && ( it.status in ['Validated','Questioned','Unknown'] )}
					}
					if(conflictIssue){
						showDb = true
					}
				}
				// Check for unResolved Dependencies if showDb is false
				if(!showDb && unresolvedDep){
					def statusIssue = dependsOnList.find{it.status in ['Questioned','Unknown']}
					if(!statusIssue){
						statusIssue = supportsList.find{it.status in ['Questioned','Unknown']}
					}
					if(statusIssue){
						showDb = true
					}
				}

				// Check for Run On if showDb is false
				if(!showDb && noApps){
					if (!supportsList.find { it.type == 'DB' }) {
						showDb = true
					}
				}
			}
			if(showDb)
				assetList.add(app: asset, dependsOnList: dependsOnList, supportsList: supportsList, header: header,
				              dependsOnIssueCount: dependsOnList.size, supportsIssueCount: supportsList.size)
		}

		[project: project, appList: assetList, title: StringUtils.stripStart(titleString.toString(), ","),
		 moveBundle: moveBundleId.isNumber() ? MoveBundle.get(moveBundleId) : "Planning Bundles", columns: 9]
	}

	/**
	 * used to get cyclical references, multiple sink vertices and assignment data.
	 */
	def getTaskAnalysisInfo(MoveEvent moveEvent, eventErrorList, boolean viewUnpublished = false) {
		def dfsMap
		String cyclicalsError = ''
		String startsError = ''
		String sinksError = ''
		String personAssignErr = ''
		List<AssetComment> personTasks = []
		String exceptionString = ''
		StringBuilder startsRef = new StringBuilder()
		StringBuilder sinksRef = new StringBuilder()
		StringBuilder cyclicalsRef = new StringBuilder()

		def publishedValues = viewUnpublished ? [true, false] : [true]
		def tasks = runbookService.getEventTasks(moveEvent).findAll { it.isPublished in publishedValues }
		def deps = runbookService.getTaskDependencies(tasks)
		def tmp = runbookService.createTempObject(tasks, deps)

		try {
			dfsMap = runbookService.processDFS( tasks, deps, tmp )
		} catch (e) {
			exceptionString += "No Tasks"
		}

		if (dfsMap) {
			if (dfsMap.cyclicals?.size() == 0) {
				cyclicalsError = greenSpan('Cyclical References: OK')
			} else {
				eventErrorList << 'Tasks'
				cyclicalsError = redSpan('Cyclical References:')
				cyclicalsRef.append('<ol>')

				dfsMap.cyclicals.each { c ->
					def task = c.value.loopback
					cyclicalsRef.append("<li> Circular Reference Stack: <ul>")
					c.value.stack.each { cycTaskId ->
						task = tasks.find { it.id == cycTaskId }
						if (task) {
							cyclicalsRef.append("<li>$task.taskNumber $task.comment [TaskSpec $task.taskSpec]")
						} else {
							cyclicalsRef.append("<li>Unexpected error trying to find task record id $cycTaskId")
						}
					}
					cyclicalsRef.append(" >> $c.value.loopback.taskNumber $c.value.loopback.comment</li>")
					cyclicalsRef.append('</ul>')
				}

				cyclicalsRef.append('</ol>')
			}

			// check for multiple starts
			if (dfsMap.starts?.size() == 1) {
				startsError = greenSpan('Start Vertices: OK')
			} else if (dfsMap.starts?.size() == 0) {
				eventErrorList << 'Tasks'
				startsError = redSpan('Start Vertices: <br> No start task was found.')
			} else {
				eventErrorList << 'Tasks'
				startsError = redSpan('''Start Vertices: <br>
					Warning - More than one task has no predecessors. Typical events will have just one starting task
					(e.g. Prep for Move Event). This is an indicator that some task wiring may be incorrect.''')
				startsRef.append('<ul>')
				dfsMap.starts.each { startsRef.append("<li>$it [TaskSpec $it.taskSpec]") }
				startsRef.append('</ul>')
			}

			// check for multiple sinks
			if (dfsMap.sinks?.size() == 1) {
				sinksError = greenSpan('Sink Vertices: OK')
			} else if (dfsMap.sinks?.size() == 0) {
				eventErrorList << 'Tasks'
				sinksError = redSpan('Sink Vertices: <br> No end task was found, which is typically the result of cyclical references.')
			} else {
				eventErrorList << 'Tasks'
				sinksError = redSpan('''Sink Vertices: <br>
					Warning - More than one task has no successors. Typical events will have just one ending task
					(e.g. Move Event Complete). This is an indicator that some task wiring may be incorrect.''')
				sinksRef.append('<ul>')
				dfsMap.sinks.each { sinksRef.append("<li>$it [TaskSpec $it.taskSpec]") }
				sinksRef.append('</ul>')
			}

			personTasks = AssetComment.executeQuery('''
				from AssetComment
				where moveEvent=:moveEvent
				  and (assignedTo is null and (role is null or role=''))
				  and category in (:category)
				  AND isPublished IN (:publishedValues)
			''', [moveEvent: moveEvent, category: AssetComment.moveDayCategories, publishedValues: publishedValues])

			def missedAssignments = personTasks.size()
			if (missedAssignments == 0) {
				personAssignErr = greenSpan('Person/Team Assignments : OK')
			} else {
				eventErrorList << 'Tasks'
				personAssignErr = redSpan('Person/Team Assignments : ' + missedAssignments + ' task' +
					(missedAssignments > 1 ? 's have' : ' has') + ' no person or team assigned')
			}
		}

		[cyclicalsError: cyclicalsError, cyclicalsRef: cyclicalsRef, sinksRef: sinksRef, startsRef: startsRef,
		 sinksError: sinksError, startsError: startsError, personAssignErr: personAssignErr, personTasks: personTasks,
		 exceptionString: exceptionString]
	}

	private String redSpan(String contents, String extraCss = '', boolean includeBrTags = true) {
		span 'red', contents, extraCss, includeBrTags
	}

	private String greenSpan(String contents, String extraCss = '', boolean includeBrTags = true) {
		span 'green', contents, extraCss, includeBrTags
	}

	private String span(String color, String contents, String extraCss, boolean includeBrTags) {
		'<span style="color:' + color + ';' + extraCss + '"><b>' + contents + '</b>' +
				(includeBrTags ? '<br></br>' : '') + '</span>'
	}

	private find(target1, target2, Closure closure) {
		target1.find(closure) || target2.find(closure)
	}
}
