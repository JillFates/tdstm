package net.transitionmanager.application

import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.command.ApplicationMigrationCommand
import net.transitionmanager.command.reports.ActivityMetricsCommand
import net.transitionmanager.common.CustomDomainService
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.person.Person
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveBundleService
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.Project
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.project.Workflow
import net.transitionmanager.project.WorkflowTransition
import net.transitionmanager.reporting.ReportsService
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.security.Permission
import net.transitionmanager.task.AssetComment
import net.transitionmanager.command.reports.ApplicationConflictsCommand


@Secured("isAuthenticated()")
class WsReportsController implements ControllerMethods {

    ReportsService reportsService
    UserPreferenceService userPreferenceService
    MoveBundleService moveBundleService
    CustomDomainService customDomainService

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
        render (view: "/reports/generateCheckList", model: reportsService.generatePreMoveCheckList(project.id, moveEvent))
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

    def smeList(Long moveBundleId) {
        if ( !moveBundleId ) {
            log.warn "moveBundleId param missing"
            throw new InvalidParamException("moveBundleId param missing")
        }
        renderSuccessJson(reportsService.getSmeList(moveBundleId, true).sort({it.lastName}))
    }

    def appOwnerList(Long moveBundleId) {
        if ( !moveBundleId ) {
            log.warn "moveBundleId param missing"
            throw new InvalidParamException("moveBundleId param missing")
        }
        renderSuccessJson(reportsService.getSmeList(moveBundleId, false).sort({it.lastName}))
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
