<%@page import="net.transitionmanager.asset.Application"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%-- <g:set var="assetClass" value="${(new Application()).assetClass}" /> --%>
<g:set var="assetClass" value="Application" />

<style>
	#select2-drop{ width: 200px !important; }
</style>

<script type="text/javascript">
	$("#appl_assetName").val($('#gs_assetName').val())
	$("#appl_sme").val($('#gs_sme').val())
	$("#appl_validation").val($('#gs_validation').val())
	$("#appl_planStatus").val($('#gs_planStatus').val())
	$("#appl_moveBundle").val($('#gs_moveBundle').val())
	$(document).ready(function() {
		// Ajax to populate dependency selects in edit pages
		var assetId = '${applicationInstance.id}'
		populateDependency(assetId, 'application','edit')

		var myOption = "<option value='0'>Add Person...</option>"
		<tds:hasPermission permission="${Permission.PersonCreate}">
			$("#sme1 option:first").after(myOption);
			$("#sme2 option:first").after(myOption);
			$("#appOwnerEdit option:first").after(myOption);
		</tds:hasPermission>
		$("#shutdownByEditId").val('${raw(applicationInstance.shutdownBy)}')
		$("#startupByEditId").val('${raw(applicationInstance.startupBy)}')
		$("#testingByEditId").val('${raw(applicationInstance.testingBy)}')
		if(!isIE7OrLesser)
			$("select.assetSelect").select2();
		changeDocTitle('${raw(escapedName)}');
	})
</script>
<g:form method="post" action="update" name="createEditAssetForm" onsubmit="return validateFields('Edit',this.name)">

	<input type="hidden" id="appl_assetName" name="assetNameFilter" value="" />
	<input type="hidden" id="appl_sme" name="appSmeFilter" value="" />
	<input type="hidden" id="appl_validation" name="appValidationFilter" value="" />
	<input type="hidden" id="appl_moveBundle" name="moveBundleFilter" value="" />
	<input type="hidden" id="appl_planStatus" name="planStatusFilter" value="" />

	<%-- Key field and optimistic locking var --%>
	<input type="hidden" id="assetId" 	name="id" value="${assetId}"/>
	<input type="hidden" id="version" 	name="version" value="${version}"/>

	<input type="hidden" id="appId" value ="${applicationInstance.id}"/>
	<input type="hidden" id="dstPath" name = "dstPath" value ="${redirectTo}"/>
	<input type="hidden" id="tabType" name="tabType" value =""/>
	<input type="hidden" id="updateView" name="updateView" value=""/>

	<input type="hidden" name="id" value="${applicationInstance?.id}" />

	<%-- Used to track dependencies added and deleted --%>
	<g:render template="/assetEntity/dependentHidden" />
	<div class="legacy-modal-dialog">
		<div class="legacy-modal-content">
			<%-- Header Content Here --%>
			<g:render template="/assetEntity/showHeader" model="[assetEntity:applicationInstance]"></g:render>
			<div id="modalBody" class="legacy-modal-body">
				<div class="legacy-modal-body-content">
					<%-- New Summary Table --%>
					<div class="grid-form" id="details">						
						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.assetName}" value="${applicationInstance.assetName}" />
							<tds:inputControl field="${standardFieldSpecs.assetName}" tabindex="11" value="${applicationInstance.assetName}" />
						</div>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.description}" value="${applicationInstance.description}"/>
							<tds:inputControl field="${standardFieldSpecs.description}" tabindex="11" value="${applicationInstance.description}"/>
						</div>

						<tds:inputLabelAndField field="${standardFieldSpecs.appVendor}" value="${applicationInstance.appVendor}" tabindex="13"/>
						<tds:inputLabelAndField field="${standardFieldSpecs.supportType}" value="${applicationInstance.supportType}" tabindex="22"/>
						<tds:inputLabelAndField field="${standardFieldSpecs.appFunction}" value="${applicationInstance.appFunction}" tabindex="32"/>
						<tds:inputLabelAndField field="${standardFieldSpecs.userCount}" value="${applicationInstance.userCount}" tabindex="42"/>
						<tds:inputLabelAndField field="${standardFieldSpecs.appVersion}" value="${applicationInstance.appVersion}" tabindex="14"/>
						
						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.sme}" value="${applicationInstance.sme}"/>
							<div class="clr-control-container">
  								<div class="clr-select-wrapper">
									<g:select from="${personList}" id="sme1" name="sme.id" class="clr-select" optionKey="personId"
										optionValue="${{it.fullName}}"
										onchange="openPersonDiv(this.value,this.id)" value="${applicationInstance.sme?.id}"
										tabindex="23"
										noSelection="${['null':' Please Select']}"
									/>
								</div>
							</div>
						</div>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.environment}" value="${applicationInstance.environment}"/>
							<div class="clr-control-container">
								<div class="clr-select-wrapper">
									<g:select id="environment" class="clr-select" name="environment"
										from="${environmentOptions}" value="${applicationInstance.environment}"
										noSelection="${['':' Please Select']}" tabindex="33"></g:select>
								</div>
							</div>
						</div>
						
						<tds:inputLabelAndField field="${standardFieldSpecs.userLocations}" value="${applicationInstance.userLocations}" tabindex="43" tooltipDataPlacement="bottom"/>
						<tds:inputLabelAndField field="${standardFieldSpecs.appTech}" value="${applicationInstance.appTech}" tabindex="15"/>
						
						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.sme2}" value="${applicationInstance.sme2}"/>
							<%-- TODO: Update shuffle --%>
							<div class="clr-control-container">
								<div class="clr-select-wrapper">							
									<g:select from="${personList}" id="sme2" name="sme2.id" class="clr-select" optionKey="personId"
										optionValue="${{it.fullName}}"
										onchange="openPersonDiv(this.value, this.id)"
										value="${applicationInstance.sme2?.id}"
										tabindex="24"
										noSelection="${['null':' Please Select']}"/>
								</div>
							</div>
							<asset:image src="images/swapicon.png" onclick="shufflePerson('sme1','sme2')" class="SuffleImage" alt="Swap Contacts" title="Swap contacts with SME1"/>
						</div>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.criticality}" value="${applicationInstance.criticality}"/>
							<div class="clr-control-container">
								<div class="clr-select-wrapper">
									<g:select id="criticality" class="clr-select" name="criticality"
										from="${com.tdssrc.grails.GormUtil.getConstrainedProperties(applicationInstance.class).criticality.inList}" value="${applicationInstance.criticality}"
										noSelection="${['':'Please select']}"
										tabindex="34">
									</g:select>
								</div>
							</div>
						</div>
						
						<tds:inputLabelAndField field="${standardFieldSpecs.useFrequency}" value="${applicationInstance.useFrequency}" tabindex="44" tooltipDataPlacement="bottom"/>
						<tds:inputLabelAndField field="${standardFieldSpecs.appSource}" value="${applicationInstance.appSource}" tabindex="16"/>
						
						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.appOwner}" value="${applicationInstance.appOwner}"/>
							<div class="clr-control-container">
								<div class="clr-select-wrapper">
									<g:select from="${personList}" id="appOwnerEdit" class="clr-select" name="appOwner.id"  optionKey="personId"
										optionValue="${{it.fullName}}"
										onchange="openPersonDiv(this.value, this.id)"
										value="${applicationInstance.appOwner?.id}"
										tabindex="25"
										noSelection="${['null':' Please Select']}"
									/>
								</div>
							</div>
							<asset:image src="images/swapicon.png" onclick="shufflePerson('sme2','appOwnerEdit')" class="SuffleImage" alt="Swap Contacts" title="Swap contacts with SME2"/>
						</div>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.moveBundle}" value="${applicationInstance.moveBundle?.id}"/>
							<div class="clr-control-container">
								<div class="clr-select-wrapper">
									<g:select from="${moveBundleList}" id="moveBundle" class="clr-select" name="moveBundle.id"
									value="${applicationInstance.moveBundle?.id}" optionKey="id" optionValue="name" tabindex="35" />
								</div>
							</div>	
						</div>

						<tds:inputLabelAndField field="${standardFieldSpecs.drRpoDesc}" value="${applicationInstance.drRpoDesc}" tabindex="45" tooltipDataPlacement="bottom"/>
						<tds:inputLabelAndField field="${standardFieldSpecs.license}" value="${applicationInstance.license}" tabindex="17"/>
						<tds:inputLabelAndField field="${standardFieldSpecs.businessUnit}" value="${applicationInstance.businessUnit}" tabindex="26"/>
						
						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.planStatus}" value="${applicationInstance.planStatus}"/>
							<div class="clr-control-container">
								<div class="clr-select-wrapper">
									<g:select from="${planStatusOptions}" id="planStatus" class="clr-select" name="planStatus" value="${applicationInstance.planStatus}" tabindex="36" />
								</div>
							</div>	
						</div>

						<tds:inputLabelAndField field="${standardFieldSpecs.drRtoDesc}" value="${applicationInstance.drRtoDesc}" tabindex="46" tooltipDataPlacement="bottom"/>
						
						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.retireDate}" value="${applicationInstance.retireDate}"/>
							<input type="text" class="dateRange" name="retireDate" id="retireDate" tabindex="27"
							value="<tds:convertDate date="${applicationInstance?.retireDate}"  />">
						</div>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.validation}" value="${applicationInstance.validation}"/>
							<div class="clr-control-container">
								<div class="clr-select-wrapper">
									<g:select  id="validation"	class="clr-select" name="validation"
										from="${com.tdssrc.grails.GormUtil.getConstrainedProperties(applicationInstance.class).validation.inList }"
										value="${applicationInstance.validation}" tabindex="37" />
									</div>
							</div>	
						</div>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.testProc}" value="${applicationInstance.testProc}"/>
							<div class="clr-control-container">
								<div class="clr-select-wrapper">
									<g:select  id="testProc" class="clr-select" name="testProc"  from="${['Y', 'N']}" value="?"
										noSelection="['':'?']" tabindex="46" value="${applicationInstance.testProc}" tabindex="47"/>
								</div>
							</div>	
						</div>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${applicationInstance.maintExpDate}"/>
							<script type="text/javascript" charset="utf-8">
								jQuery(function($){ $(".dateRange").kendoDatePicker({ animation: false, format:tdsCommon.kendoDateFormat()  }); });
							</script>
							<input type="text" class="dateRange"
								name="maintExpDate" id="maintExpDate" tabindex="28"
								value="<tds:convertDate date="${applicationInstance?.maintExpDate}" />">
						</div>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.latency}" value="${applicationInstance.latency}"/>
							<div class="clr-control-container">
								<div class="clr-select-wrapper">
									<g:select  id="latency" class="clr-select"	name="latency"  from="${['Y', 'N']}" value="?"
										noSelection="['':'?']" tabindex="46" value="${applicationInstance.latency}" tabindex="38" />
								</div>
							</div>	
						</div>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.startupProc}" value="${applicationInstance.startupProc}"/>
							<div class="clr-control-container">
								<div class="clr-select-wrapper">
									<g:select  id="startupProc" class="clr-select" name="startupProc" from="${['Y', 'N']}" value="?"
										noSelection="['':'?']" tabindex="46" value="${applicationInstance.startupProc}" tabindex="48"/>
								</div>
							</div>	
						</div>


						<tds:inputLabelAndField field="${standardFieldSpecs.url}" value="${applicationInstance.url}" tabindex="18"/>
						<tds:inputLabelAndField field="${standardFieldSpecs.externalRefId}" value="${applicationInstance.externalRefId}" tabindex="28"/>

						<div class="clr-form-control">
							<label class="clr-control-label ${standardFieldSpecs.shutdownBy.imp?:''}" for="shutdownBy">
								<tds:tooltipSpan field="${standardFieldSpecs.shutdownBy}">
									${standardFieldSpecs.shutdownBy.label}
								</tds:tooltipSpan>
							</label>
							<g:render template="bySelect" model="[name:'shutdownBy' , id:'shutdownByEditId']"></g:render>
							<div class="clr-form-control with-inline-checkbox">
								<div class="clr-control-container">
									<div class="clr-checkbox-wrapper">
										<input type="checkbox" id="shutdownByEditIdFixed"  name="shutdownFixed" value="${applicationInstance.shutdownFixed} "
											${!applicationInstance.shutdownBy || applicationInstance.shutdownBy.contains('@') ? 'disabled="disabled"' : ''}
											onclick="if(this.checked){this.value = 1} else {this.value = 0 }"
											${applicationInstance.shutdownFixed==1? 'checked="checked"' : ''}/>
										<label class="clr-control-label" for="shutdownByEditIdFixed">Fixed</label>
									</div>
								</div>
							</div>
						</div>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.shutdownDuration}" value="${applicationInstance.shutdownDuration}"/>
							<div class="clr-control-container">
      							<div class="clr-input-wrapper">				
									<input type="text" id="shutdownDuration" name="shutdownDuration" class="clr-input"
										value="${applicationInstance.shutdownDuration}" tabindex="48"/>
										<span style="position: relative; right: 18px;">m</span>
								</div>
							</div>
						</div>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.startupBy}" value="${applicationInstance.startupBy}"/>
							<g:render template="bySelect" model="[name:'startupBy', id:'startupByEditId']" tabindex="19"></g:render>
							<div class="clr-form-control with-inline-checkbox">
								<div class="clr-control-container">
									<div class="clr-checkbox-wrapper">
										<input type="checkbox" id="startupByEditIdFixed" name="startupFixed" value="${applicationInstance.startupFixed}"
											${!applicationInstance.startupBy || applicationInstance.startupBy.contains('@') ? 'disabled="disabled"' : ''}
											onclick="if(this.checked){this.value = 1} else {this.value = 0 }"
											${applicationInstance.startupFixed ==1? 'checked="checked"' : ''}/>
										<label class="clr-control-label" for="startupByEditIdFixed">Fixed</label>
									</div>
								</div>
							</div>
						</div>

						<tds:inputLabelAndField field="${standardFieldSpecs.startupDuration}" value="${applicationInstance.startupDuration}" tabindex="29"/>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.testingBy}" value="${applicationInstance.testingBy}"/>
							<g:render template="bySelect" model="[name:'testingBy', id:'testingByEditId']"></g:render>
							<div class="clr-form-control with-inline-checkbox">
								<div class="clr-control-container">
									<div class="clr-checkbox-wrapper">
										<input type="checkbox" id="testingByEditIdFixed" name="testingFixed" value="${applicationInstance.testingFixed}"
											${!applicationInstance.testingBy || applicationInstance.testingBy.contains('@') ? 'disabled="disabled"' : ''}
											onclick="if(this.checked){this.value = 1} else {this.value = 0 }"
											${applicationInstance.testingFixed ==1? 'checked="checked"' : ''}/>
										<label class="clr-control-label" for="testingByEditIdFixed">Fixed</label>
									</div>
								</div>
							</div>
						</div>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.testingDuration}" value="${applicationInstance.testingDuration}"/>
							<div class="clr-control-container">
      							<div class="clr-input-wrapper">
									<input type="text" id="testingDuration" class="clr-input" name="testingDuration"
											value="${applicationInstance.testingDuration}" tabindex="49"/>
									<span style="position: relative; right: 18px;">m</span>
								</div>
							</div>
						</div>

						<%-- TODO: Update these. --%>
						<g:render template="/assetEntity/customEdit" model="[assetEntityInstance:applicationInstance]"></g:render>
					</div>
					<g:render template="/comment/assetTagsEdit"></g:render>

						<%-- Dependency Edit Block --%>
						<%-- Q: How is this getting populated? --%>
					<table id="supports" style="border: 0" class="asset-entities-dialog-table-content">
						<tr id="applicationDependentId" class="assetDependent">
							<td class="depSpin"><span><asset:image src="images/processing.gif"/></span></td>
						</tr>
					</table>
				</div>
			</div>
		</div>
		<g:render template="/assetEntity/editButtons" model="[assetEntity:applicationInstance]"></g:render>
	</div>
</g:form>

<script>
    $(document).ready(function() {
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
    });

	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')
	$('#tabType').val($('#assetTypesId').val());
</script>
