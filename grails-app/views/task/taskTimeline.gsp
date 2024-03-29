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
		<g:render template="/layouts/responsiveAngularResources" />
		<g:javascript src="lodash/lodash.min.js" />
		<g:javascript src="progressTimer.js" />

    	<asset:stylesheet href="css/resources/ui.datetimepicker.css" />
		<asset:stylesheet href="css/jqgrid/ui.jqgrid.css" />
		<asset:stylesheet href="css/task-timeline.css" />
		<asset:stylesheet href="css/force.css" />
		<g:javascript src="keyevent_constants.js" />
		<g:javascript src="graph.js" />
		<g:javascript src="task-timeline.js" />
<style>

h2 {
	font-family: 'Roboto', Metropolis, "Avenir Next", "Helvetica Neue", Arial, sans-serif, 'Glyphicons Halflings', FontAwesome, 'WebComponentsIcons' !important;
	font-weight: 200;
	line-height: 2rem;
	font-size: x-large;
	color: #000;
	letter-spacing: normal;
}

.container-checkbox {
	display: inline-block;
	position: relative;
	padding-left: 35px;
	margin-bottom: 12px;
	cursor: pointer;
	font-size: 12px;
	font-weight: 400;
	font-family: 'Roboto', Metropolis, "Avenir Next", "Helvetica Neue", Arial, sans-serif, 'Glyphicons Halflings', FontAwesome, 'WebComponentsIcons' !important;

	-webkit-user-select: none;
	-moz-user-select: none;
	-ms-user-select: none;
	user-select: none;
}

.container-checkbox input {
	position: absolute;
	opacity: 0;
	cursor: pointer;
	line-height: normal;
	height: .66667rem;
	width: .66667rem;
}

.checkmark {	
	position: absolute;
	top: -5px;
	left: 12px;
	height: 17px;
	width: 17px;
	background-color: #fff;	
	border-radius: .125rem;

	outline: none;
	padding: 3px 0px 3px 3px;
	margin: 5px 1px 3px 0px;
	border: 1px solid #999;
}

.container-checkbox input:checked ~ .checkmark {
	background: #0077b8;
}

.container-checkbox input:focus ~ .checkmark {
	box-shadow: 0 0 5px rgba(81, 203, 238, 1);
	padding: 3px 0px 3px 3px;
	margin: 5px 1px 3px 0px;
	border: 1px solid rgba(81, 203, 238, 1);
}

.checkmark:after {
	content: "";
	position: absolute;
	display: none;
	box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.2);
}

.container-checkbox input:checked ~ .checkmark:after {
	display: block;
}

.container-checkbox .checkmark:after {
	left: 5px;
	top: 2px;
	width: 5px;
	height: 10px;
	border: solid white;
	border-width: 0 1.5px 1.5px 0;
	-webkit-transform: rotate(45deg);
	-ms-transform: rotate(45deg);
	transform: rotate(45deg);
}

	</style>
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
							<form onsubmit="performSearch()" id="highlightFormId" >
								<input type="text" name="Search Box" id="searchBoxId" value="" placeholder="Enter highlighting filter" size="24" />
								<span id="filterClearId" class="disabled ui-icon ui-icon-closethick" onclick="clearFilter()" title="Clear the current filter"></span>
								<input type="submit" name="Submit Button" id="SubmitButtonId" class="pointer graphButton" value="Highlight" />
							</form>
						</span>
						<div id="zoomInButtonId" class="graphButton graphTabButton zoomButton pointer hasMargin"><i class="fas fa-search-plus"></i></div>
						<div id="zoomOutButtonId" class="graphButton graphTabButton zoomButton pointer"><i class="fas fa-search-minus"></i></div>
						<span class="controlSpan checkboxContainer">
							<label class="container-checkbox" for="highlightCriticalPathId">Highlight Critical Path
								<input id="highlightCriticalPathId" name="highlightCriticalPathId" class="pointer" type="checkbox" checked="checked" />
								<span class="checkmark"></span>
							</label>
						</span>
						<span class="controlSpan radioboxContainer">
							<input type="radio" name="mode" value="C" checked id="modecId"> <label for="modecId" class="pointer">&nbsp;Current Plan</label>
							<input type="radio"  name="mode" value="R" id="modeRId"> <label for="modeRId" class="pointer">&nbsp;Recalculate</label>
						</span>
						<span id="baselinePlanButton">
							<button onclick="baseLine()" title="Baseline" class="baseline-btn"><label>Baseline Plan</label></button>
						</span>

						<div style="float: right;" class="task-timeline-progress-wrapper">
							<g:render template="/assetEntity/progressTimerControls" model="${[timerValues:[60, 120, 180, 240, 300]]}"/>
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
				<g:render template="/assetEntity/initAssetEntityData"/>
				<g:render template="/layouts/error"/>
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
