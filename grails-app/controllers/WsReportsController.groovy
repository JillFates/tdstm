import com.tds.asset.AssetComment
import com.tdsops.tm.enums.FilenameFormat
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import com.tdssrc.grails.FilenameUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WorkbookUtil
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.service.*
import org.apache.commons.lang3.math.NumberUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook

@Secured("isAuthenticated()")
class WsReportsController implements ControllerMethods {

    ReportsService reportsService
    UserPreferenceService userPreferenceService
    MoveEventService moveEventService
    MoveBundleService moveBundleService
    PartyRelationshipService partyRelationshipService

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
     * This endpoint receives the moveEvent and return the corresponding data for the
     * Task report.
     * @param  moveEvent The id of the event, or 'all' for all events.
     * @return  The Task report in HTML format.
     */
    def tasksReport() {
        def events = request.JSON.moveEvent
        String tzId = userPreferenceService.timeZone
        String userDTFormat = userPreferenceService.dateFormat

        if(events) {
            Project project = getProjectForWs()
            boolean allBundles = events.find { it == 'all' }
            def badReqEventIds

            if( !allBundles ){
                events = events.collect {id-> NumberUtils.toDouble(id, 0).round() }
                //Verifying events id are in same project or not.
                badReqEventIds = moveEventService.verifyEventsByProject(events, project)
            }
            //if found any bad id returning to the user
            if( badReqEventIds ){
                throw new InvalidParamException("Event ids $badReqEventIds is not associated with current project.")
            }

            def argMap = [type: AssetCommentType.ISSUE, project: project]
            def taskListHql = "FROM AssetComment WHERE project =:project AND commentType =:type "

            if(!allBundles){
                taskListHql +=" AND moveEvent.id IN (:events) "
                argMap.events = events
            }

            if( request.JSON.wUnresolved ){
                taskListHql += "AND status != :status"
                argMap.status = AssetCommentStatus.COMPLETED
            }

            // handle unpublished tasks
            userPreferenceService.setPreference(UserPreferenceEnum.VIEW_UNPUBLISHED, request.JSON.viewUnpublished as Boolean)

            boolean viewUnpublished = securityService.hasPermission(Permission.TaskPublish) && request.JSON.viewUnpublished
            if (!viewUnpublished) {
                taskListHql += " AND isPublished = :isPublished "
                argMap.isPublished = true
            }

            List taskList = AssetComment.findAll(taskListHql, argMap)
            if (viewUnpublished) {
                taskList.addAll(request.JSON.wComment ? AssetComment.findAllByCommentTypeAndProject(AssetCommentType.COMMENT, project) : [])
            }
            else {
                taskList.addAll(request.JSON.wComment ? AssetComment.findAllByCommentTypeAndProjectAndIsPublished(AssetCommentType.COMMENT, project, true) : [])
            }

            //Generating XLS Sheet
            switch(request.JSON._action_tasksReport){
                case "Generate Xls" :
                    exportTaskReportExcel(taskList, tzId, userDTFormat, project, events)
                    break

                case "Generate Web" :
                    render (view : "/reports/tasksReport",
                            model:[taskList : taskList, tzId:tzId, viewUnpublished:viewUnpublished,
                                   userDTFormat:userDTFormat, tzId:tzId])
                    break
            }
        } else{
            throw new InvalidParamException("Please select a Move Event to get the Task Report.")
        }
    }

    /**
     * Export task report in XLS format
     * @param taskList : list of tasks
     * @param tzId : timezone
     * @param project : project instance
     * @param reqEvents : list of requested events.
     * @return : will generate a XLS file having task task list
     */
    def exportTaskReportExcel(taskList, tzId, userDTFormat, project, reqEvents){
        File file = grailsApplication.parentContext.getResource( "/templates/TaskReport.xls" ).getFile()

        def currDate = TimeUtil.nowGMT()
        String eventsTitleSheet = "ALL"
        boolean allEvents = (reqEvents.size() > 1 || reqEvents[0] != "all") ? false : true
        def moveEvents = []
        if(!allEvents){
            moveEvents = MoveEvent.findAll("FROM MoveEvent WHERE id IN(:ids)", [ids: reqEvents])
            def eventNames = moveEvents.collect{it.name}
            eventsTitleSheet = eventNames.join(", ")
        }
        def nameParams = [project:project, moveEvent: moveEvents, allEvents: allEvents]
        String filename = FilenameUtil.buildFilename(FilenameFormat.CLIENT_PROJECT_EVENT_DATE, nameParams, 'xls')

        //set MIME TYPE as Excel
        response.setContentType( "application/vnd.ms-excel" )
        setHeaderContentDisposition(filename)


        def book = new HSSFWorkbook(new FileInputStream( file ))

        def tasksSheet = book.getSheet("tasks")

        def preMoveColumnList = ['taskNumber', 'comment', 'assetEntity', 'assetClass', 'assetId', 'taskDependencies', 'assignedTo', 'instructionsLink', 'role', 'status',
                                 '','','', 'notes', 'duration', 'durationLocked', 'durationScale', 'estStart','estFinish','actStart', 'dateResolved', 'workflow', 'category',
                                 'dueDate', 'dateCreated', 'createdBy', 'moveEvent', 'taskBatchId']

        moveBundleService.issueExport(taskList, preMoveColumnList, tasksSheet, tzId,
                userDTFormat, 3, securityService.viewUnpublished())

        def exportTitleSheet = {
            def userLogin = securityService.getUserLogin()
            def titleSheet = book.getSheet("Title")
            WorkbookUtil.addCell(titleSheet, 1, 2, project.client.toString())
            WorkbookUtil.addCell(titleSheet, 1, 3, project.id.toString())
            WorkbookUtil.addCell(titleSheet, 2, 3, project.name.toString())
            WorkbookUtil.addCell(titleSheet, 1, 4, partyRelationshipService.getProjectManagers(project).toString())
            WorkbookUtil.addCell(titleSheet, 1, 5, eventsTitleSheet)
            WorkbookUtil.addCell(titleSheet, 1, 6, userLogin.person.toString())

            def exportedOn = TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, new Date(), TimeUtil.FORMAT_DATE_TIME_22)
            WorkbookUtil.addCell(titleSheet, 1, 7, exportedOn)
            WorkbookUtil.addCell(titleSheet, 1, 8, tzId)
            WorkbookUtil.addCell(titleSheet, 1, 9, userDTFormat)

            WorkbookUtil.addCell(titleSheet, 30, 0, "Note: All times are in ${tzId ? tzId : 'EDT'} time zone")
        }
        exportTitleSheet()

        book.write(response.getOutputStream())
    }
}
