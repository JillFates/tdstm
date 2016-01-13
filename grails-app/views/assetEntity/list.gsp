<%@page import="com.tds.asset.AssetComment" %>
<%@page import="com.tds.asset.AssetEntity" %>
<%@page import="com.tds.asset.Application" %>
<%@page import="com.tds.asset.Database" %>
<%@page import="com.tds.asset.Files" %>

<g:set var="assetClass" value="${(new AssetEntity()).assetClass}" />

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="projectHeader" />
		<title>${title}</title>
		<g:javascript src="asset.tranman.js" />
		<g:javascript src="entity.crud.js" />
		<g:javascript src="model.manufacturer.js"/>
		<g:javascript src="angular/angular.min.js" />
		<g:javascript src="angular/plugins/angular-ui.js"/>
		<g:javascript src="angular/plugins/angular-resource.js" />
		<script type="text/javascript" src="${resource(dir:'components/core',file:'core.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'components/comment',file:'comment.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'components/asset',file:'asset.js')}" /></script>
		<g:javascript src="asset.comment.js" />
		<g:javascript src="cabling.js"/>
		<jqgrid:resources />
		<g:javascript src="jqgrid-support.js" />
		<g:javascript src="bootstrap.js" />
		<g:javascript src="angular/plugins/ui-bootstrap-tpls-0.10.0.min.js" />
		<g:javascript src="angular/plugins/ngGrid/ng-grid-2.0.7.min.js" />
		<g:javascript src="angular/plugins/ngGrid/ng-grid-layout.js" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'jquery.autocomplete.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.accordion.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.resizable.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.slider.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.tabs.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datepicker.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datetimepicker.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
		<link href="/tdstm/css/jqgrid/ui.jqgrid.css" rel="stylesheet" type="text/css" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'components/comment',file:'comment.css')}" />
		<script type="text/javascript">
			// TODO : move this code to JS once verified in tmdev

			$(document).ready(function() {
				$("#createEntityView").dialog({ autoOpen: false });
				$("#showEntityView").dialog({ autoOpen: false });
				$("#editEntityView").dialog({ autoOpen: false });
				$("#manufacturerShowDialog").dialog({ autoOpen: false });
				$("#modelShowDialog").dialog({ autoOpen: false });
				$("#editManufacturerView").dialog({ autoOpen: false});
				$("#cablingDialogId").dialog({ autoOpen:false });
				$("#filterPane").draggable();
				var filter = '${filter}';
				var type = '${type}';
				var event = '${event}';
				var plannedStatus = '${plannedStatus}' ;
				var assetName = '${assetName}';
				var planStatus = '${planStatus}';
				var moveBundle = '${moveBundle}';
				var assetType = '${assetType}';
				var model = '${model}';
				var manufacturer = '${manufacturer}';
				var sourceLocation = '${sourceLocation}';
				var sourceRack = '${sourceRack}';
				var targetLocation = '${targetLocation}';
				var targetRack = '${targetRack}';
				var assetTag = '${assetTag}';
				var serialNumber = '${serialNumber}';
				var sortIndex = '${sortIndex}';
				var sortOrder = '${sortOrder}';
				var moveBundleId = '${moveBundleId}';
				var toValidate = '${toValidate}';
				// var justPlanning = '${justPlanning}';
				var windowWidth = $(window).width() - $(window).width()*5/100 ;
				var sizePref = '${sizePref}';
				var unassigned = '${unassigned}';
				var listCaption = '<tds:hasPermission permission="AssetEdit">\
					Device:&nbsp;&nbsp;\
					<span class="button">\
						<input type="button" value="Create Device" class="create" \
							onclick="EntityCrud.showAssetCreateView(\'${assetClass}\');"/>\
					</span></tds:hasPermission>\
					<tds:hasPermission permission="AssetDelete"> \
						<span class="capBtn"><input type="button" id="deleteAssetId" value="Bulk Delete" onclick="deleteAssets(\'AssetEntity\')" disabled="disabled"/></span> \
					</tds:hasPermission> \
					<span><input type="checkbox" id="justPlanning" ${ (justPlanning == "true" ? "checked" : "") } onclick="toggleJustPlanning($(this))"/><label for="justPlanning"> Just Planning</label></span> \
					<g:if test="${fixedFilter}"> \
						<g:link class="mmlink" controller="assetEntity" params="[]" action="list"><span class="capBtn"><input type="button" class="clearFilterId" value="Clear Filters" /></span></g:link> \
					</g:if><g:else> \
						<span class="capBtn"><input type="button" class="clearFilterId" value="Clear Filters" disabled="disabled" onclick="clearFilter(\'assetListId\')"/></span> \
					</g:else>';
				<jqgrid:grid id="assetListId" url="'${createLink(action: 'listJson')}'"
					editurl="'${createLink(action: 'deleteBulkAsset')}'"
					colNames="'Actions', 'Name', 'Device Type', 'Manufacturer', 'Model', 'Location','${modelPref['1']}','${modelPref['2']}', '${modelPref['3']}','${modelPref['4']}','${modelPref['5']}','Plan Status','Bundle', 'id', 'commentType'"
					colModel="{name:'act', index: 'act' , sortable: false, formatter:myCustomFormatter, search:false,width:'65', fixed:true},
						{name:'assetName',index: 'assetName', formatter: myLinkFormatter, width:'250'},
						{name:'assetType', width:'110'},
						{name:'manufacturer', width:'120'},
						{name:'model', width:'150'}, 
						{name:'sourceLocation'},
						{name:'${assetPref['1']}', width:'130', formatter: tdsCommon.jqgridPrefCellFormatter},
						{name:'${assetPref['2']}', width:'130', formatter: tdsCommon.jqgridPrefCellFormatter},
						{name:'${assetPref['3']}', width:'130', formatter: tdsCommon.jqgridPrefCellFormatter}, 
						{name:'${assetPref['4']}', width:'130', formatter: tdsCommon.jqgridPrefCellFormatter},
						{name:'${assetPref['5']}', width:'130', formatter: tdsCommon.jqgridPrefCellFormatter},
						{name:'planStatus'},
						{name:'moveBundle'},
						{name:'id', hidden: true},
						{name:'commentType', hidden: true} "
					sortname="'assetName'"
					caption="listCaption"
					width="windowWidth"
					rowNum="sizePref"
					multiselect="true"
					loadComplete="initCheck"
					gridComplete="function(){bindResize('assetListId');recompileDOM('assetListIdWrapper');}"
					onSelectRow="validateMergeCount"
					showPager="true"
					postData="{filter: filter, event:event, type:type, plannedStatus:plannedStatus, assetName:assetName, planStatus:planStatus, moveBundle:moveBundle,
						moveBundle : moveBundle, assetType:assetType , model :model , sourceLocation: sourceLocation , sourceRack:sourceRack,
						targetLocation:targetLocation, targetRack :targetRack,assetTag :assetTag,serialNumber:serialNumber, moveBundleId:moveBundleId, manufacturer: manufacturer,
						unassigned:unassigned, toValidate:toValidate }">
					<jqgrid:filterToolbar id="assetListId" searchOnEnter="false" />
					<jqgrid:navigation id="assetListId" add="false" edit="false" del="false" search="false" refresh="false" />
					<jqgrid:refreshButton id="assetListId" />
				</jqgrid:grid>
				populateFilter();
				$("#del_assetListIdGrid").click(function() {
					$("#assetListId").jqGrid("editGridRow","new",
						{afterSubmit:deleteMessage});
				});

				<g:each var="key" in="['1','2','3','4','5']">
					var assetPref= '${assetPref[key]}';
					$("#assetListIdGrid_" + assetPref).append("<img src=\"${resource(dir:'images',file:'select2Arrow.png')}\" class=\"selectImage customizeSelect editSelectimage_"+${key}+"\" onclick=\"showSelect('"+assetPref+"','assetList','"+${key}+"')\">");
				</g:each>
			
				$.jgrid.formatter.integer.thousandsSeparator = '';
				function myLinkFormatter (cellvalue, options, rowObject) {
					var value = cellvalue ? cellvalue : '';
					return '<a href="javascript:EntityCrud.showAssetDetailView(\'${assetClass}\',' + options.rowId + ');">' + value + '</a>';
				}

				function myCustomFormatter (cellVal,options,rowObject) {
					var editButton = ''
					if (${hasPerm}) {
						editButton += '<a href="javascript:EntityCrud.showAssetEditView(\'${assetClass}\',' + options.rowId + ');" title=\'Edit Asset\'>' +
							"<img src='${resource(dir:'icons',file:'database_edit.png')}' border='0px'/>"+"</a>&nbsp;&nbsp;"
					}
					editButton += "<grid-buttons asset-id='" + options.rowId + "' asset-type='" + rowObject[2] + "' tasks='" + rowObject[13] + "' comments='" + rowObject[16] + "' can-edit-tasks='true' can-edit-comments='" + ${hasPerm} + "'></grid-buttons>"

					return editButton
				}

				function deleteMessage (response, postdata) {
					$("#messageId").show()
					$("#messageDivId").hide()
					$("#messageId").html(response.responseText)
					$("#delmodassetListIdGrid").remove()
					$(".jqmOverlay").remove()
					return true
				}

				function populateFilter () {
					$("#gs_assetName").val('${assetName}')
					$("#gs_assetType").val('${assetType}')
					$("#gs_model").val('${model}')
					$("#gs_sourceLocation").val('${sourceLocation}')
					$("#gs_sourceRack").val('${sourceRack}')
					$("#gs_targetLocation").val('${targetLocation}')
					$("#gs_targetRack").val('${targetRack}')
					$("#gs_serialNumber").val('${serialNumber}')
					if(planStatus) {
						$("#gs_planStatus").val(planStatus)
					} else if (plannedStatus){
						$("#gs_planStatus").val(plannedStatus)
					}
					
					$("#gs_moveBundle").val('${moveBundle}')
					$("#gs_assetTag").val('${assetTag}')
				}
				
			})
		</script>
	</head>
	<body>
		<div class="body fluid" ng-app="tdsAssets" ng-controller="tds.assets.controller.MainController as assets">
			<h1>${title} ${(event)?(' for Move Event '+moveEvent.name):('')}</h1>
			<g:render template="../assetEntity/listTitleAlerts" ></g:render>
			<g:each var="key" in="['1','2','3','4','5']">
				<div id="columnCustomDiv_${assetPref[key]}" style="display:none;">
					<div class="columnDiv_${key} customScroll customizeDiv">
						<input type="hidden" id="previousValue_${key}" value="${assetPref[key]}" />
						<g:each var="attribute" in="${attributesList}">
							<label><input type="radio" name="coloumnSelector_${assetPref[key]}" id="coloumnSelector_${assetPref[key]}" value="${attribute.attributeCode}" 
								${assetPref[key]==attribute.attributeCode ? 'checked' : '' } style="margin-left:2px;" 
								onchange="setColumnAssetPref(this.value,'${key}','${prefType}')"/> ${attribute.frontendLabel}</label><br>
						</g:each>
					</div>
				</div>
			</g:each>
			<div id="commentScopeId" ng-controller="tds.comments.controller.MainController as comments">
				<jqgrid:wrapper id="assetListId" /> 
			</div>
		</div> <%-- End of Body --%>
		<g:render template="modelDialog"/>
		<g:render template="../assetEntity/entityCrudDivs" />
		<g:render template="../assetEntity/dependentAdd" />
		<g:render template="../assetEntity/initAssetEntityData"/>
		<script>
			currentMenuId = "#assetMenu";
			$("#assetMenuId a").css('background-color','#003366')
		</script>
	</body>
</html>
