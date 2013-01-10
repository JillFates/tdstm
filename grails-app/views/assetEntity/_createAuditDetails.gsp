<g:form method="post"  name="editAssetsAuditFormId" controller="assetEntity" action="save">
<div>
<input type="hidden" name="redirectTo" value="${redirectTo}"/>
<input name="attributeSet.id" type="hidden" value="1">
<input name="project.id" type="hidden" value="${projectId}" />
<input name="dependentCount" id="dependentCount" type="hidden" value="0"/>
<input  name="supportCount"  id="supportCount" type="hidden" value="0"/>
<input  name="source"  id="sourceId" type="hidden" value="1"/>
<table>
	<tr><td colspan="2"><b>Asset Audit Create</b></td></tr>
	<tr class="prop" >
		<td class="label">Location</td>
		<td class="label" nowrap="nowrap">
			<input type="text" id="auditLocationId" name="sourceLocation"  size="6" /> / 
			<input type="text" id="auditRoomId" name="sourceRoom"  size="6" />
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Name</td>
		<td class="label">
			<input type="text" name="assetName" />
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Manufacturer</td>
		<td class="label">
		 <div id="manufacturerId">
		   <input type="text" id="manufacturersAuditId" name="manufacturers" value="" onkeyup="getAlikeManu(this.value)" />
		 </div>
		 <div id="autofillId" class="autoFillDiv" style="display: none " ></div>
		</td>
	</tr>
	<tr class="prop trAnchor" >
		<td class="label"><b>Model</b></td>
		<td class="label">
		<div id="modelId">
		<input type="text" id="modelsAuditId" name="models" value="" onkeyup="getAlikeModel(this.value)" />
		</div>
		 <div id="autofillIdModel" class="autoFillDiv"  style="display: none"></div>
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Type</td>
		<td class="label">
			<input type="text" id="assetTypeId" name="assetType" readonly="readonly">
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Serial Number</td>
		<td class="label">
			<input type="text" id="serialNumber" name="serialNumber" />
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Rack</td>
		<td class="label" nowrap="nowrap">
			<input type="text" name="sourceRack"  id="auditRackId" size="6" > 
			Pos :<input type="text" id="auditPosId" name="sourceRackPosition" size="6" >
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Tag</td>
		<td class="label">
			<input type="text" name="assetTag">
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Bundle</td>
		<td class="label">
			<g:select from="${moveBundleList}" id="room" name="moveBundle.id" optionKey="id" optionValue="name" />
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Validation</td>
		<td class="label">
			<g:select from="${assetEntityInstance.constraints.validation.inList}" id="validation" name="validation" value="Discovery"
			noSelection="${['':' Please Select']}" />
		</td>
	</tr>
	<tr class="prop">
		<td class="label">PlanStatus</td>
		<td class="label">
			<g:select id="planStatus" name ="planStatus" from="${planStatusOptions}"  noSelection="${['':' Please Select']}"/>
		</td>
	</tr>
</table>
</div>
<div class="buttons">
	<input type="button" class="edit" value="save" onclick="updateAudit()" /> 
</div>
<br/>
</g:form>
<div id="modelAuditId" style="display: none"></div>
