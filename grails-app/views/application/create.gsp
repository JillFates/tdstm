<script type="text/javascript">
	$("#appl_assetName").val($('#gs_assetName').val())
	$("#appl_sme").val($('#gs_sme').val())
	$("#appl_validation").val($('#gs_validation').val())
	$("#appl_planStatus").val($('#gs_planStatus').val())
	$("#appl_moveBundle").val($('#gs_moveBundle').val())
	$("#createStaffDialog").dialog({ autoOpen: false })
	
	var myOption = "<option value='0'>Add Person...</option>"
	<tds:hasPermission permission='PersonCreateView'>
		$("#sme1 option:first").after(myOption);
		$("#sme2 option:first").after(myOption);
		$("#appOwner option:first").after(myOption);
	</tds:hasPermission>
	if(!isIE7OrLesser)
		$("select.assetSelect").select2();
	
</script>
<g:form method="post" action="save" name="createAssetsFormId" onsubmit="return validateFields('',this.name)">
	<input type="hidden" id="appl_assetName" name="assetNameFilter" value="" />
	<input type="hidden" id="appl_sme" name="appSmeFilter" value="" />
	<input type="hidden" id="appl_validation" name="appValidationFilter" value="" />
	<input type="hidden" id="appl_moveBundle" name="moveBundleFilter" value="" />
	<input type="hidden" id="appl_planStatus" name="planStatusFilter" value="" />

	<table style="border: 0">
		<tr>
			<td colspan="2">
				<div class="dialog">
					<table>
						<tbody>
							<tr>
								<td class="label ${config.assetName}" nowrap="nowrap"><label for="assetName">Name<span style="color: red;">*</span></label></td>
								<td>
									<input type="text" id="assetName" class="${config.assetName}" name="assetName" 
										value="${applicationInstance.assetName}" tabindex="9" /></td>
								<td class="label ${config.description}" nowrap="nowrap"><label for="assetName">Description</label></td>
								<td colspan="3">
									<input type="text" id="description" class="${config.description}" name="description" 
										value="" size="50" tabindex="10" />
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="assetType">Type</label></td>
								<td><input type="text" id="assetType" name="assetType" value="Application" readonly="readonly" /></td>
								<td class="label ${config.supportType}" nowrap="nowrap"><label for="supportType">Support</label>
								</td>
								<td ><input type="text" id="supportType" class="${config.supportType}"
									name="supportType" value="${applicationInstance.supportType}" tabindex="20" />
								</td>
								<td class="label ${config.appFunction}" nowrap="nowrap"><label for="appFunction">Function</label>
								</td>
								<td ><input type="text" id="appFunction" class="${config.appFunction}"
									name="appFunction" value="${applicationInstance.appFunction}"  tabindex="31" />
								</td>
								<td class="label ${config.userCount}" nowrap="nowrap"><label for="userCount">Users</label>
								</td>
								<td>
									<input type="text" id="userId" class="${config.userCount}" name="userCount" 
										value="${applicationInstance.userCount}"  tabindex="41" />
								</td>
							</tr>
							<tr>
								<td class="label ${config.appVendor}" nowrap="nowrap"><label for="appVendor">Vendor</label></td>
								<td ><input type="text" id="appVendor" class="${config.appVendor}"
									name="appVendor" value="${applicationInstance.appVendor}"  tabindex="11" />
								</td>
								<td class="label ${applicationInstance.sme}" nowrap="nowrap"><label for="sme">SME1</label></td>
								<td >
									<g:select from="${personList}" id="sme1" name="sme.id" class="${config.sme} personContact assetSelect"  optionKey="id" 
										optionValue="${{it.lastNameFirst}}" 
										onchange="openPersonDiv(this.value,this.id)" 
										noSelection="${['null':'Please Select']}" 
										tabIndex="21"
									/>
								</td>
								<td class="label ${config.environment}" nowrap="nowrap"><label for="environment">Environment</label>
								</td>
								<td >
									<g:select id="environment" class="${config.environment}" name="environment" 
										from="${environmentOptions}" 
										value="${applicationInstance.environment}" 
										noSelection="${['':' Please Select']}" tabindex="32">
									</g:select>
								</td>
								<td class="label ${config.userLocations}" nowrap="nowrap"><label for="userLocations">User Location</label>
								</td>
								<td ><input type="text" id="userLocations" class="${config.userLocations}"
									name="userLocations"
									value="${applicationInstance.userLocations}" 
									tabindex="42" />
								</td>
							</tr>
							<tr>
								<td class="label ${config.appVersion}" nowrap="nowrap"><label for="appVersion">Version</label>
								</td>
								<td ><input type="text" id="appVersion" class="${config.appVersion}"
									name="appVersion" value="${applicationInstance.appVersion}"  tabindex="12" />
								</td>
								<td class="label ${config.sme2}" nowrap="nowrap"><label for="sme2">SME2</label></td>
								<td class="suffleTd">
								 <img src="../images/swapicon.png" onclick="shufflePerson('sme1','sme2')" class="SuffleImage"  alt="Swap Contacts" title="Swap Contacts"/>
									<g:select from="${personList}" id="sme2" name="sme2.id" class="${config.sme2} suffleSelect personContact assetSelect" optionKey="id" 
										optionValue="${{it.lastNameFirst}}" 
										onchange="openPersonDiv(this.value, this.id)" 
										tabindex="22" 
										noSelection="${['null':'Please select']}"
									/>
								</td>
								<td class="label ${config.criticality}" nowrap="nowrap">
									<label for="criticality">Criticality</label>
								</td>
								<td>
									<g:select id="criticality" class="${config.criticality}" name="criticality" 
										from="${applicationInstance.constraints.criticality.inList}" 
										value="${applicationInstance.criticality}" 
										noSelection="${['':'Please select']}"
										tabindex="33">
									</g:select>
								</td>
								<td class="label ${config.useFrequency}" nowrap="nowrap">
									<label for="useFrequency">Use	Frequency</label>
								</td>
								<td>
									<input type="text" id="useFrequency" name="useFrequency" 
										class="${config.useFrequency}" value="${applicationInstance.useFrequency}" tabindex="43" />
								</td>

							</tr>
							<tr>
								<td class="label ${config.appTech}" nowrap="nowrap"><label for="appTech">Tech.</label></td>
								<td ><input type="text" id="appTech" class="${config.appTech}" name="appTech" 
									value="${applicationInstance.appTech}" tabindex="13" />
								</td>
								<td class="label ${config.owner}" nowrap="nowrap"><label for="appOwner">App Owner</label></td>
								<td class="suffleTd">
								 <img src="../images/swapicon.png" onclick="shufflePerson('sme2','appOwner')" class="SuffleImage" alt="Swap Contacts" title="Swap Contacts"/>
									<g:select from="${personList}" id="appOwner" name="appOwner.id" class="${config.owner} suffleSelect personContact assetSelect" optionKey="id" 
										optionValue="${{it.lastNameFirst}}" 
										onchange="openPersonDiv(this.value, this.id)" 
										tabindex="23" 
										noSelection="${['null':' Please Select']}" 
									/>
								</td>
								<td class="label ${config.moveBundle}" nowrap="nowrap"><label for="moveBundle">Bundle</label></td>
								<td ><g:select from="${moveBundleList}" id="moveBundle" class="${config.moveBundle}" name="moveBundle.id" value="${applicationInstance.moveBundle}" optionKey="id" optionValue="name" tabindex="34" />
								</td>
								<td class="label ${config.drRpoDesc}" nowrap="nowrap"><label for="drRpoDesc">DR RPO</label>
								</td>
								<td ><input type="text" id="drRpoDesc"	class="${config.drRpoDesc}" name="drRpoDesc" value="${applicationInstance.drRpoDesc}" tabindex="44" />
								</td>
							</tr>
							<tr>
								<td class="label ${config.appSource}" nowrap="nowrap"><label for="appSource">Source</label></td>
								<td ><input type="text" id="appSource"	class="${config.appSource}" name="appSource" 
									value="${applicationInstance.appSource}" tabindex="15" />
								</td>
								<td class="label ${config.businessUnit}" nowrap="nowrap"><label for="businessUnit">Bus	Unit</label>
								</td>
								<td ><input type="text" id="businessUnit" class="${config.businessUnit}" name="businessUnit" value="${applicationInstance.businessUnit}" tabindex="24" />
								</td>
								<td class="label ${config.planStatus}" nowrap="nowrap"><label for="planStatus">Plan Status</label>
								</td>
								<td ><g:select from="${planStatusOptions}" id="planStatus" class="${config.planStatus}" name="planStatus" value="${applicationInstance.planStatus}" tabindex="35" />
								</td>
								<td class="label ${config.drRtoDesc}" nowrap="nowrap"><label for="drRtoDesc">DR RTO</label>
								</td>
								<td ><input type="text" id="drRtoDesc"	class="${config.drRtoDesc}" name="drRtoDesc" value="${applicationInstance.drRtoDesc}" tabindex="45" />
								</td>
							</tr>
							<tr>
								<td class="label ${config.license}" nowrap="nowrap"><label for="license">License</label></td>
								<td ><input type="text" id="license" class="${config.license}" name="license" value="${applicationInstance.license}" tabindex="16" />
								</td>
								<td class="label ${config.retireDate}"><label for="retireDate">Retire Date:</label>
								</td>
								<td valign="top"
									class="value ${hasErrors(bean:applicationInstance,field:'retireDate','errors')}">
								    <script type="text/javascript" charset="utf-8">
									jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${resource(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
									</script>
									<input type="text" class="dateRange ${config.retireDate}" size="15" style="width: 112px; height: 14px;" name="retireDate" id="retireDate" tabindex="26"
									value="<tds:convertDate date="${applicationInstance?.retireDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>" /> 
								</td>
								<td class="label ${config.validation}" nowrap="nowrap"><label for="validation">Validation</label>
								</td>
								<td ><g:select  id="validation" class="${config.validation}" name="validation" from="${applicationInstance.constraints.validation.inList }" onChange="assetFieldImportance(this.value,'Application');"  value="" tabindex="36" />
								</td>
								<td class="label ${config.testProc}" nowrap="nowrap"><label for="testProc">Test Proc OK</label>
								</td>
								<td ><g:select  id="testProc"	class="${config.testProc} ynselect" name="testProc"  from="${['Y', 'N']}" value="?"
		                                 noSelection="['':'?']" tabindex="46" />
								</td>
							</tr>
							<tr>
							    <td></td>
							    <td></td>
							    <td  class="label ${config.maintExpDate}"><label for="maintExpDate">Maint Exp.</label></td>
								<td valign="top" class="value ${hasErrors(bean:applicationInstance,field:'maintExpDate','errors')}">
								    <script type="text/javascript" charset="utf-8">
									jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${resource(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
									</script>
									<input type="text" class="dateRange ${config.maintExpDate}" size="15" style="width: 112px; height: 14px;" 
										name="maintExpDate" id="maintExpDate" tabindex="27" 
									value="<tds:convertDate date="${applicationInstance?.maintExpDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />" /> 
								</td>
								<td class="label ${config.latency}" nowrap="nowrap"><label for="latency">Latency OK</label>
								</td>
								<td ><g:select  id="latency" class="${config.latency} ynselect"	name="latency"  from="${['Y', 'N']}" value="?"
		                                 noSelection="['':'?']" tabindex="37" />
								</td>
								<td class="label ${config.startupProc}" nowrap="nowrap"><label for="startupProc">Startup Proc OK</label>
								</td>
								<td ><g:select  id="startupProc" class="${config.startupProc} ynselect" name="startupProc" from="${['Y', 'N']}" value="?"
		                                 noSelection="['':'?']" tabindex="47" />
								</td>
								
							</tr>
							<tr>
								<td class="label ${config.url}" nowrap="nowrap"><label for="license">URL</label></td>
								<td><input type="text" id="url" class="${config.url}" name="url" value="${applicationInstance.url}" tabindex="19" />
								</td>
								<td class="label ${config.externalRefId}" nowrap="nowrap"><label for="externalRefId">External Ref Id</label></td>
								<td>
									<input type="text" id="externalRefId" class="${config.externalRefId}" name="externalRefId" 
										value="${applicationInstance.externalRefId}" tabindex="28" /></td>
								<td class="label ${config.shutdownBy}" nowrap="nowrap"><label for="shutdownBy">Shutdown By</label></td>
								<td colspan="1" nowrap="nowrap">
								  <g:render template="bySelect" model="[name:'shutdownBy', id:'shutdownById', className:config.shutdownBy]"></g:render>
									<input type="checkbox" id="shutdownByIdFixed" name="shutdownFixed" value="0"
										onclick="if(this.checked){this.value = 1} else {this.value = 0 }" disabled="disabled"/>Fixed
								</td>
								<td class="label ${config.shutdownDuration}" nowrap="nowrap"><label for="shutdownDuration">Shutdown Duration </label>
								</td>
								<td ><input type="text" id="shutdownDuration" class="${config.shutdownDuration}" name="shutdownDuration"
											value="${applicationInstance.shutdownDuration}" tabindex="55"  size="7"/>m
								</td>
							</tr>
							<tr>
							<td class="label ${config.startupBy}" nowrap="nowrap"><label for="startupBy">Startup By</label></td>
								<td colspan="1" nowrap="nowrap">
								   <g:render template="bySelect"  model="[name:'startupBy', id:'startupById', className:config.startupBy]"></g:render>
									<input type="checkbox" id="startupByIdFixed"  name="startupFixed" value="0"
										onclick="if(this.checked){this.value = 1} else {this.value = 0 }"  disabled="disabled"/>Fixed
								</td>
								
								<td class="label ${config.startupDuration}" nowrap="nowrap"><label for="startupDuration">Startup Duration </label>
								</td>
								<td ><input type="text" id="startupDuration" name="startupDuration" class="${config.startupDuration}"
											value="${applicationInstance.startupDuration}" tabindex="55" size="7"/>m
								</td>
								
								<td class="label ${config.testingBy}" nowrap="nowrap"><label for="testingBy">Testing By</label></td>
								<td colspan="1" nowrap="nowrap">
								  <g:render template="bySelect" model="[name:'testingBy', id:'testingById', className:config.testingBy]"></g:render>
									<input type="checkbox" id="testingByIdFixed" name="testingFixed" value="0"  disabled="disabled"
										onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />Fixed
								</td>
								<td class="label ${config.shutdownDuration}" nowrap="nowrap"><label for="testingDuration">Testing Duration </label>
								</td>
								<td ><input type="text" id="testingDuration" name="testingDuration" class="${config.shutdownDuration}"
											value="${applicationInstance.testingDuration}" tabindex="55" size="7"/>m
								</td>
							</tr>
							<tbody class="customTemplate">
								<g:render template="../assetEntity/customEdit" model="[assetEntityInstance:applicationInstance]"></g:render>
							</tbody>
							
							<tr>
								<td class="label" nowrap="nowrap"><label for="events">Event</label>
								</td>
								<td colspan="7"><g:each in="${moveEventList}" var="moveEvent">
										<div class="label"
											style="float: left; width: auto; padding: 5px;">
											<label for="moveEvent"><b>${moveEvent.name}</b>
											</label> <label for="moveEvent"> <g:select id="okToMove_${moveEvent.id}" class="ynselect" name="okToMove_${moveEvent.id}" from="${['Y', 'N']}" 
											value="?" noSelection="['':'?']" />
											</label>
										</div>
									</g:each></td>
							</tr>
						</tbody>
					</table>
					
				</div></td>
		</tr>
		<tr>
			<g:render template="../assetEntity/dependent" model="[whom:'create',supportAssets:[],dependentAssets:[]]"></g:render>
		</tr>
		<tr>
			<td colspan="2">
				<input name="dependentCount" id="create_dependentCount" type="hidden" value="0" />
				<input name="supportCount"  id="create_supportCount" type="hidden" value="0" />
				<input name="attributeSet.id" type="hidden" value="1" />
				<input name="project.id" type="hidden" value="${projectId}" />
				<input type="hidden" id="create_supportAddedId" name="addedSupport" value ="0"/>
				<input type="hidden" id="create_dependentAddedId" name="addedDep" value ="0"/>
				<input name="showView" id="showView" type="hidden" value=""/>
				<g:render template="../assetEntity/createButtons" model="[whom:'Application']"></g:render>
			</td>
		</tr>
	</table>
</g:form>

<script>
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')
	
</script>