<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="com.tds.asset.AssetType"%>

<%-- Set some vars based on the action being save or update --%>
<g:set var="actionLabel" value="${action == 'save' ? 'Save' : 'Update'}" />
<g:set var="jsAction" value="${action=='save' ? 'saveToShow' : 'performAssetUpdate'}" />

<g:form method="post" name="createEditAssetForm" action="${action}">

	<%-- TODO : JPM 10/2014 - Need to determine if the filter params are necessary--%>
	<%-- 
		These properties are some how used by the JQGrid logic for filtering. I am unsure if they are really 
		necessary as we use user selectable columns as well and we do not have any references for them 
	--%>
	<input type="hidden" id="asset_assetName" 		name="assetNameFilter" value="" />
	<input type="hidden" id="asset_assetTag" 		name="assetTagFilter" value="" />
	<input type="hidden" id="asset_assetType" 		name="assetTypeFilter" value="" />
	<input type="hidden" id="asset_model" 			name="modelFilter" value="" />
	<input type="hidden" id="asset_moveBundle" 		name="moveBundleFilter" value="" />
	<input type="hidden" id="asset_planStatus" 		name="planStatusFilter" value="" />
	<input type="hidden" id="asset_serialNumber" 	name="serialNumberFilter" value="" />
	<input type="hidden" id="asset_sourceLocation" 	name="sourceLocationFilter" value="" />
	<input type="hidden" id="asset_sourceRack" 		name="sourceRackFilter" value="" />
	<input type="hidden" id="asset_targetLocation" 	name="targetLocationFilter" value="" />
	<input type="hidden" id="asset_targetRack" 		name="targetRackFilter" value="" />

	<%-- Flow control parameters --%>
	<input type="hidden" id="dstPath" 				name="dstPath" value ="${redirectTo}"/>
	<input type="hidden" id="redirectTo" 			name="redirectTo" value="${redirectTo}">

	<%-- Key field and optimistic locking var --%>
	<input type="hidden" id="assetId" 				name="id" value="${assetEntityInstance?.id}"/>
	<input type="hidden" id="version" 				name="version" value="${version}"/>

	<input type="hidden" id="attributeSet.id"		name="attributeSet.id" 	value="1"/>

	<%-- Used to maintain the selected AssetType --%>
	<input type="hidden" id="currentAssetType" 		name="currentAssetType" value="${currentAssetType}"/>

	<%-- Used to track dependencies added and deleted --%>
	<g:render template="../assetEntity/dependentHidden" />

	<%-- Holds original values of the various SELECTS --%>
	<input type="hidden" id="hiddenModel"        name="modelId" value="${assetEntityInstance?.model?.id}">
	<input type="hidden" id="hiddenManufacturer" name="manufacturerId" value="${assetEntityInstance.manufacturer?.id}">
	<input type="hidden" id="deviceChassisIdS" value="${assetEntityInstance?.sourceChassis?.id}"/>
	<input type="hidden" id="deviceChassisIdT" value="${assetEntityInstance?.targetChassis?.id}"/>
	<input type="hidden" id="deviceRackIdS" value="${assetEntityInstance?.rackSource?.id}"/>
	<input type="hidden" id="deviceRackIdT" value="${assetEntityInstance?.rackTarget?.id}"/>
	<input type="hidden" id="deviceRoomIdS" value="${assetEntityInstance?.roomSource?.id}"/>
	<input type="hidden" id="deviceRoomIdT" value="${assetEntityInstance?.roomTarget?.id}"/>

	<%-- Not sure what these are used for (jpm 9/2014) --%>
	<%-- TODO : JPM 9/2014 : Note that the fields with id containing dot (.) can not be referenced by JQuery $('#project.id') therefore it begs the question if they are used/needed --%>
	<input type="hidden" id="project.id"			name="project.id" value="${projectId}"/>
	<input type="hidden" id="labelsListId" 			name="labels" value =""/>
	<input type="hidden" id="tabType" 				name="tabType" value =""/>
	<input type="hidden" id="updateView" 			name="updateView" value="">
	<input type="hidden" id="updateViewId" 			name="updateViewId" value=""/>

	<table style="border:0;width:1000px;" class="ui-widget">
		<tr>
			<td colspan="2">
				<div class="dialog">
					<table>
						<tbody>
							<tr>
								<td class="label ${config.assetName}" nowrap="nowrap">
									<label for="assetName">Name*</label>
								</td>
								<td>
									<input type="text" id="assetName" name="assetName" class="${config.assetName} ${highlightMap.validation?:''}" 
										value="${quotelessName}" tabindex="100" />
								</td>
								<td class="label ${config.description} ${highlightMap.description?:''}" nowrap="nowrap">
									<label for="description">Description</label></td>
								<td colspan="2">
									<input type="text" id="description" class="${config.description}" name="description" 
										value="${assetEntityInstance.description}" tabindex="101" />
								</td>
								<td class="label_sm">Source</td>
								<td class="label_sm">Target</td>
							</tr>
							<tr>
								<td class="label ${config.manufacturer} ${highlightMap.manufacturer?:''}" nowrap="nowrap">
									<g:if test="${assetEntityInstance.manufacturer?.id}">
										<label for="manufacturer"><a href='javascript:showManufacturer(${assetEntityInstance.manufacturer?.id})' style='color:#00E'>Manufacturer</a></label>
									</g:if>
									<g:else>
										<label for="manufacturer">Manufacturer</label>
									</g:else>	
								</td>
								<td style="border-top: 1px solid #BBBBBB; border-left: 1px solid #BBBBBB; border-right: 1px solid #BBBBBB;">
									<div id="manufacturerEditId" style="display:inline">
										<div id="manufacturerSelect" tabindex="102">
										</div>
									</div>
									<img src="/tdstm/i/filter-unset.png" />
								</td>
								<td class="label ${config.environment} ${highlightMap.environment?:''}" nowrap="nowrap">
									<label for="environment">Environment</label>
								</td>
								<td>
									<g:select id="environment" name="environment" class="${config.environment}" from="${environmentOptions}" 
										value="${assetEntityInstance.environment}" noSelection="${['':'Please select...']}" 
										tabindex="205"
									/>
								</td>
								<td class="label ${config.sourceLocation} ${highlightMap.sourceLocation?:''}" nowrap="nowrap">
									<label for="sourceLocationId">Location/Room</label>
								</td>
								<td >
									<span class="useRoomS">
										<g:select id="roomSelectS" name="roomSourceId"
											from="${sourceRoomSelect}" value="${assetEntityInstance.roomSource?.id}" 
											optionKey="id" optionValue="${{it.value}}"
											noSelection="${[0:'Please select...']}" 
											class="${config.sourceLocation} assetSelect roomSelectS" 
											onchange="EntityCrud.updateOnRoomSelection(this, 'S', 'Edit')" 
											tabindex="300"
										/>
									</span>
									<%-- Theses fields are used to allow user to create a source room on the fly --%>
									<span class="newRoomS" style="display:none">
										<input type="text" id="sourceLocationId" name="sourceLocation" value="" 
											placeholder="Location" 
											class="${config.sourceLocation}"
											size=10 
											tabindex="301"
										/>
										<input type="text" id="sourceRoomId" name="sourceRoom" value="" 
											placeholder="Room Name" 
											class="${config.sourceRoom}" 
											size=10 
											tabindex="302"
										/>
									</span>
								</td>
								<td nowrap>
									<span class="useRoomT">
										<g:select id="roomSelectT" name="roomTargetId"
											from="${targetRoomSelect}" value="${assetEntityInstance.roomTarget?.id}" 
											optionKey="id" optionValue="${{it.value}}"
											noSelection="${[0:'Please select...']}" 
											class="${config.targetLocation} assetSelect roomSelectT" 
											onchange="EntityCrud.updateOnRoomSelection(this, 'T', 'Edit')" 
											tabindex="330" 
										/>
									</span>
									<%-- Theses fields are used to allow user to create a source room on the fly --%>
									<span class="newRoomT" style="display:none">
										<br/>
										<input type="text" id="targetLocationId" name="targetLocation" value="" 
											placeholder="Location" 
											class="${config.targetLocation}"
											size=10 tabindex="331"
										/>
										<input type="text" id="targetRoomId" name="targetRoom" value="" 
											placeholder="Room Name" 
											class="${config.targetRoom}" 
											size=10 tabindex="332"
										/>
									</span>
								</td>

							</tr>
							<tr>
								<td class="label ${config.model} ${highlightMap.model?:''}" nowrap="nowrap">
									<label for="model"><div id="assetTypeLabel">Device Type</div></label>
								</td>
								<td style="border-left: 1px solid #BBBBBB; border-right: 1px solid #BBBBBB;">
									<div id="modelEditId">
										<div id="assetTypeSelectContainer" style="display:inline">
											<select id="assetTypeSelect" name="assetType" onchange="setType(this.value, '${forWhom}')" style="width:120px" tabindex="103">
	    										<option></option>
												<g:each in="${assetTypeOptions}" var="assetType">
													<option ${ (assetType == assetEntityInstance.assetType ? 'selected ' : '') } value="${assetType}" >${assetType}</option>
												</g:each>
											</select>
										</div>
										<img src="/tdstm/i/filter-unset.png" />
									</div>
								</td>
								<td class="label ${config.priority} ${highlightMap.priority?:''}" nowrap="nowrap">
									<label for="priority">Priority</label>
								</td>
								<td>
									<g:select id="priority" name ="priority" 
										from="${priorityOption}" value= "${assetEntityInstance.priority}" noSelection="${['':'Please select...']}" 
										class="${config.priority}" tabindex="210"
									/>
								</td>
								<td class="label rackLabel ${config.sourceRack} ${highlightMap.sourceRack?:''}" nowrap="nowrap" id="rackId">
									<label for="sourceRackId">Rack/Cabinet</label>
								</td>
								<td class="label bladeLabel ${config.sourceChassis} ${highlightMap.sourceChassis?:''}" nowrap="nowrap" id="bladeId" style="display:none">
									<label for="sourceChassisId">Blade Chassis</label>
								</td>

								<td class="label rackLabel">
									<span class="useRackS">
										<g:render template="deviceRackSelect" model="[clazz:config.sourceRack, options:sourceRackSelect, rackId:assetEntityInstance.rackSource?.id, 
											rackDomId:'rackSourceId', rackDomName:'rackSourceId', sourceTarget:'S', forWhom:'Edit', tabindex:'310']" />
									</span>
									<span class="newRackS">
										<input type="text" id="sourceRackId" name="sourceRack" value=""  
											placeholder="New rack name" 
											class="${config.sourceRack}"
											xstyle="display:none"
											size=20 tabindex="311" 
										/>
										<input type="hidden" id="newRackSourceId" name="newRackSourceId" value="-1">
									</span>
								</td>
								<td class="label rackLabel">
									<span class="useRackT">
										<g:render template="deviceRackSelect"  model="[clazz:config.targetRack, options:targetRackSelect, rackId: assetEntityInstance.rackTarget?.id,
											rackDomId:'rackTargetId', rackDomName:'rackTargetId', sourceTarget:'T', forWhom:'Edit', tabindex:'340']" />
									</span>
									<span class="newRackT">
										<input type="text" id="targetRackId" name="targetRack" value=""  
										placeholder="New rack name" 
										class="${config.targetRack}"
										xstyle="display:none"
										size=20 tabindex="341" />
										<input type="hidden" id="newRackTargetId" name="newRackTargetId" value="-1">
									</span>
								</td>
								
								<td class="label bladeLabel" style="display:none">
									<span class="useBladeS">
										<g:render template="deviceChassisSelect"
											model="[ domId:'sourceChassisSelectId', domName:'sourceChassis', 
												options:sourceChassisSelect, value:assetEntityInstance.sourceChassis?.id,
												domClass: config.sourceChassis, 
												sourceTarget:'S', forWhom:'$forWhom', tabindex:'312']"
										/>
									</span>
								</td>
								<td class="label bladeLabel" style="display:none">
									<span class="useBladeT">
										<g:render template="deviceChassisSelect"
											model="[ domId:'targetChassisSelectId', domName:'targetChassis',
												options:targetChassisSelect, value:assetEntityInstance.targetChassis?.id, 
												domClass: config.targetChassis, 
												sourceTarget:'T', forWhom:'$forWhom', tabindex:'342']"
										/>
									</span>
								</td>

							</tr>
							<tr>
								<td class="label ${config.assetType} ${highlightMap.assetType?:''}" nowrap="nowrap">
									<label for="assetType">Model</label>
								</td>
								<td style="border-bottom: 1px solid #BBBBBB; border-left: 1px solid #BBBBBB; border-right: 1px solid #BBBBBB;">
									<div id="modelSelect" tabindex="104">
									</div>
									<input type="hidden" value="${assetEntityInstance?.model?.id}" id="hiddenModel" name="model">
								</td>
								<td class="label ${config.ipAddress} ${highlightMap.ipAddress?:''}" nowrap="nowrap"><label for="ipAddress">IP1</label></td>
								<td>
									<input type="text" id="ipAddress" name="ipAddress" 
										value="${assetEntityInstance.ipAddress}" 
										class="${config.ipAddress}" tabindex="215"
									/>
								</td>

								<%-- Note that the next set of TDs are toggled on/off based on the assetType selected --%>
								<td class="label positionLabel ${config.sourceRackPosition} ${highlightMap.sourceRackPosition?:''}" nowrap="nowrap">
									<label for="sourceRackPositionId">Position</label>
								</td>
								<td class="rackLabel">
									<input type="text" id="sourceRackPositionId" name="sourceRackPosition" 
										value="${assetEntityInstance.sourceRackPosition}" 
										placeholder="U position"
										class="${config.sourceRackPosition} useRackS"
										size=10 tabindex="320" 
									/>
								</td>
								<td class="rackLabel">
									<input type="text" id="targetRackPositionId" name="targetRackPosition"
										value="${assetEntityInstance.targetRackPosition}" 
										placeholder="U position"
										class="${config.targetRackPosition} useRackT"
										size=10 tabindex="350" />
								</td>
								<td class="bladeLabel ${config.sourceBladePosition} ${highlightMap.sourceBladePosition?:''}">
									<input type="text" id="sourceBladePositionId" name="sourceBladePosition" 
										value="${assetEntityInstance.sourceBladePosition}" 
										placeholder="Chassis position"
										class="${config.sourceBladePosition} useBladeS"
										size=10 tabindex="320"
									/>
								</td>
								<td class="bladeLabel">
									<input type="text" id="targetBladePositionId" name="targetBladePosition" 
										value="${assetEntityInstance.targetBladePosition}" 
										placeholder="Chassis position"
										class="${config.targetBladePosition} useBladeT"
										size=10 tabindex="350" 
									/>
								</td>
								
							</tr>
							<tr>
								<td class="label ${config.shortName} ${highlightMap.shortName?:''}" nowrap="nowrap">
									<label for="shortName">Alt Name</label>
								</td>
								<td>
									<input type="text" id="shortName" name="shortName" 
										value="${assetEntityInstance.shortName}" 
										class="${config.shortName}"
										tabindex="105"
									/>
								</td>
								<td class="label ${config.os} ${highlightMap.os?:''}" nowrap="nowrap"><label for="os">OS</label></td>
								<td>
									<input type="text" id="os" name="os" class="${config.os}" value="${assetEntityInstance.os}" tabindex="220"/>
								</td>
								<td class="label ${config.moveBundle} ${highlightMap.moveBundle?:''}" nowrap="nowrap">
									<label for="moveBundle">Bundle</label>
								</td>
								<td>
									<g:select from="${moveBundleList}" id="moveBundle" name="moveBundle.id" 
										value="${assetEntityInstance.moveBundle?.id}" optionKey="id" optionValue="name" 
										class="${config.moveBundle}" 
										tabindex="360"
									/>
								</td>
								<td class="label ${config.size} ${highlightMap.size?:''}" nowrap="nowrap">
									<label for="size">Size/Scale </label>
								</td>
								<td nowrap="nowrap" class="sizeScale">
									<input type="text" id="size" name="size" class="${config.size}" value="${assetEntityInstance.size}" tabindex="410"/>
									<g:select id="scale" name="scale" 
										from="${assetEntityInstance.constraints.scale.inList}" 
										optionValue="value" noSelection="${['':'Please select...']}"
										value="${assetEntityInstance.scale}" 
										class="${config.scale}" 
										tabindex="412" 
									/>
								</td>  
							</tr>
							<tr>
								<td class="label ${config.serialNumber} ${highlightMap.serialNumber?:''}" nowrap="nowrap">
									<label for="serialNumber">Serial #</label>
								</td>
								<td>
									<input type="text" id="serialNumber" name="serialNumber" 
										class="${config.serialNumber}"
										value="${assetEntityInstance.serialNumber}" tabindex="106"/>
								</td>
								<td class="label ${config.supportType} ${highlightMap.supportType?:''}" nowrap="nowrap">
									<label for="supportType">Support Type</label>
								</td>
								<td ><input type="text" id="supportType" name="supportType" class="${config.supportType}"
									value="${assetEntityInstance.supportType}" tabindex="225"/>
								</td>
								<td class="label ${config.planStatus} ${highlightMap.planStatus?:''}" nowrap="nowrap">
									<label for="planStatus">Plan Status</label>
								</td>
								<td>
									<g:select id="planStatus" name ="planStatus"
										from="${planStatusOptions}" value= "${assetEntityInstance.planStatus}" 
										noSelection="${['':'Please select']}" 
										class="${config.planStatus}" 
										tabindex="365"
									/>
								</td>
								<td class="label ${config.rateOfChange} ${highlightMap.validation?:''}" nowrap="nowrap">
									<label for="rateOfChange">Rate of Change (%)</label>
								</td>
                                <td>
                                	<input type="text" name="rateOfChange" id="rateOfChange" 
										value="${assetEntityInstance.rateOfChange}" 
	                                	class="${config.rateOfChange}" size="3" 
	                                	tabindex="420">
                                </td>
							</tr>
							<tr>
								<td class="label ${config.assetTag} ${highlightMap.assetTag?:''}" nowrap="nowrap">
									<label for="assetTag">Tag</label>
								</td>
								<td>
									<input type="text" id="assetTag" class="${config.assetTag}" name="assetTag" value="${assetEntityInstance.assetTag}" tabindex="107"/>
								</td>
								<td class="label ${config.retireDate} ${highlightMap.retireDate?:''}">
									<label for="retireDate">Retire Date:</label>
								</td>
								<td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'retireDate','errors')}">
								    <script type="text/javascript" charset="utf-8">
				                    	jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${resource(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
				                    </script>
				                    <input type="text" id="retireDate" name="retireDate"  
										value="<tds:convertDate date="${assetEntityInstance?.retireDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />" 
					                    class="dateRange ${config.retireDate}" 
					                    size="15" style="width: 112px; height: 14px;" 
					                    tabindex="230"
									/> 
								</td>
								<td class="label ${config.validation} ${highlightMap.validation?:''}"><label for="validation">Validation</label></td>
								<td  colspan="2">
									<g:select id="validation" name="validation" 
										from="${assetEntityInstance.constraints.validation.inList}" 
										value="${assetEntityInstance.validation}" 
										onChange="assetFieldImportance(this.value,'AssetEntity');highlightCssByValidation(this.value,'AssetEntity','${assetEntityInstance.id}');" 
										class="${config.validation}"
										tabindex="370"
									/>	
								</td>
							</tr>
							<tr>
								<td class="label ${config.railType}  ${highlightMap.railType?:''}" nowrap="nowrap">
									<label for="railType">Rail Type</label>
								</td>
								<td>
									<g:select id="railType" name ="railType" 
										from="${railTypeOption}" value= "${assetEntityInstance.railType}" 
										noSelection="${['':'Please select...']}"
										class="${config.railType}"  
										tabindex="108"/>
									</td>
								<td  class="label ${config.maintExpDate}  ${highlightMap.maintExpDate?:''}">
									<label for="maintExpDate">Maint Exp.</label>
								</td>
								<td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'maintExpDate','errors')}">
									<script type="text/javascript" charset="utf-8">
					                	jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${resource(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
					                </script>
					                <input type="text" id="maintExpDate" name="maintExpDate"
										value="<tds:convertDate date="${assetEntityInstance?.maintExpDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>" 
										class="dateRange ${config.maintExpDate}" 
										size="15" style="width: 112px; height: 14px;" 
										tabindex="235"
									/> 
								</td>
								
							</tr>
							<tr>
								<td class="label ${config.externalRefId} ${highlightMap.externalRefId?:''}" nowrap="nowrap">
									<label for="externalRefId">External Ref Id</label>
								</td>
								<td>
									<input type="text" id="externalRefId" class="${config.externalRefId}" name="externalRefId" value="${assetEntityInstance.externalRefId}" tabindex="109" />
								</td>
								<td class="label ${config.truck} ${highlightMap.truck?:''}" nowrap="nowrap">
									<label for="truck">Truck/Cart/Shelf</label>
								</td>
								<td>
									<input type="text" id="truck" class="${config.truck}" name="truck" value="${assetEntityInstance.truck}" size=3 tabindex="240" />
									<input type="text" id="cart" class="${config.cart}" name="cart" value="${assetEntityInstance.cart}" size=3 tabindex="241" />
									<input type="text" id="shelf" class="${config.shelf}" name="shelf" value="${assetEntityInstance.shelf}" size=2 tabindex="242" />
								</td>
							</tr>
							<tbody class="customTemplate">
							<g:render template="customEdit"></g:render>
							</tbody>
						</tbody>
					</table>
				</div></td>
		</tr>

		<g:if test="${action == 'save'}">
		<tr>
			<g:render template="dependentCreateEdit" model="[whom:'create', supportAssets:[], dependentAssets:[]]"></g:render>
		</tr>
		</g:if><g:else>
		<tr id="assetDependentId" class="assetDependent">
			<td class="depSpin"><span><img alt="" src="${resource(dir:'images',file:'processing.gif')}"/> </span></td>
		</tr>
		</g:else>

		<tr>
			<td colspan="2">
				<div class="buttons">
					 <span class="button">
					 	<input type="button" class="save updateDep" data-redirect='${redirectTo}' data-action='show' 
					 		value="${actionLabel}" onclick="EntityCrud.${jsAction}($(this), '${assetEntityInstance.assetClass}'); " /> 
					 </span>
					 <tds:hasPermission permission='AssetDelete'>
						 <span class="button"><g:actionSubmit class="delete" 
						 	onclick=" return confirm('You are about to delete selected asset for which there is no undo. Are you sure? Click OK to delete otherwise press Cancel');" value="Delete" /> </span>
					 </tds:hasPermission>
					 <span class="button"><input type="button" class="cancel" value="Cancel" onclick="$('#createEntityView').dialog('close'); $('#showEntityView').dialog('close'); $('#editEntityView').dialog('close');"/> </span>
				</div>
			</td>
		</tr>
	</table>
</g:form>

<script type="text/javascript">
// Run when Document Ready and JQuery available
( function($) {
	//$(document).ready( function() {
		currentMenuId = "#assetMenu";
		$("#assetMenuId a").css('background-color','#003366');
		$('#tabType').val($('#assetTypesId').val());

		var labelsList = " "
		$('#labelTree input:checked').each(function() {
			labelsList += $(this).val()+',';
		});
		$('#labelsListId').val(labelsList)

		// This appear to be related to the dependency map...
		$('#distanceId').val($('#distance').val())
		$('#frictionId').val($('#friction').val())
		$('#forceId').val($('#force').val())

		var assetType = EntityCrud.getAssetType();
		EntityCrud.toggleAssetTypeFields( assetType );
		EntityCrud.loadFormFromJQGridFilters();

		EntityCrud.initializeUI("${assetEntityInstance?.model?.id}", "${modelName}", "${assetEntityInstance.manufacturer?.id}", "${assetEntityInstance.manufacturer?.name}");

		<g:if test="${action == 'update'}">
			EntityCrud.setManufacturerValues("${assetEntityInstance.model?.id}", "${assetEntityInstance.model?.modelName}", "${assetEntityInstance.assetType}", "${assetEntityInstance.manufacturer?.id}", "${assetEntityInstance.manufacturer?.name}");
		</g:if>
		

		// Ajax to populate dependency selects in edit pages
		var assetId = '${assetEntityInstance.id}'
		populateDependency(assetId, 'asset', 'edit')

		if(!isIE7OrLesser)
			$("select.assetSelect").select2();

})(jQuery);

</script>