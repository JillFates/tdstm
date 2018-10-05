<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="com.tds.asset.AssetType"%>
<%@page import="net.transitionmanager.security.Permission"%>

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
	<input type="hidden" id="asset_locationSource" 	name="locationSourceFilter" value="" />
	<input type="hidden" id="asset_sourceRack" 		name="sourceRackFilter" value="" />
	<input type="hidden" id="asset_targetLocation" 	name="targetLocationFilter" value="" />
	<input type="hidden" id="asset_rackTarget" 		name="rackTargetFilter" value="" />

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

	<table style="border:0;width:1000px;" class="ui-widget asset-entities-dialog-table-content">
		<tr>
			<td colspan="2">
				<div class="dialog">
					<table>
						<tbody>
							<tr>
								<tds:inputLabel field="${standardFieldSpecs.assetName}" value="${assetEntityInstance.assetName}"/>
								<td colspan="3">
									<tds:inputControl field="${standardFieldSpecs.assetName}" tabindex="100" value="${assetEntityInstance.assetName}" />
								</td>

								<tds:inputLabel field="${standardFieldSpecs.description}" value="${assetEntityInstance.description}"/>
								<td colspan="3">
									<tds:inputControl field="${standardFieldSpecs.description}" tabindex="101" value="${assetEntityInstance.description}" tooltipDataPlacement="bottom"/>
								</td>
							</tr>
							<tr>
								<td class="label ${standardFieldSpecs.assetType.imp?:''}" nowrap="nowrap">
									<label for="model">
										<span id="assetTypeLabel" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.assetType.tip?: standardFieldSpecs.assetType.label}">
											${standardFieldSpecs.assetType.label}
										</span>
									</label>
								</td>
								<td class="${standardFieldSpecs.assetType.imp?:''}" data-for="model" style="border-top: 1px solid #BBBBBB; border-left: 1px solid #BBBBBB; border-right: 1px solid #BBBBBB;">
									<div id="modelEditId" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.assetType.tip?: standardFieldSpecs.assetType.label}">
										<div id="assetTypeSelect" tabindex="102">
										</div>
									</div>
								</td>

								<tds:inputLabel field="${standardFieldSpecs.environment}" value="${assetEntityInstance.environment}"/>
								<td>
								<tds:tooltipSpan field="${standardFieldSpecs.environment}">
									<g:select id="environment" name="environment" class="${standardFieldSpecs.environment.imp?:''}" from="${environmentOptions}"
											  value="${assetEntityInstance.environment}" noSelection="${['':'Please select...']}"
											  tabindex="205"
											/>
								</tds:tooltipSpan>
								</td>
								<td colspan="1"></td>
								<td class="label_sm">Source</td>
								<td class="label_sm">Target</td>
							</tr>
							<tr>
								<td class="label ${standardFieldSpecs.manufacturer.imp?:''}" nowrap="nowrap">
									<g:if test="${assetEntityInstance.manufacturer?.id}">
										<label for="manufacturer" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.manufacturer.tip?: standardFieldSpecs.manufacturer.label}">
											<a href='javascript:showManufacturer(${assetEntityInstance.manufacturer?.id})' style='color:#00E'>Manufacturer</a>
										</label>
									</g:if>
									<g:else>
										<label for="manufacturer" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.manufacturer.tip?: standardFieldSpecs.manufacturer.label}">
											${standardFieldSpecs.manufacturer.label}
										</label>
									</g:else>
								</td>
								<td class="${standardFieldSpecs.manufacturer.imp?:''}" data-for="manufacturer" style="border-left: 1px solid #BBBBBB; border-right: 1px solid #BBBBBB;">
									<div id="manufacturerEditId" style="display:inline" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.manufacturer.tip?: standardFieldSpecs.manufacturer.label}">
										<div id="manufacturerSelect" tabindex="103">
										</div>
									</div>
								</td>

								<tds:inputLabel field="${standardFieldSpecs.priority}" value="${assetEntityInstance.priority}"/>
								<td>
								<tds:tooltipSpan field="${standardFieldSpecs.priority}">
									<g:select id="priority" name ="priority"
											  from="${priorityOption}" value= "${assetEntityInstance.priority}" noSelection="${['':'Please select...']}"
											  class="${standardFieldSpecs.priority.imp?:''}" tabindex="210"
											/>
								</tds:tooltipSpan>
								</td>
								<td class="label ${standardFieldSpecs.locationSource.imp?:''}" nowrap="nowrap">
									<label for="locationSourceId">Location/Room</label>
								</td>
								<td class="${standardFieldSpecs.locationSource.imp?:''}" style="vertical-align: text-top;" data-for="locationSourceId">
										<tds:tooltipSpan class="useRoomS" field="${standardFieldSpecs.locationSource}">
											<g:select id="roomSelectS"  name="roomSourceId"
													  from="${sourceRoomSelect}" value="${assetEntityInstance.roomSource?.id}"
													  optionKey="id" optionValue="${{it.value}}"
													  noSelection="${[0:'Please select...']}"
													  class="${standardFieldSpecs.locationSource.imp?:''} assetSelect roomSelectS"
													  onchange="EntityCrud.updateOnRoomSelection(this, 'S', 'Edit')"
													  tabindex="300"
													/>
										</tds:tooltipSpan>
									<%-- Theses fields are used to allow user to create a source room on the fly --%>
										<span class="newRoomS" style="display:none" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.locationSource.tip?: standardFieldSpecs.locationSource.label}">
											<input type="text" id="locationSourceId" name="locationSource" value=""
												   placeholder="Location"
												   class="${standardFieldSpecs.locationSource.imp?:''}"
												   size=10
												   tabindex="301"
													/>
											<input type="text" id="roomSourceId" name="roomSource" value=""
												   placeholder="Room Name"
												   class="${standardFieldSpecs.roomSource.imp?:''}"
												   size=10
												   tabindex="302"
													/>
										</span>
								</td>
								<td nowrap style="vertical-align: text-top;" class="${standardFieldSpecs.locationTarget.imp?:''}" data-for="locationSourceId">
										<tds:tooltipSpan class="useRoomT" field="${standardFieldSpecs.locationTarget}">
											<g:select id="roomSelectT" name="roomTargetId"
													  from="${targetRoomSelect}" value="${assetEntityInstance.roomTarget?.id}"
													  optionKey="id" optionValue="${{it.value}}"
													  noSelection="${[0:'Please select...']}"
													  class="${standardFieldSpecs.locationTarget.imp?:''} assetSelect roomSelectT"
													  onchange="EntityCrud.updateOnRoomSelection(this, 'T', 'Edit')"
													  tabindex="330"
													/>
										</tds:tooltipSpan>
									<%-- Theses fields are used to allow user to create a source room on the fly --%>
										<span class="newRoomT" style="display:none" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.locationTarget.tip?: standardFieldSpecs.locationTarget.label}">
											<br/>
											<input type="text" id="locationTargetId" name="locationTarget" value=""
												   placeholder="Location"
												   class="${standardFieldSpecs.locationTarget.imp?:''}"
												   size=10 tabindex="331"
													/>
											<input type="text" id="roomTargetId" name="roomTarget" value=""
												   placeholder="Room Name"
												   class="${standardFieldSpecs.roomTarget.imp?:''}"
												   size=10 tabindex="332"
													/>
										</span>
								</td>

							</tr>
							<tr>
								<tds:inputLabel field="${standardFieldSpecs.model}" value="${assetEntityInstance.model}"/>
								<td class="${standardFieldSpecs.model.imp?:''}" data-for="assetType"  style="border-bottom: 1px solid #BBBBBB; border-left: 1px solid #BBBBBB; border-right: 1px solid #BBBBBB;">
								<tds:tooltipSpan field="${standardFieldSpecs.model}">
									<div id="modelSelect" tabindex="104">
									</div>
									<input type="hidden" value="${assetEntityInstance?.model?.id}" id="hiddenModel" name="model">
								</tds:tooltipSpan>
								</td>

								<td class="label ${standardFieldSpecs.ipAddress.imp?:''}" nowrap="nowrap">
									<label for="ipAddress" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.ipAddress.tip ?: standardFieldSpecs.ipAddress.label}">
										${standardFieldSpecs.ipAddress.label}
									</label>
								</td>
								<td>
									<tds:tooltipSpan field="${standardFieldSpecs.ipAddress}">
										<input type="text" id="ipAddress" name="ipAddress"
											   value="${assetEntityInstance.ipAddress}"
											   class="${standardFieldSpecs.ipAddress.imp?:''}" tabindex="215"
												/>
									</tds:tooltipSpan>
								</td>

								<td class="label rackLabel ${standardFieldSpecs.rackSource.imp?:''}" nowrap="nowrap" id="rackId">
									<label for="rackSourceId" data-toggle="popover" data-trigger="hover" data-content="Rack/Cabinet" >Rack/Cabinet</label>
								</td>
								<td class="label bladeLabel ${standardFieldSpecs.sourceChassis.imp?:''}" nowrap="nowrap" id="bladeId" style="display:none">
									<label for="sourceChassisId" data-toggle="popover" data-trigger="hover" data-content="Blade Chassis">Blade Chassis</label>
								</td>

								<td class="label rackLabel ${standardFieldSpecs.rackSource.imp?:''}" data-for="rackSourceId">
									<tds:tooltipSpan class="useRackS" field="${standardFieldSpecs.rackSource}" tooltipDataPlacement="bottom">
										<g:render template="deviceRackSelect" model="[clazz:standardFieldSpecs.rackSource.imp?:'', options:sourceRackSelect, rackId:assetEntityInstance?.rackSource?.id,
																					  rackDomId:'rackSourceId', rackDomName:'rackSourceId', sourceTarget:'S', forWhom:'Edit', tabindex:'310']" />
									</tds:tooltipSpan>
									<tds:tooltipSpan class="newRackS" tooltipDataPlacement="bottom" field="${standardFieldSpecs.rackSource}">
										<input type="text" id="rackSourceId" name="rackSource" value=""
											   placeholder="New rack name"
											   class="${standardFieldSpecs.rackSource.imp?:''}"
											   xstyle="display:none"
											   size=20 tabindex="311"
										/>
										<input type="hidden" id="newRackSourceId" name="newRackSourceId" value="-1">
									</tds:tooltipSpan>
								</td>
								<td class="label rackLabel ${standardFieldSpecs.rackTarget.imp?:''}" data-for="rackSourceId">
									<tds:tooltipSpan class="useRackT" tooltipDataPlacement="bottom" field="${standardFieldSpecs.rackTarget}">
										<g:render template="deviceRackSelect"  model="[clazz:standardFieldSpecs.rackTarget.imp?:'', options:targetRackSelect, rackId: assetEntityInstance.rackTarget?.id,
																					   rackDomId:'rackTargetId', rackDomName:'rackTargetId', sourceTarget:'T', forWhom:'Edit', tabindex:'340']" />
									</tds:tooltipSpan>
									<tds:tooltipSpan class="newRackT" tooltipDataPlacement="bottom" field="${standardFieldSpecs.rackTarget}">
										<input type="text" id="rackTargetId" name="rackTarget" value=""
											   placeholder="New rack name"
											   class="${standardFieldSpecs.rackTarget.imp?:''}"
											   xstyle="display:none"
											   size=20 tabindex="341" />
										<input type="hidden" id="newRackTargetId" name="newRackTargetId" value="-1">
									</tds:tooltipSpan>
								</td>

								<td class="label bladeLabel" style="display:none">
									<tds:tooltipSpan class="useBladeS" tooltipDataPlacement="bottom" field="${standardFieldSpecs.sourceChassis}">
										<g:render template="deviceChassisSelect"
												  model="[ domId:'sourceChassisSelectId', domName:'sourceChassis',
														   options:sourceChassisSelect, value:assetEntityInstance.sourceChassis?.id,
														   domClass: standardFieldSpecs.sourceChassis.imp?:'',
														   sourceTarget:'S', forWhom:'$forWhom', tabindex:'312']"
										/>
									</tds:tooltipSpan>
								</td>
								<td class="label bladeLabel" style="display:none">
									<tds:tooltipSpan class="useBladeT" tooltipDataPlacement="bottom" field="${standardFieldSpecs.targetChassis}">
										<g:render template="deviceChassisSelect"
												  model="[ domId:'targetChassisSelectId', domName:'targetChassis',
														   options:targetChassisSelect, value:assetEntityInstance.targetChassis?.id,
														   domClass: standardFieldSpecs.targetChassis.imp?:'',
														   sourceTarget:'T', forWhom:'$forWhom', tabindex:'342']"
										/>
									</tds:tooltipSpan>
								</td>


							</tr>
							<tr>
								<tds:inputLabelAndField field="${standardFieldSpecs.shortName}" value="${assetEntityInstance.shortName}" tabindex="105"/>

								<tds:inputLabelAndField field="${standardFieldSpecs.os}" value="${assetEntityInstance.os}" tabindex="220"/>

								<%-- Note that the next set of TDs are toggled on/off based on the assetType selected --%>
								<td class="label positionLabel ${standardFieldSpecs.sourceRackPosition.imp?:''}" nowrap="nowrap">
									<label for="sourceRackPositionId" data-toggle="popover" data-trigger="hover" data-content="Position">Position</label>
								</td>
								<td class="rackLabel">
									<tds:tooltipSpan class="sourceRackPositionT" tooltipDataPlacement="bottom" field="${standardFieldSpecs.sourceRackPosition}">
										<input type="text" id="sourceRackPositionId" name="sourceRackPosition"
											   value="${assetEntityInstance.sourceRackPosition}"
											   placeholder="U position"
											   class="${standardFieldSpecs.sourceRackPosition.imp?:''} useRackS"
											   size=10 tabindex="320"
										/>
									</tds:tooltipSpan>
								</td>
								<td class="rackLabel" data-toggle="popover" data-trigger="hover" data-placement="bottom" data-content="${standardFieldSpecs.targetRackPosition.tip?: standardFieldSpecs.targetRackPosition.label}">
									<input type="text" id="targetRackPositionId" name="targetRackPosition"
										   value="${assetEntityInstance.targetRackPosition}"
										   placeholder="U position"
										   class="${standardFieldSpecs.targetRackPosition.imp?:''} useRackT"
										   targetRackPosition tabindex="350" />
								</td>
								<td class="bladeLabel ${standardFieldSpecs.sourceRackPosition.imp?:''}" data-toggle="popover" data-placement="bottom" data-trigger="hover" data-content="${standardFieldSpecs.sourceBladePosition.tip?: standardFieldSpecs.sourceBladePosition.label}">
									<input type="text" id="sourceBladePositionId" name="sourceBladePosition"
										   value="${assetEntityInstance.sourceBladePosition}"
										   placeholder="Chassis position"
										   class="${standardFieldSpecs.sourceRackPosition.imp?:''} useBladeS"
										   size=10 tabindex="320"
									/>
								</td>
								<td class="bladeLabel" data-toggle="popover" data-trigger="hover" data-placement="bottom" data-content="${standardFieldSpecs.targetBladePosition.tip?: standardFieldSpecs.targetBladePosition.label}">
									<input type="text" id="targetRackPositionId" name="targetBladePosition"
										   value="${assetEntityInstance.targetBladePosition}"
										   placeholder="Chassis position"
										   class="${standardFieldSpecs.targetRackPosition.imp?:''} useBladeT"
										   size=10 tabindex="350"
									/>
								</td>

							</tr>
							<tr>
								<tds:inputLabelAndField field="${standardFieldSpecs.serialNumber}" value="${assetEntityInstance.serialNumber}" tabindex="106"/>

								<tds:inputLabelAndField field="${standardFieldSpecs.supportType}" value="${assetEntityInstance.supportType}" tabindex="225"/>

								<tds:inputLabel field="${standardFieldSpecs.moveBundle}" value="${assetEntityInstance.moveBundle?.id}"/>
								<td>
								<tds:tooltipSpan field="${standardFieldSpecs.moveBundle}">
									<g:select from="${moveBundleList}" id="moveBundle" name="moveBundle.id"
											  value="${assetEntityInstance.moveBundle?.id}" optionKey="id" optionValue="name"
											  class="${standardFieldSpecs.moveBundle.imp?:''}"
											  tabindex="360"
											/>
								</tds:tooltipSpan>
								</td>

								<tds:inputLabel field="${standardFieldSpecs.size}" value="${assetEntityInstance.size}" />
								<td nowrap="nowrap" class="sizeScale">
								<tds:tooltipSpan tooltipDataPlacement="bottom" field="${standardFieldSpecs.size}">
									<input type="number" min="0"  id="size" name="size" class="${standardFieldSpecs.size.imp?:''}" value="${assetEntityInstance.size}" tabindex="410"/>
									<g:select id="scale" name="scale"
											  from="${assetEntityInstance.constraints.scale.inList}"
											  optionValue="value" noSelection="${['':'Please select...']}"
											  value="${assetEntityInstance.scale}"
											  class="${standardFieldSpecs.scale.imp?:''}"
											  tabindex="412"
											/>
								</tds:tooltipSpan>
								</td>
							</tr>
							<tr>
								<tds:inputLabelAndField field="${standardFieldSpecs.assetTag}" value="${assetEntityInstance.assetTag}" tabindex="107"/>

								<tds:inputLabel field="${standardFieldSpecs.retireDate}" value="${assetEntityInstance.retireDate}"/>
								<td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'retireDate','errors')}">
									<tds:tooltipSpan field="${standardFieldSpecs.retireDate}">
										<script type="text/javascript" charset="utf-8">
											jQuery(function($){ $(".dateRange").kendoDatePicker({ animation: false, format:tdsCommon.kendoDateFormat()  }); });
										</script>
										<input type="text" id="retireDate" name="retireDate"
											value="<tds:convertDate date="${assetEntityInstance?.retireDate}" />"
											class="dateRange ${standardFieldSpecs.retireDate.imp?:''}"
											size="15" style="width: 138px;"
											tabindex="230"
										/>
									</tds:tooltipSpan>
								</td>

								<tds:inputLabel field="${standardFieldSpecs.planStatus}" value="${assetEntityInstance.planStatus}"/>
								<td>
								<tds:tooltipSpan field="${standardFieldSpecs.planStatus}">
									<g:select id="planStatus" name ="planStatus"
											  from="${planStatusOptions}" value= "${assetEntityInstance.planStatus}"
											  noSelection="${['':'Please select']}"
											  class="${standardFieldSpecs.planStatus.imp?:''}"
											  tabindex="365"
											/>
								</tds:tooltipSpan>
								</td>

								<tds:inputLabelAndField field="${standardFieldSpecs.rateOfChange}" value="${assetEntityInstance.rateOfChange}" tabindex="420" tooltipDataPlacement="bottom"/>
							</tr>
							<tr>
								<tds:inputLabel field="${standardFieldSpecs.railType}" value="${assetEntityInstance.railType}"/>
								<td>
								<tds:tooltipSpan field="${standardFieldSpecs.railType}">
									<g:select id="railType" name ="railType"
											  from="${railTypeOption}" value= "${assetEntityInstance.railType}"
											  noSelection="${['':'Please select...']}"
											  class="${standardFieldSpecs.railType.imp?:''}"
											  tabindex="108"/>
								</tds:tooltipSpan>
								</td>

								<tds:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${assetEntityInstance.maintExpDate}"/>
								<td valign="top" class="value ${hasErrors(bean:assetEntityInstance,field:'maintExpDate','errors')}">
								<tds:tooltipSpan field="${standardFieldSpecs.maintExpDate}">
									<input type="text" id="maintExpDate" name="maintExpDate"
										value="<tds:convertDate date="${assetEntityInstance?.maintExpDate}" />"
										class="dateRange ${standardFieldSpecs.maintExpDate.imp?:''}"
										size="15" style="width: 138px;"
										tabindex="235"
									/>
								</tds:tooltipSpan>
								</td>

								<tds:inputLabel field="${standardFieldSpecs.validation}" value="${assetEntityInstance.validation}"/>
								<td  colspan="2">
									<tds:tooltipSpan field="${standardFieldSpecs.validation}">
										<g:select id="validation" name="validation"
											  from="${assetEntityInstance.constraints.validation.inList}"
											  value="${assetEntityInstance.validation}"
											  class="${standardFieldSpecs.validation.imp?:''}"
											  tabindex="370"
											/>
									</tds:tooltipSpan>
								</td>
							</tr>
							<tr>
								<tds:inputLabelAndField field="${standardFieldSpecs.externalRefId}" value="${assetEntityInstance.externalRefId}" tabindex="109"/>

								<td class="label ${standardFieldSpecs.truck.imp?:''}" nowrap="nowrap">
									<label for="truck" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.truck.tip?: standardFieldSpecs.truck.label}">Truck/Cart/Shelf</label>
								</td>
								<td>
									<tds:tooltipSpan field="${standardFieldSpecs.truck}">
										<input type="text" id="truck" class="${standardFieldSpecs.truck.imp?:''}" name="truck" value="${assetEntityInstance.truck}" size=3 tabindex="240" />
										<input type="text" id="cart" class="${standardFieldSpecs.cart.imp?:''}" name="cart" value="${assetEntityInstance.cart}" size=3 tabindex="241" />
										<input type="text" id="shelf" class="${standardFieldSpecs.shelf.imp?:''}" name="shelf" value="${assetEntityInstance.shelf}" size=2 tabindex="242" />
									</tds:tooltipSpan>
								</td>
							</tr>
							<tbody class="customTemplate">
								<g:render template="customEdit"></g:render>
							</tbody>

							<g:render template="/comment/assetTagsEdit"></g:render>
						</tbody>
					</table>
				</div>
			</td>
		</tr>



		<g:if test="${action == 'save'}">
			<tr>
				<g:render template="dependentCreateEdit" model="[whom:'create', supportAssets:[], dependentAssets:[]]"></g:render>
			</tr>
		</g:if><g:else>
		<tr id="assetDependentId" class="assetDependent">
			<td class="depSpin"><span><asset:image src="images/processing.gif"/> </span></td>
		</tr>
	</g:else>

		<tr>
			<td colspan="2">
				<div class="buttons">
					 <span class="button">
					 	<input type="button" class="save updateDep" data-redirect='${redirectTo}' data-action='show'
							   value="${actionLabel}" onclick="EntityCrud.${jsAction}($(this), '${assetEntityInstance.assetClass}'); " />
					 </span>
					<tds:hasPermission permission="${Permission.AssetDelete}">
						 <span class="button"><g:actionSubmit class="delete"
															  onclick=" return confirm('You are about to delete selected asset for which there is no undo. Are you sure? Click OK to delete otherwise press Cancel');" value="Delete" /> </span>
					</tds:hasPermission>
					<span class="button"><input type="button" class="cancel" value="Cancel" onclick="$('#createEntityView').dialog('close'); $('#showEntityView').dialog('close'); $('#editEntityView').dialog('close');"/> </span>
                    <input type="submit" id="assetUpdateSubmit" style="display:none;">
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
        $('[data-toggle="popover"]').popover();

        // TM-7943 - mozilla browser based hack-fix for this particular scenario when displaying tooltip popover w/ select2 component.
        if (isMozillaBrowser) {
            $('.select2-offscreen').each(function () {
                $(this).on('select2-open', function () {
                    $('div.popover').hide();
                });
            });
        }

        EntityCrud.loadAssetTags();

    })(jQuery);

</script>