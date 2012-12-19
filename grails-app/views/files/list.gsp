<%@page import="com.tds.asset.AssetEntity;com.tds.asset.Application;com.tds.asset.Database;com.tds.asset.Files;"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Storage List</title>
<g:javascript src="asset.tranman.js" />
<g:javascript src="entity.crud.js" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'jquery.autocomplete.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.accordion.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.resizable.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.slider.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.tabs.css')}" />
<link rel="stylesheet" type="text/css" href="${resource(dir:"plugins/jmesa-0.8/css",file:"jmesa.css")}" />
<script language="javascript" src="${resource(dir:"plugins/jmesa-0.8/js",file:"jmesa.js")}"></script>
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
	//$('#assetMenu').show();
	$("#createEntityView").dialog({ autoOpen: false })
	$("#showEntityView").dialog({ autoOpen: false })
	$("#editEntityView").dialog({ autoOpen: false })
    $("#commentsListDialog").dialog({ autoOpen: false })
    $("#createCommentDialog").dialog({ autoOpen: false })
    $("#showCommentDialog").dialog({ autoOpen: false })
    $("#editCommentDialog").dialog({ autoOpen: false })
	$("#manufacturerShowDialog").dialog({ autoOpen: false })
	$("#modelShowDialog").dialog({ autoOpen: false })
})
</script>

</head>
<body>
<div class="body">
<h1>Storage List</h1>
<g:if test="${flash.message}">
<div class="message">${flash.message}</div>
</g:if>
<div id="jmesaId" class="body">
<tds:hasPermission permission='EditAndDelete'>
	<div style="margin-left: 5px;">
		<input id="selectAssetId" type="checkbox" onclick="selectAllAssets()" title="Select All" />&nbsp;&nbsp;<label for="selectAssetId" style="cursor:pointer;"> <b>All</b></label>
    </div>
</tds:hasPermission>
	<form name="listFileForm" action="list">
		<jmesa:tableFacade id="tag" items="${filesList}" maxRows="50"
			exportTypes="csv,excel" stateAttr="restore" var="fileInstance"
			autoFilterAndSort="true" maxRowsIncrements="50,100,200">
			<jmesa:htmlTable style=" border-collapse: separate" editable="true">
				<jmesa:htmlRow highlighter="true" style="cursor: pointer;">
					<jmesa:htmlColumn property="id" sortable="false" filterable="false"
						cellEditor="org.jmesa.view.editor.BasicCellEditor" title="Actions">
					<tds:hasPermission permission='EditAndDelete'>
					    <g:checkBox name="assetCheckBox" id="checkId_${fileInstance.id}" onclick="enableButton(${filesList.id})"></g:checkBox>
						<a href="javascript:editEntity('files','${fileInstance?.assetType}',${fileInstance?.id})"><img src="${resource(dir:'images/skin',file:'database_edit.png')}" border="0px"/></a>
					</tds:hasPermission>	
					<tds:hasPermission permission="CommentCrudView">
						<span id="icon_${fileInstance.id}">
							<g:if test="${fileInstance.commentType == 'issue'}">
								<g:remoteLink controller="assetEntity" action="listComments" id="${fileInstance.id}" before='setAssetId(${fileInstance.id});'	onComplete="listCommentsDialog( e ,'never' );">
									<img src="${resource(dir:'i',file:'db_table_red.png')}"	border="0px"/>
								</g:remoteLink>
							</g:if>
							<g:elseif test="${fileInstance.commentType == 'comment'}">
								<g:remoteLink controller="assetEntity" action="listComments" id="${fileInstance.id}" before="setAssetId(${fileInstance.id});" onComplete="listCommentsDialog( e ,'never' ); ">
									<img src="${resource(dir:'i',file:'db_table_bold.png')}" border="0px"/>
								</g:remoteLink>
							</g:elseif>
							<g:else>
							<a href="javascript:createNewAssetComment(${fileInstance.id},'${fileInstance.assetName}');">
								<img src="${resource(dir:'i',file:'db_table_light.png')}" border="0px"/>
							</a>
							</g:else>
						</span>
					</tds:hasPermission>
					</jmesa:htmlColumn>
					<jmesa:htmlColumn property="assetName" title="Name"
						sortable="true" filterable="true"
						cellEditor="org.jmesa.view.editor.BasicCellEditor">
						<span onclick="getEntityDetails('files','${fileInstance.assetType}', ${fileInstance.id})">${fileInstance.assetName}</span>
					</jmesa:htmlColumn>
					<jmesa:htmlColumn property="fileFormat" sortable="true"
						title="Storage Format" filterable="true"
						cellEditor="org.jmesa.view.editor.BasicCellEditor">
						<span onclick="getEntityDetails('files','${fileInstance.assetType}', ${fileInstance.id})">${fileInstance.fileFormat}</span>
					</jmesa:htmlColumn>
					<jmesa:htmlColumn property="fileSize" title="Storage Size"
						sortable="true" filterable="true"
						cellEditor="org.jmesa.view.editor.BasicCellEditor">
						<span onclick="getEntityDetails('files','${fileInstance.assetType}', ${fileInstance.id})">${fileInstance.fileSize}</span>
					</jmesa:htmlColumn>
					<jmesa:htmlColumn property="planStatus" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
						<span onclick="getEntityDetails('files','${fileInstance.assetType}', ${fileInstance.id})">${fileInstance.planStatus}</span>
					</jmesa:htmlColumn>
					<jmesa:htmlColumn property="moveBundle" title="Bundle" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
						<span onclick="getEntityDetails('files','${fileInstance.assetType}', ${fileInstance.id})">${fileInstance.moveBundle}</span>
					</jmesa:htmlColumn>
					<jmesa:htmlColumn property="dependencyBundleNumber" title="Dep #" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
						<span onclick="getEntityDetails('files','${fileInstance.assetType}', ${fileInstance.id})">${fileInstance.dependencyBundleNumber}</span>
					</jmesa:htmlColumn>
					<jmesa:htmlColumn property="depUp" sortable="true"  filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span onclick="getEntityDetails('files','${fileInstance.assetType}', ${fileInstance.id} )">${fileInstance.depUp}</span>
		        	</jmesa:htmlColumn>
		        	<jmesa:htmlColumn property="depDown" sortable="true"  filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
		        		<span onclick="getEntityDetails('files','${fileInstance.assetType}', ${fileInstance.id} )">${fileInstance.depDown}</span>
		        	</jmesa:htmlColumn>

				</jmesa:htmlRow>
			</jmesa:htmlTable>
		</jmesa:tableFacade>
	</form>
	<div class="buttons">
	<tds:hasPermission permission='EditAndDelete'>
		<span class="button"><input type="button" class="save"
			value="Create Storage"
			onclick="${remoteFunction(action:'create', onComplete:'createEntityView(e, \'Storage\')')}" />
		</span>
		<span class="button"><input id="deleteAsset" type="button" 
		    value="Delete Selected..." class="save" title="Delete Selected" disabled="disabled"  
		    onclick="deleteAssets(${filesList.id},'files')" /></span>
	</tds:hasPermission>
	</div>
<div id="createEntityView" style="display: none;" ></div>
<div id="showEntityView" style="display: none;"></div>
<div id="editEntityView" style="display: none;"></div>	
<div style="display: none;">
     <table id="assetDependencyRow">
	  <tr>
		<td><g:select name="dataFlowFreq" from="${assetDependency.constraints.dataFlowFreq.inList}"></g:select></td>
		<td><g:select name="entity" from="['Server','Application','Database','Storage']" onchange='updateAssetsList(this.name, this.value)'></g:select></td>
		<td><g:select name="asset" from="${servers}" optionKey="id" optionValue="assetName" style="width:90px;"></g:select></td>
		<td><g:select name="dtype" from="${dependencyType.value}"  optionValue="value"></g:select></td>
		<td><g:select name="status" from="${dependencyStatus.value}" optionValue="value"></g:select></td>
	</tr>
	</table>
    </div>
     <div style="display: none;">
		<span id="Server"><g:select name="asset" from="${servers}" optionKey="id" optionValue="assetName" style="width:90px;"></g:select></span>
		<span id="Application"><g:select name="asset" from="${applications}" optionKey="id" optionValue="assetName" style="width:90px;"></g:select></span>
		<span id="Database"><g:select name="asset" from="${dbs}" optionKey="id" optionValue="assetName" style="width:90px;"></g:select></span>
		<span id="Storage"><g:select name="asset" from="${files}" optionKey="id" optionValue="assetName" style="width:90px;"></g:select></span>
	</div>
</div>
</div>
<g:render template="../assetEntity/commentCrud"/>
<g:render template="../assetEntity/modelDialog"/>
<script>
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')
</script>
</body>
</html>
