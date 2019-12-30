<%@page import="net.transitionmanager.asset.AssetType; net.transitionmanager.asset.AssetType;"%>
<%@page import="net.transitionmanager.security.Permission"%>

<div class="modal-sidenav form-group-center">
	<nav class="modal-sidenav btn-link">
		<g:if test="${redirectTo!='dependencyConsole'}">
			<button type="button" class="btn btn-icon selected-button">
				<i class="fas fa-edit"></i>
			</button>
			
			<button type="button" class="btn btn-icon" type="button" id="assetUpdateButton" value="Update">
				<i class="far fa-save"></i>
			</button>

			<tds:hasPermission permission="${Permission.AssetDelete}">
				<button type="button" class="btn btn-icon" type="submit" onclick="return confirm('This chassis has blades assigned to it. Deleting the chassis will clear the reference for all associated blades. Click OK to continue otherwise click Cancel.');" value="Delete">
					<i class="far fa-trash-alt"></i>
				</button>
			</tds:hasPermission>
			
			<button type="button" class="btn btn-icon" type="button" value="Cancel" onclick="$('#editEntityView').dialog('close');">
				<i class="fas fa-ban"></i>
			</button>
		</g:if>
		<g:else>
			<button type="button" class="btn btn-icon selected-button">
				<i class="fas fa-edit"></i>
			</button>

			<button type="button" class="btn btn-icon" type="button" id="assetUpdateButton" value="Update">
				<i class="far fa-save"></i>
			</button>
			<button type="button" class="btn btn-icon" type="button" id="deleteId" onclick="deleteAsset('${assetEntity.id}','${assetEntity.assetClass}')" value="Delete">
				<i class="far fa-trash-alt"></i>
			</button>
			<button type="button" class="btn btn-icon" type="button" value="Cancel" onclick="$('#editEntityView').dialog('close');">
				<i class="fas fa-ban"></i>
			</button>
		</g:else>
	</nav>
</div>

<%-- TODO : JPM 08/2014 - isCableExist does NOT seem to make sense and it should be ALL physical servers, network, etc - need to move to domain AssetEntity.canCable() --%>
<%-- <g:if test="${assetEntity && assetEntity.model?.assetType && assetEntity.model.assetType in net.transitionmanager.asset.AssetType.getPhysicalServerTypes() && assetEntity.isCableExist()}">
 	<span class="button"><input type="button" id="cableId" name="cableId" class="edit" value="Cable" onclick="openCablingDiv('${assetEntity.id}','S')" /> </span>
</g:if> --%>


<script>
	<%--
	-- Initialize the update button so that it can only be clicked once. Note that for some reason that the JQuery one method is not working as expected 
	-- so the performAssetUpdate method has some extra logic in it with a singleton flag controlling access
	--%>
	$('#assetUpdateButton').on('click', function() {
		return EntityCrud.performAssetUpdate($(this),'${assetEntity.assetClass}');
	});
</script>
