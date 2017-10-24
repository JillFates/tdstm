package pages.TaskManager

import geb.Page

class TaskManagerPage extends Page{

    static at = {
        headerTitle.text() == "Task Manager"
        justRemainingCB.value() == "on"
        justMyTasksCB.value() != "on"
        viewUnpublishedCB.value() != "on"
        //breadcrumb.text() == "Task"::before"TaskManager"
    }

    static content = {

        //menu text to determine Event
        projEventUser               { $("a#nav-project-name")}
        // eventUserTxt = projEventUser.getText().substr(projEventUser.getText().indexOf(":") + 1)}

        // START Layout elements
        headerTitle                 { $("section",     class:"content-header").find("h1") }
        //breadcrumb(wait:true)       { $("section",     class:"content-header").find("o1", class:"breadcrumb") }
        justRemainingCB             { $("input#justRemainingCB") }
        justMyTasksCB               { $("input#justMyTasksCB")}
        viewUnpublishedCB           { $("input#viewUnpublishedCB")}
        createTaskBtLb              { $("#createtask_text_createTask")}
        taskTColLb                  { $("#jqgh_taskNumber")}
        firstElementTaskTbl         { $("#taskListIdGrid").$("tr")[1].$("td")[0].find("a")}
        descriptionTColFlt          { $("#gs_comment")}
        firstElementDesc            { $("#taskListIdGrid").$("tr")[1].$("td")[2]}
        //firstElementTaskTbl          { $("#table")}




        //viewTaskGraphBt             { $("#viewtaskgraph_button_graph")}
        //moveEventDD                 { $("#moveEventId")}
        //moveEventDDOptions          { $("#moveEventId option")}
        //moveEventDDSelected         { $("#moveEventId option:checked")}
        //eventLb                     { $("label" , "for":"lbl-task-event-title").text()}
        //bulkEditBtLb                { $("#bulkedit_text_bulkEdit")}
        //actionTColLb                { $("#jqgh_act")}
        //taskTCol                    { $("#taskListIdGrid_taskNumber")}
        //descriptionTColLb           { $("#jqgh_comment")}
        //updatedTColLb               { $("#jqgh_updated")}
        //dueDateTColLb               { $("#jqgh_dueDate")}
        //paginationDD                { $("select" , "class":"ui-pg-selbox")}
        //paginationInfo              { $("div" , "class":"ui-paging-info")}



/*
        ///menu text to determine Event
        projEventUser { $("div#nav-project-name")}
        // eventUserTxt = projEventUser.getText().substr(projEventUser.getText().indexOf(":") + 1)}

        // START Layout elements
        //headerTitle(wait:true) { $("section", 	class:"content-header").find("h1") }
        //controlRowElements { $("#controlRowId")}
        //controlRow	{ $("#controlRowId")}
        //eventLb { $("for","lbl-task-event-title")}
        //moveEventDD { $("#moveEventId")}
        //moveEventDDOptions { $("#moveEventId option")}
        //moveEventDDSelected { $("#moveEventId option:checked")}
       // breadCrumb { $("div", class:"breadcrumb")}
        //justRemainingCB { $("#justRemainingCB")}
        justRemainingLb { $("for","justRemainingCB")}
        //justMyTasksCB { $("#justMyTasksCB")}
        justMyTasksLb { $("for","justMyTasksCB")}
        //viewUnpublishedCB { $("#viewUnpublishedCB")}
        viewUnpublishedLb { $("for","viewUnpublishedCB")}
        viewTaskGraphBt { $("#viewtaskgraph_button_graph")}
        viewTaskGraphBtLb { $("#viewtaskgraph_text_graph")}
        viewTaskGraphBtImg {$("ng-show":"icon != ")}
        viewTimeLineBt { $("#viewtimeline_button_timeline")}
        viewTimeLineBtLb { $("#viewtimeline_text_timeline")}
        viewTimeLineBtImg { $("ng-show":"icon != ")}
        timeRefreshBt { $("div", class:"col-xs-4 item-wrapper refresh-button")}
        selectTimedBarDD { $("#selectTimedBarId")}
        selectTimedBarDDOptions { $("#selectTimedBarId option")}
        selectTimedBarDDSelected { $("#selectTimedBarId option:checked")}
        // Action Buttons
        tasksLb { $("for","lbl-task-list-title")}
        createTaskBt { $("#createtask_button_createTask")}
        //createTaskBtLb { $("#createtask_text_createTask")}
        bulkEditBt { $("#bulkedit_button_bulkEdit")}
        //bulkEditBtLb { $("#bulkedit_text_bulkEdit")}
        clearFiltersBt { $("#clearfilters_button_clearFilters")}
        clearFiltersBtLb { $("#clearfilters_text_clearFilters")}
        tableToggleBt { $("div", class:"ui-jqgrid-titlebar-close HeaderButton")}
        // Table Header
        actionTCol { $("#taskListIdGrid_act")}
        //actionTColLb { $("#jqgh_act")}
        //taskTCol { $("#taskListIdGrid_taskNumber")}
        taskTColLb { $("#jqgh_taskNumber")}
        taskTColFlt { $("#gs_taskNumber")}
        descriptionTCol { $("#taskListIdGrid_comment")}
        //descriptionTColLb { $("#jqgh_comment")}
        descriptionTColFlt { $("#gs_comment")}
        updatedTCol { $("#taskListIdGrid_updated")}
        //updatedTColLb { $("#jqgh_updated")}
        dueDateTCol { $("#taskListIdGrid_dueDate")}
        //dueDateTColLb { $("#jqgh_dueDate")}
        dueDateTColFlt { $("#gs_dueDate")}
        statusTCol { $("#taskListIdGrid_status")}
        statusTColLb { $("#jqgh_status")}
        statusTColFlt { $("#gs_status")}
        sucTCol { $("#taskListIdGrid_suc")}
        sucTColLb { $("#jqgh_suc")}
        scoreTCol { $("#taskListIdGrid_score	")}
        scoreTColLb { $("#jqgh_score")}
        customColumnTCol { $("#taskListIdGrid_custom")} // TODO Check and obtain custom columns for validate them
        customColumnTColLb  { $("#jqgh_custom")}// TODO Check and obtain custom columns for validate them
        customColumnTColFlt { $("#gs_custom")} // TODO Check and obtain custom columns for validate them
        //Table Footer
        tableRefreshBt { $("#taskListId")}
        //-END of Layout Elements

        //-START Functionality

        // Error Modal window
        errorModal { $("#errorModal")}
        errorModalText {$("#errorModalText")}
        errorModalCloseBtn {$("div", class:"btn btn-default")}
        // End of error modal window

        // Create Task Modal Window
        ctModal { $("div",class:"modal fade modal-task in")}
        ctModalTitle {$("#ui-id-5")}
        ctModalNameLb {$("#commentEditTdId")}
        ctModalNameErrMsg {$("div",class:"error-msg")}
        ctModalNameTA {$("#commentEditId")}
        ctModalPersTeamLb {$("for","assignedToEditTdId")}
        ctModalPersDD {$("#assignedTo")}
        ctModalPersDDOptions {$("#assignedTo option")}
        ctModalPersDDSelected {$("#assignedTo option:checked")}
        ctModalTeamDD {$("#roleType")}
        ctModalTeamDDOptions {$("#roleType option")}
        ctModalTeamDDSelected {$("#roleType option:checked")}
        ctModalFixAssgCB {$("#hardAssignedEdit")}
        ctModalFixAssgLb {$("for","hardAssignedEdit")}
        ctModalSendNotifCB {$("#sendNotificationEdit")}
        ctModalSendNotifLb {$("for","sendNotificationEdit")}
        ctModalEventLb {$("for","moveEvent")}
        ctModalEventDD {$("#moveEvent")}
        ctModalEventDDOptions {$("#moveEvent option")}
        ctModalEventDDSelected {$("#moveEvent option:checked")}
        ctModalCategoryLb {$("for","category")}
        ctModalCategoryDD {$("#category")}
        ctModalCategoryDDOptions {$("#category option")}
        ctModalCategoryDDSelected {$("#category option:checked")}
        ctModalAssetLb {$("for","asset")}
        ctModalAssetTypeDD {$("ng-model", "commentInfo.currentAssetClass")}
        ctModalAssetTypeDDOptions {$("ng-model","commentInfo.currentAssetClass option")}
        ctModalAssetTypeDDSelected {$("ng-model","commentInfo.currentAssetClass option:checked")}
        ctModalAssetNameDD {$("#s2id_currentAsset")}
        ctModalAssetNameDDOptions {$("#s2id_currentAsset option")}
        ctModalAssetNameDDSelected {$("#s2id_currentAsset option:checked")}
        ctModalInstLinkLb {$("for","instructionsLink")}
        ctModalInstLinkTF {$("#instructionsLinkId")}
        ctModalPriorityLb {$("for","priority")}
        ctModalPriorityDD {$("#priority")}
        ctModalPriorityDDOptions {$("#priority option")}
        ctModalPriorityDDSelected {$("#priority option:checked")}
        ctModalDueDateLb {$("for","dueDateEditSpanId")}
        ctModalDueDateTF {$("#dueDate")}
        ctModalDueDateCal {$("div",class:"k-icon k-i-calendar")}
        ctModalEstDurLb {$("for","durationEditId")}
        ctModalEstDurDaysTF {$("ng-model","durationpicker.day")}
        ctModalEstDurDaysLb {$("div",class:"duration_days duration_label")}
        ctModalEstDurHourTF {$("ng-model","durationpicker.hour")}
        ctModalEstDurHourLb {$("div",class:"duration_hours duration_label")}
        ctModalEstDurMinTF {$("ng-model","durationpicker.minutes")}
        ctModalEstDurMinLb {$("div",class:"duration_minutes duration_label")}
        ctModalEstDurLock {$("ng-if","!ac.durationLocked")}
        ctModalEstStFinLb {$("for","estStartEditTrId")}
        ctModalEstStFinTF {$("#estRange")}
        ctModalStatusLb {$("for","status")}
        ctModalStatusDD {$("#status")}
        ctModalDepLb {$("for","predecessorHeadTrId")}
        //ctModalDepPredLb {$("for","predecessors")}
        //ctModalDepPredBt {$("ng-click="$broadcast("addDependency","predecessor")")}
        //ctModalDepPredType {$(")}
        //ctModalDepPredTsk {$(")}
        //ctModalDepPredDel {$("ng-click="deleteRow($index)")}
        //ctModalDepSuccLb {$("for","successors")}
        //ctModalDepSuccBt {$("ng-click="$broadcast("addDependency","successor")")}
        //ctModalDepSuccType {$(")}
        //ctModalDepSuccTsk {$(")}
        //ctModalDepSuccTsk {$("ng-click="deleteRow($index)")}
        ctModalButtons { $(".buttons button")}
        //ctModalSaveBt { $("#saveAndCloseBId")}
        //ctModalCancelBt { $("div",class:"btn btn-default tablesave cancel")}

*/
    }
}


