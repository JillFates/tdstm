<%@page import="com.tds.asset.Application;"%>
<g:form method="post">
	<input type="hidden" name="id" value="${applicationInstance?.id}" />
	<table style="border: 0">
		<tr>
			<td colspan="2">
				<div class="dialog">
					<table>
						<tbody>
							<tr>
								<td class="label" nowrap="nowrap"><label for="assetName">App Name</label></td>
								<td ><input type="text" id="assetName" name="assetName" value="${applicationInstance.assetName}" tabindex="11"/></td>
								<td class="label" nowrap="nowrap">Description</td>
								<td colspan="3"><input type="text" id="description" name="description" value="" size="50" tabindex="21"/>
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="assetType">Type</label></td>
								<td ><input type="text" id="assetType" name="assetType" value="Application" readonly="readonly"/></td>
								<td class="label" nowrap="nowrap"><label for="supportType">Support</label>
								</td>
								<td ><input type="text" id="supportType"
									name="supportType" value="${applicationInstance.supportType}"  tabindex="22"/>
								</td>
								<td class="label" nowrap="nowrap"><label for="appFunction">Function</label>
								</td>
								<td ><input type="text" id="appFunction"
									name="appFunction" value="${applicationInstance.appFunction}"  tabindex="31"/>
								</td>
								<td class="label" nowrap="nowrap"><label for="userConcurrent">Users</label>
								</td>
								<td ><input type="text" id="userId" name="userCount" value="${applicationInstance.userCount}"  tabindex="41"/>
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="appVendor">Vendor</label></td>
								<td ><input type="text" id="appVendor"
									name="appVendor" value="${applicationInstance.appVendor}"  tabindex="13"/>
								</td>
								<td class="label" nowrap="nowrap"><label for="sme">SME1</label></td>
								<td ><input type="text" id="sme" name="sme"
									value="${applicationInstance.sme}"  tabindex="23"/>
								</td>
								<td class="label" nowrap="nowrap"><label for="environment">Environment</label>
								</td>
								<td ><g:select id="environment" name="environment" from="${applicationInstance.constraints.environment.inList}" value="${applicationInstance.environment}"  tabindex="32"></g:select>
								</td>
								<td class="label" nowrap="nowrap"><label for="userLocations">User Location</label>
								</td>
								<td ><input type="text" id="userLocations"
									name="userLocations"
									value="${applicationInstance.userLocations}"  tabindex="42"/>
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="appVersion">Version</label>
								</td>
								<td ><input type="text" id="appVersion"
									name="appVersion" value="${applicationInstance.appVersion}"  tabindex="14"/>
								</td>
								<td class="label" nowrap="nowrap"><label for="sme2">SME2</label></td>
								<td ><input type="text" id="sme2" name="sme2"
									value="${applicationInstance.sme2}"  tabindex="24"/>
								</td>
								<td class="label" nowrap="nowrap"><label for="criticality">Criticality</label>
								</td>
								<td ><g:select id="criticality" name="criticality" from="${applicationInstance.constraints.criticality.inList}" value="${applicationInstance.criticality}"  tabindex="33"></g:select></td>
								<td class="label" nowrap="nowrap"><label for="userConcurrent">Concurrent</label>
								</td>
								<td ><input type="text" id="userConcurrent"	name="userConcurrent" value="${applicationInstance.userConcurrent}"  tabindex="43" />
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="appTech">Tech.</label></td>
								<td ><input type="text" id="appTech" name="appTech" value="${applicationInstance.appTech}" tabindex="15" />
								</td>
								<td class="label" nowrap="nowrap"><label for="businessUnit">Bus	Unit</label>
								</td>
								<td ><input type="text" id="businessUnit" name="businessUnit" value="${applicationInstance.businessUnit}" tabindex="25" />
								</td>
								<td class="label" nowrap="nowrap"><label for="moveBundle">Bundle</label></td>
								<td ><g:select from="${moveBundleList}" id="moveBundle" name="moveBundle.id" value="${applicationInstance.moveBundle}" optionKey="id" optionValue="name" tabindex="34" />
								</td>
								<td class="label" nowrap="nowrap"><label for="useFrequency">Use	Frequency</label>
								</td>
								<td ><input type="text" id="useFrequency" name="useFrequency" value="${applicationInstance.userConcurrent}" tabindex="44" />
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="appSource">Source</label></td>
								<td ><input type="text" id="appSource"	name="appSource" value="${applicationInstance.appSource}" tabindex="17" />
								</td>
								<td class="label" nowrap="nowrap"><label for="appOwner">App Owner</label></td>
								<td ><input type="text" id="appOwner" name="appOwner"	value="${applicationInstance.appOwner}" tabindex="26" />
								</td>
								<td class="label" nowrap="nowrap"><label for="planStatus">Plan Status</label>
								</td>
								<td ><g:select from="${planStatusOptions}" id="planStatus" name="planStatus" value="${applicationInstance.planStatus}" tabindex="35" />
								</td>
								<td class="label" nowrap="nowrap"><label for="drRpoDesc">DR RPO</label>
								</td>
								<td ><input type="text" id="drRpoDesc"	name="drRpoDesc" value="${applicationInstance.drRpoDesc}" tabindex="45" />
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="license">License</label></td>
								<td ><input type="text" id="license" name="license" value="${applicationInstance.license}" tabindex="18" />
								</td>
								<td class="label" nowrap="nowrap">Retire</td>
								<td ><script type="text/javascript">
				                    $(document).ready(function(){
				                      $("#retireDate").datetimepicker();
				                    });
								</script> 
								<input type="text" class="dateRange" size="15" style="width: 132px; height: 14px;" id="retireDate" name="retireDate"
                                   value="<tds:convertDateTime date="${applicationInstance?.retireDate}" formate="12hrs" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>" 
                                   onchange="isValidDate(this.value)" tabindex="27" />
								</td>
								<td class="label" nowrap="nowrap">Maint Exp.</td>
								<td >
								<script type="text/javascript">
				                    $(document).ready(function(){
				                      $("#maintExpDate").datetimepicker();
				                    });
								</script> 
								<input type="text" class="dateRange" size="15" style="width: 132px; height: 14px;" id="maintExpDate" name="maintExpDate"
                                   value="<tds:convertDateTime date="${applicationInstance?.maintExpDate}" formate="12hrs" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>" 
                                   onchange="isValidDate(this.value)" tabindex="28" />
								</td>
								<td class="label" nowrap="nowrap"><label for="drRtoDesc">DR RTO</label>
								</td>
								<td ><input type="text" id="drRtoDesc"	name="drRtoDesc" value="${applicationInstance.drRtoDesc}" tabindex="46" />
								</td>
							</tr>
						</tbody>
					</table>
				</div></td>
		</tr>
		<tr>
			<td valign="top">
				<div style="width: 400px;">
					<span style="float: left;"><h1>Supports:</h1></span>
					<span style="float: right;"><input type='button' value='Add' onclick="addAssetDependency('support')"></span>
					<br/>
					<table style="width: 100%;">
						<thead>
							<tr>
								<th>Frequency</th>
								<th>Asset</th>
								<th>Type</th>
								<th>Status</th>
								<th>&nbsp;</th>
							</tr>
						</thead>
						<tbody id="createSupportsList">
							<g:each in="${supportAssets}" var="support" status="i">
								<tr id='row_s_${i}'>
									<td><g:select name="dataFlowFreq_support_${i}" value="${support.dataFlowFreq}" from="${support.constraints.dataFlowFreq.inList}" /></td>
									<td><g:select name="asset_support_${i}" from="${com.tds.asset.Application.findAllByAssetType('application')}" value="${support?.asset?.id}" optionKey="id" optionValue="assetName"></g:select></td>
									<td><g:select name="dtype_support_${i}" value="${support.type}" from="${support.constraints.type.inList}" />
									</td>
									<td><g:select name="status_support_${i}" value="${support.status}"	from="${support.constraints.status.inList}" />
									</td>
									<td><a href="javascript:deleteRow('row_s_${i}')"><span class='clear_filter'><u>X</u></span></a></td>
								</tr>
							</g:each>
						</tbody>
					</table>
				</div></td>
			<td valign="top">
				<div style="width: 400px;">
					<span style="float: left;"><h1>Is dependent on:</h1></span>
					<span style="float: right;"><input type='button' value='Add' onclick="addAssetDependency('dependent')"></span>
					<br/>
					<table style="width: 100%;">
						<thead>
							<tr>
								<th>Frequency</th>
								<th>Asset</th>
								<th>Type</th>
								<th>Status</th>
								<th>&nbsp;</th>
							</tr>
						</thead>
						<tbody id="createDependentsList">
						<g:each in="${dependentAssets}" var="dependent" status="i">
							<tr id='row_d_${i}'>
								<td><g:select name="dataFlowFreq_dependent_${i}" value="${dependent.dataFlowFreq}" from="${dependent.constraints.dataFlowFreq.inList}" /></td>
								<td><g:select name="asset_dependent_${i}" from="${com.tds.asset.Application.findAllByAssetType('application')}" value="${dependent?.dependent?.id}" optionKey="id" optionValue="assetName"></g:select></td>
								<td><g:select name="dtype_dependent_${i}" value="${dependent.type}" from="${dependent.constraints.type.inList}" />
								</td>
								<td><g:select name="status_dependent_${i}" value="${dependent.status}" from="${dependent.constraints.status.inList}" />
								</td>
								<td><a href="javascript:deleteRow('row_d_${i}')"><span class='clear_filter'><u>X</u></span></a></td>
							</tr>
						</g:each>
						</tbody>
					</table>
				</div></td>
		</tr>
		<tr>
			<td colspan="2">
				<div class="buttons">
					<input name="dependentCount" id="dependentCount" type="hidden" value="${dependentAssets.size()}">
					<input  name="supportCount"  id="supportCount" type="hidden" value="${supportAssets.size()}">
					<span class="button"><g:actionSubmit class="save" value="Update" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </div></td>
		</tr>
	</table>
</g:form>
