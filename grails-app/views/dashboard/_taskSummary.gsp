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

		var scrollTeamsIcon = $('#scrollTeamsIcon');
		if(scrollTeamsIcon && scrollTeamsIcon.length > 0){
		    var teamTableHeight = $('#teamTableContent').height();
		    scrollTeamsIcon.animate({marginTop: (teamTableHeight/2)-5}, 1000);
		}
});

var toggleScrollTeamsLeft = true;
function scrollTeams(){
		    var scrollSize = $('#teamTableContent').width();
		    if(toggleScrollTeamsLeft){
		        $('#teamTableContent').animate({ scrollLeft: scrollSize }, 800);
		        $('#scrollTeamsLeftIcon').hide();
		        $('#scrollTeamsRightIcon').show();
		    }
		    else{
		        $('#teamTableContent').animate({ scrollLeft: -scrollSize }, 800);
		        $('#scrollTeamsRightIcon').hide();
		        $('#scrollTeamsLeftIcon').show();
		    }
		    toggleScrollTeamsLeft = !toggleScrollTeamsLeft;
}
</script>

<div class="toprightcontent">
	<div class="taskSummaryDiv">
			<h3 style="margin-top:4px; margin-bottom:20px;"></h3><br>
			<span class="taskCountSpan">${remainTaskCountFormat}</span>
			<h5 style="margin-top:12px;">Tasks Remaining</h5>
	</div>
</div>

<%--
	If there are no tasks that are on hold, the legend for Hold will not be displayed
--%>
<g:set var="legendWidth" value="20%"/>
<g:set var="legendHoldWidth" value="20%"/>
<g:if test="${countHold == 0}">
	<g:set var="legendWidth" value="25%"/>
	<g:set var="legendHoldWidth" value="0%"/>
</g:if>

<div class="statusTaskEffort toprightcontent">
	<table class="task_bar">
		<tr>
			<td class="task_bar_label" rowspan="2"># of<br>Tasks</td>
			<td class="task_bar_base">
				<div class="task_done task_bar_graph" id="tasksDoneBar" style="width: 0%;">
					<%-- Display the perc done once enough is done that the text will be in blue --%>
					<div class="prog_bar_text" id="taskDoneText">${(percTaskDone<5)? '' : percTaskDone+'%'}</div>
				</div>
				<div class="task_started task_bar_graph" id="tasksStartBar" style="width: 0%;"></div>
				<div class="task_ready task_bar_graph" id="tasksReadyBar" style="width: 0%;"></div>
				<div class="task_Hold task_bar_graph" id="tasksHoldBar" style="width: 0%;"></div>
			</td>
		</tr>
		<tr>
		<td class="task_bar_legend">
			<a href="${createLink(controller:'assetEntity', action:'listTasks', params:[status:'Completed'])}">
				<div class="task_done task_bar_graph" id="tasksDoneBar" style="width: ${legendWidth};" align="center">Done<br />${countDone}</div>
			</a>
			<a href="${createLink(controller:'assetEntity', action:'listTasks', params:[status:'Started'])}">
				<div class="task_started task_bar_graph" id="tasksStartBar" style="width: ${legendWidth};" align="center">Started<br />${countStarted}</div>
			</a>
			<a href="${createLink(controller:'assetEntity', action:'listTasks', params:[status:'Ready'])}">
				<div class="task_ready task_bar_graph" id="tasksReadyBar" style="width: ${legendWidth};" align="center">Ready<br />${countReady}</div>
			</a>
			<a href="${createLink(controller:'assetEntity', action:'listTasks', params:[status:'Hold'])}">
				<div class="task_hold task_bar_graph" id="tasksHoldBar" style="width: ${legendHoldWidth};" align="center">Hold<br />${countHold}</div>
			</a>
			<a href="${createLink(controller:'assetEntity', action:'listTasks', params:[status:'Pending'])}">
			<div class="task_pending task_bar_graph" id="tasksPendingBar" style="width: ${legendWidth};" align="center">Pending<br />${countPending}</div>
			</a>
		</td>
		</tr>
		<tr><td>&nbsp;</td></tr>
		<tr>
			<td class="task_bar_label" rowspan="2">Level<br>of Effort</td>
			<td class="task_bar_base">
				<div class="task_done task_bar_graph" id="effortDoneBar" style="width: 0%;">
					<div class="prog_bar_text" id="effortDoneText">${(percDurationDone<5)? '' : percDurationDone+'%'}</div>
				</div>
				<div class="task_started task_bar_graph" id="effortStartBar" style="width: 0%;"></div>
				<div class="task_ready task_bar_graph" id="effortReadyBar" style="width: 0%;"></div>
				<div class="task_hold task_bar_graph" id="effortHoldBar" style="width: 0%;"></div>
			</td>
		</tr>
		<tr>
		<td class="task_bar_legend">
			<div class="task_done task_bar_graph" id="effortDoneBar" style="width: ${legendWidth};" align="center">Done<br />${effortRemainDone}</div>
			<div class="task_started task_bar_graph" id="effortStartBar" style="width: ${legendWidth};" align="center">Started<br />${effortRemainStarted}</div>
			<div class="task_ready task_bar_graph" id="effortReadyBar" style="width: ${legendWidth};" align="center">Ready<br />${effortRemainReady}</div>
			<div class="task_hold task_bar_graph" id="effortHoldBar" style="width: ${legendHoldWidth};" align="center">Hold<br />${effortRemainHold}</div>
			<div class="task_pending task_bar_graph" id="effortPendingBar" style="width: ${legendWidth};" align="center">Pending<br />${effortRemainPending}</div>

		</td>
		</tr>
	</table>
</div>


<g:set var="teamMatrixSize" value="${0}"/>
<g:if test="${teamTaskMatrix.size > 0}">
    <g:set var="teamMatrixSize" value="${teamTaskMatrix.size * teamTaskMatrix[0].size}" />
</g:if>
<g:if test="${teamMatrixSize <= 6}" >
    <div id="teamTableContent" class="teamBreakdown toprightcontent smallTable">
</g:if>
<g:else>
    <div id="teamTableContent" class="teamBreakdown toprightcontent">
</g:else>

	<table style="border:none;">
		<%--
			This section is going to iterate over the Team Task matrix of data to create the table of the team
			summary information. It will be sorted in a newspaper serpentine columns
		--%>
		<g:each var="r" in="${teamTaskMatrix}">
			<tr>
			<g:each var="c" in="${r}">
				<g:set var="team" value="${c}" />
					<td class="teamcol" nowrap>
						<a href="${createLink(controller:'assetEntity', action:'listTasks', params:[role:team.role.id])}" style="display:block">${team.role.description}</a>
					</td>

				<td class="teamcountcolumn">
					<a href="${createLink(controller:'assetEntity', action:'listTasks', params:[role:team.role.id])}" style="display:block">${team.teamTaskCount}</a>
				</td>
				<td class="teambarcolumn" nowrap>
					<g:set var="remainingTeamTask" value="${team.teamTaskCount - team.teamDoneCount}" />
					<g:if test="${team.percDone < 100}" >
						<div class="team_bar_base_small">
							<div class="team_bar_graph_small" id="team_${team.role.id}" style="width: 0%;"></div>
						</div>
						<span class="small_text"><b>${remainingTeamTask} to go</b></span>
					</g:if>
					<g:else>
						<asset:image src="images/checked-icon.png" />
					</g:else>
				</td>
			</g:each>
			</tr>
		</g:each>
	</table>
</div>

<g:if test="${teamMatrixSize > 18}" >
    <div id="scrollTeamsIcon" class="text-right toprightcontent teamScrollButton">
         <a id="scrollTeamsLeftIcon" href="javascript:void(0);" onclick="scrollTeams();">
            <asset:image src="images/right_arrow.png" alt="back" border="0" width="16" height="23" align="right" />
          </a>
          <a id="scrollTeamsRightIcon" href="javascript:void(0);" onclick="scrollTeams();" style="display: none;">
            <asset:image src="images/left_arrow.png" alt="back" border="0" width="16" height="23" align="right" />
          </a>
     </div>
</g:if>
