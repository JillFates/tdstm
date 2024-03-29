<%@page defaultCodec="none" %>
<html>
<head>
	<meta name="layout" content="topNav" />
	<title>Architecture Graph</title>
	<g:javascript src="asset.tranman.js" />
	<g:javascript src="entity.crud.js" />
	<g:javascript src="model.manufacturer.js"/>
	<g:javascript src="projectStaff.js" />

	<g:render template="/layouts/responsiveAngularResources" />

	<g:javascript src="asset.comment.js" />

	<asset:stylesheet href="css/ui.datepicker.css" />
	<g:javascript src="d3/d3.js" />
	<g:javascript src="lodash/lodash.min.js" />
	<g:javascript src="svg.js"/>
	<g:javascript src="load.shapes.js"/>
	<g:javascript src="keyevent_constants.js" />
	<g:javascript src="graph.js" />
	<asset:stylesheet href="css/force.css" />
</head>
<body>
<tds:subHeader title="Architecture Graph" crumbs="['Assets','Architecture']"/>
<div id="body" class="body" ng-app="tdsComments" ng-controller="tds.comments.controller.MainController as comments">
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>

	<div id="item1" class="graphContainer">
		<div id="toolsContainerId">
			<div id="graphToolbarId">
				<div class="btn-group">
					<button id="controlPanelTabId" class="btn activeTab" onclick="GraphUtil.togglePanel(GraphUtil.PANELS.CONTROL)">Control Panel</button>
					<button id="legendTabId" class="btn" onclick="GraphUtil.togglePanel(GraphUtil.PANELS.LEGEND)">Legend</button>
				</div>
				<button id="fullscreenButtonId" class="btn btn-outline"  onclick="GraphUtil.toggleFullscreen()" title="Toggles fullscreen mode">Fullscreen</button>
			</div>

			<div id="controlPanelId" class="graphPanel ">
				<table class="labelTree" cellpadding="0" cellspacing="0" style="border: 0;" >
					<tr title="Filters which class of assets are searched Asset search above">
						<td class="controlPanelControl" colspan="3">
							<input type="hidden" id="assetClassesId" name="assetClasses" class="filterScrollSelect" style="width:130px" />
						</td>
					</tr>
				</table>
				<form id="preferencesformId">
					<table class="labelTree savedToPrefs" cellpadding="0" cellspacing="0" style="border: 0;" >
						<tr title="Sets the asset to use as the root node">
							<td class="controlPanelControl" colspan="3">
								<input type="hidden" id="assetSelectId" name="assetList" class="scrollSelect" style="width:130px" data-asset-type="ALL" />
							</td>
						</tr>
						<tr title="Sets the max number of links to follow up">
							<td class="controlPanelControl" colspan="3">
								<span style="font-size: 14px; cursor: pointer;" class="pointer plusMinusIcon disabled minus" onclick="modifyParameter('sub', 'levelsUpId')"><i class="fas fa-minus-circle"></i></span>
								<input name="levelsUp" id="levelsUpId" class="controlPanelprop" value="${(graphPrefs.levelsUp) ?: levelsUp}" size="1" disabled="disabled" />
								<span style="font-size: 14px; cursor: pointer;" class="pointer plusMinusIcon disabled plus" onclick="modifyParameter('add', 'levelsUpId')"><i class="fas fa-plus-circle"></i></span>
								<label for="levelsUpId">&nbsp;Tiers Above</label>
							</td>
						</tr>
						<tr title="Sets the max number of links to follow down">
							<td class="controlPanelControl" colspan="3">
								<span style="font-size: 14px;" class="pointer plusMinusIcon disabled minus" onclick="modifyParameter('sub', 'levelsDownId')"><i class="fas fa-minus-circle"></i></span>
								<input name="levelsDown" id="levelsDownId" class="controlPanelprop" class="pointer plusMinusIcon disabled plus" value="${(graphPrefs.levelsDown) ?: levelsDown}" disabled="disabled" />
								<span style="font-size: 14px;" onclick="modifyParameter('add', 'levelsDownId')"><i class="fas fa-plus-circle"></i></span>
								<label for="assetSelectId">&nbsp;Tiers Below</label>
							</td>
						</tr>
						<tr title="Highlights cyclical structures in the dependency tree">
							<td class="controlPanelControl" colspan="3">
								<div class="clr-form-control" style="margin-top:unset;">
									<div class="clr-control-container">
										<div class="clr-checkbox-wrapper">
											<input type="checkbox" id="highlightCyclicalCheckBoxId" name="showCycles" value="true" ${(graphPrefs.showCycles) ? 'checked' : ''} />
											<label class="clr-control-label" for="highlightCyclicalCheckBoxId">Show Cyclical References</label>
										</div>
									</div>
								</div>
							</td>
						</tr>
						<tr title="Sets the color of the background to black">
							<td class="controlPanelControl" colspan="3">
								<div class="clr-form-control" style="margin-top:unset;">
									<div class="clr-control-container">
										<div class="clr-checkbox-wrapper">
											<input type="checkbox" id="blackBackgroundId" name="blackBackground" value="true" ${(graphPrefs.blackBackground)?('checked="checked"'):('')} onchange="rebuildMap(false)">
											<label class="clr-control-label" for="blackBackgroundId" class="pointer">Black Background</label>
										</div>
									</div>
								</div>
							</td>
						</tr>
						<tr>
							<td colspan="3" class="noPadding">
								<br />
							</td>
						</tr>

						<!-- Label checkboxes -->
						<tr id="twistieRowId">
							<td colspan="3" class="noPadding">
								<span id="twistieSpanId" class="closed pointer" onclick="GraphUtil.toggleGraphTwistie($(this))" for="labelControlContainerId">
									Show Labels:&nbsp;<!--
									--><svg style="width: 12px;height: 12px;border-width: 0px;"><g transform="rotate(90 6 6)"><g id="twistieId"><path d="M10 6 L4 10 L4 2 Z" class="link NotApplicable"></g></g></svg>
								</span>
							</td>
						</tr>

					</table>
					<div id="labelControlContainerId">
						<table class="labelTree" cellpadding="0" cellspacing="0" style="margin-left: 5px;border: 0;" >
							<g:each in="${assetTypes}" var="entry" status="i">
								<g:set var="type" value="${entry.getKey()}" />
								<g:set var="names" value="${entry.getValue()}" />
								<g:if test="${ ! names.labelHandles.equals('') }">
									<tr class="labelToggleRow">
										<td colspan="3" class="labelToggleCol">
											<div style="padding:0px;">
												<div class="checkboxContainer">
													<div class="clr-form-control" style="margin-top:unset;">
														<div class="clr-control-container">
															<div class="clr-checkbox-wrapper">
																<input type="checkbox" id="${type}CheckboxId" name="${names.labelPreferenceName}" value="true" ${(graphPrefs[names.labelPreferenceName]) ? 'checked' : ''} class="pointer ${names.labelHandles}" onchange="rebuildMap(false)" />
																<label for="${type}CheckboxId" class="clr-control-label">
																	<svg id="${names.internalName}ShapeLeftPanel"><use xlink:href="#${names.internalName}ShapeId" class="node" x="15" y="15" style="fill: #1f77b4;"></use></svg>
																	${names.labelText ?: names.frontEndName}
																</label>
															</div>
														</div>
													</div>
												</div>
											</div>
										</td>
									</tr>
								</g:if>
							</g:each>
						</table>
					</div>
					<table class="labelTree" cellpadding="0" cellspacing="0">

						<tr>
							<td colspan="3" class="noPadding">
								<br />
							</td>
						</tr>

						<tr title="Sets the distance between nodes">
							<td class="controlPanelControl" colspan="3">
								<div style="display: flex;">
									<div style="font-size: 18px; color: #0092d1; margin-right: 10px; cursor: pointer;" id="spacingDecreaseId"><i class="fas fa-expand-arrows-alt"></i></div>
									<div style="font-size: 18px; color: #0092d1; margin-right: 10px; cursor: pointer;" id="spacingIncreaseId"><i class="fas fa-compress-arrows-alt"></i></div>
									<div>Spacing</div>
								</div>
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
							<input type="button" name="Submit Button" id="graphSubmitButtonId" class="btn btn-sm btn-outline btn-block" value="Regenerate Graph">
						</td>
					</tr>
					<tr>
						<td colspan="3" class="noPadding">
							<input id="updatePrefsButtonId" type="button" value="Save Preferences" class="btn btn-sm btn-outline btn-block" onclick="GraphUtil.updateUserPrefs('${com.tdsops.tm.enums.domain.UserPreferenceEnum.ARCH_GRAPH.name()}')">
						</td>
					</tr>
					<tr>
						<td colspan="3" class="noPadding">
							<input id="resetPrefsButtonId" type="button" value="Reset Defaults" class="btn btn-sm btn-outline btn-block" onclick="GraphUtil.resetToDefaults('${com.tdsops.tm.enums.domain.UserPreferenceEnum.ARCH_GRAPH.name()}')">
						</td>
					</tr>
				</table>
			</div>

			<g:include controller="assetEntity" action="graphLegend" params="${[displayMoveEvents:false, displayFuture:true, displayCycles:true, displayBundleConflicts:false, arrowheadOffset:true]}" />
		</div>
		<div id="svgContainerId"></div>
		<div id="spinnerDivId" style="display: none"></div>
	</div>

	<g:render template="/assetEntity/modelDialog"/>
	<g:render template="/assetEntity/entityCrudDivs" />
	<g:render template="/assetEntity/dependentAdd" />
	<g:render template="/layouts/error"/>
	<div id="createStaffDialog" style="display:none;" class="static-dialog">
		<g:render template="/person/createStaff" model="['forWhom':'application']"></g:render>
	</div>
</div>
<g:render template="/assetEntity/initAssetEntityData"/>

<script type="text/javascript">

	// if the user navigated here from an asset crud button, there will be a specific asset to start with
	var initialAssetId = ${assetId ?: 'null'};
	var parameterRanges = {'levelsUp':[0, 10], 'levelsDown':[0, 10]};
	var defaultPrefs = ${raw(defaultPrefs)};
	// var assetSelectWidth = 172;

	// Used to track ajax requests and abort them when needed
	var ajaxRequest;

	$(document).ready(function () {
		// close the labels twistie by default
		$('#labelControlContainerId').slideUp(0);


		// define the select2 for assets
		if (!isIE7OrLesser) {
			EntityCrud.assetNameSelect2( $(".scrollSelect") );
			$("#select2-chosen-1").html('Select an Asset');
			filterSelect2( $(".filterScrollSelect"), ${assetClassesForSelect2} );
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

		// set the initial value of the asset classes dropdown based on user preference
		$('#assetClassesId').val('${graphPrefs.assetClasses ?: ('')}').trigger("change");

		// disable any plus/minus icons that should be initially disabled
		GraphUtil.checkForDisabledButtons(parameterRanges);

		// if the page was loaded with an assetId, show the graph for it
		if (! isNaN(initialAssetId)){
			generateGraph();
			if(initialAssetId != null){
				$('#s2id_assetSelectId').find("a").removeClass("select2-default");
			}
		}

		// set the width for the asset select2
		$('#s2id_assetSelectId').css('width', '100%');

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
		if ($('#assetSelectId').val() !== '') {
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
		if (svgElement.size() === 0) {
			var spinnerDiv = $('#spinnerDivId').clone().css('display','block');
			$('#svgContainerId').html(spinnerDiv);
		} else {
			svgElement.attr('class', 'loading');
		}
	}

	// loads the graph code into the DOM
	function loadGraph (response) {
		ajaxRequest = null;
		// $('#svgContainerId').html($.parseHTML(response.responseText, '', true));
		$('#svgContainerId').html(response.responseText);
	}

	function modifyParameter (action, element) {
		var input = $('#' + element);
		var ids = ['levelsUpId', 'levelsDownId'];
		var oldValue = parseInt($('#' + input.attr('id')).val());
		var plusButton = input.parent().children('.plus');
		var minusButton = input.parent().children('.minus');


		if (action === 'add') {
			if (plusButton.hasClass('disabled'))
				return;
			if (ids.indexOf(input.attr('id')) !== -1)
				input.val(Math.min(oldValue + 1, parameterRanges[input.attr('name')][1]));
		} else if (action === 'sub') {
			if (minusButton.hasClass('disabled'))
				return;
			if (ids.indexOf(input.attr('id')) !== -1)
				input.val(Math.max(oldValue - 1, parameterRanges[input.attr('name')][0]));
		}
		if (input.val() === parameterRanges[input.attr('name')][0])
			minusButton.addClass('disabled');
		else
			minusButton.removeClass('disabled');
		if (input.val() === parameterRanges[input.attr('name')][1])
			plusButton.addClass('disabled');
		else
			plusButton.removeClass('disabled');
		input.trigger('change');
	}

	// Asset is on the entity.crud.js because is generic, the filter On is only used here.
	function filterSelect2(element, data) {
		element.select2( {
			minimumInputLength: 0,
			width: '100%',
			placeholder: "Filter: All Classes",
			data: data
		} );
	}

	$(document).ready(function () {
		// Safari doesn't render correctly svg inline, since the D3 is who is injecting, we preload the values injecting in the DOM.
		$('#graphSVGContainer').append(appSVGShapes.getAll());
	});

	$(".menu-parent-assets-architecture-graph").addClass('active');
	$(".menu-parent-assets").addClass('active');

</script>
<div style="display: none;" id="graphSVGContainer"></div>
<div class="tdsAssetsApp" ng-app="tdsAssets" ng-controller="tds.assets.controller.MainController as assets"></div>
</body>
</html>
