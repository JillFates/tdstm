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
	$("#appShowView").dialog({ autoOpen: false })
})
</script>

<title>APPLICATION LIST</title>
</head>

<div id= "jmesaId" class="body">
<h1>ApplicationList</h1>
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
		        		<a  href="javascript:getAppDetails(${assetEntityInstance.id})">${assetEntityInstance.application}</a>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="appOwner"   sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<a  href="javascript:getAppDetails(${assetEntityInstance.id})">${assetEntityInstance.appOwner}</a>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="appSme" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span id="appSme_${assetEntityInstance.id}" onclick="showAppsDetails( ${assetEntityInstance.id} )">${assetEntityInstance.appSme}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="moveBundle" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span id="moveBundle_${assetEntityInstance.id}" onclick="showAppsDetails( ${assetEntityInstance.id} )">${assetEntityInstance.moveBundle}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="planStatus" sortable="true"  filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span id="planStatus_${assetEntityInstance.id}" onclick="showAppsDetails( ${assetEntityInstance.id} )">${assetEntityInstance.planStatus}</span>
		        	</jmesa:htmlColumn>
		        	
		        </jmesa:htmlRow>
			</jmesa:htmlTable>
		</jmesa:tableFacade>
	</form>
</div>
<div id="showDialog" title="Show Asset" style="display: none;">
<g:form action="save" method="post" name="showForm">
	<div class="dialog" id="showDiv">
	
	</div>
	<div class="buttons">
	<input type="hidden" name="id" value="" />
	<input type="hidden" name="projectId" value="${projectId}" />
	 <span class="button"><input
		type="button" class="edit" value="Edit"
		onClick="return editAssetDialog()" /></span> <span class="button"><g:actionSubmit 
		class="delete" onclick="return confirm('Delete Asset, are you sure?');"
		value="Delete" /></span>
		</div>
</g:form></div>
<div id="editDialog" title="Edit Asset" style="display: none;">
<g:form method="post" name="editForm">
	<input type="hidden" name="id" value="" />
	<input type="hidden" name="projectId" value="${projectId}" />
	<div class="dialog" id="editDiv">
	
	</div>
	<div class="buttons"><span class="button">
	<input type="button" class="save" value="Update Asset" />
	</span> <span class="button"><input type="button"
		class="delete" onclick="return editDialogDeleteRemove('delete')"
		value="Delete" /></span>
		</div>
</g:form></div>

<div id="appShowView" style="display: none; width: auto;" title="Show Applicaiton">

</div>

</html>
<script type ="text/javascript">
$('#assetMenu').show();
$('#reportsMenu').hide();
 function getAppDetails(value){
	   var val = value
	   ${remoteFunction(action:'show', params:'\'id=\' + value ', onComplete:"showAppView(e)")}
}
function showAppView(e){
	 var resp = e.responseText;
	 $("#appShowView").html(resp);
	 $("#appShowView").dialog('option', 'width', 'auto')
	 $("#appShowView").dialog('option', 'position', ['center','top']);
	 $("#appShowView").dialog('open');
}
</script>