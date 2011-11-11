<%@page import="com.tds.asset.AssetComment;com.tds.asset.AssetEntity;com.tds.asset.Application;com.tds.asset.Database;com.tds.asset.Files;com.tds.asset.AssetComment;"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Asset List</title>
<g:javascript src="asset.tranman.js" />
<g:javascript src="entity.crud.js" />
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
			$("#createEntityView").dialog({ autoOpen: false })
			$("#showEntityView").dialog({ autoOpen: false })
			$("#editEntityView").dialog({ autoOpen: false })
	        $("#commentsListDialog").dialog({ autoOpen: false })
	        $("#createCommentDialog").dialog({ autoOpen: false })
	        $("#showCommentDialog").dialog({ autoOpen: false })
	        $("#editCommentDialog").dialog({ autoOpen: false })
	        $("#manufacturerShowDialog").dialog({ autoOpen: false })
	        $("#modelShowDialog").dialog({ autoOpen: false })
	        $("#filterPane").draggable()
	        
})
</script>
</head>
<body>

<div class="body">
<h1>AssetList</h1>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<input type="hidden" id="role" value="role"/>
<div>
	<form name="assetEntityForm" action="list">
		<jmesa:tableFacade id="tag" items="${assetEntityList}" maxRows="25" exportTypes="csv,excel" stateAttr="restore" var="assetEntityInstance" autoFilterAndSort="true" maxRowsIncrements="25,50,100">
		    <jmesa:htmlTable style=" border-collapse: separate">
		        <jmesa:htmlRow highlighter="true">
		        	<jmesa:htmlColumn property="id" sortable="false" filterable="false" cellEditor="org.jmesa.view.editor.BasicCellEditor" title="Actions" >
		        		<a href="javascript:editEntity('assetEntity','${assetEntityInstance?.assetType}',${assetEntityInstance.id})"><img src="${createLinkTo(dir:'images/skin',file:'database_edit.png')}" border="0px"/></a>
						<span id="icon_${assetEntityInstance.id}">
							<g:if test="${assetEntityInstance.commentType == 'issue'}">
								<g:remoteLink controller="assetEntity" action="listComments" id="${assetEntityInstance.id}" before="setAssetId('${assetEntityInstance.id}');" onComplete="listCommentsDialog(e,'never');">
									<img src="${createLinkTo(dir:'i',file:'db_table_red.png')}" border="0px"/>
								</g:remoteLink>
							</g:if>
							<g:elseif test="${assetEntityInstance.commentType == 'comment'}">
							<g:remoteLink controller="assetEntity" action="listComments" id="${assetEntityInstance.id}" before="setAssetId('${assetEntityInstance.id}');" onComplete="listCommentsDialog(e,'never');">
								<img src="${createLinkTo(dir:'i',file:'db_table_bold.png')}" border="0px"/>
							</g:remoteLink>
							</g:elseif>
							<g:else>
							<a href="javascript:createNewAssetComment(${assetEntityInstance.id});">
								<img src="${createLinkTo(dir:'i',file:'db_table_light.png')}" border="0px"/>
							</a>
							</g:else>
						</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="application" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span onclick="getEntityDetails('assetEntity','${assetEntityInstance.assetType}', ${assetEntityInstance.id} )">${assetEntityInstance.application}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="assetName" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<a id="assetName_${assetEntityInstance.id}" href="javascript:getEntityDetails('assetEntity','${assetEntityInstance.assetType}', ${assetEntityInstance.id} )">${assetEntityInstance.assetName}</a>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="modelName" title="Model" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span id="model_${assetEntityInstance.id}" onclick="getEntityDetails('assetEntity','${assetEntityInstance.assetType}', ${assetEntityInstance.id} )">${assetEntityInstance.model}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="sourceLocation" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span id="sourceLocation_${assetEntityInstance.id}" onclick="getEntityDetails('assetEntity','${assetEntityInstance.assetType}', ${assetEntityInstance.id} )">${assetEntityInstance.sourceLocation}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="sourceRack" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span id="sourceRack_${assetEntityInstance.id}" onclick="getEntityDetails('assetEntity','${assetEntityInstance.assetType}', ${assetEntityInstance.id} )">${assetEntityInstance.sourceRack}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="targetLocation" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span id="targetLocation_${assetEntityInstance.id}" onclick="getEntityDetails('assetEntity','${assetEntityInstance.assetType}', ${assetEntityInstance.id} )">${assetEntityInstance.targetLocation}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="targetRack" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span id="targetRack_${assetEntityInstance.id}" onclick="getEntityDetails('assetEntity','${assetEntityInstance.assetType}', ${assetEntityInstance.id} )">${assetEntityInstance.targetRack}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="assetType" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span id="assetType_${assetEntityInstance.id}" onclick="getEntityDetails('assetEntity','${assetEntityInstance.assetType}', ${assetEntityInstance.id} )">${assetEntityInstance.assetType}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="assetTag" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span id="assetTag_${assetEntityInstance.id}" onclick="getEntityDetails('assetEntity','${assetEntityInstance.assetType}', ${assetEntityInstance.id} )">${assetEntityInstance.assetTag}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="serialNumber" title="Serial #" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span id="serialNumber_${assetEntityInstance.id}" onclick="getEntityDetails('assetEntity','${assetEntityInstance.assetType}', ${assetEntityInstance.id} )">${assetEntityInstance.serialNumber}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="moveBundle" title="Move Bundle" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span id="moveBundle_${assetEntityInstance.id}" onclick="getEntityDetails('assetEntity','${assetEntityInstance.assetType}', ${assetEntityInstance.id} )">${assetEntityInstance.moveBundle}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="depUp" sortable="true"  filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span onclick="getEntityDetails('assetEntity','${assetEntityInstance.assetType}', ${assetEntityInstance.id} )">${assetEntityInstance.depUp}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="depDown" sortable="true"  filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span onclick="getEntityDetails('assetEntity','${assetEntityInstance.assetType}', ${assetEntityInstance.id} )">${assetEntityInstance.depDown}</span>
		        	</jmesa:htmlColumn>
		        </jmesa:htmlRow>
			</jmesa:htmlTable>
		</jmesa:tableFacade>
	</form>
</div>
<div class="buttons"><g:form>
	<span class="button"><input type="button" value="New Asset" class="create" onclick="${remoteFunction(action:'create', onComplete:'createEntityView(e, \'Server\')')}"/></span>
</g:form></div>
</div> <%-- End of Body --%>
<g:render template="commentCrud"/>
<g:render template="modelDialog"/>
<div id="createEntityView" style="display: none;"></div>
<div id="showEntityView" style="display: none;"></div>
<div id="editEntityView" style="display: none;"></div>
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
<script type="text/javascript">
$('#assetMenu').show();
</script>
</body>
</html>
