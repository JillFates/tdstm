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
		var assetType = $("#assetTypeEditId").val()
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

		// Ajax to populate dependency selects in edit pages
		var assetId = '${assetEntityInstance.id}'
		populateDependency(assetId, 'asset')

		 $(".assetSelect").combobox();
	})
</script>

<g:form method="post" name="editAssetsFormId"  action="update">

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
	<table style="border:0;width:1000px;" class="ui-widget">
		<tr>
			<td colspan="2">
				<div class="dialog">
					<table>
						<tbody>
							<tr>
								<td class="label ${config.assetName}" nowrap="nowrap"><label for="assetName">Name</label></td>
								<td><input type="text" id="assetName" name="assetName" class="${config.assetName}" title="gagdjgajgdjgs" value="${assetEntityInstance.assetName}" tabindex="11" /></td>
								<td class="label ${config.environment}" nowrap="nowrap"><label for="environment">Environment</label></td>
								<td><g:select id="environment" name="environment" class="${config.environment}" from="${assetEntityInstance.constraints.environment.inList}" value="${assetEntityInstance.environment}"  tabindex="32"></g:select></td>
								<td>&nbsp</td>
								<td class="label_sm">Source</td>
								<td class="label_sm">Target</td>
							</tr>
							<tr>
								<td class="label ${config.assetType}" nowrap="nowrap"><label for="assetType">Type</label></td>
								<td ><g:select from="${assetTypeOptions}"  class="${config.assetType} assetSelect" id="assetTypeEditId" name="assetType"  value="${assetEntityInstance.assetType}" onChange="selectManufacturer(this.value, 'Edit')" tabindex="12"/></td>
								<td class="label ${config.priority}" nowrap="nowrap"><label for="priority">Priority</label>
								</td>
								<td ><g:select id="priority" name ="priority" class="${config.priority}" from="${priorityOption}" value= "${assetEntityInstance.priority}" noSelection="${['':' Please Select']}" tabindex="21"/>
								</td>
								<td class="label ${config.sourceLocation}" nowrap="nowrap"><label for="sourceLocationId">Location</label></td>
								<td><input type="text" id="sourceLocationId"
									name="sourceLocation" class="${config.sourceLocation}" value="${assetEntityInstance.sourceLocation}" size=10 tabindex="31"/></td>
									<td><input type="text" id="targetLocationId"
									name="targetLocation" class="${config.targetLocation}" value="${assetEntityInstance.targetLocation}" size=10 tabindex="41"/></td>
							</tr>
							<tr>
								<td class="label ${config.manufacturer}" nowrap="nowrap">
									
									<g:if test="${assetEntityInstance.manufacturer?.id}">
										<label for="manufacturer"><a href='javascript:showManufacturer(${assetEntityInstance.manufacturer?.id})' style='color:#00E'>Manufacturer</a></label>
									</g:if>
									<g:else>
										<label for="manufacturer">Manufacturer</label>
									</g:else>	
								</td>
								 <td >
								 <div id="manufacturerEditId">
								   <g:select id="manufacturer" name="manufacturer.id" class="${config.manufacturer} assetSelect" from="${manufacturers}" value="${assetEntityInstance.manufacturer?.id}" onChange="selectModel(this.value,'Edit')" optionKey="id" optionValue="name" noSelection="${[null:'Unassigned']}" tabindex="13"/>
								 </div>
								</td>
								<td class="label ${config.ipAddress}" nowrap="nowrap"><label for="ipAddress">IP1</label></td>
								<td ><input type="text" id="ipAddress" name="ipAddress" class="${config.ipAddress}"
									value="${assetEntityInstance.ipAddress}" tabindex="22"/>
								</td>
								<td class="label ${config.sourceRoom}" nowrap="nowrap"><label for="sourceRoomId">Room</label></td>
								<td><input type="text" id="sourceRoomId" class="${config.sourceRoom}"
									name="sourceRoom" value="${assetEntityInstance.roomSource?.roomName}" size=10 tabindex="32"/></td>
									<td><input type="text" id="targetRoomId" class="${config.targetRoom}"
									name="targetRoom" value="${assetEntityInstance.roomTarget?.roomName}" size=10 tabindex="42" /></td>
							</tr>
							<tr>
								<td class="label ${config.model}" nowrap="nowrap">
									<g:if test="${assetEntityInstance.model?.id}">
										<label for="model"><a href='javascript:showModel(${assetEntityInstance.model?.id})' style='color:#00E'>Model</a></label>
									</g:if>
									<g:else>
										<label for="model">Model</label>
									</g:else>
								</td>
								<td>
								<div id="modelEditId">
								   <g:select id="model" class="${config.model} assetSelect" name ="model.id" from="${models}" value= "${assetEntityInstance.model?.id}" optionKey="id" optionValue="modelName"  noSelection="${[null:' Unassigned']}" tabindex="14"
								   optionValue="${{it.modelName+' '+(it.modelStatus =='new' || !it.modelStatus ? '?' :'')}}" onChange="setType(this.value, 'Edit')"/>
								 </div>
								</td>
								<td class="label ${config.os}" nowrap="nowrap"><label for="os">OS</label></td>
								<td ><input type="text" id="os" name="os" class="${config.os}" value="${assetEntityInstance.os}"  tabindex="24"/></td>
								<td class="label rackLabel ${config.sourceRack}"  nowrap="nowrap" id="rackId"><label for="sourceRackId">Rack/Cab</label></td>
								<td class="label bladeLabel ${config.sourceBladeChassis}" nowrap="nowrap" id="bladeId" style="display: none"><label for="sourceBladeChassisId">Blade</label></td>
								<td class="label vmLabel ${config.virtualHost}" style="display: none" class="label" nowrap="nowrap"><label for="virtualHost">Virtual Host</label>
								<td class="rackLabel"><input type="text" id="sourceRackId" class="${config.sourceRack}"
									name="sourceRack" value="${assetEntityInstance.sourceRack}" size=10 tabindex="33" /></td>
								<td class="rackLabel"><input type="text" id="targetRackId" class="${config.targetRack}"
									name="targetRack" value="${assetEntityInstance.targetRack}" size=10 tabindex="44" /></td>
								<td class="bladeLabel" style="display: none"><g:select id='sourceBladeChassis' class="${config.sourceBladeChassis}" from='${sourceChassisSelect}' optionKey='${-2}' optionValue='${1}'
									  name="sourceBladeChassis" value="${assetEntityInstance.sourceBladeChassis}" noSelection="${['':' Please Select']}"/></td>
								<td class="bladeLabel" style="display: none"><g:select id='targetBladeChassis' class="${config.targetBladeChassis}" from='${targetChassisSelect}' optionKey='${-2}' optionValue='${1}'
										name="targetBladeChassis"  value="${assetEntityInstance.targetBladeChassis}" noSelection="${['':' Please Select']}"/></td>
								<td class="vmLabel" style="display: none"><input type="text" id="virtualHost" class="${config.virtualHost}" name="virtualHost" value="${assetEntityInstance.virtualHost}" size=10 tabindex="37" /></td>
								<td class="vmLabel" style="display: none">&nbsp;</td>
								
							</tr>
							<tr>
								<td class="label ${config.shortName}" nowrap="nowrap"><label for="shortName">Alt Name</label></td>
								<td ><input type="text" id="shortName" class="${config.shortName}"
									name="shortName" value="${assetEntityInstance.shortName}" tabindex="15"/>
								</td>
								<td class="label ${config.supportType}" nowrap="nowrap"><label for="supportType">Support Type</label></td>
								<td ><input type="text" id="supportType" name="supportType" class="${config.supportType}"
									value="${assetEntityInstance.supportType}" tabindex="26"/>
								</td>
								
								<td class="label ${config.sourceRackPosition}" nowrap="nowrap"><label for="sourceRackPositionId">Position</label>
								<td class="rackLabel"><input type="text" id="sourceRackPositionId" class="${config.sourceRackPosition}"
									name="sourceRackPosition" value="${assetEntityInstance.sourceRackPosition}" size=10 tabindex="34" /></td>
								<td class="rackLabel"> <input type="text" id="targetRackPositionId" class="${config.targetRackPosition}"
									name="targetRackPosition" value="${assetEntityInstance.targetRackPosition}" size=10 tabindex="44" /></td>
								<td class="bladeLabel ${config.sourceBladePosition}"><input type="text" id="sourceBladePositionId" class="${config.sourceBladePosition}"
									name="sourceBladePosition" value="${assetEntityInstance.sourceBladePosition}" size=10 tabindex="36"/></td>
								<td class="bladeLabel"><input type="text" id="targetBladePositionId" class="${config.targetBladePosition}"
									name="targetBladePosition" value="${assetEntityInstance.targetBladePosition}" size=10 tabindex="46" /></td>	
								<td class="vmLabel">&nbsp;</td>
								<td class="vmLabel">&nbsp;</td>	
							</tr>
							<tr>
								<td class="label ${config.serialNumber}" nowrap="nowrap"><label for="serialNumber">Serial #</label></td>
								<td ><input type="text" id="serialNumber" class="${config.serialNumber}"
									name="serialNumber" value="${assetEntityInstance.serialNumber}" tabindex="16"/>
								</td>
								<td class="label ${config.retireDate}"><label for="retireDate">Retire Date:</label></td>
								<td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'retireDate','errors')}">
								    <script type="text/javascript" charset="utf-8">
				                    jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${resource(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
				                    </script> <input type="text" class="dateRange ${config.retireDate}" size="15" style="width: 112px; height: 14px;" name="retireDate" id="retireDate" tabindex="27"
									value="<tds:convertDate date="${assetEntityInstance?.retireDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" tabindex="27" />" > 
								</td>
								<td class="label ${config.moveBundle}" nowrap="nowrap"><label for="moveBundle">Bundle</label></td>
								<td colspan="2"><g:select from="${moveBundleList}" id="moveBundle" class="${config.moveBundle}" name="moveBundle.id" value="${assetEntityInstance.moveBundle?.id}" optionKey="id" optionValue="name" tabindex="38" noSelection="${['null':' Please Select']}"/></td>
							</tr>
							<tr>
								<td class="label ${config.assetTag}" nowrap="nowrap"><label for="assetTag">Tag</label></td>
								<td ><input type="text" id="assetTag" class="${config.assetTag}" name="assetTag" value="${assetEntityInstance.assetTag}" tabindex="17"/></td>
								<td  class="label ${config.maintExpDate}"><label for="maintExpDate">Maint Exp.</label></td>
								<td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'maintExpDate','errors')}">
									<script type="text/javascript" charset="utf-8">
					                	    jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${resource(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
					                	    </script> <input type="text" class="dateRange ${config.maintExpDate}" size="15" style="width: 112px; height: 14px;" name="maintExpDate" id="maintExpDate" tabindex="28"
									value="<tds:convertDate date="${assetEntityInstance?.maintExpDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>" > 
								</td>
								<td class="label ${config.planStatus}" nowrap="nowrap"><label for="planStatus">Plan Status</label></td>
								<td colspan="2"><g:select id="planStatus" class="${config.planStatus}" name ="planStatus" from="${planStatusOptions}" value= "${assetEntityInstance.planStatus}" noSelection="${['':' Please Select']}" tabindex="39"/></td>
							</tr>
							<tr>
								<td class="label ${config.railType}" nowrap="nowrap"><label for="railType">Rail Type</label></td>
								 <td ><g:select id="railType" class="${config.railType}" name ="railType" from="${railTypeOption}" value= "${assetEntityInstance.railType}" noSelection="${['':' Please Select']}" tabindex="64"/></td>
								<td class="label ${config.truck}" nowrap="nowrap"><label for="truck">Truck/Cart/Shelf</label></td>
								<td ><input type="text" id="truck" class="${config.truck}" name="truck" value="${assetEntityInstance.truck}" size=3 tabindex="61" />
								<input type="text" id="cart" class="${config.cart}" name="cart" value="${assetEntityInstance.cart}" size=3 tabindex="62" />
								<input type="text" id="shelf" class="${config.shelf}" name="shelf" value="${assetEntityInstance.shelf}" size=2 tabindex="63" /></td>
								<td class="label ${config.validation}"><label for="validation">Validation</label></td>
								<td  colspan="2"><g:select from="${assetEntityInstance.constraints.validation.inList}" id="validation" class="${config.validation}" name="validation" onChange="assetFieldImportance(this.value,'AssetEntity');" value="${assetEntityInstance.validation}"/>	
								</td>
							</tr>
							<tr>
								<td class="label ${config.externalRefId}" nowrap="nowrap"><label for="externalRefId">External Ref Id</label></td>
								<td><input type="text" id="externalRefId" class="${config.externalRefId}" name="externalRefId" value="${assetEntityInstance.externalRefId}" tabindex="11" /></td>
							</tr>
							<tbody class="customTemplate">
							<g:render template="customEdit" ></g:render>
							</tbody>
						</tbody>
					</table>
				</div></td>
		</tr>
		<tr id="assetDependentId">
			<td class="depSpin"><span><img alt="" src="${resource(dir:'images',file:'processing.gif')}"/> </span></td>
		</tr>
		<tr>
			<td colspan="2">
				<div class="buttons">
				    <input name="dependentCount" id="edit_dependentCount" type="hidden" value="${dependentAssets.size()}"/>
					<input  name="supportCount"  id="edit_supportCount" type="hidden" value="${supportAssets.size()}"/>
					<input name="attributeSet.id" type="hidden" value="1"/>
					<input name="project.id" type="hidden" value="${projectId}"/>
					<input name="id" id="assetId" type="hidden" value="${assetEntityInstance.id}"/>
					<input type = "hidden" id = "dstPath" name = "dstPath" value ="${redirectTo}"/>
					<input name="redirectTo" id="redirectTo" type="hidden" value="${redirectTo}">
					<input name="updateView" id="updateView" type="hidden" value="">
					<input type = "hidden" id = "tabType" name="tabType" value =""/>
					<input type="hidden" id="labelsListId" name="labels" value =""/>
					<input type="hidden" id="updateViewId" name="updateViewId" value =""/>
					<input type="hidden" id="deletedDepId" name="deletedDep" value =""/>
					
					<input type="hidden" id="edit_supportAddedId" name="addedSupport" value ="0"/>
					<input type="hidden" id="edit_dependentAddedId" name="addedDep" value ="0"/>
					
					<g:if test="${redirectTo!='dependencyConsole'}">
					  <g:if test="${redirectTo=='listTask'}">
					  	<span class="button"><input type="button" class="save updateDep" value="Update/Close" onclick="updateToRefresh()" /></span>
					  </g:if>
					  <g:else>
					  	<span class="button"><g:actionSubmit class="save updateDep" value="Update/Close" action="Update" /></span>
					  </g:else>
					  <span class="button"><input type="button" class="save updateDep" value="Update/View" onclick="updateToShow()" /> </span>
					  <span class="button"><g:actionSubmit class="delete"	onclick=" return confirm('Are you sure?');" value="Delete" /> </span>
					</g:if>
					<g:else>
					  
					  <span class="button"><input id="updatedId" name="updatedId" type="button" class="save updateDep" value="Update/Close" onclick="submitRemoteForm()"> </span>
					  <span class="button"><input type="button" class="save updateDep" value="Update/View" onclick="updateToShow()" /> </span>
					  <span class="button"><input id="deleteId"	name="deleteId" class="delete" value="Delete" onclick=" deleteAsset($('#assetId').val(),'server')" value="Delete" /> </span>
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
