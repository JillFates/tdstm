<%@page import="com.tds.asset.AssetEntity;com.tds.asset.Application;com.tds.asset.Database;com.tds.asset.Files;"%>
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
				
				var listCaption ='Storages: <tds:hasPermission permission="AssetEdit"><span class="capBtn"><input type="button" value="Create Storage" onclick="createAssetDetails(\'Files\')"/></span></tds:hasPermission>\
					<tds:hasPermission permission="AssetDelete">\
						<span class="capBtn"><input type="button" id="deleteAssetId" value="Bulk Delete" onclick="deleteAssets(\'Files\')" disabled="disabled"/></span>\
						<span><input type="checkbox" id="justPlanning" ${ (justPlanning == 'true' ? 'checked="checked"': '') } onclick="toggleJustPlanning($(this))"/> Just Planning</span>\
					</tds:hasPermission>\
					<g:if test="${fixedFilter}"><g:link class="mmlink" controller="files" action="list"><span class="capBtn"><input type="button" class="clearFilterId" value="Clear Filters" /></span></g:link>\
					</g:if><g:else><span class="capBtn"><input type="button" class="clearFilterId" value="Clear Filters" disabled="disabled" onclick="clearFilter(\'storageId\')"/></g:else></span>'
				// JqGrid implementations 
				<jqgrid:grid id="storageId" url="'${createLink(action: 'listJson')}'"
					editurl="'${createLink(action: 'deleteBulkAsset')}'"
					colNames="'Actions','Name', '${modelPref['1']}','${modelPref['2']}', '${modelPref['3']}','${modelPref['4']}','id', 'commentType'"
					colModel="{name:'act', index: 'act' , sortable: false, ${hasPerm? 'formatter:myCustomFormatter,' :''} search:false, width:'50'},
						{name:'assetName',index: 'assetName', formatter: myLinkFormatter, width:'300'},
						{name:'${filesPref['1']}',width:'120'},
						{name:'${filesPref['2']}', width:'120'},
						{name:'${filesPref['3']}', width:'120'}, 
						{name:'${filesPref['4']}', width:'120'},
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
						planStatus:planStatus, moveBundle:moveBundle, fileFormat:fileFormat, size:size,toValidate:toValidate}"
					showPager="true">
					<jqgrid:filterToolbar id="storageId" searchOnEnter="false" />
					<jqgrid:navigation id="storageId" add="false" edit="false" del="false" search="false" refresh="false"/>
					<jqgrid:refreshButton id="storageId" />
				</jqgrid:grid>
				populateFilter();

				<g:each var="key" in="['1','2','3','4']">
					var filePref= '${filesPref[key]}';
					$("#storageIdGrid_"+filePref).append('<img src="../images/select2Arrow.png" class="selectImage customizeSelect editSelectimage_'+${key}+'" onclick="showSelect(\''+filePref+'\',\'storage\',\''+${key}+'\')">');
				</g:each>
				
				$.jgrid.formatter.integer.thousandsSeparator='';
				function myLinkFormatter (cellvalue, options, rowObjcet) {
					var value = cellvalue ? cellvalue : ''
					return '<a href="javascript:getEntityDetails(\'files\',\''+rowObjcet[7]+'\','+options.rowId+')">'+value+'</a>'
				}
				
				function myCustomFormatter (cellVal,options,rowObject) {
					var editButton = '<a href="javascript:editEntity(\'files\',\''+rowObject[7]+'\','+options.rowId+')">'+
							"<img src='${resource(dir:'icons',file:'database_edit.png')}' border='0px'/>"+"</a>&nbsp;&nbsp;"
					if(rowObject[6]=='issue'){
					   editButton+='<span id="icon_'+options.rowId+'"><a ng-click="comments.listBy('+options.rowId+',\''+rowObject[7]+'\');">'+"<img src='${resource(dir:'i',file:'db_table_red.png')}' border='0px'/>"+"</a></span>"
					} else if (rowObject[6]=='comment') {
					   editButton+='<span id="icon_'+options.rowId+'"><a ng-click="comments.listBy('+options.rowId+',\''+rowObject[7]+'\');">'+"<img src='${resource(dir:'icons',file:'comment.png')}' border='0px'/>"+"</a></span>"
					} else {
					   editButton+='<span id="icon_'+options.rowId+'"><a ng-click="comments.createCommentBy(\'issue\','+options.rowId+',\''+rowObject[7]+'\')">'
						+"<img src='${resource(dir:'i',file:'db_table_light.png')}' border='0px'/>"+"</a></span>"
					}
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
			<h1>Storage List${(event)?(' for Move Event '+moveEvent.name):('')}</h1>
            <div class="alert alert-{{alert.type}}" ng-repeat="alert in alerts.list" ng-class="{animateShow: !alert.hidden}">
                <button type="button" class="alert-close" aria-hidden="true" ng-click="alerts.closeAlert($index)">&times;</button>
                {{alert.msg}}
            </div>
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			<div>
				<div id="messageId" class="message" style="display:none"></div>
			</div>
            <div ng-controller="tds.comments.controller.MainController as comments">
			    <jqgrid:wrapper id="storageId" />
            </div>
			<g:each var="key" in="['1','2','3','4']">
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
			<div id="createEntityView" style="display: none;"></div>
			<div id="showEntityView" style="display: none;"></div>
			<div id="editEntityView" style="display: none;"></div>	
			<div id="cablingDialogId" style="display: none;"></div>
			<g:render template="../assetEntity/newDependency" model="['forWhom':'Storage', entities:files]"></g:render>
		</div>
		<g:render template="../assetEntity/modelDialog"/>
        <g:render template="../assetEntity/initAssetEntityData"/>
		<script>
			currentMenuId = "#assetMenu";
			$("#assetMenuId a").css('background-color','#003366')
		</script>
	</body>
</html>
