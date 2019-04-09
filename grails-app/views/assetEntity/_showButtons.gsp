<%@page import="net.transitionmanager.asset.AssetType; com.tds.asset.AssetType;"%>
<%@page import="net.transitionmanager.security.Permission"%>

<tds:hasPermission permission="${Permission.AssetEdit}">
    <button class="btn btn-default" type="button" onclick="EntityCrud.showAssetEditView('${assetEntity.assetClass}', ${assetEntity?.id});">
        <asset:image src="icons/database_edit.png" border="0px"/> Edit
    </button>
</tds:hasPermission>

<tds:hasPermission permission="${Permission.AssetDelete}">
	<g:if test="${deleteChassisWarning}">
        <button class="btn btn-default" type="submit" onclick="return confirm('This chassis has blades assigned to it. Deleting the chassis will clear the reference for all associated blades. Click OK to continue otherwise click Cancel.');">
            <asset:image src="icons/database_delete.png" border="0px"/> Delete
        </button>
	</g:if>
	<g:else>
        <button type="submit" class="btn btn-default" name="_action_Delete" value="Delete" onclick=" return confirm('You are about to delete selected asset for which there is no undo. Are you sure? Click OK to delete otherwise press Cancel');">
            <asset:image src="icons/database_delete.png" border="0px"/> Delete
        </button>
	</g:else>

</tds:hasPermission>
<tds:hasPermission permission="${Permission.AssetCreate}">
    <button type="button" class="btn btn-default" name="_action_clone" value="Clone" onclick="EntityCrud.cloneAssetView('${assetEntity.assetClass}', '${escapedName}', ${assetEntity?.id});">
        <asset:image src="icons/database_copy.png" border="0px"/> Clone
    </button>
</tds:hasPermission>

<tds:hasPermission permission="${net.transitionmanager.security.Permission.TaskCreate}">
<button class="btn btn-default" type="button" onclick="createIssue('${escapedName}','', ${assetEntity.id}, 'update', '${assetEntity.assetType}');">
    <asset:image src="icons/table_add.png" border="0px"/> Add Task
</button>
</tds:hasPermission>

<tds:hasPermission permission="${net.transitionmanager.security.Permission.CommentCreate}">
    <button class="btn btn-default" type="button" onclick="createIssue('${escapedName}','comment', ${assetEntity.id}, 'update', '${assetEntity.assetType}');">
        <asset:image src="icons/comment_add.png" border="0px"/> Add Comment
    </button>
</tds:hasPermission>

<%-- TODO : JPM 10/2014 : Refactor logic for cable button test to be in domain --%>
<g:if test="${assetEntity && assetEntity?.assetType in net.transitionmanager.asset.AssetType.physicalServerTypes && assetEntity?.model && assetEntity.isCableExist()}">
    <button class="btn btn-default" type="button" onclick="openCablingDiv(${assetEntity?.id},'S')">
        <asset:image src="icons/disconnect.png" border="0px"/> Cable
    </button>
</g:if>

<tds:hasPermission permission="${Permission.ArchitectureView}">

<g:link controller="assetEntity" action="architectureViewer" params="[assetId:assetEntity?.id, level:2]" class="btn btn-default" role="button">
    <input type="button" class="architectureGraph" value="Arch Graph" />
</g:link>

</tds:hasPermission>
