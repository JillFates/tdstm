<%@page import="com.tds.asset.AssetType;"%>
<g:if test="${redirectTo!='dependencyConsole'}">
	 <span class="button"><input type="button" class="save updateDep" data-redirect='${redirectTo}' data-action='' value="Update" onclick="updateToShow($(this),'${whom}'); " /> </span>
	 <tds:hasPermission permission='AssetDelete'>
		 <span class="button"><g:actionSubmit class="delete" 
		 	onclick=" return confirm('You are about to delete selected asset for which there is no undo. Are you sure? Click OK to delete otherwise press Cancel');" value="Delete" /> </span>
	 </tds:hasPermission>
	 <span class="button"><input type="button" class="cancel" value="Cancel" onclick="$('#editEntityView').dialog('close');"/> </span>
</g:if>
<g:else>
	 <span class="button"><input type="button" class="save updateDep" data-action='' value="Update" onclick="updateToShow($(this),'${whom}')" /> </span>
	 <span class="button"><input type="button" id="deleteId" name="deleteId"  class="save" value="Delete" onclick=" deleteAsset('${value}','${whom}')" /> </span>
	 <span class="button"><input type="button" class="cancel" value="Cancel" onclick="$('#editEntityView').dialog('close');"/> </span>
</g:else>
<g:if test="${assetEntity && assetEntity?.assetType in AssetType.getPhysicalServerTypes() && assetEntity?.model && assetEntity.isCableExist()}">
 	<span class="button"><input type="button" id="cableId" name="cableId" class="edit" value="Cable" onclick="openCablingDiv('${value}','S')" /> </span>
</g:if>