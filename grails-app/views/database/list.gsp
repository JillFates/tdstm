<%@page import="com.tds.asset.AssetEntity;com.tds.asset.Application;com.tds.asset.Database;com.tds.asset.Files;"%>
<%@page import="com.tds.asset.Database"%>
<g:set var="assetClass" value="${(new Database()).assetClass}" />

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="topNav" />
		<g:javascript src="asset.tranman.js" />
		<g:javascript src="entity.crud.js" />
		<g:render template="../layouts/responsiveAngularResources" />
		<g:javascript src="asset.comment.js" />
		<g:javascript src="cabling.js"/>
		<jqgrid:resources />
		<g:javascript src="jqgrid-support.js" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'jquery.autocomplete.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.accordion.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.resizable.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.slider.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.tabs.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datepicker.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
		<script type="text/javascript">

			$(document).ready(function() {

				//$('#assetMenu').show();
				$("#createEntityView").dialog({ autoOpen: false })
				$("#showEntityView").dialog({ autoOpen: false })
				$("#editEntityView").dialog({ autoOpen: false })
				$("#manufacturerShowDialog").dialog({ autoOpen: false })
				$("#modelShowDialog").dialog({ autoOpen: false })
				$("#cablingDialogId").dialog({ autoOpen:false })
				

				// JqGrid implementations 
				var filter = '${filter}'
				var event = '${event}'
				var plannedStatus = '${plannedStatus}' 
				var validation = '${validation}'
				var moveBundleId = '${moveBundleId}'
				var dbName = '${dbName}'
				var planStatus = '${planStatus}'
				var moveBundle = '${moveBundle}'
				var dbFormat = '${validationFilter}'
				var sizePref = '${sizePref}'
				var toValidate = '${toValidate}'
				var unassigned = '${unassigned}'
					
				var listCaption ='Database: \
					<tds:hasPermission permission="AssetEdit">\
						<span class="capBtn">\
							<input type="button" value="Create DB" onclick="EntityCrud.showAssetCreateView(\'${assetClass}\');"/>\
						</span>\
					</tds:hasPermission>\
					<tds:hasPermission permission="AssetDelete">\
						<span class="capBtn"><input type="button" id="deleteAssetId" value="Bulk Delete" onclick="deleteAssets(\'Database\')" disabled="disabled"/></span>\
					</tds:hasPermission>\
						<span><input type="checkbox" id="justPlanning" ${ (justPlanning == 'true' ? 'checked="checked"': '') } onclick="toggleJustPlanning($(this))"/> Just Planning</span>\
					<g:if test="${fixedFilter}"><g:link class="mmlink" controller="database" action="list"><span class="capBtn"><input type="button" class="clearFilterId" value="Clear Filters" /></span></g:link>\
					</g:if><g:else><span class="capBtn"><input type="button" class="clearFilterId" value="Clear Filters" disabled="disabled" onclick="clearFilter(\'databaseId\')"/></g:else></span>'		
				<jqgrid:grid id="databaseId" url="'${createLink(action: 'listJson')}'"
					editurl="'${createLink(action: 'deleteBulkAsset')}'"
					colNames="'Actions','Name', '${modelPref['1']}','${modelPref['2']}', '${modelPref['3']}','${modelPref['4']}','${modelPref['5']}','id', 'commentType'"
					colModel="{name:'act', index: 'act' , sortable: false, formatter:myCustomFormatter, search:false, width:'50', fixed:false},
						{name:'assetName',index: 'assetName', formatter: myLinkFormatter, width:'300'},
						{name:'${dbPref['1']}', width:'120', formatter: tdsCommon.jqgridPrefCellFormatter},
						{name:'${dbPref['2']}', width:'120', formatter: tdsCommon.jqgridPrefCellFormatter},
						{name:'${dbPref['3']}', width:'120', formatter: tdsCommon.jqgridPrefCellFormatter}, 
						{name:'${dbPref['4']}', width:'120', formatter: tdsCommon.jqgridPrefCellFormatter},
						{name:'${dbPref['5']}', width:'120', formatter: tdsCommon.jqgridPrefCellFormatter},
						{name:'id', hidden: true},
						{name:'commentType', hidden: true} "
					sortname="'assetName'"
					caption="listCaption"
					rowNum="sizePref"
					multiselect="true"
					loadComplete="initCheck"
					gridComplete="function(){bindResize('databaseId');recompileDOM('databaseIdWrapper');}"
					onSelectRow="validateMergeCount"
					postData="{filter: filter, event:event, plannedStatus:plannedStatus, validation:validation, moveBundleId:moveBundleId,
						assetName:dbName, planStatus:planStatus, moveBundle:moveBundle, dbFormat:dbFormat, toValidate:toValidate, unassigned:unassigned}"
					showPager="true">
					<jqgrid:navigation id="databaseId" add="false" edit="false" del="false" search="false" refresh="false" afterSubmit="deleteMessage"/>
					<jqgrid:resize id="databaseId" resizeOffset="-2" />
					<jqgrid:refreshButton id="databaseId" />
				</jqgrid:grid>
				TDS.jqGridFilterToolbar('databaseId');

				jQuery("#databaseId").jqGrid('navButtonAdd','#pcolch',{ caption: "Columns", title: "Reorder Columns", onClickButton : function (){ jQuery("#colch").jqGrid('columnChooser'); } });
				populateFilter();
				$("#del_databaseIdGrid").click(function(){
				$("#databaseId").jqGrid("editGridRow","new",
					{afterSubmit:deleteMessage});
				
			});
			<g:each var="key" in="['1','2','3','4','5']">
				var dbPref= '${dbPref[key]}';
				$("#databaseIdGrid_"+dbPref).append("<img src=\"${resource(dir:'images',file:'select2Arrow.png')}\" class=\"selectImage customizeSelect editSelectimage_"+${key}+"\" onclick=\"showSelect('"+dbPref+"', 'database','"+${key}+"')\">");
			</g:each>
				
			$.jgrid.formatter.integer.thousandsSeparator='';
			function myLinkFormatter (cellvalue, options, rowObject) {
				var value = cellvalue ? _.escape(cellvalue) : ''
				return '<a href="javascript:EntityCrud.showAssetDetailView(\'${assetClass}\','+options.rowId+');">'+value+'</a>'
			}
			
			function myCustomFormatter (cellVal,options,rowObject) {
				var editButton = '';
				if (${hasPerm}) {
					editButton += '<a href="javascript:EntityCrud.showAssetEditView(\'${assetClass}\','+options.rowId+')" title=\'Edit Asset\'>'+
					"<img src='${resource(dir:'icons',file:'database_edit.png')}' border='0px'/>"+"</a>&nbsp;&nbsp;"
				}
				editButton += "<grid-buttons asset-id='" + options.rowId + "' asset-type='${assetClass}' tasks='" + rowObject[7] + "' comments='" + rowObject[9] + "' can-edit-tasks='true' can-edit-comments='" + ${hasPerm} + "'></grid-buttons>"
				return editButton
			}
			function deleteMessage(response, postdata){
				 $("#messageId").show()
				 $("#messageDivId").hide()
				 $("#messageId").html(response.responseText)
				 $("#delmoddatabaseIdGrid").hide()
				   return true
			}
			function populateFilter(){
				$("#gs_assetName").val('${dbName}')
				$("#gs_dbFormat").val('${dbFormat}')
				$("#gs_planStatus").val('${planStatus}')
				$("#gs_moveBundle").val('${moveBundle}')
			}
			})
		</script>

		<title>DB list</title>
	</head>
	<body>
		<div class="body fluid" ng-app="tdsAssets" ng-controller="tds.assets.controller.MainController as assets">
			<h1>Database List${(event)?(' for Move Event '+moveEvent.name):('')}</h1>
			<g:render template="../assetEntity/listTitleAlerts" ></g:render>
			<g:each var="key" in="['1','2','3','4','5']">
				<div id="columnCustomDiv_${dbPref[key]}" style="display:none;">
					<div class="columnDiv_${key} customScroll customizeDiv" style="width: 13.3% !important;">
						<input type="hidden" id="previousValue_${key}" value="${dbPref[key]}" />
						<g:each var="attribute" in="${attributesList}">
							<label><input type="radio" name="coloumnSelector_${dbPref[key]}" id="coloumnSelector_${dbPref[key]}" value="${attribute.attributeCode}" 
								${dbPref[key]==attribute.attributeCode ? 'checked' : '' } style="margin-left:11px;" 
								onchange="setColumnAssetPref(this.value,'${key}','Database_Columns')"/> ${attribute.frontendLabel}</label><br>
						</g:each>
					</div>
				</div>
			</g:each>
			<div id="commentScopeId" ng-controller="tds.comments.controller.MainController as comments">
			    <jqgrid:wrapper id="databaseId" /> 
			</div>
			<g:render template="../assetEntity/entityCrudDivs" />
			<g:render template="../assetEntity/dependentAdd" />
		</div>
		<g:render template="../assetEntity/modelDialog"/>
        <g:render template="../assetEntity/initAssetEntityData"/>
		<script>
			currentMenuId = "#assetMenu";
			$(".menu-parent-assets-database-list").addClass('active');
			$(".menu-parent-assets").addClass('active');
		</script>
	</body>
</html>
