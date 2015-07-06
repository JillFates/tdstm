<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
		<meta name="layout" content="projectHeader" />
		<title>Architecture Graph</title>
		<g:javascript src="asset.tranman.js" />
		<g:javascript src="entity.crud.js" />
		<g:javascript src="model.manufacturer.js"/>
		<g:javascript src="projectStaff.js" />
		<g:javascript src="angular/angular.min.js" />
		<g:javascript src="angular/plugins/angular-ui.js"/>
		<g:javascript src="angular/plugins/angular-resource.js" />
		<script type="text/javascript" src="${resource(dir:'components/core',file:'core.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'components/comment',file:'comment.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'components/asset',file:'asset.js')}" /></script>
		<g:javascript src="asset.comment.js" />
		<g:javascript src="cabling.js"/>
		<g:javascript src="angular/plugins/ui-bootstrap-tpls-0.10.0.min.js" />
		<g:javascript src="angular/plugins/ngGrid/ng-grid-2.0.7.min.js" />
		<g:javascript src="angular/plugins/ngGrid/ng-grid-layout.js" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datepicker.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'components/comment',file:'comment.css')}" />
		<g:javascript src="d3/d3.min.js" />
		<g:javascript src="lodash/lodash.min.js" />
		<g:javascript src="load.shapes.js"/>
		<g:javascript src="graph.js" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'force.css')}" />
	</head>
<body>
<div id="body" class="body" ng-app="tdsComments" ng-controller="tds.comments.controller.MainController as comments">
	<h1 id="pageTitleId">Architecture Graph</h1>
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>
	
	<div id="item1" class="graphContainer">
		<div id="toolsContainerId">
			<span id="panelLink" style="padding: 0px;">
				<table id="mapReferenceId">
					<tr>
						<td id="controlPanelTabId" class="graphPanelTab activeTab" onclick="GraphUtil.togglePanel('control')"><h4>Control Panel</h4></td>
						<td id="legendTabId" class="graphPanelTab" onclick="GraphUtil.togglePanel('legend')"><h4>Legend</h4></td>
						<td id="fullscreenButtonId" onclick="GraphUtil.toggleFullscreen()" title="Toggles fullscreen mode"><h4>Fullscreen</h4></td>
					</tr>
				</table>
			</span>
		
			<div id="controlPanel" class="graphPanel openPanel">
				<table id="labelTree" cellpadding="0" cellspacing="0" style="margin-left: 5px;border: 0;" >
					<form style="display:inline-block;" id="graphFormId">
						<tr title="Sets the asset to use as the root node">
							<td class="controlPanelLabel">Asset</td>
							<td class="controlPanelControl">
								<input type="hidden" id="assetSelectId" name="assetList" class="scrollSelect" style="width:150px" data-asset-type="ALL" />
							</td>
						</tr>
						<tr title="Sets the asset classes that will show up in the asset search box">
							<td class="controlPanelLabel">Search for Classes</td>
							<td class="controlPanelControl">
								<g:select name="assetClasses" id="assetClassesId" from="${assetClassesForSelect.values()}" keys="${assetClassesForSelect.keySet()}" value="ALL"></g:select>
							</td>
						</tr>
						<tr title="Sets the max number of links to follow up">
							<td class="controlPanelLabel">Levels Up</td>
							<td class="controlPanelControl">
								<img src="${resource(dir:'images',file:'minus.gif')}" class="pointer plusMinusIcon" onclick="modifyParameter('sub', this)"/>
								<g:select name="levelsUp" id="levelsUpId" from="${0..10}" value="${levelsUp}"></g:select>
								<img src="${resource(dir:'images',file:'plus.gif')}" class="pointer plusMinusIcon" onclick="modifyParameter('add', this)"/>
							</td>
						</tr>
						<tr title="Sets the max number of links to follow down">
							<td class="controlPanelLabel">Levels Down</td>
							<td class="controlPanelControl">
								<img src="${resource(dir:'images',file:'minus.gif')}" class="pointer plusMinusIcon" onclick="modifyParameter('sub', this)"/>
								<g:select name="levelsDown" id="levelsDownId" from="${0..10}" value="${levelsDown}"></g:select>
								<img src="${resource(dir:'images',file:'plus.gif')}" class="pointer plusMinusIcon" onclick="modifyParameter('add', this)"/>
							</td>
						</tr>
						<tr title="Reloads the graph">
							<td class="controlPanelControl" colspan="2">
								<input type="submit" name="Submit Button" id="graphSubmitButtonId" class="pointer" value="Regenerate Graph">
							</td>
						</tr>
					</form>
					<tr title="Hides dependencies that are implied by other shown dependencies" style="display:none;">
						<td class="controlPanelLabel">Hide Redundant</td>
						<td class="controlPanelControl">
							<input type="checkbox" id="hideRedundantCheckBoxId" />
						</td>
					</tr>
					<tr title="Highlights cyclical structures in the dependency tree">
						<td class="controlPanelLabel">Highlight Cycles</td>
						<td class="controlPanelControl">
							<input type="checkbox" id="highlightCyclicalCheckBoxId" />
						</td>
					</tr>
					<tr title="Shows labels for each asset">
						<td class="controlPanelLabel">Show Labels</td>
						<td class="controlPanelControl">
							<input type="checkbox" id="labelCheckBoxId" checked="checked" />
						</td>
					</tr>
					<tr title="Sets the maximum number of rows the labels can use when stacking">
						<td class="controlPanelLabel">Max Label Offset</td>
						<td class="controlPanelControl">
							<g:select name="labelOffset" id="labelOffsetId" from="${1..4}" value="2"></g:select>
						</td>
					</tr>
					
					<tr title="Sets the distance between nodes">
						<td class="controlPanelLabel">Spacing</td>
						<td class="controlPanelControl">
							&nbsp;
							<img src="${resource(dir:'icons',file:'arrow_in.png')}" id="spacingDecreaseId" height="20" class="pointer plusMinusIcon"/>
							&nbsp;
							<img src="${resource(dir:'icons',file:'arrow_out.png')}" id="spacingIncreaseId" height="20" class="pointer plusMinusIcon"/>
						</td>
					</tr>
					
				</table>
			</div>
			
			<g:include controller="assetEntity" action="graphLegend" params="${[displayMoveEvents:false, displayFuture:true, displayCycles:true, displayBundleConflicts:false, arrowheadOffset:false]}" />
		</div>
		<div id="svgContainerId"></div>
		<div id="spinnerDivId" style="display: none"></div>
	</div>
	
	<g:render template="../assetEntity/modelDialog"/>
	<g:render template="../assetEntity/entityCrudDivs" />
	<g:render template="../assetEntity/dependentAdd" />
	<div id="createStaffDialog" style="display:none;">
		<g:render template="../person/createStaff" model="['forWhom':'application']"></g:render>
	</div>
</div>
<g:render template="../assetEntity/initAssetEntityData"/>

<script type="text/javascript">
	
	// if the user navigated here from an asset crud button, there will be a specific asset to start with
	var initialAssetId = ${assetId ?: 'null'};
	
	$(document).ready(function(){

		
		// define the select2 for assets
		if (!isIE7OrLesser) {
			EntityCrud.assetNameSelect2( $(".scrollSelect") );
		}
		
		// bind the custom submit behavior for the control panel
		$('#graphFormId').submit(function (event) {
			generateGraph();
			event.preventDefault();
		});
		
		// bind changing the asset or level to regenerating the graph automatically
		$('#assetSelectId').on('change', function (event) {
			generateGraph();
		});
		$('#assetClassesId').on('change', function (event) {
			$('#assetSelectId')
				.attr('data-asset-type', $(this).val())
				.data('asset-type', $(this).val());
		});
		$('#levelsUpId').on('change', function (event) {
			generateGraph();
		});
		$('#levelsDownId').on('change', function (event) {
			generateGraph();
		});
		
		// if the page was loaded with an assetId, show the graph for it
		if (! isNaN(initialAssetId)){
			generateGraph();
			if(initialAssetId != null){
				$('#s2id_assetSelectId').find("a").removeClass("select2-default");	
			}
			
		}
	});
	
	// makes an ajax call to get the graph data, then loads it into the DOM
	function generateGraph () {
		
		$('#graphSubmitButtonId').attr('disabled', 'disabled');
		
		// get the params to use for the request
		var params = {};
		if ($('#assetSelectId').val() != '') {
			params.assetId = $('#assetSelectId').val();
		} else if (initialAssetId) {
			params.assetId = initialAssetId;
		} else {
			return;
		}
		params.levelsUp = $('#levelsUpId').val();
		params.levelsDown = $('#levelsDownId').val();
		params.mode = 'assetId';
		
		// make the ajax request for the graph data
		jQuery.ajax({
			dataType: 'json',
			url: 'applicationArchitectureGraph',
			data: params,
			type:'GET',
			complete: loadGraph
		});
		
		var svgElement = d3.select('#graphSvgId');
		if (svgElement.size() == 0) {
			var spinnerDiv = $('#spinnerDivId').clone().css('display','block');
			$('#svgContainerId').html(spinnerDiv);
		} else {
			svgElement.attr('class', 'loading');
		}
	}
	
	// loads the graph code into the DOM
	function loadGraph (response) {
		$('#svgContainerId').html(response.responseText);
	}
	
	function modifyParameter (action, element) {
		var input = $(element).parent().children('input,select');
		var ids = ['levelsUpId', 'levelsDownId'];
		if (action == 'add') {
			if (ids.indexOf(input.attr('id')) != -1)
				$('#' + input.attr('id')).val(parseInt($('#' + input.attr('id')).val()) + 1);
		} else if (action == 'sub') {
			if (ids.indexOf(input.attr('id')) != -1)
				$('#' + input.attr('id')).val(parseInt($('#' + input.attr('id')).val()) - 1);
		}
		input.trigger('change');
	}
</script>

</body>
</html>