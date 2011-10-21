<%@page import="com.tds.asset.Database"%>
<%@page import="com.tds.asset.AssetEntity"%>
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
    location.href = 'listApps?' + parameterString;
}
$(document).ready(function() {
	$("#showDBView").dialog({ autoOpen: false })
	$("#createDBView").dialog({ autoOpen: false })
	$("#editDBView").dialog({ autoOpen: false })
	$('#assetMenu').show();
	$('#reportsMenu').hide();
})
</script>

<title>DATABASE LIST</title>
</head>

<g:if test="${flash.message}">
<div class="message">${flash.message}</div>
</g:if>

<div id="jmesaId" class="body">
	<h1>DB List</h1>
	<form name="listDBForm" action="list">
		<jmesa:tableFacade id="tag" items="${databaseList}" maxRows="50"
			exportTypes="csv,excel" stateAttr="restore" var="dataBaseInstance"
			autoFilterAndSort="true" maxRowsIncrements="50,100,200">
			<jmesa:htmlTable style=" border-collapse: separate" editable="true">
				<jmesa:htmlRow highlighter="true">
					<jmesa:htmlColumn property="id" sortable="false" filterable="false"
						cellEditor="org.jmesa.view.editor.BasicCellEditor" title="Actions">
						<g:remoteLink controller="assetEntity" action="editShow"
							id="${dataBaseInstance.id}">
							<img
								src="${createLinkTo(dir:'images/skin',file:'database_edit.png')}"
								border="0px" />
						</g:remoteLink>
					</jmesa:htmlColumn>
					<jmesa:htmlColumn property="dbFormat" sortable="true"
						title="DBFormat" filterable="true"
						cellEditor="org.jmesa.view.editor.BasicCellEditor">
						<a href="javascript:getDbDetails(${dataBaseInstance.id})">${dataBaseInstance.dbFormat}</a>
					</jmesa:htmlColumn>
					<jmesa:htmlColumn property="dbSize" title="DBSize" sortable="true"
						filterable="true"
						cellEditor="org.jmesa.view.editor.BasicCellEditor">
						<a href="javascript:getDbDetails(${dataBaseInstance.id})">${dataBaseInstance.dbSize}</a>
					</jmesa:htmlColumn>
					<jmesa:htmlColumn property="moveBundle" sortable="true"
						filterable="true"
						cellEditor="org.jmesa.view.editor.BasicCellEditor">
						<a href="javascript:getDbDetails(${dataBaseInstance.id})">${dataBaseInstance.moveBundle}</a>
					</jmesa:htmlColumn>
					<jmesa:htmlColumn property="planStatus" sortable="true"
						filterable="true"
						cellEditor="org.jmesa.view.editor.BasicCellEditor">
						<a href="javascript:getDbDetails(${dataBaseInstance.id})">${dataBaseInstance.planStatus}</a>
					</jmesa:htmlColumn>

				</jmesa:htmlRow>
			</jmesa:htmlTable>
		</jmesa:tableFacade>
	</form>
	<div class="buttons">
		<span class="button"><input type="button" class="save"
			value="Create DB"
			onclick="${remoteFunction(action:'create', onComplete:'createDbView(e)')}" />
		</span>
	</div>
	<div id="createDBView" style="display: none;" title="Create DB"></div>
	<div id="showDBView" style="display: none;" title="Show DB"></div>
	<div id="editDBView" style="display: none;" title="Edit DB"></div>
	<div style="display: none;">
     <table id="assetDependencyRow">
	  <tr>
		<td><g:select name="dataFlowFreq" from="${assetDependency.constraints.dataFlowFreq.inList}"></g:select></td>
		<td><g:select name="asset" from="${Database.findAllByAssetType('DataBase')}" optionKey="id" optionValue="assetName"></g:select></td>
		<td><g:select name="dtype" from="${assetDependency.constraints.type.inList}"></g:select></td>
		<td><g:select name="status" from="${assetDependency.constraints.status.inList}"></g:select></td>
	</tr>
	</table>
    </div>
</div>

</div>


</html>
<script type="text/javascript">
 function getDbDetails(value){
     var val = value
     ${remoteFunction(action:'show',controller:'database', params:'\'id=\' + val ', onComplete:"showDbView(e)")}
}
function showDbView(e){
	 var resp = e.responseText;
	 $("#showDBView").html(resp);
	 $("#showDBView").dialog('option', 'width', 'auto')
	 $("#showDBView").dialog('option', 'position', ['center','top']);
	 $("#showDBView").dialog('open');
}
function createDbView(e){
	 var resp = e.responseText;
	 $("#createDBView").html(resp);
	 $("#createDBView").dialog('option', 'width', 'auto')
	 $("#createDBView").dialog('option', 'position', ['center','top']);
	 $("#createDBView").dialog('open');
}
function editDb(value){
     var val = value
     ${remoteFunction(action:'edit', params:'\'id=\' + val ', onComplete:'editDbView(e)')}
}
function editDbView(e){
	 var resp = e.responseText;
	 $("#editDBView").html(resp);
	 $("#editDBView").dialog('option', 'width', 'auto')
	 $("#editDBView").dialog('option', 'position', ['center','top']);
	 $("#editDBView").dialog('open');
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