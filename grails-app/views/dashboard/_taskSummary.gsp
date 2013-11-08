<script type="text/javascript">
$(document).ready(function() {
    var percentageTaskDone=${taskStatusMap['Completed'].taskCount ? Math.round((taskStatusMap['Completed'].taskCount/taskCountByEvent)*100) :0};
    $("#tasksDoneBar").animate({width: percentageTaskDone+"%" }, 1000);
    
    var percentageTaskStarted=${taskStatusMap['Started'].taskCount ? Math.round((taskStatusMap['Started'].taskCount/taskCountByEvent)*100) :0};
    $("#tasksStartBar").animate({width: percentageTaskStarted+"%" }, 1000);
    
    var percentageTaskReady=${taskStatusMap['Ready'].taskCount ? Math.round((taskStatusMap['Ready'].taskCount/taskCountByEvent)*100) :0};
    $("#tasksReadyBar").animate({width: percentageTaskReady+"%" }, 1000);
    
    var percentageDurationCompleted=${taskStatusMap['Completed'].timeInMin ? Math.round((taskStatusMap['Completed'].timeInMin/totalDuration)*100) :0};
    $("#effortDoneBar").animate({width: percentageDurationCompleted+"%" }, 1000);
    
    var percentageDurationStarted=${taskStatusMap['Started'].timeInMin ? Math.round((taskStatusMap['Started'].timeInMin/totalDuration)*100) :0};
    $("#effortStartBar").animate({width: percentageDurationStarted+"%" }, 1000);
    
    var percentageDurationReady=${taskStatusMap['Ready'].timeInMin ? Math.round((taskStatusMap['Ready'].timeInMin/totalDuration)*100) :0};
    $("#effortReadyBar").animate({width: percentageDurationReady+"%" }, 1000);
});
</script>
<g:set var="percentageTaskDone" value="${taskStatusMap['Completed'].taskCount ? Math.round((taskStatusMap['Completed'].taskCount/taskCountByEvent)*100) :0}" />
<g:set var="percentageTaskStarted" value="${taskStatusMap['Started'].taskCount ? Math.round((taskStatusMap['Started'].taskCount/taskCountByEvent)*100) :0}" />
<g:set var="percentageTaskReady" value="${taskStatusMap['Ready'].taskCount ? Math.round((taskStatusMap['Ready'].taskCount/taskCountByEvent)*100) :0}" />
<g:set var="percentageDurationDone" value="${taskStatusMap['Completed'].timeInMin ? Math.round((taskStatusMap['Completed'].timeInMin/totalDuration)*100) :0}" />
<g:set var="percentageDurationStarted" value="${taskStatusMap['Started'].timeInMin ? Math.round((taskStatusMap['Started'].timeInMin/totalDuration)*100) :0}" />
<g:set var="percentageDurationReady" value="${taskStatusMap['Ready'].timeInMin ? Math.round((taskStatusMap['Ready'].timeInMin/totalDuration)*100) :0}" />

<div class="toprightcontent">
<div class="taskSummaryDiv">
    <h3>Task Summary </h3><br>
    <span class="taskCountSpan">${taskCountByEvent-taskStatusMap['Completed'].taskCount}</span><br><br>
    <h5>Remaining</h5>
</div>
</div>
<div class="toprightcontent">
<div class="taskDetailsDiv">
        <div class="task_done taskSummaryCounts"><b>Done: ${taskStatusMap['Completed'].taskCount} (${taskStatusMap['Completed'].timeInMin}m)</b></div>
        <div class="task_started taskSummaryCounts"><b>Started: ${taskStatusMap['Started'].taskCount} (${taskStatusMap['Started'].timeInMin}m)</b></div>
        <div class="task_ready taskSummaryCounts"><b>Ready: ${taskStatusMap['Ready'].taskCount} (${taskStatusMap['Ready'].timeInMin}m)</b></div>
        <div class="taskSummaryCounts" style="border:1px solid black;"><b>Pending: ${taskStatusMap['Pending'].taskCount} (${taskStatusMap['Pending'].timeInMin}m)</b></div>
</div>
</div>
<div class="toprightcontent">
    <table class="task_bar_table">
         <tr>
          <td class="task_bar_label">
           Tasks
          </td>
          <td class="task_bar_base">
           <div class="task_done task_bar_graph" id="tasksDoneBar" style="width:0%;"></div>
           <div class="task_started task_bar_graph" id="tasksStartBar" style="width:0%;"></div>
           <div class="task_ready task_bar_graph" id="tasksReadyBar" style="width:0%;"></div>
           <div class="prog_bar_text" id="taskDoneText">${(percentageTaskDone<10)? '' : percentageTaskDone+'%'}</div>
          </td>
         </tr>
         <tr>
          <td class="task_bar_label">
           Effort
          </td>
          <td class="task_bar_base">
           <div class="task_done task_bar_graph" id="effortDoneBar" style="width:0%;"></div>
           <div class="task_started task_bar_graph" id="effortStartBar" style="width:0%;"></div>
           <div class="task_ready task_bar_graph" id="effortReadyBar" style="width:0%;"></div>
           <div class="prog_bar_text" id="effortDoneText">${(percentageDurationDone<10)? '' : percentageDurationDone+'%'}</div>
          </td>
         </tr>
    </table>
</div>
