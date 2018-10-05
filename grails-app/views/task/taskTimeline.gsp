<%@page import="net.transitionmanager.security.Permission"%>
<html>
	<head>
		<title>Task Timeline</title>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="topNav" />
		<g:javascript src="asset.comment.js" />
		<g:javascript src="asset.tranman.js"/>
		<g:javascript src="entity.crud.js" />
		<g:javascript src="model.manufacturer.js"/>
		<g:javascript src="d3/d3.js"/>
		<g:render template="../layouts/responsiveAngularResources" />
		<g:javascript src="lodash/lodash.min.js" />
		<g:javascript src="progressTimer.js" />

    	<asset:stylesheet href="css/resources/ui.datetimepicker.css" />
		<asset:stylesheet href="css/jqgrid/ui.jqgrid.css" />
		<asset:stylesheet href="css/task-timeline.css" />
		<asset:stylesheet href="css/force.css" />
		<g:javascript src="keyevent_constants.js" />
		<g:javascript src="graph.js" />
		<g:javascript src="task-timeline.js" />
	</head>
	<body>
		<tds:subHeader title="Task Timeline" crumbs="['Task','Timeline']"/>
		<input type="hidden" id="timeBarValueId" value="0"/>
		<div class="taskTimebar hide" id="issueTimebar">
			<div id="issueTimebarId"></div>
		</div>
		<div class="body fluid task-timeline-container" ng-app="tdsComments" ng-controller="tds.comments.controller.MainController as comments">
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			<div id="taskTimelineGraphId" class="graphContainer">
				<div id="toolsContainerId">
					<!-- controls toolbar -->
					<div id="graphToolbarId" style="width: 100%;">
						<div id="controlPanelTabId" class="graphPanelTab" onclick="GraphUtil.togglePanel(GraphUtil.PANELS.CONTROL)"><h4>Control Panel</h4></div>
						<span class="controlSpan">
							<label for="moveEventId">Event:</label>
							<g:select from="${moveEvents}" name="moveEventId" id="moveEventId" optionKey="id" optionValue="name" noSelection="${['0':' Select a move event']}" value="${selectedEventId}" onchange="submitForm()" />
						</span>
						<span class="controlSpan">
							<label for="teamSelectId">Highlight:</label>
							<select name="teamSelect" id="teamSelectId" style="width:120px;"></select>
						</span>
						<span class="controlSpan">
							<form onsubmit="return performSearch()" id="highlightFormId">
								<input type="text" name="Search Box" id="searchBoxId" value="" placeholder="Enter highlighting filter" size="24" />
								<span id="filterClearId" class="disabled ui-icon ui-icon-closethick" onclick="clearFilter()" title="Clear the current filter"></span>
								<input type="submit" name="Submit Button" id="SubmitButtonId" class="pointer graphButton" value="Highlight" />
							</form>
						</span>
						<div id="zoomInButtonId" class="graphButton graphTabButton zoomButton pointer hasMargin"></div>
						<div id="zoomOutButtonId" class="graphButton graphTabButton zoomButton pointer"></div>
						<span class="controlSpan checkboxContainer">
							<input type="checkbox" id="highlightCriticalPathId" class="pointer" checked="checked"/>
							<label for="highlightCriticalPathId" class="pointer">&nbsp;Highlight Critical Path</label>
						</span>
						<div style="float: right;" class="task-timeline-progress-wrapper">
							<g:render template="../assetEntity/progressTimerControls" model="${[timerValues:[60, 120, 180, 240, 300]]}"/>
						</div>
					</div>
					<!-- control panel -->
					<div id="controlPanelId" class="graphPanel">
						<form id="preferencesformId">
							<table class="labelTree savedToPrefs" cellpadding="0" cellspacing="0">

								<tds:hasPermission permission="${Permission.TaskViewUnpublished}">
									<tr title="Shows tasks that are not published">
										<td colspan="3" style="padding-left :0px">
											<span class="checkboxContainer">
												<input type="checkbox" name="viewUnpublished" id="viewUnpublishedId" class="pointer" ${ (viewUnpublished=='1' ? 'checked="checked"' : '') } onchange="submitForm()" /><!--
												--><label for="viewUnpublishedId" class="pointer">&nbsp;View Unpublished</label>
											</span>
										</td>
									</tr>
								</tds:hasPermission>

								<tr title="Sets the hight of an individual task">
									<td colspan="3" style="padding-left :0px">
										<span>
											<input type="text" id="mainHeightFieldId" value="30" size="3" style="width:25px;" /><!--
											--><label for="mainHeightFieldId" class="pointer">&nbsp;Task Size (pixels)</label>
										</span>
									</td>
								</tr>

								<tr title="Sets if tasks should become taller to fill the empty space">
									<td colspan="3" style="padding-left :0px">
										<span class="checkboxContainer">
											<input type="checkbox" id="useHeightCheckBoxId" name="useHeights" class="pointer" checked="checked" /><!--
											--><label for="useHeightCheckBoxId" class="pointer">&nbsp;Use Heights</label>
										</span>
									</td>
								</tr>

								<tr title="Hides task dependencies that are redundant">
									<td colspan="3" style="padding-left :0px">
										<span class="checkboxContainer">
											<input type="checkbox" id="hideRedundantCheckBoxId" class="pointer" checked="checked" /><!--
											--><label for="hideRedundantCheckBoxId" class="pointer">&nbsp;Hide Redundant</label>
										</span>
									</td>
								</tr>

								<tds:hasPermission permission="${Permission.TaskViewCriticalPath}">
								<tr title="Export Critical Path Data of an event">
									<td colspan="3" style="padding-left :0px">
										<span>
											<label id="exportCriticalPathButton" for="exportCriticalPath" class="pointer" onclick="exportCriticalPath()">
												<asset:image src="icons/hourglass_go.png" border="0px"/>&nbsp;Export Timeline Data
											</label>
										</span>
									</td>
								</tr>
								</tds:hasPermission>

							</table>
						</form>
					</div>
				</div>
				<span id="spinnerId" style="display: none"><asset:image src="images/spinner.gif"/></span>
				<g:render template="../assetEntity/initAssetEntityData"/>
				<g:render template="../layouts/error"/>
				<div id="svgContainerId" style="display: none;"></div>
			</div>
		</div>
		<script type="text/javascript">
			var progressTimer;
			$(document).ready(function() {
				progressTimer = new ProgressTimer(0, '${com.tdsops.tm.enums.domain.UserPreferenceEnum.TIMELINE_REFRESH}', function () {
					reloadGraph();
					progressTimer.resetTimer();
				});
			})

			function reloadGraph () {
				submitForm();
			}

			$(".menu-parent-tasks-task-timeline").addClass('active');
			$(".menu-parent-tasks").addClass('active');
		</script>
	</body>
</html>
