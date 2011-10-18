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
								<td colspan="3"><input type="text" id="description" name="description"value="This Application Support the XYZ Business" size="50" tabindex="21"/>
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="assetType">App Type</label></td>
								<td ><g:select from="${assetTypeOptions}" id="assetType" name="assetType" value="${applicationInstance.assetType}"  tabindex="12"/></td>
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
			<td>
				<div>
					<h1>Supports:</h1>
					<table style="width: 400px;">
						<thead>
							<tr>
								<th>Frequency</th>
								<th>Asset</th>
								<th>Type</th>
								<th>Status</th>
							</tr>
						</thead>
						<tbody>
							<g:each in="${supportAssets}" var="support">
								<tr>
									<td>${support?.dataFlowFreq}</td>
									<td>${support?.asset}</td>
									<td><g:select id="selectId" value=${support.type}
											from="${AssetDependency.constraints.type.inList}" />
									</td>
									<td><g:select id="typeId" value=${support.status}
											from="${AssetDependency.constraints.status.inList}" />
									</td>
								</tr>
							</g:each>
						</tbody>
					</table>
				</div></td>
			<td>
				<div>
					<h1>Is dependent on:</h1>
					<table style="width: 400px;">
						<thead>
							<tr>
								<th>Frequency</th>
								<th>Asset</th>
								<th>Type</th>
								<th>Status</th>
							</tr>
						</thead>
						<tbody>
						<g:each in="${supportAssets}" var="support">
							<tr>
								<td>${support?.dataFlowFreq}</td>
								<td>${support?.dependent}</td>
								<td><g:select id="selectId" value=${support.type}
										from="${AssetDependency.constraints.type.inList}" />
								</td>
								<td><g:select id="typeId" value=${support.status}
										from="${AssetDependency.constraints.status.inList}" />
								</td>
							</tr>
						</g:each>
						</tbody>
					</table>
				</div></td>
		</tr>
		<tr>
			<td colspan="2">
				<div class="buttons">
					<span class="button"><g:actionSubmit class="save" value="Update" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </div></td>
		</tr>
	</table>
</g:form>
