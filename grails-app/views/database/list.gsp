<%@page import="com.tds.asset.AssetEntity;com.tds.asset.Application;com.tds.asset.Database;com.tds.asset.Files;"%>
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
	$("#showDBView").dialog({ autoOpen: false })
	$("#createDBView").dialog({ autoOpen: false })
	$("#editDBView").dialog({ autoOpen: false })
	$('#assetMenu').show();
    $("#commentsListDialog").dialog({ autoOpen: false })
    $("#createCommentDialog").dialog({ autoOpen: false })
    $("#showCommentDialog").dialog({ autoOpen: false })
    $("#editCommentDialog").dialog({ autoOpen: false })
})
</script>

<title>Db list</title>
</head>
<body>
<div class="body">
<h1>DB List</h1>
<g:if test="${flash.message}">
<div class="message">${flash.message}</div>
</g:if>
<input type="hidden" id="role" value="role"/>
<div id="jmesaId" class="body">
	<form name="listDBForm" action="list">
		<jmesa:tableFacade id="tag" items="${databaseList}" maxRows="50"
			exportTypes="csv,excel" stateAttr="restore" var="dataBaseInstance"
			autoFilterAndSort="true" maxRowsIncrements="50,100,200">
			<jmesa:htmlTable style=" border-collapse: separate" editable="true">
				<jmesa:htmlRow highlighter="true" style="cursor: pointer;">
					<jmesa:htmlColumn property="id" sortable="false" filterable="false"
						cellEditor="org.jmesa.view.editor.BasicCellEditor" title="Actions">
						<a href="javascript:editDb(${dataBaseInstance?.id})"><img src="${createLinkTo(dir:'images/skin',file:'database_edit.png')}" border="0px"/></a>
						<span id="icon_${dataBaseInstance.id}">
							<g:if test="${dataBaseInstance.commentType == 'issue'}">
								<g:remoteLink controller="assetEntity" action="listComments" id="${dataBaseInstance.id}" before='setAssetId(${dataBaseInstance.id});'	onComplete="listCommentsDialog( e ,'never' );">
									<img src="${createLinkTo(dir:'i',file:'db_table_red.png')}"	border="0px"/>
								</g:remoteLink>
							</g:if>
							<g:elseif test="${dataBaseInstance.commentType == 'comment'}">
								<g:remoteLink controller="assetEntity" action="listComments" id="${dataBaseInstance.id}" before="setAssetId(${dataBaseInstance.id});" onComplete="listCommentsDialog( e ,'never' ); ">
									<img src="${createLinkTo(dir:'i',file:'db_table_bold.png')}" border="0px"/>
								</g:remoteLink>
							</g:elseif>
							<g:else>
							<a href="javascript:createNewAssetComment(${appEntityInstance.id});">
								<img src="${createLinkTo(dir:'i',file:'db_table_light.png')}" border="0px"/>
							</a>
							</g:else>
						</span>
					</jmesa:htmlColumn>
					<jmesa:htmlColumn property="assetName" title="Name" sortable="true"
						filterable="true"
						cellEditor="org.jmesa.view.editor.BasicCellEditor">
							<span onclick="getDbDetails('${dataBaseInstance.assetType}', ${dataBaseInstance.id})">${dataBaseInstance.assetName}</span>
					</jmesa:htmlColumn>
					<jmesa:htmlColumn property="dbFormat" sortable="true"
						title="DB Format" filterable="true"
						cellEditor="org.jmesa.view.editor.BasicCellEditor">
						<span onclick="getDbDetails('${dataBaseInstance.assetType}', ${dataBaseInstance.id})">${dataBaseInstance.dbFormat}</a>/
					</jmesa:htmlColumn>
					<jmesa:htmlColumn property="moveBundle" sortable="true"
						filterable="true"
						cellEditor="org.jmesa.view.editor.BasicCellEditor">
						<span onclick="getDbDetails('${dataBaseInstance.assetType}', ${dataBaseInstance.id})">${dataBaseInstance.moveBundle}</span>
					</jmesa:htmlColumn>
					<jmesa:htmlColumn property="planStatus" sortable="true"
						filterable="true"
						cellEditor="org.jmesa.view.editor.BasicCellEditor">
						<span onclick="getDbDetails('${dataBaseInstance.assetType}', ${dataBaseInstance.id})">${dataBaseInstance.planStatus}</span>
					</jmesa:htmlColumn>
					<jmesa:htmlColumn property="depUp" sortable="true"  filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span onclick="getDbDetails('${dataBaseInstance.assetType}', ${dataBaseInstance.id} )">${dataBaseInstance.depUp}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="depDown" sortable="true"  filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span onclick="getDbDetails('${dataBaseInstance.assetType}', ${dataBaseInstance.id} )">${dataBaseInstance.depDown}</span>
		        	</jmesa:htmlColumn>
				</jmesa:htmlRow>
			</jmesa:htmlTable>
		</jmesa:tableFacade>
	</form>
	
	<div class="buttons">
		<span class="button"><input type="button" class="save" value="Create DB"
			onclick="${remoteFunction(action:'create', onComplete:'createDbView(e)')}" />
		</span>
	</div>
	<div id="createDBView" style="display: none;" title="Create Database"></div>
	<div id="showDBView" style="display: none;" title="Show Database"></div>
	<div id="editDBView" style="display: none;" title="Edit Database"></div>
	<div style="display: none;">
     <table id="assetDependencyRow">
	  <tr>
		<td><g:select name="dataFlowFreq" from="${assetDependency.constraints.dataFlowFreq.inList}"></g:select></td>
		<td><g:select name="entity" from="['Server','Application','Database','Files']" onchange='updateAssetsList(this.name, this.value)'></g:select></td>
		<td><g:select name="asset" from="${servers}" optionKey="id" optionValue="assetName" style="width:90px;"></g:select></td>
		<td><g:select name="dtype" from="${assetDependency.constraints.type.inList}"></g:select></td>
		<td><g:select name="status" from="${assetDependency.constraints.status.inList}"></g:select></td>
	</tr>
	</table>
    </div>
    <div style="display: none;">
		<span id="Server"><g:select name="asset" from="${servers}" optionKey="id" optionValue="assetName" style="width:90px;"></g:select></span>
		<span id="Application"><g:select name="asset" from="${applications}" optionKey="id" optionValue="assetName" style="width:90px;"></g:select></span>
		<span id="Database"><g:select name="asset" from="${dbs}" optionKey="id" optionValue="assetName" style="width:90px;"></g:select></span>
		<span id="Files"><g:select name="asset" from="${files}" optionKey="id" optionValue="assetName" style="width:90px;"></g:select></span>
	</div>
</div>
<g:render template="../assetentity/commentCrud"/>
</div>
<script type="text/javascript">
 function getDbDetails(type, value){
	 if(type == "Database"){
	     var val = value
	     ${remoteFunction(action:'show',controller:'database', params:'\'id=\' + val ', onComplete:"showDbView(e)")}
	 }
}
function showDbView(e){
	 var resp = e.responseText;
	 $("#showDBView").html(resp);
	 $("#showDBView").dialog('option', 'width', 'auto')
	 $("#showDBView").dialog('option', 'position', ['center','top']);
	 $("#showDBView").dialog('open');
	 $("#createDBView").dialog('close');
	 $("#editDBView").dialog('close');
}
function createDbView(e){
	 var resp = e.responseText;
	 $("#createDBView").html(resp);
	 $("#createDBView").dialog('option', 'width', 'auto')
	 $("#createDBView").dialog('option', 'position', ['center','top']);
	 $("#createDBView").dialog('open');
	 $("#showDBView").dialog('close');
	 $("#editDBView").dialog('close');
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
	 $("#showDBView").dialog('close');
	 $("#createDBView").dialog('close');
}
function addAssetDependency( type ){
	var rowNo = $("#"+type+"Count").val()
	var rowData = $("#assetDependencyRow tr").html().replace("dataFlowFreq","dataFlowFreq_"+type+"_"+rowNo).replace("asset","asset_"+type+"_"+rowNo).replace("dtype","dtype_"+type+"_"+rowNo).replace("status","status_"+type+"_"+rowNo).replace("entity","entity_"+type+"_"+rowNo)
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
function updateAssetsList( name, value ){
	var idValues = name.split("_")
	$("select[name='asset_"+idValues[1]+"_"+idValues[2]+"']").html($("#"+value+" select").html())
}
</script>
</body>
</html>
