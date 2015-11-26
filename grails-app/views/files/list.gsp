<%@page import="com.tds.asset.AssetEntity"%>
<%@page import="com.tds.asset.Application"%>
<%@page import="com.tds.asset.Database"%>
<%@page import="com.tds.asset.Files"%>

<g:set var="assetClass" value="${(new Files()).assetClass}" />

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="projectHeader" />
		<title>Storage List</title>
		<g:javascript src="asset.tranman.js" />
		<g:javascript src="entity.crud.js" />
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
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'components/comment',file:'comment.css')}" />
		<script type="text/javascript">
			$(document).ready(function() {
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
				
				var listCaption ='Storages: <tds:hasPermission permission="AssetEdit"> \
					<span class="capBtn">\
						<input type="button" value="Create Storage" onclick="EntityCrud.showAssetCreateView(\'${assetClass}\')"/>\
					</span></tds:hasPermission>\
					<tds:hasPermission permission="AssetDelete">\
						<span class="capBtn"><input type="button" id="deleteAssetId" value="Bulk Delete" onclick="deleteAssets(\'Files\')" disabled="disabled"/></span>\
					</tds:hasPermission>\
						<span><input type="checkbox" id="justPlanning" ${ (justPlanning == 'true' ? 'checked="checked"': '') } onclick="toggleJustPlanning($(this))"/> Just Planning</span>\
					<g:if test="${fixedFilter}"><g:link class="mmlink" controller="files" action="list"><span class="capBtn"><input type="button" class="clearFilterId" value="Clear Filters" /></span></g:link>\
					</g:if><g:else><span class="capBtn"><input type="button" class="clearFilterId" value="Clear Filters" disabled="disabled" onclick="clearFilter(\'storageId\')"/></g:else></span>'
				// JqGrid implementations 
				<jqgrid:grid id="storageId" url="'${createLink(action: 'listJson')}'"
					editurl="'${createLink(action: 'deleteBulkAsset')}'"
					colNames="'Actions','Name', '${modelPref['1']}','${modelPref['2']}', '${modelPref['3']}','${modelPref['4']}','${modelPref['5']}','id', 'commentType'"
					colModel="{name:'act', index: 'act' , sortable: false, formatter:myCustomFormatter, search:false, width:'50'},
						{name:'assetName',index: 'assetName', formatter: myLinkFormatter, width:'300'},
						{name:'${filesPref['1']}',width:'120'},
						{name:'${filesPref['2']}', width:'120'},
						{name:'${filesPref['3']}', width:'120'}, 
						{name:'${filesPref['4']}', width:'120'},
						{name:'${filesPref['5']}', width:'120'},
						{name:'id', hidden: true},
						{name:'commentType', hidden: true} "
					sortname="'assetName'"
					caption="listCaption"
					rowNum="sizePref"
					multiselect="true"
					loadComplete="initCheck"
					gridComplete="function(){bindResize('storageId');recompileDOM('storageIdWrapper');}"
					onSelectRow="validateMergeCount"
					postData="{filter: filter, event:event, plannedStatus:plannedStatus, validation:validation, moveBundleId:moveBundleId, assetName:fileName, 
						planStatus:planStatus, moveBundle:moveBundle, fileFormat:fileFormat, size:size,toValidate:toValidate, unassigned:unassigned}"
					showPager="true">
					<jqgrid:filterToolbar id="storageId" searchOnEnter="false" />
					<jqgrid:navigation id="storageId" add="false" edit="false" del="false" search="false" refresh="false"/>
					<jqgrid:refreshButton id="storageId" />
				</jqgrid:grid>
				populateFilter();

				<g:each var="key" in="['1','2','3','4','5']">
					var filePref= '${filesPref[key]}';
					$("#storageIdGrid_"+filePref).append('<img src="../images/select2Arrow.png" class="selectImage customizeSelect editSelectimage_'+${key}+'" onclick="showSelect(\''+filePref+'\',\'storage\',\''+${key}+'\')">');
				</g:each>
				
				$.jgrid.formatter.integer.thousandsSeparator='';
				function myLinkFormatter (cellvalue, options, rowObjcet) {
					var value = cellvalue ? cellvalue : ''
					return '<a href="javascript:EntityCrud.showAssetDetailView(\'${assetClass}\','+options.rowId+')">'+value+'</a>'
				}
				
				function myCustomFormatter (cellVal,options,rowObject) {
					var editButton = '';
					if (${hasPerm}) {
						editButton += '<a href="javascript:EntityCrud.showAssetEditView(\'${assetClass}\','+options.rowId+')" title=\'Edit Asset\'>'+
							"<img src='${resource(dir:'icons',file:'database_edit.png')}' border='0px'/>"+"</a>&nbsp;&nbsp;"
					}
					editButton += "<grid-buttons asset-id='" + options.rowId + "' asset-type='" + rowObject[8] + "' tasks='" + rowObject[7] + "' comments='" + rowObject[9] + "' can-edit-tasks='true' can-edit-comments='" + ${hasPerm} + "'></grid-buttons>";
					return editButton
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
		<div class="body fluid" ng-app="tdsAssets" ng-controller="tds.assets.controller.MainController as assets">
			<h1>Logical Storage List${(event)?(' for Move Event '+moveEvent.name):('')}</h1>
			<g:render template="../assetEntity/listTitleAlerts" ></g:render>
            <div ng-controller="tds.comments.controller.MainController as comments">
			    <jqgrid:wrapper id="storageId" />
            </div>
			<g:each var="key" in="['1','2','3','4','5']">
				<div id="columnCustomDiv_${filesPref[key]}" style="display:none;">
					<div class="columnDiv_${key} customScroll customizeDiv" style="width: 13.3% !important;">
						<input type="hidden" id="previousValue_${key}" value="${filesPref[key]}" />
						<g:each var="attribute" in="${attributesList}">
							<label><input type="radio" name="coloumnSelector_${filesPref[key]}" id="coloumnSelector_${filesPref[key]}" value="${attribute.attributeCode}" 
								${filesPref[key]==attribute.attributeCode?'checked':'' } style="margin-left:11px;" 
								onchange="setColumnAssetPref(this.value,'${key}','Storage_Columns')"/> ${attribute.frontendLabel}</label><br>
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
			$("#assetMenuId a").css('background-color','#003366')
		</script>
	</body>
</html>
