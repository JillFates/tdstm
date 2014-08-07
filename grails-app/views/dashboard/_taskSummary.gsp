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
		
		<g:each in="${roles['id']}" var="role"> 
		 <g:set var="role" value="'${role}'" />
			var roleId='${role}'
			 var percentage = "${teamTaskMap[role].teamTaskCount ? Math.round((teamTaskMap[role].teamDoneCount/teamTaskMap[role].teamTaskCount)*100) :0}";
			 $("#team_"+roleId).animate({width: percentage+"%" }, 1000);
		</g:each>
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
		<h3 style="margin-top:4px;">Task Summary</h3><br>
		<span class="taskCountSpan">${remainTaskCountFormat}</span>
		<h5 style="margin-top:12px;">Remaining</h5>
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
<g:form  method="post" name="teamTaskPercentageFormId" >
	<input type="hidden" id="taskDoneWidthId"  name="taskDoneWidthId" value="${percentageTaskDone}"/>
	<input type="hidden" id="taskStartedWidthId" name="taskStartedWidthId" value="${percentageTaskStarted}"/>
	<input type="hidden" id="taskReadyWidthId" name="taskReadyWidthId" value="${percentageTaskReady}"/>
	<input type="hidden" id="effortDoneWidthId" name="effortDoneWidthId" value="${percentageDurationDone}"/>
	<input type="hidden" id="effortStartedWidthId" name="effortStartedWidthId" value="${percentageDurationStarted}"/>
	<input type="hidden" id="effortReadyWidthId" name="effortReadyWidthId" value="${percentageDurationReady}"/>
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
 <div style="margin-left:52%; position:absolute; margin-top:5px; width: 550px; overflow: auto;">  	
	<table style="border:none;">
		<g:set var="i" value="${0}" />
		<g:each var="r" in="${teamTaskMatrix}">
			<tr>
			<g:each var="c" in="${r}">
				<g:set var="team" value="${c}" />
				<g:set var="percComp" value="${ team.teamTaskCount ? Math.round( team.teamDoneCount / team.teamTaskCount * 100) : 100 }" />
				<input type="hidden" name="${team.role.id}" id="${team.role.id}" value="${percComp}" />
				<td nowrap><b>${team.role.description}</b></td>
				<td style="text-align:right;"><b>${team.teamTaskCount}</b></td>
				<td nowrap style="width: 70px; text-align: center;">
					<g:set var="remainingTeamTask" value="${team.teamTaskCount - team.teamDoneCount}" />
					<g:if test="${percComp < 100}" >
						<div class="team_bar_base_small">
							<div class="team_bar_graph_small" id="team_${team.role.id}" style="width: 0;"></div>
						</div>
						<span class="small_text"><b>${remainingTeamTask} to go</b></span>
					</g:if>
					<g:else>
						<img src="${resource(dir:'images',file:'checked-icon.png')}" />
					</g:else>
				</td>
				<g:set var="i" value="${++i}" />
			</g:each>
			</tr>
		</g:each>
		</table>
 </div>
</g:form>

