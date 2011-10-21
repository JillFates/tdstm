<%@page import="com.tds.asset.Files" %>
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
	$("#createFilesView").dialog({ autoOpen: false })
	$("#showFilesView").dialog({ autoOpen: false })
	$("#editFilesView").dialog({ autoOpen: false })
	$('#assetMenu').show();
	$('#reportsMenu').hide();
})
</script>

<title>Files List</title>
</head>
<g:if test="${flash.message}">
<div class="message">${flash.message}</div>
</g:if>

<div id="jmesaId" class="body">
	<h1>Files List</h1>
	<form name="listFileForm" action="list">
		<jmesa:tableFacade id="tag" items="${filesList}" maxRows="50"
			exportTypes="csv,excel" stateAttr="restore" var="fileInstance"
			autoFilterAndSort="true" maxRowsIncrements="50,100,200">
			<jmesa:htmlTable style=" border-collapse: separate" editable="true">
				<jmesa:htmlRow highlighter="true">
					<jmesa:htmlColumn property="id" sortable="false" filterable="false"
						cellEditor="org.jmesa.view.editor.BasicCellEditor" title="Actions">
						<g:remoteLink controller="assetEntity" action="editShow"
							id="${fileInstance.id}">
							<img
								src="${createLinkTo(dir:'images/skin',file:'database_edit.png')}"
								border="0px" />
						</g:remoteLink>
					</jmesa:htmlColumn>
					<jmesa:htmlColumn property="fileFormat" sortable="true"
						title="FileFormat" filterable="true"
						cellEditor="org.jmesa.view.editor.BasicCellEditor">
						<a href="javascript:getFilesDetails(${fileInstance.id})">${fileInstance.fileFormat}</a>
					</jmesa:htmlColumn>
					<jmesa:htmlColumn property="fileSize" title="FileSize"
						sortable="true" filterable="true"
						cellEditor="org.jmesa.view.editor.BasicCellEditor">
						<a href="javascript:getFilesDetails(${fileInstance.id})">${fileInstance.fileSize}</a>
					</jmesa:htmlColumn>
					<jmesa:htmlColumn property="moveBundle" sortable="true"
						filterable="true"
						cellEditor="org.jmesa.view.editor.BasicCellEditor">
						<a href="javascript:getFilesDetails(${fileInstance.id})">${fileInstance.moveBundle}</a>
					</jmesa:htmlColumn>
					<jmesa:htmlColumn property="planStatus" sortable="true"
						filterable="true"
						cellEditor="org.jmesa.view.editor.BasicCellEditor">
						<a href="javascript:getFilesDetails(${fileInstance.id})">${fileInstance.planStatus}</a>
					</jmesa:htmlColumn>

				</jmesa:htmlRow>
			</jmesa:htmlTable>
		</jmesa:tableFacade>
	</form>
	<div class="buttons">
		<span class="button"><input type="button" class="save"
			value="Create Files"
			onclick="${remoteFunction(action:'create', onComplete:'createFileView(e)')}" />
		</span>
	</div>
	<div id="createFilesView" style="display: none;" title="Create Files"></div>
	<div id="showFilesView" style="display: none;" title="Show Files"></div>
	<div id="editFilesView" style="display: none;" title="Edit Files"></div>
	<div style="display: none;">
     <table id="assetDependencyRow">
	  <tr>
		<td><g:select name="dataFlowFreq" from="${assetDependency.constraints.dataFlowFreq.inList}"></g:select></td>
		<td><g:select name="asset" from="${Files.findAllByAssetType('File')}" optionKey="id" optionValue="assetName"></g:select></td>
		<td><g:select name="dtype" from="${assetDependency.constraints.type.inList}"></g:select></td>
		<td><g:select name="status" from="${assetDependency.constraints.status.inList}"></g:select></td>
	</tr>
	</table>
    </div>
</div>

</div>

<div id="dbShowView" style="display: none;"></div>

</html>
<script type="text/javascript">
 function getFilesDetails(value){
	   var val = value
	   ${remoteFunction(action:'show', params:'\'id=\' + val ', onComplete:"showFileView(e)")}
}
function showFileView(e){
	 var resp = e.responseText;
	 $("#showFilesView").html(resp);
	 $("#showFilesView").dialog('option', 'width', 'auto')
	 $("#showFilesView").dialog('option', 'position', ['center','top']);
	 $("#showFilesView").dialog('open');
}
function createFileView(e){
	 var resp = e.responseText;
	 $("#createFilesView").html(resp);
	 $("#createFilesView").dialog('option', 'width', 'auto')
	 $("#createFilesView").dialog('option', 'position', ['center','top']);
	 $("#createFilesView").dialog('open');
}
function editFile(value){
	var resp = value
	${remoteFunction(action:'edit', params:'\'id=\' + value ', onComplete:'editFileView(e)')}
}
function editFileView(e){
	 var resp = e.responseText;
	 $("#editFilesView").html(resp);
	 $("#editFilesView").dialog('option', 'width', 'auto')
	 $("#editFilesView").dialog('option', 'position', ['center','top']);
	 $("#editFilesView").dialog('open');
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