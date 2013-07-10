
<script type="text/javascript">
	$("#asset_assetName").val($('#gs_assetName').val())
	$("#asset_assetType").val($('#gs_assetType').val())
	$("#asset_model").val($('#gs_model').val())
	$("#asset_sourceLocation").val($('#gs_sourceLocation').val())
	$("#asset_sourceRack").val($('#gs_sourceRack').val())
	$("#asset_targetLocation").val($('#gs_targetLocation').val())
	$("#asset_targetRack").val($('#gs_targetRack').val())
	$("#asset_serialNumber").val($('#gs_serialNumber').val())
	$("#asset_planStatus").val($('#gs_planStatus').val())
	$("#asset_moveBundle").val($('#gs_moveBundle').val())
	$("#asset_assetTag").val($('#gs_assetTag').val())
	
	$(document).ready(function() { 
		if($("#assetTypeCreateId").val()=='Blade'){
			$(".bladeLabel").show()
			$(".rackLabel").hide()
			$(".vmLabel").hide()
		} else if($("#assetTypeCreateId").val()=='VM') {
			$(".bladeLabel").hide()
			$(".rackLabel").hide()
			$(".vmLabel").show()
		} else {
			$(".bladeLabel").hide()
			$(".rackLabel").show()
			$(".vmLabel").hide()
		}
	})
	
</script>
<g:form method="post">
	<input type="hidden" id="asset_assetName" name="assetNameFilter" value="" />
	<input type="hidden" id="asset_assetType" name="assetTypeFilter" value="" />
	<input type="hidden" id="asset_model" name="modelFilter" value="" />
	<input type="hidden" id="asset_sourceLocation" name="sourceLocationFilter" value="" />
	<input type="hidden" id="asset_sourceRack" name="sourceRackFilter" value="" />
	<input type="hidden" id="asset_targetLocation" name="targetLocationFilter" value="" />
	<input type="hidden" id="asset_targetRack" name="targetRackFilter" value="" />
	<input type="hidden" id="asset_moveBundle" name="moveBundleFilter" value="" />
	<input type="hidden" id="asset_serialNumber" name="serialNumberFilter" value="" />
	<input type="hidden" id="asset_planStatus" name="planStatusFilter" value="" />
	<input type="hidden" id="asset_assetTag" name="assetTagFilter" value="" />

	<table style="border:0;width:1000px;">
		<tr>
			<td colspan="2">
				<div class="dialog">
					<table>
						<tbody>
							<tr>
								<td class="label ${config.assetName}" nowrap="nowrap"><label for="assetName">Name</label></td>
								<td><input type="text" id="assetName" class="${config.assetName}" name="assetName" value="${assetEntityInstance.assetName}" tabindex="11" /></td>
								<td class="label ${config.environment}" nowrap="nowrap"><label for="environment">Environment</label></td>
								<td><g:select id="environment" class="${config.environment}" name="environment" from="${assetEntityInstance.constraints.environment.inList}" value="${assetEntityInstance.environment}"  tabindex="32"></g:select></td>
								<td>&nbsp</td>
								<td class="label_sm">Source</td>
								<td class="label_sm">Target</td>
							</tr>
							<tr>
								<td class="label ${config.assetType}" nowrap="nowrap"><label for="assetType">Type</label></td>
								<td><g:select from="${assetTypeOptions}" class="${config.assetType}" id="assetTypeCreateId" name="assetType" value="${assetType}" onChange="selectManufacturer(this.value,'Create')" tabindex="12" /></td>
								<td class="label ${config.priority}" nowrap="nowrap"><label for="priority">Priority</label>
								</td>
								<td ><g:select id="priority" class="${config.priority}" name ="priority" from="${priorityOption}" value= "${assetEntityInstance.priority}" noSelection="${['':' Please Select']}" tabindex="21" />
								</td>
								<td class="label ${config.sourceLocation}" nowrap="nowrap"><label for="sourceLocationId">Location</label></td>
								<td><input type="text" id="sourceLocationId" class="${config.sourceLocation}"
									name="sourceLocation" value="${assetEntityInstance.sourceLocation}" size=10 tabindex="31" /></td>
									<td><input type="text" id="targetLocationId" class="${config.targetLocation}"
									name="targetLocation" value="${assetEntityInstance.targetLocation}" size=10 tabindex="41" /></td>
							</tr>
							<tr>
								<td class="label ${config.manufacturer}" nowrap="nowrap"><label for="manufacturer">Manufacturer</label></td>
								 <td >
								 <div id="manufacturerCreateId">
								   <g:select id="manufacturer" class="${config.manufacturer}" name="manufacturer.id" from="${manufacturers}" value="${manufacuterer?.id}" onChange="selectModel(this.value,'Create')" optionKey="id" optionValue="name" noSelection="${[null:' Unassigned']}" tabindex="13" />
								 </div>
								</td>
								<td class="label ${config.ipAddress}" nowrap="nowrap"><label for="ipAddress">IP1</label></td>
								<td ><input type="text" id="ipAddress" name="ipAddress" class="${config.ipAddress}"
									value="${assetEntityInstance.ipAddress}" tabindex="22" />
								</td>
								<td class="label ${config.sourceRoom}" nowrap="nowrap"><label for="sourceRoomId">Room</label></td>
								<td><input type="text" id="sourceRoomId" class="${config.sourceRoom}"
									name="sourceRoom" value="${assetEntityInstance.sourceRoom}" size=10 tabindex="32" /></td>
									<td><input type="text" id="targetRoomId" class="${config.targetRoom}"
									name="targetRoom" value="${assetEntityInstance.targetRoom}" size=10 tabindex="42" /></td>
							</tr>
							<tr>
								<td class="label ${config.model}" nowrap="nowrap"><label for="model">Model</label></td>
								<td>
								<div id="modelCreateId">
								   <g:select id="model" class="${config.model}" name ="model.id" from="${models}" value= "${assetEntityInstance.model}" noSelection="${[null:' Unassigned']}" tabindex="14" 
								  		optionKey="id" optionValue="${{it.modelName+' '+(it.modelStatus =='new' || !it.modelStatus ? '?' :'')}}" onChange="setType(this.value, 'Create')"/>
								 </div>
								</td>
								<td class="label ${config.os}" nowrap="nowrap"><label for="os">OS</label></td>
								<td ><input type="text" id="os" name="os" class="${config.os}" value="${assetEntityInstance.os}"  tabindex="24" /></td>
								<td class="label ${config.sourceRack} rackLabel"  nowrap="nowrap" id="rackId"><label for="sourceRackId">Rack/Cab</label></td>
								<td class="label bladeLabel" nowrap="nowrap" id="bladeId" style="display: none"><label for="sourceBladeChassisId">Blade</label></td>
								<td class="label vmLabel" style="display: none" class="label" nowrap="nowrap"><label for="virtualHost">Virtual Host</label>
								<td class="rackLabel"><input type="text" id="sourceRackId" class="${config.sourceRack}"
									name="sourceRack" value="${assetEntityInstance.sourceRack}" size=10 tabindex="33" /></td>
								<td class="rackLabel"><input type="text" id="targetRackId" class="${config.targetRack}"
									name="targetRack" value="${assetEntityInstance.targetRack}" size=10 tabindex="44" /></td>
								<td class="bladeLabel" style="display: none"><input type="text" id="sourceBladeChassisId" class="${config.sourceBladeChassis}"
									name="sourceBladeChassis" value="${assetEntityInstance.sourceBladeChassis}" size=10 tabindex="35"/></td>
								<td class="bladeLabel" style="display: none"><input type="text" id="targetBladeChassisId" class="${config.targetBladeChassis}"
									name="targetBladeChassis" value="${assetEntityInstance.targetBladeChassis}" size=10 tabindex="45" /></td>
								<td class="vmLabel" style="display: none"><input type="text" id="virtualHost" class="${config.virtualHost}" name="virtualHost" value="${assetEntityInstance.virtualHost}" size=10 tabindex="37" /></td>
								<td class="vmLabel" style="display: none">&nbsp;</td>
								
							</tr>
							<tr>
								<td class="label ${config.shortName}" nowrap="nowrap"><label for="shortName">Alt Name</label></td>
								<td ><input type="text" id="shortName" class="${config.shortName}"
									name="shortName" value="${assetEntityInstance.shortName}" tabindex="15" />
								</td>
								<td class="label ${config.supportType}" nowrap="nowrap"><label for="supportType">Support Type</label></td>
								<td ><input type="text" id="supportType" class="${config.supportType}" name="supportType"
									value="${assetEntityInstance.supportType}" tabindex="26" />
								</td>
								
								<td class="label ${config.sourceRackPosition}" nowrap="nowrap"><label for="sourceRackPositionId">Position</label>
								<td class="rackLabel"><input type="text" id="sourceRackPositionId" class="${config.sourceRackPosition}"
									name="sourceRackPosition" value="${assetEntityInstance.sourceRackPosition}" size=10 tabindex="34" /></td>
								<td class="rackLabel"> <input type="text" id="targetRackPositionId" class="${config.targetRackPosition}"
									name="targetRackPosition" value="${assetEntityInstance.targetRackPosition}" size=10 tabindex="44" /></td>
								<td class="bladeLabel"><input type="text" id="sourceBladePositionId" class="${config.sourceBladePosition}"
									name="sourceBladePosition" value="${assetEntityInstance.sourceBladePosition}" size=10 tabindex="36"/></td>
								<td class="bladeLabel"><input type="text" id="targetBladePositionId" class="${config.targetBladePosition}"
									name="targetBladePosition" value="${assetEntityInstance.targetBladePosition}" size=10 tabindex="46" /></td>	
								<td class="vmLabel">&nbsp;</td>
								<td class="vmLabel">&nbsp;</td>	
							</tr>
							<tr>
								<td class="label ${config.serialNumber}" nowrap="nowrap"><label for="serialNumber">Serial #</label></td>
								<td ><input type="text" id="serialNumber" class="${config.serialNumber}"
									name="serialNumber" value="${assetEntityInstance.serialNumber}" tabindex="16" />
								</td>
								<td class="label ${config.retireDate}"><label for="retireDate">Retire Date:</label></td>
								<td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'retireDate','errors')}">
								    <script type="text/javascript" charset="utf-8">
				           				jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${resource(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
				        				</script> <input type="text" class="dateRange ${config.retireDate}" size="15" style="width: 112px; height: 14px;" name="retireDate" id="retireDate" tabindex="27"
									value="<tds:convertDate date="${assetEntityInstance?.retireDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" tabindex="27" />" > 
								</td>
								<td class="label ${config.moveBundle}" nowrap="nowrap"><label for="moveBundle">Bundle</label></td>
								<td colspan="2"><g:select from="${moveBundleList}" id="moveBundle" class="${config.moveBundle}" name="moveBundle.id" value="${assetEntityInstance.moveBundle}" optionKey="id" optionValue="name" tabindex="38" noSelection="${['':' Please Select']}" />
								</td>
							</tr>
							<tr>
								<td class="label ${config.assetTag}" nowrap="nowrap"><label for="assetTag">Tag</label></td>
								<td ><input type="text" id="assetTag" class="${config.assetTag}" name="assetTag" value="${assetEntityInstance.assetTag}" tabindex="17"/></td>
								<td  class="label ${config.maintExpDate}"><label for="maintExpDate">Maint Exp.</label></td>
								<td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'maintExpDate','errors')}">
								<script type="text/javascript" charset="utf-8">
					                    jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${resource(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
					                    </script> <input type="text" class="dateRange ${config.maintExpDate}" size="15" style="width: 112px; height: 14px;" name="maintExpDate" id="maintExpDate" tabindex="28"
									value="<tds:convertDate date="${assetEntityInstance?.maintExpDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />" > 
								</td>
								<td class="label ${config.planStatus}" nowrap="nowrap"><label for="planStatus">Plan Status</label></td>
								<td colspan="2"><g:select id="planStatus" class="${config.planStatus}" name ="planStatus" from="${planStatusOptions}" value= "Unassigned"  noSelection="${['':' Please Select']}" tabindex="39" /></td>
							</tr>
							<tr>
								<td class="label ${config.railType}" nowrap="nowrap"><label for="railType">Rail Type</label></td>
								<td ><g:select id="railType" class="${config.railType}" name ="railType" from="${railTypeOption}" value= "${assetEntityInstance.railType}" noSelection="${['':' Please Select']}" tabindex="64" /></td>
								<td class="label ${config.truck}" nowrap="nowrap"><label for="truck">Truck/Cart/Shelf</label></td>
								<td ><input type="text" id="truck" class="${config.truck}" name="truck" value="${assetEntityInstance.truck}" size=3 tabindex="61" />
								<input type="text" id="cart" class="${config.cart}" name="cart" value="${assetEntityInstance.cart}" size=3 tabindex="62" />
								<input type="text" id="shelf" class="${config.shelf}" name="shelf" value="${assetEntityInstance.shelf}" size=2 tabindex="63" /></td>
								<td class="label ${config.validation}"><label for="validation">Validation</label></td>
								<td colspan="2">
									<g:select from="${assetEntityInstance.constraints.validation.inList}" id="validation" class="${config.validation}" name="validation" onChange="assetCustoms('',this.value,'AssetEntity');assetFieldImportance(this.value,'AssetEntity');" value="Discovery"/>
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="externalRefId">External Ref Id</label></td>
								<td><input type="text" id="externalRefId" name="externalRefId" value="${assetEntityInstance.externalRefId}" tabindex="11" /></td>
							</tr>
							<tbody class="customTemplate">
								<g:render template="customEdit" ></g:render>
							</tbody>
						</tbody>
					</table>
				</div></td>
		</tr>
		<tr>
			<td valign="top">
				<div style="width: auto;">
					<span style="float: left;"><h1>Supports:</h1></span>
					<span style="float: right;"><input type='button' value='Add' onclick="addAssetDependency('support', 'create')"></span>
					<br/>
					<table style="width: 100%;">
						<thead>
							<tr>
								<th>Frequency</th>
								<th>Type</th>
								<th>Name</th>
								<th>Type</th>
								<th>Status</th>
								<th>&nbsp;</th>
							</tr>
						</thead>
						<tbody id="createSupportsList">
						</tbody>
					</table>
				</div></td>
			<td valign="top">
				<div style="width: auto;">
					<span style="float: left;"><h1>Is dependent on:</h1></span>
					<span style="float: right;"><input type='button' value='Add' onclick="addAssetDependency('dependent', 'create')"></span>
					<br/>
					<table style="width: 100%;">
						<thead>
							<tr>
								<th>Frequency</th>
								<th>Type</th>
								<th>Name</th>
								<th>Type</th>
								<th>Status</th>
								<th>&nbsp;</th>
							</tr>
						</thead>
						<tbody id="createDependentsList">
						</tbody>
					</table>
				</div>
			</td>
		</tr>
		<tr>
			<td colspan="19">
				<div class="buttons">
					<input name="attributeSet.id" type="hidden" value="1">
					<input name="project.id" type="hidden" value="${projectId}" />
					<input name="dependentCount" id="create_dependentCount" type="hidden" value="0" />
					<input  name="supportCount"  id="create_supportCount" type="hidden" value="0" />
					<input name="redirectTo" type="hidden" value="${redirectTo}">
					<span class="button"><g:actionSubmit class="save" value="Save" /> </span>
				</div></td>
		</tr>
	</table>
</g:form>
<script>
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#0020366')
</script>
