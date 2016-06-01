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
		<g:javascript src="TimerBar.js" />

		<link type="text/css" rel="stylesheet" href="${g.resource(dir:'css',file:'ui.datepicker.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datetimepicker.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'task-timeline.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'force.css')}" />
		<g:javascript src="task-timeline.js" />
		<g:javascript src="graph.js" />
	</head>
	<body>
		<input type="hidden" id="timeBarValueId" value="0"/>
		<div class="taskTimebar hide" id="issueTimebar">
			<div id="issueTimebarId"></div>
		</div>
		<div class="body fluid" ng-app="tdsComments" ng-controller="tds.comments.controller.MainController as comments">
			<h1>Task Timeline</h1>
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			<div class="graphContainer">
				<div id="toolsContainerId">
					<span id="panelLink" colspan="2" class="noPadding">
						<div id="mapReferenceId" style="width: 100%;">
							<div id="controlPanelTabId" class="graphPanelTab" onclick="GraphUtil.togglePanel('control')"><h4>Control Panel</h4></div><!-- This comment prevents the browser from trying to evaluate the whitespace between these divs as a space character
							--><span class="controlSpan"><!--
								--><label for="moveEventId">Event:</label><!--
								--><g:select from="${moveEvents}" name="moveEventId" id="moveEventId" optionKey="id" optionValue="name" noSelection="${['0':' Select a move event']}" value="${selectedEventId}" onchange="submitForm()" /><!--
							--></span><!--
							--><span class="controlSpan"><!--
								--><label for="teamSelectId">Highlight:</label><!--
								--><select name="teamSelect" id="teamSelectId" style="width:120px;"></select><!--
							--></span><!--
							--><span class="controlSpan"><!--
								--><form onsubmit="return performSearch()" id="taskSearchFormId"><!--
									--><input type="text" name="Search Box" id="searchBoxId" value="" placeholder="Enter highlighting filter" size="24" /><!--
									--><span id="filterClearId" class="disabled ui-icon ui-icon-closethick" onclick="clearFilter()" title="Clear the current filter"></span><!--
									--><input type="submit" name="Submit Button" id="SubmitButtonId" class="pointer graphButton" value="Highlight" /><!--
								--></form><!--
							--></span><!--
							--><span class="controlSpan checkboxContainer"><!--
								--><input type="checkbox" id="highlightCriticalPathId" class="pointer" checked="checked"/><!--
								--><label for="highlightCriticalPathId" class="pointer">&nbsp;Highlight Critical Path</label><!--
							--></span><!--
							--><span class="controlSpan"><!--
								--><span style="float:right;"><!--
									--><g:render template="../assetEntity/timerBarControls" model="${[timerValues:[60, 120, 180, 240, 300]]}"/><!--
								--></span><!--
							--></span>
						</div>
					</span>
					<div id="controlPanel" class="graphPanel">
						<form id="preferencesformId">
							<table class="labelTree savedToPrefs" cellpadding="0" cellspacing="0">
								
								<tds:hasPermission permission="PublishTasks">
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
											<input type="text" id="mainHeightFieldId" value="30" size="3" style="width:20px;" /><!--
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

								<tds:hasPermission permission='CriticalPathExport'>
								<tr title="Export Critical Path Data of an event">
									<td colspan="3" style="padding-left :0px">
										<span>
											<label id="exportCriticalPathButton" for="exportCriticalPath" class="pointer" onclick="exportCriticalPath()"><img src='${resource(dir:'icons',file:'hourglass_go.png')}' border='0px'/>&nbsp;Export Timeline Data</label>
										</span>
									</td>
								</tr>
								</tds:hasPermission>

							</table>
						</form>
					</div>
				</div>
				<span id="spinnerId" style="display: none"><img alt="" src="${resource(dir:'images',file:'spinner.gif')}"/></span>
				<g:render template="../assetEntity/initAssetEntityData"/>
				<g:render template="../layouts/error"/>
			</div>
		</div>
		<script type="text/javascript">
			var timerBar;
			$(document).ready(function() {
				timerBar = new TimerBar(0, 'RefreshTimeline', function () {
					reloadGraph();
					timerBar.resetTimer();
				});
			})

			function reloadGraph () {
				submitForm();
			}
		</script>
	</body>
</html>