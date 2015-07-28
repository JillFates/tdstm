<html>
	<head>
		<title>Task Timeline</title>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="projectHeader" />
		<g:javascript src="asset.comment.js" />
		<g:javascript src="asset.tranman.js"/>
		<g:javascript src="entity.crud.js" />
		<g:javascript src="model.manufacturer.js"/>
		<g:javascript src="d3/d3.min.js"/>
		<g:javascript src="angular/angular.min.js" />
		<g:javascript src="angular/plugins/angular-ui.js"/>	
		<g:javascript src="angular/plugins/angular-resource.js" />
		<script type="text/javascript" src="${resource(dir:'components/core',file:'core.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'components/comment',file:'comment.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'components/asset',file:'asset.js')}" /></script>
		<g:javascript src="bootstrap.js" />
		<g:javascript src="angular/plugins/ui-bootstrap-tpls-0.10.0.min.js" />
		<g:javascript src="angular/plugins/ngGrid/ng-grid-2.0.7.min.js" />
		<g:javascript src="lodash/lodash.min.js" />
		<g:javascript src="TimerBar.js" />

		<link type="text/css" rel="stylesheet" href="${g.resource(dir:'css',file:'ui.datepicker.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datetimepicker.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'components/comment',file:'comment.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'task-timeline.css')}" />
		<g:javascript src="task-timeline.js" />
	</head>
	<body>
		<input type="hidden" id="timeBarValueId" value="0"/>
		<div class="taskTimebar hide" id="issueTimebar" >
			<div id="issueTimebarId"></div>
		</div>
		<div class="body fluid" ng-app="tdsComments" ng-controller="tds.comments.controller.MainController as comments">
			<h1>Task Timeline</h1>
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			Event: <g:select from="${moveEvents}" name="moveEventId" id="moveEventId" optionKey="id" optionValue="name" noSelection="${['0':' Select a move event']}" value="${selectedEventId}" onchange="submitForm()" />
			&nbsp; Highlight: <select name="teamSelect" id="teamSelectId" style="width:120px;"></select>
			<form onsubmit="return performSearch()" id="taskSearchFormId">
				&nbsp;<input type="text" name="Search Box" id="searchBoxId" value="" placeholder="Enter highlighting filter" size="24">
				<span id="filterClearId" class="disabled ui-icon ui-icon-closethick" onclick="clearFilter()" title="Clear the current filter"></span>
				&nbsp;<input type="submit" name="Submit Button" id="SubmitButtonId" value="Highlight">
			</form>
			<tds:hasPermission permission="PublishTasks">
				<span class="checkboxContainer">
					&nbsp;<input type="checkbox" name="viewUnpublished" id="viewUnpublishedId" class="pointer" ${ (viewUnpublished=='1' ? 'checked="checked"' : '') } onchange="submitForm()" /><!--
					--><label for="viewUnpublishedId" class="pointer">&nbsp;View Unpublished</label>
				</span>
			</tds:hasPermission>
			<span style="float:right;">
				Task Size (pixels): <input type="text" id="mainHeightFieldId" value="30" size="3" style="width:20px;" />&nbsp;&nbsp;
				<span class="checkboxContainer">
					<input type="checkbox" id="useHeightCheckBoxId" class="pointer" checked="checked" /><!--
					--><label for="useHeightCheckBoxId" class="pointer">&nbsp;Use Heights</label>&nbsp;&nbsp;
				</span>
				<span class="checkboxContainer">
					<input type="checkbox" id="hideRedundantCheckBoxId" class="pointer" checked="checked" /><!--
					--><label for="hideRedundantCheckBoxId" class="pointer">&nbsp;Hide Redundant</label>&nbsp;&nbsp;
				</span>
				<g:render template="../assetEntity/timerBarControls" model="${[timerValues:[60, 120, 180, 240, 300]]}"/>
			</span>
			<br />
			<span id="spinnerId" style="display: none"><img alt="" src="${resource(dir:'images',file:'spinner.gif')}"/></span>
			<g:render template="../assetEntity/initAssetEntityData"/>
			<g:render template="../layouts/error"/>
		</div>
		<script type="text/javascript">
			var timerBar;
			$(document).ready(function() {
				timerBar = new TimerBar(60, 'RefreshTimeline', function () {
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