<script type="text/javascript">
$(document).ready(function() {
    var percentageTaskDone=${taskStatusMap['Completed'].taskCount ? ((taskStatusMap['Completed'].taskCount/taskCountByEvent)*100).intValue() :0};
    $("#tasksDoneBar").animate({width: percentageTaskDone+"%" }, 1000);
    
    var percentageTaskStarted=${taskStatusMap['Started'].taskCount ? ((taskStatusMap['Started'].taskCount/taskCountByEvent)*100).intValue() :0};
    $("#tasksStartBar").animate({width: percentageTaskStarted+"%" }, 1000);
    
    var percentageTaskReady=${taskStatusMap['Ready'].taskCount ? ((taskStatusMap['Ready'].taskCount/taskCountByEvent)*100).intValue() :0};
    $("#tasksReadyBar").animate({width: percentageTaskReady+"%" }, 1000);
    
    var percentageDurationCompleted=${taskStatusMap['Completed'].timeInMin ? ((taskStatusMap['Completed'].timeInMin/totalDuration)*100).intValue() :0};
    $("#effortDoneBar").animate({width: percentageDurationCompleted+"%" }, 1000);
    
    var percentageDurationStarted=${taskStatusMap['Started'].timeInMin ? ((taskStatusMap['Started'].timeInMin/totalDuration)*100).intValue() :0};
    $("#effortStartBar").animate({width: percentageDurationStarted+"%" }, 1000);
    
    var percentageDurationReady=${taskStatusMap['Ready'].timeInMin ? ((taskStatusMap['Ready'].timeInMin/totalDuration)*100).intValue() :0};
    $("#effortReadyBar").animate({width: percentageDurationReady+"%" }, 1000);
});
</script>
<g:set var="percentageTaskDone" value="${taskStatusMap['Completed'].taskCount ? ((taskStatusMap['Completed'].taskCount/taskCountByEvent)*100).intValue() :0}" />
<g:set var="percentageTaskStarted" value="${taskStatusMap['Started'].taskCount ? ((taskStatusMap['Started'].taskCount/taskCountByEvent)*100).intValue() :0}" />
<g:set var="percentageTaskReady" value="${taskStatusMap['Ready'].taskCount ? ((taskStatusMap['Ready'].taskCount/taskCountByEvent)*100).intValue() :0}" />
<g:set var="percentageDurationDone" value="${taskStatusMap['Completed'].timeInMin ? ((taskStatusMap['Completed'].timeInMin/totalDuration)*100).intValue() :0}" />
<g:set var="percentageDurationStarted" value="${taskStatusMap['Started'].timeInMin ? ((taskStatusMap['Started'].timeInMin/totalDuration)*100).intValue() :0}" />
<g:set var="percentageDurationReady" value="${taskStatusMap['Ready'].timeInMin ? ((taskStatusMap['Ready'].timeInMin/totalDuration)*100).intValue() :0}" />

<div class="toprightcontent">
<div class="taskSummaryDiv">
    <h4>Task Summary </h4><br>
    <span class="taskCountSpan">${taskCountByEvent-taskStatusMap['Completed'].taskCount}</span><br><br>
    <h5>Remaining</h5>
</div>
</div>
<div class="toprightcontent">
<div class="taskDetailsDiv">
        <div class="task_done taskSummaryCounts"><b>Done: ${taskStatusMap['Completed'].taskCount} ${taskStatusMap['Completed'].taskCount ? ('('+taskStatusMap['Completed'].timeInMin+'m)'):''}</b></div>
        <div class="task_started taskSummaryCounts"><b>Started: ${taskStatusMap['Started'].taskCount} ${taskStatusMap['Started'].taskCount ? ('('+taskStatusMap['Started'].timeInMin+'m)'):''}</b></div>
        <div class="task_ready taskSummaryCounts"><b>Ready: ${taskStatusMap['Ready'].taskCount} ${taskStatusMap['Ready'].taskCount ? ('('+taskStatusMap['Ready'].timeInMin+'m)'):''}</b></div>
        <div class="taskSummaryCounts"><b>Pending: ${taskStatusMap['Pending'].taskCount} ${taskStatusMap['Pending'].taskCount ? ('('+taskStatusMap['Pending'].timeInMin+'m)'):''}</b></div>
</div>
</div>
	<input type="hidden" id="task_done_width" value="${percentageTaskDone}"/>
	<input type="hidden" id="task_started_width" value="${percentageTaskStarted}"/>
	<input type="hidden" id="task_ready_width" value="${percentageTaskReady}"/>
	<input type="hidden" id="effort_done_width" value="${percentageDurationDone}"/>
	<input type="hidden" id="effort_started_width" value="${percentageDurationStarted}"/>
	<input type="hidden" id="effort_ready_width" value="${percentageDurationReady}"/>
<div class="toprightcontent">
    <table class="task_bar_table">
         <tr>
          <td class="task_bar_label">
           Tasks
          </td>
          <td class="task_bar_base">
           <div class="task_done task_bar_graph" id="tasksDoneBar" style="width:${taskDonewidth?:'0'}%;"></div>
           <div class="task_started task_bar_graph" id="tasksStartBar" style="width: ${taskStartedwidth?:'0'}%;"></div>
           <div class="task_ready task_bar_graph" id="tasksReadyBar" style="width:${taskReadywidth?:'0'}%;"></div>
           <div class="prog_bar_text" id="taskDoneText">${(percentageTaskDone<10)? '' : percentageTaskDone+'%'}</div>
          </td>
         </tr>
         <tr>
          <td class="task_bar_label">
           Effort
          </td>
          <td class="task_bar_base">
           <div class="task_done task_bar_graph" id="effortDoneBar" style="width:${effortDonewidth?:'0'}%;"></div>
           <div class="task_started task_bar_graph" id="effortStartBar" style="width:${effortStartedwidth?:'0'}%;"></div>
           <div class="task_ready task_bar_graph" id="effortReadyBar" style="width:${effortReadywidth?:'0'}%;"></div>
           <div class="prog_bar_text" id="effortDoneText">${(percentageDurationDone<10)? '' : percentageDurationDone+'%'}</div>
          </td>
         </tr>
    </table>
</div>
