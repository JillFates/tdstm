<g:form method="post"  name="editAssetsAuditFormId" controller="assetEntity" action="update">
<div>
<%-- Holds original values of the various SELECTS --%>
<input type="hidden" id="hiddenModel"        name="modelId" value="${assetEntityInstance?.model?.id}">
<input type="hidden" id="hiddenManufacturer" name="manufacturerId" value="${manufacturer?.id}">
<input type="hidden" id="deviceChassisIdS" value="${assetEntityInstance?.sourceChassis?.id}"/>
<input type="hidden" id="deviceChassisIdT" value="${assetEntityInstance?.targetChassis?.id}"/>
<input type="hidden" id="deviceRackIdS" value="${assetEntityInstance?.rackSource?.id}"/>
<input type="hidden" id="deviceRackIdT" value="${assetEntityInstance?.rackTarget?.id}"/>
<input type="hidden" id="deviceRoomIdS" value="${assetEntityInstance?.roomSource?.id}"/>
<input type="hidden" id="deviceRoomIdT" value="${assetEntityInstance?.roomTarget?.id}"/>
<input type="hidden" id="roomSourceId" name="roomSourceId" value="${assetEntityInstance.roomSource?assetEntityInstance.roomSource.id:0}"/>
<input type="hidden" id="roomTargetId" name="roomTargetId" value="${assetEntityInstance.roomTarget?assetEntityInstance.roomTarget.id:0}"/>

<%-- Used to maintain the selected AssetType --%>
<input type="hidden" id="currentAssetType" 		name="currentAssetType" value="${currentAssetType}"/>

<%-- Key field and optimistic locking var --%>
<input type="hidden" id="assetId" name="id" value="${assetEntityInstance?.id}"/>
<input type="hidden" id="version" name="version" value="${version}"/>

<input type="hidden" name="redirectTo" value="${redirectTo}"/>
<input name="dependentCount" id="dependentCount" type="hidden" value="0"/>
<input  name="supportCount"  id="supportCount" type="hidden" value="0"/>
<input  name="source"  id="sourceId" type="hidden" value="${source ?: 1}"/>
<table>
	<tr><td colspan="2"><b>Asset Audit Edit</b></td></tr>
	<tr class="prop rackLabel" >
		<td class="label ">Location</td>
		<td class="label" nowrap="nowrap">
			<input readonly="true" type="text" ${source=='1' ? 'name="sourceLocation11" value="'+assetEntityInstance.sourceLocationName+'"' : 'name="targetLocation11" value="'+assetEntityInstance.targetLocationName+'"'} size="8" /> /
			<input readonly="true" type="text" ${source=='1' ? 'name="sourceRoom11" value="'+assetEntityInstance.sourceRoomName+'"' : 'name="targetRoom11" value="'+assetEntityInstance.targetRoomName+'"'} size="8" />
		</td>
	</tr>
	<tr class="prop bladeLabel">
		<td class="label">Blade</td>
		<td class="label">
		<g:if test="${source=='1'}">
			<g:render template="deviceChassisSelect"
				model="[ domId:'sourceChassisSelectId', domName:'sourceChassis', 
					options:sourceChassisSelect, value:assetEntityInstance.sourceChassis?.id,
					domClass: config.sourceChassis, 
					sourceTarget:'S', forWhom:'$forWhom', tabindex:'312']"
			/>
		</g:if>
		<g:else>
			<g:render template="deviceChassisSelect"
				model="[ domId:'targetChassisSelectId', domName:'targetChassis',
					options:targetChassisSelect, value:assetEntityInstance.targetChassis?.id, 
					domClass: config.targetChassis, 
					sourceTarget:'T', forWhom:'$forWhom', tabindex:'342']"
			/>
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
			<input type="text" id="serialNumber" name="serialNumber" value="${assetEntityInstance.serialNumber}">
		</td>
	</tr>
	<g:if test="${source=='1'}">
		<tr class="prop rackLabel">
			<td class="label">Rack</td>
			<td class="label" nowrap="nowrap">
				<g:render template="deviceRackSelect" model="[clazz:config.sourceRack, options:sourceRackSelect, rackId:assetEntityInstance.rackSource?.id, 
					rackDomId:'rackSourceId', rackDomName:'rackSourceId', sourceTarget:'S', forWhom:'Edit', tabindex:'310']" />
				<input type="hidden" id="rackTargetId" name="rackTargetId" value="${assetEntityInstance.rackTarget?assetEntityInstance.rackTarget.id:0}"/>
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
				<input type="hidden" id="targetRackPositionId" name="targetRackPositionId" value="${assetEntityInstance.targetRackPosition}"/>
			</td>
		</tr>
	</g:if>
	<g:else>
		<tr class="prop rackLabel">
			<td class="label">Rack Target</td>
			<td class="label" nowrap="nowrap">
				<g:render template="deviceRackSelect"  model="[clazz:config.targetRack, options:targetRackSelect, rackId: assetEntityInstance.rackTarget?.id,
												rackDomId:'rackTargetId', rackDomName:'rackTargetId', sourceTarget:'T', forWhom:'Edit', tabindex:'340']" />
				<input type="hidden" id="rackSourceId" name="rackSourceId" value="${assetEntityInstance.rackSource?assetEntityInstance.rackSource.id:0}"/>
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
				<input type="hidden" id="sourceRackPositionId" name="sourceRackPositionId" value="${assetEntityInstance.sourceRackPosition}"/>
			</td>
		</tr>
	</g:else>

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
		<td><g:select from="${assetEntityInstance.constrainedProperties.validation.inList}" id="validation" name="validation" noSelection="${['':' Please Select']}"
					 value="${assetEntityInstance.validation}"/>	
		</td>
	</tr>
</table>
</div>
<div class="buttons">
	<input type="button" class="edit" value="Update" onclick="updateAudit()" /> 
	<input type="button" class="edit" value="Delete" onclick="deleteAudit(${assetEntityInstance.id},'server')" /> 
	<input type="button" class="edit" value="More..." onclick="showEditDeviceViewFromAudit('${assetEntityInstance.assetClass}', ${assetEntityInstance?.id})" /> 
</div><br>
</g:form>
<div id="modelAuditPanel">
<g:form name="modelAuditEdit" controller="model" action="updateModel" method="post" >
	<div>
	<input type="hidden" id="modelAuditPanel_updateModelId" name="id" value="">
	<input type="hidden" name="redirectTo" value="assetAudit"> 
		<table>
			<tr><td colspan="2"><b>Model Audit Edit</b></td></tr>
			<tr>
				<td>Model Name:</td>
				<td><span id="modelAuditPanel_modelName"></span></td>
			</tr>

			<tr>
				<td>Usize:</td>
				<td><g:select id="modelAuditPanel_usize" name="usize"
						from="${assetEntityInstance.model?.constrainedProperties?.usize?.inList ?: (1..42)}"
						value=""></g:select></td>
			</tr>
			<tr>
				<td>Manufacturer</td>
				<td>
					<span id="modelAuditPanel_manufacturerName"></span>
				</td>
			</tr>
		</table>
	</div>
	</g:form>
	<div class="buttons">
		<input type="button" class="edit" value="Update" onclick="updateModelAudit()" /> 
		<g:form action="edit" controller="model" target="new">
			<input name="id" type="hidden" id="modelAuditPanel_editModelId" value=""/>
			<span class="button">
				<input type="submit" class="edit" value="More..."></input>
			</span>
		</g:form>
	</div>
</div>

<script type="text/javascript">
$(document).ready(function() { 
	var assetType = EntityCrud.getAssetType();

	$(document).on('selectedAssetModelChanged', function(evt, evtSelectedModel) {
		updateAuditModelPanel(evtSelectedModel);
	});

	EntityCrud.toggleAssetTypeFields( assetType );
	EntityCrud.loadFormFromJQGridFilters();

	EntityCrud.initializeUI("${assetEntityInstance?.model?.id}", "${assetEntityInstance?.model?.modelName}", "${assetEntityInstance?.model?.manufacturer?.id}", "${assetEntityInstance?.model?.manufacturer?.name}");

	EntityCrud.setManufacturerValues("${assetEntityInstance.model?.id}", "${assetEntityInstance.model?.modelName}", "${assetEntityInstance.model?.assetType}", "${assetEntityInstance.model?.manufacturer?.id}", "${assetEntityInstance.model?.manufacturer?.name}");
})
</script>
