<%@page import="com.tds.asset.AssetType;"%>
<tds:hasPermission permission='AssetEdit'>
	<span class="button"><input type="button" class="edit" value="Edit" onclick="editEntity('${redirectTo}','${type}',${assetEntity?.id})" /> </span>
</tds:hasPermission>
<tds:hasPermission permission='AssetDelete'>
	<g:if test="${redirectTo!='dependencyConsole'}">
	   <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /> </span>
	</g:if>
	<g:else>
	   <span class="button"><input type="button" id="deleteId" name="deleteId" class="delete" value="Delete" onclick=" deleteAsset('${assetEntity?.id}','${forWhom}')" /> </span>
	</g:else>
</tds:hasPermission>
<tds:hasPermission permission="CommentCrudView">
	<a href="javascript:createIssue('${assetEntity.assetName}','comment', ${assetEntity.id}, 'update', '${assetEntity.assetType}');">
		<img src="${resource(dir:'i',file:'db_table_light.png')}" border="0px" style="margin-bottom: -4px;"/> &nbsp;&nbsp;Add Comment
	</a>
	<a href="javascript:createIssue('${assetEntity.assetName}','', ${assetEntity.id}, 'update', '${assetEntity.assetType}');">
		<img src="${resource(dir:'i',file:'db_table_light.png')}" border="0px" style="margin-bottom: -4px;"/> &nbsp;&nbsp;Add Task 
	</a>
</tds:hasPermission>
<g:if test="${assetEntity && assetEntity?.assetType in AssetType.getPhysicalServerTypes() && assetEntity?.model && assetEntity.isCableExist()}">
 	<span class="button"><input type="button" id="cableId" name="cableId" class="edit" value="Cable" onclick="openCablingDiv(${assetEntity?.id},'S')" /> </span>
</g:if>