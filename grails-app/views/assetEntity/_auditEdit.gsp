<g:form method="post"  name="editAssetsFormId" controller="assetEntity" action="update">
<div>
<input type="hidden" name="redirectTo" value="${redirectTo}"/>
<input type="hidden" name="id" value="${assetEntityInstance.id}"/>
<input name="dependentCount" id="dependentCount" type="hidden" value="0"/>
<input  name="supportCount"  id="supportCount" type="hidden" value="0"/>
<table>
	<tr><td colspan="2"><b>Asset Audit Edit</b></td></tr>
	<tr class="prop" >
		<td class="label">Location</td>
		<td class="label" nowrap="nowrap">
			<input type="text" name="sourceLocation" value="${assetEntityInstance.sourceLocation}" size="6" /> / 
			<input type="text" name="sourceRoom" value="${assetEntityInstance.sourceRoom}" size="6" />
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Name</td>
		<td class="label">
			<input type="text" name="assetName" value="${assetEntityInstance.assetName}"/>
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Manufacturer</td>
		<td class="label">
		 <div id="manufacturerId">
		   <g:select id="manufacturer" name="manufacturer.id" from="${manufacturers}" value="${assetEntityInstance.manufacturer?.id}" onChange="selectModel(this.value)" optionKey="id" optionValue="name" noSelection="${[null:'Unassigned']}" tabindex="13"/>
		 </div>
		</td>
	</tr>
	<tr class="prop trAnchor" onclick="showModelAudit(${assetEntityInstance.model?.id})">
		<td class="label"><b>Model</b></td>
		<td class="label">
		<div id="modelId">
			<g:select from="${models}" id="modelName" name="model.id" optionKey="id" optionValue="modelName"/>
		</div>
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Type</td>
		<td class="label">
			<input type="text" id="assetTypeId" name="assetType" value="${assetEntityInstance.assetType}">
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Rack</td>
		<td class="label" nowrap="nowrap">
			<input type="text" name="sourceRack" value="${assetEntityInstance.sourceRack}" size="6" > 
			Pos :<input type="text" name="sourceRackPosition" value="${assetEntityInstance.sourceRackPosition }" size="6" ">
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Tag</td>
		<td class="label">
			<input type="text" name="assetTag" value="${assetEntityInstance.assetTag}">
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Bundle</td>
		<td class="label">
			<g:select from="${moveBundleList}" id="room" name="moveBundle.id" optionKey="id" optionValue="name" value="${assetEntityInstance.moveBundle?.id}"/>
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Validation</td>
		<td class="label">
			
		</td>
	</tr>
	<tr class="prop">
		<td class="label">PlanStatus</td>
		<td class="label">
			<g:select id="planStatus" name ="planStatus" from="${planStatusOptions}" value= "${assetEntityInstance.planStatus}" noSelection="${['':' Please Select']}"/>
		</td>
	</tr>
</table>
</div>
<div class="buttons">
	<input type="button" class="edit" value="Update" onclick="updateAudit()" /> 
	<input type="button" class="edit" value="Delete" onclick="deleteAudit(${assetEntityInstance.id},'server')" /> 
	<input type="button" class="edit" value="More..." onclick="editEntity('room','Server', ${assetEntityInstance?.id})" /> 
</div><br>
<div id="modelAuditId" style="display: none"></div>
</g:form>