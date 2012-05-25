<g:form method="post"  name="editAssetsFormId" action="update">
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
								<td class="label" nowrap="nowrap"><label for="custom1">${assetEntityInstance.project.custom1 ?: 'Custom1' }</label></td>
								<td ><input type="text" id="custom1" name="custom1" value="${assetEntityInstance.custom1}" size=8 tabindex="51"/></td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="assetType">Type</label></td>
								<td ><g:select from="${assetTypeOptions}" id="assetTypeId" name="assetType" value="${assetEntityInstance.assetType}" onChange="selectManufacturer(this.value)" tabindex="12"/></td>
								<td class="label" nowrap="nowrap"><label for="priority">Priority</label>
								</td>
								<td ><g:select id="priority" name ="priority" from="${priorityOption}" value= "${assetEntityInstance.priority}" noSelection="${['':' Please Select']}" tabindex="21"/>
								</td>
								<td class="label" nowrap="nowrap"><label for="sourceLocationId">Location</label></td>
								<td><input type="text" id="sourceLocationId"
									name="sourceLocation" value="${assetEntityInstance.sourceLocation}" size=10 tabindex="31"/></td>
									<td><input type="text" id="targetLocationId"
									name="targetLocation" value="${assetEntityInstance.targetLocation}" size=10 tabindex="41"/></td>
								<td class="label" nowrap="nowrap"><label for="custom2">${assetEntityInstance.project.custom2 ?: 'Custom2' }</label></td>
								<td ><input type="text" id="custom2" name="custom2" value="${assetEntityInstance.custom2}" size=8 tabindex="51"/></td>
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
								 <div id="manufacturerId">
								   <g:select id="manufacturer" name="manufacturer.id" from="${manufacturers}" value="${assetEntityInstance.manufacturer?.id}" onChange="selectModel(this.value)" optionKey="id" optionValue="name" noSelection="${[null:'Unassigned']}" tabindex="13"/>
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
								<td class="label" nowrap="nowrap"><label for="custom3">${assetEntityInstance.project.custom3 ?: 'Custom3' }</label></td>
								<td ><input type="text" id="custom3" name="custom3" value="${assetEntityInstance.custom3}" size=8 tabindex="52" /></td>
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
								<div id="modelId">
								   <g:select id="model" name ="model.id" from="${models}" value= "${assetEntityInstance.model?.id}" optionKey="id" optionValue="modelName"  noSelection="${[null:' Unassigned']}" tabindex="14"/>
								 </div>
								</td>
								<td class="label" nowrap="nowrap"><label for="os">OS</label></td>
								<td ><input type="text" id="os" name="os" value="${assetEntityInstance.os}"  tabindex="24"/></td>
								<td class="label" nowrap="nowrap"><label for="sourceRackId">Rack/Cab</label></td>
								<td><input type="text" id="sourceRackId"
									name="sourceRack" value="${assetEntityInstance.rackSource?.tag}" size=10 tabindex="33"/></td>
									<td><input type="text" id="targetRackId"
									name="targetRack" value="${assetEntityInstance.rackTarget?.tag}" size=10 tabindex="44" /></td>
								<td class="label" nowrap="nowrap"><label for="custom4">${assetEntityInstance.project.custom4 ?: 'Custom4' }</label></td>
								<td ><input type="text" id="custom4" name="custom4" value="${assetEntityInstance.custom4}" size=8 tabindex="53" /></td>
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
								<td><input type="text" id="sourceRackPositionId"
									name="sourceRackPosition" value="${assetEntityInstance.sourceRackPosition}" size=10 tabindex="34" /></td>
									<td><input type="text" id="targetRackPositionId"
									name="targetRackPosition" value="${assetEntityInstance.targetRackPosition}" size=10 tabindex="44"/></td>
								</td>
								<td class="label" nowrap="nowrap"><label for="custom5">${assetEntityInstance.project.custom5 ?: 'Custom5' }</label></td>
								<td ><input type="text" id="custom5" name="custom5" value="${assetEntityInstance.custom5}" size=8 tabindex="54"/></td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="serialNumber">Serial #</label></td>
								<td ><input type="text" id="serialNumber"
									name="serialNumber" value="${assetEntityInstance.serialNumber}" tabindex="16"/>
								</td>
								<td class="label"><label for="retireDate">Retire Date:</label></td>
								<td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'retireDate','errors')}">
								    <script type="text/javascript" charset="utf-8">
				                    jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
				                    </script> <input type="text" class="dateRange" size="15" style="width: 112px; height: 14px;" name="retireDate" id="retireDate" tabindex="27"
									value="<tds:convertDate date="${assetEntityInstance?.retireDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" tabindex="27" />" > 
								</td>
								<td class="label" nowrap="nowrap"><label for="sourceBladeChassis">Blade</label></td>
								<td><input type="text" id="sourceBladeChassis"
									name="sourceBladeChassis" value="${assetEntityInstance.sourceBladeChassis}" size=10 tabindex="35"/></td>
									<td><input type="text" id="targetBladeChassis"
									name="targetBladeChassis" value="${assetEntityInstance.targetBladeChassis}" size=10 tabindex="45" /></td>
								<td class="label" nowrap="nowrap"><label for="custom6">${assetEntityInstance.project.custom6 ?: 'Custom6' }</label></td>
								<td ><input type="text" id="custom6" name="custom6" value="${assetEntityInstance.custom6}" size=8 tabindex="55" /></td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="assetTag">Tag</label></td>
								<td ><input type="text" id="assetTag" name="assetTag" value="${assetEntityInstance.assetTag}" tabindex="17"/></td>
								<td  class="label"><label for="maintExpDate">Maint Exp.</label></td>
								<td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'maintExpDate','errors')}">
									<script type="text/javascript" charset="utf-8">
					                	    jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
					                	    </script> <input type="text" class="dateRange" size="15" style="width: 112px; height: 14px;" name="maintExpDate" id="maintExpDate" tabindex="28"
									value="<tds:convertDate date="${assetEntityInstance?.maintExpDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>" > 
								</td>
								<td class="label" nowrap="nowrap"><label for="sourceBladePosition">Blade Position</label></td>
								<td><input type="text" id="sourceBladePosition"
									name="sourceBladePosition" value="${assetEntityInstance.sourceBladePosition}" size=10 tabindex="36"/></td>
									<td><input type="text" id="targetBladePosition"
									name="targetBladePosition" value="${assetEntityInstance.targetBladePosition}" size=10 tabindex="46" /></td>
								<td class="label" nowrap="nowrap"><label for="custom7">${assetEntityInstance.project.custom7 ?: 'Custom7' }</label></td>
								<td ><input type="text" id="custom7" name="custom7" value="${assetEntityInstance.custom7}" size=8 tabindex="56"/></td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="railType">Rail Type</label></td>
								 <td ><g:select id="railType" name ="railType" from="${railTypeOption}" value= "${assetEntityInstance.railType}" noSelection="${['':' Please Select']}" tabindex="64"/></td>
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td class="label" nowrap="nowrap"><label for="virtualHost">Virtual Host</label>
								<td><input type="text" id="virtualHost"
									name="virtualHost" value="${assetEntityInstance.virtualHost}" size=10 tabindex="37"/></td>
								<td>&nbsp</td>
								<td class="label" nowrap="nowrap"><label for="custom8">${assetEntityInstance.project.custom8 ?: 'Custom8' }</label></td>
								<td ><input type="text" id="custom8" name="custom8" value="${assetEntityInstance.custom8}" size=8 tabindex="57" /></td>
							</tr>
							<tr>
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td class="label" nowrap="nowrap"><label for="moveBundle">Bundle</label></td>
								<td colspan="2"><g:select from="${moveBundleList}" id="moveBundle" name="moveBundle.id" value="${assetEntityInstance.moveBundle?.id}" optionKey="id" optionValue="name" tabindex="38" noSelection="${['null':' Please Select']}"/></td>
								<td class="label" nowrap="nowrap"><label for="truck">Truck</label></td>
								<td ><input type="text" id="truck" name="truck" value="${assetEntityInstance.truck}" size=6 tabindex="61"/></td>
							</tr>
							<tr>
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td class="label" nowrap="nowrap"><label for="planStatus">Plan Status</label></td>
								<td colspan="2"><g:select id="planStatus" name ="planStatus" from="${planStatusOptions}" value= "${assetEntityInstance.planStatus}" noSelection="${['':' Please Select']}" tabindex="39"/></td>
								<td class="label" nowrap="nowrap"><label for="cart">Cart / Shelf</label></td>
								<td nowrap="nowrap"><input type="text" id="cart" name="cart" value="${assetEntityInstance.cart}" size=3 tabindex="62" />
								<input type="text" id="shelf" name="shelf" value="${assetEntityInstance.shelf}" size=2 tabindex="63"/></td>
							</tr>
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
								<th>Entity Type</th>
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
									<td><g:select name="entity_support_${i}" from="['Server','Application','Database','Files']" onchange='updateAssetsList(this.name, this.value)' value="${support?.asset?.assetType}"></g:select></td>
									<g:if test="${support?.asset?.assetType=='Server'}">
									<td><g:select name="asset_support_${i}" from="${com.tds.asset.AssetEntity.findAll('from AssetEntity where assetType in (\'Server\',\'VM\',\'Blade\') and project = ? order by assetName asc ',[project])}" value="${support?.asset?.id}" optionKey="id" optionValue="assetName"  style="width:105px;"></g:select></td>
									</g:if>
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
								<th>Entity Type</th>
								<th>Name</th>
								<th>Type</th>
								<th>Status</th>
								<th>&nbsp;</th>
							</tr>
						</thead>
						<tbody id="createDependentsList">
						<g:each in="${dependentAssets}" var="dependent" status="i">
							<tr id='row_d_${i}'>
							${dependent?.dependent?.assetType}
								<td><g:select name="dataFlowFreq_dependent_${i}" value="${dependent.dataFlowFreq}" from="${dependent.constraints.dataFlowFreq.inList}" /></td>
								<td><g:select name="entity_dependent_${i}" from="['Server','Application','Database','Files']" onchange='updateAssetsList(this.name, this.value)' value="${dependent?.dependent?.assetType}"></g:select></td>
								<g:if test="${dependent?.dependent?.assetType=='Server'}">
								  <td><g:select name="asset_dependent_${i}" from="${com.tds.asset.AssetEntity.findAll('from AssetEntity where assetType in (\'Server\',\'VM\',\'Blade\') and project = ? order by assetName asc ',[project])}" value="${dependent?.dependent?.id}" optionKey="id" optionValue="assetName"  style="width:105px;"></g:select></td>
								</g:if>
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
				    <input name="dependentCount" id="dependentCount" type="hidden" value="${dependentAssets.size()}">
					<input  name="supportCount"  id="supportCount" type="hidden" value="${supportAssets.size()}">
					<input name="attributeSet.id" type="hidden" value="1">
					<input name="project.id" type="hidden" value="${projectId}">
					<input name="id" id ="assetId" type="hidden" value="${assetEntityInstance.id}">
					<input type = "hidden" id = "dstPath" name = "dstPath" value ="${redirectTo}"/>
					<input name="redirectTo" type="hidden" value="${redirectTo}">
					<input type = "hidden" id = "tabType" name="tabType" value =""/>
					<input type="hidden" id="labelsListId" name="labels" value =""/>
					<g:if test="${redirectTo!='planningConsole'}">
					  <span class="button"><g:actionSubmit class="save" value="Update"  /> </span>
					  <span class="button"><g:actionSubmit class="delete"	onclick=" return confirm('Are you sure?');" value="Delete" /> </span>
					</g:if>
					<g:else>
					  <span class="button"><input id="updatedId" name="updatedId" type="button" class="save" value="Update" onclick="submitRemoteForm()"> 
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
})
</script>