<%@page import="com.tds.asset.AssetType;"%>

<g:if test="${redirectTo!='dependencyConsole'}">
	 <span class="button">
	 	<input type="button" class="save updateDep" data-action='' value="Update" 
	 		onclick="EntityCrud.performAssetUpdate($(this),'${assetEntity.assetClass}'); " /> 
	 </span>
	 <tds:hasPermission permission='AssetDelete'>
		 <span class="button"><g:actionSubmit class="delete" 
		 	onclick=" return confirm('You are about to delete selected asset for which there is no undo. Are you sure? Click OK to delete otherwise press Cancel');" value="Delete" /> </span>
	 </tds:hasPermission>
	 <span class="button"><input type="button" class="cancel" value="Cancel" onclick="$('#editEntityView').dialog('close');"/> </span>
</g:if>
<g:else>
	 <span class="button">
	 	<input type="button" class="save updateDep" data-action='' value="Update" onclick="EntityCrud.performAssetUpdate($(this),'${assetEntity.assetClass}')" />
	 </span>
	 <span class="button"><input type="button" id="deleteId" name="deleteId"  class="save" value="Delete" onclick=" deleteAsset('${assetEntity.id}','${assetEntity.assetClass}')" /> </span>
	 <span class="button"><input type="button" class="cancel" value="Cancel" onclick="$('#editEntityView').dialog('close');"/> </span>
</g:else>

<%-- TODO : JPM 08/2014 - isCableExist does NOT seem to make sense and it should be ALL physical servers, network, etc - need to move to domain AssetEntity.canCable() --%>
<g:if test="${assetEntity && assetEntity.model?.assetType && assetEntity.model.assetType in AssetType.getPhysicalServerTypes() && assetEntity.isCableExist()}">
 	<span class="button"><input type="button" id="cableId" name="cableId" class="edit" value="Cable" onclick="openCablingDiv('${assetEntity.id}','S')" /> </span>
</g:if>