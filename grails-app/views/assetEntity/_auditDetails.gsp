<table>
 <tr><td colspan="2"><b>Asset Audit Details</b></td></tr>
	<tr class="prop">
		<td class="label">Location</td>
		<td class="label">
			${assetEntity.sourceLocation} / ${assetEntity.sourceRoom}
		</td>
	</tr>
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
	<tr class="prop trAnchor" onclick="showModelAudit(${assetEntity.model?.id})">
		<td class="label"><a><b>Model</b></a></td>
		<td class="label">
			<a>${assetEntity.model}</a>
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
	<tr class="prop">
		<td class="label">Rack</td>
		<td class="label">
			${assetEntity.sourceRack} Pos : ${assetEntity.sourceRackPosition }
		</td>
	</tr>
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
	<input type="button" class="edit" value="Edit" onclick="editAudit('roomAudit','Server', ${assetEntity?.id})" /> 
	<input type="button" class="edit" value="Delete" onclick="deleteAudit(${assetEntity.id},'server')" /> 
	<input type="button" class="edit" value="More..." onclick="getEntityDetails('room','Server', ${assetEntity?.id})" /> 
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