<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="projectHeader" />
		<title>Event List</title>
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
		<jqgrid:resources />
		<g:javascript src="jqgrid-support.js" />
		<script type="text/javascript">
			$(document).ready(function() {
				var listCaption ="Event List: <tds:hasPermission permission='MoveEventEditView'><span class='capBtn'>"+
					"<input type='button' value='Create Event'  onClick=\"window.location.href=\'"+contextPath+"/moveEvent/create\'\"/></span></tds:hasPermission>"
				<jqgrid:grid id="moveEventListId" url="'${createLink(action: 'listJson')}'"
					colNames="'Name', 'Description','Status', 'Runbook Status', 'Bundles'"
					colModel="{name:'name',index: 'name', width:'300',formatter: linkFormatter},
						{name:'description'},
						{name:'inProgress'}, 
						{name:'runbookStatus'},
						{name:'moveBundlesString', search:false, sortable:false}"
					sortname="'name'"
					caption="listCaption"
					width="'100%'"
					gridComplete="function(){bindResize('moveEventListId')}"
					showPager="true">
					<jqgrid:filterToolbar id="moveEventListId" searchOnEnter="false" />
					<jqgrid:navigation id="moveEventListId" add="false" edit="false" del="false" search="false" refresh="true" />
				</jqgrid:grid>
				
			})
			
			function linkFormatter (cellvalue, options, rowObjcet) {
				var value = cellvalue ? cellvalue : ''
				return "<a href="+contextPath+"/moveEvent/show/"+options.rowId+">"+value+"</a>"
			}
		</script>
		
	</head>
	<body>
		<div class="body fluid">
			<h1>Event List</h1>
			<g:if test="${flash.message}">
				<div class="message">
					${flash.message}
				</div>
			</g:if>
			<div>
				<jqgrid:wrapper id="moveEventListId" />
			</div>
		</div>
		<script>
			currentMenuId = "#eventMenu";
			$("#eventMenuId a").css('background-color','#003366')
		</script>
	</body>
</html>
