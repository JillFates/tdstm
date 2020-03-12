package net.transitionmanager.reporting

import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.FilenameFormat
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import com.tdssrc.grails.FilenameUtil
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WebUtil
import com.tdssrc.grails.WorkbookUtil
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.time.TimeCategory
import groovy.transform.CompileStatic
import net.transitionmanager.application.ApplicationProfilesCommand
import net.transitionmanager.asset.Application
import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetDependencyBundle
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.AssetEntityService
import net.transitionmanager.asset.AssetType
import net.transitionmanager.asset.Database
import net.transitionmanager.command.ApplicationMigrationCommand
import net.transitionmanager.command.reports.ActivityMetricsCommand
import net.transitionmanager.command.reports.DatabaseConflictsCommand
import net.transitionmanager.common.ControllerService
import net.transitionmanager.common.CustomDomainService
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.party.PartyRelationship
import net.transitionmanager.party.PartyRelationshipService
import net.transitionmanager.person.Person
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.project.AppMoveEvent
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveBundleService
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.MoveEventService
import net.transitionmanager.project.Project
import net.transitionmanager.project.ProjectService
import net.transitionmanager.project.ProjectTeam
import net.transitionmanager.security.Permission
import net.transitionmanager.security.RoleType
import net.transitionmanager.security.UserLogin
import net.transitionmanager.service.ServiceMethods
import net.transitionmanager.tag.TagAssetService
import net.transitionmanager.task.AssetComment
import net.transitionmanager.task.Task
import net.transitionmanager.task.TaskService
import net.transitionmanager.task.timeline.CPAResults
import net.transitionmanager.task.timeline.TaskTimeLineGraph
import net.transitionmanager.task.timeline.TaskVertex
import net.transitionmanager.task.timeline.TimelineService
import net.transitionmanager.task.timeline.TimelineSummary
import net.transitionmanager.task.timeline.TimelineTask
import org.apache.commons.lang3.RandomUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

import javax.servlet.http.HttpServletResponse
import java.sql.ResultSet
import java.sql.SQLException

@Transactional
class ReportsService implements ServiceMethods {

    JdbcTemplate jdbcTemplate
	NamedParameterJdbcTemplate namedParameterJdbcTemplate
    PartyRelationshipService partyRelationshipService
    TaskService taskService
    MoveBundleService moveBundleService
    UserPreferenceService userPreferenceService
    MoveEventService moveEventService
    CustomDomainService customDomainService
    AssetEntityService assetEntityService
    ControllerService controllerService
    ProjectService projectService
    TagAssetService tagAssetService
    TimelineService timelineService

    @Transactional(readOnly = true)
    def generatePreMoveCheckList(projectId, MoveEvent moveEvent, boolean viewUnpublished = false) {
        Project project = Project.get(projectId)
        List<MoveBundle> moveBundles = moveEvent.moveBundles.sort { it.name }
        List<String> eventErrorList = []

        //---------------------------------------for Events and Project ---------------------------------------//

        def eventsProjectInfo = getEventsProjectInfo(moveEvent, project, projectId, moveBundles, eventErrorList)


        //---------------------------------------for Assets and Bundles --------------------------------------//

        List<AssetEntity> assetEntityList
        if (moveBundles) {
            assetEntityList = AssetEntity.findAllByMoveBundleInListAndProject(moveBundles, project, [sort: 'assetName'])
        } else {
            assetEntityList = AssetEntity.findAllByProject(project, [sort: 'assetName'])
        }


        def assetsInfo = getAssetInfo(assetEntityList, moveBundles, project, projectId, moveEvent, eventErrorList, viewUnpublished)

        //---------------------------------------For Teams---------------------------------------------------//

        def moveBundleTeamInfo = getMoveBundleTeamInfo(moveEvent, assetEntityList, eventErrorList)

        //---------------------------------------For Transport------------------------------------------------//

        def transportInfo = getTransportInfo(assetEntityList, eventErrorList)
        def modelInfo = getModelInfo(moveEvent, eventErrorList)

        Set allErrors = eventErrorList
        String eventErrorString
        if (allErrors) {
            eventErrorString = """<span style="color:red;text-align: center;"><h2>There were ${
                allErrors.size()
            } sections with Issues (see details below).</h2></span><br/>"""
        } else {
            eventErrorString = """<span style="color:green;text-align: center;"><h3>No preparation issues for this event</h3></span>"""
        }

        //---------------------------------------For Task Analysis------------------------------------------------//

        def taskAnalysisInfo = getTaskAnalysisInfo(moveEvent, eventErrorList, viewUnpublished)

        [
            time                   : eventsProjectInfo.time,
            moveEvent              : moveEvent,
            userLoginError         : eventsProjectInfo.userLoginError,
            errorForEventTime      : eventsProjectInfo.errorForEventTime,
            project                : project,
            clientAccess           : eventsProjectInfo.clientAccess,
            projectStaffList       : eventsProjectInfo.projectStaffList,
            list                   : eventsProjectInfo.list,
            moveBundleSize         : moveBundles.size(),
            moveBundles            : moveBundles,
            summaryOk              : assetsInfo.summaryOk,
            duplicatesAssetNames   : assetsInfo.duplicatesAssetNames,
            duplicates             : assetsInfo.duplicates,
            duplicatesTag          : assetsInfo.duplicatesTag,
            duplicatesAssetTagNames: assetsInfo.duplicatesAssetTagNames,
            missedRacks            : assetsInfo.missedRacks,
            missingRacks           : assetsInfo.missingRacks,
            dependenciesOk         : assetsInfo.dependenciesOk,
            issue                  : assetsInfo.issue,
            issueMap               : assetsInfo.issues,
            bundleMap              : moveBundleTeamInfo.bundleMap,
            notAssignedToTeam      : moveBundleTeamInfo.notAssignedToTeam,
            teamAssignment         : moveBundleTeamInfo.teamAssignment,
            inValidUsers           : moveBundleTeamInfo.inValidUsers,
            userLogin              : moveBundleTeamInfo.userLogin,
            truckError             : transportInfo.truckError,
            truck                  : transportInfo.truck,
            cartError              : transportInfo.cartError,
            cart                   : transportInfo.cart,
            shelf                  : transportInfo.shelf,
            shelfError             : transportInfo.shelfError,
            nullAssetname          : assetsInfo.nullAssetname,
            questioned             : assetsInfo.questioned,
            blankAssets            : assetsInfo.blankAssets,
            questionedDependency   : assetsInfo.questionedDependency,
            specialInstruction     : assetsInfo.specialInstruction,
            importantInstruction   : assetsInfo.importantInstruction,
            eventErrorString       : eventErrorString,
            allErrors              : allErrors,
            nullAssetTag           : assetsInfo.nullAssetTag,
            blankAssetTag          : assetsInfo.blankAssetTag,
            modelList              : modelInfo.modelList,
            modelError             : modelInfo.modelError,
            eventIssues            : assetsInfo.eventIssues,
            nonAssetIssue          : assetsInfo.nonAssetIssue,
            dependenciesNotValid   : assetsInfo.dependenciesNotValid,
            cyclicalsError         : taskAnalysisInfo.cyclicalsError,
            cyclicalsRef           : taskAnalysisInfo.cyclicalsRef,
            startsError            : taskAnalysisInfo.startsError,
            startsRef              : taskAnalysisInfo.startsRef,
            sinksError             : taskAnalysisInfo.sinksError,
            sinksRef               : taskAnalysisInfo.sinksRef,
            personAssignErr        : taskAnalysisInfo.personAssignErr,
            personTasks            : taskAnalysisInfo.personTasks,
            taskerrMsg             : taskAnalysisInfo.exceptionString
        ]
    }

    /**
     * Calculates the data to be used by the generateApplicationConflicts view to create a report of applications
     * with issues in a bundle
     * @param projectId The id of the user's current project
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

        if (planning) {
            appsInBundle = Application.findAllByMoveBundleInList(
                    MoveBundle.findAllByProjectAndUseForPlanning(project, true), [max: assetCap])
        } else {
            appsInBundle = Application.findAllByMoveBundle(MoveBundle.load(moveBundleId), [max: assetCap])
        }

        if (ownerId != 'null') {
            currAppOwner = Person.get(ownerId)
            appsInBundle = appsInBundle.findAll { it.appOwner == currAppOwner }
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
                if (missing) {
                    if (!dependsOnList & !supportsList) {
                        showApp = true
                    }
                }
                // Check for bundleConflicts if showApp is false
                if (!showApp && conflicts) {
                    def conflictIssue = dependsOnList.find {
                        (it.asset.moveBundle?.id != it.dependent.moveBundle?.id) && (it.status in ['Validated', 'Questioned', 'Unknown'])
                    }
                    if (!conflictIssue) {
                        conflictIssue = supportsList.find {
                            (it.asset.moveBundle?.id != it.dependent.moveBundle?.id) && (it.status in ['Validated', 'Questioned', 'Unknown'])
                        }
                    }
                    if (conflictIssue) {
                        showApp = true
                    }
                }
                // Check for unResolved Dependencies if showApp is false
                if (!showApp && unresolved) {
                    def statusIssue = dependsOnList.find { it.status in ['Questioned', 'Unknown'] }
                    if (!statusIssue) {
                        statusIssue = supportsList.find { it.status in ['Questioned', 'Unknown'] }
                    }
                    if (statusIssue) {
                        showApp = true
                    }
                }
            }

			if (showApp) {
				appList.add(app: it, dependsOnList: dependsOnList*.toMap(), supportsList: supportsList*.toMap(),
				            dependsOnIssueCount: dependsOnList.size(), supportsIssueCount: supportsList.size())
			}
		}

        [project   : project, appList: appList, columns: 9, currAppOwner: currAppOwner ?: 'All',
         moveBundle: moveBundleId.isNumber() ? MoveBundle.get(moveBundleId) : moveBundleId]
    }

    /**
     * @return bundleMap, inValidUsers, teamAssignment, notAssignedToTeam
     */
    def getMoveBundleTeamInfo(event, assetEntityList, eventErrorList) {
        List<MoveBundle> moveBundles = event.moveBundles.sort { it.name }
        Project project = event.project
        def bundleMap = []
        if (project.runbookOn == 0) {
            moveBundles.each { moveBundle ->
                List<ProjectTeam> teams = ProjectTeam.findAllByMoveBundle(moveBundle, [sort: 'name'])
                List<Map> teamList = teams.collect { ProjectTeam team ->
                    [teamList : partyRelationshipService.getBundleTeamMembers(team), name: team.name,
                     assetSize: AssetEntity.countByMoveBundle(moveBundle), moveBundle: moveBundle, role: team.role]
                }
                bundleMap << [name: moveBundle?.name, size: teams.size(), teamList: teamList]
            }
        } else {
            // This can not be replaced with  partyRelationshipService.getStaffingRoles()
            RoleType.findAllByDescriptionIlike("Staff%").each { RoleType func ->
                bundleMap << [name         : func.description, code: func.id,
                              assignedStaff: partyRelationshipService.getProjectStaffByFunction(func, project),
                              tasks        : AssetComment.countByMoveEventAndRole(event, func.id)]
            }
            bundleMap = bundleMap.findAll { it.assignedStaff || it.tasks }
        }

        def notAssignedToTeam = []
        String teamAssignment
        if (notAssignedToTeam) {
            teamAssignment = redSpan('MoveTech Assignment: Asset Not Assigned')
            eventErrorList << 'Teams'
        } else {
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
        } else {
            userLogin = greenSpan('Team Details Check: OK')
        }

        [
            bundleMap        : bundleMap,
            inValidUsers     : inValidUsers,
            teamAssignment   : teamAssignment,
            notAssignedToTeam: notAssignedToTeam,
            userLogin        : userLogin,
            eventErrorList   : eventErrorList
        ]
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
            summaryOk[moveBundle] = counts.replace('[[', '').replace(']]', '').replace(',', '').replace('] [', ' , ').replace('[]', '0')
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
        } else {
            duplicates = greenSpan('Naming Check: OK')
        }

        String blankAssets
        if (nullAssetname) {
            blankAssets = redSpan('Blank Naming Check:')
            eventErrorList << 'Assets'
        } else {
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
        } else {
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
        } else {
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
        } else {
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
                    '<div style="margin-left:50px;"> ' + HtmlUtil.escape(WebUtil.listAsMultiValueString(assetsWithOutDep.assetName)) + '</div>'
        } else {
            dependenciesNotValid = redSpan('Assets without dependency: 0 Assets')
        }

        String dependenciesOk
        if (dependencies) {
            dependenciesOk = greenSpan('Dependency found: ' + dependencies.size + ' Dependency Found:')
            eventErrorList << 'Assets'
        } else {
            dependenciesOk = redSpan('Dependency: OK-No Dependencies:')
        }

        def publishedValues = viewUnpublished ? [true, false] : [true]
        def categories = ['shutdown', 'moveday', 'startup', 'physical', 'physical-source', 'physical-target']
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
        } else {
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
        } else {
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
        } else {
            questioned = greenSpan('Dependencies Questioned: OK')
        }

        def nonAssetIssue = AssetComment.executeQuery('''
			from AssetComment
			where moveEvent = :event
			  and category in(:categories)
			  and dateResolved = null
			  and isPublished IN (:publishedValues)
		''', [event          : moveEvent, categories: ['general', 'discovery', 'planning', 'walkthru'],
              publishedValues: publishedValues])

        String eventIssues
        if (nonAssetIssue) {
            eventIssues = redSpan('Event Tasks:')
        } else {
            eventIssues = greenSpan('Event Tasks: OK')
        }

        [
            summaryOk              : summaryOk,
            issue                  : issue, issues: issues,
            dependenciesOk         : dependenciesOk,
            dependencies           : dependencies,
            missingRacks           : missingRacks,
            missedRacks            : missedRacks,
            duplicatesTag          : duplicatesTag,
            duplicates             : duplicates,
            duplicatesAssetTagNames: duplicatesAssetTagNames,
            duplicatesAssetNames   : duplicatesAssetNames,
            nullAssetname          : nullAssetname,
            blankAssets            : blankAssets,
            questioned             : questioned,
            eventIssues            : eventIssues,
            questionedDependency   : questionedDependency,
            specialInstruction     : specialInstruction,
            nullAssetTag           : nullAssetTag,
            importantInstruction   : importantInstruction,
            eventErrorList         : eventErrorList,
            blankAssetTag          : blankAssetTag,
            nonAssetIssue          : nonAssetIssue,
            dependenciesNotValid   : dependenciesNotValid
        ]
    }

    /**
     * @return time,moveEvent,errorForEventTime, userLoginError,clientAccess,list
     */
    def getEventsProjectInfo(MoveEvent moveEvent, Project project, currProj, moveBundles, List<String> eventErrorList) {

        Date date = new Date()
        String time = TimeUtil.formatDateTime(date, TimeUtil.FORMAT_DATE_TIME_8)

        String errorForEventTime = ''
        moveBundles.each {
            if (it.startTime > project.startDate && it.completionTime > project.completionDate) {
                eventErrorList << 'Project'
                errorForEventTime += redSpan('Move bundle ' + HtmlUtil.escape(it.name) + ' is completing after project completion')
            } else {
                def projectStartTime = 'Not Available'
                def projectEndTime = 'Not Available'

                if (it.startTime) {
                    projectStartTime = TimeUtil.formatDateTime(it.startTime, TimeUtil.FORMAT_DATE_TIME_8)
                }
                if (it.completionTime) {
                    projectEndTime = TimeUtil.formatDateTime(it.completionTime, TimeUtil.FORMAT_DATE_TIME_8)
                }

                errorForEventTime += """<span style="color:green"><b>Event Time Period ${HtmlUtil.escape(it.name)}: OK </b>$projectStartTime - $projectEndTime</span><br></br>"""
            }
        }

        def lastMoveBundleDate = moveEvent.moveBundles.completionTime
        lastMoveBundleDate.sort()
        def lastMoveBundleDateSize = lastMoveBundleDate.size()
        def moveEventCompletiondate
        if (lastMoveBundleDateSize > 0) {
            moveEventCompletiondate = lastMoveBundleDate[-1]
        }

        // Get the list of staff assigned to the Event and sort it by company/role/name
        List<Map> projectStaffList = partyRelationshipService.getProjectStaff(currProj)
        projectStaffList.sort {
            a, b ->
                a.company?.toString() <=> b.company?.toString() ?:
                    a.role?.toString() <=> b.role?.toString() ?:
                        a.name?.toString() <=> b.name?.toString()
        }

        def projectStaff = PartyRelationship.executeQuery("""
			from PartyRelationship
			where partyRelationshipType = 'PROJ_STAFF'
			  and partyIdFrom.id=?
			  and roleTypeCodeFrom = '$RoleType.CODE_PARTY_PROJECT'
			  and roleTypeCodeTo = '$RoleType.CODE_PARTY_STAFF'
		""".toString(), [currProj.toLong()])

        String userLoginError = ''

        projectStaff.each { staff ->
            Person person = staff.partyIdTo
            UserLogin user = person.userLogin

            if (!user) {
                eventErrorList << 'Project'
                userLoginError += redSpan(HtmlUtil.escape(person.toString()) + ' no login', 'margin-left:50px;')
            } else if (user.active == 'N') {
                eventErrorList << 'Project'
                userLoginError += greenSpan(HtmlUtil.escape(user.toString()) + ' login inactive', 'margin-left:50px;')
            }
        }

        List<Person> persons = Person.executeQuery("""
			from Person where
			id in (select p.partyIdTo from PartyRelationship p
			       where p.partyRelationshipType='STAFF'
			         and p.partyIdFrom.id=:clientId
			         and p.roleTypeCodeFrom= '$RoleType.CODE_PARTY_COMPANY'
			         and p.roleTypeCodeTo='$RoleType.CODE_PARTY_STAFF')
			order by lastName
		""".toString(), [clientId: project.clientId])

        String clientAccess
        if (!persons) {
            clientAccess = redSpan('No Client Access', '', false)
            eventErrorList << 'Project'
        } else {
            clientAccess = greenSpan(
                'Client Access:&nbsp;' + persons.collect { HtmlUtil.escape(it.toString()) }.join(', ')
                , '', false)
        }

        [
            time             : time,
            moveEvent        : moveEvent,
            errorForEventTime: errorForEventTime,
            userLoginError   : userLoginError,
            clientAccess     : clientAccess,
            projectStaffList: projectStaffList,
            eventErrorList   : eventErrorList
        ]
    }

    def getTransportInfo(assetEntityList, List<String> eventErrorList) {
        Set trucks = []
        String truckError = transportError(assetEntityList, 'truck', eventErrorList, 'Trucks', trucks)
        Set carts = []
        String cartError = transportError(assetEntityList, 'cart', eventErrorList, 'Carts', carts)
        Set shelves = []
        String shelfError = transportError(assetEntityList, 'shelf', eventErrorList, 'Shelves', shelves)

        [
            truckError    : truckError,
            truck         : trucks,
            cartError     : cartError,
            cart          : carts,
            shelf         : shelves,
            shelfError    : shelfError,
            eventErrorList: eventErrorList
        ]
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
     * @deprecated As of release 4.1.4, replaced by {net.transitionmanager.common.GraphvizService#generateSVGFromDOT()}
     */
    @Deprecated
    def generateDotGraph(filenamePrefix, dotText) {

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
        if (moveBundleId && moveBundleId != 'useForPlanning') {
            apps = Application.findAllByMoveBundleAndProject(MoveBundle.load(moveBundleId), project)
        } else if (moveBundleId == 'useForPlanning') {
            apps = Application.findAllByProjectAndMoveBundleInList(project, MoveBundle.getUseForPlanningBundlesByProject(project))
        } else {
            apps = Application.findAllByProject(project)
        }

        Set<Person> smes = []
        if (forSme) {
            smes.addAll apps*.sme
            smes.addAll apps*.sme2
        } else {
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
        if (planning) {
            bundles = MoveBundle.findAllByProjectAndUseForPlanning(project, true)
        } else {
            bundles = [MoveBundle.get(moveBundleId)]
        }
        if (bundles) {
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
        if (bundleConflicts) {
            titleString.append('Bundle Conflicts')
        }
        if (unresolvedDep) {
            titleString.append(', UnResolved Dependencies')
        }
        if (runsOn) {
            titleString.append(', No Runs On')
        }
        if (vmSupport) {
            titleString.append(', VM With NO support')
        }
        if (!bundleConflicts && !unresolvedDep && !runsOn && !vmSupport) {
            titleString.append('All')
        }
        assetsInBundle.each { asset ->
            boolean showAsset = false
            def dependsOnList = AssetDependency.findAllByAsset(asset)
            def supportsList = AssetDependency.findAllByDependent(asset)
            String header = ' '
            // skipt the asset if there is no deps and support
            if (!dependsOnList && !supportsList) {
                return
            }

            if (!bundleConflicts && !unresolvedDep && !runsOn && !vmSupport) {
                showAsset = true
            } else {
                // Check for vm No support if showAsset is true
                if (asset.assetType == 'VM' && !supportsList && vmSupport) {
                    header = 'No VM support?'
                    showAsset = true
                }
                // Check for bundleConflicts if showAsset is false
                if (!showAsset && bundleConflicts) {
                    def conflictIssue = dependsOnList.find {
                        (it.asset.moveBundle?.id != it.dependent.moveBundle?.id) && (it.status in ['Validated', 'Questioned', 'Unknown'])
                    }
                    if (!conflictIssue) {
                        conflictIssue = supportsList.find {
                            (it.asset.moveBundle?.id != it.dependent.moveBundle?.id) && (it.status in ['Validated', 'Questioned', 'Unknown'])
                        }
                    }
                    if (conflictIssue) {
                        showAsset = true
                    }
                }
                // Check for unResolved Dependencies if showAsset is false
                if (!showAsset && unresolvedDep) {
                    def statusIssue = dependsOnList.find { it.status in ['Questioned', 'Unknown'] }
                    if (!statusIssue) {
                        statusIssue = supportsList.find { it.status in ['Questioned', 'Unknown'] }
                    }
                    if (statusIssue) {
                        showAsset = true
                    }
                }

                // Check for Run On if showAsset is false
                if (!showAsset && runsOn) {
                    if (!supportsList.find { it.type == 'Runs On' }) {
                        showAsset = true
                        header = 'No applications?'
                    }
                }
            }

            if (showAsset) {
                assetList.add(app: asset, dependsOnList: dependsOnList, supportsList: supportsList, header: header,
                        dependsOnIssueCount: dependsOnList.size, supportsIssueCount: supportsList.size)
            }
        }

        [project               : project, appList: assetList, title: StringUtils.stripStart(titleString.toString(), ","),
         moveBundle            : moveBundleId.isNumber() ? MoveBundle.get(moveBundleId) : "Planning Bundles", columns: 9,
         maxR                  : maxR, ofst: ofst + maxR, bundleConflicts: bundleConflicts, noRunsOn: params.noRuns,
         unresolvedDependencies: params.unresolvedDep, vmWithNoSupport: params.vmWithNoSupport,
         moveBundleId          : params.moveBundle, appCount: appCount]
    }

    /**
     * Create and return a map with the information that should populates the Database Conflict Report.
     * @param project - a given project. If null, the user's current project will be used.
     * @param command - a DatabaseConflictsCommand instance with the parameters selection.
     * @return a map containing the corresponding database, the project, bundle and other data for the report.
     */
    Map generateDatabaseConflictsMap(Project project, DatabaseConflictsCommand command) {
        if (!project) {
            project = securityService.userCurrentProject
        }

        List<String> titleParts = []
        if (command.bundleConflicts) {
            titleParts.add('Bundle Conflicts')
        }
        if (command.unresolvedDependencies) {
            titleParts.add('Unresolved Dependencies')
        }
        if (command.missingApplications) {
            titleParts.add('No Applications')
        }
        if (command.unsupportedDependencies) {
            titleParts.add('DB With NO support')
        }

        // No checkbox has been checked?
        boolean noProblemsSelected = titleParts.size() == 0

        if (noProblemsSelected) {
            titleParts.add('All')
        }

        MoveBundle moveBundle
        List<MoveBundle> bundles
        if (command.moveBundle == 'useForPlanning') {
            bundles = MoveBundle.findAllByProjectAndUseForPlanning(project, true)
        } else {
            Long moveBundleId = NumberUtil.toLong(command.moveBundle)
            moveBundle = GormUtil.findInProject(project, MoveBundle, moveBundleId, true)
            bundles = [moveBundle]
        }

        List<Database> databasesInBundles = []
        if (bundles) {
            databasesInBundles = Database.where {
                moveBundle in (bundles)
            }.order('assetName').max(command.maxAssets).list()
        }


        List<String> conflictingStatus = ['Validated', 'Questioned', 'Unknown']
        List<String> unresolvedStatus = ['Questioned', 'Unknown']

        List<Map> databaseList = []

        databasesInBundles.each { Database database ->
            List<AssetDependency> dependsOnList = AssetDependency.findAllByAsset(database)
            List<AssetDependency> supportsList = AssetDependency.findAllByDependent(database)
            boolean noAppSupport = !supportsList.asset.assetType.contains(AssetType.APPLICATION.toString())

            // skip the asset if there is no deps and support
            if (!dependsOnList && !supportsList) {
                return
            }

            String header = ''

            if (!supportsList) {
                header += ' No DB support?'
            }
            if (noAppSupport) {
                header += ' No applications?'
            }

            boolean showDb = noProblemsSelected

            if (!showDb) {
                // Check for vm No support if showDb is true
                if (!supportsList && command.unsupportedDependencies) {
                    showDb = true
                }
                // Check for bundleConflicts if showDb is false
                if (!showDb && command.bundleConflicts) {
                    AssetDependency conflictIssue = dependsOnList.find {
                        (it.asset.moveBundle?.id != it.dependent.moveBundle?.id) && (it.status in conflictingStatus)
                    }
                    if (!conflictIssue) {
                        conflictIssue = supportsList.find {
                            (it.asset.moveBundle?.id != it.dependent.moveBundle?.id) && (it.status in conflictingStatus)
                        }
                    }
                    if (conflictIssue) {
                        showDb = true
                    }
                }
                // Check for unResolved Dependencies if showDb is false
                if (!showDb && command.unresolvedDependencies) {
                    def statusIssue = dependsOnList.find { it.status in unresolvedStatus }
                    if (!statusIssue) {
                        statusIssue = supportsList.find { it.status in unresolvedStatus }
                    }
                    if (statusIssue) {
                        showDb = true
                    }
                }

                // Check for Run On if showDb is false
                if (!showDb && command.missingApplications) {
                    if (!supportsList.find { it.type == 'DB' }) {
                        showDb = true
                    }
                }
            }
            if (showDb)
                databaseList.add([db: database, dependsOnList: dependsOnList*.toMap(), supportsList: supportsList*.toMap(), header: header,
                    dependsOnIssueCount: dependsOnList.size(), supportsIssueCount: supportsList.size()])
        }

        return [project: project,
                dbList: databaseList,
                title: titleParts.join(', '),
                moveBundle: moveBundle ?: "Planning Bundles",
                columns: 9] // Not sure what's the purpose of this.


    }

    /**
     * @Deprecated on 4.7 (TM-13137)
     */
    def genDatabaseConflicts(moveBundleId, bundleConflicts, unresolvedDep, noApps, dbSupport, planning, int assetCap) {
        Project project = securityService.userCurrentProject
        List assetList = []
        def assetsInBundle = []
        log.debug "****bundle:$moveBundleId bundleConflicts:$bundleConflicts unresolvedDep:$unresolvedDep noApps:$noApps  dbSupport:$dbSupport planning:$planning "

        def bundles
        if (planning) {
            bundles = MoveBundle.findAllByProjectAndUseForPlanning(project, true)
        } else {
            bundles = [MoveBundle.get(moveBundleId)]
        }
        if (bundles) {
            assetsInBundle = AssetEntity.executeQuery('''
				FROM AssetEntity
				WHERE moveBundle IN (:bundles)
				  AND assetType=:type
				ORDER BY assetName
			''', [bundles: bundles, type: AssetType.DATABASE.toString()], [max: assetCap])
        }

        def titleString = new StringBuilder()
        if (bundleConflicts) {
            titleString.append('Bundle Conflicts')
        }
        if (unresolvedDep) {
            titleString.append(', UnResolved Dependencies')
        }
        if (noApps) {
            titleString.append(', No Applications')
        }
        if (dbSupport) {
            titleString.append(', DB With NO support')
        }
        if (!bundleConflicts && !unresolvedDep && !noApps && !dbSupport) {
            titleString.append('All')
        }
        assetsInBundle.each { asset ->
            boolean showDb = false
            def dependsOnList = AssetDependency.findAllByAsset(asset)
            def supportsList = AssetDependency.findAllByDependent(asset)
            def noAppSupport = !supportsList.asset.assetType.contains(AssetType.APPLICATION.toString())

            String header = ''

            if (!supportsList) {
                header += ' No DB support?'
            }
            if (noAppSupport) {
                header += ' No applications?'
            }

            // skip the asset if there is no deps and support
            if (!dependsOnList && !supportsList) {
                return
            }

            if (!bundleConflicts && !unresolvedDep && !noApps && !dbSupport) {
                showDb = true
            } else {
                // Check for vm No support if showDb is true
                if (!supportsList && dbSupport) {
                    showDb = true
                }
                // Check for bundleConflicts if showDb is false
                if (!showDb && bundleConflicts) {
                    def conflictIssue = dependsOnList.find {
                        (it.asset.moveBundle?.id != it.dependent.moveBundle?.id) && (it.status in ['Validated', 'Questioned', 'Unknown'])
                    }
                    if (!conflictIssue) {
                        conflictIssue = supportsList.find {
                            (it.asset.moveBundle?.id != it.dependent.moveBundle?.id) && (it.status in ['Validated', 'Questioned', 'Unknown'])
                        }
                    }
                    if (conflictIssue) {
                        showDb = true
                    }
                }
                // Check for unResolved Dependencies if showDb is false
                if (!showDb && unresolvedDep) {
                    def statusIssue = dependsOnList.find { it.status in ['Questioned', 'Unknown'] }
                    if (!statusIssue) {
                        statusIssue = supportsList.find { it.status in ['Questioned', 'Unknown'] }
                    }
                    if (statusIssue) {
                        showDb = true
                    }
                }

                // Check for Run On if showDb is false
                if (!showDb && noApps) {
                    if (!supportsList.find { it.type == 'DB' }) {
                        showDb = true
                    }
                }
            }
            if (showDb)
                assetList.add(app: asset, dependsOnList: dependsOnList, supportsList: supportsList, header: header,
                        dependsOnIssueCount: dependsOnList.size, supportsIssueCount: supportsList.size)
        }

        [project   : project, appList: assetList, title: StringUtils.stripStart(titleString.toString(), ","),
         moveBundle: moveBundleId.isNumber() ? MoveBundle.get(moveBundleId) : "Planning Bundles", columns: 9]
    }

    /**
     * used to get cyclical references, multiple sink vertices and assignment data.
     */
    def getTaskAnalysisInfo(MoveEvent moveEvent, eventErrorList, boolean viewUnpublished = false) {
        String cyclicalsError = ''
        String startsError = ''
        String sinksError = ''
        String personAssignErr = ''
        List<AssetComment> personTasks = []
        String exceptionString = ''
        StringBuilder startsRef = new StringBuilder()
        StringBuilder sinksRef = new StringBuilder()
        StringBuilder cyclicalsRef = new StringBuilder()

        CPAResults cpaResults

        try {
            cpaResults = timelineService.calculateCPA(moveEvent, viewUnpublished)
        } catch (e) {
            exceptionString += "No Tasks"
        }

        Closure<String> htmlConverter = { TaskVertex taskVertex, TimelineTask task ->
            String content = "<li>${taskVertex.taskId} ${taskVertex.taskComment?.encodeAsHTML()}"
            if (task.taskSpec) {
                content += " [TaskSpec ${task.taskSpec}]"
            }
            return content
        }

        Closure<Task> findInTasks = { TaskVertex taskVertex, List<TimelineTask> taskList ->
            return taskList.find { it.id == taskVertex.taskId }
        }

        if (cpaResults) {

            TaskTimeLineGraph graph = cpaResults.graph
            TimelineSummary summary = cpaResults.summary
            List<TimelineTask> tasks = cpaResults.tasks

            if (!summary.hasCycles()) {
                cyclicalsError = greenSpan('Cyclical References: OK')
            } else {
                eventErrorList << 'Tasks'
                cyclicalsError = redSpan('Cyclical References:')
                cyclicalsRef.append('<ol>')
                summary.cycles.each { List<TaskVertex> c ->
                    cyclicalsRef.append("<li> Circular Reference Stack: <ul>")
                    c.each { TaskVertex cyclicalTask ->
                        Task task = findInTasks(cyclicalTask, tasks)
                        cyclicalsRef.append(htmlConverter(cyclicalTask, task))
                    }
                    cyclicalsRef.append('</ul>')
                }
                cyclicalsRef.append('</ol>')
            }

            // check for multiple starts
            if (graph.hasNoStarts()) {
				eventErrorList << 'Tasks'
				startsError = redSpan('Start Vertices: <br> No start task was found.')
            } else if (graph.hasOneStart()) {
				startsError = greenSpan('Start Vertices: OK')
            } else {
                eventErrorList << 'Tasks'
                startsError = redSpan('''Start Vertices: <br>
					Warning - More than one task has no predecessors. Typical events will have just one starting task
					(e.g. Prep for Move Event). This is an indicator that some task wiring may be incorrect.''')
                startsRef.append('<ul>')

                graph.starts.each { TaskVertex taskVertex ->
                    TimelineTask task = findInTasks(taskVertex, tasks)
                    startsRef.append(htmlConverter(taskVertex, task))
                }
                startsRef.append('</ul>')
            }

            // check for multiple sinks
            if (graph.hasNoSinks()) {
                eventErrorList << 'Tasks'
                sinksError = redSpan('Sink Vertices: <br> No end task was found, which is typically the result of cyclical references.')
            } else if (graph.hasOneSink()) {
                sinksError = greenSpan('Sink Vertices: OK')
            } else {
                eventErrorList << 'Tasks'
                sinksError = redSpan('''Sink Vertices: <br>
					Warning - More than one task has no successors. Typical events will have just one ending task
					(e.g. Move Event Complete). This is an indicator that some task wiring may be incorrect.''')
                sinksRef.append('<ul>')
                graph.sinks.each { TaskVertex taskVertex ->
                    TimelineTask task = findInTasks(taskVertex, tasks)
                    sinksRef.append(htmlConverter(taskVertex, task))
                }
                sinksRef.append('</ul>')
            }

            List<Boolean> publishedValues = viewUnpublished ? [true, false] : [true]

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

        [cyclicalsError : cyclicalsError, cyclicalsRef: cyclicalsRef, sinksRef: sinksRef, startsRef: startsRef,
         sinksError     : sinksError, startsError: startsError, personAssignErr: personAssignErr, personTasks: personTasks,
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

    /**
     * Generates the model used to create the Tasks Report.
     *
     * @param events The ids of the events, or 'all' for all events.
     * @param project The associated Project
     * @param wUnresolved Flag that indicates that only remaining tasks should be included in the report
     * @param viewUnpublished Flag that indicates if unpublished tasks should be included in the report
     * @param wComment Flag that indicates if comments should be included in the report
     * @return A map with model used to create the report [taskList, tzId, userDTFormat, vUnpublished]
     */
    def getTasksReportModel(def events, Project project, Boolean wUnresolved, Boolean viewUnpublished, Boolean wComment) {
        String tzId = userPreferenceService.timeZone
        String userDTFormat = userPreferenceService.dateFormat

        boolean allBundles = events.find { it == 'all' }
        List badReqEventIds

        if (!allBundles) {
            events = events.collect { id -> NumberUtil.toLong(id, 0) }
            //Verifying events id are in same project or not.
            badReqEventIds = moveEventService.verifyEventsByProject(events, project)
        }
        //if found any bad id returning to the user
        if (badReqEventIds) {
            throw new InvalidParamException("Event ids $badReqEventIds is not associated with current project.")
        }
        Map argMap = [type: AssetCommentType.ISSUE, project: project]
        String taskListHql = "FROM AssetComment WHERE project =:project AND commentType =:type "

        if (!allBundles) {
            taskListHql += " AND moveEvent.id IN (:events) "
            argMap.events = events
        }
        if (wUnresolved) {
            taskListHql += "AND status != :status"
            argMap.status = AssetCommentStatus.COMPLETED
        }
        // handle unpublished tasks
        userPreferenceService.setPreference(UserPreferenceEnum.VIEW_UNPUBLISHED, viewUnpublished as Boolean)

        boolean vUnpublished = securityService.hasPermission(Permission.TaskPublish) && viewUnpublished
        if (!vUnpublished) {
            taskListHql += " AND isPublished = :isPublished "
            argMap.isPublished = true
        }

        List taskList = AssetComment.findAll(taskListHql, argMap)
        if (vUnpublished) {
            taskList.addAll(wComment ? AssetComment.findAllByCommentTypeAndProject(AssetCommentType.COMMENT, project) : [])
        } else {
            taskList.addAll(wComment ? AssetComment.findAllByCommentTypeAndProjectAndIsPublished(AssetCommentType.COMMENT, project, true) : [])
        }
        return [taskList: taskList, tzId: tzId, userDTFormat: userDTFormat, vUnpublished: vUnpublished]
    }

    /**
     * Export task report in XLS format
     * @param taskList : list of tasks
     * @param tzId : timezone
     * @param project : project instance
     * @param reqEvents : list of requested events.
     * @return : will generate a XLS file having task task list
     */
    def exportTaskReportExcel(List taskList, String tzId, String userDTFormat, Project project, reqEvents, HttpServletResponse response) {
        File file = grailsApplication.parentContext.getResource("/templates/TaskReport.xls").getFile()

        String eventsTitleSheet = "ALL"
        boolean allEvents = (reqEvents.size() > 1 || reqEvents[0] != "all") ? false : true
        List moveEvents = []
        if (!allEvents) {
            reqEvents = reqEvents.collect { id -> NumberUtil.toLong(id, 0) }
            moveEvents = MoveEvent.findAll("FROM MoveEvent WHERE id IN(:ids)", [ids: reqEvents])
            List eventNames = moveEvents.collect { it.name }
            eventsTitleSheet = eventNames.join(", ")
        }
        Map nameParams = [project: project, moveEvent: moveEvents, allEvents: allEvents]
        String filename = FilenameUtil.buildFilename(FilenameFormat.CLIENT_PROJECT_EVENT_DATE, nameParams, 'xls')

        //set MIME TYPE as Excel
        response.setContentType("application/vnd.ms-excel")
        response.addHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"")


        HSSFWorkbook book = new HSSFWorkbook(new FileInputStream(file))

        HSSFSheet tasksSheet = book.getSheet("tasks")

        List preMoveColumnList = ['taskNumber', 'comment', 'assetEntity', 'assetClass', 'assetId', 'taskDependencies', 'assignedTo', 'instructionsLink', 'role', 'status',
                                  '', '', '', 'notes', 'duration', 'durationLocked', 'durationScale', 'estStart', 'estFinish', 'actStart', 'dateResolved', 'category',
                                  'dueDate', 'dateCreated', 'createdBy', 'moveEvent', 'taskBatchId']

        moveBundleService.issueExport(taskList, preMoveColumnList, tasksSheet, tzId,
                userDTFormat, 3, securityService.viewUnpublished())

        Closure exportTitleSheet = {
            UserLogin userLogin = securityService.getUserLogin()
            HSSFSheet titleSheet = book.getSheet("Title")
            WorkbookUtil.addCell(titleSheet, 1, 2, project.client.toString())
            WorkbookUtil.addCell(titleSheet, 1, 3, project.id.toString())
            WorkbookUtil.addCell(titleSheet, 2, 3, project.name.toString())
            WorkbookUtil.addCell(titleSheet, 1, 4, partyRelationshipService.getProjectManagers(project).toString())
            WorkbookUtil.addCell(titleSheet, 1, 5, eventsTitleSheet)
            WorkbookUtil.addCell(titleSheet, 1, 6, userLogin.person.toString())

            String exportedOn = TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, new Date(), TimeUtil.FORMAT_DATE_TIME_22)
            WorkbookUtil.addCell(titleSheet, 1, 7, exportedOn)
            WorkbookUtil.addCell(titleSheet, 1, 8, tzId)
            WorkbookUtil.addCell(titleSheet, 1, 9, userDTFormat)

            WorkbookUtil.addCell(titleSheet, 30, 0, "Note: All times are in ${tzId ? tzId : 'EDT'} time zone")
        }
        exportTitleSheet()

        book.write(response.getOutputStream())
    }

    /**
     * Generates Application Migration Report web output.
     * @param project : Project, the current user project.
     * @param command : ApplicationMigrationCommand instance with the user's selection.
     * @returns a Map with the model report to be used on the view gsp rendering.
     */
    Map generateApplicationMigration(Project project, ApplicationMigrationCommand command) {
        List<Map> appList = []

        Person currentSme
        if (command.sme != 'null') {
            Long smeId = NumberUtil.toPositiveLong(command.sme)
            currentSme = Person.get(smeId)
        }

        MoveBundle currentBundle
        if (command.moveBundle != 'useForPlanning') {
            Long moveBundleId = NumberUtil.toPositiveLong(command.moveBundle)
            currentBundle = MoveBundle.get(moveBundleId)
            userPreferenceService.setPreference(UserPreferenceEnum.MOVE_BUNDLE, command.moveBundle)
        }

        List<Application> applicationList = Application.where {
            project == project

            if (currentSme) {
                sme == currentSme || sme2 == currentSme
            }

            if (currentBundle) {
                moveBundle == currentBundle
            } else {
                moveBundle in MoveBundle.getUseForPlanningBundlesByProject(project)
            }
        }.list()


        applicationList.each {
            Application application = Application.get(it.id)
            Collection<AssetComment> appComments = application.comments

            List<AssetComment> startComments = appComments.findAll { it.category == command.startCategory }

            List<Date> finishTimeList = appComments.findAll { it.category == command.stopCategory }.sort {
                it.actFinish
            }?.actFinish


            List<Date> beginStartTimeList = []
            List<Date> beginFinishTimeList = []
            startComments.each {
                if(it.actStart != null) {
                    beginStartTimeList.push(it.actStart)
                }
                if (it.actFinish != null && it.status == 'Completed') {
                    beginFinishTimeList.push(it.actFinish)
                }
            }

            Date startTime = beginStartTimeList ? beginStartTimeList[0] : (beginFinishTimeList ? beginFinishTimeList[0] : null)
            Date finishTime = finishTimeList ? finishTimeList[-1] : null

            StringBuilder duration = new StringBuilder()
            def customParam
            String windowColor
            def durationHours

            if (finishTime && startTime) {
                def dayTime = TimeCategory.minus(finishTime, startTime)
                durationHours = (dayTime.days * 24) + dayTime.hours
                if (durationHours) {
                    if (durationHours < 10) {
                        duration.append(0)
                    }
                    duration.append(durationHours)
                }
                if (dayTime.minutes) {
                    duration.append((durationHours ? ':' : '00:') +  (dayTime.minutes > 9 ? "" : "0") + dayTime.minutes)
                }
            }
            if (command.outageWindow == 'drRtoDesc') {
                customParam = application.drRtoDesc ? NumberUtils.toInt((application.drRtoDesc).split(" ")[0]) : ''
                if (duration && customParam) {
                    windowColor = customParam < durationHours ? 'red' : ''
                }
            } else {
                customParam = it[command.outageWindow]
            }

            appList.add(app: application, startTime: TimeUtil.formatDateTime(startTime), finishTime: TimeUtil.formatDateTime(finishTime), duration: duration ?: '',
                    customParam: customParam ? customParam + (command.outageWindow == 'drRtoDesc' ? 'h' : '') : '',
                    windowColor: windowColor)
        }

        [appList: appList, moveBundle: currentBundle, sme: currentSme ?: 'All', project: project]
    }

    /**
	 * Used to generate Application Profiles model
	 * @return list of applications
	 */
	def generateApplicationProfiles(Project project, ApplicationProfilesCommand command) {
		MoveBundle currentBundle
		Person currentSme
		Person applicationOwner

		StringBuilder query = new StringBuilder(""" SELECT a.app_id AS id
			FROM application a
			LEFT OUTER JOIN asset_entity ae ON a.app_id=ae.asset_entity_id
			LEFT OUTER JOIN move_bundle mb ON mb.move_bundle_id=ae.move_bundle_id
			LEFT OUTER JOIN person p ON p.person_id=a.sme_id
			LEFT OUTER JOIN person p1 ON p1.person_id=a.sme2_id
			LEFT OUTER JOIN person p2 ON p2.person_id=ae.app_owner_id
			WHERE ae.project_id = :projectId """)

		Map queryParams = [projectId: project.id]

		if(command.moveBundle == 'useForPlanning'){
			query.append(" AND mb.use_for_planning = true ")
		}else{
			query.append(" AND mb.move_bundle_id= :currentBundle ")
            Long moveBundleId = NumberUtil.toPositiveLong(command.moveBundle)
            MoveBundle moveBundle = GormUtil.findInProject(project, MoveBundle, moveBundleId, true)
			queryParams.currentBundle = moveBundle.id
		}

		if(command.sme!='null'){
			currentSme = Person.get(command.sme)
            if (!currentSme) {
                throw new InvalidParamException("Invalid SME1 given.")
            }
			query.append( "AND (p.person_id = :smeId OR p1.person_id = :sme2Id)")
			queryParams.smeId = currentSme.id
			queryParams.sme2Id = currentSme.id
		}

		if(command.appOwner!='null'){
			query.append(" AND p2.person_id= :appOwner")
            Long ownerId = NumberUtil.toPositiveLong(command.appOwner)
            Person owner = Person.get(ownerId)
            if (!owner) {
                throw new InvalidParamException("Invalid owner id given.")
            }
			queryParams.appOwner = GormUtil.findInProject(project, Person, ownerId, true).id
		}

		int assetCap = 100 // default value
		if(command.reportMaxAssets){
			try{
				assetCap = command.reportMaxAssets.toInteger()
			}catch(Exception e){
				log.info("Invalid value given for assetCap: $assetCap")
			}
		}

		query.append(" LIMIT $assetCap")

		log.info "query = $query"

		List applicationList = namedParameterJdbcTemplate.query(query.toString(), queryParams, new ApplicationProfileRowMapper())

		// TODO: we'd like to flush the session.
		List appList = []
		//TODO:need to write a service method since the code used below is almost similar to application show.
		applicationList.eachWithIndex { app, idx ->
			def assetEntity = AssetEntity.get(app.id)
			Application application = Application.get(app.id)

			// assert assetEntity != null  //TODO: oluna should I add an assertion here?

			def assetComment
			List<AssetDependency> dependentAssets = assetEntity.requiredDependencies()
			List<AssetDependency> supportAssets =  assetEntity.supportedDependencies()
			if (AssetComment.countByAssetEntityAndCommentTypeAndDateResolved(application, 'issue', null)) {
				assetComment = "issue"
			} else if (AssetComment.countByAssetEntity(application)) {
				assetComment = "comment"
			} else {
				assetComment = "blank"
			}
			def prefValue= userPreferenceService.getPreference(UserPreferenceEnum.SHOW_ALL_ASSET_TASKS) ?: 'FALSE'
			def assetCommentList = AssetComment.findAllByAssetEntity(assetEntity)
			def appMoveEvent = AppMoveEvent.findAllByApplication(application)
			def moveEventList = MoveEvent.findAllByProject(project, [sort: 'name'])
			def appMoveEventlist = AppMoveEvent.findAllByApplication(application).value

			//field importance styling for respective validation.
			def validationType = assetEntity.validation

			def shutdownBy = assetEntity.shutdownBy  ? assetEntityService.resolveByName(assetEntity.shutdownBy) : ''
			def startupBy = assetEntity.startupBy  ? assetEntityService.resolveByName(assetEntity.startupBy) : ''
			def testingBy = assetEntity.testingBy  ? assetEntityService.resolveByName(assetEntity.testingBy) : ''

			def shutdownById = shutdownBy instanceof Person ? shutdownBy.id : -1
			def startupById = startupBy instanceof Person ? startupBy.id : -1
			def testingById = testingBy instanceof Person ? testingBy.id : -1

            // Load asset tags.
            def tagAssetList = tagAssetService.list(project, app.id)*.toMap()

			// TODO: we'd like to flush the session
			// GormUtil.flushAndClearSession(idx)
			appList.add([
				app: application, supportAssets: supportAssets, dependentAssets: dependentAssets,
				assetComment: assetComment, assetCommentList: assetCommentList,
				appMoveEvent: appMoveEvent,
				moveEventList: moveEventList,
				appMoveEvent: appMoveEventlist,
				dependencyBundleNumber: AssetDependencyBundle.findByAsset(application)?.dependencyBundle,
				project: project, prefValue: prefValue,
				shutdownById: shutdownById,
				startupById: startupById,
				testingById: testingById,
				shutdownBy: shutdownBy,
				startupBy: startupBy,
				testingBy: testingBy,
                tagAssetList: tagAssetList ? tagAssetList : []
            ])
		}

		Map standardFieldSpecs = customDomainService.standardFieldSpecsByField(project, AssetClass.APPLICATION)
		List customFields = assetEntityService.getCustomFieldsSettings(project, "Application", true)

		[applicationList: appList, moveBundle: currentBundle ?: 'Planning Bundles', sme: currentSme ?: 'All',
		 appOwner: applicationOwner ?: 'All', project: project, standardFieldSpecs: standardFieldSpecs, customs: customFields]
	}

	/**
	 * RowMapper that maps each result from the query for the Event News list
	 * into a map column -> value that can be sent back to the UI.
	 */
	@CompileStatic
	private class ApplicationProfileRowMapper implements RowMapper {
		def mapRow(ResultSet rs, int rowNum) throws SQLException {[
			id: rs.getInt('id'),
		]}
	}

    /**
     * Used to generate project activity metrics excel file.
     */
    @HasPermission(Permission.ReportViewProjectDailyMetrics)
    void generateProjectActivityMetrics(ActivityMetricsCommand command, HttpServletResponse response) {

        Project project = controllerService.getProjectForPage(this)
        if (!project) return

        List projectIds = command.projectIds
        Date startDate
        Date endDate

        boolean validDates = true
        try {
            startDate = TimeUtil.parseDate(command.startDate)
            endDate = TimeUtil.parseDate(command.endDate)
        } catch (e) {
            validDates = false
        }

        if (projectIds && validDates) {
            boolean allProjects = projectIds.find { it == 'all' }
            boolean badProjectIds = false
            List<Project> userProjects = projectService.getUserProjects(securityService.hasPermission(Permission.ProjectShowAll), ProjectStatus.ACTIVE)
            Map<Long, Project> userProjectsMap = [:]
            List<Long> invalidProjectIds = []
            List<Long> allProjectIds = []

            for (Project p in userProjects) {
                userProjectsMap[p.id] = p
                allProjectIds << p.id
            }

            if ( allProjects ) {
                projectIds = allProjectIds
            } else {
                projectIds = projectIds.collect { NumberUtil.toLong(it) }
                // Verify that the user can access the project.
                projectIds.each { id ->
                    if (!userProjectsMap[id]) {
                        invalidProjectIds << id
                        badProjectIds = true
                    }
                }
            }

            //if found any bad id returning to the user
            if( badProjectIds ){
                throw new InvalidParamException("Project ids $invalidProjectIds are not associated with current user.")
            }

            List<Map<String, Object>> activityMetrics = projectService.searchProjectActivityMetrics(projectIds, startDate, endDate)
            exportProjectActivityMetricsExcel(activityMetrics, command.includeNonPlanning, response)

        }
    }

    /**
     * Export task report in XLS format
     * @param activityMetrics: activity metrics
     * @param includeNonPlanning: display or not non planning information
     * @return : will generate a XLS file
     */
    private void exportProjectActivityMetricsExcel(List<Map<String, Object>> activityMetrics, boolean includeNonPlanning, HttpServletResponse response) {
        File file = grailsApplication.parentContext.getResource( "/templates/ActivityMetrics.xls" ).getFile()
        String fileDate = TimeUtil.formatDateTime(TimeUtil.nowGMT(), TimeUtil.FORMAT_DATE_ISO8601)
        String filename = 'ActivityMetrics-' + fileDate + '-Report'

        //set MIME TYPE as Excel
        response.setContentType("application/vnd.ms-excel")
        response.setHeader("Content-Disposition", 'attachment; filename="' + filename + '.xls"')

        def book = new HSSFWorkbook(new FileInputStream( file ))
        def metricsSheet = book.getSheet("metrics")

        def projectNameFont = book.createFont()
        projectNameFont.setFontHeightInPoints((short)12)
        projectNameFont.setFontName("Arial")
        projectNameFont.setBold(true)

        def projectNameCellStyle
        projectNameCellStyle = book.createCellStyle()
        projectNameCellStyle.setFont(projectNameFont)

        def rowNum = 5
        def project_code

        activityMetrics.each { Map<String, Object> am ->

            if (project_code != am['project_code']) {
                rowNum++
                project_code = am['project_code']
                WorkbookUtil.addCell(metricsSheet, 0, rowNum, am['project_code'])
                WorkbookUtil.applyStyleToCell(metricsSheet, 0, rowNum, projectNameCellStyle)
            }

            WorkbookUtil.addCell(metricsSheet, 1, rowNum, TimeUtil.formatDateTime(am['metric_date'], TimeUtil.FORMAT_DATE_TIME_23))
            WorkbookUtil.addCell(metricsSheet, 2, rowNum, 'Planning')
            WorkbookUtil.addCell(metricsSheet, 3, rowNum, am['planning_servers'])
            WorkbookUtil.addCell(metricsSheet, 4, rowNum, am['planning_applications'])
            WorkbookUtil.addCell(metricsSheet, 5, rowNum, am['planning_databases'])
            WorkbookUtil.addCell(metricsSheet, 6, rowNum, am['planning_network_devices'])
            WorkbookUtil.addCell(metricsSheet, 7, rowNum, am['planning_physical_storages'])
            WorkbookUtil.addCell(metricsSheet, 8, rowNum, am['planning_logical_storages'])
            WorkbookUtil.addCell(metricsSheet, 9, rowNum, am['planning_other_devices'])
            WorkbookUtil.addCell(metricsSheet, 10, rowNum, am['dependency_mappings'])
            WorkbookUtil.addCell(metricsSheet, 11, rowNum, am['tasks_all'])
            WorkbookUtil.addCell(metricsSheet, 12, rowNum, am['tasks_done'])
            WorkbookUtil.addCell(metricsSheet, 13, rowNum, am['total_persons'])
            WorkbookUtil.addCell(metricsSheet, 14, rowNum, am['total_user_logins'])
            WorkbookUtil.addCell(metricsSheet, 15, rowNum, am['active_user_logins'])

            rowNum++

            if (includeNonPlanning) {
                WorkbookUtil.addCell(metricsSheet, 2, rowNum, 'Non Planning')
                WorkbookUtil.addCell(metricsSheet, 3, rowNum, am['non_planning_servers'])
                WorkbookUtil.addCell(metricsSheet, 4, rowNum, am['non_planning_applications'])
                WorkbookUtil.addCell(metricsSheet, 5, rowNum, am['non_planning_databases'])
                WorkbookUtil.addCell(metricsSheet, 6, rowNum, am['non_planning_network_devices'])
                WorkbookUtil.addCell(metricsSheet, 7, rowNum, am['non_planning_physical_storages'])
                WorkbookUtil.addCell(metricsSheet, 8, rowNum, am['non_planning_logical_storages'])
                WorkbookUtil.addCell(metricsSheet, 9, rowNum, am['non_planning_other_devices'])
                rowNum++
            }
        }

        book.write(response.getOutputStream())
    }
}
