import com.tdsops.tm.enums.domain.UserPreferenceEnum
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.command.reports.ApplicationConflictsCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Project
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.ReportsService
import net.transitionmanager.service.UserPreferenceService


@Secured("isAuthenticated()")
class WsReportsController implements ControllerMethods {

    ReportsService reportsService
    UserPreferenceService userPreferenceService

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
     * Find and return the map with the content for the Application Conflicts report.
     *
     * @return a map with the content for the Application Conflicts Report.
     */
    def getApplicationConflicts() {
        Project project = getProjectForWs()
        ApplicationConflictsCommand command = populateCommandObject(ApplicationConflictsCommand)
        boolean useForPlanning = command.moveBundle == 'useForPlanning'
        if (!useForPlanning) {
            Long moveBundleId = NumberUtil.toPositiveLong(command.moveBundle)
            GormUtil.findInProject(project, MoveBundle, moveBundleId, true)
            userPreferenceService.setPreference(UserPreferenceEnum.MOVE_BUNDLE, command.moveBundle)
        }

        Map applicationConflictsMap = reportsService.genApplicationConflicts(project.id, command.moveBundle, command.bundleConflicts,
            command.unresolvedDependencies, command.missingDependencies, useForPlanning, command.appOwner, command.maxAssets)

        renderSuccessJson(applicationConflictsMap)

    }
}