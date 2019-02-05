package pages.Tasks.TaskManager

import geb.Page
import modules.TasksMenuModule

class TaskManagerPage extends Page{

    static at = {
        tmHeaderTitle.text() == "Task Manager"
        tmJustRemainingCB.value() == "on"
        tmJustMyTasksCB.value() != "on"
    }

    static content = {
        //menu text to determine Event
        projEventUser               { $("a#nav-project-name")}

        // START Layout elements
        tmHeaderTitle { $("section", class:"content-header").find("h1")}
        tmJustRemainingCB { $("input#justRemainingCB")}
        tmJustMyTasksCB { $("input#justMyTasksCB")}
        tmViewUnpublishedCB { $("input#viewUnpublishedCB")}
        tmCreateTaskBtn { $("#createtask_text_createTask")}
        tmTaskTColLb  { $("#jqgh_taskNumber")}
        tmFirstElementTaskTbl { $("#taskListIdGrid").$("tr")[1].$("td")[0].find("a")}
        tmDescriptionTColFlt { $("#gs_comment")}
        tmFirstElementDesc { $("#taskListIdGrid").$("tr")[1].$("td")[2]}
        tmFirstElementStatus { $("#taskListIdGrid").$("tr")[1].$("td")[7].find("span").text()}
        tmStatusButtonBar { $("span#actionBarId")}
        tmTaskDoneBtn (wait:true, required:false)   { $("a", "ng-click":"doAction(button)").find("span",text:"Done")}
        tmTaskDetailBtn (wait:true, required:false)   { $("a", "ng-click":"doAction(button)").find("span",text:"Details...")}
        tmTaskAssignMeBtn (wait:true, required:false) { $("a", "ng-click":"doAction(button)").find("span",text:"Assign To Me")}
        tasksModule { module TasksMenuModule}
        moveEvent {$("#moveEventId")}
    }

    def selectEvent(evt){
        moveEvent = evt
    }

    def clickCreateTask(){
        waitFor{tmCreateTaskBtn.click()}
    }
}


