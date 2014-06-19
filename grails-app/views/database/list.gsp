<%@page import="com.tds.asset.AssetEntity;com.tds.asset.Application;com.tds.asset.Database;com.tds.asset.Files;"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="projectHeader" />
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
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datepicker.css')}" />
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
					
				var listCaption ='DataBases: <tds:hasPermission permission="AssetEdit"><span class="capBtn"><input type="button" value="Create DB" onclick="createAssetDetails(\'Database\')"/></span></tds:hasPermission>\
					<tds:hasPermission permission="AssetDelete">\
						<span class="capBtn"><input type="button" id="deleteAssetId" value="Bulk Delete" onclick="deleteAssets(\'Database\')" disabled="disabled"/></span>\
						<span><input type="checkbox" id="justPlanning" ${ (justPlanning == 'true' ? 'checked="checked"': '') } onclick="toggleJustPlanning($(this))"/> Just Planning</span>\
					</tds:hasPermission>\
					<g:if test="${fixedFilter}"><g:link class="mmlink" controller="database" action="list"><span class="capBtn"><input type="button" class="clearFilterId" value="Clear Filters" /></span></g:link>\
					</g:if><g:else><span class="capBtn"><input type="button" class="clearFilterId" value="Clear Filters" disabled="disabled" onclick="clearFilter(\'databaseId\')"/></g:else></span>'		
				<jqgrid:grid id="databaseId" url="'${createLink(action: 'listJson')}'"
					editurl="'${createLink(action: 'deleteBulkAsset')}'"
					colNames="'Actions','Name', '${modelPref['1']}','${modelPref['2']}', '${modelPref['3']}','${modelPref['4']}','id', 'commentType'"
					colModel="{name:'act', index: 'act' , sortable: false, ${hasPerm? 'formatter:myCustomFormatter,' :''} search:false, width:'50', fixed:false},
						{name:'assetName',index: 'assetName', formatter: myLinkFormatter, width:'300'},
						{name:'${dbPref['1']}',width:'120'},
						{name:'${dbPref['2']}', width:'120'},
						{name:'${dbPref['3']}', width:'120'}, 
						{name:'${dbPref['4']}', width:'120'},
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
						assetName:dbName, planStatus:planStatus, moveBundle:moveBundle, dbFormat:dbFormat, toValidate:toValidate}"
					showPager="true">
					<jqgrid:filterToolbar id="databaseId" searchOnEnter="false" />
					<jqgrid:navigation id="databaseId" add="false" edit="false" del="false" search="false" refresh="false" afterSubmit="deleteMessage"/>
					<jqgrid:resize id="databaseId" resizeOffset="-2" />
					<jqgrid:refreshButton id="databaseId" />
				</jqgrid:grid>
				jQuery("#databaseId").jqGrid('navButtonAdd','#pcolch',{ caption: "Columns", title: "Reorder Columns", onClickButton : function (){ jQuery("#colch").jqGrid('columnChooser'); } });
				populateFilter();
				$("#del_databaseIdGrid").click(function(){
				$("#databaseId").jqGrid("editGridRow","new",
					{afterSubmit:deleteMessage});
			 });
			<g:each var="key" in="['1','2','3','4']">
				var dbPref= '${dbPref[key]}';
				$("#databaseIdGrid_"+dbPref).append('<img src="../images/select2Arrow.png" class="selectImage customizeSelect editSelectimage_'+${key}+'" onclick="showSelect(\''+dbPref+'\',\'database\',\''+${key}+'\')">');
			</g:each>
				
			$.jgrid.formatter.integer.thousandsSeparator='';
			function myLinkFormatter (cellvalue, options, rowObjcet) {
				var value = cellvalue ? cellvalue : ''
				return '<a href="javascript:getEntityDetails(\'database\',\''+rowObjcet[7]+'\','+options.rowId+')">'+value+'</a>'
			}
			
			function myCustomFormatter (cellVal,options,rowObject) {
				var editButton = '<a href="javascript:editEntity(\'database\',\''+rowObject[7]+'\','+options.rowId+')">'+
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
			<h1>DB List${(event)?(' for Move Event '+moveEvent.name):('')}</h1>
            <div class="alert alert-{{alert.type}}" ng-repeat="alert in alerts.list" ng-class="{animateShow: !alert.hidden}">
                <button type="button" class="alert-close" aria-hidden="true" ng-click="alerts.closeAlert($index)">&times;</button>
                {{alert.msg}}
            </div>
			<g:if test="${flash.message}">
				<div id="messageDivId" class="message">${flash.message}</div>
			</g:if>
			<div >
				<div id="messageId" class="message" style="display:none"></div>
			</div>
			<g:each var="key" in="['1','2','3','4']">
				<div id="columnCustomDiv_${dbPref[key]}" style="display:none;">
					<div class="columnDiv_${key} customScroll customizeDiv" style="width: 13.3% !important;">
						<input type="hidden" id="previousValue_${key}" value="${dbPref[key]}" />
						<g:each var="attribute" in="${attributesList}">
							<label><input type="radio" name="coloumnSelector_${dbPref[key]}" id="coloumnSelector_${dbPref[key]}" value="${attribute.attributeCode}" 
								${dbPref[key]==attribute.attributeCode?'checked':'' } style="margin-left:11px;" 
								onchange="setColumnAssetPref(this.value,'${key}','Database_Columns')"/> ${attribute.frontendLabel}</label><br>
						</g:each>
					</div>
				</div>
			</g:each>
            <div ng-controller="tds.comments.controller.MainController as comments">
			    <jqgrid:wrapper id="databaseId" /> 
            </div>
			<div id="createEntityView" style="display: none;" ></div>
			<div id="showEntityView" style="display: none;"></div>
			<div id="editEntityView" style="display: none;"></div>
			<div id="cablingDialogId" style="display: none;"></div>
			<g:render template="../assetEntity/newDependency" model="['forWhom':'Database', entities:dbs]"></g:render>
		</div>
		<g:render template="../assetEntity/modelDialog"/>
        <g:render template="../assetEntity/initAssetEntityData"/>
		<script>
			currentMenuId = "#assetMenu";
			$("#assetMenuId a").css('background-color','#003366')
		</script>
	</body>
</html>
