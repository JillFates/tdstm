<%@page import="com.tds.asset.Application;"%>
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
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.datepicker.css')}" />

<link rel="stylesheet" type="text/css" href="${createLinkTo(dir:"plugins/jmesa-0.8/css",file:"jmesa.css")}" />
<script language="javascript" src="${createLinkTo(dir:"plugins/jmesa-0.8/js",file:"jmesa.js")}"></script>

<script type="text/javascript">
function onInvokeAction(id) {
    setExportToLimit(id, '');
    createHiddenInputFieldsForLimitAndSubmit(id);
}
function onInvokeExportAction(id) {
    var parameterString = createParameterStringForLimit(id);
    location.href = 'list?' + parameterString;
}
$(document).ready(function() {
	$("#createAppView").dialog({ autoOpen: false })
	$("#showAppView").dialog({ autoOpen: false })
	$("#editAppView").dialog({ autoOpen: false })
	$('#assetMenu').show();
	$('#reportsMenu').hide();
})
</script>

<title>Application list</title>
</head>
<body>
<div class="body">
<h1>Application List</h1>
<g:if test="${flash.message}">
<div class="message">${flash.message}</div>
</g:if>
<div id= "jmesaId">
	<form name="listAppsForm" action="list">
		<jmesa:tableFacade id="tag" items="${assetEntityList}" maxRows="50" exportTypes="csv,excel" stateAttr="restore" var="appEntityInstance" autoFilterAndSort="true" maxRowsIncrements="50,100,200">
		    <jmesa:htmlTable style=" border-collapse: separate" editable="true">
		        <jmesa:htmlRow highlighter="true">
		        	<jmesa:htmlColumn property="id" sortable="false" filterable="false" cellEditor="org.jmesa.view.editor.BasicCellEditor" title="Actions" >
						</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="assetName" title="Name" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span id="appSme_${appEntityInstance.id}" onclick="getAppDetails(${appEntityInstance.id} )">${appEntityInstance.assetName}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="appOwner"   sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span id="appSme_${appEntityInstance.id}" onclick="getAppDetails(${appEntityInstance.id} )">${appEntityInstance.appOwner}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="appSme" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span id="appSme_${appEntityInstance.id}" onclick="getAppDetails(${appEntityInstance.id} )">${appEntityInstance.appSme}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="moveBundle" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span id="moveBundle_${appEntityInstance.id}" onclick="getAppDetails(${appEntityInstance.id} )">${appEntityInstance.moveBundle}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="planStatus" sortable="true"  filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span id="planStatus_${appEntityInstance.id}" onclick="getAppDetails(${appEntityInstance.id} )">${appEntityInstance.planStatus}</span>
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
<div style="display: none;">
<table id="assetDependencyRow">
	<tr>
		<td><g:select name="dataFlowFreq" from="${assetDependency.constraints.dataFlowFreq.inList}"></g:select></td>
		<td><g:select name="asset" from="${Application.findAllByAssetType('Application')}" optionKey="id" optionValue="assetName"></g:select></td>
		<td><g:select name="dtype" from="${assetDependency.constraints.type.inList}"></g:select></td>
		<td><g:select name="status" from="${assetDependency.constraints.status.inList}"></g:select></td>
	</tr>
	</table>
</div>
</div>
<script type ="text/javascript">
function createAppView(e){
	 var resp = e.responseText;
	 $("#createAppView").html(resp);
	 $("#createAppView").dialog('option', 'width', 'auto')
	 $("#createAppView").dialog('option', 'position', ['center','top']);
	 $("#createAppView").dialog('open');
	 $("#editAppView").dialog('close');
	 $("#showAppView").dialog('close');
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
function addAssetDependency( type ){
	var rowNo = $("#"+type+"Count").val()
	var rowData = $("#assetDependencyRow tr").html().replace("dataFlowFreq","dataFlowFreq_"+type+"_"+rowNo).replace("asset","asset_"+type+"_"+rowNo).replace("dtype","dtype_"+type+"_"+rowNo).replace("status","status_"+type+"_"+rowNo)
	if(type!="support"){
		$("#createDependentsList").append("<tr id='row_d_"+rowNo+"'>"+rowData+"<td><a href=\"javascript:deleteRow(\'row_d_"+rowNo+"')\"><span class='clear_filter'><u>X</u></span></a></td></tr>")
	} else {
		$("#createSupportsList").append("<tr id='row_s_"+rowNo+"'>"+rowData+"<td><a href=\"javascript:deleteRow('row_s_"+rowNo+"')\"><span class='clear_filter'><u>X</u></span></a></td></tr>")
	}
	$("#"+type+"Count").val(parseInt(rowNo)+1)
}
function deleteRow( rowId ){
	$("#"+rowId).remove()
}
</script>
</body>
</html>