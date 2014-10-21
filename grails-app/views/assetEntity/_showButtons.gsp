<%@page import="com.tds.asset.AssetType;"%>
<tds:hasPermission permission='AssetEdit'>
	<span class="button"><input type="button" class="edit" value="Edit" onclick="EntityCrud.showAssetEditView('${assetEntity.assetClass}', ${assetEntity?.id});" /> </span>
</tds:hasPermission>
<tds:hasPermission permission='AssetDelete'>
   <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /> </span>
</tds:hasPermission>
<tds:hasPermission permission="CommentCrudView">
	<a href="javascript:createIssue('${escapedName}','', ${assetEntity.id}, 'update', '${assetEntity.assetType}');">
		<img src="${resource(dir:'icons',file:'table_add.png')}" border="0px" style="margin-bottom: -4px;"/> &nbsp;&nbsp;Add Task 
	</a>
	<a href="javascript:createIssue('${escapedName}','comment', ${assetEntity.id}, 'update', '${assetEntity.assetType}');">
		<img src="${resource(dir:'icons',file:'comment_add.png')}" border="0px" style="margin-bottom: -4px;"/> &nbsp;&nbsp;Add Comment
	</a>	
</tds:hasPermission>

<%-- TODO : JPM 10/2014 : Refactor logic for cable button test to be in domain --%>
<g:if test="${assetEntity && assetEntity?.assetType in AssetType.getPhysicalServerTypes() && assetEntity?.model && assetEntity.isCableExist()}">
 	<span class="button"><input type="button" id="cableId" name="cableId" class="cableedit" value="Cable" onclick="openCablingDiv(${assetEntity?.id},'S')" /> </span>
</g:if>

