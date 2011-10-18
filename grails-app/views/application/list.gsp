<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<g:javascript src="asset.tranman.js" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'jquery.autocomplete.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.accordion.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.resizable.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.slider.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.tabs.css')}" />

<link rel="stylesheet" type="text/css" href="${createLinkTo(dir:"plugins/jmesa-0.8/css",file:"jmesa.css")}" />
<script language="javascript" src="${createLinkTo(dir:"plugins/jmesa-0.8/js",file:"jmesa.js")}"></script>

<script type="text/javascript">
function onInvokeAction(id) {
    setExportToLimit(id, '');
    createHiddenInputFieldsForLimitAndSubmit(id);
}
function onInvokeExportAction(id) {
    var parameterString = createParameterStringForLimit(id);
    location.href = 'listApps?' + parameterString;
}
$(document).ready(function() {
	$("#createAppView").dialog({ autoOpen: false })
	$("#showAppView").dialog({ autoOpen: false })
	$("#editAppView").dialog({ autoOpen: false })
	$('#assetMenu').show();
	$('#reportsMenu').hide();
})
</script>

<title>APPLICATION LIST</title>
</head>
<body>
<div class="body">
<h1>Application List</h1>
<g:if test="${flash.message}">
<div class="message">${flash.message}</div>
</g:if>
<div id= "jmesaId">
	<form name="listAppsForm" action="listApps">
		<jmesa:tableFacade id="tag" items="${assetEntityList}" maxRows="50" exportTypes="csv,excel" stateAttr="restore" var="assetEntityInstance" autoFilterAndSort="true" maxRowsIncrements="50,100,200">
		    <jmesa:htmlTable style=" border-collapse: separate" editable="true">
		        <jmesa:htmlRow highlighter="true">
		        	<jmesa:htmlColumn property="id" sortable="false" filterable="false" cellEditor="org.jmesa.view.editor.BasicCellEditor" title="Actions" >
		        		<g:remoteLink controller="assetEntity" action="editShow" id="${assetEntityInstance.id}" >
							<img src="${createLinkTo(dir:'images/skin',file:'database_edit.png')}" border="0px"/>
						</g:remoteLink>
						</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="application" sortable="true" title="Application" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span id="appSme_${assetEntityInstance.id}" onclick="getAppDetails(${assetEntityInstance.id} )">${assetEntityInstance.application}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="appOwner"   sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span id="appSme_${assetEntityInstance.id}" onclick="getAppDetails(${assetEntityInstance.id} )">${assetEntityInstance.appOwner}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="appSme" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span id="appSme_${assetEntityInstance.id}" onclick="getAppDetails(${assetEntityInstance.id} )">${assetEntityInstance.appSme}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="moveBundle" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span id="moveBundle_${assetEntityInstance.id}" onclick="getAppDetails(${assetEntityInstance.id} )">${assetEntityInstance.moveBundle}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="planStatus" sortable="true"  filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span id="planStatus_${assetEntityInstance.id}" onclick="getAppDetails(${assetEntityInstance.id} )">${assetEntityInstance.planStatus}</span>
		        	</jmesa:htmlColumn>
		        	
		        </jmesa:htmlRow>
			</jmesa:htmlTable>
		</jmesa:tableFacade>
	</form>
</div>
<div class="buttons"> 
		<span class="button"><input type="button" class="save" value="Create App" onclick="${remoteFunction(action:'create', onComplete:'createAppView(e)')}"/></span>
</div>
<div id="createAppView" style="display: none;" title="Create Applicaiton"></div>
<div id="showAppView" style="display: none;" title="Show Applicaiton"></div>
<div id="editAppView" style="display: none;" title="Edit Application"></div>
</div>
<script type ="text/javascript">
function createAppView(e){
	 var resp = e.responseText;
	 $("#createAppView").html(resp);
	 $("#createAppView").dialog('option', 'width', 'auto')
	 $("#createAppView").dialog('option', 'position', ['center','top']);
	 $("#createAppView").dialog('open');
	 $("#editAppView").dialog('close');
	 $("#sAppView").dialog('close');
}
function getAppDetails(value){
	   var val = value
	   ${remoteFunction(action:'show', params:'\'id=\' + value ', onComplete:'showAppView(e)')}
}
function showAppView(e){
	 var resp = e.responseText;
	 $("#showAppView").html(resp);
	 $("#showAppView").dialog('option', 'width', 'auto')
	 $("#showAppView").dialog('option', 'position', ['center','top']);
	 $("#showAppView").dialog('open');
	 $("#editAppView").dialog('close');
	 $("#createAppView").dialog('close');
}
function editApp(value){
	var resp = value
	${remoteFunction(action:'edit', params:'\'id=\' + resp ', onComplete:'editAppView(e)')}
}
function editAppView(e){
     var resps = e.responseText;
     $("#editAppView").html(resps);
	 $("#editAppView").dialog('option', 'width', 'auto')
	 $("#editAppView").dialog('option', 'position', ['center','top']);
	 $("#editAppView").dialog('open');
	 $("#showAppView").dialog('close');
	 $("#createAppView").dialog('close');
}
function isValidDate( date ){
    var returnVal = true;
  	var objRegExp  = /^(0[1-9]|1[012])[/](0[1-9]|[12][0-9]|3[01])[/](19|20)\d\d ([0-1][0-9]|[2][0-3])(:([0-5][0-9])){1,2} ([APap][Mm])$/;
  	if( date && !objRegExp.test(date) ){
      	alert("Date should be in 'mm/dd/yyyy HH:MM AM/PM' format");
      	returnVal  =  false;
  	} 
  	return returnVal;
  }
</script>
</body>
</html>