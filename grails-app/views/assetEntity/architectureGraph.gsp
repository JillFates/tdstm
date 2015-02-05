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
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'force.css')}" />
	</head>
<body>
<div id="body" class="body" ng-app="tdsComments" ng-controller="tds.comments.controller.MainController as comments">
	<h1 id="pageTitleId">Architecture Graph</h1>
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>
	
	<span id="panelLink" colspan="2" style="padding: 0px;">
		<table id="mapReferenceId">
			<tr>
				<td onclick="openPanel('controlPanel')"><h4>Control Panel</h4></td>
				<td onclick="openPanel('legendDivId')"><h4>Legend</h4></td>
			</tr>
		</table>
	</span>
	
	<div id="controlPanel" style="display: ${/*(showControls=='controls')?('block'):('none')*/'block'};">
		<table id="labelTree" cellpadding="0" cellspacing="0" style="margin-left: 5px;border: 0;" >
			<tr>
				<td style="padding: 3px 3px;" colspan="2"><h3>Control Panel</h3></td>
			</tr>
			<form style="display:inline-block;" id="graphFormId">
				<tr title="Sets the asset to use a the root node">
					<td style="padding: 0px;width: 30px;">Asset</td>
					<td style="padding-left :5px;">
						<input type="hidden" id="assetSelectId" name="assetList" class="scrollSelect" style="width:150px" data-asset-type="ALL" />
					</td>
				</tr>
				<tr title="Sets the max number of links to follow">
					<td style="padding: 0px;width: 30px;">Levels</td>
					<td style="padding-left :5px">
						<g:select name="level" id="levelsId" from="${1..10}" value="${level}"></g:select>
					</td>
				</tr>
				<tr title="Reloads the graph">
					<td style="padding: 0px;"></td>
					<td style="padding: 0px;">
						<input type="submit" name="Submit Button" id="graphSubmitButtonId" value="Regenerate Graph">
					</td>
				</tr>
			</form>
			<tr title="Hides dependencies that are implied by other shown dependencies">
				<td style="padding: 0px;width: 30px;">Hide Redundant</td>
				<td style="padding-left :5px">
					<input type="checkbox" id="hideRedundantCheckBoxId" />
				</td>
			</tr>
			<tr title="Hides dependencies that would cause cycles in the structure">
				<td style="padding: 0px;width: 30px;">Hide Cyclical</td>
				<td style="padding-left :5px">
					<input type="checkbox" id="hideCyclicalCheckBoxId" />
				</td>
			</tr>
			<tr title="Shows labels for each asset">
				<td style="padding: 0px;width: 30px;">Show Labels</td>
				<td style="padding-left :5px">
					<input type="checkbox" id="labelCheckBoxId" checked="checked" />
				</td>
			</tr>
			<tr title="Sets the maximum number of rows the labels can use when stacking">
				<td style="padding: 0px;width: 30px;">Max Label Offset</td>
				<td style="padding-left :5px">
					<g:select name="labelOffset" id="labelOffsetId" from="${1..4}" value="2"></g:select>
				</td>
			</tr>
			<tr title="Sets the distance between nodes horizontally">
				<td style="padding: 0px;width: 30px;">Horizontal Spacing</td>
				<td style="padding-left :5px">
					<input type="text" name="horizontalSpace" id="horizontalSpaceId" value="30" size="4" maxlength="10" style="width:20px;" />
				</td>
			</tr>
			<tr title="Sets the distance between nodes vertically">
				<td style="padding: 0px;width: 30px;">Vertical Spacing</td>
				<td style="padding-left :5px">
					<input type="text" name="verticalSpace" id="verticalSpaceId" value="80" size="4" maxlength="10" style="width:20px;" />
				</td>
			</tr>
			
		</table>
	</div>
	<div id="legendDivId" style="display: ${(showControls=='legend')?('block'):('none')};">
		<table id="legendId" cellpadding="0" cellspacing="0" style="margin-left: 5px;border: 0;width: 140px;" >
			<tr><td style="padding: 3px 3px;" colspan="2"><h3>Legend</h3></td></tr>
				<tr><td colspan="2"><span style="color: blue;"><h4>Nodes:</h4></span></td></tr>
				<tr>
					<td nowrap="nowrap" ><img src="${resource(dir:'images',file:'iconApp.png')}" height="14" /></td>
					<td><span style="vertical-align: text-top;">Applications</span></td>
				</tr>
				<tr>
					<td nowrap="nowrap" ><img src="${resource(dir:'images',file:'iconServer.png')}" height="14" /></td>
					<td><span style="vertical-align: text-top;">Servers</span></td>
				</tr>
				<tr>
					<td nowrap="nowrap" ><img src="${resource(dir:'images',file:'iconDB.png')}" height="14" /></td>
					<td><span style="vertical-align: text-top;">Databases</span></td>
				</tr>
				<tr>
					<td nowrap="nowrap"><img src="${resource(dir:'images',file:'iconStorage.png')}" height="21" /></td>
					<td><span style="vertical-align: text-top;">Storage Devices</span></td>
				</tr>
				<tr>
					<td nowrap="nowrap"><img src="${resource(dir:'images',file:'iconStorage.png')}" height="21" /></td>
					<td><span style="vertical-align: text-top;">Logical Storage</span></td>
				</tr>
				<tr>
					<td nowrap="nowrap"><img src="${resource(dir:'images',file:'iconNetwork.png')}" height="16" /></td>
					<td><span style="vertical-align: text-top;">Network</span></td>
				</tr>
				<tr><td width="5px"><hr style="width: 30px;height: 1px;background-color:rgb(56,56,56);"></hr></td><td>Valid Links</td></tr>
				<tr><td><hr style="width: 30px;height: 1px;background-color:red;"></hr></td><td>Questioned</td></tr>
				<tr><td><hr style="width: 30px;height: 1px;background-color:rgb(224,224,224);"></hr></td><td>N/A</td></tr>
		</table>
	</div>
	
	<br />
	<div id="item1"></div>
	
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
		$('#levelsId').on('change', function (event) {
			generateGraph();
		});
		
		// if the page was loaded with an assetId, show the graph for it
		if (! isNaN(initialAssetId))
			generateGraph();
	});
	
	// makes an ajax call to get the graph data, then loads it into the DOM
	function generateGraph () {
		
		// get the params to use for the request
		var params = {};
		if ($('#assetSelectId').val() != '') {
			params.assetId = $('#assetSelectId').val();
		} else if (initialAssetId) {
			params.assetId = initialAssetId;
		} else {
			return;
		}
		params.level = $('#levelsId').val();
		params.mode = 'assetId';
		
		// make the ajax request for the graph data
		jQuery.ajax({
			dataType: 'json',
			url: 'applicationArchitectureGraph',
			data: params,
			type:'GET',
			complete: loadGraph
		});
	}
	
	// loads the graph code into the DOM
	function loadGraph (response) {
		$('#item1').html(response.responseText);
	}
	
	// handles switching between the control panel and the legend
	function openPanel (source) {
		if ( $('#'+source).css('display') == 'block' ) {
			$('#'+source).css('display', 'none')
		} else if (source == 'controlPanel') {
			$('#controlPanel').css('display','block')
			$('#legendDivId').css('display','none')
		} else if (source == 'legendDivId') {
			$('#controlPanel').css('display','none')
			$('#legendDivId').css('display','block')
		} else if (source == 'hide') {
			$('#controlPanel').css('display','none')
			$('#legendDivId').css('display','none')
		}
	}
</script>

</body>
</html>