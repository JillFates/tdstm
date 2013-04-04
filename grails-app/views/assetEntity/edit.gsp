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
		var assetType = $("#assetTypeId").val()
		if(assetType =='Blade'){
			$(".bladeLabel").show()
			$(".rackLabel").hide()
			$(".vmLabel").hide()
		 } else if(assetType=='VM') {
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
<g:form method="post"  name="editAssetsFormId"  action="update">

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
<g:set var="isBlade" value="${assetEntityInstance.assetType == 'Blade' ? true : false}"/>
	<table style="border:0;width:1000px;">
		<tr>
			<td colspan="2">
				<div class="dialog">
					<table>
						<tbody>
							<tr>
								<td class="label" nowrap="nowrap"><label for="assetName">Name</label></td>
								<td ><input type="text" id="assetName" name="assetName" value="${assetEntityInstance.assetName}" tabindex="11" /></td>
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td class="label_sm">Source</td>
								<td class="label_sm">Target</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="assetType">Type</label></td>
								<td ><g:select from="${assetTypeOptions}" id="assetTypeId" name="assetType" value="${assetType}" onChange="selectManufacturer(this.value, 'Edit')" tabindex="12"/></td>
								<td class="label" nowrap="nowrap"><label for="priority">Priority</label>
								</td>
								<td ><g:select id="priority" name ="priority" from="${priorityOption}" value= "${assetEntityInstance.priority}" noSelection="${['':' Please Select']}" tabindex="21"/>
								</td>
								<td class="label" nowrap="nowrap"><label for="sourceLocationId">Location</label></td>
								<td><input type="text" id="sourceLocationId"
									name="sourceLocation" value="${assetEntityInstance.sourceLocation}" size=10 tabindex="31"/></td>
									<td><input type="text" id="targetLocationId"
									name="targetLocation" value="${assetEntityInstance.targetLocation}" size=10 tabindex="41"/></td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap">
									
									<g:if test="${assetEntityInstance.manufacturer?.id}">
										<a href='javascript:showManufacturer(${assetEntityInstance.manufacturer?.id})' style='color:#00E'>Manufacturer</a>
									</g:if>
									<g:else>
										<label for="manufacturer">Manufacturer</label>
									</g:else>	
								</td>
								 <td >
								 <div id="manufacturerEditId">
								   <g:select id="manufacturer" name="manufacturer.id" from="${manufacturers}" value="${manufacturer?.id}" onChange="selectModel(this.value,'Edit')" optionKey="id" optionValue="name" noSelection="${[null:'Unassigned']}" tabindex="13"/>
								 </div>
								</td>
								<td class="label" nowrap="nowrap"><label for="ipAddress">IP1</label></td>
								<td ><input type="text" id="ipAddress" name="ipAddress"
									value="${assetEntityInstance.ipAddress}" tabindex="22"/>
								</td>
								<td class="label" nowrap="nowrap"><label for="sourceRoomId">Room</label></td>
								<td><input type="text" id="sourceRoomId"
									name="sourceRoom" value="${assetEntityInstance.roomSource?.roomName}" size=10 tabindex="32"/></td>
									<td><input type="text" id="targetRoomId"
									name="targetRoom" value="${assetEntityInstance.roomTarget?.roomName}" size=10 tabindex="42" /></td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap">
									<g:if test="${assetEntityInstance.model?.id}">
										<a href='javascript:showModel(${assetEntityInstance.model?.id})' style='color:#00E'>Model</a>
									</g:if>
									<g:else>
										<label for="model">Model</label>
									</g:else>
								</td>
								<td>
								<div id="modelEditId">
								   <g:select id="model" name ="model.id" from="${models}" value= "${assetEntityInstance.model}" optionKey="id" optionValue="modelName"  noSelection="${[null:' Unassigned']}" tabindex="14"
								   optionValue="${{it.modelName+' '+(it.modelStatus =='new' ? '?' :'')}}" onChange="setType(this.value)"/>
								 </div>
								</td>
								<td class="label" nowrap="nowrap"><label for="os">OS</label></td>
								<td ><input type="text" id="os" name="os" value="${assetEntityInstance.os}"  tabindex="24"/></td>
								<td class="label rackLabel"  nowrap="nowrap" id="rackId"><label for="sourceRackId">Rack/Cab</label></td>
								<td class="label bladeLabel" nowrap="nowrap" id="bladeId" style="display: none"><label for="sourceBladeChassisId">Blade</label></td>
								<td class="label vmLabel" style="display: none" class="label" nowrap="nowrap"><label for="virtualHost">Virtual Host</label>
								<td class="rackLabel"><input type="text" id="sourceRackId"
									name="sourceRack" value="${assetEntityInstance.sourceRack}" size=10 tabindex="33" /></td>
								<td class="rackLabel"><input type="text" id="targetRackId"
									name="targetRack" value="${assetEntityInstance.targetRack}" size=10 tabindex="44" /></td>
								<td class="bladeLabel" style="display: none"><g:select id='sourceBladeChassis' from='${sourceChassisSelect}' optionKey='${-2}' optionValue='${1}'
									  name="sourceBladeChassis" value="${assetEntityInstance.sourceBladeChassis}" noSelection="${['':' Please Select']}"/></td>
								<td class="bladeLabel" style="display: none"><g:select id='targetBladeChassis' from='${targetChassisSelect}' optionKey='${-2}' optionValue='${1}'
										name="targetBladeChassis"  value="${assetEntityInstance.targetBladeChassis}" noSelection="${['':' Please Select']}"/></td>
								<td class="vmLabel" style="display: none"><input type="text" id="virtualHost" name="virtualHost" value="${assetEntityInstance.virtualHost}" size=10 tabindex="37" /></td>
								<td class="vmLabel" style="display: none">&nbsp;</td>
								
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="shortName">Alt Name</label></td>
								<td ><input type="text" id="shortName"
									name="shortName" value="${assetEntityInstance.shortName}" tabindex="15"/>
								</td>
								<td class="label" nowrap="nowrap"><label for="supportType">Support Type</label></td>
								<td ><input type="text" id="supportType" name="supportType"
									value="${assetEntityInstance.supportType}" tabindex="26"/>
								</td>
								
								<td class="label" nowrap="nowrap"><label for="sourceRackPositionId">Position</label>
								<td class="rackLabel"><input type="text" id="sourceRackPositionId"
									name="sourceRackPosition" value="${assetEntityInstance.sourceRackPosition}" size=10 tabindex="34" /></td>
								<td class="rackLabel"> <input type="text" id="targetRackPositionId"
									name="targetRackPosition" value="${assetEntityInstance.targetRackPosition}" size=10 tabindex="44" /></td>
								<td class="bladeLabel"><input type="text" id="sourceBladePositionId"
									name="sourceBladePosition" value="${assetEntityInstance.sourceBladePosition}" size=10 tabindex="36"/></td>
								<td class="bladeLabel"><input type="text" id="targetBladePositionId"
									name="targetBladePosition" value="${assetEntityInstance.targetBladePosition}" size=10 tabindex="46" /></td>	
								<td class="vmLabel">&nbsp;</td>
								<td class="vmLabel">&nbsp;</td>	
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="serialNumber">Serial #</label></td>
								<td ><input type="text" id="serialNumber"
									name="serialNumber" value="${assetEntityInstance.serialNumber}" tabindex="16"/>
								</td>
								<td class="label"><label for="retireDate">Retire Date:</label></td>
								<td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'retireDate','errors')}">
								    <script type="text/javascript" charset="utf-8">
				                    jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${resource(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
				                    </script> <input type="text" class="dateRange" size="15" style="width: 112px; height: 14px;" name="retireDate" id="retireDate" tabindex="27"
									value="<tds:convertDate date="${assetEntityInstance?.retireDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" tabindex="27" />" > 
								</td>
								<td class="label" nowrap="nowrap"><label for="moveBundle">Bundle</label></td>
								<td colspan="2"><g:select from="${moveBundleList}" id="moveBundle" name="moveBundle.id" value="${assetEntityInstance.moveBundle?.id}" optionKey="id" optionValue="name" tabindex="38" noSelection="${['null':' Please Select']}"/></td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="assetTag">Tag</label></td>
								<td ><input type="text" id="assetTag" name="assetTag" value="${assetEntityInstance.assetTag}" tabindex="17"/></td>
								<td  class="label"><label for="maintExpDate">Maint Exp.</label></td>
								<td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'maintExpDate','errors')}">
									<script type="text/javascript" charset="utf-8">
					                	    jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${resource(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
					                	    </script> <input type="text" class="dateRange" size="15" style="width: 112px; height: 14px;" name="maintExpDate" id="maintExpDate" tabindex="28"
									value="<tds:convertDate date="${assetEntityInstance?.maintExpDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>" > 
								</td>
								<td class="label" nowrap="nowrap"><label for="planStatus">Plan Status</label></td>
								<td colspan="2"><g:select id="planStatus" name ="planStatus" from="${planStatusOptions}" value= "${assetEntityInstance.planStatus}" noSelection="${['':' Please Select']}" tabindex="39"/></td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="railType">Rail Type</label></td>
								 <td ><g:select id="railType" name ="railType" from="${railTypeOption}" value= "${assetEntityInstance.railType}" noSelection="${['':' Please Select']}" tabindex="64"/></td>
								<td class="label" nowrap="nowrap"><label for="truck">Truck/Cart/Shelf</label></td>
								<td ><input type="text" id="truck" name="truck" value="${assetEntityInstance.truck}" size=3 tabindex="61" />
								<input type="text" id="cart" name="cart" value="${assetEntityInstance.cart}" size=3 tabindex="62" />
								<input type="text" id="shelf" name="shelf" value="${assetEntityInstance.shelf}" size=2 tabindex="63" /></td>
								<td class="label">Validation</td>
								<td  colspan="2"><g:select from="${assetEntityInstance.constraints.validation.inList}" id="validation" name="validation" value="${assetEntityInstance.validation}"/>	
								</td>
							</tr>
							<g:render template="customEdit" ></g:render>
						</tbody>
					</table>
				</div></td>
		</tr>
<tr>
			<td valign="top">
				<div style="width: auto;">
					<span style="float: left;"><h1>Supports:</h1></span>
					<span style="float: right;"><input type='button' value='Add' onclick="addAssetDependency('support')"></span>
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
							<g:each in="${supportAssets}" var="support" status="i">
								<tr id='row_s_${i}'>
									<td><g:select name="dataFlowFreq_support_${i}" value="${support.dataFlowFreq}" from="${support.constraints.dataFlowFreq.inList}" /></td>
									<td><g:select name="entity_support_${i}" from="['Server','Application','Database','Storage','Network']" onchange='updateAssetsList(this.name, this.value)' value="${support?.asset?.assetType=='Files'?'Storage':support?.asset?.assetType}"></g:select></td>
									<g:if test="${support?.asset?.assetType=='Server'|| support?.asset.assetType=='Blade' || support?.asset.assetType=='VM'}">
									<td><g:select name="asset_support_${i}" from="${com.tds.asset.AssetEntity.findAll('from AssetEntity where assetType in (\'Server\',\'VM\',\'Blade\') and project = ? order by assetName asc ',[project])}" value="${support?.asset?.id}" optionKey="id" optionValue="assetName"  style="width:105px;"></g:select></td>
									</g:if>
									 <g:elseif test="${support?.asset?.assetType!='Application'|| support?.asset.assetType!='Database' || support?.asset.assetType!='Files'}">
									<td><g:select name="asset_support_${i}" from="${com.tds.asset.AssetEntity.findAll('from AssetEntity where assetType not in (\'Server\',\'VM\',\'Blade\',\'Application\',\'Database\',\'Files\') and project = ? order by assetName asc ',[project])}" value="${support?.asset?.id}" optionKey="id" optionValue="assetName" style="width:105px;"></g:select></td>
									</g:elseif>
									<g:else>
									<td><g:select name="asset_support_${i}" from="${com.tds.asset.AssetEntity.findAll('from AssetEntity where assetType = ? and project =? order by assetName asc',[support?.asset?.assetType, project])}" value="${support?.asset?.id}" optionKey="id" optionValue="assetName"  style="width:105px;"></g:select></td>
									</g:else>
									<td><g:select name="dtype_support_${i}" value="${support.type}" from="${dependencyType.value}" optionValue="value" />
									</td>
									<td><g:select name="status_support_${i}" value="${support.status}" from="${dependencyStatus.value}" optionValue="value" />
									</td>
									<td><a href="javascript:deleteRow('row_s_${i}')"><span class='clear_filter'><u>X</u></span></a></td>
								</tr>
							</g:each>
						</tbody>
					</table>
				</div></td>
			<td valign="top">
				<div style="width: auto;">
					<span style="float: left;"><h1>Is dependent on:</h1></span>
					<span style="float: right;"><input type='button' value='Add' onclick="addAssetDependency('dependent')"></span>
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
						<g:each in="${dependentAssets}" var="dependent" status="i">
							<tr id='row_d_${i}'>
								<td><g:select name="dataFlowFreq_dependent_${i}" value="${dependent.dataFlowFreq}" from="${dependent.constraints.dataFlowFreq.inList}" /></td>
								<td><g:select name="entity_dependent_${i}" from="['Server','Application','Database','Storage','Network']" onchange='updateAssetsList(this.name, this.value)' value="${dependent?.dependent?.assetType == 'Files' ? 'Storage': dependent?.dependent?.assetType}"></g:select></td>
								<g:if test="${dependent?.dependent?.assetType=='Server'|| dependent?.dependent?.assetType=='Blade' || dependent?.dependent?.assetType=='VM'}">
								  <td><g:select name="asset_dependent_${i}" from="${com.tds.asset.AssetEntity.findAll('from AssetEntity where assetType in (\'Server\',\'VM\',\'Blade\') and project = ? order by assetName asc ',[project])}" value="${dependent?.dependent?.id}" optionKey="id" optionValue="assetName"  style="width:105px;"></g:select></td>
								</g:if>
								 <g:elseif test="${support?.asset?.assetType!='Application'|| support?.asset.assetType!='Database' || support?.asset.assetType!='Files'}">
								<td><g:select name="asset_support_${i}" from="${com.tds.asset.AssetEntity.findAll('from AssetEntity where assetType not in (\'Server\',\'VM\',\'Blade\',\'Application\',\'Database\',\'Files\') and project = ? order by assetName asc ',[project])}" value="${support?.asset?.id}" optionKey="id" optionValue="assetName" style="width:105px;"></g:select></td>
								</g:elseif>
								<g:else>
								  <td><g:select name="asset_dependent_${i}" from="${com.tds.asset.AssetEntity.findAll('from AssetEntity where assetType = ? and project = ? order by assetName asc ',[dependent?.dependent?.assetType, project])}" value="${dependent?.dependent?.id}" optionKey="id" optionValue="assetName"  style="width:105px;"></g:select></td>
								</g:else>
								<td><g:select name="dtype_dependent_${i}" value="${dependent.type}" from="${dependencyType.value}" optionValue="value"/>
								</td>
								<td><g:select name="status_dependent_${i}" value="${dependent.status}" from="${dependencyStatus.value}" optionValue="value"/>
								</td>
								<td><a href="javascript:deleteRow('row_d_${i}')"><span class='clear_filter'><u>X</u></span></a></td>
							</tr>
						</g:each>
						</tbody>
					</table>
				</div>
			</td>
		</tr>
		<tr>
			<td colspan="2">
				<div class="buttons">
				    <input name="dependentCount" id="dependentCount" type="hidden" value="${dependentAssets.size()}"/>
					<input  name="supportCount"  id="supportCount" type="hidden" value="${supportAssets.size()}"/>
					<input name="attributeSet.id" type="hidden" value="1"/>
					<input name="project.id" type="hidden" value="${projectId}"/>
					<input name="id" id="assetId" type="hidden" value="${assetEntityInstance.id}"/>
					<input type = "hidden" id = "dstPath" name = "dstPath" value ="${redirectTo}"/>
					<input name="redirectTo" id="redirectTo" type="hidden" value="${redirectTo}">
					<input name="updateView" id="updateView" type="hidden" value="">
					<input type = "hidden" id = "tabType" name="tabType" value =""/>
					<input type="hidden" id="labelsListId" name="labels" value =""/>
					<input type="hidden" id="updateViewId" name="updateViewId" value =""/>
					<g:if test="${redirectTo!='planningConsole'}">
					  <span class="button"><g:actionSubmit class="save" value="Update/Close" action="Update" /> </span>
					  <span class="button"><input type="button" class="save" value="Update/View" onclick="updateToShow()" /> </span>
					  <span class="button"><g:actionSubmit class="delete"	onclick=" return confirm('Are you sure?');" value="Delete" /> </span>
					</g:if>
					<g:else>
					  
					  <span class="button"><input id="updatedId" name="updatedId" type="button" class="save" value="Update/Close" onclick="submitRemoteForm()"> </span>
					  <span class="button"><input type="button" class="save" value="Update/View" onclick="updateToShow()" /> </span>
					  <span class="button"><input id="deleteId"	 name="deleteId"  class="save" value="Delete" onclick=" deleteAsset($('#assetId').val(),'server')" value="Delete" /> </span>
					</g:else>
					
				</div></td>
		</tr>
	</table>
</g:form>
<script>
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')
	$('#tabType').val($('#assetTypesId').val())
$(document).ready(function() {
	var labelsList = " "
		$('#labelTree input:checked').each(function() {
			labelsList += $(this).val()+',';
		});
	$('#labelsListId').val(labelsList)
	$('#distanceId').val($('#distance').val())
	$('#frictionId').val($('#friction').val())
	$('#forceId').val($('#force').val())
	var isBlade = '${isBlade}'
	if(isBlade == 'false'){
		$("#sourceBladeChassis").attr('onChange','this.selectedIndex = 0')
		$("#targetBladeChassis").attr('onChange','this.selectedIndex = 0')
	} else {
		$("#sourceBladeChassis").removeAttr('onChange')
		$("#targetBladeChassis").removeAttr('onChange')
	}
	
})
</script>
