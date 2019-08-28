package net.transitionmanager.application

import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import net.transitionmanager.asset.Room
import net.transitionmanager.command.ApplicationMigrationCommand
import net.transitionmanager.command.MoveBundleCommand
import net.transitionmanager.command.reports.DatabaseConflictsCommand
import net.transitionmanager.command.reports.ActivityMetricsCommand
import net.transitionmanager.common.ControllerService
import net.transitionmanager.common.CustomDomainService
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.exception.EmptyResultException
import net.transitionmanager.exception.ServiceException
import net.transitionmanager.party.Party
import net.transitionmanager.party.PartyRelationshipService
import net.transitionmanager.person.Person
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveBundleService
import net.transitionmanager.project.MoveBundleStep
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.MoveEventService
import net.transitionmanager.project.Project
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.project.StateEngineService
import net.transitionmanager.project.StepSnapshot
import net.transitionmanager.project.Workflow
import net.transitionmanager.project.WorkflowTransition
import net.transitionmanager.reporting.ReportsService
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.security.Permission
import net.transitionmanager.security.RoleType
import net.transitionmanager.task.AssetComment
import net.transitionmanager.command.reports.ApplicationConflictsCommand


@Secured("isAuthenticated()")
class WsReportsController implements ControllerMethods {

    ReportsService reportsService
    UserPreferenceService userPreferenceService
    MoveBundleService moveBundleService
    MoveEventService moveEventService
    CustomDomainService customDomainService
    ControllerService controllerService
    PartyRelationshipService partyRelationshipService
    StateEngineService stateEngineService

    /**
     * This endpoint receives the moveEvent and return the corresponding data for the
     * Pre-Event CheckList report.
     * @param  moveEvent The id of the event
     * @return  The Pre-Event CheckList report in HTML format.
     */
    def generateCheckList() {
        Project project = getProjectForWs()
        def moveEventId = request.JSON.moveEvent
        if (! moveEventId) {
            throw new InvalidParamException('Invalid value for parameter moveEvent')
        }
        userPreferenceService.setMoveEventId(moveEventId)
        MoveEvent moveEvent = MoveEvent.findByIdAndProject(moveEventId, project )
        render (view: "/reports/generateCheckList", model: reportsService.generatePreMoveCheckList(project.id, moveEvent, request.JSON.viewUnpublished))
    }

    /**
     * This endpoint receives the moveEvent and returns the corresponding Task report.
     *
     * @param  moveEvent The ids of the events, or 'all' for all events.
     * @param wUnresolved  Flag that indicates that only remaining tasks should be included in the report
     * @param viewUnpublished  Flag that indicates if unpublished tasks should be included in the report
     * @param wComment  Flag that indicates if comments should be included in the report
     * @param _action_tasksReport  Indicates the format of the generated report. Values could be 'Generate Xls' or 'Generate Web'.
     * @return  The Task report in HTML or Excel format.
     */
    def tasksReport() {
        def events = request.JSON.moveEvent
        if(events) {
            Project project = getProjectForWs()
            Map model = reportsService.getTasksReportModel(events, project, request.JSON.wUnresolved, request.JSON.viewUnpublished, request.JSON.wComment)
            //Generating XLS Sheet
            switch(request.JSON._action_tasksReport){
                case "Generate Xls" :
                    reportsService.exportTaskReportExcel(model.taskList, model.tzId, model.userDTFormat, project, events, response)
                    break

                case "Generate Web" :
                    render (view : "/reports/tasksReport",
                            model:[taskList : model.taskList, tzId: model.tzId, viewUnpublished: model.vUnpublished,
                                   userDTFormat: model.userDTFormat, tzId: model.tzId])
                    break
            }
        } else{
            throw new InvalidParamException("Please select a Move Event to get the Task Report.")
        }
    }

    /**
     * Get and returns the list of curent Project move bundles to be rendered in a select input.
     * @return List of Move Bundles {id, name, description}
     */
    def moveBundles() {
        Project project = getProjectForWs()
        renderSuccessJson(moveBundleService.moveBundlesByProject(project))
    }

    @HasPermission(Permission.BundleCreate)
    def modelForBundleCreate() {
        Project project = securityService.userCurrentProject
        renderSuccessJson([managers: partyRelationshipService.getProjectStaff(project.id),
         projectInstance: project, workflowCodes: stateEngineService.getWorkflowCode(), rooms: Room.findAllByProject(project)])
    }

    @HasPermission(Permission.BundleEdit)
    def modelForBundleViewEdit(String moveBundleId) {
        MoveBundle moveBundle = MoveBundle.get(NumberUtil.toPositiveLong(moveBundleId))
        if (!moveBundle) {
            flash.message = "MoveBundle not found with id $moveBundleId"
            renderErrorJson()
        }

        stateEngineService.loadWorkflowTransitionsIntoMap(moveBundle.workflowCode, 'project')
        Project project = securityService.userCurrentProject
        def availableMoveEvents = moveEventService.listMoveEvents(project)
        def managers = partyRelationshipService.getProjectStaff(project.id)
        def projectManager = partyRelationshipService.getPartyToRelationship("PROJ_BUNDLE_STAFF", moveBundle, RoleType.CODE_PROJECT_MOVE_BUNDLE, RoleType.CODE_TEAM_PROJ_MGR)
        def moveManager = partyRelationshipService.getPartyToRelationship("PROJ_BUNDLE_STAFF", moveBundle, RoleType.CODE_PROJECT_MOVE_BUNDLE, RoleType.CODE_TEAM_MOVE_MGR)

        //get the all Dashboard Steps that are associated to moveBundle.project
        def allDashboardSteps = moveBundleService.getAllDashboardSteps(moveBundle)

        renderSuccessJson([
                moveBundleInstance: moveBundle,
                moveEvent: [id: moveBundle.moveEvent?.id, name: moveBundle.moveEvent?.toString()],
                availableMoveEvents: availableMoveEvents,
                projectId: project.id,
                managers: managers,
                projectManager: projectManager?.partyIdToId,
                moveManager: moveManager?.partyIdToId,
                dashboardSteps: allDashboardSteps.dashboardSteps?.sort{it["step"].id},
                remainingSteps: allDashboardSteps.remainingSteps,
                workflowCodes: stateEngineService.getWorkflowCode(),
                rooms: Room.findAllByProject(project)
        ])
    }

    @HasPermission(Permission.BundleCreate)
    def saveBundle(Long moveBundleId) {
        Map requestParams = request.JSON
        // SL : 11-2018 : doing this here to avoid command validation errors, it will go away when front-end correctly
        // implement the way dates are sent to backend
        requestParams.startTime = TimeUtil.parseISO8601DateTime(requestParams.startTime) ?: null
        requestParams.completionTime = TimeUtil.parseISO8601DateTime(requestParams.completionTime)

        requestParams.sourceRoom = requestParams.sourceRoom != '0' && requestParams.sourceRoom != 0 ? requestParams.sourceRoom : null
        requestParams.targetRoom = requestParams.targetRoom != '0' && requestParams.targetRoom != 0 ? requestParams.targetRoom : null

        def projectManagerId = NumberUtil.toPositiveLong(requestParams.projectManager)
        def moveManagerId = NumberUtil.toPositiveLong(requestParams.moveManager)

        Project currentUserProject = controllerService.getProjectForPage(this)
        MoveBundleCommand command = populateCommandObject(MoveBundleCommand)
        command.useForPlanning = StringUtil.toBoolean(requestParams.useForPlanning) ?: false
        if (requestParams?.moveEvent?.id) {
            command.moveEvent = GormUtil.findInProject(currentUserProject, MoveEvent, requestParams.moveEvent.id as Long, false)
        }
        command.operationalOrder = NumberUtil.toPositiveLong(requestParams.operationalOrder)
        command.sourceRoom = requestParams.sourceRoom ? GormUtil.findInProject(currentUserProject, Room, requestParams.sourceRoom, false) : null
        command.targetRoom = requestParams.targetRoom ? GormUtil.findInProject(currentUserProject, Room, requestParams.targetRoom, false) : null

        if (command.validate()) {
            try {
                MoveBundle moveBundle
                if(moveBundleId) {
                    moveBundle = moveBundleService.update(moveBundleId, command)
                    stateEngineService.loadWorkflowTransitionsIntoMap(moveBundle.workflowCode, 'project')
                } else {
                    moveBundle = moveBundleService.save(command)
                }

                if (projectManagerId) {
                    partyRelationshipService.savePartyRelationship("PROJ_BUNDLE_STAFF", moveBundle, RoleType.CODE_PROJECT_MOVE_BUNDLE,
                            Party.findById(projectManagerId), RoleType.CODE_TEAM_PROJ_MGR)
                }
                if (moveManagerId) {
                    partyRelationshipService.savePartyRelationship("PROJ_BUNDLE_STAFF", moveBundle, RoleType.CODE_PROJECT_MOVE_BUNDLE,
                            Party.findById(moveManagerId), RoleType.CODE_TEAM_MOVE_MGR)
                }

                flash.message = "MoveBundle $moveBundle created"
                renderSuccessJson(id: moveBundle.id)

            } catch (ServiceException e) {
                flash.message = e.message
            } catch (EmptyResultException e) {
                flash.message = "MoveBundle not found with id $requestParams.id"
            } catch (ValidationException e) {
                flash.message = "Error updating MoveBundle with id $requestParams.id"
            }
        } else {
            flash.message = 'Unable to save MoveBundle due to: ' + GormUtil.allErrorsString(command)
        }

        // in case of error saving new move bundle
        renderErrorJson(flash.message)
    }

    @HasPermission(Permission.BundleDelete)
    def deleteBundle(String moveBundleId) {
        String message = moveBundleService.deleteBundle(MoveBundle.get(moveBundleId),
                securityService.loadUserCurrentProject())
        renderSuccessJson(message: message)
    }

    @HasPermission(Permission.BundleDelete)
    def deleteBundleAndAssets(String moveBundleId) {
        MoveBundle moveBundle = MoveBundle.get(moveBundleId)
        String message
        if (moveBundle) {
            try{
                moveBundleService.deleteBundleAndAssets(moveBundle)
                message = "MoveBundle $moveBundle deleted"
            }
            catch (e) {
                message = "Unable to Delete MoveBundle and Assets: $e.message"
                renderErrorJson(message)
            }
        }
        else {
            message = "MoveBundle not found with id $moveBundleId"
            renderErrorJson(message)
        }
        renderSuccessJson(message)
    }

    def smeList(String moveBundleId) {
        if ( !moveBundleId ) {
            log.warn "moveBundleId param missing"
            throw new InvalidParamException("moveBundleId param missing")
        }
        renderSuccessJson(
                reportsService.getSmeList(moveBundleId, true)
                        .sort({it.lastName})
                        .collect { entry -> [
                            id: entry.id,
                            firstName: entry.firstName,
                            lastName: entry.lastName
                        ]}
        )
    }

    def appOwnerList(String moveBundleId) {
        if ( !moveBundleId ) {
            log.warn "moveBundleId param missing"
            throw new InvalidParamException("moveBundleId param missing")
        }
        renderSuccessJson(
                reportsService.getSmeList(moveBundleId, false)
                        .sort({it.lastName})
                        .collect { entry -> [
                            id: entry.id,
                            firstName: entry.firstName,
                            lastName: entry.lastName
                        ]}
        )
    }

    /**
     * Retunrs the UI options lists for the Application Migration Report.
     * @param moveBundleId: Id of the move bundle.
     * @return Object containing the option lists.
     */
    def applicationMigrationLists(Long moveBundleId) {
        if ( !moveBundleId ) {
            log.warn "moveBundleId param missing"
            throw new InvalidParamException("moveBundleId param missing")
        }
        Project project = getProjectForWs()
        def smeList = reportsService.getSmeList(moveBundleId, true)
        def testingList = WorkflowTransition.findAll(
                'FROM WorkflowTransition where workflow=? order by transId',
                [Workflow.findByProcess(project.workflowCode)])
        List outageList = customDomainService.fieldSpecs(project, AssetClass.APPLICATION.toString(), CustomDomainService.ALL_FIELDS, ["field", "label"])
        def categories = GormUtil.getConstrainedProperties(AssetComment).category.inList.collect { entry -> [
                id: entry,
                text: entry
        ]}
        renderSuccessJson([smeList: smeList.sort{it.lastName}, testingList: testingList, outageList: outageList,  categories: categories ])
    }

    /**
     * Generates the Server Conflicts web output report.
     *
     * @param moveBundle The id of the move bundle selected or 'useForPlanning' value.
     * @param bundleConflicts Flag to enable/disable bundleConflicts on the report
     * @param unresolvedDep Flag to enable/disable unresolvedDependencies on the report
     * @param noRuns Flag to enable/disable noRunsOn on the report
     * @param vmWithNoSupport Flag to enable/disable vmWithNoSupport on the report
     * @param report_max_assets number to limit the number of assets on the report
     * @return The html Server Conflicts report.
     */
    def generateServerConflicts() {
        Project project = getProjectForWs()
        def moveBundleId = (request.JSON.moveBundle).toString()
        int assetCap = request.JSON.report_max_assets ? request.JSON.report_max_assets : 100 // default value
        def view = request.JSON.rows ? "_serverConflicts" : "/reports/generateServerConflicts"

        if( request.JSON.moveBundle == 'useForPlanning' ){
            render (view : view ,
                    model : reportsService.genServerConflicts(project,
                            moveBundleId,
                            request.JSON.bundleConflicts,
                            request.JSON.unresolvedDep,
                            request.JSON.noRuns,
                            request.JSON.vmWithNoSupport,
                            true,
                            params,
                            assetCap))
        }

        def isProjMoveBundle  = MoveBundle.findByIdAndProject( moveBundleId, project )
        if ( !isProjMoveBundle ) {
            log.warn "generateCheckList: User tried to access moveBundle $moveBundleId that was not found in project : $project"
            throw new InvalidParamException("generateCheckList: User tried to access moveBundle $moveBundleId that was not found in project : $project")
        } else {
            userPreferenceService.setPreference(UserPreferenceEnum.MOVE_BUNDLE, moveBundleId)
            render(view: view , model: reportsService.genServerConflicts(project,
                    moveBundleId,
                    request.JSON.bundleConflicts,
                    request.JSON.unresolvedDep,
                    request.JSON.noRuns,
                    request.JSON.vmWithNoSupport,
                    false,
                    params,
                    assetCap))
        }
    }

    /**
     * Generates Application Migration Report web output.
     * @param moveBundle: The id of the move bundle selected value.
     * @param sme: The id of the SME selected value.
     * @param startCategory: string.
     * @param stopCategory: string.
     * @param testing: integer - The id of the Workflow.
     * @param outageWindow: string - The custom field.
     * @returns The rendered gsp view.
     */
    def generateApplicationMigration(Long moveBundleId) {
        Project project = getProjectForWs()
        ApplicationMigrationCommand command = populateCommandObject(ApplicationMigrationCommand)
        Map applicationMigrationMap = reportsService.generateApplicationMigration(project, command)
        render(view: "/reports/generateApplicationMigration" , model: applicationMigrationMap)
    }

    /**
     * Create and return a map containing all the bundles for the user's current project, along
     * with the id of the default bundle for the selector -- either the user preference or the
     * first element in the list of bundles.
     *
     * @return a map with all the bundles for the user's project (id and name) and the default bundle.
     */
    def getMoveBundles() {
        Project project = getProjectForWs()
        List<Map> moveBundles = MoveBundle.findAllByProject(project).collect { MoveBundle bundle ->
            [id: bundle.id, name: bundle.name]
        }
        String moveBundleId = (userPreferenceService.moveBundleId ?: moveBundles[0]?.id).toString()
        renderSuccessJson(moveBundles: moveBundles, moveBundleId: moveBundleId)
    }

    /**
     * Return a list with all the possible App Owners for the given bundle.
     * @param moveBundleId
     */
    def getOwnersForMoveBundle(Long moveBundleId) {
        List<Map> owners = reportsService.getSmeList(moveBundleId, false).collect { Person person ->
            [id: person.id, fullName: person.toString()]
        }
        renderSuccessJson(owners: owners)
    }

     /**
     * Find and return the map with the content for the Application Conflicts report.
     *
     * @return a map with the content for the Application Conflicts Report.
     */
    def getApplicationConflicts() {
        Project project = getProjectForWs()
        ApplicationConflictsCommand command = populateCommandObject(ApplicationConflictsCommand)
        boolean useForPlanning = command.moveBundle == 'useForPlanning'
        if (command.moveBundle && command.moveBundle != 'useForPlanning') {
            Long moveBundleId = NumberUtil.toPositiveLong(command.moveBundle)
            GormUtil.findInProject(project, MoveBundle, moveBundleId, true)
            userPreferenceService.setPreference(UserPreferenceEnum.MOVE_BUNDLE, command.moveBundle)
        }

        Map applicationConflictsMap = reportsService.genApplicationConflicts(project.id, command.moveBundle, command.bundleConflicts,
            command.unresolvedDependencies, command.missingDependencies, useForPlanning, command.appOwner, command.maxAssets)

        renderSuccessJson(applicationConflictsMap)

    }

    /**
     * Fetch and return the information for the Database Conflicts Report.
     * @return a map with the information for populating the report.
     */
    def getDatabaseConflicts() {
        Project project = getProjectForWs()
        DatabaseConflictsCommand command = populateCommandObject(DatabaseConflictsCommand)
        if (command.moveBundle.isNumber()) {
            MoveBundle moveBundle = fetchDomain(MoveBundle, [id: command.moveBundle], project)
            if (moveBundle) {
                userPreferenceService.setPreference(UserPreferenceEnum.MOVE_BUNDLE, command.moveBundle)
            }
        }
        renderSuccessJson(reportsService.generateDatabaseConflictsMap(project, command))
    }

    /**
     * Generates Application Profiles Report web output.
     * @param moveBundle: The id of the move bundle selected value.
     * @param sme: The id of the SME selected value.
     * @param appOwner: The id of the SME selected value.
     * @returns The rendered gsp view.
     */
    def generateApplicationProfiles() {
        Project project = getProjectForWs()
        ApplicationProfilesCommand command = populateCommandObject(ApplicationProfilesCommand)
        Map model = reportsService.generateApplicationProfiles(project, command)
        render(view: "/reports/generateApplicationProfiles" , model: model)
    }

    /**
     * Returns the options lists of the Project Metrics Report.
     * @return Returns the options lists of the Project Metrics Report.
     */
    def projectMetricsLists() {
        List<Project> userProjects = projectService.getUserProjects(securityService.hasPermission(Permission.ProjectShowAll), ProjectStatus.ACTIVE)
        Calendar start = Calendar.instance
        start.set(Calendar.DATE, 1)
        start.add(Calendar.MONTH, -2)
        Date startDate = start.time
        Date endDate = new Date()
        renderSuccessJson(projects: userProjects.collect { entry -> [
                id: entry.id,
                name: entry.name
        ]}, startDate: startDate, endDate: endDate)
    }

    /**
     * Generates and returns the Project Activity Metrics report excel file.
     * @param reportIds: The list of project ids.
     * @param startDate: The start date range.
     * @param endDate: The end date range.
     * @param includeNonPlanning: Include NonPlanning flag.
     * @returns The rendered gsp view.
     */
    def generateProjectMetrics() {
        ActivityMetricsCommand command = populateCommandObject(ActivityMetricsCommand)
        reportsService.generateProjectActivityMetrics(command, response)
    }
}
