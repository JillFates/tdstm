<%@page import="com.tds.asset.AssetEntity" %>
<%@page import="com.tds.asset.Database" %>
<%@page import="com.tds.asset.Files" %>
<%@page import="com.tds.asset.AssetComment" %>
<%@page import="com.tds.asset.Application" %>

<g:set var="assetClass" value="${(new Application()).assetClass}" />

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="projectHeader" />
		<title>Application list</title>
		<g:javascript src="bootstrap.js" />
		<g:javascript src="asset.tranman.js" />
		<g:javascript src="entity.crud.js" />
		<g:javascript src="projectStaff.js" />
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
				$("#createEntityView").dialog({ autoOpen: false })
				$("#showEntityView").dialog({ autoOpen: false })
				$("#editEntityView").dialog({ autoOpen: false })
				$("#cablingDialogId").dialog({ autoOpen:false })
				$("#manufacturerShowDialog").dialog({ autoOpen: false })
				$("#modelShowDialog").dialog({ autoOpen: false })
				$("#createStaffDialog").dialog({ autoOpen: false })
				$("#filterPane").draggable()

				// JqGrid implementations 
				var filter = '${filter}'
				var latencys = '${latencys}'
				var event = '${event}'
				var moveEvent = '${moveEvent}'
				var plannedStatus = '${plannedStatus}' 
				var validation = '${validation}'
				var moveBundleId = '${moveBundleId}'
				var appName = '${appName}'
				var planStatus = '${planStatus}'
				var moveBundle = '${moveBundle}'
				var validationFilter = '${validationFilter}'
				var appSme = '${appSme}'
				var toValidate = '${toValidate}'
				var runbook = '${runbook}'
				var unassigned = '${unassigned}'
				
				var sizePref = '${sizePref}'
				var listCaption = 'Application: \
					<tds:hasPermission permission="AssetEdit">\
						<span class="capBtn"><input type="button" value="Create App" onclick="EntityCrud.showAssetCreateView(\'${assetClass}\');"/></span>\
					</tds:hasPermission>\
					<tds:hasPermission permission="AssetDelete">\
						<span class="capBtn"><input type="button" id="deleteAssetId" value="Bulk Delete" onclick="deleteAssets(\'Application\')" disabled="disabled"/></span>\
					</tds:hasPermission>\
						<span><input type="checkbox" id="justPlanning" ${ (justPlanning == 'true' ? 'checked="checked"': '') } onclick="toggleJustPlanning($(this))"/> Just Planning</span>\
					<g:if test="${fixedFilter}"><g:link class="mmlink" controller="application" action="list"><span class="capBtn"><input type="button" class="clearFilterId" value="Clear Filters" /></span></g:link>\
					</g:if><g:else><span class="capBtn"><input type="button" class="clearFilterId" value="Clear Filters" disabled="disabled" onclick="clearFilter(\'applicationId\')"/></g:else></span>'
				<jqgrid:grid id="applicationId" url="'${createLink(action: 'listJson')}'"
					editurl="'${createLink(action: 'deleteBulkAsset')}'"
					colNames="'Actions','Name', '${modelPref['1']}','${modelPref['2']}', '${modelPref['3']}','${modelPref['4']}','${modelPref['5']}','id', 'commentType', 'Event'"
					colModel="{name:'act', index: 'act' , sortable: false, ${hasPerm? 'formatter:myCustomFormatter,' :''} search:false, width:'65', fixed:true},
						{name:'assetName',index: 'assetName', formatter: myLinkFormatter, width:'300'},
						{name:'${appPref['1']}',width:'120'},
						{name:'${appPref['2']}', width:'120'},
						{name:'${appPref['3']}', width:'120'}, 
						{name:'${appPref['4']}', width:'120'},
						{name:'${appPref['5']}', width:'120'},
						{name:'id', hidden: true},
						{name:'commentType', hidden: true},
						{name:'event', hidden: true} "
					sortname="'assetName'"
					caption="listCaption"
					rowNum="sizePref"
					multiselect="true"
					loadComplete="initCheck"
					gridComplete="function(){bindResize('applicationId');recompileDOM('applicationIdWrapper');}"
					onSelectRow="validateMergeCount"
					showPager="true"
					loadComplete=function(){
						resizeGrid()
					}
					postData="{filter: filter, event:event, latencys:latencys, plannedStatus:plannedStatus, validationFilter:validation, moveBundleId:moveBundleId,
						assetName:appName, planStatus:planStatus, moveBundle:moveBundle, validation:validationFilter, sme:appSme, 
						toValidate:toValidate,runbook:runbook, unassigned:unassigned}">
					<jqgrid:filterToolbar id="applicationId" searchOnEnter="false" />
					<jqgrid:navigation id="applicationId" add="false" edit="false" del="false" search="false" refresh="false" />
					<jqgrid:refreshButton id="applicationId" />
				</jqgrid:grid>
				populateFilter();
				
				$("#del_applicationIdGrid").click(function(){
					$("#applicationId").jqGrid("editGridRow","new",
						{afterSubmit:deleteMessage}
					);
				});
				<g:each var="key" in="['1','2','3','4','5']">
					var appPref= '${appPref[key]}';
					$("#applicationIdGrid_"+appPref).append('<img src="../images/select2Arrow.png" class="selectImage customizeSelect editSelectimage_'+${key}+'" onclick="showSelect(\''+appPref+'\',\'application\',\''+${key}+'\')">');
				</g:each>
				$.jgrid.formatter.integer.thousandsSeparator='';

			function myLinkFormatter (cellvalue, options, rowObject) {
				var value = cellvalue ? cellvalue : ''
				return '<a href="javascript:EntityCrud.showAssetDetailView(\'${assetClass}\','+options.rowId+')">'+value+'</a>'
			}

			function myCustomFormatter (cellVal,options,rowObject) {
				var editButton = '<a href="javascript:EntityCrud.showAssetEditView(\'${assetClass}\','+options.rowId+');" title=\'Edit Asset\'>'+
					"<img src='${resource(dir:'icons',file:'database_edit.png')}' border='0px'/>"+"</a>&nbsp;&nbsp;"
				editButton += "<grid-buttons asset-id='" + options.rowId + "' asset-type='" + rowObject[8] + "' tasks='" + rowObject[7] + "' comments='" + rowObject[10] + "'></grid-buttons>"
				return editButton
			}
			function deleteMessage(response, postdata) {
				 $("#messageId").show()
				 $("#messageDivId").hide()
				 $("#messageId").html(response.responseText)
				 $("#delmodapplicationIdGrid").hide()
				 return true
			}

			function populateFilter() {
				$("#gs_assetName").val('${appName}')
				$("#gs_sme").val('${appSme}')
				
				if (validationFilter)
					$("#gs_validation").val('${validationFilter}')
				else if ( validation )
					$("#gs_validation").val('${validation}')
					
				if (planStatus)
					$("#gs_planStatus").val('${planStatus}')
				else if( plannedStatus )
					$("#gs_planStatus").val( plannedStatus )
					
				if (event)
					$("#gs_event").val('${event}')
				else if( event )
					$("#gs_event").val( event )
				
				$("#gs_assetName").trigger( 'keydown' );
			}
			})
		</script>
	</head>
	<body>
		<div class="body fluid" ng-app="tdsAssets" ng-controller="tds.assets.controller.MainController as assets">
			<h1>Application List${(event)?(' for Move Event '+moveEvent.name):('')}</h1>
			<g:render template="../assetEntity/listTitleAlerts" ></g:render>
			<g:each var="key" in="['1','2','3','4','5']">
				<div id="columnCustomDiv_${appPref[key]}" style="display:none;">
					<div class="columnDiv_${key} customScroll customizeDiv" style="width:13.7% !important;">
						<input type="hidden" id="previousValue_${key}" value="${appPref[key]}" />
						<g:each var="attribute" in="${attributesList}">
							<label><input type="radio" name="coloumnSelector_${appPref[key]}" id="coloumnSelector_${appPref[key]}" value="${attribute.attributeCode}" 
								${appPref[key]==attribute.attributeCode?'checked':'' } style="margin-left:11px;" 
								onchange="setColumnAssetPref(this.value,'${key}','App_Columns')"/> ${attribute.frontendLabel}</label><br>
						</g:each>
					</div>
				</div>
			</g:each>
			<div ng-controller="tds.comments.controller.MainController as comments">
			    <jqgrid:wrapper id="applicationId" />
			</div>
			<g:render template="../assetEntity/modelDialog"/>
			<g:render template="../assetEntity/entityCrudDivs" />
			<g:render template="../assetEntity/dependentAdd" />
			<div id="createStaffDialog" style="display:none;">
				<g:render template="../person/createStaff" model="['forWhom':'application']"></g:render>
			</div>
		</div>
            <g:render template="../assetEntity/initAssetEntityData"/>
		<script>
			currentMenuId = "#assetMenu";
			$("#assetMenuId a").css('background-color','#003366')
		</script>
	</body>
</html>
