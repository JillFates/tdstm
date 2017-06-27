<%@ page import="net.transitionmanager.security.Permission" %>
<script type="text/javascript">
	$("#appl_assetName").val($('#gs_assetName').val());
	$("#appl_sme").val($('#gs_sme').val());
	$("#appl_validation").val($('#gs_validation').val())
	$("#appl_planStatus").val($('#gs_planStatus').val())
	$("#appl_moveBundle").val($('#gs_moveBundle').val())
	$("#createStaffDialog").dialog({ autoOpen: false })
	
	var myOption = "<option value='0'>Add Person...</option>"
	<tds:hasPermission permission="${Permission.PersonCreate}">
		$("#sme1 option:first").after(myOption);
		$("#sme2 option:first").after(myOption);
		$("#appOwner option:first").after(myOption);
	</tds:hasPermission>
	if(!isIE7OrLesser)
		$("select.assetSelect").select2();
	
</script>
<g:form method="post" action="save" name="createEditAssetForm" onsubmit="return validateFields('',this.name)">
	<input type="hidden" id="appl_assetName" name="assetNameFilter" value="" />
	<input type="hidden" id="appl_sme" name="appSmeFilter" value="" />
	<input type="hidden" id="appl_validation" name="appValidationFilter" value="" />
	<input type="hidden" id="appl_moveBundle" name="moveBundleFilter" value="" />
	<input type="hidden" id="appl_planStatus" name="planStatusFilter" value="" />
	<input type="hidden" name="showView" id="showView" value=""/>

	<%-- Used to track dependencies added and deleted --%>
	<g:render template="../assetEntity/dependentHidden" />

	<table style="border: 0">
		<tr>
			<td colspan="2">
				<div class="dialog">
					<table>
						<tbody>
							<tr>
								<tds:inputLabel field="${standardFieldSpecs.assetName}"/>
								<td colspan="3">
									<tds:inputControl field="${standardFieldSpecs.assetName}" tabindex="11" value="${applicationInstance.assetName}" />	
								</td>

								<tds:inputLabel field="${standardFieldSpecs.description}"/>
								<td colspan="3">
									<tds:inputControl field="${standardFieldSpecs.description}" size="50" tabindex="21" value="${applicationInstance.description}"/>
								</td>
							</tr>
							<tr>
								<tds:inputLabel field="${standardFieldSpecs.appAccess}"/>
								<td>
									<input type="text" id="assetType" name="assetType" readonly="readonly" value="${applicationInstance.assetType}" tabindex="12" />
								</td>

								<tds:inputLabelAndField field="${standardFieldSpecs.supportType}" value="${applicationInstance.supportType}" tabindex="22"/>

								<tds:inputLabelAndField field="${standardFieldSpecs.appFunction}" value="${applicationInstance.appFunction}" tabindex="32"/>
								
								<tds:inputLabelAndField field="${standardFieldSpecs.userCount}" value="${applicationInstance.userCount}" tabindex="42"/>
							</tr>
							<tr>
								<tds:inputLabelAndField field="${standardFieldSpecs.appVendor}" value="${applicationInstance.appVendor}" tabindex="13"/>
								
								<tds:inputLabel field="${standardFieldSpecs.environment}"/>
								<td data-for="sme" class="${standardFieldSpecs.sme.imp?:''}">
									<g:select from="${personList}" id="sme1" name="sme.id" class="${standardFieldSpecs.sme.imp?:''} personContact assetSelect"  optionKey="personId" 
										optionValue="${{it.fullName}}" 
										onchange="openPersonDiv(this.value,this.id)" 
										noSelection="${['null':'Select...']}" 
										tabIndex="23"
									/>
								</td>
								
								<tds:inputLabel field="${standardFieldSpecs.environment}"/>
								<td>
									<g:select id="environment" class="${standardFieldSpecs.environment.imp?:''}" name="environment" 
										from="${environmentOptions}" 
										value="${applicationInstance.environment}" 
										noSelection="${['':'Select...']}" tabindex="33">
									</g:select>
								</td>
								
								<tds:inputLabelAndField field="${standardFieldSpecs.userLocations}" value="${applicationInstance.userLocations}" tabindex="43"/>
							</tr>
							<tr>
								<tds:inputLabelAndField field="${standardFieldSpecs.appVersion}" value="${applicationInstance.appVersion}" tabindex="14"/>
								
								<tds:inputLabel field="${standardFieldSpecs.sme2}"/>
								<td data-for="sme2" class="${standardFieldSpecs.sme2.imp?:''} suffleTd">
								 <img src="${resource(dir:'images',file:'swapicon.png')}" onclick="shufflePerson('sme1','sme2')" class="SuffleImage"  alt="Swap Contacts" title="Swap Contacts"/>
									<g:select from="${personList}" id="sme2" name="sme2.id" class="${standardFieldSpecs.sme2.imp?:''} suffleSelect personContact assetSelect" optionKey="personId" 
										optionValue="${{it.fullName}}" 
										onchange="openPersonDiv(this.value, this.id)" 
										tabindex="24" 
										noSelection="${['null':'Select...']}"
									/>
								</td>
								
								<tds:inputLabel field="${standardFieldSpecs.criticality}"/>
								<td>
									<g:select id="criticality" class="${standardFieldSpecs.criticality.imp?:''}" name="criticality" 
										from="${applicationInstance.constraints.criticality.inList}" 
										value="${applicationInstance.criticality}" 
										noSelection="${['':'Select...']}"
										tabindex="34">
									</g:select>
								</td>
								
								<tds:inputLabelAndField field="${standardFieldSpecs.useFrequency}" value="${applicationInstance.useFrequency}" tabindex="44"/>
							</tr>
							<tr>
								<tds:inputLabelAndField field="${standardFieldSpecs.appTech}" value="${applicationInstance.appTech}" tabindex="15"/>

								<tds:inputLabel field="${standardFieldSpecs.appOwner}"/>
								<td class="suffleTd ${standardFieldSpecs.appOwner.imp?:''}">
								 <img src="${resource(dir:'images',file:'swapicon.png')}" onclick="shufflePerson('sme2','appOwner')" class="SuffleImage" alt="Swap Contacts" title="Swap Contacts"/>
									<g:select from="${personList}" id="appOwner" name="appOwner.id" class="${standardFieldSpecs.appOwner.imp?:''} suffleSelect personContact assetSelect" optionKey="personId" 
										optionValue="${{it.fullName}}" 
										onchange="openPersonDiv(this.value, this.id)" 
										tabindex="25" 
										noSelection="${['null':' Select...']}" 
									/>
								</td>
								
								<tds:inputLabel field="${standardFieldSpecs.moveBundle}"/>
								<td>
									<g:select from="${moveBundleList}" id="moveBundle" class="${standardFieldSpecs.moveBundle.imp?:''}" 
										name="moveBundle.id" value="${project.defaultBundle.id}" optionKey="id" optionValue="name" tabindex="35" />
								</td>
								
								<tds:inputLabelAndField field="${standardFieldSpecs.drRpoDesc}" value="${applicationInstance.drRpoDesc}" tabindex="45"/>
							</tr>
							<tr>
								<tds:inputLabelAndField field="${standardFieldSpecs.appSource}" value="${applicationInstance.appSource}" tabindex="16"/>

								<tds:inputLabelAndField field="${standardFieldSpecs.businessUnit}" value="${applicationInstance.businessUnit}" tabindex="26"/>

								<tds:inputLabel field="${standardFieldSpecs.planStatus}"/>
								<td>
									<g:select from="${planStatusOptions}" id="planStatus" class="${standardFieldSpecs.planStatus.imp?:''}" 
										name="planStatus" value="${applicationInstance.planStatus}" tabindex="36" />
								</td>
								
								<tds:inputLabelAndField field="${standardFieldSpecs.drRtoDesc}" value="${applicationInstance.drRtoDesc}" tabindex="46"/>
							</tr>
							<tr>
								<tds:inputLabelAndField field="${standardFieldSpecs.license}" value="${applicationInstance.license}" tabindex="17"/>
								
								<tds:inputLabel field="${standardFieldSpecs.retireDate}"/>
								<td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'retireDate','errors')}">
								    <script type="text/javascript" charset="utf-8">
										jQuery(function($){ $(".dateRange").kendoDatePicker({ animation: false, format:tdsCommon.kendoDateFormat()  }); });
									</script>
									<input type="text" class="dateRange ${standardFieldSpecs.retireDate.imp?:''}" size="15" style="width: 138px;" name="retireDate" id="retireDate" tabindex="27"
									value="<tds:convertDate date="${applicationInstance?.retireDate}" />" /> 
								</td>
								
								<tds:inputLabel field="${standardFieldSpecs.validation}"/>
								<td>
									<g:select  id="validation" class="${standardFieldSpecs.validation.imp?:''}" name="validation" from="${applicationInstance.constraints.validation.inList }" 
										onChange="assetFieldImportance(this.value,'Application');highlightCssByValidation(this.value,'Application','');"  value="" tabindex="37" />
								</td>
								
								<tds:inputLabel field="${standardFieldSpecs.testProc}"/>
								<td>
									<g:select  id="testProc"	class="${standardFieldSpecs.testProc.imp?:''} ynselect" name="testProc"  from="${['Y', 'N']}" value="?"
		                                 noSelection="['':'?']" tabindex="47" />
								</td>
							</tr>
							<tr>
							    <td></td>
							    <td></td>
							    
								<tds:inputLabel field="${standardFieldSpecs.maintExpDate}"/>
								<td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'maintExpDate','errors')}">
									<input type="text" class="dateRange ${standardFieldSpecs.maintExpDate.imp?:''}" 
										size="15" style="width: 138px;"
										name="maintExpDate" id="maintExpDate" tabindex="28" 
										value="<tds:convertDate date="${applicationInstance?.maintExpDate}"  />" /> 
								</td>

								<tds:inputLabel field="${standardFieldSpecs.latency}"/>
								<td>
									<g:select  id="latency" class="${standardFieldSpecs.latency.imp?:''} ynselect"	name="latency"  from="${['Y', 'N']}" value="?"
		                                 noSelection="['':'?']" tabindex="38" />
								</td>
								
								<tds:inputLabel field="${standardFieldSpecs.startupProc}"/>
								<td>
									<g:select  id="startupProc" class="${standardFieldSpecs.startupProc.imp?:''} ynselect" name="startupProc" from="${['Y', 'N']}" value="?"
		                                 noSelection="['':'?']" tabindex="48" />
								</td>
								
							</tr>
							<tr>
								<tds:inputLabelAndField field="${standardFieldSpecs.url}" value="${applicationInstance.url}" tabindex="18"/>

								<tds:inputLabelAndField field="${standardFieldSpecs.externalRefId}" value="${applicationInstance.externalRefId}" tabindex="28"/>
								
								<td class="label ${standardFieldSpecs.shutdownBy.imp?:''}" nowrap="nowrap"><label for="shutdownBy">${standardFieldSpecs.shutdownBy.label}</label></td>
								<td colspan="1" nowrap="nowrap" data-for="shutdownBy" class="${standardFieldSpecs.shutdownBy.imp?:''}" nowrap="nowrap">
								  <g:render template="bySelect" model="[name:'shutdownBy', id:'shutdownById', className:'assetSelect']"></g:render>
									<input type="checkbox" id="shutdownByIdFixed" name="shutdownFixed" value="0"
										onclick="if(this.checked){this.value = 1} else {this.value = 0 }" disabled="disabled"/>Fixed
								</td>
								
								<tds:inputLabel field="${standardFieldSpecs.shutdownDuration}"/>
								<td nowrap="nowrap">
									<input type="text" id="shutdownDuration" name="shutdownDuration"
										value="${applicationInstance.shutdownDuration}" 
										class="${standardFieldSpecs.shutdownDuration.imp?:''}"
										tabindex="55"  size="7"/>m
								</td>
							</tr>
							<tr>
								<tds:inputLabel field="${standardFieldSpecs.startupBy}"/>
								<td colspan="1" nowrap="nowrap" data-for="startupBy" class="${standardFieldSpecs.startupBy.imp?:''}" nowrap="nowrap">
								   <g:render template="bySelect"  model="[name:'startupBy', id:'startupById', className:' assetSelect']"></g:render>
									<input type="checkbox" id="startupByIdFixed"  name="startupFixed" value="0"
										onclick="if(this.checked){this.value = 1} else {this.value = 0 }"  disabled="disabled"/>Fixed
								</td>
								
								<tds:inputLabelAndField field="${standardFieldSpecs.startupDuration}" value="${applicationInstance.startupDuration}" tabindex="29"/>
								
									<tds:inputLabel field="${standardFieldSpecs.testingBy}"/>
								<td colspan="1" nowrap="nowrap" data-for="testingBy" class="${standardFieldSpecs.testingBy.imp?:''}" nowrap="nowrap">
								  <g:render template="bySelect" model="[name:'testingBy', id:'testingById', className:'assetSelect']"></g:render>
									<input type="checkbox" id="testingByIdFixed" name="testingFixed" value="0"  disabled="disabled"
										onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />Fixed
								</td>
								
								<tds:inputLabel field="${standardFieldSpecs.testingDuration}"/>
								<td>
									<input type="text" id="testingDuration" name="testingDuration" class="${standardFieldSpecs.testingDuration.imp?:''}"
											value="${applicationInstance.testingDuration}" tabindex="55" size="7"/>m
								</td>
							</tr>
							<tbody class="customTemplate">
								<g:render template="../assetEntity/customEdit" model="[assetEntityInstance:applicationInstance]"></g:render>
							</tbody>
						</tbody>
					</table>
					
				</div></td>
		</tr>

		<%-- Dependency Edit Block --%>
		<tr>
			<g:render template="../assetEntity/dependentCreateEdit" model="[whom:'create',supportAssets:[],dependentAssets:[]]"></g:render>
		</tr>
		
		<tr>
			<td colspan="2">
				<g:render template="../assetEntity/createButtons" model="[assetClass: applicationInstance.assetClass]"></g:render>
			</td>
		</tr>
	</table>
</g:form>

<script>
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366');
    $('[data-toggle="popover"]').popover();
</script>
<style>
	#select2-drop{ width: 200px !important; } 
</style>
