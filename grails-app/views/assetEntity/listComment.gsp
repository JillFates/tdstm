<%@page import="com.tds.asset.AssetComment"%>
<%@page defaultCodec="html" %> 

<html>
	<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
	<meta name="layout" content="topNav" />
		<title>Comments</title>
		<g:javascript src="asset.tranman.js" />
		<g:javascript src="entity.crud.js" />

		<g:render template="../layouts/responsiveAngularResources" />

		<jqgrid:resources />
		<g:javascript src="jqgrid-support.js" />

		<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datepicker.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tds.css')}" />
        
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
				$(".menu-parent-assets-comments-list").addClass('active');
				$(".menu-parent-assets").addClass('active');
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
					rowList="${ raw(com.tdsops.common.ui.Pagination.optionsAsText()) }"
					caption="'Asset Comment:'"
					gridComplete="function(){bindResize('listCommentGridId');recompileDOM('listCommentGridIdWrapper');}"
					showPager="true">
					<jqgrid:navigation id="listCommentGridId" add="false" edit="false" del="false" search="false"/>
					<jqgrid:refreshButton id="listCommentGridId" />
				</jqgrid:grid>
				TDS.jqGridFilterToolbar('listCommentGridId');

				$.jgrid.formatter.integer.thousandsSeparator='';
				function myLinkFormatter (cellvalue, options, rowObjcet) {
					var value = cellvalue ? _.escape(cellvalue) : ''
						return '<span class="Arrowcursor" ng-click="comments.showCommentById(\''+options.rowId+'\',\'comment\')">'+value+'</span>'
				}
				function myCustomFormatter (cellVal,options,rowObject) {
					var editButton = '';
					if (${canEditComments})
						editButton = '<a ng-click="comments.editCommentById(\''+options.rowId+'\',\'comment\')">'+
							"<img src='${resource(dir:'icons',file:'comment_edit.png')}' border='0px'/>"+"</a>"
					return editButton
				}
				function assetFormatter(cellVal,options,rowObject) {
					return cellVal ? '<span class="Arrowcursor" onclick="EntityCrud.showAssetDetailView(\''+rowObject[8]+'\', '+rowObject[7]+ ')">' + _.escape(cellVal) + '</span>' : "" 
				} 
			});
		</script>
	</head>
	<body>
		<tds:subHeader title="Asset Comment" crumbs="['Assets','Comments']"/>
		<div id="commentScopeId" class="body fluid" ng-app="tdsComments" ng-controller="tds.comments.controller.MainController as comments">

			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			<div>
				<jqgrid:wrapper id="listCommentGridId" />
				<g:render template="../assetEntity/entityCrudDivs" />
				<g:render template="../assetEntity/dependentAdd" />
			</div>
		</div>
        <g:render template="../assetEntity/initAssetEntityData"/>
	</body>
</html>