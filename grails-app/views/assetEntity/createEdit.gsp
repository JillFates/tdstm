<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="net.transitionmanager.asset.AssetType"%>
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
	<g:render template="/assetEntity/dependentHidden" />

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

	<div class="legacy-modal-dialog">
		<div class="legacy-modal-content">
			<%-- Header Content Here --%>
			<g:render template="/assetEntity/showHeader" model="[assetEntity:assetEntityInstance]"></g:render>
			<div id="modalBody" class="legacy-modal-body">
				<div class="legacy-modal-body-content">
					<div class="grid-form" id="details">

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.assetName}" value="${assetEntityInstance.assetName}"/>
							<tds:inputControl field="${standardFieldSpecs.assetName}" tabindex="100" value="${assetEntityInstance.assetName}" />
						</div>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.description}" value="${assetEntityInstance.description}"/>
							<tds:inputControl field="${standardFieldSpecs.description}" tabindex="101" value="${assetEntityInstance.description}" tooltipDataPlacement="bottom"/>
						</div>

						<div class="clr-form-control">
							<label class="clr-control-label ${standardFieldSpecs.assetType.imp?:''}" for="model">
								<span id="assetTypeLabel" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.assetType.tip?: standardFieldSpecs.assetType.label}">
									${standardFieldSpecs.assetType.label}
								</span>
							</label>
							<div id="modelEditId" style="flex:1;" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.assetType.tip?: standardFieldSpecs.assetType.label}">
								<div id="assetTypeSelect" tabindex="102">
								</div>
							</div>
						</div>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.environment}" value="${assetEntityInstance.environment}"/>
							<div class="clr-control-container">
  								<div class="clr-select-wrapper">
									<g:select class="clr-select" id="environment" name="environment" from="${environmentOptions}"
											value="${assetEntityInstance.environment}" noSelection="${['':'Please select...']}"
											tabindex="205"/>		
								</div>
							</div>
						</div>
						
						<%-- SOURCE --%>
						<div class="source-target-wrapper">
							<label class="header-label ${standardFieldSpecs.locationSource.imp?:''}">Source</label>
							<div class="clr-form-control useRoomS">
								<label class="clr-control-label ${standardFieldSpecs.locationSource.imp?:''}" for="locationSourceId">Location/Room</label>
								<div class="clr-control-container">
									<div class="clr-select-wrapper">
										<g:select class="clr-select" id="roomSelectS" name="roomSourceId"
												from="${sourceRoomSelect}" value="${assetEntityInstance.roomSource?.id}"
												optionKey="id" optionValue="${{it.value}}"
												noSelection="${[0:'Please select...']}"
												onchange="EntityCrud.updateOnRoomSelection(this, 'S', 'Edit')"
												tabindex="300" />	
									</div>
								</div>
							</div>
						
							<%-- Theses fields are used to allow user to create a source room on the fly --%>		
							<div class="newRoomS" style="display:none" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.locationSource.tip?: standardFieldSpecs.locationSource.label}">
								<div class="clr-form-control">
								<label class="clr-control-label ${standardFieldSpecs.locationSource.imp?:''}">Location Name</label>
									<div class="clr-control-container">
										<div class="clr-input-wrapper">
											<input type="text" id="locationSourceId" name="locationSource" value=""
												placeholder="Location"
												class="clr-input"
												tabindex="301"/>
										</div>
									</div>
								</div>
								<div class="clr-form-control">
									<label class="clr-control-label ${standardFieldSpecs.locationSource.imp?:''}">RoomName</label>
									<div class="clr-control-container">
										<div class="clr-input-wrapper">
											<input type="text" id="roomSourceId" name="roomSource" value=""
												placeholder="Room Name"
												class="clr-input"
												tabindex="302"/>
										</div>
									</div>
								</div>
							</div>

							<div class="clr-form-control">
								<label class="clr-control-label ${standardFieldSpecs.rackSource.imp?:''}" for="rackSourceId" data-toggle="popover" data-trigger="hover" data-content="Rack/Cabinet" >Rack/Cabinet</label>
									
								<div class="clr-control-container">
  									<div class="clr-select-wrapper">
										<g:render template="deviceRackSelect" model="[options:sourceRackSelect, rackId:assetEntityInstance?.rackSource?.id,
											rackDomId:'rackSourceId', rackDomName:'rackSourceId', sourceTarget:'S', forWhom:'Edit', tabindex:'310']" />
									</div>
								</div>	
							</div>

							<div style="display:none" class="newRackS">
								<div class="clr-form-control">
									<label class="clr-control-label ${standardFieldSpecs.rackSource.imp?:''}">Rack Name</label>
									<div class="clr-control-container">
										<div class="clr-input-wrapper">
											<input type="text" id="rackSourceId" name="rackSource" value=""
												placeholder="New Rack Name"
												class="clr-input"
												tabindex="311"/>
											<input type="hidden" id="newRackSourceId" name="newRackSourceId" value="-1">
										</div>
									</div>
								</div>
							</div>

							<div class="useBladeS" style="display:none">
								<div class="clr-form-control">
									<label class="clr-control-label ${standardFieldSpecs.sourceChassis.imp?:''}" for="sourceChassisSelectId">Chassis</label>
									<div class="clr-control-container">
										<div class="clr-select-wrapper">
											<g:render template="deviceChassisSelect"
													model="[ domId:'sourceChassisSelectId', domName:'sourceChassis',
															options:sourceChassisSelect, value:assetEntityInstance.sourceChassis?.id,
															sourceTarget:'S', forWhom:'$forWhom', tabindex:'312']"/>
										</div>
									</div>
								</div>
								<div class="clr-form-control">
									<label class="clr-control-label ${standardFieldSpecs.sourceBladePosition.imp?:''}">Chassis Position</label>
									<div class="clr-control-container">
										<div class="clr-input-wrapper">
										<input type="number" id="sourceBladePositionId" name="sourceBladePosition"
											value="${assetEntityInstance.sourceBladePosition}"
											placeholder="Chassis Position"
											class="clr-input"
											tabindex="320"/>
										</div>
									</div>
								</div>
							</div>

							<div class="useRackS" style="display:none">
								<div class="clr-form-control">
									<label class="clr-control-label ${standardFieldSpecs.sourceRackPosition.imp?:''}" for="sourceRackPositionId" data-toggle="popover" data-trigger="hover" data-content="Position">Position</label>
									<div class="clr-control-container">
										<div class="clr-input-wrapper">
											<input type="number" id="sourceRackPositionId" name="sourceRackPosition"
												value="${assetEntityInstance.sourceRackPosition}"
												placeholder="Position"
												class="clr-input"
												tabindex="320"/>
										</div>
									</div>
								</div>
							</div>
						</div>

						<%-- TARGET --%>
						<div class="source-target-wrapper">
							<label class="header-label ${standardFieldSpecs.locationTarget.imp?:''}">Target</label>
							
							<div class="clr-form-control useRoomT">
								<label class="clr-control-label ${standardFieldSpecs.locationTarget.imp?:''}" for="locationTargetId">Location/Room</label>
								<div class="clr-control-container">
									<div class="clr-select-wrapper">
										<g:select class="clr-select" id="roomSelectT" name="roomTargetId"
											from="${targetRoomSelect}" value="${assetEntityInstance.roomTarget?.id}"
											optionKey="id" optionValue="${{it.value}}"
											noSelection="${[0:'Please select...']}"
											onchange="EntityCrud.updateOnRoomSelection(this, 'T', 'Edit')"
											tabindex="330"/>
									</div>
								</div>
							</div>

							<%-- Theses fields are used to allow user to create a source room on the fly --%>
							<div class="newRoomT" style="display:none" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.locationTarget.tip?: standardFieldSpecs.locationTarget.label}">
								<div class="clr-form-control">
								<label class="clr-control-label ${standardFieldSpecs.locationTarget.imp?:''}">Location Name</label>
									<div class="clr-control-container">
										<div class="clr-input-wrapper">
											<input type="text" id="locationTargetId" name="locationTarget" value=""
												placeholder="Location"
												class="clr-input"
												tabindex="331"/>
										</div>
									</div>
								</div>
								<div class="clr-form-control">
									<label class="clr-control-label ${standardFieldSpecs.locationTarget.imp?:''}">RoomName</label>
									<div class="clr-control-container">
										<div class="clr-input-wrapper">
											<input type="text" id="roomTargetId" name="roomTarget" value=""
												placeholder="Room Name"
												class="clr-input"
												tabindex="332"/>
										</div>
									</div>
								</div>
							</div>

							<div class="clr-form-control">
								<label class="clr-control-label ${standardFieldSpecs.rackTarget.imp?:''}" for="rackTargetId" data-toggle="popover" data-trigger="hover" data-content="Rack/Cabinet" >Rack/Cabinet</label>
								<div class="clr-control-container">
  									<div class="clr-select-wrapper">
										<g:render template="deviceRackSelect"  model="[options:targetRackSelect, rackId: assetEntityInstance.rackTarget?.id,
											rackDomId:'rackTargetId', rackDomName:'rackTargetId', sourceTarget:'T', forWhom:'Edit', tabindex:'340']" />
									</div>
								</div>	
							</div>

							<div style="display:none" class="newRackT">
								<div class="clr-form-control">
									<label class="clr-control-label ${standardFieldSpecs.rackTarget.imp?:''}">Rack Name</label>
									<div class="clr-control-container">
										<div class="clr-input-wrapper">
											<input type="text" id="rackTargetId" name="rackTarget" value=""
												placeholder="New rack name"
												class="clr-input"
												tabindex="311"/>
											<input type="hidden" id="newRackTargetId" name="newRackTargetId" value="-1">
										</div>
									</div>
								</div>
							</div>

							<div class="useBladeT" style="display:none">
								<div class="clr-form-control">
									<label class="clr-control-label ${standardFieldSpecs.targetChassis.imp?:''}" for="targetChassisSelectId">Chassis</label>
									<div class="clr-control-container">
										<div class="clr-select-wrapper">
											<g:render template="deviceChassisSelect"
												model="[domId:'targetChassisSelectId', domName:'targetChassis',
														options:targetChassisSelect, value:assetEntityInstance.targetChassis?.id,
														sourceTarget:'T', forWhom:'$forWhom', tabindex:'342']"/>
										</div>
									</div>
								</div>
								<div class="clr-form-control">
									<label class="clr-control-label ${standardFieldSpecs.targetBladePosition.imp?:''}">Chassis Position</label>
									<div class="clr-control-container">
										<div class="clr-input-wrapper">
											<input type="number" id="targetRackPositionId" name="targetBladePosition"
												value="${assetEntityInstance.targetBladePosition}"
												placeholder="Chassis Position"
												class="clr-input"
												tabindex="350"/>
										</div>
									</div>
								</div>
							</div>

							<div class="useRackT" style="display:none">
								<div class="clr-form-control">
									<label class="clr-control-label ${standardFieldSpecs.targetRackPosition.imp?:''}" for="targetRackPositionId" data-toggle="popover" data-trigger="hover" data-content="Position">Position</label>
									<div class="clr-control-container">
										<div class="clr-input-wrapper">
											<input type="number" id="targetRackPositionId" name="targetRackPosition"
												value="${assetEntityInstance.targetRackPosition}"
												placeholder="Position"
												class="clr-input"
												tabindex="355"/>
										</div>
									</div>
								</div>
							</div>
						</div>
						
						<div class="clr-form-control">
							<g:if test="${assetEntityInstance.manufacturer?.id}">
								<label class="clr-control-label ${standardFieldSpecs.manufacturer.imp?:''}" for="manufacturer" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.manufacturer.tip?: standardFieldSpecs.manufacturer.label}">
									<a href='javascript:showManufacturer(${assetEntityInstance.manufacturer?.id})' style='color:#00E'>Manufacturer</a>
								</label>
							</g:if>
							<g:else>
								<label class="clr-control-label ${standardFieldSpecs.manufacturer.imp?:''}" for="manufacturer" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.manufacturer.tip?: standardFieldSpecs.manufacturer.label}">
									${standardFieldSpecs.manufacturer.label}
								</label>
							</g:else>
							<div id="manufacturerEditId" style="display:inline; flex:1;" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.manufacturer.tip?: standardFieldSpecs.manufacturer.label}">
								<div id="manufacturerSelect" tabindex="103">
								</div>
							</div>
						</div>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.priority}" value="${assetEntityInstance.priority}"/>
							<div class="clr-control-container">
  								<div class="clr-select-wrapper">
									<g:select class="clr-select" id="priority" name ="priority"
											from="${priorityOption}" value= "${assetEntityInstance.priority}" noSelection="${['':'Please select...']}"
											tabindex="210"
											/>
								</div>
							</div>							
						</div>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.model}" value="${assetEntityInstance.model}"/>
							<div id="modelSelect" style="flex:1;" tabindex="104">
							</div>
							<input type="hidden" value="${assetEntityInstance?.model?.id}" id="hiddenModel" name="model">
						</div>

						<tds:inputLabelAndField field="${standardFieldSpecs.ipAddress}" value="${assetEntityInstance.ipAddress}"data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.ipAddress.tip ?: standardFieldSpecs.ipAddress.label}" tabindex="215"/>


							<tds:inputLabelAndField field="${standardFieldSpecs.shortName}" value="${assetEntityInstance.shortName}" tabindex="105"/>
							<tds:inputLabelAndField field="${standardFieldSpecs.os}" value="${assetEntityInstance.os}" tabindex="220"/>

							<%-- Note that the next set of TDs are toggled on/off based on the assetType selected --%>


						<tds:inputLabelAndField field="${standardFieldSpecs.serialNumber}" value="${assetEntityInstance.serialNumber}" tabindex="106"/>
						<tds:inputLabelAndField field="${standardFieldSpecs.supportType}" value="${assetEntityInstance.supportType}" tabindex="225"/>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.moveBundle}" value="${assetEntityInstance.moveBundle?.id}"/>
							<div class="clr-control-container">
  								<div class="clr-select-wrapper">
									<g:select class="clr-select" from="${moveBundleList}" id="moveBundle" name="moveBundle.id"
											value="${assetEntityInstance.moveBundle?.id}" optionKey="id" optionValue="name"
											tabindex="360"
											/>									
								</div>
							</div>

						</div>

						<div class="clr-form-control">
							<label for="size" class="clr-control-label ${standardFieldSpecs.size.imp?:''}" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.size.tip?:standardFieldSpecs.size.label}">
								${standardFieldSpecs.size.label}/${standardFieldSpecs.scale.label}
							</label>
							<tds:inputControl field="${standardFieldSpecs.size}" tabindex="14" value="${assetEntityInstance.size}"/>

							<div class="clr-control-container" style="padding-left: 30px;">
  								<div class="clr-select-wrapper">
									<g:select class="clr-select" id="scale" name="scale"
										from="${com.tdssrc.grails.GormUtil.getConstrainedProperties(assetEntityInstance.class).scale.inList}"
										optionValue="value" noSelection="${['':'Please select...']}"
										value="${assetEntityInstance.scale}"
										tabindex="412"/>
								</div>
							</div>
						</div>

						<tds:inputLabelAndField field="${standardFieldSpecs.assetTag}" value="${assetEntityInstance.assetTag}" tabindex="107"/>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.retireDate}" value="${assetEntityInstance.retireDate}"/>
							<script type="text/javascript" charset="utf-8">
								jQuery(function($){ $(".dateRange").kendoDatePicker({ animation: false, format:tdsCommon.kendoDateFormat()  }); });
							</script>
							<input type="text" id="retireDate" name="retireDate"
								value="<tds:convertDate date="${assetEntityInstance?.retireDate}" />"
								class="dateRange ${standardFieldSpecs.retireDate.imp?:''}"
								style="width: 138px;"
								tabindex="230"/>
						</div>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.planStatus}" value="${assetEntityInstance.planStatus}"/>
							<div class="clr-control-container">
  								<div class="clr-select-wrapper">
									<g:select class="clr-select" id="planStatus" name ="planStatus"
											from="${planStatusOptions}" value= "${assetEntityInstance.planStatus}"
											noSelection="${['':'Please select']}"
											tabindex="365"
											/>
								</div>
							</div>

						</div>

						<tds:inputLabelAndField field="${standardFieldSpecs.rateOfChange}" value="${assetEntityInstance.rateOfChange}" tabindex="420" tooltipDataPlacement="bottom"/>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.railType}" value="${assetEntityInstance.railType}"/>
							<div class="clr-control-container">
  								<div class="clr-select-wrapper">
									<g:select class="clr-select" id="railType" name ="railType"
											from="${railTypeOption}" value= "${assetEntityInstance.railType}"
											noSelection="${['':'Please select...']}"
											tabindex="108"/>
								</div>
							</div>

						</div>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${assetEntityInstance.maintExpDate}"/>
							<input type="text" id="maintExpDate" name="maintExpDate"
								value="<tds:convertDate date="${assetEntityInstance?.maintExpDate}" />"
								class="dateRange ${standardFieldSpecs.maintExpDate.imp?:''}"
								tabindex="235"/>
						</div>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.validation}" value="${assetEntityInstance.validation}"/>
							<div class="clr-control-container">
  								<div class="clr-select-wrapper">
									<g:select class="clr-select" id="validation" name="validation"
										from="${com.tdssrc.grails.GormUtil.getConstrainedProperties(assetEntityInstance.class).validation.inList}"
										value="${assetEntityInstance.validation}"
										tabindex="370"/>
								</div>
							</div>

						</div>

						<tds:inputLabelAndField field="${standardFieldSpecs.externalRefId}" value="${assetEntityInstance.externalRefId}" tabindex="109"/>

						<div class="clr-form-control">
							<label class="clr-control-label ${standardFieldSpecs.truck.imp?:''}" for="truck" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.truck.tip?: standardFieldSpecs.truck.label}">Truck/Cart/Shelf</label>
							<tds:inputControl id="truck" field="${standardFieldSpecs.truck}" tabindex="101" name="truck" value="${assetEntityInstance.truck}" tabindex="240"/>
							<tds:inputControl id="cart" field="${standardFieldSpecs.cart}" tabindex="101" name="cart" value="${assetEntityInstance.cart}" tabindex="241"/>
							<tds:inputControl id="shelf" field="${standardFieldSpecs.shelf}" tabindex="101" name="shelf" value="${assetEntityInstance.shelf}" tabindex="242"/>
							
							<%-- <input type="text" id="truck" class="${standardFieldSpecs.truck.imp?:''}" name="truck" value="${assetEntityInstance.truck}" tabindex="240" />
							<input type="text" id="cart" class="${standardFieldSpecs.cart.imp?:''}" name="cart" value="${assetEntityInstance.cart}" tabindex="241" />
							<input type="text" id="shelf" class="${standardFieldSpecs.shelf.imp?:''}" name="shelf" value="${assetEntityInstance.shelf}" tabindex="242" /> --%>
						</div>

						<g:render template="customEdit"></g:render>
					</div>
					
					<g:render template="/comment/assetTagsEdit"></g:render>

					<table class="ui-widget asset-entities-dialog-table-content">
						<g:if test="${action == 'save'}">
							<tr>
								<g:render template="dependentCreateEdit" model="[whom:'create', supportAssets:[], dependentAssets:[]]"></g:render>
							</tr>
						</g:if><g:else>
						<tr id="assetDependentId" class="assetDependent">
							<td class="depSpin"><span><asset:image src="images/processing.gif"/> </span></td>
						</tr>
						</g:else>
					</table>
				</div>
			</div>
		</div>
		<g:render template="/assetEntity/editButtons" model="[assetEntity:assetEntityInstance]"></g:render>
	</div>
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
