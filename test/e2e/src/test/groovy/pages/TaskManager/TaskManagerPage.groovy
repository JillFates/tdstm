package pages.TaskManager

import geb.Page

class TaskManagerPage extends Page{

    static at = {
        headerTitle.text() == "Task Manager"
        justRemainingCB.value() == "on"
        justMyTasksCB.value() != "on"
        viewUnpublishedCB.value() != "on"
    }

    static content = {

        //menu text to determine Event
        projEventUser               { $("a#nav-project-name")}

        // START Layout elements
        headerTitle                 { $("section", class:"content-header").find("h1") }
        justRemainingCB             { $("input#justRemainingCB") }
        justMyTasksCB               { $("input#justMyTasksCB")}
        viewUnpublishedCB           { $("input#viewUnpublishedCB")}
        createTaskBtLb              { $("#createtask_text_createTask")}
        taskTColLb                  { $("#jqgh_taskNumber")}
        firstElementTaskTbl         { $("#taskListIdGrid").$("tr")[1].$("td")[0].find("a")}
        descriptionTColFlt          { $("#gs_comment")}
        firstElementDesc            { $("#taskListIdGrid").$("tr")[1].$("td")[2]}
        detailButton                { $("a", "ng-click":"doAction(button)").find("span",class:"ng-binding task_button")}
    }
}


