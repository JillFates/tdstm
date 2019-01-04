<table>
 <tr><td colspan="2"><b>Asset Audit Details</b></td></tr>
 	<g:if test="${assetType != 'Blade'}" >
	<tr class="prop">
		<td class="label">Location</td>
		<td class="label">
			${source=='1'? ''+assetEntity.sourceLocationName+' / '+assetEntity.sourceRoomName+'' : ''+assetEntity.targetLocationName+' / '+assetEntity.targetRoomName +''}
		</td>
	</tr>
	</g:if>
	<g:if test="${assetType == 'Blade'}" >
	<tr class="prop">
		<td class="label">Blade</td>
		<td class="label">
			${source=='1'? assetEntity.sourceChassis : assetEntity.targetChassis}
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Blade Position</td>
		<td class="label">
			${source=='1'? assetEntity.sourceBladePosition  : assetEntity.targetBladePosition}
		</td>
	</tr>
	</g:if>
	<tr class="prop">
		<td class="label">Name</td>
		<td class="label">
			${assetEntity.assetName}
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Manufacturer</td>
		<td class="label">
			${assetEntity.manufacturer}
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Model</td>
		<td class="label">
			${assetEntity.model}
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Type</td>
		<td class="label">
			${assetEntity.assetType}
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Serial Number</td>
		<td class="label">
			${assetEntity.serialNumber}
		</td>
	</tr>
	<g:if test="${assetType != 'Blade'}" >
	<tr class="prop">
		<td class="label">Rack</td>
		<td class="label">
			${source=='1'? ''+assetEntity.sourceRackName+' / '+assetEntity.sourceRackPosition+'' : ''+assetEntity.targetRackName+' / '+assetEntity.targetRackPosition+''}
		</td>
	</tr>
	</g:if>
	<tr class="prop">
		<td class="label">Tag</td>
		<td class="label">
			${assetEntity.assetTag}
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Bundle</td>
		<td class="label">
			${assetEntity.moveBundle}
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Validation</td>
		<td class="label">
			${assetEntity.validation}
		</td>
	</tr>
	<tr class="prop">
		<td class="label">PlanStatus</td>
		<td class="label">
			${assetEntity.planStatus}		
		</td>
	</tr>
</table>
<div class="buttons">
	<input type="button" class="edit" value="Edit" onclick="editAudit('roomAudit','${source}','${assetType}', ${assetEntity?.id})" /> 
	<input type="button" class="edit" value="Delete" onclick="deleteAudit(${assetEntity.id},'server')" /> 
	<input type="button" class="edit" value="More..." onclick="EntityCrud.showAssetDetailsView('${assetEntity?.assetClass}', ${assetEntity?.id})" /> 
</div>
<br>
<div >
 <div id="modelAuditId" >
 <div>
 <input type="hidden" id="manufacturersAuditId" value="${assetEntity.model?.manufacturer}">
 <table> 
 <tr><td colspan="2"><b>Model Details View</b></td></tr>
	<tr class="prop">
		<td class="label">Name</td>
		<td class="label">
			${assetEntity.model?.modelName}
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Size</td>
		<td class="label">
			${assetEntity.model?.usize}
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Manufacturer</td>
		<td class="label">
			${assetEntity.model?.manufacturer}
		</td>
	</tr>
	</table>
	<div class="buttons noWrapDiv">
		<input type="button" class="edit" value="Edit" onclick="editModelAudit('${assetEntity.model?.modelName}')" /> 
		<g:form action="edit" controller="model" target="new">
			<input name="id" type="hidden" id="show_modelId" value="${assetEntity.model?.id}"/>
			<span class="button">
				<input type="submit" class="edit" value="More..."></input>
			</span>
		</g:form>
	</div>
 </div>
 </div>
</div>
