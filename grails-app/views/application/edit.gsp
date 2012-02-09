<g:form method="post">
	<input type="hidden" name="id" value="${applicationInstance?.id}" />
	<table style="border: 0">
		<tr>
			<td colspan="2">
				<div class="dialog">
					<table>
						<tbody>
							<tr>
								<td class="label" nowrap="nowrap"><label for="assetName">Name<span style="color: red;">*</span></label></td>
								<td ><input type="text" id="assetName" name="assetName" value="${applicationInstance.assetName}" tabindex="11" /></td>
								<td class="label" nowrap="nowrap">Description</td>
								<td colspan="3"><input type="text" id="description" name="description" value="${applicationInstance.description}" size="50" tabindex="21" />
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="assetType">Type</label></td>
								<td ><input type="text" id="assetType" name="assetType" value="Application" readonly="readonly" /></td>
								<td class="label" nowrap="nowrap"><label for="supportType">Support</label>
								</td>
								<td ><input type="text" id="supportType"
									name="supportType" value="${applicationInstance.supportType}"  tabindex="22" />
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
									name="appVendor" value="${applicationInstance.appVendor}"  tabindex="13" />
								</td>
								<td class="label" nowrap="nowrap"><label for="sme">SME1</label></td>
								<td ><input type="text" id="sme" name="sme"
									value="${applicationInstance.sme}"  tabindex="23" />
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
									name="appVersion" value="${applicationInstance.appVersion}"  tabindex="14" />
								</td>
								<td class="label" nowrap="nowrap"><label for="sme2">SME2</label></td>
								<td ><input type="text" id="sme2" name="sme2"
									value="${applicationInstance.sme2}"  tabindex="24" />
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
								<td ><g:select from="${moveBundleList}" id="moveBundle" name="moveBundle.id" value="${applicationInstance.moveBundle?.id}" optionKey="id" optionValue="name" tabindex="34" />
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
								<td class="label" nowrap="nowrap"><label for="custom1">Custom 1</label></td>
								<td ><input type="text" id="custom1"	name="custom1" value="${applicationInstance.custom1}"  />
								</td>
								<td class="label" nowrap="nowrap"><label for="custom1">Custom 2</label></td>
								<td ><input type="text" id="custom2"	name="custom2" value="${applicationInstance.custom2}"  />
								</td>
								<td class="label" nowrap="nowrap"><label for="custom3">Custom 3</label></td>
								<td ><input type="text" id="custom3"	name="custom3" value="${applicationInstance.custom3}"  />
								</td>
								<td class="label" nowrap="nowrap"><label for="custom4">Custom 4</label></td>
								<td ><input type="text" id="custom4"	name="custom4" value="${applicationInstance.custom4}"  />
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="custom5">Custom 5</label></td>
								<td ><input type="text" id="custom5"	name="custom5" value="${applicationInstance.custom5}"/>
								</td>
								<td class="label" nowrap="nowrap"><label for="custom6">Custom 6</label></td>
								<td ><input type="text" id="custom6"	name="custom6" value="${applicationInstance.custom6}"  />
								</td>
								<td class="label" nowrap="nowrap"><label for="custom7">Custom 7</label></td>
								<td ><input type="text" id="custom7"	name="custom7" value="${applicationInstance.custom7}" />
								</td>
								<td class="label" nowrap="nowrap"><label for="custom8">Custom 8</label></td>
								<td ><input type="text" id="custom8"	name="custom8" value="${applicationInstance.custom8}"  />
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
					<span style="float: right;"><input type='button' value='Add' onclick="addAssetDependency('support')"></span>
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
							<g:each in="${supportAssets}" var="support" status="i">
								<tr id='row_s_${i}'>
									<td><g:select name="dataFlowFreq_support_${i}" value="${support.dataFlowFreq}" from="${support.constraints.dataFlowFreq.inList}" /></td>
									<td><g:select name="entity_support_${i}" from="['Server','Application','Database','Files']" onchange='updateAssetsList(this.name, this.value)' value="${support?.asset?.assetType}"></g:select></td>
									<td><g:select name="asset_support_${i}" from="${com.tds.asset.AssetEntity.findAllByAssetTypeAndProject(support?.asset?.assetType, project)}" value="${support?.asset?.id}" optionKey="id" optionValue="assetName"  style="width:90px;"></g:select></td>
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
						<g:each in="${dependentAssets}" var="dependent" status="i">
							<tr id='row_d_${i}'>
								<td><g:select name="dataFlowFreq_dependent_${i}" value="${dependent.dataFlowFreq}" from="${dependent.constraints.dataFlowFreq.inList}" /></td>
								<td><g:select name="entity_dependent_${i}" from="['Server','Application','Database','Files']" onchange='updateAssetsList(this.name, this.value)' value="${dependent?.dependent?.assetType}"></g:select></td>
								<td><g:select name="asset_dependent_${i}" from="${com.tds.asset.AssetEntity.findAllByAssetTypeAndProject(dependent?.dependent?.assetType, project)}" value="${dependent?.dependent?.id}" optionKey="id" optionValue="assetName"  style="width:90px;"></g:select></td>
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
					<input name="dependentCount" id="dependentCount" type="hidden" value="${dependentAssets.size()}" />
					<input name="supportCount"  id="supportCount" type="hidden" value="${supportAssets.size()}" />
					<input name="redirectTo" type="hidden" value="${redirectTo}">
					<span class="button"><g:actionSubmit class="save" value="Update" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </div></td>
		</tr>
	</table>
</g:form>
<script>
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')
</script>
