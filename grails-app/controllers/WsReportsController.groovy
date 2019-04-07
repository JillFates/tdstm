import com.tdsops.tm.enums.domain.UserPreferenceEnum
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Project
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.MoveEventService
import net.transitionmanager.service.ReportsService
import net.transitionmanager.service.UserPreferenceService


@Secured("isAuthenticated()")
class WsReportsController implements ControllerMethods {

    ReportsService reportsService
    UserPreferenceService userPreferenceService
    MoveEventService moveEventService

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

    def moveBundles() {
        Project project = getProjectForWs()
        List<MoveBundle> moveBundles = MoveBundle.findAllByProject(project)
        renderSuccessJson(moveBundles.collect { MoveBundle entry -> [
                id: entry.id,
                name: entry.name,
                description: entry.description,
        ]})
    }

    def generateServerConflicts() {
        Project project = getProjectForWs()

        def moveBundleId = (request.JSON.moveBundle).toString()
        def bundleConflicts = request.JSON.bundleConflicts
        def unresolvedDependencies = request.JSON.unresolvedDep
        def noRunsOn = request.JSON.noRuns
        def vmWithNoSupport = request.JSON.vmWithNoSupport
        def view = request.JSON.rows ? "_serverConflicts" : "/reports/generateServerConflicts"

        int assetCap = request.JSON.report_max_assets ? request.JSON.report_max_assets : 100 // default value

        if( request.JSON.moveBundle == 'useForPlanning' ){
            render (view : view ,
                    model : reportsService.genServerConflicts(project, moveBundleId, bundleConflicts,
                            unresolvedDependencies, noRunsOn, vmWithNoSupport, true, params, assetCap))
        }

        def isProjMoveBundle  = MoveBundle.findByIdAndProject( moveBundleId, project )
        if ( !isProjMoveBundle ) {
            log.warn "generateCheckList: User tried to access moveBundle $moveBundleId that was not found in project : $project"
            throw new InvalidParamException("generateCheckList: User tried to access moveBundle $moveBundleId that was not found in project : $project")
        } else {
            userPreferenceService.setPreference(UserPreferenceEnum.MOVE_BUNDLE, moveBundleId)
            render(view: view , model: reportsService.genServerConflicts(project, moveBundleId, bundleConflicts, unresolvedDependencies,
                    noRunsOn, vmWithNoSupport, false, params, assetCap))
        }
    }
}
