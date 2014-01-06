<script type="text/javascript">
$(document).ready(function() { 
	var assetType = $("#assetTypeEditId").val()
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
<g:form method="post"  name="editAssetsAuditFormId" controller="assetEntity" action="update">
<div>
<input type="hidden" name="redirectTo" value="${redirectTo}"/>
<input type="hidden" name="id" value="${assetEntityInstance.id}"/>
<input name="dependentCount" id="dependentCount" type="hidden" value="0"/>
<input  name="supportCount"  id="supportCount" type="hidden" value="0"/>
<input  name="source"  id="sourceId" type="hidden" value="${source ?: 1}"/>
<table>
	<tr><td colspan="2"><b>Asset Audit Edit</b></td></tr>
	<tr class="prop rackLabel" >
		<td class="label ">Location</td>
		<td class="label" nowrap="nowrap">
			<input type="text" ${source=='1' ? 'name="sourceLocation" value="'+assetEntityInstance.sourceLocation+'"' : 'name="targetLocation" value="'+assetEntityInstance.targetLocation+'"'} size="6" /> / 
			<input type="text" ${source=='1' ? 'name="sourceRoom" value="'+assetEntityInstance.sourceRoom+'"' : 'name="targetRoom" value="'+assetEntityInstance.targetRoom+'"'} size="6" />
		</td>
	</tr>
	<tr class="prop bladeLabel">
		<td class="label">Blade</td>
		<td class="label">
		<g:if test="${source=='1'}">
		<g:select id='sourceBladeChassis' from='${sourceChassisSelect}' optionKey='${-2}' optionValue='${1}'
			  name="sourceBladeChassis" value="${assetEntityInstance.sourceBladeChassis }" noSelection="${['':' Please Select']}"/>
		</g:if>
		<g:else>
		<g:select id='targetBladeChassis' from='${targetChassisSelect}' optionKey='${-2}' optionValue='${1}'
				name="targetBladeChassis"  value="${assetEntityInstance.targetBladeChassis}" noSelection="${['':' Please Select']}"/>
		</g:else>
		</td>
	</tr>
	<tr class="prop bladeLabel">
		<td class="label">Blade Position</td>
		<td class="label">
			<input type="text" ${source=='1' ? 'name="sourceBladePosition" value="'+(assetEntityInstance.sourceBladePosition ?:'') +'"' : 'name="targetBladePosition" value="'+(assetEntityInstance.targetBladePosition ?:'')+'"'} />
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
		 <div id="manufacturerEditId">
		   <g:select id="manufacturer" name="manufacturer.id" from="${manufacturers}" value="${assetEntityInstance.manufacturer?.id}" 
		   	onChange="selectModel(this.value,'Edit')" 
		   optionKey="id" optionValue="name" noSelection="${[null:'Unassigned']}" tabindex="13"/>
		 </div>
		</td>
	</tr>
	<tr class="prop" >
		<td class="label">Model</td>
		<td class="label">
		<div id="modelEditId">
			<g:render template="modelView"  model="[clazz:config.model, models:models, assetEntity:assetEntityInstance, forWhom:'Edit']" />
		</div>
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Type</td>
		<td class="label">
			<g:select from="${assetTypeOptions}" id="assetTypeEditId" name="assetType" value="${assetEntityInstance.assetType}" 
			onChange="selectManufacturer(this.value, 'Edit')" tabindex="12" />
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Serial Number</td>
		<td class="label">
			<input type="text" id="serialNumber" name="serialNumber" value="${assetEntityInstance.serialNumber}">
		</td>
	</tr>
	<tr class="prop rackLabel">
		<td class="label">Rack</td>
		<td class="label" nowrap="nowrap">
			<input type="text" ${source=='1' ? 'name="sourceRack" value="'+assetEntityInstance.sourceRack+'"' : 'name="targetRack" value="'+assetEntityInstance.targetRack+'"'} size="6" > 
			Pos :<input type="text" ${source=='1' ? 'name="sourceRackPosition" value="'+(assetEntityInstance.sourceRackPosition ?:'')+'"' : 'name="targetRackPosition" value="'+(assetEntityInstance.targetRackPosition ?:'')+'"'} size="6">
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
		<td class="label">PlanStatus</td>
		<td class="label">
			<g:select id="planStatus" name ="planStatus" from="${planStatusOptions}" value= "${assetEntityInstance.planStatus}" noSelection="${['':' Please Select']}"/>
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Validation</td>
		<td><g:select from="${assetEntityInstance.constraints.validation.inList}" id="validation" name="validation" noSelection="${['':' Please Select']}" 
					 value="${assetEntityInstance.validation}"/>	
		</td>
	</tr>
</table>
</div>
<div class="buttons">
	<input type="button" class="edit" value="Update" onclick="updateAudit()" /> 
	<input type="button" class="edit" value="Delete" onclick="deleteAudit(${assetEntityInstance.id},'server')" /> 
	<input type="button" class="edit" value="More..." onclick="editEntity('room','Server', ${assetEntityInstance?.id})" /> 
</div><br>
</g:form>
<div id="modelAuditId">
<g:form name="modelAuditEdit" controller="model" action="updateModel" method="post" >
	<div>
	<input type="hidden" name="id" value="${assetEntityInstance.model?.id}"> 
	<input type="hidden" name="redirectTo" value="assetAudit"> 
		<table>
			<tr><td colspan="2"><b>Model Audit Edit</b></td></tr>
			<tr>
				<td>Model Name:</td>
				<td>${assetEntityInstance.model?.modelName}</td>
			</tr>

			<tr>
				<td>Usize:</td>
				<td><g:select id="usizeId" name="usize"
						from="${assetEntityInstance.model?.constraints?.usize?.inList ?: (1..42)}"
						value="${assetEntityInstance.model?.usize}"></g:select></td>
			</tr>
			<tr>
				<td>Manufacturer</td>
				<td>
					${assetEntityInstance.model?.manufacturer}
				</td>
			</tr>
		</table>
	</div>
	</g:form>
	<div class="buttons">
		<input type="button" class="edit" value="Update" onclick="updateModelAudit()" /> 
		<g:form action="edit" controller="model" target="new">
			<input name="id" type="hidden" id="show_modelId" value="${assetEntityInstance.model?.id}"/>
			<span class="button">
				<input type="submit" class="edit" value="More..."></input>
			</span>
		</g:form>
	</div>
</div>
