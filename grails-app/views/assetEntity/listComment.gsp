<%@page import="com.tds.asset.AssetComment"%>
<html>
	<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
	<meta name="layout" content="projectHeader" />
		<title>Asset Comment</title>
		<g:javascript src="asset.tranman.js" />
		<g:javascript src="entity.crud.js" />
		<g:javascript src="angular/angular.min.js" />
		<g:javascript src="angular/plugins/angular-ui.js"/>
        <g:javascript src="angular/plugins/angular-resource.js" />
		<g:javascript src="cabling.js"/>
        <script type="text/javascript" src="${resource(dir:'components/core',file:'core.js')}"></script>
        <script type="text/javascript" src="${resource(dir:'components/comment',file:'comment.js')}"></script>
		<jqgrid:resources />
		<g:javascript src="jqgrid-support.js" />
		<g:javascript src="bootstrap.js" />
		<g:javascript src="angular/plugins/ui-bootstrap-tpls-0.10.0.min.js" />
		<g:javascript src="angular/plugins/ngGrid/ng-grid-2.0.7.min.js" />
		<g:javascript src="angular/plugins/ngGrid/ng-grid-layout.js" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datepicker.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tds.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'components/comment',file:'comment.css')}" />
        
		<script type="text/javascript">
			function onInvokeAction(id) {
				setExportToLimit(id, '');
				createHiddenInputFieldsForLimitAndSubmit(id);
			}
			function onInvokeExportAction(id) {
				var parameterString = createParameterStringForLimit(id);
				location.href = '../list?' + parameterString;
			}
			$(document).ready(function() {
				$('#assetMenu').show();
				$("#showEntityView").dialog({ autoOpen: false })
				$("#editEntityView").dialog({ autoOpen: false })
				$("#createEntityView").dialog({ autoOpen: false })
				$("#cablingDialogId").dialog({ autoOpen:false })
				currentMenuId = "#assetMenu";
				$("#assetMenuId a").css('background-color','#003366')
				$(".span_ready").parent().addClass("task_ready")
				$(".span_hold").parent().addClass("task_hold")
				$(".span_started").parent().addClass("task_started")
				$(".span_pending").parent().addClass("task_pending")
				$(".span_planned").parent().addClass("task_planned")
				$(".span_completed").parent().addClass("task_completed")
				$(".span_na").parent().addClass("task_na")
				
				<jqgrid:grid id="listCommentGridId" url="'${createLink(action: 'listCommentJson')}'"
					colNames="'Action', 'Description','Updated', 'Type', 'Asset', 'AssetType','category'"
					colModel="{name:'act', index: 'act' , sortable: false, formatter: myCustomFormatter, search:false, width:40, fixed:true},
						{name:'comment', width:'350',formatter: myLinkFormatter},
						{name:'lastUpdated',width:'80',formatter: myLinkFormatter},
						{name:'commentType',width:'80',formatter: myLinkFormatter},
						{name:'assetName', width:'180',formatter: assetFormatter},
						{name:'assetType',width:'80',formatter: myLinkFormatter},
						{name:'category',width:'80',formatter: myLinkFormatter}"
					sortname="'lastUpdated'"
					sortorder="'desc'"
					caption="'Asset Comment:'"
					gridComplete="function(){bindResize('listCommentGridId');recompileDOM('listCommentGridIdWrapper');}"
					showPager="true">
					<jqgrid:filterToolbar id="listCommentGridId" searchOnEnter="false" />
					<jqgrid:navigation id="listCommentGridId" add="false" edit="false" del="false" search="false"/>
					<jqgrid:refreshButton id="listCommentGridId" />
				</jqgrid:grid>
				$.jgrid.formatter.integer.thousandsSeparator='';
				function myLinkFormatter (cellvalue, options, rowObjcet) {
					var value = cellvalue ? cellvalue : ''
						return '<span class="Arrowcursor" ng-click="comments.showCommentById(\''+options.rowId+'\',\'comment\')">'+value+'</span>'
				}
				function myCustomFormatter (cellVal,options,rowObject) {
					var editButton = '<a ng-click="comments.editCommentById(\''+options.rowId+'\',\'comment\')">'+
						"<img src='${resource(dir:'icons',file:'database_edit.png')}' border='0px'/>"+"</a>&nbsp;&nbsp;"
					return editButton
				}
				function assetFormatter(cellVal,options,rowObject){
					return cellVal ? '<span class="Arrowcursor" onclick= "getEntityDetails(\'listComment\', \''+rowObject[5]+'\', '+rowObject[7]+')\" >' + (cellVal) + '</span>' : "" 
				} 
			});
		</script>
	</head>
	<body>
		<div class="body fluid" ng-app="tdsComments" ng-controller="tds.comments.controller.MainController as comments">
			<h1>Asset Comment</h1>
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			<div>
				<jqgrid:wrapper id="listCommentGridId" />
				<div id="showEntityView" style="display: none;"></div>
				<div id="editEntityView" style="display: none;"></div>
				<div id="createEntityView" style="display: none;"></div>
				<div id="cablingDialogId" style="display: none;"></div>
				<g:render template="../assetEntity/newDependency" model="['forWhom':'Server', entities:servers]"></g:render>
			</div>
		</div>
        <g:render template="../assetEntity/initAssetEntityData"/>
	</body>
</html>