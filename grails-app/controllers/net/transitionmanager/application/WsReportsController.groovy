package net.transitionmanager.application

import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.Project
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
}