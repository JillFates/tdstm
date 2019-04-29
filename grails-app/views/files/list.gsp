<%@page import="com.tds.asset.AssetEntity"%>
<%@page import="com.tds.asset.Application"%>
<%@page import="com.tds.asset.Database"%>
<%@page import="com.tds.asset.Files"%>
<%@page import="net.transitionmanager.security.Permission"%>

<g:set var="assetClass" value="${(new Files()).assetClass}" />

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="topNav" />
		<title>Storage List</title>
		<g:javascript src="asset.tranman.js" />
		<g:javascript src="entity.crud.js" />
		<g:render template="../layouts/responsiveAngularResources" />
		<g:javascript src="asset.comment.js" />
		<jqgrid:resources />
		<g:javascript src="jqgrid-support.js" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'jquery.autocomplete.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.accordion.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.resizable.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.slider.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.tabs.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
		<script type="text/javascript">
			$(document).ready(function() {
                $(document).on('entityAssetUpdated',function (e,obj) {
                    $("#messageId").html(_.escape(obj.asset.assetName) + ' Updated').show();
                    $('#storageIdGrid').trigger("reloadGrid")
                });
                $(document).on('entityAssetCreated',function (e,obj) {
                    if(obj != null) {
                    	$("#messageId").html(_.escape(obj.asset.assetName) + ' Created').show();
                    }
                    $('#storageIdGrid').trigger("reloadGrid")
                });
				//$('#assetMenu').show();
				$("#createEntityView").dialog({ autoOpen: false })
				$("#showEntityView").dialog({ autoOpen: false })
				$("#editEntityView").dialog({ autoOpen: false })
				$("#manufacturerShowDialog").dialog({ autoOpen: false })
				$("#modelShowDialog").dialog({ autoOpen: false })
				$("#cablingDialogId").dialog({ autoOpen:false })

				var filter = '${filter}'
				var event = '${event}'
				var plannedStatus = '${plannedStatus}'
				var validation = '${validation}'
				var	moveBundleId = '${moveBundleId}'
				var fileName = '${fileName}'
				var planStatus = '${planStatus}'
				var moveBundle = '${moveBundle}'
				var fileFormat = '${fileFormat}'
				var size = '${size}'
				var sizePref = '${sizePref}'
				var toValidate = '${toValidate}'
				var unassigned = '${unassigned}'

				var listCaption ='Storages: <tds:hasPermission permission="${Permission.AssetEdit}"> \
					<span class="capBtn">\
						<input type="button" value="Create Storage" onclick="EntityCrud.showAssetCreateView(\'${assetClass}\')"/>\
					</span></tds:hasPermission>\
					<tds:hasPermission permission="${Permission.AssetDelete}">\
						<span class="capBtn"><input type="button" id="deleteAssetId" value="Bulk Delete" onclick="deleteAssets(\'Logical Storage\')" disabled="disabled"/></span>\
					</tds:hasPermission>\
						<span><input type="checkbox" id="justPlanning" ${ (justPlanning == 'true' ? 'checked="checked"': '') } onclick="toggleJustPlanning($(this))"/> Just Planning</span>\
					<g:if test="${fixedFilter}"><g:link class="mmlink" controller="files" action="list"><span class="capBtn"><input type="button" class="clearFilterId" value="Clear Filters" /></span></g:link>\
					</g:if><g:else><span class="capBtn"><input type="button" class="clearFilterId" value="Clear Filters" disabled="disabled" onclick="clearFilter(\'storageId\')"/></g:else></span>'
				// JqGrid implementations
				<jqgrid:grid id="storageId" url="'${createLink(action: 'listJson')}'"
					editurl="'${createLink(action: 'deleteBulkAsset')}'"
					colNames="'Actions','Name', '${modelPref['1']}','${modelPref['2']}', '${modelPref['3']}','${modelPref['4']}','${modelPref['5']}','id', 'commentType'"
					colModel="{name:'act', index: 'act' , sortable: false, formatter:myCustomFormatter, search:false, width:'90', fixed:true},
						{name:'assetName',index: 'assetName', formatter: myLinkFormatter, width:'300'},
						{name:'${filesPref['1']}',width:'120', formatter: tdsCommon.jqgridPrefCellFormatter},
						{name:'${filesPref['2']}', width:'120', formatter: tdsCommon.jqgridPrefCellFormatter},
						{name:'${filesPref['3']}', width:'120', formatter: tdsCommon.jqgridPrefCellFormatter},
						{name:'${filesPref['4']}', width:'120', formatter: tdsCommon.jqgridPrefCellFormatter},
						{name:'${filesPref['5']}', width:'120', formatter: tdsCommon.jqgridPrefCellFormatter},
						{name:'id', hidden: true},
						{name:'commentType', hidden: true} "
					sortname="'assetName'"
					caption="listCaption"
					rowNum="sizePref"
					rowList="${ raw(com.tdsops.common.ui.Pagination.optionsAsText()) }"
					multiselect="true"
					loadComplete="initCheck"
					gridComplete="function(){bindResize('storageId');recompileDOM('storageIdWrapper');}"
					onSelectRow="validateMergeCount"
					postData="{filter: filter, event:event, plannedStatus:plannedStatus, validation:validation, moveBundleId:moveBundleId, assetName:fileName,
						planStatus:planStatus, moveBundle:moveBundle, fileFormat:fileFormat, size:size,toValidate:toValidate, unassigned:unassigned}"
					showPager="true">
					<jqgrid:navigation id="storageId" add="false" edit="false" del="false" search="false" refresh="false"/>
					<jqgrid:refreshButton id="storageId" />
				</jqgrid:grid>
				TDS.jqGridFilterToolbar('storageId');

				populateFilter();

				<g:each var="key" in="['1','2','3','4','5']">
					var filePref= '${filesPref[key]}';
					$("#storageIdGrid_"+filePref).append("<img src=\"${resource(dir:'images',file:'select2Arrow.png')}\" class=\"selectImage customizeSelect editSelectimage_"+${key}+"\" onclick=\"showSelect(\'"+filePref+"\',\'storage\',\'"+${key}+"\')\">");
				</g:each>

				$.jgrid.formatter.integer.thousandsSeparator='';
				function myLinkFormatter (cellvalue, options, rowObjcet) {
					var value = cellvalue ? _.escape(cellvalue) : ''
					return '<a href="javascript:EntityCrud.showAssetDetailView(\'${assetClass}\','+options.rowId+')">'+value+'</a>'
				}

				function myCustomFormatter (cellVal,options,rowObject) {
					var actionButton = '';
					if (${hasPerm}) {
                        actionButton += '<a href="javascript:EntityCrud.showAssetEditView(\'${assetClass}\','+options.rowId+')" title=\'Edit Asset\'>'+
							"<img src=\"${resource(dir:'icons',file:'database_edit.png')}\" border='0px'/>"+"</a>&nbsp;&nbsp;"
					}
                    actionButton += "<grid-buttons asset-id='" + options.rowId + "' asset-type='" + rowObject[8] + "' tasks='" + rowObject[7] + "' comments='" + rowObject[9] +
                        "' can-view-tasks='" + ${canViewTasks} + "' can-view-comments='" + ${canViewComments} +
                            "' can-create-tasks='" + ${canCreateTasks} + "' can-create-comments='" + ${canCreateComments} + "'>" +
                            "</grid-buttons>";
					<tds:hasPermission permission="${Permission.AssetCreate}">
						var value = rowObject[1] ? _.escape(rowObject[1]) : '';
						actionButton += '&nbsp;&nbsp;<a href="javascript:EntityCrud.cloneAssetView(\'${assetClass}\', \'' + value + '\', '+options.rowId+');" title=\'Clone Asset\'>'+
							"<img src='${resource(dir:'icons',file:'database_copy.png')}' border='0px'/>"+"</a>";
                    </tds:hasPermission>
                    return actionButton;
				}

				function populateFilter(){
					$("#gs_assetName").val('${fileName}')
					$("#gs_fileFormat").val('${fileFormat}')
					$("#gs_size").val('${size}')
					$("#gs_planStatus").val('${planStatus}')
					$("#gs_moveBundle").val('${moveBundle}')
				}
			})
		</script>

	</head>
	<body>
		<tds:subHeader title="Logical Storage List${(event)?(' for Move Event '+moveEvent.name):('')}" crumbs="['Assets','Logical Storage List']"/>
		<div class="body fluid" ng-app="tdsAssets" ng-controller="tds.assets.controller.MainController as assets">
			<g:render template="../assetEntity/listTitleAlerts" ></g:render>
            <div ng-controller="tds.comments.controller.MainController as comments">
			    <jqgrid:wrapper id="storageId" />
            </div>
			<g:each var="key" in="['1','2','3','4','5']">
				<div id="columnCustomDiv_${filesPref[key]}" style="display:none;">
					<div class="columnDiv_${key} customScroll customizeDiv" style="width: 13.3% !important;">
						<input type="hidden" id="previousValue_${key}" value="${filesPref[key]}" />
						<g:each var="attribute" in="${fieldSpecs}">
							<label><input type="radio" name="coloumnSelector_${filesPref[key]}" id="coloumnSelector_${filesPref[key]}" value="${attribute.attributeCode}"
								${filesPref[key]==attribute.attributeCode?'checked':'' } style="margin-left:11px;"
								onchange="setColumnAssetPref(this.value,'${key}','${com.tdsops.tm.enums.domain.UserPreferenceEnum.Storage_Columns}')"
								/> 
								${attribute.frontendLabel}
							</label>
							<br>
						</g:each>
					</div>
				</div>
			</g:each>
			<g:render template="../assetEntity/entityCrudDivs" />
			<g:render template="../assetEntity/dependentAdd" />
		</div>
		<g:render template="../assetEntity/modelDialog"/>
        <g:render template="../assetEntity/initAssetEntityData"/>
		<script>
			currentMenuId = "#assetMenu";
			$($(".menu-parent-assets-storage-logical-list")[1]).addClass('active');
			$(".menu-parent-assets").addClass('active');
		</script>
	</body>
</html>