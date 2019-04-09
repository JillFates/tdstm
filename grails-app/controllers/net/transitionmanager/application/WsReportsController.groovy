package net.transitionmanager.application

import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.Project
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.reporting.ReportsService
import net.transitionmanager.person.UserPreferenceService


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
}
