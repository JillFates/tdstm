<%@page import="com.tds.asset.AssetType;"%>
<g:if test="${redirectTo!='dependencyConsole'}">
	 <g:if test="${redirectTo=='listTask'}">
	 	<span class="button"><input id="updateCloseId" type="button" class="save updateDep" value="Update/Close" onclick="updateToRefresh()" /></span>
	 </g:if>
	 <g:elseif test="${redirectTo && !(redirectTo in ['assetEntity','application','database','files','myIssues'])}">
	 	<span class="button"><g:actionSubmit id="updateCloseId" class="save updateDep" value="Update/Close" action="Update" /></span>
	 </g:elseif>
	 <g:else>
	 	<span class="button"><input type="button"  id="updateCloseId" class="save updateDep" data-action='close' value="Update/Close" onclick="updateToShow($(this),'${whom}'); " /> </span>
	 </g:else>
	 <span class="button"><input type="button" class="save updateDep" data-redirect='${redirectTo}' data-action='' value="Update/View" onclick="updateToShow($(this),'${whom}'); " /> </span>
	 <tds:hasPermission permission='AssetDelete'>
		 <span class="button"><g:actionSubmit class="delete" 
		 	onclick=" return confirm('You are about to delete selected asset for which there is no undo. Are you sure? Click OK to delete otherwise press Cancel');" value="Delete" /> </span>
	 </tds:hasPermission>
	 <span class="button"><input type="button" class="delete" value="Cancel" onclick="$('#editEntityView').dialog('close');"/> </span>
</g:if>
<g:else>
	 <span class="button"><input id="updatedId" name="updatedId" type="button" class="save updateDep" value="Update/Close" onclick="submitRemoteForm()"> </span>
	 <span class="button"><input type="button" class="save updateDep" data-action='' value="Update/View" onclick="updateToShow($(this),'${whom}')" /> </span>
	 <span class="button"><input type="button" id="deleteId" name="deleteId"  class="save" value="Delete" onclick=" deleteAsset('${value}','${whom}')" /> </span>
	 <span class="button"><input type="button" class="delete" value="Cancel" onclick="$('#editEntityView').dialog('close');"/> </span>
</g:else>
<g:if test="${assetEntity && assetEntity?.assetType in AssetType.getPhysicalServerTypes() && assetEntity?.model && assetEntity.isCableExist()}">
 	<span class="button"><input type="button" id="cableId" name="cableId" class="edit" value="Cable" onclick="openCablingDiv('${value}','S')" /> </span>
</g:if>