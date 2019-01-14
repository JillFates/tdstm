<%@ page import="com.tdsops.tm.enums.domain.ValidationType" %>
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
<%-- Holds original values of the various SELECTS --%>
<input type="hidden" id="hiddenModel"        name="modelId" value="">
<input type="hidden" id="hiddenManufacturer" name="manufacturerId" value="${manufacturer?.id}">
<input type="hidden" id="deviceChassisIdS" value=""/>
<input type="hidden" id="deviceChassisIdT" value=""/>
<input type="hidden" id="deviceRackIdS" value=""/>
<input type="hidden" id="deviceRackIdT" value=""/>
<input type="hidden" id="deviceRoomIdS" value=""/>
<input type="hidden" id="deviceRoomIdT" value=""/>

<%-- Used to maintain the selected AssetType --%>
<input type="hidden" id="currentAssetType" 		name="currentAssetType" value="${currentAssetType}"/>

<%-- Key field and optimistic locking var --%>
<input type="hidden" id="assetId" name="id" value=""/>
<input type="hidden" id="version" name="version" value="${version}"/>

<input type="hidden" id="roomSelectS" name="roomSourceId" value="0"/>
<input type="hidden" id="roomSelectT" name="roomTargetId" value="0"/>

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
			<input readonly="true" type="text" id="auditLocationName" name="locationName"  size="8" />
			<input readonly="true" type="text" id="auditRoomName" name="roomName"  size="8" />
		</td>
	</tr>
	<tr class="prop bladeLabel">
		<td class="label">Blade</td>
		<td class="label">
			<input type="text" id="BladeChassisId" ${source=='1' ? 'name="sourceChassis"' : 'name="targetChassis"'} />
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
		<td class="label">Device Type</td>
		<td class="label">
			<div id="assetTypeSelect" tabindex="103">
			</div>		
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Manufacturer</td>
		<td class="label">
			<div id="manufacturerSelect" tabindex="102">
			</div>
		</td>
	</tr>
	<tr class="prop" >
		<td class="label">Model</td>
		<td class="label">
			<div id="modelSelect" tabindex="104"></div>
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Serial Number</td>
		<td class="label">
			<input type="text" id="serialNumber" name="serialNumber" />
		</td>
	</tr>
	<g:if test="${source=='1'}">
		<tr class="prop rackLabel">
			<td class="label">Rack</td>
			<td class="label" nowrap="nowrap">
				<g:render template="deviceRackSelect" model="[clazz:config.sourceRack, options:sourceRackSelect, rackId:assetEntityInstance.rackSource?.id, 
					rackDomId:'rackSourceId', rackDomName:'rackSourceId', sourceTarget:'S', forWhom:'Edit', tabindex:'310']" />
				<input type="hidden" id="rackTargetId" name="rackTargetId" value="0"/>
			</td>
		</tr>
		<tr class="prop rackLabel">
			<td class="label">Pos</td>
			<td class="label" nowrap="nowrap">
				<input type="text" id="sourceRackPositionId" name="sourceRackPosition" 
					value="${assetEntityInstance.sourceRackPosition}" 
					placeholder="U position"
					class="${config.sourceRackPosition} useRackS"
					size=10 tabindex="320" 
				/>
				<input type="hidden" id="targetRackPositionId" name="targetRackPosition" value="${assetEntityInstance.targetRackPosition}"/>
			</td>
		</tr>
	</g:if>
	<g:else>
		<tr class="prop rackLabel">
			<td class="label">Rack Target</td>
			<td class="label" nowrap="nowrap">
				<g:render template="deviceRackSelect"  model="[clazz:config.targetRack, options:targetRackSelect, rackId: assetEntityInstance.rackTarget?.id,
												rackDomId:'rackTargetId', rackDomName:'rackTargetId', sourceTarget:'T', forWhom:'Edit', tabindex:'340']" />
				<input type="hidden" id="rackSourceId" name="rackSourceId" value="0"/>
			</td>
		</tr>
		<tr class="prop rackLabel">
			<td class="label">Pos</td>
			<td class="label" nowrap="nowrap">
				<input type="text" id="targetRackPositionId" name="targetRackPosition"
					value="${assetEntityInstance.targetRackPosition}" 
					placeholder="U position"
					class="${config.targetRackPosition} useRackT"
					size=10 tabindex="350" />
				<input type="hidden" id="sourceRackPositionId" name="sourceRackPosition" value="${assetEntityInstance.sourceRackPosition}"/>
			</td>
		</tr>
	</g:else>
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
			<g:select from="${assetEntityInstance.constrainedProperties.validation.inList}" id="validation" name="validation" value="${ValidationType.UNKNOWN}"
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

<script type="text/javascript">
$(document).ready(function() { 
	var assetType = EntityCrud.getAssetType();

	EntityCrud.toggleAssetTypeFields( assetType );
	EntityCrud.loadFormFromJQGridFilters();

	EntityCrud.initializeUI("${assetEntityInstance?.model?.id}", "${assetEntityInstance?.model?.modelName}", "${assetEntityInstance?.model?.manufacturer?.id}", "${assetEntityInstance?.model?.manufacturer?.name}");

	EntityCrud.setManufacturerValues("${assetEntityInstance.model?.id}", "${assetEntityInstance.model?.modelName}", "${assetEntityInstance.model?.assetType}", "${assetEntityInstance.model?.manufacturer?.id}", "${assetEntityInstance.model?.manufacturer?.name}");
})
</script>
