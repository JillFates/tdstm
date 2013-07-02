<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Event List</title>
<link type="text/css" rel="stylesheet"
	href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
<link href="/tdstm/css/jqgrid/ui.jqgrid.css" rel="stylesheet"
	type="text/css">
<link id="jquery-ui-theme" media="screen, projection" rel="stylesheet"
	type="text/css"
	href="/tdstm/plugins/jquery-ui-1.8.15/jquery-ui/themes/ui-lightness/jquery-ui-1.8.15.custom.css">
<jqgrid:resources />
<script type="text/javascript">
$(document).ready(function() {
	var listCaption ="Event List:<span class='capBtn'>"+
		"<input type='button' value='Create Event'  onClick=\"window.location.href=\'"+contextPath+"/moveEvent/create\'\"/></span>"
	<jqgrid:grid id="moveEventListId" url="'${createLink(action: 'listJson')}'"
	    colNames="'Name', 'Description','Status', 'Runbook Status', 'Bundles'"
	    colModel="{name:'name',index: 'name', editable: true, width:'300',formatter: linkFormatter},
	                  {name:'description', editable: true},
	                  {name:'inProgress', editable: true}, 
	                  {name:'runbookStatus', editable: true},
	                  {name:'moveBundlesString', editable: true, search:false, sortable:false}"
	    sortname="'name'"
	    caption="listCaption"
	   	height="'100%'"
	    rowNum="'25'"
	    rowList= "'25','50','100'"
	    viewrecords="true"
	    showPager="true"
	    datatype="'json'">
	    <jqgrid:filterToolbar id="moveEventListId" searchOnEnter="false" />
	    <jqgrid:navigation id="moveEventListId" add="false" edit="false" del="false" search="false" refresh="true" />
	</jqgrid:grid>

	$('#moveEventListIdWrapper').width($('.fluid').width()-16) // 16 pixels comptensates for the border/padding/etc and the scrollbar
	$('#moveEventListIdGrid').fluidGrid({ base:'#moveEventListIdWrapper', offset: 0 });
	$(window).resize(resizeGrid);
	
})

function linkFormatter (cellvalue, options, rowObjcet) {
	var value = cellvalue ? cellvalue : ''
	return "<a href="+contextPath+"/moveEvent/show/"+options.rowId+">"+value+"</a>"
}


// Called when the window is resized to resize the grid wrapper 
function resizeGrid(){
	$('#moveEventListIdWrapper').width($('.fluid').width()-2) // 2 pixels comptensates for the border/padding/etc
	$('#moveEventListIdGrid').fluidGrid({ base:'#moveEventListIdWrapper', offset: 0 });
}
		
</script>

</head>
<body>
	<div class="body">
		<h1>Event List</h1>
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<div>
			<jqgrid:wrapper id="moveEventListId" />
		</div>
		<div class="paginateButtons">
			<tds:hasPermission permission='MoveEventEditView'>
				<span class="menuButton"><g:link class="create"
						action="create">Create New</g:link></span>
			</tds:hasPermission>
		</div>
	</div>
	<script>
	currentMenuId = "#eventMenu";
	$("#eventMenuId a").css('background-color','#003366')
</script>
</body>
</html>
