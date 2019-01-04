<%@page import="com.tds.asset.AssetType;"%>
<%@page import="net.transitionmanager.security.Permission"%>

<g:if test="${redirectTo!='dependencyConsole'}">
	 <span class="button">
		<input type="button" class="save updateDep" data-action='' value="Update" id="assetUpdateButton">
		<input type="submit" id="assetUpdateSubmit" style="display:none;">
	 </span>
	 <tds:hasPermission permission="${Permission.AssetDelete}">
		 <span class="button"><g:actionSubmit class="delete" 
		 	onclick=" return confirm('You are about to delete selected asset for which there is no undo. Are you sure? Click OK to delete otherwise press Cancel');" value="Delete" /> </span>
	 </tds:hasPermission>
	 <span class="button"><input type="button" class="cancel" value="Cancel" onclick="$('#editEntityView').dialog('close');"/> </span>
</g:if>
<g:else>
	 <span class="button">
		<input type="button" class="save updateDep" data-action='' value="Update" id="assetUpdateButton">
	 </span>
	 <span class="button"><input type="button" id="deleteId" name="deleteId"  class="save" value="Delete" onclick=" deleteAsset('${asset.id}','${asset.assetClass}')" /> </span>
	 <span class="button"><input type="button" class="cancel" value="Cancel" onclick="$('#editEntityView').dialog('close');"/> </span>
</g:else>

<%-- TODO : JPM 08/2014 - isCableExist does NOT seem to make sense and it should be ALL physical servers, network, etc - need to move to domain asset.canCable() --%>
<g:if test="${asset && asset.model?.assetType && asset.model.assetType in AssetType.getPhysicalServerTypes() && asset.isCableExist()}">
 	<span class="button"><input type="button" id="cableId" name="cableId" class="edit" value="Cable" onclick="openCablingDiv('${asset.id}','S')" /> </span>
</g:if>


<script>

<%--
  -- Initialize the update button so that it can only be clicked once. Note that for some reason that the JQuery one method is not working as expected 
  -- so the performAssetUpdate method has some extra logic in it with a singleton flag controlling access
  --%>
$('#assetUpdateButton').on('click', function() {
	return EntityCrud.performAssetUpdate($(this),'${asset.assetClass}');
});

</script>
