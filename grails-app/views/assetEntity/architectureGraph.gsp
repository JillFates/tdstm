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
			<span id="panelLink" class="noPadding">
				<div id="mapReferenceId">
					<div id="controlPanelTabId" class="graphPanelTab activeTab" onclick="GraphUtil.togglePanel('control')"><h4>Control Panel</h4></div><!-- This comment prevents the browser from trying to evaluate the whitespace between these divs as a space character
					--><div id="legendTabId" class="graphPanelTab" onclick="GraphUtil.togglePanel('legend')"><h4>Legend</h4></div>
					<div id="fullscreenButtonId" class="showMenu" onclick="GraphUtil.toggleFullscreen()" title="Toggles fullscreen mode"><h4>Fullscreen</h4></div>
				</div>
			</span>
		
			<div id="controlPanel" class="graphPanel openPanel">
				<table class="labelTree" cellpadding="0" cellspacing="0" style="border: 0;" >
					<tr title="Sets the asset to use as the root node">
						<td class="controlPanelControl" colspan="3">
							<input type="hidden" id="assetSelectId" name="assetList" class="scrollSelect" style="width:130px" data-asset-type="ALL" />
							<label for="assetSelectId">&nbsp;Asset</label>
						</td>
					</tr>
				</table>
				<form id="preferencesformId">
					<table class="labelTree savedToPrefs" cellpadding="0" cellspacing="0" style="border: 0;" >
						<tr title="Filters which class of assets are searched Asset search above">
							<td class="controlPanelControl" colspan="3">
								<g:select name="assetClasses" id="assetClassesId" from="${assetClassesForSelect.values()}" keys="${assetClassesForSelect.keySet()}" value="${(graphPrefs.assetClasses) ?: defaultPrefs.assetClasses}"></g:select>
								<label for="assetClassesId">&nbsp;Filter On</label>
							</td>
						</tr>
						<tr title="Sets the max number of links to follow up">
							<td class="controlPanelControl" colspan="3">
								<img src="${resource(dir:'images',file:'minus.gif')}" class="pointer plusMinusIcon disabled minus" onclick="modifyParameter('sub', 'levelsUpId')"/>
								<input name="levelsUp" id="levelsUpId" class="controlPanelprop" value="${(graphPrefs.levelsUp) ?: levelsUp}" size="1" disabled="disabled" />
								<img src="${resource(dir:'images',file:'plus.gif')}" class="pointer plusMinusIcon disabled plus" onclick="modifyParameter('add', 'levelsUpId')"/>
								<label for="levelsUpId">&nbsp;Tiers Up</label>
							</td>
						</tr>
						<tr title="Sets the max number of links to follow down">
							<td class="controlPanelControl" colspan="3">
								<img src="${resource(dir:'images',file:'minus.gif')}" class="pointer plusMinusIcon minus" onclick="modifyParameter('sub', 'levelsDownId')"/>
								<input name="levelsDown" id="levelsDownId" class="controlPanelprop" value="${(graphPrefs.levelsDown) ?: levelsDown}" disabled="disabled" />
								<img src="${resource(dir:'images',file:'plus.gif')}" class="pointer plusMinusIcon plus" onclick="modifyParameter('add', 'levelsDownId')"/>
								<label for="assetSelectId">&nbsp;Tiers Down</label>
							</td>
						</tr>
						<tr title="Highlights cyclical structures in the dependency tree">
							<td class="controlPanelControl" colspan="3">
								<input type="checkbox" id="highlightCyclicalCheckBoxId" name="showCycles" class="pointer" ${(graphPrefs.showCycles) ? 'checked' : ''} />
								<label for="highlightCyclicalCheckBoxId" class="pointer">&nbsp;Show Cyclical Refs</label>
							</td>
						</tr>
						<tr title="Sets the color of the background to black">
							<td class="controlPanelControl" colspan="3">
								<input type="checkbox" id="blackBackgroundId" name="blackBackground" class="pointer" value="true" ${(graphPrefs.blackBackground)?('checked="checked"'):('')} onchange="rebuildMap(false)">
								<label for="blackBackgroundId" class="pointer">&nbsp;Black Background</label>
							</td>
						</tr>
						<tr>
							<td colspan="3" class="noPadding">
								<br />
							</td>
						</tr>
						
						<!-- Label checkboxes -->
						<tr id="twistieRowId" class="closed">
							<td colspan="3" class="noPadding">
								Show Labels:&nbsp;<svg class="pointer" style="width: 12px;height: 12px;border-width: 0px;" onclick="GraphUtil.toggleGraphTwistie()"><g transform="rotate(90 6 6)"><g id="twistieId" class=""><path d="M10 6 L4 10 L4 2 Z" class="link NotApplicable"></g></g></svg>
							</td>
						</tr>
					</table>
					<div id="layoutControlContainerId" style="display:none;">
						<table class="labelTree savedToPrefs" cellpadding="0" cellspacing="0">
							<tr class="labelToggleRow">
								<td colspan="1">
									<input type="checkbox" id="Application" name="appLbl" value="true" ${(graphPrefs.appLbl) ? 'checked' : ''} class="pointer application" onchange="rebuildMap(false)">
								</td>
								<td colspan="2" class="labelToggleCol noPadding">
									<label for="Application" class="pointer">
										<svg><use xlink:href="${'#applicationShapeId'}" class="node" x="15" y="15" style="fill: #1f77b4; stroke: #666666;"></use></svg>
										Applications
									</label>
								</td>
							</tr>
							<tr class="labelToggleRow">
								<td>
									<input type="checkbox" name="srvLbl" id="Server" ${(graphPrefs.srvLbl) ? 'checked' : ''} value="true" class="pointer serverPhysical serverVirtual" onchange="rebuildMap(false)"/>
								</td>
								<td colspan="2" class="labelToggleCol noPadding">
									<label for="Server" class="pointer">
										<svg><use xlink:href="${'#serverPhysicalShapeId'}" class="node" x="15" y="15" style="fill: #1f77b4; stroke: #666666;"></use></svg>
										Servers
									</label>
								</td>
							</tr>
							<tr class="labelToggleRow">
								<td>
									<input type="checkbox" name="dbLbl" id="Database" ${(graphPrefs.dbLbl) ? 'checked' : ''} value="true" class="pointer database" onchange="rebuildMap(false)"/>
								</td>
								<td colspan="2" class="labelToggleCol noPadding">
									<label for="Database" class="pointer">
										<svg><use xlink:href="${'#databaseShapeId'}" class="node" x="15" y="15" style="fill: #1f77b4; stroke: #666666;"></use></svg>
										Databases
									</label>
								</td>
							</tr>
							<tr class="labelToggleRow">
								<td>
									<input type="checkbox" name="spLbl" id="StoragePhysical" ${(graphPrefs.spLbl) ? 'checked' : ''} value="true" class="pointer storagePhysical" onchange="rebuildMap(false)"/>
								</td>
								<td colspan="2" class="labelToggleCol noPadding">
									<label for="StoragePhysical" class="pointer">
										<svg><use xlink:href="${'#storagePhysicalShapeId'}" class="node" x="15" y="15" style="fill: #1f77b4; stroke: #666666;"></use></svg>
										Storage Devices
									</label>
								</td>
							</tr>
							<tr class="labelToggleRow">
								<td>
									<input type="checkbox" name="slLbl" id="Files" ${(graphPrefs.slLbl) ? 'checked' : ''} value="true" class="pointer storageLogical" onchange="rebuildMap(false)"/>
								</td>
								<td colspan="2" class="labelToggleCol">
									<label for="Files" class="pointer">
										<svg><use xlink:href="${'#storageLogicalShapeId'}" class="node" x="15" y="15" style="fill: #1f77b4; stroke: #666666;"></use></svg>
										Logical Storage
									</label>
								</td>
							</tr>
							<tr class="labelToggleRow">
								<td>
									<input type="checkbox" name="netLbl" id="Network" ${(graphPrefs.netLbl) ? 'checked' : ''} value="true" class="pointer networkPhysical networkLogical" onchange="rebuildMap(false)"/>
								</td>
								<td colspan="2" class="labelToggleCol noPadding">
									<label for="Network" class="pointer">
										<svg><use xlink:href="${'#networkPhysicalShapeId'}" class="node" x="15" y="15" style="fill: #1f77b4; stroke: #666666;"></use></svg>
										Network
									</label>
								</td>
							</tr>
						</table>
					</div>
					<table class="labelTree" cellpadding="0" cellspacing="0">
						
						<tr>
							<td colspan="3" class="noPadding">
								<br />
							</td>
						</tr>
						
						<tr title="Sets the maximum number of rows the labels can use when stacking">
							<td class="controlPanelControl" colspan="3">
								<g:select name="labelOffset" id="labelOffsetId" from="${1..4}" value="${(graphPrefs.labelOffset) ?: defaultPrefs.assetClasses}"></g:select>
								&nbsp;Max Label Offset
							</td>
						</tr>
						<tr title="Sets the distance between nodes">
							<td class="controlPanelControl" colspan="3">
								<img src="${resource(dir:'icons',file:'arrow_in.png')}" id="spacingDecreaseId" height="20" class="pointer plusMinusIcon"/>
								&nbsp;
								<img src="${resource(dir:'icons',file:'arrow_out.png')}" id="spacingIncreaseId" height="20" class="pointer plusMinusIcon"/>
								&nbsp;Spacing
							</td>
						</tr>
						
					</table>
				</form>
					
				<!-- Preference controls -->
				<table class="labelTree" cellpadding="0" cellspacing="0">
						
					<tr>
						<td colspan="3" class="noPadding">
							<br />
						</td>
					</tr>
					
					<tr title="Reloads the graph">
						<td colspan="3" class="noPadding">
							<input type="button" name="Submit Button" id="graphSubmitButtonId" class="pointer fullButton" value="Regenerate Graph">
						</td>
					</tr>
					<tr>
						<td colspan="3" class="noPadding">
							<input id="updatePrefsButtonId" type="button" value="Save Preferences" class="pointer fullButton" onclick="GraphUtil.updateUserPrefs('archGraph')">
						</td>
					</tr>
					<tr>
						<td colspan="3" class="noPadding">
							<input id="resetPrefsButtonId" type="button" value="Reset Defaults" class="pointer fullButton" onclick="GraphUtil.resetToDefaults('archGraph')">
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
	var parameterRanges = {'levelsUp':[0, 9], 'levelsDown':[0, 9]};
	var defaultPrefs = ${defaultPrefs};

	// Used to track ajax requests and abort them when needed
	var ajaxRequest;
	
	$(document).ready(function () {
		// close the labels twistie by default
		$('#layoutControlContainerId').slideUp(0);

		
		// define the select2 for assets
		if (!isIE7OrLesser) {
			EntityCrud.assetNameSelect2( $(".scrollSelect") );
		}
		
		// bind the custom submit behavior for the control panel
		$('#graphSubmitButtonId').on('click', function (event) {
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
		
		// disable any plus/minus icons that should be initially disabled
		GraphUtil.checkForDisabledButtons(parameterRanges);
		
		// if the page was loaded with an assetId, show the graph for it
		if (! isNaN(initialAssetId)){
			generateGraph();
			if(initialAssetId != null){
				$('#s2id_assetSelectId').find("a").removeClass("select2-default");	
			}
		}
		
		generateGraph();
	});
	
	// makes an ajax call to get the graph data, then loads it into the DOM
	function generateGraph () {
		
		$('#graphSubmitButtonId').attr('disabled', 'disabled');
		
		// abort the last ajax request if it still hasn't completed
		if (ajaxRequest)
			ajaxRequest.abort();
		
		// get the params to use for the request
		var params = {};
		if ($('#assetSelectId').val() != '') {
			params.assetId = $('#assetSelectId').val();
		} else if (initialAssetId) {
			params.assetId = initialAssetId;
		} else {
			params.assetId = -1;
		}
		params.levelsUp = $('#levelsUpId').val();
		params.levelsDown = $('#levelsDownId').val();
		params.mode = 'assetId';
		
		// make the ajax request for the graph data
		ajaxRequest = jQuery.ajax({
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
		ajaxRequest = null;
		$('#svgContainerId').html(response.responseText);
	}
	
	function modifyParameter (action, element) {
		var input = $('#' + element);
		var ids = ['levelsUpId', 'levelsDownId'];
		var oldValue = parseInt($('#' + input.attr('id')).val());
		var plusButton = input.parent().children('.plus');
		var minusButton = input.parent().children('.minus');
		
		
		if (action == 'add') {
			if (plusButton.hasClass('disabled'))
				return;
			if (ids.indexOf(input.attr('id')) != -1)
				input.val(Math.min(oldValue + 1, parameterRanges[input.attr('name')][1]));
		} else if (action == 'sub') {
			if (minusButton.hasClass('disabled'))
				return;
			if (ids.indexOf(input.attr('id')) != -1)
				input.val(Math.max(oldValue - 1, parameterRanges[input.attr('name')][0]));
		}
		if (input.val() == parameterRanges[input.attr('name')][0])
			minusButton.addClass('disabled');
		else
			minusButton.removeClass('disabled');
		if (input.val() == parameterRanges[input.attr('name')][1])
			plusButton.addClass('disabled');
		else
			plusButton.removeClass('disabled');
		input.trigger('change');
	}
</script>

</body>
</html>