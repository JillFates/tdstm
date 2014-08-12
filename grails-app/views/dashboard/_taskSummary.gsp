<script type="text/javascript">
$(document).ready(function() {
		<%-- 
			This code will animate all of the bargraphs in the Task Summary Section on load and refresh of section 
		--%>
		$("#tasksReadyBar").animate({width: "${percTaskReady}%" }, 1000);
		$("#tasksStartBar").animate({width: "${percTaskStarted}%" }, 1000);
		$("#tasksDoneBar").animate({width: "${percTaskDone}%" }, 1000);
		$("#effortReadyBar").animate({width: "${percDurationReady}%" }, 1000);
		$("#effortStartBar").animate({width: "${percDurationStarted}%" }, 1000);
		$("#effortDoneBar").animate({width: "${percDurationDone}%" }, 1000);
		
		<g:each var="r" in="${teamTaskMap}">
			<g:set var="teamData" value="${r.getValue()}"/>
			<g:if test="${teamData.percDone > 0}">$("#team_${teamData.role.id}").animate({width: "${teamData.percDone}%" }, 1000);</g:if>
		</g:each>
});
</script>

<div class="toprightcontent">
	<div class="taskSummaryDiv">
			<h3 style="margin-top:4px; margin-bottom:20px;">Task Summary</h3><br>
			<span class="taskCountSpan">${remainTaskCountFormat}</span>
			<h5 style="margin-top:12px;">Tasks Remaining</h5>
	</div>
</div>

<div class="statusTaskEffort toprightcontent">
	<table class="task_bar">
		<tr>
			<td class="task_bar_label" rowspan="2"># of<br>Tasks</td>
			<td class="task_bar_base">
				<div class="task_done task_bar_graph" id="tasksDoneBar" style="width: 0%;"></div>
				<div class="task_started task_bar_graph" id="tasksStartBar" style="width: 0%;"></div>
				<div class="task_ready task_bar_graph" id="tasksReadyBar" style="width: 0%;"></div>
				<%-- Display the perc done once enough is done that the text will be in blue --%>
				 <div class="prog_bar_text" id="taskDoneText">${(percTaskDone<5)? '' : percTaskDone+'%'}</div>
			</td>
		</tr>
		<tr>
		<td class="task_bar_base">
			<div class="task_done task_bar_graph" id="tasksDoneBar" style="width: 25%;" align="center">Done<br />${countDone}</div>
			<div class="task_started task_bar_graph" id="tasksStartBar" style="width: 25%;" align="center">Started<br />${countStarted}</div>
			<div class="task_ready task_bar_graph" id="tasksReadyBar" style="width: 25%;" align="center">Ready<br />${countReady}</div>
			<div class="task_pending task_bar_graph" id="tasksPendingBar" style="width: 25%;" align="center">Pending<br />${countPending}</div>
		</td>
		</tr>
		<tr><td>&nbsp;</td></tr>
		<tr>
			<td class="task_bar_label" rowspan="2">Level<br>of Effort</td>
			<td class="task_bar_base">
				<div class="task_done task_bar_graph" id="effortDoneBar" style="width: 0%;"></div>
				<div class="task_started task_bar_graph" id="effortStartBar" style="width: 0%;"></div>
				<div class="task_ready task_bar_graph" id="effortReadyBar" style="width: 0%;"></div>
				<div class="prog_bar_text" id="effortDoneText">${(percDurationDone<5)? '' : percDurationDone+'%'}</div>
			</td>
		</tr>
		<tr>
		<td class="task_bar_base">
			<div class="task_done task_bar_graph" id="effortDoneBar" style="width: 25%;" align="center">Done<br />${effortRemainDone}</div>
			<div class="task_started task_bar_graph" id="effortStartBar" style="width: 25%;" align="center">Started<br />${effortRemainStarted}</div>
			<div class="task_ready task_bar_graph" id="effortReadyBar" style="width: 25%;" align="center">Ready<br />${effortRemainReady}</div>
			<div class="task_pending task_bar_graph" id="effortPendingBar" style="width: 25%;" align="center">Pending<br />${effortRemainPending}</div>
		</td>
		</tr>
	</table>
</div>

<div class="teamBreakdown toprightcontent">  	
	<table style="border:none;">
		<%-- 	
			This section is going to iterate over the Team Task matrix of data to create the table of the team 
			summary information. It will be sorted in a newspaper serpentine columns
		--%>
		<g:each var="r" in="${teamTaskMatrix}">
			<tr>
			<g:each var="c" in="${r}">
				<g:set var="team" value="${c}" />
				<td nowrap><b>${team.role.description}</b></td>
				<td style="text-align:right;"><b>${team.teamTaskCount}</b></td>
				<td nowrap style="width: 70px; text-align: center;">
					<g:set var="remainingTeamTask" value="${team.teamTaskCount - team.teamDoneCount}" />
					<g:if test="${team.percDone < 100}" >
						<div class="team_bar_base_small">
							<div class="team_bar_graph_small" id="team_${team.role.id}" style="width: 0%;"></div>
						</div>
						<span class="small_text"><b>${remainingTeamTask} to go</b></span>
					</g:if>
					<g:else>
						<img src="${resource(dir:'images',file:'checked-icon.png')}" />
					</g:else>
				</td>
			</g:each>
			</tr>
		</g:each>
	</table>
</div>
