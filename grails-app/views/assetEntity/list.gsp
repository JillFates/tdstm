<%@page import="com.tds.asset.AssetComment" %>
<%@page import="com.tds.asset.AssetEntity" %>
<%@page import="com.tds.asset.Application" %>
<%@page import="com.tds.asset.Database" %>
<%@page import="com.tds.asset.Files" %>
<%@page import="net.transitionmanager.security.Permission"%>

<g:set var="assetClass" value="${(new AssetEntity()).assetClass}" />

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="topNav" />
		<title>${title}</title>
		<g:javascript src="asset.tranman.js" />
		<g:javascript src="entity.crud.js" />
		<g:javascript src="model.manufacturer.js"/>

		<g:render template="../layouts/responsiveAngularResources" />
		<script type="text/javascript" src="${resource(dir:'components/manufacturer',file:'manufacturer.js')}"></script>

		<g:javascript src="asset.comment.js" />
		<jqgrid:resources />
		<g:javascript src="jqgrid-support.js" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'jquery.autocomplete.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.accordion.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.resizable.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.slider.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.tabs.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datepicker.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datetimepicker.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
		<link href="/tdstm/css/jqgrid/ui.jqgrid.css" rel="stylesheet" type="text/css" />
		<script type="text/javascript">
			// TODO : move this code to JS once verified in tmdev
			$(document).on('entityAssetUpdated',function (e,obj) {
				$("#messageId").html(obj.asset.assetName + ' Updated').show();
				$('#assetListIdGrid').trigger("reloadGrid");
			});
			$(document).on('entityAssetCreated',function (e,obj) {
                if(obj != null) {
                    $("#messageId").html(obj.asset.assetName + ' Created').show();
                }
				$('#assetListIdGrid').trigger("reloadGrid");
			});
			$(document).ready(function() {
				$(window).unbind('storage').bind('storage', function (e) {
					var id = $('#showEntityView .assetEntity').data('id');
					if(id && localStorage.getItem('editAsset')) {
						$('#assetListIdGrid').trigger("reloadGrid");
						$('#modelShowDialog').dialog("close")
						localStorage.removeItem('editAsset');
						EntityCrud.showAssetDetailView('DEVICE', id);
					}
				});
				
				$("#createEntityView").dialog({ autoOpen: false });
				$("#showEntityView").dialog({ autoOpen: false });
				$("#editEntityView").dialog({ autoOpen: false });
				$("#manufacturerShowDialog").dialog({ autoOpen: false });
				$("#modelShowDialog").dialog({ autoOpen: false });
				$("#editManufacturerView").dialog({ autoOpen: false, close:function(){
					var id = $('#showEntityView .assetEntity').data('id');
					EntityCrud.showAssetDetailView('DEVICE', id);
					$('#assetListIdGrid').trigger("reloadGrid");
				}});
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
				var sourceLocationName = '${sourceLocationName}';
				var sourceRackName = '${sourceRackName}';
				var targetLocationName = '${targetLocationName}';
				var targetRackName = '${targetRackName}';
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
				var listCaption = '<tds:hasPermission permission="${Permission.AssetEdit}">\
					Device:&nbsp;&nbsp;\
					<span class="button">\
						<input type="button" value="Create Device" class="create" \
							onclick="EntityCrud.showAssetCreateView(\'${assetClass}\');"/>\
					</span></tds:hasPermission>\
					<tds:hasPermission permission="${Permission.AssetDelete}"> \
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
					colModel="{name:'act', index: 'act' , sortable: false, formatter:myCustomFormatter, search:false,width:'90', fixed:true},
						{name:'assetName',index: 'assetName', formatter: myLinkFormatter, width:'250'},
						{name:'assetType', width:'110', formatter:tdsCommon.jqgridTextCellFormatter},
						{name:'manufacturer', width:'120', formatter:tdsCommon.jqgridTextCellFormatter},
						{name:'model', width:'150', formatter:tdsCommon.jqgridTextCellFormatter},
						{name:'sourceLocationName', formatter:tdsCommon.jqgridTextCellFormatter, hidden: true},
						{name:'${assetPref['1']}', width:'130', formatter: tdsCommon.jqgridPrefCellFormatter},
						{name:'${assetPref['2']}', width:'130', formatter: tdsCommon.jqgridPrefCellFormatter},
						{name:'${assetPref['3']}', width:'130', formatter: tdsCommon.jqgridPrefCellFormatter}, 
						{name:'${assetPref['4']}', width:'130', formatter: tdsCommon.jqgridPrefCellFormatter},
						{name:'${assetPref['5']}', width:'130', formatter: tdsCommon.jqgridPrefCellFormatter},
						{name:'planStatus', formatter:tdsCommon.jqgridTextCellFormatter},
						{name:'moveBundle', formatter:tdsCommon.jqgridTextCellFormatter},
						{name:'id', hidden: true},
						{name:'commentType', hidden: true} "
					sortname="'assetName'"
					caption="listCaption"
					width="windowWidth"
					rowNum="sizePref"
					rowList="${ raw(com.tdsops.common.ui.Pagination.optionsAsText()) }"
					multiselect="true"
					loadComplete="initCheck"
					gridComplete="function(){bindResize('assetListId');recompileDOM('assetListIdWrapper');}"
					onSelectRow="validateMergeCount"
					showPager="true"
					postData="{filter: filter, event:event, type:type, plannedStatus:plannedStatus, assetName:assetName, planStatus:planStatus, moveBundle:moveBundle,
						moveBundle : moveBundle, assetType:assetType , model :model , sourceLocationName: sourceLocationName , sourceRackName:sourceRackName,
						targetLocationName:targetLocationName, targetRackName :targetRackName,assetTag :assetTag,serialNumber:serialNumber, moveBundleId:moveBundleId, manufacturer: manufacturer,
						unassigned:unassigned, toValidate:toValidate }">
				<jqgrid:navigation id="assetListId" add="false" edit="false" del="false" search="false" refresh="false" />
				<jqgrid:refreshButton id="assetListId" />
				</jqgrid:grid>
				TDS.jqGridFilterToolbar('assetListId');

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
					var value = cellvalue ? _.escape(cellvalue) : '';
					return '<a href="javascript:EntityCrud.showAssetDetailView(\'${assetClass}\',' + options.rowId + ');">' + value + '</a>';
				}

				function myCustomFormatter (cellVal,options,rowObject) {
					var actionButton = ''
					if (${hasPerm}) {
                        actionButton += '<a href="javascript:EntityCrud.showAssetEditView(\'${assetClass}\',' + options.rowId + ');" title=\'Edit Asset\'>' +
							"<img src='${resource(dir:'icons',file:'database_edit.png')}' border='0px'/>"+"</a>&nbsp;&nbsp;"
					}
                    actionButton += "<grid-buttons asset-id='" + options.rowId + "' asset-type='" + rowObject[2] +
                        "' tasks='" + rowObject[13] + "' comments='" + rowObject[16] +
                        "' can-view-tasks='" + ${canViewTasks} + "' can-view-comments='" + ${canViewComments} +
                        "' can-create-tasks='" + ${canCreateTasks} + "' can-create-comments='" + ${canCreateComments} + "'>" +
                        "</grid-buttons>"

					<tds:hasPermission permission="${Permission.AssetCreate}">
						var value = rowObject[1] ? _.escape(rowObject[1]) : ''
						actionButton += '&nbsp;&nbsp;<a href="javascript:EntityCrud.cloneAssetView(\'${assetClass}\', \'' + value + '\', '+options.rowId+');" title=\'Clone Asset\'>'+
							"<img src='${resource(dir:'icons',file:'database_copy.png')}' border='0px'/>"+"</a>";
                    </tds:hasPermission>
					return actionButton;
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
					$("#gs_sourceLocationName").val('${sourceLocationName}')
					$("#gs_sourceRackName").val('${sourceRackName}')
					$("#gs_targetLocationName").val('${targetLocationName}')
					$("#gs_targetRackName").val('${targetRackName}')
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
		<tds:subHeader title="${title} ${(event)?(' for Move Event '+moveEvent.name):('')}" crumbs="['Assets', title]"/>
		<div class="body fluid" ng-app="tdsAssets" ng-controller="tds.assets.controller.MainController as assets">
			<g:render template="../assetEntity/listTitleAlerts" ></g:render>
			<g:each var="key" in="['1','2','3','4','5']">
				<div id="columnCustomDiv_${assetPref[key]}" style="display:none;">
					<div class="columnDiv_${key} customScroll customizeDiv">
						<input type="hidden" id="previousValue_${key}" value="${assetPref[key]}" />
						%{--<g:each var="attribute" in="${attributesList}">--}%
						<g:each var="attribute" in="${fieldSpecs}">
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
		<div id="manufacturersScopeId" ng-app="tdsManufacturers" ng-controller="tds.manufacturers.controller.MainController as manufacturers">
			<g:render template="modelDialog"/>
		</div>
		<g:render template="../assetEntity/entityCrudDivs" />
		<g:render template="../assetEntity/dependentAdd" />
		<g:render template="../assetEntity/initAssetEntityData"/>
		<script>
			currentMenuId = "#assetMenu";
			var filter = '${filter}';
			if(filter && filter == 'all') {
				$($(".menu-parent-assets-all-list")[1]).addClass('active');
			} else if(filter && filter == 'storage') {
				$($(".menu-parent-assets-storage-list")[1]).addClass('active');
			} else {
				$($(".menu-parent-assets-server-list")[1]).addClass('active');
			}
			$(".menu-parent-assets").addClass('active');

			(function($) {
				angular.bootstrap(document.getElementById("manufacturersScopeId"), ['tdsManufacturers']);
			})(jQuery);

		</script>
	</body>
</html>
