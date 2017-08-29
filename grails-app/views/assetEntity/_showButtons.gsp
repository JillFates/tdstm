<%@page import="com.tds.asset.AssetType;"%>
<%@page import="net.transitionmanager.security.Permission"%>

<tds:hasPermission permission="${Permission.AssetEdit}">
    <button class="btn btn-default" type="button" onclick="EntityCrud.showAssetEditView('${assetEntity.assetClass}', ${assetEntity?.id});">
        <img src="${resource(dir:'icons',file:'database_edit.png')}" border="0px"/> Edit
    </button>
</tds:hasPermission>

<tds:hasPermission permission="${Permission.AssetDelete}">
	<g:if test="${deleteChassisWarning}">
        <button class="btn btn-default" type="submit" onclick="return confirm('This chassis has blades assigned to it. Deleting the chassis will clear the reference for all associated blades. Click OK to continue otherwise click Cancel.');">
            <img src="${resource(dir:'icons',file:'database_delete.png')}" border="0px"/> Delete
        </button>
	</g:if>
	<g:else>
        <button type="submit" class="btn btn-default" name="_action_Delete" value="Delete" onclick=" return confirm('You are about to delete selected asset for which there is no undo. Are you sure? Click OK to delete otherwise press Cancel');">
            <img src="${resource(dir:'icons',file:'database_delete.png')}" border="0px"/> Delete
        </button>
	</g:else>

</tds:hasPermission>
<tds:hasPermission permission="${Permission.AssetCreate}">
    <button type="button" class="btn btn-default" name="_action_clone" value="Clone" onclick="EntityCrud.cloneAssetView('${assetEntity.assetClass}', '${escapedName}', ${assetEntity?.id});">
        <img src="${resource(dir:'icons',file:'database_copy.png')}" border="0px"/> Clone
    </button>
</tds:hasPermission>

<tds:hasPermission permission="${net.transitionmanager.security.Permission.TaskCreate}">
<button class="btn btn-default" type="button" onclick="createIssue('${escapedName}','', ${assetEntity.id}, 'update', '${assetEntity.assetType}');">
    <img src="${resource(dir:'icons',file:'table_add.png')}" border="0px"/> Add Task
</button>
</tds:hasPermission>

<tds:hasPermission permission="AssetEdit">
    <button class="btn btn-default" type="button" onclick="createIssue('${escapedName}','comment', ${assetEntity.id}, 'update', '${assetEntity.assetType}');">
        <img src="${resource(dir:'icons',file:'comment_add.png')}" border="0px"/> Add Comment
    </button>
</tds:hasPermission>

<%-- TODO : JPM 10/2014 : Refactor logic for cable button test to be in domain --%>
<g:if test="${assetEntity && assetEntity?.assetType in AssetType.physicalServerTypes && assetEntity?.model && assetEntity.isCableExist()}">
    <button class="btn btn-default" type="button" onclick="openCablingDiv(${assetEntity?.id},'S')">
        <img src="${resource(dir:'icons',file:'disconnect.png')}" border="0px"/> Cable
    </button>
</g:if>

<tds:hasPermission permission="${Permission.ArchitectureView}">

<g:link controller="assetEntity" action="architectureViewer" params="[assetId:assetEntity?.id, level:2]" class="btn btn-default" role="button">
    <input type="button" class="architectureGraph" value="Arch Graph" />
</g:link>

</tds:hasPermission>
