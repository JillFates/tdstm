<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="topNav" />
		<title>Event List</title>
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
		<jqgrid:resources />
		<g:javascript src="jqgrid-support.js" />
		<script type="text/javascript">
			$(document).ready(function() {
				var listCaption ="Event List: <tds:hasPermission permission='MoveEventEditView'><span class='capBtn'>"+
					"<input type='button' value='Create Event'  onClick=\"window.location.href=\'"+contextPath+"/moveEvent/create\'\"/></span></tds:hasPermission>"
				<jqgrid:grid id="moveEventListId" url="'${createLink(action: 'listJson')}'"
					colNames="'Name','Estimated Start', 'Description','News Bar Mode', 'Runbook Status', 'Bundles'"
					colModel="{name:'name',index: 'name', width:'300',formatter: linkFormatter},
						{name:'estStartTime', search:false, formatter: dateFormatter},
						{name:'description'},
						{name:'newsBarMode'}, 
						{name:'runbookStatus'},
						{name:'moveBundlesString', search:false, sortable:false}"
					sortname="'name'"
					caption="listCaption"
					width="'100%'"
					gridComplete="function(){bindResize('moveEventListId')}"
					showPager="true">
					<jqgrid:navigation id="moveEventListId" add="false" edit="false" del="false" search="false" refresh="true" />
				</jqgrid:grid>
				TDS.jqGridFilterToolbar('moveEventListId');
				
			})
			
			function linkFormatter (cellvalue, options, rowObjcet) {
				var value = cellvalue ? cellvalue : ''
				return "<a href="+contextPath+"/moveEvent/show/"+options.rowId+">"+value+"</a>"
			}
			
			function dateFormatter(cellvalue, options, rowObjcet) {
			    var value ='';
                if(cellvalue)
				{
					var date =  new Date(cellvalue);
					value = date.toLocaleDateString() +' '+ date.toLocaleTimeString();
				}
				return value;
            }

		</script>
		
	</head>
	<body>
		<tds:subHeader title="Event List" crumbs="['Planning','Event', 'List']"/>
		<div class="body fluid">
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
			$(".menu-parent-planning-event-list").addClass('active');
			$(".menu-parent-planning").addClass('active');

		</script>
	</body>
</html>
