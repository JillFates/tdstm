<g:form method="post">
	<table style="border: 0">
		<tr>
			<td colspan="2">
				<div class="dialog">
					<table>
						<tbody>
							<tr>
								<td class="label" nowrap="nowrap"><label for="assetName">Name<span style="color: red;">*</span></label></td>
								<td ><input type="text" id="assetName" name="assetName" value="${applicationInstance.assetName}" tabindex="10" /></td>
								<td class="label" nowrap="nowrap">Description</td>
								<td colspan="3"><input type="text" id="description" name="description" value="" size="50" tabindex="20" />
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="assetType">Type</label></td>
								<td ><input type="text" id="assetType" name="assetType" value="Application" readonly="readonly" /></td>
								<td class="label" nowrap="nowrap"><label for="supportType">Support</label>
								</td>
								<td ><input type="text" id="supportType"
									name="supportType" value="${applicationInstance.supportType}"  tabindex="21" />
								</td>
								<td class="label" nowrap="nowrap"><label for="appFunction">Function</label>
								</td>
								<td ><input type="text" id="appFunction"
									name="appFunction" value="${applicationInstance.appFunction}"  tabindex="31" />
								</td>
								<td class="label" nowrap="nowrap"><label for="userConcurrent">Users</label>
								</td>
								<td ><input type="text" id="userId" name="userCount" value="${applicationInstance.userCount}"  tabindex="41" />
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="appVendor">Vendor</label></td>
								<td ><input type="text" id="appVendor"
									name="appVendor" value="${applicationInstance.appVendor}"  tabindex="11" />
								</td>
								<td class="label" nowrap="nowrap"><label for="sme">SME1</label></td>
								<td ><input type="text" id="sme" name="sme"
									value="${applicationInstance.sme}"  tabindex="22" />
								</td>
								<td class="label" nowrap="nowrap"><label for="environment">Environment</label>
								</td>
								<td ><g:select id="environment" name="environment" from="${applicationInstance.constraints.environment.inList}" value="${applicationInstance.environment}"  tabindex="32"></g:select>
								</td>
								<td class="label" nowrap="nowrap"><label for="userLocations">User Location</label>
								</td>
								<td ><input type="text" id="userLocations"
									name="userLocations"
									value="${applicationInstance.userLocations}"  tabindex="42" />
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="appVersion">Version</label>
								</td>
								<td ><input type="text" id="appVersion"
									name="appVersion" value="${applicationInstance.appVersion}"  tabindex="13" />
								</td>
								<td class="label" nowrap="nowrap"><label for="sme2">SME2</label></td>
								<td ><input type="text" id="sme2" name="sme2"
									value="${applicationInstance.sme2}"  tabindex="23" />
								</td>
								<td class="label" nowrap="nowrap"><label for="criticality">Criticality</label>
								</td>
								<td ><g:select id="criticality" name="criticality" from="${applicationInstance.constraints.criticality.inList}" value="${applicationInstance.criticality}"  tabindex="33"></g:select></td>
								<td class="label" nowrap="nowrap"><label for="useFrequency">Use	Frequency</label>
								</td>
								<td ><input type="text" id="useFrequency" name="useFrequency" value="${applicationInstance.userConcurrent}" tabindex="43" />
								</td>

							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="appTech">Tech.</label></td>
								<td ><input type="text" id="appTech" name="appTech" value="${applicationInstance.appTech}" tabindex="14" />
								</td>
								<td class="label" nowrap="nowrap"><label for="businessUnit">Bus	Unit</label>
								</td>
								<td ><input type="text" id="businessUnit" name="businessUnit" value="${applicationInstance.businessUnit}" tabindex="24" />
								</td>
								<td class="label" nowrap="nowrap"><label for="moveBundle">Bundle</label></td>
								<td ><g:select from="${moveBundleList}" id="moveBundle" name="moveBundle.id" value="${applicationInstance.moveBundle}" optionKey="id" optionValue="name" tabindex="34" />
								</td>
								<td class="label" nowrap="nowrap"><label for="drRpoDesc">DR RPO</label>
								</td>
								<td ><input type="text" id="drRpoDesc"	name="drRpoDesc" value="${applicationInstance.drRpoDesc}" tabindex="44" />
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="appSource">Source</label></td>
								<td ><input type="text" id="appSource"	name="appSource" value="${applicationInstance.appSource}" tabindex="15" />
								</td>
								<td class="label" nowrap="nowrap"><label for="appOwner">App Owner</label></td>
								<td ><input type="text" id="appOwner" name="appOwner"	value="${applicationInstance.appOwner}" tabindex="25" />
								</td>
								<td class="label" nowrap="nowrap"><label for="planStatus">Plan Status</label>
								</td>
								<td ><g:select from="${planStatusOptions}" id="planStatus" name="planStatus" value="${applicationInstance.planStatus}" tabindex="35" />
								</td>
								<td class="label" nowrap="nowrap"><label for="drRtoDesc">DR RTO</label>
								</td>
								<td ><input type="text" id="drRtoDesc"	name="drRtoDesc" value="${applicationInstance.drRtoDesc}" tabindex="45" />
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="license">License</label></td>
								<td ><input type="text" id="license" name="license" value="${applicationInstance.license}" tabindex="16" />
								</td>
								<td class="label"><label for="retireDate">Retire Date:</label>
								</td>
								<td valign="top"
									class="value ${hasErrors(bean:applicationInstance,field:'retireDate','errors')}">
								    <script type="text/javascript" charset="utf-8">
									jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
									</script>
									<input type="text" class="dateRange" size="15" style="width: 112px; height: 14px;" name="retireDate" id="retireDate" tabindex="26"
									value="<tds:convertDate date="${applicationInstance?.retireDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>" /> 
								</td>
								<td class="label" nowrap="nowrap"><label for="testProcs">Validation</label>
								</td>
								<td ><g:select  id="validation"	name="validation" from="${applicationInstance.constraints.validation.inList }"  value="" tabindex="36" />
								</td>
								<td class="label" nowrap="nowrap"><label for="testProcs">Test Procs</label>
								</td>
								<td ><g:select  id="testProc"	name="testProc"  from="${['Y', 'N']}" value="?"
		                                 noSelection="['':'?']" tabindex="46" />
								</td>
							</tr>
							<tr>
							    <td></td>
							    <td></td>
							    <td  class="label"><label for="maintExpDate">Maint Exp.</label></td>
								<td valign="top"
										class="value ${hasErrors(bean:applicationInstance,field:'maintExpDate','errors')}">
								    <script type="text/javascript" charset="utf-8">
									jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
									</script>
									<input type="text" class="dateRange" size="15" style="width: 112px; height: 14px;" name="maintExpDate" id="maintExpDate" tabindex="27" 
									value="<tds:convertDate date="${applicationInstance?.maintExpDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />" /> 
								</td>
								<td class="label" nowrap="nowrap"><label for="latency">Latency</label>
								</td>
								<td ><g:select  id="latency"	name="latency"  from="${['Y', 'N']}" value="?"
		                                 noSelection="['':'?']" tabindex="37" />
								</td>
								<td class="label" nowrap="nowrap"><label for="startupProc">Startup Procs</label>
								</td>
								<td ><g:select  id="testProcs"	name="startupProc" from="${['Y', 'N']}" value="?"
		                                 noSelection="['':'?']" tabindex="47" />
								</td>
								
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="custom1">${project?.custom1 ?: 'Custom1'} </label></td>
								<td ><input type="text" id="custom1"	name="custom1" value="${applicationInstance.custom1}" tabindex="17" />
								</td>
								<td class="label" nowrap="nowrap"><label for="custom1">${project?.custom2 ?: 'Custom2'}</label></td>
								<td ><input type="text" id="custom2"	name="custom2" value="${applicationInstance.custom2}"  tabindex="28"/>
								</td>
								<td class="label" nowrap="nowrap"><label for="custom3">${project?.custom3 ?: 'Custom3'}</label></td>
								<td ><input type="text" id="custom3"	name="custom3" value="${applicationInstance.custom3}"  tabindex="38"  />
								</td>
								<td class="label" nowrap="nowrap"><label for="custom4">${project?.custom4 ?: 'Custom4'}</label></td>
								<td ><input type="text" id="custom4"	name="custom4" value="${applicationInstance.custom4}" tabindex="48" />
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="custom5">${project?.custom5 ?: 'Custom5'}</label></td>
								<td ><input type="text" id="custom5"	name="custom5" value="${applicationInstance.custom5}" tabindex="18"/>
								</td>
								<td class="label" nowrap="nowrap"><label for="custom6">${project?.custom6 ?: 'Custom6'}</label></td>
								<td ><input type="text" id="custom6"	name="custom6" value="${applicationInstance.custom6}"  tabindex="29"/>
								</td>
								<td class="label" nowrap="nowrap"><label for="custom7">${project?.custom7 ?: 'Custom7'}</label></td>
								<td ><input type="text" id="custom7"	name="custom7" value="${applicationInstance.custom7}" tabindex="39" />
								</td>
								<td class="label" nowrap="nowrap"><label for="custom8">${project?.custom8 ?: 'Custom8'}</label></td>
								<td ><input type="text" id="custom8"	name="custom8" value="${applicationInstance.custom8}" tabindex="49"  />
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="license">URL</label></td>
								<td colspan="7"><input type="text" id="url" name="url" value="${applicationInstance.url}" size=50 tabindex="19" />
								</td>
							</tr>
						</tbody>
					</table>
				</div></td>
		</tr>
		<tr>
			<td valign="top">
				<div style="width: auto;">
					<span style="float: left;"><h1>Supports:</h1></span>
					<span style="float: right;"><input type='button' value='Add' onclick="addAssetDependency('support')" /></span>
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
						</tbody>
					</table>
				</div></td>
			<td valign="top">
				<div style="width: auto;">
					<span style="float: left;"><h1>Is dependent on:</h1></span>
					<span style="float: right;"><input type='button' value='Add' onclick="addAssetDependency('dependent')" /></span>
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
						</tbody>
					</table>
				</div></td>
		</tr>
		<tr>
			<td colspan="2">
				<div class="buttons">
					<input name="dependentCount" id="dependentCount" type="hidden" value="0" />
					<input  name="supportCount"  id="supportCount" type="hidden" value="0" />
					<input name="attributeSet.id" type="hidden" value="1" />
					<input name="project.id" type="hidden" value="${projectId}" />
					<span class="button"><g:actionSubmit class="save" value="Save" /> </span>
				</div></td>
		</tr>
	</table>
</g:form>
<script>
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')
</script>
