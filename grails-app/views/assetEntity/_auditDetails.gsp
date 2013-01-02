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
	<input type="button" class="edit" value="Edit" onclick="editAudit('${redirectTo}','Server', ${assetEntity?.id})" /> 
	<input type="button" class="edit" value="Delete" onclick="deleteAudit(${assetEntity.id},'server')" /> 
	<input type="button" class="edit" value="More..." onclick="getEntityDetails('room','Server', ${assetEntity?.id})" /> 
</div>
<br>
<div >
 <div id="modelAuditId" style="display: none"></div>
</div>