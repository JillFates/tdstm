<%@page import="com.tds.asset.Application"%>
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
		changeDocTitle('${escapedName}');
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
	<g:render template="../assetEntity/dependentHidden" />

	<table style="border: 0">
		<tr>
			<td colspan="2">
				<div class="dialog">
					<table>
						<tbody>
							<tr>
								<td class="label ${standardFieldSpecs.assetName.imp?:''}" nowrap="nowrap"><label for="assetName">${standardFieldSpecs.assetName.label}<span style="color: red;">*</span></label></td>
								<td colspan="3" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.assetName.tip?:''}">
									<input type="text" id="assetName" class="${standardFieldSpecs.assetName.imp?:''}" name="assetName"
										value="${applicationInstance.assetName}" tabindex="9" /></td>

								<td class="label ${standardFieldSpecs.description.imp?:''}" nowrap="nowrap"><label for="description">${standardFieldSpecs.description.label}</label></td>
								<td colspan="3" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.description.tip?:''}">
									<input type="text" id="description" class="${standardFieldSpecs.description.imp?:''}" name="description"
										value="${applicationInstance.description}" size="50" tabindex="10" />
								</td>
							</tr>
							<tr>
								<td class="label ${standardFieldSpecs.appAccess.imp?:''}" nowrap="nowrap"><label for="assetType">${standardFieldSpecs.appAccess.label}</label></td>
								<td data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.appAccess.tip?:''}">
									<input type="text" id="assetType" name="assetType" value="Application" readonly="readonly" />
								</td>
								
								<td class="label ${standardFieldSpecs.supportType.imp?:''}" nowrap="nowrap"><label for="supportType">${standardFieldSpecs.supportType.label}</label>
								</td>
								<td data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.supportType.tip?:''}">
									<input type="text" id="supportType" class="${standardFieldSpecs.supportType.imp?:''}"
										name="supportType" value="${applicationInstance.supportType}"  tabindex="21" />
								</td>
								
								<td class="label ${standardFieldSpecs.appFunction.imp?:''}" nowrap="nowrap"><label for="appFunction">${standardFieldSpecs.appFunction.label}</label>
								</td>
								<td data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.appFunction.tip?:''}">
									<input type="text" id="appFunction" class="${standardFieldSpecs.appFunction.imp?:''}"
										name="appFunction" value="${applicationInstance.appFunction}"  tabindex="31" />
								</td>
								
								<td class="label ${standardFieldSpecs.userCount.imp?:''}" nowrap="nowrap"><label for="userCount">${standardFieldSpecs.userCount.label}</label>
								</td>
								<td data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.userCount.tip?:''}">
									<input type="text" id="userId" class="${standardFieldSpecs.userCount.imp?:''}" name="userCount" value="${applicationInstance.userCount}"  tabindex="41" />
								</td>
							</tr>
							<tr>
								<td class="label ${standardFieldSpecs.appVendor.imp?:''}" nowrap="nowrap"><label for="appVendor">${standardFieldSpecs.appVendor.label}</label></td>
								<td data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.appVendor.tip?:''}">
									<input type="text" id="appVendor" class="${standardFieldSpecs.appVendor.imp?:''}"
										name="appVendor" value="${applicationInstance.appVendor}"  tabindex="11" />
								</td>

								<td class="label ${standardFieldSpecs.sme.imp?:''}" nowrap="nowrap"><label for="sme">${standardFieldSpecs.sme.label}</label></td>
								<td data-for="sme" class="${standardFieldSpecs.sme.imp?:''}" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.sme.tip?:''}">
									<g:select from="${personList}" id="sme1" name="sme.id" class="${standardFieldSpecs.sme.imp?:''} personContact assetSelect" optionKey="personId"
										optionValue="${{it.fullName}}"
										onchange="openPersonDiv(this.value,this.id)" value="${applicationInstance.sme?.id}"
										tabindex="22"
										noSelection="${['null':' Please Select']}"
									/>
								</td>

								<td class="label ${standardFieldSpecs.environment.imp?:''}" nowrap="nowrap"><label for="environment">${standardFieldSpecs.environment.label}</label>
								</td>
								<td data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.environment.tip?:''}">
									<g:select id="environment" class="${standardFieldSpecs.environment.imp?:''}" name="environment" from="${environmentOptions}" value="${applicationInstance.environment}"  noSelection="${['':' Please Select']}" tabindex="32"></g:select>
								</td>

								<td class="label ${standardFieldSpecs.userLocations.imp?:''}" nowrap="nowrap"><label for="userLocations">${standardFieldSpecs.userLocations.label}</label>
								</td>
								<td data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.userLocations.tip?:''}">
									<input type="text" id="userLocations" class="${standardFieldSpecs.userLocations.imp?:''}"
										name="userLocations" value="${applicationInstance.userLocations}"  tabindex="42" />
								</td>
							</tr>
							<tr>
								<td class="label ${standardFieldSpecs.appVersion.imp?:''}" nowrap="nowrap"><label for="appVersion">${standardFieldSpecs.appVersion.label}</label>
								</td>
								<td data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.appVersion.tip?:''}">
									<input type="text" id="appVersion" class="${standardFieldSpecs.appVersion.imp?:''}"
										name="appVersion" value="${applicationInstance.appVersion}"  tabindex="12" />
								</td>

								<td class="label ${standardFieldSpecs.sme2.imp?:''}" nowrap="nowrap"><label for="sme2">${standardFieldSpecs.sme2.label}</label></td>
								<td data-for="sme2" class="${standardFieldSpecs.sme2.imp?:''} suffleTd" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.sme2.tip?:''}">
								 <img src="${resource(dir:'images',file:'swapicon.png')}" onclick="shufflePerson('sme1','sme2')" class="SuffleImage" alt="Swap Contacts" title="Swap Contacts"/>
									<g:select from="${personList}" id="sme2" name="sme2.id" class="${standardFieldSpecs.sme2.imp?:''} suffleSelect personContact assetSelect" optionKey="personId"
										optionValue="${{it.fullName}}"
										onchange="openPersonDiv(this.value, this.id)"
										value="${applicationInstance.sme2?.id}"
										tabindex="23"
										noSelection="${['null':' Please Select']}"
									/>
								</td>

								<td class="label ${standardFieldSpecs.criticality.imp?:''}" nowrap="nowrap"><label for="criticality">${standardFieldSpecs.criticality.label}</label>
								</td>
								<td data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.criticality.tip?:''}">
									<g:select id="criticality" class="${standardFieldSpecs.criticality.imp?:''}" name="criticality"
										from="${applicationInstance.constraints.criticality.inList}" value="${applicationInstance.criticality}"
										noSelection="${['':'Please select']}"
										tabindex="33">
									</g:select>
								</td>

								<td class="label ${standardFieldSpecs.userFrequency.imp?:''}" nowrap="nowrap"><label for="useFrequency">${standardFieldSpecs.userFrequency.label}</label>
								</td>
								<td data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.userFrequency.tip?:''}">
									<input type="text" id="useFrequency" class="${standardFieldSpecs.userFrequency.imp?:''}" name="useFrequency" value="${applicationInstance.useFrequency}" tabindex="43" />
								</td>
							</tr>
							<tr>
								<td class="label ${standardFieldSpecs.appTech.imp?:''}" nowrap="nowrap"><label for="appTech">${standardFieldSpecs.appTech.label}</label></td>
								<td data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.appTech.tip?:''}">
									<input type="text" id="appTech" class="${standardFieldSpecs.appTech.imp?:''}" name="appTech" value="${applicationInstance.appTech}" tabindex="13" />
								</td>

								<td class="label ${standardFieldSpecs.appOwner.imp?:''}" nowrap="nowrap"><label for="appOwnerId">${standardFieldSpecs.appOwner.label}</label></td>
								<td class="suffleTd ${standardFieldSpecs.appOwner.imp?:''}" data-for="appOwner" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.appOwner.tip?:''}">
								 <img src="${resource(dir:'images',file:'swapicon.png')}" onclick="shufflePerson('sme2','appOwnerEdit')" class="SuffleImage" alt="Swap Contacts" title="Swap Contacts"/>
									<g:select from="${personList}" id="appOwnerEdit" class="${standardFieldSpecs.appOwner.imp?:''} suffleSelect personContact assetSelect" name="appOwner.id"  optionKey="personId"
										optionValue="${{it.fullName}}"
										onchange="openPersonDiv(this.value, this.id)"
										value="${applicationInstance.appOwner?.id}"
										tabindex="24"
										noSelection="${['null':' Please Select']}"
									/>
								</td>
								
								<td class="label ${standardFieldSpecs.moveBundle.imp?:''}" nowrap="nowrap"><label for="moveBundle">${standardFieldSpecs.moveBundle.label}</label></td>
								<td data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.moveBundle.tip?:''}">
									<g:select from="${moveBundleList}" id="moveBundle" class="${standardFieldSpecs.moveBundle.imp?:''}" name="moveBundle.id" value="${applicationInstance.moveBundle?.id}" optionKey="id" optionValue="name" tabindex="34" />
								</td>
								
								<td class="label ${standardFieldSpecs.drRpoDesc.imp?:''}" nowrap="nowrap"><label for="drRpoDesc">${standardFieldSpecs.drRpoDesc.label}</label>
								</td>
								<td data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.drRpoDesc.tip?:''}">
									<input type="text" id="drRpoDesc"	class="${standardFieldSpecs.drRpoDesc.imp?:''}" name="drRpoDesc" value="${applicationInstance.drRpoDesc}" tabindex="44" />
								</td>
							</tr>
							<tr>
								<td class="label ${standardFieldSpecs.appSource.imp?:''}" nowrap="nowrap"><label for="appSource">${standardFieldSpecs.appSource.label}</label></td>
								<td data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.appSource.tip?:''}">
									<input type="text" id="appSource"	class="$${standardFieldSpecs.appSource.imp?:''}" name="appSource" value="${applicationInstance.appSource}" tabindex="14" />
								</td>

								<td class="label ${standardFieldSpecs.businessUnit.imp?:''}" nowrap="nowrap"><label for="businessUnit">${standardFieldSpecs.businessUnit.label}</label>
								</td>
								<td data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.businessUnit.tip?:''}">
									<input type="text" id="businessUnit" class="${standardFieldSpecs.businessUnit.imp?:''}" name="businessUnit" value="${applicationInstance.businessUnit}" tabindex="25" />
								</td>

								<td class="label ${standardFieldSpecs.planStatus.imp?:''}" nowrap="nowrap"><label for="planStatus">${standardFieldSpecs.planStatus.label}</label>
								</td>
								<td data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.planStatus.tip?:''}">
									<g:select from="${planStatusOptions}" id="planStatus" class="${standardFieldSpecs.planStatus.imp?:''}" name="planStatus" value="${applicationInstance.planStatus}" tabindex="35" />
								</td>

								<td class="label ${standardFieldSpecs.drRtoDesc.imp?:''}" nowrap="nowrap"><label for="drRtoDesc">${standardFieldSpecs.drRtoDesc.label}</label>
								</td>
								<td data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.drRtoDesc.tip?:''}">
									<input type="text" id="drRtoDesc"	class="${standardFieldSpecs.drRtoDesc.imp?:''}" name="drRtoDesc" value="${applicationInstance.drRtoDesc}" tabindex="45" />
								</td>
							</tr>
							<tr>
							<tr>
								<td class="label ${standardFieldSpecs.license.imp?:''}" nowrap="nowrap"><label for="license">${standardFieldSpecs.license.label}</label></td>
								<td data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.license.tip?:''}">
									<input type="text" id="license" class="${standardFieldSpecs.license.imp?:''}" name="license" value="${applicationInstance.license}" tabindex="15" />
								</td>

								<td class="label ${standardFieldSpecs.retireDate.imp?:''}"><label for="retireDate">${standardFieldSpecs.retireDate.label}</label>
								</td>
								<td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'retireDate','errors')}" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.retireDate.tip?:''}">
									<input type="text" class="dateRange ${standardFieldSpecs.retireDate.imp?:''}" size="15" style="width: 138px;" name="retireDate" id="retireDate" tabindex="26"
									value="<tds:convertDate date="${applicationInstance?.retireDate}"  />" >
								</td>
								
								<td class="label ${standardFieldSpecs.validation.imp?:''}" nowrap="nowrap"><label for="validation">${standardFieldSpecs.validation.label}</label>
								</td>
								<td data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.validation.tip?:''}">
									<g:select  id="validation"	class="${standardFieldSpecs.validation.imp?:''}" name="validation" from="${applicationInstance.constraints.validation.inList }"  onChange="assetFieldImportance(this.value,'Application');highlightCssByValidation(this.value,'Application','${applicationInstance.id}');" value="${applicationInstance.validation}" tabindex="36" />
								</td>
								
								<td class="label ${standardFieldSpecs.testProc.imp?:''}" nowrap="nowrap"><label for="testProc">${standardFieldSpecs.testProc.label}</label>
								</td>
								<td data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.testProc.tip?:''}">
									<g:select  id="testProc"	class="${standardFieldSpecs.testProc.imp?:''} ynselect" name="testProc"  from="${['Y', 'N']}" value="?"
		                                 noSelection="['':'?']" tabindex="46" value="${applicationInstance.testProc}" tabindex="46"/>
								</td>
							</tr>
							<tr>
							    <td></td>
							    <td></td>
							    
								<td  class="label ${standardFieldSpecs.maintExpDate.imp?:''}"><label for="maintExpDate">${standardFieldSpecs.maintExpDate.label}</label></td>
								<td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'maintExpDate','errors')}" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.maintExpDate.tip?:''}">
								    <script type="text/javascript" charset="utf-8">
										jQuery(function($){ $(".dateRange").kendoDatePicker({ animation: false, format:tdsCommon.kendoDateFormat()  }); });
									</script>
									<input type="text" class="dateRange ${standardFieldSpecs.maintExpDate.imp?:''}"
										size="15" style="width: 138px;" name="maintExpDate" id="maintExpDate" tabindex="27"
										value="<tds:convertDate date="${applicationInstance?.maintExpDate}" />">
								</td>
								
								<td class="label ${standardFieldSpecs.latency.imp?:''}" nowrap="nowrap"><label for="latency">${standardFieldSpecs.latency.label}</label>
								</td>
								<td data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.latency.tip?:''}">
									<g:select  id="latency" class="${standardFieldSpecs.latency.imp?:''} ynselect"	name="latency"  from="${['Y', 'N']}" value="?"
		                                 noSelection="['':'?']" tabindex="46" value="${applicationInstance.latency}" tabindex="37" />
								</td>
								
								<td class="label ${standardFieldSpecs.startupProc.imp?:''}" nowrap="nowrap"><label for="startupProc">${standardFieldSpecs.startupProc.label}</label>
								</td>
								<td data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.startupProc.tip?:''}">
									<g:select  id="startupProc" class="${standardFieldSpecs.startupProc.imp?:''} ynselect" name="startupProc" from="${['Y', 'N']}" value="?"
		                                 noSelection="['':'?']" tabindex="46" value="${applicationInstance.startupProc}" tabindex="47"/>
								</td>

							</tr>
							<tr>
								<td class="label ${standardFieldSpecs.url.imp?:''}" nowrap="nowrap"><label for="url">${standardFieldSpecs.url.label}</label></td>
								<td data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.url.tip?:''}">
									<input type="text" class="${standardFieldSpecs.url.imp?:''}" id="url" name="url" value="${applicationInstance.url}" tabindex="18" />
								</td>
								
								<td class="label ${standardFieldSpecs.externalRefId.imp?:''}" nowrap="nowrap"><label for="externalRefId">${standardFieldSpecs.externalRefId.label}</label></td>
								<td ata-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.externalRefId.tip?:''}">
									<input type="text" id="externalRefId" class="${standardFieldSpecs.externalRefId.imp?:''}" name="externalRefId"
									value="${applicationInstance.externalRefId}" tabindex="28" />
								</td>
								
								<td class="label ${standardFieldSpecs.shutdownBy.imp?:''}" nowrap="nowrap"><label for="shutdownBy">${standardFieldSpecs.shutdownBy.label}</label></td>
								<td class="${standardFieldSpecs.shutdownBy.imp?:''}" data-for="shutdownBy" nowrap="nowrap" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.shutdownBy.tip?:''}">
								   <g:render template="bySelect" model="[name:'shutdownBy' , id:'shutdownByEditId', className:'assetSelect']"></g:render>
									<input type="checkbox" id="shutdownByEditIdFixed"  name="shutdownFixed" value="${applicationInstance.shutdownFixed} "
										${!applicationInstance.shutdownBy || applicationInstance.shutdownBy.contains('@') ? 'disabled="disabled"' : ''}
										onclick="if(this.checked){this.value = 1} else {this.value = 0 }"
										${applicationInstance.shutdownFixed==1? 'checked="checked"' : ''}/>Fixed
								</td>
								
								<td class="label ${standardFieldSpecs.shutdownDuration.imp?:''}" nowrap="nowrap"><label for="shutdownDuration">${standardFieldSpecs.shutdownDuration.label}</label>
								</td>
								<td data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.shutdownDuration.tip?:''}">
									<input type="text" id="shutdownDuration" name="shutdownDuration" class="${standardFieldSpecs.shutdownDuration.imp?:''}"
										value="${applicationInstance.shutdownDuration}" tabindex="55" size="7"/>m
								</td>
							</tr>
							<tr>
								<td class="label ${standardFieldSpecs.startupBy.imp?:''}" nowrap="nowrap"><label for="startupBy">${standardFieldSpecs.startupBy.label}</label></td>
								<td colspan="1" nowrap="nowrap" data-for="startupBy" class="${standardFieldSpecs.startupBy.imp?:''}" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.startupBy.tip?:''}">
								   <g:render template="bySelect" model="[name:'startupBy', id:'startupByEditId', className:'assetSelect']"></g:render>
									<input type="checkbox" id="startupByEditIdFixed" name="startupFixed" value="${applicationInstance.startupFixed}"
										${!applicationInstance.startupBy || applicationInstance.startupBy.contains('@') ? 'disabled="disabled"' : ''}
										onclick="if(this.checked){this.value = 1} else {this.value = 0 }"
										${applicationInstance.startupFixed ==1? 'checked="checked"' : ''}/>Fixed
								</td>

								<td class="label ${standardFieldSpecs.startupDuration.imp?:''}" nowrap="nowrap"><label for="startupDuration">${standardFieldSpecs.startupDuration.label}</label>
								</td>
								<td data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.startupDuration.tip?:''}">
									<input type="text" id="startupDuration" class="${standardFieldSpecs.startupDuration.imp?:''}" name="startupDuration"
											value="${applicationInstance.startupDuration}" tabindex="55" size="7" />m
								</td>


								<td class="label ${standardFieldSpecs.testingBy.imp?:''}" nowrap="nowrap"><label for="testingBy">${standardFieldSpecs.testingBy.label}</label></td>
								<td colspan="1" nowrap="nowrap" class="${standardFieldSpecs.testingBy.imp?:''}" data-for="testingBy" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.testingBy.tip?:''}">
								  <g:render template="bySelect" model="[name:'testingBy', id:'testingByEditId', className:'assetSelect']"></g:render>
									<input type="checkbox" id="testingByEditIdFixed" name="testingFixed" value="${applicationInstance.testingFixed}"
										${!applicationInstance.testingBy || applicationInstance.testingBy.contains('@') ? 'disabled="disabled"' : ''}
										onclick="if(this.checked){this.value = 1} else {this.value = 0 }"
										${applicationInstance.testingFixed ==1? 'checked="checked"' : ''}/>Fixed
								</td>
								
								<td class="label ${standardFieldSpecs.testingDuration.imp?:''}" nowrap="nowrap"><label for="testingDuration">${standardFieldSpecs.testingDuration.label}</label>
								</td>
								<td data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.testingDuration.tip?:''}">
									<input type="text" id="testingDuration" class="${standardFieldSpecs.testingDuration.imp?:''}" name="testingDuration"
											value="${applicationInstance.testingDuration}" tabindex="55"  size="7"/>m
								</td>
							</tr>

							<%-- Custom User Defined Fields Section --%>
							<tbody class="customTemplate">
								<g:render template="../assetEntity/customEdit" model="[assetEntityInstance:applicationInstance]"></g:render>
							</tbody>

						</tbody>
					</table>
				</div></td>
		</tr>

		<%-- Dependency Edit Block --%>
		<tr id="applicationDependentId" class="assetDependent">
			<td class="depSpin"><span><img alt="" src="${resource(dir:'images',file:'processing.gif')}"/></span></td>
		</tr>

		<%-- Action Buttons --%>
		<tr>
			<td colspan="2">
				<div class="buttons">
					<g:render template="../assetEntity/editButtons" model="[assetEntity:applicationInstance]"></g:render>
				</div>
			</td>
		</tr>
	</table>
</g:form>
<script>
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')
	$('#tabType').val($('#assetTypesId').val());
</script>
