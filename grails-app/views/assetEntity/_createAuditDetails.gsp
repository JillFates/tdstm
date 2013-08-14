<script type="text/javascript">
$(document).ready(function() { 
	var assetType = $("#assetTypeCreateId").val()
	if(assetType =='Blade'){
		$(".bladeLabel").show()
		$(".rackLabel").hide()
		$(".vmLabel").hide()
	 } else {
		$(".bladeLabel").hide()
		$(".rackLabel").show()
		$(".vmLabel").hide()
	}
})
</script>
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
	<tr class="prop bladeLabel">
		<td class="label">Blade</td>
		<td class="label">
			<input type="text" id="BladeChassisId" ${source=='1' ? 'name="sourceBladeChassis"' : 'name="targetBladeChassis"'} />
		</td>
	</tr>
	<tr class="prop bladeLabel">
		<td class="label">Blade Position</td>
		<td class="label">
			<input type="text" id="bladePositionId" ${source=='1' ? 'name="sourceBladePosition" ' : 'name="targetBladePosition"'} />
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
		  <div id="manufacturerCreateId">
		   <g:select id="manufacturer" name="manufacturer.id" from="${manufacturers}" value="${manufacuterer?.id}" 
		   		onChange="selectModel(this.value,'Create')" optionKey="id" optionValue="name" noSelection="${[null:' Unassigned']}" tabindex="13" />
		 </div>
		</td>
	</tr>
	<tr class="prop trAnchor" >
		<td class="label"><b>Model</b></td>
		<td class="label">
			<div id="modelCreateId">
			  		<g:render template="modelView"  model="[clazz:config.model, models:models, assetEntity:assetEntityInstance, forWhom:'Create']" />
			 </div>
		</td>
		</tr>
	<tr class="prop">
		<td class="label">Type</td>
		<td class="label">
			<g:select from="${assetTypeOptions}" id="assetTypeCreateId" name="assetType" value="${assetEntityInstance.assetType}" 
			onChange="selectManufacturer(this.value, 'Create')" tabindex="12" />
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Serial Number</td>
		<td class="label">
			<input type="text" id="serialNumber" name="serialNumber" />
		</td>
	</tr>
	<tr class="prop rackLabel">
		<td class="label">Rack</td>
		<td class="label" nowrap="nowrap">
			<input  type="text" ${source=='1' ? 'id="sourceRack" name="sourceRack" ' : 'id="targetRack" name="targetRack"'} size="6" > 
			Pos :<input type="text" ${source=='1' ? 'id="sourceRackPosition" name="sourceRackPosition" ' : 'id="targetRackPosition" name="targetRackPosition" '} size="6">
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
			<g:select from="${moveBundleList}" id="moveBundleId" name="moveBundle.id" optionKey="id" optionValue="name" />
		</td>
	</tr>
	<tr class="prop">
		<td class="label">PlanStatus</td>
		<td class="label">
			<g:select id="planStatus" name ="planStatus" from="${planStatusOptions}"  noSelection="${['':' Please Select']}"/>
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Validation</td>
		<td class="label">
			<g:select from="${assetEntityInstance.constraints.validation.inList}" id="validation" name="validation" value="Discovery"
			noSelection="${['':' Please Select']}" />
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
